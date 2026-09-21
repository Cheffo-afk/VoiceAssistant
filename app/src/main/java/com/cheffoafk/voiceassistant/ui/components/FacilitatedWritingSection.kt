package com.cheffoafk.voiceassistant.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cheffoafk.voiceassistant.domain.SpeechSynthesizer
import com.cheffoafk.voiceassistant.ui.HomeScreenViewModel
import com.cheffoafk.voiceassistant.ui.common.HeightClass
import com.cheffoafk.voiceassistant.ui.common.UiText
import com.cheffoafk.voiceassistant.ui.common.WidthClass
import com.cheffoafk.voiceassistant.ui.common.logOnFailure
import com.cheffoafk.voiceassistant.ui.common.rememberHeightClass
import com.cheffoafk.voiceassistant.ui.common.rememberWidthClass
import com.cheffoafk.voiceassistant.ui.theme.PillShape

private const val TAG = "FacilitatedWriting"

@Composable
fun FacilitatedWritingSection(
    viewModel: HomeScreenViewModel,
    synthesizer: SpeechSynthesizer
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val compact = rememberWidthClass() == WidthClass.COMPACT
    val compactHeight = rememberHeightClass() == HeightClass.COMPACT
    val collapseHeight = when {
        compact -> 54.dp
        compactHeight -> 56.dp
        else -> 60.dp
    }
    val collapseCloseHeight = when {
        compact -> 48.dp
        compactHeight -> 50.dp
        else -> 52.dp
    }
    val suggestionSpacing = if (compact) 6.dp else 8.dp
    val trailingIconSize = when {
        compact -> 42.dp
        compactHeight -> 44.dp
        else -> 48.dp
    }
    var isCollapsedInLandscape by rememberSaveable { mutableStateOf(isLandscape) }

    LaunchedEffect(isLandscape) {
        isCollapsedInLandscape = isLandscape
    }

    if (isLandscape && isCollapsedInLandscape) {
        Button(
            onClick = { isCollapsedInLandscape = false },
            shape = PillShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(collapseHeight)
        ) {
            Text("Premi qui per inserire una nuova frase")
        }
        return
    }

    AppSectionCard(
        title = UiText.FACILITATED_WRITING_TITLE,
        description = UiText.FACILITATED_WRITING_DESCRIPTION
    ) {
        if (isLandscape) {
            Button(
                onClick = { isCollapsedInLandscape = true },
                shape = PillShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(collapseCloseHeight)
            ) {
                Text("Chiudi sezione")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(suggestionSpacing)
        ) {
            OutlinedTextField(
                value = uiState.inputText,
                onValueChange = { viewModel.updateInputText(it) },
                label = { Text(UiText.WRITE_HERE) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { viewModel.saveInputTextAsPhrase() },
                modifier = Modifier.size(trailingIconSize),
                enabled = uiState.inputText.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = UiText.SAVE_PHRASE
                )
            }
        }

        if (uiState.suggestions.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(suggestionSpacing)) {
                items(uiState.suggestions) { suggestion ->
                    Button(
                        onClick = { viewModel.registerSuggestionUse(suggestion) },
                        shape = PillShape,
                        contentPadding = PaddingValues(
                            horizontal = if (compact) 12.dp else 14.dp,
                            vertical = if (compact) 5.dp else 6.dp
                        )
                    ) {
                        Text(text = suggestion)
                    }
                }
            }
        }

        WritingActionButton(
            label = UiText.PLAY_TEXT,
            onClick = {
                if (uiState.inputText.isNotBlank()) {
                    runCatching { synthesizer.speak(uiState.inputText) }
                        .logOnFailure(TAG, "Errore riproduzione testo")
                }
            },
            onShoutClick = {
                if (uiState.inputText.isNotBlank()) {
                    runCatching { synthesizer.speakLoud(uiState.inputText) }
                        .logOnFailure(TAG, "Errore riproduzione shout testo")
                }
            },
            shoutEnabled = uiState.inputText.isNotBlank(),
            compact = compact
        )

        WritingActionButton(
            label = UiText.CLEAR_TEXT,
            onClick = {
                runCatching { synthesizer.stop() }
                    .logOnFailure(TAG, "Errore stop TTS")
                viewModel.clearInputText()
            },
            compact = compact
        )
    }
}

@Composable
private fun WritingActionButton(
    label: String,
    onClick: () -> Unit,
    onShoutClick: (() -> Unit)? = null,
    shoutEnabled: Boolean = true,
    compact: Boolean = false
) {
    val compactHeight = rememberHeightClass() == HeightClass.COMPACT
    val buttonHeight = when {
        compact -> 48.dp
        compactHeight -> 50.dp
        else -> 52.dp
    }
    val shoutIconSize = if (compact) 34.dp else 40.dp
    val shoutFontSize = if (compact) 18.sp else 20.sp

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(buttonHeight)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(label, modifier = Modifier.align(Alignment.Center))
            if (onShoutClick != null) {
                IconButton(
                    onClick = onShoutClick,
                    enabled = shoutEnabled,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(shoutIconSize)
                ) {
                    Text(text = UiText.SHOUT_ICON, fontSize = shoutFontSize)
                }
            }
        }
    }
}
