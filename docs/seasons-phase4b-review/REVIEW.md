# Seasons Phase 4B review

**Status:** committed and pushed in `b516b0bc80b34900cc04d1507440db0bafaca888`;
the short-landscape Explore evidence was corrected in the Phase 4C working tree.
Implementation baseline: `6ea391af4fc2348ae7f59f82f0a17d216571c667`.

## Existing activity inventory

The existing route is Illustrated Home **Time & Seasons / Zeit & Jahreszeiten**
→ **Days & Seasons / Tage & Jahreszeiten** → Seasons. The old Seasons entry
opened one screen with phase selection above the learning content. The actual
source inventory is nine phases, not an inferred audit count:

| Mode | Existing title (English) | Existing title (German) |
| --- | --- | --- |
| Explore | Learn | Lernen |
| Practice | Practise | Üben |
| Next | What comes next? | Was kommt danach? |
| Before | What comes before? | Was kommt davor? |
| Missing | Missing season | Fehlende Jahreszeit |
| Clues | Season clues | Jahreszeiten-Rätsel |
| Order | Build the Year | Ordne die Jahreszeiten |
| Match | Match season to clue | Jahreszeit zu Hinweis |
| Combined | Before and after | Davor und danach |

Explore retains four seasonal names, order, illustrations, descriptions,
listen/replay and season-choice interactions. All practice modes remain reachable
from the new chooser. Months separately owns **Sort Months into Seasons / Monate
den Jahreszeiten zuordnen** and the Seasons & Months Wheel Explore/Match modes;
those routes and UI were not changed. The seasonal artwork assets and any wheel
geometry are unchanged.

## Implementation and navigation

The new chooser presents Explore as the primary entry and groups the eight
practice modes into recognition/sequence and ordering/connection sets. It uses
the canonical four-season artwork, existing palette and rounded surfaces, natural
wrapping, scrolling and accessible touch/keyboard activation. Explore no longer
starts with the full mode-selector row; seasonal learning content appears first.
On wide and short-landscape layouts, the existing picture and controls use a
side-by-side arrangement to make useful room for the learning content.

Shell navigation adds the chooser parent while retaining the existing Seasons
activity route and its `SeasonsViewModel` owner. Android Back from a phase returns
to the chooser; chooser Back returns to Days & Seasons. Home, Options return and
language change continue through established shell behavior. Existing journal
IDs, checkpoint formats and saved-session data are unchanged. Months-owned shared
activities remain on their original paths; no duplicate activity was introduced.

The tests exercise the actual destinations for all nine modes, the chooser’s EN/DE
labels, keyboard/touch operation, locale changes, Options return, back/home,
recreation and saved-session restoration, plus the Months package compatibility
and existing phase progression. No curriculum, question-generation, difficulty,
answer-checking, completion, scoring, hint, feedback, audio/TTS or progress-format
changes were made.

## Emulator gallery and visual review

The [HTML gallery](index.html) contains 14 actual MainActivity captures:

1. English chooser, phone portrait.
2. German chooser, phone portrait.
3. German chooser, phone portrait at 2× text.
4–5. Explore in English and German, phone portrait.
6. German practice activity, phone.
7. Chooser, short landscape phone.
8. Explore, short-landscape phone. The corrected screenshot is **2400×1080** and
   shows the existing German spring artwork, name, description, replay and first
   answer row in a real landscape frame. The remaining choices continue below the
   viewport and are reachable by scrolling. The capture helper now applies
   `cmd window user-rotation lock 1` after navigation so UiAutomator cannot revert
   the display rotation before capture.
9–12. Chooser and Explore on tablet portrait and tablet landscape.
13. Options language change returning to chooser.
14. Existing Months-owned Sort Months into Seasons activity.

The valid captures were inspected for hierarchy, clipping, German wrapping,
artwork proportions, touch-target readability and wasted space. The chooser is
visually related to Months while keeping its existing seasonal identity. Large
German copy wraps naturally and scrolls. Explore places the canonical seasonal
image and season name before replay and choices. The shared Months route remains
available. No blocking visual problem was observed in the valid captures. Emulator
TTS availability is environment-dependent; the shared activity screenshot shows
its existing offline-voice-unavailable feedback. This does not establish actual
offline voice quality.

## Validation

- Focused Seasons JVM tests: **61/61 passed**, no failures/errors/skips.
- Full JVM suite: **555/555 passed**, no failures/errors/skips.
- API 35 Seasons package instrumentation: **42/42 passed**, no failures/skips.
- API 35 complete Months package instrumentation: **185/185 passed**, no
  failures/skips.
- `assembleDebug`, `assembleDebugAndroidTest`, and `lintDebug`: passed; lint
  reports **0 errors, 5 existing warnings**.
- `git diff --check`: passed before the Phase 4C evidence correction.
- Complete app-wide instrumentation was **not run**.

One earlier complete Seasons instrumentation attempt recorded a checkpoint byte
length mismatch (6034 versus 6035) in `SeasonsProgressionRouteTest`. The focused
progression test then passed, and subsequent complete Seasons package runs passed
42/42, including after the final landscape presentation changes. The intermittent
cause was not established; it remains a small unresolved test-stability risk and
is not represented as a clean first-run history.

## Changed-file inventory

Tracked modifications:

- `UX_NAVIGATION_SPEC.md`
- `TESTING_QA_SPEC.md`
- `SESSION_HANDOFF.md`
- `app/src/main/java/com/bellfamily/bastischool/MainActivity.kt`
- `app/src/main/java/com/bellfamily/bastischool/ShellNavigation.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/seasons/SeasonsScreen.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/seasons/SeasonsViewModel.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/seasons/SeasonsCluesScreenTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/seasons/SeasonsProgressionRouteTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/seasons/SeasonsRouteTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/seasons/SeasonsScreenTest.kt`
- `app/src/test/java/com/bellfamily/bastischool/SeasonsNavigationTest.kt`
- `app/src/test/java/com/bellfamily/bastischool/VocabularyNavigationTest.kt`
- `app/src/test/java/com/bellfamily/bastischool/WilmaNavigationTest.kt`

New files:

- `app/src/main/java/com/bellfamily/bastischool/ui/seasons/SeasonsActivityChooserScreen.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/seasons/SeasonsActivityChooserScreenTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/seasons/SeasonsChooserRouteTest.kt`
- `docs/seasons-phase4b-review/REVIEW.md`
- `docs/seasons-phase4b-review/capture.py`
- `docs/seasons-phase4b-review/index.html`
- `docs/seasons-phase4b-review/gallery/01-chooser-en-phone.png`
- `docs/seasons-phase4b-review/gallery/02-chooser-de-phone.png`
- `docs/seasons-phase4b-review/gallery/03-chooser-de-2x.png`
- `docs/seasons-phase4b-review/gallery/04-explore-en-phone.png`
- `docs/seasons-phase4b-review/gallery/05-explore-de-phone.png`
- `docs/seasons-phase4b-review/gallery/06-practice-de-phone.png`
- `docs/seasons-phase4b-review/gallery/07-chooser-short-landscape.png`
- `docs/seasons-phase4b-review/gallery/08-explore-short-landscape.png` (corrected 2400×1080 landscape screenshot)
- `docs/seasons-phase4b-review/gallery/09-chooser-tablet-portrait.png`
- `docs/seasons-phase4b-review/gallery/10-explore-tablet-portrait.png`
- `docs/seasons-phase4b-review/gallery/11-chooser-tablet-landscape.png`
- `docs/seasons-phase4b-review/gallery/12-explore-tablet-landscape.png`
- `docs/seasons-phase4b-review/gallery/13-options-return.png`
- `docs/seasons-phase4b-review/gallery/14-shared-months-seasons.png`

## Remaining acceptance and recommendation

Physical-device, actual TalkBack, switch-access, child-usability and offline-voice
acceptance remain owner-deferred and are not claimed as passed. Complete app-wide
instrumentation was not run for the Phase 4B commit; the later Phase 4C tree passed
567/567 on API 35. UX-ANR-01 remains independent. Phase 4B is committed and
pushed; its short-landscape image and stale commit-status wording were corrected
in the Phase 4C working tree. Those evidence-only changes remain uncommitted.
