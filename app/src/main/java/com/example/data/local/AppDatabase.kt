package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DefaultReminders
import com.example.data.model.ReminderItem
import com.example.data.model.TasbihItem
import com.example.data.model.TasbihPresets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TasbihItem::class, ReminderItem::class, QuranBookmark::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tasbihDao(): TasbihDao
    abstract fun reminderDao(): ReminderDao
    abstract fun quranBookmarkDao(): QuranBookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "noor_islamic.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate default Tasbih dhikrs and default customizable reminders
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            database.tasbihDao().insertAll(TasbihPresets.DEFAULTS)
                            database.reminderDao().insertAll(DefaultReminders.LIST)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
