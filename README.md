# JARVIS v1.4 (starter project)

Android Studio project targeting Android 6.0+ (minSdk 23), with Android 9 compatibility target. Includes a dark blue interface, Android speech-recognition activity, Android Text-to-Speech, basic local commands, local note-style memory, volume controls, and YouTube/Google search intents. Vision mode is not included.

## Build
Open this folder in Android Studio, allow Gradle sync, then Build > Build Bundle(s) / APK(s) > Build APK(s).

Alternatively, upload the project contents to a GitHub repository (including `.github/workflows/build-apk.yml`), then run the **Build JARVIS APK** workflow under Actions. Download the `JARVIS-debug-APK` artifact and extract `app-debug.apk`.

## Honest limitations
This is a starter implementation, not a full always-on assistant. Speech recognition is launched when the user taps TALK TO JARVIS; it does not run a persistent wake-word listener or guarantee screen-off listening. Speech recognition may require network access depending on the phone's installed recognizer. Offline commands only work if speech-to-text can recognize the words offline. YouTube voice search opens results; it does not guarantee autoplay of a selected video. This build does not include online LLM chat, alarms/reminders, calls/SMS sending, reboot startup, or a dedicated Jarvis voice. The voice is Android's installed TTS voice with a slightly slower/lower default setting. Local memory stores user-entered facts in app preferences on this device.

The GitHub workflow is provided but has not been executed here. No APK is included in this ZIP.
