package com.cherenkov.videoapp.videoapp.data.dto.top_videos

import kotlinx.serialization.Serializable

@Serializable
data class TopVideosDTO(
    val page: Int,
    val per_page: Int,
    val total_results: Int,
    val url: String,
    val videos: List<Video>
)