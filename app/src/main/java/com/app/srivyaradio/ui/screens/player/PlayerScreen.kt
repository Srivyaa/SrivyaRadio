package com.app.srivyaradio.ui.screens.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.app.srivyaradio.R
import com.app.srivyaradio.ui.MainViewModel
import com.app.srivyaradio.ui.components.RadioLogoSmall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    mainViewModel: MainViewModel, onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        true
    )

    var isFavorite by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        mainViewModel.refreshPlaybackControlsState()
        mainViewModel.refreshQueue()
    }

    LaunchedEffect(mainViewModel.selectedStation) {

        val result = withContext(Dispatchers.IO) {
            val favoriteItem =
                mainViewModel.selectedStation?.let { mainViewModel.getFavoriteItem(it.id) }
            favoriteItem != null
        }

        isFavorite = result
    }
    ModalBottomSheet(onDismissRequest = { onDismiss() }, sheetState = sheetState) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {

            mainViewModel.selectedStation?.let {
                Spacer(Modifier.padding(10.dp))
                RadioLogoSmall(imageUrl = it.favicon, size = 200)
                Spacer(Modifier.padding(10.dp))
                Text(
                    text = it.name,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(10.dp)
                )
                if (mainViewModel.currentSong.isNotBlank() && mainViewModel.currentSong != "null") {
                    Text(
                        text = mainViewModel.currentSong,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                } else {
                    val label =
                        (it.tags.split(",").take(4).joinToString(separator = ", ")).capitalize()
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                }
                Spacer(Modifier.padding(15.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Previous
                    IconButton(
                        onClick = { mainViewModel.skipToPrevious() },
                        modifier = Modifier
                            .size(100.dp)
                            .padding(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = null,
                            modifier = Modifier.size(35.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            scope.launch {
                                mainViewModel.addOrRemoveFromFavorites(it.id)
                                isFavorite = !isFavorite
                            }
                        }, modifier = Modifier
                            .size(100.dp)
                            .padding(5.dp)
                    ) {
                        Icon(
                            painterResource(id = if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outlined),
                            contentDescription = null,
                            modifier = Modifier.size(35.dp)
                        )
                    }
                    FilledTonalIconButton(
                        onClick = {
                            mainViewModel.playOrPause()
                        }, modifier = Modifier.size(90.dp)
                    ) {
                        if (mainViewModel.isRadioLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(35.dp)
                            )
                        } else {
                            Icon(
                                painterResource(id = if (mainViewModel.isRadioPlaying) R.drawable.ic_pause else R.drawable.ic_play_circle),
                                null,
                                modifier = Modifier.size(35.dp)
                            )
                        }
                    }

                    // Next
                    IconButton(
                        onClick = { mainViewModel.skipToNext() },
                        modifier = Modifier
                            .size(100.dp)
                            .padding(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = null,
                            modifier = Modifier.size(35.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            mainViewModel.resetPlayer()

                        },
                        modifier = Modifier
                            .size(100.dp)
                            .padding(5.dp),
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(35.dp))
                    }
                }
                Spacer(Modifier.padding(6.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Shuffle toggle (match AA/notification icons)
                    IconButton(onClick = { mainViewModel.toggleShuffle() }) {
                        Icon(
                            imageVector = Icons.Filled.Shuffle,
                            contentDescription = if (mainViewModel.shuffleEnabled) "Shuffle On" else "Shuffle Off",
                            modifier = Modifier.size(26.dp),
                            tint = if (mainViewModel.shuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Seek back 10s
                    IconButton(
                        onClick = { mainViewModel.seekBack() },
                        enabled = mainViewModel.isSeekable
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Replay10,
                            contentDescription = "Seek back",
                            modifier = Modifier.size(26.dp),
                        )
                    }

                    // Seek forward 10s
                    IconButton(
                        onClick = { mainViewModel.seekForward() },
                        enabled = mainViewModel.isSeekable
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Forward10,
                            contentDescription = "Seek forward",
                            modifier = Modifier.size(26.dp),
                        )
                    }

                    // Repeat mode cycle
                    IconButton(onClick = { mainViewModel.cycleRepeatMode() }) {
                        val icon = when (mainViewModel.repeatMode) {
                            Player.REPEAT_MODE_ONE -> Icons.Filled.RepeatOne
                            Player.REPEAT_MODE_ALL -> Icons.Filled.Repeat
                            else -> Icons.Filled.Repeat
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Repeat",
                            modifier = Modifier.size(26.dp),
                        )
                    }

                    // Sleep timer menu
                    var timerMenuExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { timerMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = "Sleep timer",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = timerMenuExpanded,
                        onDismissRequest = { timerMenuExpanded = false }
                    ) {
                        val options = listOf(
                            0 to "Off",
                            15 to "15 min",
                            30 to "30 min",
                            60 to "60 min",
                            90 to "90 min"
                        )
                        options.forEach { (minutes, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    mainViewModel.sleepTimer(minutes)
                                    timerMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.padding(25.dp))

                // Up Next / Queue (similar to Android Auto): tap to play, remove to delete from queue
                val currentIdx = mainViewModel.queueStations.indexOfFirst { qs ->
                    qs.id == mainViewModel.selectedStation?.id
                }
                val startIndex = if (currentIdx >= 0) currentIdx + 1 else 0
                val upcoming = mainViewModel.queueStations.drop(startIndex).take(15)
                if (upcoming.isNotEmpty()) {
                    Text(
                        text = "Up Next",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        upcoming.forEachIndexed { idx, station ->
                            val queueIndex = startIndex + idx
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { mainViewModel.playFromQueue(queueIndex) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                RadioLogoSmall(imageUrl = station.favicon, size = 36)
                                Text(
                                    text = station.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 12.dp)
                                )
                                IconButton(onClick = { mainViewModel.removeFromQueue(queueIndex) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove from queue")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
