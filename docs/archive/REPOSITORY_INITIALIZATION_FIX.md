# Repository Initialization Fix

## The Problem

Even though the default "Belmont" configuration was marked as active in the database (`isActive=1`),
the app showed "No active configuration" error when trying to load entities.

## Root Cause

The **Repository's `currentApi` and `currentConfig` were null** even though an active configuration
existed in the database.

### Why This Happened:

1. Default configuration created in database with `isActive=1`
2. Repository initialized with `currentApi = null` and `currentConfig = null`
3. ViewModels observe the active configuration from database
4. But Repository never loaded it into memory
5. When trying to fetch entities: `currentApi` was null → "No active configuration" error

## The Fix

**Call `repository.setActiveConfiguration()` before fetching entities**

This ensures the Repository loads the configuration from database and initializes the API client.

### DashboardViewModel - Before:

```kotlin
val config = _activeConfiguration.value
if (config == null) {
    _error.value = Event("No active configuration")
    return@launch
}

// Get all states from Home Assistant
val result = repository.fetchAllStates()  // ← currentApi is null!
```

### DashboardViewModel - After:

```kotlin
val config = _activeConfiguration.value
if (config == null) {
    _error.value = Event("No active configuration")
    return@launch
}

// Ensure repository knows about the active configuration
repository.setActiveConfiguration(config.id)  // ← Initialize API client!

// Get all states from Home Assistant
val result = repository.fetchAllStates()  // ← Now currentApi is set!
```

### EntitySelectionViewModel - Before:

```kotlin
private fun loadEntities(configId: Long) {
    viewModelScope.launch {
        _isLoading.value = true
        
        // Fetch all entities from Home Assistant
        val result = repository.fetchAllStates()  // ← currentApi is null!
    }
}
```

### EntitySelectionViewModel - After:

```kotlin
private fun loadEntities(configId: Long) {
    viewModelScope.launch {
        _isLoading.value = true
        
        // Ensure repository knows about the active configuration
        repository.setActiveConfiguration(configId)  // ← Initialize API client!
        
        // Fetch all entities from Home Assistant
        val result = repository.fetchAllStates()  // ← Now currentApi is set!
    }
}
```

## What `setActiveConfiguration` Does

```kotlin
suspend fun setActiveConfiguration(id: Long) {
    configDao.deactivateAllConfigurations()
    configDao.setActiveConfiguration(id)
    currentConfig = configDao.getConfigurationById(id)  // ← Load from DB
    currentConfig?.let { config ->
        currentApi = RetrofitClient.createApi(config.internalUrl)  // ← Create API client!
    }
}
```

It:

1. Loads the configuration from database
2. Creates the Retrofit API client with the correct URL
3. Stores both in memory for future use

## Files Modified

1. **`ui/dashboard/DashboardViewModel.kt`**
    - Added `repository.setActiveConfiguration(config.id)` before fetching

2. **`ui/entityselection/EntitySelectionViewModel.kt`**
    - Added `repository.setActiveConfiguration(configId)` before fetching

## Why This Wasn't an Issue Before

In the ConfigurationFragment, when you manually activate a configuration:

```kotlin
onActivate = { configuration ->
    viewModel.setActiveConfiguration(configuration)  // ← This initializes repository!
}
```

But with the **default configuration**, it's already active in the database, so no one calls
`setActiveConfiguration()` to initialize the Repository!

## Testing

### Before Fix:

1. Open app (default config exists and is active)
2. Go to Dashboard
3. Tap "Refresh"
4. ❌ Error: "No active configuration"
5. Go to "Select Entities"
6. ❌ Error: "No active configuration"

### After Fix:

1. Open app (default config exists and is active)
2. Go to Dashboard
3. Tap "Refresh"
4. ✅ Repository initializes
5. ✅ Connects to Home Assistant
6. ✅ Entities load successfully
7. Go to "Select Entities"
8. ✅ Repository already initialized
9. ✅ Entities load successfully

## Pattern to Follow

**Always call `setActiveConfiguration()` before making API calls**

This ensures the Repository is properly initialized, regardless of how the configuration became
active (manual activation vs. pre-existing in database).

## Status

✅ **FIXED**: Both Dashboard and Entity Selection now work with the default configuration.

## All Fixes Complete

The app now has ALL issues resolved:

1. ✅ Dashboard infinite loop
2. ✅ Entity Selection infinite loop
3. ✅ Entity Selection ANR
4. ✅ Flickering errors
5. ✅ Settings persistence
6. ✅ Default configuration
7. ✅ Repository initialization (this fix)

**The app is now fully functional!** 🎉
