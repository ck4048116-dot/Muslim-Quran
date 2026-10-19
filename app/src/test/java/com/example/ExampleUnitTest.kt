package com.example

import com.example.data.model.CalculationMethod
import com.example.data.model.HijriCalendarHelper
import com.example.data.model.PrayerTimeCalculator
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {
    @Test
    fun testHijriDateConversion() {
        val hijri = HijriCalendarHelper.gregorianToHijri(2026, 10, 19, 0)
        assertTrue("Year should be around 1448 AH", hijri.year in 1447..1449)
        assertTrue("Month must be 1-12", hijri.month in 1..12)
        assertTrue("Day must be 1-30", hijri.day in 1..30)
        assertNotNull("Month name EN should exist", hijri.monthNameEn)
        assertNotNull("Month name AR should exist", hijri.monthNameAr)
    }

    @Test
    fun testIslamicEventsLookup() {
        val ramadanEvent = HijriCalendarHelper.findEvent(9, 1)
        assertNotNull("Should find Ramadan start event", ramadanEvent)
        assertEquals("First Day of Ramadan", ramadanEvent?.nameEn)

        val arafahEvent = HijriCalendarHelper.findEvent(12, 9)
        assertNotNull("Should find Day of Arafah event", arafahEvent)
        assertEquals("Day of Arafah", arafahEvent?.nameEn)
    }

    @Test
    fun testPrayerTimesCalculation() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 19, 12, 0, 0)
        }
        val mecca = PrayerTimeCalculator.POPULAR_CITIES.first { it.name == "Mecca" }
        val prayerTimes = PrayerTimeCalculator.calculatePrayerTimes(cal, mecca, CalculationMethod.MAKKAH)

        assertNotNull(prayerTimes.fajr)
        assertNotNull(prayerTimes.dhuhr)
        assertNotNull(prayerTimes.asr)
        assertNotNull(prayerTimes.maghrib)
        assertNotNull(prayerTimes.isha)

        // Sunrise comes after Fajr, Dhuhr after Sunrise, Asr after Dhuhr, etc.
        val fajrMin = prayerTimes.fajr.toMinutesOfDay()
        val sunriseMin = prayerTimes.sunrise.toMinutesOfDay()
        val dhuhrMin = prayerTimes.dhuhr.toMinutesOfDay()
        val asrMin = prayerTimes.asr.toMinutesOfDay()
        val maghribMin = prayerTimes.maghrib.toMinutesOfDay()
        val ishaMin = prayerTimes.isha.toMinutesOfDay()

        assertTrue("Fajr before Sunrise", fajrMin < sunriseMin)
        assertTrue("Sunrise before Dhuhr", sunriseMin < dhuhrMin)
        assertTrue("Dhuhr before Asr", dhuhrMin < asrMin)
        assertTrue("Asr before Maghrib", asrMin < maghribMin)
        assertTrue("Maghrib before Isha", maghribMin < ishaMin)
    }

    @Test
    fun testQiblaBearingFromCairo() {
        // Cairo is around lat 30.04, lon 31.23; Kaaba is at lat 21.42, lon 39.82 (southeast)
        val bearing = PrayerTimeCalculator.calculateQiblaDirection(30.0444, 31.2357)
        assertTrue("Bearing from Cairo should be south-east (around 135 deg)", bearing in 120.0..150.0)
    }
}
