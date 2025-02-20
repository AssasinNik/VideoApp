package com.cherenkov.videoapp.videoapp.presentation.player


sealed interface PlayerAction {
    data object OnBackClicked: PlayerAction
    data class OnVideoClicked(val id: Int): PlayerAction
    data object OnFullScreenToggle : PlayerAction
}