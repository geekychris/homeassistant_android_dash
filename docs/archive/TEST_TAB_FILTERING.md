# Test Tab Filtering - Updated Instructions

## Enhanced Debug Logging Added

I've added logs with `>>>>>>>` markers that will DEFINITELY show up. These track:

1. When tabs are loaded from database
2. What tabs are available
3. When you select a tab
4. The complete filtering process

## Testing Steps

**Step 1: Clear logs and start fresh**

```bash
~/Library/Android/sdk/platform-tools/adb logcat -c
```

**Step 2: Open app and go to Dashboard**

- Open the app
- Tap "Dashboard" in bottom navigation
- Wait for entities to load

**Step 3: Select your third tab**

- Tap on your third custom tab (the one with 4 devices)
- Note whether devices appear or not

**Step 4: Capture ALL relevant logs**

```bash
~/Library/Android/sdk/platform-tools/adb logcat -d | grep ">>>>>>>" 
```

This will show you:

- What tabs were loaded
- When you selected the tab
- What happened during filtering

**Step 5: Also check the filter output**

```bash
~/Library/Android/sdk/platform-tools/adb logcat -d | grep "Applying tab filter"
```

## What You Should See

### Example Output (when working):

```
>>>>>>> Loaded user tabs for config 1: [Kitchen(ID:1), Bedroom(ID:2), Living Room(ID:3)]
>>>>>>> Tab list to display: [All, Kitchen, Bedroom, Living Room]
>>>>>>> selectTab called with: 'Living Room'
=== Applying tab filter ===
Current tab: 'Living Room'
Available user tabs: [Kitchen(ID:1), Bedroom(ID:2), Living Room(ID:3)]
Entity IDs assigned to tab: [light.living_room, switch.living_room]
Number of assigned entities: 2
Filtered result: 2 entities match
```

### Example Output (when broken):

```
>>>>>>> Loaded user tabs for config 1: []
>>>>>>> Tab list to display: [All]
(No selectTab calls appear - tab selection isn't working)
```

OR

```
>>>>>>> selectTab called with: 'My Tab'
=== Applying tab filter ===
Entity IDs assigned to tab: []
Filtered result: 0 entities match
```

## Quick Diagnostics

**Are tabs even loading?**

```bash
~/Library/Android/sdk/platform-tools/adb logcat -d | grep "Loaded user tabs"
```

**Is tab selection working?**

```bash
~/Library/Android/sdk/platform-tools/adb logcat -d | grep "selectTab called"
```

**Are entities assigned?**

```bash
~/Library/Android/sdk/platform-tools/adb logcat -d | grep "Entity IDs assigned"
```

## Share Results

After running the tests, share the output of:

```bash
~/Library/Android/sdk/platform-tools/adb logcat -d | grep ">>>>>>>" 
```

This will tell me exactly where the issue is!
