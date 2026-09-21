package com.cheffoafk.voiceassistant.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class WordsTrieTest {

    @Test
    fun findByPrefix_returnsDeterministicMatches() {
        val trie = WordsTrie()
        trie.insertAll(listOf("acqua", "aiuto", "amico", "barca"))

        val matches = trie.findByPrefix("a", limit = 3)

        assertEquals(listOf("acqua", "aiuto", "amico"), matches)
    }

    @Test
    fun findByPrefix_returnsEmptyWhenPrefixMissing() {
        val trie = WordsTrie()
        trie.insertAll(listOf("acqua", "aiuto"))

        val matches = trie.findByPrefix("zz", limit = 5)

        assertEquals(emptyList<String>(), matches)
    }
}

