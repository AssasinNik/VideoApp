package com.cherenkov.videoapp.videoapp.presentation.list_videos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cherenkov.videoapp.core.domain.onError
import com.cherenkov.videoapp.core.domain.onSuccess
import com.cherenkov.videoapp.core.presentation.toUiText
import com.cherenkov.videoapp.videoapp.domain.VideoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VideoListViewModel(
    private val repository: VideoRepository
): ViewModel(){
    private val _state = MutableStateFlow(VideoListState())
    val state = _state
        .onStart {
            searchPopularVideos()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    private var searchJob: Job? = null

    fun onAction(action: VideoListAction){
        when(action){
            is VideoListAction.OnChangedText -> {
                searchVideos(text = action.text)
            }
            is VideoListAction.OnRefreshSwipe -> {
                if (action.query.isNotEmpty()) {
                    searchVideos(text = action.query)
                } else {
                    updatePopularVideos()
                }
            }
            else -> Unit
        }
    }

    private fun searchPopularVideos() = viewModelScope.launch{
        _state.update { it.copy(
            isLoading = true
        ) }
        repository
            .getPopularVideos()
            .onSuccess { result ->
                _state.update { it.copy(
                    isLoading = false,
                    errorMessage = null,
                    topVideos = result
                ) }
            }
            .onError { error ->
                _state.update { it.copy(
                    topVideos = emptyList(),
                    isLoading = false,
                    errorMessage = error.toUiText()
                ) }
            }
    }

    private fun updatePopularVideos() = viewModelScope.launch{
        _state.update { it.copy(
            isLoading = true
        ) }
        repository
            .updateListVideos()
            .onSuccess { result ->
                _state.update { it.copy(
                    isLoading = false,
                    errorMessage = null,
                    topVideos = result
                ) }
            }
            .onError { error ->
                _state.update { it.copy(
                    topVideos = emptyList(),
                    isLoading = false,
                    errorMessage = error.toUiText()
                ) }
            }
    }



    private fun searchVideos(text:String){
        searchJob?.cancel()

        if (text != ""){
            searchJob = viewModelScope.launch {
                _state.update { it.copy(
                    isFinding = true
                ) }
                repository.searchVideo(text)
                    .onSuccess { result ->
                        _state.update { it.copy(
                            isFinding = false,
                            errorMessage = null,
                            searchedVideos = result
                        ) }
                    }
                    .onError { error ->
                        _state.update { it.copy(
                            searchedVideos = emptyList(),
                            isLoading = false,
                            errorMessage = error.toUiText()
                        ) }
                    }
            }
        }
    }
}