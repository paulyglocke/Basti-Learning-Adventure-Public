# V1 verification and fixes

This file records build/test evidence and should not be treated as a product or architecture specification.

## Evidence scope

The V1 results below are historical verification from 2026-09-20, before the Compose shell and subsequent P0 work. They do not establish current native behavior. Source determines current implementation; master specs determine direction.

The [historical audit](TECHNICAL_AUDIT.md) records a later hybrid build at `290ff03`: build/lint passed with 0 errors / 6 warnings and 9 browser tests passed. The prior session handoff records the subsequent answer/audio isolation patch: build/lint passed and 12 browser tests passed. These results were not rerun during Documentation Pass 1 (2026-09-21).

No physical S24/Fire validation is recorded. Browser tests exercise legacy assets with mocked speech, not Compose, Android Back/recreation, device insets or actual TTS output.

## Historical hybrid audit verification (2026-09-20)

Retained from the prior handoff; these observations describe the audit revision, before the answer/audio isolation fix:

- `JAVA_HOME=/home/paul/.local/share/basti-tools/jdk17 ./gradlew assembleDebug lintDebug --console=plain`: successful, 0 lint errors / 6 warnings. First sandboxed attempt failed on network-interface initialization; escalated run passed in 2m41s.
- Existing Playwright suite: 9/9 passed. Uses mocked Android speech; does not establish actual device audio or Compose behavior.
- Focused bilingual matrix: seven quiz modes × English/German × all/questions/off. Automatic speech present in all/questions and absent in off; four answer speakers rendered.
- Tested scores 0/5, 2/5, 5/5, 10/10: generated star count and text agree. Double correct tap awards only once; Continue resets session. Ten-star visual layout still clips on phones.
- Confirmed keyboard speaker activation submits an answer; correct speaker + Enter sets score=1 and answered=true.
- Confirmed manual speech bypasses Sound Off; tutorials marked heard while off; fetch/chase select old animation family; preposition sentences reference wrong objects.
- `adb devices` outside sandbox: no devices attached. No physical S24/Fire validation.
- Latest published release is v1.0.0, an older artifact; current debug APK ~8.5MB versus tracked V1 APK ~48KB. Version remains code 1/name 1.0.


The subsequent P0 patch added English/German coverage across seven quiz modes for pointer/Enter/Space audio activation, Tab order and repeat scoring. The handoff recorded 12 browser tests passing in 2.8 minutes and a passing build/lint run.

## Historical V1 fixes (2026-09-20)

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

## Historical V1 verification

Verified on 2026-09-20 against the V1 artifact, not current main:

- Final Playwright run: **9 passed (1.0 minute)**.

- `./gradlew assembleDebug lintDebug` with the local complete JDK 17: **passed**.
  The checksum-pinned wrapper was used for the final build.
- Android lint: **0 errors, 3 warnings**. Two warnings note that newer manifest
  attributes are ignored on older Android releases; the third notes JavaScript
  is enabled in the intentional local WebView. See `verification/android-lint.txt`.
- APK signature validation: **passed**, with v1 and v2 signing.
- Packaged `assets/index.html` and `assets/app.js`: byte-for-byte identical to
  the source files used for that historical build.
- APK manifest inspection: Android 6+ (minimum API 23), target API 35,
  exported launcher Activity, and VIBRATE as the only permission. No network
  permission or Google Play Services runtime libraries.
- Offline browser checks cover persisted English/German settings, all seven
  quiz modes through completion, wrong-answer retries, same-question lesson
  return before/after answering, browser viewport resizing without regeneration, all 53 lessons
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

At historical verification time no Android phone/tablet was attached, and the
verification host lacked `/dev/kvm` for an accelerated Android emulator. Native launch, touch behavior, audio output,
system Back and insets must still be checked on the Samsung and Fire devices.
Browser tests exercise the actual packaged assets offline, with a mock native
speech bridge; they cannot prove the device's TTS engine or installed voices.

For spoken questions, install English and German offline voice data using the
device's TTS settings. Current Kotlin speech selection prefers a matching offline voice but does not
guarantee silence or a safe fallback when none is found. Missing-voice handling
and actual offline speech require device testing; visual activities and Hint remain available.

Artwork still uses system emoji and lightweight CSS. The emoji appearance and
availability depend on the device's fonts. Some lessons share general motion
families rather than bespoke illustrations of each action.

The original verification was local. The historical CI also built/linted the
source and ran the then-current nine browser tests. The version 1 release
contains that historical APK; it does not represent current main.
The published APK retains its original debug signature. CI build artifacts
use an ephemeral debug key and are separate test builds.

## Build/test commands

With JDK 17 and the Android SDK configured (see README.md):

```sh
./gradlew assembleDebug lintDebug
npm ci
npx playwright install chromium
npm test
```

Generated APK: `app/build/outputs/apk/debug/app-debug.apk`. Historical release
checksum and screenshots in `verification/` apply only to their original artifact.

## Technical references

The retained Android Gradle Plugin 8.7.3/Gradle 8.9 combination follows the
[Android compatibility table](https://developer.android.com/build/releases/agp-8-7-0-release-notes).
TTS discovery and offline voice filtering follow the
[Android TTS engine documentation](https://developer.android.com/reference/android/speech/tts/TextToSpeech.Engine).
