package com.cherenkov.videoapp.videoapp.data.dto.info_video

import kotlinx.serialization.Serializable

@Serializable
data class VideoPicture(
    val id: Int,
    val nr: Int,
    val picture: String
)