package com.app.srivyaradio.data.repositories

import android.app.Application
import com.app.srivyaradio.data.database.AppDatabase
import com.app.srivyaradio.data.database.EntityDao
import com.app.srivyaradio.data.models.Favorite
import com.app.srivyaradio.data.models.Station

class DatabaseRepository(application: Application) {

    private val entityDao: EntityDao

    init {
        val db = AppDatabase.getInstance(application)
        entityDao = db.radioStationDao()
    }

    suspend fun getAllStations(countryCode: String): List<Station> {
        return entityDao.getStations(countryCode)
    }

    suspend fun insertStations(radioStations: List<Station>) {
        entityDao.insertStation(radioStations)
    }

    suspend fun getRadioStationByID(id: String): Station? {
        return entityDao.getStationById(id)
    }

    suspend fun getRadioStationByName(name: String): List<Station> {
        val pattern = toSqlLikePattern(name)
        return entityDao.getStationByName(pattern)
    }

    suspend fun getRadioStationByNameAndCountry(name: String, countryCode: String): List<Station> {
        val pattern = toSqlLikePattern(name)
        return entityDao.getStationByNameAndCountry(pattern, countryCode)
    }

    suspend fun insertFavoriteItem(favoriteStation: Favorite) {
        entityDao.insertFavoriteItem(favoriteStation)
    }

    suspend fun getFavoriteStations(): Map<Station, Favorite> {
        return entityDao.getFavoriteStations()
    }

    suspend fun getFavoriteItemById(id: String): Favorite? {
        return entityDao.getFavoriteStationById(id)
    }

    suspend fun deleteFavoriteItem(item: Favorite) {
        entityDao.deleteFavoriteStation(item)
    }

    suspend fun updateFavoriteItem(item: Favorite) {
        entityDao.updateFavoriteItem(item)
    }

    /**
     * Search for stations by name, tags, country, or state (case-insensitive)
     * Supports wildcard: '*' -> any sequence, '?' -> single char. Spaces are treated as wildcards.
     */
    suspend fun searchStations(query: String): List<Station> {
        val pattern = toSqlLikePattern(query)
        return entityDao.searchStations(pattern)
    }

    private fun toSqlLikePattern(input: String): String {
        // Normalize whitespace and translate wildcard chars to SQL LIKE equivalents
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""
        return trimmed
            .replace("*", "%")
            .replace("?", "_")
            .replace(Regex("\\s+"), "%")
    }
}