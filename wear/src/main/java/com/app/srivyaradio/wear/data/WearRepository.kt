package com.app.srivyaradio.wear.data

import android.util.Log
import com.app.srivyaradio.wear.data.api.StationsApi
import com.app.srivyaradio.wear.data.models.Station
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object WearRepository {
    private const val TAG = "WearRepository"
    private const val BASE_URL = "https://srivyaa.github.io/RadioStations/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val api: StationsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StationsApi::class.java)
    }

    suspend fun getStations(countryCode: String): List<Station> {
        return try {
            Log.d(TAG, "Fetching stations for country: $countryCode")
            val stations = api.getStations(countryCode)
            Log.d(TAG, "Successfully fetched ${stations.size} stations")
            stations
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching stations for $countryCode", e)
            e.printStackTrace()
            emptyList()
        }
    }
}
