package com.cherenkov.videoapp.videoapp.presentation.player

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.cherenkov.videoapp.videoapp.domain.models.VideoItem
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import com.cherenkov.videoapp.videoapp.presentation.list_videos.VideoItemShimmer
import com.cherenkov.videoapp.videoapp.presentation.list_videos.VideoListItem

@Composable
fun PlayerScreenRoot(
    viewModel: PlayerViewModel = koinViewModel(),
    onBackClick: () -> Unit,
    onVideoClick: (Int) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isFullScreen by remember { mutableStateOf(false) }

    if (isFullScreen) {
        FullScreenVideoPlayer(
            videoUrl = state.playedVideo?.video_link ?: "",
            isFullScreen = isFullScreen,
            onFullScreenToggle = { isFullScreen = !isFullScreen },
            onBack = onBackClick
        )
    } else {
        PlayerScreen(
            state = state,
            onAction = { action ->
                when (action) {
                    is PlayerAction.OnBackClicked -> onBackClick()
                    is PlayerAction.OnVideoClicked -> onVideoClick(action.id)
                    is PlayerAction.OnFullScreenToggle -> isFullScreen = true
                }
                viewModel.onAction(action)
            }
        )
    }
}

@Composable
fun PlayerScreen(
    state: PlayerState,
    onAction: (PlayerAction) -> Unit
) {
    val context = LocalContext.current
    val videoUri = state.playedVideo?.video_link ?: ""
    val player = remember(videoUri) {
        ExoPlayer.Builder(context).build().apply {
            if (videoUri.isNotEmpty()) {
                setMediaItem(MediaItem.fromUri(videoUri))
            }
        }
    }
    DisposableEffect(player) {
        onDispose { player.release() }
    }
    val playerView = remember {
        PlayerView(context).apply {
            useController = false
        }
    }
    playerView.player = player
    LaunchedEffect(videoUri) {
        if (videoUri.isNotEmpty()) {
            player.prepare()
            player.playWhenReady = true
        }
    }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    LaunchedEffect(player) {
        while (true) {
            currentPosition = player.currentPosition
            duration = player.duration
            delay(500L)
        }
    }
    var showControls by remember { mutableStateOf(true) }
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(3000L)
            showControls = false
        }
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF121212), Color(0xFF1F1B24))
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .clickable { showControls = true }
                ) {
                    AndroidView(
                        factory = { playerView },
                        modifier = Modifier.fillMaxSize()
                    )
                    this@Column.AnimatedVisibility(
                        visible = showControls,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        VideoPlayerControls(
                            isPlaying = player.isPlaying,
                            currentPosition = currentPosition,
                            duration = duration,
                            onPlayPause = {
                                if (player.isPlaying) player.pause() else player.play()
                            },
                            onSeek = { newPosition -> player.seekTo(newPosition) },
                            onFullScreenToggle = { onAction(PlayerAction.OnFullScreenToggle) },
                            onBack = { onAction(PlayerAction.OnBackClicked) }
                        )
                    }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!state.isLoading) {
                        items(state.topVideos) { video ->
                            VideoListItem(
                                video = video,
                                onVideoClick = { onAction(PlayerAction.OnVideoClicked(video.id)) }
                            )
                        }
                    } else {
                        items(5) { VideoItemShimmer() }
                    }
                }
            }
        }
    }
}


@Composable
fun FullScreenVideoPlayer(
    videoUrl: String,
    isFullScreen: Boolean,
    onFullScreenToggle: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            if (videoUrl.isNotEmpty()) {
                setMediaItem(MediaItem.fromUri(videoUrl))
            }
        }
    }
    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }
    LaunchedEffect(exoPlayer) {
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    LaunchedEffect(exoPlayer) {
        while (true) {
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration
            delay(500L)
        }
    }
    var showControls by remember { mutableStateOf(true) }
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(3000L)
            showControls = false
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { showControls = true }
    ) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        AnimatedVisibility(
            visible = showControls,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            VideoPlayerControls(
                isPlaying = exoPlayer.isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                onPlayPause = {
                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                },
                onSeek = { newPosition ->
                    exoPlayer.seekTo(newPosition)
                },
                onFullScreenToggle = onFullScreenToggle,
                onBack = onBack
            )
        }
    }
}

@Composable
fun VideoPlayerControls(
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onFullScreenToggle: () -> Unit,
    onBack: () -> Unit
) {
    val alphaAnim by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 500)
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x99000000))
            .padding(12.dp)
            .alpha(alphaAnim)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color.White
                )
            }
            IconButton(onClick = onPlayPause) {
                if (isPlaying) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Пауза",
                        tint = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Воспроизвести",
                        tint = Color.White
                    )
                }
            }
            IconButton(onClick = onFullScreenToggle) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Полноэкранный режим",
                    tint = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        val sliderPosition = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
        Slider(
            value = sliderPosition,
            onValueChange = { value ->
                onSeek((value * duration).toLong())
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.Red,
                inactiveTrackColor = Color.Gray
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatTime(currentPosition), color = Color.White)
            Text(text = formatTime(duration), color = Color.White)
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val seconds = (totalSeconds % 60).toInt()
    val minutes = ((totalSeconds / 60) % 60).toInt()
    val hours = (totalSeconds / 3600).toInt()
    return if (hours > 0)
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    else
        String.format("%02d:%02d", minutes, seconds)
}

