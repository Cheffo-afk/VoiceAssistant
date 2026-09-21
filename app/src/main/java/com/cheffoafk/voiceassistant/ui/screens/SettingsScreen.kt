package com.cheffoafk.voiceassistant.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cheffoafk.voiceassistant.domain.SpeechSynthesizer
import com.cheffoafk.voiceassistant.ui.HomeScreenViewModel
import com.cheffoafk.voiceassistant.ui.common.AppLogger
import com.cheffoafk.voiceassistant.ui.common.HeightClass
import com.cheffoafk.voiceassistant.ui.common.UiText
import com.cheffoafk.voiceassistant.ui.common.WidthClass
import com.cheffoafk.voiceassistant.ui.common.rememberHeightClass
import com.cheffoafk.voiceassistant.ui.common.rememberWidthClass
import com.cheffoafk.voiceassistant.ui.common.rememberSystemAwareNavBottomPadding
import com.cheffoafk.voiceassistant.ui.components.AppTopBarHeight
import com.cheffoafk.voiceassistant.ui.components.AppSectionCard
import com.cheffoafk.voiceassistant.ui.components.appTopBarColors

private const val TAG = "SettingsScreen"

private data class SettingsSection(
    val title: String,
    val description: String,
    val actionLabel: String,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: HomeScreenViewModel,
    synthesizer: SpeechSynthesizer,
    shoutGainMb: Int,
    onShoutGainChanged: (Int) -> Unit,
    isRightHanded: Boolean,
    onRightHandedChanged: (Boolean) -> Unit,
    onBack: () -> Unit,
    onRepeatOnboarding: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val widthClass = rememberWidthClass()
    val heightClass = rememberHeightClass()
    val compact = widthClass == WidthClass.COMPACT
    val compactHeight = heightClass == HeightClass.COMPACT
    val adjustButtonWidth = if (widthClass == WidthClass.EXPANDED) 80.dp else 60.dp
    val adjustButtonHeight = if (compactHeight) 44.dp else 48.dp
    val sectionSpacing = if (compactHeight) 12.dp else 16.dp
    val valueHorizontalPadding = if (compactHeight) 16.dp else 24.dp
    val navBottomPadding = rememberSystemAwareNavBottomPadding()
    val horizontalPadding = when (widthClass) {
        WidthClass.COMPACT -> 10.dp
        WidthClass.MEDIUM -> 16.dp
        WidthClass.EXPANDED -> 24.dp
    }

    val sections = listOf(
        SettingsSection(
            title = "Voce del dispositivo",
            description = "L'app usa il motore TTS già presente sul telefono.",
            actionLabel = "Apri impostazioni voce",
            onClick = {
                    // Alcuni device (Samsung, Xiaomi…) non espongono l'intent TTS_SETTINGS:
                    // proviamo più varianti in cascata, con fallback alle impostazioni generali.
                    val candidates = listOf(
                        "android.settings.TTS_SETTINGS",
                        "com.android.settings.TTS_SETTINGS"
                    )
                    val opened = candidates.any { action ->
                        runCatching {
                            context.startActivity(
                                Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }.isSuccess
                    }
                    if (!opened) {
                        // Fallback: impostazioni di accessibilità (voce è spesso lì) o generali.
                        val fallback = runCatching {
                            context.startActivity(
                                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                        if (fallback.isFailure) {
                            runCatching {
                                context.startActivity(
                                    Intent(Settings.ACTION_SETTINGS)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        }
                        Toast.makeText(
                            context,
                            "Impostazioni voce non disponibili su questo dispositivo.\nCerca \"sintesi vocale\" nelle impostazioni.",
                            Toast.LENGTH_LONG
                        ).show()
                        AppLogger.error(TAG, "Schermata TTS_SETTINGS non trovata, aperto fallback")
                    }
                }
        )
    )

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
                    UiText.SETTINGS_TITLE,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Torna indietro"
                    )
                }
            },
            colors = appTopBarColors()
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding),
            contentPadding = PaddingValues(vertical = if (compact || compactHeight) 12.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(sectionSpacing)
        ) {
            item {
                AppSectionCard(
                    title = "Intervallo scansione",
                    description = "Tempo di attesa prima che la scansione avanzi alla frase successiva."
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (uiState.scanningIntervalSeconds > 1) {
                                    viewModel.updateScanningInterval(uiState.scanningIntervalSeconds - 1)
                                }
                            },
                            modifier = Modifier
                                .width(adjustButtonWidth)
                                .height(adjustButtonHeight),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("◀")
                        }

                        Text(
                            text = "${uiState.scanningIntervalSeconds} s",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = valueHorizontalPadding)
                        )

                        Button(
                            onClick = {
                                if (uiState.scanningIntervalSeconds < 10) {
                                    viewModel.updateScanningInterval(uiState.scanningIntervalSeconds + 1)
                                }
                            },
                            modifier = Modifier
                                .width(adjustButtonWidth)
                                .height(adjustButtonHeight),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("▶")
                        }
                    }
                }
            }

            item {
                AppSectionCard(
                    title = "Intensita SHOUT",
                    description = "Regola il boost LoudnessEnhancer sopra i livelli di sicurezza. Riduci se senti gracchiamenti sul dispositivo."
                ) {
                    val minGain = 300
                    val maxGain = 2400
                    val step = 100
                    val gainPercent = ((shoutGainMb.toFloat() / maxGain.toFloat()) * 100f).toInt()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                onShoutGainChanged((shoutGainMb - step).coerceAtLeast(minGain))
                            },
                            modifier = Modifier
                                .width(adjustButtonWidth)
                                .height(adjustButtonHeight),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("◀")
                        }

                        Text(
                            text = "$shoutGainMb mB ($gainPercent%)",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = valueHorizontalPadding)
                        )

                        Button(
                            onClick = {
                                onShoutGainChanged((shoutGainMb + step).coerceAtMost(maxGain))
                            },
                            modifier = Modifier
                                .width(adjustButtonWidth)
                                .height(adjustButtonHeight),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("▶")
                        }
                    }

                    Button(
                        onClick = {
                            runCatching { synthesizer.speakLoudPreview("PROVA") }
                                .onFailure { AppLogger.error(TAG, "Errore anteprima volume SHOUT", it) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("PROVA")
                    }
                }
            }

            item {
                AppSectionCard(
                    title = "Area caregiver",
                    description = "Configura disposizione comandi per destro/mancino e riavvia la guida iniziale."
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isRightHanded) "Modalita destro" else "Modalita mancino",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = isRightHanded,
                            onCheckedChange = onRightHandedChanged
                        )
                    }

                    Button(
                        onClick = onRepeatOnboarding,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ripeti onboarding iniziale")
                    }
                }
            }

            items(sections) { section ->
                AppSectionCard(
                    title = section.title,
                    description = section.description
                ) {
                    Button(
                        onClick = section.onClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(section.actionLabel)
                    }
                }
            }
        }
    }
}
