package com.cheffoafk.voiceassistant.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cheffoafk.voiceassistant.data.FrequencyAutocompleteEngine
import com.cheffoafk.voiceassistant.data.PhraseDataSource
import com.cheffoafk.voiceassistant.data.SuggestionUsageDataSource
import com.cheffoafk.voiceassistant.data.UserProfileDataSource
import com.cheffoafk.voiceassistant.domain.DEFAULT_PRESET_PHRASES
import com.cheffoafk.voiceassistant.domain.UserProfile
import com.cheffoafk.voiceassistant.ui.common.AppLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/** Direzione della scansione automatica. */
enum class ScanDirection { UP, DOWN }

private const val TAG = "HomeScreenViewModel"

data class HomeScreenUiState(
    val allPhrases: List<String> = emptyList(),
    val customPhrases: List<String> = emptyList(),
    val inputText: String = "",
    val suggestions: List<String> = emptyList(),
    val customPhraseInput: String = "",
    val isScanningMode: Boolean = false,
    val scanningIndex: Int = 0,
    val scanningIntervalSeconds: Int = 4,
    val userProfile: UserProfile = UserProfile(),
    val scanDirection: ScanDirection = ScanDirection.DOWN
)

class HomeScreenViewModel(
    private val phraseRepository: PhraseDataSource,
    private val userProfileRepository: UserProfileDataSource,
    dictionary: List<String>,
    private val suggestionUsageDataSource: SuggestionUsageDataSource
) : ViewModel() {

    private val autocompleteEngine = FrequencyAutocompleteEngine(dictionary)
    private val scanningController = ScanningController()

    private val _inputText         = MutableStateFlow("")
    private val _customPhraseInput = MutableStateFlow("")
    private val _isScanningMode    = MutableStateFlow(false)
    private val _scanningIndex     = MutableStateFlow(0)
    private val _scanDirection     = MutableStateFlow(ScanDirection.DOWN)
    private val _userProfile       = MutableStateFlow(userProfileRepository.getUserProfile())
    private val suggestionUsageCountsFlow = suggestionUsageDataSource.usageCounts

    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    /**
     * Emette la frase da riprodurre quando la scansione la seleziona.
     * La UI osserva questo flow con LaunchedEffect e chiama synthesizer.speak().
     */
    private val _speakEffect = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val speakEffect: SharedFlow<String> = _speakEffect.asSharedFlow()

    private var scanningJob: Job? = null

    init {
        viewModelScope.launch {
            try {
                // Deriva l'ordine effettivo delle frasi: usa il custom order se salvato,
                // altrimenti preset + custom phrases.
                val allPhrasesFlow = combine(
                    phraseRepository.customPhrases,
                    phraseRepository.savedOrder
                ) { custom, saved -> saved ?: (DEFAULT_PRESET_PHRASES + custom) }

                val suggestionsFlow = combine(
                    _inputText,
                    suggestionUsageCountsFlow
                ) { input, usageCounts ->
                    if (input.isNotEmpty()) {
                        autocompleteEngine.suggest(input, usageCounts = usageCounts)
                    } else {
                        emptyList()
                    }
                }

                combine(
                    allPhrasesFlow,
                    suggestionsFlow,
                    _isScanningMode,
                    _scanningIndex,
                    _userProfile
                ) { allPhrases, suggestions, isScanning, scanIndex, profile ->
                    HomeScreenUiState(
                        allPhrases = allPhrases,
                        customPhrases = phraseRepository.customPhrases.value,
                        inputText = _inputText.value,
                        suggestions = suggestions,
                        customPhraseInput = _customPhraseInput.value,
                        isScanningMode = isScanning,
                        scanningIndex = scanIndex,
                        scanningIntervalSeconds = profile.scanningIntervalSeconds,
                        userProfile = profile,
                        scanDirection = _scanDirection.value
                    )
                }.collect { _uiState.value = it }
            } catch (_: CancellationException) {
                // Coroutine cancellata dal lifecycle.
            } catch (t: Throwable) {
                AppLogger.error(TAG, "Errore nel combine/collect dello stato", t)
            }
        }
    }

    // ─── Testo & suggerimenti ─────────────────────────────────────────────────

    fun updateInputText(text: String) { _inputText.value = text }

    fun clearInputText() {
        _inputText.value = ""
    }

    fun updateCustomPhraseInput(text: String) {
        _customPhraseInput.value = text
        _uiState.value = _uiState.value.copy(customPhraseInput = text)
    }

    fun saveCustomPhrase() {
        val phrase = _customPhraseInput.value.trim()
        if (phrase.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    phraseRepository.addPhrase(phrase)
                    _customPhraseInput.value = ""
                } catch (e: CancellationException) {
                    throw e
                } catch (t: Throwable) {
                    AppLogger.error(TAG, "Errore salvataggio frase personalizzata", t)
                }
            }
        }
    }

    fun registerSuggestionUse(suggestion: String) {
        autocompleteEngine.registerUse(suggestion)
        suggestionUsageDataSource.recordUsage(suggestion)
        _inputText.value = suggestion
    }

    /**
     * Salva il testo corrente del campo di input come frase personalizzata
     * e lo aggiunge in coda all'ordine persistito.
     */
    fun saveInputTextAsPhrase() {
        val phrase = _inputText.value.trim()
        if (phrase.isEmpty()) return
        viewModelScope.launch {
            try {
                persistPhraseInHomeOrder(phrase)
                _inputText.value = ""
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                AppLogger.error(TAG, "Errore salvataggio frase da input", t)
            }
        }
    }

    /**
     * Salva una frase proveniente da una sorgente esterna (es. tastiera sillabica)
     * e la rende disponibile nella HOME mantenendo l'ordine persistito.
     */
    fun saveExternalPhrase(phraseText: String) {
        val phrase = phraseText.trim()
        if (phrase.isEmpty()) return
        viewModelScope.launch {
            try {
                persistPhraseInHomeOrder(phrase)
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                AppLogger.error(TAG, "Errore salvataggio frase esterna", t)
            }
        }
    }

    private fun persistPhraseInHomeOrder(phrase: String) {
        phraseRepository.addPhrase(phrase)
        val currentOrder = phraseRepository.savedOrder.value
        val newOrder = if (currentOrder != null) {
            (currentOrder + phrase).distinct()
        } else {
            (DEFAULT_PRESET_PHRASES + phraseRepository.customPhrases.value).distinct()
        }
        phraseRepository.savePhraseOrder(newOrder)
    }

    /**
     * Sposta la frase all'indice [from] nella posizione [to] nell'elenco corrente
     * e persiste il nuovo ordine.
     */
    fun reorderPhrases(from: Int, to: Int) {
        val current = _uiState.value.allPhrases.toMutableList()
        if (from < 0 || to < 0 || from >= current.size || to >= current.size || from == to) return
        current.add(to, current.removeAt(from))
        phraseRepository.savePhraseOrder(current)
    }

    fun deletePhrase(phraseText: String) {
        val phrase = phraseText.trim()
        if (phrase.isEmpty()) return

        viewModelScope.launch {
            try {
                phraseRepository.removePhrase(phrase)
                val newOrder = _uiState.value.allPhrases.filterNot { it == phrase }
                phraseRepository.savePhraseOrder(newOrder)
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                AppLogger.error(TAG, "Errore cancellazione frase", t)
            }
        }
    }

    fun onPhraseUsed(phrase: String) {
        runCatching { userProfileRepository.recordPhraseUse(phrase) }
            .onFailure { AppLogger.error(TAG, "Errore aggiornamento frequenza frase", it) }
    }

    // ─── Scansione ────────────────────────────────────────────────────────────

    /** Spostamento manuale di una posizione verso il basso (tap singolo su ▼). */
    fun moveSelectionDown() {
        val phrases = _uiState.value.allPhrases
        if (phrases.isEmpty()) return
        _scanningIndex.value = scanningController.moveDown(_scanningIndex.value, phrases.size)
    }

    /** Spostamento manuale di una posizione verso l'alto (tap singolo su ▲). */
    fun moveSelectionUp() {
        val phrases = _uiState.value.allPhrases
        if (phrases.isEmpty()) return
        _scanningIndex.value = scanningController.moveUp(_scanningIndex.value, phrases.size)
    }

    /** Doppio tap su ▼: avvia (o ferma) la scansione verso il basso. */
    fun startDownScanFromCurrentSelection() {
        if (_isScanningMode.value) { stopScanning(); return }
        _scanDirection.value  = ScanDirection.DOWN
        _isScanningMode.value = true
        startScanningLoop()
    }

    /** Doppio tap su ▲: avvia (o ferma) la scansione verso l'alto. */
    fun startUpScanFromCurrentSelection() {
        if (_isScanningMode.value) { stopScanning(); return }
        _scanDirection.value  = ScanDirection.UP
        _isScanningMode.value = true
        startScanningLoop()
    }

    /**
     * Chiamato quando l'utente tocca ovunque sullo schermo DURANTE la scansione.
     * Riproduce la frase attualmente evidenziata e ferma la scansione.
     */
    fun onScreenTap() {
        if (!_isScanningMode.value) return
        val phrases = _uiState.value.allPhrases
        val index = scanningController.normalizeIndex(_scanningIndex.value, phrases.size)
        val phrase = phrases.getOrNull(index)
        stopScanning()
        if (phrase != null) {
            runCatching {
                _speakEffect.tryEmit(phrase)
                userProfileRepository.recordPhraseUse(phrase)
            }.onFailure { AppLogger.error(TAG, "Errore onScreenTap", it) }
        }
    }

    fun stopScanning() {
        scanningJob?.cancel()
        scanningJob = null
        _isScanningMode.value = false
    }

    /**
     * Scansione CONTINUA: avanza automaticamente di una frase ogni N secondi
     * nella direzione configurata. Si ferma solo quando l'utente tocca lo schermo
     * (via onScreenTap) o chiama stopScanning().
     */
    private fun startScanningLoop() {
        scanningJob?.cancel()
        scanningJob = viewModelScope.launch {
            try {
                while (_isScanningMode.value) {
                    val phrases = _uiState.value.allPhrases
                    if (phrases.isEmpty()) {
                        stopScanning()
                        break
                    }
                    delay(_userProfile.value.scanningIntervalSeconds.seconds)
                    if (!_isScanningMode.value) break
                    val nextIndex = scanningController.move(
                        currentIndex = _scanningIndex.value,
                        size = phrases.size,
                        direction = _scanDirection.value
                    )
                    _scanningIndex.value = nextIndex
                }
            } catch (_: CancellationException) {
                // Stop richiesto o lifecycle dispose.
            } catch (t: Throwable) {
                AppLogger.error(TAG, "Errore loop scansione", t)
                stopScanning()
            }
        }
    }

    // ─── Impostazioni ─────────────────────────────────────────────────────────

    fun updateScanningInterval(seconds: Int) {
        runCatching {
            userProfileRepository.updateScanningInterval(seconds)
            _userProfile.value = userProfileRepository.getUserProfile()
            if (_isScanningMode.value) stopScanning()
        }.onFailure { AppLogger.error(TAG, "Errore aggiornamento intervallo scansione", it) }
    }

}

class HomeScreenViewModelFactory(
    private val phraseRepository: PhraseDataSource,
    private val userProfileRepository: UserProfileDataSource,
    private val dictionary: List<String>,
    private val suggestionUsageDataSource: SuggestionUsageDataSource
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        HomeScreenViewModel(
            phraseRepository = phraseRepository,
            userProfileRepository = userProfileRepository,
            dictionary = dictionary,
            suggestionUsageDataSource = suggestionUsageDataSource
        ) as T
}
