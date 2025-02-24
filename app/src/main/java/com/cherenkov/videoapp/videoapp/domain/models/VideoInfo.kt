package com.cherenkov.videoapp.videoapp.domain.models

data class VideoInfo(
    val id: Int,
    val thumbnail: String,
    val duration: String,
    val author: String,
    val quality: String,
    val video_link: String,
    val link: String,
    val title: String
)
