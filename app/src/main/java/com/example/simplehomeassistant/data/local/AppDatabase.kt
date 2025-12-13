package com.example.simplehomeassistant.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.simplehomeassistant.data.model.Configuration
import com.example.simplehomeassistant.data.model.SelectedEntity
import com.example.simplehomeassistant.data.model.Tab
import com.example.simplehomeassistant.data.model.EntityTab
import org.json.JSONObject
import java.io.InputStream

@Database(
    entities = [Configuration::class, SelectedEntity::class, Tab::class, EntityTab::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun configurationDao(): ConfigurationDao
    abstract fun selectedEntityDao(): SelectedEntityDao
    abstract fun tabDao(): TabDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private data class DefaultConfig(
            val name: String,
            val internalUrl: String,
            val externalUrl: String,
            val apiToken: String,
            val isActive: Boolean
        )

        private fun loadDefaultConfig(context: Context): DefaultConfig {
            val inputStream: InputStream = context.assets.open("default_config.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(jsonString)
            return DefaultConfig(
                name = json.getString("name"),
                internalUrl = json.getString("internalUrl"),
                externalUrl = json.getString("externalUrl"),
                apiToken = json.getString("apiToken"),
                isActive = json.getBoolean("isActive")
            )
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "home_assistant_database"
                )
                    .fallbackToDestructiveMigration() // For development - will recreate DB on schema changes
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Insert default configuration from JSON file
                            try {
                                val config = loadDefaultConfig(context.applicationContext)
                                db.execSQL(
                                    """
                                INSERT INTO configurations (name, internalUrl, externalUrl, apiToken, isActive)
                                VALUES (?, ?, ?, ?, ?)
                                """,
                                    arrayOf(
                                        config.name,
                                        config.internalUrl,
                                        config.externalUrl,
                                        config.apiToken,
                                        if (config.isActive) 1 else 0
                                    )
                                )
                            } catch (e: Exception) {
                                android.util.Log.e(
                                    "AppDatabase",
                                    "Failed to load default config: ${e.message}"
                                )
                                // If config file doesn't exist, app will start with no default config
                                // User can add one manually through the Configurations screen
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
