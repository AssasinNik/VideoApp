package com.cherenkov.videoapp.videoapp.data.network

import com.cherenkov.videoapp.core.domain.DataError
import com.cherenkov.videoapp.core.domain.Result
import com.cherenkov.videoapp.videoapp.data.dto.info_video.InfoVideoDTO
import com.cherenkov.videoapp.videoapp.data.dto.searched_videos.SearchedVideosDTO
import com.cherenkov.videoapp.videoapp.data.dto.top_videos.TopVideosDTO

interface RemoteVideoDataSource {
    suspend fun searchVideo(query: String): Result<SearchedVideosDTO, DataError.Remote>

    suspend fun getPopularVideos(): Result<TopVideosDTO, DataError.Remote>

    suspend fun getInfoVideo(id: String): Result<InfoVideoDTO, DataError.Remote>
}