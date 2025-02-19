package com.cherenkov.videoapp.videoapp.presentation.list_videos

import com.cherenkov.videoapp.core.presentation.UiText
import com.cherenkov.videoapp.videoapp.domain.models.VideoItem

data class VideoListState(
    val topVideos: List<VideoItem> = emptyList(),
    val isLoading: Boolean = true,
    val isFinding: Boolean = false,
    val errorMessage: UiText? = null,
    val searchedVideos: List<VideoItem> = emptyList()
)
