package com.app.srivyaradio.wear

import android.content.Context
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object WearMessaging {
    private const val PATH_CONTROL = "/srivyaradio/control"

    const val ACTION_PLAY_PAUSE = "PLAY_PAUSE"
    const val ACTION_NEXT = "NEXT"
    const val ACTION_PREV = "PREV"
    const val ACTION_SEEK_BACK = "SEEK_BACK"
    const val ACTION_SEEK_FORWARD = "SEEK_FORWARD"
    const val ACTION_TOGGLE_SHUFFLE = "TOGGLE_SHUFFLE"
    const val ACTION_CYCLE_REPEAT = "CYCLE_REPEAT"

    fun sendControl(context: Context, action: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val nodeClient = Wearable.getNodeClient(context)
                val nodes = nodeClient.connectedNodes.await()
                val messageClient = Wearable.getMessageClient(context)
                val data = action.toByteArray()
                nodes.forEach { node ->
                    try {
                        messageClient.sendMessage(node.id, PATH_CONTROL, data).await()
                    } catch (_: Exception) { }
                }
            } catch (_: Exception) { }
        }
    }
}
