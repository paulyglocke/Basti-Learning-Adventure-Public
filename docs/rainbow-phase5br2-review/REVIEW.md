# Phase 5B-R2 — Rainbow Order persistence and progress review

**Status:** Focused implementation complete; no commit or push.

**Baseline:** `483f81071b62a8b3174a842e336675cfaf172b64` (`main` / `origin/main`).

## Prior state and correction

The earlier Phase 5B-R checkpoint added the Rainbow routes and preliminary screens, but Rainbow Order used Compose-local state. It had no durable task journal, Progress Tracker events, event outbox, or process-owner restoration. The earlier focused Rainbow content/screen tests did not establish persistence. That result remains historical and is not reclassified as a pass.

R2 replaces the production route's prototype state with an activity-owned host using the established ordered-placement and write-ahead delivery pattern used by Seasons ordering. The pre-existing prototype screen and its earlier tests remain in the tree; MainActivity now uses `RainbowOrderSessionScreen` backed by `RainbowOrderViewModel` and `RainbowOrderHost`.

## Session and identity contract

| Field | Starter | Full |
|---|---|---|
| Activity | `activity.colours.rainbow_order` | `activity.colours.rainbow_order` |
| Activity revision / content | revision 1 / content 1.1 | revision 1 / content 1.1 |
| Target | Red → Yellow → Blue → Violet | Red → Orange → Yellow → Green → Blue → Indigo → Violet |
| Journal | `rainbow-order-starter-session` | `rainbow-order-full-session` |
| Task definition | `task.colours.rainbow_order.starter.position_N` | `task.colours.rainbow_order.full.position_N` |
| Skill | `skill.colours.rainbow_sequence` | `skill.colours.rainbow_sequence` |
| Context | `context.colours.rainbow_order.starter` | `context.colours.rainbow_order.full` |
| Difficulty evidence | 1 | 2 |

The selected level is durably pointed to by the independent checksummed
`rainbow-order-active-level` store. This lets a fresh ViewModel reopen the active
level rather than defaulting to Starter and overlooking a Full session. The
two level journals stay independent. Both journal and pointer formats are new to
Rainbow and versioned; no historical activity journal is migrated.

Each RBO1 journal stores session ID, level, language, seed, exact initial choice
order, all task progress (attempts, retries, last choices, replay/help support),
completion/acknowledgement state, and encoded pending Progress Tracker events.
Journal payloads are SHA-256 checked. A level mismatch or corrupt record is
rejected without replacing the on-disk bytes. The interaction places one piece
immediately, so there is no separate in-flight selected piece to restore.

## Progress and support semantics

Starter uses the four positions Red, Yellow, Blue and Violet, while the full
seven-colour conventional rainbow stays visible as its example. The shared task
support schema has no separate “model continuously visible” property. The closest
existing accurate representation is `SupportUse.hint=true` on each Starter
placement and its completion evidence. Starter therefore cannot be represented
as unsupported independent mastery. Full only records hint support after Help
is used. Replay is support metadata and produces no attempt event by itself.

Every accepted correct or incorrect placement records an `AttemptEvent` with the
stable `ProgressEventId.attempt` identity. Completion emits one
`CompletionEvent` with all four or seven actual task results and the stable
`ProgressEventId.completion(session)` identity. Explore remains unscored and
does not use either Rainbow Order journal or these progress identities.

The host writes the updated checkpoint and pending event batch before delivering
events. A failed append leaves the write-ahead batch available for retry. Progress
Repository event IDs deduplicate a retry after an append succeeded but its journal
acknowledgement was interrupted. The host blocks new actions while delivery is
pending. If checkpoint persistence fails before a durable replace, the ViewModel
does not publish the proposed placement as current; Retry reloads the prior durable
checkpoint. If an acknowledgement replace committed but reported failure, reopening
reads the acknowledged state and does not add a second completion.

## Compatibility boundaries

The original Colour Sort, Find, Match, Four-Colour Sort, Explore Colours and
Animal Groups source contracts were not changed by the R2 learning model. Rainbow
uses new session files and progress contexts; it does not expand the existing
four-colour pools or rewrite old journals. The API 35 historical CSR1 partial
fixture route test passed and completed its four-object session with the original
identity. This is fixture-based evidence, not a universal byte-compatibility
claim. Months, Seasons and Wilma persistence formats are untouched.

## Navigation and language

The Colours chooser opens Rainbow Order through its existing distinct route.
Android Back returns to the chooser, then to Home; Options returns to the same
Rainbow session. The route test exposed and fixed a language-return issue: changing
language in Options while the ViewModel was hidden left the restored state in the
old language and disabled actions. On becoming visible, the ViewModel now persists
the language change without changing the semantic order or regenerating the round.

## Evidence

Final test/build evidence must be updated here after the last verification pass:

| Check | Result |
|---|---|
| Focused Rainbow JVM suite | **10/10 passed** (8 persistence + 2 content) |
| Full JVM suite | **579/579 passed**, 0 failures/errors/skips |
| API 35 Rainbow owner restoration | **1/1 passed**; fresh ViewModels restored Starter and Full checkpoint state, support and completion without duplicate events |
| API 35 Rainbow production route | **1/1 passed**; chooser → Order, Options language return, Back to chooser and Home |
| API 35 historical Colour Sort fixture | **1/1 passed**; CSR1 partial route restored and completed once |
| API 35 existing Colours chooser regression | **1/1 passed**; existing chooser, Explore, Find, Match, Four-Colour Sort and original two-colour Sort remained available |
| `assembleDebug` / `assembleDebugAndroidTest` / `lintDebug` | All passed. Lint: 0 errors, 5 existing warnings (two manifest API attributes, two vector-size notices, legacy WebView JavaScript enablement) |
| `git diff --check` | Passed |
| Full API 35 instrumentation | Not run; remains an explicit Phase 5B-R3 follow-up, not a pass |

The first owner-restoration instrumentation run failed because its test setup
requested Full before the new ViewModel had completed its default Starter restore.
The corrected test explicitly waits for each owner operation. A subsequent run
found an assertion checking support on the current (third) task rather than the
helped first task; the assertion now checks the persisted first step. The final
focused owner test passed. The first production-route run found the real hidden-
ViewModel language synchronization defect described above; after correction,
the route passed. A following assertion was adjusted to scroll the chooser card
into view before asserting display; the final route run passed.

All focused connected tests were pinned to `emulator-5554`
(`Colours_Phase5A1B_API35`), not the owner emulator. The existing Colours route
regression takes about six minutes because it also exercises responsive captures;
it completed successfully.

## Remaining limitations and R3 work

- At the R2 checkpoint, the complete app-wide API 35 instrumentation suite was
  incomplete. The prior 579/579 baseline was before the Rainbow changes; the
  later R3 complete run is recorded below.
- Android restoration coverage reconstructs a fresh ViewModel against isolated
  durable storage. It does not simulate OS process termination or device power
  loss.
- Existing shared `SupportUse` cannot distinguish an always-visible example
  from an explicitly requested hint; Starter is conservatively marked supported.
- Physical-device, actual TalkBack, switch-access, child-use, and offline-voice
  acceptance remain owner-managed. `UX-ANR-01` remains separate.

## Exact changed-file inventory

- `PROGRESS_TRACKER_SPEC.md`
- `SESSION_HANDOFF.md`
- `TESTING_QA_SPEC.md`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/colours/ColourScreensTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/colours/RainbowOrderPersistenceTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/colours/RainbowOrderRouteTest.kt`
- `app/src/main/java/com/bellfamily/bastischool/MainActivity.kt`
- `app/src/main/java/com/bellfamily/bastischool/ShellNavigation.kt`
- `app/src/main/java/com/bellfamily/bastischool/learning/colours/ColoursAudio.kt`
- `app/src/main/java/com/bellfamily/bastischool/learning/colours/RainbowContent.kt`
- `app/src/main/java/com/bellfamily/bastischool/learning/colours/RainbowOrder.kt`
- `app/src/main/java/com/bellfamily/bastischool/learning/colours/RainbowOrderHost.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/colours/ColoursActivityChooserScreen.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/colours/ColoursViewModel.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/colours/RainbowOrderSessionScreen.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/colours/RainbowOrderViewModel.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/colours/RainbowScreens.kt`
- `app/src/test/java/com/bellfamily/bastischool/learning/colours/RainbowContentTest.kt`
- `app/src/test/java/com/bellfamily/bastischool/learning/colours/RainbowOrderPersistenceTest.kt`
- `docs/rainbow-phase5br2-review/REVIEW.md`

No application data, historical Colour Sort journal or emulator owner data was
cleared. Instrumentation used only `emulator-5554` (`Colours_Phase5A1B_API35`).

## Phase 5B-R3 full-suite attempt — 2026-10-26

Repository verification before the attempt: branch `main`, HEAD and
`origin/main` both `527730ec63754a477c25852d052f4d95b59b9905`, clean working tree.
The API 35 disposable AVD `emulator-5554` was booted and had 3.8 GB free in
`/data`; owner emulator `emulator-5556` was not used. No application data was
cleared by us.

The first full `connectedDebugAndroidTest` invocation installed the APKs but
reported **0 tests**, with UTP `INSTRUMENTATION_FAILED: Process crashed`. Its XML
has `tests=0`; the captured runner log contains only `shortMsg=Process crashed`,
with no assertion or app exception that identifies a product defect. This is a
runner-startup failure, not a test pass.

A diagnostic retry used the same serial and reported **583 total tests**. It
completed the English 1× `CompactTextVisibilityTest`, then the English 1.5× case
ended the run with `Input dispatching timed out` (the app's
`ComponentActivity` did not respond to a focus-loss event within 6 seconds).
The XML reported 2 tests, 1 failure, 0 errors, 0 skips. A separate broad live
logcat reader was running at the same time and produced excessive output; it may
have contributed to emulator load. The timeout is therefore recorded, not
reclassified as an application regression or dismissed as infrastructure.

A clean retry was run without a concurrent logcat reader. It announced 583
tests and completed **8 tests, 0 failures, 0 errors and 0 skips** before being
stopped because of the observed runtime. The 8 completed tests were four
`CompactTextVisibilityTest` cases (English/German at 1×/1.5×, 291.779 seconds
total) and four English 1× `HomeBrowseLayoutTest` cases (phone, short landscape,
tablet portrait and tablet landscape, 768.718 seconds total). The XML shows a
ninth test started but incomplete; it is not counted as completed. The clean
run had elapsed 1,106.532 seconds (18m 26.5s) when stopped; the eight completed
testcase durations sum to 1,060.497 seconds. This is not a full-suite result,
and the 575 remaining cases have no result from this run.

The clean retry had no reported test failures among its 8 completed cases.
It did not finish, so that attempt alone was incomplete. R3 was still open at
this point; the earlier 1/0 timeout and 0/0 startup failure remain part of the
validation history.

## Phase 5B-R3 complete full-suite rerun — 2026-10-26

The same disposable API 35 AVD was shut down cleanly and relaunched from its
existing user data with `-cores 4 -memory 6144 -no-snapshot-load -no-window
-no-audio`. The owner emulator `emulator-5556` remained untouched. Runtime
checks confirmed API 35, four guest CPUs and `sys.boot_completed=1`. The full,
unfiltered command was:

```sh
ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest --console=plain
```

Gradle discovered 583 tests and finished **583/583**, with 0 failures, 0 errors
and 0 skips. `BUILD SUCCESSFUL` completed in 48m 25s. The XML report records
`tests="583" failures="0" errors="0" skipped="0"` and 2888.64 seconds:
`app/build/outputs/androidTest-results/connected/debug/TEST-Medium_Phone_API_35(AVD) - 15-_app-.xml`.
This completes R3's remaining full API 35 validation item. The earlier startup
failure, diagnostic timeout, and interrupted 8/583 retry remain accurately
recorded above; none are presented as successful full-suite runs.

The R3 documentation-only changes remain uncommitted pending owner review.

R3 documentation-only changes are limited to `TESTING_QA_SPEC.md`,
`SESSION_HANDOFF.md` and this review. They are uncommitted pending owner review.

## Phase 5B-R3B visual acceptance follow-up — 2026-10-26

The next visual and interaction review is in
[`../rainbow-phase5br3b-review/REVIEW.md`](../rainbow-phase5br3b-review/REVIEW.md)
with a genuine API 35 emulator screenshot gallery. It records bounded fixes to
the Explore canvas/hit map and Rainbow Order responsive layout and destination
semantics. The R3 583/583 result predates these production UI changes and does
not validate them; R3B passed fresh complete API 35 instrumentation **586/586**,
with 0 failures/errors/skips. Its final XML is preserved in the R3B review
directory. The earlier failed/partial attempts above remain historical. No session/persistence model,
Progress Tracker schema or Colour Sort journal changed in R3B.
