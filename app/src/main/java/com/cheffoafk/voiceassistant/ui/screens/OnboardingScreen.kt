package com.cheffoafk.voiceassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cheffoafk.voiceassistant.domain.SpeechSynthesizer
import com.cheffoafk.voiceassistant.ui.common.HeightClass
import com.cheffoafk.voiceassistant.ui.common.OnboardingText
import com.cheffoafk.voiceassistant.ui.common.WidthClass
import com.cheffoafk.voiceassistant.ui.common.logOnFailure
import com.cheffoafk.voiceassistant.ui.common.rememberHeightClass
import com.cheffoafk.voiceassistant.ui.common.rememberWidthClass
import com.cheffoafk.voiceassistant.ui.common.rememberSystemAwareNavBottomPadding
import com.cheffoafk.voiceassistant.ui.components.AppSectionCard
import kotlinx.coroutines.channels.Channel

private const val TAG = "OnboardingScreen"

private data class OnboardingStep(val title: String, val description: String)

@Composable
fun OnboardingScreen(
    synthesizer: SpeechSynthesizer,
    onStart: () -> Unit
) {
    val onboardingSteps = onboardingSteps()
    val narration = remember { onboardingNarration(onboardingSteps) }
    val widthClass = rememberWidthClass()
    val compactHeight = rememberHeightClass() == HeightClass.COMPACT
    val navBottomPadding = rememberSystemAwareNavBottomPadding()
    val verticalContentPadding = if (compactHeight) 8.dp else 12.dp
    val itemSpacing = if (compactHeight) 8.dp else 10.dp
    val stepCardSpacing = if (compactHeight) 6.dp else 8.dp
    val horizontalPadding = when (widthClass) {
        WidthClass.COMPACT -> 8.dp
        WidthClass.MEDIUM -> 16.dp
        WidthClass.EXPANDED -> 24.dp
    }

    val listState = rememberLazyListState()
    // Channel CONFLATED: se più eventi arrivano prima che l'animazione finisca, teniamo solo l'ultimo.
    val scrollChannel = remember { Channel<Int>(Channel.CONFLATED) }
    val stepsCount = onboardingSteps.size  // 5

    /**
     * Avvia la narrazione con tracciamento del progresso e autoscroll.
     * Layout LazyColumn:
     *   indice 0 → Titolo
     *   indice 1 → INTRO        (segmento TTS 0)
     *   indice 2..N+1 → Step 1..N (segmenti TTS 1..N)
     */
    val startNarration: () -> Unit = {
        runCatching {
            synthesizer.speakWithProgress(narration) { segmentIndex ->
                val itemIndex = when {
                    segmentIndex == 0 -> 1                           // scroll all'INTRO
                    segmentIndex in 1..stepsCount -> segmentIndex + 1 // scroll al passo corrente
                    else -> return@speakWithProgress
                }
                scrollChannel.trySend(itemIndex)
            }
        }.logOnFailure(TAG, "Errore narrazione onboarding")
    }

    // Ferma il TTS quando l'utente lascia la schermata (bottone "Inizia" o back).
    DisposableEffect(Unit) {
        onDispose { synthesizer.stop() }
    }

    // Avvia la narrazione al primo ingresso nella schermata.
    LaunchedEffect(Unit) { startNarration() }

    // Consuma gli eventi di scroll e anima la lista.
    LaunchedEffect(scrollChannel) {
        for (targetIndex in scrollChannel) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
            .padding(bottom = navBottomPadding)
            .padding(horizontal = horizontalPadding)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = verticalContentPadding),
            verticalArrangement = Arrangement.spacedBy(itemSpacing)
        ) {
            item {
                Text(
                    text = OnboardingText.TITLE,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                Text(
                    text = OnboardingText.INTRO,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            items(onboardingSteps) { step ->
                AppSectionCard(
                    title = step.title,
                    description = step.description,
                    contentSpacing = stepCardSpacing
                )
            }
        }

        Spacer(Modifier.height(if (compactHeight) 4.dp else 8.dp))

        AppSectionCard(
            title = OnboardingText.LISTEN_GUIDE_TITLE,
            description = OnboardingText.LISTEN_GUIDE_DESCRIPTION,
            contentSpacing = stepCardSpacing
        ) {
            Button(
                onClick = startNarration,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(OnboardingText.REPEAT_BUTTON)
            }
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(OnboardingText.START_BUTTON)
            }
        }

        Spacer(Modifier.height(if (compactHeight) 4.dp else 8.dp))
    }
}

private fun onboardingSteps() = listOf(
    OnboardingStep(
        title = OnboardingText.STEP_1_TITLE,
        description = OnboardingText.STEP_1_DESCRIPTION
    ),
    OnboardingStep(
        title = OnboardingText.STEP_2_TITLE,
        description = OnboardingText.STEP_2_DESCRIPTION
    ),
    OnboardingStep(
        title = OnboardingText.STEP_3_TITLE,
        description = OnboardingText.STEP_3_DESCRIPTION
    ),
    OnboardingStep(
        title = OnboardingText.STEP_4_TITLE,
        description = OnboardingText.STEP_4_DESCRIPTION
    ),
    OnboardingStep(
        title = OnboardingText.STEP_5_TITLE,
        description = OnboardingText.STEP_5_DESCRIPTION
    )
)

private fun onboardingNarration(steps: List<OnboardingStep>) = buildString {
    append(OnboardingText.INTRO)
    append(PAUSE_MARKER)
    steps.forEach { step ->
        append(step.title.substringAfter('.').trim())
        append(". ")
        append(step.description)
        append(PAUSE_MARKER)
    }
    append(OnboardingText.NARRATION_SUFFIX)
}

private const val PAUSE_MARKER = " [PAUSE_1S] "
