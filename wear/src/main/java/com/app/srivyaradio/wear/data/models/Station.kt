package com.app.srivyaradio.wear.data.models

import com.google.gson.annotations.SerializedName

data class Station(
    @SerializedName("stationuuid") val id: String,
    val favicon: String,
    val name: String,
    val country: String,
    val tags: String,
    val countrycode: String,
    val url_resolved: String,
    val state: String = "",
    val homepage: String = "",
    val rank: Int = 0,
)
