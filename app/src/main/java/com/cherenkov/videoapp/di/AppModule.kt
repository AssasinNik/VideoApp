package com.cherenkov.videoapp.di

import androidx.room.Room
import com.cherenkov.videoapp.core.data.HttpClientFactory
import com.cherenkov.videoapp.videoapp.data.database.VideoDatabase
import com.cherenkov.videoapp.videoapp.data.network.KtorRemoteVideoDataSource
import com.cherenkov.videoapp.videoapp.data.network.RemoteVideoDataSource
import com.cherenkov.videoapp.videoapp.data.repository.DefaultVideoRepository
import com.cherenkov.videoapp.videoapp.domain.VideoRepository
import com.cherenkov.videoapp.videoapp.presentation.SelectedVideoViewModel
import com.cherenkov.videoapp.videoapp.presentation.list_videos.VideoListViewModel
import com.cherenkov.videoapp.videoapp.presentation.player.PlayerViewModel
import io.ktor.client.engine.cio.CIO
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single {
        HttpClientFactory.create(CIO.create())
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            VideoDatabase::class.java,
            VideoDatabase.DB_NAME
        ).build()
    }

    single { get<VideoDatabase>().videoDao }
    singleOf(::KtorRemoteVideoDataSource).bind<RemoteVideoDataSource>()
    singleOf(::DefaultVideoRepository).bind<VideoRepository>()

    viewModelOf(::VideoListViewModel)
    viewModelOf(::PlayerViewModel)
    viewModelOf(::SelectedVideoViewModel)
}