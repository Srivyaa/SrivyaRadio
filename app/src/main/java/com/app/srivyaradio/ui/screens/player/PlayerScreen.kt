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
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
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
                        (it.tags.split(",").take(4).joinToString(separator = ", ")).replaceFirstChar { char -> 
                            if (char.isLowerCase()) char.titlecase() else char.toString() 
                        }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                }
                Spacer(Modifier.padding(15.dp))
                
                // Playback controls row
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            try {
                                mainViewModel.queueManager.playPrevious()
                            } catch (e: UninitializedPropertyAccessException) {
                                // Queue not initialized yet
                            }
                        },
                        enabled = try { mainViewModel.queueManager.hasPrevious() } catch (e: Exception) { false },
                        modifier = Modifier.size(60.dp)
                    ) {
                        Icon(
                            Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            modifier = Modifier.size(40.dp)
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
                    
                    IconButton(
                        onClick = {
                            try {
                                mainViewModel.queueManager.playNext()
                            } catch (e: UninitializedPropertyAccessException) {
                                // Queue not initialized yet
                            }
                        },
                        enabled = try { mainViewModel.queueManager.hasNext() } catch (e: Exception) { false },
                        modifier = Modifier.size(60.dp)
                    ) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = "Next",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                Spacer(Modifier.padding(10.dp))
                
                // Additional controls row
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                mainViewModel.addOrRemoveFromFavorites(it.id)
                                isFavorite = !isFavorite
                            }
                        }, modifier = Modifier
                            .size(80.dp)
                            .padding(5.dp)
                    ) {
                        if (isFavorite) Icon(
                            Icons.Outlined.Favorite,
                            contentDescription = "Remove from favorites",
                            modifier = Modifier.size(30.dp)
                        ) else Icon(
                            Icons.Outlined.FavoriteBorder,
                            contentDescription = "Add to favorites",
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            mainViewModel.resetPlayer()
                        }, modifier = Modifier
                            .size(80.dp)
                            .padding(5.dp),
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset player", modifier = Modifier.size(30.dp))
                    }
                }
                Spacer(Modifier.padding(25.dp))
            }
        }
    }
}