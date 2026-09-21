package com.cheffoafk.voiceassistant.ui

import com.cheffoafk.voiceassistant.data.FrequencyAutocompleteEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyllabicKeyboardViewModelTest {

    @Test
    fun appendSyllable_updatesSuggestionsForCurrentWord() {
        val vm = SyllabicKeyboardViewModel(
            autocompleteEngine = FrequencyAutocompleteEngine(
                dictionary = listOf("acqua", "aiuto", "amico")
            )
        )

        vm.appendSyllable("A")

        assertEquals(listOf("acqua", "aiuto", "amico"), vm.uiState.value.suggestions)
    }

    @Test
    fun applySuggestion_replacesCurrentWord() {
        val vm = SyllabicKeyboardViewModel(
            autocompleteEngine = FrequencyAutocompleteEngine(
                dictionary = listOf("acqua", "aiuto")
            )
        )

        vm.appendSyllable("A")
        vm.appendSpace()
        vm.appendSyllable("A")
        vm.applySuggestion("aiuto")

        assertEquals("A aiuto", vm.uiState.value.composedText)
    }

    @Test
    fun clear_resetsSuggestions() {
        val vm = SyllabicKeyboardViewModel(
            autocompleteEngine = FrequencyAutocompleteEngine(
                dictionary = listOf("acqua")
            )
        )

        vm.appendSyllable("A")
        assertTrue(vm.uiState.value.suggestions.isNotEmpty())

        vm.clear()

        assertTrue(vm.uiState.value.suggestions.isEmpty())
    }
}

