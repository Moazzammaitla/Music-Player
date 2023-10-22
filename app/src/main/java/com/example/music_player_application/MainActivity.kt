package com.example.music_player_application

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {

    private lateinit var albumImage: ImageView
    private lateinit var seekBarMusicProgress: SeekBar
    private lateinit var tvTimer: TextView
    private lateinit var btnPlay: ImageButton
    private lateinit var btnPause: ImageButton
    private lateinit var btnStop: ImageButton

    private lateinit var viewModel: AudioPlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        albumImage = findViewById(R.id.album_image)
        seekBarMusicProgress = findViewById(R.id.seekbar_music_progress)
        tvTimer = findViewById(R.id.tv_timer)
        btnPlay = findViewById(R.id.btn_play)
        btnPause = findViewById(R.id.btn_pause)
        btnStop = findViewById(R.id.btn_stop)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(AudioPlayerViewModel::class.java)

        // Observe changes in playback state
        viewModel.playbackState.observe(this) { state ->
            updateUI(state)
        }

        // Set up playback controls
        btnPlay.setOnClickListener {
            viewModel.play()
        }

        btnPause.setOnClickListener {
            viewModel.pause()
        }

        btnStop.setOnClickListener {
            viewModel.stop()
        }
    }

    private fun updateUI(state: AudioPlaybackState) {
        // Update SeekBar and Timer based on the state
        seekBarMusicProgress.progress = state.currentProgress
        seekBarMusicProgress.max = state.maxProgress
        tvTimer.text = state.currentTime

        // Update playback button visibility
        btnPlay.visibility = if (state.isPlaying) View.GONE else View.VISIBLE
        btnPause.visibility = if (state.isPlaying) View.VISIBLE else View.GONE
    }
}