package com.cherenkov.videoapp.videoapp.presentation.list_videos

sealed interface VideoListAction {
    data class OnVideoClicked(val id: Int): VideoListAction
    data class OnChangedText(val text: String): VideoListAction
    data class OnRefreshSwipe(val query: String = ""): VideoListAction
}