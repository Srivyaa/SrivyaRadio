package com.app.srivyaradio.ui.screens.offline

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.srivyaradio.ui.MainViewModel

@Composable
fun OfflineScreen(mainViewModel: MainViewModel) {
    LaunchedEffect(Unit) { mainViewModel.loadDownloads() }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (mainViewModel.downloadedItems.isEmpty()) {
            item {
                Text(
                    text = "No downloads yet",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(mainViewModel.downloadedItems) { di ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(text = di.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    IconButton(onClick = { mainViewModel.playDownloaded(di) }) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    }
                    IconButton(onClick = { mainViewModel.deleteDownloaded(di) }) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                    }
                }
            }
        }
    }
}
