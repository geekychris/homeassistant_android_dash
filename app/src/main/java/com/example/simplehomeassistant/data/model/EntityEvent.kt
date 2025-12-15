package com.example.simplehomeassistant.data.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Represents a state change event for an entity
 */
data class EntityEvent(
    @SerializedName("entity_id")
    val entityId: String,

    @SerializedName("state")
    val state: String,

    @SerializedName("last_changed")
    val lastChanged: String,

    @SerializedName("last_updated")
    val lastUpdated: String,

    @SerializedName("attributes")
    val attributes: HAEntityAttributes
) {
    val name: String
        get() = attributes.friendlyName ?: entityId

    fun getFormattedTime(): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ", Locale.US)
            val outputFormat = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(lastChanged.replace("Z", "+0000"))
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            lastChanged
        }
    }

    fun getTimestamp(): Long {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ", Locale.US)
            val date = inputFormat.parse(lastChanged.replace("Z", "+0000"))
            date?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}

/**
 * History response wrapper from Home Assistant
 */
data class EntityHistory(
    val entityId: String,
    val states: List<EntityEvent>
)
