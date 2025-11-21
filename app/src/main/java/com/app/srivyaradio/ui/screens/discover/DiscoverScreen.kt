package com.app.srivyaradio.ui.screens.discover

import android.Manifest.permission.POST_NOTIFICATIONS
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.app.srivyaradio.data.models.Station
import com.app.srivyaradio.ui.MainViewModel
import com.app.srivyaradio.ui.components.AppSearchBar
import com.app.srivyaradio.ui.components.LargeDropdownMenu
import com.app.srivyaradio.ui.components.OptionsBottomSheet
import com.app.srivyaradio.ui.components.ShimmerStation
import com.app.srivyaradio.ui.components.Station
import com.app.srivyaradio.utils.Constants.DISCOVER_ID
import kotlinx.coroutines.launch
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import com.app.srivyaradio.ui.components.SleepTimerSheet


@Composable
fun LazyListState.isScrollingDown(): Boolean {
    val offset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
    return remember(this) { derivedStateOf { (firstVisibleItemScrollOffset - offset) > 0 } }.value
}

@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(mainViewModel: MainViewModel) {
    val countries = mainViewModel.getCountryListForUI()
    var selectedIndex = countries.indexOfFirst { it.second == mainViewModel.selectedCountryCode }
        .let { if (it < 0) 0 else it }
    val keyboardController = LocalSoftwareKeyboardController.current
    var showBottomSheet by remember { mutableStateOf(false) }
    var showSleepSheet by remember { mutableStateOf(false) }
    var optionsStation by remember { mutableStateOf<Station?>(null) }
    val state = rememberLazyListState()
    val isAtBottom = !state.canScrollForward
    val scope = rememberCoroutineScope()
    var jumpTop by rememberSaveable { mutableStateOf(false) }

    val notificationsPermissionState = rememberPermissionState(POST_NOTIFICATIONS)
    LaunchedEffect(notificationsPermissionState) {
        if (!notificationsPermissionState.status.isGranted) {
            notificationsPermissionState.launchPermissionRequest()
        }
    }

    if (jumpTop) {
        LaunchedEffect(key1 = "scroll") {
            state.scrollToItem(0, 0)
            jumpTop = false
        }
    }

    var refreshing by remember { mutableStateOf(false) }
    LaunchedEffect(mainViewModel.discoverStations) { if (refreshing) refreshing = false }

    PullToRefreshBox(
        state = rememberPullToRefreshState(),
        isRefreshing = refreshing,
        onRefresh = {
            refreshing = true
            mainViewModel.refreshDiscover()
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppSearchBar(
                mainViewModel.searchStations,
                onSearch = { mainViewModel.search(it) },
                onClick = {
                    mainViewModel.playSearchResults(it)
                    keyboardController?.hide()
                },
                onOptions = {
                    showBottomSheet = true
                    optionsStation = it
                },
                isFavorite = { st -> mainViewModel.favoritesStations.any { it.id == st.id } },
                onToggleFavorite = { st -> scope.launch { mainViewModel.addOrRemoveFromFavorites(st.id) } },
                isOffline = { st -> mainViewModel.isStationOffline(st.id) }
            )

            Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
                LazyColumn(state = state) {
                    item("dr") {
                        LargeDropdownMenu(
                            label = "View stations from",
                            items = countries.map { it.first },
                            selectedIndex = selectedIndex,
                            onItemSelected = { index, _ ->
                                selectedIndex = index
                                mainViewModel.setCountryCodeByCode(countries[index].second)
                            },
                        )
                        // Toggle favorite country folder
                        val code = countries.getOrNull(selectedIndex)?.second ?: ""
                        val isFav = if (code.isNotBlank()) mainViewModel.isCountryFavorited(code) else false
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (code.isNotBlank()) mainViewModel.refreshCountry(code) }) {
                                Icon(Icons.Outlined.Refresh, contentDescription = null)
                            }
                            IconButton(onClick = { if (code.isNotBlank()) mainViewModel.toggleFavoriteCountry(code) }) {
                                if (isFav) Icon(Icons.Filled.Favorite, contentDescription = null) else Icon(
                                    Icons.Outlined.FavoriteBorder, contentDescription = null
                                )
                            }

                            Text(if (isFav) "Remove country from Favorites" else "Add country to Favorites")
                        }
                    }
                    if (mainViewModel.discoverStations.isEmpty()) {
                        items(15) { ShimmerStation() }
                    } else {
                        items(mainViewModel.discoverStations) { station ->
                            val label = (station.tags.split(",").take(4)
                                .joinToString(separator = ", ")).capitalize()
                            LaunchedEffect(isAtBottom) { if (isAtBottom) mainViewModel.loadMore() }
                            Station(
                                name = station.name,
                                image = station.favicon,
                                label = if (label.isNotBlank()) label else station.country,
                                isOffline = mainViewModel.isStationOffline(station.id),
                                isFavorite = mainViewModel.favoritesStations.any { it.id == station.id },
                                onToggleFavorite = {
                                    scope.launch {
                                        mainViewModel.addOrRemoveFromFavorites(
                                            station.id
                                        )
                                    }
                                },
                                onClick = { mainViewModel.playStation(station, DISCOVER_ID) },
                                onOptions = {
                                    showBottomSheet = true
                                    optionsStation = station
                                },
                                modifier = Modifier
                            )
                        }
                    }
                }

                AnimatedContent(
                    targetState = !state.isScrollingUp() && state.firstVisibleItemIndex > 15,
                    label = ""
                ) {
                    if (it) {
                        FloatingActionButton(
                            modifier = Modifier.padding(10.dp),
                            onClick = { jumpTop = true }) {
                            Icon(Icons.Default.ArrowUpward, null)
                        }
                    }
                }

                if (showBottomSheet) {
                    optionsStation?.let {
                        OptionsBottomSheet(
                            onDismiss = { showBottomSheet = false },
                            station = it,
                            mainViewModel = mainViewModel,
                            onSleepTimer = {
                                showBottomSheet = false
                                showSleepSheet = true
                            })
                    }
                }

                if (showSleepSheet) {
                    SleepTimerSheet(
                        onDismiss = { showSleepSheet = false },
                        onSelected = { mainViewModel.sleepTimer(it) })
                }
            }
        }
    }
}