# Basti's Learning Adventure

Offline Android learning app designed for a young child preparing for school.

Current source is **hybrid Kotlin/Compose + legacy WebView**, with English/German learning activities and 53 verb lessons. Compose owns home, Options and the shell; learning logic remains in bundled HTML/JavaScript. The long-term target is fully native Compose without runtime WebView/HTML/JavaScript, migrated incrementally with tested parity before legacy removal.

The links below refer to the **historical v1.0.0 APK**, not a build of current main.

[Download version 1](https://github.com/paulyglocke/Basti-Learning-Adventure-Public/releases/tag/v1.0.0)
· [Historical APK in this repository](releases/v1.0.0/Basti-Learning-Adventure-V1-debug.apk)
· [Build and test results](BUILD_NOTES.md)

The APK is debug-signed for sideload testing. Native launch and speech output
still need verification on the target Samsung and Fire devices.

## Implemented learning features
- Number practice is configurable from 1–10 up to 1–100.
- Legacy web Options/generators support custom maxima up to 9,999; native Options currently exposes presets only.
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

All five Play cards are currently inactive placeholders. Vocabulary Booster is not implemented and currently falls back to Animal Actions. Persistent skill tracking and the broader school-readiness curriculum are planned.

## Options
- English / German switch
- Audio guidance: all / questions and instructions / off (policy reliability remains backlog work)
- Replay activity introductions (reset reliability remains backlog work)
- 5 or 10 questions per round
- Number maximum presets: 10, 20, 50, 100
- Settings persist locally on the device

## Phone and tablet
Targets are Samsung Galaxy S24 phones and Amazon Fire Max tablets. Legacy learning pages use responsive CSS; native home currently uses fixed two-column cards. Full phone/tablet and landscape adaptation still needs work and physical validation.

The project has no Google Play Services dependency. Physical S24/Fire compatibility has not yet been verified.

## Offline
The app is offline-first and core learning must remain fully offline. All current learning content is stored in the APK; there is no INTERNET permission. Speech depends on installed English/German offline voices.

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

Current animations use local CSS/emoji and share some generic motion families; teaching quality and device rendering still need validation. The audio-first production direction uses custom artwork and native 2D/2.5D animation, with calm feedback and no punitive gamification.

## Building current source

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
Browser tests use mocked speech and do not verify Compose, Android lifecycle or physical audio.
See [BUILD_NOTES.md](BUILD_NOTES.md) for historical verification results and the
remaining checks on Samsung and Fire devices.

## Installing the APK

Copy `app/build/outputs/apk/debug/app-debug.apk` to the phone or tablet, open it,
and allow installation from the app used to open the file when Android asks.
This is a debug-signed APK for sideload testing.

Speech uses installed offline voices. Install English/German voice data in the
device's text-to-speech settings, then test in airplane mode. The learning app
itself does not download voice data. If audio is unavailable, the Hint button
also shows the answer to listening questions.

## Project guidance

Start with [AGENTS.md](AGENTS.md) and [CODEX_START_HERE.md](CODEX_START_HERE.md). The authoritative design files are [MASTER_PRODUCT_LEARNING_ROADMAP.md](MASTER_PRODUCT_LEARNING_ROADMAP.md), [PROGRESS_TRACKER_SPEC.md](PROGRESS_TRACKER_SPEC.md), [GAME_DESIGN_SPEC.md](GAME_DESIGN_SPEC.md), [VOICE_AUDIO_SPEC.md](VOICE_AUDIO_SPEC.md) and [ART_DIRECTION.md](ART_DIRECTION.md). See [BACKLOG.md](BACKLOG.md) for actionable work.
