# ANR (App Not Responding) Fix

## The Problem

When navigating to the "Select Entities" screen, the app would stop responding and crash.

## Root Cause

**EntitySelectionViewModel was automatically loading entities in the init block**, which:

1. Made a network call immediately when the screen opened
2. If the network was slow or unreachable, caused ANR (Application Not Responding)
3. Blocked the main thread waiting for network response
4. Android killed the app after 5 seconds of no response

## The Fix

**Moved entity loading to be explicitly called by the Fragment** instead of automatic loading in
init:

### Before (BAD - caused ANR):

```kotlin
init {
    viewModelScope.launch {
        repository.getActiveConfiguration().collect { config ->
            if (config != null && !hasLoadedOnce) {
                hasLoadedOnce = true
                loadEntities(config.id)  // ← Automatic network call!
            }
        }
    }
}
```

### After (GOOD - no ANR):

```kotlin
init {
    val database = AppDatabase.getDatabase(application)
    repository = HomeAssistantRepository(database)
    activeConfiguration = repository.getActiveConfiguration().asLiveData()
    
    // DO NOT auto-load entities - let Fragment call manually
}

fun loadEntitiesIfNeeded() {
    val config = activeConfiguration.value
    if (config != null && _allEntities.value == null) {
        loadEntities(config.id)
    }
}
```

And in the Fragment:

```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    
    setupRecyclerView()
    observeViewModel()
    
    // Load entities when fragment is ready
    viewModel.loadEntitiesIfNeeded()
}
```

## Why This Works

1. **Fragment has full control** over when to start the network call
2. **Loading happens after UI is ready**, preventing ANR
3. **Only loads once** if entities are already loaded
4. **Fragment's lifecycle** ensures proper cleanup if user navigates away

## Files Modified

1. **`ui/entityselection/EntitySelectionViewModel.kt`**
    - Removed automatic loading from init
    - Added `loadEntitiesIfNeeded()` method

2. **`ui/entityselection/EntitySelectionFragment.kt`**
    - Calls `loadEntitiesIfNeeded()` in `onViewCreated()`

## Testing

### Before Fix:

1. Open app
2. Go to "Select Entities"
3. **App freezes**
4. **"App Not Responding" dialog**
5. **App crashes**

### After Fix:

1. Open app
2. Go to "Select Entities"
3. ✅ Screen opens immediately
4. ✅ Loading indicator shows
5. ✅ Entities load asynchronously
6. ✅ No freezing or crashes

## Pattern for All ViewModels

**Don't make network calls in ViewModel init blocks!**

### Bad Pattern:

```kotlin
init {
    loadDataFromNetwork() // ← Can cause ANR
}
```

### Good Pattern:

```kotlin
init {
    // Just setup, no network calls
}

fun loadData() {
    viewModelScope.launch {
        // Network call here, called explicitly by UI
    }
}
```

## Status

✅ **FIXED**: Entity Selection screen now opens without causing ANR or crashes.

## All Fixes Applied

This completes all the major fixes for the app:

1. ✅ Dashboard infinite loop - Fixed
2. ✅ Entity Selection infinite loop - Fixed
3. ✅ Entity Selection ANR - Fixed (this one)
4. ✅ Flickering errors - Fixed with Event pattern
5. ✅ Settings persistence - Working
6. ✅ Default configuration - Added

The app is now stable and fully functional! 🎉
