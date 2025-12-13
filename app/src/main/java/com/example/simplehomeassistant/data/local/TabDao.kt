package com.example.simplehomeassistant.data.local

import androidx.room.*
import com.example.simplehomeassistant.data.model.EntityTab
import com.example.simplehomeassistant.data.model.Tab
import kotlinx.coroutines.flow.Flow

@Dao
interface TabDao {
    @Query("SELECT * FROM tabs WHERE configurationId = :configId ORDER BY displayOrder ASC")
    fun getTabsForConfig(configId: Long): Flow<List<Tab>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTab(tab: Tab): Long

    @Update
    suspend fun updateTab(tab: Tab)

    @Delete
    suspend fun deleteTab(tab: Tab)

    @Query("DELETE FROM tabs WHERE id = :tabId")
    suspend fun deleteTabById(tabId: Long)

    @Query("SELECT * FROM entity_tabs WHERE tabId = :tabId")
    suspend fun getEntityIdsForTab(tabId: Long): List<EntityTab>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntityTab(entityTab: EntityTab)

    @Query("DELETE FROM entity_tabs WHERE tabId = :tabId AND entityId = :entityId")
    suspend fun deleteEntityFromTab(tabId: Long, entityId: String)

    @Query("DELETE FROM entity_tabs WHERE tabId = :tabId")
    suspend fun deleteAllEntitiesFromTab(tabId: Long)

    @Query("DELETE FROM entity_tabs WHERE entityId = :entityId")
    suspend fun deleteEntityFromAllTabs(entityId: String)
}
