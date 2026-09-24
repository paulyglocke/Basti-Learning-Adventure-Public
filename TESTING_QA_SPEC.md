# Testing and QA Specification

## Role and completion rule

This document defines verification and feature completion gates. [NATIVE_ARCHITECTURE_SPEC.md](NATIVE_ARCHITECTURE_SPEC.md) owns runtime/recovery contracts; [CONTENT_DATA_SPEC.md](CONTENT_DATA_SPEC.md) owns data invariants; [UX_NAVIGATION_SPEC.md](UX_NAVIGATION_SPEC.md) owns interaction behavior. Apply the domain rules in [PROGRESS_TRACKER_SPEC.md](PROGRESS_TRACKER_SPEC.md), [GAME_DESIGN_SPEC.md](GAME_DESIGN_SPEC.md), [VOICE_AUDIO_SPEC.md](VOICE_AUDIO_SPEC.md) and [ART_DIRECTION.md](ART_DIRECTION.md).

**A feature is not done only because it builds.** Record evidence for its behavior and relevant device risks. A code-complete slice awaiting required hardware checks must be labelled as such, not fully verified or eligible for legacy removal.

Current baseline: CI builds/lints Android and runs legacy Playwright tests. Native foundation/unit tests and Prepositions Compose/navigation instrumentation now exist. Run instrumented tests on an available emulator/device; CI does not yet run that device suite. Browser speech mocks do not prove native audio, Compose behavior, Android navigation or device compatibility. The requirements below guide implementation; they are not a record of passed tests. [BUILD_NOTES.md](BUILD_NOTES.md) holds dated evidence, revisions, commands, artifacts and outstanding checks.

## Automated coverage

| Layer | Required cases for affected behavior |
| --- | --- |
| Unit/state | Reducer/game transitions, correct and wrong answers, retries, scoring, support levels, skipped/interrupted tasks, duplicate/rapid taps and exactly-once completion effects. |
| Generators | Fixed-seed reproducibility, boundary parameters, bounded failure, accepted answer presence, unique options, valid scenes and solvability across a representative seed/parameter matrix. |
| Persistence | Attempt-event deduplication, session completion/reward idempotency, retry after write failure, settings import and schema migration, restart with previously committed records. |
| Audio state | Readiness, failure/fallback, cancellation, request ownership, queue/interrupt policy and settings using fake engines and controlled time. |
| Content validation | Missing EN/DE fields, unresolved/duplicate IDs, invalid facts/units, impossible tasks, incorrect or contradictory answer sets, invalid scene references, missing local assets and malformed required German grammar metadata. |

Keep domain tests independent of Compose/platform objects where possible. Inject random sources/clocks rather than waiting for real delays. Test externally meaningful invariants, not private implementation details. Automated German metadata checks cannot establish natural phrasing or educational correctness; bilingual human review remains required.

## Compose UI and navigation

For each new/migrated screen, test the critical path through intro, Replay, answer/action, hint, feedback and completion. Verify Home/Continue and system/visible Back agree with UX_NAVIGATION_SPEC.md. A quiz-linked lesson must return to the same question, including answered/support state.

Test independent speaker activation by touch and relevant keyboard/accessibility semantics; it must never submit an answer. Verify Replay remains usable where intended, duplicate actions do not advance twice, parent settings return correctly, and progress/support displays reflect state where relevant. Test configuration recreation and the declared process-recreation policy; activity recreation alone is not proof of process-death recovery.

Use semantic assertions and state expectations, with screenshots as supplementary layout evidence. A static preview cannot establish navigation, lifecycle or persistence behavior.

## Layout matrix

Cover English and German at default and large system font sizes, including long labels and completion with the maximum supported visible rewards:

- Compact phone width, including narrow layouts comparable to the historical 320/360 logical-pixel web checks.
- S24-class phone sizes, portrait and landscape.
- Fire-class tablet sizes, portrait and landscape; verify actual reflow.
- Short landscape height, system bars/display cutouts, and parent screens with the keyboard open.
- Gesture and three-button navigation where available; all essential controls stay reachable and clear of unsafe areas.

Use actual device density/window metrics for native tests; browser viewport pixels are not interchangeable with Android dp. Automate representative Compose configurations/screenshots and inspect clipping, overlap, scrolling, target size and scene meaning manually. At minimum, physical S24 and Fire Max review is required before retiring a migrated route; record any unavailable configuration rather than claiming coverage.

## Audio

Use VOICE_AUDIO_SPEC.md as the policy/quality authority. Before implementing ambiguous all/questions/off behavior, record an explicit policy there and test it; do not perpetuate legacy expectations merely because old tests pass.

Automated tests must cover English/German speech requests, deliberate display/speech separation, decorative emoji never leaking accidentally, preserved umlauts/ß, Replay, tutorials, feedback, options and completion. Include stale delayed narration, navigation/background cancellation, repeated taps, TTS initialization, engine failure, absent offline language data, system fallback and language-specific pronunciation overrides where introduced.

Fake engines verify requests and state, not audible quality. Physically audition both languages on S24 and Fire Max in airplane mode. Listen for clarity, warmth, natural phrasing, specialist names, interruptions and actual silence when selected. Record engine/voice/language data and startup/replay responsiveness. Enhanced engines require the physical comparison and resource measurements in VOICE_AUDIO_SPEC.md before becoming default.

## Accessibility and emotional safety

Automate meaningful labels/roles, separate speaker/answer semantics, focus order where practical, and reduced-motion behavior. Manually review large separated touch targets, contrast, TalkBack navigation, no colour-only meaning and Switch Access where practical. Record untested assistive paths explicitly.

Check that reduced motion retains instructional meaning, unavailable controls are not misleadingly focusable, and completion actions are usable immediately. Review child-facing language and interactions for no lives, penalties, “failed” states or stressful countdowns. Hints/parent help must remain positive support, with no reward removal. Art/animation must communicate the intended concept, not merely move.

## Offline and Fire

For runtime changes affecting installation, content, audio, storage or navigation, test a clean install and relevant upgrade path. In airplane mode, launch, complete representative activities, change language, replay audio, save/reopen settings/progress and navigate without network stalls.

Inspect manifest/dependencies for no required Google Play Services or cloud service, and confirm all core content/art/audio assets are local or required offline voice data is installed. Test missing enhanced voice assets/voices explicitly and preserve a usable recovery path; do not silently select a network voice. Dependency inspection alone does not prove Fire OS compatibility: a physical Fire Max test is required for migrated activity acceptance and release validation.

## Progress and adaptation

Test independent success versus success after replay, hint or parent help, with support retained across retries and recreation. Replay alone is not an incorrect attempt; parent help is not failure. Record meaningful context exposure, language, difficulty, task and session identity through the shared API.

Test that repeated success in one context does not alone establish generalisation, and use a fake clock to verify the implemented recency/spaced-review rules. Thresholds must be explicitly documented when implemented, not invented by tests. Suggested Focus/Today’s Adventure should use shared skill progress and preserve confidence/challenge balance; do not adapt solely by response speed or total stars.

Verify duplicate completion callbacks, repeated taps and restart/retry cannot double-award session rewards or inflate progress. Test partial writes/recovery using the persistence contract in NATIVE_ARCHITECTURE_SPEC.md.

## Build, tests and artifact evidence

For runtime work, run checks relevant to the touched components and shared dependencies:

```sh
./gradlew assembleDebug lintDebug
```

As native test foundations are added, run relevant unit and instrumented Compose suites (typically `./gradlew testDebugUnitTest` and `./gradlew connectedDebugAndroidTest`, with a suitable device/emulator). Report absent infrastructure or unavailable devices as gaps, not passes.

Retain and run relevant legacy Playwright checks while web routes remain:

```sh
npm ci
npx playwright install chromium
npm test
```

See README.md for tool prerequisites. Do not remove legacy coverage until native equivalents verify the behavior and the corresponding route is retired. Update expectations for deliberate bug fixes; do not require native parity with a known defect.

Verify `app/build/outputs/apk/debug/app-debug.apk` exists for the tested build; record source revision, commands/results and artifact identity (checksum when sharing an APK). For release/sideload validation also check package/version/signature and bundled assets. Do not describe the historical tracked v1.0.0 APK as a current-source build. Physical reports should name device/OS, orientation, language and voice configuration.

Documentation-only changes require diff/whitespace/Markdown/file-reference checks, not full Gradle or Playwright runs. For code changes, broaden checks when shared behavior or unresolved failures warrant it; avoid unrelated test churn.

## Feature and migration acceptance

A new feature needs passing relevant state/content/UI tests, reviewed bilingual content, audio/progress integration and recorded layout/accessibility/offline evidence. Tests for mechanics should match the feature; open-ended speech is not graded like a choice quiz.

Before retiring a migrated activity's legacy route, verify all of the following:

- Functional parity, including content, retries, score, tutorials, settings, lesson return and completion; intentional fixes are documented.
- Bilingual and audio parity, including Replay, hints, cancellation and speech-safe output.
- Shared progress integration and idempotent completion/rewards.
- Phone/tablet, portrait/landscape, large-font and short-height layout QA.
- Native automated coverage and declared recovery behavior passing.
- Required physical S24/Fire audio, navigation, inset, touch and offline checks recorded.

Human reviews may remain manual for voice quality, natural phrasing, fact credibility, teaching clarity, art, physical ergonomics and assistive-technology usability. Automate repeatable logic, data validation, regression paths and persistence guarantees. Each completion report must distinguish automated passes, manual evidence, historical evidence and remaining gaps; missing required evidence keeps the migration gate open.


## Stable APK upgrade acceptance (2026-09-23)

Status update supplied by the owner on 2026-09-23: **S24 same-key in-place upgrade passed**, versionCode/versionName `1003901 / 1.1.39.1` → `1004501 / 1.1.45.1`, certificate SHA-256 `bea808b0c0b891d73e61b739fd43f361a952d5297892318821b97ce91b553507`. Cleaned celebration artwork (`a8877b5`) was also physically verified on S24. These are supplied physical results, not emulator claims or actions repeated in the Seasons task. Fire Max upgrade and broader new-mode/lifecycle/accessibility checks remain separate acceptance work.

Use the persistent-key release stream in README.md, not unrelated hosted-runner debug APKs. For **Fire Max** acceptance and future upgrade regressions, use this exact procedure; never uninstall, clear storage or use a downgrade flag as part of the test:

1. Record baseline/candidate APK SHA-256, package ID, versionCode/versionName and signing certificate SHA-256 (`apksigner verify --print-certs`). Record the installed code with `adb shell dumpsys package com.bellfamily.bastischool`. To inspect an installed certificate, locate its APK using `adb shell pm path com.bellfamily.bastischool`, copy that base APK to a private local path with `adb pull`, then run apksigner on the copy. These are public certificate checks, not private-key extraction.
2. Confirm both APKs use `com.bellfamily.bastischool`, the same certificate, and a strictly greater candidate code. Stop on mismatch; preserve the installation/data. Resolve signing history first. A deliberately chosen cross-certificate reinstall is a separate destructive migration, not a passing upgrade test.
3. Install the signed baseline on an appropriate test installation. Set German/Questions/10-round preferences (or record another exact combination), complete at least one native practice round to create attempts/completion, and start another round, use Help/Retry and leave a known answered or unanswered task. Record selected item/mode, task/choice order, score/index/support and completion count. Record legacy settings/session too where supported.
4. Capture the baseline progress event IDs/count and session journals using authorised QA storage inspection if available. Distribution builds are non-debuggable: `run-as` generally cannot inspect them. There is no progress dashboard/export yet, so **if records cannot be inspected, mark durable-progress/duplicate validation blocked, not passed from UI alone**. A separately identified persistent local-debug-key QA stream can test file-level preservation using `run-as`; it is supplementary evidence, not release-device acceptance.
5. Background/close the app without clearing it. Run `adb install -r path/to/newer-same-key.apk` (or the normal package installer) over the existing app. Record the actual installer result and updated package version. Do not bypass a signature/downgrade error by uninstalling.
6. Launch in airplane mode. Verify settings, selected Explore item, exact native task/choice order, index/score, answered/retry/support state and silent restoration. Complete/reopen the restored round; verify old progress IDs remain once, no new progress appears merely from upgrading, and new practice appends normally. Inspect stored records as in step 4; repeat after another app restart. Test pending-delivery recovery with controlled QA fault injection where available.
7. Repeat on Fire Max and record device/OS/signature/version evidence separately. True process death, production-key signing, voice quality and device filesystem durability must not be inferred from JVM tests or Activity recreation.

No uninstall/data wipe is authorised by this checklist. If the installed legacy certificate differs from the new permanent identity and its private key is unavailable, document the remaining one-time migration decision and lack of cross-signature export/import before asking the owner to act.


## Current physical acceptance ledger and Fire Max checklist — 2026-09-24

Owner-reported S24 passes after the latest stable-signed install: Seasons Explore, What Comes Next, What Comes Before, Winter → Spring and Spring ← Winter boundaries, Build the Year, wrong choices retaining correct placements, partial restore, completed restore, completion actions/celebration, and Wilma ordering auto-follow. Do not reopen these checks without a regression. This is owner evidence, not emulator acceptance; normal partial/completed restore does not establish process-death recovery.

Still unverified in this report on S24: both landscape orientations for these new flows, larger font, TalkBack/full accessibility, detailed EN/DE ALL/QUESTIONS/OFF and Replay sweep, pop-volume judgement, true process recreation beyond normal return/restore, and the full airplane-mode/offline-voice matrix. Recognition Practice and an exhaustive 5/10-round/settings/navigation matrix were not explicitly included in this latest report. File-level progress retention/deduplication requires the evidence described in the upgrade procedure above; visible restored state alone is insufficient.

Fire Max remains **not physically accepted**. Record device/Fire OS, installed/candidate version and certificate, language/audio mode, observed result and any defect for each row below. Reuse the stable APK upgrade procedure above rather than creating a separate install flow.

- Upgrade: same package/certificate, strictly higher version, install over existing app without uninstall/data clear. Preserve settings, known partial session and progress; distinguish inspected records from UI-only evidence.
- Activities: Seasons Explore/recognition/Next/Before/both wrap boundaries/Build the Year; wrong placements retained; Wilma Explore/practice/order and smooth follow; Prepositions and Vocabulary Explore/practice as applicable. Exercise 5/10 rounds where supported, Help/Retry/Replay, completion, Play again/Home, Options/language/return.
- Celebration: five optional targets, single pop/reveal, clean animals, brief local confetti, immediate actions, no score/progress coupling; no balloons during Explore.
- Audio/offline: airplane mode with installed offline EN/DE voices; ALL, QUESTIONS and OFF including Replay/pop silence; no wrong-language fallback or duplicated narration on return. Check unavailable voice behavior where practical without deleting existing voice data merely for testing. No runtime network dependency.
- Presentation/accessibility: portrait and both landscape orientations where supported, larger text, clear art/cards, safe insets, reachable separated touch targets, device screen reader/TalkBack-equivalent and keyboard/focus where available. Record unavailable assistive paths explicitly.
- Recovery: background/return, partial and completed ordering, answered/unanswered tasks, process recreation where practical without clearing data; restore silently and avoid duplicate completion. Confirm settings/session/progress after restart and signed upgrade; mark raw-progress inspection blocked if release storage cannot be inspected safely.

Instrumentation belongs on the designated emulator (`ANDROID_SERIAL=emulator-5554`), not the stable S24/Fire installation. Do not bypass certificate/version failures by uninstalling, downgrading or clearing data.
