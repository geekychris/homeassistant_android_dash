package com.example.simplehomeassistant.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "selected_entities")
data class SelectedEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val configurationId: Long,
    val entityId: String,
    val displayOrder: Int = 0
)
