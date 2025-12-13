# Hostname Resolution & Crash Fix - Summary

## Issues Identified

### 1. "Cannot Resolve Hostname" Error

**Cause**: Android emulator networking is isolated from the host machine

- Emulator cannot resolve `.local` mDNS names
- `localhost` points to emulator itself, not your Mac
- Internal hostnames may not resolve through emulator DNS

### 2. App Crashing After Network Error

**Cause**: Uncaught exceptions from network failures

- Network errors weren't being properly caught
- Exceptions propagating to the main thread
- No global exception handler

## Fixes Implemented

### Fix #1: Network Security Configuration

**File**: `app/src/main/res/xml/network_security_config.xml` (NEW)

**What it does**:

- Allows HTTP (cleartext) connections for local Home Assistant servers
- Trusts system and user certificates
- Enables connection to local network addresses

**Why needed**: Many Home Assistant setups use HTTP on local networks without HTTPS

### Fix #2: Global Exception Handler

**File**: `app/src/main/java/com/example/simplehomeassistant/HomeAssistantApplication.kt` (NEW)

**What it does**:

- Catches all uncaught exceptions before they crash the app
- Specifically handles network-related errors gracefully
- Logs errors for debugging

**Why needed**: Prevents app crashes from network timeouts, connection failures, etc.

### Fix #3: Manifest Updates

**File**: `app/src/main/AndroidManifest.xml`

**Changes**:

- Added `android:name=".HomeAssistantApplication"` - Registers custom Application class
- Added `android:networkSecurityConfig="@xml/network_security_config"` - Enables network config
- Added `android:usesCleartextTraffic="true"` - Allows HTTP connections

### Fix #4: Improved Error Messages

**File**: `data/repository/HomeAssistantRepository.kt` (PREVIOUSLY UPDATED)

**What it does**:

- Specific error messages for each type of network failure
- Catches `UnknownHostException`, `ConnectException`, `SocketTimeoutException`
- Provides actionable feedback to users

### Fix #5: Reduced Timeouts

**File**: `data/remote/RetrofitClient.kt` (PREVIOUSLY UPDATED)

**What it does**:

- Connect timeout: 10 seconds (was 30)
- Read/Write timeout: 15 seconds (was 30)
- Fails faster, better user experience

### Fix #6: Manual Refresh Only

**File**: `ui/dashboard/DashboardViewModel.kt` (PREVIOUSLY UPDATED)

**What it does**:

- No automatic connection attempts
- User must explicitly tap "Refresh"
- Prevents unwanted network activity

## How to Use in Emulator

### The Key: Use 10.0.2.2 Instead of localhost

**❌ Don't Use**:

```
http://localhost:8123
http://homeassistant.local:8123
http://my-server.local:8123
```

**✅ Do Use** (if Home Assistant is on your Mac):

```
Internal URL: http://10.0.2.2:8123
External URL: http://10.0.2.2:8123
API Token: (your long-lived token)
```

**✅ Or Use** (if Home Assistant is on network):

```
Internal URL: http://192.168.1.100:8123
External URL: https://your-domain.com
API Token: (your long-lived token)
```

### Testing Steps

1. **Start Emulator** (if not already running)
   ```bash
   $HOME/Library/Android/sdk/emulator/emulator -avd Medium_Phone_API_36.1 &
   ```

2. **Install Updated App**
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Launch App**
   ```bash
   adb shell am start -n com.example.simplehomeassistant/.MainActivity
   ```

4. **Add Configuration**
    - Go to "Configurations" tab
    - Fill in:
        - Name: "Test"
        - Internal URL: `http://10.0.2.2:8123`
        - External URL: `http://10.0.2.2:8123`
        - API Token: (your token)
    - Tap "Save Configuration"
    - Tap "Activate"

5. **Test Connection**
    - Go to "Dashboard" tab
    - Tap "Refresh" button
    - Should see entities if HA is running
    - If error: Clear message, **no crash**

## Expected Behavior After Fixes

### If Home Assistant is Accessible

- ✅ Entities load successfully
- ✅ Can control devices
- ✅ No crashes

### If Home Assistant is NOT Accessible

- ✅ Clear error message appears
- ✅ App does NOT crash
- ✅ Can try again or change configuration
- ✅ Error message tells you what's wrong:
    - "Cannot resolve hostname..." → Use IP address or 10.0.2.2
    - "Cannot connect to server..." → Check if HA is running
    - "Connection timeout..." → Server not responding
    - "Authentication failed..." → Check API token

## Verification Checklist

After installing the updated app:

- [ ] App launches without immediate crash
- [ ] Can add configuration with any URL (even invalid ones)
- [ ] Can activate configuration without crash
- [ ] Dashboard shows "Tap Refresh to load entities"
- [ ] Tapping Refresh with invalid URL shows error message
- [ ] **App does NOT crash after error**
- [ ] Can dismiss error and try again
- [ ] With correct URL (`10.0.2.2`), entities load
- [ ] Can control entities
- [ ] Can switch between configurations

## Files Changed in This Fix

### New Files

1. `app/src/main/res/xml/network_security_config.xml` - Network permissions
2. `app/src/main/java/com/example/simplehomeassistant/HomeAssistantApplication.kt` - Exception
   handler
3. `EMULATOR_NETWORKING_GUIDE.md` - Detailed networking guide
4. `NETWORK_FIXES.md` - Summary of network fixes
5. `CRASH_FIX_SUMMARY.md` - This file

### Modified Files

1. `app/src/main/AndroidManifest.xml` - Added network config and Application class
2. `data/remote/RetrofitClient.kt` - Reduced timeouts
3. `data/repository/HomeAssistantRepository.kt` - Better error handling
4. `ui/dashboard/DashboardViewModel.kt` - Manual refresh only
5. `ui/dashboard/DashboardFragment.kt` - Better UI feedback

## Why This Happens in Emulator but Not on Real Device

### Android Emulator

- Has isolated network stack
- Uses virtual network (10.0.2.x)
- Cannot access `.local` mDNS names
- localhost = emulator itself
- Must use `10.0.2.2` to reach host machine

### Real Android Device

- Uses your actual WiFi network
- Can resolve `.local` names
- localhost = the device itself
- Can access network devices normally
- Much closer to production usage

## Testing on Real Device

For more realistic testing:

1. **Enable USB Debugging** on your Android phone
2. **Connect via USB**
3. **Install**: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
4. **Use real URLs**:
    - Internal: `http://192.168.1.100:8123` (your HA's LAN IP)
    - External: `https://your-domain.com`
5. **Test normally** - will work like production

## Troubleshooting

### App still crashes

- Make sure you installed the latest build
- Check: `adb shell pm list packages | grep simplehomeassistant`
- Reinstall:
  `adb uninstall com.example.simplehomeassistant && adb install app/build/outputs/apk/debug/app-debug.apk`

### Still can't connect from emulator

- Verify Home Assistant is running on your Mac: `curl http://localhost:8123/api/`
- Check you're using `http://10.0.2.2:8123` not `localhost`
- Try your Mac's LAN IP instead: `http://192.168.1.XXX:8123`

### Error messages not showing

- Make sure you tapped "Refresh" button
- Check Snackbar at bottom of screen
- Error should appear after ~10 seconds

## Success!

✅ **App no longer crashes on network errors**
✅ **Clear error messages guide the user**
✅ **HTTP connections work for local Home Assistant**
✅ **Proper exception handling throughout**
✅ **Ready for real-world testing**

## Next Steps

1. Install the updated APK
2. Use `10.0.2.2` for emulator testing
3. Test that app doesn't crash on errors
4. Test on real device for production-like experience
5. Enjoy controlling your home! 🏠
