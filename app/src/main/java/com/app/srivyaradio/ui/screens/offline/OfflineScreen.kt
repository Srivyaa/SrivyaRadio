package com.app.srivyaradio.ui.screens.offline

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.app.srivyaradio.ui.MainViewModel

@Composable
fun OfflineScreen(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    val permission = remember {
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val queryState = remember { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) mainViewModel.loadDownloads()
    }
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (granted) mainViewModel.loadDownloads() else launcher.launch(permission)
    }

    val filtered = remember(mainViewModel.downloadedItems, queryState.value) {
        val q = queryState.value.trim()
        if (q.isEmpty()) mainViewModel.downloadedItems
        else mainViewModel.downloadedItems.filter { it.name.contains(q, ignoreCase = true) }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Offline",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    val granted = ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    if (granted) mainViewModel.loadDownloads() else launcher.launch(permission)
                }) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                }
            }
        }
        item {
            OutlinedTextField(
                value = queryState.value,
                onValueChange = { queryState.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                label = { Text("Search offline") }
            )
        }
        if (filtered.isEmpty()) {
            item {
                Text(
                    text = "No downloads yet",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(filtered) { di ->
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
