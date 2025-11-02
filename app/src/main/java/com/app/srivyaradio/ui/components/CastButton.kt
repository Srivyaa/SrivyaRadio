package com.app.srivyaradio.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory

@Composable
fun CastButton(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context: Context ->
            MediaRouteButton(context).apply {
                CastButtonFactory.setUpMediaRouteButton(context, this)
            }
        }
    )
}
