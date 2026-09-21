package com.cheffoafk.voiceassistant.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

private const val COMPACT_MAX_DP = 359
private const val MEDIUM_MAX_DP = 599
private const val COMPACT_HEIGHT_MAX_DP = 699
private const val MEDIUM_HEIGHT_MAX_DP = 899

enum class WidthClass {
    COMPACT,
    MEDIUM,
    EXPANDED
}

enum class HeightClass {
    COMPACT,
    MEDIUM,
    EXPANDED
}

@Composable
fun rememberEffectiveWidthDp(): Int {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val widthPx = windowInfo.containerSize.width
    return remember(widthPx, density) {
        with(density) { widthPx.toDp().value.toInt() }
    }
}

@Composable
fun rememberWidthClass(): WidthClass {
    val widthDp = rememberEffectiveWidthDp()
    return remember(widthDp) {
        when {
            widthDp <= COMPACT_MAX_DP -> WidthClass.COMPACT
            widthDp <= MEDIUM_MAX_DP -> WidthClass.MEDIUM
            else -> WidthClass.EXPANDED
        }
    }
}

@Composable
fun rememberEffectiveHeightDp(): Int {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val heightPx = windowInfo.containerSize.height
    return remember(heightPx, density) {
        with(density) { heightPx.toDp().value.toInt() }
    }
}

@Composable
fun rememberHeightClass(): HeightClass {
    val heightDp = rememberEffectiveHeightDp()
    return remember(heightDp) {
        when {
            heightDp <= COMPACT_HEIGHT_MAX_DP -> HeightClass.COMPACT
            heightDp <= MEDIUM_HEIGHT_MAX_DP -> HeightClass.MEDIUM
            else -> HeightClass.EXPANDED
        }
    }
}

@Composable
fun rememberSystemAwareNavBottomPadding(): Dp {
    val density = LocalDensity.current
    val view = LocalView.current
    val insets = ViewCompat.getRootWindowInsets(view)
    val visibleBottom = insets
        ?.getInsets(WindowInsetsCompat.Type.navigationBars())
        ?.bottom
        ?: 0
    val stableBottom = insets
        ?.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.navigationBars())
        ?.bottom
        ?: 0

    // Reserve space only when the navigation bar is currently visible and behaves as stable inset.
    val reserveBottom = visibleBottom > 0 && visibleBottom == stableBottom
    return if (reserveBottom) with(density) { visibleBottom.toDp() } else 0.dp
}



