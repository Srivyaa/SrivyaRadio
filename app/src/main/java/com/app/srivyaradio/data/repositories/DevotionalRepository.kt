package com.app.srivyaradio.data.repositories

import com.app.srivyaradio.data.api.devotional.DevotionalService
import com.app.srivyaradio.data.api.devotional.FoldersResponse
import com.app.srivyaradio.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DevotionalRepository {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://raw.githubusercontent.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service: DevotionalService = retrofit.create(DevotionalService::class.java)

    @Volatile
    private var cache: FoldersResponse? = null

    suspend fun getFolders(forceRefresh: Boolean = false): FoldersResponse {
        val cached = cache
        if (cached != null && !forceRefresh) return cached
        val base = Constants.FOLDERS_API_URL
        return try {
            // Try to load index.json listing (filenames or full URLs)
            val indexUrl = base.trimEnd('/') + "/index.json"
            val entries = service.getIndex(indexUrl)
            val urls = entries.map { e -> if (e.startsWith("http", true)) e else base + e }
                .filter { it.lowercase().endsWith(".json") }
            var metaName: String? = null
            var metaDesc: String? = null
            var metaHomepage: String? = null
            var metaGenerator: String? = null
            val allFolders = mutableListOf<com.app.srivyaradio.data.api.devotional.DevotionalFolder>()
            urls.forEach { url ->
                runCatching { service.getFolders(url) }.getOrNull()?.let { fr ->
                    if (metaName == null) {
                        metaName = fr.name
                        metaDesc = fr.description
                        metaHomepage = fr.homepage
                        metaGenerator = fr.generator
                    }
                    allFolders += fr.folders
                }
            }
            val aggregated = FoldersResponse(
                name = metaName,
                description = metaDesc,
                homepage = metaHomepage,
                generator = metaGenerator,
                folders = allFolders
            )
            cache = aggregated
            aggregated
        } catch (_: Exception) {
            // Fallback: probe known filenames and aggregate
            val candidates = listOf("devotional.json",  "samadadaalbums.json")
            var metaName: String? = null
            var metaDesc: String? = null
            var metaHomepage: String? = null
            var metaGenerator: String? = null
            val allFolders = mutableListOf<com.app.srivyaradio.data.api.devotional.DevotionalFolder>()
            candidates.forEach { file ->
                val url = base + file
                runCatching { service.getFolders(url) }.getOrNull()?.let { fr ->
                    if (metaName == null) {
                        metaName = fr.name
                        metaDesc = fr.description
                        metaHomepage = fr.homepage
                        metaGenerator = fr.generator
                    }
                    allFolders += fr.folders
                }
            }
            if (allFolders.isEmpty()) throw Exception("No folders JSON found under FOLDERS_API_URL")
            val aggregated = FoldersResponse(
                name = metaName,
                description = metaDesc,
                homepage = metaHomepage,
                generator = metaGenerator,
                folders = allFolders
            )
            cache = aggregated
            aggregated
        }
    }
}
