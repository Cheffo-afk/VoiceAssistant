package com.cheffoafk.voiceassistant.domain

import android.content.Context
import com.cheffoafk.voiceassistant.data.room.UserWordDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyllableModel(
    private val userWordDao: UserWordDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    companion object {
        private const val MIN_USAGE_FOR_PREFERENCE = 2
    }

    private val trie = WordsTrie()
    @Volatile
    private var dictionaryLoaded = false

    suspend fun loadDictionaryFromAssets(
        context: Context,
        assetFileName: String = "dictionary_it"
    ) = withContext(ioDispatcher) {
        if (dictionaryLoaded) return@withContext

        val assets = context.assets
        val children = assets.list(assetFileName).orEmpty().filter { it.endsWith(".txt") }.sorted()

        val words = if (children.isNotEmpty()) {
            children.asSequence()
                .flatMap { childName ->
                    val childPath = "$assetFileName/$childName"
                    readWordsFromAssetFile(assets = assets, path = childPath).asSequence()
                }
                .toList()
        } else {
            readWordsFromAssetFile(assets = assets, path = assetFileName)
        }

        loadDictionaryWords(words)
    }

    private fun readWordsFromAssetFile(
        assets: android.content.res.AssetManager,
        path: String
    ): List<String> {
        return assets.open(path).bufferedReader().useLines { lines ->
            lines
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
                .toList()
        }
    }

    fun loadDictionaryWords(words: Iterable<String>) {
        trie.insertAll(words)
        dictionaryLoaded = true
    }

    suspend fun registerWordUsage(word: String) = withContext(ioDispatcher) {
        userWordDao.upsertWord(word)
    }

    suspend fun getNextPredictions(
        currentComposition: String,
        maxSuggestions: Int = 5,
        chunkSize: Int = 48
    ): List<String> = withContext(ioDispatcher) {
        val prefix = extractCurrentWordPrefix(currentComposition)
        if (prefix.isBlank()) return@withContext emptyList()

        buildNextPredictionChunks(
            prefix = prefix,
            maxSuggestions = maxSuggestions,
            chunkSize = chunkSize
        )
    }

    private suspend fun buildNextPredictionChunks(
        prefix: String,
        maxSuggestions: Int,
        chunkSize: Int
    ): List<String> {
        val userCandidates = userWordDao.findTopPredictionsByPrefix(
            prefix = prefix,
            limit = maxSuggestions,
            minFrequency = MIN_USAGE_FOR_PREFERENCE
        )
        val dictionaryCandidates = trie.findByPrefix(prefix, limit = maxSuggestions * 2)

        val mergedCandidates = (userCandidates + dictionaryCandidates).distinct()

        // Dalla parola candidata estrae la parte rimanente rispetto al prefisso già digitato.
        return mergedCandidates
            .mapNotNull { candidate ->
                val remainder = candidate.removePrefix(prefix)
                if (remainder.isBlank()) null else remainder.take(chunkSize.coerceAtLeast(1))
            }
            .distinct()
            .take(maxSuggestions)
    }

    private fun extractCurrentWordPrefix(text: String): String =
        text.trimEnd()
            .substringAfterLast(' ')
            .trim()
            .lowercase()
}



