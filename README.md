# Standby

An Android take on Apple's iPhone StandBy mode: set your phone on a desk stand
(ideally charging, in landscape) and get a beautiful, glanceable display.

## Pages

Swipe horizontally between:

- **Clock** — four faces (Digital, Analog, Flip, Minimal) with per-digit roll
  animations
- **Now Playing** — album art, title/artist, play/pause/skip for whatever is
  playing in Spotify, YouTube, YouTube Music, etc., with an accent color pulled
  from the artwork and a blurred-art backdrop
- **Weather** — current conditions via Open-Meteo (no API key), from
  approximate location or a manually set city
- **Calendar** — upcoming events in the next 48 hours
- **Battery** — charge ring, percentage and charging state

Tap the screen to reveal the settings gear and a brightness slider. Between
22:00 and 07:00 an optional warm dim overlay kicks in. Everything runs on a
true-black background with subtle pixel drift to protect OLED panels.

## Building

```sh
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Requires JDK 17+ and the Android SDK (compileSdk 36). During development you
can grant notification access from the shell:

```sh
adb shell cmd notification allow_listener \
  com.adamwilkinson.standby/.data.media.MediaNotificationListener
```

## Stack

Kotlin + Jetpack Compose (Material 3), single activity, manual DI. Media via
`MediaSessionManager` + an empty `NotificationListenerService`; weather via
plain OkHttp + kotlinx.serialization; persistence via Preferences DataStore.
Fonts (Inter, Oswald variable) are bundled — no network dependency at runtime
except the weather fetch.
