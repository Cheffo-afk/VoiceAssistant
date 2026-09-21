package com.cheffoafk.voiceassistant.ui.screens

import com.cheffoafk.voiceassistant.domain.SyllabicGroup
import org.junit.Assert.assertEquals
import org.junit.Test

class SyllabicKeyboardComponentsTest {

    @Test
    fun syllableToCompositionText_keepsTheFullDisplayedSyllable() {
        assertEquals("CA", syllableToCompositionText("CA"))
        assertEquals("CHE", syllableToCompositionText("CHE"))
        assertEquals("QUA", syllableToCompositionText("QUA"))
        assertEquals("GLI", syllableToCompositionText("GLI"))
    }

    @Test
    fun buildDisplayedSyllables_putsSingleConsonantFirst() {
        val group = SyllabicGroup("C", listOf("CA", "CE", "CI", "CO", "CU"))

        assertEquals(listOf("C", "CA", "CE", "CI", "CO", "CU"), buildDisplayedSyllables(group))
    }

    @Test
    fun buildDisplayedSyllables_doesNotDuplicateWhenLabelAlreadyInSyllables() {
        val group = SyllabicGroup("K", listOf("K", "KA", "KE", "KI", "KO", "KU"))

        assertEquals(listOf("K", "KA", "KE", "KI", "KO", "KU"), buildDisplayedSyllables(group))
    }
}


