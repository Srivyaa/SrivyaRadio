package com.app.srivyaradio.data.api.stations

import com.app.srivyaradio.data.models.Station
import com.app.srivyaradio.data.models.DevotionalData
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface StationsInterface {
    @GET("data/{country}.json")
    suspend fun getStations(
        @Path("country") country: String,
    ): List<Station>

    @GET
    suspend fun getDevotionalData(
        @Url url: String
    ): DevotionalData

    @GET("version.txt")
    suspend fun getVersion(): String
}