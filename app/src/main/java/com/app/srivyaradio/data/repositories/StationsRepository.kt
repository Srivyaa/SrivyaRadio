package com.app.srivyaradio.data.repositories

import com.app.srivyaradio.data.api.stations.StationsClient
import com.app.srivyaradio.data.models.Station
import com.app.srivyaradio.data.models.UnifiedStation
import com.app.srivyaradio.utils.StationConverter

/**
 * Repository that handles both regular radio stations and devotional data
 */
class StationsRepository {
    private val apiService = StationsClient.getInstance().create(
        com.app.srivyaradio.data.api.stations.StationsInterface::class.java
    )
    
    /**
     * Get regular radio stations by country
     */
    suspend fun getStationsByCountry(country: String): Result<List<Station>> {
        return try {
            val stations = apiService.getStations(country)
            Result.success(stations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get devotional data from a specific URL and convert to UnifiedStation list
     */
    suspend fun getDevotionalStations(baseUrl: String, dataPath: String = ""): Result<List<UnifiedStation>> {
        return try {
            val fullUrl = if (dataPath.isNotEmpty()) {
                "$baseUrl$dataPath"
            } else {
                baseUrl
            }
            
            val devotionalService = StationsClient.getDevotionalService(baseUrl)
            val devotionalData = devotionalService.getDevotionalData(fullUrl)
            val unifiedStations = StationConverter.devotionalDataToUnifiedStations(devotionalData)
            
            Result.success(unifiedStations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get both regular stations and devotional stations combined
     */
    suspend fun getAllStations(
        country: String,
        devotionalBaseUrl: String? = null,
        devotionalPath: String = ""
    ): Result<Pair<List<Station>, List<UnifiedStation>>> {
        return try {
            val regularStations = apiService.getStations(country)
            
            val devotionalStations = if (devotionalBaseUrl != null) {
                val fullUrl = if (devotionalPath.isNotEmpty()) {
                    "$devotionalBaseUrl$devotionalPath"
                } else {
                    devotionalBaseUrl
                }
                
                val devotionalService = StationsClient.getDevotionalService(devotionalBaseUrl)
                val devotionalData = devotionalService.getDevotionalData(fullUrl)
                StationConverter.devotionalDataToUnifiedStations(devotionalData)
            } else {
                emptyList()
            }
            
            Result.success(Pair(regularStations, devotionalStations))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Convert regular stations to unified format
     */
    fun convertToUnifiedStations(stations: List<Station>): List<UnifiedStation> {
        return stations.map { StationConverter.stationToUnified(it) }
    }
}