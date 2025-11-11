package com.app.srivyaradio.wear

import android.content.ComponentName
import android.os.Bundle
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.app.srivyaradio.media.PlayerService
import com.app.srivyaradio.utils.Constants
import com.google.common.util.concurrent.ListenableFuture

class WearControlListenerService : WearableListenerService() {

    private lateinit var sessionToken: SessionToken
    private lateinit var browserFuture: ListenableFuture<MediaBrowser>
    @Volatile private var browser: MediaBrowser? = null

    override fun onCreate() {
        super.onCreate()
        sessionToken = SessionToken(this, ComponentName(this, PlayerService::class.java))
        browserFuture = MediaBrowser.Builder(this, sessionToken).buildAsync()
        browserFuture.addListener({
            try {
                browser = browserFuture.get()
            } catch (_: Exception) { }
        }, Runnable::run)
    }

    override fun onDestroy() {
        try { browser?.release() } catch (_: Exception) { }
        super.onDestroy()
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path != Constants.WEAR_CONTROL_PATH) return
        val action = try { String(messageEvent.data ?: ByteArray(0)) } catch (_: Exception) { "" }
        val ctrl = browser ?: return
        when (action) {
            Constants.WEAR_ACTION_PLAY_PAUSE -> if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
            Constants.WEAR_ACTION_NEXT -> ctrl.seekToNextMediaItem()
            Constants.WEAR_ACTION_PREV -> ctrl.seekToPreviousMediaItem()
            Constants.WEAR_ACTION_SEEK_BACK -> ctrl.seekBack()
            Constants.WEAR_ACTION_SEEK_FORWARD -> ctrl.seekForward()
            Constants.WEAR_ACTION_TOGGLE_SHUFFLE -> ctrl.shuffleModeEnabled = !ctrl.shuffleModeEnabled
            Constants.WEAR_ACTION_CYCLE_REPEAT -> ctrl.repeatMode = nextRepeat(ctrl.repeatMode)
        }
    }

    private fun nextRepeat(current: Int): Int {
        return when (current) {
            androidx.media3.common.Player.REPEAT_MODE_OFF -> androidx.media3.common.Player.REPEAT_MODE_ALL
            androidx.media3.common.Player.REPEAT_MODE_ALL -> androidx.media3.common.Player.REPEAT_MODE_ONE
            else -> androidx.media3.common.Player.REPEAT_MODE_OFF
        }
    }
}
