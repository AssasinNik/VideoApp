package com.cherenkov.videoapp.videoapp.data.network

import com.cherenkov.videoapp.core.data.safeCall
import com.cherenkov.videoapp.core.domain.DataError
import com.cherenkov.videoapp.core.domain.Result
import com.cherenkov.videoapp.videoapp.data.dto.info_video.InfoVideoDTO
import com.cherenkov.videoapp.videoapp.data.dto.searched_videos.SearchedVideosDTO
import com.cherenkov.videoapp.videoapp.data.dto.top_videos.TopVideosDTO
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.parameters

private const val BASE_URL = "https://api.pexels.com"

class KtorRemoteVideoDataSource(
    private val httpClient: HttpClient
) : RemoteVideoDataSource {
    override suspend fun getInfoVideo(id: Int): Result<InfoVideoDTO, DataError.Remote> {
        return safeCall{
            httpClient.get(
                urlString = "$BASE_URL/videos/videos/${id}"
            )
        }
    }

    override suspend fun getPopularVideos(): Result<TopVideosDTO, DataError.Remote> {
        return safeCall{
            httpClient.get(
                urlString = "$BASE_URL/v1/videos/popular"
            ){
                parameter("page", 1)
                parameter("per_page", 10)
            }
        }
    }

    override suspend fun searchVideo(query: String): Result<SearchedVideosDTO, DataError.Remote> {
        return safeCall{
            httpClient.get(
                urlString = "$BASE_URL/v1/videos/search"
            ){
                parameter("page", 1)
                parameter("per_page", 10)
                parameter("query", query)
            }
        }
    }
}