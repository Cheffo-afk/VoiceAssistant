package com.cheffoafk.voiceassistant.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_words")
data class UserWord(
    @PrimaryKey val word: String,
    val frequency: Int,
    /** Timestamp dell'ultimo utilizzo (epoch ms). Usato per il decay temporale nel ranking. */
    @ColumnInfo(name = "lastUsedAt", defaultValue = "0")
    val lastUsedAt: Long = System.currentTimeMillis()
)
