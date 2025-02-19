package com.cherenkov.videoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.cherenkov.videoapp.ui.theme.VideoAppTheme
import com.cherenkov.videoapp.videoapp.presentation.list_videos.VideoListScreen
import com.cherenkov.videoapp.videoapp.presentation.list_videos.VideoListScreenRoot
import com.cherenkov.videoapp.videoapp.presentation.list_videos.VideoListViewModel
import com.cherenkov.videoapp.videoapp.presentation.player.PlayerScreen
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VideoAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel = koinViewModel<VideoListViewModel>()
                    VideoListScreenRoot(
                        viewModel = viewModel,
                        onVideoClick = {  }
                    )
                }
            }
        }
    }
}