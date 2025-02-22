package com.cherenkov.videoapp.videoapp.presentation.player


sealed interface PlayerAction {
    data object PlayPause: PlayerAction
    data object ToggleControls: PlayerAction
    data class SeekTo(val position: Float): PlayerAction
    data object EnterFullScreen: PlayerAction
    data object ExitFullScreen: PlayerAction
    data object OnBackClicked: PlayerAction
    data class OnVideoClicked(val id: Int): PlayerAction
}