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
    private var progressUpdateJob: Job? = null
    private val context = getApplication<Application>()

    private val _state = MutableStateFlow(PlayerState())
    val state = _state
        .onStart {
            initializePlayer()
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
            is PlayerAction.SeekBySeconds -> handleSeekBySeconds(action.time)
            is PlayerAction.Retry -> handleRetry()
        }
    }

    private fun initializePlayer() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getInfoVideo(videoId)
                .onSuccess { videoInfo ->
                    player?.release()
                    createNewPlayer(videoInfo.video_link)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            playedVideo = videoInfo,
                            errorMessage = null
                        )
                    }
                    startProgressUpdates()
                }
                .onError { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toUiText()
                        )
                    }
                }
        }
    }

    @OptIn(UnstableApi::class)
    private fun createNewPlayer(videoUrl: String) {
        player = ExoPlayer.Builder(context)
            .setSeekForwardIncrementMs(10000)
            .setSeekBackIncrementMs(10000)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(videoUrl))
                prepare()
                playWhenReady = _state.value.isPlaying

                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_BUFFERING -> {
                                _state.update { it.copy(isBuffering = true) }
                            }
                            Player.STATE_READY -> {
                                _state.update {
                                    it.copy(
                                        isBuffering = false,
                                        totalDuration = duration
                                    )
                                }
                            }
                            Player.STATE_ENDED -> {
                                _state.update { it.copy(isPlaying = false) }
                            }
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _state.update { it.copy(isPlaying = isPlaying) }
                    }

                    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                        _state.update { it.copy(playbackSpeed = playbackParameters.speed) }
                    }
                })
            }
        _state.update { it.copy(player = player) }
    }

    private fun startProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            while (true) {
                player?.let { player ->
                    if (player.isPlaying || _state.value.isBuffering) {
                        val currentPosition = player.currentPosition
                        val bufferedPosition = player.bufferedPosition
                        val duration = player.duration

                        if (duration > 0) {
                            val progress = currentPosition.toFloat() / duration
                            val buffered = bufferedPosition.toFloat() / duration
                            _state.update {
                                it.copy(
                                    currentTime = currentPosition,
                                    progress = progress.coerceIn(0f, 1f),
                                    buffered = buffered.coerceIn(0f, 1f)
                                )
                            }
                        }
                    }
                }
                delay(1000)
            }
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
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
            _state.update { state ->
                state.copy(
                    isPlaying = it.isPlaying,
                    showControls = true
                )
            }
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
            val duration = player.duration
            if (duration != C.TIME_UNSET) {
                val seekTime = (duration * position.coerceIn(0f, 1f)).toLong()
                player.seekTo(seekTime)
                _state.update {
                    it.copy(
                        currentTime = seekTime,
                        progress = position,
                        showControls = true
                    )
                }
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
            _state.update {
                if (!it.isControlsLocked) it.copy(showControls = false) else it
            }
        }
    }

    private fun releasePlayer() {
        controlsTimerJob?.cancel()
        progressUpdateJob?.cancel()
        player?.release()
        player = null
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }

    private fun handleSeekBySeconds(seconds: Int) {
        player?.let { exoPlayer ->
            val currentPosition = exoPlayer.currentPosition
            val newPosition = currentPosition + seconds * 1000L
            val duration = exoPlayer.duration

            val safePosition = newPosition.coerceIn(0, duration)
            exoPlayer.seekTo(safePosition)

            _state.update { it.copy(
                currentTime = safePosition,
                progress = if (duration > 0) (safePosition.toFloat() / duration) else 0f
            ) }
        }
    }

    private fun handleRetry() {
        _state.update { it.copy(
            isLoading = true,
            errorMessage = null
        ) }
        initializePlayer()
    }



}