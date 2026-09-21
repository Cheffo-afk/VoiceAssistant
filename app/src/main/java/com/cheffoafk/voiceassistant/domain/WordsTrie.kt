package com.cheffoafk.voiceassistant.domain

private class TrieNode {
    val children: MutableMap<Char, TrieNode> = mutableMapOf()
    var isTerminal: Boolean = false
}

/**
 * Trie in-memory per ricerca veloce di parole per prefisso.
 * Le parole vengono normalizzate in lowercase per confronti coerenti.
 */
class WordsTrie {
    private val root = TrieNode()

    fun insert(word: String) {
        val normalized = normalizeWord(word)
        if (normalized.isBlank()) return

        var node = root
        for (ch in normalized) {
            node = node.children.getOrPut(ch) { TrieNode() }
        }
        node.isTerminal = true
    }

    fun insertAll(words: Iterable<String>) {
        words.forEach(::insert)
    }

    fun findByPrefix(prefix: String, limit: Int): List<String> {
        val normalizedPrefix = normalizeWord(prefix)
        if (normalizedPrefix.isBlank() || limit <= 0) return emptyList()

        var node = root
        for (ch in normalizedPrefix) {
            node = node.children[ch] ?: return emptyList()
        }

        val results = mutableListOf<String>()
        collectWords(node, normalizedPrefix, results, limit)
        return results
    }

    private fun collectWords(node: TrieNode, current: String, out: MutableList<String>, limit: Int) {
        if (out.size >= limit) return
        if (node.isTerminal) out += current

        // Ordine stabile per output deterministico e testabile.
        for ((ch, child) in node.children.toSortedMap()) {
            if (out.size >= limit) return
            collectWords(child, current + ch, out, limit)
        }
    }

    private fun normalizeWord(word: String): String =
        word.trim().lowercase()
}

