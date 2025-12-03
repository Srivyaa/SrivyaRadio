package com.app.srivyaradio.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Unified Station model that supports both regular radio stations and devotional items
 */
@Entity(tableName = "unified_stations")
data class UnifiedStation(
    @SerializedName("stationuuid") @PrimaryKey val id: String,
    @ColumnInfo(defaultValue = "") val favicon: String,
    @ColumnInfo(defaultValue = "") val name: String,
    @ColumnInfo(defaultValue = "") val country: String,
    @ColumnInfo(defaultValue = "0") val tags: String,
    @ColumnInfo(defaultValue = "") val countrycode: String,
    @ColumnInfo(defaultValue = "") val url_resolved: String,
    @ColumnInfo(defaultValue = "") val state: String = "",
    @ColumnInfo(defaultValue = "") val homepage: String = "",
    @ColumnInfo(defaultValue = "0") val rank: Int = 0,
    
    // Additional fields for devotional items
    @ColumnInfo(defaultValue = "") val title: String = "",
    @ColumnInfo(defaultValue = "") val album: String = "",
    @ColumnInfo(defaultValue = "") val artist: String = "",
    @ColumnInfo(defaultValue = "0") val year: Int = 0,
    @ColumnInfo(defaultValue = "") val url: String = "",
    @ColumnInfo(defaultValue = "") val favurl: String = "",
    @ColumnInfo(defaultValue = "") val language: String = "",
    @ColumnInfo(defaultValue = "0") val bitrate: Int = 0,
    @ColumnInfo(defaultValue = "") val codec: String = "",
    @ColumnInfo(defaultValue = "0") val votes: Int = 0,
    @ColumnInfo(defaultValue = "0") val negativeVotes: Int = 0,
    @ColumnInfo(defaultValue = "0") val clickCount: Int = 0,
    @ColumnInfo(defaultValue = "1") val lastCheckOk: Int = 1,
    @ColumnInfo(defaultValue = "") val lastCheckTime: String = "",
    @ColumnInfo(defaultValue = "") val clickTimestamp: String = "",
    @ColumnInfo(defaultValue = "") val changeUuid: String = "",
    @ColumnInfo(defaultValue = "") val serverUuid: String = "",
    
    // Source type to identify if this came from regular stations or devotional data
    @ColumnInfo(defaultValue = "regular") val sourceType: String = "regular"
)