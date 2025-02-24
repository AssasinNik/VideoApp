package com.cherenkov.videoapp.videoapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class VideoEntity(
    @PrimaryKey(autoGenerate = false) val id: Int,
    val duration: Int,
    val title: String,
    val thumbnail: String,
    val author: String,
    val quality: String,
    val link: String
)
