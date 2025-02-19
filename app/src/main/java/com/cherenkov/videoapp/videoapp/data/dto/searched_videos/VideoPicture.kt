package com.cherenkov.videoapp.videoapp.data.dto.searched_videos

import kotlinx.serialization.Serializable

@Serializable
data class VideoPicture(
    val id: Int,
    val nr: Int,
    val picture: String
)