package com.app.srivyaradio.ui.screens.devotional

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.srivyaradio.data.api.devotional.DevotionalFolder
import com.app.srivyaradio.data.api.devotional.DevotionalSong
import com.app.srivyaradio.data.api.devotional.FoldersResponse
import com.app.srivyaradio.data.repositories.DevotionalRepository
import com.app.srivyaradio.ui.MainViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.core.net.toUri

class DevotionalViewModel(
    private val repository: DevotionalRepository = DevotionalRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state

    private var data: FoldersResponse? = null

    fun load(force: Boolean = false) {
        if (data != null && !force) return
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repository.getFolders(force)
                data = resp
                _state.value = UiState.Data(resp)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed to load")
            }
        }
    }

    fun getFolder(uuid: String): DevotionalFolder? = data?.folders?.firstOrNull { it.folder_uuid == uuid }

    fun playFolder(
        mainViewModel: MainViewModel,
        folder: DevotionalFolder,
        startIndex: Int
    ) {
        // Build queue from songs
        val songs = folder.items.filter { it.url != null || it.url_resolved != null }
        val items = songs.map { song -> songToMediaItem(song, folder) }
        val index = if (startIndex in items.indices) startIndex else 0
        try {
            mainViewModel.isRadioLoading = true
            // Immediately select a synthetic item so MiniPlayer/Player show correct metadata
            if (items.isNotEmpty()) {
                mainViewModel.selectFromMediaItem(items[index])
            }
            mainViewModel.player.setMediaItems(items, index, /* startPositionMs */0)
            mainViewModel.player.prepare()
            mainViewModel.player.play()
            mainViewModel.refreshQueue()
        } catch (_: Exception) {
            // let existing error UI handle
        }
    }

    fun playFolderFromSong(
        mainViewModel: MainViewModel,
        folder: DevotionalFolder,
        selected: DevotionalSong
    ) {
        val songs = folder.items.filter { it.url != null || it.url_resolved != null }
        val items = songs.map { song -> songToMediaItem(song, folder) }
        val index = songs.indexOfFirst { it === selected || (
            (it.title ?: it.name) == (selected.title ?: selected.name) &&
            (it.url ?: it.url_resolved) == (selected.url ?: selected.url_resolved)
        ) }.let { if (it >= 0) it else 0 }
        try {
            mainViewModel.isRadioLoading = true
            if (items.isNotEmpty()) {
                mainViewModel.selectFromMediaItem(items[index])
            }
            mainViewModel.player.setMediaItems(items, index, 0)
            mainViewModel.player.prepare()
            mainViewModel.player.play()
            mainViewModel.refreshQueue()
        } catch (_: Exception) {
        }
    }

    private fun songToMediaItem(song: DevotionalSong, folder: DevotionalFolder): MediaItem {
        val title = song.title ?: song.name ?: "Untitled"
        val artist = song.artist ?: folder.folder_name
        val art = (song.favurl ?: folder.cover).orEmpty()
        val streamUrl = song.url ?: song.url_resolved ?: ""
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(song.album ?: folder.folder_name)
            .setArtworkUri(art.toUri())
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
            .build()
        return MediaItem.Builder()
            .setMediaId("BROWSE:" + folder.folder_uuid + ":" + title)
            .setUri(streamUrl)
            .setMediaMetadata(metadata)
            .build()
    }

    sealed interface UiState {
        data object Loading : UiState
        data class Data(val response: FoldersResponse) : UiState
        data class Error(val message: String) : UiState
    }
}
