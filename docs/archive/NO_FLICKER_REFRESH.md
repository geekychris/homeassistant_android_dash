# No-Flicker State Refresh Optimization

**Date**: December 12, 2025  
**Status**: ✅ **OPTIMIZED & DEPLOYED**

## Issue

When controlling devices (turning lights on/off, etc.), the UI would flicker/refresh multiple times
as the app checked for state changes up to 3 times. This caused an annoying visual flicker.

## Root Cause

The previous implementation called `loadEntities()` on every retry attempt (up to 3 times):

```kotlin
// OLD CODE (caused flickering):
repeat(3) { attempt ->
    delay(300ms + offset)
    loadEntities()  // ← UI refreshes here, every attempt!
    
    if (currentState == expectedState) {
        return // Found it, stop
    }
}
```

**Problem**: Each `loadEntities()` call updates `_entities` LiveData, which triggers UI updates,
causing the entity list to refresh 3 times even if the state hadn't changed yet.

## Solution

**Silent polling with single UI update** - Only refresh the UI once we detect the state actually
changed:

```kotlin
// NEW CODE (no flickering):
repeat(3) { attempt ->
    delay(300ms + offset)
    
    // Silently fetch state (no UI update)
    val result = repository.fetchAllStates()
    
    if (stateChanged && reachedExpectedState) {
        // Only NOW update UI
        loadEntities()
        return
    }
}

// Final refresh if needed
loadEntities()
```

## How It Works

### Before (Flickering)

```
User toggles light
  ↓
Command sent
  ↓
Wait 300ms → Refresh UI (flicker!)
  ↓
Check state: still "off"
  ↓
Wait 500ms → Refresh UI (flicker!)
  ↓
Check state: now "on" ✓
  ↓
Stop (but UI already refreshed 2x)
```

**Result**: UI flickers twice before showing final state

### After (Smooth)

```
User toggles light
  ↓
Command sent
  ↓
Wait 300ms → Check silently (no UI change)
  ↓
State still "off"
  ↓
Wait 500ms → Check silently (no UI change)
  ↓
State now "on" ✓
  ↓
Refresh UI once → Shows "on"
```

**Result**: UI updates only once, when state actually changes

## Technical Implementation

### Key Changes

**1. Store Original State**

```kotlin
val originalEntity = _entities.value?.values?.flatten()?.find { it.entityId == entityId }
val originalState = originalEntity?.state
```

**2. Silent State Check**

```kotlin
// Fetch state without updating UI
val result = repository.fetchAllStates()

result.fold(
    onSuccess = { allEntities ->
        val currentEntity = allEntities.find { it.entityId == entityId }
        val currentState = currentEntity?.state
        
        // Only update UI if state changed
        if (currentState != originalState && currentState == expectedState) {
            loadEntities() // Single UI update!
            return
        }
    },
    onFailure = { /* Ignore, will retry */ }
)
```

**3. Final Safety Refresh**

```kotlin
// If we never saw state change, refresh once at the end
loadEntities()
```

### Detection Logic

Two conditions must be met for early UI update:

1. **State Changed**: `currentState != originalState`
2. **Reached Expected**: `currentState == expectedState`

This ensures:

- We don't update UI until something actually changed
- We verify the change matches what we expected
- We stop checking once confirmed

## Benefits

### For Users

- ✅ **No flicker** - Smooth, single UI update
- ✅ **Faster perception** - Feels instant
- ✅ **Professional** - Polished experience
- ✅ **Less distracting** - No repeated refreshes

### For Performance

- ✅ **Fewer UI updates** - 1 instead of up to 3
- ✅ **Less work** - RecyclerView updates once
- ✅ **Reduced overhead** - Less binding/rendering
- ✅ **Better battery** - Fewer redraws

## Scenarios

### Fast Server (300ms response)

```
Toggle light
  ↓
Wait 300ms
Check: State changed to "on" ✓
  ↓
Update UI once
  ↓
Done in 300ms with 1 refresh
```

### Medium Server (500ms response)

```
Toggle light
  ↓
Wait 300ms
Check: State still "off"
  ↓
Wait 500ms
Check: State changed to "on" ✓
  ↓
Update UI once
  ↓
Done in 800ms with 1 refresh
```

### Slow Server (1000ms response)

```
Toggle light
  ↓
Wait 300ms
Check: State still "off"
  ↓
Wait 500ms
Check: State still "off"
  ↓
Wait 700ms
Check: State changed to "on" ✓
  ↓
Update UI once
  ↓
Done in 1500ms with 1 refresh
```

### Server Never Updates (error case)

```
Toggle light
  ↓
3 silent checks over 1500ms
  ↓
State never changed
  ↓
Final refresh to show current state
  ↓
Done in 1500ms with 1 refresh
```

## Performance Comparison

### Before Optimization

| Scenario | UI Refreshes | User Experience |
|----------|-------------|-----------------|
| Fast (300ms) | 1 | OK |
| Medium (500ms) | 2 | Flicker |
| Slow (1000ms) | 3 | Multiple flickers |

### After Optimization

| Scenario | UI Refreshes | User Experience |
|----------|-------------|-----------------|
| Fast (300ms) | 1 | Smooth ✓ |
| Medium (500ms) | 1 | Smooth ✓ |
| Slow (1000ms) | 1 | Smooth ✓ |

**Result**: Always 1 refresh, always smooth!

## Network Efficiency

### API Calls

- **Before**: Same (3 attempts max)
- **After**: Same (3 attempts max)
- **No change** in network usage

### UI Updates

- **Before**: 1-3 updates
- **After**: Always 1 update
- **Saved**: Up to 2 unnecessary UI refreshes

## Edge Cases Handled

### Multiple Rapid Commands

- Each command gets its own polling
- Independent state checks
- No interference

### State Doesn't Change

- Final refresh shows current state
- User sees something happened
- Consistent behavior

### Network Errors During Polling

- Errors ignored during silent checks
- Continues polling
- Final refresh attempts full load

### State Changes to Wrong Value

- Detects change but not expected state
- Continues polling
- Shows final state regardless

## Testing

### Test Case 1: Fast State Change

1. Toggle switch
2. **Verify**: UI updates once after ~300ms
3. **Verify**: No flicker

### Test Case 2: Slow State Change

1. Toggle switch
2. Wait 1.5 seconds
3. **Verify**: UI updates once
4. **Verify**: No intermediate flickers

### Test Case 3: Multiple Devices

1. Toggle 3 switches rapidly
2. **Verify**: Each updates once independently
3. **Verify**: No flickers

### Test Case 4: Failed Command

1. Toggle switch (server fails)
2. Wait 1.5 seconds
3. **Verify**: UI still refreshes once (shows original state)
4. **Verify**: No flickers during wait

## Logging

Debug logs help track behavior:

```kotlin
// Success case:
"State changed after 2 attempt(s), updating UI"

// Timeout case:
"State verification completed, final refresh"
```

View logs:

```bash
adb logcat | grep "DashboardViewModel"
```

## Summary

**What Changed**:

- ✅ Silent state polling (no UI updates during checks)
- ✅ Single UI refresh when state confirmed
- ✅ Final refresh as fallback

**Impact**:

- No more flickering
- Smooth, professional UI updates
- Same reliability, better UX

**Result**:

- Users see instant, flicker-free state updates
- App feels faster and more polished
- Better user experience

**Status**: ✅ Deployed and ready to test!

