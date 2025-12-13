# Flicker-Free Entity Update Optimization

**Date**: December 13, 2025  
**Status**: ✅ **FULLY IMPLEMENTED**

## The Problem

When controlling a device (turning on/off), the UI was flickering because:

- The app did 3 retry attempts to verify state change
- Each attempt called `loadEntities()` which refreshed the **entire list** (291+ entities)
- This caused the RecyclerView to rebind everything multiple times
- Result: Visible flicker/flash as the UI updated 3-4 times

**Logs showed**:

```
00:10:01 - Full refresh (291 entities)
00:10:02 - Full refresh (291 entities) 
00:10:03 - Full refresh (291 entities)
00:10:04 - Full refresh (291 entities)
```

## The Solution

**Smart Single-Entity Update**:
Instead of refreshing the entire list, only update the single entity that changed.

### Implementation

**Before**:

```kotlin
if (stateChanged && reachedExpectedState) {
    loadEntities() // ❌ Full refresh of 291+ entities!
    return
}
```

**After**:

```kotlin
if (stateChanged && reachedExpectedState) {
    currentEntity?.let { updateSingleEntity(it) } // ✅ Update just one entity!
    return
}
```

**New Method**:

```kotlin
private fun updateSingleEntity(updatedEntity: HAEntity) {
    // Update the entity in the list without full refresh
    val currentEntities = _allEntities.value ?: emptyList()
    val updatedList = currentEntities.map { entity ->
        if (entity.entityId == updatedEntity.entityId) {
            updatedEntity  // Replace with updated version
        } else {
            entity  // Keep existing
        }
    }
    _allEntities.value = updatedList
    applyTabFilter()  // Reapply filter with updated list
}
```

## How It Works Now

**Control Flow**:

1. User taps switch → Send command to Home Assistant
2. Wait 300ms → Check state silently
3. **If state changed** → Update just that one entity ✅ **NO FLICKER**
4. **If not changed** → Wait 500ms, check again
5. Repeat up to 3 times
6. Final attempt → Update just that entity

**Key Optimization**:

- Only the **changed entity** is updated
- RecyclerView's DiffUtil detects it's the same list
- Only the single item gets rebound
- All other items stay as-is
- **No visual flicker!**

## Benefits

✅ **No flicker** - Smooth, professional UI updates  
✅ **Fast** - Only one entity processed instead of 291+  
✅ **Efficient** - RecyclerView doesn't rebind unchanged items  
✅ **Stable** - Tab selection preserved  
✅ **Responsive** - Entity updates within 300-1500ms

## Testing

**To verify no flicker**:

1. Open Dashboard with 291 entities
2. Select any custom tab (e.g., "Living Room")
3. Toggle a switch or light
4. **Observe**: Only that switch updates smoothly
5. **Verify**: No full-list flicker
6. **Confirm**: Tab stays selected

## Technical Details

**What Changed**:

- `refreshWithRetry()` now calls `updateSingleEntity()` instead of `loadEntities()`
- Added `updateSingleEntity()` method that updates one entity in-place
- Final fallback check also uses single-entity update
- Only falls back to `loadEntities()` if API call fails

**Performance Impact**:

- Before: ~3-4 full refreshes × 291 entities = ~1164 entity updates
- After: 1 single entity update = **99.9% reduction** in UI updates

## Result

**Flicker-free, buttery-smooth entity control!** 🎉

Users can now toggle switches, lights, and other devices without any visual disruption. The UI
updates feel instant and professional.
