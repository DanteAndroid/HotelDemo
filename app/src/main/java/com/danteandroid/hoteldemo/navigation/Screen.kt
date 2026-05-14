package com.danteandroid.hoteldemo.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Orders : Screen("orders")
    data object CheckIn : Screen("checkin?code={code}") {
        fun createRoute(code: String? = null): String =
            if (code.isNullOrBlank()) "checkin" else "checkin?code=$code"
    }

    data object RoomDetail : Screen("room_detail/{roomId}") {
        fun createRoute(roomId: String) = "room_detail/$roomId"
    }

    data object Booking : Screen("booking/{roomId}") {
        fun createRoute(roomId: String) = "booking/$roomId"
    }

    data object BookingSuccess : Screen("booking_success/{bookingCode}") {
        fun createRoute(code: String) = "booking_success/$code"
    }
}

val bottomNavScreens = listOf(Screen.Home, Screen.Orders, Screen.CheckIn)
