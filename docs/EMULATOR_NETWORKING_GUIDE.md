# Android Emulator Networking Guide

## The Hostname Resolution Problem

### Why "cannot resolve hostname" happens in the emulator

The Android emulator has its own isolated network stack:

- **Emulator IP**: 10.0.2.15 (the emulator itself)
- **Host Machine**: 10.0.2.2 (your Mac)
- **Emulator DNS**: 10.0.2.3
- **Emulator Router**: 10.0.2.1

**This means**:

- ❌ `localhost` in emulator points to the emulator itself, NOT your Mac
- ❌ `.local` mDNS names (like `homeassistant.local`) don't work
- ❌ Internal hostnames may not resolve through emulator DNS
- ✅ IP addresses usually work
- ✅ `10.0.2.2` always points to your Mac

## Solutions for Testing in Emulator

### Option 1: Use 10.0.2.2 (Recommended for Emulator Testing)

If your Home Assistant is running on your Mac at `http://localhost:8123`:

**Instead of**: `http://localhost:8123`
**Use**: `http://10.0.2.2:8123`

**Instead of**: `http://homeassistant.local:8123`
**Use**: `http://10.0.2.2:8123`

### Option 2: Use Your Mac's LAN IP Address

Find your Mac's IP on the local network:

```bash
ifconfig | grep "inet " | grep -v 127.0.0.1
```

Example output: `inet 192.168.1.100`

Then use: `http://192.168.1.100:8123`

### Option 3: Test on Real Device

Real Android devices use your actual network:

- They can resolve `.local` names
- They can access your local network normally
- Much closer to real-world usage

## What We Fixed to Prevent Crashes

### 1. Network Security Configuration

Created `/app/src/main/res/xml/network_security_config.xml`:

- Allows HTTP (cleartext) traffic
- Necessary for local Home Assistant servers without HTTPS
- Trusts system and user certificates

### 2. Global Exception Handler

Created `HomeAssistantApplication.kt`:

- Catches uncaught network exceptions
- Prevents app crashes from network errors
- Logs errors for debugging

### 3. Better Error Handling

Updated `HomeAssistantRepository.kt`:

- Specific catch blocks for different network errors
- User-friendly error messages
- Graceful failure handling

## Testing Scenarios

### Scenario 1: Home Assistant on Your Mac

**Setup**:

```
Internal URL: http://10.0.2.2:8123
External URL: http://10.0.2.2:8123
API Token: your_token_here
```

**Testing**:

1. Emulator can access your Mac via 10.0.2.2
2. Should connect successfully
3. Can load entities

### Scenario 2: Home Assistant on Network Device

**Setup** (if your HA IP is 192.168.1.100):

```
Internal URL: http://192.168.1.100:8123
External URL: https://your-external-domain.com
API Token: your_token_here
```

**Testing**:

1. Emulator can access network IPs
2. Should connect successfully
3. Can switch between internal/external

### Scenario 3: Testing with Unresolvable Hostname

**Setup**:

```
Internal URL: http://homeassistant.local:8123
External URL: http://homeassistant.local:8123
API Token: your_token_here
```

**Expected Behavior**:

1. Tap "Refresh" on dashboard
2. After ~10 seconds: "Cannot resolve hostname" error
3. **App does NOT crash**
4. Error message displayed in Snackbar
5. User can try again or change configuration

## Common Issues and Solutions

### Issue: "Cannot resolve hostname" with .local address

**Problem**: mDNS doesn't work in emulator
**Solution**: Use `10.0.2.2` or your Mac's LAN IP

### Issue: "Connection refused"

**Problem**: Home Assistant not running or wrong port
**Solution**:

- Verify HA is running
- Check port number (usually 8123)
- Try accessing from browser first

### Issue: "Authentication failed"

**Problem**: Invalid API token
**Solution**:

- Regenerate token in Home Assistant
- Copy full token (they're very long)
- Paste carefully without extra spaces

### Issue: App crashes after error

**Problem**: (Now fixed!) Uncaught exception
**Solution**:

- Updated app includes global exception handler
- Rebuild and reinstall

## Debugging Network Issues

### Check if HA is accessible from Mac

```bash
# From your Mac terminal
curl http://localhost:8123/api/

# Should return: {"message": "API running."}
```

### Check from within emulator

```bash
# From terminal with emulator running
adb shell

# Inside emulator shell
ping 10.0.2.2
curl http://10.0.2.2:8123/api/
```

### Monitor app logs

```bash
adb logcat | grep -i "homeassistant\|retrofit\|okhttp"
```

## Production vs Development

### In Development (Emulator/Testing)

- Use `10.0.2.2` for localhost
- HTTP is allowed
- Detailed logging enabled

### In Production (Real Device)

- Use actual hostnames/IPs
- Prefer HTTPS for external access
- Consider VPN for secure remote access

## Summary of Changes

**Files Modified**:

1. `AndroidManifest.xml` - Added network security config
2. `network_security_config.xml` - Allows cleartext traffic
3. `HomeAssistantApplication.kt` - Global exception handler
4. `RetrofitClient.kt` - Reduced timeouts
5. `HomeAssistantRepository.kt` - Better error messages
6. `DashboardViewModel.kt` - Manual refresh only

**Result**:

- ✅ HTTP connections allowed
- ✅ App won't crash on network errors
- ✅ Clear error messages
- ✅ Works with IP addresses
- ✅ Graceful failure handling

## Next Steps

1. **Rebuild the app**: `./gradlew assembleDebug`
2. **Reinstall**: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
3. **Use correct URL**: Remember `10.0.2.2` for emulator testing
4. **Test**: The app should no longer crash on network errors
