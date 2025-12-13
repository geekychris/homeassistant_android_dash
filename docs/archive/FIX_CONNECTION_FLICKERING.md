# Fix for Connection Flickering Issue

## Problem Description

When entering a hostname that cannot be resolved on Android (or any unreachable URL) and activating
the configuration, the app would flicker. This was caused by:

1. **Automatic Connection Attempts**: The app automatically tried to connect to Home Assistant
   immediately upon configuration activation
2. **Repeated Failed Attempts**: When the connection failed, the ViewModel would repeatedly try to
   reconnect
3. **Long Timeouts**: 30-second timeouts meant the app would hang for extended periods
4. **No User Feedback**: Users weren't aware that the connection was optional or failing

## Changes Made

### 1. Prevented Automatic Loading on Configuration Change

**File**: `DashboardViewModel.kt`

**Before**:

```kotlin
init {
    viewModelScope.launch {
        repository.getActiveConfiguration().collect { config ->
            _activeConfiguration.value = config
            if (config != null) {
                repository.setActiveConfiguration(config.id)
                loadEntities()  // ← Automatic load!
            }
        }
    }
}
```

**After**:

```kotlin
private var isInitialLoad = true

init {
    viewModelScope.launch {
        repository.getActiveConfiguration().collect { config ->
            _activeConfiguration.value = config
            if (config != null && isInitialLoad) {
                isInitialLoad = false
                repository.setActiveConfiguration(config.id)
                // Don't auto-load on init - user must manually refresh
                // This prevents connection attempts to unreachable servers on startup
            }
        }
    }
}
```

**Impact**:

- ✅ No automatic connection attempts when configuration is activated
- ✅ User must explicitly tap "Refresh" to load entities
- ✅ Prevents flickering from repeated failed connection attempts

### 2. Reduced Connection Timeouts

**File**: `RetrofitClient.kt`

**Before**:

```kotlin
.connectTimeout(30, TimeUnit.SECONDS)
.readTimeout(30, TimeUnit.SECONDS)
.writeTimeout(30, TimeUnit.SECONDS)
```

**After**:

```kotlin
.connectTimeout(10, TimeUnit.SECONDS)  // Reduced from 30
.readTimeout(15, TimeUnit.SECONDS)     // Reduced from 30
.writeTimeout(15, TimeUnit.SECONDS)    // Reduced from 30
```

**Impact**:

- ✅ Faster failure detection (10s vs 30s)
- ✅ Better user experience - won't hang as long
- ✅ Still enough time for slow connections

### 3. Improved Error Messages

**File**: `HomeAssistantRepository.kt`

**Before**:

```kotlin
catch (e: Exception) {
    Result.failure(e)
}
```

**After**:

```kotlin
catch (e: java.net.UnknownHostException) {
    Result.failure(Exception("Cannot resolve hostname. Check your URL or network connection."))
} catch (e: java.net.ConnectException) {
    Result.failure(Exception("Cannot connect to server. Check if Home Assistant is running."))
} catch (e: java.net.SocketTimeoutException) {
    Result.failure(Exception("Connection timeout. Server is not responding."))
} catch (e: Exception) {
    Result.failure(Exception("Connection error: ${e.message ?: "Unknown error"}"))
}
```

**Impact**:

- ✅ Clear, actionable error messages
- ✅ Users know exactly what went wrong
- ✅ Specific handling for common network issues

### 4. Better UI Feedback

**File**: `DashboardFragment.kt`

**Changes**:

- Hide connection chips and refresh button when no configuration is set
- Show helpful message: "Tap Refresh to load entities from [config name]"
- Update empty state messages to be more informative

**Impact**:

- ✅ User knows they need to manually refresh
- ✅ Clear guidance on what to do next
- ✅ UI elements only shown when relevant

## How It Works Now

### User Flow:

1. **Add Configuration**
    - User goes to Configurations tab
    - Fills in name, URLs, and API token
    - Taps "Save Configuration"
    - Taps "Activate"

2. **Dashboard Shows Helpful Message**
    - Instead of auto-connecting, dashboard shows:
    - "Tap Refresh to load entities from [config name]"
    - Refresh button is visible
    - Connection chips (Internal/External) are visible

3. **User Manually Refreshes**
    - User taps Refresh button
    - App attempts to connect
    - If successful: entities load
    - If fails: Clear error message displayed

4. **Error Handling**
    - If hostname can't be resolved: "Cannot resolve hostname. Check your URL or network
      connection."
    - If server unreachable: "Cannot connect to server. Check if Home Assistant is running."
    - If timeout: "Connection timeout. Server is not responding."
    - If auth fails: "Authentication failed. Check your API token."

## Testing Scenarios

### Scenario 1: Invalid Hostname

**Setup**: Enter `http://my-invalid-hostname.local:8123`
**Expected**:

- Configuration saves successfully
- Dashboard shows "Tap Refresh to load entities"
- User taps Refresh
- After ~10 seconds, error appears: "Cannot resolve hostname"
- No flickering or repeated attempts

### Scenario 2: Unreachable Server

**Setup**: Enter `http://192.168.1.250:8123` (unused IP)
**Expected**:

- Configuration saves
- User taps Refresh
- After ~10 seconds: "Cannot connect to server"
- App remains stable

### Scenario 3: Valid Configuration

**Setup**: Enter correct Home Assistant URL and token
**Expected**:

- Configuration saves
- User taps Refresh
- Entities load successfully
- No errors

## Benefits

1. **No More Flickering**
    - App doesn't automatically retry connections
    - User controls when connections are attempted

2. **Faster Failure Detection**
    - 10-second timeout instead of 30
    - User gets feedback quickly

3. **Clear Error Messages**
    - Users know exactly what went wrong
    - Actionable suggestions provided

4. **Better UX**
    - User has control over when to connect
    - Can configure app offline
    - Can switch configurations without triggering connections

5. **Reduced Network Activity**
    - No background connection attempts
    - Only connects when user explicitly requests it

## Migration Notes

**Existing Users**:

- After update, existing configurations won't auto-load
- Users need to tap "Refresh" once to load entities
- This is by design to prevent unwanted connection attempts

**New Users**:

- Flow is more intuitive
- Clear guidance at each step
- Better error messages

## Additional Improvements Made

1. **Logging Level**
    - Changed from `BODY` to `BASIC` to reduce log spam
    - Still provides useful debugging info

2. **UI State Management**
    - Better handling of visibility states
    - Chips and buttons hidden when not applicable

3. **Configuration Validation**
    - Better checks for null configs
    - Prevents crashes from invalid state

## Future Enhancements

Potential future improvements:

- [ ] Add URL validation before saving (check format)
- [ ] Add "Test Connection" button in configuration screen
- [ ] Cache last successful connection for offline viewing
- [ ] Add connection status indicator (connected/disconnected)
- [ ] Implement WebSocket for real-time updates (eliminates polling)
- [ ] Add retry logic with exponential backoff
- [ ] Support for self-signed certificates

## Related Files

Files modified in this fix:

- `ui/dashboard/DashboardViewModel.kt` - Removed auto-loading
- `ui/dashboard/DashboardFragment.kt` - Improved UI feedback
- `data/remote/RetrofitClient.kt` - Reduced timeouts
- `data/repository/HomeAssistantRepository.kt` - Better error handling

## Testing Commands

To test the fix:

```bash
# Build and install
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.example.simplehomeassistant/.MainActivity

# Monitor logs
adb logcat | grep -i "simplehomeassistant\|retrofit\|okhttp"
```

## Summary

The flickering issue was caused by automatic, repeated connection attempts when a configuration was
activated. The fix:

1. Removes automatic loading
2. Requires manual refresh by user
3. Provides faster failure detection
4. Gives clear, actionable error messages

Users now have full control over when the app attempts to connect to Home Assistant, preventing
unwanted behavior with unreachable servers.
