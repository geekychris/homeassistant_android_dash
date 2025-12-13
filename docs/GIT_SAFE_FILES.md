# Git Safety - What to Commit

## 🟢 SAFE to Commit (No Credentials)

```
✅ .gitignore
✅ app/src/main/assets/default_config.json.template
✅ app/src/main/java/com/example/simplehomeassistant/data/local/AppDatabase.kt
✅ All other source files
✅ All resource files
✅ build.gradle.kts
✅ All documentation (.md files)
```

---

## 🔴 NEVER Commit (Contains Credentials)

```
❌ app/src/main/assets/default_config.json
```

This file contains:

- Your internal network URL
- Your external URL
- Your Home Assistant API token

**This is automatically excluded by `.gitignore`** ✅

---

## 🔍 Quick Verification

### Before Your First Git Commit

Run this command:

```bash
cd /Users/chris/AndroidStudioProjects/SimpleHomeAssistant
git status
```

**Make sure you DON'T see**:

- ❌ `app/src/main/assets/default_config.json`

**You SHOULD see**:

- ✅ `app/src/main/assets/default_config.json.template`
- ✅ `.gitignore`
- ✅ Other source files

---

## 🚨 If You Accidentally Committed the Config

If you already committed `default_config.json` in the past:

### Remove from Git History

```bash
# Remove the file from git but keep it locally
git rm --cached app/src/main/assets/default_config.json

# Commit the removal
git commit -m "Remove sensitive config file from tracking"

# For existing remote repo, force push (BE CAREFUL!)
git push --force

# Note: File still exists in git history!
# Consider rotating your API token in Home Assistant
```

### Rotate Your Token

1. Go to Home Assistant
2. **Profile** → **Long-Lived Access Tokens**
3. **Revoke** the old token
4. **Create** a new token
5. Update `default_config.json` with new token

---

## 📋 Checklist Before Pushing to Remote

- [ ] Verify `.gitignore` includes `app/src/main/assets/default_config.json`
- [ ] Run `git status` and confirm config file is NOT listed
- [ ] Confirm `default_config.json.template` IS listed (safe to commit)
- [ ] Consider using `git log --all --full-history -- "*default_config.json"` to check history

---

## ✅ Current Status

Your project is **configured correctly**:

- ✅ `.gitignore` has the exclusion rule
- ✅ Template file exists (safe)
- ✅ Private config file exists (excluded)
- ✅ Code loads from JSON (no hardcoded secrets)

**You're ready to use git safely!** 🎉
