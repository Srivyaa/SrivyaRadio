package com.app.srivyaradio.ui.screens.player

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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
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
import com.app.srivyaradio.R
import com.app.srivyaradio.ui.MainViewModel
import com.app.srivyaradio.ui.components.RadioLogoSmall
import com.app.srivyaradio.ui.components.CastButton
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
                    // Cast button
                    CastButton(modifier = Modifier.size(48.dp))

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

                        }, modifier = Modifier
                            .size(100.dp)
                            .padding(5.dp),
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(35.dp))
                    }

                    // Shuffle toggle
                    IconButton(
                        onClick = { mainViewModel.toggleShuffle() },
                        modifier = Modifier
                            .size(100.dp)
                            .padding(5.dp)
                    ) {
                        Icon(Icons.Filled.Shuffle, null, modifier = Modifier.size(35.dp))
                    }

                    // Recording toggle
                    IconButton(
                        onClick = { mainViewModel.toggleRecording() },
                        modifier = Modifier
                            .size(100.dp)
                            .padding(5.dp)
                    ) {
                        if (mainViewModel.isRecording) {
                            Icon(Icons.Filled.Stop, null, modifier = Modifier.size(35.dp))
                        } else {
                            Icon(Icons.Filled.FiberManualRecord, null, modifier = Modifier.size(35.dp))
                        }
                    }
                }
                Spacer(Modifier.padding(25.dp))
            }
        }
    }
}