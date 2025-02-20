package com.cherenkov.videoapp.videoapp.utils

import kotlinx.serialization.Serializable

sealed interface Route {


    @Serializable
    data object VideoGraph: Route

    @Serializable
    data object VideoListScreen: Route

    @Serializable
    data class PlayerScreen(val id: Int): Route

}