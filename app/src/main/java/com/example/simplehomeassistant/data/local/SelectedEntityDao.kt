package com.example.simplehomeassistant.data.local

import androidx.room.*
import com.example.simplehomeassistant.data.model.SelectedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SelectedEntityDao {

    @Query("SELECT * FROM selected_entities WHERE configurationId = :configId ORDER BY displayOrder ASC")
    fun getSelectedEntitiesForConfig(configId: Long): Flow<List<SelectedEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSelectedEntity(entity: SelectedEntity)

    @Delete
    suspend fun deleteSelectedEntity(entity: SelectedEntity)

    @Query("DELETE FROM selected_entities WHERE configurationId = :configId")
    suspend fun deleteAllForConfiguration(configId: Long)

    @Query("DELETE FROM selected_entities WHERE configurationId = :configId AND entityId = :entityId")
    suspend fun deleteByEntityId(configId: Long, entityId: String)
}
