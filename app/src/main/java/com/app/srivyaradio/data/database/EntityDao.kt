package com.app.srivyaradio.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.srivyaradio.data.models.Favorite
import com.app.srivyaradio.data.models.Station

@Dao
interface EntityDao {
    @Query("SELECT * FROM radio_stations WHERE LOWER(countrycode) = LOWER(:countryCode) ORDER BY name COLLATE NOCASE ASC")
    suspend fun getStations(countryCode: String): List<Station>

    @Query("SELECT * FROM radio_stations WHERE id = (:id)")
    suspend fun getStationById(id: String): Station?

    @Query(
        """
        SELECT * FROM radio_stations 
        WHERE 
            name LIKE '%' || :search || '%' COLLATE NOCASE 
            OR tags LIKE '%' || :search || '%' COLLATE NOCASE 
            OR country LIKE '%' || :search || '%' COLLATE NOCASE 
            OR state LIKE '%' || :search || '%' COLLATE NOCASE
        """
    )
    suspend fun searchStations(search: String): List<Station>

    @Query(
        """
        SELECT * FROM radio_stations 
        WHERE 
            (:title IS NULL OR name LIKE '%' || :title || '%' COLLATE NOCASE)
            AND (:artist IS NULL OR tags LIKE '%' || :artist || '%' COLLATE NOCASE)
            AND (:album IS NULL OR tags LIKE '%' || :album || '%' COLLATE NOCASE)
            AND (:genre IS NULL OR tags LIKE '%' || :genre || '%' COLLATE NOCASE)
            AND (:country IS NULL OR country LIKE '%' || :country || '%' COLLATE NOCASE OR countrycode LIKE '%' || :country || '%' COLLATE NOCASE)
            AND (:state IS NULL OR state LIKE '%' || :state || '%' COLLATE NOCASE)
            AND (
                :year IS NULL OR 
                name LIKE '%' || :year || '%' COLLATE NOCASE OR 
                tags LIKE '%' || :year || '%' COLLATE NOCASE OR 
                homepage LIKE '%' || :year || '%' COLLATE NOCASE
            )
            AND (
                :free IS NULL OR (
                    name LIKE '%' || :free || '%' COLLATE NOCASE OR 
                    tags LIKE '%' || :free || '%' COLLATE NOCASE OR 
                    country LIKE '%' || :free || '%' COLLATE NOCASE OR 
                    state LIKE '%' || :free || '%' COLLATE NOCASE
                )
            )
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    suspend fun advancedSearchStations(
        title: String?,
        artist: String?,
        album: String?,
        genre: String?,
        country: String?,
        state: String?,
        year: String?,
        free: String?
    ): List<Station>

    @Query("SELECT * FROM radio_stations WHERE name LIKE '%' || :search || '%' COLLATE NOCASE")
    suspend fun getStationByName(search: String): List<Station>

    @Query("SELECT * FROM radio_stations WHERE name LIKE '%' || :search || '%' COLLATE NOCASE and LOWER(countrycode) = LOWER(:countryCode)")
    suspend fun getStationByNameAndCountry(search: String, countryCode: String): List<Station>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStation(radioStations: List<Station>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteItem(favoriteStation: Favorite)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteItems(favoriteStations: List<Favorite>)

    @Query("SELECT * FROM radio_stations JOIN saved_items ON radio_stations.id = saved_items.id ORDER BY saved_items.`order`")
    suspend fun getFavoriteStations(): Map<Station, Favorite>

    @Query("SELECT * FROM saved_items WHERE id = (:id)")
    suspend fun getFavoriteStationById(id: String): Favorite?

    @Update
    suspend fun updateFavoriteItem(item:Favorite)

    @Delete
    suspend fun deleteFavoriteStation(favItem:Favorite)

    @Delete
    suspend fun deleteFavoriteStations(favItems:List<Favorite>)
}