# Example Configuration Setup

This file contains example configurations to help you set up your app.

## Example 1: Single Home with DuckDNS

If you have one home and use DuckDNS for remote access:

```
Configuration Name: Home
Internal URL: http://192.168.1.100:8123
External URL: https://myhome.duckdns.org
API Token: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
```

**When to use Internal:** When you're connected to your home WiFi
**When to use External:** When you're away from home

## Example 2: Multiple Homes

### Primary Residence (Belmont)

```
Configuration Name: Belmont House
Internal URL: http://192.168.1.100:8123
External URL: https://belmont.myhomes.duckdns.org
API Token: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUz...
```

### Vacation Home (Orlando)

```
Configuration Name: Orlando Vacation Home
Internal URL: http://192.168.2.50:8123
External URL: https://orlando.myhomes.duckdns.org
API Token: eyJ0eXBiOiJKV2QiLCJhbGdpOi...
```

## Example 3: With Nabu Casa

If you're using Home Assistant Cloud (Nabu Casa):

```
Configuration Name: My Home
Internal URL: http://homeassistant.local:8123
External URL: https://abcdefgh12345678.ui.nabu.casa
API Token: eyJ0eXAiOiJKV1QiLCJhbGciOi...
```

## Example 4: Custom Domain

If you have your own domain:

```
Configuration Name: Smart Home
Internal URL: http://192.168.0.150:8123
External URL: https://homeassistant.example.com
API Token: eyJ0eXAiOiJKV1QiLCJhbGciOi...
```

## Example 5: Multiple Locations (Family Setup)

### Your Home

```
Configuration Name: My House
Internal URL: http://192.168.1.100:8123
External URL: https://myhouse.example.com
API Token: [your token]
```

### Parents' Home

```
Configuration Name: Parents House
Internal URL: http://192.168.50.10:8123
External URL: https://parents.family.duckdns.org
API Token: [parents' token]
```

### Cabin

```
Configuration Name: Lake Cabin
Internal URL: http://192.168.3.100:8123
External URL: https://cabin.family.duckdns.org
API Token: [cabin token]
```

## Common URL Patterns

### Internal URLs (Local Network)

- `http://192.168.1.XXX:8123` - Most common
- `http://192.168.0.XXX:8123` - Alternative
- `http://10.0.0.XXX:8123` - Some routers use this
- `http://homeassistant.local:8123` - mDNS (works on most networks)

### External URLs (Remote Access)

- `https://XXXXX.duckdns.org` - DuckDNS (free)
- `https://XXXXX.ui.nabu.casa` - Nabu Casa (paid)
- `https://homeassistant.YOURDOMAIN.com` - Custom domain
- `https://home.YOURDOMAIN.net` - Alternative

**Important:**

- Internal URLs usually use `http://` (no SSL needed on local network)
- External URLs should use `https://` (SSL/TLS for security)

## Finding Your Internal URL

### Method 1: From Home Assistant

1. Go to Settings → System → Network
2. Look for "IPv4 Address"
3. Add `:8123` to the end
4. Example: If IP is `192.168.1.100`, URL is `http://192.168.1.100:8123`

### Method 2: Using mDNS

If your network supports mDNS (most do):

```
http://homeassistant.local:8123
```

### Method 3: From Your Router

1. Log into your router's admin page
2. Look for "Connected Devices" or "DHCP Clients"
3. Find the device named "homeassistant" or similar
4. Note its IP address
5. Add `:8123`

## Setting Up External Access

### Option 1: DuckDNS (Free, Recommended)

1. Go to https://www.duckdns.org
2. Create a free subdomain (e.g., `myhome.duckdns.org`)
3. Install DuckDNS add-on in Home Assistant
4. Set up port forwarding on your router (port 8123)
5. Configure SSL certificate in Home Assistant
6. Use `https://myhome.duckdns.org` as external URL

### Option 2: Nabu Casa (Easiest, Paid)

1. Subscribe to Home Assistant Cloud
2. Go to Configuration → Home Assistant Cloud
3. Note your remote access URL
4. Use that URL (looks like `https://abcd1234.ui.nabu.casa`)

### Option 3: Custom Domain

1. Own a domain (e.g., example.com)
2. Set up A record pointing to your home IP
3. Configure SSL certificate
4. Set up port forwarding
5. Use `https://homeassistant.example.com`

## Recommended Entity Selection

Here are some commonly useful entities to select for your dashboard:

### Lighting

- Living room lights
- Bedroom lights
- Kitchen lights
- Outdoor lights
- All smart bulbs you control frequently

### Climate

- Main thermostat
- Bedroom thermostat
- Portable heater/AC units

### Switches

- Coffee maker
- Fan switches
- Outlet switches for lamps
- Christmas lights (seasonal)

### Sensors (Read-only, but useful)

- Indoor temperature sensors
- Outdoor temperature
- Humidity sensors
- Energy monitoring sensors

### Security

- Door locks
- Garage door
- Security system status

### Other

- Media players (if supported)
- Fans
- Air purifiers

## Tips for Beginners

1. **Start Small**: Add one configuration first, test it, then add more
2. **Test Locally First**: Make sure internal URL works before worrying about external
3. **One Token Per Location**: Each Home Assistant instance needs its own token
4. **Be Patient**: First sync might take a moment, especially with many entities
5. **Select Wisely**: Only add entities you'll actually control from your phone

## Security Best Practices

1. ✅ Use HTTPS for external URLs
2. ✅ Use strong, unique API tokens
3. ✅ Don't share your tokens
4. ✅ Use a different token for each app/device
5. ✅ Rotate tokens periodically
6. ✅ Use a VPN when possible for extra security
7. ❌ Don't expose HTTP (non-SSL) to the internet
8. ❌ Don't use the same token everywhere

## Troubleshooting Examples

### "Failed to load entities"

**If using Internal:**

```
✓ Check: Are you on the same WiFi as Home Assistant?
✓ Check: Can you access the URL in your phone's browser?
✓ Check: Is the port :8123 included?
```

**If using External:**

```
✓ Check: Is your Home Assistant accessible from the internet?
✓ Check: Is your external URL correct?
✓ Check: Is SSL configured properly?
```

### "Entities load but controls don't work"

```
✓ Check: Is your API token valid?
✓ Check: Can you control the entity in the web interface?
✓ Check: Is the entity actually controllable (not read-only)?
```

### "Can't connect at all"

```
Try these URLs in your browser first:
1. http://192.168.1.100:8123 (replace with your IP)
2. http://homeassistant.local:8123
3. Your external URL

If browser works but app doesn't, check your token.
If browser doesn't work, fix Home Assistant access first.
```

## Need More Help?

- Check README.md for general information
- Check GETTING_STARTED.md for step-by-step guide
- Check ARCHITECTURE.md for technical details
- Visit Home Assistant documentation: https://www.home-assistant.io/docs/
