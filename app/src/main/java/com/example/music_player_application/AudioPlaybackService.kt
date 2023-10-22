package com.example.music_player_application

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class AudioPlaybackService: Service() {

    private lateinit var mediaPlayer: MediaPlayer
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateSeekBarRunnable: Runnable

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.song) // Replace with your audio resource
        mediaPlayer.isLooping = false

        // Initialize the SeekBar update Runnable
        updateSeekBarRunnable = object : Runnable {
            override fun run() {
                val currentProgress = mediaPlayer.currentPosition
                val maxProgress = mediaPlayer.duration
                val currentTime = formatTime(currentProgress)
                sendSeekBarUpdateBroadcast(currentProgress, maxProgress, currentTime)
                handler.postDelayed(this, 1000) // Update every second
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "PLAY" -> {
                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                    handler.post(updateSeekBarRunnable)
                }
            }
            "PAUSE" -> {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    handler.removeCallbacks(updateSeekBarRunnable)
                }
            }
            "STOP" -> {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                    handler.removeCallbacks(updateSeekBarRunnable)
                }
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        mediaPlayer.release()
        super.onDestroy()
    }

    private fun sendSeekBarUpdateBroadcast(
        currentProgress: Int,
        maxProgress: Int,
        currentTime: String
    ) {
        val intent = Intent("UPDATE_SEEK_BAR")
        intent.putExtra("currentProgress", currentProgress)
        intent.putExtra("maxProgress", maxProgress)
        intent.putExtra("currentTime", currentTime)
        sendBroadcast(intent)
    }

    private fun formatTime(milliseconds: Int): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}