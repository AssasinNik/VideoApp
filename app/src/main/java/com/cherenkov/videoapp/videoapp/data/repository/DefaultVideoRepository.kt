package com.cherenkov.videoapp.videoapp.data.repository

import androidx.sqlite.SQLiteException
import com.cherenkov.videoapp.core.domain.DataError
import com.cherenkov.videoapp.core.domain.EmptyResult
import com.cherenkov.videoapp.core.domain.Result
import com.cherenkov.videoapp.core.domain.map
import com.cherenkov.videoapp.videoapp.data.database.VideoDao
import com.cherenkov.videoapp.videoapp.data.dto.info_video.InfoVideoDTO
import com.cherenkov.videoapp.videoapp.data.dto.searched_videos.SearchedVideosDTO
import com.cherenkov.videoapp.videoapp.data.dto.top_videos.TopVideosDTO
import com.cherenkov.videoapp.videoapp.data.mappers.toVideoEntity
import com.cherenkov.videoapp.videoapp.data.mappers.toVideoInfo
import com.cherenkov.videoapp.videoapp.data.mappers.toVideoItem
import com.cherenkov.videoapp.videoapp.data.network.RemoteVideoDataSource
import com.cherenkov.videoapp.videoapp.domain.VideoRepository
import com.cherenkov.videoapp.videoapp.domain.models.VideoInfo
import com.cherenkov.videoapp.videoapp.domain.models.VideoItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DefaultVideoRepository(
    private val remoteVideoDataSource: RemoteVideoDataSource,
    private val videoDao: VideoDao
): VideoRepository{

    override suspend fun getPopularVideos(): Result<List<VideoItem>, DataError.Remote> {
        val cachedVideos = videoDao.getPopularVideos().first()
        return if (cachedVideos.isNotEmpty()) {
            Result.Success(cachedVideos.map { it.toVideoItem() })
        } else {
            remoteVideoDataSource.getPopularVideos().map { dto ->
                val videoItems = dto.videos.map { it.toVideoItem() }
                for (video in videoItems) {
                    upsertPopularVideo(video)
                }
                videoItems
            }
        }
    }

    override suspend fun updateListVideos(): Result<List<VideoItem>, DataError.Remote> {
        return remoteVideoDataSource
            .getPopularVideos()
            .map { dto ->
                val videoItems = dto.videos.map { it.toVideoItem() }
                for (video in videoItems) {
                    upsertPopularVideo(video)
                }
                videoItems
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

    override fun getPopularVideosCache(): Flow<List<VideoItem>> {
        return videoDao
            .getPopularVideos()
            .map { videoEntities ->
                videoEntities.map{it.toVideoItem()}
            }
    }

    override suspend fun upsertPopularVideo(video: VideoItem): EmptyResult<DataError.Local> {
        return try {
            videoDao.upsert(video.toVideoEntity())
            Result.Success(Unit)
        } catch (e: SQLiteException){
            Result.Error(DataError.Local.DISK_FULL)
        }
    }

}