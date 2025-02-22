package com.cherenkov.videoapp.videoapp.presentation.player

import androidx.media3.exoplayer.ExoPlayer
import com.cherenkov.videoapp.core.presentation.UiText
import com.cherenkov.videoapp.videoapp.domain.models.VideoInfo
import com.cherenkov.videoapp.videoapp.domain.models.VideoItem

data class PlayerState(
    val player: ExoPlayer? = null,
    val isBuffering: Boolean = false,
    val topVideos: List<VideoItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: UiText? = null,
    val playedVideo: VideoInfo? = null,
    val isPlaying: Boolean = false,
    val isFullscreen: Boolean = false,
    val showControls: Boolean = false,
    val progress: Float = 0f,
    val buffered: Float = 0f,
    val totalDuration: Long = 0L,
    val currentTime: Long = 0L,
    val playbackSpeed: Float = 1f,
    val isControlsLocked: Boolean = false
)