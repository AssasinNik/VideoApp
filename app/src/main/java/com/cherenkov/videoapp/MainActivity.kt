package com.cherenkov.videoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.cherenkov.videoapp.ui.theme.VideoAppTheme
import com.cherenkov.videoapp.videoapp.presentation.SelectedVideoViewModel
import com.cherenkov.videoapp.videoapp.presentation.list_videos.VideoListScreen
import com.cherenkov.videoapp.videoapp.presentation.list_videos.VideoListScreenRoot
import com.cherenkov.videoapp.videoapp.presentation.list_videos.VideoListViewModel
import com.cherenkov.videoapp.videoapp.presentation.player.PlayerAction
import com.cherenkov.videoapp.videoapp.presentation.player.PlayerScreenRoot
import com.cherenkov.videoapp.videoapp.presentation.player.PlayerViewModel
import com.cherenkov.videoapp.videoapp.utils.Route
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
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Route.VideoGraph
                    ){
                        navigation<Route.VideoGraph>(
                            startDestination = Route.VideoListScreen
                        ){
                            composable<Route.VideoListScreen>(
                                exitTransition = { slideOutHorizontally { initialOffset ->
                                    initialOffset
                                } },
                                popEnterTransition = {
                                    slideInHorizontally { initialOffset ->
                                        initialOffset
                                    }
                                }
                            ) {
                                val viewModel = koinViewModel<VideoListViewModel>()
                                val selectedVideoViewModel =
                                    it.sharedKoinViewModel<SelectedVideoViewModel>(navController)
                                LaunchedEffect(true) {
                                    selectedVideoViewModel.onSelectVideo(null)
                                }
                                VideoListScreenRoot(
                                    viewModel = viewModel,
                                    onVideoClick = { video ->
                                        selectedVideoViewModel.onSelectVideo(video)
                                        navController.navigate(
                                            Route.PlayerScreen(video)
                                        )
                                    }
                                )
                            }
                            composable<Route.PlayerScreen>(
                                enterTransition = {
                                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up)
                                },
                                exitTransition = {
                                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down)
                                }
                            ) {
                                val viewModel = koinViewModel<PlayerViewModel>()
                                val selectedVideoViewModel =
                                    it.sharedKoinViewModel<SelectedVideoViewModel>(navController)
                                val selectedVideo by selectedVideoViewModel.selectedVideoId.collectAsStateWithLifecycle()
                                LaunchedEffect(true) {
                                    selectedVideo?.let {
                                        viewModel.onAction(PlayerAction.OnVideoClicked(it))
                                    }
                                }
                                PlayerScreenRoot(
                                    viewModel = viewModel,
                                    onBackClick = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private inline fun <reified T: ViewModel> NavBackStackEntry.sharedKoinViewModel(
    navController: NavController
): T{
    val navGraphRoute = destination.parent?.route?:return koinViewModel<T>()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return koinViewModel(
        viewModelStoreOwner = parentEntry
    )
}