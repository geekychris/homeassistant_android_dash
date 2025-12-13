# Debugging Entity Selection Issue

## Problem

User reports that entities selected on the "Select Entities" page don't appear on the Dashboard.

## Debug Logging Added

### EntitySelectionViewModel

```kotlin
fun toggleEntitySelection(entityId: String) {
    // Logs:
    // - "Adding entity: X to config Y"  OR
    // - "Removing entity: X from config Y"
    // - "Current selected count: N"
}
```

### DashboardViewModel

```kotlin
fun loadEntities() {
    // Logs:
    // - "Total entities from HA: N"
    // - "Selected entity count: N"
    // - "Selected entity IDs: [list]"
    // - "No selections - showing all controllable" OR
    // - "Filtering to selected entities only"
    // - "Filtered entities count: N"
}
```

## Testing Steps

To diagnose the issue, please follow these steps in the emulator:

### Step 1: Select Entities

1. Open the app → Go to "Select Entities" tab
2. Search for an entity (e.g., "light")
3. **Check 2-3 entities** (tap the checkboxes)
4. Watch the logs for: `Adding entity: light.xxx to config 1`

### Step 2: Check Dashboard

1. Go to "Dashboard" tab
2. **Tap "Refresh"** button
3. Watch the logs for:
    - `Selected entity count: 3` (or however many you selected)
    - `Selected entity IDs: [light.xxx, light.yyy, ...]`
    - `Filtering to selected entities only`
    - `Filtered entities count: 3`

### Step 3: Monitor Logs

Run this command in a terminal to see real-time logs:

```bash
export PATH=$PATH:$HOME/Library/Android/sdk/platform-tools
adb logcat | grep -E "EntitySelection|DashboardViewModel"
```

## Expected Behavior

**When selecting entities:**

```
EntitySelection: Adding entity: light.kitchen to config 1
EntitySelection: Current selected count: 1
EntitySelection: Adding entity: switch.garage to config 1
EntitySelection: Current selected count: 2
```

**When refreshing dashboard:**

```
DashboardViewModel: Total entities from HA: 50
DashboardViewModel: Selected entity count: 2
DashboardViewModel: Selected entity IDs: [light.kitchen, switch.garage]
DashboardViewModel: Filtering to selected entities only
DashboardViewModel: Filtered entities count: 2
```

## Possible Issues

### Issue 1: Selections Not Saving

**Symptom**: Dashboard logs show `Selected entity count: 0`

**Cause**: Database not saving selections

- Check Room database is initialized
- Check DAO methods are correct
- Verify configurationId matches

**Solution**: Check the repository's `addSelectedEntity` method

### Issue 2: Dashboard Not Loading Selections

**Symptom**: `Selected entity count: 0` even though you checked boxes

**Cause**: Dashboard querying wrong configuration ID

**Solution**: Verify active configuration ID matches

### Issue 3: Filtered List Empty

**Symptom**: `Filtered entities count: 0` but selected count is positive

**Cause**: Entity IDs don't match between selection and Home Assistant

**Solution**: Check entity ID format in logs

### Issue 4: No Refresh

**Symptom**: Dashboard shows old data

**Cause**: User didn't tap "Refresh" button after selecting

**Solution**: Always tap Refresh after changing selections

## Code Flow

```
┌─────────────────────────────┐
│  Select Entities Tab        │
│                             │
│  User checks checkbox       │
└──────────────┬──────────────┘
               │
               ▼
┌──────────────────────────────┐
│  toggleEntitySelection()     │
│                              │
│  repository.addSelected...() │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│  Room Database               │
│  INSERT SelectedEntity       │
│  (configId, entityId, order) │
└──────────────────────────────┘

... User switches to Dashboard ...

┌─────────────────────────────┐
│  Dashboard Tab              │
│                             │
│  User taps "Refresh"        │
└──────────────┬──────────────┘
               │
               ▼
┌──────────────────────────────┐
│  loadEntities()              │
│                              │
│  1. Fetch all from HA        │
│  2. Query selected from DB   │
│  3. Filter to selected only  │
│  4. Display on UI            │
└──────────────────────────────┘
```

## App is Running with Debug Logs

The app has been rebuilt and installed with debug logging enabled.

**Please try the workflow again:**

1. Select Entities → Check some boxes
2. Dashboard → Tap Refresh
3. Report what you see

**I can then check the logs to see exactly what's happening at each step.**
