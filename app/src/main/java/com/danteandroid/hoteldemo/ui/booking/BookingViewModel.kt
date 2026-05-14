package com.danteandroid.hoteldemo.ui.booking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danteandroid.hoteldemo.data.model.BookingInsert
import com.danteandroid.hoteldemo.data.model.Room
import com.danteandroid.hoteldemo.data.repository.BookingRepository
import com.danteandroid.hoteldemo.data.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

sealed interface BookingUiState {
    data object Idle : BookingUiState
    data object Loading : BookingUiState
    data class Success(val bookingCode: String) : BookingUiState
    data class Error(val message: String) : BookingUiState
}

class BookingViewModel(private val roomId: String) : ViewModel() {
    private val roomRepository = RoomRepository()
    private val bookingRepository = BookingRepository()

    private val _room = MutableStateFlow<Room?>(null)
    val room = _room.asStateFlow()

    private val _uiState = MutableStateFlow<BookingUiState>(BookingUiState.Idle)
    val uiState = _uiState.asStateFlow()

    var guestName by mutableStateOf("")
    var guestPhone by mutableStateOf("")
    var guestIdNumber by mutableStateOf("")
    var specialRequests by mutableStateOf("")
    var checkInDateMillis by mutableLongStateOf(0L)
    var checkOutDateMillis by mutableLongStateOf(0L)

    init {
        viewModelScope.launch {
            _room.value = runCatching { roomRepository.getRoomById(roomId) }.getOrNull()
        }
    }

    val nights: Int
        get() = if (checkInDateMillis > 0 && checkOutDateMillis > checkInDateMillis)
            ((checkOutDateMillis - checkInDateMillis) / 86_400_000L).toInt()
        else 0

    val totalPrice: Double
        get() = (_room.value?.pricePerNight ?: 0.0) * nights

    fun submitBooking() {
        val room = _room.value ?: return
        if (guestName.isBlank() || guestPhone.isBlank() || guestIdNumber.isBlank()) {
            _uiState.value = BookingUiState.Error("请填写完整的入住人信息")
            return
        }
        if (nights <= 0) {
            _uiState.value = BookingUiState.Error("请选择有效的入住和退房日期")
            return
        }

        viewModelScope.launch {
            _uiState.value = BookingUiState.Loading
            try {
                val booking = bookingRepository.createBooking(
                    BookingInsert(
                        roomId = roomId,
                        roomName = room.name,
                        roomType = room.type,
                        guestName = guestName.trim(),
                        guestPhone = guestPhone.trim(),
                        guestIdNumber = guestIdNumber.trim(),
                        checkInDate = checkInDateMillis.toDateString(),
                        checkOutDate = checkOutDateMillis.toDateString(),
                        totalPrice = totalPrice,
                        specialRequests = specialRequests.takeIf { it.isNotBlank() },
                    )
                )
                _uiState.value = BookingUiState.Success(booking.bookingCode)
            } catch (e: Exception) {
                _uiState.value = BookingUiState.Error(e.message ?: "预订失败，请重试")
            }
        }
    }

    fun clearError() {
        if (_uiState.value is BookingUiState.Error) {
            _uiState.value = BookingUiState.Idle
        }
    }
}

fun Long.toDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return sdf.format(java.util.Date(this))
}

fun Long.toDisplayDate(): String {
    val cal = Calendar.getInstance().apply { timeInMillis = this@toDisplayDate }
    return "%d年%02d月%02d日".format(
        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)
    )
}
