package com.app.srivyaradio.ui.screens.recents

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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

@Composable
fun RecentsScreen(mainViewModel: MainViewModel) {
    val recents = mainViewModel.recentStations
    val scope = rememberCoroutineScope()
    val (optionsStation, setOptionsStation) = remember { mutableStateOf<Station?>(null) }

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
                    onOptions = { setOptionsStation(station) },
                    modifier = Modifier
                )
            }
            item { Spacer(modifier = Modifier.padding(bottom = 80.dp)) }
        }
    }

    optionsStation?.let {
        OptionsBottomSheet(
            station = it,
            onDismiss = { setOptionsStation(null) },
            mainViewModel = mainViewModel,
            onSleepTimer = { setOptionsStation(null) }
        )
    }
}
