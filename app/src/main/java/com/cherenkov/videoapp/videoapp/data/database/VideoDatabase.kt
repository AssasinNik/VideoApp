package com.cherenkov.videoapp.videoapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter

@Database(
    entities = [VideoEntity::class],
    version = 1
)
abstract class VideoDatabase: RoomDatabase() {
    abstract val videoDao : VideoDao

    companion object{
        const val DB_NAME = "video.db"
    }
}