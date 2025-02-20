package com.cherenkov.videoapp.videoapp.presentation.player

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
import android.net.Uri
import androidx.annotation.OptIn
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.toRoute
import com.cherenkov.videoapp.core.domain.onError
import com.cherenkov.videoapp.core.domain.onSuccess
import com.cherenkov.videoapp.core.presentation.toUiText
import com.cherenkov.videoapp.videoapp.domain.VideoRepository
import com.cherenkov.videoapp.videoapp.presentation.list_videos.VideoListAction
import com.cherenkov.videoapp.videoapp.presentation.list_videos.VideoListState
import com.cherenkov.videoapp.videoapp.utils.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val repository: VideoRepository,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val videoId = savedStateHandle.toRoute<Route.PlayerScreen>().id

    private val _state = MutableStateFlow(PlayerState())
    val state = _state
        .onStart {
            findVideoInfo()
            searchPopularVideos()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )
    fun onAction(action: PlayerAction){
        when(action){
            is PlayerAction.OnVideoClicked -> {

            }
            else -> Unit
        }
    }

    private fun findVideoInfo() = viewModelScope.launch{
        _state.update { it.copy(
            isLoading = true
        ) }
        repository
            .getInfoVideo(videoId)
            .onSuccess { result ->
                _state.update { it.copy(
                    isLoading = false,
                    errorMessage = null,
                    playedVideo = result
                ) }
            }
            .onError { error ->
                _state.update { it.copy(
                    topVideos = emptyList(),
                    isLoading = false,
                    errorMessage = error.toUiText()
                ) }
            }
    }

    private fun searchPopularVideos() = viewModelScope.launch{
        _state.update { it.copy(
            isLoading = true
        ) }
        repository
            .getPopularVideos()
            .onSuccess { result ->
                _state.update { it.copy(
                    isLoading = false,
                    errorMessage = null,
                    topVideos = result
                ) }
            }
            .onError { error ->
                _state.update { it.copy(
                    topVideos = emptyList(),
                    isLoading = false,
                    errorMessage = error.toUiText()
                ) }
            }
    }


}