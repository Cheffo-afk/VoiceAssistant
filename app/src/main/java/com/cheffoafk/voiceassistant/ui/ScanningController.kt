package com.cheffoafk.voiceassistant.ui

/**
 * Incapsula la logica di navigazione ciclica della selezione durante la scansione.
 * E indipendente da coroutine/UI cosi puo essere testata in isolamento.
 */
class ScanningController {

    fun moveDown(currentIndex: Int, size: Int): Int = move(currentIndex, size, ScanDirection.DOWN)

    fun moveUp(currentIndex: Int, size: Int): Int = move(currentIndex, size, ScanDirection.UP)

    fun move(currentIndex: Int, size: Int, direction: ScanDirection): Int {
        if (size <= 0) return 0
        val normalized = normalizeIndex(currentIndex, size)
        return when (direction) {
            ScanDirection.DOWN -> (normalized + 1) % size
            ScanDirection.UP -> (normalized - 1 + size) % size
        }
    }

    fun normalizeIndex(index: Int, size: Int): Int {
        if (size <= 0) return 0
        return index.mod(size)
    }
}
