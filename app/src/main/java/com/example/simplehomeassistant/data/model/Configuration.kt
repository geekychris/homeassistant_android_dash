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
    val apiToken: String,
    val isActive: Boolean = false
)
