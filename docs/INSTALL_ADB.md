# Install ADB Command Line Tools

## Good News!

**ADB is already installed** on your Mac at:

```
/Users/chris/Library/Android/sdk/platform-tools/adb
```

It just needs to be added to your PATH so you can use it from any terminal.

## Quick Setup (One-Time)

### Option 1: Automatic (Recommended)

Copy and paste this entire command into your terminal:

```bash
echo '' >> ~/.zshrc
echo '# Android SDK Platform Tools' >> ~/.zshrc
echo 'export PATH=$PATH:$HOME/Library/Android/sdk/platform-tools' >> ~/.zshrc
source ~/.zshrc
```

Then verify it works:

```bash
adb version
```

### Option 2: Manual

1. Open your terminal
2. Edit your zsh configuration:
   ```bash
   nano ~/.zshrc
   ```
3. Add this line at the end:
   ```bash
   export PATH=$PATH:$HOME/Library/Android/sdk/platform-tools
   ```
4. Press `Ctrl+X`, then `Y`, then `Enter` to save
5. Reload your shell:
   ```bash
   source ~/.zshrc
   ```

## Verify Installation

After setup, run:

```bash
adb version
```

You should see something like:

```
Android Debug Bridge version 1.0.41
Version 35.0.0-...
```

## Test It

Check for connected devices:

```bash
adb devices
```

You should see:

```
List of devices attached
emulator-5554          device
```

(Your tablet will appear here once connected)

## Quick Reference

Common ADB commands:

```bash
adb devices              # List connected devices
adb install app.apk      # Install an app
adb uninstall com.pkg    # Uninstall an app
adb logcat              # View device logs
adb shell               # Open device shell
```

---

**Once you've run the setup command, adb will work from any terminal!**
