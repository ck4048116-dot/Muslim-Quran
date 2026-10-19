package com.example.data.local

import androidx.room.*
import com.example.data.model.TasbihItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TasbihDao {
    @Query("SELECT * FROM tasbih_items ORDER BY id ASC")
    fun getAllTasbihItems(): Flow<List<TasbihItem>>

    @Query("SELECT * FROM tasbih_items WHERE id = :id")
    suspend fun getTasbihItemById(id: Long): TasbihItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasbihItem(item: TasbihItem): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<TasbihItem>)

    @Update
    suspend fun updateTasbihItem(item: TasbihItem)

    @Delete
    suspend fun deleteTasbihItem(item: TasbihItem)

    @Query("UPDATE tasbih_items SET currentCount = 0 WHERE id = :id")
    suspend fun resetCount(id: Long)

    @Query("UPDATE tasbih_items SET currentCount = currentCount + 1, totalLifetimeCount = totalLifetimeCount + 1 WHERE id = :id")
    suspend fun incrementCount(id: Long)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM custom_reminders")
    fun getAllReminders(): Flow<List<com.example.data.model.ReminderItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: com.example.data.model.ReminderItem)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(reminders: List<com.example.data.model.ReminderItem>)

    @Update
    suspend fun updateReminder(reminder: com.example.data.model.ReminderItem)

    @Query("UPDATE custom_reminders SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun toggleReminder(id: String, isEnabled: Boolean)

    @Query("UPDATE custom_reminders SET hour = :hour, minute = :minute WHERE id = :id")
    suspend fun updateTime(id: String, hour: Int, minute: Int)
}

@Dao
interface QuranBookmarkDao {
    @Query("SELECT * FROM quran_bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<QuranBookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBookmark(bookmark: QuranBookmark)

    @Query("DELETE FROM quran_bookmarks WHERE surahNumber = :surahNumber")
    suspend fun removeBookmark(surahNumber: Int)

    @Query("SELECT * FROM quran_bookmarks WHERE surahNumber = :surahNumber LIMIT 1")
    suspend fun getBookmarkForSurah(surahNumber: Int): QuranBookmark?
}
