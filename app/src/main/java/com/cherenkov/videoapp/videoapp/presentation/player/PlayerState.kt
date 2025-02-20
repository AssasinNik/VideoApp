package com.cherenkov.videoapp.videoapp.presentation.player

import com.cherenkov.videoapp.core.presentation.UiText
import com.cherenkov.videoapp.videoapp.domain.models.VideoInfo
import com.cherenkov.videoapp.videoapp.domain.models.VideoItem

data class PlayerState(
    val topVideos: List<VideoItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: UiText? = null,
    val playedVideo : VideoInfo? = null,
    val idPlayedVideo: Int? = null,
    val isPlaying: Boolean = false,
)