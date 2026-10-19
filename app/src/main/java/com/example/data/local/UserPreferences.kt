package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.CalculationMethod
import com.example.data.model.CityLocation
import com.example.data.model.JuristicMethod
import com.example.data.model.PrayerTimeCalculator
import com.example.data.model.PrayerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("noor_app_prefs", Context.MODE_PRIVATE)

    private val _hijriAdjustment = MutableStateFlow(prefs.getInt(KEY_HIJRI_ADJUSTMENT, 0))
    val hijriAdjustment: StateFlow<Int> = _hijriAdjustment.asStateFlow()

    private val _selectedCityIndex = MutableStateFlow(prefs.getInt(KEY_CITY_INDEX, 0))
    val selectedCityIndex: StateFlow<Int> = _selectedCityIndex.asStateFlow()

    private val _calculationMethod = MutableStateFlow(
        try {
            CalculationMethod.valueOf(prefs.getString(KEY_CALC_METHOD, CalculationMethod.MWL.name)!!)
        } catch (e: Exception) {
            CalculationMethod.MWL
        }
    )
    val calculationMethod: StateFlow<CalculationMethod> = _calculationMethod.asStateFlow()

    private val _juristicMethod = MutableStateFlow(
        try {
            JuristicMethod.valueOf(prefs.getString(KEY_JURISTIC_METHOD, JuristicMethod.STANDARD.name)!!)
        } catch (e: Exception) {
            JuristicMethod.STANDARD
        }
    )
    val juristicMethod: StateFlow<JuristicMethod> = _juristicMethod.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(prefs.getBoolean(KEY_TASBIH_VIBRATION, true))
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _soundClickEnabled = MutableStateFlow(prefs.getBoolean(KEY_TASBIH_SOUND, true))
    val soundClickEnabled: StateFlow<Boolean> = _soundClickEnabled.asStateFlow()

    private val _arabicFontSize = MutableStateFlow(prefs.getFloat(KEY_ARABIC_FONT_SIZE, 26f))
    val arabicFontSize: StateFlow<Float> = _arabicFontSize.asStateFlow()

    fun setHijriAdjustment(days: Int) {
        val clamped = days.coerceIn(-2, 2)
        prefs.edit().putInt(KEY_HIJRI_ADJUSTMENT, clamped).apply()
        _hijriAdjustment.value = clamped
    }

    fun setSelectedCityIndex(index: Int) {
        prefs.edit().putInt(KEY_CITY_INDEX, index).apply()
        _selectedCityIndex.value = index
    }

    fun setCalculationMethod(method: CalculationMethod) {
        prefs.edit().putString(KEY_CALC_METHOD, method.name).apply()
        _calculationMethod.value = method
    }

    fun setJuristicMethod(method: JuristicMethod) {
        prefs.edit().putString(KEY_JURISTIC_METHOD, method.name).apply()
        _juristicMethod.value = method
    }

    fun setTasbihVibration(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TASBIH_VIBRATION, enabled).apply()
        _vibrationEnabled.value = enabled
    }

    fun setTasbihSound(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TASBIH_SOUND, enabled).apply()
        _soundClickEnabled.value = enabled
    }

    fun setArabicFontSize(size: Float) {
        prefs.edit().putFloat(KEY_ARABIC_FONT_SIZE, size).apply()
        _arabicFontSize.value = size
    }

    fun isPrayerNotificationEnabled(prayerType: PrayerType): Boolean {
        return prefs.getBoolean("notif_prayer_${prayerType.name}", true)
    }

    fun setPrayerNotificationEnabled(prayerType: PrayerType, enabled: Boolean) {
        prefs.edit().putBoolean("notif_prayer_${prayerType.name}", enabled).apply()
    }

    fun getSelectedCity(): CityLocation {
        val idx = selectedCityIndex.value.coerceIn(0, PrayerTimeCalculator.POPULAR_CITIES.lastIndex)
        return PrayerTimeCalculator.POPULAR_CITIES[idx]
    }

    companion object {
        private const val KEY_HIJRI_ADJUSTMENT = "pref_hijri_adjustment"
        private const val KEY_CITY_INDEX = "pref_city_index"
        private const val KEY_CALC_METHOD = "pref_calc_method"
        private const val KEY_JURISTIC_METHOD = "pref_juristic_method"
        private const val KEY_TASBIH_VIBRATION = "pref_tasbih_vibrate"
        private const val KEY_TASBIH_SOUND = "pref_tasbih_sound"
        private const val KEY_ARABIC_FONT_SIZE = "pref_arabic_font_size"
    }
}
