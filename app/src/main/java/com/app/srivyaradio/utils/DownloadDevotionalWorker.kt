package com.app.srivyaradio.utils

import android.app.Application
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.app.srivyaradio.data.api.stations.StationsClient
import com.app.srivyaradio.data.api.stations.StationsInterface
import com.app.srivyaradio.data.models.UnifiedStation
import com.app.srivyaradio.data.repositories.DatabaseRepository
import com.app.srivyaradio.utils.Constants.DEVOTIONAL_BASE_URL
import com.app.srivyaradio.utils.Constants.DEVOTIONAL_DATA_PATH
import com.app.srivyaradio.utils.StationConverter

class DownloadDevotionalWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val dbRepository: DatabaseRepository =
        DatabaseRepository(applicationContext as Application)

    override suspend fun doWork(): Result {
        return try {
            val baseUrl = inputData.getString(DEVOTIONAL_BASE_URL) ?: ""
            val dataPath = inputData.getString(DEVOTIONAL_DATA_PATH) ?: ""
            
            if (baseUrl.isEmpty()) {
                return Result.failure()
            }

            val devotionalService = StationsClient.getDevotionalService(baseUrl)
            val fullUrl = if (dataPath.isNotEmpty()) {
                "$baseUrl$dataPath"
            } else {
                baseUrl
            }
            
            val devotionalData = devotionalService.getDevotionalData(fullUrl)
            val unifiedStations = StationConverter.devotionalDataToUnifiedStations(devotionalData)
            
            // Insert unified stations into database
            dbRepository.insertUnifiedStations(unifiedStations)
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}