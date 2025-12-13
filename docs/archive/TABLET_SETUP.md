# Connect Android Tablet for Development

## Quick Setup Steps

### On the Tablet

1. **Enable Developer Options**:
    - Go to **Settings** → **About tablet** (or **About device**)
    - Find **Build number**
    - Tap **Build number** 7 times rapidly
    - You'll see "You are now a developer!"

2. **Enable USB Debugging**:
    - Go back to **Settings**
    - Find **Developer options** (usually under System)
    - Enable **USB debugging**
    - Enable **Install via USB** (if available)

3. **Connect USB Cable**:
    - Plug the tablet into your Mac
    - You should see a prompt on the tablet
    - Tap **"Allow USB debugging"**
    - Check **"Always allow from this computer"**
    - Tap **OK**

### On Your Mac

Once the tablet is connected and authorized, run:

```bash
adb devices
```

You should see something like:

```
List of devices attached
emulator-5554          device    (emulator)
ABC123XYZ              device    (your tablet)
```

## Troubleshooting

### Tablet Not Showing Up?

1. **Try a different USB cable** - Some cables are charge-only
2. **Try a different USB port** on your Mac
3. **Restart ADB**:
   ```bash
   adb kill-server
   adb start-server
   adb devices
   ```

4. **Check USB connection mode** on tablet:
    - Swipe down notification area
    - Tap on USB connection notification
    - Select **"File Transfer"** or **"PTP"**

5. **Revoke and re-authorize**:
    - On tablet: Settings → Developer options
    - Tap **"Revoke USB debugging authorizations"**
    - Unplug and replug the cable
    - Accept the prompt again

### Still Not Working?

Check if the tablet needs drivers (rare on Mac):

```bash
system_profiler SPUSBDataType
```

Look for your tablet in the output.

## Ready to Deploy

Once you see your tablet in `adb devices`, let me know and I'll install the app!
