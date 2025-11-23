package com.app.srivyaradio.wear

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    LaunchedEffect(Unit) {
        // Fetch US stations by default for now
        withContext(Dispatchers.IO) {
            stations = WearRepository.getStations("US").take(20) // Limit to 20 for performance
        }
    }

    // Connect to player
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        val sessionToken = SessionToken(context, ComponentName(context, WearPlayerService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        future.addListener({
            try {
                val p = future.get()
                player = p
                isPlaying = p.isPlaying
                currentStationName = p.currentMediaItem?.mediaMetadata?.title?.toString() ?: ""
                
                p.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        isPlaying = playing
                    }
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        currentStationName = mediaItem?.mediaMetadata?.title?.toString() ?: ""
                    }
                })
            } catch (e: Exception) {
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
            
            if (currentStationName.isNotEmpty()) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Now Playing:", style = MaterialTheme.typography.caption2)
                        Text(text = currentStationName, style = MaterialTheme.typography.body2)
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                if (isPlaying) player?.pause() else player?.play()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Text(text = if (isPlaying) "||" else ">")
                        }
                    }
                }
            }

            items(stations) { station ->
                Chip(
                    onClick = {
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
