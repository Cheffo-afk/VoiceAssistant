package com.cheffoafk.voiceassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cheffoafk.voiceassistant.domain.SYLLABIC_VOWELS
import com.cheffoafk.voiceassistant.domain.SyllabicGroup
import com.cheffoafk.voiceassistant.ui.common.UiText
import com.cheffoafk.voiceassistant.ui.common.HeightClass
import com.cheffoafk.voiceassistant.ui.common.WidthClass
import com.cheffoafk.voiceassistant.ui.common.rememberHeightClass
import com.cheffoafk.voiceassistant.ui.common.rememberWidthClass
import com.cheffoafk.voiceassistant.ui.theme.CardShape
import com.cheffoafk.voiceassistant.ui.theme.PillShape
import com.cheffoafk.voiceassistant.ui.theme.SpacingDefault
import com.cheffoafk.voiceassistant.ui.theme.SpacingLarge
import com.cheffoafk.voiceassistant.ui.theme.SpacingSmall
import kotlinx.coroutines.launch
import java.util.Locale

private const val CONSONANT_ROW_COUNT = 3
private const val CONSONANT_COLUMN_COUNT = 7
private const val CONSONANT_PAGE_COUNT = 3

// Token grafici centrali per rendere il tuning coerente e facile da ritoccare.
private val ConsonantButtonSize = 88.dp
private val SyllableButtonHeight = ConsonantButtonSize
private val SuggestionButtonMinHeight = 54.dp
private val SuggestionButtonHorizontalPadding = 21.dp
private val SuggestionButtonVerticalPadding = 9.dp
private val ConsonantNavButtonWidth = 42.dp
private val ActionButtonHeight = 78.dp
private val ActionPrimaryFontSize = 20.sp
private val ActionSecondaryFontSize = 16.sp
private val ComposedTextBoxHeight = 128.dp
private val ComposedBackButtonHeight = 64.dp

fun syllableToCompositionText(syllable: String): String = syllable.trim()

fun buildDisplayedSyllables(group: SyllabicGroup): List<String> {
    val head = group.label.trim().uppercase()
    val tail = group.syllables
        .asSequence()
        .map { it.trim().uppercase() }
        .filter { it.isNotEmpty() }
        .filter { it != head }
        .toList()

    return if (head.isBlank()) tail else listOf(head) + tail
}

@Composable
internal fun SuggestionsSection(
    currentPrefix: String,
    suggestions: List<String>,
    onSuggestionTap: (String) -> Unit
) {
    val compact = rememberWidthClass() == WidthClass.COMPACT
    val compactHeight = rememberHeightClass() == HeightClass.COMPACT
    val suggestionMinHeight = when {
        compact -> 48.dp
        compactHeight -> 50.dp
        else -> SuggestionButtonMinHeight
    }
    val suggestionNavWidth = if (compact) 34.dp else ConsonantNavButtonWidth
    val suggestionMinWidth = if (compact) 78.dp else 96.dp
    val suggestionFont = if (compact) 16.sp else 18.sp
    val buttonHPadding = if (compact) 16.dp else SuggestionButtonHorizontalPadding
    val buttonVPadding = if (compact) 7.dp else SuggestionButtonVerticalPadding
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val navGutter = suggestionNavWidth + SpacingSmall
    val displayPrefix = currentPrefix.uppercase(Locale.ROOT)
    val lastIndex = suggestions.lastIndex.coerceAtLeast(0)
    val hasSuggestions = suggestions.isNotEmpty()

    SectionLabel(UiText.completionLabel(suggestions.size))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = suggestionMinHeight),
        contentAlignment = Alignment.Center
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = navGutter)
                .wrapContentWidth(align = Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
            items(suggestions) { chunk ->
                val displayChunk = chunk.uppercase(Locale.ROOT)
                val onPrimaryFaded = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.55f)
                Button(
                    onClick = { onSuggestionTap(chunk) },
                    modifier = Modifier
                        .widthIn(min = suggestionMinWidth)
                        .heightIn(min = suggestionMinHeight),
                    shape = PillShape,
                    contentPadding = PaddingValues(
                        horizontal = buttonHPadding,
                        vertical = buttonVPadding
                    )
                ) {
                    Text(
                        text = buildAnnotatedString {
                            // Prefix in tonalita ridotta per evidenziare il prossimo completamento.
                            withStyle(SpanStyle(color = onPrimaryFaded)) {
                                append(displayPrefix)
                            }
                            withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                                append(displayChunk)
                            }
                        },
                        fontSize = suggestionFont,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        OutlinedButton(
            onClick = {
                coroutineScope.launch {
                    val targetIndex = (listState.firstVisibleItemIndex - 4).coerceAtLeast(0)
                    listState.animateScrollToItem(targetIndex)
                }
            },
            enabled = hasSuggestions,
            modifier = Modifier
                .width(suggestionNavWidth)
                .height(suggestionMinHeight)
                .align(Alignment.CenterStart),
            shape = CardShape,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("<", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = {
                coroutineScope.launch {
                    val targetIndex = (listState.firstVisibleItemIndex + 4).coerceAtMost(lastIndex)
                    listState.animateScrollToItem(targetIndex)
                }
            },
            enabled = hasSuggestions,
            modifier = Modifier
                .width(suggestionNavWidth)
                .height(suggestionMinHeight)
                .align(Alignment.CenterEnd),
            shape = CardShape,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(">", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
internal fun ComposedTextDisplay(
    text: String,
    onDeleteLast: () -> Unit,
    isCompact: Boolean = false
) {
    val compact = rememberWidthClass() == WidthClass.COMPACT
    val compactHeight = rememberHeightClass() == HeightClass.COMPACT
    val boxHeight = when {
        isCompact -> ComposedTextBoxHeight / 2
        compactHeight -> (ComposedTextBoxHeight * 0.75f)
        else -> ComposedTextBoxHeight
    }
    val backButtonHeight = when {
        isCompact -> ComposedBackButtonHeight / 2
        compactHeight -> (ComposedBackButtonHeight * 0.75f)
        else -> ComposedBackButtonHeight
    }
    val maxLines = if (isCompact || compactHeight) 1 else 2
    val textFont = if (compact) 18.sp else 20.sp
    val backFont = if (compact) 11.sp else 12.sp
    val horizontalPadding = if (compact) SpacingDefault else SpacingLarge
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(boxHeight),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = SpacingDefault),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text.ifEmpty { UiText.COMPOSED_TEXT_PLACEHOLDER },
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = if (text.isEmpty()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontSize = textFont,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis
            )
            OutlinedButton(
                onClick = onDeleteLast,
                shape = PillShape,
                modifier = Modifier.height(backButtonHeight)
            ) {
                Text("BACK", fontSize = backFont, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
internal fun VowelsRow(onVowelTap: (String) -> Unit) {
    val compact = rememberWidthClass() == WidthClass.COMPACT
    val compactHeight = rememberHeightClass() == HeightClass.COMPACT
    val buttonHeight = when {
        compact -> 50.dp
        compactHeight -> 52.dp
        else -> 56.dp
    }
    val fontSize = if (compact) 18.sp else 20.sp
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
    ) {
        SYLLABIC_VOWELS.forEach { vowel ->
            Button(
                onClick = { onVowelTap(vowel) },
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                shape = CardShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(vowel, fontSize = fontSize, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
internal fun GroupSelectorRow(
    groups: List<SyllabicGroup>,
    selectedGroup: SyllabicGroup?,
    onGroupTap: (SyllabicGroup) -> Unit,
    landscapeMode: Boolean = false
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val coroutineScope = rememberCoroutineScope()
        val compactHeight = rememberHeightClass() == HeightClass.COMPACT
        val navButtonWidth = ConsonantNavButtonWidth
        val navGutter = navButtonWidth + SpacingSmall
        var currentPage by rememberSaveable { mutableIntStateOf(0) }

        val usableWidth = maxWidth - (navGutter * 2)
        val responsiveButtonSize = remember(usableWidth) {
            ((usableWidth - (SpacingSmall * (CONSONANT_COLUMN_COUNT - 1))) / CONSONANT_COLUMN_COUNT)
                .coerceAtMost(ConsonantButtonSize)
        }
        val navButtonHeight = when {
            landscapeMode && compactHeight -> responsiveButtonSize * 1.7f
            landscapeMode -> responsiveButtonSize * 2
            else -> responsiveButtonSize
        }
        val buttonsPerPage = CONSONANT_COLUMN_COUNT * CONSONANT_ROW_COUNT
        val pages = remember(groups, buttonsPerPage) { groups.chunked(buttonsPerPage) }
        val fixedPages = remember(pages) {
            val padded = pages.toMutableList()
            while (padded.size < CONSONANT_PAGE_COUNT) padded.add(emptyList())
            padded.take(CONSONANT_PAGE_COUNT)
        }
        val lastPageIndex = (fixedPages.size - 1).coerceAtLeast(0)
        if (currentPage > lastPageIndex) currentPage = lastPageIndex
        val pageGroups = fixedPages.getOrElse(currentPage) { emptyList() }
        val labelFontSize = if (responsiveButtonSize < 52.dp) 15.sp else 17.sp

        Column(verticalArrangement = Arrangement.spacedBy(SpacingSmall)) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = navGutter)
                        .width(usableWidth),
                    verticalArrangement = Arrangement.spacedBy(SpacingSmall)
                ) {
                    (0 until CONSONANT_ROW_COUNT).forEach { rowIndex ->
                        val startIndex = rowIndex * CONSONANT_COLUMN_COUNT
                        val rowGroups = pageGroups.drop(startIndex).take(CONSONANT_COLUMN_COUNT)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
                        ) {
                            repeat(CONSONANT_COLUMN_COUNT) { columnIndex ->
                                val group = rowGroups.getOrNull(columnIndex)
                                if (group == null) {
                                    Spacer(
                                        modifier = Modifier
                                            .width(responsiveButtonSize)
                                            .height(responsiveButtonSize)
                                    )
                                } else {
                                    val isSelected = selectedGroup?.label == group.label
                                    val isSingleConsonant = group.label.length == 1
                                    Button(
                                        onClick = { onGroupTap(group) },
                                        modifier = Modifier
                                            .width(responsiveButtonSize)
                                            .height(responsiveButtonSize),
                                        shape = CardShape,
                                        border = if (isSingleConsonant) {
                                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                        } else {
                                            null
                                        },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else if (isSingleConsonant) {
                                                MaterialTheme.colorScheme.tertiary
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            contentColor = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else if (isSingleConsonant) {
                                                MaterialTheme.colorScheme.onTertiary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    ) {
                                        Text(
                                            text = group.label,
                                            fontSize = labelFontSize,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            currentPage = (currentPage - 1).coerceAtLeast(0)
                        }
                    },
                    modifier = Modifier
                        .width(navButtonWidth)
                        .height(navButtonHeight)
                        .align(Alignment.CenterStart),
                    shape = CardShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("<", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            currentPage = (currentPage + 1).coerceAtMost(lastPageIndex)
                        }
                    },
                    modifier = Modifier
                        .width(navButtonWidth)
                        .height(navButtonHeight)
                        .align(Alignment.CenterEnd),
                    shape = CardShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(">", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
internal fun SyllablesSection(
    selectedGroup: SyllabicGroup?,
    onSyllableTap: (String) -> Unit
) {
    val compact = rememberWidthClass() == WidthClass.COMPACT
    val compactHeight = rememberHeightClass() == HeightClass.COMPACT
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        BoxWithConstraints {
            val usableWidth = maxWidth - (SpacingSmall * (CONSONANT_COLUMN_COUNT - 1))
            val responsiveButtonSize = remember(maxWidth) {
                (usableWidth / CONSONANT_COLUMN_COUNT).coerceAtMost(ConsonantButtonSize)
            }
            val rowHeight = responsiveButtonSize.coerceAtLeast(
                when {
                    compact -> 52.dp
                    compactHeight -> 56.dp
                    else -> 60.dp
                }
            )
            val labelFontSize = if (responsiveButtonSize < 52.dp) 15.sp else 17.sp

            Column(
                modifier = Modifier.padding(SpacingDefault),
                verticalArrangement = Arrangement.spacedBy(SpacingSmall)
            ) {
                val group = selectedGroup
                if (group == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = UiText.SELECT_CONSONANT_GROUP,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    val syllables = buildDisplayedSyllables(group)

                    Text(
                        text = UiText.syllablesLabel(group.label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(rowHeight),
                        contentPadding = PaddingValues(horizontal = 1.dp),
                        horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
                    ) {
                        items(syllables) { syllable ->
                            Button(
                                onClick = { onSyllableTap(syllableToCompositionText(syllable)) },
                                modifier = Modifier
                                    .width(responsiveButtonSize)
                                    .height(rowHeight),
                                shape = CardShape,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = syllable,
                                    fontSize = labelFontSize,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ActionBar(
    hasText: Boolean,
    speakOnLeft: Boolean = false,
    onSpace: () -> Unit,
    onSpeak: () -> Unit,
    onSpeakLoud: () -> Unit,
    onClear: () -> Unit,
    onSaveToHome: () -> Unit
) {
    val compact = rememberWidthClass() == WidthClass.COMPACT
    val compactHeight = rememberHeightClass() == HeightClass.COMPACT
    val actionButtonHeight = when {
        compact -> 64.dp
        compactHeight -> 68.dp
        else -> ActionButtonHeight
    }
    val primaryFont = if (compact) 18.sp else ActionPrimaryFontSize
    val secondaryFont = if (compact) 14.sp else ActionSecondaryFontSize
    val shoutSize = if (compact) 34.dp else 40.dp
    val shoutFont = if (compact) 18.sp else 20.sp
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingSmall)
    ) {
        OutlinedButton(
            onClick = onSpace,
            modifier = Modifier
                .fillMaxWidth()
                .height(actionButtonHeight),
            shape = PillShape
        ) {
            Text(UiText.SPACE, fontSize = primaryFont, fontWeight = FontWeight.SemiBold)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
            if (speakOnLeft) {
                Button(
                    onClick = onSpeak,
                    enabled = hasText,
                    modifier = Modifier
                        .weight(2f)
                        .height(actionButtonHeight),
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(UiText.SPEAK, fontSize = primaryFont, fontWeight = FontWeight.SemiBold)
                        IconButton(
                            onClick = onSpeakLoud,
                            enabled = hasText,
                            modifier = Modifier.size(shoutSize)
                        ) {
                            Text(UiText.SHOUT_ICON, fontSize = shoutFont)
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onSaveToHome,
                enabled = hasText,
                modifier = Modifier
                    .weight(1f)
                    .height(actionButtonHeight),
                shape = PillShape
            ) {
                Text(UiText.SAVE_TO_HOME, fontSize = secondaryFont, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onClear,
                enabled = hasText,
                modifier = Modifier
                    .weight(1f)
                    .height(actionButtonHeight),
                shape = PillShape
            ) {
                Text(UiText.CLEAR, fontSize = secondaryFont, fontWeight = FontWeight.Bold)
            }

            if (!speakOnLeft) {
                Button(
                    onClick = onSpeak,
                    enabled = hasText,
                    modifier = Modifier
                        .weight(2f)
                        .height(actionButtonHeight),
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(UiText.SPEAK, fontSize = primaryFont, fontWeight = FontWeight.SemiBold)
                        IconButton(
                            onClick = onSpeakLoud,
                            enabled = hasText,
                            modifier = Modifier.size(shoutSize)
                        ) {
                            Text(UiText.SHOUT_ICON, fontSize = shoutFont)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ActionBarLandscape(
    hasText: Boolean,
    onSpace: () -> Unit,
    onSaveToHome: () -> Unit,
    onClear: () -> Unit,
    onSpeak: () -> Unit,
    onSpeakLoud: () -> Unit
) {
    val compact = rememberWidthClass() == WidthClass.COMPACT
    val compactHeight = rememberHeightClass() == HeightClass.COMPACT
    val actionButtonHeight = when {
        compact -> 48.dp
        compactHeight -> 52.dp
        else -> 58.dp
    }
    val primaryFont = if (compact || compactHeight) 16.sp else 18.sp
    val secondaryFont = if (compact || compactHeight) 13.sp else 14.sp
    val shoutSize = if (compact || compactHeight) 30.dp else 34.dp
    val shoutFont = if (compact || compactHeight) 16.sp else 18.sp
    val buttonSpacing = if (compact || compactHeight) 6.dp else SpacingSmall
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(buttonSpacing)
    ) {
        OutlinedButton(
            onClick = onSpace,
            modifier = Modifier
                .fillMaxWidth()
                .height(actionButtonHeight),
            shape = PillShape
        ) {
            Text(UiText.SPACE, fontSize = primaryFont, fontWeight = FontWeight.SemiBold)
        }

        OutlinedButton(
            onClick = onSaveToHome,
            enabled = hasText,
            modifier = Modifier
                .fillMaxWidth()
                .height(actionButtonHeight),
            shape = PillShape
        ) {
            Text(UiText.SAVE_TO_HOME, fontSize = secondaryFont, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = onClear,
            enabled = hasText,
            modifier = Modifier
                .fillMaxWidth()
                .height(actionButtonHeight),
            shape = PillShape
        ) {
            Text(UiText.CLEAR, fontSize = secondaryFont, fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = onSpeak,
            enabled = hasText,
            modifier = Modifier
                .fillMaxWidth()
                .height(actionButtonHeight),
            shape = PillShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(UiText.SPEAK, fontSize = primaryFont, fontWeight = FontWeight.SemiBold)
                IconButton(
                    onClick = onSpeakLoud,
                    enabled = hasText,
                    modifier = Modifier.size(shoutSize)
                ) {
                    Text(UiText.SHOUT_ICON, fontSize = shoutFont)
                }
            }
        }
    }
}

