package com.cheffoafk.voiceassistant.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cheffoafk.voiceassistant.ui.common.HeightClass
import com.cheffoafk.voiceassistant.ui.common.UiText
import com.cheffoafk.voiceassistant.ui.common.WidthClass
import com.cheffoafk.voiceassistant.ui.common.rememberHeightClass
import com.cheffoafk.voiceassistant.ui.common.rememberWidthClass
import com.cheffoafk.voiceassistant.ui.theme.PillShape

/**
 * Bottone grande usato nella lista frasi della Home.
 *
 * @param phrase  Testo della frase da mostrare e riprodurre.
 * @param isHighlighted  True se il cursore di scansione è su questo item.
 * @param isScanningMode  True se la scansione automatica è attiva.
 * @param onClick  Callback alla pressione.
 */
@Composable
fun LargePhraseButton(
    phrase: String,
    isHighlighted: Boolean,
    isScanningMode: Boolean,
    onClick: () -> Unit,
    showDeleteAction: Boolean,
    onShoutClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val compact = rememberWidthClass() == WidthClass.COMPACT
    val compactHeight = rememberHeightClass() == HeightClass.COMPACT
    val buttonHeight = when {
        compact -> 58.dp
        compactHeight -> 60.dp
        else -> 64.dp
    }
    val phraseFontSize = if (compact) 18.sp else 20.sp
    val shoutSize = when {
        compact -> 42.dp
        compactHeight -> 44.dp
        else -> 46.dp
    }
    val deleteSize = when {
        compact -> 46.dp
        compactHeight -> 48.dp
        else -> 52.dp
    }
    val actionGap = if (compact) 8.dp else 10.dp
    val actionEdgePadding = if (compact) 8.dp else 10.dp
    val reservedTextEnd = if (showDeleteAction) {
        (actionEdgePadding * 2) + shoutSize + deleteSize + actionGap
    } else {
        0.dp
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(buttonHeight)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight),
            shape = PillShape,
            contentPadding = PaddingValues(horizontal = if (compact) 16.dp else 20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = when {
                    isHighlighted && isScanningMode -> MaterialTheme.colorScheme.error
                    isHighlighted -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.primary
                },
                contentColor = when {
                    isHighlighted && isScanningMode -> MaterialTheme.colorScheme.onError
                    isHighlighted -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onPrimary
                }
            )
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = phrase,
                    fontSize = phraseFontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        // Riserva spazio dinamico per i due action button evitando sovrapposizioni su schermi stretti.
                        .padding(end = reservedTextEnd)
                )
            }
        }

        if (showDeleteAction) {
            val actionContainerColor = MaterialTheme.colorScheme.secondaryContainer
            val actionContentColor = MaterialTheme.colorScheme.onSecondaryContainer
            Row(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.CenterEnd)
                    .padding(end = actionEdgePadding),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(actionGap),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onShoutClick,
                    modifier = Modifier.size(shoutSize),
                    colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                        containerColor = actionContainerColor,
                        contentColor = actionContentColor
                    )
                ) {
                    Text(
                        text = UiText.SHOUT_ICON,
                        fontSize = if (compact) 18.sp else 20.sp
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(deleteSize),
                    colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                        containerColor = actionContainerColor,
                        contentColor = actionContentColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = UiText.DELETE_PHRASE,
                        modifier = Modifier.size(if (compact) 20.dp else 24.dp)
                    )
                }
            }
        }
    }
}
