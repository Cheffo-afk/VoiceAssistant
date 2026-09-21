package com.cheffoafk.voiceassistant.domain

interface AutocompleteEngine {
    fun suggest(input: String, limit: Int = 6): List<String>
}

