package com.app.srivyaradio.media

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.MediaItem
import com.app.srivyaradio.data.models.Station
import com.app.srivyaradio.ui.MainViewModel

/**
 * Manages the queue of stations for the media player
 */
class QueueManager(private val viewModel: MainViewModel) {
    private val _currentQueue = mutableStateListOf<Station>()
    private var _currentIndex = -1
    private var _currentTag = "discover"

    val currentQueue: List<Station>
        get() = _currentQueue

    val currentIndex: Int
        get() = _currentIndex

    val currentTag: String
        get() = _currentTag

    /**
     * Add a station to the queue
     */
    fun addToQueue(station: Station, playNext: Boolean = false) {
        if (_currentQueue.none { it.id == station.id }) {
            val insertIndex = if (playNext && _currentIndex >= 0) {
                (_currentIndex + 1).coerceAtMost(_currentQueue.size)
            } else {
                _currentQueue.size
            }
            _currentQueue.add(insertIndex, station)
            
            if (_currentQueue.size == 1) {
                _currentIndex = 0
            }
        }
    }

    /**
     * Remove a station from the queue
     */
    fun removeFromQueue(station: Station) {
        val index = _currentQueue.indexOfFirst { it.id == station.id }
        if (index != -1 && index < _currentQueue.size) {
            _currentQueue.removeAt(index)
            if (index < _currentIndex) {
                _currentIndex--
            } else if (index == _currentIndex) {
                // If removing current item, keep index but ensure it's valid
                if (_currentIndex >= _currentQueue.size) {
                    _currentIndex = _currentQueue.size - 1
                }
            }
            // Ensure index stays valid
            if (_currentQueue.isEmpty()) {
                _currentIndex = -1
            }
        }
    }

    /**
     * Clear the queue
     */
    fun clearQueue() {
        _currentQueue.clear()
        _currentIndex = -1
    }

    /**
     * Set the entire queue with a list of stations and the current playing station
     */
    fun setQueue(stations: List<Station>, currentStation: Station, tag: String) {
        if (stations.isEmpty()) {
            _currentQueue.clear()
            _currentIndex = -1
            _currentTag = tag
            return
        }
        
        _currentQueue.clear()
        _currentQueue.addAll(stations)
        _currentIndex = stations.indexOfFirst { it.id == currentStation.id }
        
        // If station not found in list, add it at the beginning
        if (_currentIndex == -1) {
            _currentQueue.add(0, currentStation)
            _currentIndex = 0
        }
        
        _currentTag = tag
        
        // Sync queue with PlayerService for notification controls
        syncQueueWithService()
    }
    
    /**
     * Sync the current queue with the PlayerService
     */
    private fun syncQueueWithService() {
        if (_currentQueue.isEmpty() || _currentIndex < 0) return
        
        try {
            val stationIds = ArrayList(_currentQueue.map { it.id })
            
            viewModel.player.sendCustomCommand(
                androidx.media3.session.SessionCommand(
                    com.app.srivyaradio.utils.Constants.SET_QUEUE_COMMAND,
                    android.os.Bundle.EMPTY
                ),
                android.os.Bundle().apply {
                    putStringArrayList(com.app.srivyaradio.utils.Constants.SET_QUEUE_ITEMS_KEY, stationIds)
                    putInt(com.app.srivyaradio.utils.Constants.SET_QUEUE_INDEX_KEY, _currentIndex)
                    putString(com.app.srivyaradio.utils.Constants.SET_QUEUE_TAG_KEY, _currentTag)
                }
            )
        } catch (e: UninitializedPropertyAccessException) {
            android.util.Log.w("QueueManager", "Player not initialized yet, queue will sync later")
        } catch (e: Exception) {
            android.util.Log.e("QueueManager", "Error syncing queue with service: ${e.message}")
        }
    }

    /**
     * Get the next station in the queue
     */
    fun getNextStation(): Station? {
        return if (_currentIndex >= 0 && _currentIndex < _currentQueue.size - 1) {
            _currentQueue.getOrNull(_currentIndex + 1)
        } else {
            null
        }
    }

    /**
     * Get the previous station in the queue
     */
    fun getPreviousStation(): Station? {
        return if (_currentIndex > 0 && _currentIndex < _currentQueue.size) {
            _currentQueue.getOrNull(_currentIndex - 1)
        } else {
            null
        }
    }

    /**
     * Play the next station in the queue
     */
    fun playNext() {
        getNextStation()?.let { nextStation ->
            _currentIndex++
            viewModel.playStation(nextStation, _currentTag)
            syncQueueWithService()
        }
    }

    /**
     * Play the previous station in the queue
     */
    fun playPrevious() {
        getPreviousStation()?.let { prevStation ->
            _currentIndex--
            viewModel.playStation(prevStation, _currentTag)
            syncQueueWithService()
        }
    }

    /**
     * Check if there is a next station in the queue
     */
    fun hasNext(): Boolean {
        return _currentIndex >= 0 && _currentIndex < _currentQueue.size - 1 && _currentQueue.isNotEmpty()
    }

    /**
     * Check if there is a previous station in the queue
     */
    fun hasPrevious(): Boolean {
        return _currentIndex > 0 && _currentIndex < _currentQueue.size && _currentQueue.isNotEmpty()
    }
}
