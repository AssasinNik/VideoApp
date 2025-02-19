package com.cherenkov.videoapp.videoapp.domain

import com.cherenkov.videoapp.core.domain.DataError
import com.cherenkov.videoapp.core.domain.Result
import com.cherenkov.videoapp.videoapp.data.dto.info_video.InfoVideoDTO
import com.cherenkov.videoapp.videoapp.data.dto.searched_videos.SearchedVideosDTO
import com.cherenkov.videoapp.videoapp.data.dto.top_videos.TopVideosDTO
import com.cherenkov.videoapp.videoapp.domain.models.VideoInfo
import com.cherenkov.videoapp.videoapp.domain.models.VideoItem

interface VideoRepository {

    suspend fun searchVideo(query: String): Result<List<VideoItem>, DataError.Remote>

    suspend fun getPopularVideos(): Result<List<VideoItem>, DataError.Remote>

    suspend fun getInfoVideo(id: String): Result<VideoInfo, DataError.Remote>
}