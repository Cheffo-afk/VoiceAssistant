package com.cheffoafk.voiceassistant.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ScanningControllerTest {

    private val controller = ScanningController()

    @Test
    fun moveDown_wrapsAtEnd() {
        val next = controller.moveDown(currentIndex = 2, size = 3)
        assertEquals(0, next)
    }

    @Test
    fun moveUp_wrapsAtStart() {
        val next = controller.moveUp(currentIndex = 0, size = 3)
        assertEquals(2, next)
    }

    @Test
    fun move_normalizesOutOfRangeIndex() {
        val next = controller.move(currentIndex = 7, size = 3, direction = ScanDirection.DOWN)
        assertEquals(2, next)
    }

    @Test
    fun normalizeIndex_handlesNegativeValues() {
        val normalized = controller.normalizeIndex(index = -1, size = 3)
        assertEquals(2, normalized)
    }

    @Test
    fun move_withEmptyList_returnsZero() {
        val next = controller.move(currentIndex = 4, size = 0, direction = ScanDirection.UP)
        assertEquals(0, next)
    }
}
