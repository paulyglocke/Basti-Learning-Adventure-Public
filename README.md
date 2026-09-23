# Basti's Learning Adventure

Offline, bilingual Android school-readiness app built around short, calm, audio-first learning activities in English and German.

Current source is **hybrid Kotlin/Jetpack Compose + legacy WebView**. The native side now includes Home/Options plus end-to-end native **Prepositions, Seasons, Wilma’s Week and Vocabulary Booster**, backed by shared native content, audio, deterministic session/restoration and durable progress foundations. Legacy activities remain bundled locally in HTML/JavaScript while migration continues. The long-term target is fully native Compose with no runtime WebView/HTML/JavaScript dependency.

The links below refer to the **historical v1.0.0 APK**, not a build of current main.

[Download version 1](https://github.com/paulyglocke/Basti-Learning-Adventure-Public/releases/tag/v1.0.0)
· [Historical APK in this repository](releases/v1.0.0/Basti-Learning-Adventure-V1-debug.apk)
· [Build and test results](BUILD_NOTES.md)

The historical v1.0.0 APK is debug-signed. Current `main` also supports a separate **stable-signed distribution APK** for repeat physical testing and in-place upgrades without changing the application ID. Samsung S24 Ultra testing has covered key native navigation, lifecycle, 5/10-question completion and offline English/German speech paths; broader Samsung accessibility/content checks and Fire Max acceptance remain open.

## Current learning features

### Native Compose
- **Prepositions**: in/on/under/behind/next to/between with deterministic 5/10-question rounds, Help/Retry, shared audio, exact restoration and durable progress delivery.
- **Seasons**: Explore plus Practice using four canonical season scenes and authored English/German narration. The roadmap now also defines next/before, missing-season, clue matching and **Build the Year** ordering as the next expansion.
- **Wilma’s Week / Wilmas Woche**: familiar seven-day caterpillar with Explore, Find Day, Before/After and tap-to-place weekday ordering.
- **Vocabulary Booster**: first reviewed native slice with six bilingual animal words, Explore/examples, word-to-picture and picture-to-word practice. The current animal glyphs are temporary pending original illustrations.
- Shared native foundations provide semantic content IDs, English/German speech/display text, audio policy, deterministic sessions, checkpoint restoration and local durable progress events.

### Legacy bundled activities still available
- Animal actions / verbs and Verb Explorer
- Number practice with parent-selected maximum
- Addition and subtraction within 10
- First-letter matching
- Mixed practice
- Remaining calendar/legacy routes retained where migration parity is not yet retired

Number practice remains configurable from 1–10 up to 1–100 in native Options, while legacy generators support custom maxima up to 9,999. Higher number levels adapt between visual counting, listening/recognition, before/after, missing numbers, biggest/smallest and tens/ones instead of drawing hundreds of individual animals.

## Native migration status

The project is no longer only a WebView prototype. Four substantial learning areas now run natively in Compose:

- Prepositions
- Seasons
- Wilma’s Week
- Vocabulary Booster

These native activities use the shared session/audio/progress architecture rather than duplicating state logic per screen. Explore-only browsing does not create correctness events; practice rounds persist meaningful attempt/completion evidence locally and restore silently after supported navigation/recreation.

The wider school-readiness curriculum is still in progress. Near-term roadmap work includes richer Seasons sequencing/reasoning, Follow the Instructions, expressive-language activities, Memory Pairs, broader Vocabulary, Maths concepts, School Skills, Focus & Flex, Compare & Discover and larger native games.

A shared native completion celebration is also on the roadmap: gently floating balloons, tap-to-pop interaction, an animal jumping out, and a brief confetti burst, with Continue/Home always immediately usable.

## Options
- English / German switch
- Audio guidance: **All / Questions & Instructions / Off**
- Sound Off blocks manual Replay/Listen and completion SFX where implemented
- Replay/help controls use authored activity speech through the shared audio policy
- 5 or 10 questions per round
- Number maximum presets: 10, 20, 50, 100
- Settings persist locally on the device

## Phone and tablet
Primary physical targets are Samsung Galaxy S24-class phones and Amazon Fire Max tablets. Native screens are tested in portrait/landscape and include safe-inset and larger-font work, while the remaining physical acceptance matrix is tracked in [TESTING_QA_SPEC.md](TESTING_QA_SPEC.md).

Samsung S24 Ultra checks already cover important navigation/inset behavior, 5/10-question completion, one saved-task process-recovery path and user-confirmed offline English/German speech plus Sound Off. Fire Max validation and broader accessibility/audio/content/lifecycle coverage are still outstanding.

The project has no Google Play Services dependency.

## Offline
The app is offline-first and core learning must remain fully offline. All current learning content is stored in the APK; there is no INTERNET permission. Speech depends on installed English/German offline voices.

## Build
Open the project in a recent Android Studio and build the `app` module, or push to GitHub and use the included workflow.

Local development APK:

`app/build/outputs/apk/debug/app-debug.apk`

For ongoing physical testing, use the stable-signed distribution workflow described under **Installing the APK** rather than treating hosted debug artifacts as an upgrade stream.


## Verb Explorer (included in V1)
- 53 child-friendly animal verbs/actions.
- Every verb has English and German wording.
- Every verb has a short child-friendly explanation.
- Every verb has a simple example sentence.
- Every verb has an offline looping animated scene.
- The animation style changes by action (jumping, flying, swimming, slithering, running, sound-making, stomping, stretching, etc.).
- Verb quiz questions contain a **Learn this verb** button that opens the matching lesson and returns to the same question.
- Audio reads the verb, explanation and example using the device text-to-speech engine.

Current legacy Verb Explorer animations use local CSS/emoji and generic motion families. The production direction is custom artwork plus native 2D/2.5D action-specific animation; several local MP4 demonstrations are being prepared for future native verb work. Teaching quality and device rendering still require broader physical validation.

## Native architecture at a glance

The native migration is built around shared foundations rather than one-off screens:

- **Content**: stable semantic IDs, required EN/DE display/speech fields, deterministic canonical repositories and validation.
- **Audio**: one shared policy/controller with owned cancellation, explicit language/voice failures and silent restore.
- **Sessions**: immutable deterministic task plans, Help/Retry/support tracking, correct-once scoring and exact checkpoint restoration.
- **Progress**: bounded local event storage with stable dedupe keys and retryable delivery.
- **Navigation**: Compose owns native destinations while legacy WebView routes remain available during migration.
- **Offline-first**: no INTERNET permission is required for learning; system speech depends only on installed offline voices.

See [NATIVE_ARCHITECTURE_SPEC.md](NATIVE_ARCHITECTURE_SPEC.md), [CONTENT_DATA_SPEC.md](CONTENT_DATA_SPEC.md), [VOICE_AUDIO_SPEC.md](VOICE_AUDIO_SPEC.md) and [PROGRESS_TRACKER_SPEC.md](PROGRESS_TRACKER_SPEC.md) for the detailed contracts.

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

For ongoing physical testing use the **stable-signed distribution** artifact described below, not a CI debug APK. Copy the APK to the device and open it, allowing that installer when Android asks, or use `adb install -r path/to/app-release.apk`. Do not uninstall or clear app data between upgrades.

Local `assembleDebug` remains available without secrets and uses the machine's standard `~/.android/debug.keystore`. Its default version is code **1**, name **1.1-dev**. CI debug artifacts are named `Basti-debug-only-<run>-<attempt>`: fresh hosted runners do not share a persistent debug key, so these are not a reliable physical upgrade stream. The application ID remains `com.bellfamily.bastischool` for both build types; do not switch debug/distribution streams on a data-bearing device.

Speech uses installed offline voices. Install English/German voice data in the
device's text-to-speech settings, then test in airplane mode. The learning app
itself does not download voice data. If audio is unavailable, the Hint button
also shows the answer to listening questions.

## Stable signing setup (one time, owner action)

1. Create a dedicated long-lived signing keystore **outside this checkout**, using Android Studio → Build → Generate Signed Bundle/APK → APK → Create new. Keep the alias and passwords private. Choose at least 25 years validity, save an encrypted offline backup of the keystore and credentials, and retain its public certificate SHA-256 fingerprint. Do not regenerate the key for subsequent builds. The wizard can be cancelled after key creation; builds below use the repository's explicit signing configuration.
2. In GitHub repository Settings → Secrets and variables → Actions, add these **repository secrets** (no secret values belong in Git, workflow YAML, issues or logs):

   | Secret | Value |
   | --- | --- |
   | `BASTI_KEYSTORE_BASE64` | Base64 encoding of the entire private keystore file |
   | `BASTI_STORE_PASSWORD` | Keystore password |
   | `BASTI_KEY_ALIAS` | Key alias selected during creation |
   | `BASTI_KEY_PASSWORD` | Private-key password |

   On macOS, `base64 -i /absolute/private/path/basti-distribution.jks | pbcopy` copies the encoding without printing it. Paste it directly into the secret form, then clear the clipboard. Base64 is encoding, not encryption. Never upload the keystore as an artifact. Keep access to repository secrets and the default branch restricted to trusted maintainers.
3. Actions → **Build Android APK** → Run workflow → branch **main** → enable **distribution**. Ordinary push/PR builds do not need or receive signing secrets. The distribution job waits for JVM/debug/lint and browser jobs, decodes the key with restrictive permissions into runner temporary storage, signs `assembleRelease`, verifies the APK signature, and removes the decoded key on exit/cleanup. Signed builds disable Gradle configuration caching and run without a persistent daemon; no credentials are echoed. Private release signing is never used for PR code.
4. Download `Basti-stable-signed-v<versionCode>`. Verify its certificate fingerprint matches the saved identity before distribution. This is a non-debuggable release APK, not a Play upload requirement; no cloud runtime or Play Services is added.

Release packaging fails clearly if any signing field is missing, the file is absent, or an explicit distribution version code greater than 1 is not supplied. It never silently falls back to debug signing or an unsigned release.

### Version policy

CI derives `versionCode = 1_000_000 + 100 × GITHUB_RUN_NUMBER + GITHUB_RUN_ATTEMPT` and `versionName = 1.1.<run>.<attempt>`. Run numbers and attempts must be positive; attempt must be 1–99; the code must not exceed **2,100,000,000**. Reruns of the same run receive distinct increasing codes, and a new run exceeds all attempts of the previous run. Range exhaustion fails rather than wrapping or reusing a code. Python unit tests cover the boundaries.

Keep this workflow's version stream stable. **Never distribute an older run's rerun after a newer run has already been installed**: dispatch a new run instead. Download/install order can differ from build order; always check that the candidate code is greater than the device's installed code. If the workflow is renamed/recreated, the repository is recreated, or local signed builds use higher codes, explicitly advance the base after checking the highest distributed code. Do not reset the counter or use `adb -d` to force a downgrade.

Local signed builds read the following environment variables, or identically named properties in private `~/.gradle/gradle.properties`: `BASTI_KEYSTORE_PATH` (absolute path), `BASTI_STORE_PASSWORD`, `BASTI_KEY_ALIAS`, `BASTI_KEY_PASSWORD`, `BASTI_VERSION_CODE` (explicitly above the installed/distributed code), and optionally `BASTI_VERSION_NAME` (default `1.1`). Environment variables take precedence. Load credentials privately; do not put passwords in `-P` command arguments/shell history or tracked Gradle properties. Then run:

```sh
./gradlew assembleRelease --no-daemon --no-configuration-cache --console=plain
```

Prefer CI as the sole distribution version allocator. Ordinary development remains `./gradlew assembleDebug`; it does not require these credentials. Avoid switching back to a lower-code debug build over a distribution installation.

### Existing installations and data

An installed app can only update with a compatible signing certificate and package ID. A higher version code cannot fix a certificate mismatch. Earlier S24 evidence recorded `INSTALL_FAILED_UPDATE_INCOMPATIBLE`; CI runner debug keys were not retained by the workflow. A private signing key cannot be recovered from the certificate in an APK.

Before migration, compare the installed APK's public certificate against the intended key and look for the original keystore. If the original signing identity cannot be retained, **one final owner-approved uninstall/reinstall may be necessary**, and uninstall erases local data. There is currently no implemented cross-signature progress export/import. Do not do this silently or describe it as a data-preserving upgrade. After moving to one permanent key, subsequent same-key/higher-code installs should preserve data; both target devices still need the procedure in [TESTING_QA_SPEC.md](TESTING_QA_SPEC.md).

References: [Android app signing](https://developer.android.com/studio/publish/app-signing), [Android versioning](https://developer.android.com/studio/publish/versioning), [GitHub run variables](https://docs.github.com/en/actions/reference/workflows-and-actions/variables), [GitHub secret handling](https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/use-secrets).

## Project guidance

Start with [AGENTS.md](AGENTS.md) and [CODEX_START_HERE.md](CODEX_START_HERE.md). The authoritative design files are [MASTER_PRODUCT_LEARNING_ROADMAP.md](MASTER_PRODUCT_LEARNING_ROADMAP.md), [PROGRESS_TRACKER_SPEC.md](PROGRESS_TRACKER_SPEC.md), [GAME_DESIGN_SPEC.md](GAME_DESIGN_SPEC.md), [VOICE_AUDIO_SPEC.md](VOICE_AUDIO_SPEC.md) and [ART_DIRECTION.md](ART_DIRECTION.md). See [BACKLOG.md](BACKLOG.md) for actionable work.
