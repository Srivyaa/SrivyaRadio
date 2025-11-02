package com.app.srivyaradio.ui.screens.queue

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.app.srivyaradio.ui.MainViewModel
import com.app.srivyaradio.ui.components.RadioLogoSmall
import com.app.srivyaradio.ui.components.rememberDragDropListState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun QueueScreen(mainViewModel: MainViewModel) {
    val scope = rememberCoroutineScope()
    var overscrollJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(Unit) { mainViewModel.refreshQueue() }

    val dragDropListState = rememberDragDropListState(onMove = { from, to ->
        mainViewModel.moveInQueue(from, to)
    }, onInterrupt = {
        mainViewModel.refreshQueue()
    })

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset -> dragDropListState.onDragStart(offset) },
                    onDrag = { change, offset ->
                        change.consume()
                        dragDropListState.onDrag(offset)
                        if (overscrollJob?.isActive == true) return@detectDragGesturesAfterLongPress
                        dragDropListState.checkForOverScroll().takeIf { it != 0f }?.let {
                            overscrollJob = scope.launch { dragDropListState.lazyListState.scrollBy(it) }
                        } ?: run { overscrollJob?.cancel() }
                    },
                    onDragCancel = { dragDropListState.onDragInterrupted() },
                    onDragEnd = { dragDropListState.onDragInterrupted() }
                )
            },
        state = dragDropListState.lazyListState
    ) {
        itemsIndexed(mainViewModel.queueStations) { index, station ->
            Row(
                modifier = Modifier
                    .composed {
                        val offsetOrNull = dragDropListState.elementDisplacement.takeIf { index == dragDropListState.currentIndexOfDraggedItem }
                        Modifier.graphicsLayer { translationY = offsetOrNull ?: 0f }
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                RadioLogoSmall(imageUrl = station.favicon, size = 40)
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier.weight(1f).padding(start = 12.dp)
                )
                IconButton(onClick = { mainViewModel.removeFromQueue(index) }) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
        }
    }
}
