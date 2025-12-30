package com.app.srivyaradio.utils

import com.app.srivyaradio.data.models.Station
import com.app.srivyaradio.data.models.DevotionalItem
import com.app.srivyaradio.data.models.UnifiedStation

/**
 * Utility class to convert between different station formats
 */
object StationConverter {
    
    /**
     * Convert regular Station to UnifiedStation
     */
    fun stationToUnified(station: Station): UnifiedStation {
        return UnifiedStation(
            id = station.id,
            favicon = station.favicon,
            name = station.name,
            country = station.country,
            tags = station.tags,
            countrycode = station.countrycode,
            url_resolved = station.url_resolved,
            state = station.state,
            homepage = station.homepage,
            rank = station.rank,
            sourceType = "regular"
        )
    }
    
    /**
     * Convert DevotionalItem to UnifiedStation
     */
    fun devotionalItemToUnified(devotionalItem: DevotionalItem): UnifiedStation {
        return UnifiedStation(
            id = devotionalItem.stationUuid,
            favicon = devotionalItem.favUrl,
            name = devotionalItem.name,
            country = devotionalItem.country,
            tags = devotionalItem.tags,
            countrycode = devotionalItem.countryCode,
            url_resolved = devotionalItem.urlResolved,
            state = "",
            homepage = devotionalItem.homepage,
            rank = 0,
            title = devotionalItem.title,
            album = devotionalItem.album,
            artist = devotionalItem.artist,
            year = devotionalItem.year,
            url = devotionalItem.url,
            favurl = devotionalItem.favUrl,
            language = devotionalItem.language,
            bitrate = devotionalItem.bitrate,
            codec = devotionalItem.codec,
            votes = devotionalItem.votes,
            negativeVotes = devotionalItem.negativeVotes,
            clickCount = devotionalItem.clickCount,
            lastCheckOk = devotionalItem.lastCheckOk,
            lastCheckTime = devotionalItem.lastCheckTime,
            clickTimestamp = devotionalItem.clickTimestamp,
            changeUuid = devotionalItem.changeUuid,
            serverUuid = devotionalItem.serverUuid,
            sourceType = "devotional"
        )
    }
    
    /**
     * Convert UnifiedStation back to regular Station (for backward compatibility)
     * Only includes fields that exist in the original Station model
     */
    fun unifiedToStation(unifiedStation: UnifiedStation): Station {
        return Station(
            id = unifiedStation.id,
            favicon = unifiedStation.favicon,
            name = unifiedStation.name,
            country = unifiedStation.country,
            tags = unifiedStation.tags,
            countrycode = unifiedStation.countrycode,
            url_resolved = unifiedStation.url_resolved,
            state = unifiedStation.state,
            homepage = unifiedStation.homepage,
            rank = unifiedStation.rank
        )
    }
    
    /**
     * Extract all DevotionalItems from DevotionalData and convert them to UnifiedStation list
     */
    fun devotionalDataToUnifiedStations(devotionalData: com.app.srivyaradio.data.models.DevotionalData): List<UnifiedStation> {
        val allItems = mutableListOf<UnifiedStation>()
        
        devotionalData.folders.forEach { folder ->
            folder.items.forEach { item ->
                allItems.add(devotionalItemToUnified(item))
            }
        }
        
        return allItems
    }
}