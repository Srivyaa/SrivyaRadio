package com.app.srivyaradio.data.api.devotional

data class FoldersResponse(
    val name: String? = null,
    val description: String? = null,
    val homepage: String? = null,
    val generator: String? = null,
    val folders: List<DevotionalFolder> = emptyList()
)

data class DevotionalFolder(
    val folder_name: String,
    val folder_uuid: String,
    val cover: String,
    val year: Int? = null,
    val items: List<DevotionalSong> = emptyList()
)

data class DevotionalSong(
    val name: String? = null,
    val title: String? = null,
    val album: String? = null,
    val artist: String? = null,
    val url: String? = null,
    val url_resolved: String? = null,
    val favurl: String? = null,
    val year: Int? = null
)
