package com.app.srivyaradio.wear

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

class WearMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp(
                onOpenOnPhone = {
                    // Open the website as a placeholder. Later we can use RemoteIntent to open the phone app.
                    val url = "https://srivya.io"
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            )
        }
    }
}

@Composable
fun WearApp(onOpenOnPhone: () -> Unit) {
    val ctx = LocalContext.current
    val actions = listOf(
        WearAction("Play/Pause") { WearMessaging.sendControl(ctx, WearMessaging.ACTION_PLAY_PAUSE) },
        WearAction("Next") { WearMessaging.sendControl(ctx, WearMessaging.ACTION_NEXT) },
        WearAction("Previous") { WearMessaging.sendControl(ctx, WearMessaging.ACTION_PREV) },
        WearAction("Seek Back") { WearMessaging.sendControl(ctx, WearMessaging.ACTION_SEEK_BACK) },
        WearAction("Seek Forward") { WearMessaging.sendControl(ctx, WearMessaging.ACTION_SEEK_FORWARD) },
        WearAction("Toggle Shuffle") { WearMessaging.sendControl(ctx, WearMessaging.ACTION_TOGGLE_SHUFFLE) },
        WearAction("Cycle Repeat") { WearMessaging.sendControl(ctx, WearMessaging.ACTION_CYCLE_REPEAT) },
        WearAction("Open on Phone", onOpenOnPhone)
    )

    MaterialTheme {
        ScalingLazyColumn(modifier = Modifier) {
            items(actions) { action ->
                Chip(
                    onClick = action.onClick,
                    label = { Text(text = action.label) },
                    colors = ChipDefaults.primaryChipColors(),
                )
            }
        }
    }
}

data class WearAction(val label: String, val onClick: () -> Unit)
