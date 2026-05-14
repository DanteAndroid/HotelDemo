package com.danteandroid.hoteldemo.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Room(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val description: String = "",
    @SerialName("price_per_night") val pricePerNight: Double = 0.0,
    val capacity: Int = 2,
    @SerialName("image_url") val imageUrl: String? = null,
    val floor: Int? = null,
    @SerialName("size_sqm") val sizeSqm: Int? = null,
    val amenities: List<String> = emptyList(),
    @SerialName("is_available") val isAvailable: Boolean = true,
)

val ROOM_TYPES = listOf("全部", "标准间", "豪华间", "套房", "总统套房")
