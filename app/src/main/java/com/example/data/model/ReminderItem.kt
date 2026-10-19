package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_reminders")
data class ReminderItem(
    @PrimaryKey
    val id: String,
    val title: String,
    val subtitle: String,
    val arabicPhrase: String,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean,
    val isFridayOnly: Boolean = false,
    val iconCategory: String = "quran" // "quran", "adhkar", "prayer", "fasting"
) {
    val formattedTime: String
        get() {
            val period = if (hour >= 12) "PM" else "AM"
            val h = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            return String.format("%02d:%02d %s", h, minute, period)
        }
}

object DefaultReminders {
    val LIST = listOf(
        ReminderItem(
            id = "quran_daily",
            title = "Daily Quran Recitation",
            subtitle = "Nourish your soul with your daily portion of the Noble Quran",
            arabicPhrase = "وَرَتِّلِ الْقُرْآنَ تَرْتِيلًا",
            hour = 7,
            minute = 30,
            isEnabled = true,
            iconCategory = "quran"
        ),
        ReminderItem(
            id = "adhkar_morning",
            title = "Morning Adhkar",
            subtitle = "Begin your morning protected in the remembrance of Allah",
            arabicPhrase = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ",
            hour = 6,
            minute = 15,
            isEnabled = true,
            iconCategory = "adhkar"
        ),
        ReminderItem(
            id = "adhkar_evening",
            title = "Evening Adhkar",
            subtitle = "Guard your evening and close the day with peace",
            arabicPhrase = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ",
            hour = 17,
            minute = 30,
            isEnabled = true,
            iconCategory = "adhkar"
        ),
        ReminderItem(
            id = "friday_kahf",
            title = "Friday Surah Al-Kahf",
            subtitle = "A light between two Fridays for whoever recites Surah Al-Kahf",
            arabicPhrase = "سُورَةُ الْكَهْفِ يَوْمَ الْجُمُعَةِ",
            hour = 9,
            minute = 0,
            isEnabled = true,
            isFridayOnly = true,
            iconCategory = "quran"
        ),
        ReminderItem(
            id = "tahajjud_prayer",
            title = "Tahajjud / Night Prayer",
            subtitle = "Stand in the last third of the night when prayers are answered",
            arabicPhrase = "وَمِنَ اللَّيْلِ فَتَهَجَّدْ بِهِ نَافِلَةً لَّكَ",
            hour = 4,
            minute = 0,
            isEnabled = false,
            iconCategory = "prayer"
        ),
        ReminderItem(
            id = "fasting_mon_thu",
            title = "Sunnah Fasting (Mon & Thu)",
            subtitle = "Reminder for tomorrow's Sunnah fast and Suhoor preparation",
            arabicPhrase = "صِيَامُ الإِثْنَيْنِ وَالْخَمِيسِ",
            hour = 20,
            minute = 30,
            isEnabled = false,
            iconCategory = "fasting"
        )
    )
}
