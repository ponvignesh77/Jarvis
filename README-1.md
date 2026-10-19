# JARVIS (Android)

A voice and text assistant for Android. It sends your request to Gemini, which replies with speech and, when needed, a phone action.

## Features

- Type or speak commands (Mic button uses the system speech recognizer)
- Spoken replies (Text-to-Speech, UK English voice when available)
- Phone actions chosen by Gemini:
  - Flashlight on and off
  - Open an installed app by name
  - Open the dialer for a number
  - Set an alarm
  - Web search
  - Open system settings

## Setup

1. Open this folder in Android Studio (Hedgehog or newer). Let it sync. It creates the Gradle wrapper automatically.
2. Create a file named `local.properties` in the project root (it is git-ignored) and add:
   ```
   GEMINI_API_KEY=your_key_here
   ```
3. Run the `app` configuration on a device running Android 7.0 (API 24) or newer.

## Limits

- Android does not allow a normal app to tap buttons or control other apps. Full hands-free control would need an Accessibility Service, which is not included here.
- The API key is compiled into the app. Fine for personal use. For distribution, move the key to a backend server.

## Project layout

```
app/src/main/java/com/example/jarvis/
  MainActivity.kt     UI, voice input, text-to-speech, action dispatch
  GeminiClient.kt     Gemini API call, returns reply and action as JSON
  PhoneActions.kt     Flashlight, apps, dialer, alarm, search, settings
```
