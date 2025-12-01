package com.app.srivyaradio.ui.screens.offline

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.app.srivyaradio.data.models.DownloadedItem
import com.app.srivyaradio.ui.MainViewModel
import com.app.srivyaradio.ui.components.RadioLogoSmall
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineScreen(mainViewModel: MainViewModel, navController: NavController) {
    val context = LocalContext.current
    val permission = remember {
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val queryState = remember { mutableStateOf("") }
    var refreshing by remember { mutableStateOf(false) }
    
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) mainViewModel.loadDownloads()
    }
    
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (granted) mainViewModel.loadDownloads() else launcher.launch(permission)
    }
    
    LaunchedEffect(mainViewModel.downloadedItems) { 
        if (refreshing) refreshing = false 
    }

    val filtered = remember(mainViewModel.downloadedItems, queryState.value) {
        val q = queryState.value.trim()
        if (q.isEmpty()) mainViewModel.downloadedItems
        else mainViewModel.downloadedItems.filter { it.name.contains(q, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Offline") },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null)
                }
            }
        )

        PullToRefreshBox(
        state = rememberPullToRefreshState(),
        isRefreshing = refreshing,
        onRefresh = {
            refreshing = true
            val granted = ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (granted) mainViewModel.loadDownloads() else launcher.launch(permission)
        }
    ) {
        if (mainViewModel.downloadedItems.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center, 
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    "No downloads yet",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = TextUnit(22f, TextUnitType.Sp)
                )
            }
        } else {
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
                            if (granted) mainViewModel.scanDeviceForAudio() else launcher.launch(permission)
                        }) {
                            Icon(Icons.Filled.Search, contentDescription = "Scan Device")
                        }
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
                            text = "No results found",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(filtered) { item ->
                        OfflineItem(
                            item = item,
                            onClick = { mainViewModel.playDownloaded(item) },
                            onDelete = { mainViewModel.deleteDownloaded(item) }
                        )
                    }
                    item { 
                        Spacer(modifier = Modifier.padding(bottom = 80.dp)) 
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun OfflineItem(
    item: DownloadedItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.padding(15.dp)) {
            RadioLogoSmall(imageUrl = item.image, size = 53)
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.name,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = TextUnit(18f, TextUnitType.Sp),
            )
            if (item.countrycode.isNotBlank()) {
                Text(
                    text = item.countrycode,
                    fontWeight = FontWeight.W400,
                    fontSize = TextUnit(14f, TextUnitType.Sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        IconButton(onClick = { onDelete() }) {
            Icon(
                Icons.Default.Delete,
                modifier = Modifier.size(24.dp),
                contentDescription = "Delete"
            )
        }
    }
}
