package com.cheffoafk.voiceassistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import com.cheffoafk.voiceassistant.ui.theme.ArrowButtonShape
import com.cheffoafk.voiceassistant.ui.theme.ScrollbarShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cheffoafk.voiceassistant.ui.common.logOnFailure
import com.cheffoafk.voiceassistant.ui.common.rememberHeightClass
import com.cheffoafk.voiceassistant.ui.common.rememberWidthClass
import com.cheffoafk.voiceassistant.ui.common.HeightClass
import com.cheffoafk.voiceassistant.ui.common.WidthClass
import kotlinx.coroutines.launch

private const val TAG = "PhraseSidebar"

@Composable
fun PhraseSidebar(
    listState: LazyListState,
    totalItems: Int,
    isScanning: Boolean,
    onScanUpSingleTap: () -> Unit,
    onScanUpDoubleTap: () -> Unit,
    onScanDownSingleTap: () -> Unit,
    onScanDownDoubleTap: () -> Unit,
    widthOverride: Dp? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val widthClass = rememberWidthClass()
    val compactHeight = rememberHeightClass() == HeightClass.COMPACT
    val compact = widthClass == WidthClass.COMPACT
    val sidebarWidth = widthOverride ?: when (widthClass) {
        WidthClass.COMPACT -> 44.dp
        WidthClass.MEDIUM -> 52.dp
        WidthClass.EXPANDED -> 64.dp
    }
    val arrowHeight = when {
        compact -> 44.dp
        compactHeight -> 46.dp
        else -> 52.dp
    }
    val arrowPadding = when {
        compact -> 48.dp
        compactHeight -> 50.dp
        else -> 56.dp
    }
    val arrowFontSize = if (compact) 18.sp else 20.sp

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(sidebarWidth)
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        ScanArrowButton(
            symbol = "▲",
            isScanning = isScanning,
            fontSize = arrowFontSize,
            onSingleTap = onScanUpSingleTap,
            onDoubleTap = onScanUpDoubleTap,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = arrowPadding, bottom = arrowPadding)
        ) {
            val trackHeightPx = constraints.maxHeight.toFloat()
            val minThumbPx = with(density) { 40.dp.toPx() }

            val metrics by remember {
                derivedStateOf {
                    val info = listState.layoutInfo
                    val total = info.totalItemsCount.coerceAtLeast(1)
                    val visible = info.visibleItemsInfo.size.coerceAtLeast(1)
                    val thumbHeight = (visible.toFloat() / total * trackHeightPx)
                        .coerceAtLeast(minThumbPx)
                        .coerceAtMost(trackHeightPx)
                    val range = (total - visible).coerceAtLeast(1)
                    val fraction = listState.firstVisibleItemIndex.toFloat() / range
                    val offset = fraction * (trackHeightPx - thumbHeight)
                    thumbHeight to offset
                }
            }

            val (thumbHeightPx, thumbOffsetPx) = metrics

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = ScrollbarShape
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(with(density) { thumbHeightPx.toDp() })
                    .offset(y = with(density) { thumbOffsetPx.toDp() })
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                        shape = ScrollbarShape
                    )
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            coroutineScope.launch {
                                runCatching {
                                    val visibleCount = listState.layoutInfo.visibleItemsInfo.size
                                    val range = (totalItems - visibleCount).coerceAtLeast(1)
                                    val movableTrack = trackHeightPx - thumbHeightPx
                                    if (movableTrack > 0f) {
                                        val itemsPerPx = range.toFloat() / movableTrack
                                        val newIndex = (listState.firstVisibleItemIndex +
                                            (delta * itemsPerPx).toInt())
                                            .coerceIn(0, range)
                                        listState.scrollToItem(newIndex)
                                    }
                                }.logOnFailure(TAG, "Errore drag scrollbar")
                            }
                        }
                    )
            )
        }

        ScanArrowButton(
            symbol = "▼",
            isScanning = isScanning,
            fontSize = arrowFontSize,
            onSingleTap = onScanDownSingleTap,
            onDoubleTap = onScanDownDoubleTap,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(arrowHeight)
        )
    }
}

@Composable
private fun ScanArrowButton(
    symbol: String,
    isScanning: Boolean,
    fontSize: androidx.compose.ui.unit.TextUnit,
    onSingleTap: () -> Unit,
    onDoubleTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .combinedClickable(
                onClick = onSingleTap,
                onDoubleClick = onDoubleTap
            ),
        shape = ArrowButtonShape,
        color = if (isScanning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
        contentColor = if (isScanning) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSecondary
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(symbol, fontSize = fontSize, fontWeight = FontWeight.Bold)
        }
    }
}
