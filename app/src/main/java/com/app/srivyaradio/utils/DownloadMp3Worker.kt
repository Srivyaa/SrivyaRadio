package com.app.srivyaradio.utils

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.app.srivyaradio.data.models.DownloadedItem
import com.app.srivyaradio.data.repositories.DatabaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.URL

class DownloadMp3Worker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val url = inputData.getString("url") ?: return@withContext Result.failure()
        val name = inputData.getString("name") ?: "Audio"
        val country = inputData.getString("countryCode") ?: ""
        val image = inputData.getString("image") ?: ""

        val baseUrl = url.substringBefore('?')
        val lower = baseUrl.lowercase()
        val ext = when {
            lower.endsWith(".mp3") -> ".mp3"
            lower.endsWith(".aac") -> ".aac"
            lower.endsWith(".m4a") -> ".m4a"
            lower.endsWith(".wav") -> ".wav"
            lower.endsWith(".flac") -> ".flac"
            else -> return@withContext Result.failure()
        }
        val mime = when (ext) {
            ".mp3" -> "audio/mpeg"
            ".aac" -> "audio/aac"
            ".m4a" -> "audio/mp4"
            ".wav" -> "audio/wav"
            ".flac" -> "audio/flac"
            else -> "application/octet-stream"
        }

        val resolver = applicationContext.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, sanitizeFilename("$name$ext"))
            put(MediaStore.Downloads.MIME_TYPE, mime)
            // Relative path inside Downloads
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/SrivyaRadio")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val itemUri: Uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: return@withContext Result.retry()

        var totalBytes: Long = 0
        try {
            resolver.openOutputStream(itemUri)?.use { os ->
                BufferedOutputStream(os).use { bos ->
                    val connection = URL(url).openConnection()
                    connection.connect()
                    BufferedInputStream(connection.getInputStream()).use { bis ->
                        val buffer = ByteArray(8 * 1024)
                        while (true) {
                            val read = bis.read(buffer)
                            if (read == -1) break
                            bos.write(buffer, 0, read)
                            totalBytes += read
                        }
                        bos.flush()
                    }
                }
            }
            // Mark as not pending so it shows up in Downloads
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(itemUri, values, null, null)

            // Save to DB
            val repo = DatabaseRepository(applicationContext as Application)
            val existing = repo.getDownloadedItemBySourceUrl(url)
            if (existing == null) {
                repo.insertDownloadedItem(
                    DownloadedItem(
                        name = name,
                        countrycode = country.uppercase(),
                        sourceUrl = url,
                        fileUri = itemUri.toString(),
                        image = image,
                        sizeBytes = totalBytes,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
            Result.success()
        } catch (e: Exception) {
            // Cleanup failed file
            try { resolver.delete(itemUri, null, null) } catch (_: Exception) {}
            Result.retry()
        }
    }

    private fun sanitizeFilename(input: String): String =
        input.replace(Regex("[\\\\/:*?\"<>|]"), "_")
}
