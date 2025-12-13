# Getting Started with Simple Home Assistant

This guide will help you set up and use the Simple Home Assistant Android app.

## Step 1: Build and Install

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Connect your Android device or start an emulator
4. Click Run (green play button) or press `Shift + F10`

## Step 2: Get Your Home Assistant Token

Before using the app, you need a long-lived access token from Home Assistant:

1. Open your Home Assistant web interface
2. Click your profile picture in the bottom left
3. Scroll down to "Long-Lived Access Tokens"
4. Click "Create Token"
5. Enter a name like "Android App"
6. **Copy the token immediately** (you won't see it again!)

## Step 3: Add Your First Configuration

Example: Setting up a configuration for your "Belmont" house

1. Open the app
2. Tap **"Configurations"** in the bottom navigation
3. Fill in the form:
   ```
   Configuration Name: Belmont
   Internal URL: http://192.168.1.100:8123
   External URL: https://belmont.myhouse.duckdns.org
   API Token: [paste your token here]
   ```
4. Tap **"Save Configuration"**
5. Tap **"Activate"** on your new configuration

### URL Examples

**Internal URL** (when on the same network):

- `http://192.168.1.100:8123`
- `http://homeassistant.local:8123`

**External URL** (when away from home):

- `https://yourhome.duckdns.org`
- `https://homeassistant.example.com`
- Your Nabu Casa URL if you have a subscription

## Step 4: Select Entities to Display

1. Tap **"Select Entities"** in the bottom navigation
2. Wait for entities to load
3. Check the boxes for entities you want on your dashboard
4. Examples to select:
    - Living Room Light (light.living_room)
    - Kitchen Switch (switch.kitchen)
    - Main Thermostat (climate.main)
    - Temperature Sensor (sensor.living_room_temperature)

## Step 5: Use the Dashboard

1. Tap **"Dashboard"** in the bottom navigation
2. Your selected entities will appear as cards
3. Control your devices:
    - **Switches**: Toggle on/off
    - **Lights**: Toggle and adjust brightness
    - **Thermostats**: Change temperature and mode
    - **Sensors**: View current readings

## Multiple Configurations

### Adding a Second House (e.g., "Orlando")

1. Go to **"Configurations"**
2. Add a new configuration:
   ```
   Configuration Name: Orlando
   Internal URL: http://192.168.2.50:8123
   External URL: https://orlando.vacation.duckdns.org
   API Token: [Orlando house token]
   ```
3. Save and activate when needed

### Switching Between Configurations

1. Go to **"Configurations"**
2. Tap **"Activate"** on the configuration you want to use
3. Go to **"Select Entities"** to choose entities for this configuration
4. Return to **"Dashboard"** to control

## Network Switching

When you're on your home network, use **Internal**. When you're away, use **External**.

1. On the Dashboard, you'll see chips: **Internal** | **External**
2. Tap the appropriate chip to switch
3. The app will reconnect using that URL

**Example scenarios:**

- At home: Use "Internal" → faster, no internet needed
- At work: Use "External" → connects over internet
- Traveling: Use "External"

## Tips

### Best Practices

- ✅ Give configurations clear names ("Home", "Cabin", "Parents House")
- ✅ Test both internal and external URLs before saving
- ✅ Only select the entities you use frequently
- ✅ Keep your API tokens secure

### Common Issues

**"No configuration set"**

- Solution: Go to Configurations and activate one

**"Failed to load entities"**

- Check: Is your URL correct?
- Check: Is your device connected to the right network?
- Check: Is your API token valid?

**Controls not responding**

- Try: Pull down to refresh
- Try: Switch between Internal/External
- Check: Can you control it in the Home Assistant web interface?

## Example Setup: Two Houses

### House 1: Main Home (Belmont)

```
Name: Belmont
Internal: http://192.168.1.100:8123
External: https://belmont.home.duckdns.org
Token: eyJ0eXAiOiJKV1QiLCJhbGc... (your token)

Selected Entities:
- Living Room Lights
- Bedroom Lights
- Main Thermostat
- Front Door Lock
- Garage Door
```

### House 2: Vacation Home (Orlando)

```
Name: Orlando
Internal: http://192.168.2.50:8123
External: https://orlando.vacation.duckdns.org
Token: eyJ0eXBiOiJKV2QiLCJhbGd... (different token)

Selected Entities:
- Pool Pump
- AC Thermostat
- Security System
- Front Gate
```

## Advanced: Finding Your Internal IP

### On Home Assistant

1. Go to Settings → System → Network
2. Look for "IPv4 Address"

### Or from your router

1. Log into your router
2. Look for connected devices
3. Find "homeassistant" or the Raspberry Pi

The IP usually looks like:

- `192.168.1.xxx`
- `192.168.0.xxx`
- `10.0.0.xxx`

Always append `:8123` (the default Home Assistant port)

## Need Help?

- Check the main README.md for troubleshooting
- Verify your Home Assistant is accessible
- Check your Home Assistant logs for errors
- Ensure entities are controllable in the web interface first
