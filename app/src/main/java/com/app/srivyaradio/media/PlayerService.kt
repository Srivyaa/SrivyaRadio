package com.app.srivyaradio.media

import android.content.Context.MODE_PRIVATE
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
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.session.CommandButton
import com.app.srivyaradio.MainActivity
import com.app.srivyaradio.data.api.location.LocationClient
import com.app.srivyaradio.data.api.location.LocationInterface
import com.app.srivyaradio.data.repositories.DatabaseRepository
import com.app.srivyaradio.data.repositories.SharedPreferencesRepository
import com.app.srivyaradio.utils.Constants
import com.app.srivyaradio.utils.Constants.DISCOVER_ID
import com.app.srivyaradio.utils.Constants.FAVORITES_ID
import com.app.srivyaradio.R
import com.app.srivyaradio.utils.Constants.SHARED_PREF
import com.app.srivyaradio.utils.countryList
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
    private val timerOptions = intArrayOf(0, 15, 30, 60, 90)
    private var timerIndex = 0

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
                val favButton = CommandButton.Builder()
                    .setDisplayName(if (isFav) "Remove Favorite" else "Add Favorite")
                    .setIconResId(if (isFav) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outlined)
                    .setSessionCommand(SessionCommand(Constants.TOGGLE_FAVORITE_COMMAND, Bundle.EMPTY))
                    .build()

                val shuffleIcon = if (player.shuffleModeEnabled) R.drawable.ic_shuffle else R.drawable.ic_shuffle_off
                val shuffleButton = CommandButton.Builder()
                    .setDisplayName(if (player.shuffleModeEnabled) "Shuffle On" else "Shuffle Off")
                    .setIconResId(shuffleIcon)
                    .setSessionCommand(SessionCommand(Constants.TOGGLE_SHUFFLE_COMMAND, Bundle.EMPTY))
                    .build()

                val (repeatIcon, repeatLabel) = when (player.repeatMode) {
                    Player.REPEAT_MODE_ONE -> R.drawable.ic_repeat_one to "Repeat One"
                    Player.REPEAT_MODE_ALL -> R.drawable.ic_repeat to "Repeat All"
                    else -> R.drawable.ic_repeat to "Repeat Off"
                }
                val repeatButton = CommandButton.Builder()
                    .setDisplayName(repeatLabel)
                    .setIconResId(repeatIcon)
                    .setSessionCommand(SessionCommand(Constants.CYCLE_REPEAT_COMMAND, Bundle.EMPTY))
                    .build()

                val seekBackButton = CommandButton.Builder()
                    .setDisplayName("Seek -10s")
                    .setIconResId(R.drawable.ic_fast_rewind)
                    .setSessionCommand(SessionCommand(Constants.SEEK_BACK_COMMAND, Bundle.EMPTY))
                    .build()

                val seekForwardButton = CommandButton.Builder()
                    .setDisplayName("Seek +10s")
                    .setIconResId(R.drawable.ic_fast_forward)
                    .setSessionCommand(SessionCommand(Constants.SEEK_FORWARD_COMMAND, Bundle.EMPTY))
                    .build()

                val timerButton = CommandButton.Builder()
                    .setDisplayName("Timer")
                    .setIconResId(R.drawable.ic_timer)
                    .setSessionCommand(SessionCommand(Constants.SET_TIMER_COMMAND, Bundle.EMPTY))
                    .build()

                // Order matters: first items are more likely to appear on the primary AA screen
                mediaLibrarySession.setCustomLayout(listOf(
                    favButton,
                    shuffleButton,
                    // Place the rest in overflow/additional actions
                    repeatButton,
                    seekBackButton,
                    seekForwardButton,
                    timerButton,
                ))
            } catch (_: Exception) {
                mediaLibrarySession.setCustomLayout(emptyList())
            }
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val httpFactory = androidx.media3.datasource.okhttp.OkHttpDataSource.Factory(okHttpClient)
            .setUserAgent("SrivyaRadio/3.0 (ExoPlayer)")
            .setDefaultRequestProperties(mapOf("Icy-MetaData" to "1"))

        val mediaSourceFactory = androidx.media3.exoplayer.source.DefaultMediaSourceFactory(this)
            .setDataSourceFactory(httpFactory)

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true)
            .setSeekBackIncrementMs(10_000)
            .setSeekForwardIncrementMs(10_000)
            .build()

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
            MediaLibrarySession.Builder(this, player, MediaLibrarySessionCallback(this))
                .setSessionActivity(sessionActivityPendingIntent).build()

        // Update custom actions when media item or modes change
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                updateCustomActions()
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                updateCustomActions()
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
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

    private class MediaLibrarySessionCallback(private val service: PlayerService) : MediaLibrarySession.Callback {
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

            availableSessionCommands.add(SessionCommand(Constants.TOGGLE_SHUFFLE_COMMAND, Bundle.EMPTY))
            availableSessionCommands.add(SessionCommand(Constants.CYCLE_REPEAT_COMMAND, Bundle.EMPTY))
            // Re-add seek custom commands for Android Auto additional actions
            availableSessionCommands.add(SessionCommand(Constants.SEEK_BACK_COMMAND, Bundle.EMPTY))
            availableSessionCommands.add(SessionCommand(Constants.SEEK_FORWARD_COMMAND, Bundle.EMPTY))

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
                service.countryCode = newCode
                service.repository.setUserCountry(newCode)
                MediaItemFactory.getStations(newCode, service.application, "load")
            }
            if (Constants.UPDATE_FAVORITE_COMMAND == customCommand.customAction) {
                service.serviceScope.launch {
                    MediaItemFactory.loadFavorite(service.dbRepository)
                }
            }

            if (Constants.TOGGLE_FAVORITE_COMMAND == customCommand.customAction) {
                service.serviceScope.launch {
                    try {
                        val mediaId = service.player.currentMediaItem?.mediaId ?: ""
                        val id = mediaId.removePrefix(DISCOVER_ID).removePrefix(FAVORITES_ID)
                        if (id.isNotBlank()) {
                            val existing = service.dbRepository.getFavoriteItemById(id)
                            if (existing != null) {
                                service.dbRepository.deleteFavoriteItem(existing)
                            } else {
                                val order = service.dbRepository.getFavoriteStations().size.toLong()
                                service.dbRepository.insertFavoriteItem(com.app.srivyaradio.data.models.Favorite(null, id, order))
                            }
                            MediaItemFactory.loadFavorite(service.dbRepository)
                            service.updateCustomActions()
                        }
                    } catch (_: Exception) {}
                }
            }

            if (Constants.SET_TIMER_COMMAND == customCommand.customAction) {
                // If explicit millis provided, respect it; else cycle through preset durations
                val hasKey = args.containsKey(Constants.SET_TIMER_KEY)
                if (hasKey) {
                    val stopTimeMillis = args.getLong(Constants.SET_TIMER_KEY)
                    service.timer?.cancel()
                    if (stopTimeMillis.toInt() != 0) {
                        service.timer = object : CountDownTimer(stopTimeMillis, 1000) {
                            override fun onTick(millisUntilFinished: Long) {}
                            override fun onFinish() { service.player.pause() }
                        }.start()
                    }
                } else {
                    // Cycle: Off -> 15 -> 30 -> 60 -> 90 -> Off
                    service.timerIndex = (service.timerIndex + 1) % service.timerOptions.size
                    val minutes = service.timerOptions[service.timerIndex]
                    service.timer?.cancel()
                    if (minutes > 0) {
                        val millis = minutes * 60_000L
                        service.timer = object : CountDownTimer(millis, 1000) {
                            override fun onTick(millisUntilFinished: Long) {}
                            override fun onFinish() { service.player.pause() }
                        }.start()
                    }
                }
            }

            if (Constants.TOGGLE_SHUFFLE_COMMAND == customCommand.customAction) {
                service.player.shuffleModeEnabled = !service.player.shuffleModeEnabled
                service.updateCustomActions()
            }

            if (Constants.CYCLE_REPEAT_COMMAND == customCommand.customAction) {
                val next = when (service.player.repeatMode) {
                    Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                    Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                    else -> Player.REPEAT_MODE_OFF
                }
                service.player.repeatMode = next
                service.updateCustomActions()
            }

            if (Constants.SEEK_BACK_COMMAND == customCommand.customAction) {
                service.player.seekBack()
            }

            if (Constants.SEEK_FORWARD_COMMAND == customCommand.customAction) {
                service.player.seekForward()
            }

            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }

        @OptIn(UnstableApi::class)
        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val extras = Bundle().apply {
                // Signal to Android Auto that search is supported (legacy-compatible key)
                putBoolean("android.media.browse.SEARCH_SUPPORTED", true)
            }
            val rootParams = LibraryParams.Builder().setExtras(extras).build()
            return Futures.immediateFuture(LibraryResult.ofItem(MediaItemFactory.getRoot(), rootParams))
        }

        override fun onGetItem(
            session: MediaLibrarySession, browser: MediaSession.ControllerInfo, mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val future = SettableFuture.create<LibraryResult<MediaItem>>()

            service.serviceScope.launch {
                try {
                    val mediaItem = MediaItemFactory.getItemFromDB(service.dbRepository, mediaId)
                    val result = LibraryResult.ofItem(mediaItem, null)
                    future.set(result)
                } catch (e: Exception) {
                    future.setException(e)
                }
            }

            return future
        }

        override fun onSearch(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
            val future = SettableFuture.create<LibraryResult<Void>>()

            service.serviceScope.launch {
                try {
                    val q = query.trim()
                    val total = if (q.isBlank()) 0 else service.dbRepository.searchStations(q).size
                    Log.d("AA-Search", "onSearch query='${q}', total=${total}")
                    future.set(LibraryResult.ofVoid())
                    session.notifySearchResultChanged(browser, query, total, null)
                } catch (e: Exception) {
                    future.setException(e)
                }
            }

            return future
        }

        override fun onGetSearchResult(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val future = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

            service.serviceScope.launch {
                try {
                    val q = query.trim()
                    val results = if (q.isBlank()) emptyList() else service.dbRepository.searchStations(q)
                    val size = if (pageSize > 0) pageSize else results.size
                    val from = (if (page >= 0) page else 0) * size
                    val pageItems = if (from >= results.size) emptyList() else results.drop(from).take(size)
                    // Mark items as originating from a search along with the query so we can rebuild the queue on play
                    val mediaItems = pageItems.map { st ->
                        val extras = Bundle().apply {
                            putBoolean("IS_SEARCH_RESULT", true)
                            putString("SEARCH_QUERY", q)
                        }
                        MediaItemFactory.stationToMediaItemWithExtras(st, DISCOVER_ID, extras)
                    }
                    Log.d("AA-Search", "onGetSearchResult query='${q}', page=${page}, size=${size}, returned=${mediaItems.size}")
                    future.set(LibraryResult.ofItemList(ImmutableList.copyOf(mediaItems), null))
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

            service.serviceScope.launch {
                try {
                    // Track last requested browse node
                    service.lastBrowseParentId = parentId
                    service.lastBrowsePage = page
                    service.lastBrowsePageSize = pageSize

                    // If requesting Favorites, include favorite country folders as browsable nodes before stations
                    if (parentId == com.app.srivyaradio.utils.Constants.FAVORITES_ID) {
                        val raw = service.dbRepository.getFavoriteEntries()
                        val codes = raw.mapNotNull { fav ->
                            val id = fav.id
                            if (id.startsWith(com.app.srivyaradio.utils.Constants.COUNTRY_PREFIX))
                                id.removePrefix(com.app.srivyaradio.utils.Constants.COUNTRY_PREFIX).uppercase()
                            else null
                        }.distinct()

                        // Map codes to names using static list first, then user-managed overrides
                        val userEntries = service.repository.getUserCountryEntries()
                        val userByCode = userEntries.associateBy { it.code.uppercase() }
                        val folderItems = codes.map { code ->
                            val name = countryList.find { it.second.equals(code, true) }?.first
                                ?: userByCode[code]?.name
                                ?: code
                            MediaItem.Builder()
                                .setMediaId(com.app.srivyaradio.utils.Constants.COUNTRY_PREFIX + code)
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setIsBrowsable(true)
                                        .setIsPlayable(false)
                                        .setTitle(name)
                                        .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                                        .setExtras(Bundle().apply { putString("COUNTRY_CODE", code) })
                                        .build()
                                )
                                .build()
                        }.sortedBy { it.mediaMetadata.title?.toString() ?: "" }

                        val stationItems = MediaItemFactory.getFavorite()
                        val items = folderItems + stationItems
                        future.set(LibraryResult.ofItemList(ImmutableList.copyOf(items), null))
                        return@launch
                    }

                    // If requesting Countries, include user-managed countries with overrides and soft-deletes
                    if (parentId == com.app.srivyaradio.utils.Constants.COUNTRIES_ID) {
                        val userEntries = service.repository.getUserCountryEntries()
                        val byCode = userEntries.associateBy { it.code.uppercase() }.toMutableMap()
                        val combinedPairs = mutableListOf<Pair<String, String>>()
                        countryList.forEach { (name, code) ->
                            val key = code.uppercase()
                            val override = byCode[key]
                            if (override != null) {
                                if (override.active) combinedPairs.add(override.name to key)
                                byCode.remove(key)
                            } else {
                                combinedPairs.add(name to key)
                            }
                        }
                        byCode.values.filter { it.active }.forEach { e -> combinedPairs.add(e.name to e.code.uppercase()) }
                        val items = combinedPairs.distinctBy { it.second.uppercase() }.map { (name, code) ->
                            MediaItem.Builder()
                                .setMediaId(com.app.srivyaradio.utils.Constants.COUNTRY_PREFIX + code.uppercase())
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setIsBrowsable(true)
                                        .setIsPlayable(false)
                                        .setTitle(name)
                                        .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                                        .setExtras(Bundle().apply { putString("COUNTRY_CODE", code.uppercase()) })
                                        .build()
                                )
                                .build()
                        }
                        future.set(LibraryResult.ofItemList(ImmutableList.copyOf(items), null))
                        return@launch
                    }

                    // Preload data for requested node
                    if (parentId.startsWith(Constants.COUNTRY_PREFIX)) {
                        val code = parentId.removePrefix(Constants.COUNTRY_PREFIX).uppercase()
                        if (code.length == 2) {
                            if (code != service.countryCode) {
                                service.countryCode = code
                                service.repository.setUserCountry(code)
                            }
                            val count = service.dbRepository.getAllStations(code).size
                            if (count == 0) {
                                MediaItemFactory.getStations(code, service.application, "load")
                            }
                        } else {
                            // Custom categories (e.g., INDIA, TAMILFM)
                            val count = service.dbRepository.getAllStations(code).size
                            if (count == 0) {
                                MediaItemFactory.getStations(code, service.application, "load")
                            }
                        }
                    } else if (parentId.startsWith(Constants.ALPHABET_PREFIX)) {
                        val parts = parentId.removePrefix(Constants.ALPHABET_PREFIX).split(":")
                        val code = parts.getOrNull(0)?.uppercase().orEmpty()
                        if (code.length == 2) {
                            if (code != service.countryCode) {
                                service.countryCode = code
                                service.repository.setUserCountry(code)
                            }
                            val count = service.dbRepository.getAllStations(code).size
                            if (count == 0) {
                                MediaItemFactory.getStations(code, service.application, "load")
                            }
                        }
                    }

                    val result = MediaItemFactory.getChildrenWithParent(
                        parentId, page, pageSize, service.dbRepository, service.countryCode
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

            service.serviceScope.launch {
                try {
                    // Track last requested browse node
                    service.lastBrowseParentId = parentId
                    service.lastBrowsePage = 1
                    service.lastBrowsePageSize = 20

                    // If subscribing to Favorites, include folders + station items
                    if (parentId == com.app.srivyaradio.utils.Constants.FAVORITES_ID) {
                        val raw = service.dbRepository.getFavoriteEntries()
                        val folderCount = raw.count { it.id.startsWith(com.app.srivyaradio.utils.Constants.COUNTRY_PREFIX) }
                        val stationCount = MediaItemFactory.getFavorite().size
                        val total = folderCount + stationCount
                        future.set(LibraryResult.ofVoid())
                        session.notifyChildrenChanged(browser, parentId, total, params)
                        return@launch
                    }

                    // If subscribing to Countries, notify with combined size (static + user-managed with overrides)
                    if (parentId == com.app.srivyaradio.utils.Constants.COUNTRIES_ID) {
                        val userEntries = service.repository.getUserCountryEntries()
                        val byCode = userEntries.associateBy { it.code.uppercase() }.toMutableMap()
                        val combined = mutableListOf<Pair<String, String>>()
                        countryList.forEach { (name, code) ->
                            val key = code.uppercase()
                            val override = byCode[key]
                            if (override != null) {
                                if (override.active) combined.add(override.name to key)
                                byCode.remove(key)
                            } else {
                                combined.add(name to key)
                            }
                        }
                        byCode.values.filter { it.active }.forEach { e -> combined.add(e.name to e.code.uppercase()) }
                        val total = combined.distinctBy { it.second.uppercase() }.size
                        future.set(LibraryResult.ofVoid())
                        session.notifyChildrenChanged(browser, parentId, total, params)
                        return@launch
                    }

                    // Preload for requested node
                    if (parentId.startsWith(Constants.COUNTRY_PREFIX)) {
                        val code = parentId.removePrefix(Constants.COUNTRY_PREFIX).uppercase()
                        val count = service.dbRepository.getAllStations(code).size
                        if (count == 0) {
                            MediaItemFactory.getStations(code, service.application, "load")
                        }
                    } else if (parentId.startsWith(Constants.ALPHABET_PREFIX)) {
                        val parts = parentId.removePrefix(Constants.ALPHABET_PREFIX).split(":")
                        val code = parts.getOrNull(0)?.uppercase().orEmpty()
                        val count = service.dbRepository.getAllStations(code).size
                        if (count == 0) {
                            MediaItemFactory.getStations(code, service.application, "load")
                        }
                    }

                    val children = MediaItemFactory.getChildrenWithParent(
                        parentId, 1, 20, service.dbRepository, service.countryCode
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

            // If selection came from search results, mirror app behavior:
            // - App sends a fully built list flagged with IS_SEARCH_RESULT, but without SEARCH_QUERY. Accept as-is.
            // - Android Auto sends one selected item with IS_SEARCH_RESULT + SEARCH_QUERY, or requestMetadata.searchQuery set. Build full queue from query.
            val selected = mediaItems.getOrNull(startIndex) ?: mediaItems[0]
            val selectedExtras = selected.mediaMetadata.extras
            val aaSearchQuery = try { selectedExtras?.getString("SEARCH_QUERY") } catch (_: Exception) { null }
            val isFlaggedSearch = try { selectedExtras?.getBoolean("IS_SEARCH_RESULT") == true } catch (_: Exception) { false }
            val hasRequestQuery = try { mediaItems.firstOrNull()?.requestMetadata?.searchQuery != null } catch (_: Exception) { false }

            // If it's an app-provided full search queue (flagged, but no query and no request query), accept as-is
            if (isFlaggedSearch && aaSearchQuery.isNullOrEmpty() && !hasRequestQuery && mediaItems.size > 1) {
                return Futures.immediateFuture(
                    MediaSession.MediaItemsWithStartPosition(mediaItems, startIndex, startPositionMs)
                )
            }

            // If it's an AA search selection (has query), rebuild full queue based on the query
            if ((isFlaggedSearch && !aaSearchQuery.isNullOrEmpty()) || hasRequestQuery) {
                val future = SettableFuture.create<MediaSession.MediaItemsWithStartPosition>()
                service.serviceScope.launch {
                    try {
                        val queryText = aaSearchQuery ?: mediaItems.first().requestMetadata.searchQuery.toString()
                        val stations = service.dbRepository.searchStations(queryText)
                        val marker = Bundle().apply {
                            putBoolean("IS_SEARCH_RESULT", true)
                            putString("SEARCH_QUERY", queryText)
                        }
                        val queue = stations.map { st ->
                            MediaItemFactory.stationToMediaItemWithExtras(st, DISCOVER_ID, marker)
                        }
                        val selId = selected.mediaId.removePrefix(DISCOVER_ID).removePrefix(FAVORITES_ID)
                        val idx = queue.indexOfFirst { it.mediaId.endsWith(selId) }.let { if (it >= 0) it else 0 }
                        future.set(MediaSession.MediaItemsWithStartPosition(queue, idx, 0))
                    } catch (e: Exception) {
                        future.setException(e)
                    }
                }
                return future
            }

            // (legacy single-item search handling removed; handled by the branch above)
            val selectedItem = mediaItems.getOrNull(startIndex)?.let { it } ?: mediaItems[0]
            val future = SettableFuture.create<MediaSession.MediaItemsWithStartPosition>()

            service.serviceScope.launch {
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
                        val selectedStation = service.dbRepository.getRadioStationByID(selectedId)
                        val itemCountry = selectedStation?.countrycode?.uppercase()
                            ?: extras?.getString("COUNTRY_CODE")?.uppercase()
                        val alpha = extras?.getString("BROWSE_ALPHA")?.firstOrNull()?.uppercaseChar()

                        // Persist country so app and AA stay in sync
                        if (!itemCountry.isNullOrEmpty() && itemCountry != service.countryCode) {
                            service.countryCode = itemCountry
                            service.repository.setUserCountry(itemCountry)
                        }

                        // Derive country from browse context if extras are missing
                        val browseCode = when {
                            service.lastBrowseParentId?.startsWith(Constants.ALPHABET_PREFIX) == true ->
                                service.lastBrowseParentId!!.removePrefix(Constants.ALPHABET_PREFIX).substringBefore(":").uppercase()
                            service.lastBrowseParentId?.startsWith(Constants.COUNTRY_PREFIX) == true ->
                                service.lastBrowseParentId!!.removePrefix(Constants.COUNTRY_PREFIX).uppercase()
                            else -> null
                        }

                        val code = (itemCountry ?: browseCode ?: service.countryCode).uppercase()
                        val stations = service.dbRepository.getAllStations(code)
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
                            try { MediaItemFactory.getStations(code, service.application, "load") } catch (_: Exception) {}
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
