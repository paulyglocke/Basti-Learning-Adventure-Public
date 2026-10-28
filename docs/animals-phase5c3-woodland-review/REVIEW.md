# Phase 5C.3 — Woodland Explore review

**Status:** validation complete. Changes remain uncommitted and unpushed.

## Baseline and working state

- Repository: `paulyglocke/Basti-Learning-Adventure-Public`, branch `main`.
- Starting local HEAD and `origin/main`: `2cbaec64eabe165a06852a3799cf397e47c8b350`.
- Starting tree was clean; no unrelated work was present.
- The implementation adds Woodland to the existing Phase 5C.2 Ocean Explore route and preserves the historical Animal Groups activity contract.

## Scene and content

The selected scene is the approved pack asset
`app/src/main/assets/SceneDescriptions/woodland_forest/canonical/woodland_008_homes_choices.png`.
The scene manifest identifies it as `scene.woodland.homes_choices.08`; it is a
1448×1086 RGB PNG (4:3). The loader decodes it at half resolution to 724×543
pixels and displays it with `ContentScale.Fit` in a matching 4:3 frame. No
external artwork or new animal cutouts were introduced. The repository's pack
records prompts and visual QA but no per-file SPDX/source-owner field; new
asset licensing was not assumed or changed.

Two scene targets are interactive:

| Animal ID | EN / DE | Source-image target (left, top, right, bottom) | Approximate source pixels |
|---|---|---|---|
| `animal.rabbit` | Rabbit / Kaninchen | (0.39, 0.46, 0.55, 0.69) | (565, 500)–(796, 749) |
| `animal.fox` | Fox / Fuchs | (0.73, 0.48, 0.91, 0.75) | (1057, 521)–(1318, 815) |

Targets are proportional to the source image, so they move and resize with the
fitted 4:3 scene. Review identified the first target bounds as too low and wide;
the final bounds were narrowed and shifted to better fit the depicted animals.
The route test compares each rendered hotspot's semantics bounds against the
source-image fractions with a 0.025 tolerance. Direct taps at image fractions
(.45, .67) and (.80, .67) select Rabbit and Fox; a tap at (.15, .40) leaves the
selection empty. Both animals remain available through full-width labeled
controls when image tapping is difficult. A dark outline and selected semantics
identify the current target without relying on colour. The composite also shows
an owl, squirrel and hedgehog; they remain decorative and are not presented as
selectable. The original wider-target captures are retained in
`screenshots/before-hotspot-refinement/`; their SHA-256 values differ from the
final selected-state captures and they document the visually demonstrated
correction.

Facts match the Phase 5C.1 `HABITAT_FACTS.md` wording exactly:

- Rabbit: “Wild rabbits can use underground warrens for shelter.” / “Wildkaninchen können unterirdische Baue als Schutz nutzen.”
- Fox: “Foxes live in many habitats. A fox may use an underground den to raise its cubs.” / “Füchse leben in vielen Lebensräumen. Ein Fuchs kann einen unterirdischen Bau nutzen, um seine Jungen aufzuziehen.”

The copy distinguishes habitat from shelter and keeps Fox's broader habitat
relationship non-exclusive. English/German names and facts use the same stable
animal ID across language changes.

## Navigation, audio and state

Navigation is Home → Animals & Nature → Explore Habitats → Woodland, with Ocean
and Woodland as the only active chooser entries. Scene Back returns to the
chooser; shell Back returns to the Animals & Nature category; Home returns to
illustrated Home. Options overlays and language changes restore the route and
selected animal. Animal Groups and Animal Actions entry points remain intact.

Selection speaks the current animal name followed by its short fact through the
existing offline speech controller. Replay uses the current selection; changing
animal cancels/replaces the prior utterance. Sound Off creates no speech engine
and does not alter selection. The screen does not speak on entry. Automated
audio-owner tests use a controlled speech-engine double; audible voice quality
and installed offline voices remain owner-managed.

Woodland is unscored. Its ViewModel owns only transient selected-animal UI state
and asset/audio lifecycle; it has no session journal, assessment event, attempt,
completion, mastery evidence or Progress Tracker identity. Activity recreation
restores the current selection through the Activity ViewModel. OS process-death
recovery is not claimed. No Animal Groups journal, migration, progress identity
or sorting pool was changed; its original Whale/Dolphin/Horse/Rabbit Water/Land
exercise remains covered by regression tests.

## Visual and accessibility review

The genuine screenshot gallery is [`gallery.html`](gallery.html). It links 16
curated capture states in `screenshots/animals-phase5c3/`; the folder also
retains three earlier-pass captures. Portrait captures are
1080×2400 px. Phone landscape is 2400×1080 px; short landscape is 1280×720 px.
Tablet-size emulator viewport overrides are 1600×2560 and 2560×1600 px and are
not physical tablet tests. Font-scale screenshots were taken at 1×, 1.5× and 2×.
The 21 PNG files produce 19 unique bitmaps: the Rabbit after selection and after
Activity recreation renders identically, as does the German Rabbit after direct
selection and after Options return. Those captures document distinct interaction
steps; the gallery does not use duplicate files to inflate the required states.

The selected scene is recognizable and the final selected outlines fit closely
around Rabbit and the main Fox body; image-relative semantic bounds are checked
against the rendered 4:3 container. The image is not stretched. The long-landscape
capture is a scrolled scene view, so title and prompt are above its viewport;
scrolling exposes the complete scene and controls. Short landscape shows the
task instruction in its first viewport, while the scene and lower controls are
reachable by scrolling. German instructions wrap and
the 1.5× capture retains the heading, prompt, scene and named alternatives. At
2×, text remains legible and the page is scrollable; the selected fact and Replay
are reachable lower in the page. The chooser is also scrollable, with both
habitats presented as large cards.

Compose semantics expose each image target as a labeled button with selected
state, and named selection controls expose selected/not-selected state. The
outline, animal name, fact panel, Replay, Back and Home remain visible as textual
or structural UI. Focused UI tests cover the named alternatives and touch
targets; the full instrumentation run exercises existing Home and Animal Groups
accessibility regressions. This is not actual TalkBack or switch-access
acceptance. Keyboard focus, physical-device behavior and child-use remain
owner-managed.

## Validation

- Fresh full JVM suite: **588/588 passed**, 0 failures, 0 errors, 0 skipped; XML is in `app/build/test-results/testDebugUnitTest/`.
- Focused Woodland/Ocean Compose and route tests: Woodland screen **5/5**, Woodland owner plus route **2/2**, final Woodland route including hotspot geometry **1/1**, Ocean production route **1/1**. A prior combined run had one transient Ocean route failure at a font-scale transition (`expected 2.0, got 1.0`); bounded synchronization was added and the isolated rerun passed. The first owner-test attempt had an incorrect replay assertion; it was corrected and passed.
- Focused route capture exercises the actual app navigation, image touch locations, selection replacement, bilingual facts, Replay, Sound Off, Options return, Activity recreation and layout/font-scale overrides.
- Full API 35 suite: **595/595 passed**, 0 failures, 0 errors, 0 skipped; Gradle `BUILD SUCCESSFUL` in 59m 2s on disposable `Rainbow_R3B_API35` (`emulator-5554`, four virtual CPUs / 6 GB). XML is preserved as [`api35-full-android-test.xml`](api35-full-android-test.xml); it records 595 tests and 3503.78 seconds.
- A preceding full attempt stopped at 185/595 when the disposable emulator disappeared after the host tool session restarted. Its only reported failure was the missing-device teardown error. The completed rerun above is the final full-suite evidence.
- `assembleDebug`, `assembleDebugAndroidTest`, `lintDebug` and `git diff --check`: all passed. Lint reports 0 errors; existing warnings are in the generated lint report.

## Known limits and exact change inventory

Physical phone/tablet acceptance, real TalkBack, switch access, child usability
and audible offline voice quality are still owner-managed. Woodland selection
does not persist through OS process death. Freshwater and Underground remain
unimplemented and absent from the chooser.

Exact changed-file inventory:

- Production: `app/src/main/java/com/bellfamily/bastischool/MainActivity.kt`; `ShellNavigation.kt`; `learning/animals/HabitatExploreContent.kt`; `ui/habitats/HabitatExploreScreens.kt`; new `ui/habitats/WoodlandArtworkLoader.kt`; new `ui/habitats/WoodlandExploreViewModel.kt`.
- Tests: `app/src/androidTest/java/com/bellfamily/bastischool/ui/habitats/HabitatExploreScreensTest.kt`; `OceanExploreRouteTest.kt`; new `WoodlandExploreOwnerTest.kt`; new `WoodlandExploreRouteTest.kt`; `app/src/test/java/com/bellfamily/bastischool/HomeIntegrationTest.kt`; `learning/animals/HabitatExploreContentTest.kt`.
- Product documentation: `MASTER_PRODUCT_LEARNING_ROADMAP.md`; `SESSION_HANDOFF.md`; `TESTING_QA_SPEC.md`; `UX_NAVIGATION_SPEC.md`; `docs/animals-phase5c1-audit/IMPLEMENTATION_PLAN.md`; `docs/animals-phase5c1-audit/SUMMARY.md`.
- Review artifacts: this `REVIEW.md`; `gallery.html`; `api35-full-android-test.xml`; 19 PNGs in `screenshots/animals-phase5c3/` (16 curated in the gallery and three retained from earlier passes); two genuine original-bound PNGs in `screenshots/before-hotspot-refinement/` for the documented hotspot correction.

No other curriculum, saved-session schema, Animal Groups journal or Progress
Tracker identity changed. No commit or push occurred.
