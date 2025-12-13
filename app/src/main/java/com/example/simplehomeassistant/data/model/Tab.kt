package com.example.simplehomeassistant.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabs")
data class Tab(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val configurationId: Long,
    val name: String,
    val displayOrder: Int
)

@Entity(
    tableName = "entity_tabs",
    primaryKeys = ["tabId", "entityId"]
)
data class EntityTab(
    val tabId: Long,
    val entityId: String
)

data class TabWithEntities(
    val tab: Tab,
    val entityIds: List<String>
)
