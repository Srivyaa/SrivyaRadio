package com.app.srivyaradio.data.repositories

import android.content.SharedPreferences
import com.app.srivyaradio.utils.Constants.COUNTRY_CODE
import com.app.srivyaradio.utils.Constants.DEFAULT_SCREEN
import com.app.srivyaradio.utils.Constants.IS_FIRST_START
import com.app.srivyaradio.utils.Constants.LAST_PLAY
import com.app.srivyaradio.utils.Constants.MODE
import com.app.srivyaradio.utils.Constants.HAS_PURCHASED
import com.app.srivyaradio.utils.Constants.RECENTS_LIST
import com.app.srivyaradio.utils.Constants.USER_COUNTRIES
import com.app.srivyaradio.utils.Constants.VERSION
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

    // Default start screen
    fun getDefaultScreen(): String? {
        return sharedPreferences.getString(DEFAULT_SCREEN, null)
    }

    fun setDefaultScreen(screen: String) {
        sharedPreferences.edit().apply {
            putString(DEFAULT_SCREEN, screen)
            apply()
        }
    }

    // Recents (store up to 20 IDs, comma-separated)
    fun getRecents(): List<String> {
        val raw = sharedPreferences.getString(RECENTS_LIST, "") ?: ""
        return raw.split(',').map { it.trim() }.filter { it.isNotBlank() }
    }

    fun addRecent(id: String) {
        val list = getRecents().toMutableList()
        list.remove(id)
        list.add(0, id)
        while (list.size > 20) list.removeLast()
        val serialized = list.joinToString(",")
        sharedPreferences.edit().apply {
            putString(RECENTS_LIST, serialized)
            apply()
        }
    }

    // User-managed countries (serialize as name|code;name|code;...)
    fun getUserCountries(): List<Pair<String, String>> {
        val raw = sharedPreferences.getString(USER_COUNTRIES, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(';').mapNotNull { entry ->
            val parts = entry.split('|')
            val name = parts.getOrNull(0)?.trim().orEmpty()
            val code = parts.getOrNull(1)?.trim().orEmpty()
            if (name.isNotBlank() && code.isNotBlank()) name to code else null
        }
    }

    fun setUserCountries(countries: List<Pair<String, String>>) {
        val serialized = countries.joinToString(";") { (n, c) -> "$n|$c" }
        sharedPreferences.edit().apply {
            putString(USER_COUNTRIES, serialized)
            apply()
        }
    }
}