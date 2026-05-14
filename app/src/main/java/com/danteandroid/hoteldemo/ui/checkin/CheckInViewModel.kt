package com.danteandroid.hoteldemo.ui.checkin

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

sealed interface CheckInUiState {
    data object Initial : CheckInUiState
    data object Searching : CheckInUiState
    data class Found(val booking: Booking) : CheckInUiState
    data object ProcessingCheckIn : CheckInUiState
    data class CheckedIn(val booking: Booking) : CheckInUiState
    data class NotFound(val message: String) : CheckInUiState
    data class Error(val message: String) : CheckInUiState
}

class CheckInViewModel : ViewModel() {
    private val repository = BookingRepository()

    private val _uiState = MutableStateFlow<CheckInUiState>(CheckInUiState.Initial)
    val uiState = _uiState.asStateFlow()

    var bookingCode by mutableStateOf("")

    fun searchBooking() {
        if (bookingCode.isBlank()) return
        viewModelScope.launch {
            _uiState.value = CheckInUiState.Searching
            try {
                val booking = repository.getBookingByCode(bookingCode.trim())
                if (booking == null) {
                    _uiState.value = CheckInUiState.NotFound("未找到订单号为「${bookingCode.uppercase()}」的订单")
                    return@launch
                }
                _uiState.value = when (booking.status) {
                    "confirmed", "pending" -> CheckInUiState.Found(booking)
                    "checked_in" -> CheckInUiState.NotFound("该订单已办理入住")
                    "completed" -> CheckInUiState.NotFound("该订单已完成")
                    "cancelled" -> CheckInUiState.NotFound("该订单已取消，无法办理入住")
                    else -> CheckInUiState.Found(booking)
                }
            } catch (e: Exception) {
                _uiState.value = CheckInUiState.Error(e.message ?: "查询失败")
            }
        }
    }

    fun confirmCheckIn() {
        val current = _uiState.value as? CheckInUiState.Found ?: return
        viewModelScope.launch {
            _uiState.value = CheckInUiState.ProcessingCheckIn
            try {
                repository.checkIn(current.booking.id)
                _uiState.value = CheckInUiState.CheckedIn(current.booking.copy(status = "checked_in"))
            } catch (e: Exception) {
                _uiState.value = CheckInUiState.Error(e.message ?: "办理入住失败，请重试")
            }
        }
    }

    fun reset() {
        _uiState.value = CheckInUiState.Initial
        bookingCode = ""
    }
}
