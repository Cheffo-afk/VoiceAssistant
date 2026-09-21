package com.cheffoafk.voiceassistant.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.cheffoafk.voiceassistant.domain.UserProfile
import org.json.JSONObject

private const val PREFS_NAME = "user_profile_prefs"
private const val KEY_USER_PROFILE = "user_profile_json"

interface UserProfileDataSource {
    fun getUserProfile(): UserProfile
    fun updateScanningInterval(seconds: Int)
    fun recordPhraseUse(phrase: String)
}

class UserProfileRepository(context: Context) : UserProfileDataSource {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getUserProfile(): UserProfile {
        return decodeProfile(preferences.getString(KEY_USER_PROFILE, null))
    }

    override fun updateScanningInterval(seconds: Int) {
        val current = getUserProfile()
        val updated = current.copy(scanningIntervalSeconds = seconds)
        saveProfile(updated)
    }

    override fun recordPhraseUse(phrase: String) {
        val current = getUserProfile()
        val updated = current.copy(
            phraseFrequency = (current.phraseFrequency + (phrase to (current.phraseFrequency[phrase] ?: 0) + 1))
        )
        saveProfile(updated)
    }

    private fun saveProfile(profile: UserProfile) {
        val json = JSONObject().apply {
            put("scanningIntervalSeconds", profile.scanningIntervalSeconds)
            put("tonePreference", profile.tonePreference)
        }
        preferences.edit {
            putString(KEY_USER_PROFILE, json.toString())
        }
    }

    private fun decodeProfile(rawValue: String?): UserProfile {
        if (rawValue.isNullOrBlank()) return UserProfile()
        return try {
            val json = JSONObject(rawValue)
            UserProfile(
                scanningIntervalSeconds = json.optInt("scanningIntervalSeconds", 4),
                tonePreference = json.optString("tonePreference", "neutrale")
            )
        } catch (_: Exception) {
            UserProfile()
        }
    }
}


