# Testing and QA Specification

## Role and completion rule

This document defines verification and feature completion gates. [NATIVE_ARCHITECTURE_SPEC.md](NATIVE_ARCHITECTURE_SPEC.md) owns runtime/recovery contracts; [CONTENT_DATA_SPEC.md](CONTENT_DATA_SPEC.md) owns data invariants; [UX_NAVIGATION_SPEC.md](UX_NAVIGATION_SPEC.md) owns interaction behavior. Apply the domain rules in [PROGRESS_TRACKER_SPEC.md](PROGRESS_TRACKER_SPEC.md), [GAME_DESIGN_SPEC.md](GAME_DESIGN_SPEC.md), [VOICE_AUDIO_SPEC.md](VOICE_AUDIO_SPEC.md) and [ART_DIRECTION.md](ART_DIRECTION.md).

**A feature is not done only because it builds.** Record evidence for its behavior and relevant device risks. A code-complete slice awaiting required hardware checks must be labelled as such, not fully verified or eligible for legacy removal.

## UX Refresh Phase 4B — Seasons chooser and content-first Explore (committed 2026-10-20)

The Days & Seasons route now opens a dedicated bilingual Seasons chooser. Its
Explore action opens the existing Seasons learning route; eight practice modes
remain directly available from grouped chooser cards. The existing nine phase
titles, season order, illustrations, answer behavior, feedback, hints and voice
behavior remain in place. Back from a phase returns to the chooser and chooser
Back returns to Days & Seasons. Existing Seasons journals/checkpoints and the
Months-owned Seasons Wheel and Sort Months into Seasons paths are unchanged; no
progress-format migration or wheel-geometry change was made.

- PASS — focused Seasons JVM **61/61** and full JVM **555/555**, zero
  failures/errors/skips. `assembleDebug`, `assembleDebugAndroidTest`, `lintDebug`
  and `git diff --check` passed. Lint reports 0 errors and 5 existing warnings.
- PASS — complete API 35 Seasons package **42/42**, zero failures/skips, including
  chooser EN/DE, font scales 1×/1.5×/2× and form-factor coverage, all nine actual
  phase destinations, keyboard activation, Options/language return, recreation,
  saved session, and progression restoration. One earlier full run had a single
  checkpoint byte-length mismatch (6034/6035); the focused test and subsequent
  complete reruns passed. Treat this as an observed intermittent comparison
  failure whose cause was not established.
- PASS — complete Months-package API 35 instrumentation **185/185**, zero
  failures/skips after the shell-route changes. App-wide instrumentation was not
  run. The 14-screenshot gallery covers chooser and Explore in EN/DE, German 2×,
  practice, tablet layouts, Options return and the shared Months/Seasons activity.
  The genuine short-landscape Explore screenshot is **2400×1080** and shows the
  existing artwork, selected season, description, replay and answer controls;
  lower choices remain scroll-reachable. Rotation is locked only after screen
  navigation to avoid UiAutomator restoring portrait orientation. The corrected
  image is in the gallery and has been visually inspected.
- Physical-device, actual TalkBack, switch-access, child-usability and offline
  voice acceptance remain **NOT RUN — owner-deferred**. UX-ANR-01 remains
  independent. See [the Phase 4B review](docs/seasons-phase4b-review/REVIEW.md)
  for the inventory, changed paths and visual evidence.

Phase 4B implementation commit `b516b0bc80b34900cc04d1507440db0bafaca888` is
pushed to `main`. The invalid landscape evidence and stale status wording were
corrected in commit `24de69103af3d79dc1966eb93b3bfd49b8ec2556` with Phase 4C.
See [SESSION_HANDOFF.md](SESSION_HANDOFF.md).

## UX Refresh Phase 4C — Wilma’s Week chooser and Explore (2026-10-21, committed)

The Days & Seasons Wilma entry now opens a bilingual chooser. Explore is the
prominent first action, with direct cards for the other five existing modes.
Explore removes the mode catalogue from its learning screen and gives the
existing Wilma/week artwork, selected day and controls the main space. The
six canonical modes, weekday order/colours, question generation, feedback,
completion, audio policy, asset files and persistence identities/formats remain
unchanged. A narrow wide-screen layout defect found in the tablet screenshot was
corrected by grouping Explore copy so the artwork and heading render beside it.
The Phase 4B screenshot/status correction remains separate and uncommitted.

- PASS — focused Wilma chooser instrumentation **24/24** on API 35 across English
  and German, 1×/1.5×/2×, phone/short landscape/tablet configurations. The
  Wilma route/restoration test passed **1/1** in the complete run and exercises
  all six actual mode destinations, Options/language, Home, recreation and saved
  session preservation.
- PASS — full JVM **555/555**, with **24 Wilma-related JVM cases**; Android-test
  Kotlin compilation, debug build, test APK assembly, `lintDebug` and
  `git diff --check` pass. Lint reports 0 errors, 5 existing warnings and 2
  informational findings.
- PASS — complete API 35 app instrumentation **567/567**, zero failures or
  skips, via the installed AndroidJUnitRunner. This included the Months and
  Seasons regression suites and all Wilma modes, chooser layout cases, route,
  restoration and production navigation. The first complete Gradle run exposed
  five stale test expectations (four removed Seasons `learn` tag checks and one
  `MONTHS` vs `MONTHS_CHOOSER` route expectation); both test files were corrected
  to match the already-committed routes/screens. Gradle's later device installer
  returned `INSTALL_FAILED_INSUFFICIENT_STORAGE` with 1.2 GB data free, so the
  final complete run used direct APK installation and `adb shell am instrument`.
- PASS — the 14-image gallery in
  [the Phase 4C review](docs/wilma-phase4c-review/REVIEW.md) includes portrait,
  genuine 2400×1080 phone landscape, tablet portrait and tablet landscape
  captures. Physical-device, actual TalkBack, switch-access, child-usability and
  offline-voice acceptance remain owner-deferred. UX-ANR-01 remains separate.

After the 567/567 instrumentation run, the temporary guard that suppressed
speech during a persistence failure was removed to restore the baseline audio
condition; final source was rebuilt and linted, but the full suite was not rerun
after that one-line audio restoration. `WilmaAudioTest` later passed 3/3; it
does not exercise the view-model persistence-failure branch. See the Phase 4C
review for that precise limit. Phase 4C and the bounded Phase 4B evidence
correction are committed and pushed in `24de69103af3d79dc1966eb93b3bfd49b8ec2556`.

## UX Refresh Phase 4A — Months chooser and Explore (2026-10-20, uncommitted)

The Months activity chooser now separates activity selection from the existing
content-first Year Wheel Explore route. Existing quiz, Build Year (three-/four-month),
Months ↔ Seasons and Seasons Wheel activities remain on their established learning
owners and saved-progress formats. Back from Explore returns to the chooser; Between's
contextual Year Wheel still returns to the same question. The Year Wheel artwork,
rotation geometry, month order and season mapping are unchanged. No six-/twelve-month
levels, new progress identity or persistence migration was introduced.

- PASS — focused Months JVM tests **57/57** and full JVM suite **555/555**, zero
  failures/errors/skips. `assembleDebug`, `assembleDebugAndroidTest`, `lintDebug`
  and `git diff --check` passed.
- PASS — complete Months package instrumentation on API 35 **185/185**, zero
  failures/skips. This includes chooser layouts, wheel presentation, quiz and
  season routes, Between contextual return, Build Year levels and saved-session
  restoration. The earlier **169/185** attempt had fourteen wheel assertions
  blocked by the misplaced `months-content` semantics tag and one route assertion
  expecting the old parent; both corrections are present and the full suite now
  passes.
- The remaining earlier four-month failure was a test-thread issue, not an app or
  emulator failure. Its stack ends at
  `MonthsBuildYearFourScreenTest.kt:89`: the test called Compose's keyboard input
  mode manager from the instrumentation thread, leading through Compose focus to
  Android `ViewRootImpl.requestChildFocus` and
  `CalledFromWrongThreadException`. Dispatching that request via `runOnUiThread`
  preserves the keyboard-mode assertion. The next complete API 35 run passed
  **185/185**. The captured failure is in the connected-test XML/logcat under
  `app/build/outputs/androidTest-results/connected/debug/`.
- PASS — actual API 35 MainActivity screenshots are in
  [the visual gallery](docs/months-phase4a-review/index.html), with a concise
  [review report](docs/months-phase4a-review/REVIEW.md). The capture script restores
  emulator display, font, orientation and language settings.
- `lintDebug` reports the existing five warnings and two informational findings;
  Kotlin reports the existing deprecated `VIBRATOR_SERVICE` warning. Physical-device,
  TalkBack, switch-access, child-usability and offline-voice acceptance remain
  **NOT RUN**. UX-ANR-01 remains independent.

This checkpoint is uncommitted and does not authorize commit or push. See
[SESSION_HANDOFF.md](SESSION_HANDOFF.md) for the exact working-tree state.

## UX Refresh Phase 3 — production Home checkpoint (2026-10-20)

At baseline `d0679a8ec227ccae41ba42eed6690fde5fa47784` (uncommitted), fresh
JDK 17 full JVM validation passed **555/555** with zero failures, errors or
skips. `assembleDebug`, `assembleDebugAndroidTest` and `lintDebug` passed; lint
reported five warnings and two informational findings. On the API 35 emulator,
the focused Home matrix passed **86/86**, the production route and saved-session
matrix passed **24/24**, and the final typography rerun passed **12/12** across
EN/DE, 1×/1.5×/2× and portrait/landscape. The final capture gallery contains 23
production screens; visual review found and resolved large-text navigation and
Options-toolbar wrapping. See [SESSION_HANDOFF.md](SESSION_HANDOFF.md) and
[docs/UX_HOME_INTEGRATION.md](docs/UX_HOME_INTEGRATION.md) for exact scope and
evidence location.

These are focused automated results, not a complete instrumentation-suite pass.
Browser 55/55 is historical for unchanged web sources. Physical-device
acceptance, actual TalkBack/switch-access usability, child acceptance and
offline-voice quality remain open; physical acceptance was deferred by the
owner. The emulator's original network and display settings were restored after
the offline test run and captures.

## Follow Instructions introductory checkpoint (2026-10-14)

The two-level native Follow activity requires separate evidence for one-step and
ordered two-step behavior. JVM coverage checks correct order, retry/hint/replay,
German accusative wording, canonical image mappings, deterministic 5/10 rounds,
unique pairs, old one-step checkpoint recovery, separate identities and partial
two-step restoration. API 35 Compose coverage checks level selection, Options,
language, Activity recreation, keyboard/semantics, and EN/DE at 1×/1.5×/2× in
portrait and short landscape. An emulator can verify control semantics and layout,
but cannot establish audible offline-voice quality, actual TalkBack usability or
child comprehension. Physical-device acceptance is **NOT RUN — deferred by owner**.

Final JDK 17 validation: focused Follow JVM **15/15**, full JVM **522/522**, focused Follow API 35 emulator
**29/29**, and full API 35 emulator **382/382** passed with zero failures/skips;
`assembleDebug`, `lintDebug` and Android-test compilation passed. Lint retains
0 errors, 5 existing warnings and 2 informational findings. A fresh-boot isolated
rerun of two unchanged shared-UI classes passed **16/16** after an earlier
instrumented-process startup failure with no captured Java/native exception;
the final full suite also passed. The original emulator-runner trigger is not
proved. See SESSION_HANDOFF.md for the scoped route-test synchronization correction
and preservation of historical Level 1 checkpoint meaning. This is automated
validation, not physical-device acceptance.

## Months Phase 7 — four-month Build the Year checkpoint (2026-10-14)

Starting at `35fc1299c015ae46966f2ca5556bd3199a0a244c`, the four-month Level 2 and
scoped test-harness synchronization corrections passed the final JDK 17 validation:
full JVM **513/513**; API 35 emulator instrumentation **360/360, 0 failures, 0
skips**; `assembleDebug`, `lintDebug` and `compileDebugAndroidTestKotlin` passed.
Focused Level 2 Compose, route, recreation and restoration tests also passed. The full
suite initially encountered an emulator system/package-manager failure; after
emulator recovery, focused reruns isolated rotation/order synchronization dependencies
in existing instrumentation fixtures. Those test-only waits/orientation requests were
corrected without weakening assertions; the complete suite then passed. No production
regression was identified. Lint reports 5 warnings (2 manifest `UnusedAttribute`, 2
legacy `VectorRaster`, 1 legacy `SetJavaScriptEnabled`) and 2 informational findings;
Kotlin reports the existing deprecated `VIBRATOR_SERVICE` warning. Physical-device
acceptance is **NOT RUN — deferred by owner**.

Current baseline reconciled on 2026-10-08 against `ccc6423`: the [workflow](.github/workflows/build-apk.yml) runs full native JVM tests, Python content/asset/freshness checks, `assembleDebug`, `lintDebug`, release guards and legacy Playwright tests. It has no explicit Android-test compilation/build gate and no instrumentation execution job. Compose/owner/navigation tests exist, including the recent native modes; existence and compilation do not prove execution. Browser speech mocks do not prove native audio, Compose behavior, Android navigation or device compatibility.

| Evidence layer | Current recorded status / boundary |
| --- | --- |
| JVM/content/build | The 2026-10-08 implementation pass recorded 438 JVM, 36 Python and 55 browser passes, plus debug build/lint and six release guards. [CI for ccc6423](https://github.com/paulyglocke/Basti-Learning-Adventure-Public/actions/runs/37766428034) passed build/browser jobs. |
| Android-test compilation | `compileDebugAndroidTestKotlin` passed locally for that implementation pass. This is separate from CI and from execution. |
| Instrumentation execution | Not run for that pass: no emulator/device connected. Earlier recorded emulator passes apply to their named revisions and suites, not automatically to new Maths, Wilma or Prepositions behavior. Current affected-suite execution remains open. |
| Physical acceptance | The ledger below records specific owner-reported S24 passes, not blanket acceptance. Preserve other dated evidence within its tested scope. New modes and the remaining S24 matrix are unverified; Fire Max remains outstanding. |
| Signed distribution / upgrade | Push CI produced a debug artifact; its distribution job was skipped. Stable-signed release packaging requires manual main-branch dispatch. Neither packaging nor release-guard tests prove an in-place physical upgrade. |

These results are carried forward from the dated [SESSION_HANDOFF.md](SESSION_HANDOFF.md), not rerun by this documentation reconciliation. Preserve historical counts, failures and commands there and in [BUILD_NOTES.md](BUILD_NOTES.md); newer full JVM/lint results supersede earlier “not run” gaps only for those layers. Requirements below are not a record of passed tests.

## Months ↔ Seasons release checkpoint — 2026-10-12

At `9a6fa54` (Phase 6 changes remain uncommitted), the full JVM suite passed
**508/508**; `assembleDebug`, `lintDebug` and `compileDebugAndroidTestKotlin` passed
under JDK 17.0.20.1. Focused emulator coverage passed for Months ↔ Seasons **13/13**,
Months Build the Year **12/12**, NativeTextVisibility **8/8**, corrected
SupportPresentation **12/12**, corrected Prepositions artwork-screen restoration
**4/4**, and corrected Seasons matching **1/1**. After those narrow test corrections,
the full API 35 emulator suite passed **347/347, 0 failures, 0 skips**; the focused
PrepositionsArtwork class passed **3/3**. The artwork test no longer treats raster
width as a proxy for visual relation: both Snake scenes use the shared 1448×1086
canvas, while the visual criterion is recorded in the content audit. No artwork was
changed. The Lint report lists **5 warnings** (2 `UnusedAttribute`, 2 `VectorRaster`,
1 legacy `SetJavaScriptEnabled`) and 2 informational `AutoboxingStateCreation`
recommendations. Automated validation is complete; commit/push and CI verification are
still pending. Physical acceptance is **NOT RUN — deferred by owner**.

The initial two NativeTextVisibility “No compose hierarchies found” failures did not
recur in isolation or the full suite; their original trigger was not captured, so do
not treat the transient runner symptom as a diagnosed product fix. The original four
SupportPresentation failures came from searching globally for hint wording that is
also present in answer choices; checks now target the dedicated support-message nodes.
The earlier Months/Seasons process crash also did not recur (Months Build the Year
**12/12**, full instrumentation completed; emulator `/data` had 4.1 GB available and
recent logcat contained no fatal app/runtime or low-memory event). Its original crash
cause remains unknown. Lint’s UAST `AlreadyDisposedException` occurred with JDK
25.0.3; a fresh no-daemon `lintDebug` under JDK 17 passed without cache invalidation or
dependency changes.

## Automated coverage

Interactive Seasons & Months Wheel coverage must verify all canonical month labels
and chronological order, exact 30° segments and 90° season boundaries, solved/wrong
alignment, snapping, retry/success feedback and state restoration. Check EN/DE,
portrait/landscape and 1×/1.5×/2× font scales; the supplied centre PNG must retain
aspect ratio and alpha, while its position remains fixed relative to the ring. Test
keyboard activation, TalkBack semantics, previous/next controls, route/Options and
recreation. The API 35 emulator is automated evidence only. Solved-state English
portrait and German landscape screenshots were visually inspected; physical-device
acceptance is **NOT RUN — deferred by owner**.

Seasonal weather exploration must cover all twelve defaults, curated alternatives,
EN/DE phrases, arrow-derived selection, rapid changes, and isolation from Match.
Test old MSW1 and new MSW2 checkpoints, weather visibility/override restoration,
silent recreation, manual audio policy and cancellation. The weather screen matrix
covers portrait/landscape × EN/DE × 1×/1.5×/2×, keyboard activation and labelled
48 dp controls. Pixel comparisons must establish movement for every effect, final
scene replacement after rapid changes, static frames when lifecycle is not resumed
or system animator scale is zero, and resumed movement on return. Inspect captures
of January snow, February frost, April rain, July sun, October wind/leaves, German
2× landscape and static weather. Screenshots alone are not motion/performance proof;
Fire-tablet frame rate, battery use, actual TalkBack and offline voice quality need
physical checks. Current run evidence is recorded in SESSION_HANDOFF.md.

Months Between coverage must enumerate all 12 middle-month definitions, explicitly
check November–December–January and December–January–February, verify unique targets
and four unique choices in deterministic 5/10 rounds, and exercise retry/replay/hint,
bilingual prompt/audio, exact restoration, progress identity and independent journal.
Compose coverage must keep both named endpoints visible and labelled while answering,
check EN/DE at 1×/1.5×/2× in portrait/short landscape, keyboard activation, target
size and relationship semantics. Emulator evidence is not physical-device acceptance.

Months ↔ Seasons coverage must assert the fixed meteorological Germany/Northern
Hemisphere mapping for all twelve months, including December–January–February winter;
verify EN/DE labels, stable month-task and season-choice identities, deterministic
balanced unique-target 5/10 rounds across seeds, and reachability of all months.
Exercise correct/wrong placement, retry, Replay, guide/hint support, accurate progress,
dedicated-journal isolation, exact silent restore, and route/Options/language/Activity
recreation. Compose coverage must verify all four illustrated and named destinations,
accessible group descriptions, keyboard activation, 48dp+ targets, placement retention,
and EN/DE at 1×/1.5×/2× in portrait and short landscape without horizontal clipping.
Physical-device acceptance remains **NOT RUN — deferred by owner**.

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

## Acceptance matrix additions — 2026-10-08 (unverified unless separately recorded)

Apply these additions on S24 and Fire alongside the checklist above. They expand its
activity inventory without rewriting the 2026-09-23/24 evidence. The original S24
upgrade, named Seasons flows and Wilma ordering auto-follow remain accepted; do not
reopen them without a regression. Record source/APK identity, device/OS, language,
voice/audio mode, orientation and pass/fail/blocked status. No new physical result
is asserted by this list.

| Activity / mode | Additional acceptance checks |
| --- | --- |
| Maths: How many? / Subitising | Stable 1–5 patterns legible without crowding; one semantic dot group; four numeric choices; Replay says only the prompt. |
| Maths: Numbers & Groups | Both quantity → numeral and numeral → quantity; independent accessible dot choices, no individual-dot focus targets; correct target/direction restored. |
| Maths: More or Fewer | Equal-size left/right displays; all Left/Right/Same outcomes; no claim of a separate fewer-question mode; speech does not automatically reveal counts. |
| Maths: Number Order | BEFORE/AFTER anchors remain within 1–5 without wraparound; MISSING sequence has one clear gap and one semantic group; exact sequence restore. |
| Maths: Add Together | Two distinguishable nonempty groups and plus sign, totals 2–5; no automatic addend/answer narration; numeric choices reachable in short landscape. |
| Wilma TODAY | Explicit Today anchor, Yesterday/Tomorrow prompts, Monday/Sunday wrapping, EN/DE question/option speech and exact mode/anchor restoration. No actual-date inference. |
| Wilma BILINGUAL | German left/English right, aligned seven-day rows, correct-language tap-to-speak, Off silence, readable 1.5× labels and focus order; no quiz/progress counter or completion flow. |
| Seasons MISSING / CLUES / MATCH | Gap and clue meaning, independent Listen, correct match preservation after mistakes, exact restoration and appropriate completion. MATCH is a four-season matching set, not a 5/10-question quiz. |
| Seasons COMBINED | Explicit Before/After semantics, both wrap boundaries, correct slot retained through retry, Help independent of Replay, fresh Again and task-specific ordered-pair progress evidence. |
| Months Explore / Find / Before-Next / Missing / Build the Year (3) | Canonical Year Wheel and December/January adjacency; bilingual names/prompts/audio/hints; existing quiz modes' deterministic 5/10 rounds; Build the Year's shuffled unique three-month window, December→January→February and other cyclic starts, tap-select/tap-place, example, wrong-placement retry, separate progress identity, exact silent restoration and pending-write retry. Check tile/slot position/action semantics, keyboard activation, EN/DE wrapping, portrait/short landscape and 1×–2× text. Emulator results do not establish physical acceptance. |
| Months Build the Year Level 2 (4) | All twelve four-month cyclic starts; December/January wrapping; deterministic unique 5/10 windows; non-solved shuffle; Level 1/2 independent selection and progress; retries, hints, replay, bilingual instructions, exact state restore and separate journal. Exercise tap placement, keyboard and accessibility slot/tile semantics, portrait/short landscape, 1×/1.5×/2× text and scrollable completion controls. |
| Months ↔ Seasons | Fixed Germany meteorological mapping for all twelve months, including December–January–February; four existing season illustrations; deterministic balanced unique 5/10 targets; tap assignment into cumulative groups; retry, guide/hint and Replay; bilingual prompt/audio; separate shared progress identity/journal; exact restoration through Options, Back and Activity recreation; TalkBack group labels, keyboard activation, large targets, EN/DE wrapping at 1×/1.5×/2×, portrait/short landscape and no horizontal clipping. Physical acceptance is **NOT RUN — deferred by owner**. |
| Tell Me revision 11 pilot | Three C bundles only (Ocean .03, Classroom .02, Zoo .08); A/B/C/A versus A/B/A rotation, paired EN/DE support, Options/language/recreation/background/Home behavior and text reachability. Keep conversation open-ended; imagery, German naturalness, TalkBack and physical S24/Fire review remain human acceptance items. |

| Prepositions v3 / content 1.3 | All 13 relations and scene-specific German phrases; BELOW “unter”, INSIDE “in”, OUTSIDE “außerhalb der Höhle”. Review both resolved replacement batches in-device against the [audit](PREPOSITIONS_CONTENT_AUDIT.md), including distance progression, full silhouettes and cave threshold separation. All 52 are now source-art CLEAR; that is not hardware acceptance. |
| Prepositions guided Listen / recovery | Question → stem → four phrases in visible order; separate option Listen never answers; highlight clears on cancellation/navigation/language change. Supported v1/v2 saved rounds retain original wording/state after upgrade; Again creates v3. Inspect progress identities independently of restored UI. |
| Other native consumers | Include Vocabulary, Follow the Instructions, Colour Sort, Animal Groups, Wash Hands and Tell Me in the broad sweep; test their actual interaction/recovery contracts rather than imposing quiz flow on every activity. |

Months Build the Year and Months ↔ Seasons physical-device acceptance is **NOT RUN — deferred by owner**.
The API 35 emulator matrix is automated evidence only; it does not establish child
usability, TalkBack quality, offline voice output, S24/Fire touch comfort or device
lifecycle acceptance.

For applicable quizzes, cover both 5/10 rounds, Help/Retry/Replay, partial/completed
restore, Again/Home and Options/language/return. Across modes, review airplane-mode
EN/DE and unavailable voices, All/Questions/Off, portrait/both landscapes, 1.5× text,
insets/touch, TalkBack or device equivalent, keyboard/D-pad, backgrounding and true
process recreation under the declared recovery policy. BILINGUAL is non-quiz; Tell Me
is an open-ended conversation with its own reset policy. Do not fabricate progress
for exploration or infer durable deduplication from UI alone. Preserve all existing
upgrade safety and blocked-record-inspection rules above.

Instrumentation belongs on the designated emulator (`ANDROID_SERIAL=emulator-5554`), not the stable S24/Fire installation. Do not bypass certificate/version failures by uninstalling, downgrading or clearing data.

## Vocabulary UX Refresh Phase 4D (2026-10-21; uncommitted)

The Vocabulary entry opens a chooser; Explore, Find and What Is It? remain the
three existing modes. The six word identities, translations, canonical image
mappings, question plans, audio owner/preferences, independent quiz
journals/checkpoints and Explore selection format are unchanged. Focused JVM
content/art/audio/navigation tests passed 35/35; the full JVM suite passed
555/555 with 0 skipped. Focused API 35 chooser/screen/text-choice/artwork/route
instrumentation passed 14/14. API 35 Home/Months/Seasons/Wilma/Tell Me route
regressions passed 14/15; one unchanged Seasons order-route test failed to find
`seasons-order-prompt` after selecting ORDER and failed again when run alone.
Keep that result visible; do not count it as a Vocabulary pass or suppress it.

The complete app-wide API 35 instrumentation suite was not completed. Gradle's
connected installation failed earlier with `INSTALL_FAILED_INSUFFICIENT_STORAGE`;
the final relevant tests ran through direct APK installation and
`AndroidJUnitRunner`. Debug build, Android-test compilation, lint and
`git diff --check` passed. Lint reported 0 errors and 5 existing warnings. See
[`docs/vocabulary-phase4d-review/REVIEW.md`](docs/vocabulary-phase4d-review/REVIEW.md)
for the detailed results and API 35 visual gallery.
