# Devotional JSON Format Support

This document explains how to add support for the new devotional JSON format to your SrivyaRadio Android app while maintaining backward compatibility with the existing format.

## Overview

The app now supports two JSON formats:

1. **Existing Format**: Array of radio stations with basic metadata
2. **New Devotional Format**: Structured format with folders containing devotional items

## JSON Format Comparison

### Existing Format (Radio Stations)
```json
[
    {
        "stationuuid": "4295f174-970b-478a-8746-218d9c651827",
        "name": "Station Name",
        "url_resolved": "https://example.com/stream",
        "favicon": "https://example.com/logo.png",
        "country": "Country Name",
        "tags": "genre;tags",
        "countrycode": "CC"
    }
]
```

### New Devotional Format
```json
{
    "name": "Tamil Devotional Songs • DEVOTIONAL Edition",
    "description": "Premium Tamil devotional songs collection",
    "homepage": "https://samadada.com",
    "generator": "SamaDada Custom Radio",
    "folders": [
        {
            "folder_name": "Aadhi Sivan",
            "folder_uuid": "bbd14d84-2419-4d0a-a231-61a03ed3c74f",
            "cover": "http://samadada.com/image.jpg",
            "year": 2024,
            "items": [
                {
                    "stationuuid": "51c4b089-6505-4a8d-ae41-f8f36144bd61",
                    "name": "Song Name",
                    "title": "Song Title",
                    "album": "Album Name",
                    "artist": "Artist Name",
                    "year": 2024,
                    "url": "https://files.example.com/song.mp3",
                    "url_resolved": "https://files.example.com/song.mp3",
                    "bitrate": 320,
                    "codec": "mp3"
                }
            ]
        }
    ]
}
```

## Implementation Components

### 1. Data Models

#### `DevotionalData.kt`
```kotlin
data class DevotionalData(
    val name: String,
    val description: String,
    val homepage: String,
    val generator: String,
    val folders: List<DevotionalFolder>
)

data class DevotionalFolder(
    val folderName: String,
    val folderUuid: String,
    val cover: String,
    val year: Int,
    val items: List<DevotionalItem>
)

data class DevotionalItem(
    // All the devotional item fields...
)
```

#### `UnifiedStation.kt`
A unified model that supports both formats:
```kotlin
@Entity(tableName = "unified_stations")
data class UnifiedStation(
    @PrimaryKey val id: String,
    // Standard fields (from regular stations)
    val favicon: String,
    val name: String,
    val country: String,
    // Extended fields (from devotional items)
    val title: String = "",
    val album: String = "",
    val artist: String = "",
    val year: Int = 0,
    val url: String = "",
    val favurl: String = "",
    // Source identification
    val sourceType: String = "regular" // or "devotional"
)
```

### 2. API Support

#### Updated `StationsInterface.kt`
```kotlin
interface StationsInterface {
    @GET("data/{country}.json")
    suspend fun getStations(@Path("country") country: String): List<Station>

    @GET
    suspend fun getDevotionalData(@Url url: String): DevotionalData
}
```

#### Enhanced `StationsClient.kt`
```kotlin
object StationsClient {
    fun getInstance(): Retrofit { /* existing */ }
    fun getDevotionalInstance(baseUrl: String): Retrofit
    fun getDevotionalService(baseUrl: String): StationsInterface
}
```

### 3. Repository Layer

#### `StationsRepository.kt`
```kotlin
class StationsRepository {
    suspend fun getStationsByCountry(country: String): Result<List<Station>>
    suspend fun getDevotionalStations(baseUrl: String, dataPath: String = ""): Result<List<UnifiedStation>>
    suspend fun getAllStations(country: String, devotionalBaseUrl: String? = null): Result<Pair<List<Station>, List<UnifiedStation>>>
    fun convertToUnifiedStations(stations: List<Station>): List<UnifiedStation>
}
```

### 4. Database Support

#### Updated `AppDatabase.kt`
- Added `UnifiedStation` entity to database
- Database version increased to 3
- Migration added to create `unified_stations` table

#### Extended `EntityDao.kt`
```kotlin
@Dao
interface EntityDao {
    // Existing methods...
    
    // Unified Station methods
    @Query("SELECT * FROM unified_stations ORDER BY name COLLATE NOCASE ASC")
    suspend fun getUnifiedStations(): List<UnifiedStation>
    
    @Query("SELECT * FROM unified_stations WHERE sourceType = :sourceType")
    suspend fun getUnifiedStationsByType(sourceType: String): List<UnifiedStation>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnifiedStations(unifiedStations: List<UnifiedStation>)
}
```

### 5. Media Integration

#### Enhanced `MediaItemFactory.kt`
```kotlin
object MediaItemFactory {
    var devotionalList: List<UnifiedStation> = listOf()
    
    fun unifiedStationToMediaItem(station: UnifiedStation, tag: String): MediaItem
    fun getDevotional(): List<MediaItem>
}
```

### 6. Background Processing

#### `DownloadDevotionalWorker.kt`
```kotlin
class DownloadDevotionalWorker : CoroutineWorker {
    override suspend fun doWork(): Result {
        // Download and process devotional data
        val devotionalData = apiService.getDevotionalData(url)
        val unifiedStations = StationConverter.devotionalDataToUnifiedStations(devotionalData)
        dbRepository.insertUnifiedStations(unifiedStations)
        return Result.success()
    }
}
```

## Usage Examples

### Loading Regular Stations (Existing)
```kotlin
val stationsRepository = StationsRepository()
val result = stationsRepository.getStationsByCountry("IN")
if (result.isSuccess) {
    val stations = result.getOrThrow()
    // Use existing stations...
}
```

### Loading Devotional Data (New)
```kotlin
val result = stationsRepository.getDevotionalStations(
    baseUrl = "https://files.samadada.com/devotional/",
    dataPath = "tamil/devotional.json"
)
if (result.isSuccess) {
    val devotionalStations = result.getOrThrow()
    // Use devotional stations...
}
```

### Loading Both Formats Together
```kotlin
val result = stationsRepository.getAllStations(
    country = "IN",
    devotionalBaseUrl = "https://files.samadada.com/devotional/",
    devotionalPath = "tamil/devotional.json"
)
if (result.isSuccess) {
    val (regular, devotional) = result.getOrThrow()
    println("Regular: ${regular.size}, Devotional: ${devotional.size}")
}
```

### Converting Between Formats
```kotlin
// Convert regular stations to unified format
val unifiedFromRegular = stationsRepository.convertToUnifiedStations(regularStations)

// Filter by source type
val devotionalOnly = allStations.filter { it.sourceType == "devotional" }
val regularOnly = allStations.filter { it.sourceType == "regular" }
```

## Key Benefits

1. **Backward Compatibility**: Existing code continues to work unchanged
2. **Unified Data Model**: Both formats can be handled uniformly when needed
3. **Extended Metadata**: Devotional items provide additional fields like album, artist, year
4. **Database Integration**: All data is stored in Room database for offline access
5. **Media Integration**: Both formats work with the existing media player system

## Migration Path

1. **Current Implementation**: App already has all components in place
2. **Database Migration**: Automatic migration from v2 to v3 will create unified_stations table
3. **API Integration**: New endpoints are available for devotional data
4. **UI Integration**: Media items can be created for both station types

## Constants Added

```kotlin
object Constants {
    const val DEVOTIONAL_ID = "devotional"
    const val DEVOTIONAL_BASE_URL = "devotionalBaseUrl"
    const val DEVOTIONAL_DATA_PATH = "devotionalDataPath"
}
```

## Implementation Status

✅ **Completed Components:**
- Data models (DevotionalData, DevotionalItem, UnifiedStation)
- API interface extensions
- Repository layer with conversion methods
- Database schema and migrations
- Media item factory integration
- Background workers
- Utility converters

The implementation is ready to use and fully supports both JSON formats while maintaining backward compatibility.