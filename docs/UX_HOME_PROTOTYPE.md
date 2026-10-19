# UX Refresh Phase 2 / 2B — isolated illustrated Home

Baseline: `c94687d8c5b38d5fcf94d97b091e95ba7992321b`. Phase 1 was committed,
pushed and passed Android/browser CI before this work. The owner approved Phase 2
and 2B for joint commit/push on 2026-10-19, subject to final validation. This does
not authorise production Home integration. This document supersedes the earlier handoff's
incomplete-brief note, not the production navigation specification.

## Boundary and inspection findings

The production `MainActivity.kt` catalogue still owns 20 functioning Home entries,
their bilingual titles/descriptions and dispatch identity. Its historical 5/4/11
grouping and the four hidden future games remain untouched. The prototype reads
`homeCardGroups()` and maps existing `mode` / `verbExplorer` identities; it does not
copy learning descriptions or create another curriculum registry.

All implementation lives in `src/debug/java/.../ui/homeprototype`. A separate exported
debug Activity supports explicit review launch, with no launcher entry or link from
production Home. The additive debug manifest declaration does not change Phase 1's
showcase or any release manifest. No existing Phase 1 Kotlin implementation is edited.

Phase 1's `BastiDesignTheme`, palette, typography, shapes, spacing, section heading
and illustrated category card are reused unchanged. Existing `NativeActionButton`
and `NativeSupportMessage` supply page actions and preview explanations. Phase 2B's
compact labelled header actions use Material `TextButton` with the same design roles
and 56dp minimum targets, without editing production headers. The Tell Me
banner component was inspected: its nine authored adventure identities are not reused
as subject identities, and its 4:1 composition is unsuitable as a small square subject
icon. No new framework, dependency, bitmap asset or production illustration is added.

## Complete mapping

| Category EN / DE | Existing identities, in prototype order |
|---|---|
| Numbers & Maths / Zahlen & Rechnen | `count`, `subitising`, `quantityMatch`, `compareQuantity`, `numberOrder`, `addTogether`, `math` |
| Words & Talking / Wörter & Erzählen | `tellme`, `vocabulary`, `verbExplorer`, `letters`, `positions` |
| Time & Seasons / Zeit & Jahreszeiten | `time`, `clock` |
| Animals & Nature / Tiere & Natur | `verbs`, `animalGroups` |
| Colours / Farben | `colourSort` |
| Everyday Skills / Alltag | `washHands`, `follow` |
| All Activities / Alle Aktivitäten | All 20 original cards, in original catalogue order, including `mixed` |

The six categories contain 19 distinct entries; Mixed Adventure is the twentieth,
not Today's Adventure. Colours does not promise Shapes. Learn Verbs (`verbExplorer`)
is distinct from Animal Actions (`verbs`). The Days & Seasons preview explicitly
retains Wilma, Seasons, Months and previous-version access for eventual integration.
No nested legacy/native destination is deleted or remapped. The prototype does not
execute those destinations, so it cannot establish native/legacy learning parity.

## Artwork inventory

All paths below are under `app/src/main/`. Images use Fit; original compositions and
canonical character identities are unchanged. Decorative images are excluded from
TalkBack focus because the surrounding label names the control.

| Use | Reused artwork | Limit / future requirement |
|---|---|---|
| Welcome | `assets/Animals/canonical/dragon.png` | Existing friendly dragon; no new pose or animation |
| Maths | `assets/Animals/canonical/dinosaur.png` beside native groups of one, two and three outlined blocks | Temporary counting composition, labelled in the prototype; distinct quantities do not depend on colour or reading and do not describe the whole maths curriculum |
| Words | `assets/Animals/canonical/dragon.png` with a separate native speech bubble | Single canonical character replaces the dense classroom thumbnail; unchanged bitmap, no new pose, speech or learning content |
| Time | `res/drawable-nodpi/seasons_center.png` | Full four-season composition; no new wheel, animation or labels baked into art |
| Animals | `assets/Animals/canonical/whale.png` | Familiar animal matches existing animal curriculum; inspected habitat art remains available for a later reviewed treatment |
| Colours | `assets/SceneDescriptions/classroom_school/canonical/classroom_006_art_sorting.png` | Recognisable paint pots/rainbows; full scene is busier than a dedicated coloured-object illustration |
| Everyday | `assets/SceneDescriptions/classroom_school/canonical/classroom_007_school_routines.png` | Real practical classroom routines, not handwashing. Dedicated handwashing art remains missing; the Wash Hands detail explicitly labels this missing illustration rather than showing an unrelated scene |

The small sampled asset loader is debug-only and sized for the largest 240dp preview
use; decoding is remembered per image/context, not repeated on every recomposition.
No independent asset registry is introduced into production.

Phase 2B inspected classroom Tasks, School Routines, Helping/Cleanup and Art Sorting,
and searched existing asset/metadata names for handwashing and practical routines.
No dedicated single-action handwashing illustration was found. The existing routine
scene stays temporary; do not crop several children into a misleading instruction.
A future dedicated Everyday asset should show one child washing hands at a sink,
with hands/water/soap clearly visible at 64–96dp, no embedded text, and the existing
friendly art style. A separate simple listening/action illustration would help Follow
the Instructions. Neither asset is generated in this task. Colours retains Fit and
its original composition: paint pots and rainbows remain recognisable, and no
decorative crop has been approved or applied.

## Review interaction and state

Home → Category → Activity preview, or Home → All Activities → Activity preview.
Every visible button does something within the prototype. There is no fake Start
button, locked level, score, reward, session or automatic speech. Activity selection
shows the existing title/description plus an explicit no-session-started message.

Visible Back and Android Back pop the same bounded local route stack. Home returns
to prototype Home. A saveable state holder retains each page's scroll position;
route stack and preview language survive Activity recreation. These are transient
Android saved-instance values, not production journals/preferences/progress. An
independent launch defaults to its supplied language extra. Back at prototype Home
uses normal Activity exit. The grown-up entry offers only a clearly labelled preview
language choice; real audio, sessions and settings are not accessed or changed.

Colours demonstrates its single activity and explains that eventual production
integration should open Colour Sort directly, without an unnecessary extra chooser.
This task deliberately does not implement that production dispatch.

## Adaptive and accessibility contract

The page uses available width, a bounded 1040dp inner reading/grid area and system
insets. Category columns are 1–3 according to width and font scale, not device names.
At larger text they reduce before labels are squeezed; no font shrinking, fixed text
height, ellipsis or horizontal scroll is used. The welcome illustration is smaller
when it cannot sit comfortably beside the invitation. Phase 2B places the labelled
Grown-ups / Erwachsene action within the Home header. At enlarged text the dragon
shares that header row, leaving the title and invitation their full reading width;
no separate decorative dragon row or full-width settings button adds height.
Below 480dp available height,
a compact dragon/brand/settings header brings the first category row into view;
the full-size invitation follows the categories. Short-window cards put a fitted
64dp illustration beside the full-size label in at most two columns, using the same
Material Surface, tokens and Button semantics. This small debug-only composition
avoids changing Phase 1's normal vertical card or hiding labels below tall artwork.
Equal-height cards within each row
accommodate differently wrapped German labels without staggered card bottoms.
Category artwork is decorative and fitted; activity titles/descriptions remain
full-width wrapping text.

Back/Home remain outside the scrolling content on nested pages. Material components
provide focus/press feedback and keyboard activation. Cards expose Button semantics;
preview language controls expose selected state. Touch targets are at least 56dp.
No decorative motion or new speech is added. Physical TalkBack/switch usability and
child recognition of category imagery require separate owner/device review.

Category introductions combine fitted 80dp artwork and the heading where width
permits, or a 64dp image above naturally wrapping text at enlarged/narrow widths.
The preview invitation uses the existing parent-information role; activity labels,
descriptions and card semantics are unchanged. The temporary Maths-art note follows
the choices instead of delaying the first activity. Back/Home are compact labelled
text actions at opposite ends of one header, not a second production navigation bar.

## Launch and validation

```sh
adb shell am start -n com.bellfamily.bastischool/.ui.homeprototype.HomePrototypeActivity --es language en
```

Use `de` for German. Navigate with real controls; no production session is launched.
JVM tests live in `src/testDebug` because the model deliberately does not exist in
release builds. Android coverage includes the 24 EN/DE × 1/1.5/2 font × phone/short/
tablet portrait/tablet landscape constrained Compose cases, all activity preview
routes, scroll retention, language and actual Activity recreation. These fixtures
are distinct from launched-Activity screenshots and physical-device acceptance.

Historical Phase 2 validation (before 2B): focused JVM **8/8**, full JVM **549/549**, relevant API 35
instrumentation **55/55**, all with zero failures/skips. Debug build, Android-test
compilation/APK build, lint and diff checks passed using JDK 17.0.20.1. Lint retains
five existing warnings and two informational findings, with zero errors.
All **17** final emulator screenshots were captured and inspected. The short-window
layout correction, earlier failed runs, evidence links and limitations are recorded
in SESSION_HANDOFF.md and `/private/tmp/basti-ux-phase2-review.f2r4tO/REVIEW.md`;
the gallery is `index.html` beside that report. Physical acceptance is not claimed.
UX-ANR-01 remains separate; no legacy WebView code or diagnostic is changed.

Phase 2B evidence is separate: `/private/tmp/basti-ux-phase2b-review.Iw6FpN/`.
`index.html` pairs the preserved Phase 2 captures with the new screenshots; `REVIEW.md`
records current validation and limitations. The existing nine-file change boundary
is retained. Phase 2B modifies only art/screen presentation, their JVM/instrumentation
tests, this document and the handoff; host, manifest and navigation model are preserved.

Phase 2B automated validation: focused JVM **9/9**, full JVM **550/550**, relevant
API 35 instrumentation **56/56** (27 prototype, 16 production Home, 13 foundation),
zero failures/errors/skips. Debug build, Android-test compilation/APK build, lint and
diff/new-file whitespace checks pass; warnings remain unchanged. The new header
checks caught a compact Text/paragraph width mismatch, corrected with intrinsic
button width and Text filling the allocated label width. The new host keyboard test
now enters keyboard mode via Android Tab before requesting focus. Assertions were
preserved; the earlier failures and isolated rechecks are recorded in the review.
Physical-device acceptance remains **NOT RUN — deferred by owner**.

All **19** final-code emulator screenshots were captured and visually inspected:
17 before/after pairs plus German 2× Words and settings. No overlapping/horizontally
clipped text was observed. Scrolling remains necessary at large text/short heights.
The German preview settings notice still breaks a long compound word at 2×; shorter
parent-facing copy is optional later polish, not lost text or a learning defect.
The first English Home capture shows keyboard-focus shading on Grown-ups.
The owner has reviewed and approved this isolated prototype for commit. The dated
finalisation section in SESSION_HANDOFF.md records final checks and release status;
the historical validation above does not by itself establish a new CI result.

## Changed-file inventory

- `app/src/debug/AndroidManifest.xml`: additive prototype Activity declaration.
- `app/src/debug/java/com/bellfamily/bastischool/ui/homeprototype/HomePrototypeActivity.kt`: isolated host.
- `app/src/debug/java/com/bellfamily/bastischool/ui/homeprototype/HomePrototypeModel.kt`: read-only catalogue mapping, local navigation and adaptive column rule.
- `app/src/debug/java/com/bellfamily/bastischool/ui/homeprototype/HomePrototypeArt.kt`: references/sampling and temporary counting sketch.
- `app/src/debug/java/com/bellfamily/bastischool/ui/homeprototype/HomePrototypeScreen.kt`: welcome, six category presentations, All Activities, activity previews and preview settings.
- `app/src/testDebug/java/com/bellfamily/bastischool/ui/homeprototype/HomePrototypeModelTest.kt`: mapping/navigation/content-reference tests.
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/homeprototype/HomePrototypeTest.kt`: responsive/interaction/restoration coverage.
- `docs/UX_HOME_PROTOTYPE.md`: scope, mapping, assets and review contract.
- `SESSION_HANDOFF.md`: validation and continuation evidence.

## Next slice — not authorised here

Prepare a bounded production-integration task with explicit mapping for all 20
existing entries and route/Options/Back/session compatibility tests before switching
Home. No integration is implemented here. Dedicated Everyday artwork and the German
2× preview-settings compound-word wrapping remain separate follow-up items; owner
visual approval does not close physical/child or actual TalkBack acceptance.
