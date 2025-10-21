package com.app.srivyaradio.data.repositories

import android.content.SharedPreferences
import com.app.srivyaradio.utils.Constants.COUNTRY_CODE
import com.app.srivyaradio.utils.Constants.IS_FIRST_START
import com.app.srivyaradio.utils.Constants.LAST_PLAY
import com.app.srivyaradio.utils.Constants.MODE
import com.app.srivyaradio.utils.Constants.HAS_PURCHASED
import com.app.srivyaradio.utils.Constants.VERSION
import com.app.srivyaradio.utils.Constants.LANGUAGE_CODE
import com.app.srivyaradio.utils.ThemeMode

class SharedPreferencesRepository(private val sharedPreferences: SharedPreferences) {
    fun getCountryCode(): String? {
        return sharedPreferences.getString(COUNTRY_CODE, "US")
    }

    fun isFirstStartUp(): Boolean {
        return sharedPreferences.getBoolean(IS_FIRST_START, true)
    }

    fun setUserCountry(code: String) {
        sharedPreferences.edit().apply {
            putString(COUNTRY_CODE, code)
            putBoolean(IS_FIRST_START, false)
            apply()
        }
    }

    fun getLanguageCode(): String? {
        return sharedPreferences.getString(LANGUAGE_CODE, "")
    }

    fun setUserLanguage(code: String) {
        sharedPreferences.edit().apply {
            putString(LANGUAGE_CODE, code)
            apply()
        }
    }

    fun setLastPlayID(stationId: String) {
        sharedPreferences.edit().apply {
            putString(LAST_PLAY, stationId)
            apply()
        }
    }

    fun getLastPlayID(): String? {
        return sharedPreferences.getString(LAST_PLAY, "")
    }

    fun setThemeMode(mode: Int) {
        sharedPreferences.edit().apply {
            putInt(MODE, mode)
            apply()
        }
    }

    fun getThemeMode(): Int {
        return sharedPreferences.getInt(MODE, ThemeMode.AUTO.id)
    }

    fun setDBVersion(version: Int) {
        sharedPreferences.edit().apply {
            putInt(VERSION, version)
            apply()
        }
    }

    fun getDBVersion(): Int {
        return sharedPreferences.getInt(VERSION, 1)
    }

    fun setHasPurchased(hasPurchased: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean(HAS_PURCHASED, hasPurchased)
            apply()
        }
    }

    fun getHasPurchased(): Boolean {
        return sharedPreferences.getBoolean(HAS_PURCHASED, true)
    }
}