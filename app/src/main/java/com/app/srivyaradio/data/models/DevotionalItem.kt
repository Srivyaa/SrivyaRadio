package com.app.srivyaradio.data.models

import com.google.gson.annotations.SerializedName

data class DevotionalItem(
    @SerializedName("changeuuid")
    val changeUuid: String,
    
    @SerializedName("stationuuid")
    val stationUuid: String,
    
    @SerializedName("serveruuid")
    val serverUuid: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("album")
    val album: String,
    
    @SerializedName("artist")
    val artist: String,
    
    @SerializedName("year")
    val year: Int,
    
    @SerializedName("url")
    val url: String,
    
    @SerializedName("url_resolved")
    val urlResolved: String,
    
    @SerializedName("homepage")
    val homepage: String,
    
    @SerializedName("favurl")
    val favUrl: String,
    
    @SerializedName("tags")
    val tags: String,
    
    @SerializedName("country")
    val country: String,
    
    @SerializedName("countrycode")
    val countryCode: String,
    
    @SerializedName("language")
    val language: String,
    
    @SerializedName("bitrate")
    val bitrate: Int,
    
    @SerializedName("codec")
    val codec: String,
    
    @SerializedName("votes")
    val votes: Int,
    
    @SerializedName("negativevotes")
    val negativeVotes: Int,
    
    @SerializedName("clickcount")
    val clickCount: Int,
    
    @SerializedName("lastcheckok")
    val lastCheckOk: Int,
    
    @SerializedName("lastchecktime")
    val lastCheckTime: String,
    
    @SerializedName("clicktimestamp")
    val clickTimestamp: String
)