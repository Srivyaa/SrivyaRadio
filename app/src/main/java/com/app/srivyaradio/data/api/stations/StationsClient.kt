package com.app.srivyaradio.data.api.stations

import com.app.srivyaradio.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object StationsClient {
    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(Constants.STATIONS_API_URL).addConverterFactory(
            GsonConverterFactory.create()
        ).build()
    }
}