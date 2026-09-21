package com.cheffoafk.voiceassistant.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface UserWordDao {

    /**
     * Restituisce le prime [limit] parole che iniziano con [prefix], ordinate per
     * punteggio decrescente con decay temporale:
     *   score = (frequency × 1_000_000) / (1 + giorni_dall'ultimo_uso)
     *
     * Così parole usate di recente scalano rispetto a parole vecchie con stessa frequenza.
     */
    @Query(
        """
        SELECT word
        FROM user_words
        WHERE word LIKE :prefix || '%'
          AND frequency >= :minFrequency
        ORDER BY (frequency * 1000000) / (1 + ((:nowMs - lastUsedAt) / 86400000)) DESC,
                 word ASC
        LIMIT :limit
        """
    )
    suspend fun findTopPredictionsByPrefix(
        prefix: String,
        limit: Int = 5,
        nowMs: Long = System.currentTimeMillis(),
        minFrequency: Int = 1
    ): List<String>

    @Query("""
        UPDATE user_words
        SET frequency = frequency + 1, lastUsedAt = :timestamp
        WHERE word = :word
    """)
    suspend fun incrementFrequency(word: String, timestamp: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(word: UserWord): Long

    @Transaction
    suspend fun upsertWord(rawWord: String) {
        val normalized = rawWord.trim().lowercase()
        if (normalized.isBlank()) return

        val now = System.currentTimeMillis()
        val updatedRows = incrementFrequency(word = normalized, timestamp = now)
        if (updatedRows == 0) {
            insert(UserWord(word = normalized, frequency = 1, lastUsedAt = now))
        }
    }
}
