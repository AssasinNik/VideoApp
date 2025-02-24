package com.cherenkov.videoapp.videoapp.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    @Upsert
    suspend fun upsert(video: VideoEntity)

    @Query("SELECT * FROM VideoEntity")
    fun getPopularVideos(): Flow<List<VideoEntity>>


}