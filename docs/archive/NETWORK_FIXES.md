# Network and Hostname Resolution Fixes

## Problems Identified

1. **Hostname Resolution**: Android emulator cannot resolve hostnames that work on the host Mac
    - Emulator uses its own DNS (typically 10.0.2.3)
    - `.local` mDNS names don't work in emulator
    - Some internal hostnames may not be accessible

2. **App Crashing**: After "cannot resolve hostname" error, the app crashes
    - Likely from unhandled exceptions in coroutines
    - Network errors propagating to UI thread

## Solutions Implemented

### 1. Special IP for Emulator to Host Communication

- Use `10.0.2.2` to access the host machine from Android emulator
- This is a special alias that always points to the host

### 2. Better Exception Handling

- Wrap all network calls with proper error handling
- Prevent crashes from network errors
- Add proper coroutine exception handlers

### 3. Network Security Configuration

- Allow cleartext HTTP traffic for development
- Some Home Assistant setups use HTTP on local network

## Key Changes Needed

See the following files for detailed changes.
