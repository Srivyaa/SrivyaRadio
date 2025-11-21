package com.app.srivyaradio.ui.screens.favorites

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.app.srivyaradio.data.models.Station
import com.app.srivyaradio.ui.MainViewModel
import com.app.srivyaradio.ui.components.OptionsBottomSheet
import com.app.srivyaradio.ui.components.SleepTimerSheet
import com.app.srivyaradio.ui.components.Station
import com.app.srivyaradio.ui.components.rememberDragDropListState
import com.app.srivyaradio.utils.Constants
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

@SuppressLint("UnnecessaryComposedModifier")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    mainViewModel: MainViewModel
) {
    var refreshing by remember { mutableStateOf(false) }
    LaunchedEffect(mainViewModel.favoritesStations) { if (refreshing) refreshing = false }
    var showSleepSheet by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var optionsStation by remember {
        mutableStateOf<Station?>(null)
    }

    val scope = rememberCoroutineScope()

    var overscrollJob by remember { mutableStateOf<Job?>(null) }

    val dragDropListState = rememberDragDropListState(onMove = { from, to ->
        mainViewModel.moveFav(from, to)
    }, onInterrupt = {
        scope.launch {
            mainViewModel.reorderStations()
        }
    })
    PullToRefreshBox(
        state = rememberPullToRefreshState(),
        isRefreshing = refreshing,
        onRefresh = {
            refreshing = true
            mainViewModel.refreshFavorites()
        }
    ) {
        if (mainViewModel.favoritesStations.isEmpty() && mainViewModel.favoriteFolderCodes.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    "No favorite stations yet",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = TextUnit(
                        22f, TextUnitType.Sp
                    )
                )
            }
        }else {
                val scope = rememberCoroutineScope()
                var expandedFolders by remember { mutableStateOf(setOf<String>()) }
                val folderStations = remember { mutableStateMapOf<String, List<Station>>() }

                val canReorder = mainViewModel.favoriteFolderCodes.isEmpty()
                val listModifier = if (canReorder) {
                    Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { offset ->
                                    dragDropListState.onDragStart(offset)
                                },
                                onDrag = { change, offset ->
                                    change.consume()
                                    dragDropListState.onDrag(offset)
                                    dragDropListState.checkForOverScroll()
                                        ?.let {
                                            overscrollJob =
                                                scope.launch {
                                                    dragDropListState.lazyListState.scrollBy(
                                                        it
                                                    )
                                                }
                                        } ?: run { overscrollJob?.cancel() }
                                },
                                onDragEnd = { dragDropListState.onDragInterrupted() },
                                onDragCancel = { dragDropListState.onDragInterrupted() }
                            )
                        }
                } else {
                    // Disable drag & drop when folders are present to avoid index mismatch
                    Modifier.fillMaxSize()
                }

                LazyColumn(
                    modifier = listModifier,
                    state = dragDropListState.lazyListState
                ) {
                    // Favorite country folders
                    itemsIndexed(mainViewModel.favoriteFolderCodes) { _, code ->
                        val isExpanded = expandedFolders.contains(code)
                        Row(
                            modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                expandedFolders =
                                    if (isExpanded) expandedFolders - code else expandedFolders + code
                                if (!isExpanded && folderStations[code] == null) {
                                    scope.launch {
                                        folderStations[code] =
                                            mainViewModel.getStationsForCountry(code)
                                    }
                                }
                            }) {
                                Icon(
                                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = null
                                )
                            }
                            IconButton(onClick = { mainViewModel.downloadAllForCountry(code) }) {
                                Icon(
                                    Icons.Outlined.FileDownload,
                                    contentDescription = null
                                )
                            }
                            IconButton(onClick = { mainViewModel.refreshCountry(code) }) {
                                Icon(
                                    Icons.Outlined.Refresh,
                                    contentDescription = null
                                )
                            }
                            Text(
                                modifier = Modifier.weight(1f),
                                text = mainViewModel.getCountryNameByCode(code)
                            )
                            val fav = mainViewModel.isCountryFavorited(code)
                            IconButton(onClick = { mainViewModel.toggleFavoriteCountry(code) }) {
                                if (fav) Icon(
                                    Icons.Filled.Favorite,
                                    contentDescription = null
                                ) else Icon(
                                    Icons.Outlined.FavoriteBorder,
                                    contentDescription = null
                                )
                            }
                        }
                        if (isExpanded) {
                            val stations = folderStations[code]
                            if (stations == null) {
                                // simple placeholders while loading
                                androidx.compose.foundation.layout.Column(
                                    modifier = Modifier.fillMaxSize()
                                        .padding(start = 16.dp, end = 16.dp)
                                ) {
                                    repeat(5) { Spacer(modifier = Modifier.padding(vertical = 6.dp)) }
                                }
                            } else {
                                androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
                                    stations.forEach { station ->
                                        Station(
                                            name = station.name,
                                            image = station.favicon,
                                            label = station.country,
                                            isOffline = mainViewModel.isStationOffline(station.id),
                                            isFavorite = mainViewModel.favoritesStations.any { it.id == station.id },
                                            onDownload = run {
                                                val u = station.url_resolved.lowercase()
                                                if (u.endsWith(".mp3") || u.endsWith(".aac") || u.endsWith(".m4a") || u.endsWith(".wav") || u.endsWith(".flac")) ({ mainViewModel.downloadStationMp3(station) }) else null
                                            },
                                            onToggleFavorite = {
                                                scope.launch {
                                                    mainViewModel.addOrRemoveFromFavorites(
                                                        station.id
                                                    )
                                                }
                                            },
                                            onClick = {
                                                mainViewModel.playStation(
                                                    station,
                                                    Constants.DISCOVER_ID
                                                )
                                            },
                                            onOptions = {
                                                showBottomSheet = true
                                                optionsStation = station
                                            },
                                            modifier = Modifier.composed {
                                                Modifier.graphicsLayer { }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    itemsIndexed(mainViewModel.favoritesStations) { index, station ->

                        Station(
                            name = station.name,
                            image = station.favicon,
                            label = station.country,
                            isOffline = mainViewModel.isStationOffline(station.id),
                            isFavorite = true,
                            onDownload = run {
                                val u = station.url_resolved.lowercase()
                                if (u.endsWith(".mp3") || u.endsWith(".aac") || u.endsWith(".m4a") || u.endsWith(".wav") || u.endsWith(".flac")) ({ mainViewModel.downloadStationMp3(station) }) else null
                            },
                            onToggleFavorite = {
                                scope.launch { mainViewModel.addOrRemoveFromFavorites(station.id) }
                            },
                            onClick = {
                                mainViewModel.playStation(station, Constants.FAVORITES_ID)
                            },
                            onOptions = {
                                showBottomSheet = true
                                optionsStation = station
                            },
                            modifier = Modifier.composed {
                                val offsetOrNull = dragDropListState.elementDisplacement.takeIf {
                                    index == dragDropListState.currentIndexOfDraggedItem
                                }

                                Modifier.graphicsLayer {
                                    translationY = offsetOrNull ?: 0f
                                }
                            }
                        )
                    }
                }
            }
        }
        if (showBottomSheet) {
            optionsStation?.let {
                OptionsBottomSheet(
                    onDismiss = {
                        showBottomSheet = false
                    },
                    station = it,
                    mainViewModel = mainViewModel,
                    onSleepTimer = {
                        showBottomSheet = false
                        showSleepSheet = true
                    }
                )
            }
        }

        if (showSleepSheet) {
            SleepTimerSheet(
                onDismiss = {
                    showSleepSheet = false
                }, onSelected = {
                    mainViewModel.sleepTimer(it)
                }
            )
        }
    }
