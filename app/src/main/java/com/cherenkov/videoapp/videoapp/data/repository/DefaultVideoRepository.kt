package com.cherenkov.videoapp.videoapp.data.repository

import com.cherenkov.videoapp.core.domain.DataError
import com.cherenkov.videoapp.core.domain.Result
import com.cherenkov.videoapp.core.domain.map
import com.cherenkov.videoapp.videoapp.data.dto.info_video.InfoVideoDTO
import com.cherenkov.videoapp.videoapp.data.dto.searched_videos.SearchedVideosDTO
import com.cherenkov.videoapp.videoapp.data.dto.top_videos.TopVideosDTO
import com.cherenkov.videoapp.videoapp.data.mappers.toVideoInfo
import com.cherenkov.videoapp.videoapp.data.mappers.toVideoItem
import com.cherenkov.videoapp.videoapp.data.network.RemoteVideoDataSource
import com.cherenkov.videoapp.videoapp.domain.VideoRepository
import com.cherenkov.videoapp.videoapp.domain.models.VideoInfo
import com.cherenkov.videoapp.videoapp.domain.models.VideoItem

class DefaultVideoRepository(
    private val remoteVideoDataSource: RemoteVideoDataSource,
): VideoRepository{

    override suspend fun getPopularVideos(): Result<List<VideoItem>, DataError.Remote> {
        return remoteVideoDataSource
            .getPopularVideos()
            .map { dto ->
                dto.videos.map { info ->
                    info.toVideoItem()
                }
            }
    }

    override suspend fun getInfoVideo(id: Int): Result<VideoInfo, DataError.Remote> {
        return remoteVideoDataSource
            .getInfoVideo(id)
            .map { dto ->
                dto.toVideoInfo()
            }
    }

    override suspend fun searchVideo(query: String): Result<List<VideoItem>, DataError.Remote> {
        return remoteVideoDataSource
            .searchVideo(query)
            .map { dto ->
                dto.videos.map { info ->
                    info.toVideoItem()
                }
            }
    }

}