package com.danteandroid.hoteldemo.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danteandroid.hoteldemo.data.model.Room
import com.danteandroid.hoteldemo.data.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RoomDetailUiState {
    data object Loading : RoomDetailUiState
    data class Success(val room: Room) : RoomDetailUiState
    data class Error(val message: String) : RoomDetailUiState
}

class RoomDetailViewModel(private val roomId: String) : ViewModel() {
    private val repository = RoomRepository()

    private val _uiState = MutableStateFlow<RoomDetailUiState>(RoomDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadRoom()
    }

    private fun loadRoom() {
        viewModelScope.launch {
            _uiState.value = RoomDetailUiState.Loading
            try {
                val room = repository.getRoomById(roomId)
                _uiState.value = if (room != null) RoomDetailUiState.Success(room)
                else RoomDetailUiState.Error("未找到该房间")
            } catch (e: Exception) {
                _uiState.value = RoomDetailUiState.Error(e.message ?: "加载失败")
            }
        }
    }
}
