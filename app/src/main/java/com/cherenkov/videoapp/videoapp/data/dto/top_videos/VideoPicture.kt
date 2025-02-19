package com.cherenkov.videoapp.videoapp.data.dto.top_videos

import kotlinx.serialization.Serializable

@Serializable
data class VideoPicture(
    val id: Int,
    val nr: Int,
    val picture: String
)