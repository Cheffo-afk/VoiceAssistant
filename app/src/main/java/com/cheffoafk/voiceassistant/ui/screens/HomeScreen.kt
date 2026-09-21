package com.cheffoafk.voiceassistant.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.ui.zIndex
import com.cheffoafk.voiceassistant.domain.SpeechSynthesizer
import com.cheffoafk.voiceassistant.ui.HomeScreenViewModel
import com.cheffoafk.voiceassistant.ui.common.logOnFailure
import com.cheffoafk.voiceassistant.ui.common.UiText
import com.cheffoafk.voiceassistant.ui.common.HeightClass
import com.cheffoafk.voiceassistant.ui.common.WidthClass
import com.cheffoafk.voiceassistant.ui.common.rememberHeightClass
import com.cheffoafk.voiceassistant.ui.common.rememberWidthClass
import com.cheffoafk.voiceassistant.ui.common.rememberSystemAwareNavBottomPadding
import com.cheffoafk.voiceassistant.ui.components.AppTopBarHeight
import com.cheffoafk.voiceassistant.ui.components.appTopBarColors
import com.cheffoafk.voiceassistant.ui.components.DragDropState
import com.cheffoafk.voiceassistant.ui.components.FacilitatedWritingSection
import com.cheffoafk.voiceassistant.ui.components.LargePhraseButton
import com.cheffoafk.voiceassistant.ui.components.PhraseSidebar
import com.cheffoafk.voiceassistant.ui.theme.BannerShape

private const val TAG = "HomeScreen"

/**
 * Numero di item non-frase che precedono i bottoni frasi nella LazyColumn:
 *   0 – FacilitatedWritingSection
 *   1 – Spacer
 *   2 – hint text
 *   3 – Spacer
 */
private const val PHRASE_LIST_OFFSET = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    synthesizer: SpeechSynthesizer,
    viewModel: HomeScreenViewModel,
    onOpenThemeMenu: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSyllabicKeyboard: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val widthClass = rememberWidthClass()
    val heightClass = rememberHeightClass()
    val compactScreen = widthClass == WidthClass.COMPACT
    val compactHeight = heightClass == HeightClass.COMPACT
    val appBarActionSize = when (widthClass) {
        WidthClass.COMPACT -> 42.dp
        WidthClass.MEDIUM -> 46.dp
        WidthClass.EXPANDED -> 50.dp
    }.let { base -> if (compactHeight) base - 4.dp else base }
    val appBarEmojiSize = when (widthClass) {
        WidthClass.COMPACT -> if (compactHeight) 25.sp else 27.sp
        WidthClass.MEDIUM -> if (compactHeight) 31.sp else 33.sp
        WidthClass.EXPANDED -> if (compactHeight) 36.sp else 38.sp
    }
    val appBarIconSize = when (widthClass) {
        WidthClass.COMPACT -> 30.dp
        WidthClass.MEDIUM -> 36.dp
        WidthClass.EXPANDED -> 42.dp
    }.let { base -> if (compactHeight) base - 2.dp else base }
    val sidebarWidth = when (widthClass) {
        WidthClass.COMPACT -> 44.dp
        WidthClass.MEDIUM -> 56.dp
        WidthClass.EXPANDED -> 64.dp
    }
    val listHorizontalPaddingStart = if (compactScreen) 8.dp else 12.dp
    val listHorizontalPaddingEnd = if (compactScreen) 4.dp else 8.dp
    val hintFontSize = when (widthClass) {
        WidthClass.COMPACT -> if (compactHeight) 20.sp else 22.sp
        WidthClass.MEDIUM -> if (compactHeight) 26.sp else 28.sp
        WidthClass.EXPANDED -> if (compactHeight) 30.sp else 32.sp
    }
    val listContentTopBottom = if (compactHeight) 8.dp else 12.dp
    val listItemSpacing = if (compactHeight) 8.dp else 10.dp
    val bannerPaddingVertical = if (compactHeight) 12.dp else 20.dp
    val bannerTextPaddingVertical = if (compactHeight) 10.dp else 12.dp
    val navBottomPadding = rememberSystemAwareNavBottomPadding()
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var phrasePendingDeletion by rememberSaveable { mutableStateOf<String?>(null) }

    // Stato drag & drop
    val dragDropState = remember(listState) {
        DragDropState(
            lazyListState = listState,
            phraseOffset = PHRASE_LIST_OFFSET,
            phraseCount = { uiState.allPhrases.size },
            onMove = { from, to -> viewModel.reorderPhrases(from, to) }
        )
    }

    LaunchedEffect(uiState.scanningIndex, uiState.allPhrases.size) {
        if (uiState.allPhrases.isNotEmpty()) {
            runCatching {
                listState.animateScrollToItem(
                    uiState.scanningIndex.coerceIn(0, uiState.allPhrases.lastIndex)
                )
            }.logOnFailure(TAG, "Errore scroll selezione")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
            .padding(bottom = navBottomPadding)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                modifier = Modifier.height(AppTopBarHeight),
                title = {
                    Text(
                        text = UiText.APP_TITLE,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = onOpenThemeMenu,
                        modifier = Modifier.size(appBarActionSize)
                    ) {
                        Text("🎨", fontSize = appBarEmojiSize)
                    }
                    IconButton(
                        onClick = onNavigateToSyllabicKeyboard,
                        modifier = Modifier.size(appBarActionSize)
                    ) {
                        Text(UiText.KEYBOARD_SHORTCUT, fontSize = appBarEmojiSize)
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.size(appBarActionSize)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = UiText.SETTINGS_TITLE,
                            modifier = Modifier.size(appBarIconSize)
                        )
                    }
                },
                colors = appTopBarColors()
            )

            Row(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(dragDropState) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { offset -> dragDropState.onDragStart(offset) },
                                onDragEnd   = { dragDropState.onDragEnd() },
                                onDragCancel = { dragDropState.onDragEnd() },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragDropState.onDrag(dragAmount.y)
                                }
                            )
                        },
                    contentPadding = PaddingValues(
                        start = listHorizontalPaddingStart,
                        end = listHorizontalPaddingEnd,
                        top = listContentTopBottom,
                        bottom = listContentTopBottom
                    ),
                    verticalArrangement = Arrangement.spacedBy(listItemSpacing)
                ) {
                    item {
                        FacilitatedWritingSection(
                            viewModel = viewModel,
                            synthesizer = synthesizer
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }

                    item {
                        Text(
                            text = UiText.TAP_TO_SPEAK_HINT,
                            fontSize = hintFontSize,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    item { Spacer(Modifier.height(8.dp)) }

                    items(uiState.allPhrases, key = { phrase -> phrase }) { phrase ->
                        val phraseIdx = uiState.allPhrases.indexOf(phrase)
                        val listIdx   = phraseIdx + PHRASE_LIST_OFFSET
                        val isDragging = dragDropState.draggingItemListIndex == listIdx

                        Box(
                            modifier = Modifier
                                .zIndex(if (isDragging) 1f else 0f)
                                .then(if (!isDragging) Modifier.animateItem() else Modifier)
                                .graphicsLayer {
                                    if (isDragging) {
                                        translationY = dragDropState.draggingItemOffset
                                        scaleX = 1.03f
                                        scaleY = 1.03f
                                        shadowElevation = 12f
                                    }
                                }
                        ) {
                            LargePhraseButton(
                                phrase = phrase,
                                isHighlighted = uiState.scanningIndex < uiState.allPhrases.size &&
                                    uiState.allPhrases[uiState.scanningIndex] == phrase,
                                isScanningMode = uiState.isScanningMode,
                                onClick = {
                                    runCatching {
                                        synthesizer.speak(phrase)
                                        viewModel.onPhraseUsed(phrase)
                                    }.logOnFailure(TAG, "Errore riproduzione frase")
                                },
                                showDeleteAction = true,
                                onShoutClick = {
                                    runCatching {
                                        synthesizer.speakLoud(phrase)
                                        viewModel.onPhraseUsed(phrase)
                                    }.logOnFailure(TAG, "Errore riproduzione shout frase")
                                },
                                onDeleteClick = { phrasePendingDeletion = phrase }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }

                PhraseSidebar(
                    listState = listState,
                    totalItems = uiState.allPhrases.size,
                    isScanning = uiState.isScanningMode,
                    onScanUpSingleTap = { viewModel.moveSelectionUp() },
                    onScanUpDoubleTap = { viewModel.startUpScanFromCurrentSelection() },
                    onScanDownSingleTap = { viewModel.moveSelectionDown() },
                    onScanDownDoubleTap = { viewModel.startDownScanFromCurrentSelection() },
                    widthOverride = sidebarWidth
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.isScanningMode,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { viewModel.onScreenTap() }
                    }
            ) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = bannerPaddingVertical),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = BannerShape,
                    shadowElevation = 6.dp
                ) {
                    Text(
                        text = UiText.SCANNING_BANNER,
                        modifier = Modifier.padding(vertical = bannerTextPaddingVertical, horizontal = 16.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        if (phrasePendingDeletion != null) {
            val dialogWidth = when {
                isLandscape && widthClass == WidthClass.EXPANDED -> 640.dp
                isLandscape -> 500.dp
                compactScreen -> 340.dp
                else -> 560.dp
            }
            val dialogMinHeight = if (compactScreen) 320.dp else 440.dp
            val responsiveDialogMinHeight = if (compactHeight) dialogMinHeight - 60.dp else dialogMinHeight
            val dialogTextSize = if (compactScreen || compactHeight) 18.sp else 24.sp
            val actionButtonSize = when {
                compactScreen || compactHeight -> 116.dp
                heightClass == HeightClass.MEDIUM -> 150.dp
                else -> 173.dp
            }
            val actionIconSize = when {
                compactScreen || compactHeight -> 56.dp
                heightClass == HeightClass.MEDIUM -> 74.dp
                else -> 86.dp
            }
            val actionLabelSize = if (compactScreen || compactHeight) 18.sp else 22.sp
            AlertDialog(
                modifier = Modifier
                    .width(dialogWidth)
                    .heightIn(min = responsiveDialogMinHeight),
                onDismissRequest = { phrasePendingDeletion = null },
                title = { Text(UiText.DELETE_PHRASE) },
                text = {
                    Text(
                        text = "Vuoi davvero eliminare la frase?\n\n\"${phrasePendingDeletion.orEmpty()}\"",
                        fontSize = dialogTextSize
                    )
                },
                confirmButton = {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            IconButton(
                                onClick = { phrasePendingDeletion = null },
                                modifier = Modifier.size(actionButtonSize),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "annulla",
                                    modifier = Modifier.size(actionIconSize)
                                )
                            }
                            Text("annulla", fontSize = actionLabelSize)
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            IconButton(
                                onClick = {
                                    phrasePendingDeletion?.let(viewModel::deletePhrase)
                                    phrasePendingDeletion = null
                                },
                                modifier = Modifier.size(actionButtonSize),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "elimina",
                                    modifier = Modifier.size(actionIconSize)
                                )
                            }
                            Text("elimina", fontSize = actionLabelSize)
                        }
                    }
                },
                dismissButton = {}
            )
        }
    }
}
