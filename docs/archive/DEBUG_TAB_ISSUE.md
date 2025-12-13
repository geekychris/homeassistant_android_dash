# Debug Tab Filtering Issue

## The Problem

You created a third custom tab and assigned 4 devices to it, but when viewing that tab on the
Dashboard, no devices appear. However, the Tab Management UI still shows those 4 devices are
assigned.

## Debug Logging Added

I've added comprehensive logging to track:

- Which tab is being filtered
- What custom tabs are available
- How many entities are assigned to the tab
- The actual entity IDs assigned
- How many entities match after filtering

## How to Capture Debug Info

**Step 1: Clear the logs**

```bash
~/Library/Android/sdk/platform-tools/adb logcat -c
```

**Step 2: Reproduce the issue**

1. Open the app
2. Go to Dashboard
3. Select your third custom tab (the one with 4 devices)
4. Note that no devices appear

**Step 3: Capture the logs**

```bash
~/Library/Android/sdk/platform-tools/adb logcat -d | grep "DashboardViewModel" > ~/tab_debug.txt
```

**Step 4: Check the logs**

```bash
cat ~/tab_debug.txt
```

## What to Look For

The logs will show something like:

```
=== Applying tab filter ===
Current tab: 'Your Tab Name'
Available user tabs: [Tab1(ID:1), Tab2(ID:2), Tab3(ID:3)]
Total entities to filter: 291
Selected tab object: Your Tab Name (ID: 3)
Entity IDs assigned to tab: [light.kitchen, switch.bedroom, ...]
Number of assigned entities: 4
Filtered result: 0 entities match  ← THIS IS THE PROBLEM IF IT SHOWS 0
```

## Possible Causes

### 1. Entity ID Mismatch

The entity IDs stored in the database don't match the entity IDs from Home Assistant.

- **Check**: Do the entity IDs in the log match what you see in Home Assistant?
- **Example**: Database has `light.kitchen_light` but HA returns `light.kitchen`

### 2. Entities Not Loaded

The entities might not be in the `allEntities` list yet when filtering happens.

- **Check**: Does "Total entities to filter" show 0 or the full count?

### 3. Tab Not Found

The custom tab isn't being loaded from the database.

- **Check**: Does "Available user tabs" include your third tab?

### 4. Wrong Configuration ID

The entities are assigned to a tab for a different configuration.

- **Check**: Are you using the same configuration when assigning vs viewing?

## Quick Test Commands

**See all tabs in database:**

```bash
~/Library/Android/sdk/platform-tools/adb shell "run-as com.example.simplehomeassistant cat /data/data/com.example.simplehomeassistant/databases/home_assistant_db | strings | grep -A5 'Tab'"
```

**See entity assignments:**

```bash
~/Library/Android/sdk/platform-tools/adb logcat -d | grep -E "Entity IDs assigned|Number of assigned"
```

## Next Steps

After you capture the logs, share what you see and I can pinpoint the exact issue!
