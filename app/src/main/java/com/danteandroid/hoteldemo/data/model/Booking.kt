package com.danteandroid.hoteldemo.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Booking(
    val id: String = "",
    @SerialName("booking_code") val bookingCode: String = "",
    @SerialName("room_id") val roomId: String = "",
    @SerialName("room_name") val roomName: String = "",
    @SerialName("room_type") val roomType: String = "",
    @SerialName("guest_name") val guestName: String = "",
    @SerialName("guest_phone") val guestPhone: String = "",
    @SerialName("guest_id_number") val guestIdNumber: String = "",
    @SerialName("check_in_date") val checkInDate: String = "",
    @SerialName("check_out_date") val checkOutDate: String = "",
    val nights: Int? = null,
    @SerialName("total_price") val totalPrice: Double = 0.0,
    val status: String = "confirmed",
    @SerialName("special_requests") val specialRequests: String? = null,
    @SerialName("actual_check_in") val actualCheckIn: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class BookingInsert(
    @SerialName("room_id") val roomId: String,
    @SerialName("room_name") val roomName: String,
    @SerialName("room_type") val roomType: String,
    @SerialName("guest_name") val guestName: String,
    @SerialName("guest_phone") val guestPhone: String,
    @SerialName("guest_id_number") val guestIdNumber: String,
    @SerialName("check_in_date") val checkInDate: String,
    @SerialName("check_out_date") val checkOutDate: String,
    @SerialName("total_price") val totalPrice: Double,
    @SerialName("special_requests") val specialRequests: String? = null,
)

enum class BookingStatus(val value: String, val label: String) {
    PENDING("pending", "待确认"),
    CONFIRMED("confirmed", "已确认"),
    CHECKED_IN("checked_in", "已入住"),
    COMPLETED("completed", "已完成"),
    CANCELLED("cancelled", "已取消");

    companion object {
        fun fromValue(value: String) = entries.find { it.value == value } ?: CONFIRMED
    }
}
