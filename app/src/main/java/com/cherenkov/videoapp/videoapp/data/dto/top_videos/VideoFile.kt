package com.cherenkov.videoapp.videoapp.data.dto.top_videos

import kotlinx.serialization.Serializable

@Serializable
data class VideoFile(
    val file_type: String,
    val fps: Double,
    val height: Int,
    val id: Int,
    val link: String?,
    val quality: String?,
    val size: Int,
    val width: Int
)