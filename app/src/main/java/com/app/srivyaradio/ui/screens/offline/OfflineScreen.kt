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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.app.srivyaradio.data.models.DownloadedItem
import com.app.srivyaradio.data.models.Station
import com.app.srivyaradio.ui.MainViewModel
import com.app.srivyaradio.ui.components.AppSearchBar
import com.app.srivyaradio.ui.components.SleepTimerSheet
import com.app.srivyaradio.ui.components.Station

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineScreen(mainViewModel: MainViewModel, onBackClick: (() -> Unit)? = null) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val permission = remember {
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
    }
    
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) mainViewModel.loadDownloads()
    }
    
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (granted) mainViewModel.loadDownloads() else launcher.launch(permission)
    }

    val folderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
            mainViewModel.scanAudioInFolder(uri)
        }
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    var showSleepSheet by remember { mutableStateOf(false) }
    var optionsItem by remember { mutableStateOf<DownloadedItem?>(null) }
    
    var refreshing by remember { mutableStateOf(false) }
    // Simulate refresh delay
    LaunchedEffect(refreshing) {
        if (refreshing) {
            mainViewModel.loadDownloads()
            refreshing = false
        }
    }

    // Filter logic for search
    var searchQuery by remember { mutableStateOf("") }
    val filteredItems = remember(mainViewModel.downloadedItems, searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) mainViewModel.downloadedItems
        else mainViewModel.downloadedItems.filter { it.name.contains(q, ignoreCase = true) }
    }

    // Convert to Station for UI
    fun DownloadedItem.toStation(): Station {
        return Station(
            id = id?.toString() ?: sourceUrl,
            favicon = image,
            name = name,
            country = countrycode, // Artist
            tags = "",
            countrycode = "",
            url_resolved = fileUri,
            state = "",
            homepage = "",
            rank = 0
        )
    }
    
    val displayStations = filteredItems.map { it.toStation() }

    Scaffold(
        topBar = {
            if (onBackClick != null) {
                TopAppBar(
                    title = { Text("Offline Library") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            state = rememberPullToRefreshState(),
            isRefreshing = refreshing,
            onRefresh = { refreshing = true },
            modifier = Modifier.fillMaxSize()
        ) {
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            AppSearchBar(
                searchStations = displayStations, // Use filtered list for search results too
                onSearch = { searchQuery = it },
                onClick = { st ->
                    // Find item and play
                    val item = mainViewModel.downloadedItems.find { (it.id?.toString() ?: it.sourceUrl) == st.id }
                    if (item != null) {
                        mainViewModel.playOfflineItems(filteredItems, filteredItems.indexOf(item))
                        keyboardController?.hide()
                    }
                },
                onOptions = { st ->
                    val item = mainViewModel.downloadedItems.find { (it.id?.toString() ?: it.sourceUrl) == st.id }
                    if (item != null) {
                        optionsItem = item
                        showBottomSheet = true
                    }
                },
                isFavorite = { false },
                onToggleFavorite = { },
                isOffline = { true }
            )

            Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
                LazyColumn {
                    item {
                        // Header with actions
                         Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Offline Library",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { folderLauncher.launch(null) }) {
                                Icon(Icons.Filled.Folder, contentDescription = "Scan Folder")
                            }
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
                    
                    if (filteredItems.isEmpty()) {
                         item {
                            Text(
                                text = "No downloads yet",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(filteredItems) { item ->
                            Station(
                                name = item.name,
                                image = item.image,
                                label = item.countrycode,
                                isOffline = true,
                                isFavorite = false,
                                onToggleFavorite = { },
                                onDownload = null,
                                onClick = { mainViewModel.playOfflineItems(filteredItems, filteredItems.indexOf(item)) },
                                onOptions = {
                                    optionsItem = item
                                    showBottomSheet = true
                                },
                                modifier = Modifier
                            )
                        }
                    }
                }
                
                if (showBottomSheet) {
                    optionsItem?.let { item ->
                        OfflineOptionsBottomSheet(
                            item = item,
                            onDismiss = { showBottomSheet = false },
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
                        onDismiss = { showSleepSheet = false },
                        onSelected = { mainViewModel.sleepTimer(it) })
                }
            }
        }
    }
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineOptionsBottomSheet(
    item: DownloadedItem,
    onDismiss: () -> Unit,
    mainViewModel: MainViewModel,
    onSleepTimer: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(true)
    
    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.background,
    ) {
        Column {
             Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
            )
            ListItem(
                headlineContent = { Text("Delete") },
                leadingContent = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                modifier = Modifier.clickable {
                    mainViewModel.deleteDownloaded(item)
                    onDismiss()
                }
            )
            ListItem(
                headlineContent = { Text("Sleep timer") },
                leadingContent = { Icon(Icons.Outlined.Timer, contentDescription = null) },
                modifier = Modifier.clickable {
                    onSleepTimer()
                    onDismiss()
                }
            )
            Spacer(Modifier.padding(25.dp))
        }
    }
}
