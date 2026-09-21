package com.cheffoafk.voiceassistant.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset

/**
 * Stato per il drag & drop su una [LazyListState].
 *
 * @param lazyListState  stato della LazyColumn
 * @param phraseOffset   numero di item non-frase prima dei bottoni (header items)
 * @param phraseCount    lambda che ritorna la dimensione corrente di allPhrases
 * @param onMove         callback con indici *frase* (0-based) da scambiare
 */
class DragDropState(
    val lazyListState: LazyListState,
    private val phraseOffset: Int,
    private val phraseCount: () -> Int,
    private val onMove: (from: Int, to: Int) -> Unit
) {
    /** Indice LazyList dell'item in trascinamento, null se non si sta trascinando. */
    var draggingItemListIndex: Int? by mutableStateOf(null)
        private set

    /** Offset visivo verticale accumulato in pixel durante il drag. */
    var draggingItemOffset: Float by mutableFloatStateOf(0f)
        private set

    val isDragging: Boolean get() = draggingItemListIndex != null

    /** Chiamato all'inizio del long-press drag. */
    fun onDragStart(startOffset: Offset) {
        val count = phraseCount()
        lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { item ->
                item.index in phraseOffset until (phraseOffset + count) &&
                    startOffset.y.toInt() in item.offset..(item.offset + item.size)
            }
            ?.also {
                draggingItemListIndex = it.index
                draggingItemOffset = 0f
            }
    }

    /** Chiamato al rilascio o all'interruzione del drag. */
    fun onDragEnd() {
        draggingItemListIndex = null
        draggingItemOffset = 0f
    }

    /** Chiamato ad ogni delta di movimento; aggiorna l'offset e controlla lo swap. */
    fun onDrag(deltaY: Float) {
        draggingItemOffset += deltaY
        checkForReorder()
    }

    private fun checkForReorder() {
        val listIndex = draggingItemListIndex ?: return
        val itemInfo = lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == listIndex } ?: return

        val visualCenter =
            itemInfo.offset.toFloat() + draggingItemOffset + itemInfo.size / 2f
        val count = phraseCount()

        val targetItem = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { item ->
            item.index in phraseOffset until (phraseOffset + count) &&
                item.index != listIndex &&
                visualCenter.toInt() in item.offset..(item.offset + item.size)
        } ?: return

        val fromPhrase = listIndex - phraseOffset
        val toPhrase = targetItem.index - phraseOffset

        onMove(fromPhrase, toPhrase)
        // Aggiusta l'offset per mantenere la posizione visiva dopo lo swap
        draggingItemOffset += itemInfo.offset - targetItem.offset
        draggingItemListIndex = targetItem.index
    }
}

