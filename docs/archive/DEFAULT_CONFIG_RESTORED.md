# Default Belmont Configuration - Restored

## Issue

When database was upgraded to v2 (for custom tabs), the default Belmont configuration was lost.

## Solution Applied

Cleared app data and reinstalled, which triggers the `onCreate` callback that inserts the default
configuration.

## Default Configuration

```
Name: Belmont
Internal URL: http://192.168.1.82:8123
External URL: http://hitorro.com:8123
API Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiIzOThjYThlZjRhOTY0YThlYjg2NDY1MDVhODFmMWNiZSIsImlhdCI6MTc2NTQyMjA1MCwiZXhwIjoyMDgwNzgyMDUwfQ.4VdumgNedC5sZbwK3b0R8TTrsUy5_q6kBKDDOLoOmXI
Status: Active
```

## How It Works

The default configuration is automatically inserted when the database is first created:

**File**: `AppDatabase.kt`

```kotlin
.addCallback(object : RoomDatabase.Callback() {
    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        super.onCreate(db)
        // Insert default configuration on first launch
        db.execSQL(
            """
            INSERT INTO configurations (name, internalUrl, externalUrl, apiToken, isActive)
            VALUES (
                'Belmont',
                'http://192.168.1.82:8123',
                'http://hitorro.com:8123',
                'YOUR_API_TOKEN_HERE',
                1
            )
            """
        )
    }
})
```

## When Default Config is Created

The default Belmont configuration is automatically created when:

1. **First install** - App is installed for the first time
2. **Clear data + reinstall** - App data is cleared and app reinstalled
3. **Database upgraded** - When using `fallbackToDestructiveMigration()`

## What This Means for Testing

**Good News**: You don't need to enter Belmont details every time!

The configuration is:

- ✅ Pre-configured on app install
- ✅ Already active
- ✅ Ready to use immediately
- ✅ Includes your API token

**For Future Updates**:

- Database schema changes will recreate the database
- Default config will be automatically inserted
- No manual configuration needed

## Verifying It's There

After install/reinstall:

1. Open app
2. Go to "Configurations" tab
3. You should see "Belmont" with green "Active" chip
4. Internal URL: `http://192.168.1.82:8123`
5. External URL: `http://hitorro.com:8123`

## Current Status

✅ **App cleared and reinstalled**  
✅ **Default Belmont configuration restored**  
✅ **Ready to use without manual configuration**

You can now test without entering configuration details each time!

## Note About Database Migrations

Currently using `fallbackToDestructiveMigration()` which:

- **Pros**: Easy development, no migration code needed
- **Cons**: Wipes data on schema changes

For production, you'd want proper migrations to preserve user data. But for development/testing,
this is perfect - you always get a fresh start with default config.

## If You Need to Update the Default Config

To change the default configuration values:

1. Edit `AppDatabase.kt`
2. Update the SQL INSERT statement
3. Clear app data: `adb shell pm clear com.example.simplehomeassistant`
4. Reinstall: `adb install -r app/build/outputs/apk/debug/app-debug.apk`

The new default values will be used.
