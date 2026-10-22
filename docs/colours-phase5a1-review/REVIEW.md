# Phase 5A.1 — Explore Colours & Find the Colour

## Implemented inventory

The Colours chooser now has three available destinations: Explore Colours / Farben entdecken, Find the Colour / Finde die Farbe, and the existing Colour Sort / Farben sortieren. Explore is the prominent first choice. It presents red, blue, yellow and green with stable shared IDs `colour.red`, `colour.blue`, `colour.yellow` and `colour.green`; labels come from `CoreContent` and are English/German. The screen is unscored and has no completion or mastery event. Its only stored value is the selected introductory colour in `colours-explore-selection`.

Find the Colour uses a dedicated `activity.colours.find_colour` activity, revision 1, content version 1.1, and `colours-find-session` journal. It offers four swatches, deterministic five- and ten-question rounds, balanced target frequency, retry after an incorrect answer, a non-answering hint, replay, language changes and normal durable progress events. The activity uses the existing durable session host, progress recorder and offline audio controller. Its task, skill and context IDs are `task.colours.find.{red|blue|yellow|green}`, `skill.colours.recognition` and `context.colours.named_swatches`.

Colour Sort remains the existing `activity.colours.sort`, revision 1, content version 1.1, CSR1 journal and four objects (red/blue balls and blocks). Its rule, activity implementation, checkpoint and progress identity were not changed. The legacy `COLOUR_SORT` shell route still resolves directly to that screen; the chooser launches it on a distinct `COLOUR_SORT_FROM_CHOOSER` route so Back returns to the chooser without changing old route restoration.

## Navigation and compatibility

The production Colours Home category opens the new chooser. Explore and Find use separate shell routes and a separate view model/storage. Chooser Back returns to the native Home parent; activity Back returns to the chooser; explicit Home returns to illustrated Home. Options remains the existing shell overlay and returns to its originating route. Existing saved Colour Sort routes keep their former direct destination. No persisted progress format or Sort journal bytes were changed. No data migration or app-data clearing was used as an implementation step.

The existing `ColourSortRouteTest` fixture itself deletes its Colour Sort session file before exercising its assertions. It was run once on the dedicated API 35 test AVD; therefore that AVD's pre-existing Sort journal may have been replaced. This was test-fixture behavior, not an app migration. No physical device or owner device was used. This risk is recorded rather than treated as a compatibility proof from a preserved fixture session.

## Visual review

The actual API 35 emulator captures are in [gallery.html](gallery.html). They show the English chooser, English Explore with red selected, and English Find. The captures are 1080×2400 portrait. In the inspected Explore screen, the large sample, four labeled choices, check and stronger selected border make the current color identifiable without relying on color alone. Find keeps all four swatches and the prompt, Replay and Help visible together. The chooser has a strong Explore entry and readable practice cards. The amber `#F9A825` sample reads as yellow/amber on this display; it is intentionally a saturated canonical yellow rather than a subtle shade distinction.

Automated Compose coverage includes English and German chooser labels, English Explore, German Find with 2× text, selected-state semantics, accessible localized color names, scrolling, Replay, Help, wrong-answer retry and correct progression. The full requested physical layout screenshot matrix (all 18 requested states, multiple orientations and tablet sizes) was not captured. The emulator visual evidence is limited to the three actual portrait screenshots listed above; no portrait image is represented as landscape or tablet evidence. Physical-device, TalkBack, switch-access and child-use acceptance remain owner-deferred.

## Validation

Recorded results for this implementation:

| Check | Result |
| --- | --- |
| Focused and full JVM suite | 563/563 passed; 0 failures, errors or skips |
| New Colours and Home API 35 regression run | 12/12 passed; 0 skipped, 0 failed |
| Existing Colour Sort API route regression | 1 test passed in the earlier focused 13-test run; its test fixture may replace the AVD's Sort journal as described above |
| `assembleDebug` | Passed |
| `assembleDebugAndroidTest` | Passed |
| `lintDebug` | Passed with 0 errors and 5 pre-existing warnings |
| `git diff --check` | Passed |

The complete app-wide instrumentation suite was not run. Earlier in this task a 13-test focused run passed after correction of stale Home-route assertions; after the final content and test changes, the 12-test Colours/Home run passed. The 571-test app-wide baseline was not rerun. No claim is made that physical acceptance, TalkBack, switch access, offline voice quality or child usability passed.

## Files and scope

Implementation files are the new `learning/colours/` content, selection-store, host and audio files; new `ui/colours/` chooser, Explore, Find, swatch and view-model files; `MainActivity.kt`, `ShellNavigation.kt` and `ui/home/HomeCatalogue.kt`. Tests add focused content/host/store JVM coverage, Compose chooser and activity coverage, route/restoration coverage, and update Home route assertions to the new chooser. The existing Colour Sort learning implementation and data files were not modified.

The current worktree is intentionally uncommitted and unpushed for owner review. The full 18-state screenshot matrix was not completed; the gallery intentionally contains only actual captures. See the final task report for the exact changed-file inventory and remaining acceptance gaps.
