package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quran_bookmarks")
data class QuranBookmark(
    @PrimaryKey
    val surahNumber: Int,
    val surahName: String,
    val lastAyahNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)
