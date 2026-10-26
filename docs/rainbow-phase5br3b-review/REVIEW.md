# Phase 5B-R3B — Rainbow visual acceptance and final review

## Baseline and working state

- Repository: `paulyglocke/Basti-Learning-Adventure-Public`; branch `main`.
- Starting HEAD: `527730ec63754a477c25852d052f4d95b59b9905`; `origin/main` matched.
- The starting tree contained the three pre-existing documentation edits in
  `SESSION_HANDOFF.md`, `TESTING_QA_SPEC.md` and the Phase 5B-R2 Rainbow review.
  They have been preserved. R3B adds the source/test changes and documentation
  listed below; no commit or push was made.
- Review emulator: disposable API 35 AVD `Rainbow_R3B_API35`, explicit serial
  `emulator-5554`, four guest CPUs and 6 GB RAM. `emulator-5556` and physical
  devices were not used. Display overrides were restored to 1080×2400 at 420 dpi,
  portrait, 1× font scale after capture.
- The previously reported Phase 5B-R3 complete API 35 result was 583/583 in
  48m25s, but its XML is no longer present at the path recorded in the prior
  report. Gradle’s current report directory contains a later focused run instead.
  The prior result is retained as historical evidence, not as final-code
  validation. R3B reruns the full suite after its production UI changes.

## Implemented route inventory

Illustrated Home → Colours / Farben chooser → Rainbow Explore / Der Regenbogen
and Rainbow Order / Regenbogen ordnen. Explore presents the seven conventional
colours in Red / Rot, Orange, Yellow / Gelb, Green / Grün, Blue / Blau, Indigo,
Violet / Violett order. The user can select either the named colour controls or
the matching curved band; the selected name, outline and marker are visible,
replay is available, Sound Off is supported through Options, and Back returns to
the chooser. The chooser remains the parent of both routes.

Rainbow Order retains two existing levels and durable journals:

- **Starter / Mit Beispiel:** four answer positions: Red → Yellow → Blue → Violet;
  a complete seven-colour model remains visible as the example.
- **Full / Alle sieben:** Red → Orange → Yellow → Green → Blue → Indigo → Violet.

Pieces are selected by tapping their bilingual labels and placed immediately.
Numbered destinations distinguish empty positions; placed positions show a
checkmark. Incorrect placement displays a supportive retry message, correct
completion has text feedback and replay/navigation controls, Help and Listen
again remain available, and Options returns to the active route. No sequence,
progress identity, persistence format, or Colour Sort journal was changed.

## Screenshot gallery

[Open the HTML gallery](gallery.html) · [Verified screenshot metadata](gallery-manifest.json)

The gallery contains **49 genuine ADB emulator screenshots** in
`screenshots/`. The PNG dimensions were read from each file and recorded in the
manifest. Portrait images are 1080×2400; phone landscape is 2400×1080; short
landscape is 1280×720; tablet-sized emulator overrides are 1600×2560 and
2560×1600. The tablet captures are emulator viewport overrides, not physical
tablet tests. The gallery has Colours navigation, Explore, Starter, Full,
bilingual comparison, responsive layouts, and restoration/Options return groups.
It includes a genuine before/after pair for the short-landscape prompt fix.

## Visual findings

### Explore

The portrait capture shows seven recognisable curved bands in the expected
Red-to-Violet order with consistent white separation and no overlaps. Blue,
Indigo and Violet are distinct on the captured display and are also individually
named by visible controls. The selected colour has a white/black marker, selected
name and outlined/tinted label, so selection is not communicated by hue alone.
The model is recognisable and the label controls are large, practical touch
alternatives; the narrow drawn bands themselves are not 56dp targets. Replay and
Home remain available below the explanatory copy when scrolling.

Focused instrumentation paints all seven palette colours, taps near every band’s
outer edge and at both curved shoulders, and confirms the selected index matches
the visible band. Taps beyond the outer band and inside the opening do not select
a colour. The explicit bilingual colour controls provide an alternate selection
path. The arc geometry and hit mapping share the same outer-radius/band-width
model.

### Starter

The initial view labels the full seven-colour illustration “Example / Beispiel”,
shows four numbered answer positions and only the four Starter pieces. The model
is clearly separate from the learner’s answer. A partial state shows Red placed
and the next three choices available. Wrong feedback is plain text with a Retry
control; completion is separately confirmed with text. The model and answer
labels remain visible in German and English. Starter and Full controls are
visually distinct and selectable.

### Full

The Full initial state shows seven numbered destinations and seven labelled
pieces; after placement the prompt identifies the colour already in place and
asks what comes next. Full is distinguishable from Starter by the selected level,
seven destinations and absence of the always-visible Starter model. The Help
state displays the rainbow reference, replay remains available, incorrect
feedback permits retry, and completion has an explicit summary and Play again.
The seven pieces wrap to a second row on phone portrait, keeping touch controls
large rather than squeezing all seven into tiny columns.

## Bilingual and responsive assessment

- English and German chooser, Explore initial/selection, Starter and Full states
  were captured. Colour names remain canonical and ordered in both languages.
- Explore at 1.5× English and 2× German remains readable; German labels wrap
  naturally and the lower explanatory/navigation content scrolls.
- Order at 1.5× English and 2× German keeps piece labels and destinations
  readable. German instruction wraps to three lines without overlap. Pieces use
  three columns at 2× font scale.
- Phone portrait uses four columns for seven labels, with the last three on a
  second row. At short landscape, the original full-size heading and prompt were
  clipped. Production now uses compact title, spacing and prompt typography for
  windows below 350dp high. The after image shows the complete instruction and
  both level controls in the first viewport; subsequent pieces/controls remain
  reachable by scrolling.
- Full phone landscape shows the instruction, all seven destinations and all
  seven pieces. Lower Replay, Help and Home controls require vertical scrolling.
  Short landscape also requires scrolling after the prompt; the first viewport
  now makes the task clear before scrolling.
- Tablet portrait and landscape emulator viewports use seven columns for Full
  choices and make Replay, Help and Home visible with ample whitespace. These
  are window/display overrides only.
- No horizontal scroll or overlapped targets were observed. All order choices
  and destinations are at least the project’s 56dp minimum; Explore’s labelled
  controls are the large touch alternative to the narrower drawn bands.

## Interaction and accessibility review

Real production UI flows were exercised through visible controls and ADB input:
chooser to both activities, colour selection controls, English/German Options
switching, Sound Off selection, Starter and Full correct sequences, deliberate
wrong answers and retry, Help, Replay, completion, Play again, Home/Back and
return from Options. The screenshot helper uses visible text/bounds and actual
tap input; it does not inject Compose state. The new graphic instrumentation
checks the rendered canvas and taps its band geometry, including curved ends,
outside space and the inner opening.

Explore exposes one canvas description with the selected colour, plus separately
labelled colour controls. Order pieces expose bilingual names and have touch
surfaces at least 56dp. Destination semantics now announce a position as empty
or name the placed colour in English/German; tests assert both states. Wrong,
retry and completion are conveyed by text and controls as well as colour.
Replay, Help, Home, Back and Options are labelled Compose controls. Compose
semantics and focusable controls were reviewed; actual TalkBack, switch access,
keyboard-only use across hardware, and child usability were not certified by
this emulator review.

Speech requests were exercised through replay and selection flows, and Sound Off
was visibly selected in Options. Audible speech quality/volume and offline voice
quality were not judged as hardware audio acceptance.

## Restoration

Starter partial and completed states and a Full two-placement state were
captured before/after app `am force-stop`, `am start` of the production
MainActivity, then navigation back into Rainbow Order. No application data was
cleared. The same placed pieces, prompt and level rendered after reopening from
the existing durable journal. This is a forced app stop/relaunch and fresh
Activity/ViewModel route; it is not described as Android OS process-death
recovery. The dedicated persistence instrumentation reconstructs fresh
ViewModels against isolated durable storage and verifies state/event
deduplication. Device-managed process-death recovery remains unverified.

## Corrective changes

1. **Explore canvas had no useful measured height.** Before evidence showed the
   rainbow area missing. Replaced an ineffective min/max height constraint with
   a measured viewport-based height. The final screenshot has all seven curved
   bands. Added a pixel and touch-region regression test.
2. **Explore arc sweep direction was wrong.** The final captured screen confirms
   the familiar upper arch. The positive 180-degree sweep is documented against
   Compose’s screen coordinate convention.
3. **Explore hit regions did not align with visible strokes.** Hit selection now
   rounds to the nearest band using the drawing radius/width; new tests verify
   matching centre-edge/curved-shoulder selections and reject taps outside the
   rainbow.
4. **Full-order labels split into seven very narrow phone controls.** Choice
   layout now uses four phone columns, seven wide-layout columns and three at
   1.8×+ font scale. Existing 56dp minimum control height remains.
5. **Short-landscape instruction was clipped.** Before/after captures and a
   411×274dp constrained Compose test show the compact typography/padding change
   keeps the task prompt within the initial viewport.
6. **Placed destination semantics lacked the placed colour name.** Slots now
   announce their position and empty/placed colour in the current language; a
   focused semantics test covers the empty and placed states.

No learning sequence, progress schema, saved-session format, historical journal,
or unrelated route was changed.

## Automated validation

Final-code results are recorded here after the final run:

| Check | Result |
|---|---|
| Focused Rainbow/Colours/Colour Sort instrumentation | **11/11 passed**, 0 failures/errors/skips. Includes `ColourScreensTest` (7), `RainbowOrderPersistenceTest` (1), `RainbowOrderRouteTest` (1), `ColoursRouteTest` (1) and historical `ColourSortHistoricalRouteTest` (1). The focused Gradle XML was replaced by the subsequent unfiltered full run; the full-run XML is preserved below. |
| Fresh full JVM suite | **579/579 passed**, 0 failures/errors/skips; forced task execution, 1m01s. XML reports are under `app/build/test-results/testDebugUnitTest/`. |
| `assembleDebug` / `assembleDebugAndroidTest` / `lintDebug` | All passed after the final production source edits. Lint: **0 errors, 5 existing warnings** (two manifest API attributes, two vector-size notices and the existing WebView JavaScript warning). |
| Complete API 35 instrumentation | **586/586 passed**, 0 failures, 0 errors, 0 skips; disposable `Rainbow_R3B_API35` on `emulator-5554`, four virtual CPUs / 6 GB RAM. Unfiltered `connectedDebugAndroidTest`; Gradle `BUILD SUCCESSFUL` in 48m56s. XML testcase time is 2921.992s. Preserved copy: [`api35-full-android-test.xml`](api35-full-android-test.xml); Gradle report: `app/build/outputs/androidTest-results/connected/debug/TEST-Rainbow_R3B_API35(AVD) - 15-_app-.xml`. This is final-code acceptance and supersedes the pre-R3B 583/583 historical run. |
| `git diff --check` | Passed after final documentation and inventory updates. |

Two unsuccessful focused iterations were limited to the newly added test
harness: first a missing `RainbowOrderAction` import, then a selector ambiguity
because model and choice share a colour tag. The test now advances the real
content reducer for its semantics assertion; the subsequent 11-test focused
run passed. No production behavior was weakened to address these test issues.

Earlier instrumentation startup, timeout and interrupted attempts are retained
in the chronology in [`../rainbow-phase5br2-review/REVIEW.md`](../rainbow-phase5br2-review/REVIEW.md)
and `TESTING_QA_SPEC.md`; none are reclassified as successful test runs.

## Known remaining limitations

- No physical phone/tablet or owner device was tested; tablet captures are
  emulator display overrides.
- Actual TalkBack, switch access and child-use acceptance remain owner-managed.
- Audible/offline voice quality is not established by UI capture.
- OS-managed process-death recovery is unverified; forced stop/relaunch and
  fresh-ViewModel restoration were demonstrated.
- Short/landscape and enlarged-text screens can scroll to reach lower controls;
  the first viewport communicates the activity and compact short-landscape
  task instruction.
- `UX-ANR-01` remains separate.

## Exact changed-file inventory

Exact path inventory is maintained in
[`changed-files.txt`](changed-files.txt), generated from the final complete
working-tree status. The principal production/test paths are:

- `SESSION_HANDOFF.md`
- `TESTING_QA_SPEC.md`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/colours/ColourScreensTest.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/colours/RainbowOrderSessionScreen.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/colours/RainbowScreens.kt`
- `docs/rainbow-phase5br2-review/REVIEW.md`
- `docs/rainbow-phase5br3b-review/` (capture and gallery builders, generated
  gallery/manifests, review and genuine screenshots; see exact file inventory)

## Recommendation

**READY TO COMMIT**. The current production source passed focused checks,
fresh full JVM and complete API 35 instrumentation; screenshots/gallery and
review docs are complete, and no blocking visual defects remain. Physical-device,
actual TalkBack/switch access, child-use, audible voice and OS-managed
process-death acceptance remain owner-managed/unverified. No commit or push was
performed.
