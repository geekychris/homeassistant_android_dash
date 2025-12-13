# Infinite Loop Fix - The Root Cause

## The Problem

When activating a configuration, the app would:

1. Flicker rapidly
2. UI would become unresponsive
3. Crash after a few seconds

## Root Cause: Infinite Loop

### The Flow:

1. User taps "Activate" on a configuration
2. `ConfigurationViewModel.setActiveConfiguration()` is called
3. This calls `repository.setActiveConfiguration(id)`
4. Repository updates database and emits new value through Flow
5. `DashboardViewModel` observes this Flow in `init{}`
6. **BUG**: ViewModel then called `repository.setActiveConfiguration(id)` AGAIN
7. This triggered the Flow again
8. **Loop repeats infinitely**
9. App crashes from too many operations

### The Code That Caused It:

```kotlin
// DashboardViewModel - BEFORE (BAD)
init {
    viewModelScope.launch {
        repository.getActiveConfiguration().collect { config ->
            _activeConfiguration.value = config
            if (config != null && config != previousConfig) {
                repository.setActiveConfiguration(config.id) // ← INFINITE LOOP!
            }
        }
    }
}
```

### Why It Looped:

1. Flow emits config
2. ViewModel calls `setActiveConfiguration`
3. This triggers database update
4. Database update triggers Flow emission
5. Go to step 1 (INFINITE LOOP)

## The Fix

**Simply OBSERVE the configuration - don't try to set it!**

The configuration is already set when the user activates it. The ViewModel should just observe and
display, not react by setting it again.

```kotlin
// DashboardViewModel - AFTER (GOOD)
init {
    val database = AppDatabase.getDatabase(application)
    repository = HomeAssistantRepository(database)

    // Simply observe the active configuration
    // DO NOT call repository.setActiveConfiguration here!
    viewModelScope.launch {
        repository.getActiveConfiguration().collect { config ->
            _activeConfiguration.value = config
            // That's it - just observe, don't react
        }
    }
}
```

## Files Changed

**File**: `ui/dashboard/DashboardViewModel.kt`

**Change**: Removed the call to `repository.setActiveConfiguration(config.id)` from the init block

## Why This Fix Works

1. **User activates configuration** → `ConfigurationViewModel` calls repository
2. **Repository updates database** → Sets isActive flag
3. **Flow emits new configuration** → DashboardViewModel receives it
4. **ViewModel updates LiveData** → UI updates
5. **NO recursive call** → No loop!

## Testing

### Before Fix:

- ✅ Add configuration
- ✅ Tap "Activate"
- ❌ UI flickers rapidly
- ❌ App crashes

### After Fix:

- ✅ Add configuration
- ✅ Tap "Activate"
- ✅ UI updates smoothly
- ✅ No flickering
- ✅ No crash
- ✅ Can tap "Refresh" to load entities
- ✅ Settings persist

## Lesson Learned

**When observing a Flow/LiveData, be careful about calling methods that update the same data you're
observing!**

### Anti-Pattern:

```kotlin
dataSource.observe { value ->
    // DON'T do this if it triggers the same dataSource!
    dataSource.update(value)
}
```

### Good Pattern:

```kotlin
dataSource.observe { value ->
    // Just update UI state
    _uiState.value = value
}
```

## Related Issues Fixed

With this fix, all the following now work:

- ✅ No flickering
- ✅ No crashes
- ✅ Settings persist
- ✅ Error messages show once
- ✅ Manual refresh works
- ✅ Configuration switching works

## Status

**FIXED**: The infinite loop is resolved. The app is now stable when activating configurations.
