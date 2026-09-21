package com.cheffoafk.voiceassistant.data

import com.cheffoafk.voiceassistant.domain.AutocompleteEngine

class FrequencyAutocompleteEngine(
    private val dictionary: List<String>,
    private val usageMap: MutableMap<String, Int> = mutableMapOf()
) : AutocompleteEngine {

    override fun suggest(input: String, limit: Int): List<String> =
        suggest(input = input, limit = limit, usageCounts = usageMap)

    fun suggest(
        input: String,
        limit: Int = 6,
        usageCounts: Map<String, Int> = usageMap
    ): List<String> {
        val query = input.trim().lowercase()
        if (query.isEmpty()) return emptyList()

        return dictionary
            .asSequence()
            .filter { it.lowercase().startsWith(query) }
            .sortedWith(compareByDescending<String> { usageCounts[it] ?: 0 }.thenBy { it })
            .take(limit)
            .toList()
    }

    fun registerUse(word: String) {
        val normalized = word.trim().lowercase()
        if (normalized.isBlank()) return
        usageMap[normalized] = (usageMap[normalized] ?: 0) + 1
    }

    fun setUsageCounts(usageCounts: Map<String, Int>) {
        usageMap.clear()
        usageMap.putAll(
            usageCounts
                .mapKeys { it.key.trim().lowercase() }
                .filterValues { it > 0 }
        )
    }
}


