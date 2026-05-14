package com.danteandroid.hoteldemo.data.repository

import com.danteandroid.hoteldemo.SupabaseModule
import com.danteandroid.hoteldemo.data.model.Room
import io.github.jan.supabase.postgrest.postgrest

class RoomRepository {
    private val postgrest = SupabaseModule.client.postgrest

    suspend fun getRooms(): List<Room> =
        postgrest["rooms"].select {
            filter { eq("is_available", true) }
        }.decodeList()

    suspend fun getRoomById(id: String): Room? =
        postgrest["rooms"].select {
            filter { eq("id", id) }
            limit(1)
        }.decodeSingleOrNull()
}
