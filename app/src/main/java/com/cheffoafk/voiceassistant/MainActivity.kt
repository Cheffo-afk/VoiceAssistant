package com.cheffoafk.voiceassistant

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.cheffoafk.voiceassistant.data.AndroidTtsSynthesizer
import com.cheffoafk.voiceassistant.data.PhraseRepository
import com.cheffoafk.voiceassistant.data.SuggestionUsageRepository
import com.cheffoafk.voiceassistant.data.ThemePreferencesRepository
import com.cheffoafk.voiceassistant.data.UserProfileRepository
import com.cheffoafk.voiceassistant.domain.SpeechSynthesizer
import com.cheffoafk.voiceassistant.ui.VoiceAssistantApp
import com.cheffoafk.voiceassistant.ui.common.AppLogger
import com.cheffoafk.voiceassistant.ui.theme.VoiceAssistantTheme

private const val TAG = "MainActivity"
private const val PREFS_NAME = "voice_assistant_prefs"
private const val KEY_ONBOARDING_DONE = "onboarding_done"
private const val KEY_SHOUT_GAIN_MB = "shout_gain_mb"
private const val KEY_RIGHT_HANDED_LAYOUT = "right_handed_layout"
private const val DEFAULT_SHOUT_GAIN_MB = 1200
private const val MIN_SHOUT_GAIN_MB = 300
private const val MAX_SHOUT_GAIN_MB = 2400
/** Attesa per l'inizializzazione asincrona del TTS prima di riprodurre dal widget. */
private const val WIDGET_TTS_DELAY_MS = 600L

class MainActivity : ComponentActivity() {
    private lateinit var synthesizer: SpeechSynthesizer
    private lateinit var phraseRepository: PhraseRepository
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var suggestionUsageRepository: SuggestionUsageRepository
    private lateinit var themePreferencesRepository: ThemePreferencesRepository
    private lateinit var audioManager: AudioManager

    companion object {
        /** Extra usato dal widget Aiuto per richiedere la riproduzione di un testo all'avvio. */
        const val EXTRA_SPEAK_TEXT = "extra_speak_text"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        synthesizer = AndroidTtsSynthesizer(applicationContext)
        synthesizer.setLoudGainMb(loadShoutGainMb())
        phraseRepository = PhraseRepository(applicationContext)
        userProfileRepository = UserProfileRepository(applicationContext)
        suggestionUsageRepository = SuggestionUsageRepository(applicationContext)
        themePreferencesRepository = ThemePreferencesRepository(applicationContext)

        enableEdgeToEdge()
        setContent {
            val themePreferences by themePreferencesRepository.preferences.collectAsState()
            val shoutGainState = remember { mutableIntStateOf(loadShoutGainMb()) }
            val rightHandedState = remember { mutableStateOf(loadRightHandedLayout()) }

            VoiceAssistantTheme(themePreferences = themePreferences) {
                VoiceAssistantApp(
                    initialShowOnboarding = !isOnboardingCompleted(),
                    synthesizer = synthesizer,
                    phraseRepository = phraseRepository,
                    userProfileRepository = userProfileRepository,
                    suggestionUsageRepository = suggestionUsageRepository,
                    themePreferences = themePreferences,
                    onDarkModeChanged = { themePreferencesRepository.setDarkMode(it) },
                    onPaletteSelected = { themePreferencesRepository.setPalette(it) },
                    shoutGainMb = shoutGainState.intValue,
                    onShoutGainChanged = { requestedGain ->
                        val normalized = requestedGain.coerceIn(MIN_SHOUT_GAIN_MB, MAX_SHOUT_GAIN_MB)
                        shoutGainState.intValue = normalized
                        synthesizer.setLoudGainMb(normalized)
                        saveShoutGainMb(normalized)
                    },
                    isRightHanded = rightHandedState.value,
                    onRightHandedChanged = { isRightHanded ->
                        rightHandedState.value = isRightHanded
                        saveRightHandedLayout(isRightHanded)
                    },
                    onOnboardingFinished = { markOnboardingCompleted() }
                )
            }
        }

        // Gestisce il tap dal widget Aiuto (avvio a freddo).
        handleWidgetSpeakIntent(intent)
    }

    /** Chiamato quando l'app è già attiva e riceve un nuovo intent (es. tap widget con app aperta). */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleWidgetSpeakIntent(intent)
    }

    override fun onPause() {
        super.onPause()
        runCatching { synthesizer.stop() }
    }

    override fun onResume() {
        super.onResume()
        ensureAppVolume()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (event?.repeatCount == 0) {
                Toast.makeText(
                    this,
                    "Volume bloccato al massimo per garantire l'utilizzo dell'app",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        runCatching { synthesizer.shutdown() }
            .onFailure { AppLogger.error(TAG, "Errore durante shutdown TTS", it) }
        super.onDestroy()
    }

    // -------------------------------------------------------------------------
    // Widget support
    // -------------------------------------------------------------------------

    /**
     * Se l'intent contiene [EXTRA_SPEAK_TEXT], riproduce il testo via TTS.
     * Il delay garantisce che il motore TTS sia pronto in caso di cold start.
     */
    private fun handleWidgetSpeakIntent(intent: Intent?) {
        val text = intent?.getStringExtra(EXTRA_SPEAK_TEXT)?.takeIf { it.isNotBlank() } ?: return
        Handler(Looper.getMainLooper()).postDelayed({
            runCatching { synthesizer.speak(text) }
                .onFailure { AppLogger.error(TAG, "Errore TTS da widget", it) }
        }, WIDGET_TTS_DELAY_MS)
    }

    // -------------------------------------------------------------------------
    // Preferences helpers
    // -------------------------------------------------------------------------

    private fun isOnboardingCompleted(): Boolean =
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(KEY_ONBOARDING_DONE, false)

    private fun markOnboardingCompleted() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit { putBoolean(KEY_ONBOARDING_DONE, true) }
    }

    private fun loadShoutGainMb(): Int =
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getInt(KEY_SHOUT_GAIN_MB, DEFAULT_SHOUT_GAIN_MB)
            .coerceIn(MIN_SHOUT_GAIN_MB, MAX_SHOUT_GAIN_MB)

    private fun saveShoutGainMb(gainMb: Int) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit {
            putInt(KEY_SHOUT_GAIN_MB, gainMb.coerceIn(MIN_SHOUT_GAIN_MB, MAX_SHOUT_GAIN_MB))
        }
    }

    private fun loadRightHandedLayout(): Boolean =
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(KEY_RIGHT_HANDED_LAYOUT, true)

    private fun saveRightHandedLayout(isRightHanded: Boolean) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit {
            putBoolean(KEY_RIGHT_HANDED_LAYOUT, isRightHanded)
        }
    }

    private fun ensureAppVolume() {
        runCatching {
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, 0)
        }.onFailure { AppLogger.error(TAG, "Impossibile impostare il volume al massimo", it) }
    }
}
