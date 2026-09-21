package com.cheffoafk.voiceassistant.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

private const val DATASTORE_NAME = "suggestion_usage_prefs"
private val Context.suggestionUsageDataStore by preferencesDataStore(name = DATASTORE_NAME)
private val KEY_USAGE_COUNTS_JSON = stringPreferencesKey("usage_counts_json")

interface SuggestionUsageDataSource {
    val usageCounts: StateFlow<Map<String, Int>>
    fun recordUsage(word: String)
}

class SuggestionUsageRepository(context: Context) : SuggestionUsageDataSource {
    private val dataStore = context.suggestionUsageDataStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _usageCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    override val usageCounts: StateFlow<Map<String, Int>> = _usageCounts.asStateFlow()

    init {
        scope.launch {
            dataStore.data.collect { preferences ->
                _usageCounts.value = decodeUsageCounts(preferences[KEY_USAGE_COUNTS_JSON])
            }
        }
    }

    override fun recordUsage(word: String) {
        val normalized = word.trim().lowercase()
        if (normalized.isBlank()) return

        val updated = _usageCounts.value.toMutableMap().apply {
            put(normalized, (get(normalized) ?: 0) + 1)
        }.toMap()

        _usageCounts.value = updated
        scope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_USAGE_COUNTS_JSON] = encodeUsageCounts(updated)
            }
        }
    }

    private fun encodeUsageCounts(counts: Map<String, Int>): String =
        JSONObject(counts).toString()

    private fun decodeUsageCounts(rawValue: String?): Map<String, Int> {
        if (rawValue.isNullOrBlank()) return emptyMap()

        return runCatching {
            val json = JSONObject(rawValue)
            val result = mutableMapOf<String, Int>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = json.optInt(key, 0)
                if (value > 0) {
                    result[key] = value
                }
            }
            result.toMap()
        }.getOrDefault(emptyMap())
    }
}
