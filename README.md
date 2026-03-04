# HA Notif TV — Home Assistant Notification App for Android TV / Google TV

A **lightweight, minimal** Android TV / Google TV app that serves as a notification receiver and RTSP camera stream viewer for **Home Assistant (HA)** and **Frigate NVR** doorbell/camera events.

No bloat. No unnecessary dependencies. Just a clean, fast, D-pad navigable TV app.

---

## Screenshots

| Dashboard | Stream Player | Settings |
|-----------|---------------|----------|
| *(Camera grid + recent events)* | *(Full-screen RTSP stream)* | *(Configuration form)* |

---

## Features

- 📲 **Push Notifications** — Receives FCM notifications from Home Assistant
- 📷 **Frigate Integration** — Displays camera snapshots and recent detection events
- 📺 **RTSP Live Streams** — Full-screen ExoPlayer RTSP stream with auto-reconnect
- 🔔 **Overlay Notifications** — Popup overlay on TV when events occur (even in background)
- ⚙️ **Settings Screen** — Configure HA URL, Frigate URL, and RTSP camera streams
- 🎮 **D-pad Navigation** — Fully navigable with TV remote

---

## Prerequisites

### Home Assistant
- Home Assistant instance accessible on your local network
- Long-Lived Access Token (Profile → Security → Long-Lived Access Tokens)

### Frigate NVR (optional but recommended)
- [Frigate](https://frigate.video/) running and accessible on your local network
- Camera(s) configured in Frigate

### Firebase Cloud Messaging
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project (or use existing)
3. Add an Android app with package name `com.hanotif.tv`
4. Download `google-services.json` and place it in `app/`

---

## Build Instructions

### Requirements
- Android Studio Hedgehog or newer
- JDK 17+
- Android SDK 34

### Steps

```bash
# Clone the repository
git clone https://github.com/phdindota/GOOGLE-TV-NOTIF-HA.git
cd GOOGLE-TV-NOTIF-HA

# Copy and configure google-services.json
cp google-services.json.example app/google-services.json
# Edit app/google-services.json with your Firebase project values

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing config)
./gradlew assembleRelease
```

### Install on TV
```bash
# Via ADB (enable ADB debugging on your TV first)
adb connect <TV_IP_ADDRESS>
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Configuration Guide

### In-App Settings
Launch the app and press **Settings** to configure:

1. **Home Assistant URL** — e.g., `http://192.168.1.100:8123`
2. **HA Long-Lived Token** — Your HA access token
3. **Frigate URL** — e.g., `http://192.168.1.100:5000`
4. **Cameras** — Add one or more cameras with:
   - Camera name (e.g., "Front Door")
   - RTSP stream URL (e.g., `rtsp://192.168.1.100:8554/front_door`)

### Overlay Permission
On first launch, the app will request **"Display over other apps"** permission (required for notification overlays).

---

## Home Assistant Automation Example

Send notifications to the TV when Frigate detects a person at the front door:

```yaml
automation:
  - alias: "Frigate Person Detection - Notify TV"
    trigger:
      - platform: mqtt
        topic: frigate/events
    condition:
      - condition: template
        value_template: "{{ trigger.payload_json.type == 'new' }}"
      - condition: template
        value_template: "{{ trigger.payload_json.after.label == 'person' }}"
    action:
      - service: notify.mobile_app_ha_notif_tv
        data:
          title: "Person Detected"
          message: "{{ trigger.payload_json.after.camera }} - {{ trigger.payload_json.after.label }}"
          data:
            camera: "{{ trigger.payload_json.after.camera }}"
            event_id: "{{ trigger.payload_json.after.id }}"
            stream_url: "rtsp://192.168.1.100:8554/{{ trigger.payload_json.after.camera }}"
```

### FCM Notification Data Payload Format

The app accepts the following data fields in FCM messages:

| Field | Description | Example |
|-------|-------------|---------|
| `title` | Notification title | `"Person Detected"` |
| `message` | Notification body | `"Front Door camera"` |
| `camera` | Camera name | `"front_door"` |
| `event_id` | Frigate event ID | `"abc123xyz"` |
| `snapshot_url` | Direct snapshot URL | `"http://frigate:5000/api/events/abc123/snapshot.jpg"` |
| `stream_url` | RTSP stream URL | `"rtsp://192.168.1.100:8554/front_door"` |

---

## Frigate Integration

The app automatically constructs Frigate API URLs using your configured Frigate URL:

- **Events list**: `GET /api/events?limit=20`
- **Event snapshot**: `GET /api/events/{id}/snapshot.jpg`
- **Camera latest**: `GET /api/{camera}/latest.jpg`
- **Camera list**: `GET /api/config`

---

## App Architecture

```
app/src/main/java/com/hanotif/tv/
├── MainActivity.kt              # Single entry point
├── fragments/
│   ├── DashboardFragment.kt     # Camera grid + recent events
│   ├── StreamFragment.kt        # Full-screen RTSP player
│   └── SettingsFragment.kt      # Configuration screen
├── service/
│   └── FCMService.kt            # Firebase Cloud Messaging handler
├── notification/
│   └── NotificationHelper.kt   # System + overlay notifications
├── network/
│   ├── HAClient.kt              # Home Assistant REST API client
│   └── FrigateClient.kt        # Frigate API client
├── model/
│   ├── CameraStream.kt          # Camera data model
│   └── FrigateEvent.kt          # Frigate event data model
├── player/
│   └── RTSPPlayerManager.kt    # ExoPlayer RTSP lifecycle
├── view/
│   ├── CameraAdapter.kt         # Camera grid RecyclerView adapter
│   └── EventAdapter.kt          # Events list RecyclerView adapter
└── util/
    ├── PrefsManager.kt          # SharedPreferences wrapper
    └── Constants.kt             # App constants
```

---

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| `androidx.leanback` | 1.2.0 | TV theme & compatibility |
| `androidx.media3:exoplayer` | 1.3.0 | Video player engine |
| `androidx.media3:exoplayer-rtsp` | 1.3.0 | RTSP stream support |
| `androidx.media3:media3-ui` | 1.3.0 | PlayerView UI |
| `firebase-messaging` | 32.7.4 | FCM push notifications |
| `okhttp3` | 4.12.0 | HTTP client |
| `gson` | 2.10.1 | JSON parsing |
| `coil` | 2.6.0 | Image loading |

---

## License

```
MIT License

Copyright (c) 2024 phdindota

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```