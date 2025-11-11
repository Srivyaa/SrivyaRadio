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

    // --- New helpers for raw favorites (folders etc.) ---
    suspend fun getFavoriteEntries(): List<Favorite> = entityDao.getFavoriteEntries()
    suspend fun getFavoritesCount(): Long = entityDao.getFavoritesCount()
    suspend fun deleteFavoriteById(id: String) = entityDao.deleteFavoriteById(id)

    /**
     * Search for stations by name, tags, country, or state (case-insensitive)
     * Supports wildcard: '*' -> any sequence, '?' -> single char. Spaces are treated as wildcards.
     */
    suspend fun searchStations(query: String): List<Station> {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return emptyList()

        val filters = parseFieldedQuery(trimmed)
        return if (filters.hasAnyFilter()) {
            entityDao.advancedSearchStations(
                patternOrNull(filters.title),
                patternOrNull(filters.artist),
                patternOrNull(filters.album),
                patternOrNull(filters.genre),
                patternOrNull(filters.country),
                patternOrNull(filters.state),
                patternOrNull(filters.year),
                patternOrNull(filters.free)
            )
        } else {
            val pattern = toSqlLikePattern(trimmed)
            entityDao.searchStations(pattern)
        }
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

    // ----- Advanced search parsing -----
    private data class FieldFilters(
        val title: String? = null,
        val artist: String? = null,
        val album: String? = null,
        val genre: String? = null,
        val country: String? = null,
        val state: String? = null,
        val year: String? = null,
        val free: String? = null,
    ) {
        fun hasAnyFilter(): Boolean =
            listOf(title, artist, album, genre, country, state, year, free)
                .any { !it.isNullOrBlank() }
    }

    private fun parseFieldedQuery(input: String): FieldFilters {
        // Strategy: find all occurrences of key: value pairs; remainder is free text
        val keys = listOf("title", "artist", "album", "genre", "country", "state", "year")
        val keyRegex = Regex("(?i)(" + keys.joinToString("|") + ")\\s*:\\s*")

        val matches = keyRegex.findAll(input).toList()
        if (matches.isEmpty()) return FieldFilters(free = input)

        var title: String? = null
        var artist: String? = null
        var album: String? = null
        var genre: String? = null
        var country: String? = null
        var state: String? = null
        var year: String? = null

        val consumed = mutableListOf<IntRange>()
        for ((idx, m) in matches.withIndex()) {
            val key = m.groupValues[1].lowercase()
            val start = m.range.last + 1
            val end =
                if (idx + 1 < matches.size) matches[idx + 1].range.first - 1 else input.lastIndex
            val raw = input.substring(start, end + 1).trim()
            consumed.add(m.range.first..end)

            val value = raw.trim().removeSurrounding("\"", "\"")
            if (value.isBlank()) continue
            when (key) {
                "title" -> title = value
                "artist" -> artist = value
                "album" -> album = value
                "genre" -> genre = value
                "country" -> country = value
                "state" -> state = value
                "year" -> year = value
            }
        }

        // Build free text by removing consumed ranges
        val freeBuilder = StringBuilder()
        var cursor = 0
        val sorted = consumed.sortedBy { it.first }
        for (r in sorted) {
            if (cursor < r.first) freeBuilder.append(input.substring(cursor, r.first)).append(' ')
            cursor = r.last + 1
        }
        if (cursor <= input.lastIndex) freeBuilder.append(input.substring(cursor))
        val free = freeBuilder.toString().trim().ifBlank { null }

        return FieldFilters(title, artist, album, genre, country, state, year, free)
    }

    private fun patternOrNull(s: String?): String? = s?.let { v ->
        val t = v.trim()
        if (t.isEmpty()) null else toSqlLikePattern(t)
    }
}