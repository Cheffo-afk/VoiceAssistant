package com.cheffoafk.voiceassistant.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class PaletteOption(val key: String, val label: String) {
    BASE("base", "Base"),
    CALMA("calma", "Calma"),
    ALTO_CONTRASTO("alto_contrasto", "Alto Contrasto"),
    RELAX("relax", "Relax"),
    SOFT_CANDY("soft_candy", "Soft Candy"),
    GIORNO_CHIARA("giorno_chiara", "Clear Day");

    companion object {
        fun fromKey(value: String?): PaletteOption = entries.firstOrNull { it.key == value } ?: BASE
    }
}

data class ThemePreferences(
    val isDarkMode: Boolean = false,
    val palette: PaletteOption = PaletteOption.BASE
)

data class PaletteDefinition(
    val background: Color,
    val primary: Color,
    val onPrimary: Color,
    val accent: Color
)

fun paletteDefinition(option: PaletteOption): PaletteDefinition? = when (option) {
    PaletteOption.BASE -> null
    PaletteOption.CALMA -> PaletteDefinition(
        background = Color(0xFF1A2530),
        primary = Color(0xFF4A90E2),
        onPrimary = Color(0xFFFFFFFF),
        accent = Color(0xFFF5A623)
    )

    PaletteOption.ALTO_CONTRASTO -> PaletteDefinition(
        background = Color(0xFF000000),
        primary = Color(0xFFFFD700),
        onPrimary = Color(0xFFFFFFFF),
        accent = Color(0xFFFF6B6B)
    )

    PaletteOption.RELAX -> PaletteDefinition(
        background = Color(0xFF1B4332),
        primary = Color(0xFF52B788),
        onPrimary = Color(0xFFF8F9FA),
        accent = Color(0xFFFFC300)
    )

    PaletteOption.SOFT_CANDY -> PaletteDefinition(
        background = Color(0xFF2B2D42),
        primary = Color(0xFF9B5DE5),
        onPrimary = Color(0xFFFFFFFF),
        accent = Color(0xFFF15BB5)
    )

    PaletteOption.GIORNO_CHIARA -> PaletteDefinition(
        background = Color(0xFFF4F6F9),
        primary = Color(0xFF0F4C81),
        onPrimary = Color(0xFF1C1C1E),
        accent = Color(0xFFE63946)
    )
}

fun buildPaletteColorScheme(option: PaletteOption, darkMode: Boolean): ColorScheme? {
    val palette = paletteDefinition(option) ?: return null

    return if (darkMode) {
        darkColorScheme(
            primary = palette.primary,
            onPrimary = palette.onPrimary,
            secondary = palette.accent,
            onSecondary = Color.White,
            tertiary = palette.accent,
            onTertiary = Color.White,
            background = palette.background,
            onBackground = palette.onPrimary,
            surface = palette.background,
            onSurface = palette.onPrimary,
            surfaceVariant = palette.background.copy(alpha = 0.92f),
            onSurfaceVariant = palette.onPrimary,
            error = palette.accent,
            onError = Color.White,
            outline = palette.accent
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            onPrimary = palette.onPrimary,
            secondary = palette.accent,
            onSecondary = Color.White,
            tertiary = palette.accent,
            onTertiary = Color.White,
            background = palette.background,
            onBackground = palette.onPrimary,
            surface = palette.background,
            onSurface = palette.onPrimary,
            surfaceVariant = palette.background.copy(alpha = 0.96f),
            onSurfaceVariant = palette.onPrimary,
            error = palette.accent,
            onError = Color.White,
            outline = palette.accent
        )
    }
}
