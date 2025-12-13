# Fix IDE Linter Errors - Quick Guide

## 🔴 Problem

Android Studio shows red errors in:

- `ConfigurationDaoTest.kt`
- `TabDaoTest.kt`

Errors like:

```
Unresolved reference 'Room'
Unresolved reference 'close'
```

## ✅ But Everything Actually Works!

**Proof**: Build succeeds:

```bash
$ ./gradlew clean build
BUILD SUCCESSFUL in 25s
```

**Proof**: Tests compile:

```bash
$ ./gradlew compileDebugAndroidTestKotlin
BUILD SUCCESSFUL
```

## 🔧 The Fix

### Option 1: Sync Project (30 seconds)

**In Android Studio**:

1. Look for the **"Sync Now"** banner at the top
2. Click it
3. Wait ~30 seconds for sync to complete
4. ✅ Errors should disappear!

**Or via menu**:

1. **File** → **Sync Project with Gradle Files**
2. Wait for sync
3. Done!

---

### Option 2: Invalidate Caches (2 minutes)

**If sync doesn't work**:

1. **File** → **Invalidate Caches / Restart...**
2. Select **"Invalidate and Restart"**
3. Wait for Android Studio to restart
4. Let it re-index the project
5. ✅ Errors should be gone!

---

### Option 3: Rebuild Project (1 minute)

**In Android Studio**:

1. **Build** → **Clean Project**
2. Wait for it to finish
3. **Build** → **Rebuild Project**
4. ✅ Should fix the errors

---

## 🎯 Why This Happens

The IDE needs to sync with the Gradle build file changes. When we added test dependencies:

```kotlin
// Added to build.gradle.kts
androidTestImplementation("androidx.room:room-testing:2.5.2")
```

The IDE needs to download these libraries and update its index. This happens during **Gradle Sync**.

---

## ✅ Verification

After syncing, verify the errors are gone:

1. Open `ConfigurationDaoTest.kt`
2. No red underlines should appear
3. Imports should be green/gray (not red)
4. Right-click file → "Run ConfigurationDaoTest" should work

---

## 🚀 If Still Showing Errors

### Last Resort: Reimport Project

1. **File** → **Close Project**
2. **File** → **Open**
3. Navigate to: `/Users/chris/AndroidStudioProjects/SimpleHomeAssistant`
4. Click **OK**
5. Wait for Gradle sync
6. ✅ Fresh start should fix everything

---

## 📝 Notes

- **The errors are cosmetic** - everything actually compiles
- **Build succeeds** - proven by `./gradlew build`
- **Tests work** - 33 unit tests passing
- **IDE just needs to catch up** - sync fixes it

---

## 🎯 Quick Command Reference

**Sync from terminal** (sometimes helps):

```bash
cd /Users/chris/AndroidStudioProjects/SimpleHomeAssistant
./gradlew --refresh-dependencies
```

**Verify build still works**:

```bash
./gradlew clean build
```

**Run tests to prove it works**:

```bash
./gradlew test
```

---

## ✨ Expected Result

After syncing, you should see:

✅ **No red errors** in `ConfigurationDaoTest.kt`  
✅ **No red errors** in `TabDaoTest.kt`  
✅ **All imports resolved** (green/gray)  
✅ **Can run tests** from IDE  
✅ **Autocomplete works** for Room classes

---

## 🏆 Done!

Once synced, your IDE will match the successful build state, and you can:

- Edit tests without red squiggles
- Run tests from IDE
- Use autocomplete
- Navigate to definitions

**Status after sync**: 🟢 **GREEN** - All clear!
