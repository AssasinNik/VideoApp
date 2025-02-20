package com.cherenkov.videoapp.videoapp.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SelectedVideoViewModel: ViewModel() {

    private val _selectedVideoId = MutableStateFlow<Int?>(null)
    val selectedVideoId = _selectedVideoId.asStateFlow()

    fun onSelectVideo(id: Int?){
        _selectedVideoId.value = id
    }

}