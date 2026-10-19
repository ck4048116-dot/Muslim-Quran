package com.example.data.model

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

enum class PrayerType(val displayName: String, val displayNameAr: String) {
    FAJR("Fajr", "الفجر"),
    SUNRISE("Sunrise", "الشروق"),
    DHUHR("Dhuhr", "الظهر"),
    ASR("Asr", "العصر"),
    MAGHRIB("Maghrib", "المغرب"),
    ISHA("Isha", "العشاء")
}

data class SinglePrayer(
    val type: PrayerType,
    val hour: Int,
    val minute: Int,
    val isNotificationEnabled: Boolean = true
) {
    val formattedTime12h: String
        get() {
            val period = if (hour >= 12) "PM" else "AM"
            val h = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            return String.format("%02d:%02d %s", h, minute, period)
        }

    val formattedTime24h: String
        get() = String.format("%02d:%02d", hour, minute)

    fun toMinutesOfDay(): Int = hour * 60 + minute
}

data class DailyPrayerTimes(
    val dateGregorian: String,
    val fajr: SinglePrayer,
    val sunrise: SinglePrayer,
    val dhuhr: SinglePrayer,
    val asr: SinglePrayer,
    val maghrib: SinglePrayer,
    val isha: SinglePrayer,
    val currentCity: CityLocation,
    val calculationMethod: CalculationMethod
) {
    val allPrayers: List<SinglePrayer>
        get() = listOf(fajr, sunrise, dhuhr, asr, maghrib, isha)

    fun getNextPrayer(currentMinutesOfDay: Int): Pair<SinglePrayer, Int> {
        // Returns next prayer and minutes remaining until it occurs
        for (prayer in allPrayers) {
            val pMin = prayer.toMinutesOfDay()
            if (pMin > currentMinutesOfDay) {
                return prayer to (pMin - currentMinutesOfDay)
            }
        }
        // Past Isha: next is tomorrow's Fajr
        val remainingToday = 1440 - currentMinutesOfDay
        val totalMinutes = remainingToday + fajr.toMinutesOfDay()
        return fajr to totalMinutes
    }

    fun getCurrentPrayer(currentMinutesOfDay: Int): SinglePrayer {
        var active = isha
        for (prayer in allPrayers) {
            if (currentMinutesOfDay >= prayer.toMinutesOfDay()) {
                active = prayer
            } else {
                break
            }
        }
        return active
    }
}

data class CityLocation(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneOffsetHours: Double
)

enum class CalculationMethod(
    val title: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaIntervalMin: Int = 0
) {
    MWL("Muslim World League", 18.0, 17.0),
    ISNA("Islamic Society of North America", 15.0, 15.0),
    EGYPT("Egyptian General Authority", 19.5, 17.5),
    MAKKAH("Umm al-Qura University, Makkah", 18.5, 0.0, 90),
    KARACHI("Univ. of Islamic Sciences, Karachi", 18.0, 18.0),
    DUBAI("Dubai / UAE", 18.2, 18.2),
    TEHRAN("Institute of Geophysics, Tehran", 17.7, 14.0)
}

enum class JuristicMethod(val title: String, val shadowFactor: Double) {
    STANDARD("Shafi'i, Maliki, Hanbali (Shadow 1x)", 1.0),
    HANAFI("Hanafi (Shadow 2x)", 2.0)
}

object PrayerTimeCalculator {

    val POPULAR_CITIES = listOf(
        CityLocation("Mecca", "Saudi Arabia", 21.4225, 39.8262, 3.0),
        CityLocation("Medina", "Saudi Arabia", 24.4672, 39.6111, 3.0),
        CityLocation("Cairo", "Egypt", 30.0444, 31.2357, 3.0),
        CityLocation("Istanbul", "Turkey", 41.0082, 28.9784, 3.0),
        CityLocation("Dubai", "United Arab Emirates", 25.2048, 55.2708, 4.0),
        CityLocation("Jakarta", "Indonesia", -6.2088, 106.8456, 7.0),
        CityLocation("Kuala Lumpur", "Malaysia", 3.1390, 101.6869, 8.0),
        CityLocation("Karachi", "Pakistan", 24.8607, 67.0011, 5.0),
        CityLocation("Dhaka", "Bangladesh", 23.8103, 90.4125, 6.0),
        CityLocation("London", "United Kingdom", 51.5074, -0.1278, 1.0),
        CityLocation("New York", "United States", 40.7128, -74.0060, -4.0),
        CityLocation("Los Angeles", "United States", 34.0522, -118.2437, -7.0),
        CityLocation("Toronto", "Canada", 43.6532, -79.3832, -4.0),
        CityLocation("Paris", "France", 48.8566, 2.3522, 2.0),
        CityLocation("Sydney", "Australia", -33.8688, 151.2093, 11.0)
    )

    private const val KAABA_LAT = 21.4225
    private const val KAABA_LON = 39.8262

    /**
     * Calculates Qibla direction in degrees clockwise from True North (0-360)
     */
    fun calculateQiblaDirection(userLat: Double, userLon: Double): Double {
        val lat1 = Math.toRadians(userLat)
        val lat2 = Math.toRadians(KAABA_LAT)
        val dLon = Math.toRadians(KAABA_LON - userLon)

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        var bearing = Math.toDegrees(atan2(y, x))
        if (bearing < 0) {
            bearing += 360.0
        }
        return bearing
    }

    /**
     * Calculate Daily Prayer Times using standard solar equations (Jean Meeus / PrayTimes)
     */
    fun calculatePrayerTimes(
        calendar: Calendar,
        city: CityLocation,
        method: CalculationMethod = CalculationMethod.MWL,
        juristic: JuristicMethod = JuristicMethod.STANDARD,
        notificationSettings: Map<PrayerType, Boolean> = emptyMap()
    ): DailyPrayerTimes {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Julian Day Number
        var m = month
        var y = year
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5

        // Solar parameters
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val dec = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        val ra = fixAngle(Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l))))) / 15.0

        val eqt = q / 15.0 - ra

        // Midday (Dhuhr)
        val tz = city.timezoneOffsetHours
        val noon = 12.0 + tz - (city.longitude / 15.0) - eqt

        // Sun altitude helpers
        fun sunAngleTime(angle: Double, isMorning: Boolean): Double {
            val latRad = Math.toRadians(city.latitude)
            val decRad = Math.toRadians(dec)
            val angleRad = Math.toRadians(-angle)

            val cosH = (sin(angleRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
            if (cosH > 1.0 || cosH < -1.0) {
                // Extreme latitudes fallback
                return if (isMorning) noon - 3.5 else noon + 3.5
            }
            val h = Math.toDegrees(acos(cosH)) / 15.0
            return if (isMorning) noon - h else noon + h
        }

        // Asr helper
        fun asrTime(factor: Double): Double {
            val latRad = Math.toRadians(city.latitude)
            val decRad = Math.toRadians(dec)
            val altAngle = atan(1.0 / (factor + tan(abs(latRad - decRad))))
            val cosH = (sin(altAngle) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
            val h = if (cosH in -1.0..1.0) Math.toDegrees(acos(cosH)) / 15.0 else 3.25
            return noon + h
        }

        val fajrDecimal = sunAngleTime(method.fajrAngle, true)
        val sunriseDecimal = sunAngleTime(0.833, true)
        val dhuhrDecimal = noon
        val asrDecimal = asrTime(juristic.shadowFactor)
        val maghribDecimal = sunAngleTime(0.833, false)

        val ishaDecimal = if (method.ishaIntervalMin > 0) {
            maghribDecimal + (method.ishaIntervalMin / 60.0)
        } else {
            sunAngleTime(method.ishaAngle, false)
        }

        fun decimalToHourMinute(decTime: Double): Pair<Int, Int> {
            var totalMin = (decTime * 60).roundToInt()
            if (totalMin < 0) totalMin += 1440
            totalMin %= 1440
            val h = totalMin / 60
            val min = totalMin % 60
            return h to min
        }

        fun makePrayer(type: PrayerType, decTime: Double): SinglePrayer {
            val (h, min) = decimalToHourMinute(decTime)
            val enabled = notificationSettings[type] ?: true
            return SinglePrayer(type, h, min, enabled)
        }

        val dateStr = String.format("%04d-%02d-%02d", calendar.get(Calendar.YEAR), month, day)

        return DailyPrayerTimes(
            dateGregorian = dateStr,
            fajr = makePrayer(PrayerType.FAJR, fajrDecimal),
            sunrise = makePrayer(PrayerType.SUNRISE, sunriseDecimal),
            dhuhr = makePrayer(PrayerType.DHUHR, dhuhrDecimal),
            asr = makePrayer(PrayerType.ASR, asrDecimal),
            maghrib = makePrayer(PrayerType.MAGHRIB, maghribDecimal),
            isha = makePrayer(PrayerType.ISHA, ishaDecimal),
            currentCity = city,
            calculationMethod = method
        )
    }

    private fun fixAngle(a: Double): Double {
        var angle = a - 360.0 * floor(a / 360.0)
        if (angle < 0) angle += 360.0
        return angle
    }
}
