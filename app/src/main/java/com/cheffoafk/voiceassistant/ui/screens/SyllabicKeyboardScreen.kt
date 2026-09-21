package com.cheffoafk.voiceassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.res.Configuration
import com.cheffoafk.voiceassistant.domain.SYLLABIC_GROUPS
import com.cheffoafk.voiceassistant.domain.SpeechSynthesizer
import com.cheffoafk.voiceassistant.domain.SyllabicGroup
import com.cheffoafk.voiceassistant.ui.common.UiText
import com.cheffoafk.voiceassistant.ui.common.HeightClass
import com.cheffoafk.voiceassistant.ui.common.WidthClass
import com.cheffoafk.voiceassistant.ui.common.logOnFailure
import com.cheffoafk.voiceassistant.ui.common.rememberHeightClass
import com.cheffoafk.voiceassistant.ui.common.rememberWidthClass
import com.cheffoafk.voiceassistant.ui.common.rememberSystemAwareNavBottomPadding
import com.cheffoafk.voiceassistant.ui.components.AppTopBarHeight
import com.cheffoafk.voiceassistant.ui.components.appTopBarColors
import com.cheffoafk.voiceassistant.ui.theme.SpacingDefault
import com.cheffoafk.voiceassistant.ui.theme.SpacingLarge
import com.cheffoafk.voiceassistant.ui.theme.SpacingSmall
import com.cheffoafk.voiceassistant.ui.syllabic.mvp.SyllabicKeyboardMvpFactory
import com.cheffoafk.voiceassistant.ui.syllabic.mvp.SyllabicKeyboardContract

private const val TAG = "SyllabicKeyboardScreen"

// ─────────────────────────────────────────────────────────────────────────────
// Schermata principale
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabicKeyboardScreen(
    synthesizer: SpeechSynthesizer,
    onSavePhraseToHome: (String) -> Unit,
    isRightHanded: Boolean,
    onOpenThemeMenu: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val widthClass = rememberWidthClass()
    val heightClass = rememberHeightClass()
    val compactHeight = heightClass == HeightClass.COMPACT
    val appBarActionSize = if (compactHeight) 50.dp else 56.dp
    val appBarEmojiSize = if (compactHeight) 29.sp else 33.sp
    val contentHorizontalPadding = if (compactHeight) SpacingDefault else SpacingLarge
    val contentVerticalPadding = if (compactHeight) SpacingSmall else SpacingDefault
    val contentSpacing = if (compactHeight) SpacingSmall else SpacingDefault
    val navBottomPadding = rememberSystemAwareNavBottomPadding()

    var composedText by rememberSaveable { mutableStateOf("") }
    var suggestionItems by remember { mutableStateOf(emptyList<String>()) }
    var selectedGroup by rememberSaveable { mutableStateOf<SyllabicGroup?>(null) }

    val presenter = remember(context.applicationContext) {
        SyllabicKeyboardMvpFactory.createPresenter(context.applicationContext)
    }

    val viewAdapter = remember(haptic, synthesizer) {
        object : SyllabicKeyboardContract.View {
            override fun renderComposedText(text: String) {
                composedText = text
            }

            override fun showSuggestionBar(suggestions: List<String>) {
                suggestionItems = suggestions
            }

            override fun triggerHapticFeedback() {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }

            override fun speakFeedback(text: String) {
                runCatching { synthesizer.speak(text) }
                    .logOnFailure(TAG, "Errore feedback vocale suggerimento")
            }
        }
    }

    DisposableEffect(presenter) {
        onDispose {
            presenter.detachView()
            presenter.destroy()
        }
    }

    LaunchedEffect(presenter, viewAdapter) {
        presenter.attachView(viewAdapter)
    }

    LaunchedEffect(presenter, context.applicationContext) {
        presenter.preloadDictionary(context.applicationContext)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
            .padding(bottom = navBottomPadding)
    ) {

        TopAppBar(
            modifier = Modifier.height(AppTopBarHeight),
            title = {
                Text(
                    text = UiText.SYLLABIC_KEYBOARD_TITLE,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = UiText.BACK
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = onOpenThemeMenu,
                    modifier = Modifier.size(if (isLandscape) appBarActionSize - 4.dp else appBarActionSize)
                ) {
                    Text("🎨", fontSize = appBarEmojiSize)
                }
            },
            colors = appTopBarColors()
        )

        val contentModifier = if (isLandscape) {
            Modifier
                .fillMaxSize()
                .padding(horizontal = contentHorizontalPadding, vertical = contentVerticalPadding)
        } else {
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = contentHorizontalPadding, vertical = contentVerticalPadding)
        }

        Column(
            modifier = contentModifier,
            verticalArrangement = Arrangement.spacedBy(contentSpacing)
        ) {

            // ── 1. Area testo composto ────────────────────────────────────────
            ComposedTextDisplay(
                text = composedText,
                onDeleteLast = presenter::onBackspaceClicked,
                isCompact = isLandscape
            )

            // ── 2. Vocali ──────────────────────────────────���──────────────────
            SectionLabel(UiText.VOWELS)
            VowelsRow(onVowelTap = presenter::onPrefixClicked)

            if (isLandscape) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val landscapeSpacing = SpacingSmall
                    val leftRatio = when (widthClass) {
                        WidthClass.COMPACT -> 0.58f
                        WidthClass.MEDIUM -> 0.55f
                        WidthClass.EXPANDED -> 0.45f
                    }
                    val leftWidth = maxWidth * leftRatio
                    val rightWidth = maxWidth - leftWidth
                    Row(horizontalArrangement = Arrangement.spacedBy(landscapeSpacing)) {
                        if (isRightHanded) {
                            Column(
                                modifier = Modifier.width(leftWidth),
                                verticalArrangement = Arrangement.spacedBy(landscapeSpacing)
                            ) {
                                SectionLabel(UiText.CONSONANTS)
                                GroupSelectorRow(
                                    groups = SYLLABIC_GROUPS,
                                    selectedGroup = selectedGroup,
                                    onGroupTap = { group ->
                                        selectedGroup = if (selectedGroup?.label == group.label) null else group
                                    },
                                    landscapeMode = true
                                )

                                SyllablesSection(
                                    selectedGroup = selectedGroup,
                                    onSyllableTap = presenter::onSuffixClicked
                                )
                            }

                            Column(
                                modifier = Modifier.width(rightWidth - landscapeSpacing),
                                verticalArrangement = Arrangement.spacedBy(landscapeSpacing)
                            ) {
                                SuggestionsSection(
                                    currentPrefix = composedText.trimEnd().substringAfterLast(' '),
                                    suggestions = suggestionItems,
                                    onSuggestionTap = presenter::onSuggestionClicked
                                )

                                ActionBarLandscape(
                                    hasText = composedText.isNotBlank(),
                                    onSpace = presenter::onSpaceClicked,
                                    onSaveToHome = {
                                        val phraseToSave = composedText.trim()
                                        if (phraseToSave.isNotEmpty()) {
                                            onSavePhraseToHome(phraseToSave)
                                        }
                                    },
                                    onClear = {
                                        while (composedText.isNotEmpty()) {
                                            presenter.onBackspaceClicked()
                                        }
                                    },
                                    onSpeak = {
                                        runCatching { synthesizer.speak(composedText) }
                                            .logOnFailure(TAG, "Errore TTS tastiera sillabica")
                                        },
                                        onSpeakLoud = {
                                            runCatching { synthesizer.speakLoud(composedText) }
                                                .logOnFailure(TAG, "Errore TTS shout tastiera sillabica")
                                        }
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.width(leftWidth),
                                verticalArrangement = Arrangement.spacedBy(landscapeSpacing)
                            ) {
                                SuggestionsSection(
                                    currentPrefix = composedText.trimEnd().substringAfterLast(' '),
                                    suggestions = suggestionItems,
                                    onSuggestionTap = presenter::onSuggestionClicked
                                )

                                ActionBarLandscape(
                                    hasText = composedText.isNotBlank(),
                                    onSpace = presenter::onSpaceClicked,
                                    onSaveToHome = {
                                        val phraseToSave = composedText.trim()
                                        if (phraseToSave.isNotEmpty()) {
                                            onSavePhraseToHome(phraseToSave)
                                        }
                                    },
                                    onClear = {
                                        while (composedText.isNotEmpty()) {
                                            presenter.onBackspaceClicked()
                                        }
                                    },
                                    onSpeak = {
                                        runCatching { synthesizer.speak(composedText) }
                                            .logOnFailure(TAG, "Errore TTS tastiera sillabica")
                                        },
                                        onSpeakLoud = {
                                            runCatching { synthesizer.speakLoud(composedText) }
                                                .logOnFailure(TAG, "Errore TTS shout tastiera sillabica")
                                        }
                                )
                            }

                            Column(
                                modifier = Modifier.width(rightWidth - landscapeSpacing),
                                verticalArrangement = Arrangement.spacedBy(landscapeSpacing)
                            ) {
                                SectionLabel(UiText.CONSONANTS)
                                GroupSelectorRow(
                                    groups = SYLLABIC_GROUPS,
                                    selectedGroup = selectedGroup,
                                    onGroupTap = { group ->
                                        selectedGroup = if (selectedGroup?.label == group.label) null else group
                                    },
                                    landscapeMode = true
                                )

                                SyllablesSection(
                                    selectedGroup = selectedGroup,
                                    onSyllableTap = presenter::onSuffixClicked
                                )
                            }
                        }
                    }
                }
            } else {
                // ── 3. Selettore gruppi consonantici ──────────────────────────
                SectionLabel(UiText.CONSONANTS)
                GroupSelectorRow(
                    groups = SYLLABIC_GROUPS,
                    selectedGroup = selectedGroup,
                    onGroupTap = { group ->
                        selectedGroup = if (selectedGroup?.label == group.label) null else group
                    }
                )

                // ── 4. Sillabe del gruppo selezionato ─────────────────────────
                SyllablesSection(
                    selectedGroup = selectedGroup,
                    onSyllableTap = presenter::onSuffixClicked
                )

                SuggestionsSection(
                    currentPrefix = composedText.trimEnd().substringAfterLast(' '),
                    suggestions = suggestionItems,
                    onSuggestionTap = presenter::onSuggestionClicked
                )

                Spacer(Modifier.height(SpacingSmall))

                // ── 5. Barra azioni ───────────────────────────────────────────
                ActionBar(
                    hasText = composedText.isNotBlank(),
                    speakOnLeft = !isRightHanded,
                    onSpace = presenter::onSpaceClicked,
                    onSpeak = {
                        runCatching { synthesizer.speak(composedText) }
                            .logOnFailure(TAG, "Errore TTS tastiera sillabica")
                    },
                    onSpeakLoud = {
                        runCatching { synthesizer.speakLoud(composedText) }
                            .logOnFailure(TAG, "Errore TTS shout tastiera sillabica")
                    },
                    onClear = {
                        while (composedText.isNotEmpty()) {
                            presenter.onBackspaceClicked()
                        }
                    },
                    onSaveToHome = {
                        val phraseToSave = composedText.trim()
                        if (phraseToSave.isNotEmpty()) {
                            onSavePhraseToHome(phraseToSave)
                        }
                    }
                )
            }

            Spacer(Modifier.height(SpacingLarge))
        }
    }
}
