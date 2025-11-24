package com.app.srivyaradio.wear

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.app.srivyaradio.wear.data.WearRepository
import com.app.srivyaradio.wear.data.models.Station
import com.app.srivyaradio.wear.media.WearPlayerService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WearMainActivity : ComponentActivity() {
    private var controllerFuture: ListenableFuture<MediaController>? = null

    companion object {
        private const val TAG = "WearMainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "WearMainActivity created")
        setContent {
            WearApp()
        }
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, WearPlayerService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
    }

    override fun onStop() {
        super.onStop()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}

@Composable
fun WearApp() {
    var stations by remember { mutableStateOf<List<Station>>(emptyList()) }
    var player by remember { mutableStateOf<Player?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentStationName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var retryTrigger by remember { mutableStateOf(0) }

    // Fetch stations
    LaunchedEffect(retryTrigger) {
        isLoading = true
        errorMessage = null
        Log.d("WearApp", "Starting to fetch stations...")
        
        withContext(Dispatchers.IO) {
            try {
                val fetchedStations = WearRepository.getStations("US")
                Log.d("WearApp", "Fetched ${fetchedStations.size} stations")
                
                if (fetchedStations.isEmpty()) {
                    errorMessage = "No stations found"
                    Log.w("WearApp", "Station list is empty")
                } else {
                    stations = fetchedStations.take(20) // Limit to 20 for performance
                    Log.d("WearApp", "Displaying ${stations.size} stations")
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load: ${e.message}"
                Log.e("WearApp", "Error loading stations", e)
            } finally {
                isLoading = false
            }
        }
    }

    // Connect to player
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        Log.d("WearApp", "Connecting to MediaController...")
        val sessionToken = SessionToken(context, ComponentName(context, WearPlayerService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        future.addListener({
            try {
                val p = future.get()
                player = p
                isPlaying = p.isPlaying
                currentStationName = p.currentMediaItem?.mediaMetadata?.title?.toString() ?: ""
                Log.d("WearApp", "MediaController connected successfully")
                
                p.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        isPlaying = playing
                        Log.d("WearApp", "Playback state changed: $playing")
                    }
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        currentStationName = mediaItem?.mediaMetadata?.title?.toString() ?: ""
                        Log.d("WearApp", "Media item changed: $currentStationName")
                    }
                })
            } catch (e: Exception) {
                Log.e("WearApp", "Error connecting to MediaController", e)
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    MaterialTheme {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Srivya Radio",
                    style = MaterialTheme.typography.title2,
                    color = MaterialTheme.colors.primary
                )
            }
            
            // Show loading indicator
            if (isLoading) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Loading stations...",
                            style = MaterialTheme.typography.caption2
                        )
                    }
                }
            }
            
            // Show error message with retry button
            if (!isLoading && errorMessage != null) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.caption2,
                            color = MaterialTheme.colors.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { retryTrigger++ },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Text(text = "↻")
                        }
                    }
                }
            }
            
            // Show current playback controls
            if (currentStationName.isNotEmpty()) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Now Playing:", style = MaterialTheme.typography.caption2)
                        Text(text = currentStationName, style = MaterialTheme.typography.body2)
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                if (isPlaying) {
                                    player?.pause()
                                    Log.d("WearApp", "Pausing playback")
                                } else {
                                    player?.play()
                                    Log.d("WearApp", "Starting playback")
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Text(text = if (isPlaying) "||" else ">")
                        }
                    }
                }
            }

            // Show stations list
            if (!isLoading && errorMessage == null) {
                items(stations) { station ->
                    Chip(
                        onClick = {
                            Log.d("WearApp", "Station clicked: ${station.name}")
                            player?.let { p ->
                                val mediaItem = MediaItem.Builder()
                                    .setUri(station.url_resolved)
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setTitle(station.name)
                                            .setArtist(station.country)
                                            .build()
                                    )
                                    .build()
                                p.setMediaItem(mediaItem)
                                p.prepare()
                                p.play()
                                Log.d("WearApp", "Playing station: ${station.name} from ${station.url_resolved}")
                            }
                        },
                        label = { Text(text = station.name) },
                        secondaryLabel = { Text(text = station.tags) },
                        colors = ChipDefaults.secondaryChipColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
