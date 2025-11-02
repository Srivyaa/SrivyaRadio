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
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.session.CommandButton
import com.app.srivyaradio.R
import com.app.srivyaradio.MainActivity
import com.app.srivyaradio.data.api.location.LocationClient
import com.app.srivyaradio.data.api.location.LocationInterface
import com.app.srivyaradio.data.repositories.DatabaseRepository
import com.app.srivyaradio.data.repositories.SharedPreferencesRepository
import com.app.srivyaradio.utils.Constants
import com.app.srivyaradio.utils.Constants.DISCOVER_ID
import com.app.srivyaradio.utils.Constants.FAVORITES_ID
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

    // Track last requested browse node to refresh children after background loads
    private var lastBrowseParentId: String? = null
    private var lastBrowsePage: Int = 1
    private var lastBrowsePageSize: Int = 20

    private var timer: CountDownTimer? = null

    private val retrofit = LocationClient.getInstance()
    var apiInterface: LocationInterface = retrofit.create(LocationInterface::class.java)

    private fun updateCustomActions() {
        val mediaItem = player.currentMediaItem ?: run {
            mediaLibrarySession.setCustomLayout(emptyList())
            return
        }
        val mediaId = mediaItem.mediaId
        val id = mediaId.removePrefix(DISCOVER_ID).removePrefix(FAVORITES_ID)
        if (id.isBlank()) {
            mediaLibrarySession.setCustomLayout(emptyList())
            return
        }
        serviceScope.launch(Dispatchers.Main) {
            try {
                val isFav = dbRepository.getFavoriteItemById(id) != null
                val toggleCommand = SessionCommand(Constants.TOGGLE_FAVORITE_COMMAND, Bundle.EMPTY)
                val button = CommandButton.Builder()
                    .setDisplayName(if (isFav) "Remove from Favorites" else "Add to Favorites")
                    .setIconResId(if (isFav) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outlined)
                    .setSessionCommand(toggleCommand)
                    .build()
                mediaLibrarySession.setCustomLayout(listOf(button))
            } catch (_: Exception) {
                mediaLibrarySession.setCustomLayout(emptyList())
            }
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

        mediaLibrarySession =
            MediaLibrarySession.Builder(this, player, MediaLibrarySessionCallback())
                .setSessionActivity(sessionActivityPendingIntent).build()

        // Update custom actions when media item changes
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                updateCustomActions()
            }
        })


        MediaItemFactory.onFinishedLoading = {
            serviceScope.launch {
                // Keep existing behavior
                MediaItemFactory.loadDiscover(dbRepository, countryCode)
                MediaItemFactory.loadFavorite(dbRepository)

                // Also refresh the last browsed node (e.g., alpha:CC:L) so AA updates UI
                lastBrowseParentId?.let { parent ->
                    try {
                        val children = MediaItemFactory.getChildrenWithParent(
                            parent, lastBrowsePage, lastBrowsePageSize, dbRepository, countryCode
                        )
                        mediaLibrarySession.notifyChildrenChanged(parent, children.size, null)
                    } catch (_: Exception) {
                    }
                }
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
            updateCustomActions()
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

        if (player.currentMediaItem != null) {
            repository.setLastPlayID(player.currentMediaItem!!.mediaId)
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
                    Constants.SET_TIMER_COMMAND, Bundle.EMPTY
                )
            )

            availableSessionCommands.add(
                SessionCommand(
                    Constants.TOGGLE_FAVORITE_COMMAND, Bundle.EMPTY
                )
            )

            return MediaSession.ConnectionResult.accept(
                availableSessionCommands.build(), connectionResult.availablePlayerCommands
            )
        }


        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            if (Constants.CHANGE_COUNTRY_COMMAND == customCommand.customAction) {
                val newCode = args.getString(Constants.CHANGE_COUNTRY_KEY).toString()
                countryCode = newCode
                repository.setUserCountry(newCode)
                MediaItemFactory.getStations(newCode, application, "load")
            }
            if (Constants.UPDATE_FAVORITE_COMMAND == customCommand.customAction) {
                serviceScope.launch {
                    MediaItemFactory.loadFavorite(dbRepository)
                }
            }

            if (Constants.TOGGLE_FAVORITE_COMMAND == customCommand.customAction) {
                serviceScope.launch {
                    try {
                        val mediaId = player.currentMediaItem?.mediaId ?: ""
                        val id = mediaId.removePrefix(DISCOVER_ID).removePrefix(FAVORITES_ID)
                        if (id.isNotBlank()) {
                            val existing = dbRepository.getFavoriteItemById(id)
                            if (existing != null) {
                                dbRepository.deleteFavoriteItem(existing)
                            } else {
                                val order = dbRepository.getFavoriteStations().size.toLong()
                                dbRepository.insertFavoriteItem(com.app.srivyaradio.data.models.Favorite(null, id, order))
                            }
                            MediaItemFactory.loadFavorite(dbRepository)
                            updateCustomActions()
                        }
                    } catch (_: Exception) {}
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

            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            return Futures.immediateFuture(LibraryResult.ofItem(MediaItemFactory.getRoot(), params))
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
                    // Track last requested browse node
                    lastBrowseParentId = parentId
                    lastBrowsePage = page
                    lastBrowsePageSize = pageSize

                    // Preload data for requested node
                    if (parentId.startsWith(Constants.COUNTRY_PREFIX)) {
                        val code = parentId.removePrefix(Constants.COUNTRY_PREFIX).uppercase()
                        if (code.length == 2) {
                            if (code != countryCode) {
                                countryCode = code
                                repository.setUserCountry(code)
                            }
                            val count = dbRepository.getAllStations(code).size
                            if (count == 0) {
                                MediaItemFactory.getStations(code, application, "load")
                            }
                        } else {
                            // Custom categories (e.g., INDIA, TAMILFM)
                            val count = dbRepository.getAllStations(code).size
                            if (count == 0) {
                                MediaItemFactory.getStations(code, application, "load")
                            }
                        }
                    } else if (parentId.startsWith(Constants.ALPHABET_PREFIX)) {
                        val parts = parentId.removePrefix(Constants.ALPHABET_PREFIX).split(":")
                        val code = parts.getOrNull(0)?.uppercase().orEmpty()
                        if (code.length == 2) {
                            if (code != countryCode) {
                                countryCode = code
                                repository.setUserCountry(code)
                            }
                            val count = dbRepository.getAllStations(code).size
                            if (count == 0) {
                                MediaItemFactory.getStations(code, application, "load")
                            }
                        }
                    }

                    val result = MediaItemFactory.getChildrenWithParent(
                        parentId, page, pageSize, dbRepository, countryCode
                    )
                    future.set(LibraryResult.ofItemList(result, null))
                } catch (e: Exception) {
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
                    // Track last requested browse node
                    lastBrowseParentId = parentId
                    lastBrowsePage = 1
                    lastBrowsePageSize = 20

                    // Preload for requested node
                    if (parentId.startsWith(Constants.COUNTRY_PREFIX)) {
                        val code = parentId.removePrefix(Constants.COUNTRY_PREFIX).uppercase()
                        val count = dbRepository.getAllStations(code).size
                        if (count == 0) {
                            MediaItemFactory.getStations(code, application, "load")
                        }
                    } else if (parentId.startsWith(Constants.ALPHABET_PREFIX)) {
                        val parts = parentId.removePrefix(Constants.ALPHABET_PREFIX).split(":")
                        val code = parts.getOrNull(0)?.uppercase().orEmpty()
                        val count = dbRepository.getAllStations(code).size
                        if (count == 0) {
                            MediaItemFactory.getStations(code, application, "load")
                        }
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

            if (mediaItems.isEmpty()) {
                return Futures.immediateFuture(
                    MediaSession.MediaItemsWithStartPosition(emptyList(), 0, 0)
                )
            }

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
            val selectedItem = mediaItems.getOrNull(startIndex)?.let { it } ?: mediaItems[0]
            val future = SettableFuture.create<MediaSession.MediaItemsWithStartPosition>()

            serviceScope.launch {
                try {
                    val result = if (selectedItem.mediaId.startsWith(FAVORITES_ID)) {
                        val queue = MediaItemFactory.getFavorite()
                        val idx = queue.indexOfFirst { it.mediaId == selectedItem.mediaId }
                            .takeIf { it >= 0 } ?: MediaItemFactory.getItemIndex(selectedItem)
                        MediaSession.MediaItemsWithStartPosition(queue, idx, C.TIME_UNSET)
                    } else {
                        val extras = selectedItem.mediaMetadata.extras
                        val selectedId = when {
                            selectedItem.mediaId.startsWith(DISCOVER_ID) -> selectedItem.mediaId.removePrefix(DISCOVER_ID)
                            selectedItem.mediaId.startsWith(FAVORITES_ID) -> selectedItem.mediaId.removePrefix(FAVORITES_ID)
                            else -> selectedItem.mediaId
                        }
                        val selectedStation = dbRepository.getRadioStationByID(selectedId)
                        val itemCountry = selectedStation?.countrycode?.uppercase()
                            ?: extras?.getString("COUNTRY_CODE")?.uppercase()
                        val alpha = extras?.getString("BROWSE_ALPHA")?.firstOrNull()?.uppercaseChar()

                        // Persist country so app and AA stay in sync
                        if (!itemCountry.isNullOrEmpty() && itemCountry != countryCode) {
                            countryCode = itemCountry
                            repository.setUserCountry(itemCountry)
                        }

                        // Derive country from browse context if extras are missing
                        val browseCode = when {
                            lastBrowseParentId?.startsWith(Constants.ALPHABET_PREFIX) == true ->
                                lastBrowseParentId!!.removePrefix(Constants.ALPHABET_PREFIX).substringBefore(":").uppercase()
                            lastBrowseParentId?.startsWith(Constants.COUNTRY_PREFIX) == true ->
                                lastBrowseParentId!!.removePrefix(Constants.COUNTRY_PREFIX).uppercase()
                            else -> null
                        }

                        val code = (itemCountry ?: browseCode ?: countryCode).uppercase()
                        val stations = dbRepository.getAllStations(code)
                        val filtered = if (alpha != null) {
                            stations.filter { st ->
                                val n = st.name.trim()
                                n.isNotEmpty() && n[0].uppercaseChar() == alpha
                            }
                        } else {
                            stations
                        }
                        val queue = if (filtered.isEmpty() && selectedStation != null) {
                            // Data for this country may not be loaded yet; trigger load and play selected only
                            try { MediaItemFactory.getStations(code, application, "load") } catch (_: Exception) {}
                            listOf(MediaItemFactory.stationToMediaItem(selectedStation, DISCOVER_ID))
                        } else {
                            filtered.map { st -> MediaItemFactory.stationToMediaItem(st, DISCOVER_ID) }
                        }
                        val idx = queue.indexOfFirst { it.mediaId.endsWith(selectedId) }
                            .takeIf { it >= 0 } ?: 0
                        MediaSession.MediaItemsWithStartPosition(queue, idx, C.TIME_UNSET)
                    }
                    future.set(result)
                } catch (e: Exception) {
                    future.setException(e)
                }
            }

            return future
        }

    }
}
