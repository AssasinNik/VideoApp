package com.cherenkov.videoapp.videoapp.data.mappers

import android.annotation.SuppressLint
import android.util.Log
import com.cherenkov.videoapp.videoapp.data.database.VideoEntity
import com.cherenkov.videoapp.videoapp.data.dto.info_video.InfoVideoDTO
import com.cherenkov.videoapp.videoapp.domain.models.VideoInfo
import com.cherenkov.videoapp.videoapp.domain.models.VideoItem

@SuppressLint("DefaultLocale")
fun convertSecondsToMMSS(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

fun formatVideoTitle(url: String): String {
    val segments = url.split('/')
    val videoIndex = segments.indexOf("video")

    if (videoIndex == -1 || videoIndex >= segments.lastIndex) {
        return ""
    }

    val titleSegment = segments[videoIndex + 1]

    return titleSegment
        .replace(Regex("-\\d+$"), "")
        .replace("-", " ")
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
        }
}

fun com.cherenkov.videoapp.videoapp.data.dto.top_videos.Video.toVideoItem(): VideoItem {
    return VideoItem(
        id = id,
        thumbnail = image,
        author = user.name,
        quality = video_files[0].quality ?: "HD",
        link = url,
        duration = duration,
        title = formatVideoTitle(url)
    )
}

fun com.cherenkov.videoapp.videoapp.data.dto.searched_videos.Video.toVideoItem(): VideoItem {
    return VideoItem(
        id = id,
        thumbnail = image,
        author = user.name,
        quality = video_files[0].quality ?: "HD",
        link = url,
        duration = duration,
        title = formatVideoTitle(url)
    )
}
fun InfoVideoDTO.toVideoInfo(): VideoInfo {
    return VideoInfo(
        id = id,
        title = formatVideoTitle(url),
        thumbnail = image,
        author = user.name,
        quality = video_files[0].quality ?: "HD",
        link = url,
        video_link = video_files[0].link ?: "No link",
        duration = convertSecondsToMMSS(duration)
    )
}

fun VideoEntity.toVideoItem(): VideoItem{
    return VideoItem(
        id = id,
        title = title,
        duration = duration,
        thumbnail = thumbnail,
        author = author,
        quality = quality,
        link = link
    )
}

fun VideoItem.toVideoEntity(): VideoEntity{
    return VideoEntity(
        id = id,
        title = title,
        duration = duration,
        thumbnail = thumbnail,
        author = author,
        quality = quality,
        link = link
    )
}

