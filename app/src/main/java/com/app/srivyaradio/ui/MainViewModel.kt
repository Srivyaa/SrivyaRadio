package com.app.srivyaradio.ui

import android.app.Application
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.media.session.PlaybackState
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.app.srivyaradio.MainActivity
import com.app.srivyaradio.R
import com.app.srivyaradio.data.api.stations.StationsClient
import com.app.srivyaradio.data.models.Favorite
import com.app.srivyaradio.data.models.Station
import com.app.srivyaradio.data.repositories.DatabaseRepository
import com.app.srivyaradio.data.repositories.SharedPreferencesRepository
import com.app.srivyaradio.media.MediaItemFactory
import com.app.srivyaradio.media.PlayerService
import com.app.srivyaradio.utils.Constants
import com.app.srivyaradio.utils.Constants.CHANGE_COUNTRY_COMMAND
import com.app.srivyaradio.utils.Constants.CHANGE_COUNTRY_KEY
import com.app.srivyaradio.utils.Constants.DISCOVER_ID
import com.app.srivyaradio.utils.Constants.FAVORITES_ID
import com.app.srivyaradio.utils.Constants.SHARED_PREF
import com.app.srivyaradio.utils.ThemeMode
import com.app.srivyaradio.utils.countryList
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.common.util.concurrent.ListenableFuture
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(private val application: Application) : AndroidViewModel(application) {

    private val repository = SharedPreferencesRepository(
        application.getSharedPreferences(
            SHARED_PREF,
            Context.MODE_PRIVATE
        )
    )
    private val dbRepository: DatabaseRepository = DatabaseRepository(application)

    private val retrofit = StationsClient.getInstance()

    var hasSaved by mutableStateOf(false)

    var selectedStation by mutableStateOf<Station?>(null)
        private set

    var appTheme by mutableStateOf(ThemeMode.AUTO)
        private set

    var selectedCountryCode by mutableStateOf("")
        private set

    var discoverStations by mutableStateOf<MutableList<Station>>(mutableListOf())
    var favoritesStations by mutableStateOf<List<Station>>(listOf())
    var searchStations by mutableStateOf<List<Station>>(listOf())
    var recentStations by mutableStateOf<List<Station>>(listOf())
    var queueStations by mutableStateOf<List<Station>>(listOf())

    var isRadioPlaying by mutableStateOf(false)
    var isRadioLoading by mutableStateOf(false)

    var currentSong by mutableStateOf("")

    // Recording and shuffle UI state
    var isRecording by mutableStateOf(false)
        private set
    var isShuffleOn by mutableStateOf(false)
        private set

    var isPremium by mutableStateOf(
        false
    )

    private val purchaseListener = UpdatedCustomerInfoListener { customerInfo ->
        isPremium = customerInfo.entitlements.active.isNotEmpty()
    }

    fun toggleShuffle() {
        try {
            isShuffleOn = !isShuffleOn
            if (this::player.isInitialized) {
                player.shuffleModeEnabled = isShuffleOn
            }
        } catch (_: Exception) { }
    }

    fun startRecording() {
        try {
            if (!this::player.isInitialized) return
            player.sendCustomCommand(
                SessionCommand(Constants.START_RECORDING_COMMAND, Bundle.EMPTY),
                Bundle.EMPTY
            )
            isRecording = true
        } catch (_: Exception) { }
    }

    fun stopRecording() {
        try {
            if (!this::player.isInitialized) return
            player.sendCustomCommand(
                SessionCommand(Constants.STOP_RECORDING_COMMAND, Bundle.EMPTY),
                Bundle.EMPTY
            )
            isRecording = false
        } catch (_: Exception) { }
    }

    fun toggleRecording() {
        if (isRecording) stopRecording() else startRecording()
    }

    private lateinit var playerFuture: ListenableFuture<MediaBrowser>
    lateinit var player: MediaBrowser

    var page = 1
    var pageSize = 20

    var open = false
    var openID = ""

    var totalClickCount: Int = 0
    var totalImpressionCount: Int = 0
    var totalPlayCount: Int = 0
    var lastAdShownTime: Long = 0

    private fun loadBitmapFromUrl(imageUrl: String, callback: (Bitmap?) -> Unit) {
        Glide.with(application).setDefaultRequestOptions(
            RequestOptions()
                .placeholder(R.drawable.icon_launcher)
                .error(R.drawable.icon_launcher)
                .transform(CenterCrop())
                .diskCacheStrategy(DiskCacheStrategy.DATA)
        )
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    callback(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })
    }

    private fun getCurrentItem() {
        try {
            val item = player.currentMediaItem ?: return
            val mediaId = item.mediaId
            val id = mediaId.replace(DISCOVER_ID, "").replace(FAVORITES_ID, "")
            viewModelScope.launch {
                try {
                    selectedStation = dbRepository.getRadioStationByID(id)
                    // Update recents list
                    repository.addRecent(id)
                    loadRecents()
                } catch (_: Exception) { }
            }
        } catch (_: Exception) { }
    }

    fun setCountryCode(index: Int) {
        val location = countryList[index]
        if (location.second != selectedCountryCode) {
            selectedCountryCode = location.second
            repository.setUserCountry(selectedCountryCode)
            discoverStations = mutableListOf()
            player.sendCustomCommand(
                SessionCommand(
                    CHANGE_COUNTRY_COMMAND, Bundle.EMPTY
                ), Bundle().apply {
                    putString(CHANGE_COUNTRY_KEY, selectedCountryCode)
                }
            )
        }
    }

    // Dynamic country list for UI
    fun getCountryListForUI(): List<Pair<String, String>> {
        val user = repository.getUserCountries()
        return countryList + user
    }

    fun setCountryCodeByCode(code: String) {
        if (code != selectedCountryCode) {
            selectedCountryCode = code
            repository.setUserCountry(selectedCountryCode)
            discoverStations = mutableListOf()
            player.sendCustomCommand(
                SessionCommand(CHANGE_COUNTRY_COMMAND, Bundle.EMPTY),
                Bundle().apply { putString(CHANGE_COUNTRY_KEY, selectedCountryCode) }
            )
        }
    }

    fun playStation(station: Station, tag: String, openPlayer: Boolean = false) {
        try {
            selectedStation = station
            isRadioLoading = true
            player.setMediaItem(MediaItemFactory.stationToMediaItem(station, tag))
            player.prepare()
            player.play()
        } catch (_: Exception) {
            isRadioLoading = false
            Toast.makeText(application, "Playback failed", Toast.LENGTH_SHORT).show()
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            try {
                val q = query.trim()
                searchStations = if (q.isBlank()) {
                    listOf()
                } else {
                    dbRepository.searchStations(q)
                }
            } catch (_: Exception) {
                searchStations = listOf()
            }
        }
    }

    // Recents
    fun loadRecents() {
        viewModelScope.launch {
            try {
                val ids = repository.getRecents()
                val list = ids.mapNotNull { dbRepository.getRadioStationByID(it) }
                recentStations = list
            } catch (_: Exception) { recentStations = listOf() }
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
        } catch (_: Exception) { }
    }

    suspend fun getFavoriteItem(id: String): Favorite? {
        return try {
            dbRepository.getFavoriteItemById(id)
        } catch (_: Exception) { null }
    }

    suspend fun addOrRemoveFromFavorites(id: String) {
        try {
            val existing = dbRepository.getFavoriteItemById(id)
            if (existing != null) {
                dbRepository.deleteFavoriteItem(existing)
            } else {
                val order = dbRepository.getFavoriteStations().size.toLong()
                dbRepository.insertFavoriteItem(Favorite(null, id, order))
            }

            val updated = dbRepository.getFavoriteStations().keys.toList()
            favoritesStations = updated
            hasSaved = updated.isNotEmpty()

            player.sendCustomCommand(
                SessionCommand(
                    Constants.UPDATE_FAVORITE_COMMAND,
                    Bundle.EMPTY
                ), Bundle.EMPTY
            )
        } catch (_: Exception) { }
    }

    fun skipToPrevious() {
        try {
            if (this::player.isInitialized) {
                player.seekToPreviousMediaItem()
                if (!player.isPlaying) player.play()
            }
        } catch (_: Exception) { }
    }

    fun skipToNext() {
        try {
            if (this::player.isInitialized) {
                player.seekToNextMediaItem()
                if (!player.isPlaying) player.play()
            }
        } catch (_: Exception) { }
    }

    fun resetPlayer() {
        try {
            if (!this::player.isInitialized) return
            isRadioLoading = true
            if (player.currentMediaItem != null) {
                player.seekToDefaultPosition()
            }
            player.prepare()
            player.play()
        } catch (_: Exception) {
            isRadioLoading = false
        }
    }

    fun openRadioFromLink(id: String) {
        if (!this::player.isInitialized) {
            open = true
            openID = id
            return
        }
        viewModelScope.launch {
            try {
                val item = dbRepository.getRadioStationByID(id)
                if (item != null) {
                    playStation(item, DISCOVER_ID, true)
                } else {
                    open = true
                    openID = id
                }
            } catch (_: Exception) {
                open = true
                openID = id
            }
        }
    }

    fun addStation(name: String, link: String) {
        viewModelScope.launch {
            try {
                val id = UUID.randomUUID().toString()
                val station = Station(
                    id = id,
                    favicon = "",
                    name = name,
                    country = "",
                    tags = "",
                    countrycode = selectedCountryCode.ifBlank { "" },
                    url_resolved = link,
                    state = "",
                    homepage = "",
                    rank = 0,
                )
                dbRepository.insertStations(listOf(station))
                val order = dbRepository.getFavoriteStations().size.toLong()
                dbRepository.insertFavoriteItem(Favorite(null, id, order))

                val updated = dbRepository.getFavoriteStations().keys.toList()
                favoritesStations = updated
                hasSaved = updated.isNotEmpty()

                player.sendCustomCommand(
                    SessionCommand(
                        Constants.UPDATE_FAVORITE_COMMAND,
                        Bundle.EMPTY
                    ), Bundle.EMPTY
                )
            } catch (_: Exception) { }
        }
    }

    fun onPurchase() {
        Toast.makeText(application, "Purchases UI not implemented yet", Toast.LENGTH_SHORT).show()
    }

    // Default screen preference
    fun getDefaultScreen(): String? = repository.getDefaultScreen()
    fun setDefaultScreen(screenRoute: String) { repository.setDefaultScreen(screenRoute) }

    fun getStartDestinationRoute(): String {
        val pref = getDefaultScreen()
        return when {
            pref != null -> pref
            hasSaved -> com.app.srivyaradio.ui.navigation.NavigationItem.Favorites.route
            else -> com.app.srivyaradio.ui.navigation.NavigationItem.Discover.route
        }
    }

    // Queue management
    fun refreshQueue() {
        try {
            val items = (0 until player.mediaItemCount).mapNotNull { idx ->
                val mediaId = player.getMediaItemAt(idx).mediaId
                val id = mediaId.replace(DISCOVER_ID, "").replace(FAVORITES_ID, "")
                // Will fill on background
                id
            }
            viewModelScope.launch {
                val stations = items.mapNotNull { dbRepository.getRadioStationByID(it) }
                queueStations = stations
            }
        } catch (_: Exception) { queueStations = listOf() }
    }

    fun moveInQueue(from: Int, to: Int) {
        try {
            player.moveMediaItem(from, to)
            refreshQueue()
        } catch (_: Exception) { }
    }

    fun removeFromQueue(index: Int) {
        try {
            player.removeMediaItem(index)
            refreshQueue()
        } catch (_: Exception) { }
    }

    fun clearQueue() {
        try {
            player.clearMediaItems()
            refreshQueue()
        } catch (_: Exception) { }
    }

    // Manage user countries
    fun addUserCountry(name: String, code: String) {
        val trimmedName = name.trim()
        val trimmedCode = code.trim().uppercase()
        if (trimmedName.isBlank() || trimmedCode.isBlank()) return
        val current = repository.getUserCountries().toMutableList()
        if (current.any { it.second.equals(trimmedCode, ignoreCase = true) }) return
        current.add(trimmedName to trimmedCode)
        repository.setUserCountries(current)
    }

    fun updateUserCountry(oldName: String, oldCode: String, newName: String, newCode: String) {
        val current = repository.getUserCountries().toMutableList()
        val idx = current.indexOfFirst { it.first == oldName && it.second.equals(oldCode, true) }
        if (idx >= 0) {
            current[idx] = newName.trim() to newCode.trim().uppercase()
            repository.setUserCountries(current)
        }
    }

    fun removeUserCountry(name: String, code: String) {
        val current = repository.getUserCountries().toMutableList()
        current.removeAll { it.first == name && it.second.equals(code, true) }
        repository.setUserCountries(current)
    }

    private fun getCountryCode() {
        selectedCountryCode = repository.getCountryCode() ?: "US"
    }

    fun playOrPause() {
        isRadioLoading = true

        if (player.isPlaying) {
            player.pause()
            isRadioLoading = false
        } else {
            if (player.currentMediaItem != null) {
                player.seekToDefaultPosition()
            }
            player.prepare()
            player.play()
            if (isRadioPlaying) {
                isRadioLoading = false
            }
        }
    }


    private fun listenToPlayer() {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying != isRadioPlaying) {
                    isRadioPlaying = isPlaying
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == PlaybackState.STATE_STOPPED) {
                    isRadioPlaying = false
                    isRadioLoading = false
                }
                if (playbackState == PlaybackState.STATE_PLAYING) {
                    isRadioLoading = false
                }
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                super.onMediaMetadataChanged(mediaMetadata)
                currentSong = mediaMetadata.title.toString()
                getCurrentItem()
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                Toast.makeText(application, "Something went wrong", Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun loadMore() {
        page++
        val loadPageSize = page * pageSize
        val childrenFuture = player.getChildren(
            DISCOVER_ID, page, loadPageSize, null
        )

        childrenFuture.addListener(
            {
                val result = childrenFuture.get()!!
                val children = result.value!!

                discoverStations = children.map {
                    Station(
                        it.mediaId.replace(DISCOVER_ID, ""),
                        it.mediaMetadata.extras?.getString("ARTWORK")!!,
                        it.mediaMetadata.extras?.getString("NAME")!!,
                        it.mediaMetadata.extras?.getString("COUNTRY")!!,
                        it.mediaMetadata.extras?.getString("GENRE")!!,
                        it.mediaMetadata.extras?.getString("COUNTRY_CODE")!!,
                        it.mediaMetadata.extras?.getString("STREAMING_URL_RESOLVED")!!,
                        it.mediaMetadata.extras?.getString("STATE")!!
                    )
                }.toMutableList()
            }, ContextCompat.getMainExecutor(application)
        )
    }

    private fun initPlayer() {
        playerFuture = MediaBrowser.Builder(
            application,
            SessionToken(application, ComponentName(application, PlayerService::class.java))
        ).setListener(
            object : MediaBrowser.Listener {
                @OptIn(UnstableApi::class)
                override fun onChildrenChanged(
                    browser: MediaBrowser,
                    parentId: String,
                    itemCount: Int,
                    params: MediaLibraryService.LibraryParams?
                ) {
                    val discoverFuture = browser.getChildren(
                        DISCOVER_ID, page, pageSize, null
                    )
                    val favoritesFuture = browser.getChildren(
                        FAVORITES_ID, 1, 90000, null
                    )

                    val type = params?.extras?.getString("type")

                    if (type != FAVORITES_ID) {
                        discoverFuture.addListener(
                            {
                                val result = discoverFuture.get()!!
                                val children = result.value!!

                                getCountryCode()

                                discoverStations = children.map {
                                    Station(
                                        it.mediaId.replace(DISCOVER_ID, ""),
                                        it.mediaMetadata.extras?.getString("ARTWORK")!!,
                                        it.mediaMetadata.extras?.getString("NAME")!!,
                                        it.mediaMetadata.extras?.getString("COUNTRY")!!,
                                        it.mediaMetadata.extras?.getString("GENRE")!!,
                                        it.mediaMetadata.extras?.getString("COUNTRY_CODE")!!,
                                        it.mediaMetadata.extras?.getString("STREAMING_URL_RESOLVED")!!,
                                        it.mediaMetadata.extras?.getString("STATE")!!
                                    )
                                }.toMutableList()
                            }, ContextCompat.getMainExecutor(application)
                        )
                    }

                    favoritesFuture.addListener(
                        {
                            val result = favoritesFuture.get()!!
                            val children = result.value!!

                            favoritesStations = children.map {
                                Station(
                                    it.mediaId.replace(FAVORITES_ID, ""),
                                    it.mediaMetadata.extras?.getString("ARTWORK")!!,
                                    it.mediaMetadata.extras?.getString("NAME")!!,
                                    it.mediaMetadata.extras?.getString("COUNTRY")!!,
                                    it.mediaMetadata.extras?.getString("GENRE")!!,
                                    it.mediaMetadata.extras?.getString("COUNTRY_CODE")!!,
                                    it.mediaMetadata.extras?.getString("STREAMING_URL_RESOLVED")!!,
                                    it.mediaMetadata.extras?.getString("STATE")!!
                                )
                            }
                        }, ContextCompat.getMainExecutor(application)
                    )
                }

            }
        ).buildAsync()

        playerFuture.addListener({
            player = playerFuture.get()
            player.subscribe(DISCOVER_ID, null)
            player.subscribe(FAVORITES_ID, null)
            listenToPlayer()
            getCurrentItem()
            if (open) {
                viewModelScope.launch {
                    var item = dbRepository.getRadioStationByID(openID)
                    if (item != null) {
                        playStation(item, DISCOVER_ID, true)
                        open = false
                    }
                }
            }
        }, ContextCompat.getMainExecutor(application))
    }

    fun createShortcut(station: Station) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val shortcutManager =
            ContextCompat.getSystemService(application, ShortcutManager::class.java)

        if (shortcutManager!!.isRequestPinShortcutSupported) {
            val shortcutIntent = Intent(application, MainActivity::class.java)
            shortcutIntent.action = Intent.ACTION_VIEW
            shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            shortcutIntent.data = "srivyaradio://shortcut/${station.id}".toUri()

            loadBitmapFromUrl(station.favicon) { bitmap ->
                val icon = if (bitmap != null) {
                    Icon.createWithBitmap(bitmap)
                } else {
                    Icon.createWithResource(application, R.mipmap.ic_launcher)
                }

                val shortcut = ShortcutInfo.Builder(application, station.id)
                    .setShortLabel(station.name)
                    .setLongLabel("Shortcut for ${station.name}")
                    .setIcon(icon)
                    .setIntent(shortcutIntent)
                    .build()

                val pinnedShortcutCallbackIntent =
                    shortcutManager.createShortcutResultIntent(shortcut)

                val flag = if (Build.VERSION.SDK_INT >= 31 && Build.VERSION.SDK_INT < 34) {
                    PendingIntent.FLAG_MUTABLE
                } else if (Build.VERSION.SDK_INT >= 34) {
                    PendingIntent.FLAG_IMMUTABLE
                } else {
                    0
                }

                val successCallback = PendingIntent.getBroadcast(
                    application, /* request code */ 0,
                    pinnedShortcutCallbackIntent, /* flags */ flag
                )

                shortcutManager.requestPinShortcut(
                    shortcut,
                    successCallback.intentSender
                )
            }
        }
    }

    fun sleepTimer(time: Int) {
        player.sendCustomCommand(
            SessionCommand(
                Constants.SET_TIMER_COMMAND, Bundle.EMPTY
            ), Bundle().apply {
                putLong(Constants.SET_TIMER_KEY, (time * 60000).toLong())
            }
        )

        if (time == 0) {
            Toast.makeText(
                application,
                "Queue cleared",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                application,
                "Playback will stop in $time minutes",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun getTheme() {
        appTheme = when (repository.getThemeMode()) {
            ThemeMode.DARK.id -> ThemeMode.DARK
            ThemeMode.LIGHT.id -> ThemeMode.LIGHT
            else -> ThemeMode.AUTO
        }
    }

    fun setTheme(mode: ThemeMode) {
        appTheme = mode
        repository.setThemeMode(mode.id)
    }

    private fun hasSaved() {
        viewModelScope.launch {
            try {
                hasSaved = dbRepository.getFavoriteStations().isNotEmpty()
            } catch (_: Exception) {
                hasSaved = false
            }
        }
    }

    init {
        Purchases.sharedInstance.updatedCustomerInfoListener = purchaseListener
        Purchases.sharedInstance.syncPurchases()
        hasSaved()
        getCountryCode()
        getTheme()
        initPlayer()
        loadRecents()
    }
}