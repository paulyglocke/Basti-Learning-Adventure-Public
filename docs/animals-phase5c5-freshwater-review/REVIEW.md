# Phase 5C.5B — Freshwater Explore review

**Development status: READY TO COMMIT.** Final complete-suite acceptance passed. Generated-art release approval and physical/child accessibility acceptance remain separate owner-managed checks.

## Baseline and scope

Started from `main` at `3c6c8e6a2fcf57525fad0909a1c205323c41448c`, matching
`origin/main`. The pre-existing Phase 5C.5A design/roadmap edits were
uncommitted and have been preserved. This implementation adds only
Freshwater assets, the fourth unscored habitat route, focused coverage and
review/documentation updates. There are no commits or pushes.

## Artwork and provenance

The scene is `app/src/main/assets/SceneDescriptions/freshwater_pond/canonical/freshwater_001_pond.png`
(1448×1086, 4:3, RGB, opaque). It depicts a temperate pond with a clear
shoreline, reeds, lilies, submerged plants, stones and distant meadow/trees.
It contains no animals or text. The chosen resolution is the generated native
size; it was not enlarged to the brief's preferred 2048×1536.

Separate cutouts are `app/src/main/assets/Animals/canonical/frog.png` and
`app/src/main/assets/Animals/canonical/duck.png` (each 1312×1199, RGBA with
alpha). Both have clean transparent edges in the rendered screenshots. The
generic friendly Frog reads clearly as an amphibian; the warm brown/cream Duck
reads clearly as a duck and is shown floating at the surface. The Frog is at
the damp shoreline. Neither is baked into the background, and no Fish art is
used.

The built-in OpenAI image generation tool created the pond and animal source
illustrations for this task. The pond prompt described a calm central-European
pond with a visible waterline, shallow underwater plants, lilies, reeds, shore,
meadow/trees, no animals and no lettering. Frog and Duck prompts requested
separate child-friendly whole-animal cutouts, visually guided by inspected
Seasons lakeside art and the repository's canonical Rabbit illustration. No
external source image was incorporated. Initial Frog/Duck outputs had a
checker pattern baked into the RGB; those versions were rejected. The accepted
cutouts came from image-generation edits requesting a transparent background,
then were verified as RGBA PNG. No editable generation project was exported.

The local image-generation output records were named
`exec-584de998-c7e7-45e6-ab76-197969ee3078.png` (pond),
`exec-48518516-5030-48b2-bcb4-1f2fd86549ef.png` (transparent Frog edit), and
`exec-37df1dea-fcff-4737-84eb-bc12e98912a9.png` (transparent Duck edit).
These temporary source outputs live in the local Codex generated-image cache,
not in the repository. The runtime uses the imported asset paths listed above.

The available provenance record supports that the images were generated for
this repository task and used no external image source. It does not establish a
separate owner license, rights grant or commercial-use approval. Generated-art
approval remains an owner release check; no rights claim is made here. The
assets are marked and described in `Animals/README-freshwater.md` and the
Freshwater scene pack metadata.

## Content and interaction

Production copy retains the Phase 5C.5A reviewed facts exactly:

| Animal | English | German | Relationship |
|---|---|---|---|
| Frog / Frosch | “Many frogs lay eggs in water and also use damp places on land.” | “Viele Frösche legen ihre Eier im Wasser ab und nutzen auch feuchte Plätze an Land.” | Breeding in water and use of damp land places; not an exclusive aquatic claim. |
| Duck / Ente | “Many ducks swim on ponds and lakes.” | “Viele Enten schwimmen auf Teichen und Seen.” | Swimming/context statement, not an exclusive habitat claim. |

Canonical IDs are `habitat.freshwater`, `animal.frog` and `animal.duck`.
There is no Fish selection, scoring, quiz, journal, attempt/completion event,
mastery or Progress Tracker owner. The screen begins silent. Selection speaks
the selected localized name and fact using the existing offline speech
controller. Replay repeats current content; Sound Off suppresses speech.
Changing language retains the semantic selection and renders/speaks the new
language. Options return preserves the active route and selection.

The four active chooser entries remain ordered Ocean, Woodland, Underground,
Freshwater. Navigation follows Animals & Nature → Explore Habitats →
Freshwater, with Back to chooser, parent-category return, Home and Options.
Ocean, Woodland, Underground and original Animal Groups content/journals were
not changed.

Selection belongs to an Activity-scoped ViewModel. It survives ordinary
Activity recreation and Options navigation. There is no durable session
journal; OS process-death recovery is not claimed or required for this
unscored exploration.

## Scene placement and responsive review

Both cutouts and their direct-touch frames use normalized source-image
coordinates mapped into the fitted 4:3 scene, not device pixels:

| Animal | Frame `(left, top, right, bottom)` | Evidence |
|---|---|---|
| Frog | `(0.07, 0.43, 0.33, 0.75)` | Left shoreline; selected outline encloses the pictured Frog. |
| Duck | `(0.60, 0.34, 0.93, 0.74)` | Pond surface on right; outline encloses the floating Duck. |

The genuine captures linked in [gallery.html](gallery.html) were inspected.
The pond reads as freshwater rather than ocean, is uncluttered enough for the
animals to stand out, and retains its aspect ratio. Frog and Duck are
recognizable at phone scale. Hit frames align at phone portrait, phone
landscape and tablet-size emulator viewports; empty-scene taps leave selection
unchanged. Large labeled alternatives make both choices reachable even when
the image is small.

English and German are legible at 1×. German at 1.5× wraps naturally and
remains usable. At 2×, the longer content requires scrolling; the scene,
labels, fact and Replay remain reachable without overlap. Phone landscape and
short landscape also require vertical scrolling to reach lower controls. The
initial instruction explains both animal-tap and label choices; Back/Home and
Options remain in the shell header. Tablet portrait and landscape are
emulator size overrides, not physical tablet testing. Screens use standard
Compose button and selected semantics; automated semantics are covered, but
actual TalkBack, switch access and keyboard acceptance are not claimed.

There are 17 unique genuine captures: portrait states use 1080×2400 px,
phone landscape is 2400×1080, short landscape is 1280×720, tablet portrait
emulator override is 1600×2560, and tablet landscape emulator override is
2560×1600. The English initial portrait capture also documents the phone
portrait viewport; it is not duplicated solely to fill a second matrix slot.

## Automated validation

- Focused API 35: **10/10 passed**, 0 failures/errors/skips, on API 35 AVD
  `Rainbow_R3B_API35`, explicit serial `emulator-5554`. Includes Freshwater
  route/render/selection/replay/language/Sound Off/Options/recreation, chooser,
  layout captures and existing habitat screen regressions.
- Fresh full JVM with `--rerun-tasks`: **595/595 passed**, 0 failures/errors/
  skips (109 suites).
- Complete unfiltered API 35: **602/602 passed**, 0 failures/errors/skips, on
  disposable `emulator-5554` (API 35; AVD `Rainbow_R3B_API35`), Gradle runtime
  45m 53s. Full JUnit XML is preserved at
  [`evidence/api35-instrumentation.xml`](evidence/api35-instrumentation.xml).
- `assembleDebug`, `assembleDebugAndroidTest` and `lintDebug --rerun-tasks`:
  **BUILD SUCCESSFUL**. Lint: 0 errors, 5 warnings; all five warnings are in
  pre-existing manifest/vector/MainActivity code, with no Freshwater warning.
- `git diff --check`: passed after final documentation changes.
- `git diff --check`: final result to be recorded after documentation edits.

The focused run initially exposed two assertion issues in tests: an off-screen
chooser action was asserted before scrolling, and the Animals parent returns
to its activity-card page rather than rendering a category tile. Assertions
were corrected to match established navigation/scroll behavior, then the
10-test focused run passed. No production defect was indicated by those
failures.

One earlier focused invocation was interrupted after Android's connected test
task began scheduling both attached emulators without an explicit serial. The
owner-side `emulator-5556` did not complete a test; a read-only follow-up showed
it at Launcher with its original 1080×2400 size and 1× font scale and no app
process. All subsequent focused and full validation runs explicitly selected
only disposable `emulator-5554`. No device data was cleared.

## Remaining acceptance limits

Owner approval of generated art for release, physical-device and child-use
acceptance, actual TalkBack and switch-access review, keyboard review beyond
automated coverage, and audible offline voice quality remain owner-managed.
The 2× and short-landscape experiences use scrolling. `UX-ANR-01` remains
separate. No commit or push has been made.

## Changed-file inventory

Production and asset changes:

- `app/src/main/java/com/bellfamily/bastischool/MainActivity.kt`
- `app/src/main/java/com/bellfamily/bastischool/ShellNavigation.kt`
- `app/src/main/java/com/bellfamily/bastischool/learning/animals/HabitatExploreContent.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/habitats/HabitatExploreScreens.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/habitats/FreshwaterArtworkLoader.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/habitats/FreshwaterExploreViewModel.kt`
- `app/src/main/assets/Animals/manifest.json`
- `app/src/main/assets/Animals/README-freshwater.md`
- `app/src/main/assets/Animals/canonical/frog.png`
- `app/src/main/assets/Animals/canonical/duck.png`
- `app/src/main/assets/SceneDescriptions/freshwater_pond/category.json`
- `app/src/main/assets/SceneDescriptions/freshwater_pond/manifest.json`
- `app/src/main/assets/SceneDescriptions/freshwater_pond/README.md`
- `app/src/main/assets/SceneDescriptions/freshwater_pond/canonical/freshwater_001_pond.png`

Tests:

- `app/src/test/java/com/bellfamily/bastischool/ShellNavigationTest.kt`
- `app/src/test/java/com/bellfamily/bastischool/learning/animals/HabitatExploreContentTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/habitats/HabitatExploreScreensTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/habitats/FreshwaterExploreOwnerTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/habitats/FreshwaterExploreRouteTest.kt`

Project documentation:

- `MASTER_PRODUCT_LEARNING_ROADMAP.md`
- `SESSION_HANDOFF.md`
- `TESTING_QA_SPEC.md`
- `UX_NAVIGATION_SPEC.md`
- `docs/animals-phase5c5-freshwater-design/SUMMARY.md`
- `docs/animals-phase5c5-freshwater-design/ASSET_INVENTORY.md`
- `docs/animals-phase5c5-freshwater-design/ANIMAL_FACTS.md`
- `docs/animals-phase5c5-freshwater-design/SCENE_SPEC.md`
- `docs/animals-phase5c5-freshwater-design/ARTWORK_BRIEF.md`
- `docs/animals-phase5c5-freshwater-design/IMPLEMENTATION_PLAN.md`
- `docs/animals-phase5c5-freshwater-review/REVIEW.md`
- `docs/animals-phase5c5-freshwater-review/gallery.html`
- `docs/animals-phase5c5-freshwater-review/evidence/api35-instrumentation.xml`
- `docs/animals-phase5c5-freshwater-review/screenshots/animals-phase5c5/01-chooser-en.png`
- `docs/animals-phase5c5-freshwater-review/screenshots/animals-phase5c5/02-freshwater-initial-en.png`
- `docs/animals-phase5c5-freshwater-review/screenshots/animals-phase5c5/03-freshwater-initial-de.png`
- `docs/animals-phase5c5-freshwater-review/screenshots/animals-phase5c5/04-frog-selected-en.png`
- `docs/animals-phase5c5-freshwater-review/screenshots/animals-phase5c5/05-frog-selected-de.png`
- `docs/animals-phase5c5-freshwater-review/screenshots/animals-phase5c5/06-duck-selected-en.png`
- `docs/animals-phase5c5-freshwater-review/screenshots/animals-phase5c5/07-duck-selected-de.png`
- `docs/animals-phase5c5-freshwater-review/screenshots/animals-phase5c5/08-frog-to-duck-switch.png`
- `docs/animals-phase5c5-freshwater-review/screenshots/animals-phase5c5/09-selection-restored-recreation.png`
- `docs/animals-phase5c5-freshwater-review/screenshots/animals-phase5c5/10-options-language-return-de.png`
- `docs/animals-phase5c5-freshwater-review/screenshots/animals-phase5c5/12-phone-landscape.png`
- `docs/animals-phase5c5-freshwater-review/screenshots/animals-phase5c5/13-short-landscape.png`
- `docs/animals-phase5c5-freshwater-review/screenshots/animals-phase5c5/14-tablet-portrait-emulator.png`
- `docs/animals-phase5c5-freshwater-review/screenshots/animals-phase5c5/15-tablet-landscape-emulator.png`
- `docs/animals-phase5c5-freshwater-review/screenshots/animals-phase5c5/16-german-1_5x.png`
- `docs/animals-phase5c5-freshwater-review/screenshots/animals-phase5c5/17-german-2x.png`
- `docs/animals-phase5c5-freshwater-review/screenshots/animals-phase5c5/18-back-to-chooser.png`

No Kotlin changes touch Ocean, Woodland, Underground, Animal Groups, journals
or Progress Tracker schemas. All changes remain uncommitted and unpushed.
