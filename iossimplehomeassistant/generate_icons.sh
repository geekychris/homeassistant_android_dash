#!/bin/bash

# Icon Generator for HAbitat iOS App
# This script generates all required iOS app icon sizes from a source image

SOURCE_IMAGE="$1"
ASSETS_DIR="/Users/chris/AndroidStudioProjects/SimpleHomeAssistant/iossimplehomeassistant/SimpleHomeAssistant/SimpleHomeAssistant/Assets.xcassets/AppIcon.appiconset"

if [ -z "$SOURCE_IMAGE" ]; then
    echo "Usage: $0 <source-image.png>"
    echo ""
    echo "Example: $0 HAbitat.png"
    echo ""
    echo "The source image should be at least 1024x1024 pixels"
    exit 1
fi

if [ ! -f "$SOURCE_IMAGE" ]; then
    echo "❌ Error: Source image '$SOURCE_IMAGE' not found"
    exit 1
fi

echo "🎨 Generating iOS app icons for HAbitat..."
echo "Source: $SOURCE_IMAGE"
echo "Output: $ASSETS_DIR"
echo ""

# Check if sips command is available (built-in on macOS)
if ! command -v sips &> /dev/null; then
    echo "❌ Error: 'sips' command not found (should be available on macOS)"
    exit 1
fi

# Create output directory if it doesn't exist
mkdir -p "$ASSETS_DIR"

# iOS App Icon Sizes (based on Contents.json requirements)
declare -A SIZES=(
    ["20"]="Icon-20"
    ["29"]="Icon-29"
    ["40"]="Icon-40"
    ["58"]="Icon-58"
    ["60"]="Icon-60"
    ["76"]="Icon-76"
    ["80"]="Icon-80"
    ["87"]="Icon-87"
    ["120"]="Icon-120"
    ["152"]="Icon-152"
    ["167"]="Icon-167"
    ["180"]="Icon-180"
    ["1024"]="Icon-1024"
)

# Generate each size
for size in "${!SIZES[@]}"; do
    output_file="$ASSETS_DIR/${SIZES[$size]}.png"
    echo "Generating ${size}x${size} → ${SIZES[$size]}.png"
    sips -z "$size" "$size" "$SOURCE_IMAGE" --out "$output_file" > /dev/null 2>&1
    
    if [ $? -eq 0 ]; then
        echo "  ✅ ${SIZES[$size]}.png"
    else
        echo "  ❌ Failed to generate ${SIZES[$size]}.png"
    fi
done

echo ""
echo "✅ Icon generation complete!"
echo ""
echo "Next steps:"
echo "1. Open the Xcode project"
echo "2. Clean build folder (⌘⇧K)"
echo "3. Build and run (⌘R)"
echo ""
echo "The HAbitat icon should now appear on the home screen!"
