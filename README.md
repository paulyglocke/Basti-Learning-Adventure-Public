# Basti's Learning Adventure

Offline Android learning app designed for a young child preparing for school.

Current source is **hybrid Kotlin/Compose + legacy WebView**, with English/German learning activities and 53 verb lessons. Compose owns home, Options, native Prepositions, Seasons, Wilma and the Vocabulary starter slice; other learning activities remain in bundled HTML/JavaScript. The long-term target is fully native Compose without runtime WebView/HTML/JavaScript, migrated incrementally with tested parity before legacy removal.

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
