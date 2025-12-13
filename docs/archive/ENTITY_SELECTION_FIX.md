# Entity Selection Infinite Loop Fix

## The Problem

**Symptom**: Entity Selection page loads entities successfully, then starts flickering and crashes

## Root Cause

**Another infinite loop!** The same pattern as DashboardViewModel existed in
EntitySelectionViewModel.

### The Code That Caused It:

```kotlin
// EntitySelectionViewModel - BEFORE (BAD)
init {
    viewModelScope.launch {
        repository.getActiveConfiguration().collect { config ->
            if (config != null) {
                repository.setActiveConfiguration(config.id) // ← INFINITE LOOP!
                loadEntities(config.id)
            }
        }
    }
}
```

### Why It Looped:

1. Fragment opens → ViewModel init runs
2. Flow emits current configuration
3. ViewModel calls `setActiveConfiguration(id)`
4. This triggers database update
5. Database update emits new Flow value
6. Go to step 3 → **INFINITE LOOP**
7. Each loop calls `loadEntities()` → network requests pile up
8. UI flickers as entities keep reloading
9. Too many operations → app crashes

## The Fix

**Only load entities ONCE when configuration is first available**

```kotlin
// EntitySelectionViewModel - AFTER (GOOD)
private var hasLoadedOnce = false

init {
    val database = AppDatabase.getDatabase(application)
    repository = HomeAssistantRepository(database)
    activeConfiguration = repository.getActiveConfiguration().asLiveData()

    // Load entities once when configuration is available
    // DO NOT call setActiveConfiguration - it causes infinite loop!
    viewModelScope.launch {
        repository.getActiveConfiguration().collect { config ->
            if (config != null && !hasLoadedOnce) {
                hasLoadedOnce = true
                loadEntities(config.id)
            }
        }
    }
}
```

## Additional Improvements

### Also Applied Event Pattern

Updated error handling to use `Event<String>` to prevent repeated error messages:

```kotlin
private val _error = MutableLiveData<Event<String>>()
val error: LiveData<Event<String>> = _error
```

And in the Fragment:

```kotlin
viewModel.error.observe(viewLifecycleOwner) { event ->
    event.getContentIfNotHandled()?.let { errorMessage ->
        Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
    }
}
```

## Files Modified

1. **`ui/entityselection/EntitySelectionViewModel.kt`**
    - Removed `repository.setActiveConfiguration()` call
    - Added `hasLoadedOnce` flag
    - Changed error to use `Event<String>`
    - Removed `clearError()` method

2. **`ui/entityselection/EntitySelectionFragment.kt`**
    - Updated error observer to handle `Event`
    - Removed `clearError()` call

## Pattern Identified

**This was the SECOND occurrence of the same infinite loop pattern!**

### The Anti-Pattern:

```kotlin
// DON'T DO THIS - causes infinite loop
flow.collect { value ->
    updateSource(value) // This triggers the flow again!
}
```

### The Correct Pattern:

```kotlin
// DO THIS - just observe
flow.collect { value ->
    _state.value = value // Just update UI state
}
```

## All ViewModels Now Fixed

- ✅ **DashboardViewModel** - Fixed in previous iteration
- ✅ **EntitySelectionViewModel** - Fixed now
- ✅ **ConfigurationViewModel** - No loop (doesn't observe config changes)

## Testing

### Before Fix:

1. Open app
2. Go to "Select Entities"
3. Entities load...
4. **UI starts flickering rapidly**
5. **App crashes**

### After Fix:

1. Open app
2. Go to "Select Entities"
3. Entities load once
4. ✅ **No flickering**
5. ✅ **No crash**
6. ✅ Can check/uncheck entities smoothly
7. ✅ Selections persist

## Status

**FIXED**: Entity Selection page now works correctly without flickering or crashes.

## Lessons Learned

1. **Be extremely careful with Flow.collect() + database updates**
2. **Never call methods that update the same Flow you're observing**
3. **Use flags (like `hasLoadedOnce`) to prevent repeated operations**
4. **Apply the Event pattern for one-time UI events (errors, messages)**

## All Known Infinite Loops Now Fixed

This was the last infinite loop in the codebase. The app should now be fully stable.
