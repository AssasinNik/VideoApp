package com.cherenkov.videoapp.videoapp.presentation.player

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
) {}

@Composable
fun VideoPlayerScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val systemUiController = rememberSystemUiController()
    val isFullscreen by remember { derivedStateOf { state.isFullscreen } }

    DisposableEffect(isFullscreen) {
        if (isFullscreen) {
            systemUiController.isNavigationBarVisible = false
        } else {
            systemUiController.isNavigationBarVisible = true
        }
        onDispose {}
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading -> LoadingShimmer()
            state.errorMessage != null -> ErrorState(error = state.errorMessage!!.asString(), onRetry = viewModel::findVideoInfo)
            else -> {
                VideoPlayerComponent(
                    state = state,
                    onEvent = viewModel::onAction,
                    isFullscreen = isFullscreen,
                    onBack = onBack,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun VideoPlayerComponent(
    state: PlayerState,
    onEvent: (PlayerAction) -> Unit,
    isFullscreen: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orientation = rememberOrientation()
    val animationSpec = remember { tween<Float>(300, easing = FastOutSlowInEasing) }

    Box(modifier = modifier.clickable { onEvent(PlayerAction.ToggleControls) }) {
        PlayerSurface(
            player = state.player,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        )

        AnimatedVisibility(
            visible = state.showControls,
            enter = fadeIn(animationSpec),
            exit = fadeOut(animationSpec)
        ) {
            ControlsOverlay(
                state = state,
                onEvent = onEvent,
                isFullscreen = isFullscreen,
                onBack = onBack,
                orientation = orientation,
                modifier = Modifier.fillMaxSize()
            )
        }

        AnimatedVisibility(
            visible = state.isBuffering,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun PlayerSurface(
    player: ExoPlayer?,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = false
                this.player = player
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setShowBuffering(SHOW_BUFFERING_ALWAYS)
                setBackgroundColor(0xFF88888)
            }
        },
        modifier = modifier
    )
}

@Composable
private fun ControlsOverlay(
    state: PlayerState,
    onEvent: (PlayerAction) -> Unit,
    isFullscreen: Boolean,
    onBack: () -> Unit,
    orientation: Orientation,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.background(
        Brush.verticalGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.7f),
                Color.Transparent,
                Color.Black.copy(alpha = 0.7f)
            ),
            startY = 0f,
            endY = 500f
        )
    )) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = {
                    if (isFullscreen) onEvent(PlayerAction.ExitFullScreen)
                    else onEvent(PlayerAction.EnterFullScreen)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit
                    else Icons.Default.Fullscreen,
                    contentDescription = "Fullscreen",
                    tint = Color.White
                )
            }
        }

        // Center Controls
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = { onEvent(PlayerAction.PlayPause) },
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Default.Pause
                    else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Bottom Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            SeekBar(
                progress = state.progress,
                buffered = state.buffered,
                onSeek = { pos -> onEvent(PlayerAction.SeekTo(pos)) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = state.currentTime.toFormattedTime(),
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                )

                Text(
                    text = state.totalDuration.toFormattedTime(),
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                )
            }
        }
    }
}

@Composable
private fun SeekBar(
    progress: Float,
    buffered: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var dragging by remember { mutableStateOf(false) }

    Box(modifier = modifier.height(40.dp)) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .align(Alignment.CenterStart)
        ) {
            drawRect(
                color = Color.White.copy(alpha = 0.3f),
                size = Size(size.width * buffered, size.height)
            )
            drawRect(
                color = Color.Cyan,
                size = Size(size.width * progress, size.height)
            )
        }

        Slider(
            value = progress,
            onValueChange = {
                dragging = true
                onSeek(it)
            },
            onValueChangeFinished = { dragging = false },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun LoadingShimmer() {
    Box(modifier = Modifier.fillMaxSize()) {
        ShimmerAnimation(
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Retry", color = Color.White)
        }
    }
}
@Composable
fun ShimmerAnimation(
    modifier: Modifier = Modifier,
    baseColor: Color = Color.DarkGray,
    highlightColor: Color = Color.LightGray,
    durationMillis: Int = 1000
) {
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            )
        )
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            baseColor,
            highlightColor,
            baseColor
        ),
        start = Offset(translateAnim - 0.2f, 0f),
        end = Offset(translateAnim + 0.2f, 0f)
    )

    Box(
        modifier = modifier
            .background(brush)
            .shimmerEffect()
    )
}

private fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val gradient = listOf(
        Color.DarkGray.copy(alpha = 0.3f),
        Color.DarkGray.copy(alpha = 0.5f),
        Color.DarkGray.copy(alpha = 0.3f)
    )

    val transition = rememberInfiniteTransition()
    val startOffsetX by transition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        )
    )

    background(
        brush = Brush.linearGradient(
            colors = gradient,
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + 1f, 1f)
        )
    )
}
fun Long.toFormattedTime(): String {
    return SimpleDateFormat("mm:ss", Locale.getDefault()).format(Date(this))
}

@Composable
fun rememberOrientation(): Orientation {
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        if (configuration.screenWidthDp > configuration.screenHeightDp) {
            Orientation.Horizontal
        } else {
            Orientation.Vertical
        }
    }
}

