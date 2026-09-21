# Testing and QA Specification

## Role and completion rule

This document defines verification and feature completion gates. [NATIVE_ARCHITECTURE_SPEC.md](NATIVE_ARCHITECTURE_SPEC.md) owns runtime/recovery contracts; [CONTENT_DATA_SPEC.md](CONTENT_DATA_SPEC.md) owns data invariants; [UX_NAVIGATION_SPEC.md](UX_NAVIGATION_SPEC.md) owns interaction behavior. Apply the domain rules in [PROGRESS_TRACKER_SPEC.md](PROGRESS_TRACKER_SPEC.md), [GAME_DESIGN_SPEC.md](GAME_DESIGN_SPEC.md), [VOICE_AUDIO_SPEC.md](VOICE_AUDIO_SPEC.md) and [ART_DIRECTION.md](ART_DIRECTION.md).

**A feature is not done only because it builds.** Record evidence for its behavior and relevant device risks. A code-complete slice awaiting required hardware checks must be labelled as such, not fully verified or eligible for legacy removal.

Current baseline: CI builds/lints Android and runs legacy Playwright tests. Native unit/Compose test foundations are not yet present. Browser speech mocks do not prove native audio, Compose behavior, Android navigation or device compatibility. The requirements below guide implementation; they are not a record of passed tests. [BUILD_NOTES.md](BUILD_NOTES.md) holds dated evidence, revisions, commands, artifacts and outstanding checks.

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
