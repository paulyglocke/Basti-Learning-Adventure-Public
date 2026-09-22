# V1 verification and fixes

This file records build/test evidence and should not be treated as a product or architecture specification.

## Samsung physical P0 pass and landscape top-bar fix — 2026-09-22

Source: `6e44e10` plus `fix: keep native navigation clear of landscape system bars`. Device: **Samsung Galaxy S24 Ultra SM-S928B, Android 16 / API 36**, 1440×3120 physical resolution, density override 560 dpi (reported physical density 600), three-button system navigation. Tested portrait and both landscape directions, system font scales 1.0 and 1.3. This is a partial Samsung acceptance pass, not complete S24/Fire release validation.

The normal APK could not update the existing installed app: `INSTALL_FAILED_UPDATE_INCOMPATIBLE` (different signing key). The original installation/data were preserved. A temporary checkout built the same source with **only applicationId changed to `com.bellfamily.bastischool.p0qa`**, then installed it alongside the existing app. No package-ID change was made in the repository. The final QA and normal builds have identical MainActivity source and all three packaged web assets. The production-package upgrade path remains unverified. The QA copy remains installed; the original app was not uninstalled or cleared.

Physical finding/fix: the native top bar applied only status-bar insets. In landscape, the German Options label extended to x=3022 while the right system navigation bar began at x=2952, visibly covering part of Options. The top bar now applies the top and horizontal safe-drawing insets, including side navigation/cutouts, without bottom padding. This follows the [Material 3 top-bar inset contract](https://developer.android.com/develop/ui/compose/system/material-insets). After the fix, the full Options touch target is [2422,133]–[2938,301], clear of the right bar [2952,0]–[3120,1440]. Reverse landscape and portrait measurements also passed for both Options and Back. Home/Options were physically retested on the corrected build. No learning/audio/animation logic changed.

Device evidence (ADB UI input, UIAutomator hierarchy/screenshots and live debug-WebView state comparisons; no mocked audio engine):

- Home → Options → system Back returned Home. Quiz → Options → system/visible Back preserved the unanswered question, then the answered question, choices, answer lock and score. English → German translated the same “climb/klettern” question with score 1 and correct answer monkey retained; German Options round-trip also preserved it.
- Quiz-linked lesson → Options → Back returned the lesson; another Back returned the exact answered quiz. Library → Options → Back and standalone lesson → library → native Home passed. Back at native Home returned to the Samsung launcher.
- Rotation and a system font-scale change preserved the answered quiz. Backgrounded the app, then `am kill com.bellfamily.bastischool.p0qa`; confirmed no remaining app PID before reopening the existing task. PID changed **27592 → 31395**. It restored native Options with the same hidden quiz; Back returned the exact question DOM/state/score. No active speech or pending narration remained after restoration. This is actual saved-task process recovery, not force-stop/relaunch.
- Completed the existing five-question round (5/5), then used Continue with the native ten-question preference and completed 10/10; ten reward stars appeared. Landscape completion kept Replay/Continue/Home above the reward area. Screenshots of the English quiz and German Options at font scale 1.3 were reviewed; long quiz content remains scrollable. The WebView keeps its existing textZoom=100 policy, so native system-font enlargement is not a claim of equivalent web-text enlargement.
- The user confirmed clear **English narration**, clear **German Replay in airplane mode with Wi-Fi off**, and clear **English “jump” lesson playback in airplane mode with Wi-Fi off**. Device logs show the app selected/bound `com.google.android.tts` (Samsung's private engine was unavailable to the app). Exact installed voice IDs were not extracted; no Samsung-engine compatibility claim is made.
- The user confirmed complete silence during **Sound Off** Replay, option speaker, answers, completion and balloon interaction. Live state showed no active speech, no created balloon AudioContext and zero active tones. Narration timers settled without playback. This is an audible spot-check, not exhaustive missing-voice/interruption coverage.
- Original device settings restored and read back: airplane mode 0, Wi-Fi 1, mobile data 1, font scale 1.0, accelerometer rotation 0, user rotation 0. No global speech-engine/voice settings changed. An intermittent UIAutomator dump termination/missing-node result was retried against the live UI; it was not counted as an app failure or a pass.

Automated validation of the fix:

- `npm test -- --reporter=line`: **55 passed / 0 failed** (50.2s); existing tests unchanged.
- `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/paulbell/Library/Android/sdk' ./gradlew testDebugUnitTest assembleDebug lintDebug --console=plain`: **BUILD SUCCESSFUL** (11s), unit XML **15 passed / 0 failures / 0 errors / 0 skipped**; assembleDebug/lintDebug passed.
- Lint: **0 errors / 7 warnings / 2 informational findings** (unchanged). Existing VIBRATOR_SERVICE compiler deprecation remains.
- Separate QA build: assembleDebug passed (4s); upgrade of the QA copy succeeded. Normal debug APK SHA-256: `cbeaff6529e67999b32803eb962728e0666f9956573dac66c93597f4dc076266`. Physically tested fixed QA APK SHA-256: `7c2181dd902e1da6c1b8466fd1742799d50a4dfd5db714e705125c104c637777`.
- `git diff --check`: passed. The regression for this small platform-layout fix is the measured physical inset matrix above; no test that merely mirrors the inset expression was added.

Remaining physical P0: Amazon Fire Max (not connected/tested); Samsung gesture navigation, TalkBack/Switch Access and full touch/large-font/content matrix; missing-voice behavior, Questions Only and exhaustive speech-safety/interruption/reset cases; all activity/number/calendar boundaries and all recreation origins/languages. Audio spot-checks and one real process-recovery case do not close these wider matrices. No neural TTS, animation/video assets or broad native migration was started. No push performed.

## P0 native navigation/session recovery — 2026-09-21

Source: clean `main` at `a7dd258` plus local checkpoint `fix: preserve sessions across navigation and recreation`. No push requested or performed.

Audit: native Options destroyed the current WebView and always returned Home; the shell had no saved route/session. Language changes regenerated a random question under the existing score/answer state. Existing lesson-aware web Back, speech cancellation, tutorial reset and WebView disposal were retained.

Changes: one hidden/paused WebView survives Options; Home disposes it. Native route/Options origin and the latest synchronous bridge checkpoint are stored in Android saved state. Versioned, bounded recovery reproduces the question/choices, round/index, score, answer/feedback state and lesson origin, without replaying speech or awarding answers. Language changes retain question identity; round changes apply to the next round and number-range changes to subsequent questions. Invalid recovery returns Home with bilingual restart guidance. Back requests are coalesced and obsolete callbacks ignored. See NATIVE_ARCHITECTURE_SPEC.md for compatibility/version policy and limits.

Validation:

- Focused `npm test -- tests/navigation.spec.js tests/audio-reliability.spec.js --reporter=line`: **24 passed / 0 failed** (10.3s).
- Full `npm test -- --reporter=line`: **55 passed / 0 failed** (47.9s).
- New browser coverage exercises every quiz in EN/DE, answered/unanswered questions, hints/retries, five-/ten-question progress, repeated Options/Back, Home/library/lesson origins, fresh-page checkpoint restoration, hidden native-shell restoration, completion, language/range changes, silent resume/Replay, no double score and malformed/incompatible/inconsistent checkpoint rejection. All 44 prior browser tests remain unchanged.
- `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/paulbell/Library/Android/sdk' ./gradlew testDebugUnitTest assembleDebug lintDebug --console=plain`: **BUILD SUCCESSFUL**. Final run 3s; native compilation/unit execution succeeded in the preceding 10s run, and was UP-TO-DATE in the final asset-only rebuild.
- Native unit XML: **15 passed / 0 failures / 0 errors / 0 skipped** (8 navigation/checkpoint/Back tests plus 7 preserved audio tests). These test production pure Kotlin policies; they do not instantiate Compose, WebView or Android saved-state lifecycle.
- `assembleDebug`: **passed**. `lintDebug`: **passed, 0 errors / 7 warnings / 2 informational findings**. Warning categories unchanged: OldTargetApi (1), UnusedAttribute (2), GradleDependency (3), SetJavaScriptEnabled (1); information: AutoboxingStateCreation (2). Kotlin retains the existing VIBRATOR_SERVICE deprecation warning.
- APK `app/build/outputs/apk/debug/app-debug.apk` SHA-256: `761096405f4e36c8e19dcfa3b1dda5d156ff68a99568f53d0f8a34d81ba4f2f2`. Packaged `app.js`, `index.html` and `legacy-session.js` match source byte-for-byte.
- `git diff --check`: passed.
- Intermediate checks caught and corrected a Kotlin receiver/name collision, restored correct-answer CSS ordering, a test fixture's retained round setting and inconsistent completion checkpoint acceptance. No existing test was weakened.

No physical Samsung S24 or Amazon Fire Max validation was performed. Remaining checks: system/visible Back and rapid Options interactions from Home, unanswered/answered quizzes, quiz-linked lessons, library and completion; rotate/recreate/background and recover an OS-saved task with each origin, in both languages; verify exact score/answer lock, one learning surface, silent resume and actual offline audio. Browser reload and pure JVM tests are not physical lifecycle proof. Force-stop/new-task recovery and durable Progress Tracker records are outside this legacy saved-state implementation. Broader P0 device layout/insets/font/accessibility/audio acceptance remains outstanding. No neural TTS, animation/video assets or broad Compose migration work was started.

## P0 completion/reward layout — 2026-09-21

Source: clean `ff57c8b` plus local checkpoint `fix: keep completion actions visible on small screens`.

Completion now places Replay and Continue/Home before the reward area. Individual star spans wrap without clipping; the plain localized summary remains accessible while decorative trophy/stars are hidden from screen readers. Optional balloons occupy their own region below the rewards and cannot cover navigation. Duplicate web headers/quiz controls are hidden only during completion and restored on leaving/restarting. Completion resets scroll and focuses its heading, so keyboard navigation reaches Replay, Continue and Home before balloons. Scoring, reward count and speech policy are preserved.

Validation:

- Focused `npm test -- tests/completion.spec.js --reporter=line`: **4 passed / 0 failed** (2.9s). Layout matrix: EN/DE × scores 0/5, 2/5, 5/5, 10/10 × six viewports (320×480, 360×640, 568×320, 640×240, 800×1280, 1280×800). Checks cover star containment/wrapping, immediate unobstructed ≥48px controls, exact summary, no horizontal overflow and scroll reset.
- Additional coverage: 200% CSS text in three small viewports, reduced motion, keyboard order/summary Replay, immediate Continue/Home without balloon interaction, and restored quiz layout after Continue. CSS text enlargement is browser evidence, not Android system-font validation.
- Full `npm test -- --reporter=line`: **39 passed / 0 failed** (39.5s), including existing audio, Prepositions and learning regressions.
- Visual review: German ten-star completion at 320×480 and 568×320, plus 200% text at 640×240. Navigation remains visible; optional rewards/summary may extend below the fold and remain scrollable on very short/large-text screens.
- Android Studio JDK 21: `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/paulbell/Library/Android/sdk' ./gradlew testDebugUnitTest assembleDebug lintDebug --console=plain`: **BUILD SUCCESSFUL** (3s), assembleDebug/lintDebug passed. Native unit task was UP-TO-DATE with its unchanged **7 passing tests**.
- Lint: **0 errors / 7 warnings / 2 informational findings**, unchanged categories from the audio checkpoint.
- APK assets match `app.js` and `index.html` byte-for-byte. APK SHA-256: `bbd255dcc53400899ab4963ae5b3a90d287679672d5b3478b56dcca37b4c4321` at `app/build/outputs/apk/debug/app-debug.apk`.
- Initial sandboxed Chromium launch failed on permissions. Authorized runs passed; an intermediate focused run was interrupted to correct the test's repeated fresh-shell setup.

No physical Samsung S24/Amazon Fire Max testing was performed. Device insets, real system font scaling, TalkBack/touch and both orientations remain outstanding. No neural TTS or native migration was started. Next implementable P0: Letters display-case consistency and bilingual initial-letter/sound review.

## P0 Prepositions correctness — 2026-09-21

Source: clean `231f875` plus the local checkpoint `fix: align preposition scenes and bilingual narration`. No native migration or neural audio work.

Changes: six relation records now bind the reference object/count to authored EN/DE phrases. Spoken choices are the actual four displayed choices in display order (including “in” when offered). Correct feedback and hints name the table, box or two rocks accurately, using German dative phrases and capitalized subjects. Scene accessibility text describes the same relation. Responsive spacing prevents next-to clipping/between overlap; the animal stays within the table legs in under scenes.

Validation:

- Focused `npm test -- tests/prepositions.spec.js --reporter=line`: **3 passed / 0 failed** (10.9s). Exhaustive matrix: 6 relations × 4 animals × 2 languages = 48 cases, covering actual choice ordering, Replay, speaker isolation, hints, retries and score/feedback. Geometry checks cover all six relations at 320×700, 360×800, 800×1280 and 1280×800.
- Full `npm test -- --reporter=line`: **35 passed / 0 failed** (38.8s), preserving audio and learning regressions.
- Browser visual review: inspected all six narrow-screen scenes as a montage; stacking, containment and object separation were visible as intended. This is Chromium/macOS rendering, not Android font/device evidence.
- Android Studio JDK 21: `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/paulbell/Library/Android/sdk' ./gradlew testDebugUnitTest assembleDebug lintDebug --console=plain`: **BUILD SUCCESSFUL** (4s). `assembleDebug` and `lintDebug` passed. Unchanged native unit task was UP-TO-DATE, retaining its **7 passed / 0 failures / 0 errors** result.
- Lint XML: **0 errors / 7 warnings / 2 informational findings**, same warning categories as the audio checkpoint below.
- Packaged `app.js` and `index.html` match current source byte-for-byte.
- APK: `app/build/outputs/apk/debug/app-debug.apk`; SHA-256 `ad708db0798064966dc96deb7668f344c70e67616a4239758d807d256fdb596b`.
- Initial focused browser launch was sandbox-blocked; authorized Chromium runs produced the results above.

Samsung S24 and Amazon Fire Max physical scene/voice review remains outstanding. Emoji remain temporary artwork; no native Prepositions migration or varied-context content expansion is claimed. Next implementable P0: completion/reward layout on narrow/short screens.

## P0 audio reliability — 2026-09-21

Source: local changes based on clean `eb5b68e`, committed as `fix: harden audio policy and TTS lifecycle`. Protected checkpoints `f72c5f7` (speech safety) and `796359e` (product priorities) remain intact. No neural engine, Voice Lab, downloaded model or personality pack was introduced.

Final automated evidence:

- Focused: `npm test -- tests/speech.spec.js tests/audio-reliability.spec.js --reporter=line`: **20 passed, 0 failed** (6.6s).
- Full: `npm test -- --reporter=line`: **32 passed, 0 failed** (30.9s). All original tests remain; old force-through-Off expectations deliberately follow the new policy. Coverage includes all 53 bilingual lessons, seven quiz modes, sanitation and pointer/Enter/Space speaker isolation.
- `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/paulbell/Library/Android/sdk' ./gradlew testDebugUnitTest assembleDebug lintDebug --console=plain`: **BUILD SUCCESSFUL** (10s).
- Native unit tests: **7 passed, 0 failures, 0 errors, 0 skipped**. They exercise the actual pure Kotlin legacy lifecycle helper: latest-only readiness, cancellation, stale callbacks, initialization/playback failure, EN/DE switching, locale preference, same-language fallback and exclusion of network/uninstalled voices. They do not instantiate Android TextToSpeech or prove audible output.
- `assembleDebug`: **passed**, APK at `app/build/outputs/apk/debug/app-debug.apk`.
- `lintDebug`: **passed, 0 errors / 7 warnings / 2 informational findings**. Warnings: OldTargetApi (1), UnusedAttribute (2), GradleDependency (3), SetJavaScriptEnabled (1). Information: AutoboxingStateCreation (2). Separately, Kotlin emits one pre-existing VIBRATOR_SERVICE deprecation warning.
- APK SHA-256: `1b998f60584d730dbf5ba89265b84ef4b2a2275e18d374dddbea6f4bd19e3fff`.
- JDK: Android Studio bundled JDK 21; source/bytecode target remains 17. Initial sandbox runs failed on Chromium Mach-port permissions and Gradle socket initialization; authorized runs outside the sandbox produced the passes above.

Speech mocks establish policy/ownership requests, not Samsung/Fire compatibility. No physical device was tested. Samsung S24 and Amazon Fire Max checks remain required: airplane-mode EN/DE voices, absent language data, first-use readiness, rapid Replay/options, pause/resume, Home/Back/lesson return, durable reset before first activity, actual silence in Off and audible clarity/sanitization. Full native Options return/session recovery remains wider P0; leaving the learning surface currently disposes it and Options returns Home. No legacy migration gate is declared closed.

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
device's TTS settings. The historical implementation did not guarantee a safe fallback for missing voices. The P0 reliability implementation above now rejects missing-language requests; actual offline speech still requires device testing. Visual activities and Hint remain available.

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
