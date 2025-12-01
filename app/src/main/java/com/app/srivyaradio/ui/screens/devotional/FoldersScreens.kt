package com.app.srivyaradio.ui.screens.devotional

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.app.srivyaradio.R
import com.app.srivyaradio.ui.MainViewModel
import androidx.lifecycle.viewModelScope
import com.app.srivyaradio.ui.components.RadioLogoSmall
import com.app.srivyaradio.ui.components.ShimmerStation
import com.app.srivyaradio.data.api.devotional.DevotionalFolder
import com.app.srivyaradio.data.api.devotional.DevotionalSong
import com.app.srivyaradio.utils.Constants.BROWSE_FOLDER_PREFIX
import androidx.compose.material.icons.filled.ArrowBack

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
fun FoldersGridScreen(
    navController: NavController,
    devotionalViewModel: DevotionalViewModel,
    mainViewModel: MainViewModel
) {
    val state by devotionalViewModel.state.collectAsState()
    LaunchedEffect(Unit) { devotionalViewModel.load() }

    when (state) {
        is DevotionalViewModel.UiState.Loading -> {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(10) { ShimmerStation(size = 80, round = 12) }
            }
        }
        is DevotionalViewModel.UiState.Error -> {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text((state as DevotionalViewModel.UiState.Error).message, modifier = Modifier.padding(20.dp))
            }
        }
        is DevotionalViewModel.UiState.Data -> {
            val response = (state as DevotionalViewModel.UiState.Data).response
            var query by remember { mutableStateOf("") }
            val folders = remember(response.folders, query) {
                val all = response.folders
                val q = query.trim()
                val filtered = if (q.isEmpty()) all else all.filter { it.folder_name.contains(q, ignoreCase = true) }
                filtered.distinctBy { it.folder_uuid }
            }
            if (folders.isEmpty()) {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No folders found", modifier = Modifier.padding(20.dp))
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = { Text("Browse") },
                        navigationIcon = {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = null)
                            }
                        }
                    )
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        label = { Text("Search folders") }
                    )
                    if (!response.name.isNullOrBlank() || !response.description.isNullOrBlank()) {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                            if (!response.name.isNullOrBlank()) {
                                Text(text = response.name!!, style = MaterialTheme.typography.titleLarge)
                            }
                            if (!response.description.isNullOrBlank()) {
                                Text(text = response.description!!, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 140.dp),
                        contentPadding = PaddingValues(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(folders, key = { it.folder_uuid }) { folder ->
                            var isFav by remember(folder.folder_uuid) { mutableStateOf(false) }
                            LaunchedEffect(folder.folder_uuid) {
                                val id = BROWSE_FOLDER_PREFIX + folder.folder_uuid
                                isFav = (mainViewModel.getFavoriteItem(id) != null)
                            }
                            FolderCard(folder = folder, isFavorite = isFav, onToggleFavorite = {
                                val id = BROWSE_FOLDER_PREFIX + folder.folder_uuid
                                mainViewModel.viewModelScope.launch {
                                    mainViewModel.addOrRemoveFromFavorites(id)
                                    isFav = !isFav
                                }
                            }) {
                                navController.navigate("BROWSE_FOLDER/${folder.folder_uuid}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderCard(folder: DevotionalFolder, isFavorite: Boolean, onToggleFavorite: () -> Unit, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(),
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth()) {
                RadioLogoSmall(imageUrl = folder.cover, size = 120, round = 16)
            }
            Text(
                text = folder.folder_name,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (folder.year != null) {
                Text(
                    text = folder.year.toString(),
                    modifier = Modifier.padding(bottom = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onToggleFavorite) {
                    if (isFavorite) Icon(Icons.Filled.Favorite, contentDescription = null) else Icon(Icons.Outlined.FavoriteBorder, contentDescription = null)
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun FolderSongsScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    devotionalViewModel: DevotionalViewModel,
    folderUuid: String
) {
    val folder = remember(folderUuid) { devotionalViewModel.getFolder(folderUuid) }
    if (folder == null) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Folder not found", modifier = Modifier.padding(20.dp))
        }
        return
    }
    var query by remember { mutableStateOf("") }
    val songs = remember(folder.items, query) {
        val all = folder.items.filter { it.url != null || it.url_resolved != null }
        val q = query.trim()
        if (q.isEmpty()) all else all.filter { s ->
            val t = (s.title ?: s.name ?: ""); val a = s.artist ?: ""; val al = s.album ?: ""
            t.contains(q, true) || a.contains(q, true) || al.contains(q, true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(folder.folder_name) },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null)
                }
            }
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                label = { Text("Search in folder") }
            )
        }
        items(items = songs, key = { s: DevotionalSong -> (s.title ?: s.name).orEmpty() + (s.url ?: s.url_resolved ?: "") }) { song: DevotionalSong ->
            var isFav by remember((song.title ?: song.name).orEmpty() + (song.url ?: song.url_resolved ?: "")) { mutableStateOf(false) }
            LaunchedEffect(song) {
                val id = "BROWSE:" + folder.folder_uuid + ":" + (song.title ?: song.name ?: "")
                isFav = (mainViewModel.getFavoriteItem(id) != null)
            }
            SongRow(
                song = song,
                fallbackCover = folder.cover,
                onPlay = {
                    val index = songs.indexOf(song).let { if (it >= 0) it else 0 }
                    devotionalViewModel.playFolder(mainViewModel, folder, index)
                },
                onFavorite = {
                    val id = "BROWSE:" + folder.folder_uuid + ":" + (song.title ?: song.name ?: "")
                    mainViewModel.viewModelScope.launch {
                        mainViewModel.addOrRemoveFromFavorites(id)
                        isFav = !isFav
                    }
                },
                onDownload = {
                    val title = (song.title ?: song.name ?: "Audio")
                    val art = (song.favurl ?: folder.cover).orEmpty()
                    val streamUrl = song.url ?: song.url_resolved ?: return@SongRow
                    mainViewModel.downloadAudioUrl(streamUrl, title, art, "")
                },
                isFavorite = isFav,
                onClick = {
                    val index = songs.indexOf(song).let { if (it >= 0) it else 0 }
                    devotionalViewModel.playFolder(mainViewModel, folder, index)
                }
            )
        }
        item { androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(bottom = 80.dp)) }
        }
    }
}

@Composable
private fun SongRow(
    song: DevotionalSong,
    fallbackCover: String,
    onPlay: () -> Unit,
    isFavorite: Boolean,
    onFavorite: () -> Unit,
    onDownload: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            RadioLogoSmall(imageUrl = (song.favurl ?: fallbackCover), size = 53)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = (song.title ?: song.name ?: "Untitled"),
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 18.sp,
            )
            val subtitle = listOfNotNull(song.artist, song.album, song.year?.toString()).joinToString(" • ")
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    fontWeight = FontWeight.W400,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        IconButton(onClick = onFavorite, modifier = Modifier.padding(6.dp)) {
            if (isFavorite) {
                Icon(imageVector = Icons.Filled.Favorite, contentDescription = null)
            } else {
                Icon(imageVector = Icons.Outlined.FavoriteBorder, contentDescription = null)
            }
        }
        IconButton(onClick = onDownload, modifier = Modifier.padding(6.dp)) { Icon(imageVector = Icons.Outlined.FileDownload, contentDescription = null) }
        IconButton(onClick = onPlay, modifier = Modifier.padding(6.dp)) { Icon(painter = painterResource(id = R.drawable.ic_play_circle), contentDescription = null) }
    }
}
