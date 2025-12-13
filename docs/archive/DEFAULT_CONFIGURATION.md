# Default Configuration Added

## What Was Added

The app now includes a **default configuration** that is automatically created on first launch.

### Default Configuration Details

- **Name**: Belmont
- **Internal URL**: `http://192.168.1.82:8123`
- **External URL**: `http://hitorro.com:8123`
- **API Token**:
  `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiIzOThjYThlZjRhOTY0YThlYjg2NDY1MDVhODFmMWNiZSIsImlhdCI6MTc2NTQyMjA1MCwiZXhwIjoyMDgwNzgyMDUwfQ.4VdumgNedC5sZbwK3b0R8TTrsUy5_q6kBKDDOLoOmXI`
- **Status**: Active by default

## How It Works

The configuration is created automatically when the database is first created using a Room database
callback:

```kotlin
.addCallback(object : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Insert default configuration on first launch
        db.execSQL("""
            INSERT INTO configurations (name, internalUrl, externalUrl, apiToken, isActive)
            VALUES (
                'Belmont',
                'http://192.168.1.82:8123',
                'http://hitorro.com:8123',
                'eyJhbGci...',
                1
            )
        """)
    }
})
```

## When It's Created

- **First Launch**: When the app is installed for the first time and the database is created
- **After Uninstall/Reinstall**: When you clear app data or reinstall the app
- **NOT on Updates**: If the database already exists, the default won't be added again

## Benefits

1. **Immediate Use**: Users can start using the app right away without configuration
2. **Testing**: Makes it easier to test the app with a known configuration
3. **Example**: Shows users what a configuration should look like

## User Experience

### On First Launch:

1. User opens the app
2. Default "Belmont" configuration is already created and active
3. User can immediately:
    - Go to Dashboard
    - Tap "Refresh" to load entities
    - Go to "Select Entities" to choose devices
    - Go to "Configurations" to see the default or add more

### Managing Configurations:

Users can still:

- ✅ Add additional configurations
- ✅ Edit the default configuration
- ✅ Delete the default configuration
- ✅ Switch between configurations
- ✅ Deactivate and activate as needed

## File Modified

**File**: `data/local/AppDatabase.kt`

**Changes**:

- Added `.addCallback()` to Room database builder
- Implemented `onCreate()` callback
- Inserted default configuration via SQL

## Important Notes

### Security Consideration

⚠️ **The API token is stored in plain text in the app code**

This is fine for:

- Personal use
- Internal testing
- Single-user scenarios

For production/distribution, consider:

- Having users enter their own credentials
- Using encrypted storage
- Not hardcoding tokens in source code

### Testing

To test the default configuration on an existing installation:

```bash
# Uninstall the app (clears database)
adb uninstall com.example.simplehomeassistant

# Reinstall (triggers onCreate callback)
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch
adb shell am start -n com.example.simplehomeassistant/.MainActivity
```

### Updating the Default

To change the default configuration in the future, simply edit the SQL statement in `AppDatabase.kt`
and reinstall the app (after uninstalling first).

## Current Status

✅ **Default configuration is active**

- App installed with fresh database
- "Belmont" configuration created automatically
- Set as active configuration
- Ready to use immediately

## Next Steps

1. Open the app
2. The default "Belmont" configuration is already active
3. Go to Dashboard
4. Tap "Refresh" to connect to `http://192.168.1.82:8123`
5. If successful, entities will load
6. Go to "Select Entities" to choose which devices to display

The app is now ready to use with zero configuration required! 🎉
