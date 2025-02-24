package com.cherenkov.videoapp.videoapp.domain.models

data class VideoItem(
    val id: Int,
    val title: String,
    val duration: Int,
    val thumbnail: String,
    val author: String,
    val quality: String,
    val link: String
)
