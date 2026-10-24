# Phase 5A.2 — Match Colours & Four-Colour Sorting

## Scope and implementation

Adds two native activities to the existing four-colour area. The chooser retains Explore as its featured entry and now offers Explore, Find, Match, two-colour Sort and four-colour Sort. Existing Find and Explore identities and the Home category are unchanged.

**Match Colours / Farben zuordnen** presents a large solid-colour target in one of four shapes. Four choices represent red, blue, yellow and green using different shapes; the correct choice always has a different shape from the target. Target hue is balanced across five- and ten-question rounds, answer order is shuffled from the session seed, and the exact choices are part of the durable checkpoint. Replay repeats the bilingual instruction; Help asks the child to compare colour rather than naming the answer. Wrong answers can be retried. Activity ID `activity.colours.match`, revision 1, content version 1.1, journal `colours-match-session`, skill `skill.colours.matching`, context `context.colours.different_shapes`.

Canonical matching sample IDs are `sample.colours.{red|blue|yellow|green}.{circle|square|triangle|rectangle}`. The samples are simple Compose shapes without new raster assets. Their accessible choice names do not disclose the correct hue. The target's correct sample shape is checked to differ from the target shape.

**Four-Colour Sort / Vier Farben sortieren** presents eight objects, two each in red, blue, yellow and green, using the existing ball/block representations. Interaction remains tap an object, then tap a category; incorrect placement retains the selection. Activity ID `activity.colours.sort.four`, revision 1, content version 1.1, journal `four-colour-sort-session`, journal marker `CSF1` (`0x43534631`), skill `skill.colours.sort.four`, context `context.colours.four_categories`.

## Compatibility

The original `activity.colours.sort` remains the fixed four-object, red/blue, revision 1/content 1.1 CSR1 contract and continues to use `colour-sort-session`. Its learning source was not edited. The previous `COLOUR_SORT` route still restores directly to the original activity. `COLOUR_SORT_FROM_CHOOSER` also remains its separate chooser entry. Four-colour sorting has a separate route, journal and progress identity; old Sort history does not complete the new activity.

Match and four-colour sorting return to the Colours chooser on Android Back. Options retains the originating route. Both new view models use the app's existing audio controller and offline speech policy. Sound Off remains supported. No progress schema or generic learning engine was changed.

## Visual review

The [HTML gallery](gallery.html) contains 22 actual API 35 emulator captures. Explore red, blue and yellow use reviewed Phase 5A.1B captures; green and all new states are captured in the Phase 5A.2 run. The gallery links to the original Sort example from the Phase 5A.1B evidence and includes this phase's fresh chooser route capture.

| Layout | Captured pixel dimensions | Evidence |
| --- | ---: | --- |
| Phone portrait | 1080×2400 | Chooser, Explore, Find, Match, two/four-colour Sort |
| Phone landscape | 2400×1080 | Chooser and Match |
| Short landscape | 1200×600 | Four-colour Sort |
| Tablet portrait display override | 1200×1920 | Chooser and Match |
| Tablet landscape display override | 1920×1200 | Chooser and four-colour Sort |
| German chooser at 2× text | 1080×2400 | Large-text chooser |

All landscape captures have width greater than height. Tablet-sized layouts are window-size overrides on the API 35 emulator, not separate tablet hardware. The phone landscape chooser and match keep their artwork/choices large; vertical scroll is needed to reach lower chooser entries and some Match controls. At German 2×, chooser labels wrap and remain readable, while the Home control sits below the first viewport. The short-landscape four-colour Sort first viewport shows its heading, count and instruction but not the objects; the screen is vertically scrollable, and automated tests scroll to every object and destination group. Treat short-landscape as usable with scrolling, not as an all-controls-at-once layout.

Visually inspected chooser German 2×, Match active/incorrect/correct, four-colour Sort start and short-landscape, plus the portrait/landscape chooser and tablet views. The four swatches are plainly red, blue, yellow (`#FFEB3B`) and green. Match keeps the target visible with four distinct shapes/colours; the selection border provides a non-colour cue. Incorrect feedback is supportive and retry is clear. Correct feedback remains visible with Next. Four-colour Sort retains readable object/category labels and its grouped targets; short landscape needs scrolling as noted. Automated German, font-scale and scroll-reachability assertions supplement these captures.

Physical phone/tablet acceptance remains owner-managed; emulator display overrides do not establish physical-device behavior. Actual TalkBack, switch-access and child-use acceptance remain deferred.

## Validation

| Check | Result |
| --- | --- |
| Focused Match/Four-Sort/Shell JVM | Included in full JVM: 569 passed, 0 failed, 0 errors, 0 skipped |
| Full JVM (`testDebugUnitTest`) | 569 passed, 0 failed, 0 errors, 0 skipped |
| Match/Four-Sort Compose instrumentation | Included in complete API 35 run: 579 passed, 0 failed, 0 skipped |
| Production chooser/routes/restoration | Route test 1/1 passed after the chooser-capture assertion was moved before capture; also included in 579-test run |
| Complete API 35 instrumentation, first run | 576 passed, 3 PixelCopy failures, 0 skipped; all three in pre-existing `MonthsSeasonWheelScreenTest` image capture |
| Isolated Months wheel responsive test | 12/12 passed on rerun, including the three prior failure parameters |
| Complete API 35 instrumentation, clean rerun | 579 passed, 0 failures, 0 skipped, 56m 46s |
| `assembleDebug` | Passed |
| `assembleDebugAndroidTest` | Passed |
| `lintDebug` | Passed; 0 errors and 5 existing warnings |
| `git diff --check` | Passed after final documentation updates |

One route test iteration asserted a durable sorting update before the worker finished. The test now waits for the view model to settle. A later iteration asserted completion via a text selector that did not match the shared completion semantics; it now asserts the completion summary tag and the durable completed state. Visual inspection then found the English chooser screenshot was taken during its navigation transition. The route test now asserts the chooser destination and waits for Compose idle before capture; the corrected route test passed 1/1 and the English screenshot was replaced. These are test synchronization/selector corrections, not production behavior changes. The initial complete run's three PixelCopy failures are retained here as historical evidence; the isolated test and a second complete run passed without production changes.

## Remaining acceptance

Automated emulator tests do not establish physical-device readability, actual TalkBack or switch-access behavior, child usability, or offline voice quality. Colour discrimination remains inherently visual; screen-reader choice labels avoid revealing the answer. These acceptance items remain owner-deferred. `UX-ANR-01` remains separate. No physical device was tested.
