package com.app.srivyaradio.ui.screens.recents

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.app.srivyaradio.data.models.Station
import com.app.srivyaradio.ui.MainViewModel
import com.app.srivyaradio.ui.components.OptionsBottomSheet
import com.app.srivyaradio.ui.components.Station
import com.app.srivyaradio.utils.Constants
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentsScreen(mainViewModel: MainViewModel) {
    val recents = mainViewModel.recentStations
    val scope = rememberCoroutineScope()
    var optionsStation by remember { mutableStateOf<Station?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    LaunchedEffect(mainViewModel.recentStations) { if (refreshing) refreshing = false }

    PullToRefreshBox(
        state = rememberPullToRefreshState(),
        isRefreshing = refreshing,
        onRefresh = {
            refreshing = true
            mainViewModel.loadRecents()
        }
    ) {
        if (recents.isEmpty()) {
            androidx.compose.foundation.layout.Box(
                contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    "No recent stations",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = TextUnit(22f, TextUnitType.Sp)
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(recents) { station ->
                    Station(
                        name = station.name,
                        image = station.favicon,
                        label = station.country,
                        isFavorite = mainViewModel.favoritesStations.any { it.id == station.id },
                        onToggleFavorite = { scope.launch { mainViewModel.addOrRemoveFromFavorites(station.id) } },
                        onClick = { mainViewModel.playStation(station, Constants.DISCOVER_ID) },
                        onOptions = { optionsStation = station },
                        modifier = Modifier
                    )
                }
                item { Spacer(modifier = Modifier.padding(bottom = 80.dp)) }
            }
        }
    }

    optionsStation?.let {
        OptionsBottomSheet(
            station = it,
            onDismiss = { optionsStation = null },
            mainViewModel = mainViewModel,
            onSleepTimer = { optionsStation = null }
        )
    }
}

