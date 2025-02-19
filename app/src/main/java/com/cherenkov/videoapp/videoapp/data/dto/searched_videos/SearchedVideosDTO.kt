package com.cherenkov.videoapp.videoapp.data.dto.searched_videos

import kotlinx.serialization.Serializable

@Serializable
data class SearchedVideosDTO(
    val page: Int,
    val per_page: Int,
    val total_results: Int,
    val url: String,
    val videos: List<Video>
)