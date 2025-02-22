package com.cherenkov.videoapp.videoapp.presentation.player

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val repository: VideoRepository,
    application: Application,
    private val savedStateHandle: SavedStateHandle
): AndroidViewModel(application = application) {

    private val videoId = savedStateHandle.toRoute<Route.PlayerScreen>().id
    private var player: ExoPlayer? = null
    private var controlsTimerJob: Job? = null
    private val context = getApplication<Application>()

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
            is PlayerAction.PlayPause -> togglePlayPause()
            is PlayerAction.ToggleControls -> toggleControls()
            is PlayerAction.SeekTo -> seekTo(action.position)
            is PlayerAction.EnterFullScreen -> enterFullscreen()
            is PlayerAction.ExitFullScreen -> exitFullscreen()
            is PlayerAction.OnBackClicked -> {
                onCleared()
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer(url: String) {
        player = ExoPlayer.Builder(context)
            .setSeekForwardIncrementMs(10000)
            .setSeekBackIncrementMs(10000)
            .build()
            .apply {
                Log.d("video", url)
                setMediaItem(MediaItem.fromUri(url))
                prepare()
                playWhenReady = _state.value.isPlaying

                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_BUFFERING -> {
                                _state.value = _state.value.copy(
                                    isBuffering = true
                                )
                            }
                            Player.STATE_READY -> {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    isBuffering = false,
                                    totalDuration = duration
                                )
                            }
                            else -> Unit
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _state.value = _state.value.copy(
                            isPlaying = isPlaying
                        )
                    }

                    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                        _state.value = _state.value.copy(
                            playbackSpeed = playbackParameters.speed
                        )
                    }
                })
            }
        _state.value = _state.value.copy(
            player = player
        )
    }

    fun findVideoInfo() = viewModelScope.launch{
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
                initializePlayer(result.video_link)
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

    private fun togglePlayPause() {
        player?.let {
            it.playWhenReady = !it.isPlaying
            _state.value = _state.value.copy(
                isPlaying = it.playWhenReady,
                showControls = true
            )
            startControlsTimer()
        }
    }

    private fun toggleControls() {
        _state.value = _state.value.copy(
            showControls = !_state.value.showControls
        )
        if (_state.value.showControls) {
            startControlsTimer()
        }
    }

    private fun seekTo(position: Float) {
        player?.let { player ->
            if (player.duration > 0) {
                val seekTime = (player.duration * position).toLong()
                player.seekTo(seekTime)
                _state.value = _state.value.copy(
                    progress = position,
                    currentTime = seekTime,
                    showControls = true
                )
                startControlsTimer()
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun enterFullscreen() {
        _state.value = _state.value.copy(
            isFullscreen = true,
            showControls = false
        )
        player?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        startControlsTimer()
    }

    @OptIn(UnstableApi::class)
    private fun exitFullscreen() {
        _state.value = _state.value.copy(
            isFullscreen = false,
            showControls = true
        )
        player?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
        startControlsTimer()
    }

    private fun startControlsTimer() {
        controlsTimerJob?.cancel()
        controlsTimerJob = viewModelScope.launch {
            delay(3000L)
            _state.value = _state.value.copy(
                showControls = false
            )
        }
    }

    private fun releasePlayer() {
        controlsTimerJob?.cancel()
        player?.release()
        player = null
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }



}