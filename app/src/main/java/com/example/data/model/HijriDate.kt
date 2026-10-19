package com.example.data.model

import java.util.Calendar
import kotlin.math.floor

data class HijriDate(
    val year: Int,
    val month: Int, // 1-12
    val day: Int,   // 1-30
    val monthNameEn: String,
    val monthNameAr: String,
    val designation: String = "AH",
    val event: String? = null
) {
    val formatted: String
        get() = "$day $monthNameEn $year $designation"

    val formattedAr: String
        get() = "$day $monthNameAr $year هـ"
}

data class IslamicEvent(
    val hijriMonth: Int,
    val hijriDay: Int,
    val nameEn: String,
    val nameAr: String,
    val description: String,
    val isMajorHoliday: Boolean = true
)

object HijriCalendarHelper {

    val ISLAMIC_MONTHS_EN = listOf(
        "Muharram",
        "Safar",
        "Rabi' al-Awwal",
        "Rabi' al-Thani",
        "Jumada al-Ula",
        "Jumada al-Akhirah",
        "Rajab",
        "Sha'ban",
        "Ramadan",
        "Shawwal",
        "Dhu al-Qi'dah",
        "Dhu al-Hijjah"
    )

    val ISLAMIC_MONTHS_AR = listOf(
        "محرّم",
        "صفر",
        "ربيع الأول",
        "ربيع الثاني",
        "جمادى الأولى",
        "جمادى الآخرة",
        "رجب",
        "شعبان",
        "رمضان",
        "شوّال",
        "ذو القعدة",
        "ذو الحجة"
    )

    val ISLAMIC_EVENTS = listOf(
        IslamicEvent(1, 1, "Islamic New Year", "رأس السنة الهجرية", "Beginning of Hijri year 1448", true),
        IslamicEvent(1, 9, "Tasu'a", "تاسوعاء", "Day before Ashura fasting", false),
        IslamicEvent(1, 10, "Day of Ashura", "يوم عاشوراء", "Commemoration of Prophet Musa's salvation, Sunnah fast", true),
        IslamicEvent(3, 12, "Mawlid al-Nabi", "المولد النبوي الشريف", "Birth of Prophet Muhammad (ﷺ)", true),
        IslamicEvent(7, 27, "Isra and Mi'raj", "الإسراء والمعراج", "The Miraculous Night Journey and Ascension", true),
        IslamicEvent(8, 15, "Mid-Sha'ban (Shab-e-Barat)", "ليلة النصف من شعبان", "Night of forgiveness before Ramadan", false),
        IslamicEvent(9, 1, "First Day of Ramadan", "أول أيام شهر رمضان", "First day of holy month of fasting", true),
        IslamicEvent(9, 17, "Battle of Badr Anniversary", "غزوة بدر الكبرى", "17th of Ramadan", false),
        IslamicEvent(9, 21, "Laylat al-Qadr (Search)", "ليلة القدر", "Odd night in the last ten days of Ramadan", false),
        IslamicEvent(9, 23, "Laylat al-Qadr (Search)", "ليلة القدر", "Odd night in the last ten days of Ramadan", false),
        IslamicEvent(9, 25, "Laylat al-Qadr (Search)", "ليلة القدر", "Odd night in the last ten days of Ramadan", false),
        IslamicEvent(9, 27, "Laylat al-Qadr (Most Likely)", "ليلة القدر", "Night of Decree, better than a thousand months", true),
        IslamicEvent(9, 29, "Laylat al-Qadr (Search)", "ليلة القدر", "Odd night in the last ten days of Ramadan", false),
        IslamicEvent(10, 1, "Eid al-Fitr (Day 1)", "عيد الفطر المبارك", "Celebration marking the conclusion of Ramadan", true),
        IslamicEvent(10, 2, "Eid al-Fitr (Day 2)", "عيد الفطر المبارك", "Second day of Eid al-Fitr", true),
        IslamicEvent(10, 3, "Eid al-Fitr (Day 3)", "عيد الفطر المبارك", "Third day of Eid al-Fitr", true),
        IslamicEvent(12, 1, "Start of 10 Blessed Days of Dhul Hijjah", "أول ذي الحجة", "Virtuous days for good deeds and fasting", false),
        IslamicEvent(12, 8, "Day of Tarwiyah", "يوم التروية", "Pilgrims travel to Mina for Hajj", false),
        IslamicEvent(12, 9, "Day of Arafah", "يوم عرفة", "Pivotal day of Hajj; highly recommended fast for non-pilgrims", true),
        IslamicEvent(12, 10, "Eid al-Adha (Feast of Sacrifice)", "عيد الأضحى المبارك", "Commemorating Ibrahim's devotion and sacrifice", true),
        IslamicEvent(12, 11, "Ayyam al-Tashreeq (Day 1)", "أيام التشريق", "Days of remembrance and celebration", false),
        IslamicEvent(12, 12, "Ayyam al-Tashreeq (Day 2)", "أيام التشريق", "Days of remembrance and celebration", false),
        IslamicEvent(12, 13, "Ayyam al-Tashreeq (Day 3)", "أيام التشريق", "Conclusion of Tashreeq", false)
    )

    /**
     * Converts a Gregorian date (year, month 1-12, day 1-31) to HijriDate
     * with an adjustment offset (in days: -2, -1, 0, +1, +2).
     */
    fun gregorianToHijri(
        year: Int,
        month: Int,
        day: Int,
        adjustmentDays: Int = 0
    ): HijriDate {
        var m = month
        var y = year
        if (m < 3) {
            y -= 1
            m += 12
        }

        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)

        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5 + adjustmentDays

        // Julian Day to Hijri (Umm al-Qura standard civil algorithm)
        val z = jd + 0.5
        val i = floor((z - 1948440.0) / 10631.0)
        val r = (z - 1948440.0) - 10631.0 * i
        val j = floor((r) / 354.367)
        val hYear = (30 * i + j + 1).toInt()

        val dayOfYear = r - floor(j * 354.367)
        var hMonth = floor((dayOfYear) / 29.5).toInt() + 1
        if (hMonth > 12) hMonth = 12
        if (hMonth < 1) hMonth = 1

        var hDay = (dayOfYear - floor((hMonth - 1) * 29.5)).toInt() + 1
        if (hDay > 30) {
            hDay = 30
        }
        if (hDay < 1) {
            hDay = 1
        }

        val monthNameEn = ISLAMIC_MONTHS_EN.getOrElse(hMonth - 1) { "Month $hMonth" }
        val monthNameAr = ISLAMIC_MONTHS_AR.getOrElse(hMonth - 1) { "" }

        val event = findEvent(hMonth, hDay)

        return HijriDate(
            year = hYear,
            month = hMonth,
            day = hDay,
            monthNameEn = monthNameEn,
            monthNameAr = monthNameAr,
            event = event?.nameEn
        )
    }

    fun findEvent(hijriMonth: Int, hijriDay: Int): IslamicEvent? {
        return ISLAMIC_EVENTS.firstOrNull { it.hijriMonth == hijriMonth && it.hijriDay == hijriDay }
    }

    /**
     * Returns approximate moon phase for a given Hijri day (1-30)
     */
    fun getMoonPhaseInfo(hijriDay: Int): Pair<String, String> {
        return when (hijriDay) {
            1 -> "New Moon (Hilal)" to "🌒"
            in 2..6 -> "Waxing Crescent" to "🌒"
            7, 8 -> "First Quarter" to "🌓"
            in 9..12 -> "Waxing Gibbous" to "🌔"
            in 13..15 -> "Full Moon (Badr / White Days)" to "🌕"
            in 16..21 -> "Waning Gibbous" to "🌖"
            22, 23 -> "Third Quarter" to "🌗"
            in 24..28 -> "Waning Crescent" to "🌘"
            else -> "Dark Moon / Conjunction" to "🌑"
        }
    }
}

data class DayOfWeekInfo(
    val dayOfWeek: Int, // Calendar.SUNDAY..Calendar.SATURDAY
    val nameEn: String,
    val nameAr: String,
    val shortNameEn: String,
    val shortNameAr: String,
    val isFriday: Boolean,
    val isSunnahFastDay: Boolean,
    val description: String,
    val specialBadge: String? = null
)

data class FridaySunnahItem(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val description: String,
    val hadithRef: String
)

object WeekdayHelper {
    val DAYS = listOf(
        DayOfWeekInfo(
            dayOfWeek = Calendar.SUNDAY,
            nameEn = "Sunday",
            nameAr = "الأحد",
            shortNameEn = "Sun",
            shortNameAr = "أحد",
            isFriday = false,
            isSunnahFastDay = false,
            description = "Al-Ahad: The first AD day of the week, beginning lawful labor and seeking halal sustenance."
        ),
        DayOfWeekInfo(
            dayOfWeek = Calendar.MONDAY,
            nameEn = "Monday",
            nameAr = "الإثنين",
            shortNameEn = "Mon",
            shortNameAr = "إثنين",
            isFriday = false,
            isSunnahFastDay = true,
            description = "Al-Ithnayn: Recommended Sunnah fasting day. The Prophet (ﷺ) was born on Monday and weekly deeds are presented to Allah.",
            specialBadge = "Sunnah Fasting"
        ),
        DayOfWeekInfo(
            dayOfWeek = Calendar.TUESDAY,
            nameEn = "Tuesday",
            nameAr = "الثلاثاء",
            shortNameEn = "Tue",
            shortNameAr = "ثلاثاء",
            isFriday = false,
            isSunnahFastDay = false,
            description = "Ath-Thulatha': Midweek pursuit of beneficial knowledge, gratitude, and good character."
        ),
        DayOfWeekInfo(
            dayOfWeek = Calendar.WEDNESDAY,
            nameEn = "Wednesday",
            nameAr = "الأربعاء",
            shortNameEn = "Wed",
            shortNameAr = "أربعاء",
            isFriday = false,
            isSunnahFastDay = false,
            description = "Al-Arba'a': Day of steadfast devotion, strengthening family ties, and supplication."
        ),
        DayOfWeekInfo(
            dayOfWeek = Calendar.THURSDAY,
            nameEn = "Thursday",
            nameAr = "الخميس",
            shortNameEn = "Thu",
            shortNameAr = "خميس",
            isFriday = false,
            isSunnahFastDay = true,
            description = "Al-Khamis: Recommended Sunnah fasting day. Weekly deeds ascend to the Almighty on this day.",
            specialBadge = "Sunnah Fasting"
        ),
        DayOfWeekInfo(
            dayOfWeek = Calendar.FRIDAY,
            nameEn = "Friday",
            nameAr = "الجمعة",
            shortNameEn = "Fri",
            shortNameAr = "جمعة",
            isFriday = true,
            isSunnahFastDay = false,
            description = "Sayyid al-Ayyam (سيد الأيام) - Master of All Days. The most blessed, holy day of the week in Islam, featuring Jumu'ah congregational prayer, Surah Al-Kahf, and the Hour of Acceptance (ساعة الإجابة).",
            specialBadge = "★ Blessed Jumu'ah (سيد الأيام)"
        ),
        DayOfWeekInfo(
            dayOfWeek = Calendar.SATURDAY,
            nameEn = "Saturday",
            nameAr = "السبت",
            shortNameEn = "Sat",
            shortNameAr = "سبت",
            isFriday = false,
            isSunnahFastDay = false,
            description = "As-Sabt: Weekend day for contemplation, visiting kin, rest, and community service."
        )
    )

    val FRIDAY_SUNNAHS = listOf(
        FridaySunnahItem("ghusl", "Ghusl (Full Ritual Wash)", "الغسل يوم الجمعة", "Taking a ritual bath and wearing clean clothes before the congregational prayer.", "Sahih al-Bukhari 877"),
        FridaySunnahItem("kahf", "Recite Surah Al-Kahf", "قراءة سورة الكهف", "Provides a divine light illuminating the believer from this Friday to the next.", "Sunan al-Bayhaqi"),
        FridaySunnahItem("salawat", "Abundant Salawat on the Prophet (ﷺ)", "الإكثار من الصلاة على النبي", "\"Send abundant blessings upon me on Friday, for your blessings are shown to me.\"", "Sunan Abi Dawud 1047"),
        FridaySunnahItem("masjid_early", "Early Arrival to the Masjid", "التبكير إلى المسجد", "Arriving early to pray Tahiyyat al-Masjid and listen attentively to the Khutbah.", "Sahih al-Bukhari 881"),
        FridaySunnahItem("siwak", "Siwak & Pleasant Fragrance", "السواك والتطيب", "Cleaning teeth with siwak and applying pleasant non-alcoholic perfume / Attar.", "Sahih al-Bukhari 880"),
        FridaySunnahItem("ijabah", "Supplication in Sa'at al-Ijabah", "تحري ساعة الإجابة", "Making earnest Dua during the last hour of Friday before Maghrib when prayers are answered.", "Sahih Muslim 852")
    )

    fun getDayInfo(dayOfWeek: Int): DayOfWeekInfo {
        return DAYS.firstOrNull { it.dayOfWeek == dayOfWeek } ?: DAYS[0]
    }
}

