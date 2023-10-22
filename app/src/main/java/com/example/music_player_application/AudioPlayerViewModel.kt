package com.example.music_player_application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import java.util.Timer
import java.util.TimerTask

class AudioPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var timer: Timer

    // LiveData for observing playback state
    val playbackState = MutableLiveData<AudioPlaybackState>()

    init {
        initializeMediaPlayer()
    }

    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer.create(getApplication(), R.raw.song) // Replace with your audio resource
        mediaPlayer.isLooping = false
        playbackState.value = AudioPlaybackState(
            isPlaying = false,
            currentProgress = 0,
            maxProgress = mediaPlayer.duration,
            currentTime = "00:00"
        )
    }

    fun play() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            startSeekBarTimer()
            playbackState.value = playbackState.value?.copy(isPlaying = true)
        }
    }

    fun pause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            stopSeekBarTimer()
            playbackState.value = playbackState.value?.copy(isPlaying = false)
        }
    }

    fun stop() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.prepare()
            mediaPlayer.seekTo(0)
            stopSeekBarTimer()
            playbackState.value = AudioPlaybackState(
                isPlaying = false,
                currentProgress = 0,
                maxProgress = mediaPlayer.duration,
                currentTime = "00:00"
            )

            // Show the notification when the song ends
            showNotification()
        }
    }

    private fun startSeekBarTimer() {
        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val currentProgress = mediaPlayer.currentPosition
                val maxProgress = mediaPlayer.duration
                val currentTime = formatTime(currentProgress)
                playbackState.postValue(
                    AudioPlaybackState(
                        isPlaying = true,
                        currentProgress = currentProgress,
                        maxProgress = maxProgress,
                        currentTime = currentTime
                    )
                )
            }
        }, 0, 1000)
    }

    private fun stopSeekBarTimer() {
        timer.cancel()
    }

    private fun formatTime(milliseconds: Int): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun showNotification() {
        val channelId = "song_notification_channel"
        val notificationId = 1

        // Create a PendingIntent for when the user taps the notification
        val intent = Intent(getApplication(), MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            getApplication(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create a notification channel (required for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                channelId,
                "Song Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val notification = NotificationCompat.Builder(getApplication(), channelId)
            .setSmallIcon(R.drawable.ic_launcher_background) // Replace with your notification icon
            .setContentTitle("Song Ended")
            .setContentText("The song has finished playing.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Show the notification
        val notificationManager =
            getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    override fun onCleared() {
        mediaPlayer.release()
        super.onCleared()
    }
}