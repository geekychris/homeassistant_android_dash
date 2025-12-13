package com.example.simplehomeassistant.data.local

import androidx.room.*
import com.example.simplehomeassistant.data.model.Configuration
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigurationDao {

    @Query("SELECT * FROM configurations ORDER BY name ASC")
    fun getAllConfigurations(): Flow<List<Configuration>>

    @Query("SELECT * FROM configurations WHERE id = :id")
    suspend fun getConfigurationById(id: Long): Configuration?

    @Query("SELECT * FROM configurations WHERE isActive = 1 LIMIT 1")
    fun getActiveConfiguration(): Flow<Configuration?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfiguration(configuration: Configuration): Long

    @Update
    suspend fun updateConfiguration(configuration: Configuration)

    @Delete
    suspend fun deleteConfiguration(configuration: Configuration)

    @Query("UPDATE configurations SET isActive = 0")
    suspend fun deactivateAllConfigurations()

    @Query("UPDATE configurations SET isActive = 1 WHERE id = :id")
    suspend fun setActiveConfiguration(id: Long)
}
