package com.example.music_player_application

data class AudioPlaybackState (
    val isPlaying: Boolean = false,
    val currentProgress: Int = 0,
    val maxProgress: Int = 0,
    val currentTime: String = "00:00"
)