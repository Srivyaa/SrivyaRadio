package com.app.srivyaradio.wear.data

import com.app.srivyaradio.wear.data.api.StationsApi
import com.app.srivyaradio.wear.data.models.Station
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WearRepository {
    private const val BASE_URL = "https://srivyaa.github.io/RadioStations/"

    private val api: StationsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StationsApi::class.java)
    }

    suspend fun getStations(countryCode: String): List<Station> {
        return try {
            api.getStations(countryCode)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
