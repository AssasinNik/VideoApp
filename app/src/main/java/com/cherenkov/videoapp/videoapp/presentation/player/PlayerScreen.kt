package com.cherenkov.videoapp.videoapp.presentation.player

import android.content.Context
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.AndroidExternalSurface
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

private const val VIDEO_URL = "https://videos.pexels.com/video-files/3923315/3923315-uhd_2560_1440_30fps.mp4"


@Composable
fun ModernVideoApp() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFFF1744),
            secondary = Color(0xFFBB86FC),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onPrimary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PlayerScreen()
        }
    }
}

@Composable
fun PlayerScreen() {
    val context = LocalContext.current
    val exoPlayer = rememberExoPlayer(context)

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    YouTubeStylePlayer(exoPlayer = exoPlayer)
}

@Composable
private fun rememberExoPlayer(context: Context): ExoPlayer {
    return remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = ExoPlayer.REPEAT_MODE_ALL
            playWhenReady = true
            setMediaItem(MediaItem.fromUri(VIDEO_URL))
            prepare()
        }
    }
}

@Composable
fun YouTubeStylePlayer(exoPlayer: ExoPlayer) {
    var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }
    var showControls by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0f) }
    var totalDuration by remember { mutableStateOf(0L) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onEvents(player: Player, events: Player.Events) {
                totalDuration = player.duration.coerceAtLeast(0L)
                progress = if (totalDuration > 0) {
                    player.currentPosition.toFloat() / totalDuration
                } else 0f
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clickable { showControls = !showControls }
        ) {
            VideoSurface(exoPlayer = exoPlayer)

            androidx.compose.animation.AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(
                    animationSpec = tween(300),
                    expandFrom = Alignment.Bottom
                ),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(
                    animationSpec = tween(300),
                    shrinkTowards = Alignment.Bottom
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                PlayerControls(
                    exoPlayer = exoPlayer,
                    isPlaying = isPlaying,
                    progress = progress,
                    totalDuration = totalDuration,
                    onPlayPause = { exoPlayer.playWhenReady = !exoPlayer.playWhenReady },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.3f))
                )
            }
        }

        VideoInfoSection(
            title = "Великолепное видео о природе",
            author = "Канал Любителей Природы",
            subscribers = "1.2M подписчиков"
        )
    }
}

@Composable
private fun VideoInfoSection(title: String, author: String, subscribers: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Subscriptions,
                contentDescription = "Канал",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = author,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = subscribers,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            var isSubscribed by remember { mutableStateOf(false) }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSubscribed) Color.Gray else MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { isSubscribed = !isSubscribed }
            ) {
                Text(
                    text = if (isSubscribed) "Подписаны" else "Подписаться",
                    color = if (isSubscribed) MaterialTheme.colorScheme.onSurface else Color.White,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PlayerControls(
    exoPlayer: ExoPlayer,
    isPlaying: Boolean,
    progress: Float,
    totalDuration: Long,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Верхняя панель управления
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* Обработка возврата */ }) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            IconButton(onClick = { /* Обработка меню */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Меню",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Центральные элементы управления воспроизведением
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { /* Предыдущее видео */ },
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Предыдущее",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(24.dp))
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Пауза" else "Воспроизведение",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.width(24.dp))
            IconButton(
                onClick = { /* Следующее видео */ },
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Следующее",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Индикатор прогресса видео
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Slider(
                value = progress,
                onValueChange = { newValue ->
                    exoPlayer.seekTo((newValue * totalDuration).toLong())
                },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun VideoSurface(exoPlayer: ExoPlayer) {
    AndroidExternalSurface(
        modifier = Modifier.fillMaxSize(),
        onInit = {
            onSurface { surface, _, _ ->
                exoPlayer.setVideoSurface(surface)
                surface.onDestroyed { exoPlayer.setVideoSurface(null) }
            }
        }
    )
}
