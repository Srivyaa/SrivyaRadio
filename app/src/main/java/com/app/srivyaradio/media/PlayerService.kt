package com.app.srivyaradio.media

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.session.CommandButton
import com.app.srivyaradio.MainActivity
import com.app.srivyaradio.data.api.location.LocationClient
import com.app.srivyaradio.data.api.location.LocationInterface
import com.app.srivyaradio.data.models.Favorite
import com.app.srivyaradio.data.repositories.DatabaseRepository
import com.app.srivyaradio.data.repositories.SharedPreferencesRepository
import com.app.srivyaradio.utils.Constants
import com.app.srivyaradio.utils.Constants.DISCOVER_ID
import com.app.srivyaradio.utils.Constants.FAVORITES_ID
import com.app.srivyaradio.utils.Constants.ROOT_ID
import com.app.srivyaradio.utils.Constants.SHARED_PREF
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PlayerService : MediaLibraryService() {
    lateinit var player: ExoPlayer
    private lateinit var mediaLibrarySession: MediaLibrarySession
    private lateinit var dbRepository: DatabaseRepository
    private lateinit var repository: SharedPreferencesRepository
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var countryCode = "US"

    private var timer: CountDownTimer? = null
    
    // Queue management for previous/next navigation
    private val currentQueue = mutableListOf<MediaItem>()
    private var currentQueueIndex = -1
    private var currentQueueTag = DISCOVER_ID

    private val retrofit = LocationClient.getInstance()
    var apiInterface: LocationInterface = retrofit.create(LocationInterface::class.java)
    
    private fun updatePlaybackState() {
        // Force a notification update to reflect favorite status changes
        mediaLibrarySession.broadcastCustomCommand(
            SessionCommand("UPDATE_PLAYBACK_STATE", Bundle.EMPTY),
            Bundle.EMPTY
        )
        updateCustomActions()
    }
    
    private fun updateCustomActions() {
        val currentMediaItem = player.currentMediaItem
        if (currentMediaItem != null) {
            serviceScope.launch {
                try {
                    // Get the actual station ID
                    val stationId = currentMediaItem.mediaId
                        .replace(DISCOVER_ID, "")
                        .replace(FAVORITES_ID, "")
                    
                    // Check if station is in favorites
                    val isFavorite = dbRepository.getFavoriteItemById(stationId) != null
                    
                    // Create custom action for toggling favorite
                    val favoriteActionExtras = Bundle().apply {
                        putString(Constants.TOGGLE_FAVORITE_STATION_ID_KEY, currentMediaItem.mediaId)
                    }
                    
                    val favoriteCommand = SessionCommand(Constants.TOGGLE_FAVORITE_COMMAND, favoriteActionExtras)
                    
                    // Create custom layout with favorite action
                    val customLayout = ImmutableList.of(
                        CommandButton.Builder()
                            .setSessionCommand(favoriteCommand)
                            .setDisplayName(if (isFavorite) "Remove from Favorites" else "Add to Favorites")
                            .setIconResId(if (isFavorite) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
                            .build()
                    )
                    
                    // Update the custom layout for all connected controllers
                    mediaLibrarySession.connectedControllers.forEach { controller ->
                        mediaLibrarySession.setCustomLayout(controller, customLayout)
                    }
                    
                    Log.d("PlayerService", "Updated custom actions for station $stationId, isFavorite: $isFavorite")
                } catch (e: Exception) {
                    Log.e("PlayerService", "Error updating custom actions: ${e.message}")
                }
            }
        }
    }
    
    @OptIn(UnstableApi::class)
    private fun updateAvailableCommands() {
        val commandsBuilder = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
        
        val hasNext = currentQueueIndex < currentQueue.size - 1
        val hasPrevious = currentQueueIndex > 0
        
        // Enable/disable next button based on queue state
        if (hasNext) {
            commandsBuilder.add(Player.COMMAND_SEEK_TO_NEXT)
        } else {
            commandsBuilder.remove(Player.COMMAND_SEEK_TO_NEXT)
        }
        
        // Enable/disable previous button based on queue state
        if (hasPrevious) {
            commandsBuilder.add(Player.COMMAND_SEEK_TO_PREVIOUS)
        } else {
            commandsBuilder.remove(Player.COMMAND_SEEK_TO_PREVIOUS)
        }
        
        val playerCommands = commandsBuilder.build()
        
        Log.d("PlayerService", "updateAvailableCommands: index=$currentQueueIndex, size=${currentQueue.size}, hasNext=$hasNext, hasPrevious=$hasPrevious, controllers=${mediaLibrarySession.connectedControllers.size}")
        
        // Update commands for all connected controllers
        mediaLibrarySession.connectedControllers.forEach { controller ->
            mediaLibrarySession.setAvailableCommands(
                controller,
                MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS,
                playerCommands
            )
        }
    }


    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this).setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true).build()

        dbRepository = DatabaseRepository(application)

        repository = SharedPreferencesRepository(
            application.getSharedPreferences(
                SHARED_PREF, MODE_PRIVATE
            )
        )

        fun getLastPlay() {
            val stationId = repository.getLastPlayID()
            serviceScope.launch {
                if (!stationId.isNullOrEmpty()) {
                    try {
                        val item = dbRepository.getRadioStationByID(
                            stationId.replace(DISCOVER_ID, "").replace(FAVORITES_ID, "")
                        )
                        if (item != null) {
                            player.setMediaItem(
                                MediaItemFactory.stationToMediaItem(
                                    item, DISCOVER_ID
                                )
                            )
                            // Update custom actions for the loaded station
                            updateCustomActions()
                        }
                    } catch (e: Exception) {
                        Log.e("error", e.message.toString())
                    }
                }
            }
        }

        getLastPlay()

        serviceScope.launch {
            val isFirstStartup = repository.isFirstStartUp()

            if (isFirstStartup) {
                try {
                    repository.setUserCountry(apiInterface.getIpInfo().countryCode)
                } catch (e: Exception) {
                    repository.setUserCountry(
                        "US"
                    )
                }
            }

            countryCode = repository.getCountryCode().toString()

            MediaItemFactory.getStations(countryCode, application, "load")
        }

        val sessionActivityPendingIntent = TaskStackBuilder.create(this).run {
            addNextIntent(Intent(this@PlayerService, MainActivity::class.java))

            val immutableFlag = PendingIntent.FLAG_IMMUTABLE
            getPendingIntent(0, immutableFlag or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        // Add player listener for queue navigation and favorite state updates
        player.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
                // Update available commands based on queue state
                updateAvailableCommands()
                
                // Update custom actions when media item changes
                if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                    updateCustomActions()
                }
            }
            
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                // Update favorite action when a new item starts playing
                updateCustomActions()
            }
        })

        mediaLibrarySession =
            MediaLibrarySession.Builder(this, player, MediaLibrarySessionCallback())
                .setSessionActivity(sessionActivityPendingIntent).build()


        MediaItemFactory.onFinishedLoading = {
            serviceScope.launch {
                MediaItemFactory.loadDiscover(dbRepository, countryCode)
                MediaItemFactory.loadFavorite(dbRepository)
            }
        }

        MediaItemFactory.onFinishedReadingDiscover = {
            mediaLibrarySession.notifyChildrenChanged(
                "discover", MediaItemFactory.getDiscover().size, null
            )
            serviceScope.launch {
                MediaItemFactory.checkVersion(countryCode, application)
            }
        }

        MediaItemFactory.onFinishedReadingFavorite = {
            mediaLibrarySession.notifyChildrenChanged(
                "favorites", MediaItemFactory.getFavorite().size, LibraryParams.Builder().apply {
                    setExtras(Bundle().apply {
                        putString("type", FAVORITES_ID)
                    })
                }.build()
            )
        }
        
        MediaItemFactory.onCountryStationsLoaded = { loadedCountryCode ->
            serviceScope.launch {
                try {
                    val stationCount = dbRepository.getAllStations(loadedCountryCode.uppercase()).size
                    Log.d("PlayerService", "Country stations loaded for $loadedCountryCode: $stationCount stations")
                    mediaLibrarySession.notifyChildrenChanged(
                        "${Constants.COUNTRY_PREFIX}$loadedCountryCode", 
                        stationCount, 
                        null
                    )
                } catch (e: Exception) {
                    Log.e("PlayerService", "Error notifying country stations loaded: ${e.message}")
                }
            }
        }

    }


    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return mediaLibrarySession
    }

    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        super.onUpdateNotification(session, true)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {

        val repository = SharedPreferencesRepository(
            application.getSharedPreferences(
                SHARED_PREF, MODE_PRIVATE
            )
        )

        player.currentMediaItem?.let { 
            repository.setLastPlayID(it.mediaId) 
        }

        super.onTaskRemoved(rootIntent)
        release()
        stopSelf()
    }

    override fun onDestroy() {
        release()
        super.onDestroy()
    }

    private fun release() {
        mediaLibrarySession.run {
            release()
            if (player.playbackState != Player.STATE_IDLE) {
                player.release()
            }
        }
        serviceScope.cancel()
    }

    private inner class MediaLibrarySessionCallback : MediaLibrarySession.Callback {
        
        override fun onPlayerCommandRequest(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            playerCommand: Int
        ): Int {
            when (playerCommand) {
                Player.COMMAND_SEEK_TO_NEXT -> {
                    if (currentQueueIndex < currentQueue.size - 1) {
                        currentQueueIndex++
                        player.setMediaItem(currentQueue[currentQueueIndex])
                        player.prepare()
                        player.play()
                        updateAvailableCommands()
                        updateCustomActions()
                    }
                    return SessionResult.RESULT_SUCCESS
                }
                Player.COMMAND_SEEK_TO_PREVIOUS -> {
                    if (currentQueueIndex > 0) {
                        currentQueueIndex--
                        player.setMediaItem(currentQueue[currentQueueIndex])
                        player.prepare()
                        player.play()
                        updateAvailableCommands()
                        updateCustomActions()
                    }
                    return SessionResult.RESULT_SUCCESS
                }
            }
            return super.onPlayerCommandRequest(session, controller, playerCommand)
        }
        
        @OptIn(UnstableApi::class)
        override fun onConnect(
            session: MediaSession, controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()

            availableSessionCommands.add(
                SessionCommand(
                    Constants.CHANGE_COUNTRY_COMMAND, Bundle.EMPTY
                )
            )

            availableSessionCommands.add(
                SessionCommand(
                    Constants.UPDATE_FAVORITE_COMMAND, Bundle.EMPTY
                )
            )
            
            availableSessionCommands.add(
                SessionCommand(
                    Constants.TOGGLE_FAVORITE_COMMAND, Bundle.EMPTY
                )
            )

            availableSessionCommands.add(
                SessionCommand(
                    Constants.SET_TIMER_COMMAND, Bundle.EMPTY
                )
            )
            
            availableSessionCommands.add(
                SessionCommand(
                    Constants.PLAY_NEXT_COMMAND, Bundle.EMPTY
                )
            )
            
            availableSessionCommands.add(
                SessionCommand(
                    Constants.PLAY_PREVIOUS_COMMAND, Bundle.EMPTY
                )
            )
            
            availableSessionCommands.add(
                SessionCommand(
                    Constants.SET_QUEUE_COMMAND, Bundle.EMPTY
                )
            )

            // Enable previous/next player commands based on queue state
            val availablePlayerCommands = connectionResult.availablePlayerCommands.buildUpon()
            
            // Only add next command if there's a next item in queue
            if (currentQueueIndex < currentQueue.size - 1) {
                availablePlayerCommands.add(Player.COMMAND_SEEK_TO_NEXT)
            }
            
            // Only add previous command if there's a previous item in queue
            if (currentQueueIndex > 0) {
                availablePlayerCommands.add(Player.COMMAND_SEEK_TO_PREVIOUS)
            }

            val updatedConnectionResult = MediaSession.ConnectionResult.accept(
                availableSessionCommands.build(), availablePlayerCommands.build()
            )
            
            // Set up initial custom actions if a media item is already playing
            if (player.currentMediaItem != null) {
                updateCustomActions()
            }
            
            return updatedConnectionResult
        }


        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            if (Constants.CHANGE_COUNTRY_COMMAND == customCommand.customAction) {
                countryCode = args.getString(Constants.CHANGE_COUNTRY_KEY).toString()
                MediaItemFactory.getStations(countryCode, application, "load")
            }
            if (Constants.UPDATE_FAVORITE_COMMAND == customCommand.customAction) {
                serviceScope.launch {
                    MediaItemFactory.loadFavorite(dbRepository)
                }
            }
            
            if (Constants.TOGGLE_FAVORITE_COMMAND == customCommand.customAction) {
                val stationId = args.getString(Constants.TOGGLE_FAVORITE_STATION_ID_KEY)
                if (!stationId.isNullOrEmpty()) {
                    serviceScope.launch {
                        try {
                            // Remove discover/favorites prefix to get the actual station ID
                            val actualStationId = stationId
                                .replace(DISCOVER_ID, "")
                                .replace(FAVORITES_ID, "")
                            
                            // Check if station is already in favorites
                            val favoriteItem = dbRepository.getFavoriteItemById(actualStationId)
                            
                            if (favoriteItem != null) {
                                // Remove from favorites
                                dbRepository.deleteFavoriteItem(favoriteItem)
                                Log.d("PlayerService", "Removed station $actualStationId from favorites")
                            } else {
                                // Add to favorites
                                dbRepository.insertFavoriteItem(
                                    Favorite(null, actualStationId, null)
                                )
                                Log.d("PlayerService", "Added station $actualStationId to favorites")
                            }
                            
                            // Reload the favorites list
                            MediaItemFactory.loadFavorite(dbRepository)
                            
                            // Update the current media item if it's the one being toggled
                            val currentMediaId = player.currentMediaItem?.mediaId
                            if (currentMediaId != null && 
                                (currentMediaId.contains(actualStationId))) {
                                // Update the playback state to reflect favorite status
                                updatePlaybackState()
                            }
                        } catch (e: Exception) {
                            Log.e("PlayerService", "Error toggling favorite: ${e.message}")
                        }
                    }
                }
            }

            if (Constants.SET_TIMER_COMMAND == customCommand.customAction) {
                val stopTimeMillis = args.getLong(Constants.SET_TIMER_KEY)

                timer?.cancel()

                if (stopTimeMillis.toInt() != 0) {
                    timer?.cancel()
                    timer = object : CountDownTimer(stopTimeMillis, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                        }

                        override fun onFinish() {
                            player.pause()
                        }
                    }.start()
                }
            }
            
            if (Constants.SET_QUEUE_COMMAND == customCommand.customAction) {
                val queueItems = args.getStringArrayList(Constants.SET_QUEUE_ITEMS_KEY) ?: arrayListOf()
                val currentIndex = args.getInt(Constants.SET_QUEUE_INDEX_KEY, -1)
                val tag = args.getString(Constants.SET_QUEUE_TAG_KEY, DISCOVER_ID)
                
                Log.d("PlayerService", "SET_QUEUE_COMMAND received: ${queueItems.size} items, index: $currentIndex")
                
                currentQueue.clear()
                serviceScope.launch {
                    queueItems.forEach { stationId ->
                        try {
                            val mediaItem = MediaItemFactory.getItemFromDB(dbRepository, "$tag$stationId")
                            currentQueue.add(mediaItem)
                        } catch (e: Exception) {
                            Log.e("PlayerService", "Error loading queue item: ${e.message}")
                        }
                    }
                    currentQueueIndex = currentIndex
                    currentQueueTag = tag
                    Log.d("PlayerService", "Queue loaded: ${currentQueue.size} items, index: $currentQueueIndex")
                    updateAvailableCommands()
                }
            }
            
            if (Constants.PLAY_NEXT_COMMAND == customCommand.customAction) {
                if (currentQueueIndex < currentQueue.size - 1) {
                    currentQueueIndex++
                    player.setMediaItem(currentQueue[currentQueueIndex])
                    player.prepare()
                    player.play()
                    updateAvailableCommands()
                    updateCustomActions()
                }
            }
            
            if (Constants.PLAY_PREVIOUS_COMMAND == customCommand.customAction) {
                if (currentQueueIndex > 0) {
                    currentQueueIndex--
                    player.setMediaItem(currentQueue[currentQueueIndex])
                    player.prepare()
                    player.play()
                    updateAvailableCommands()
                    updateCustomActions()
                }
            }

            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }

        @OptIn(UnstableApi::class)
        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            Log.d("PlayerService", "onGetLibraryRoot called by: ${browser.packageName}")
            
            // Create LibraryParams with content style hints for Android Auto
            val libraryParams = LibraryParams.Builder()
                .setExtras(Bundle().apply {
                    putInt("android.media.browse.CONTENT_STYLE_BROWSABLE_HINT", 2)
                    putInt("android.media.browse.CONTENT_STYLE_PLAYABLE_HINT", 2)
                })
                .build()
            
            return Futures.immediateFuture(LibraryResult.ofItem(MediaItemFactory.getRoot(), libraryParams))
        }

        override fun onGetItem(
            session: MediaLibrarySession, browser: MediaSession.ControllerInfo, mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val future = SettableFuture.create<LibraryResult<MediaItem>>()

            serviceScope.launch {
                try {
                    val mediaItem = MediaItemFactory.getItemFromDB(dbRepository, mediaId)
                    val result = LibraryResult.ofItem(mediaItem, null)
                    future.set(result)
                } catch (e: Exception) {
                    future.setException(e)
                }
            }

            return future
        }


        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {

            val future = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

            serviceScope.launch {
                try {
                    // If requesting children for a country item, ensure stations are downloaded
                    if (parentId.startsWith(Constants.COUNTRY_PREFIX)) {
                        val selectedCountryCode = parentId.removePrefix(Constants.COUNTRY_PREFIX)
                        val existingStations = dbRepository.getAllStations(selectedCountryCode.uppercase())
                        
                        // If no stations exist for this country, trigger download
                        if (existingStations.isEmpty()) {
                            Log.d("PlayerService", "No stations found for $selectedCountryCode, triggering download")
                            MediaItemFactory.getStations(selectedCountryCode, application, "country_browse_$selectedCountryCode")
                            // Return empty list for now, will be populated after download completes
                            future.set(LibraryResult.ofItemList(emptyList(), null))
                            return@launch
                        }
                    }
                    
                    val result = MediaItemFactory.getChildrenWithParent(
                        parentId, page, pageSize, dbRepository, countryCode
                    )
                    
                    Log.d("PlayerService", "onGetChildren: parentId=$parentId, browser=${browser.packageName}, count=${result.size}")
                    if (parentId == ROOT_ID) {
                        result.forEach { item ->
                            Log.d("PlayerService", "Root child: ${item.mediaId} - ${item.mediaMetadata.title}")
                        }
                    }
                    
                    future.set(LibraryResult.ofItemList(result, null))
                } catch (e: Exception) {
                    Log.e("PlayerService", "Error in onGetChildren: ${e.message}", e)
                    future.setException(e)
                }
            }
            return future
        }

        override fun onSubscribe(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
            val future = SettableFuture.create<LibraryResult<Void>>()

            serviceScope.launch {
                try {
                    // If subscribing to a country item, download stations for that country
                    if (parentId.startsWith(Constants.COUNTRY_PREFIX)) {
                        val selectedCountryCode = parentId.removePrefix(Constants.COUNTRY_PREFIX)
                        Log.d("PlayerService", "Subscribing to country: $selectedCountryCode, triggering station download")
                        MediaItemFactory.getStations(selectedCountryCode, application, "country_browse")
                        
                        // Wait a moment for the download to start and potentially complete
                        kotlinx.coroutines.delay(500)
                    }
                    
                    val children = MediaItemFactory.getChildrenWithParent(
                        parentId, 1, 20, dbRepository, countryCode
                    )
                    future.set(LibraryResult.ofVoid())
                    session.notifyChildrenChanged(browser, parentId, children.size, params)
                } catch (e: Exception) {
                    future.setException(e)
                }
            }

            return future
        }

        @UnstableApi
        override fun onSetMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
            startIndex: Int,
            startPositionMs: Long
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {

            if (mediaItems[0].requestMetadata.searchQuery != null) {
                val future = SettableFuture.create<MediaSession.MediaItemsWithStartPosition>()

                serviceScope.launch {
                    try {
                        val mediaItem = MediaItemFactory.getItemFromDBByName(
                            dbRepository,
                            mediaItems[0].requestMetadata.searchQuery.toString()
                                .substringBefore("on").substringBefore("from").trim().lowercase()
                        )
                        val result = MediaSession.MediaItemsWithStartPosition(
                            listOf(mediaItem), C.INDEX_UNSET, 0
                        )
                        future.set(result)
                    } catch (e: Exception) {
                        future.setException(e)
                    }
                }

                return future

            }
            
            val future = SettableFuture.create<MediaSession.MediaItemsWithStartPosition>()
            
            serviceScope.launch {
                try {
                    val selectedMediaId = mediaItems[0].mediaId
                    val mediaItem = if (selectedMediaId.startsWith(DISCOVER_ID) || selectedMediaId.startsWith(FAVORITES_ID)) {
                        MediaItemFactory.getItemFromDB(dbRepository, selectedMediaId)
                    } else {
                        mediaItems[0]
                    }
                    
                    // Determine which queue to build based on the media item
                    when {
                        selectedMediaId.startsWith(DISCOVER_ID) -> {
                            // Check if this is from a country-specific browse or general discover
                            val countryCode = mediaItem.mediaMetadata.extras?.getString("COUNTRY_CODE")
                            
                            val stationList = if (!countryCode.isNullOrEmpty() && countryCode != this@PlayerService.countryCode) {
                                // Load stations for the specific country
                                Log.d("PlayerService", "Loading stations for country: $countryCode")
                                dbRepository.getAllStations(countryCode.uppercase()).reversed().map {
                                    MediaItemFactory.stationToMediaItem(it, DISCOVER_ID)
                                }
                            } else {
                                // Use the current discover list
                                MediaItemFactory.getDiscover().ifEmpty {
                                    // If discover list is empty, load from database
                                    dbRepository.getAllStations(this@PlayerService.countryCode.uppercase()).reversed().map {
                                        MediaItemFactory.stationToMediaItem(it, DISCOVER_ID)
                                    }
                                }
                            }
                            
                            // Find the index of the selected item in the list
                            val itemIndex = stationList.indexOfFirst { 
                                it.mediaId == selectedMediaId 
                            }
                            
                            // Build the queue
                            currentQueue.clear()
                            currentQueue.addAll(stationList)
                            currentQueueIndex = if (itemIndex >= 0) itemIndex else 0
                            currentQueueTag = DISCOVER_ID
                            
                            Log.d("PlayerService", "onSetMediaItems (Discover): Queue built with ${currentQueue.size} items, index: $currentQueueIndex")
                            
                            val result = MediaSession.MediaItemsWithStartPosition(
                                listOf(mediaItem), 0, C.TIME_UNSET
                            )
                            future.set(result)
                            updateAvailableCommands()
                            updateCustomActions()
                        }
                        selectedMediaId.startsWith(FAVORITES_ID) -> {
                            // Load the full favorites list as queue
                            val favoriteList = MediaItemFactory.getFavorite()
                            val itemIndex = favoriteList.indexOfFirst { 
                                it.mediaId == selectedMediaId 
                            }
                            
                            // Build the queue
                            currentQueue.clear()
                            currentQueue.addAll(favoriteList)
                            currentQueueIndex = if (itemIndex >= 0) itemIndex else 0
                            currentQueueTag = FAVORITES_ID
                            
                            Log.d("PlayerService", "onSetMediaItems (Favorites): Queue built with ${currentQueue.size} items, index: $currentQueueIndex")
                            
                            val result = MediaSession.MediaItemsWithStartPosition(
                                favoriteList, itemIndex, C.TIME_UNSET
                            )
                            future.set(result)
                            updateAvailableCommands()
                            updateCustomActions()
                        }
                        else -> {
                            // For any other media item (e.g., from Android Auto browse without DISCOVER_ID prefix)
                            // Try to determine context from the controller info or metadata
                            Log.d("PlayerService", "onSetMediaItems: Unknown media ID pattern: $selectedMediaId")
                            
                            // Build a queue from the current context
                            // Check if we can get country code from the media item
                            val countryCode = mediaItem.mediaMetadata.extras?.getString("COUNTRY_CODE")
                            if (!countryCode.isNullOrEmpty()) {
                                // Load stations for this country as the queue
                                val stationList = dbRepository.getAllStations(countryCode.uppercase()).reversed().map {
                                    MediaItemFactory.stationToMediaItem(it, DISCOVER_ID)
                                }
                                
                                // Find the current item in the list
                                val itemIndex = stationList.indexOfFirst { station ->
                                    // Match by name or URL since we don't have the exact ID
                                    station.mediaMetadata.artist == mediaItem.mediaMetadata.artist ||
                                    station.requestMetadata.mediaUri == mediaItem.requestMetadata.mediaUri
                                }
                                
                                if (stationList.isNotEmpty()) {
                                    currentQueue.clear()
                                    currentQueue.addAll(stationList)
                                    currentQueueIndex = if (itemIndex >= 0) itemIndex else 0
                                    currentQueueTag = DISCOVER_ID
                                    
                                    Log.d("PlayerService", "onSetMediaItems (Country context): Queue built with ${currentQueue.size} items from $countryCode, index: $currentQueueIndex")
                                    updateAvailableCommands()
                                    updateCustomActions()
                                }
                            }
                            
                            // Return the requested item
                            val result = MediaSession.MediaItemsWithStartPosition(
                                mediaItems, 0, C.TIME_UNSET
                            )
                            future.set(result)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PlayerService", "Error in onSetMediaItems: ${e.message}")
                    future.setException(e)
                }
            }
            
            return future
        }

    }
}
