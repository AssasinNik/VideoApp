package com.cherenkov.videoapp.videoapp.presentation.list_videos

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.cherenkov.videoapp.videoapp.domain.models.VideoItem
import com.cherenkov.videoapp.videoapp.presentation.reusable_components.ErrorMessage
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.koin.androidx.compose.koinViewModel


@Composable
fun VideoListScreenRoot(
    viewModel: VideoListViewModel = koinViewModel(),
    onVideoClick: (Int) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    VideoListScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is VideoListAction.OnVideoClicked -> onVideoClick(action.id)
                else -> Unit
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
fun VideoListScreen(
    state: VideoListState,
    onAction: (VideoListAction) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
            if (isLandscape) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(300.dp)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Text(
                            text = "Pexels Видео",
                            style = MaterialTheme.typography.titleLarge.copy(color = Color.White)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Найди лучшее видео",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        SearchBar(
                            query = searchQuery,
                            onQueryChanged = {
                                searchQuery = it
                                onAction(VideoListAction.OnChangedText(it))
                            },
                            searchActive = true,
                            onFocusChanged = {  },
                            onBackClicked = { searchQuery = "" },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    SwipeRefresh(
                        state = swipeRefreshState,
                        onRefresh = {
                            onAction(VideoListAction.OnRefreshSwipe(searchQuery))
                        },
                        modifier = Modifier.fillMaxSize()
                    ){
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .padding(paddingValues)
                                .fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (searchQuery.isNotEmpty()) {
                                if (state.isFinding) {
                                    items(5) { VideoItemShimmer() }
                                } else if (state.searchedVideos.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Ничего не найдено",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = Color.LightGray
                                            )
                                        }
                                    }
                                } else {
                                    items(state.searchedVideos) { video ->
                                        VideoListItem(
                                            video = video,
                                            onVideoClick = { onAction(VideoListAction.OnVideoClicked(it)) }
                                        )
                                    }
                                }
                            } else {
                                if (state.isLoading) {
                                    items(5) { VideoItemShimmer() }
                                } else {
                                    items(state.topVideos) { video ->
                                        VideoListItem(
                                            video = video,
                                            onVideoClick = { onAction(VideoListAction.OnVideoClicked(it)) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    CustomHeader(
                        query = searchQuery,
                        onQueryChanged = {
                            searchQuery = it
                            onAction(VideoListAction.OnChangedText(it))
                        },
                        onBackClicked = { searchQuery = "" },
                        onFocusChanged = {  }
                    )
                    SwipeRefresh(
                        state = swipeRefreshState,
                        onRefresh = {
                            onAction(VideoListAction.OnRefreshSwipe(searchQuery))
                        },
                        modifier = Modifier.fillMaxSize()
                    ){
                        LazyColumn(
                            modifier = Modifier
                                .padding(paddingValues)
                                .fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (searchQuery.isNotEmpty()) {
                                if (state.isFinding) {
                                    items(5) { VideoItemShimmer() }
                                } else if (state.searchedVideos.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 40.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Ничего не найдено",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = Color.LightGray
                                            )
                                        }
                                    }
                                } else {
                                    items(state.searchedVideos) { video ->
                                        VideoListItem(
                                            video = video,
                                            onVideoClick = { onAction(VideoListAction.OnVideoClicked(it)) }
                                        )
                                    }
                                }
                            } else {
                                if (state.isLoading) {
                                    items(5) { VideoItemShimmer() }
                                } else {
                                    items(state.topVideos) { video ->
                                        VideoListItem(
                                            video = video,
                                            onVideoClick = { onAction(VideoListAction.OnVideoClicked(it)) }
                                        )
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    }
                }
            }
            state.errorMessage?.let { uiText ->
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Spacer(Modifier.weight(1f))
                    ErrorMessage(
                        message = uiText.asString(),
                        modifier = Modifier
                            .systemBarsPadding()
                    )
                }
            }
        }
    }
}

@Composable
fun CustomHeader(
    query: String,
    onQueryChanged: (String) -> Unit,
    onBackClicked: () -> Unit,
    onFocusChanged: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Pexels Видео",
                style = MaterialTheme.typography.titleLarge.copy(color = Color.White)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Найди лучшее видео прямо сейчас!",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray)
            )
            Spacer(modifier = Modifier.height(16.dp))
            SearchBar(
                query = query,
                onQueryChanged = onQueryChanged,
                searchActive = true,
                onFocusChanged = onFocusChanged,
                onBackClicked = onBackClicked,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    searchActive: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = {
            Text(
                text = "Поиск видео",
                color = Color.LightGray
            )
        },
        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
        leadingIcon = {
            if (query.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color.LightGray,
                    modifier = Modifier.clickable {
                        focusManager.clearFocus()
                        onBackClicked()
                    }
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Поиск",
                    tint = Color.LightGray
                )
            }
        },
        singleLine = true,
        modifier = modifier.onFocusChanged { focusState ->
            onFocusChanged(focusState.isFocused)
        },
        shape = RoundedCornerShape(20.dp),
    )
}

@Composable
fun VideoListItem(
    video: VideoItem,
    onVideoClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onVideoClick(video.id) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(video.thumbnail)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(rememberShimmerBrush())
                        )
                    },
                    contentDescription = "Обложка видео",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xAA000000))
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.5f),
                                    Color.White.copy(alpha = 0.1f)
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = video.duration.toString() + " sec",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(
                    text = video.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 2
                )
            }
        }
    }
}


@Composable
fun VideoItemShimmer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(rememberShimmerBrush())
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(rememberShimmerBrush())
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(rememberShimmerBrush())
                )
            }
        }
    }
}

@Composable
fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition()
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 1200)
        )
    )
    return Brush.linearGradient(
        colors = listOf(
            Color.DarkGray.copy(alpha = 0.6f),
            Color.Gray.copy(alpha = 0.2f),
            Color.DarkGray.copy(alpha = 0.6f)
        ),
        start = Offset(translateAnim.value, translateAnim.value),
        end = Offset(translateAnim.value + 300f, translateAnim.value + 300f)
    )
}
