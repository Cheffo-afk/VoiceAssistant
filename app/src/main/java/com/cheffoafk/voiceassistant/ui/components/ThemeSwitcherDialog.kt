package com.cheffoafk.voiceassistant.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cheffoafk.voiceassistant.ui.theme.PaletteOption
import com.cheffoafk.voiceassistant.ui.theme.ThemePreferences
import com.cheffoafk.voiceassistant.ui.theme.paletteDefinition
import com.cheffoafk.voiceassistant.ui.common.HeightClass
import com.cheffoafk.voiceassistant.ui.common.WidthClass
import com.cheffoafk.voiceassistant.ui.common.rememberHeightClass
import com.cheffoafk.voiceassistant.ui.common.rememberWidthClass
import com.cheffoafk.voiceassistant.ui.common.rememberSystemAwareNavBottomPadding

@Composable
fun ThemeSwitcherDialog(
    currentPreferences: ThemePreferences,
    onDismiss: () -> Unit,
    onDarkModeChanged: (Boolean) -> Unit,
    onPaletteSelected: (PaletteOption) -> Unit
) {
    val options = PaletteOption.entries
    val widthClass = rememberWidthClass()
    val heightClass = rememberHeightClass()
    val compact = widthClass == WidthClass.COMPACT
    val compactHeight = heightClass == HeightClass.COMPACT
    val buttonHeight = when {
        compact -> 74.dp
        compactHeight -> 76.dp
        else -> 82.dp
    }
    val previewStripeHeight = if (compact) 40.dp else 46.dp
    val labelSize = if (compact) 21.sp else 25.sp
    val confirmSize = if (compact) 20.sp else 24.sp
    val dialogHorizontalInset = if (compact) 4.dp else 8.dp
    val maxListHeight = when {
        compact -> 320.dp
        compactHeight -> 360.dp
        else -> 480.dp
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact || compactHeight) 64.dp else 72.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32),
                    contentColor = Color.White
                )
            ) {
                Text("CONFERMA ✓", fontSize = confirmSize)
            }
        },
        dismissButton = {},
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tema Notte/Giorno",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = currentPreferences.isDarkMode,
                        onCheckedChange = onDarkModeChanged,
                        modifier = Modifier.scale(if (compact) 1.15f else 1.25f)
                    )
                }

                LazyColumn(
                    modifier = Modifier.heightIn(max = maxListHeight),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(options) { option ->
                        val isSelected = option == currentPreferences.palette
                        val palette = paletteDefinition(option)
                        val previewBackground = palette?.background ?: MaterialTheme.colorScheme.surfaceVariant
                        val previewPrimary = palette?.primary ?: MaterialTheme.colorScheme.primary
                        val previewText = palette?.onPrimary ?: contrastTextFor(previewBackground)
                        val previewAccent = palette?.accent ?: MaterialTheme.colorScheme.secondary

                        Button(
                            onClick = { onPaletteSelected(option) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(buttonHeight),
                            border = if (isSelected) {
                                BorderStroke(4.dp, MaterialTheme.colorScheme.tertiary)
                            } else {
                                BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = previewBackground,
                                contentColor = previewText
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(previewStripeHeight)
                                            .background(previewBackground)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(previewStripeHeight)
                                            .background(previewPrimary)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(previewStripeHeight)
                                            .background(previewAccent)
                                    )
                                }

                                Text(
                                    text = option.label,
                                    fontSize = labelSize,
                                    color = contrastTextFor(previewBackground),
                                    modifier = Modifier.weight(1f)
                                )

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selezionata",
                                        modifier = Modifier.size(34.dp),
                                        tint = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        modifier = Modifier
            .padding(dialogHorizontalInset)
            .widthIn(max = if (compact) 360.dp else 640.dp)
    )
}

private fun contrastTextFor(background: Color): Color {
    return if (background.luminance() > 0.55f) Color(0xFF1C1C1E) else Color.White
}
