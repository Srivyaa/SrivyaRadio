package com.app.srivyaradio.wear.data.api

import com.app.srivyaradio.wear.data.models.Station
import retrofit2.http.GET
import retrofit2.http.Path

interface StationsApi {
    @GET("data/{country}.json")
    suspend fun getStations(
        @Path("country") country: String,
    ): List<Station>
}
