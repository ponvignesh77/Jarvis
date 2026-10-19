# JARVIS HUD

A Stark Industries style HUD with voice output, voice commands, and Gemini chat, in a single HTML file.

## Run locally

Open `index.html` in Chrome or Edge. Click **Activate JARVIS**, then paste your Gemini API key.

## Features

- Animated arc reactor HUD with cycling status banner
- Speech output using the browser's speech synthesis
- Voice input (Chrome and Edge)
- Voice commands: "status report", "what time is it", "mute", "unmute", "stop"
- Gemini chat using the `generateContent` endpoint, model configurable in the page

## Security

The API key is entered in the page and sent directly from the browser to Google. Anyone who can open the page can see the key while it is in use, so do not deploy this publicly with a personal key. Use a backend proxy for public deployments.

## Limits

A web page cannot control phone hardware or other apps. That requires a native Android app.

## Deploy with GitHub Pages

1. Push this folder to a GitHub repository.
2. In the repository, go to Settings, then Pages.
3. Under Source, choose the `main` branch and the root folder, then save.
