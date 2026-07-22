# Android Standby Mode

An Android app that turns the phone into an aesthetic, glanceable display when it's charging, or placed on it's side. Inspired by iPhone's Standby mode.

## Pages

Swipe between widget options:

- **Clock** — (the default) four clock faces available
- **Now Playing** — displays media being played whether that is Spotify, YouTube, or any other media player
- **Weather** — current conditions via Open-Meteo api
- **Calendar** — upcoming events in the next 48 hours
- **Battery** — percentage and charging status

Tap the screen to reveal the settings gear and a brightness slider. 
Between 22:00 and 07:00 an optional warm dim overlay is used. 
OLED Support: Everything runs on a true black background with subtle pixel drift.

## Building

1. Connect your phone via USB with USB debugging enabled.
2. Run the following command from within the project directory:

​```
./gradlew installDebug
​```

Requires JDK 17+ and Android SDK. 

