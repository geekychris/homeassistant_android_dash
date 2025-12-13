# Quick Reference Card

## ⚡ Fixed Issues

✅ **Hostname resolution crashes** - Now handles gracefully
✅ **App crashing on network errors** - Global exception handler added
✅ **Flickering when activating config** - Manual refresh only

## 🔑 Key Point for Emulator Testing

**Use `10.0.2.2` instead of `localhost`**

```
❌ http://localhost:8123
❌ http://homeassistant.local:8123

✅ http://10.0.2.2:8123
```

## 📱 Quick Start

### 1. Install Latest Build

```bash
cd /Users/chris/AndroidStudioProjects/SimpleHomeAssistant
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 2. Launch App

```bash
adb shell am start -n com.example.simplehomeassistant/.MainActivity
```

### 3. Configure

- Tap "Configurations"
- Enter:
    - Name: `My Home`
    - Internal URL: `http://10.0.2.2:8123`
    - External URL: `http://10.0.2.2:8123`
    - API Token: `eyJ...` (your long token)
- Tap "Save Configuration"
- Tap "Activate"

### 4. Load Entities

- Go to "Dashboard"
- Tap "Refresh" button
- Entities will load (or show clear error)

## 🐛 Common Errors & Solutions

| Error Message | Solution |
|--------------|----------|
| "Cannot resolve hostname" | Use `10.0.2.2` or IP address instead of hostname |
| "Cannot connect to server" | Check if Home Assistant is running |
| "Connection timeout" | Reduce timeout or check network |
| "Authentication failed" | Regenerate and paste API token correctly |

## 📁 Important Files

- `CRASH_FIX_SUMMARY.md` - Complete fix details
- `EMULATOR_NETWORKING_GUIDE.md` - Networking explained
- `FIX_CONNECTION_FLICKERING.md` - Flickering fix details
- `BUILD_VERIFICATION.md` - Build status
- `TESTING_GUIDE.md` - Test scenarios

## 🎯 What Changed

1. **Network Security Config** - Allows HTTP connections
2. **Global Exception Handler** - Prevents crashes
3. **Manual Refresh** - No automatic connections
4. **Better Errors** - Clear, actionable messages
5. **Faster Timeouts** - 10s instead of 30s

## ✨ App Status

- **Build**: ✅ Successful
- **Crashes Fixed**: ✅ Yes
- **Network Errors**: ✅ Handled gracefully
- **Ready to Test**: ✅ Yes

## 💡 Pro Tips

- **Emulator**: Use `10.0.2.2` for localhost
- **Real Device**: Use actual hostname/IP
- **Testing**: Always tap "Refresh" manually
- **Debugging**: Check logs with `adb logcat`
