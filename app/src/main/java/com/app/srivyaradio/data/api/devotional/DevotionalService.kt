package com.app.srivyaradio.data.api.devotional

import retrofit2.http.GET
import retrofit2.http.Url

interface DevotionalService {
    @GET
    suspend fun getFolders(@Url url: String): FoldersResponse
    @GET
    suspend fun getIndex(@Url url: String): List<String>
}
