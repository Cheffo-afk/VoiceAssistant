package com.cheffoafk.voiceassistant.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PREFS_NAME = "custom_phrases_prefs"
private const val KEY_CUSTOM_PHRASES = "custom_phrases_json"
private const val KEY_PHRASES_ORDER = "phrases_order_json"

interface PhraseDataSource {
    val customPhrases: StateFlow<List<String>>
    val savedOrder: StateFlow<List<String>?>
    fun addPhrase(phrase: String)
    fun removePhrase(phrase: String)
    fun savePhraseOrder(orderedPhrases: List<String>)
}

class PhraseRepository(context: Context) : PhraseDataSource {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _customPhrases = MutableStateFlow(loadCustomPhrases())
    override val customPhrases: StateFlow<List<String>> = _customPhrases.asStateFlow()

    private val _savedOrder = MutableStateFlow(loadSavedOrder())
    override val savedOrder: StateFlow<List<String>?> = _savedOrder.asStateFlow()

    override fun addPhrase(phrase: String) {
        val normalized = phrase.trim()
        if (normalized.isEmpty()) return

        val updated = (_customPhrases.value + normalized).distinct()
        _customPhrases.value = updated
        preferences.edit().putString(KEY_CUSTOM_PHRASES, PhrasePersistenceCodec.encode(updated)).apply()
    }

    override fun removePhrase(phrase: String) {
        val normalized = phrase.trim()
        if (normalized.isEmpty()) return

        val updatedCustom = _customPhrases.value.filterNot { it == normalized }
        _customPhrases.value = updatedCustom
        preferences.edit().putString(KEY_CUSTOM_PHRASES, PhrasePersistenceCodec.encode(updatedCustom)).apply()

        val updatedOrder = _savedOrder.value?.filterNot { it == normalized }
        _savedOrder.value = updatedOrder
        preferences.edit().putString(KEY_PHRASES_ORDER, PhrasePersistenceCodec.encode(updatedOrder.orEmpty())).apply()
    }

    override fun savePhraseOrder(orderedPhrases: List<String>) {
        _savedOrder.value = orderedPhrases
        preferences.edit().putString(KEY_PHRASES_ORDER, PhrasePersistenceCodec.encode(orderedPhrases)).apply()
    }

    private fun loadCustomPhrases(): List<String> =
        PhrasePersistenceCodec.decode(preferences.getString(KEY_CUSTOM_PHRASES, null))

    private fun loadSavedOrder(): List<String>? {
        val raw = preferences.getString(KEY_PHRASES_ORDER, null) ?: return null
        return PhrasePersistenceCodec.decode(raw).takeIf { it.isNotEmpty() }
    }
}