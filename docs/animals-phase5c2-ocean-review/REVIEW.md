# Phase 5C.2 — Ocean Explore Foundation review

**Status: READY TO COMMIT for this development milestone.** Automated and emulator visual acceptance is complete. Physical-device, assistive-technology and child-use acceptance remain owner-managed. Changes are uncommitted and unpushed.

## Baseline and scope

- Branch: `main`; starting HEAD: `745e14c6b9e7c102a67ca977e0291d06904fe173`, matching `origin/main` at task start. Local HEAD remains that SHA; no commit or push occurred.
- The working tree already contained uncommitted Phase 5C.1 audit changes: modified roadmap, handoff and Animals audit documents plus seven new audit documents. Those are preserved; their later status/link changes are identified in the inventory below.
- Phase numbering is now **5C — Animals & Habitats**. Historical 5B references remain historical.
- Implemented Ocean only. Woodland, Freshwater, Underground and scored habitat matching remain deferred.

## Implemented experience

The route is illustrated Home → Animals & Nature → Explore Habitats → Ocean. Ocean is the only active habitat. Back returns through the chooser to the originating Animals & Nature category; Home, Options, language switching and Sound Off follow the existing shell conventions. Animal Groups and Animal Actions remain listed and functional.

The approved scene is `SceneDescriptions/ocean_underwater/canonical/ocean_001_reef_actions.png`, the existing 1448×1086 RGB 4:3 composite, displayed with `ContentScale.Fit`. Its dolphin is upper-left, jumping with a splash. The pack metadata identifies the depicted Dolphin target and permits scene use. The hotspot is anchored to the visible dolphin/splash region; a large localized Dolphin button is a labeled alternative. No separate dolphin art is overlaid, avoiding duplication.

Content uses habitat ID `habitat.ocean` and existing canonical animal ID `animal.dolphin`. The reviewed bilingual fact is **“Dolphins live in the sea.” / “Delfine leben im Meer.”** It remains broad and does not claim a specific species. Selection shows localized name and fact, a non-colour dark outline, and Replay. The shared offline speech controller speaks name plus fact only after explicit selection; Replay repeats it. Entry and Options return are silent. Sound Off is respected.

Ocean Explore is unscored. Its ViewModel has no journal, attempt/completion events, mastery evidence or Progress Tracker access. The selected Dolphin survives Activity/configuration recreation through the Activity-owned ViewModel; it is not stored across OS process death, which was not tested. No migration is required or assumed.

The original Animal Groups sorting behavior, activity/revision/content identity, four-item Whale/Dolphin/Horse/Rabbit Water/Land pool, AGR1 journal and progress identity were not changed. Regression tests check this contract.

## Screenshots and visual findings

The [HTML gallery](gallery.html) contains **13 genuine UiAutomation screenshots**, captured from disposable API 35 `emulator-5554` (`Rainbow_R3B_API35`). The regular portrait emulator is 1080×2400; phone landscape override 2400×1080; short landscape 1280×720; tablet-size emulator overrides 1600×2560 and 2560×1600. German 1.5× and 2× screenshots assert the runtime font scale. Tablet overrides are emulator viewports, not physical-tablet testing.

Portrait captures show the complete scrollable flow: prompt, fitted scene, Dolphin control, selected fact/replay, and navigation. The image keeps the dolphin large and recognizable. The target outline matches its depicted upper-left position. The selected card adds visible name and fact, so selection is not communicated through colour alone. No horizontal overflow was observed.

At short landscape, the initial viewport shows the Ocean task prompt and the beginning of the scene; the whole scene and lower controls require vertical scrolling. The phone landscape and tablet-landscape gallery captures are genuine selected-state frames scrolled into the scene: their title/prompt or lower controls are outside the current viewport, while the fixed Back/Options bar remains visible. Captions state this explicitly. German at 2× keeps the prompt and selected fact readable; lower controls remain reachable by scrolling. German at 1.5× was also visually inspected. The test suite asserted scene and prompt semantics across each viewport; the gallery does not claim every control is simultaneously visible.

## Accessibility and interaction

- Dolphin hotspot and labeled alternative expose localized button/name and selected/not-selected semantics, including a localized state description.
- The chooser, prompt, name control, Replay, Back, Home and Options are labeled native controls. The app's large action controls and vertical scroll remain available.
- Focused instrumentation covers route entry, scene decode/render, Dolphin selection, fact/name, Replay availability, German switching, Sound Off, Options return, Activity recreation, chooser/category/Home navigation and viewport overrides. ViewModel tests check the expected bilingual utterance and silent route return.
- Keyboard traversal was exercised in the shared Home route/touch coverage. Compose semantics and emulator screenshots do not establish actual TalkBack, switch-access, physical-device voice quality or child-use acceptance; those remain owner-managed.

## Validation evidence

- Focused Ocean/content and related Home/Shell/Animal Groups JVM checks passed. Fresh full JVM suite: **584/584**, zero failures, errors or skips.
- Focused Ocean API 35 instrumentation: **5/5**. Final route/language/options/recreation/viewport capture passed **1/1**. Animals & Nature route regression passed **2/2**; Home touch/keyboard regression passed **2/2**.
- Fresh full API 35 instrumentation: **591/591**, zero failures, errors or skips; `BUILD SUCCESSFUL` in 38m36s on disposable `emulator-5554`, API 35 AVD `Rainbow_R3B_API35`. Preserved XML: [full API 35 results](api35-full-android-test.xml), with 2302.094 seconds summed testcase time. `emulator-5556` and physical devices were not used. No production source changed after that full run; subsequent changes were test-only capture setup and documentation. The updated focused capture test passed independently.
- `assembleDebug`, `assembleDebugAndroidTest` and `lintDebug` passed. Lint reported 0 errors, 5 warnings and 2 informational findings, none on changed files.
- `git diff --check` passed after the final edits.

Early failures remain part of the record and are not counted as passes. The first full JVM attempt reached 584 tests and found two stale Home prototype catalogue-count expectations; after updating those expectations the rerun passed 584/584. The first full API 35 run found four stale Home assertions by 47/591 (two EN/DE expectations for return destination, two EN/DE clickable-card counts); focused regressions passed after the expectation changes and the full rerun passed 591/591. Earlier capture harness attempts exposed asynchronous art/capture timing and viewport-scroll issues; helper changes were limited to test code and the final route test passed. No production assertion was weakened.

## Exact changed-file inventory

**Phase 5C.2 production:**

- `app/src/main/java/com/bellfamily/bastischool/MainActivity.kt`
- `app/src/main/java/com/bellfamily/bastischool/ShellNavigation.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/home/HomeBrowseScreen.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/home/HomeCatalogue.kt`
- New `app/src/main/java/com/bellfamily/bastischool/learning/animals/HabitatExploreContent.kt`
- New `app/src/main/java/com/bellfamily/bastischool/ui/habitats/HabitatExploreScreens.kt`, `OceanArtworkLoader.kt`, `OceanExploreViewModel.kt`

**Phase 5C.2 tests:**

- `app/src/test/java/com/bellfamily/bastischool/HomeCardsTest.kt`
- `app/src/test/java/com/bellfamily/bastischool/HomeIntegrationTest.kt`
- `app/src/testDebug/java/com/bellfamily/bastischool/ui/homeprototype/HomePrototypeModelTest.kt`
- New `app/src/test/java/com/bellfamily/bastischool/learning/animals/HabitatExploreContentTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/HomeRouteTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/HomeScreenTest.kt`
- New `app/src/androidTest/java/com/bellfamily/bastischool/ui/habitats/HabitatExploreScreensTest.kt`, `OceanExploreOwnerTest.kt`, `OceanExploreRouteTest.kt`

**Phase 5C.2 review artifacts:** `docs/animals-phase5c2-ocean-review/REVIEW.md`, `gallery.html`, `api35-full-android-test.xml`, and 13 PNGs in `screenshots/animals-phase5c2/`.

**Documentation updated for Phase 5C.2 / phase-number reconciliation:** `MASTER_PRODUCT_LEARNING_ROADMAP.md`, `SESSION_HANDOFF.md`, `TESTING_QA_SPEC.md`, `UX_NAVIGATION_SPEC.md`, `docs/animals-phase5c1-audit/IMPLEMENTATION_PLAN.md`.

**Pre-existing uncommitted Phase 5C.1 audit preserved:** `docs/curriculum-phase5-audit/ANIMALS_EXPANSION.md`; `docs/animals-phase5c1-audit/ANIMAL_INVENTORY.md`, `ARTWORK_GAPS.md`, `HABITAT_FACTS.md`, `IMPLEMENTATION_PLAN.md`, `INTERACTION_PLAN.md`, `SCENE_DESIGNS.md`, `SUMMARY.md`; and pre-existing edits in `MASTER_PRODUCT_LEARNING_ROADMAP.md` and `SESSION_HANDOFF.md`. No unrelated work was reset, stashed, staged or overwritten.

## Remaining owner-managed acceptance

Physical phone/tablet rendering, actual TalkBack and switch access, audible local voice quality, and child-use acceptance remain. OS process-death recovery is not implemented or claimed. No blocking automated or emulator visual defect remains for this Ocean-only milestone.

**Recommendation: READY TO COMMIT.** All changes remain uncommitted and unpushed; this recommendation is not commit/push authorization.
