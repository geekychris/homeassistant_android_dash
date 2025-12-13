# Architecture Documentation

## Project Structure

```
app/src/main/java/com/example/simplehomeassistant/
├── data/
│   ├── local/              # Room database
│   │   ├── AppDatabase.kt
│   │   ├── ConfigurationDao.kt
│   │   └── SelectedEntityDao.kt
│   ├── model/              # Data models
│   │   ├── HAEntity.kt
│   │   ├── Configuration.kt
│   │   └── SelectedEntity.kt
│   ├── remote/             # API layer
│   │   ├── HomeAssistantApi.kt
│   │   └── RetrofitClient.kt
│   └── repository/         # Repository pattern
│       └── HomeAssistantRepository.kt
└── ui/
    ├── dashboard/          # Main dashboard
    │   ├── DashboardFragment.kt
    │   ├── DashboardViewModel.kt
    │   └── EntityAdapter.kt
    ├── config/             # Configuration management
    │   ├── ConfigurationFragment.kt
    │   ├── ConfigurationViewModel.kt
    │   └── ConfigurationAdapter.kt
    └── entityselection/    # Entity picker
        ├── EntitySelectionFragment.kt
        ├── EntitySelectionViewModel.kt
        └── EntitySelectionAdapter.kt
```

## Architecture Pattern: MVVM

### Model

- Data classes representing entities, configurations
- Room database entities
- API response models

### View

- Fragments for each screen
- XML layouts
- RecyclerView adapters

### ViewModel

- Business logic
- LiveData for UI state
- Coroutines for async operations

## Data Flow

```
┌─────────────┐
│  Fragment   │
│   (View)    │
└──────┬──────┘
       │ observes LiveData
       │
┌──────▼──────────┐
│   ViewModel     │
│ (Business Logic)│
└──────┬──────────┘
       │ calls
       │
┌──────▼──────────┐
│   Repository    │
│ (Data Manager)  │
└─────┬─────┬─────┘
      │     │
      │     └──────────┐
┌─────▼───┐      ┌─────▼────┐
│ Room DB │      │ REST API │
│ (Local) │      │ (Remote) │
└─────────┘      └──────────┘
```

## Database Schema

### Configuration Table

```sql
CREATE TABLE configurations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    internalUrl TEXT NOT NULL,
    externalUrl TEXT NOT NULL,
    apiToken TEXT NOT NULL,
    isActive INTEGER NOT NULL DEFAULT 0
);
```

### SelectedEntity Table

```sql
CREATE TABLE selected_entities (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    configurationId INTEGER NOT NULL,
    entityId TEXT NOT NULL,
    displayOrder INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY(configurationId) REFERENCES configurations(id)
);
```

## Home Assistant REST API

### Authentication

All API calls include the header:

```
Authorization: Bearer YOUR_LONG_LIVED_TOKEN
```

### Endpoints Used

#### Get All States

```
GET /api/states
Response: List<HAEntity>
```

#### Get Single Entity State

```
GET /api/states/{entity_id}
Response: HAEntity
```

#### Turn On Entity

```
POST /api/services/{domain}/turn_on
Body: { "entity_id": "light.kitchen" }
Response: List<HAEntity>
```

#### Turn Off Entity

```
POST /api/services/{domain}/turn_off
Body: { "entity_id": "switch.living_room" }
Response: List<HAEntity>
```

#### Toggle Entity

```
POST /api/services/{domain}/toggle
Body: { "entity_id": "switch.bedroom" }
Response: List<HAEntity>
```

#### Set Light Attributes

```
POST /api/services/light/turn_on
Body: {
  "entity_id": "light.kitchen",
  "brightness": 255
}
Response: List<HAEntity>
```

#### Set Thermostat Temperature

```
POST /api/services/climate/set_temperature
Body: {
  "entity_id": "climate.main",
  "temperature": 22.5
}
Response: List<HAEntity>
```

#### Set Thermostat Mode

```
POST /api/services/climate/set_hvac_mode
Body: {
  "entity_id": "climate.main",
  "hvac_mode": "heat"
}
Response: List<HAEntity>
```

## Data Models

### HAEntity

```kotlin
data class HAEntity(
    val entityId: String,           // "light.kitchen"
    val state: String,              // "on", "off", "23.5", etc.
    val attributes: HAEntityAttributes,
    val lastChanged: String,
    val lastUpdated: String
)
```

### HAEntityAttributes

```kotlin
data class HAEntityAttributes(
    val friendlyName: String?,      // "Kitchen Light"
    val area: String?,              // "Kitchen" (room)
    
    // Light specific
    val brightness: Int?,           // 0-255
    val rgbColor: List<Int>?,       // [255, 0, 0] for red
    val colorTemp: Int?,            // Color temperature
    
    // Climate specific
    val temperature: Float?,        // Target temperature
    val currentTemperature: Float?, // Current reading
    val hvacMode: String?,          // "heat", "cool", "auto", "off"
    val hvacModes: List<String>?,   // Available modes
    
    // Sensor specific
    val unitOfMeasurement: String?, // "°C", "°F", "kWh", etc.
    val deviceClass: String?,       // "temperature", "humidity", etc.
    
    // Generic
    val icon: String?,              // "mdi:lightbulb"
    val supportedFeatures: Int?     // Bitmask of features
)
```

### Configuration

```kotlin
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
```

### SelectedEntity

```kotlin
@Entity(tableName = "selected_entities")
data class SelectedEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val configurationId: Long,
    val entityId: String,
    val displayOrder: Int = 0
)
```

## Key Classes

### HomeAssistantRepository

Central data management class that:

- Manages multiple Home Assistant API instances
- Handles configuration switching
- Provides coroutine-based async operations
- Manages local database operations
- Switches between internal/external URLs

```kotlin
class HomeAssistantRepository(private val database: AppDatabase) {
    suspend fun fetchAllStates(): Result<List<HAEntity>>
    suspend fun turnOnEntity(entity: HAEntity): Result<Unit>
    suspend fun setActiveConfiguration(id: Long)
    suspend fun switchToExternalUrl()
    // ... more methods
}
```

### EntityAdapter

RecyclerView adapter with multiple ViewHolders:

- `SwitchViewHolder` - For switches
- `LightViewHolder` - For lights with brightness
- `ClimateViewHolder` - For thermostats
- `SensorViewHolder` - For sensors
- `GenericViewHolder` - For other entities

Uses `getItemViewType()` to determine which ViewHolder to use based on entity domain.

## Threading Model

### Coroutines Usage

- All network calls: `suspend` functions in Repository
- Database operations: `suspend` functions in DAOs
- ViewModels: Use `viewModelScope.launch`
- UI updates: Automatic via LiveData observation

### Flow vs LiveData

- **Flow**: Used for Room database queries (reactive)
- **LiveData**: Used for UI state in ViewModels (lifecycle-aware)

Example:

```kotlin
// In DAO (Flow)
@Query("SELECT * FROM configurations")
fun getAllConfigurations(): Flow<List<Configuration>>

// In ViewModel (convert to LiveData)
val configurations = repository.getAllConfigurations().asLiveData()
```

## Error Handling

### Repository Layer

Uses Kotlin's `Result` type:

```kotlin
suspend fun fetchAllStates(): Result<List<HAEntity>> {
    return try {
        // API call
        Result.success(entities)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### ViewModel Layer

Handles Results and updates LiveData:

```kotlin
result.fold(
    onSuccess = { data -> 
        _entities.value = data 
    },
    onFailure = { error -> 
        _error.value = error.message 
    }
)
```

### UI Layer

Observes error LiveData and displays Snackbar:

```kotlin
viewModel.error.observe(viewLifecycleOwner) { error ->
    error?.let {
        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
    }
}
```

## Network Configuration

### RetrofitClient

Singleton object that creates Retrofit instances:

- OkHttp logging interceptor for debugging
- 30-second timeouts
- Gson converter for JSON
- Dynamic base URL per configuration

```kotlin
fun createApi(baseUrl: String): HomeAssistantApi {
    val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(createOkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    return retrofit.create(HomeAssistantApi::class.java)
}
```

## Future Enhancements

### Planned Features

1. **Room-based grouping in dashboard** - Group entities by room with headers
2. **Custom entity ordering** - Drag and drop reordering
3. **Widgets** - Home screen widgets for quick access
4. **Automation triggers** - Run automations from the app
5. **Scene support** - Activate scenes
6. **History graphs** - Show sensor history
7. **Notifications** - Push notifications for events
8. **Themes** - Dark/light theme support
9. **Backup/Restore** - Export/import configurations

### Potential Improvements

- Add caching layer for offline support
- Implement WebSocket for real-time updates
- Add biometric authentication for API tokens
- Support for custom components
- Multi-language support
- Tablet-optimized layouts

## Testing Strategy

### Unit Tests

- Repository methods
- ViewModel logic
- Data transformations

### Integration Tests

- Database operations
- API calls with mock server

### UI Tests

- Fragment navigation
- User interactions
- Data binding

## Dependencies

### Core

- AndroidX libraries
- Material Components 3
- Navigation Component

### Networking

- Retrofit 2.9.0
- OkHttp 4.12.0
- Gson 2.10.1

### Database

- Room 2.6.1

### Async

- Kotlin Coroutines 1.7.3

### Build

- KSP (Kotlin Symbol Processing) for Room
- Kotlin 2.0.21
- Gradle 8.13.2
