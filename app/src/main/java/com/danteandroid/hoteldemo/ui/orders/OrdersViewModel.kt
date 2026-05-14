package com.danteandroid.hoteldemo.ui.orders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danteandroid.hoteldemo.data.model.Booking
import com.danteandroid.hoteldemo.data.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface OrdersUiState {
    data object Initial : OrdersUiState
    data object Loading : OrdersUiState
    data class Success(val bookings: List<Booking>) : OrdersUiState
    data class Error(val message: String) : OrdersUiState
}

enum class SearchMode { BY_PHONE, BY_CODE }

class OrdersViewModel : ViewModel() {
    private val repository = BookingRepository()

    private val _uiState = MutableStateFlow<OrdersUiState>(OrdersUiState.Initial)
    val uiState = _uiState.asStateFlow()

    var searchQuery by mutableStateOf("")
    var searchMode by mutableStateOf(SearchMode.BY_PHONE)

    fun search() {
        if (searchQuery.isBlank()) return
        viewModelScope.launch {
            _uiState.value = OrdersUiState.Loading
            try {
                val bookings = when (searchMode) {
                    SearchMode.BY_PHONE -> repository.getBookingsByPhone(searchQuery.trim())
                    SearchMode.BY_CODE -> {
                        val b = repository.getBookingByCode(searchQuery.trim())
                        if (b != null) listOf(b) else emptyList()
                    }
                }
                _uiState.value = if (bookings.isEmpty()) OrdersUiState.Error("未找到相关订单")
                else OrdersUiState.Success(bookings)
            } catch (e: Exception) {
                _uiState.value = OrdersUiState.Error(e.message ?: "查询失败")
            }
        }
    }
}
