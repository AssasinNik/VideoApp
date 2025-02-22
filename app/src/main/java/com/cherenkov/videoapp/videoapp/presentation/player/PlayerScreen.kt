package com.cherenkov.videoapp.videoapp.presentation.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.SHOW_BUFFERING_ALWAYS
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

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
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    DisposableEffect(state.isFullscreen) {
        systemUiController.isNavigationBarVisible = !state.isFullscreen
        systemUiController.isStatusBarVisible = !state.isFullscreen
        onDispose {}
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when {
            state.isLoading -> YouTubeStyleLoading()
            state.errorMessage != null -> YouTubeErrorState(
                error = state.errorMessage!!.asString(),
                onRetry = { onAction(PlayerAction.Retry) }
            )
            else -> {
                PlayerContent(state, onAction, isLandscape)
                ControlsOverlay(state, onAction, isLandscape)
            }
        }
    }
}

@Composable
private fun PlayerContent(
    state: PlayerState,
    onAction: (PlayerAction) -> Unit,
    isLandscape: Boolean
) {
    val transition = updateTransition(targetState = state.showControls, label = "controlsTransition")
    val controlsAlpha by transition.animateFloat { show ->
        if (show) 1f else 0f
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { onAction(PlayerAction.ToggleControls) },
                onDoubleTap = { offset ->
                    val seekPosition = if (offset.x < size.width / 2) -3 else 3
                    onAction(PlayerAction.SeekBySeconds(seekPosition))
                }
            )
        }
    ) {
        YouTubePlayerView(player = state.player)

        AnimatedVisibility(
            visible = state.isBuffering,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            BufferingIndicator()
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun YouTubePlayerView(player: ExoPlayer?) {
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                this.player = player
                setShowBuffering(SHOW_BUFFERING_ALWAYS)
                setBackgroundColor(0xFF00000)
            }
        },
        update = { playerView ->
            playerView.player = player
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun ControlsOverlay(
    state: PlayerState,
    onAction: (PlayerAction) -> Unit,
    isLandscape: Boolean
) {
    AnimatedVisibility(
        visible = state.showControls,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        modifier = Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    Color.Black.copy(alpha = 0.7f),
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.7f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TopControlsBar(onAction, state)
            Spacer(Modifier.weight(1f))
            BottomControlsPanel(state, onAction, isLandscape)
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

        Text(
            text = "Pexels Video",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        )

        IconButton(
            onClick = { onAction(
                if (state.isFullscreen) PlayerAction.ExitFullScreen
                else PlayerAction.EnterFullScreen
            ) },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = if (state.isFullscreen) Icons.Default.FullscreenExit
                else Icons.Default.Fullscreen,
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
    onAction: (PlayerAction) -> Unit,
    isLandscape: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        YouTubeSeekBar(state, onAction)

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PlaybackControls(state, onAction)

            if (isLandscape) {
                Spacer(Modifier.weight(1f))
                //PlaybackSpeedButton(state, onAction)
            }
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
                thumbColor = Color.Red,
                activeTrackColor = Color.Red.copy(alpha = 0.5f),
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
            Text(
                text = state.totalDuration.toYouTubeTime(),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
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
                contentDescription = "Rewind 10s",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        YouTubePlayPauseButton(state.isPlaying) {
            onAction(PlayerAction.PlayPause)
        }

        IconButton(
            onClick = { onAction(PlayerAction.SeekBySeconds(3)) },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Forward10,
                contentDescription = "Forward 10s",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@kotlin.OptIn(ExperimentalAnimationApi::class)
@Composable
private fun YouTubePlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val transition = updateTransition(isPlaying, label = "playPause")
    val size by transition.animateDp(label = "size") { playing ->
        if (playing) 48.dp else 56.dp
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.Red.copy(alpha = 0.9f))
            .clickable(onClick = onClick)
    ) {
        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = {
                scaleIn() with scaleOut()
            }
        ) { playing ->
            if (playing) {
                Icon(
                    Icons.Default.Pause,
                    "Pause",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Icon(
                    Icons.Default.PlayArrow,
                    "Play",
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
                color = Color.White,
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
private fun YouTubeStyleLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = Color.Red,
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
private fun YouTubeErrorState(
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
                Icons.Default.ErrorOutline,
                "Error",
                tint = Color.Red,
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
                    containerColor = Color.Red,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(8.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Default.Refresh, "Retry", Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
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