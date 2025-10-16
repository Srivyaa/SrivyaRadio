package com.app.srivyaradio.data.api.location

import com.app.srivyaradio.data.models.Location
import retrofit2.http.GET

interface LocationInterface {
    @GET("json")
    suspend fun getIpInfo(): Location
}