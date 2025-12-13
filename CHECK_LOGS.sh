#!/bin/bash

# Script to monitor entity selection and dashboard logs
export PATH=$PATH:$HOME/Library/Android/sdk/platform-tools

echo "=========================================="
echo "Monitoring Entity Selection & Dashboard"
echo "=========================================="
echo ""
echo "Instructions:"
echo "1. Select some entities on 'Select Entities' tab"
echo "2. Go to Dashboard and tap 'Refresh'"
echo "3. Watch the output below"
echo ""
echo "Press Ctrl+C to stop monitoring"
echo ""
echo "=========================================="
echo ""

adb logcat -c  # Clear old logs
adb logcat | grep -E "EntitySelection|DashboardViewModel"
