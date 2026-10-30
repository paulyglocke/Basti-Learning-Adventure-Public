# Phase 5C.6 — Match Animals to Habitats

## Status

**READY TO COMMIT.** Implementation, focused interaction review, fresh full JVM suite, complete unfiltered API 35 suite, builds, lint and whitespace validation all pass. The changes remain uncommitted and unpushed for owner review.

## Starting state

- Branch: `main`.
- Starting local HEAD and `origin/main`: `cd9d25548d76c726d398122593c82090c30abeb8`.
- Working tree at start: clean.
- Device selection: disposable API 35 `emulator-5554` (Rainbow_R3B_API35, 4 vCPU/6 GB AVD configuration). `emulator-5556` was not used.
- Prior complete baseline: 595/595 JVM and 602/602 API 35 tests. These are historical, not evidence for this change.

## Activity identity and curriculum

- Activity: `activity.discover.habitat_matching`, revision 1.
- Content version: 1.1.
- Skill: `skill.discover.animal_habitat_matching`.
- Checkpoint namespace: `animal-habitat-match-session`; it is distinct from Animal Groups, Colours, and Months.
- The authored starter has seven prompts, shuffled per session. Answer-card order is independently shuffled. App round-length preferences do not truncate this fixed starter pack.
- Questions and accepted options are catalogued in [QUESTION_BANK.md](QUESTION_BANK.md).

Each question stores a non-empty set of accepted canonical habitat IDs. Six prompts have a single accepted scene/factual answer. The Rabbit “place where we met rabbits” item accepts both Woodland and Underground, because both are taught contexts. A separate warrens question asks explicitly about shelter. Prompts about the other animals refer to the app's pictured learning scene or a specific relation. No item asserts that the pictured habitat is the animal's only possible environment. Generic clownfish is not used as a freshwater species; Fox has no separate canonical cutout in this reviewed animal set and is not included.

## Assessment, persistence, and progress

The activity uses the existing `DurableSessionHost`, progress codec, attempt recorder, and completion event model. Wrong answers record an attempt and remain on the same question for explicit retry; a correct answer advances only through Continue. Continue is tied to the current task identity and duplicate advancement/completion is guarded by the shared host. Starting Again creates a new session without rewriting prior results.

The new checkpoint schema is version 2 and serializes the full accepted-answer set. It restores legacy schema-v1 choice questions by treating the former single `correct` ID as the sole accepted answer. The matching journal uses the independent file `animal-habitat-match-session`; no historic journal identity or migration was changed. Instrumentation verifies partial-session restore after Activity recreation, not Android process death. Language switching preserves question, accepted IDs, answer and progress.

The route is available from Animals & Nature. It creates attempt and completion records under only the new matching activity ID. The test completed seven tasks with eight attempt records after one wrong answer, and one completion event containing seven tasks. No Animal Groups result was emitted by this route. Original Animal Groups `activity.discover.animal_groups`, revision 1, fixed Whale/Dolphin/Horse/Rabbit Water/Land pool, AGR1 journal and progress identity remain unchanged.

## Interaction and accessibility

The screen presents one prompt, one canonical animal image, two large image-led habitat choices (the Rabbit multi-answer scene question has three), a progress count, Replay, Help, and Home. Choice cards are buttons with bilingual accessible labels and selected/correct/retry state descriptions. A thick outline and explicit text communicate selection/correctness without relying on colour. Correct feedback has a Continue button; wrong feedback has a neutral Try again action. There are no timers or punitive scoring.

Replay, Help, Sound Off, Options/language return, Back, Home, Activity recreation and session continuation were exercised on the emulator. Speech requests use the existing offline speech controller. Sound was disabled in the UI test to prevent external voices/audio timing from affecting assertions; actual offline voice quality remains owner-managed. No online service was added.

At 2× font scale, habitat cards become a single column so long German labels are not cramped into narrow side-by-side cards. Scroll resets to the start for a new question, language/font/orientation/viewport change, and a newly composed route, keeping the current instruction easy to find. Landscape and short-landscape first viewports show the task prompt; the answer cards and Replay require scrolling and were verified reachable. Tablet dimensions below are emulator display overrides, not physical tablet testing.

## Visual evidence

The [genuine emulator screenshot gallery](gallery.html) contains 15 captures (PNG dimensions were asserted in the instrumentation capture helper): 1080×2400 phone portrait, 2400×1080 phone landscape, 1280×720 short landscape, 1600×2560 and 2560×1600 tablet-sized emulator overrides, plus German 1.5× and 2×. It covers navigation, English/German question screens, wrong/retry, correct, Continue/mid-session, Activity recreation, completion, Options return, and responsive layouts. Images are direct Android UiAutomation screenshots saved from the running application, not mockups.

Visual inspection found the images distinguishable and choices comfortably sized in the portrait layouts. Feedback is readable and sufficiently explicit. German 1.5× remains a two-column card layout with readable labels. At German 2×, one-column choices keep `Süßwasser` and `Unter der Erde` legible, at the cost of a longer scroll. Short landscape exposes the instruction and animal; choices sit below the fold but remain reachable. The route test also verifies Replay's scroll reachability in short landscape.

## Test evidence

### Focused instrumentation

`HabitatMatchingRouteTest.matchingActivityRunsAndCapturesRealBilingualResponsiveStates` passed **1/1**, with 0 failures/errors/skips, on API 35 `emulator-5554`. It verifies Animals & Nature entry, question assets and semantics, bilingual identity-preserving language change, incorrect answer/retry, correct answer/Continue, seven-question completion and progress records, Activity recreation, Options return, 1.5×/2× German, landscape and tablet-sized viewport layouts, and Back to the category. After the full suite, the test was extended to tap Replay while Sound Off is enabled and assert that question identity/progress remain unchanged; that final focused rerun also passed 1/1. Its XML is retained under `api35/focused-reports/`. Three earlier focused attempts exposed test-language assertion/setup issues; those harness issues were corrected. Only the final 15 successful genuine captures are included here.

### Final full suites and builds

- Focused JVM: **47/47 passed**, zero failures/errors/skips, covering matching content/host, checkpoint, Home cards/integration, and shell navigation. Focused XML is retained under `jvm/focused-reports/`.
- Fresh full JVM: **607/607 passed**, zero failures/errors/skips. Complete XML reports are retained under `jvm/reports/`.
- Focused API 35: **1/1 passed**, zero failures/errors/skips. The final rerun includes Replay while Sound Off is enabled; its XML is retained under `api35/focused-reports/`.
- Complete unfiltered API 35: **603/603 passed**, zero failures/errors/skips, on API 35 `emulator-5554` in **42m 23s**. The report XML has 603 tests, 0 failures, 0 errors, 0 skipped; the complete runner logcat/UTP output and `test-result.pb` are retained under `api35/reports/`.
- `assembleDebug`, `assembleDebugAndroidTest`, and `lintDebug`: passed. Lint reports 0 errors and 5 warnings (the existing project warning set); production compilation also emits the existing `VIBRATOR_SERVICE` deprecation warning.
- `git diff --check`: passed.

The API 35 instrumentation command was unfiltered and targeted only the disposable `emulator-5554`; `emulator-5556` was not used. Historical counts of 595 JVM and 602 API 35 are superseded by these final-code results.

Three early focused-run failures were test-harness assertions rather than product failures: the progress assertion expected English while the UI was German; `performScrollTo` was called on a completion screen with no scroll parent; and a Back assertion expected the category root tag rather than the matching activity card visible within that category. Each was corrected. Landscape and 2× capture actions were reordered so screenshots represent the initial viewport before Replay reachability is checked. The complete unfiltered API run passed all 603 discovered tests against the final production code. A subsequent test-only Replay/Sound Off assertion passed in a focused 1/1 rerun; production code did not change after the complete run.

## Known limitations and owner-managed acceptance

- Activity recreation is verified; OS process-death recovery is not claimed.
- Emulator viewport overrides do not establish physical tablet or phone acceptance.
- Actual child-use, physical-device touch behavior, TalkBack, switch access, and audible offline voice quality need owner review.
- The seven-question activity is a bounded starter, not a general quiz engine; a more difficult level is deferred.
- A fixed seven-item pack is intentionally independent of the standard 5/10 round preference.

## Changed-file inventory

Production Kotlin:

- `app/src/main/java/com/bellfamily/bastischool/MainActivity.kt`
- `app/src/main/java/com/bellfamily/bastischool/ShellNavigation.kt`
- `app/src/main/java/com/bellfamily/bastischool/learning/progress/SessionProgressRecorder.kt`
- `app/src/main/java/com/bellfamily/bastischool/learning/session/SessionCheckpoint.kt`
- `app/src/main/java/com/bellfamily/bastischool/learning/session/SessionModels.kt`
- `app/src/main/java/com/bellfamily/bastischool/learning/session/SessionReducer.kt`
- `app/src/main/java/com/bellfamily/bastischool/learning/animals/HabitatMatchingContent.kt` (new)
- `app/src/main/java/com/bellfamily/bastischool/ui/home/HomeCatalogue.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/habitats/HabitatMatchingScreen.kt` (new)
- `app/src/main/java/com/bellfamily/bastischool/ui/habitats/HabitatMatchingViewModel.kt` (new)

Tests:

- `app/src/androidTest/java/com/bellfamily/bastischool/HomeRouteTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/habitats/HabitatMatchingRouteTest.kt` (new)
- `app/src/test/java/com/bellfamily/bastischool/HomeCardsTest.kt`
- `app/src/test/java/com/bellfamily/bastischool/HomeIntegrationTest.kt`
- `app/src/test/java/com/bellfamily/bastischool/ShellNavigationTest.kt`
- `app/src/test/java/com/bellfamily/bastischool/learning/animals/HabitatMatchingContentTest.kt` (new)
- `app/src/test/java/com/bellfamily/bastischool/learning/animals/HabitatMatchingHostTest.kt` (new)
- `app/src/test/java/com/bellfamily/bastischool/learning/session/SessionCheckpointTest.kt`
- `app/src/testDebug/java/com/bellfamily/bastischool/ui/homeprototype/HomePrototypeModelTest.kt`

Documentation and evidence:

- `MASTER_PRODUCT_LEARNING_ROADMAP.md`
- `SESSION_HANDOFF.md`
- `TESTING_QA_SPEC.md`
- `UX_NAVIGATION_SPEC.md`
- `docs/animals-phase5c6-habitat-matching-review/QUESTION_BANK.md`
- `docs/animals-phase5c6-habitat-matching-review/REVIEW.md`
- `docs/animals-phase5c6-habitat-matching-review/gallery.html`
- 15 genuine PNG screenshots under `docs/animals-phase5c6-habitat-matching-review/screenshots/`
- 114 full-JVM report artifacts under `jvm/reports/` (111 XML files and Gradle report binary files), plus 6 focused XML reports under `jvm/focused-reports/`.
- 616 API 35 report/diagnostic artifacts under `api35/reports/`, including the aggregate 603-test XML, `test-result.pb`, device/runner metadata, UTP logs, and 603 per-test logcat files.
- 1 focused API 35 XML report under `api35/focused-reports/` for the final Replay/Sound Off assertion.

No historical Animal Groups data or unrelated habitat content was changed.
