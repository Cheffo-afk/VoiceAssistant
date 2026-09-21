package com.cheffoafk.voiceassistant.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cheffoafk.voiceassistant.data.FrequencyAutocompleteEngine
import com.cheffoafk.voiceassistant.domain.AutocompleteEngine
import com.cheffoafk.voiceassistant.domain.SyllabicGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ── UI State ─────────────────────────────────────────────────────────────────

data class SyllabicKeyboardUiState(
    /** Testo composto dalla sequenza di sillabe toccate. */
    val composedText: String = "",
    /** Gruppo consonantico attualmente selezionato; null = nessuno selezionato. */
    val selectedGroup: SyllabicGroup? = null,
    /** Parole suggerite in base al testo composto. */
    val suggestions: List<String> = emptyList()
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class SyllabicKeyboardViewModel(
    private val autocompleteEngine: AutocompleteEngine,
    private val usageTracker: (String) -> Unit = {}
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyllabicKeyboardUiState())
    val uiState: StateFlow<SyllabicKeyboardUiState> = _uiState.asStateFlow()

    /** Seleziona o deseleziona un gruppo consonantico. */
    fun selectGroup(group: SyllabicGroup) {
        _uiState.update { state ->
            val next = if (state.selectedGroup?.label == group.label) null else group
            state.copy(selectedGroup = next)
        }
        refreshSuggestions()
    }

    /** Aggiunge una sillaba (o vocale) al testo composto. */
    fun appendSyllable(syllable: String) {
        _uiState.update { it.copy(composedText = it.composedText + syllable) }
        refreshSuggestions()
    }

    /** Cancella l'ultimo carattere del testo composto. */
    fun deleteLast() {
        _uiState.update { state ->
            if (state.composedText.isNotEmpty())
                state.copy(composedText = state.composedText.dropLast(1))
            else state
        }
        refreshSuggestions()
    }

    /** Inserisce uno spazio. */
    fun appendSpace() {
        _uiState.update { it.copy(composedText = it.composedText + " ") }
        refreshSuggestions()
    }

    /** Sostituisce l'ultima parola composta con una suggestion completa. */
    fun applySuggestion(suggestion: String) {
        _uiState.update { state ->
            val trimmedEnd = state.composedText.trimEnd()
            val prefix = trimmedEnd.substringBeforeLast(' ', "")
            val newText = if (prefix.isBlank()) suggestion else "$prefix $suggestion"
            state.copy(composedText = newText)
        }
        registerSuggestionUse(suggestion)
        refreshSuggestions()
    }

    /** Registra l'uso della parola per migliorare il ranking futuro. */
    fun registerSuggestionUse(word: String) {
        if (word.isBlank()) return
        (autocompleteEngine as? FrequencyAutocompleteEngine)?.registerUse(word)
        usageTracker(word)
    }

    /** Testo della parola corrente su cui basare la predizione. */
    private fun currentWordQuery(text: String): String =
        text.substringAfterLast(' ').trim()

    private fun refreshSuggestions() {
        _uiState.update { state ->
            val query = currentWordQuery(state.composedText)
            state.copy(
                suggestions = if (query.isBlank()) emptyList() else autocompleteEngine.suggest(query)
            )
        }
    }

    /** Svuota completamente il testo e deseleziona il gruppo. */
    fun clear() {
        _uiState.update { SyllabicKeyboardUiState() }
    }
}

class SyllabicKeyboardViewModelFactory(
    private val dictionary: List<String>,
    private val usageTracker: (String) -> Unit = {}
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SyllabicKeyboardViewModel(
            autocompleteEngine = FrequencyAutocompleteEngine(dictionary),
            usageTracker = usageTracker
        ) as T
}

