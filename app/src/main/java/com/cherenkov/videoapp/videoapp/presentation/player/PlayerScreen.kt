package com.cherenkov.videoapp.videoapp.presentation.player

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.cherenkov.videoapp.videoapp.domain.models.VideoInfo
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.koin.androidx.compose.koinViewModel
import java.util.concurrent.TimeUnit
import android.content.res.Configuration
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.media3.common.util.UnstableApi
import com.cherenkov.videoapp.videoapp.presentation.reusable_components.ErrorMessage

@Composable
fun PlayerScreenRoot(
    viewModel: PlayerViewModel = koinViewModel(),
    onBackClick: () -> Unit,
    onVideoClick: (Int) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PlayerScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is PlayerAction.OnBackClicked -> onBackClick()
                is PlayerAction.OnVideoClicked -> onVideoClick(action.id)
                else -> Unit
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
fun PlayerScreen(
    state: PlayerState,
    onAction: (PlayerAction) -> Unit
) {
    val systemUiController = rememberSystemUiController()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    LaunchedEffect(configuration.orientation) {
        if (isLandscape && !state.isFullscreen) {
            onAction(PlayerAction.EnterFullScreen)
        } else if (!isLandscape && state.isFullscreen) {
            onAction(PlayerAction.ExitFullScreen)
        }
    }

    DisposableEffect(state.isFullscreen) {
        systemUiController.isNavigationBarVisible = !state.isFullscreen
        systemUiController.isStatusBarVisible = !state.isFullscreen
        onDispose { }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF121212), Color(0xFF1F1B24))
                )
            )
    ) {
        when {
            state.isLoading -> FullScreenLoading()
            state.errorMessage != null -> ErrorState(
                error = state.errorMessage.asString(),
                onRetry = { onAction(PlayerAction.Retry) }
            )
            else -> {
                if (state.isFullscreen) {
                    FullScreenPlayer(state, onAction)
                } else {
                    NonFullScreenPlayer(state, onAction)
                }
            }
        }
    }
}

@Composable
private fun FullScreenPlayer(
    state: PlayerState,
    onAction: (PlayerAction) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .animateContentSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onAction(PlayerAction.ToggleControls) },
                    onDoubleTap = { offset ->
                        val seekBy = if (offset.x < size.width / 2) -3 else 3
                        onAction(PlayerAction.SeekBySeconds(seekBy))
                    }
                )
            }
    ) {
        Crossfade(targetState = state.player) { player ->
            YouTubePlayerView(player = player)
        }
        AnimatedVisibility(
            visible = state.isBuffering,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            BufferingIndicator()
        }
        AnimatedVisibility(
            visible = state.showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )
        }
        ControlsOverlay(state = state, onAction = onAction)
    }
}

@Composable
private fun NonFullScreenPlayer(
    state: PlayerState,
    onAction: (PlayerAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .animateContentSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onAction(PlayerAction.ToggleControls) },
                        onDoubleTap = { offset ->
                            val seekBy = if (offset.x < size.width / 2) -3 else 3
                            onAction(PlayerAction.SeekBySeconds(seekBy))
                        }
                    )
                }
        ) {
            Crossfade(targetState = state.player) { player ->
                YouTubePlayerView(player = player)
            }
            this@Column.AnimatedVisibility(
                visible = state.isBuffering,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BufferingIndicator()
            }
            this@Column.AnimatedVisibility(
                visible = state.showControls,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )
            }
            ControlsOverlay(state = state, onAction = onAction)
        }
        VideoInfoPanel(videoInfo = state.playedVideo)
    }
}


@Composable
fun VideoInfoPanel(videoInfo: VideoInfo?) {
    if (videoInfo == null) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp)
    ) {
        Text(
            text = videoInfo.title,
            style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = videoInfo.author,
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray)
        )
    }
}

@Composable
private fun ControlsOverlay(
    state: PlayerState,
    onAction: (PlayerAction) -> Unit
) {
    AnimatedVisibility(
        visible = state.showControls,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TopControlsBar(onAction, state)
            Spacer(modifier = Modifier.weight(1f))
            BottomControlsPanel(state, onAction)
        }
    }
}

@Composable
private fun TopControlsBar(
    onAction: (PlayerAction) -> Unit,
    state: PlayerState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = { onAction(PlayerAction.OnBackClicked) },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        state.playedVideo?.let {
            Text(
                text = it.title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
        }
        IconButton(
            onClick = {
                onAction(
                    if (state.isFullscreen) PlayerAction.ExitFullScreen
                    else PlayerAction.EnterFullScreen
                )
            },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = if (state.isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                contentDescription = "Fullscreen",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun BottomControlsPanel(
    state: PlayerState,
    onAction: (PlayerAction) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        YouTubeSeekBar(state, onAction)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PlaybackControls(state, onAction)
        }
    }
}

@Composable
private fun YouTubeSeekBar(
    state: PlayerState,
    onAction: (PlayerAction) -> Unit
) {
    var dragging by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = state.progress,
            onValueChange = {
                dragging = true
                onAction(PlayerAction.SeekTo(it))
            },
            onValueChangeFinished = { dragging = false },
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFBB86FC),
                activeTrackColor = Color(0xFFBB86FC).copy(alpha = 0.5f),
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = state.currentTime.toYouTubeTime(),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
            state.playedVideo?.duration?.let {
                Text(
                    text = it,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun PlaybackControls(
    state: PlayerState,
    onAction: (PlayerAction) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconButton(
            onClick = { onAction(PlayerAction.SeekBySeconds(-3)) },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Replay10,
                contentDescription = "Rewind",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        YouTubePlayPauseButton(isPlaying = state.isPlaying) { onAction(PlayerAction.PlayPause) }
        IconButton(
            onClick = { onAction(PlayerAction.SeekBySeconds(3)) },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Forward10,
                contentDescription = "Forward",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun YouTubePlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val transition = androidx.compose.animation.core.updateTransition(targetState = isPlaying, label = "playPause")
    val size by transition.animateDp(label = "size") { playing ->
        if (playing) 48.dp else 56.dp
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFBB86FC).copy(alpha = 0.9f))
            .clickable(onClick = onClick)
    ) {
        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = { fadeIn() with fadeOut() }
        ) { playing ->
            if (playing) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun BufferingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator(
                color = Color(0xFFBB86FC),
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Buffering...",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun FullScreenLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = Color(0xFFBB86FC),
                strokeWidth = 4.dp,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Loading Video...",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Error",
                tint = Color(0xFFBB86FC),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = error,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFBB86FC),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(8.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Retry", modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@SuppressLint("DefaultLocale")
private fun Long.toYouTubeTime(): String {
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun YouTubePlayerView(player: ExoPlayer?) {
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                this.player = player
                setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
            }
        },
        update = { playerView ->
            playerView.player = player
        },
        modifier = Modifier.fillMaxSize()
    )
}

