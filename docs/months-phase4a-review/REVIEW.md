# Months UX Refresh Phase 4A — review

**Status:** implemented, uncommitted and unpushed. Baseline is
`00fdaac51eb87ac1a3f99b7d25c15332274b9a07` on `main`.

## What changed

Days & Seasons now opens a bilingual Months chooser, with Explore the Year Wheel
featured first. Find Month, Before and Next, Missing Month, Between, Build the Year,
Sort Months into Seasons and the Seasons & Months Wheel remain directly available.
Build the Year still offers only the existing three- and four-month levels. No
learning content, task generation, difficulty, score, audio policy, artwork,
progress identity, saved format or dependency changed.

Explore now leads with the existing Year Wheel, then provides month names, Listen,
season groupings, save/audio recovery and Home. Tablets place the wheel beside its
controls. Short landscape uses a compact side-by-side layout. The chooser and Explore
screen use the warm neutral foundation, shared rounded controls and existing
month/season colours; the Year Wheel geometry and weather remain untouched.

## Visual review

The [screenshot gallery](index.html) contains 14 real MainActivity captures from the
API 35 emulator: English and German chooser; German chooser at 2×; English and German
Explore; short phone landscape; tablet portrait and landscape; Between question and
its contextual Year Wheel; Build Year level selection; and Seasons Wheel entry plus
Explore and Match modes.

The first visual pass found two issues, both corrected in the final captures:

- The duplicated shell title split “Monate” awkwardly at 2×. The chooser and Explore
  content headings are now the visible page titles; Back and Options remain in the
  shell bar.
- The phone landscape viewport showed only part of the Year Wheel. The compact split
  now shows the complete wheel, selected month and Listen control without changing
  rotation geometry.

The chooser keeps Explore prominent and its remaining choices scroll without
shrinking text. German labels wrap and scroll at 2×. Tablet space goes to the wheel
and month/season choices rather than scaling the phone column. Between's contextual
return action remains visible; the chooser keeps Build Year levels separate; and the
Seasons Wheel still exposes both modes beside its original artwork. No clipping or
overlap was observed in the final capture review.

## Validation and limits

- Focused Months JVM tests: **57/57 passed**. Full JVM: **555/555 passed**, with no
  failures, errors or skips.
- Final complete Months API 35 instrumentation: **185/185 passed**, zero failures or
  skips. This includes chooser and Explore presentation, learning routes, Between's
  contextual return, Build Year levels/session restoration, and Seasons Wheel modes.
- The earlier complete run had **169/185 passed, 16 failed, zero skipped**. Fourteen
  wheel cases used a semantics tag on the wrong node; the tag is now on the scrolling
  column. One route assertion expected the old Explore parent; it now expects the
  chooser. The remaining four-month failure reproduced as
  `CalledFromWrongThreadException`; its stack identified
  `MonthsBuildYearFourScreenTest.kt:89`, where the test called Compose keyboard input
  mode from the instrumentation runner thread. The stack continued through Compose
  focus and Android `ViewRootImpl.requestChildFocus`. Dispatching the request via
  `runOnUiThread` preserves the assertion and fixes the test synchronization. The
  next full run passed **185/185**. This was a test-thread issue, not a production UI
  defect or emulator failure. The first rerun's XML and logcat are under
  `app/build/outputs/androidTest-results/connected/debug/`.
- `assembleDebug`, `assembleDebugAndroidTest`, `lintDebug` and `git diff --check`
  passed. Existing lint reports five warnings and two informational findings; Kotlin
  reports the existing deprecated `VIBRATOR_SERVICE` warning.
- `assembleDebug`, `assembleDebugAndroidTest`, `lintDebug` and `git diff --check`
  passed. Existing lint reports five warnings and two informational findings; Kotlin
  reports the existing deprecated `VIBRATOR_SERVICE` warning.
- Emulator captures and instrumentation do not establish physical S24/Fire behavior,
  TalkBack or switch-access usability, child usability, or offline voice quality.
  Those acceptance items remain **NOT RUN — owner-deferred**. UX-ANR-01 remains
  independent.

## Exact changed-file inventory

Modified tracked files:

- `TESTING_QA_SPEC.md`
- `UX_NAVIGATION_SPEC.md`
- `SESSION_HANDOFF.md`
- `app/src/main/java/com/bellfamily/bastischool/MainActivity.kt`
- `app/src/main/java/com/bellfamily/bastischool/ShellNavigation.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/months/MonthsScreen.kt`
- `app/src/test/java/com/bellfamily/bastischool/HomeIntegrationTest.kt`
- `app/src/test/java/com/bellfamily/bastischool/MonthsNavigationTest.kt`
- `app/src/test/java/com/bellfamily/bastischool/ShellNavigationTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/months/MonthsRouteTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/months/MonthsQuizRouteTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/months/MonthsBuildYearFourScreenTest.kt`

New source and review files:

- `app/src/main/java/com/bellfamily/bastischool/ui/months/MonthsActivityChooserScreen.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/months/MonthsActivityChooserScreenTest.kt`
- `docs/months-phase4a-review/REVIEW.md`
- `docs/months-phase4a-review/capture.py`
- `docs/months-phase4a-review/index.html`

Gallery images:

- `docs/months-phase4a-review/gallery/01-chooser-en.png`
- `docs/months-phase4a-review/gallery/02-chooser-de.png`
- `docs/months-phase4a-review/gallery/03-chooser-de-2x.png`
- `docs/months-phase4a-review/gallery/04-explore-en.png`
- `docs/months-phase4a-review/gallery/05-explore-de.png`
- `docs/months-phase4a-review/gallery/06-explore-phone-landscape.png`
- `docs/months-phase4a-review/gallery/07-explore-tablet-portrait.png`
- `docs/months-phase4a-review/gallery/08-explore-tablet-landscape.png`
- `docs/months-phase4a-review/gallery/09-between-question.png`
- `docs/months-phase4a-review/gallery/10-between-contextual-wheel.png`
- `docs/months-phase4a-review/gallery/11-build-year-levels.png`
- `docs/months-phase4a-review/gallery/12-seasons-wheel-entry.png`
- `docs/months-phase4a-review/gallery/13-seasons-wheel-explore.png`
- `docs/months-phase4a-review/gallery/13-seasons-wheel-match.png`

No commit or push was made. Remaining acceptance is owner-led physical-device,
TalkBack, switch-access, child-usability and offline-voice review. UX-ANR-01 remains
independent. The Year Wheel geometry and saved-progress formats were not changed.

## Deferred roadmap proposal

The master roadmap now records **Living Year Wheel — Interactive Seasonal
Landscape** as a proposed, not-started Months Explore enhancement, scheduled after
the current Animals & Habitats priority and committed curriculum milestones. It
explores one landscape changing continuously with wheel position across the year.
No implementation or acceptance criteria are completed by this cross-reference;
see [the roadmap proposal](../../MASTER_PRODUCT_LEARNING_ROADMAP.md).
