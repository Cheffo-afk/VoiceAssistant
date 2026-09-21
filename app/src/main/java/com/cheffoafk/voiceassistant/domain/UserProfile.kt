package com.cheffoafk.voiceassistant.domain

data class UserProfile(
    val favoriteWords: List<String> = emptyList(),
    val phraseFrequency: Map<String, Int> = emptyMap(),
    val tonePreference: String = "neutrale",
    val scanningIntervalSeconds: Int = 4
)

