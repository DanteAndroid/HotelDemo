package com.danteandroid.hoteldemo.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danteandroid.hoteldemo.data.model.Room
import com.danteandroid.hoteldemo.data.model.ROOM_TYPES
import com.danteandroid.hoteldemo.data.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val rooms: List<Room>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel : ViewModel() {
    private val repository = RoomRepository()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var allRooms: List<Room> = emptyList()
    var selectedType by mutableStateOf(ROOM_TYPES[0])
        private set

    init {
        loadRooms()
    }

    fun loadRooms() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                allRooms = repository.getRooms()
                applyFilter()
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "加载房间列表失败")
            }
        }
    }

    fun selectType(type: String) {
        selectedType = type
        applyFilter()
    }

    private fun applyFilter() {
        val filtered = if (selectedType == ROOM_TYPES[0]) allRooms
        else allRooms.filter { it.type == selectedType }
        _uiState.value = HomeUiState.Success(filtered)
    }
}
