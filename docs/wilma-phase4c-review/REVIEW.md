# Wilma’s Week Phase 4C review

Status: implementation, testing and screenshot review complete; changes remain
uncommitted for owner visual approval. The dedicated chooser is entered directly from Days &
Seasons. It features Explore and preserves direct access to the six existing
Wilma phases. Explore retains the canonical seven-day artwork, order, colours,
selection, speech and replay behavior. All phases continue to use the existing
Wilma route and persistence owners.

## Existing activity inventory

| Mode | English | German | Route and session owner |
|---|---|---|---|
| Explore | Learn | Lernen | `WILMA`, shared `WilmaViewModel`; persisted day selection |
| Find | Find the day | Finde den Tag | `WILMA`, `wilma-find` journal/checkpoint |
| Relations | Before and after | Davor und danach | `WILMA`, `wilma-relations` journal/checkpoint |
| Order | Put the week in order | Ordne die Woche | `WILMA`, `wilma-order` journal/checkpoint |
| Bilingual | German & English weekdays | Wochentage auf Deutsch & Englisch | `WILMA`, shared `WilmaViewModel` and bilingual selection store |
| Today | Today, yesterday and tomorrow | Heute, gestern und morgen | `WILMA`, `wilma-today` journal/checkpoint |

All six phases share the same Compose screen and view model. Explore tapping a
day selects and speaks it; replay remains available. Find, Relations and Today
retain their existing question, hint, replay and feedback behavior. Order keeps
its placement, help and replay controls. Bilingual retains explicit English and
German speech per day. This work does not alter question generation, audio
policy, IDs, checkpoints, weekday order or colours.

The production entry remains Illustrated Home **Time & Seasons / Zeit &
Jahreszeiten** → **Days & Seasons / Tage & Jahreszeiten** → **Wilma’s Week /
Wilmas Woche**. The existing Days & Seasons button now opens `WILMA_CHOOSER`;
each selection changes to the existing `WILMA` route. Back from an activity
returns to the chooser, chooser Back returns to Days & Seasons, and explicit
Home returns to Illustrated Home. Options overlays preserve the originating
route. Old saved `WILMA` route states still restore directly to the activity.

Explore requires a day tap to select and speak the day; the existing week strip,
day names, colours, order, swipe interaction and replay remain available.
Quiz modes retain their own speaker controls, hints, retry/feedback and
completion. Order retains placement, help and replay. Bilingual still speaks
each day in its corresponding language. No active question is discarded when
returning to the chooser; journal/checkpoint identities and formats are
unchanged.

## Visual evidence

Gallery: [API 35 emulator screenshots](index.html), with capture procedure in
[`capture.py`](capture.py). All 14 images came from the actual `MainActivity`
route. Phone portrait images are 1080×2400; genuine short-landscape images are
2400×1080; tablet portrait images are 1600×2560; tablet landscape images are
2560×1600. They were opened and visually inspected.

The chooser makes Explore the first, illustrated action; practice remains
directly reachable. German names wrap naturally at 2× with scrolling. On a
phone, the full chooser is intentionally scrollable and the bilingual activity
card continues below the initial viewport. The tablet chooser uses a two-column
practice grid and a side-by-side Explore illustration and copy; no content is
clipped. Explore is content-led on phone and tablet. Short landscape keeps
Wilma, the seven day segments and selected day visible; lower replay/navigation
controls remain scroll-reachable. The Find practice capture includes the
emulator’s existing “speech unavailable” notice because offline voice
acceptance was not performed; this does not change the application’s audio
policy. No blocking hierarchy, artwork or clipping issue was observed.

## Validation

The focused Wilma JVM run passed **24/24**; the full JVM suite passed
**555/555**, zero failures/errors/skips. Focused chooser instrumentation passed
**24/24** and the production route/restoration instrumentation passed **1/1**.
The full API 35 Android instrumentation run passed **567/567**, zero failures or
skips, including Months/Seasons regression and production navigation. The first
complete Gradle run had five stale test expectations (four previous Seasons
`learn` selector checks and one Months destination expecting `MONTHS` instead of
`MONTHS_CHOOSER`); the tests were updated to assert the current content-first
destinations, and the direct full rerun passed. Gradle's later connected-test
installer failed with `INSTALL_FAILED_INSUFFICIENT_STORAGE`; the debug and test
APKs installed directly and the full suite ran through AndroidJUnitRunner.

Final debug and Android-test APK assembly and `lintDebug` passed after the final
source edit; lint reports **0 errors, 5 existing warnings and 2 informational
findings**. `git diff --check` passed. After the 567/567 instrumentation run, one
temporary guard that suppressed speech during a persistence failure was removed
to restore the exact pre-task audio condition; the final source was rebuilt and
linted. That one-line restoration does not affect chooser/navigation logic but
was not followed by a second complete 567-test run. Do not describe the complete
run as testing that restored edge condition.

## Exact working-tree inventory

The working tree has 40 changed paths. Six support the separate Phase 4B
evidence/status correction (including the shared QA/handoff documents); the
remaining 34 support Phase 4C.

Shared and Phase 4B correction paths:

- `SESSION_HANDOFF.md`
- `TESTING_QA_SPEC.md`
- `docs/seasons-phase4b-review/REVIEW.md`
- `docs/seasons-phase4b-review/capture.py`
- `docs/seasons-phase4b-review/gallery/08-explore-short-landscape.png`
- `docs/seasons-phase4b-review/index.html`

Phase 4C implementation, tests and navigation contract:

- `UX_NAVIGATION_SPEC.md`
- `app/src/main/java/com/bellfamily/bastischool/MainActivity.kt`
- `app/src/main/java/com/bellfamily/bastischool/ShellNavigation.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/wilma/WilmaActivityChooserScreen.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/wilma/WilmaScreen.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/wilma/WilmaViewModel.kt`
- `app/src/test/java/com/bellfamily/bastischool/VocabularyNavigationTest.kt`
- `app/src/test/java/com/bellfamily/bastischool/WilmaNavigationTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/CompactTextVisibilityTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/HomeProductionNavigationTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/common/ActivityActionAdoptionTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/common/RemainingSupportPresentationTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/wilma/WilmaActivityChooserScreenTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/wilma/WilmaBilingualScreenTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/wilma/WilmaDayColoursScreenTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/wilma/WilmaRouteTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/wilma/WilmaScreenTest.kt`

Phase 4C visual review artifacts:

- `docs/wilma-phase4c-review/REVIEW.md`
- `docs/wilma-phase4c-review/capture.py`
- `docs/wilma-phase4c-review/index.html`
- `docs/wilma-phase4c-review/gallery/01-chooser-en-phone.png`
- `docs/wilma-phase4c-review/gallery/02-chooser-de-phone.png`
- `docs/wilma-phase4c-review/gallery/03-chooser-de-2x.png`
- `docs/wilma-phase4c-review/gallery/04-explore-en-phone.png`
- `docs/wilma-phase4c-review/gallery/05-explore-de-phone.png`
- `docs/wilma-phase4c-review/gallery/06-practice-de-phone.png`
- `docs/wilma-phase4c-review/gallery/07-chooser-short-landscape.png`
- `docs/wilma-phase4c-review/gallery/08-explore-short-landscape.png`
- `docs/wilma-phase4c-review/gallery/09-chooser-tablet-portrait.png`
- `docs/wilma-phase4c-review/gallery/10-explore-tablet-portrait.png`
- `docs/wilma-phase4c-review/gallery/11-options-return.png`
- `docs/wilma-phase4c-review/gallery/12-practice-back-to-chooser.png`
- `docs/wilma-phase4c-review/gallery/13-chooser-tablet-landscape.png`
- `docs/wilma-phase4c-review/gallery/14-explore-tablet-landscape.png`

## Acceptance limits

Physical-device acceptance, actual TalkBack, switch access, child usability and
offline voice quality are owner-deferred and not claimed as passed. UX-ANR-01
remains independent. All changes remain uncommitted and unpushed for owner visual
review.
