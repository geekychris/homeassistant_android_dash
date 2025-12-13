# Quick Start Guide - Home Assistant Android App

## 🚀 Getting Started

### Default Configuration

The app comes pre-configured with your Belmont home:

```
Name: Belmont
Internal URL: http://192.168.1.82:8123
External URL: http://hitorro.com:8123
Status: Active (ready to use)
```

## 📱 Using the App

### Step 1: Load Entities (Dashboard Tab)

1. Open the app (it starts on the **Dashboard** tab)
2. Tap the **"Refresh"** button
3. Wait for entities to load from your Home Assistant
4. By default, shows all controllable entities OR your selected entities

### Step 2: Select Specific Entities (Select Entities Tab)

1. Tap the **"Select Entities"** tab at the bottom
2. Use the **search bar** to find entities:
    - Type entity name (e.g., "Kitchen Light")
    - Type entity ID (e.g., "light.kitchen")
    - Type room name (e.g., "Kitchen")
3. **Check the boxes** next to entities you want on your dashboard
4. Selection is saved automatically

### Step 3: View Selected Entities (Dashboard Tab)

1. Go back to the **"Dashboard"** tab
2. Tap **"Refresh"** to reload
3. Dashboard now shows **only your selected entities**!

### Step 4: Control Your Devices

- **Switches**: Tap to toggle on/off
- **Lights**:
    - Tap to toggle on/off
    - Use slider to adjust brightness
- **Thermostats**:
    - Use +/- buttons to adjust temperature
    - Tap mode dropdown to change heating/cooling mode
- **Sensors**: View current readings (temperature, humidity, etc.)

## 🔍 Search Features

### In "Select Entities" Tab

- **Real-time search** as you type
- **Search by**:
    - Entity name (friendly name)
    - Entity ID (technical name)
    - Room/Area name
- **Case-insensitive** search
- **Clear search** by tapping X in search bar
- **Selection persists** during search

### Search Examples

- Search `"light"` → Shows all light entities
- Search `"kitchen"` → Shows all entities in kitchen
- Search `"switch.garage"` → Finds specific entity by ID

## ⚙️ Managing Configurations

### View/Edit Configurations (Configurations Tab)

1. Tap the **"Configurations"** tab
2. See your active configuration (has green "Active" chip)
3. Tap **"Edit"** to modify URL or token
4. Tap **"Delete"** to remove configuration

### Add New Configuration

1. In **"Configurations"** tab, tap **"+ Add Configuration"**
2. Fill in:
    - **Name** (e.g., "Orlando Home")
    - **Internal URL** (e.g., `http://192.168.1.100:8123`)
    - **External URL** (e.g., `http://myhouse.com:8123`)
    - **API Token** (from Home Assistant)
3. Tap **"Save"**
4. Tap **"Activate"** to make it the active configuration

### Switch Between Homes

1. In **"Configurations"** tab
2. Find the home you want to use
3. Tap **"Activate"**
4. Go to **"Dashboard"** and tap **"Refresh"**

## 💡 Tips & Tricks

### Refresh is Key

- Always tap **"Refresh"** on Dashboard after:
    - Changing entity selections
    - Switching configurations
    - Wanting to see latest device states

### Internal vs External URLs

- **Internal URL**: Use when on same network as Home Assistant
- **External URL**: Use when away from home (requires port forwarding/VPN)
- Tap the chip on Dashboard to toggle between them

### No Entities Selected?

- If you haven't selected any entities, dashboard shows **all controllable entities**
- This is helpful when first setting up
- Select specific entities for a cleaner dashboard

### Testing with Emulator?

- Use `http://10.0.2.2:8123` instead of `localhost`
- This special IP maps to your host machine
- Works when Home Assistant runs on your Mac/PC

### Finding Your API Token

1. Open Home Assistant web interface
2. Click your profile (bottom left)
3. Scroll to **"Long-Lived Access Tokens"**
4. Click **"Create Token"**
5. Copy the token and paste in app

## 🔧 Troubleshooting

### "No active configuration" message

- **Solution**: Go to Configurations tab and activate a configuration

### "Cannot resolve hostname" error

- **Solution**: Check URL is correct and Home Assistant is running
- For emulator: Use `10.0.2.2` instead of `localhost`

### Dashboard shows empty after selecting entities

- **Solution**: Tap the **"Refresh"** button on Dashboard

### Entity selection doesn't persist

- **Solution**: Make sure configuration is active before selecting entities
- Each configuration has its own entity selections

### Can't connect to Home Assistant

- Check Home Assistant is running
- Verify URL is correct
- Ensure API token is valid
- Check firewall/network settings

## 📊 Entity Types Supported

- ✅ **Switches** (on/off control)
- ✅ **Lights** (on/off + brightness)
- ✅ **Thermostats/Climate** (temperature + mode)
- ✅ **Sensors** (temperature, humidity, etc.)
- ✅ **Binary Sensors** (door/window states)

## 🎯 Common Workflows

### Daily Use

1. Open app → Dashboard tab
2. Tap "Refresh" to get latest states
3. Control your devices
4. That's it!

### Setting Up New Home

1. Configurations tab → Add Configuration
2. Enter details → Save → Activate
3. Select Entities tab → Search and select devices
4. Dashboard tab → Refresh → Control devices

### Customizing Dashboard

1. Select Entities tab
2. Search for devices you want
3. Check/uncheck boxes
4. Dashboard tab → Refresh
5. Dashboard shows only selected entities

---

**Need help?** Check the full documentation in the project root:

- `README.md` - Complete overview
- `ARCHITECTURE.md` - Technical details
- `SEARCH_AND_SELECTION_UPDATE.md` - Latest features

**The app is ready to use!** 🏠✨
