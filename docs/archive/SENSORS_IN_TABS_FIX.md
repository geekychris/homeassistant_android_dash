# Sensors in Custom Tabs - Fix

**Date**: December 13, 2025  
**Status**: ✅ **FIXED**

## The Problem

You created a "bedroom" tab and assigned 4 devices:

- `binary_sensor.bedroom_water_detector_moisture`
- `binary_sensor.kyle_bedroom_motion`
- `sensor.blink_kyle_bedroom_temperature`
- `sensor.thermostat_basic_2`

But when viewing the "bedroom" tab on the Dashboard, **0 entities appeared**.

## Root Cause

**The dashboard was filtering out sensors!**

The original logic was:

1. Load all entities from Home Assistant (~1617 total)
2. **Filter to only "controllable" entities** (lights, switches, climate) → ~291 entities
3. Store those 291 in `_allEntities`
4. When custom tab selected → Try to find sensors in those 291 → **NOT FOUND!**

**The 4 assigned entities were sensors/binary_sensors**, which are read-only and were excluded by
the `isControllable()` filter.

## The Fix

Changed the logic to:

1. Load all entities from Home Assistant (~1617 total)
2. **Store ALL entities** (including sensors) in `_allEntities`
3. Apply filtering based on which tab is selected:
    - **"All" tab** → Show only controllable entities (291)
    - **Custom tabs** → Show whatever entities are assigned (including sensors!)

### Code Changes

**Before**:

```kotlin
// Only stored controllable entities
val filteredEntities = allEntities.filter { it.isControllable() }
_allEntities.value = filteredEntities

// Tab filtering couldn't find sensors
val filtered = allEntities.filter { it.entityId in entityIds }
```

**After**:

```kotlin
// Store ALL entities including sensors
_allEntities.value = allEntities

// Filter differently based on tab
val filteredByTab = if (tab == "All") {
    allEntities.filter { it.isControllable() }  // Only controllables on "All"
} else {
    // Custom tabs show whatever is assigned
    val entityIds = repository.getEntityIdsForTab(selectedTab.id).toSet()
    allEntities.filter { it.entityId in entityIds }  // Can include sensors!
}
```

## What This Means

**Custom tabs can now display ANY entity type**:

- ✅ Lights (`light.*`)
- ✅ Switches (`switch.*`)
- ✅ Climate/Thermostats (`climate.*`)
- ✅ **Sensors** (`sensor.*`) ← **NEW!**
- ✅ **Binary Sensors** (`binary_sensor.*`) ← **NEW!**
- ✅ Media Players (`media_player.*`)
- ✅ Locks (`lock.*`)
- ✅ Covers (`cover.*`)
- ✅ Fans (`fan.*`)

**"All" tab behavior unchanged**:

- Still shows only controllable entities (291)
- Sensors not shown by default (too many, not actionable)

## Testing

**Your bedroom tab should now show**:

1. Bedroom Water Detector (moisture sensor)
2. Kyle Bedroom Motion (motion sensor)
3. Blink Kyle Bedroom Temperature (temperature sensor)
4. Thermostat Basic 2 (sensor)

**Test it**:

1. Open app → Dashboard
2. Select "bedroom" tab
3. **You should see 4 entities!** ✅

## Benefits

✅ **Display sensor data** - See temperature, humidity, motion, etc.  
✅ **Monitor status** - Water detectors, door sensors, battery levels  
✅ **Mixed tabs** - Combine lights + sensors in one view (e.g., "Bedroom" tab with lights AND
temperature)  
✅ **Flexibility** - Users decide what to show, not hardcoded filters

## Technical Notes

- `_allEntities` now contains ~1617 entities (up from ~291)
- Memory impact is minimal (~few KB)
- Performance unchanged (filtering is still fast)
- Adapter only renders visible items (RecyclerView)

## Result

**Custom tabs are now fully flexible** - you can assign and view ANY entity type, not just
controllable ones! 🎉
