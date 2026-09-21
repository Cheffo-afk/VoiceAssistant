package com.cheffoafk.voiceassistant.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cheffoafk.voiceassistant.data.PhraseRepository
import com.cheffoafk.voiceassistant.data.SuggestionUsageRepository
import com.cheffoafk.voiceassistant.data.UserProfileRepository
import com.cheffoafk.voiceassistant.domain.DEFAULT_SUGGESTION_DICTIONARY
import com.cheffoafk.voiceassistant.domain.SpeechSynthesizer
import com.cheffoafk.voiceassistant.ui.common.AppLogger
import com.cheffoafk.voiceassistant.ui.common.logOnFailure
import com.cheffoafk.voiceassistant.ui.components.ThemeSwitcherDialog
import com.cheffoafk.voiceassistant.ui.screens.HomeScreen
import com.cheffoafk.voiceassistant.ui.screens.OnboardingScreen
import com.cheffoafk.voiceassistant.ui.screens.SettingsScreen
import com.cheffoafk.voiceassistant.ui.screens.SyllabicKeyboardScreen
import com.cheffoafk.voiceassistant.ui.theme.PaletteOption
import com.cheffoafk.voiceassistant.ui.theme.ThemePreferences
import kotlinx.coroutines.CancellationException

private const val TAG = "VoiceAssistantApp"

private enum class AppScreen { HOME, SETTINGS, SYLLABIC_KEYBOARD }

@Composable
fun VoiceAssistantApp(
    initialShowOnboarding: Boolean,
    synthesizer: SpeechSynthesizer,
    phraseRepository: PhraseRepository,
    userProfileRepository: UserProfileRepository,
    suggestionUsageRepository: SuggestionUsageRepository,
    themePreferences: ThemePreferences,
    onDarkModeChanged: (Boolean) -> Unit,
    onPaletteSelected: (PaletteOption) -> Unit,
    shoutGainMb: Int,
    onShoutGainChanged: (Int) -> Unit,
    isRightHanded: Boolean,
    onRightHandedChanged: (Boolean) -> Unit,
    onOnboardingFinished: () -> Unit
) {
    var showOnboarding by rememberSaveable { mutableStateOf(initialShowOnboarding) }
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.HOME) }

    val vm: HomeScreenViewModel = viewModel(
        factory = HomeScreenViewModelFactory(
            phraseRepository = phraseRepository,
            userProfileRepository = userProfileRepository,
            dictionary = DEFAULT_SUGGESTION_DICTIONARY,
            suggestionUsageDataSource = suggestionUsageRepository
        )
    )

    LaunchedEffect(vm) {
        try {
            vm.speakEffect.collect { phrase ->
                runCatching { synthesizer.speak(phrase) }
                    .logOnFailure(TAG, "Errore nella riproduzione TTS")
            }
        } catch (_: CancellationException) {
            // Coroutine cancellata dal lifecycle, comportamento atteso.
        } catch (t: Throwable) {
            AppLogger.error(TAG, "Errore nel collect di speakEffect", t)
        }
    }

    var contentVisible by remember { mutableStateOf(false) }
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { contentVisible = true }

    AnimatedVisibility(
        visible = contentVisible,
        enter = fadeIn(tween(500)) + slideInVertically(
            initialOffsetY = { it / 10 },
            animationSpec = tween(500)
        )
    ) {
        when {
            showOnboarding -> OnboardingScreen(
                synthesizer = synthesizer,
                onStart = {
                    showOnboarding = false
                    onOnboardingFinished()
                }
            )

            currentScreen == AppScreen.SETTINGS -> SettingsScreen(
                viewModel = vm,
                synthesizer = synthesizer,
                shoutGainMb = shoutGainMb,
                onShoutGainChanged = onShoutGainChanged,
                isRightHanded = isRightHanded,
                onRightHandedChanged = onRightHandedChanged,
                onBack = { currentScreen = AppScreen.HOME },
                onRepeatOnboarding = {
                    currentScreen = AppScreen.HOME
                    showOnboarding = true
                }
            )

            currentScreen == AppScreen.SYLLABIC_KEYBOARD -> SyllabicKeyboardScreen(
                synthesizer = synthesizer,
                onSavePhraseToHome = vm::saveExternalPhrase,
                isRightHanded = isRightHanded,
                onOpenThemeMenu = { showThemeDialog = true },
                onBack = { currentScreen = AppScreen.HOME }
            )

            else -> HomeScreen(
                synthesizer = synthesizer,
                viewModel = vm,
                onOpenThemeMenu = { showThemeDialog = true },
                onNavigateToSettings = { currentScreen = AppScreen.SETTINGS },
                onNavigateToSyllabicKeyboard = { currentScreen = AppScreen.SYLLABIC_KEYBOARD }
            )
        }

        if (showThemeDialog) {
            ThemeSwitcherDialog(
                currentPreferences = themePreferences,
                onDismiss = { showThemeDialog = false },
                onDarkModeChanged = onDarkModeChanged,
                onPaletteSelected = onPaletteSelected
            )
        }
    }
}

