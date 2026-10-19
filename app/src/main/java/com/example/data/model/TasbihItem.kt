package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasbih_items")
data class TasbihItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val arabicText: String,
    val translation: String,
    val targetCount: Int = 33, // 33, 99, 100, or 0 for unlimited
    val currentCount: Int = 0,
    val totalLifetimeCount: Int = 0,
    val isDefault: Boolean = false
)

object TasbihPresets {
    val DEFAULTS = listOf(
        TasbihItem(
            id = 1,
            title = "SubhanAllah",
            arabicText = "سُبْحَانَ اللَّهِ",
            translation = "Glory be to Allah",
            targetCount = 33,
            currentCount = 0,
            totalLifetimeCount = 0,
            isDefault = true
        ),
        TasbihItem(
            id = 2,
            title = "Alhamdulillah",
            arabicText = "الْحَمْدُ لِلَّهِ",
            translation = "All praise is due to Allah",
            targetCount = 33,
            currentCount = 0,
            totalLifetimeCount = 0,
            isDefault = true
        ),
        TasbihItem(
            id = 3,
            title = "Allahu Akbar",
            arabicText = "اللَّهُ أَكْبَرُ",
            translation = "Allah is the Greatest",
            targetCount = 34,
            currentCount = 0,
            totalLifetimeCount = 0,
            isDefault = true
        ),
        TasbihItem(
            id = 4,
            title = "Astaghfirullah",
            arabicText = "أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ وَأَتُوبُ إِلَيْهِ",
            translation = "I seek the forgiveness of Allah and repent to Him",
            targetCount = 100,
            currentCount = 0,
            totalLifetimeCount = 0,
            isDefault = true
        ),
        TasbihItem(
            id = 5,
            title = "Tahlil (La ilaha illallah)",
            arabicText = "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
            translation = "There is no deity except Allah alone, without partner",
            targetCount = 100,
            currentCount = 0,
            totalLifetimeCount = 0,
            isDefault = true
        ),
        TasbihItem(
            id = 6,
            title = "SubhanAllahi wa bihamdihi",
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
            translation = "Glory and praise be to Allah, Glory be to Allah the Supreme",
            targetCount = 100,
            currentCount = 0,
            totalLifetimeCount = 0,
            isDefault = true
        ),
        TasbihItem(
            id = 7,
            title = "Salawat on the Prophet (ﷺ)",
            arabicText = "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ",
            translation = "O Allah, send blessings and peace upon our Prophet Muhammad",
            targetCount = 100,
            currentCount = 0,
            totalLifetimeCount = 0,
            isDefault = true
        ),
        TasbihItem(
            id = 8,
            title = "Hawqala",
            arabicText = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ الْعَلِيِّ الْعَظِيمِ",
            translation = "There is no might nor power except in Allah, the Most High, the Supreme",
            targetCount = 100,
            currentCount = 0,
            totalLifetimeCount = 0,
            isDefault = true
        ),
        TasbihItem(
            id = 9,
            title = "Open Tasbih (Free Count)",
            arabicText = "ذِكْرٌ عَامٌّ",
            translation = "Continuous remembrance without limit",
            targetCount = 0,
            currentCount = 0,
            totalLifetimeCount = 0,
            isDefault = true
        )
    )
}
