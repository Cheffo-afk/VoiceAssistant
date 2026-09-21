package com.cheffoafk.voiceassistant

import com.cheffoafk.voiceassistant.data.FrequencyAutocompleteEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class FrequencyAutocompleteEngineTest {

    @Test
    fun suggest_returnsPrefixMatches() {
        val engine = FrequencyAutocompleteEngine(
            dictionary = listOf("acqua", "aiuto", "ciao", "grazie")
        )

        val suggestions = engine.suggest("a")

        assertEquals(listOf("acqua", "aiuto"), suggestions)
    }

    @Test
    fun registerUse_prioritizesUsedWords() {
        val engine = FrequencyAutocompleteEngine(
            dictionary = listOf("acqua", "aiuto", "amico")
        )
        engine.registerUse("aiuto")
        engine.registerUse("aiuto")

        val suggestions = engine.suggest("a")

        assertEquals("aiuto", suggestions.first())
    }

    @Test
    fun suggest_usesExternalUsageCountsWhenProvided() {
        val engine = FrequencyAutocompleteEngine(
            dictionary = listOf("acqua", "aiuto", "amico")
        )

        val suggestions = engine.suggest(
            input = "a",
            usageCounts = mapOf("aiuto" to 3)
        )

        assertEquals(listOf("aiuto", "acqua", "amico"), suggestions)
    }
}

