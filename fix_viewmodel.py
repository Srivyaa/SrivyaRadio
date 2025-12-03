
import os

file_path = r"c:\Users\Srivya\antigravity\SrivyaRadio\app\src\main\java\com\app\srivyaradio\ui\MainViewModel.kt"

with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

start_marker = "fun downloadStationMp3(station: Station) {"
end_marker = "suspend fun getFavoriteItem(id: String): Favorite? {"

start_idx = content.find(start_marker)
end_idx = content.find(end_marker)

if start_idx == -1 or end_idx == -1:
    print("Could not find markers")
    print(f"Start found: {start_idx != -1}")
    print(f"End found: {end_idx != -1}")
    exit(1)

new_content = """fun downloadStationMp3(station: Station) {
        try {
            val url = station.url_resolved
            val u = url.lowercase()
            val allowed = u.endsWith(".mp3") || u.endsWith(".aac") || u.endsWith(".m4a") || u.endsWith(".wav") || u.endsWith(".flac")
            if (!allowed) {
                Toast.makeText(application, "Only audio file links (mp3, aac, m4a, wav, flac) can be downloaded", Toast.LENGTH_SHORT).show()
                return
            }
            val input = Data.Builder()
                .putString("url", url)
                .putString("name", station.name)
                .putString("countryCode", station.countrycode)
                .putString("image", station.favicon)
                .build()
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val req = OneTimeWorkRequestBuilder<DownloadMp3Worker>()
                .setConstraints(constraints)
                .addTag("dl:" + url)
                .setInputData(input)
                .build()
            WorkManager.getInstance(application).enqueue(req)
            Toast.makeText(application, "Downloading ${station.name}", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(application, "Failed to start download", Toast.LENGTH_SHORT).show()
        }
    }

    fun downloadAllForCountry(code: String) {
        viewModelScope.launch {
            try {
                val cc = code.uppercase()
                val all = dbRepository.getAllStations(cc)
                val items = all.filter {
                    val u = it.url_resolved.lowercase()
                    u.endsWith(".mp3") || u.endsWith(".aac") || u.endsWith(".m4a") || u.endsWith(".wav") || u.endsWith(".flac")
                }
                items.forEach { downloadStationMp3(it) }
                if (items.isEmpty()) {
                    Toast.makeText(application, "No audio items found to download", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(application, "Downloading ${items.size} items in background", Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                Toast.makeText(application, "Failed to start downloads", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun playOfflineItems(items: List<DownloadedItem>, startIndex: Int) {
        try {
            val mediaItems = items.map { item ->
                val mediaItemBuilder = androidx.media3.common.MediaItem.Builder()
                    .setMediaId(com.app.srivyaradio.utils.Constants.OFFLINE_ID + ":" + (item.id?.toString() ?: item.sourceUrl))
                    .setUri(item.fileUri)

                val lower = item.fileUri.lowercase()
                val mime = when {
                    lower.endsWith(".mp3") -> androidx.media3.common.MimeTypes.AUDIO_MPEG
                    lower.endsWith(".aac") -> androidx.media3.common.MimeTypes.AUDIO_AAC
                    lower.endsWith(".m4a") -> "audio/mp4"
                    lower.endsWith(".wav") -> "audio/wav"
                    lower.endsWith(".flac") -> androidx.media3.common.MimeTypes.AUDIO_FLAC
                    else -> null
                }
                if (mime != null) mediaItemBuilder.setMimeType(mime)

                val metaBuilder = androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(item.name)
                    .setArtist(item.countrycode)
                    .setIsPlayable(true)
                if (item.image.isNotBlank()) metaBuilder.setArtworkUri(item.image.toUri())

                mediaItemBuilder.setMediaMetadata(metaBuilder.build()).build()
            }

            player.stop()
            player.clearMediaItems()
            // Enable shuffle/repeat for list
            player.shuffleModeEnabled = shuffleEnabled
            player.repeatMode = repeatMode
            
            player.setMediaItems(mediaItems, startIndex, 0)
            player.prepare()
            player.play()
            
            // Update queue state
            refreshQueue()
        } catch (_: Exception) {
            Toast.makeText(application, "Unable to play files", Toast.LENGTH_SHORT).show()
        }
    }

    fun loadRecents() {
        viewModelScope.launch {
            try {
                val ids = repository.getRecents()
                val list = ids.mapNotNull { dbRepository.getRadioStationByID(it) }
                recentStations = list
            } catch (_: Exception) {
                recentStations = listOf()
            }
        }
    }

    fun moveFav(from: Int, to: Int) {
        if (from == to) return
        val list = favoritesStations.toMutableList()
        if (from in list.indices && to in 0..list.size) {
            val item = list.removeAt(from)
            val insertAt = if (to > list.size) list.size else to
            list.add(insertAt, item)
            favoritesStations = list
        }
    }

    suspend fun reorderStations() {
        try {
            val favMap = dbRepository.getFavoriteStations()
            favoritesStations.forEachIndexed { index, station ->
                favMap[station]?.let { fav ->
                    dbRepository.updateFavoriteItem(
                        Favorite(fav.favId, fav.id, index.toLong())
                    )
                }
            }
            player.sendCustomCommand(
                SessionCommand(
                    Constants.UPDATE_FAVORITE_COMMAND,
                    Bundle.EMPTY
                ), Bundle.EMPTY
            )
        } catch (_: Exception) {
        }
    }

    """

final_content = content[:start_idx] + new_content + content[end_idx:]

with open(file_path, "w", encoding="utf-8") as f:
    f.write(final_content)

print("Successfully updated MainViewModel.kt")
