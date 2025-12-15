package com.example.simplehomeassistant.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configurations")
data class Configuration(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val internalUrl: String,
    val externalUrl: String,
    val apiToken: String, // Single token that works for both URLs (same Home Assistant instance)
    val isActive: Boolean = false,
    // Authentication method selection
    val useTokenAuth: Boolean = true, // true = token, false = username/password
    val username: String? = null,
    val password: String? = null,
    // URL preference: true = use internal URL, false = use external URL
    val preferInternalUrl: Boolean = true,
    // Legacy field - kept for backward compatibility with existing databases
    // New configurations should always use null here
    @Deprecated("No longer used - same token works for both URLs")
    val externalApiToken: String? = null
) {
    // Helper to get the appropriate URL based on preference
    fun getActiveUrl(): String {
        return if (preferInternalUrl) internalUrl else externalUrl
    }
}
