# V1 verification and fixes

This file records build/test evidence and should not be treated as a product or architecture specification.


## Native Seasons migration — 2026-09-22

Base: `main` at `e3bacd6` (committed native Prepositions). Resumed the interrupted Seasons changes after checking repository root, AGENTS.md, status, history and diff. Preserved all work; no reset, pull/rebase, commit or push. Existing browser tests, legacy code and production assets are unchanged.

Legacy audit: `renderTime()` mixes cyclic weekday before/after questions (roughly 55%) with a four-choice season quiz using emoji. It has no dedicated season Explore lessons and does not use the canonical production PNGs/descriptions. Prompts are “Which season is this?” / “Welche Jahreszeit ist das?”; Hint identifies the season, Replay repeats the prompt, option speakers are isolated, correct answers score once, wrong answers allow retry without deductions and rounds use 5/10 questions. Existing browser mode/navigation/audio/completion coverage remains intact.

Implemented:

- Native Days & Seasons chooser leads to Seasons Learn/Practise or the existing legacy calendar activity. System/visible Back from Seasons returns to the chooser, then Home. Options retains its origin. No fake Wilma activity was added.
- Explore selects each canonical `season.*` record and displays its authored EN/DE name/description and original 4:3 illustration. Selection explicitly speaks the canonical description; Replay repeats it. First opening/restoration is silent. Browsing does not create progress events.
- Practice uses shared deterministic finite generation, visiting all four seasons before repeating, with five/ten tasks and stable four-choice ordering. Authored question speech enumerates that exact order. Correct answers lock/score once; wrong answers use explicit Retry without penalty; Help uses the canonical description. Returning to Learn during an unanswered task marks hint support without an attempt. Task-count progress and calm completion follow the existing native pattern, with reachable Play again/Home and no SFX/timer/lives.
- Native ViewModel/audio/session boundaries preserve task identity, choices, score, attempts/support, selected season and phase through supported Options/recreation/navigation. Future settings do not mutate a round. Shared controller applies ALL/QUESTIONS/OFF, manual explanation/Replay/option speech, language-safe offline selection and owned cancellation; stale callbacks cannot change learning state. Missing speech/image/save availability is explicit. Images decode on a worker and cache at most two; Compose uses the original aspect ratio and ContentScale.Fit.
- Extracted only the existing Prepositions write-ahead host into `DurableSessionHost`, injecting activity content/generation/validation. Its Prepositions adapter keeps the existing journal schema/path and behavior. Seasons has a separate atomic browsing checkpoint and quiz journal in private no-backup storage. An accepted transition durably retains its progress effect before publication; failed/uncertain delivery retries with the same identity, and completion deduplicates. Pending quiz delivery resumes when the quiz is loaded or Retry is selected; Explore alone does not start a background job. Corrupt/incompatible files remain preserved with Retry/Home/legacy access. No direct persistence/TTS in Compose, no new dependencies, no general migration framework.
- Canonical names, all eight spoken descriptions and all four asset paths remain authored in the existing content pack, not copied into Compose. Byte comparison against HEAD confirmed every Seasons PNG unchanged (1448×1086). Spring blossom/sparser foliage and Summer dense canopy remain distinct; identity is semantic, not colour/path.

Validation uses `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home'` and `ANDROID_HOME='/Users/paulbell/Library/Android/sdk'`:

- Focused `./gradlew testDebugUnitTest --tests 'com.bellfamily.bastischool.learning.seasons.*' --tests 'com.bellfamily.bastischool.learning.prepositions.*' --tests 'com.bellfamily.bastischool.learning.content.SeasonContentTest' --console=plain`: **46 passed, 0 failures/errors/skipped**, BUILD SUCCESSFUL (8s). Includes 19 new Seasons tests, 19 existing Prepositions tests and 8 existing canonical-season tests. An initial compile check found an AndroidViewModel application-context access error, corrected before this passing run.
- Full `./gradlew testDebugUnitTest assembleDebug lintDebug connectedDebugAndroidTest --console=plain`: **BUILD SUCCESSFUL (1m 55s)**. JVM: **188 passed, 0 failures/errors/skipped** (167 existing + 21 new). Debug APK assembled successfully.
- Instrumentation: **7 passed, 0 failures/errors/skipped** (3 existing + 4 Seasons) on `Medium_Phone_API_35`, Android 15/API 35 arm64 emulator, headless/audio disabled. Covers all canonical Explore names/descriptions/real PNG aspect ratios, separate Listen/answer actions, Hint/wrong/Retry, five-question completion, German ten-question completion at 320dp/1.5 font scale, real native route, Options/Back, EN→DE selection identity, Activity recreation, quiz checkpoint retention, landscape/reverse-landscape/portrait requests, chooser return and no-WebView assertion. Pure tests additionally reconstruct disk repositories, test unanswered/answered/completed checkpoints, same-key failed attempt/completion redelivery, failures before/after journal commit, stale-host rejection, corrupt-file preservation and stale audio callbacks. Activity recreation/JVM reconstruction are not physical process-death proof.
- Full `npm test -- --reporter=line`: **55 passed (59.6s)**. No existing browser tests weakened or removed.
- Lint: **0 errors / 10 warnings / 2 informational findings**, unchanged from baseline. Warnings: GradleDependency 6, UnusedAttribute 2, OldTargetApi 1, SetJavaScriptEnabled 1. Information: AutoboxingStateCreation 2. Existing Kotlin VIBRATOR_SERVICE deprecation remains. No unrelated dependency upgrades.
- `git diff --check` and added-file whitespace checks passed. APK SHA-256: `7f797e6e83aa07ee4d7630650230b9857a7a78be22d3e82c38ad054dae9e8b9b`.

Physical acceptance remains outstanding on **Samsung S24 Ultra and Fire Max**. On each: airplane mode; installed EN/DE and missing offline voices; ALL/QUESTIONS/OFF (including silent Replay); repeated Replay/selection; portrait and both landscapes; larger fonts, safe insets, TalkBack and full-scene clarity/no cropping; Explore/Practice/legacy chooser, Options/visible/system Back; background/return; actual process recreation for selected/unanswered/answered/completed states; five/ten completion; progress and pending-delivery retry after app restart and normal same-signature upgrade. Also review 4:3 image memory/performance on Fire. Emulator tests do not establish audible quality, physical lifecycle/upgrade acceptance or Samsung/Fire compatibility. Storage capacity/corruption recovery UI remains a separate foundation limitation; no record is silently deleted. Legacy fallback stays available. No Wilma, neural TTS, dashboard or unrelated migration.

Changed files for this migration:

- `BACKLOG.md`
- `BUILD_NOTES.md`
- `NATIVE_ARCHITECTURE_SPEC.md`
- `SESSION_HANDOFF.md`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/seasons/SeasonsRouteTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/seasons/SeasonsScreenTest.kt`
- `app/src/main/java/com/bellfamily/bastischool/MainActivity.kt`
- `app/src/main/java/com/bellfamily/bastischool/ShellNavigation.kt`
- `app/src/main/java/com/bellfamily/bastischool/learning/prepositions/PrepositionsHost.kt`
- `app/src/main/java/com/bellfamily/bastischool/learning/seasons/SeasonsAudio.kt`
- `app/src/main/java/com/bellfamily/bastischool/learning/seasons/SeasonsContent.kt`
- `app/src/main/java/com/bellfamily/bastischool/learning/seasons/SeasonsSelectionStore.kt`
- `app/src/main/java/com/bellfamily/bastischool/learning/session/DurableSessionHost.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/seasons/SeasonsScreen.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/seasons/SeasonsViewModel.kt`
- `app/src/test/java/com/bellfamily/bastischool/SeasonsNavigationTest.kt`
- `app/src/test/java/com/bellfamily/bastischool/learning/seasons/SeasonsAudioTest.kt`
- `app/src/test/java/com/bellfamily/bastischool/learning/seasons/SeasonsContentTest.kt`
- `app/src/test/java/com/bellfamily/bastischool/learning/seasons/SeasonsHostTest.kt`

## Native Prepositions migration — 2026-09-22

Source: `main` at `9b247e5` (`feat: add durable native progress event storage`). Began clean; after the usage-limit interruption, reconfirmed root, AGENTS.md, status/diff/history and preserved the four untracked implementation files. No reset, pull/rebase, commit or push. Existing foundation tests, legacy source/assets and browser tests remain unchanged.

Legacy audit (before implementation):

- `renderPosition()` and `positionScenes` in `app.js` select snake, dinosaur, dragon or crocodile with one of six relations. Reference phrases are on the rock / auf dem Stein; under the table / unter dem Tisch; behind the rock / hinter dem Stein; next to the rock / neben dem Stein; in the box / in der Kiste; between the two rocks / zwischen den beiden Steinen.
- Four unique choices include the correct relation. EN/DE questions enumerate displayed choices in the same order. Correct feedback names the actual animal/reference phrase. Hint shows that phrase; option speakers never answer. Replay repeats instruction/question. Wrong answers allow another try without deduction; correct answers lock and increment score once. Rounds support 5/10 questions; completion offers Continue/Home immediately, score/stars and optional balloon tones.
- Home `positions` previously opened the bundled WebView; mixed rounds also include positions. Art is CSS rock/table/box shapes and platform animal emoji, not separate PNGs. Existing `tests/prepositions.spec.js` covers all 24 bilingual combinations and scene geometry; broader speech/navigation/completion browser tests also apply.

Implemented:

- Canonical activity content repository with six `position.*` choices, 24 `scene.prepositions.*` / `task.prepositions.*` records, skill/context IDs, authored bilingual subject/article/phrase and separate display/speech contracts. Native object geometry derives from the semantic relation and reference count. Seeded finite selection creates five/ten distinct scenes with stable four-choice order. The calendar pack is unchanged.
- Compose `PrepositionsScreen`, retained `PrepositionsViewModel` and the existing native shell route. No WebView exists on this route. Large separate answer/listen controls, Replay, Help, explicit Retry, Next, How to play, Home and Play again; portrait/width-adaptive scene/answers and scrolling for short height/large fonts. Insets come from the existing shell Scaffold.
- Shared reducer/checkpoint/progress/audio APIs are used without rewriting those foundations. Wrong answers now use the shared explicit Retry action; hints can be spoken on request. Task-count progress and calm completion replace the prominent legacy star/mastery-style display, per UX_NAVIGATION_SPEC.md; internal score remains exact. No native balloon/SFX subsystem is introduced. Temporary animal glyphs deliberately preserve legacy imagery pending artwork acceptance.
- A bounded version-1 activity journal atomically saves checkpoint plus at most one pending progress event **before** publishing accepted state. Worker-thread delivery uses stable event/session keys; successful delivery is acknowledged in another journal write. Failure retains evidence and blocks further answers until retry; navigation/fallback stays usable. Retry re-reads uncertain commits. Atomic last-loaded-byte comparison prevents an older host overwriting newer state. Journal corruption/incompatibility is preserved, with calm Retry/Home/fallback rather than deletion. The only foundation change exposes the existing Android atomic commit adapter internally for reuse.
- Options, Home, language changes, backgrounding and recreation retain supported state and cancel obsolete speech. Returning/restoring is silent; re-entering after a cold launch resumes the disk checkpoint. New round settings apply only to Play again, which creates a new ID. Shared system TTS remains offline/language-safe; missing voice failures have visual guidance. Tutorial heard state is language/version/reset-epoch specific and only follows current successful playback; Sound Off/interruption do not consume it.
- Legacy `positions`, mixed rounds, checkpoints, all original tests and assets remain available. “Use previous version” starts that separate fallback. No Wilma/Seasons UI, Follow the Instructions, dashboard, neural TTS, new game engine, Room, DI framework or network dependency.

Validation (JDK from `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home'`, SDK from `ANDROID_HOME='/Users/paulbell/Library/Android/sdk'`):

- Focused `./gradlew testDebugUnitTest --tests 'com.bellfamily.bastischool.learning.prepositions.*' --tests 'com.bellfamily.bastischool.PrepositionsNavigationTest' --console=plain`: **20 passed / 0 failures / 0 errors / 0 skipped**, BUILD SUCCESSFUL (5s). Content 4, host 11, audio 4, navigation 1. Tests include 102 seed/round combinations, exact choice order/text, native reference geometry, incomplete content rejection, correct-once scoring, wrong/retry/support/language metadata, unanswered/answered/completed restoration, actual temporary-file progress/journal recreation, failures before/after journal replacement, stable completion deduplication, corrupt journal preservation and stale-host rejection.
- Final `./gradlew testDebugUnitTest assembleDebug lintDebug connectedDebugAndroidTest --console=plain`: **BUILD SUCCESSFUL (55s)**. JVM XML: **167 passed / 0 failures / 0 errors / 0 skipped**. Debug assembly passed.
- Instrumentation: **3 passed / 0 failures / 0 errors / 0 skipped** on existing `Medium_Phone_API_35`, Android 15/API 35 arm64 emulator, started headlessly with audio disabled. English Replay/speaker/Hint/wrong/Retry/completion isolation; German 10-question round at 320dp/1.5 font scale with reachable completion controls; actual MainActivity route, Options visible/system Back, Activity recreation, both landscape requests/portrait, Home/re-entry, EN→DE task/score retention and explicit no-WebView assertion. Activity recreation/orientation requests and JVM repository reconstruction are not physical process-death acceptance. The emulator's image does not add a Google Play dependency to the app.
- Full `npm test -- --reporter=line`: **55 passed (49.0s)**; legacy tests unchanged. Node emitted module.register deprecation and NO_COLOR/FORCE_COLOR notices.
- Lint: **0 errors / 10 warnings / 2 informational findings**. Warnings: GradleDependency 6, UnusedAttribute 2, OldTargetApi 1, SetJavaScriptEnabled 1. Information: AutoboxingStateCreation 2. The three additional GradleDependency notices concern the aligned test Compose BOM and AndroidX test runner/ext-junit versions; no unrelated dependency upgrade was attempted. Existing VIBRATOR_SERVICE Kotlin deprecation remains.
- `git diff --check` and new-file trailing-whitespace checks passed. APK: `app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `c48575b1b62a322537ce50d0299a1520841cef1508ab914036bd8637af57caab`.

Exact changed-file inventory for this migration (25 files):

- `BACKLOG.md`
- `BUILD_NOTES.md`
- `CONTENT_DATA_SPEC.md`
- `NATIVE_ARCHITECTURE_SPEC.md`
- `PROGRESS_TRACKER_SPEC.md`
- `SESSION_HANDOFF.md`
- `TESTING_QA_SPEC.md`
- `VOICE_AUDIO_SPEC.md`
- `app/build.gradle`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/prepositions/PrepositionsRouteTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/prepositions/PrepositionsScreenTest.kt`
- `app/src/main/java/com/bellfamily/bastischool/MainActivity.kt`
- `app/src/main/java/com/bellfamily/bastischool/ShellNavigation.kt`
- `app/src/main/java/com/bellfamily/bastischool/learning/models/PrepositionDefinition.kt`
- `app/src/main/java/com/bellfamily/bastischool/learning/prepositions/PositionGeometry.kt`
- `app/src/main/java/com/bellfamily/bastischool/learning/prepositions/PrepositionsAudio.kt`
- `app/src/main/java/com/bellfamily/bastischool/learning/prepositions/PrepositionsContent.kt`
- `app/src/main/java/com/bellfamily/bastischool/learning/prepositions/PrepositionsHost.kt`
- `app/src/main/java/com/bellfamily/bastischool/learning/progress/android/AndroidProgressRepository.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/prepositions/PrepositionsScreen.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/prepositions/PrepositionsViewModel.kt`
- `app/src/test/java/com/bellfamily/bastischool/PrepositionsNavigationTest.kt`
- `app/src/test/java/com/bellfamily/bastischool/learning/prepositions/PrepositionsAudioTest.kt`
- `app/src/test/java/com/bellfamily/bastischool/learning/prepositions/PrepositionsContentTest.kt`
- `app/src/test/java/com/bellfamily/bastischool/learning/prepositions/PrepositionsHostTest.kt`

Physical acceptance checklist — run on **Samsung S24 Ultra and Amazon Fire Max** before removing the fallback:

1. Record device/OS, APK/signature, system TTS engine and installed EN/DE offline voices. Test clean QA installation and a normal same-signature `adb install -r` upgrade without clearing data; do not overwrite an incompatible user installation.
2. In airplane mode, open/resume Prepositions, hear EN and DE questions/choice pronunciation, Replay, hints, introduction and feedback. Verify All versus Questions-only; OFF must silence automatic/manual narration and remain visually usable. Remove/disable one offline language in a controlled QA configuration and confirm explicit guidance, no wrong-language/network fallback; restore settings afterward.
3. Review all six relation geometries and four animal glyphs for clear meaning on each device, including occlusion for behind/in, table legs for under and two rocks for between. Check native fallback availability and unchanged legacy behavior.
4. Exercise portrait, both landscape orientations, larger font, gesture/three-button navigation, cutout/insets, German wrapping, TalkBack/focus and separated speaker/answer targets. Check Replay and completion/Home/Play again remain reachable without shrinking targets.
5. Check unanswered, wrong/retry/hint and answered states through repeated Options/Back (visible and system), language changes, Home/re-entry, background/return, rotation and process recreation. Task ID/order, attempts/support, index and score must survive; no old narration, double score or duplicated completion. Change 5/10 preference mid-round and confirm only the next round changes.
6. Complete both 5- and 10-question rounds after independent and supported attempts. Inspect local debug repository records by event/session ID (no dashboard is added), restart the app and repeat after the normal signed upgrade: saved attempts/completion must remain once. Test pending delivery and retry with controlled storage-failure injection, including interruption around journal replacement and progress acknowledgement; confirm no accepted event disappears. Actual power-loss/fsync durability is not proven by JVM fault injection or Activity recreation.

## Minimal durable native Progress Tracker foundation — 2026-09-22

Source: clean `main` at `148e756` (`feat: add native learning sessions with checkpoint restoration`). Confirmed repository root, AGENTS.md, status/history and authoritative documents before editing. Existing content/audio/session foundations, legacy routes, assets and tests were preserved. No commit or push.

Implemented six source files under `learning/progress`: `ProgressModels.kt`, `ProgressCodec.kt`, `FileProgressRepository.kt`, `AtomicProgressStorage.kt`, `SessionProgressRecorder.kt`, and `android/AndroidProgressRepository.kt`. Four new test files: `ProgressFixtures.kt`, `ProgressRepositoryTest.kt`, `ProgressCodecTest.kt`, `SessionProgressRecorderTest.kt`.

Typed attempt/completion evidence preserves semantic IDs, session/activity/content versions, skill/context/difficulty, language on attempts, choice/outcome, retries and support separately. Stable attempt/session completion keys deduplicate across repository recreation and changed completion delivery IDs. Conflicting duplicates fail explicitly. Queries filter skill/session/activity/kind and use stable acceptance sequence with optional newest-first limits.

Version-1 bounded deterministic binary snapshots include checksum and strict validation, capped at 10,000 records / 16 MiB. Exclusive process/file locks, synced temporary data, atomic replacement and directory sync provide the local storage boundary. Android uses application-context no-backup storage and system rename/fsync behind the pure storage interface. Corrupt/incompatible/capacity/I/O/clock/conflict failures are explicit; existing data is not silently reset or pruned. Duplicate retries reconfirm durability even after uncertain replacement. No new dependency, Room, network, profiling or telemetry.

The worker-thread session-effect adapter validates against the frozen plan and returns completion acknowledgements for the current delivery. It does not alter the reducer or audio. This remains unwired: native hosts still need delivery/checkpoint coordination and retention of failed effects. Checkpoints are not a durable attempt outbox. No dashboard, rewards, activity migration, settings rewrite, Wilma/Seasons UI, Follow the Instructions or neural TTS.

Validation used `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home'` and `ANDROID_HOME='/Users/paulbell/Library/Android/sdk'`:

- `./gradlew testDebugUnitTest --tests 'com.bellfamily.bastischool.learning.progress.*' --console=plain`: **31 passed, 0 failures/errors/skips**, BUILD SUCCESSFUL (6s). Repository 15, codec 8, session adapter 8. Initial compile found an unavailable Android `O_DIRECTORY` constant; adapter now uses supported read-only directory open, and all subsequent checks passed.
- `./gradlew testDebugUnitTest assembleDebug lintDebug --console=plain`: **147 passed, 0 failures/errors/skips**, **BUILD SUCCESSFUL (17s)**; assembleDebug and lintDebug passed.
- `npm test -- --reporter=line`: **55 passed (51.1s)**. Node emitted module.register deprecation and NO_COLOR/FORCE_COLOR notices; no test failure.
- Lint XML: **0 errors, 7 warnings, 2 informational findings**, unchanged categories: OldTargetApi 1, UnusedAttribute 2, GradleDependency 3, SetJavaScriptEnabled 1; AutoboxingStateCreation informational 2.
- `git diff --check` and new-source trailing-whitespace checks passed.
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`; SHA-256 `77baf8cb2f3cc46b77d0d3bdc4cb8f08e8495314e77dc3a945f7aeabc378db3b`.

Tests use real JVM temporary files with atomic replacement and injected failures before rename, after rename and during directory sync; they cover concurrency, durable restart, duplicate/conflicting delivery, filters, metadata, empty/corrupt/unknown-version/oversized stores, deterministic serialization, restored completion retry and five-/ten-task outcomes. These are not physical Android power-loss or backup/restore tests. Samsung/Fire filesystem/process acceptance remains outstanding. Full-file rewrite/linear query costs and bounded retention require reassessment before sustained live history; capacity currently reports failure without deletion.

## Shared native choice-session framework — 2026-09-22

Source: `main` at `4c254c4` (`feat: add shared native audio foundation`). Started clean, then resumed after the usage-limit interruption with the five existing untracked session source files intact. Reconfirmed repository root, AGENTS.md, history, source-of-truth docs, status and current work before continuing. No reset, pull/rebase, discard, commit or push.

Implementation:

- Added five pure Kotlin files under `learning/session`: `SessionModels.kt`, `SessionReducer.kt`, `TaskGenerator.kt`, `SessionCheckpoint.kt`, `LearningSession.kt`. Typed session/activity/task/attempt/skill/context/completion IDs; immutable complete task plans and state; canonical content IDs for ordered answers; authored EN/DE display/speech; 5/10 round policies; explicit answer/retry/hint/parent-help/Replay/Next/language/completion actions.
- Correct answers lock and score once; wrong answers follow RETRY (explicit retry) or LOCK policy without score deduction. Duplicate/stale attempts, retries, Next and completion acknowledgements are guarded. Support is distinct from attempts and retained across retries; Replay after answering does not rewrite independence. Session settings/task order are frozen, and language changes preserve the exact task/score.
- Finite seeded candidate generation sorts by semantic ID, shuffles cycles/choices deterministically and rejects invalid/empty/duplicate/missing-reference content. No unbounded candidate search or silent replacement. Generator tests cover 100 seed/round combinations as well as stable insertion-order-independent output.
- Version-1 binary checkpoints cap input at 100,000 bytes and save actual task snapshots, not a seed to regenerate. Restoration validates activity/content compatibility, references, field/count bounds, enums/booleans, index/score/progress/phase consistency and trailing/truncated input. Answer/support/retry/completion state survives exactly. No platform serialization, automatic speech or completion re-emission.
- Pure effects expose attempt metadata and logical completion requests for the future shared progress repository. Completion delivery is pending/acknowledged/failed with explicit idempotent retry identity; no persistence, rewards or navigation side effects occur. Exactly-once durable writes still require repository deduplication by attempt/session ID.
- `LearningSession` coordinates the existing `AudioController`, without changing the audio foundation. Start/new instruction, feedback, Hint and Replay use authored speech and existing policy. Hint uses MANUAL and Replay uses REPLAY. Runtime guards reject stale speech callbacks; speech cannot answer/score/complete a task. Background/disposal cancels owned audio; resume/restore/language change are silent. Completion acknowledgement does not stop summary playback.
- No new dependency, platform/UI code, Room, settings migration, activity migration or change to MainActivity, LegacySpeech, ShellNavigation, WebView recovery, existing native content/audio code, assets or existing tests. No Wilma/Seasons UI, Follow the Instructions or neural TTS.

Validation:

- Focused `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/paulbell/Library/Android/sdk' ./gradlew testDebugUnitTest --tests 'com.bellfamily.bastischool.learning.session.*' --console=plain`: **43 passed / 0 failures / 0 errors / 0 skipped**, BUILD SUCCESSFUL (4s). Breakdown: SessionReducerTest 14, SessionCheckpointTest 12, TaskGeneratorTest 6, LearningSessionTest 11. Shared fixture source is `SessionFixtures.kt`.
- Tests cover exact EN/DE unanswered/answered restoration, hints/retries/support, score/index, pending/acknowledged/failed completion restoration, no regeneration, malformed/truncated/random/oversized checkpoint input, incompatible schema/activity/content, invalid references/counters/score/booleans, immutable collections, five-/ten-task completion, duplicate scoring/events, missing hints, canonical identity, future settings isolation, audio policies and silent restore during initialization.
- Full `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/paulbell/Library/Android/sdk' ./gradlew testDebugUnitTest assembleDebug lintDebug --console=plain`: final **BUILD SUCCESSFUL** (11s); XML confirms **116 passed / 0 failures / 0 errors / 0 skipped** (43 new + 73 existing). `assembleDebug` and `lintDebug` passed. Focused and full native checks were repeated after correcting Hint speech metadata; existing tests were not weakened.
- Full `npm test -- --reporter=line`: **55 passed / 0 failed** (50.6s). Node emitted module.register deprecation and colour-environment notices, not test failures. Browser code/assets remained unchanged throughout, including the final native-only Hint metadata correction.
- Lint XML: **0 errors / 7 warnings / 2 informational findings**, unchanged categories: OldTargetApi (1), UnusedAttribute (2), GradleDependency (3), SetJavaScriptEnabled (1), AutoboxingStateCreation information (2).
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `606140525f9e0e073998fbded196cfb589ed2598b5185588bce2612070f4d49e`.
- `git diff --check` passed; all ten new Kotlin files separately checked for trailing whitespace/newline. Imports are limited to the existing pure content/audio contracts and JDK utilities/byte streams. No Android/Compose/WebView/JavaScript dependency in the session package.

Limits/follow-up: this is an unwired choice-session foundation. The first migration must add a lifecycle-aware screen owner, saved-state host storage/restart handling and shared settings/completion UI. Skill registry/scene contracts and persistent attempt/completion/reward storage remain follow-up work. Checkpoints are not an attempt journal; the host must persist emitted events with deduplication before claiming durable progress. Native session recovery remains separate from the legacy WebView checkpoint. No physical device validation was performed; Samsung S24/Fire Max rotation, process recreation, navigation/audio and wider existing P0 checks remain outstanding. These pure tests do not establish Android saved-state integration or hardware compatibility.

## Shared native audio foundation — 2026-09-22

Source: clean `main` at `b6f0661` (`feat: add shared native weekday and season content foundation`). Confirmed repository root, source-of-truth hierarchy and recent history before editing. No existing changes to preserve at start. This checkpoint remains uncommitted; no push.

Implementation (one app module; no new dependency):

- Added pure `audio/SpeechContracts.kt`, `DefaultAudioController.kt` and `SpeechSanitizer.kt`: typed all/questions/off policy, automatic/manual/Replay and speech kinds, content-language reuse, opaque context/request ownership, terminal outcomes, request/owner/session cancellation and native speech safety. `ContentText.speech` and authored localized descriptions convert directly into requests without reading display text.
- Context tokens are revoked on task/phase/language change or owner/session cancellation. A single current request waits for initialization; eligible newer requests replace, never queue. Response/manual priority blocks delayed instructions even after feedback finishes; a new task/phase opens a fresh context. Policy-muted feedback still cancels obsolete narration. Settings changes cancel forbidden playback. Return/restoration does not speak. Generated opaque IDs, detach-before-stop and terminal guards reject stale/duplicate engine callbacks; result observers run after transitions settle and can safely request Replay.
- `audio/SystemSpeechEngine.kt` is the pure coordinator behind a small `SystemTtsPort`. It caches installed offline voices once per initialization, prefers en-GB/de-DE and deterministically chooses another voice only in the same language. Missing language, rejected voice selection, playback failure and engine unavailability are explicit outcomes. A failed stop disables further playback. Close is terminal and releases the port.
- `audio/android/AndroidSystemSpeechEngine.kt` implements the port with Android TextToSpeech, application context only, main-thread callbacks, verified voice selection and QUEUE_FLUSH. Initialization callbacks are posted until assignment is safe; close drops pending callbacks and shuts down TTS. No network/alternate-language fallback or voice downloads. This new engine is not instantiated by existing screens.
- Shared test-source `FakeSpeechEngine.kt` records sanitized text, language/context, kind, trigger and cancellation; tests control readiness, success/failure and deliberately late/duplicate callbacks. `SystemSpeechEngineTest` uses a fake platform port to exercise the actual voice/lifecycle coordinator without Android mocks. Existing tests were preserved unchanged.
- MainActivity, LegacySpeech, WebView routes, browser assets, content foundation, artwork, manifest and dependencies are unchanged. No activity migration, neural TTS, Voice Lab, Wilma/Seasons UI, Follow the Instructions, tutorial persistence or settings rewrite.

Validation (Android Studio JDK, JVM target 17; local Android SDK):

- Focused `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/paulbell/Library/Android/sdk' ./gradlew testDebugUnitTest --tests 'com.bellfamily.bastischool.audio.*' --console=plain`: final **38 passed / 0 failures / 0 errors / 0 skipped**, BUILD SUCCESSFUL (3s): 23 AudioControllerTest, 12 SystemSpeechEngineTest, 3 SpeechSanitizerTest. Initial compile check exposed a nullable inferred Unit return; corrected before passing tests. A new Kotlin data-class copy-visibility warning was also removed; no suppression/test weakening.
- Focused coverage includes every mode/kind/trigger, repeated Replay/option replacement, delayed instructions after feedback, muted feedback, pending initialization replacement/cancellation, EN/DE authored speech, owner/session/request/controller isolation, return silence, exactly-once completion, reentrant observers, engine failure, installed/offline voice filtering and stable cached selection, absent language in either direction, failed selection/playback/stop, disposal and speech sanitation.
- Full `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/paulbell/Library/Android/sdk' ./gradlew testDebugUnitTest assembleDebug lintDebug --console=plain`: **BUILD SUCCESSFUL** (13s). Native XML: **73 passed / 0 failures / 0 errors / 0 skipped** (38 new + 35 existing). `assembleDebug` and `lintDebug` passed.
- Full `npm test -- --reporter=line`: **55 passed / 0 failed** (50.0s). Node emitted its module.register deprecation and colour-environment notices; no test failures.
- Lint XML: **0 errors / 7 warnings / 2 informational findings**, unchanged: OldTargetApi (1), UnusedAttribute (2), GradleDependency (3), SetJavaScriptEnabled (1), AutoboxingStateCreation information (2).
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `2a42f27896359df8ec546cac752e11e8e6ff18d21ac78368e8669ae04b5b5185`.
- `git diff --check` passed; new source/test files also checked for whitespace. Pure audio imports only shared content models; Android APIs are confined to the adapter. No existing source or test was removed or weakened.

Limits/follow-up: this is an unwired audio foundation, not native activity acceptance. The first migration must wire ownership cancellation at background/navigation boundaries, close the controller on disposal, open fresh contexts on question/phase/language changes, retain authored Replay text and react to explicit failures. Requests/tokens are ephemeral, never saved-state objects; restore silently. Real Android service callbacks, installed voice behavior, audible EN/DE quality, missing voices, interruption and silence still require Samsung S24/Fire Max airplane-mode/lifecycle validation. JVM fakes and compilation do not establish device compatibility. Existing physical P0 gaps remain open. System TTS is not isolated phoneme teaching. Enhanced engines and SFX migration remain outside this slice.

## First shared native content/data foundation — 2026-09-22

Source: existing session work begun at `ba22b15`, completed on `e6004e2` after the newer roadmap/Seasons commits. No pull, rebase, reset or discard. The newer roadmap, art direction, Wilma artwork and four Seasons PNGs were preserved. Changes are uncommitted for review; no push.

Implementation (one app module, no new dependencies):

- Pure Kotlin `learning/models/ContentModels.kt`: stable semantic `ContentId`, distinct `AssetId`, EN/DE `ContentLanguage`/`LocalizedText`, explicit display/speech `ContentText`, positive schema/revision `ContentVersion`, and immutable colour/weekday/season/image-reference records. Required bilingual fields reject blanks instead of falling back. No Android, Compose, Activity, WebView, JavaScript, preferences or file-loading dependency.
- `learning/content/ContentRepository.kt`: small synchronous interface and validated immutable bundled implementation; ID-sorted enumeration and deterministic nullable lookup, explicit weekday and season order, cyclic previous/next weekdays, defensive collection snapshots. Validation rejects duplicate IDs, incomplete/unknown calendar sets, invalid weekday order, missing/wrong-type colour references and unresolved/duplicate image references. Asset paths must be relative/local; pure validation resolves the manifest, while tests verify actual files.
- `CoreContent.kt` and `SeasonContent.kt`: schema 1/revision 1, 18 records (7 colours, 7 weekdays, 4 seasons) and 4 image references. Weekdays use the exact canonical IDs, separately authored EN/DE labels and prescribed colour cues. Colour/artwork never determines identity. Weekdays contain no Wilma asset references. Seasons provide short bilingual display/spoken names and separately authored long descriptions copied exactly from the master roadmap, preserving spring new growth/blossom versus summer full canopy. Season assets are referenced by stable semantic asset IDs; filenames are replaceable metadata. No real-date/hemisphere inference is introduced.
- No activity is migrated or wired to this repository yet. No native playback engine/sanitizer, Wilma/Seasons UI, Follow the Instructions, Focus & Flex, progress storage or broad game framework was introduced. Existing routes, MainActivity, legacy assets/code and all prior tests are unchanged.
- Hygiene: six tracked `.DS_Store` paths removed from Git and `.DS_Store` added to `.gitignore`. Locally recreated Finder metadata was retained as ignored files; no legitimate app asset was removed. No PNG compression/optimization.

Validation:

- Focused `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/paulbell/Library/Android/sdk' ./gradlew testDebugUnitTest --tests 'com.bellfamily.bastischool.learning.content.*' --console=plain`: **20 passed / 0 failed** (12 ContentRepositoryTest + 8 SeasonContentTest), BUILD SUCCESSFUL (20s). The earlier weekday-only checkpoint also passed before the Seasons extension.
- New tests cover bilingual blanks, display/speech separation and German characters; invalid IDs/versions/paths; exact weekday IDs/names/colours/order and all seven cyclic neighbours; unknown IDs; missing/wrong-type/duplicate references; insertion-order-independent lookup and immutable input/output snapshots; season IDs/labels, all eight exact roadmap narrations, canonical PNG paths/signatures, asset replacement independent of identity and missing image references.
- Full `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/paulbell/Library/Android/sdk' ./gradlew testDebugUnitTest assembleDebug lintDebug --console=plain`: **BUILD SUCCESSFUL** (21s). Native unit XML: **35 passed / 0 failures / 0 errors / 0 skipped** (20 new + 15 preserved). `assembleDebug` and `lintDebug` passed.
- Full `npm test -- --reporter=line`: **55 passed / 0 failed** (50.1s). No existing test weakened or removed.
- Lint: **0 errors / 7 warnings / 2 informational findings**, unchanged categories: OldTargetApi (1), UnusedAttribute (2), GradleDependency (3), SetJavaScriptEnabled (1), AutoboxingStateCreation information (2).
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `eaf0d877f6292a1af3e7bb2f503e00f63d6be8149216d1ad4b8f16130b80c431`. All four season PNGs match both HEAD Git blobs and packaged APK bytes. Native content imports are limited to its own models and JDK collections.
- `git diff --check` and staged diff check passed; tracked `.DS_Store` list is empty. New Kotlin files also checked for trailing whitespace.

Remaining work/limits: the repository is a data foundation, not a migrated learning surface. Add skill/vocabulary/grammar, scene/task, phonics and verb lesson contracts only as needed by the first migration; native audio/session/progress remain separate P1 work. Schema validation cannot establish educational or linguistic quality, and a future asset loader must handle file-read failures. No new physical-device acceptance was claimed for this data-only addition; prior Samsung/Fire gaps remain. Canonical narration tests intentionally require the roadmap and authored data to stay in agreement.

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
