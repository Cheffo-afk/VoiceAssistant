package com.cheffoafk.voiceassistant.data

import org.json.JSONArray

internal object PhrasePersistenceCodec {
    fun encode(phrases: List<String>): String {
        val array = JSONArray()
        phrases.forEach { phrase ->
            array.put(phrase)
        }
        return array.toString()
    }

    fun decode(rawValue: String?): List<String> {
        if (rawValue.isNullOrBlank()) return emptyList()

        val array = JSONArray(rawValue)
        val result = mutableListOf<String>()
        for (index in 0 until array.length()) {
            val phrase = array.optString(index).trim()
            if (phrase.isNotEmpty() && phrase !in result) {
                result.add(phrase)
            }
        }
        return result
    }
}
