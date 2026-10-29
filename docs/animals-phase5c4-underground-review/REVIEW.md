# Phase 5C.4 — Underground Explore implementation review

**Status:** final development acceptance complete; **READY TO COMMIT** for owner review. The complete API 35 suite now passes 599/599 on this implementation; the earlier 595/595 remains historical only. Generated-art provenance and the owner-managed release checks are documented below. No commit or push was made.

## Starting state

- Repository: `paulyglocke/Basti-Learning-Adventure-Public`, branch `main`.
- Local HEAD and `origin/main`: `f07c3959e93bbbc6c21017100c4090e1c82bf72d`.
- The worktree already contained the uncommitted Phase 5C.4A roadmap/handoff edits and six-file design package. Those were preserved and continued, not reset, cleaned or stashed.
- Existing Ocean and Woodland Explore implementations were left in place. Animal Groups' four-animal Water/Land exercise and AGR1 session/progress identity were not changed.
- All emulator interaction used API 35 AVD `Rainbow_R3B_API35(AVD) - 15` on explicit serial `emulator-5554`. `emulator-5556` and physical devices were not targeted. Emulator size, density and font scale were restored to 1080×2400, 420 dpi and 1× after capture.

## Implementation

### Art and scene

The screen uses a hybrid 4:3 composition:

- Background: `app/src/main/assets/SceneDescriptions/underground/canonical/underground_001_cutaway.png`, opaque RGB PNG, 1448×1086. One generated illustration connects a meadow/woodland surface, grass line, tree roots, visible entrance, broad tunnels and chambers. It has no embedded selectable animals or text.
- Mole: reused `app/src/main/assets/Animals/canonical/mole.png` (existing transparent Wave 3 asset).
- Wild Rabbit: reused `app/src/main/assets/Animals/canonical/rabbit.png` (existing transparent canonical asset).
- Earthworm: new `app/src/main/assets/Animals/canonical/earthworm.png`, transparent RGBA PNG, 1774×887, generated for this scene. Generation/provenance is recorded beside the asset; no external artwork was downloaded.

The base scene and independently layered PNGs are loaded from app assets. Compose `ContentScale.Fit` retains the 4:3 artwork proportion. Hotspots use normalized source-scene coordinates, so letterboxing/viewport resizing does not make them screen-relative. The visually inspected portrait tap points were Mole (0.24, 0.77), Earthworm (0.545, 0.47) and Rabbit (0.815, 0.69). The instrumentation test checks the rendered hotspot rectangles against the content coordinates to a 2.5% tolerance. The Earthworm target was enlarged from 13% to 25% of scene height after visual review; at the baseline phone viewport this is approximately 61 dp tall, isolated horizontally from the Mole and Rabbit targets. All three have large labeled selection buttons as an alternative.

### Content and audio

Habitat ID: `habitat.underground`. Animal IDs: existing `animal.rabbit`, and new habitat-scoped `animal.mole` and `animal.earthworm` (the application does not yet have one universal canonical animal registry).

| Animal | English name and reviewed fact | German name and reviewed fact |
|---|---|---|
| `animal.mole` | Mole — “A mole lives mostly underground and digs tunnels.” | Maulwurf — “Ein Maulwurf lebt überwiegend unter der Erde und gräbt Gänge.” |
| `animal.rabbit` | Wild Rabbit — “Wild rabbits can use underground warrens for shelter.” | Wildkaninchen — “Wildkaninchen können unterirdische Baue als Schutz nutzen.” |
| `animal.earthworm` | Earthworm — “Earthworms move through soil.” | Regenwurm — “Regenwürmer bewegen sich durch den Boden.” |

The copy follows the reviewed distinctions in the [Phase 5C.1 habitat fact catalogue](../animals-phase5c1-audit/HABITAT_FACTS.md) and [Phase 5C.4A animal facts](../animals-phase5c4-underground-design/ANIMAL_FACTS.md): Mole lives mostly underground, rabbit uses a warren as shelter, and earthworm moves through soil. No exclusivity claim is made for Rabbit. English/German labels, facts and utterances use stable content IDs. Selection is manual and repeatable; replay speaks the current selection. Entry is silent. Sound Off creates no speech engine, and language switching preserves the selected animal. Existing offline speech infrastructure is reused; emulator tests verify requests and Sound Off logic but do not establish audible voice quality.

### Navigation, state and progress

The active route is **Animals & Nature → Explore Habitats → Underground / Unter der Erde**. Ocean and Woodland remain active. Back returns to the habitat chooser; shell Back returns to the Animals & Nature category; Home and Options return follow existing shell behavior. The three animals each offer an image target and large named button. A selected dark outline, `selected` semantic state and bilingual fact card provide cues beyond color.

`UndergroundExploreViewModel` owns only in-memory selection and decoded scene art. It is not connected to a journal, assessment, attempt/completion event or Progress Tracker. `ActivityScenario.recreate()` evidence shows the selected animal and language-rendered fact appear again from the retained Activity ViewModel. This is **Activity recreation**, not OS process-death recovery. Process-death durability is not claimed or required for this unscored Explore screen.

## Visual and responsive review

The [genuine emulator screenshot gallery](gallery.html) contains 14 uncomposited screenshots pulled from the running application. Pixel dimensions are verified in the PNGs and gallery captions:

| Viewport | Pixel dimensions | Scale/configuration |
|---|---:|---|
| Baseline phone portrait | 1080×2400 | 420 dpi, font 1× |
| Phone landscape | 2400×1080 | Emulator viewport override, font 1× |
| Short landscape | 1280×720 | Emulator viewport override, font 1× |
| Tablet portrait | 1600×2560 | Emulator viewport override, font 1× |
| Tablet landscape | 2560×1600 | Emulator viewport override, font 1× |
| Enlarged German | 1080×2400 | font 1.5× and 2× |

Tablet-size overrides are emulator viewport checks, not physical tablet tests. Phone portrait is the default AVD viewport. Landscape captures have landscape pixel dimensions.

The illustrations keep all three animals visible and recognizable in portrait; the roots, entrance and chamber connect visually rather than reading as separate diagrams. The selected outline encloses the selected target. English and German facts are not stale when another animal is selected. Text and controls wrap/scroll rather than being clipped by the page. At 1.5× and 2× German, names, facts and Replay remain large and reachable. The 2× capture is scrolled to the selected fact; the title/instruction is above it in the same scrollable screen.

Phone landscape keeps the title and instruction and fits the cutaway proportionally; lower content can be reached by scrolling. In short landscape, the first viewport shows the title and complete instruction but only the top of the scene. The child must scroll to reach the animal targets and their names. This is the main responsive limitation observed; the content is scrollable and controls remain reachable, but a future compact landscape composition could reduce this scrolling. Tablet emulator views center the fitted 4:3 scene and do not stretch it.

## Accessibility observations

- Animal hotspot semantics expose Button role, bilingual animal name, selected/not-selected state, and an instruction to tap to hear its name and a fact.
- Labeled animal alternatives expose selection and state-description semantics; Replay, Back, Home, Options and chooser controls are semantic native buttons.
- The image itself has no duplicated reading description; each actionable animal target carries the accessible description.
- Baseline-phone touch target frames are image-relative and large; the Earthworm frame was increased after inspecting its initial capture. The named controls remain available when image tapping is difficult, including short landscape.
- Test coverage checks tags, labels, selection semantics, replay presence, target alignment, navigation and font-scale rendering. Keyboard focus traversal was not manually reviewed for every new screen. Actual TalkBack, switch access, physical-device touch behavior and child-use usability remain owner-managed.

## Implementation-checkpoint validation (before Phase 5C.4B)

- Fresh full JVM suite: **591/591 passed**, 0 failures, 0 errors, 0 skipped (`testDebugUnitTest`).
- Focused API 35 instrumentation: **21/21 passed**, 0 failures, 0 skips on `emulator-5554`. Classes covered `HabitatExploreScreensTest`, `UndergroundExploreOwnerTest`, `UndergroundExploreRouteTest`, `OceanExploreRouteTest`, `WoodlandExploreRouteTest`, `AnimalGroupsRouteTest`, `HomeRouteTest` and `HomeProductionNavigationTest`. The route test covers chooser, three image taps and exact target geometry, English/German selection, replay, Sound Off, Options return, Activity recreation, 1×/1.5×/2× text and four viewport sizes.
- During test iteration, the responsive route test first exposed a detached Compose scroll owner after viewport recreation; after that test-only issue was removed, it exposed an incorrect expectation for a shell-back control on the Home category. Assertions now follow the actual scroll and Home navigation contracts. No production behavior change was made for either test issue.
- `assembleDebug`, `assembleDebugAndroidTest`, and `lintDebug`: passed. Lint: 0 errors, 5 existing warnings (manifest API-level attributes, two oversized vector assets, and JavaScript-enabled WebView warning).
- `git diff --check`: passed after the final documentation and image target edits.
- At this implementation checkpoint, the complete API 35 suite had not been rerun; the historical **595/595** was from Phase 5C.3 and did not apply. This checkpoint record is superseded by the final 5C.4B acceptance below.

## Compatibility and limitations

- Original Animal Groups routes/content/journal/progress remain unchanged; the focused Animal Groups route regression passed. No Animal Groups content fixture or saved journal was edited.
- Ocean and Woodland regression route classes passed in the same focused API batch.
- No Freshwater, habitat matching, journal, score, progress schema or new dependency was introduced.
- English/German text has automated and visual review, but final native-speaker approval remains owner-managed.
- Art was generated for this app session using existing Mole/Rabbit style references and is locally documented. Owner review of the generated images and their suitability/licensing is still required before release.
- The final complete API 35 result is recorded below. Actual TalkBack/switch-access, physical-device acceptance, child usability and audible offline-voice quality remain unverified.
- `UX-ANR-01` remains separate.

## Exact changed-file inventory

The Phase 5C.4A worktree edits already existed when this phase began and remain included: `MASTER_PRODUCT_LEARNING_ROADMAP.md`, `SESSION_HANDOFF.md`, and these six files under `docs/animals-phase5c4-underground-design/`: `SUMMARY.md`, `ASSET_INVENTORY.md`, `SCENE_SPEC.md`, `ANIMAL_FACTS.md`, `ARTWORK_BRIEF.md`, and `IMPLEMENTATION_PLAN.md`.

New/updated implementation files:

- `app/src/main/java/com/bellfamily/bastischool/MainActivity.kt`
- `app/src/main/java/com/bellfamily/bastischool/ShellNavigation.kt`
- `app/src/main/java/com/bellfamily/bastischool/learning/animals/HabitatExploreContent.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/habitats/HabitatExploreScreens.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/habitats/UndergroundArtworkLoader.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/habitats/UndergroundExploreViewModel.kt`
- `app/src/main/assets/Animals/README-wave4.md`
- `app/src/main/assets/Animals/manifest-wave4.json`
- `app/src/main/assets/Animals/canonical/earthworm.png`
- `app/src/main/assets/SceneDescriptions/underground/README.md`
- `app/src/main/assets/SceneDescriptions/underground/canonical/underground_001_cutaway.png`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/habitats/HabitatExploreScreensTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/habitats/UndergroundExploreOwnerTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/habitats/UndergroundExploreRouteTest.kt`
- `app/src/test/java/com/bellfamily/bastischool/HomeIntegrationTest.kt`
- `app/src/test/java/com/bellfamily/bastischool/learning/animals/HabitatExploreContentTest.kt`
- `docs/animals-phase5c1-audit/IMPLEMENTATION_PLAN.md`
- `UX_NAVIGATION_SPEC.md`
- `TESTING_QA_SPEC.md`
- `docs/animals-phase5c4-underground-review/REVIEW.md`
- `docs/animals-phase5c4-underground-review/gallery.html`
- `docs/animals-phase5c4-underground-review/screenshots/animals-phase5c4/01-habitat-chooser-en.png`
- `docs/animals-phase5c4-underground-review/screenshots/animals-phase5c4/02-underground-initial-en.png`
- `docs/animals-phase5c4-underground-review/screenshots/animals-phase5c4/03-mole-selected-en.png`
- `docs/animals-phase5c4-underground-review/screenshots/animals-phase5c4/04-earthworm-selected-en.png`
- `docs/animals-phase5c4-underground-review/screenshots/animals-phase5c4/05-rabbit-selected-en.png`
- `docs/animals-phase5c4-underground-review/screenshots/animals-phase5c4/06-rabbit-selected-de-options-return.png`
- `docs/animals-phase5c4-underground-review/screenshots/animals-phase5c4/07-rabbit-restored-after-recreation-de.png`
- `docs/animals-phase5c4-underground-review/screenshots/animals-phase5c4/08-phone-landscape-de.png`
- `docs/animals-phase5c4-underground-review/screenshots/animals-phase5c4/09-short-landscape-de.png`
- `docs/animals-phase5c4-underground-review/screenshots/animals-phase5c4/10-tablet-portrait-emulator.png`
- `docs/animals-phase5c4-underground-review/screenshots/animals-phase5c4/11-tablet-landscape-emulator.png`
- `docs/animals-phase5c4-underground-review/screenshots/animals-phase5c4/12-german-1-5x.png`
- `docs/animals-phase5c4-underground-review/screenshots/animals-phase5c4/13-german-2x.png`
- `docs/animals-phase5c4-underground-review/screenshots/animals-phase5c4/14-back-to-habitat-chooser-de.png`

No commit or push was made.

## Phase 5C.4B final acceptance — 2026-10-29

### Final repository and source check

Acceptance resumed on `main` at `f07c3959e93bbbc6c21017100c4090e1c82bf72d`,
matching `origin/main`. The worktree contained the uncommitted Phase 5C.4
implementation, tests, generated assets, review screenshots and Phase 5C.4A
design documentation recorded above; none was discarded or overwritten. No
production source or asset changed during this final acceptance pass. The final
documentation and complete test reports are additional pending worktree changes.

Production inspection confirmed the route **Home → Animals & Nature → Explore
Habitats → Underground**. Ocean, Woodland and Underground are active; no new
animal or scored activity was added. The screen selects canonical IDs
`animal.mole`, `animal.rabbit` and `animal.earthworm`; entering the screen is
silent. Selection, Replay, Sound Off, Back/Home/Options and language return use
the current offline speech and shell patterns. Selection is held in the
Activity ViewModel, survives `ActivityScenario.recreate()` and Options language
return, and is not durably journaled across OS process death. No assessment
events, learning journal or Progress Tracker record are emitted.

### Visual, target and content acceptance

The cutaway and the full gallery were visually inspected. The 1448×1086 RGB
base art connects meadow, grass boundary, tree roots, surface entrance, tunnels
and chambers into one legible landscape; no animal is baked into the background.
The canonical Mole and Rabbit cutouts and transparent Earthworm PNG are
recognizable at the rendered phone size. The Earthworm is the smallest target;
its image hit region was previously enlarged to 25% of the scene height, its
selected outline surrounds the visible worm, and the 61 dp-or-larger labeled
alternative provides a reliable option when direct tapping is difficult. No
additional hotspot defect was observed. The tested image-relative points remain
Mole `(0.24, 0.77)`, Earthworm `(0.545, 0.47)` and Rabbit `(0.815, 0.69)` on the
4:3 source scene. Portrait, landscape and tablet-emulator viewport tests verify
the `ContentScale.Fit` letterboxing and target rectangles. Empty scene regions
do not select an animal.

The artwork brief requested a 2048×1536 master; the delivered cutaway is
1448×1086. It retains the exact 4:3 composition and downscales cleanly in the
reviewed app layouts, so no raster softness or cropping defect was observed.
It does not provide the brief's requested 2× master-resolution headroom; retain
that as an art-owner preference before release rather than upscaling this PNG.

The selected states use a dark outline and fact card as well as selected
semantics, so state does not depend on color. Bilingual production strings match
the reviewed Phase 5C.1/5C.4A relationships:

| Animal | English | German | Review |
|---|---|---|---|
| Mole / Maulwurf | “A mole lives mostly underground and digs tunnels.” | “Ein Maulwurf lebt überwiegend unter der Erde und gräbt Gänge.” | “Mostly” avoids an exclusive-lifetime claim. |
| Wild Rabbit / Wildkaninchen | “Wild rabbits can use underground warrens for shelter.” | “Wildkaninchen können unterirdische Baue als Schutz nutzen.” | Explicitly describes wild rabbits and possible shelter use. |
| Earthworm / Regenwurm | “Earthworms move through soil.” | “Regenwürmer bewegen sich durch den Boden.” | Describes movement through soil, not a universal depth or habitat claim. |

For all animals, selection updates the label, fact and spoken utterance together;
Replay targets the current animal. Language switching keeps the semantic animal
selection, and Sound Off suppresses speech. Emulator tests validate speech
requests and mute behavior, not the installed offline voice's audible quality.
No new issue was found with Ocean, Woodland or the original Animal Groups route.
The complete app suite below also ran the existing historical compatibility
tests, including the Animal Groups Water/Land path and storage-preservation
checks. The fixed Whale/Dolphin/Horse/Rabbit pool and AGR1 identity remain
unchanged.

### Responsive and accessibility observations

The earlier 14 genuine screenshots are preserved under
`screenshots/animals-phase5c4/`. The gallery now displays the 14 fresh captures
produced by the complete API 35 route test under `screenshots/phase5c4b/`;
their PNG dimensions were checked individually. The runner's raw copied outputs
are also preserved under `screenshots/animals-phase5c4/animals-phase5c4/` and
were verified byte-identical to the curated final set. Review of both sets exposed
that the earlier Earthworm image looked smaller than the image rendered by the
final full-suite route. The new captures show the enlarged target and current
presentation; the earlier images were retained rather than overwritten. This
evidence update required no production-code change. There are 28 distinct
genuine screenshots retained (42 PNG files including the byte-identical raw
copies); the displayed final-source set covers 1080×2400 portrait,
2400×1080 landscape, 1280×720 short landscape, and tablet-size emulator
overrides 1600×2560 and 2560×1600. German is shown at 1×, 1.5× and 2×. These
tablet captures are emulator display overrides, not physical tablet tests.

The whole screen remains scrollable. In short landscape the complete task
instruction appears first, but children must scroll to reach the scene targets
and names. At German 1.5× and 2×, facts, labels and Replay remain visible and
reachable by scrolling; the 2× image is intentionally a scrolled view. This is
a documented layout compromise, not a clipped or inaccessible control. Larger
label buttons, accessible names, button roles, selected semantics and non-color
outlines are present. Keyboard focus order was not manually reviewed. Actual
TalkBack, switch access, child comprehension, physical touch behavior and
physical-device visual acceptance remain owner-managed.

### Artwork provenance and reuse

The base cutaway and Earthworm art were created with OpenAI image generation
for this repository task, using existing in-repository Mole/Rabbit artwork as
style references; no external image was downloaded or knowingly incorporated.
The source files are the cutaway at
`app/src/main/assets/SceneDescriptions/underground/canonical/underground_001_cutaway.png`
and Earthworm at `app/src/main/assets/Animals/canonical/earthworm.png`.
Asset-side READMEs preserve the available creation workflow, dimensions and
paraphrased prompt summary. The exact prompt and editable layered source were not
exported. Current [OpenAI Terms of Use](https://openai.com/policies/terms-of-use/)
state that, between the user and OpenAI and to the extent permitted by law, the
user owns Output and OpenAI assigns its rights, if any. The same terms say output
may not be unique and require the user to have rights to provided inputs. This
supports the recorded provenance, but is not a third-party originality search,
a warranty of non-infringement, or a blanket commercial-license certification.
No known third-party source asset remains unresolved. The project owner should
still confirm that the referenced in-repository art was appropriate input and
approve these generated assets for the app before public release; do not record
that approval as already granted.

### Final automated evidence

- Complete unfiltered API 35 instrumentation: **599/599 passed**, 0 failures,
  0 errors, 0 skipped, on disposable AVD `Rainbow_R3B_API35(AVD) - 15`, explicit
  serial `emulator-5554`, 4 virtual CPUs and 6 GB RAM. Gradle reported
  `BUILD SUCCESSFUL` in **46m 51s**. The XML enumerates 599 testcases and the UTP
  textproto records `scheduled_test_case_count: 599` and `test_status: PASSED`.
  Exact artifacts are retained as [`api35-full-results-2026-10-29.xml`](api35-full-results-2026-10-29.xml)
  and [`api35-full-results-2026-10-29.textproto`](api35-full-results-2026-10-29.textproto).
- Forced fresh full JVM rerun: **591/591 passed**, 0 failures, errors or skips
  (`./gradlew testDebugUnitTest --rerun-tasks`). This includes the focused
  content/reducer and existing-activity tests.
- Forced fresh `assembleDebug`, `assembleDebugAndroidTest` and `lintDebug`:
  **BUILD SUCCESSFUL**, all 71 tasks executed. Lint has 0 errors and 5 existing
  warnings (two manifest attributes restricted by minimum API, two oversized
  vectors and the legacy JavaScript-enabled WebView). Kotlin also reports the
  existing deprecated `VIBRATOR_SERVICE` warning at MainActivity.kt:1058.
- `git diff --check`: passed after the final documentation updates.

The emulator was checked after instrumentation and restored to 1080×2400,
420 dpi and font scale 1×. `emulator-5556` and physical devices were not used.
The API 35 run executed after the final production changes and applies to the
current pending source. The earlier 595/595 result remains historical only.

### Final decision

**READY TO COMMIT for owner review.** No blocking implementation, visual,
hotspot, bilingual-content, restoration, regression or accessibility-semantics
defect was demonstrated. Before release, the owner must still approve generated
art/input provenance for public app use and complete the owner-managed checks
listed above. These are not represented as completed by this development
acceptance. All changes remain uncommitted and unpushed.

### Phase 5C.4B additional changed files

- `MASTER_PRODUCT_LEARNING_ROADMAP.md`
- `SESSION_HANDOFF.md`
- `TESTING_QA_SPEC.md`
- `docs/animals-phase5c4-underground-review/REVIEW.md`
- `docs/animals-phase5c4-underground-review/api35-full-results-2026-10-29.xml`
- `docs/animals-phase5c4-underground-review/api35-full-results-2026-10-29.textproto`
- `docs/animals-phase5c4-underground-review/screenshots/phase5c4b/01-habitat-chooser-en-final-suite.png`
- `docs/animals-phase5c4-underground-review/screenshots/phase5c4b/02-underground-initial-en-final-suite.png`
- `docs/animals-phase5c4-underground-review/screenshots/phase5c4b/03-mole-selected-en-final-suite.png`
- `docs/animals-phase5c4-underground-review/screenshots/phase5c4b/04-earthworm-selected-en-final-suite.png`
- `docs/animals-phase5c4-underground-review/screenshots/phase5c4b/05-rabbit-selected-en-final-suite.png`
- `docs/animals-phase5c4-underground-review/screenshots/phase5c4b/06-rabbit-selected-de-options-return-final-suite.png`
- `docs/animals-phase5c4-underground-review/screenshots/phase5c4b/07-rabbit-restored-after-recreation-de-final-suite.png`
- `docs/animals-phase5c4-underground-review/screenshots/phase5c4b/08-phone-landscape-de-final-suite.png`
- `docs/animals-phase5c4-underground-review/screenshots/phase5c4b/09-short-landscape-de-final-suite.png`
- `docs/animals-phase5c4-underground-review/screenshots/phase5c4b/10-tablet-portrait-emulator-final-suite.png`
- `docs/animals-phase5c4-underground-review/screenshots/phase5c4b/11-tablet-landscape-emulator-final-suite.png`
- `docs/animals-phase5c4-underground-review/screenshots/phase5c4b/12-german-1-5x-final-suite.png`
- `docs/animals-phase5c4-underground-review/screenshots/phase5c4b/13-german-2x-final-suite.png`
- `docs/animals-phase5c4-underground-review/screenshots/phase5c4b/14-back-to-habitat-chooser-de-final-suite.png`
- `docs/animals-phase5c4-underground-review/screenshots/animals-phase5c4/animals-phase5c4/` (14 byte-identical raw test screenshot copies, matching the curated 5C.4B set above)
