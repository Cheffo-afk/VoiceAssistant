package com.cheffoafk.voiceassistant.data

import android.content.Context
import android.media.AudioManager
import android.media.audiofx.LoudnessEnhancer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.cheffoafk.voiceassistant.domain.SpeechSynthesizer
import java.util.Locale
import java.util.regex.Pattern

class AndroidTtsSynthesizer(context: Context) : SpeechSynthesizer {
    private var tts: TextToSpeech? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Range boost "shout": sempre sopra i livelli standard, ma regolabile per device diversi.
    private var loudGainMb = DEFAULT_LOUD_GAIN_MB

    /**
     * Callback impostata da [speakWithProgress]; viene chiamata quando il TTS inizia un segmento.
     * Volatile: viene scritta dal thread principale e letta dal thread del TTS engine.
     */
    @Volatile private var segmentStartCallback: ((Int) -> Unit)? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val engine = tts
                if (engine != null) {
                    val italianAvailability = engine.isLanguageAvailable(Locale.ITALIAN)
                    if (italianAvailability >= TextToSpeech.LANG_AVAILABLE) {
                        engine.language = Locale.ITALIAN
                    }
                    engine.setSpeechRate(0.9f)

                    // Ascolta l'inizio di ogni utterance per notificare il progresso della narrazione.
                    engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String) {
                            // Ignora le pause silenziose; gli utterance reali finiscono con "-{index}"
                            if (utteranceId.endsWith("-pause")) return
                            val index = utteranceId.substringAfterLast('-').toIntOrNull() ?: return
                            segmentStartCallback?.invoke(index)
                        }
                        override fun onDone(utteranceId: String) {}
                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String) {}
                    })
                }
            }
        }
    }

    override fun speak(text: String) {
        ensureMaxVolume()
        segmentStartCallback = null          // nessun callback per speak standard
        applyLoudnessMode(enabled = false)
        speakInternal(text, queueMode = TextToSpeech.QUEUE_FLUSH)
    }

    override fun speakWithProgress(text: String, onSegmentStart: (segmentIndex: Int) -> Unit) {
        ensureMaxVolume()
        segmentStartCallback = onSegmentStart
        applyLoudnessMode(enabled = false)
        speakInternal(text, queueMode = TextToSpeech.QUEUE_FLUSH)
    }

    override fun speakLoud(text: String) {
        ensureMaxVolume()
        segmentStartCallback = null
        applyLoudnessMode(enabled = true)
        speakInternal(text, queueMode = TextToSpeech.QUEUE_FLUSH)
    }

    override fun speakLoudPreview(text: String) {
        ensureMaxVolume()
        segmentStartCallback = null
        applyLoudnessMode(enabled = true)
        speakInternal(text, queueMode = TextToSpeech.QUEUE_FLUSH)
    }

    override fun setLoudGainMb(gainMb: Int) {
        loudGainMb = gainMb.coerceIn(MIN_LOUD_GAIN_MB, MAX_LOUD_GAIN_MB)
    }

    override fun getLoudGainMb(): Int = loudGainMb

    private fun speakInternal(text: String, queueMode: Int) {
        val engine = tts ?: return
        val normalized = normalizeForSpeech(text)
        val parts = normalized.split(PAUSE_MARKER).map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.isEmpty()) return

        parts.forEachIndexed { index, part ->
            val currentQueueMode = if (index == 0) queueMode else TextToSpeech.QUEUE_ADD
            val utteranceId = "utterance-${System.currentTimeMillis()}-$index"
            engine.speak(part, currentQueueMode, null, utteranceId)
            if (index < parts.lastIndex) {
                engine.playSilentUtterance(PAUSE_DURATION_MS, TextToSpeech.QUEUE_ADD, "$utteranceId-pause")
            }
        }
    }

    /**
     * Porta il volume dello stream TTS al massimo prima della riproduzione.
     * Garantisce che un abbassamento accidentale del volume non renda l'app silenziosa.
     */
    private fun ensureMaxVolume() {
        runCatching {
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, 0)
        }
    }

    private fun applyLoudnessMode(enabled: Boolean) {
        if (!enabled) {
            loudnessEnhancer?.enabled = false
            return
        }

        runCatching {
            if (loudnessEnhancer == null) {
                // audioSession=0: applicazione best-effort sul mix di output del dispositivo.
                loudnessEnhancer = LoudnessEnhancer(0)
            }
            loudnessEnhancer?.setTargetGain(loudGainMb)
            loudnessEnhancer?.enabled = true
        }.onFailure {
            loudnessEnhancer?.release()
            loudnessEnhancer = null
        }
    }

    /**
     * Evita letture tipo acronimo quando il testo arriva tutto in maiuscolo
     * (caso frequente nella tastiera sillabica).
     */
    private fun normalizeForSpeech(text: String): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return text

        val symbolsNormalized = SYMBOL_TO_SPEECH.entries.fold(trimmed) { acc, (symbol, spoken) ->
            acc.replace(symbol, spoken)
        }
        val wordsNormalized = WORD_TO_SPEECH.entries.fold(symbolsNormalized) { acc, (word, spoken) ->
            acc.replace(Regex("\\b${Pattern.quote(word)}\\b"), spoken)
        }

        return if (wordsNormalized == wordsNormalized.uppercase(Locale.ROOT)) {
            wordsNormalized.lowercase(Locale.ROOT)
        } else {
            wordsNormalized
        }
    }

    override fun stop() {
        segmentStartCallback = null
        tts?.stop()
    }

    override fun shutdown() {
        loudnessEnhancer?.enabled = false
        loudnessEnhancer?.release()
        loudnessEnhancer = null
        tts?.shutdown()
        tts = null
    }
}

private const val MIN_LOUD_GAIN_MB = 300
private const val MAX_LOUD_GAIN_MB = 2400
private const val DEFAULT_LOUD_GAIN_MB = 1200
private const val PAUSE_MARKER = "[PAUSE_1S]"
private const val PAUSE_DURATION_MS = 1000L
private val WORD_TO_SPEECH = linkedMapOf(
    "SHOUT" to "shout"
)
private val SYMBOL_TO_SPEECH = linkedMapOf(
    "▲" to " freccia su ",
    "▼" to " freccia giu ",
    "◀" to " freccia sinistra ",
    "▶" to " freccia destra ",
    "⚙" to " impostazioni ",
    "⌨" to " tastiera ",
    "📢" to " volume massimo ",
    "🎨" to " colori ",
    "✓" to " conferma ",
    "🗑" to " cestino "
)

