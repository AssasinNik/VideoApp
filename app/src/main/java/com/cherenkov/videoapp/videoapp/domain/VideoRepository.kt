package com.cherenkov.videoapp.videoapp.domain

import com.cherenkov.videoapp.core.domain.DataError
import com.cherenkov.videoapp.core.domain.EmptyResult
import com.cherenkov.videoapp.core.domain.Result
import com.cherenkov.videoapp.videoapp.data.dto.info_video.InfoVideoDTO
import com.cherenkov.videoapp.videoapp.data.dto.searched_videos.SearchedVideosDTO
import com.cherenkov.videoapp.videoapp.data.dto.top_videos.TopVideosDTO
import com.cherenkov.videoapp.videoapp.domain.models.VideoInfo
import com.cherenkov.videoapp.videoapp.domain.models.VideoItem
import kotlinx.coroutines.flow.Flow

interface VideoRepository {

    suspend fun searchVideo(query: String): Result<List<VideoItem>, DataError.Remote>

    suspend fun getPopularVideos(): Result<List<VideoItem>, DataError.Remote>

    suspend fun getInfoVideo(id: Int): Result<VideoInfo, DataError.Remote>

    fun getPopularVideosCache(): Flow<List<VideoItem>>

    suspend fun upsertPopularVideo(video: VideoItem): EmptyResult<DataError.Local>

    suspend fun updateListVideos(): Result<List<VideoItem>, DataError.Remote>
}