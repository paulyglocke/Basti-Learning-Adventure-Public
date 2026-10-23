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

## Phase 5A.1B — Validation and compatibility follow-up (2026-10-23)

### Repository and implementation

Validation began at branch `main`, with both `HEAD` and `origin/main` at
`8c2b4f802f373119dd36264d3de3c04fea4fed90`; no unrelated work was present.
The three activities remain Explore Colours, Find the Colour and the existing
Colour Sort. Find keeps its own activity ID, revision, content version, journal
and durable progress; Explore stays unscored. The old `COLOUR_SORT` route still
opens Colour Sort directly, while the chooser has its separate
`COLOUR_SORT_FROM_CHOOSER` route.

No Sort learning or storage source changed. `ColourSort.kt` is byte-identical to
the baseline version (SHA-256
`a60ee78da1c783a135949dbcb4f3c12bb3f6c32e4c93ebcf32ffc843fb639185`). The
existing four red/blue objects, rule, activity revision, content revision,
progress identity and journal/checkpoint formats remain unchanged. A fixed
historical CSR1 revision-1 partial-session fixture is decoded by JVM and device
tests. The tests verify the same session ID, the one existing placement, three
remaining objects, completion, four attempt events and exactly one completion
event. This proves compatibility for the checked fixture; it is not a claim of
universal or byte-for-byte compatibility for every historical journal.

### Corrections and investigation

- The previous Home route assertion expected `COLOUR_SORT`; production now
  correctly opens the new chooser. `HomeRouteTest` now checks the chooser while
  the saved legacy `COLOUR_SORT` route is separately exercised.
- The historical Sort route test originally assumed an empty journal after
  another test had created one. It now moves test-owned Sort/progress data aside,
  installs the fixed historical fixture, and restores prior data and preferences
  in `finally`.
- The route test had also been writing test language/audio preferences and
  Find/Explore data into the install it exercised. It now isolates/restores the
  relevant no-backup directories and `lang`, `audioMode` and `round` settings.
- One early Find-recreation attempt reported an encoded checkpoint length of
  3669 versus 3670 bytes at byte 3483. This did not reproduce in subsequent
  focused route runs or the complete suite. Its exact cause was not established;
  the regression now waits for the active Find session and verifies its encoded
  state across recreation.
- Visual review found the old `#F9A825` sample could read as amber/orange.
  Yellow is now `#FFEB3B`, with a dark selection check on yellow in Explore and
  Find. A focused hue assertion rejects orange-like values. No other swatch,
  activity rule or colour inventory changed.

### Behaviour, layout and screenshots

The completed gallery at [gallery.html](gallery.html) includes the full requested
chooser, Explore, Find, Options-return and Sort-entry evidence, plus retained
original Phase 5A.1 screenshots. Captures are actual API 35 emulator images:
phone portrait `1080×2400`, phone landscape `2400×1080`, short landscape
`1200×600`, tablet viewport portrait `1200×1920`, tablet viewport landscape
`1920×1200`. The tablet and short-landscape sizes are display overrides on the
disposable emulator, not separate physical devices. The German chooser at 2× is
`1080×2400`.

Visual inspection found all four canonical swatches clearly visible in portrait
and tablet layouts. Yellow reads as yellow; selected state also uses a border
and check mark. German labels wrap naturally at 1× and 2×. Find keeps all four
choices and Replay/Help reachable. In short landscape, Explore shows its heading,
instruction and large selected sample; the remaining choices and replay are
reachable by scrolling. Landscape chooser cards also scroll. No clipped text,
overlaps or horizontal overflow were observed. One early short-landscape capture
was actually portrait and is explicitly excluded from the gallery.

The emulator reported speech unavailable and displayed the app's existing
offline-voice guidance. Tests cover localized labels, replay/action wiring,
Sound Off, semantics and state transitions, but the emulator had no usable
offline voice. Pronunciation quality, actual TalkBack, switch access, physical
devices and child use remain owner-deferred.

### Validation results and run history

| Check | Result |
| --- | --- |
| Full JVM suite | 565/565 passed; 0 failures, errors or skips |
| Focused compact-text instrumentation on 4 GB API 35 AVD | 4/4 passed |
| Focused Colours route/restoration test after screenshot-export change | 1/1 passed |
| Historical Sort compatibility route | 1/1 passed in complete suite |
| Complete API 35 instrumentation | 577/577 passed; 0 failures, errors or skips; 1h 24m 8s |
| `assembleDebug` | Passed |
| `assembleDebugAndroidTest` | Passed |
| `lintDebug` | Passed; 0 errors, 5 existing warnings |
| `git diff --check` | Passed |

Run history is retained: an earlier complete run reported 574/577 passing, with
two stale Home-route expectations and one Sort compatibility test that assumed
an empty journal. Those test issues were corrected; the focused corrected route
and compatibility tests passed. A later attempt on the original 2 GB disposable
AVD stopped at 1/577 when the instrumentation process exited by signal 9;
logcat showed low-memory process kills. The same disposable AVD was restarted
with a 4 GB allocation without clearing its data. Its compact-text test passed
4/4 and the complete final-code app-wide suite passed 577/577. A focused route
run after the final screenshot-export helper passed 1/1.

One focused connected command was inadvertently launched while both the original
AVD (`emulator-5556`) and disposable AVD were attached. It ran on both. It did not
clear app data, but it wrote test language/audio preferences and Colours
Find/Explore test state on the original AVD. The original preference/session
values were not captured before that invocation, so this session cannot restore
them or certify that the original AVD was untouched. All later test runs were
pinned to the disposable AVD. This incident is recorded rather than obscured.

### Remaining acceptance and recommendation

No production session schema or Colour Sort learning logic changed. The fixed
historical fixture is a specific compatibility case, not exhaustive coverage of
all older user journals. The original AVD's prior preference/session values are
unknown after the unpinned route test. Physical-device, child-use, TalkBack,
switch-access and offline-voice acceptance remain outstanding; `UX-ANR-01`
remains separate.

**Recommendation: READY TO COMMIT.** The owner subsequently authorized commit and
push after validation. The original-AVD state incident remains explicit: earlier
preference/session values were not captured and cannot be reconstructed here.
Physical-device, TalkBack, switch-access, offline-voice and child-use acceptance
remain owner-deferred.
