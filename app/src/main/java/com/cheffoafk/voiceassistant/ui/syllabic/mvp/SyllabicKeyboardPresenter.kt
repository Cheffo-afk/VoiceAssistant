package com.cheffoafk.voiceassistant.ui.syllabic.mvp

import android.content.Context
import com.cheffoafk.voiceassistant.domain.DEFAULT_SUGGESTION_DICTIONARY
import com.cheffoafk.voiceassistant.domain.SyllableModel
import com.cheffoafk.voiceassistant.ui.common.AppLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Locale

private const val TAG = "SyllabicKeyboardPresenter"

class SyllabicKeyboardPresenter(
    private val model: SyllableModel,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
) : SyllabicKeyboardContract.Presenter {

    private var scope = CoroutineScope(SupervisorJob() + mainDispatcher)
    private val dispatcher: CoroutineDispatcher = mainDispatcher

    private var view: SyllabicKeyboardContract.View? = null
    private var currentComposition: String = ""
    private var predictionJob: Job? = null
    private var predictionGeneration: Long = 0

    override fun attachView(view: SyllabicKeyboardContract.View) {
        ensureActiveScope()
        this.view = view
        currentComposition = normalizeComposition(currentComposition)
        view.renderComposedText(currentComposition)
        refreshPredictions()
    }

    override fun detachView() {
        this.view = null
        predictionJob?.cancel()
    }

    override fun preloadDictionary(context: Context, assetFileName: String) {
        ensureActiveScope()
        scope.launch {
            runCatching {
                model.loadDictionaryFromAssets(context, assetFileName)
                // Estende il dizionario con termini ad alta frequenza utili all'uso quotidiano.
                model.loadDictionaryWords(DEFAULT_SUGGESTION_DICTIONARY)
            }
                .onFailure { AppLogger.error(TAG, "Errore caricamento dizionario", it) }
            refreshPredictions()
        }
    }

    override fun onPrefixClicked(prefix: String) {
        appendChunk(prefix)
    }

    override fun onSuffixClicked(suffix: String) {
        appendChunk(suffix)
    }

    override fun onSuggestionClicked(suggestionChunk: String) {
        val normalizedChunk = normalizeSuggestionChunk(suggestionChunk)
        if (normalizedChunk.isBlank()) return
        currentComposition = normalizeComposition(currentComposition + normalizedChunk)
        registerLastWordUsage()
        currentComposition = currentComposition.trimEnd() + " "
        view?.renderComposedText(currentComposition)
        view?.triggerHapticFeedback()
        refreshPredictions()
    }

    override fun onBackspaceClicked() {
        if (currentComposition.isEmpty()) return
        currentComposition = currentComposition.dropLast(1)
        view?.renderComposedText(currentComposition)
        view?.triggerHapticFeedback()
        refreshPredictions()
    }

    override fun onSpaceClicked() {
        registerLastWordUsage()

        currentComposition = currentComposition.trimEnd() + " "
        view?.renderComposedText(currentComposition)
        view?.triggerHapticFeedback()
        refreshPredictions()
    }

    fun destroy() {
        scope.cancel()
        predictionJob?.cancel()
        view = null
    }

    private fun appendChunk(chunk: String) {
        val normalizedChunk = normalizeChunk(chunk)
        if (normalizedChunk.isBlank()) return
        currentComposition = normalizeComposition(currentComposition + normalizedChunk)
        view?.renderComposedText(currentComposition)
        view?.triggerHapticFeedback()
        refreshPredictions()
    }

    private fun normalizeChunk(chunk: String): String =
        chunk.trim().uppercase(Locale.ROOT)

    private fun normalizeSuggestionChunk(chunk: String): String {
        val hadLeadingSpace = chunk.firstOrNull()?.isWhitespace() == true
        val collapsed = chunk
            .replace(Regex("\\s+"), " ")
            .trim()

        if (collapsed.isBlank()) return ""

        val withBoundarySpace = if (hadLeadingSpace) " $collapsed" else collapsed
        return withBoundarySpace.uppercase(Locale.ROOT)
    }

    private fun normalizeComposition(text: String): String =
        text.uppercase(Locale.ROOT)

    private fun registerLastWordUsage() {
        val lastWord = currentComposition.trimEnd().substringAfterLast(' ').trim().lowercase()
        if (lastWord.isBlank()) return

        ensureActiveScope()
        scope.launch {
            runCatching { model.registerWordUsage(lastWord) }
                .onFailure { AppLogger.error(TAG, "Errore salvataggio parola utente", it) }
        }
    }

    private fun refreshPredictions() {
        ensureActiveScope()
        val querySnapshot = currentComposition
        val generation = ++predictionGeneration
        predictionJob?.cancel()
        predictionJob = scope.launch {
            runCatching { model.getNextPredictions(querySnapshot) }
                .onSuccess { predictions ->
                    if (generation == predictionGeneration) {
                        view?.showSuggestionBar(predictions)
                    }
                }
                .onFailure {
                    if (generation == predictionGeneration) {
                        AppLogger.error(TAG, "Errore calcolo predizioni", it)
                        view?.showSuggestionBar(emptyList())
                    }
                }
        }
    }

    // Se lo scope è già stato cancellato (es. dispose + recompose), lo rigenera in modo sicuro.
    private fun ensureActiveScope() {
        if (scope.coroutineContext[Job]?.isActive == true) return
        scope = CoroutineScope(SupervisorJob() + dispatcher)
    }
}
