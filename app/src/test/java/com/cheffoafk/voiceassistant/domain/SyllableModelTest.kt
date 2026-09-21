package com.cheffoafk.voiceassistant.domain

import com.cheffoafk.voiceassistant.data.room.UserWord
import com.cheffoafk.voiceassistant.data.room.UserWordDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.Runnable

private object ImmediateDispatcher : CoroutineDispatcher() {
    override fun dispatch(context: kotlin.coroutines.CoroutineContext, block: Runnable) {
        block.run()
    }
}

private class FakeUserWordDao : UserWordDao {
    private val freq = linkedMapOf<String, Int>()

    override suspend fun findTopPredictionsByPrefix(
        prefix: String,
        limit: Int,
        nowMs: Long,
        minFrequency: Int
    ): List<String> {
        return freq.entries
            .asSequence()
            .filter { it.key.startsWith(prefix) && it.value >= minFrequency }
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { it.key }
            .take(limit)
            .toList()
    }

    override suspend fun incrementFrequency(word: String, timestamp: Long): Int {
        val current = freq[word] ?: return 0
        freq[word] = current + 1
        return 1
    }

    override suspend fun insert(word: UserWord): Long {
        if (freq.containsKey(word.word)) return -1L
        freq[word.word] = word.frequency
        return 1L
    }
}

class SyllableModelTest {

    @Test
    fun getNextPredictions_prioritizesUserWordsAndReturnsNextChunk() = runBlocking {
        val dao = FakeUserWordDao()
        val model = SyllableModel(
            userWordDao = dao,
            ioDispatcher = ImmediateDispatcher
        )
        model.loadDictionaryWords(listOf("acqua", "accordo", "acquario"))

        model.registerWordUsage("acqua")
        model.registerWordUsage("acqua")
        model.registerWordUsage("accordo")

        val suggestions = model.getNextPredictions("ac")

        // Raggiunta la soglia (min 2 usi), la parola utente sale in cima ai suggerimenti.
        assertEquals(listOf("qua", "cordo", "quario"), suggestions)
    }

    @Test
    fun getNextPredictions_promotesWordAfterTwoUses() = runBlocking {
        val dao = FakeUserWordDao()
        val model = SyllableModel(
            userWordDao = dao,
            ioDispatcher = ImmediateDispatcher
        )
        model.loadDictionaryWords(listOf("acqua", "accordo", "acquario"))

        model.registerWordUsage("acqua")
        model.registerWordUsage("acqua")

        val suggestions = model.getNextPredictions("ac")

        // Sopra soglia, la parola utente sale in cima ai suggerimenti.
        assertEquals(listOf("qua", "cordo", "quario"), suggestions)
    }

    @Test
    fun registerWordUsage_incrementsFrequencyInDao() = runBlocking {
        val dao = FakeUserWordDao()
        val model = SyllableModel(
            userWordDao = dao,
            ioDispatcher = ImmediateDispatcher
        )
        model.loadDictionaryWords(listOf("aiuto"))

        model.registerWordUsage("aiuto")
        model.registerWordUsage("aiuto")

        val suggestions = model.getNextPredictions("a")

        assertEquals(listOf("iuto"), suggestions)
    }

    @Test
    fun getNextPredictions_returnsEmpty_whenPrefixHasNoExactMatch() = runBlocking {
        val dao = FakeUserWordDao()
        val model = SyllableModel(
            userWordDao = dao,
            ioDispatcher = ImmediateDispatcher
        )
        model.loadDictionaryWords(listOf("acqua", "accordo", "acquario"))

        val suggestions = model.getNextPredictions("acx")

        assertEquals(emptyList<String>(), suggestions)
    }

    @Test
    fun getNextPredictions_doesNotFallbackToShorterPrefix_forLongPrefix() = runBlocking {
        val dao = FakeUserWordDao()
        val model = SyllableModel(
            userWordDao = dao,
            ioDispatcher = ImmediateDispatcher
        )
        model.loadDictionaryWords(listOf("acqua", "accordo", "acquario"))

        val suggestions = model.getNextPredictions("abbra")

        assertEquals(emptyList<String>(), suggestions)
    }

    @Test
    fun getNextPredictions_returnsAbbracciamiChunk_forAbbraPrefix() = runBlocking {
        val dao = FakeUserWordDao()
        val model = SyllableModel(
            userWordDao = dao,
            ioDispatcher = ImmediateDispatcher
        )
        model.loadDictionaryWords(listOf("abbracciami", "abbraccio"))

        val suggestions = model.getNextPredictions("abbra")

        assertEquals(listOf("cciami", "ccio"), suggestions)
    }

    @Test
    fun getNextPredictions_forCiePrefix_doesNotUseCiaoOrCiboRemainders() = runBlocking {
        val dao = FakeUserWordDao()
        val model = SyllableModel(
            userWordDao = dao,
            ioDispatcher = ImmediateDispatcher
        )
        model.loadDictionaryWords(listOf("ciao", "cibo", "cielo"))

        val suggestions = model.getNextPredictions("cie")

        assertEquals(listOf("lo"), suggestions)
    }
}







