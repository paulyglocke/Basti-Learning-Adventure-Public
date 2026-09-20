# V1 verification and fixes

## Architecture

One native Android Activity hosts local HTML, CSS and JavaScript in a WebView.
The Java bridge supplies device text-to-speech and vibration. Settings persist
in WebView local storage. There are seven quiz modes (including mixed practice),
a bilingual library of 53 action lessons, and an Options screen. Arithmetic
stays within 10; number recognition supports a maximum of 9,999 and object
counting is capped at 20. All app content and animations are local.

The canonical JavaScript is now `app/src/main/assets/app.js`, loaded by the
adjacent `index.html`. The duplicate root script and embedded copy were replaced
with this single packaged source.

## Changes

- Fixed invalid quoting in the quiz's Learn this verb action and lesson speech
  button (including the owl explanation containing `'hoo'`).
- Resizing now updates the layout indicator without regenerating questions,
  resetting answers, or replacing an open lesson with the library.
- Both lesson return buttons and Android Back return to the same quiz question.
  Repeated clicks on a correct answer cannot award extra points.
- Cancelled delayed speech when navigating away or backgrounding the app.
- Added Android TTS service discovery and selection of installed offline voices.
- Blocked WebView network loads and external navigation; retained no INTERNET
  permission and no Google Play Services dependencies.
- Added system-bar insets for Android 15 and preserved Back navigation there.
- Corrected repeated English maths wording, singular agreement in both languages,
  the German horse picture/word mismatch, plural counting questions and German
  reflexive wording for slithering. These are corrections to simple existing
  content; arithmetic difficulty has not increased.
- Weekday questions now include the Sunday/Monday boundary.
- Mixed rounds draw from shuffled sets so a short round contains varied activities.
- Validated stored language/round/number settings and bounded small choice sets.
- Made spatial scenes clearer with CSS objects and proper occlusion for in/behind;
  under now uses a table, and between uses two rocks.
- Added distinct upside-down, climbing and curling animation variants.
- Improved narrow-screen text wrapping, button sizes, accessible button labels,
  live feedback, switch state and reduced-motion support.
- Escaped the Android app-name apostrophe, added a Gradle wrapper, limited local
  build memory, and updated CI to install explicit SDK packages, build and lint
  with the wrapper, and fail if the APK is missing.
- Added repeatable Playwright checks as development dependencies only.

## Verification

Verified on 2026-09-20:

- Final Playwright run: **9 passed (1.0 minute)**.

- `./gradlew assembleDebug lintDebug` with the local complete JDK 17: **passed**.
  The checksum-pinned wrapper was used for the final build.
- Android lint: **0 errors, 3 warnings**. Two warnings note that newer manifest
  attributes are ignored on older Android releases; the third notes JavaScript
  is enabled in the intentional local WebView. See `verification/android-lint.txt`.
- APK signature validation: **passed**, with v1 and v2 signing.
- Packaged `assets/index.html` and `assets/app.js`: byte-for-byte identical to
  the final source files.
- APK manifest inspection: Android 6+ (minimum API 23), target API 35,
  exported launcher Activity, and VIBRATE as the only permission. No network
  permission or Google Play Services runtime libraries.
- Offline browser checks cover persisted English/German settings, all seven
  quiz modes through completion, wrong-answer retries, same-question lesson
  return before/after answering, rotation without regeneration, all 53 lessons
  in both languages including replay buttons, manual speech with automatic
  speech off, cancellation on exit, and invalid stored settings.
- Number checks exercise 2,100 generated questions across ceilings 10, 20, 50,
  100, 101, 200 and 9,999, plus 2,100 arithmetic questions. Choices remain unique
  and within bounds; object counts stay at 20 or below; arithmetic stays within 10.
- Layout checks cover 320×700, 360×800, 800×1280 and 1280×800 in both languages,
  including all lessons. Phone/tablet previews are in `verification/`.

APK output: `app/build/outputs/apk/debug/app-debug.apk`.
Published APK: `releases/v1.0.0/Basti-Learning-Adventure-V1-debug.apk`.
Its SHA-256 is recorded in `verification/apk-sha256.txt`.

## Device checks still required

No Android phone/tablet is attached, and this computer has no `/dev/kvm` for an
accelerated Android emulator. Native launch, touch behavior, audio output,
system Back and insets must still be checked on the Samsung and Fire devices.
Browser tests exercise the actual packaged assets offline, with a mock native
speech bridge; they cannot prove the device's TTS engine or installed voices.

For spoken questions, install English and German offline voice data using the
device's TTS settings. Without a matching installed local voice, speech remains
silent; visual activities and the Hint button remain available.

Artwork still uses system emoji and lightweight CSS. The emoji appearance and
availability depend on the device's fonts. Some lessons share general motion
families rather than bespoke illustrations of each action.

The original verification was local. GitHub Actions now builds and lints the
source independently; a separate browser job runs the nine Playwright tests.
The version 1 release includes the already verified APK.
The published APK retains its original debug signature. CI build artifacts
use an ephemeral debug key and are separate test builds.

## Technical references

The retained Android Gradle Plugin 8.7.3/Gradle 8.9 combination follows the
[Android compatibility table](https://developer.android.com/build/releases/agp-8-7-0-release-notes).
TTS discovery and offline voice filtering follow the
[Android TTS engine documentation](https://developer.android.com/reference/android/speech/tts/TextToSpeech.Engine).
