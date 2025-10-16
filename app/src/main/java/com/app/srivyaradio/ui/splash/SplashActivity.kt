package com.app.srivyaradio.ui.splash

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.VideoView
import androidx.activity.ComponentActivity
import com.app.srivyaradio.MainActivity
import com.app.srivyaradio.R

class SplashActivity : ComponentActivity() {
    private lateinit var videoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        videoView = findViewById(R.id.videoView)
        
        val videoPath = "android.resource://${packageName}/${R.raw.splash_video}"
        val videoUri = Uri.parse(videoPath)
        
        videoView.setVideoURI(videoUri)
        
        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = false
            videoView.start()
        }
        
        videoView.setOnCompletionListener {
            startMainActivity()
        }
        
        // In case the video fails to load
        videoView.setOnErrorListener { _, _, _ ->
            startMainActivity()
            true
        }
    }
    
    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    override fun onPause() {
        super.onPause()
        videoView.pause()
    }
    
    override fun onResume() {
        super.onResume()
        videoView.start()
    }
}
