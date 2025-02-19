package com.cherenkov.videoapp.videoapp.data.mappers

import com.cherenkov.videoapp.videoapp.data.dto.info_video.InfoVideoDTO
import com.cherenkov.videoapp.videoapp.domain.models.VideoInfo
import com.cherenkov.videoapp.videoapp.domain.models.VideoItem

fun com.cherenkov.videoapp.videoapp.data.dto.top_videos.Video.toVideoItem(): VideoItem {
    return VideoItem(
        id = id,
        thumbnail = image,
        author = user.name,
        quality = video_files[0].quality ?: "HD",
        link = url,
        duration = duration
    )
}

fun com.cherenkov.videoapp.videoapp.data.dto.searched_videos.Video.toVideoItem(): VideoItem {
    return VideoItem(
        id = id,
        thumbnail = image,
        author = user.name,
        quality = video_files[0].quality ?: "HD",
        link = url,
        duration = duration
    )
}
fun InfoVideoDTO.toVideoInfo(): VideoInfo {
    return VideoInfo(
        id = id,
        thumbnail = image,
        author = user.name,
        quality = video_files[0].quality ?: "HD",
        link = url,
        video_link = video_files[0].link ?: "No link"
    )
}

