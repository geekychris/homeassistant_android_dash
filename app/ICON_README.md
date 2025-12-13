# HAbitat App Icon

## Source File

**Location**: `app/HA-bitat.png`  
**Size**: 1024×1024 pixels (1.1 MB)  
**Format**: PNG

This is the source file for the HAbitat app icon.

## Generated Icons

All Android icon sizes were generated from this source file:

### Mipmap Densities

| Density | Size | Location |
|---------|------|----------|
| mdpi | 48×48 | `app/src/main/res/mipmap-mdpi/ic_launcher.png` |
| hdpi | 72×72 | `app/src/main/res/mipmap-hdpi/ic_launcher.png` |
| xhdpi | 96×96 | `app/src/main/res/mipmap-xhdpi/ic_launcher.png` |
| xxhdpi | 144×144 | `app/src/main/res/mipmap-xxhdpi/ic_launcher.png` |
| xxxhdpi | 192×192 | `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png` |

## Regenerating Icons

If you need to regenerate the icons from a new source file:

```bash
cd /Users/chris/AndroidStudioProjects/SimpleHomeAssistant

# Generate all sizes
sips -z 48 48 app/HA-bitat.png --out app/src/main/res/mipmap-mdpi/ic_launcher.png
sips -z 72 72 app/HA-bitat.png --out app/src/main/res/mipmap-hdpi/ic_launcher.png
sips -z 96 96 app/HA-bitat.png --out app/src/main/res/mipmap-xhdpi/ic_launcher.png
sips -z 144 144 app/HA-bitat.png --out app/src/main/res/mipmap-xxhdpi/ic_launcher.png
sips -z 192 192 app/HA-bitat.png --out app/src/main/res/mipmap-xxxhdpi/ic_launcher.png
```

## Design Guidelines

### Requirements

- **Format**: PNG (no transparency for launcher icons)
- **Size**: Minimum 1024×1024 pixels
- **Shape**: Square (Android will apply masks)
- **Safe Zone**: Keep important content within center 80%

### Style

- Simple, recognizable design
- Good contrast for visibility
- Works at small sizes (48×48)
- Represents "Home Assistant" concept

## Current Icon

The current HAbitat icon represents:

- **Home concept**: Housing/habitat theme
- **Smart home**: Modern, tech-forward design
- **Brand identity**: "HA-bitat" wordplay

## Git

⚠️ **Note**: The source file `HA-bitat.png` is excluded from git (via `.gitignore`) as it's a large
binary file. The generated mipmap icons are included in the repository.

If you need the source file:

1. Export from design tool at 1024×1024
2. Save as `app/HA-bitat.png`
3. Regenerate mipmaps using commands above
