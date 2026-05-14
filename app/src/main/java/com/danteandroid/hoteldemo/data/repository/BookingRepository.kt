package com.danteandroid.hoteldemo.data.repository

import com.danteandroid.hoteldemo.SupabaseModule
import com.danteandroid.hoteldemo.data.model.Booking
import com.danteandroid.hoteldemo.data.model.BookingInsert
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class BookingRepository {
    private val postgrest = SupabaseModule.client.postgrest

    suspend fun createBooking(booking: BookingInsert): Booking =
        postgrest["bookings"].insert(booking) {
            select()
        }.decodeSingle()

    suspend fun getBookingsByPhone(phone: String): List<Booking> =
        postgrest["bookings"].select {
            filter { eq("guest_phone", phone) }
            order("created_at", Order.DESCENDING)
        }.decodeList()

    suspend fun getBookingByCode(code: String): Booking? =
        postgrest["bookings"].select {
            filter { eq("booking_code", code.uppercase()) }
            limit(1)
        }.decodeSingleOrNull()

    suspend fun checkIn(bookingId: String) {
        postgrest["bookings"].update({ set("status", "checked_in") }) {
            filter { eq("id", bookingId) }
        }
    }
}
