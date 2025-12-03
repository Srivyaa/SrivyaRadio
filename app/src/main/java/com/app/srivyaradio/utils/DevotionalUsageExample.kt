package com.app.srivyaradio.utils

import com.app.srivyaradio.data.models.UnifiedStation
import com.app.srivyaradio.data.repositories.StationsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Example class demonstrating how to use the new devotional JSON format support
 */
object DevotionalUsageExample {
    
    /**
     * Example usage of both old and new JSON formats
     */
    suspend fun demonstrateUsage() {
        val stationsRepository = StationsRepository()
        
        // Example 1: Load regular radio stations (existing format)
        val regularStationsResult = stationsRepository.getStationsByCountry("IN")
        if (regularStationsResult.isSuccess) {
            val regularStations = regularStationsResult.getOrThrow()
            println("Loaded ${regularStations.size} regular radio stations")
        }
        
        // Example 2: Load devotional data (new format)
        val devotionalBaseUrl = "https://files.samadada.com/devotional/"
        val devotionalDataPath = "tamil/devotional.json"
        
        val devotionalStationsResult = stationsRepository.getDevotionalStations(
            baseUrl = devotionalBaseUrl,
            dataPath = devotionalDataPath
        )
        
        if (devotionalStationsResult.isSuccess) {
            val devotionalStations = devotionalStationsResult.getOrThrow()
            println("Loaded ${devotionalStations.size} devotional stations")
            
            // Show some details about the devotional stations
            devotionalStations.take(3).forEach { station ->
                println("Station: ${station.name}")
                println("  Album: ${station.album}")
                println("  Artist: ${station.artist}")
                println("  Year: ${station.year}")
                println("  Bitrate: ${station.bitrate}")
                println("  Source Type: ${station.sourceType}")
                println()
            }
        }
        
        // Example 3: Load both formats together
        val allStationsResult = stationsRepository.getAllStations(
            country = "IN",
            devotionalBaseUrl = "https://files.samadada.com/devotional/",
            devotionalPath = "tamil/devotional.json"
        )
        
        if (allStationsResult.isSuccess) {
            val (regular, devotional) = allStationsResult.getOrThrow()
            println("Combined load:")
            println("  Regular stations: ${regular.size}")
            println("  Devotional stations: ${devotional.size}")
            println("  Total stations: ${regular.size + devotional.size}")
        }
    }
    
    /**
     * Example of converting between station formats
     */
    suspend fun demonstrateConversion() {
        val stationsRepository = StationsRepository()
        
        // Load regular stations and convert to unified format
        val regularStationsResult = stationsRepository.getStationsByCountry("IN")
        if (regularStationsResult.isSuccess) {
            val regularStations = regularStationsResult.getOrThrow()
            val unifiedFromRegular = stationsRepository.convertToUnifiedStations(regularStations)
            
            println("Converted ${regularStations.size} regular stations to unified format")
            println("All converted stations have sourceType: 'regular'")
            
            // Load devotional stations in unified format
            val devotionalStationsResult = stationsRepository.getDevotionalStations(
                baseUrl = "https://files.samadada.com/devotional/",
                dataPath = "tamil/devotional.json"
            )
            
            if (devotionalStationsResult.isSuccess) {
                val devotionalStations = devotionalStationsResult.getOrThrow()
                
                // Combine both types
                val allUnifiedStations = unifiedFromRegular + devotionalStations
                println("Total unified stations: ${allUnifiedStations.size}")
                
                // Filter by source type
                val regularOnly = allUnifiedStations.filter { it.sourceType == "regular" }
                val devotionalOnly = allUnifiedStations.filter { it.sourceType == "devotional" }
                
                println("Regular stations: ${regularOnly.size}")
                println("Devotional stations: ${devotionalOnly.size}")
            }
        }
    }
    
    /**
     * Example of working with the unified station data
     */
    fun demonstrateUnifiedStationFeatures(stations: List<UnifiedStation>) {
        println("=== Unified Station Features ===")
        
        // Show source type distribution
        val sourceTypeCounts = stations.groupingBy { it.sourceType }.eachCount()
        println("Source type distribution:")
        sourceTypeCounts.forEach { (type, count) ->
            println("  $type: $count stations")
        }
        
        // Show albums for devotional stations
        val devotionalStations = stations.filter { it.sourceType == "devotional" }
        val albumGroups = devotionalStations.groupBy { it.album }.filterKeys { it.isNotEmpty() }
        
        println("\nAlbums in devotional stations:")
        albumGroups.forEach { (album, items) ->
            println("  $album: ${items.size} songs")
        }
        
        // Show bitrate information
        val bitrates = stations.mapNotNull { it.takeIf { it.bitrate > 0 }?.bitrate }.distinct().sorted()
        if (bitrates.isNotEmpty()) {
            println("\nAvailable bitrates: $bitrates")
        }
        
        // Show year distribution for devotional stations
        val years = devotionalStations.mapNotNull { it.takeIf { it.year > 0 }?.year }.distinct().sorted()
        if (years.isNotEmpty()) {
            println("Devotional station years: $years")
        }
    }
}