package com.cherenkov.videoapp.videoapp.data.dto.info_video

import kotlinx.serialization.Serializable

@Serializable
data class InfoVideoDTO(
    val duration: Int,
    val height: Int,
    val id: Int,
    val image: String,
    val url: String,
    val user: User,
    val video_files: List<VideoFile>,
    val video_pictures: List<VideoPicture>,
    val width: Int
)