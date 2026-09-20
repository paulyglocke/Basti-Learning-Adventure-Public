# Basti's Learning Adventure — Version 1

Offline Android learning app designed for a young child preparing for school.

**Version 1.0.0** — English/German learning games, 53 verb lessons and offline audio support.

[Download version 1](https://github.com/paulyglocke/Basti-Learning-Adventure-Public/releases/tag/v1.0.0)
· [Verified APK in this repository](releases/v1.0.0/Basti-Learning-Adventure-V1-debug.apk)
· [Build and test results](BUILD_NOTES.md)

The APK is debug-signed for sideload testing. Native launch and speech output
still need verification on the target Samsung and Fire devices.

## Version 1 features
- Number practice is configurable from 1–10 up to 1–100.
- Parents can enter a custom maximum up to 9,999 for later learning.
- Higher number levels do not show 100 individual animals. The app adapts to:
  - visual counting (mainly up to 20)
  - number recognition by listening
  - before / after
  - missing numbers
  - biggest / smallest
  - tens and ones (when working up to 100)
- Animal verbs/actions expanded from 12 to more than 50 child-friendly items.
- Counting and maths wording was cleaned up for English and German plurals.
- Added "between / zwischen" to position practice.

## Activities
- Animal actions / verbs
- Number practice with a parent-selected maximum
- Addition and subtraction within 10
- Prepositions: in, on, under, behind, next to, between
- First-letter matching
- Days of the week
- Seasons
- Mixed practice mode

## Options
- English / German switch
- Read questions aloud on/off
- 5 or 10 questions per round
- Number maximum presets: 10, 20, 50, 100
- Custom number maximum: 101–9,999
- Settings persist locally on the device

## Phone and tablet
One adaptive app. CSS media queries automatically use a compact layout on phones and a larger multi-column layout on tablets and in landscape mode.

The project has no Google Play Services dependency, so it is suitable for normal Android phones and Amazon Fire OS devices that support APK sideloading.

## Offline
All learning content is stored in the APK. No network permission or internet connection is required.

## Build
Open the project in a recent Android Studio and build the `app` module, or push it to GitHub and use the included workflow.

Debug APK:
`app/build/outputs/apk/debug/app-debug.apk`


## Verb Explorer (included in V1)
- 53 child-friendly animal verbs/actions.
- Every verb has English and German wording.
- Every verb has a short child-friendly explanation.
- Every verb has a simple example sentence.
- Every verb has an offline looping animated scene.
- The animation style changes by action (jumping, flying, swimming, slithering, running, sound-making, stomping, stretching, etc.).
- Verb quiz questions contain a **Learn this verb** button that opens the matching lesson and returns to the same question.
- Audio reads the verb, explanation and example using the device text-to-speech engine.

The V1 animations are intentionally local/CSS based: they run offline, add no Google dependency, and work in the same APK on Samsung Android and Amazon Fire OS.

## Building this checked version

Requirements: a complete **JDK 17** (including `javac` and `jlink`), Android SDK
platform 35 and build-tools 34.0.0. Point `local.properties` at your SDK with
`sdk.dir=/your/android/sdk`, or set `ANDROID_HOME`.

```sh
./gradlew assembleDebug lintDebug
```

On Windows use `gradlew.bat`. Android Studio can import this folder directly.
The wrapper pins Gradle 8.9 and verifies its distribution checksum. The first
build needs internet to download development tools; the installed app does not.

Set `JAVA_HOME` to your complete JDK 17 installation if your default Java is
only a runtime. Keep your SDK location in the ignored `local.properties` file.

## Browser verification

Node.js 18+ is needed only for development checks, not for building/running the
Android app. Tests load the packaged files with the browser offline.

```sh
npm ci
npx playwright install chromium
npm test
```

On a minimal Linux CI host use `npx playwright install --with-deps chromium`.
The test configuration uses one browser worker to limit memory usage.

The packaged source files are `app/src/main/assets/index.html` (markup/styles)
and `app/src/main/assets/app.js` (content/behavior).
See [BUILD_NOTES.md](BUILD_NOTES.md) for changes, verification results and the
remaining checks on Samsung and Fire devices.

## Installing the APK

Copy `app/build/outputs/apk/debug/app-debug.apk` to the phone or tablet, open it,
and allow installation from the app used to open the file when Android asks.
This is a debug-signed APK for sideload testing.

Speech uses installed offline voices. Install English/German voice data in the
device's text-to-speech settings, then test in airplane mode. The learning app
itself does not download voice data. If audio is unavailable, the Hint button
also shows the answer to listening questions.
