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
import com.app.srivyaradio.media.QueueManager
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

    var isRadioPlaying by mutableStateOf(false)
    var isRadioLoading by mutableStateOf(false)

    var currentSong by mutableStateOf("")

    var isPremium by mutableStateOf(
        false
    )

    private lateinit var playerFuture: ListenableFuture<MediaBrowser>
    lateinit var player: MediaBrowser
    lateinit var queueManager: QueueManager

    var page = 1
    var pageSize = 20

    var open = false
    var openID = ""

    var totalClickCount: Int = 0
    var totalImpressionCount: Int = 0
    var totalPlayCount: Int = 0
    var lastAdShownTime: Long = 0

    private fun mediaItemToStation(mediaItem: androidx.media3.common.MediaItem, prefix: String): Station? {
        return try {
            val extras = mediaItem.mediaMetadata.extras
            if (extras == null) {
                Log.w("MainViewModel", "MediaItem has no extras: ${mediaItem.mediaId}")
                return null
            }

            Station(
                mediaItem.mediaId.replace(prefix, ""),
                extras.getString("ARTWORK") ?: "",
                extras.getString("NAME") ?: "Unknown Station",
                extras.getString("COUNTRY") ?: "",
                extras.getString("GENRE") ?: "",
                extras.getString("COUNTRY_CODE") ?: "",
                extras.getString("STREAMING_URL_RESOLVED") ?: "",
                extras.getString("STATE") ?: ""
            )
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error converting MediaItem to Station: ${e.message}")
            null
        }
    }

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
                })
        }
    }

    private fun getCountryCode() {
        selectedCountryCode = repository.getCountryCode() ?: "US"
    }

    fun setTheme(theme: ThemeMode) {
        appTheme = theme
        repository.setThemeMode(theme.id)
    }

    fun getTheme() {
        val appId = repository.getThemeMode()

        appTheme = when (appId) {
            0 -> ThemeMode.AUTO
            1 -> ThemeMode.LIGHT
            2 -> ThemeMode.DARK
            else -> ThemeMode.AUTO
        }
    }

    fun search(query: String) {
        if (query.isBlank()) searchStations = listOf()
        else {
            viewModelScope.launch {
                searchStations = try {
                    dbRepository.getRadioStationByNameAndCountry(
                        query.lowercase(),
                        selectedCountryCode
                    ).reversed()
                } catch (e: Exception) {
                    listOf()
                }
            }
        }
    }

    fun moveFav(from: Int, to: Int) {
        try {
            favoritesStations = favoritesStations.toMutableList().apply {
                add(to, removeAt(from))
            }
        } catch (e: Exception) {

        }
    }

    suspend fun reorderStations() {
        val favList = favoritesStations.mapNotNull {
            dbRepository.getFavoriteItemById(it.id)
        }

        for (i in favList.indices) {
            dbRepository.updateFavoriteItem(Favorite(favList[i].favId, favList[i].id, i.toLong()))
        }

        player.sendCustomCommand(
            SessionCommand(Constants.UPDATE_FAVORITE_COMMAND, Bundle.EMPTY),
            Bundle.EMPTY
        )
    }

    private fun getCurrentItem() {
        val it = player.currentMediaItem
        if (it != null) {
            viewModelScope.launch {
                try {
                    val item = dbRepository.getRadioStationByID(
                        it.mediaId.replace(DISCOVER_ID, "").replace(FAVORITES_ID, "")
                    )
                    if (item != null) {
                        selectedStation = item
                    }
                    if (player.isPlaying) {
                        isRadioPlaying = true
                    } else {
                        isRadioPlaying = false
                    }
                } catch (e: Exception) {
                    Log.e("error", e.message.toString())
                }
            }
        }
    }
    
    private fun syncQueueFromPlayer() {
        if (!::queueManager.isInitialized || !::player.isInitialized) return
        
        try {
            val currentMediaItem = player.currentMediaItem ?: return
            val mediaId = currentMediaItem.mediaId
            
            viewModelScope.launch {
                try {
                    // Wait for selectedStation to be set by getCurrentItem()
                    kotlinx.coroutines.delay(100)
                    
                    val station = selectedStation
                    if (station == null) {
                        Log.w("MainViewModel", "syncQueueFromPlayer: selectedStation is null")
                        return@launch
                    }
                    
                    // Only sync if queue is empty or doesn't contain the current station
                    val shouldSync = queueManager.currentQueue.isEmpty() || 
                        queueManager.currentQueue.none { it.id == station.id }
                    
                    if (!shouldSync) {
                        Log.d("MainViewModel", "Queue already contains station, skipping sync")
                        return@launch
                    }
                    
                    // Determine the tag and populate the queue
                    when {
                        mediaId.startsWith(DISCOVER_ID) -> {
                            if (discoverStations.isNotEmpty()) {
                                Log.d("MainViewModel", "Syncing discover queue: ${discoverStations.size} stations")
                                queueManager.setQueue(discoverStations, station, DISCOVER_ID)
                            } else {
                                Log.w("MainViewModel", "Discover stations list is empty")
                            }
                        }
                        mediaId.startsWith(FAVORITES_ID) -> {
                            if (favoritesStations.isNotEmpty()) {
                                Log.d("MainViewModel", "Syncing favorites queue: ${favoritesStations.size} stations")
                                queueManager.setQueue(favoritesStations, station, FAVORITES_ID)
                            } else {
                                Log.w("MainViewModel", "Favorites stations list is empty")
                            }
                        }
                        else -> {
                            Log.w("MainViewModel", "Unknown media ID prefix: $mediaId")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Error syncing queue: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error in syncQueueFromPlayer: ${e.message}", e)
        }
    }

    fun setStation(station: Station, tag: String) {
        player.setMediaItem(
            MediaItemFactory.stationToMediaItem(station, tag)
        )
        selectedStation = station
        repository.setLastPlayID(station.id)
    }

    suspend fun getFavoriteItem(id: String): Favorite? {
        return dbRepository.getFavoriteItemById(id)
    }

    suspend fun addOrRemoveFromFavorites(id: String) {
        val favoriteItem = getFavoriteItem(id)
        if (favoriteItem != null) {
            dbRepository.deleteFavoriteItem(favoriteItem)
            Toast.makeText(application, "Removed from favorites", Toast.LENGTH_SHORT).show()
        } else {
            dbRepository.insertFavoriteItem(
                Favorite(null, id, null)
            )
            val favList = dbRepository.getFavoriteStations().keys.toList().mapNotNull {
                dbRepository.getFavoriteItemById(it.id)
            }

            for (i in favList.indices) {
                dbRepository.updateFavoriteItem(
                    Favorite(
                        favList[i].favId,
                        favList[i].id,
                        i.toLong()
                    )
                )
            }
            Toast.makeText(application, "Added to favorites", Toast.LENGTH_SHORT).show()
        }
        player.sendCustomCommand(
            SessionCommand(Constants.UPDATE_FAVORITE_COMMAND, Bundle.EMPTY),
            Bundle.EMPTY
        )
    }

    fun playStation(station: Station, tag: String, fromShortcut: Boolean = false) {
        isRadioLoading = true
        if (player.currentMediaItem?.mediaId?.contains(station.id) == true && isRadioPlaying && !fromShortcut) {
            player.pause()
            isRadioLoading = false
        } else {
            setStation(station, tag)
            player.prepare()
            player.play()
            
            // Populate the queue based on the tag
            if (::queueManager.isInitialized) {
                val stationList = when (tag) {
                    DISCOVER_ID -> discoverStations
                    FAVORITES_ID -> favoritesStations
                    else -> listOf(station)
                }
                queueManager.setQueue(stationList, station, tag)
            }
        }
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
            
            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                getCurrentItem()
                syncQueueFromPlayer()
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                super.onMediaMetadataChanged(mediaMetadata)
                currentSong = mediaMetadata.title.toString()
                getCurrentItem()
                syncQueueFromPlayer()
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
                try {
                    val result = childrenFuture.get()
                    val children = result?.value

                    if (children != null) {
                        discoverStations = children.mapNotNull {
                            mediaItemToStation(it, DISCOVER_ID)
                        }.toMutableList()
                    } else {
                        Log.w("MainViewModel", "loadMore: No children returned")
                    }
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Error loading more stations: ${e.message}")
                }
            }, ContextCompat.getMainExecutor(application)
        )
    }

    @OptIn(UnstableApi::class)
    private fun initPlayer() {
        playerFuture = MediaBrowser.Builder(
            application,
            SessionToken(application, ComponentName(application, PlayerService::class.java))
        ).setListener(
            object : MediaBrowser.Listener {
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
                                try {
                                    val result = discoverFuture.get()
                                    val children = result?.value

                                    getCountryCode()

                                    if (children != null) {
                                        discoverStations = children.mapNotNull {
                                            mediaItemToStation(it, DISCOVER_ID)
                                        }.toMutableList()
                                    } else {
                                        Log.w("MainViewModel", "onChildrenChanged (Discover): No children returned")
                                    }
                                } catch (e: Exception) {
                                    Log.e("MainViewModel", "Error in onChildrenChanged (Discover): ${e.message}")
                                }
                            }, ContextCompat.getMainExecutor(application)
                        )
                    }

                    favoritesFuture.addListener(
                        {
                            try {
                                val result = favoritesFuture.get()
                                val children = result?.value

                                if (children != null) {
                                    favoritesStations = children.mapNotNull {
                                        mediaItemToStation(it, FAVORITES_ID)
                                    }
                                } else {
                                    Log.w("MainViewModel", "onChildrenChanged (Favorites): No children returned")
                                }
                            } catch (e: Exception) {
                                Log.e("MainViewModel", "Error in onChildrenChanged (Favorites): ${e.message}")
                            }
                        }, ContextCompat.getMainExecutor(application)
                    )
                }

            }
        ).buildAsync()

        playerFuture.addListener({
            player = playerFuture.get()
            queueManager = QueueManager(this)
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

        if (shortcutManager?.isRequestPinShortcutSupported == true) {
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

    fun addStation(name: String, url: String) {
        val id = UUID.randomUUID().toString()
        viewModelScope.launch {
            try {
                dbRepository.insertStations(
                    listOf(
                        Station(
                            id,
                            "",
                            name,
                            "Custom entry",
                            "",
                            "",
                            url,
                            ""
                        )
                    )
                )
                dbRepository.insertFavoriteItem(Favorite(null, id, null))


                val favList = dbRepository.getFavoriteStations().keys.toList().mapNotNull {
                    dbRepository.getFavoriteItemById(it.id)
                }

                for (i in favList.indices) {
                    dbRepository.updateFavoriteItem(
                        Favorite(
                            favList[i].favId,
                            favList[i].id,
                            i.toLong()
                        )
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(application, "Something went wrong", Toast.LENGTH_SHORT).show()
            }

            player.sendCustomCommand(
                SessionCommand(
                    Constants.UPDATE_FAVORITE_COMMAND,
                    Bundle.EMPTY
                ), Bundle.EMPTY
            )
        }
    }

    fun openRadioFromLink(id: String) {
        open = true
        openID = id
    }

    fun resetPlayer() {
        player.seekToDefaultPosition()
        Toast.makeText(application, "Resetting player", Toast.LENGTH_SHORT).show()
    }

    fun hasSaved() {
        viewModelScope.launch {
            hasSaved = dbRepository.getFavoriteStations().isNotEmpty()
        }
    }

    val purchaseListener = UpdatedCustomerInfoListener { customerInfo ->

        if (customerInfo.entitlements["Premium"]?.isActive == true) {
            repository.setHasPurchased(true)
            isPremium = true
        } else {
            repository.setHasPurchased(false)
            isPremium = false
        }
    }

    fun onPurchase() {
        isPremium = true
    }

    fun getIsPremium() {
        isPremium = repository.getHasPurchased()
    }

    fun sleepTimer(time: Int) {
        player.sendCustomCommand(
            SessionCommand(
                Constants.SET_TIMER_COMMAND, Bundle.EMPTY

            ), Bundle().apply {
                putLong(Constants.SET_TIMER_KEY, (time * 60000).toLong())
            })

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

    init {
        Purchases.sharedInstance.updatedCustomerInfoListener = purchaseListener
        Purchases.sharedInstance.syncPurchases()
        hasSaved()
        getCountryCode()
        getTheme()
        initPlayer()
    }
}