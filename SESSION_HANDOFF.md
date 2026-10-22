# Session handoff — 2026-10-21

## UX Refresh Phase 4D — Vocabulary Booster (implemented, uncommitted)

Started at `24de69103af3d79dc1966eb93b3bfd49b8ec2556` on `main`, which matched
`origin/main`. The native Vocabulary entry now opens a compact bilingual chooser
with immediate Explore and direct access to the existing Find and What Is It?
practice modes. Explore presents the existing canonical animal illustration,
word, replay, spoken example and prompt before the six-word selector. Tablet
Explore uses a fitted image beside its controls. Old `VOCABULARY` routes continue
to restore directly; chooser/activity routes, Options return and the independent
Find/Name journals are covered without changing saved formats or learning data.

The API 35 gallery and concise review are at
[`docs/vocabulary-phase4d-review/REVIEW.md`](docs/vocabulary-phase4d-review/REVIEW.md)
and [`docs/vocabulary-phase4d-review/index.html`](docs/vocabulary-phase4d-review/index.html).
All 14 captures have checked dimensions, including genuine 1920×1080 short
landscape and 2560×1600 tablet landscape. Full JVM passed 555/555; focused
Vocabulary JVM tests passed 35/35; focused Vocabulary instrumentation passed
14/14. Shared Home/Months/Seasons/Wilma/Tell Me instrumentation had 14/15 pass:
one unchanged Seasons order-route assertion failed both in the combined set and
in isolation. Complete app-wide API 35 instrumentation was not completed. Debug
build and Android-test compilation passed; lint passed with 0 errors and 5
existing warnings; `git diff --check` passed. Review the reproducible Seasons
failure before treating cross-feature validation as clean.

The gallery notes that 2× German chooser content scrolls naturally and its
second practice card is below the first viewport. A true baseline screenshot was
not captured; gallery comparison is descriptive alongside final captures.
Physical-device, actual TalkBack, switch-access, child-usability and offline-
voice acceptance remain owner-deferred. The work remains uncommitted and
unpushed pending owner review.

## UX Refresh Phase 4C — Wilma’s Week (committed and pushed)

Work started from the requested baseline
`b516b0bc80b34900cc04d1507440db0bafaca888` on `main`; the bounded Phase 4B
screenshot/status correction was included in commit
`24de69103af3d79dc1966eb93b3bfd49b8ec2556`.
The Wilma chooser is a new `WILMA_CHOOSER` presentation route. The six original
Wilma modes continue through the existing `WILMA` route, ViewModel, journals,
checkpoints, assets, weekday order and colours. No learning-state formats or
audio policy were changed. The tablet screenshot exposed and prompted a bounded
wide-layout correction grouping the Explore title/instruction next to the
existing artwork.

The visual gallery has 14 actual API 35 MainActivity captures. Phone portrait is
1080×2400, phone short landscape 2400×1080, tablet portrait 1600×2560 and tablet
landscape 2560×1600. All were visually inspected. Full JVM passed 555/555,
focused Wilma JVM 24/24, chooser instrumentation 24/24, and full API 35
instrumentation 567/567 via direct AndroidJUnitRunner. Debug build, Android-test
APK compilation, lint and `git diff --check` passed; lint has five existing
warnings and two informational findings. The full instrumentation run used the
previous build containing a temporary extra guard suppressing speech on
unrelated pending persistence failure; the guard was removed to restore the
baseline audio behavior, then final source was rebuilt and linted. The rest of
the final source was covered by the passing run. Gradle’s connected-test APK
installer returned `INSTALL_FAILED_INSUFFICIENT_STORAGE`; direct APK/test-APK
installation and runner execution succeeded. The emulator now has limited
remaining `/data` capacity; do not remove application data without owner request.

Physical-device, TalkBack, switch-access, child-usability and offline-voice
acceptance are owner-deferred. UX-ANR-01 remains separate. See
[`docs/wilma-phase4c-review/REVIEW.md`](docs/wilma-phase4c-review/REVIEW.md) and
[`docs/wilma-phase4c-review/index.html`](docs/wilma-phase4c-review/index.html).

## UX Refresh Phase 4B — Seasons chooser and content-first Explore (committed)

Phase 4B was implemented from baseline
`6ea391af4fc2348ae7f59f82f0a17d216571c667` and committed/pushed as
`b516b0bc80b34900cc04d1507440db0bafaca888`. Its bounded Seasons screenshot and
status documentation correction are part of the Phase 4C working tree. Phase 4A
work was preserved. No
question-generation, scoring, audio policy, progress IDs, journal/checkpoint
formats, shared Months chooser, or seasonal wheel geometry were changed.

### Existing functionality inventory

The route is Illustrated Home **Time & Seasons / Zeit & Jahreszeiten** →
**Days & Seasons / Tage & Jahreszeiten** → the prior Seasons entry. The nine
existing phase labels are: **Learn / Lernen** (Explore), **Practise / Üben**,
**What comes next? / Was kommt danach?**, **What comes before? / Was kommt
davor?**, **Missing season / Fehlende Jahreszeit**, **Season clues /
Jahreszeiten-Rätsel**, **Build the Year / Ordne die Jahreszeiten**, **Match season
to clue / Jahreszeit zu Hinweis**, and **Before and after / Davor und danach**.
Explore presents the existing four named seasons, seasonal art and descriptions,
Listen/replay, and season choices. Each phase remains on the same Seasons route
and existing `SeasonsViewModel` journal/checkpoint owners. Months separately owns
Sort Months into Seasons and its Seasons & Months Wheel Explore/Match routes;
these shared paths were not rerouted or duplicated.

### Implementation and review

- Added a dedicated Seasons chooser. Explore is the featured entry; the eight
  practice modes remain accessible in two clearly grouped sets, with canonical
  phase titles/descriptions, warm shared surfaces, existing four-seasons artwork,
  scroll and natural wrapping. Back returns from a phase to the chooser, then to
  Days & Seasons. Home remains directly available. Explore removed the competing
  phase-selector row and gives the existing learning content first position;
  tablet and compact landscape use a content/control split.
- Added route, chooser, keyboard/touch, accessibility bounds, locale/scale/layout,
  Options, language-return, recreation, actual-destination and restoration tests.
  Existing progression/checkpoint semantics remain covered. UX/navigation spec
  records the route-parent contract.
- The API 35 gallery has 14 captures. It shows EN/DE chooser, German 2×,
  portrait Explore EN/DE, practice, chooser in short landscape, Explore on tablet
  portrait/landscape, Options return and the Months-owned shared activity. The
  short-landscape Explore image has been corrected to a genuine **2400×1080**
  frame. It shows the German spring artwork, season name, description, replay and
  answer controls; lower answers remain reachable by scrolling. Rotation is now
  locked after navigation, so UiAutomator cannot revert it before capture. The
  corrected screenshot was visually inspected. All other gallery images were reviewed for clipping,
  hierarchy, German wrapping, artwork proportions and touch-target readability;
  no blocking visual issue found. See [gallery](docs/seasons-phase4b-review/index.html)
  and [review](docs/seasons-phase4b-review/REVIEW.md).

### Validation and open acceptance

- PASS — focused Seasons JVM **61/61**; full JVM **555/555** with zero
  failures/errors/skips. Debug APK, Android-test APK compilation and lint passed;
  lint has 0 errors and 5 existing warnings. `git diff --check` passed before
  final documentation edits and is to be rerun.
- PASS — API 35 Seasons instrumentation **42/42** and complete Months package
  instrumentation **185/185**, zero failures/skips. One earlier Seasons full run
  had a one-byte checkpoint assertion mismatch (6034/6035). Focused and later
  complete reruns passed; cause remains undetermined. Complete app-wide
  instrumentation was not run for Phase 4B; the Phase 4C working tree later
  passed **567/567** on API 35.
- Outstanding: physical-device, actual TalkBack, switch-access, child
  usability and offline-voice acceptance (**owner-deferred**). UX-ANR-01 remains
  separate. Emulator display settings were restored to native 1080×2400, 420 dpi,
  automatic rotation and 1× font scale.

Phase 4B files are tracked in its commit. The bounded evidence correction was
included with Phase 4C in `24de69103af3d79dc1966eb93b3bfd49b8ec2556`; see the
updated Phase 4B review.

## UX Refresh Phase 4A — Months chooser and content-first Explore (uncommitted)

**Current task:** bounded Months navigation/presentation refinement. HEAD remains
`00fdaac51eb87ac1a3f99b7d25c15332274b9a07` on `main`; all Phase 4A changes are
uncommitted. Commit and push are **not authorized**. Do not reset, stash, discard,
commit or push these changes. No activity-generation, difficulty, scoring, audio
policy, progress identity, journal/checkpoint format or persistence migration changed.

### Implementation

- Days & Seasons now opens the bilingual Months chooser. Its featured Year Wheel
  Explore choice opens the existing `MONTHS` route directly. That established route
  still restores as Explore for old saved route state. Explore Back returns to the
  chooser; chooser Back returns to Days & Seasons. Quiz practice remains directly
  reachable from the chooser; Build Year levels, Seasons Match and Seasons Wheel
  retain their existing screens and owners.
- Explore puts the existing Year Wheel first and keeps the named month list, Listen,
  season descriptions and Home reachable. Large tablet windows use a wheel/control
  split; short landscape uses a compact split so the wheel, selected month and Listen
  fit. The wheel drawing, segment boundaries, order, fixed pointer and rotations are
  unchanged. The Between contextual wheel retains its explicit return to the exact
  question.
- A real API 35 visual pass found and fixed the duplicated Months shell title wrapping
  at German 2× text and the wheel being clipped on a short landscape phone. No new
  artwork or dependency was added; the chooser uses season colour cues and approved
  shared surfaces.
- The chooser has EN/DE large-text/form-factor UI coverage. Shell restoration tests
  include chooser/Explore routes, Options and old `MONTHS` restoration. Existing
  three-/four-month state tests and Between's contextual-question test remain active.

### Validation and review evidence

- PASS — focused Months JVM **57/57** and full JVM **555/555**, zero
  failures/errors/skips. `assembleDebug`, `assembleDebugAndroidTest`, `lintDebug`
  and `git diff --check` passed. Gradle emits the existing deprecated
  `VIBRATOR_SERVICE` Kotlin warning. Lint retains five warnings and two
  informational findings.
- PASS — complete API 35 Months-package instrumentation **185/185**, zero failures
  or skips. This includes chooser layouts, Explore, quizzes, seasons, Between's
  contextual return, Build Year levels and route/session restoration. The earlier
  14 wheel failures were caused by the test tag being attached to the wrong node;
  the corrected tag is on the scroll container. The stale route assertion now
  expects the chooser parent.
- The prior four-month exception was reproduced on API 35 and traced to test
  synchronization: `MonthsBuildYearFourScreenTest.kt:89` called keyboard input-mode
  change on the instrumentation thread. The stack continued through Compose focus
  and `ViewRootImpl.requestChildFocus` to `CalledFromWrongThreadException`. Running
  that request on the UI thread keeps the assertion and fixes the test harness. The
  subsequent complete run passed **185/185**; no production Months defect or
  emulator instability was observed.
- Fourteen actual MainActivity captures are indexed in
  [the API 35 visual gallery](docs/months-phase4a-review/index.html); the concise
  [review](docs/months-phase4a-review/REVIEW.md) records hierarchy, landscape and
  tablet findings. `capture.py` restores the emulator's prior font, display,
  orientation and app language settings.
- The short-landscape screenshot was recaptured from the final build and visually
  confirms the full wheel, selected month and Listen control are visible. Remaining
  acceptance is **NOT RUN**: physical S24/Fire device, actual TalkBack,
  switch-access, child usability and offline voice quality. UX-ANR-01 remains
  independent.

Exact changed paths are listed in the Phase 4A review report. Preserve this work for
owner review; do not treat documentation or focused emulator results as full hardware
acceptance.

## UX Refresh Phase 3 — resumed validation (complete; review only)

The owner requested continuation. Original Phase 3 scope and uncommitted work are
preserved; HEAD remains `d0679a8ec227ccae41ba42eed6690fde5fa47784`. Do not commit
or push. Evidence: `/private/tmp/basti-home-resume-20261019/`. A fresh Temurin
17.0.20.1+1 JDK was restored there and its published SHA-256 verified. Only
emulator-5554 was used; its app data was backed up before in-place APK installs.
No data clear, uninstall or journal deletion occurred.

- PASS: fresh full JVM **555/555**, zero failures/errors/skips; `assembleDebug`,
  `assembleDebugAndroidTest` and `lintDebug`. Lint has five warnings and two
  informational findings. The final combined APK build is recorded in
  `/private/tmp/basti-home-resume-20261019/final-build.log`.
- PASS: API 35 focused Home matrix **86/86**, including production browse layouts,
  keyboard/touch navigation, typography, debug prototype navigation/layout and
  design foundation/isolation coverage. Production host route and saved-session
  matrix **24/24**, including all 20 routes in EN/DE, exact Numbers question
  restoration, Options/recreation and preserved sorting/routine sessions.
- PASS: after screenshot review found toolbar clipping at 2× text, both production
  headers were made to reflow. The final Home typography matrix passed **12/12**
  across EN/DE, 1×/1.5×/2× and portrait/landscape, including category navigation
  bounds. A test-fixture enum typo and a scroll-helper assumption were corrected;
  those unsuccessful targeted attempts are retained in terminal history, and
  the corrected full matrix passed.
- PASS: 23 final production MainActivity screenshots were captured and visually
  reviewed. Gallery: `/private/tmp/basti-home-resume-20261019/index.html`.
  It covers EN/DE, category/All, native Colour Sort, legacy Numbers, Options and
  return, enlarged text, short landscape and tablet windows. Browser 55/55 is
  historical evidence for unchanged web sources; browser tests were not rerun.
- The first run exposed two stale test selectors; corrected Months and Support
  Options assertions retain the learning-state checks. Months fixtures now
  preserve loaded journal state. No saved progress was cleared. The reproduced
  generic `ComponentActivity` rotation loss is fixed in a debug-only,
  non-exported `LayoutTestActivity`; production MainActivity/recreation tests
  remain real-host tests. No assertions or timeouts were weakened.
- Emulator network was set offline for automated runs and restored afterward to
  the saved airplane Off / Wi-Fi On / mobile data On values. Physical display
  size/density were restored to 1080×2400 / 420. No physical device was touched.
- Final uncommitted inventory: 36 tracked paths changed (including one promoted
  artwork deletion) plus 9 new files; exact paths are in
  `/private/tmp/basti-home-resume-20261019/changed-paths-final.txt`.

The complete instrumentation suite was not run. UX-ANR-01 remains separate and
OPEN; this fixture lifecycle diagnosis does not establish the cause of every
historical ANR. Physical S24/Fire acceptance, actual TalkBack/switch-access,
child usability and offline-voice quality remain unverified; physical acceptance
was deferred by the owner. See `docs/UX_HOME_INTEGRATION.md` for final routes,
architecture boundaries and visual-review scope.

## UX Refresh Phase 3 — continuation checkpoint (owner requested save)

**Current task:** production illustrated Home integration. The owner requested
“save conversation to here” during validation. This is a durable work/context
checkpoint, not a verbatim transcript. Stop here; resume only when requested.
This section supersedes the historical Phase 2 restrictions below for Phase 3.

**HEAD:** `d0679a8ec227ccae41ba42eed6690fde5fa47784`, main, unchanged.
Initial fetch confirmed remote alignment. All Phase 3 work is uncommitted and
unstaged. **Do not commit, push, reset, stash, discard or overwrite this work.**
No production learning/audio/progress/content/dependency/artwork changes are
authorised. Physical-device acceptance is **NOT RUN — deferred by owner**.

### Implementation preserved

- Shared `ui/home/HomeCatalogue.kt`, `HomeArtwork.kt`, `HomeBrowseScreen.kt`
  promote/reuse the approved prototype presentation. Debug prototype still works
  with its own demonstration navigation/settings. Deleted debug
  `HomePrototypeArt.kt` was promoted, not discarded artwork.
- Production MainActivity renders this Home, with no duplicate shell header or
  prototype notices. Existing catalogue supplies all labels and identities.
  Six categories contain 19 activities; All Activities contains exactly 20,
  including Mixed Adventure. Colours directly launches Colour Sort.
- Real dispatch is the former Home mapping, extracted to `homeDestination()`.
  Grown-ups uses existing native Options. Existing learning callbacks, ViewModels,
  journal/checkpoint formats, audio and legacy sources remain unchanged.
- ShellNavigation adds validated presentation-only `homePage` Bundle state.
  Category/All Back returns root; Options restores browsing page and scroll;
  old/malformed values default root. Activity-root Back intentionally remains
  **Home**, not category-parent. Nested learning Back remains unchanged.
- Tests now enter through real All Activities. New Home integration JVM tests,
  production host/navigation tests, 24-case browse layout matrix and live WebView
  DOM checks are present. Original learning-state assertions are retained.
- Documentation updates begun in README, architecture/navigation specs,
  foundation/prototype documents, and new `docs/UX_HOME_INTEGRATION.md`.
  They are not a declaration of completed validation.

### Evidence and exact validation state

Evidence folder: `/private/tmp/basti-ux-phase3-review.kAIaus/`.
Use JDK 17 at
`/private/tmp/basti-ux-phase2-finalise.UYppOq/jdk-17.0.20.1+1/Contents/Home`.
The older `basti-jdk17-gofZZZ` tree is incomplete; do not use/delete it.
SDK: `/Users/paulbell/Library/Android/sdk`; use **emulator-5554 only**, API 35.

- **PASS:** full JVM **555/555**, zero failures/errors/skips (`build-second.log`).
  Debug APK, Android-test compilation/APK and lint also passed. Lint: zero errors,
  five existing warnings, two information findings. Later focused Home/navigation
  JVM **43/43** passed (`focused-jvm.log`); current XML output contains that focused
  run, not the earlier full count.
- **PASS:** browser **55/55**, Node 22 (`browser-rerun.log`, 54.7s), unchanged tests
  and timeouts. First run (`browser.log`, Node 26) had 52 passes/3 timeouts over
  1.4h. `host-sleep-evidence.txt` confirms sleep/wake interruptions; exact causality
  for each timeout is not proven. No web source or dependency change was made.
- Initial real-shell run (`routes-first.log`): **3 passed/2 failed**. Both EN/DE
  all-20-route tests passed, including actual live WebView rendering, as did exact
  Numbers question restoration through Options/recreation. New-test failures:
  missing wait for asynchronous Addition journal load, and requesting scroll on
  a pinned header. Fixed test synchronization/click helpers, not production code.
- Initial layout run (`layout-first.log`): **45 passed/1 failed**. German 1× phone
  fixture reported “No compose hierarchies found” at its first assertion. Host
  slept during the run; logcat also records a Nexus Launcher ANR, not an app ANR.
  Preserve logs; do not conclude all failures were caused by sleep.
- Subsequent combined run (`home-final.log`): **66 passed/2 failed, 68 reported**.
  All four then-current production host tests, both all-20-route tests, all 24
  HomeBrowseLayout cases, all 12 typography cases, all 8 NativeTextVisibility
  cases, 3 debug navigation tests and 12 foundation cases passed. HomeScreen
  German passed; English lost its Compose hierarchy at the first assertion.
  The other failure was a command-selector mistake: requested nonexistent
  `HomePrototypeTest`; actual class is **HomePrototypeLayoutTest** (24 cases).
  The intended debug layout tests therefore did NOT run in this invocation.
  Also run **BastiDesignIsolationTest** (one case), separately from the 12-case
  BastiDesignFoundationTest. Do not report this combined run as green.
- The hierarchy failure is intermittent and still needs diagnosis. Detailed
  `home-final-logcat.txt` includes the English HomeScreen failure at 16:54:49.
  It passed in the initial layout run; HomeBrowseLayout's earlier failed case
  passed in the subsequent run. Check test-host lifecycle/orientation handoff;
  no proven production cause yet. No assertions/timeouts were weakened.
- **NOT RUN after latest edits:** the new fifth HomeProductionNavigation test
  `existingSortingAndRoutineSessionsSurviveNewHomeWithoutJournalReset`, and latest
  Follow/Vocabulary route-test changes (German return/re-entry now use category/
  All Activities). The requested final full build was interrupted before launch;
  `build-final.log` does not exist. Compile and validate these changes next.
- Three older ColourSort/AnimalGroups/WashHands route fixtures delete journals;
  they were NOT run. The new compatibility test preserves existing sessions and
  checks Options/recreation/Home re-entry without resetting files. Do not clear
  saved data to make tests pass. Relevant remaining native route tests still need
  execution (Months modes, Seasons/Wilma, Follow, Clock, Tell Me, Prepositions,
  Vocabulary, larger-support setting). Inspect fixtures before running.
- **NOT RUN:** Phase 3 production screenshot gallery and visual acceptance.
  `capture.cjs` is prepared outside the repo but not executed; it launches actual
  MainActivity, uses native Options for language and restores display settings.
  `test-stall.png` is a diagnostic fixture screenshot, NOT production acceptance.
  No new CI run, commit or push. Existing Phase 2 screenshots are not evidence for
  this production integration.

### Resume steps

1. Inspect status/diff and preserve every Phase 3 file. Re-read current specs.
2. Compile the latest test edits with JDK 17; run new no-reset session test and
   relevant native routing/restoration tests. Diagnose/reproduce the intermittent
   Compose test-host hierarchy failure; do not suppress it or arbitrarily extend
   timeouts. Correct the command class names above and obtain explicit final counts.
3. Capture/inspect the required production screenshots using the prepared script,
   including native/legacy activity launches, Options/return, EN/DE, phone/tablet,
   short landscape and DE 2×. Produce HTML gallery and Markdown review outside assets.
4. Finish QA/integration docs, exact changed-file inventory, full JVM/build/lint/
   Android-test compilation and diff checks. Browser already passed against
   unchanged web sources; rerun if relevant code changes.
5. Stop with an uncommitted reviewable diff and honest PASS/FAIL/NOT RUN results.

Keep **UX-ANR-01 OPEN and separate**, preserving original Phase 0 diagnostics.
Everyday Skills dedicated single-action artwork remains missing. The German 2×
debug-preview settings compound-word wrap remains outstanding; native Options
uses different copy and still needs screenshot review. No actual TalkBack,
switch-access, physical hardware or child acceptance is claimed.

## UX Refresh Phase 2 / 2B — owner-approved finalisation

The owner approved the reviewed isolated prototype for a joint commit and push.
This supersedes the earlier review-only instructions below, which are historical
validation records. Production Home integration is still not authorised.

Starting local HEAD, fetched `origin/main` and live remote main all matched
`c94687d8c5b38d5fcf94d97b091e95ba7992321b`. The complete change inventory matches
the nine files in [UX_HOME_PROTOTYPE.md](docs/UX_HOME_PROTOTYPE.md). Source review
confirmed the compact Home/category headers, reduced category introduction height,
dinosaur counting blocks and dragon speech bubble. All six bilingual categories
cover 19 existing identities; All Activities retains all 20, including Mixed.
No production Home, learning route, content, audio, persistence, legacy source,
artwork, dependency or Phase 1 implementation is changed. Finalisation edits only
this handoff and the prototype document; application/test source is unchanged from
the owner-reviewed Phase 2B validation and 19 emulator screenshots.

Final evidence: `/private/tmp/basti-ux-phase2-finalise.UYppOq/`.
The first Gradle launch could not start because the previous temporary JDK lacked
`lib/jvm.cfg`; no test/build ran in that attempt (`build.log`). Downloaded the same
Temurin **17.0.20.1+1** into the new evidence folder, verified its published SHA-256,
and left the incomplete original and all project dependencies untouched.

- **PASS:** freshly rerun full JVM **550/550**, including focused prototype **9/9**;
  zero failures/errors/skips. `build-final.log` and `validation.json` retain evidence.
- **PASS:** `assembleDebug`, `assembleDebugAndroidTest` (including Android-test
  compilation), `lintDebug`; unchanged build/analysis outputs were up-to-date.
  Lint retains zero errors, five existing warnings and two informational findings.
- **PASS:** final relevant API 35 instrumentation **56/56**, zero failures/skips,
  **446.052s** (`instrumentation-final.txt`): prototype 27, production Home 16,
  foundation/isolation 13. Covers EN/DE × 1×/1.5×/2× across phone, short landscape
  and tablet fixtures; all 20 previews and existing production routes; touch,
  keyboard/focus, semantic labels, text bounds, scroll retention and recreation.
- **PASS:** nine-file scope and diff/new-file whitespace checks; production diff empty.

Everyday Skills still needs a dedicated single-action illustration; the existing
routine scene remains temporary. The German 2× preview-settings compound-word break
remains a copy/layout polish item, without lost text. Both remain documented in
UX_HOME_PROTOTYPE.md. **UX-ANR-01 stays OPEN and separate**; its three original
diagnostic files were verified present and left unchanged. No causal attribution
to this prototype is established. Physical-device acceptance remains
**NOT RUN — deferred by owner**; actual TalkBack/switch and child acceptance are
not claimed. The owner-approved emulator screenshots remain historical visual
evidence for this unchanged implementation, not physical acceptance.

All local finalisation gates passed. Commit/push is authorised for this exact scope;
the resulting SHA and post-push CI outcome must be verified from Git/GitHub and
reported separately, not inferred from these local results. Browser/content/release
guard checks run in push CI; unrelated full instrumentation was not rerun locally.
Next proposed task: separately authorised, bounded production Home integration
with all-20-route, Options/Back, legacy fallback and saved-session compatibility
tests. Do not infer permission to implement it from this finalisation.

## UX Refresh Phase 2B — visual refinement (validated; owner review; do not commit)

HEAD remains `c94687d8c5b38d5fcf94d97b091e95ba7992321b`. Preserved the nine
uncommitted Phase 2 files. Only six of those are refined: prototype screen/art,
JVM/instrumentation tests, `docs/UX_HOME_PROTOTYPE.md` and this handoff. No production
Home, navigation, foundation tokens, artwork files, learning/audio/progress/session
code or dependencies are changed. Host, manifest and local navigation model are
unchanged from Phase 2. All six categories, 19 mapped entries plus Mixed Adventure,
preview settings, All Activities and restoration remain in scope.

Refinements: compact Grown-ups / Erwachsene header action; dragon shares that row at
large text; compact Back/Home text controls; category artwork/heading combined with
a smaller preview invitation; Maths dinosaur with one/two/three block groups; Words
canonical dragon with a separate native speech bubble. Seasons/whale/colour-table
art is preserved. No appropriate single-action handwashing art was found; existing
Everyday scene and explicit Wash Hands placeholder remain, with an asset brief in
the prototype document. No production integration is authorised.

Evidence: `/private/tmp/basti-ux-phase2b-review.Iw6FpN/`. Original Phase 2 gallery is
preserved; the new `index.html` offers 17 actual emulator before/after pairs plus
two additional German 2× category/settings captures. All **19** final-code captures
were visually inspected; display/font overrides were restored after capture.
Full JVM **550/550**, including focused **9/9**, build and lint pass on JDK 17.
Final relevant API 35 instrumentation: **56/56**, zero failures/skips, **524.452s**:
prototype **27** (24 bilingual/font/window cases and three navigation tests),
production Home **16**, existing foundation/isolation **13**. Header labels retain
56dp targets, Button semantics, keyboard activation and Back → Home focus order.
All 20 previews, preview language, scroll retention and recreation are covered.
Debug and Android-test APKs compile/build successfully; lint has zero errors, five
unchanged warnings (2 UnusedAttribute, 2 VectorRaster, 1 legacy SetJavaScriptEnabled)
and two AutoboxingStateCreation informational findings. Diff/new-file whitespace
checks pass. No production source, dependency or asset changes; exact nine-file
scope verified in `scope-check.json`. No overlapping or horizontally clipped text
was observed; large-text and short pages remain scrollable. The first English Home
capture also shows Material keyboard-focus shading on Grown-ups, not a permanent
filled button. Minor remaining polish: the preview-only German settings notice
breaks the long word Vorschau-Einstellungen across lines at 2×; no text is lost.
Everyday still needs dedicated single-action artwork. Neither is a learning defect.

The initial 56-case run had 31 passes and
25 failures: 24 compact-label width mismatches (87px Text versus 216px paragraph in
the isolated EN case), plus a keyboard test requesting focus in touch mode. The
prototype TextButton now uses intrinsic width with Text filling that allocated width;
the keyboard test enters keyboard mode via Android Tab before focus assertions.
Both isolated rechecks pass (2/2), followed by the successful complete rerun. No
assertions were weakened. Evidence and details are in the new review report.
Physical-device acceptance is **NOT RUN — deferred by owner**. Actual TalkBack,
switch and child recognition are NOT RUN. Unrelated full instrumentation/browser
suites were NOT RUN; web dispatch is unchanged. UX-ANR-01 and its diagnostics remain
separate and open. No commit/push or new CI run is authorised.

Recommendation: ready for owner approval to commit the isolated Phase 2/2B prototype,
not to replace production Home. Review the gallery first; approve an Everyday asset
brief and any remaining preview-copy polish separately before production integration.

## UX Refresh Phase 2 — isolated illustrated Home (owner review; do not commit)

Baseline and unchanged HEAD: `c94687d8c5b38d5fcf94d97b091e95ba7992321b`.
Phase 1 was finalised before this work; its Android/browser CI passed in
[run 39068010439](https://github.com/paulyglocke/Basti-Learning-Adventure-Public/actions/runs/39068010439).
Do not repeat Phase 1 or infer authorization for production Home integration.

Added a debug-only, explicitly launched Compose prototype: canonical dragon welcome,
six bilingual illustrated categories, all 20 existing activities in All Activities,
local activity previews and preview-only grown-up language controls. Six categories
cover 19 original entries; Mixed Adventure remains the twentieth. Back/Home, page
scroll, language and route restoration are local saved-instance state, not production
settings or learning journals. No game starts from this prototype.

Production Home, routes, content, progress, sessions, audio, artwork, dependencies
and Phase 1 Kotlin components are unchanged. The only manifest addition is the debug
prototype host. Full mapping, asset inventory, launch command and exact nine-file
change inventory: [UX_HOME_PROTOTYPE.md](docs/UX_HOME_PROTOTYPE.md).

Validation uses JDK **17.0.20.1**, API **35** Medium_Phone ARM64 emulator (2 GB RAM).

- **PASS:** focused prototype JVM **8/8**; full JVM **549/549**, zero failures/errors/skips.
- **PASS:** `assembleDebug`, `assembleDebugAndroidTest` including Android-test
  compilation, `lintDebug`. Zero lint errors; five unchanged warnings (2 UnusedAttribute,
  2 VectorRaster, 1 legacy SetJavaScriptEnabled) and two AutoboxingStateCreation infos.
- **PASS:** final relevant instrumentation **55/55**, zero failures/skips, 495.679 seconds:
  prototype **26** (24 EN/DE × 1/1.5/2 × four viewport cases + two navigation/restoration),
  existing Home **16** (inventory, typography, actual route entry/return), Phase 1
  foundation/isolation **13**. Includes touch/keyboard, Button/selected semantics,
  56dp targets, natural text wrapping, all 20 previews, Back/Home, scroll and recreation.
- **PASS:** `git diff --check`, plus whitespace checks on all new files.
- **PASS (emulator visual inspection):** all **17** final captures inspected: EN/DE
  phone Home, DE 2× top/scrolled, EN tablet portrait, DE tablet landscape, all six
  category pages, All Activities and short-landscape top/scrolled. No horizontal
  clipping or overlapping text observed. Short landscape now exposes the first
  category row immediately; enlarged/long pages scroll rather than shrink text.
  Normal viewport-edge cropping while scrolling is not hidden/truncated text.
- **NOT RUN:** unrelated full instrumentation/browser suites; no web dispatch changed.
  Physical-device acceptance remains **NOT RUN — deferred by owner**; actual TalkBack,
  switch and child usability acceptance are not claimed. No Phase 2 commit/push/CI run.

Evidence outside Git: `/private/tmp/basti-ux-phase2-review.f2r4tO/`.
Build log: `build.log`; full JVM counts: `jvm-results.json`; lint: `lint-results-debug.xml`.
Complete final runner output: `instrumentation-final.txt`.
Review: `index.html` and `REVIEW.md` in that evidence folder. Capture settings were
restored afterward. No screenshots, logs or generated build outputs are Git changes.
The HTML gallery and Markdown review are for emulator-rendered Compose, not browser
mockups. Tablet views are emulator window overrides, not physical tablet acceptance.

Validation findings and corrections:

- Initial screenshot inspection found a tall welcome area obscuring the first category
  in short landscape. A compact header helped actual screenshots, but the stricter
  320px-high Compose fixture still caught EN/DE first-label visibility failures
  (**53 passed / 2 failed / 0 skipped**). Short-height category cards now place fitted
  artwork beside the unchanged-size label in at most two columns. Normal layouts
  retain the unchanged Phase 1 card. The assertion remains unchanged. German rows
  also use equal card heights to accommodate different wrapping.
- An earlier 55-case attempt failed during setup while emulator user 0 was BOOTING:
  51 ComponentActivity resolution errors, two prototype host resolution errors,
  two credential-encrypted SharedPreferences errors. Independent package resolution
  also failed. A non-wiping emulator reboot restored RUNNING_UNLOCKED and activity
  resolution; the next run reached the actual UI assertions. No production/test code
  was changed for this environment failure. Evidence: `failed-locked-user-tests.txt`
  and `short-label-failure-tests.txt`. The cause of the emulator's interrupted system
  startup itself is not established.

Remaining design review: the maths dinosaur/dots are explicitly a temporary counting
sketch; classroom scenes for Words/Colours/Everyday are busier than bespoke subject
art. Dedicated handwashing art is missing and explicitly labelled in its preview.
No assets are generated or modified. UX-ANR-01 below remains a separate unresolved
legacy Numbers investigation; its diagnostics and attribution caveats are unchanged.

Next slice: owner/child review of imagery and navigation, then a bounded prototype
revision if necessary. Production integration needs separate authorization and
route/session/Options compatibility tests. Leave all Phase 2 changes uncommitted.

## UX Refresh Phase 1 — visual foundation (validated; finalisation authorised)

Started on clean `main` at `dac2fbb1ca68fb80691d2e22cb908cfa0b634919`, matching
fetched `origin/main`. Phase 0 is committed/pushed; its historical checkpoint follows.
The original task stopped before commit/push. The subsequent Phase 2 task explicitly
authorises finalising Phase 1 first, with required CI success before prototype work.
Production Home redesign remains unauthorised.

Added opt-in palette/foregrounds, system-font typography/line heights, spacing/shapes,
a semantic section heading and illustrated selectable category presentation.
Existing NativeActionButton and NativeSupportMessage are reused without changes.
The only consumer is a debug-only developer showcase; production Home, headers,
learning-specific colours, routes, audio, content, progress and dependencies are
unchanged. Six sample mascots reuse canonical assets, not final category artwork.
Details and launch instructions: [UX_DESIGN_FOUNDATION.md](docs/UX_DESIGN_FOUNDATION.md).

Final automated validation used Temurin **17.0.20.1** and the **API 35
Medium_Phone_API_35 ARM64 emulator** (2 GB RAM); no result is inferred from Phase 0.

- **PASS:** full JVM **541/541**, zero failures/errors/skips (537 existing + 4 new).
- **PASS:** affected instrumentation **44/44**, zero failures/skips, 312.916 seconds:
  new design matrix/isolation **13**, Home inventory/typography/routes **16**,
  shared actions **4**, text choices **4**, support **2**, completion **5**.
  Covers EN/DE, portrait/short landscape, 1× / 1.5× / 2×, touch/keyboard,
  Button/selected/disabled semantics, text layout and all 20 existing Home routes.
- **PASS:** `assembleDebug`, `assembleDebugAndroidTest` (including Android-test
  compilation), `lintDebug`, `git diff --check`. Lint has zero errors, 5 existing
  warnings (2 UnusedAttribute, 2 VectorRaster, 1 legacy SetJavaScriptEnabled),
  and 2 informational AutoboxingStateCreation findings. No new lint warning.
- **NOT RUN locally:** unrelated full instrumentation/browser suites (no web changes),
  physical TalkBack/switch/Fire/S24 acceptance. Push CI is checked separately below.

Build evidence: `/private/tmp/basti-ux-phase1-reviewed-build.log`.
Complete runner evidence: `/private/tmp/basti-ux-phase1-review/instrumentation-final.txt`.
Screenshot gallery: `/private/tmp/basti-ux-phase1-review/index.html`; captures and
diagnostics remain outside Git. Automated fixtures produced 60 captures across
the 12 layout cases; representative fixtures were inspected, not all 60.
Captured and inspected 7 launched debug-showcase screenshots: EN/DE six-colour
overviews in an emulated 800×1280dp tablet window, EN phone typography/actions,
DE 2× portrait typography and DE 2× landscape actions (top/scrolled). No overlap
or horizontal clipping was observed in these views; large content scrolls.
The initial manual rotation request stayed portrait; the capture helper was
corrected to use window-manager rotation and verify landscape PNG dimensions
before replacing that capture. No production change was needed. Emulator display
settings were restored. These are component previews, not redesigned Home screens
or physical-tablet acceptance.

Resolved new-component diagnostics:
The first new emulator matrix reported 12 failures/13 tests: each layout case
caught the same missing Button role on the new clickable Surface. The independent
isolation/disabled test passed. Added explicit `Role.Button` to the card, keeping
the assertion unchanged. Also corrected the debug showcase's modifier-parameter
order flagged by Lint. An intermediate build encountered old/new signature mismatch
while that edit was being completed; only the subsequent fresh build is final evidence.
The next matrix exposed a TextLayoutResult width mismatch on wrap-content text:
"Numbers & Maths" had a 421px node/right edge but a 735px paragraph width. Its
captured glyphs were not clipped. New labels/headings/demo text now fill their
allocated reading width; fonts and strict no-overflow assertions are unchanged.
The isolated case then passed. Visual inspection also corrected the debug decoder's
fixed downsampling: it now samples to the 96dp illustration's pixel size, retaining
small originals instead of unnecessarily enlarging a downsampled dragon.
Physical-device acceptance remains **NOT RUN — deferred by owner**.
UX-ANR-01 remains a separate open investigation; original diagnostics are untouched.

Finalisation review confirmed exactly 11 scoped files and no production consumer of
the new theme. Freshly fetched origin/main still matched the starting baseline.
JDK 17 `testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug` succeeded
again in 5 seconds: 75 tasks up-to-date, one executed. The unchanged source/tests and
Gradle input verification preserve applicability of the recorded 541 JVM / 44 emulator
passes; this was not a fresh instrumentation execution. Finalisation build log:
`/private/tmp/basti-ux-phase1-release-build.log`. Diff/whitespace checks pass. Only
authorisation/handoff wording changed after the reviewed implementation. The release
commit is identified in Git history; remote equality and actual CI are reported after
push, not predicted here.

Next: isolated illustrated Home prototype, only after Phase 1 push/required CI pass
and receipt of the complete Phase 2 brief (currently truncated at “Learning categories
— Display”). No Phase 2 implementation, design rollout or new category navigation
has been started.

## UX Refresh Phase 0 — Home cleanup and typography (approved finalisation)

Started on clean `main` at `af8579280282b649480f996aaf9b25f0c9f1d803`, matching
freshly fetched `origin/main` and the UX audit. This supersedes the historical
weather checkpoint's pre-release Git status below; that weather implementation is
already in the starting commit. Following owner review, commit and push of these
eight scoped files are authorised after final validation. No redesign is authorised.

Two independently reviewable presentation corrections:

1. `homeCardGroups()` applies the unavailable-game filter **after** the existing
   `take(5)`, `slice(5..8)`, `drop(9)` grouping. Home shows 20 functioning entries in
   their original Learn / Practice / Play groups (5 / 4 / 11). Dragon Treasure Hunt,
   Dinosaur Rescue, Memory Pairs and Crocodile Snap remain in the catalogue/plans,
   but are not rendered. Host dispatch and all native/legacy activities are unchanged.
2. Home's 32sp greeting previously inherited the 24sp body line height, colliding
   when wrapped. It now explicitly uses 40sp line height without shrinking text.
   Home alone opts into a full-width header title when width is below 320dp × font
   scale, preventing the Options button from squeezing long words into tiny
   fragments. Other headers retain the existing row; wrapping, scrollability,
   semantics and the existing header-height bound remain.

No learning content, answer logic, audio policy, progress/session identity, assets,
dependencies or navigation design changed. Approved future direction: Design A's
illustrated discovery, B's consistent components and C's calm learning surfaces.
This checkpoint does not implement that redesign.

Final automated validation: Temurin **17.0.20.1**, **API 35 Medium_Phone_API_35**
ARM64 emulator (2 GB RAM).

- **PASS:** full JVM **537/537**, 0 failures/errors/skips, including focused
  Home/navigation **29/29** (3 new Home catalogue tests).
- **PASS:** affected instrumentation **28/28**, 0 failures/skips: Home inventory
  and touch/keyboard **2**, typography **12**, actual host routes **2**, existing
  native text visibility **8**, existing compact text visibility **4**. Both
  languages, portrait/short landscape and 1× / 1.5× / 2× typography are covered.
  All 20 destinations and Back to Home are exercised in English and German;
  this verifies dispatch, not full educational parity of legacy activities.
- **PASS:** `assembleDebug`, `assembleDebugAndroidTest` (including Android-test
  compilation), `lintDebug`, `git diff --check`. Lint: 0 errors, 5 existing warnings
  (2 UnusedAttribute, 2 VectorRaster, 1 legacy SetJavaScriptEnabled), 2 informational
  AutoboxingStateCreation findings. Existing deprecated VIBRATOR_SERVICE warning.
- **NOT RUN locally:** unrelated full instrumentation suite, browser suite (web
  dispatch unchanged), physical-device acceptance — **deferred by owner**.
  Real TalkBack/switch hardware and S24/Fire testing remain pending.

Owner-authorised finalisation reran the full JVM suite (**537/537**) and affected
emulator suite (**28/28**, 215.043 seconds), both with zero failures/skips, on the
unchanged reviewed source. Build, Android-test compilation, Lint and diff checks
also passed. Evidence: `/private/tmp/basti-ux-phase0-release-build.log` and
`/private/tmp/basti-ux-phase0-review/instrumentation-release.txt`. Only these eight
scoped files are included; no screenshots, traces, build outputs or environment
files are included in the commit.

New-test diagnostics: the initial compile's unsupported matcher was replaced with
existing Compose matchers. Two initial route failures were a test lifecycle error:
manually calling `ComponentActivity.onSaveInstanceState` while resumed demoted its
LifecycleRegistry to CREATED and disabled lifecycle-bound Back handling. The test
now stops before saving and resumes before Back; production navigation is unchanged.
No assertions were weakened. A later host transport interruption lost streamed
output; the final complete **28-test** runner result was saved on-device and pulled
to `/private/tmp/basti-ux-phase0-review/instrumentation-final.txt`.

Separate investigation item **UX-ANR-01 — OPEN (legacy Numbers WebView)**:
a batch screenshot helper's rapid swipes
unexpectedly entered legacy Numbers, followed by an input-dispatch ANR (5001 ms).
The main-thread sample shows WebView attachment through AndroidViewHolder; total
emulator CPU was 100% with memory pressure. These observations do **not** establish
a root cause or prove independence from Phase 0. Traces are preserved in
`/private/tmp/basti-ux-phase0-review/{capture-anr.log,last-anr.txt,anr-trace.txt}`.
No legacy production code was changed. This manual-capture incident remains an
unresolved diagnostic finding, separate from the passing automated suite.
Follow-up: reproduce controlled Home → Numbers entry, retain input/lifecycle,
WebView and resource-pressure traces, and compare with the starting commit before
classifying causality. Neither attribution to Phase 0 nor pre-existence is proven.
Original diagnostic files were verified present and left unchanged at finalisation.

Review evidence and the exact file list are in
`/private/tmp/basti-ux-phase0-review/REVIEW.md`; temporary screenshots/logs are
outside Git. Captured and inspected **8 actual-app Home screenshots**: English
and German at 1× / 1.5× / 2× portrait and 2× landscape. Greeting lines and header
controls are readable without overlap; narrow enlarged-text titles use the extra
row, while wide landscape retains the compact row. The gallery also contains
**24 constrained Compose fixture captures** (top/bottom of the 12 layout cases),
clearly distinguished from actual-app screenshots. Final isolated Home capture
completed successfully; emulator language/display settings were restored.
Gallery: `/private/tmp/basti-ux-phase0-review/index.html`.
The release commit is identified by Git history; remote verification and the
corresponding GitHub Actions result are reported after the authorised push.

Next planned slice: a bounded reusable design-token baseline and a separately
reviewable illustrated Home prototype using Design A; no new categories/routes or
production redesign should be inferred from the Phase 0 changes.

## Seasonal Weather Exploration — Phase 2 review checkpoint

Started from clean `main` at `6d955c31df62fe22cb6043fbab4788da3e241eb1`, matching
freshly fetched `origin/main`. No commit or push is authorised for this task.

Explore now has optional seasonal weather, Change Weather and explicit Listen
controls. A typed bilingual content model supplies twelve defaults and curated
alternatives (educational Central European examples, never forecasts). Native
Canvas snow, frost, light/moderate rain, sunshine, wind/autumn leaves, clouds and
mixed sun/clouds sit in a stationary transparent layer clipped to the canonical
season quadrant and circular centre. The PNG, chronological ring order, solved
alignment and fixed −75° pointer are unchanged. Match has no weather aids.

The renderer has at most 24 particles, one draw-only clock and one cancellable fade
target. Hidden/inactive screens dispose or pause motion; system animator scale zero
renders a static frame, with labels and speech still available. No new dependencies,
artwork or audio engine. Existing offline audio settings apply, speech is explicit,
and month/weather changes cancel obsolete speech. MSW2 adds weather visibility and
override to the existing checkpoint; MSW1 reads without altering learning state.
Historical quiz identities, journals and progress are untouched. See architecture
and content specs for the contract and downgrade limitation.

Review found and corrected two existing interaction edge cases: gesture detectors
captured the initial rotation, and interrupted snap animations added a whole-step
delta to a fractional angle. Handlers now refresh with snapped rotation and targets
are exact 30° multiples; even a short drag returning to the same month settles.
Regression tests cover current-month tap speech, repeated quarter-turn dragging and
fractional/negative/multi-turn animation starts. Visual review also moved summer's
sun overlay onto the painted sun rather than drawing a second sun alongside it.

An initial 25-test emulator run passed 24 and failed the new motion test. Its clock
was frozen only after composition, by which point Compose's automatic test-clock
policy had cancelled the infinite animation. Taking clock ownership before
composition resolved the test without changing production weather motion. The
isolated motion test passed all eight visual variants, rapid replacement,
pause/resume and live system-scale-zero assertions. No assertion was weakened.

Final validation on Temurin **17.0.20.1** and the **API 35 Medium_Phone_API_35** emulator:

- **PASS:** full JVM **534/534**, 0 failures/errors/skips, including weather **8/8**
  and existing wheel geometry/state **4/4**.
- **PASS:** final affected instrumentation **31/31**, 0 failures/skips: weather
  layouts **12**, actual-frame motion/lifecycle **1**, existing wheel layouts **12**,
  route/options/recreation/repeated dragging **1**, Months owner/audio/restoration **5**.
- **PASS:** `testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug`
  and `git diff --check`. Lint has 0 errors, 5 existing warnings (2 UnusedAttribute,
  2 VectorRaster, 1 legacy SetJavaScriptEnabled) and 2 informational
  AutoboxingStateCreation findings. The initial compile also reported the existing
  deprecated VIBRATOR_SERVICE warning.
- **PASS:** captured and inspected January snow, February frost, April rain, July
  sun, October wind/leaves, German 2× landscape (wheel and scrolled text), and
  static February. Final captures are in `/private/tmp/basti-weather-review-final/`;
  names start `weather-month.<month>-en-1.0-false.png`,
  `weather-month.october-de-2.0-true.png`, `weather-label-month.october-de-2.0-true.png`
  and `weather-static-february.png`. The final static capture confirms exact ring
  settling, unlike the initial interrupted-snap capture. The supplied PNG is unchanged.
- **NOT RUN:** entire unrelated instrumentation suite, browser suite, CI and
  physical-device acceptance. Physical-device acceptance is **NOT RUN — deferred
  by owner**, including Fire-tablet frame rate/battery, actual TalkBack and offline
  voices. No frame-rate benchmark or long-duration performance claim has been made.

Emulator log: `/private/tmp/basti-weather-final-instrumentation.log` (31-test direct
AndroidJUnitRunner run selecting MonthsWeatherMotionTest, MonthsWeatherScreenTest,
MonthsSeasonWheelScreenTest, MonthsSeasonWheelRouteTest and MonthsOwnerTest).
Gradle XML results are in `app/build/test-results/testDebugUnitTest/`; logs, screenshots
and generated outputs are not repository changes. Earlier diagnostic logs are
`/private/tmp/basti-weather-instrumentation.log` and `/private/tmp/basti-weather-motion.log`.

Files changed: new `MonthsWeather.kt`, `MonthsWeatherOverlay.kt`, `MonthsWeatherTest.kt`,
`MonthsWeatherScreenTest.kt`, `MonthsWeatherMotionTest.kt`; updated `MonthsSeasonWheel.kt`,
`MonthsSeasonWheelScreen.kt`, `MonthsViewModel.kt`, `MainActivity.kt`, `MonthsOwnerTest.kt`
and `MonthsSeasonWheelRouteTest.kt`. Documentation: README, roadmap, backlog,
architecture, content, navigation, QA and this handoff. No resource/dependency changes.

Phase 2 implementation and required automated validation are complete; owner visual
review is next. **Uncommitted; no push or CI run.** HEAD remains the starting commit.

## Seasons Wheel pointer refinement — validated review checkpoint (2026-10-14)

Moved the stationary pointer from the twelve-o'clock seam to `−75°` Canvas angle
(15° clockwise from the top), derived from half of the existing 30° segment size.
The pointer is drawn radially inward; selected-month lookup compares this same angle
to the rendered segment centres. At solved rotation it identifies March/März, and
the existing ring order, seasonal quadrants and fixed centre illustration are
unchanged. The UI highlight, selected-month label, speech selection and accessibility
description continue to share that geometry-derived month.

Validation under JDK 17: focused wheel JVM tests **4/4**; API 35 wheel screen tests
**13/13** (0 failures/skips), separate route/recreation test **1/1**, Android test
compilation, `assembleDebug`, `lintDebug` and `git diff --check` passed. The screen
test directly rerun for capture also passed **12/12**. Inspected solved-state
screenshots show the pointer at the March/März segment centre and matching selected
month label: `/private/tmp/months-wheel-pointer-review/files/months-wheel-correct-en-1.0-false.png`
and `/private/tmp/months-wheel-pointer-review/files/months-wheel-correct-de-1.0-true.png`.
These are temporary review captures, not repo assets. Physical-device acceptance
remains **NOT RUN — deferred by owner**.

## Interactive Seasons & Months Wheel — implementation checkpoint (2026-10-14)

Started from clean `main` at `2bb5313ca591750c3e59ee7d5fcd642ff45aa025`, matching
`origin/main`. Added a separate `MONTHS_SEASONS_WHEEL` route without replacing the
existing Year Wheel or Months ↔ Seasons matching quiz. The supplied transparent
four-season artwork is preserved as a 1024×1024 PNG in `drawable-nodpi`; it remains
stationary under a native Canvas ring with 12 equal 30° month segments. At solved
rotation zero, January's centre is −135° in Canvas coordinates; the segment starts
are January −150°, March −90°, June 0°, September 90°, December 180°. Thus winter
(December–February) fills top-left, spring top-right, summer bottom-right and autumn
bottom-left exactly. Drag and keyboard-accessible previous/next controls snap at 30°.

The single fixed pointer sits 15° clockwise from twelve o'clock and points radially
inward at the centre of a month segment. Its selected month is derived by comparing
the pointer angle with the same segment-centre geometry used to render the ring; at
the solved alignment it identifies March. The central illustration and pointer stay
stationary as the outer ring rotates.
The separate revision-1 `months-seasons-wheel-v1` checkpoint stores rotation, mode,
feedback, attempts, replay/hint and round without writing shared quiz/progress
identities or changing existing journals. Match/Explore, offline MonthsAudio, retry,
celebration and child-controlled Play Again use existing native conventions.

Visual review on API 35 captured incorrect/correct English 1× portrait, incorrect
German 1× landscape and correct German 2× portrait/landscape, among the full 24-image
EN/DE × 1×/1.5×/2× × portrait/landscape matrix. Screenshots were inspected. A first
large-text screenshot showed the pointer clipped at the Canvas edge; the pointer base
was moved inside the Canvas and the 12-case screen suite passed again. The 2× UI uses
short ring labels plus a scrollable full month-name list and spatially labelled season
legend. The solved-state screenshots were inspected for pointer-to-month alignment;
these emulator captures are not physical-device acceptance (**NOT RUN — deferred by
owner**).

Validation: full JVM **526/526**, zero failures/errors/skips; `assembleDebug`,
`lintDebug` and Android-test compilation passed under JDK 17. API 35 wheel screen
matrix **12/12**, wheel route/options/recreation **1/1**, and the existing Months route
class **4/4** passed together (**17/17**) after tightening its asynchronous readiness
wait to require idle worker state and matching language. A preceding broad Months
package run completed **146/147**: its sole failure was that existing test's stale
“state non-null” readiness check selecting a disabled Build the Year button; no
product failure was demonstrated, and the corrected test passed in the 17/17 rerun.
The full Months package was rerun after that synchronization correction and passed
**147/147**, zero failures/skips. The final JDK 17 JVM/build/lint pass also succeeded.
Lint retains
five existing warnings (two `UnusedAttribute`, two `VectorRaster`, one legacy
`SetJavaScriptEnabled`) and two informational `AutoboxingStateCreation` findings;
Kotlin reports the existing deprecated `VIBRATOR_SERVICE` warning. `git diff --check`
passes. At this earlier validation checkpoint, no commit, push or CI run had been
performed; physical-device acceptance remains **NOT RUN — deferred by owner**.

Review screenshots are saved under `/private/tmp/months-wheel-review-final/` and are
not repository assets. Current examples: `months-wheel-wrong-en-1.0-false.png`,
`months-wheel-correct-en-1.0-false.png`, `months-wheel-correct-de-2.0-false.png`, and
`months-wheel-wrong-de-1.0-true.png`.

## Follow Instructions — introductory two-level native activity (2026-10-14)

Started on clean `main` at `e230105236f2ace273c6d7ed265f98e49759ee29`,
matching fetched `origin/main`. Home now opens a bilingual level chooser. Level 1
retains the established one-step activity, skill, task and journal identities;
new rounds use activity revision 4/content 1.4 to correct German accusative object
phrases, while revision 1–3 checkpoints restore their original prompts unchanged.
Level 2 is a bounded ordered two-action animal selection: four pictured choices,
both targets in one spoken instruction, fixed touch/choose templates, calm retry,
optional hint, whole-instruction replay and child-controlled Next. A pictured and
spoken example is available. Both levels use the existing six canonical animal PNGs,
shared offline audio policy, 5/10 rounds and completion presentation.

Level 2 owns `activity.follow.instructions.two`, `skill.listening.two_step`,
`context.follow.animal_order`, ordered-pair task definitions and the independent
`follow-instructions-two-session` write-ahead journal (revision 1/content 1.4).
The frozen plan, partial first step, attempts/support, language, pending event and
completion acknowledgment survive restoration. No existing Months, Seasons or
one-step progress identity/schema changed. Stage 2 colour/adjective prompts,
three-step, spatial, multi-scene, adaptive and advanced listening remain deferred.
Physical-device acceptance is **NOT RUN — deferred by owner**; emulator coverage
does not verify audible offline voices or actual TalkBack/child usability.

Automated validation under JDK 17 and the API 35 `Medium_Phone_API_35` emulator
is complete: focused Follow JVM **15/15**, full JVM **522/522**,
focused Follow instrumentation **29/29**, and
full instrumentation **382/382**, all with zero failures/skips. `assembleDebug`,
`lintDebug`, Android-test compilation and `git diff --check` passed. Lint retains
0 errors, 5 existing warnings (2 `UnusedAttribute`, 2 `VectorRaster`, 1 legacy
`SetJavaScriptEnabled`) and 2 informational `AutoboxingStateCreation` findings.
An initial broader run
encountered two `No compose hierarchies found` failures in unchanged shared-UI
fixtures; an isolated run initially suffered instrumented-process termination
without a Java/native exception, then passed **16/16** after a non-destructive
emulator reboot (3.5 GB `/data` available). A route assertion was corrected to
check the exact generated German template; another now waits for the worker's
recorded Hint state before checking the support message. No production change was
made for the transient runner failures. The complete fresh-boot rerun passed
**382/382**. The original runner trigger remains unproven; emulator stability
should be watched in later test runs, not mislabelled as a product regression.
The only content correction was German one-step accusative grammar; no other
educational behavior or artwork was changed. The release commit, push and CI
outcome are reported separately after the final scoped diff review; do not infer
physical acceptance from these checks.

## Months Phase 5 — Between (2026-10-12)

Starting `main` at `f9d3a71d21c34211df5e2018d2e987b42687631e`, matching fetched
`origin/main`, with a clean worktree. Added a native Between question whose target
is the middle-month identity and whose persistent display names both adjacent
endpoints. All 12 targets are defined, including November–December–January and
December–January–February. Seeded 5/10 rounds use unique targets and four unique
nearby choices. English/German instructions, hints, feedback and existing Months
audio policy are used. A separate `activity.months.between` / `skill.months.between`
identity and `months-between-session` journal preserve the shared session and
progress contracts; Missing Month and other Months IDs/journals are unchanged.

The Year Wheel is an optional supported-learning route: the hint is durably recorded
without an answer attempt, and returning restores the same question/endpoints. The
layout uses responsive row/stack presentation, large month text, relationship and
answer semantics, keyboard choices, and scrollable EN/DE content. No new artwork,
framework, curriculum level or runtime dependency was added.

- Full JVM: **504 passed**, zero failures/errors/skips.
- API 35 `Medium_Phone_API_35` Months instrumentation: **107/107 passed** after
  correcting an asynchronous wait/state lookup in the new route test. A separate
  focused large-text bilingual accessibility/semantics test passed **1/1** after
  the final answer-description refinement. Android instrumentation compilation
  passed. Emulator evidence is not physical acceptance.
- `assembleDebug` and `lintDebug`: **PASS**. Lint: 0 errors, 5 existing warnings,
  2 existing informational findings; none are specific to Between. Kotlin reports
  the existing deprecated `VIBRATOR_SERVICE` warning in MainActivity.
- `git diff --check`: PASS. Physical Samsung S24 / Fire Max acceptance, installed
  offline voice quality, TalkBack and child-use review are **NOT RUN — deferred by
  owner**.
- GitHub Actions and final commit/push status are recorded in the task report.

README, roadmap, backlog, content, architecture, progress, QA and navigation
contracts now list Between as implemented and retain four-/six-/twelve-month Build
the Year levels, Months ↔ Seasons, adaptive progression and calendar generalisation
as deferred.

## Months Phase 4 — Build the Year (2026-10-12)

Started on clean `main` at `454c91f`, matching fetched `origin/main`. The three-month
native Build the Year level is implemented as `activity.months.build_year.three`
(revision 1/content 1.1), with its own durable write-ahead checkpoint and progress
identity. Existing Months activities and journals are unchanged.

- The child sees a December → January → February demonstration, then orders a
  deterministic shuffled window of three consecutive canonical months. Generation
  covers all twelve cyclic starts, including the year boundary. The interaction is
  accessible tap-to-select/tap-to-place: select a large month tile, then activate
  the next numbered slot. No drag is required. Replay, a bilingual next-month hint,
  calm correction/retry, item speech and child-controlled completion are included.
- The session is one ordered three-placement activity, not a 5/10 quiz round; this
  matches the existing Seasons ordering convention. It uses `skill.months.sequence`
  and `context.months.year_order`, with separate activity/task IDs. Final three-step
  progress is now accepted by the schema-1 completion model/codec; old builds do not
  read this new completion length, so downgrade across it is unsupported.
- JVM: **498 passed**, zero failures/errors/skips. This includes cyclic generation,
  December wrapping, retry/support, exact checkpoint restore, write-ahead failure
  retry, progress event serialization and existing Months regressions.
- API 35 `Medium_Phone_API_35` instrumentation: **88 passed**, zero failures/skips
  across the Months package. New Compose coverage spans EN/DE, 1×/1.5×/2× text,
  portrait/landscape, wrapping bounds, slot semantics, keyboard placement, retries,
  completion and audio controls. Route coverage verifies Options/language, Activity
  recreation, background/return, Back and return to the Months hub. The two route
  tests were rerun after adding explicit background/return coverage: **2/2 passed**.
  Emulator coverage is not physical acceptance.
- `assembleDebug` and `lintDebug`: **PASS**; lint reports 0 errors, 5 warnings and 2
  informational findings, all pre-existing/outside Build the Year. `git diff
  --check`: PASS. No existing Months progress identity/journal or authored month
  content was changed.
- Physical Samsung S24 Ultra / Fire Max child-use, TalkBack, installed offline voice,
  insets/touch and lifecycle acceptance: **NOT RUN — deferred by owner**.
- Deferred: Between; four-/six-/twelve-month sequencing; Months ↔ Seasons; adaptive
  progression; and broader wheel/sequence/calendar generalisation. No curriculum
  expansion or generic manipulation framework was introduced.

Documentation updated: README, master roadmap, backlog, content/progress/navigation/
QA specifications and this handoff. Validation completed before commit/push; see
the final Git/CI result in the task report.

## P1 Native validation and documentation reconciliation — 2026-10-12

Starting HEAD `466315c0ba2e463b7c1212e0a71828b4ac355e26` matched fetched
`origin/main`; worktree was clean. No runtime or authored learning content changed.

- Full JVM suite: **490 passed**, zero failures/errors/skips. Focused Months/Tell Me/
  Scene Description JVM run also passed (62 tests).
- Scene Description Python suite: **32 passed**; generator check validated all 9
  categories / 81 scenes and current generated Kotlin.
- API 35 `Medium_Phone_API_35` emulator instrumentation: **88 passed**, zero
  failures/skips. This includes all 54 Months quiz-layout cases across EN/DE, three
  modes, 1×/1.5×/2× text and portrait/both landscapes; wheel layout cases include a
  2× tablet configuration. Months owner, completion, route and restoration tests
  passed. Tell Me coverage includes 2 route tests, 8 layout cases and 3 artwork
  loader tests.
- `assembleDebug` and `lintDebug`: **PASS**. Lint: 0 errors, 5 warnings and 2
  informational findings, all outside Months/Tell Me. APK SHA-256:
  `d8bbfa7525634d64cf0fae51f0114b7833de80dea0ba1caafbc071a5f6993c3f`.
- Tell Me revision 11 A/B/C/A pilot rotation, bilingual support pairing, stale
  callback rejection, Options/language, activity recreation, background/return and
  Home/fresh-owner behavior passed automated checks. No educational content changed.
- Physical S24/Fire Max acceptance remains open, including child-use clarity,
  TalkBack, installed offline voices and touch ergonomics. Tell Me still needs
  independent German review and manual visual review of the exact C bundles at
  narrow/short/large-text layouts. Emulator evidence is not physical acceptance.
- No regression was found. README, roadmap, backlog, project brief, architecture,
  content/QA specs and Tell Me pilot validation docs now record current status.

## Months Phase 3 — Missing Month — 2026-10-11 (committed in `466315c`)

Implemented on top of `9317704` (Phase 2 Find / Before and Next); committed and pushed as `466315c`.
Days & Seasons → Months now also offers Missing Month / Fehlender Monat.

- Bounded three-month sequences with one gap. All twelve canonical months and all
  three gap positions produce 36 definitions, including December/January crossings.
  Task IDs encode start month, length (`three`) and gap position. No device dates.
- Deterministic 5/10-question rounds have distinct target months/task definitions,
  four unique neighbouring-month choices, and balanced gap positions. The first
  question has an interior gap; subsequent questions mix gap positions. This is
  one fixed teaching level, not adaptive progression or a four-month level.
- Prompt: “Which month is missing?” / “Welcher Monat fehlt?” Replay includes the
  visible sequence, saying “blank” / “Lücke” without naming the answer. Hint:
  “Say the months in order and find the gap.” / “Sag die Monate der Reihe nach und
  finde die Lücke.” Existing Months audio owner and All / Questions / Off apply.
- Sequence is a wrapping, non-interactive semantic group with a localized gap
  description; answer and Listen buttons stay separate. Existing Retry, Help,
  assisted Year Wheel, completion and Again behavior is reused.
- Separate `activity.months.missing`, `skill.months.missing`,
  `context.months.year_order` and `months-missing-session` journal. Shared durable
  host/recorder retain exact plan/order/language/support/attempts and silent restore,
  pending-write retry and deduplication. Activity revision 1/content 1.1; prior
  Months generators, wording and journals are unchanged. No shared schema changes.
- Validation: focused Months/shared JVM **157 passed**; full JVM **490 passed**; the
  current focused Months JVM rerun passed **24 tests** with zero failures; the
  P1 checkpoint above reran the full JVM and broader 88-case emulator suite.
  Android test compilation, `assembleDebug` and `lintDebug` passed.
- Physical S24/Fire Max acceptance remains outstanding: child-use clarity of
  wrapping sequences, TalkBack narration, offline voices and real touch ergonomics.
  Build the Year, Months & Seasons, four-position difficulty and adaptive learning
  remain deferred. Current execution order and status are reflected in the roadmap
  and backlog.


## Tell Me pilot native validation attempt — 2026-10-10 (historical; superseded)

At that time the focused offline Gradle JVM attempt exited before tests because
JAVA_HOME was unset and java was absent from PATH. The toolchain later became
available; current results are in the 2026-10-12 checkpoint above.

The historical attempt and superseding validation evidence are recorded in
docs/tellme-variety/PILOT_VALIDATION.md. Revision 11 was committed in `dce0bb7`;
validation here is against source revision `466315c`.

## Tell Me third-question pilot — 2026-10-10

Owner requested the next task. Added complete bilingual C bundles to Ocean .03
(imaginary crab event), Classroom .02 (next step after asking for help) and Zoo .08
(penguin prediction). All three current PNGs were directly inspected. A/B bundles,
reference metadata, picture bytes and identities/order are preserved. Exact C
bundles, A/B snapshots and hashes are in THIRD_QUESTION_PILOT.json.
Content revision 11; 78 scenes still have two slots, three have three (165 total).
Generated Kotlin, current ledger, review and counts updated. Existing modulo
rotation already supports the pilot; no runtime/progress/schema change needed.

All 32 focused Python checks and generator validation passed. JVM rotation tests
exercise six complete mixed-slot visits, C support pairing, stale callbacks and
fresh-owner reset. API 35 route instrumentation covers C with Help, grown-up
disclosure, Options/language, recreation, background/return and Home wraparound.
These native tests now pass at `466315c`; independent German and physical-device
acceptance remain open. Do not expand the pilot without separate authorization.

## Tell Me short-example checkpoint — 2026-10-10

Owner requested the next task. Reviewed all 162 question/example/model pairs and
replaced 121 short EN/DE child examples across 73 scenes. Examples model the
requested action, location, feature, feeling, reason, sequence or pretend speech;
appropriate names/counts and already useful phrases remain. Names and gestures
are still accepted, and examples are optional. Only childExample changes inside
bundles; all questions, starters, words, models, guidance, purpose labels,
reference metadata, picture bytes, IDs/order and rotation are preserved.

Content revision 10 and generated Kotlin are updated. CHILD_EXAMPLE_CORRECTIONS.json
records every revision-9 bundle across all 81 scenes, exact example replacements
and identity/reference/image hashes. Historical ledgers are unchanged; tests
verify complete successor bundles before comparing earlier snapshots. This pass
derives examples from existing models, with no new visual acceptance claimed.
Validation: 31/31 focused Python tests, generator --check and git diff --check
passed. Android/JVM/instrumentation were not run; independent native-speaker and
physical-device acceptance remain pending. Changes are local and uncommitted.

Next checkpoint: a small picture-reviewed third-question pilot, followed by
three-slot rotation/support and navigation/lifecycle validation before expansion.

## Tell Me clue/story/language checkpoint — 2026-10-10

Owner requested the next task. Preserved revisions 6–8 and improved 12 complete
bilingual bundles across 11 scenes: Ocean .04 B/.07 B/.08 B, Jungle .08 A/.09 A,
Woodland .09 A/B, Zoo .07 A/.08 A, Classroom .02 A, Farm .04 B/.06 A.
Clues model usable visible details and explain optional partner guessing. Concrete
imaginary openings/dialogue support one-event stories without a required ending.
Help need is conditional; farm/jungle wording is simpler. Other slots/reference
metadata are preserved. All 11 current PNGs were directly inspected. Exact changes,
unchanged slots, picture evidence and hashes are in CLUE_STORY_CORRECTIONS.json.
Farm .06 A intentionally succeeds the preserved-A snapshot from revision 8; its
spatial B is unchanged. Earlier ledgers remain historical and unmodified.

Content revision 9, generated Kotlin, scene tables and aggregate counts are
updated. Editorial Ocean .08 B changes naming → properties and Woodland .09 B
personal → story. No runtime objective/progress schema, scoring, rotation,
navigation, IDs or artwork changed. Validation: 30/30 focused Python tests,
generator --check and git diff --check passed. Android/JVM/instrumentation were
not run; independent native-speaker and physical-device acceptance remain pending.
Changes remain local and uncommitted; no push.

Next checkpoint: remaining name-only child examples and mismatched support,
then picture-supported third-question bundles with rotation/lifecycle validation.

## Tell Me spatial checkpoint — 2026-10-10

Owner requested the next task. Preserved revisions 6 and 7 and simplified the
17 audited spatial slots across 14 scenes. Short EN/DE questions remove abstract
comparison wording and supplied locations. Optional adult guidance retains the
reference objects and accepts other accurate locations. Mountains .03 clarifies
flower-group selection; Farm .06 makes its second comparison sentence optional;
Park .06 German support uses Kegel. Other slots and all reference metadata remain
unchanged. All 14 current PNGs were directly inspected; exact changes, unchanged
slots, reference hashes and artwork hashes are in SPATIAL_PROMPT_CORRECTIONS.json.

Content revision 8, generated Kotlin, current ledger, review and summary are
updated. Objective counts remain unchanged. Validation: 29/29 focused Python
tests, generator --check and git diff --check passed. No Android/JVM/instrumentation
run; native-speaker and physical-device acceptance remain pending. IDs, artwork,
order, two-slot rotation, navigation and progress contracts are unchanged.
Changes remain local and uncommitted; no push.

Next checkpoint: improve the audited clue/story invitations and remaining awkward
bilingual wording. Third-question bundles remain a later bounded checkpoint.

## Tell Me seven action/action pairs — 2026-10-10

Owner requested the next task after the two-scene correction checkpoint. Preserved
all existing revision-6 work and revised only the second-visit bundles for Ocean
.01, Jungle .01, Woodland .02, Sky .01, Mountains .02, Classroom .01 and Zoo .02.
New alternatives invite prediction (two), imaginative expression (two), a possible
reason, visual comparison and a simple sequence. Complete EN/DE starters, words,
models, short child examples and scene-specific adult guidance stay paired. First
bundles and reference metadata for these seven scenes are unchanged.

Content revision 7, generated Kotlin, content contract and variety ledger/review/
counts are updated. QUESTION_VARIETY_CORRECTIONS.json records exact before/after
bundles, original first bundles, reference metadata hashes, artwork hashes and
scene-specific picture evidence. All seven current PNGs were directly inspected.
The original twelve editorial purpose labels remain; story includes sequencing
and creative expression. No runtime objective/progress schema was introduced.

Validation: all 28 focused Scene Description Python tests passed, including real
production image hashes, generated-output consistency, the original English
fingerprint and the new first-bundle/reference preservation checks. Generator
--check and git diff --check passed. Android/JVM/instrumentation were not run;
native-speaker and physical-device acceptance remain pending. Images, IDs, manifest
order, two-slot rotation and navigation are unchanged.
Changes remain local and uncommitted; no push.

Next checkpoint: simplify the audited spatial prompts and remove supplied spatial
answers, preserving their reference objects and paired support. Third-question
bundles and the other editorial audit recommendations remain future work.


## Tell Me audit corrections — 2026-10-10

Started from clean main `5acf611da207bd38e3622b190c628768d654b550`, matching remote
main during the preceding inspection. Owner authorised starting fixes. This first
checkpoint corrects Woodland .06 B (fox inside its den; no visible tail) and
Classroom .06 A (pictured painting tools; football already on the chair), with
complete EN/DE prompt/support bundles. Inaccurate source visual notes, the Woodland
reference sentence/purpose and Classroom brush vocabulary are corrected too.
Content revision is now 6; generated Kotlin, variety ledger/counts and content
contract are updated. EDITORIAL_CORRECTIONS.json records exact reference changes;
the original regression fingerprint and historical image-review ledger are preserved.
No artwork, scene IDs/order, rotation, navigation, speech or progress behavior changed.

Validation: all 27 Scene Description Python tests passed, including actual production
image hashes and original fingerprint; generated output consistency and diff whitespace
checks passed. Android/JVM/instrumentation were not run for this content checkpoint;
German native-speaker and physical-device acceptance remain pending.

Changes remain local and uncommitted; no push. The wider audit recommendations,
seven action/action pair improvements, simpler spatial wording and third-question
bundles remain future work. Next checkpoint: revise the seven action/action pairs
with complete picture-supported bilingual bundles, then validate before broadening.
The publication/build statements in the older revision-5 handoff below are historical;
revision 5 was already committed on current main when this correction pass began.


## Tell Me complete content-variety draft — 2026-10-10 (build blocked)

Owner authorized improving the remaining 79 scenes before build validation,
then explicitly authorized committing/pushing before Android validation and
requested the existing stable-signed APK action. Publication was attempted, but
GitHub create-blob and create-tree both returned 403 “Resource not accessible by
integration”. No remote object, commit, ref update or workflow dispatch succeeded.
The CLI authentication check also failed. GitHub is installed; usable repository
write access and Actions dispatch access must be restored before publication.
The source snapshot and complete patch remain saved. CI validation is the next check.
All 81 approved pictures now have two literal EN/DE conversation bundles: prompt,
starter, three helpful words, model, short child example and matched parent guidance.
The existing nine-category/nine-picture order remains. Owner feedback now makes
one Next picture press advance immediately; the sentence model is optional Help.
Activity-lifetime deterministic rotation selects once per entered scene; Help,
model reveal, language, Options and recreation retain the slot. Home clears conversation
but retains cursors; a fresh process resets them. No artwork, speech or storage added.
Content revision 5 supersedes the earlier two-scene draft, not a released revision.
The old reference lists and English fingerprint remain unchanged; all bundle additions
are separately asserted in docs/tellme-variety/CONTENT_CHANGES.json.

Validation: 26 isolated metadata/authoring fixture checks passed; one real-image
hash/review test skipped. Generated output and original English fingerprint pass.
JVM/Android/lint/instrumentation remain unrun because the local SDK/Gradle/network
are unavailable. New German, visual semantics and physical acceptance remain review
items; prior picture evidence is not new image acceptance. Nothing committed or pushed.
Clock feedback is also drafted: shared Explore/Practice face shows minutes 0–59,
with larger bold multiples of five. Hour markings, hand line, digital hours and
legend use the supplied dinosaur green outline; equivalent minute cues use the
dragon red outline. The darker existing vector outlines preserve text contrast.
Practice still hides the digital time until solved. Minute labels remain drawn
inside the one clock semantic target. No clock angles, drag, audio, scoring,
storage or task generation changed. All rendered clock layouts need visual QA.
Gradle validation was attempted again using a writable cache and the session proxy;
the wrapper cannot download Gradle 8.9 (socket creation: Operation not permitted). Nothing committed/pushed.
See docs/tellme-variety/README.md and REVIEW.md for scope, counts and resume steps.

## Months v1 Phase 1 — 2026-10-09 (validated Explore implementation)

Days & Seasons now offers Months / Monate. Twelve canonical semantic IDs, full EN/DE
names and existing season IDs drive one selected-month state. The clockwise native
wheel starts at January, joins December to January, uses four seasonal regions and a
dark selected outline. Full names appear below the wheel and in chronological,
responsive accessible buttons; the season/month legend and parent note explain the
German meteorological convention. No artwork/dependencies were added.

The existing lazy audio controller speaks only the explicitly tapped/listened month.
All/Questions permit these manual requests; Off stays silent. Opening, restore,
Options/language sync, background/return do not narrate. Selection persists independently
through the existing atomic browsing pattern (`months-selection`, schema 1, bounded
128 bytes). Language/audio stay shell-owned. Failed/corrupt persistence is surfaced and
retained, with Retry; no progress evidence or shared session/placement/count changes.

At this Phase 1 checkpoint, Find/Before/Next/Missing/Build/season quizzes were
deferred. Phases 2 and 3 later added Find, Before/Next and Missing Month; Build and
Months/Seasons remain future work. MONTHS_V1_RESEARCH.md remains the approved plan.
NATIVE_ARCHITECTURE_SPEC.md records implementation details and persistence limits.

Validation completed: focused Months/shared-audio JVM tests (67) passed; full JVM suite
465 passed, zero failures/errors/skips. Debug build and Android-test APK compilation
passed. lintDebug passed: zero errors, five existing warnings and two informational
findings outside the new Months files. API 35 emulator: two owner persistence/audio
tests and one shell-routing test passed; the corrected 14-case layout matrix passed
(17 unique focused instrumentation tests across targeted runs). Matrix covers EN/DE,
1×/1.5×/2× narrow portrait and short landscape, plus a tablet-sized viewport at 2×.
Tests check actual text bounds, touch selection, selected semantics, keyboard Enter,
scrolling and reachable controls. Test harness corrections account for clipped wheel
bounds during scrolling and explicitly set each orientation; product logic was not
relaxed. Representative EN/DE wheel captures were visually reviewed, including narrow
German 2× and short landscape. `git diff --check` passed. No physical S24/Fire acceptance,
real TalkBack audition or native voice-quality acceptance is claimed.

## Clock Make This Time — 2026-10-08 (locally validated)

Clock progression is now **Explore → Make This Time — whole/half hours**.
The existing Home entry opens Explore; a mode action opens `CLOCK_MAKE`, with Back
returning to Explore and normal Home/Options behavior. Both Clock surfaces now use
shell inset padding, correcting the original Explore omission. No artwork changed.

`ClockPractice` / `activity.time.clock.make` revision 1/content 1.1 has eight
seeded, stored targets: four whole + four half hours drawn without duplicates from
24 ClockTime values, mixed in seeded whole/half pairs. Start = target minus 90
minutes. ClockFace, semantic geometry and wording are reused, with configurable
30-minute practice movement; Explore retains its original five-minute resolution.
Only explicit Check counts an attempt. Wrong checks preserve clock/target; after
two wrong checks, a long-hand hint explains 12/6. Correct Check locks the clock;
Next advances and final Next completes. Eventual success completes each question;
first-attempt accuracy is derived separately, not shown as a punitive score.

Live digital time is hidden until correct. Target prompt/display/TTS share wording
(e.g. “Make half past four.” / “Stelle halb fünf ein.”). Current-clock accessibility
describes the actual hand positions, without a hidden digital/time-answer label.
TalkBack custom actions, keyboard arrows and visible buttons adjust by 30 minutes.
Existing shared title/progress/support/action/completion presentation is reused.

The existing Clock audio owner serves both modes. Explicit Listen replays target;
Next/Again prompts and hints follow All/Questions; automatic generic retry/positive
feedback/completion follow All. Off suppresses everything. Opening/restoration stay
silent; manipulation, navigation, language/policy changes revoke pending speech.

A separate strict, digest-checked `clock-make` write-ahead journal preserves target
order, index, semantic position, all task attempts/wrong/support/checked times,
language and completion/pending progress. Writes are serial before Check; failure
blocks checks until retry rereads the journal. Shared progress records submitted
time IDs and whole/half skills, with stable deduplicated attempt/completion IDs.
The only shared model/codec change adds eight-task completions to the supported
sizes; schema-1 layout and old accepted sizes remain intact. Older app builds cannot
read new eight-task progress (downgrade unsupported). Explore/other journals remain
unchanged. NATIVE_ARCHITECTURE_SPEC.md and PROGRESS_TRACKER_SPEC.md give details.

Validation with Java 17:
- Focused Clock/shared audio/session JVM run passed; final full suite: 455 tests,
  zero failures/errors/skips, including 17 Clock semantic/practice/navigation tests.
- Android test compilation and assembleDebug passed.
- lintDebug passed with zero errors, three warnings and two informational findings.
- API 35 emulator: eight Clock screen/owner tests passed, then the added real-shell
  route test passed (nine total). Covers EN/DE, hidden digital readout, circular
  movement, accessibility actions, keyboard, retry/hint, eight-question completion,
  Again, 1.5× narrow/short layout, silent durable restore, Options/recreation/Back and
  title clearance below the shell top bar.
- git diff --check passed. Remote CI is checked after push and reported separately.

Physical S24/Fire acceptance remains open: drag comfort, TalkBack focus, actual
installed offline voices, 30-minute motor demands, learning effectiveness and
lifecycle/process recovery. Emulator evidence is not physical acceptance.
Next bounded Clock task remains **What Time Is It?**; quarter hours, five-minute/
arbitrary-minute practice, Digital → Analogue, Fix/Match Clocks, clock construction
and calendar/routine missions remain explicitly deferred. Historical entries follow.

## Native Clock & Time Explore — 2026-10-08 (locally validated)

New `CLOCK` Home route / `activity.time.clock`, titled Clock & Time / Uhr & Zeit.
One `ClockTime` drives digital H:MM and both hand angles, with continuous hour-hand
geometry. Native Canvas needs no artwork. Bidirectional circular movement unwraps
hour boundaries, rounds to minutes while dragging and snaps to five minutes on
release. Whole/half hours are emphasised, with “Move the long hand.” /
“Bewege den langen Zeiger.” support. Central-pivot movement is ignored; keyboard,
TalkBack actions and visible ±5-minute controls are alternatives to dragging.

Central EN/DE display/Listen wording covers all 24 whole/half-hour teaching points
(3:30 = Half past three / Halb vier; 12:30 = Halb eins). Explicit Listen reuses the
existing native controller; no auto-speech, new TTS system, quizzes or progress
fabrication. Manipulation/navigation/settings revoke speech. A separate bounded
atomic browsing checkpoint stores exact time/language, with coalesced worker writes,
flush-on-leave and visible retry. Global Options retains settings authority.
See NATIVE_ARCHITECTURE_SPEC.md for format, gesture and failure details.

Validation with Java 17:
- Focused Clock semantic suite: 6 JVM tests passed; additional navigation test passed
  in the full suite (7 Clock tests total).
- Full JVM suite: 445 tests, zero failures/errors/skips, including shared audio/session.
- Android test compilation, assembleDebug and lintDebug passed; lint has zero errors,
  three warnings and two informational findings.
- Focused Clock instrumentation: 4 tests passed on API 35 emulator, zero skips/failures.
  Covers circular dragging, digital/hand agreement, semantics, keyboard, 1.5× narrow
  portrait/short layouts, EN/DE, cancellation and exact silent owner restoration.
- git diff --check passed. Remote CI is checked after push and reported separately.

Physical S24/Fire touch, TalkBack focus/voice quality, installed voices and lifecycle
acceptance remain open. Emulator execution is not physical acceptance. All Clock
quizzes, digital editing, quarter/five-minute curriculum, face construction, routines
and dates remain intentionally deferred. Existing activities/journal formats and
artwork are unchanged. Historical entries below remain intact.

## Prepositions remaining artwork cleanup — 2026-10-08 (locally validated)

Replaced exactly ten supplied PNGs under `app/src/main/assets/Prepositions/scenes/`:
`snake_next_to`, `dinosaur_next_to`, `dragon_next_to`, `crocodile_next_to`,
`fish_outside`, `seahorse_outside`, `dinosaur_near`, `crocodile_near`,
`dinosaur_far_from`, `dragon_far_from` (all `scene_prepositions_<name>.png`).
Bytes match the supplied ZIP; all decode as 1448 × 1086 RGB PNGs, 4:3. Manifest
metadata/hashes were recalculated for those ten records only. All 52 unique scene
IDs/paths/mappings remain intact; no other PNG changed.

Source-art review compared the dinosaur distance trio, crocodile NEXT_TO/NEAR,
and all four OUTSIDE cave scenes together. Narrow side gaps, wider local gaps and
large open-distance spans are distinct; all replacement silhouettes remain in frame.
Fish/seahorse are fully outside with empty, visible cave entrances, consistent with
the unchanged accepted turtle/octopus replacements. Also checked unchanged snake/
dragon NEAR against the new NEXT_TO scenes. No supplied replacement failed review.

Audit totals are now **52 CLEAR, 0 BORDERLINE, 0 ARTWORK_FIX_REQUIRED**. Original
observations and both replacement stages remain documented. This supersedes earlier
entries describing ten outstanding borderline scenes; those entries remain history.
Current README/backlog/roadmap/content/QA summaries were updated narrowly. The audit
regression now requires all 52 CLEAR and preserves/checks the exact ten former
borderline IDs, replacement date and original six required-fix history. Integrity,
restore and semantic assertions were not relaxed. No production Kotlin changed:
activity revision 3/content 1.3, wording, v1/v2 recovery, guided audio/highlighting,
compatibility, progress and journals are unchanged.

Validation with Java 17:
- All 46 focused Prepositions JVM tests passed, including v1/v2 restore and asset hashes.
- Full JVM: 438 tests / 79 classes, zero failures/errors/skips.
- Python CI integrity/freshness suite: 36 tests passed.
- `compileDebugAndroidTestKotlin`, `assembleDebug`, `lintDebug`: passed.
- Lint: zero errors, three warnings and two informational findings.
- Exactly ten PNG changes; all 52 decodes/mappings/hashes verified; `git diff --check` passed.

No emulator/device was connected, so instrumentation was not executed. Physical S24/
Fire acceptance remains open, including small FAR_FROM subjects at app size, distance/
threshold interpretation, screen-reader flow and installed EN/DE audio. Source-art
CLEAR is not physical acceptance or evidence of learner mastery. Remote CI status
is reported after push; these are local validation results.

## Current-status documentation reconciliation — 2026-10-08

Documentation-only follow-up to implementation `ccc6423`. README, BACKLOG, the master
roadmap and TESTING_QA_SPEC now distinguish implemented bounded Maths, Wilma
TODAY/BILINGUAL, Seasons MISSING/CLUES/MATCH/COMBINED and Prepositions v3 from future
curriculum. The six cave fixes remain resolved by source-art review; ten borderline
scenes and physical acceptance remain open. Expanded the device matrix without
reopening the specifically accepted S24 upgrade/flows; Fire Max remains outstanding.

The implementation CI run for `ccc6423` completed successfully (build and browser
jobs); distribution was skipped. Local Android-test compilation is distinct from
instrumentation execution, which did not run for that pass. Earlier headings such
as COMBINED “validation pending”, Java-25 lint failure and “full regression not run”
are dated history: the later 2026-10-08 full JVM/lint evidence supersedes only those
layers. Their hardware/instrumentation gaps are not silently closed.

No runtime, test, artwork or workflow changes; no tests/builds or device checks were
run for this documentation task. Documentation checks cover diff whitespace, local
links/anchors, scope and consistency against current source/CI and recorded evidence.
Task 2 (native UI execution / CI coverage) remains future work and was not begun.

## Native Prepositions v3 cave integration — 2026-10-08 (locally validated)

Integrated the six owner-supplied PNGs without resampling or renaming:
`fish_inside`, `turtle_inside`, `octopus_inside`, `seahorse_inside`,
`turtle_outside`, `octopus_outside` under
`app/src/main/assets/Prepositions/scenes/scene_prepositions_<name>.png`.
All decode as 1448 × 1086 RGB (4:3). Updated their actual manifest dimensions and
SHA-256 hashes; all 52 unique mappings and other artwork remain unchanged.
The six source images were visually re-reviewed: whole animals are clearly set back
inside the cave or entirely outside its threshold. The audit preserves their original
ARTWORK_FIX_REQUIRED observations and now classifies them CLEAR. Current totals:
42 CLEAR, 10 unchanged BORDERLINE, no remaining ARTWORK_FIX_REQUIRED.

Preserved the complete v3 wording/compatibility pass below: BELOW “unter der Wolke”,
INSIDE “in der Höhle”, OUTSIDE “außerhalb der Höhle”, scene-specific cases, original
four excluded pairs plus cave OUTSIDE/IN_FRONT_OF exclusion. New rounds are activity
revision 3/content 1.3; strict frozen v1/v2 restoration retains old wording/state and
progress identities. Artwork changes do not revise journals or historical evidence.
Guided Listen sequencing/highlighting and individual Listen actions are unchanged;
individual Listen semantics identify the complete phrase. The audit regression test
now checks the six resolved rows, preserved history, and exact ten borderline IDs.

Validation completed locally:
- Focused Prepositions JVM: 46 tests passed, including v1/v2 exact restore.
- Full JVM: 438 tests passed (79 classes), zero failures/errors/skips.
- Python CI checks: 36 passed, including raster integrity and generated-content freshness.
- Browser regression: 55 passed.
- `assembleDebug`, `compileDebugAndroidTestKotlin`, `lintDebug`: passed with Java 17.
  Lint: zero errors, three warnings and two informational findings.
- All six distribution signing/version guard checks passed.
- Exactly six PNG changes match ZIP bytes; all 52 manifest paths/hashes/decode checks pass.
- `git diff --check`: passed. No unrelated or unexpected untracked files.

Initial lint invocation failed with bundled Java 25 (`25.0.3`); rerunning with CI's
Java 17 completed successfully without code/assertion changes. No connected emulator
or device was available: instrumentation was compiled, not executed. Physical-device
rendering, TalkBack and learner acceptance remain outstanding, as do the ten documented
borderline scenes. Remote CI status is reported after push; this entry records local
results only.

## Native Prepositions wording / distinction audit — 2026-10-07 (historical implementation snapshot)

The following records the original unvalidated pass; the 2026-10-08 integration
entry above supersedes its artwork and validation status.

Reviewed all 13 relations and opened all 52 original scene PNGs. Full visual
contracts, final EN/DE phrase table, decisions and one classification/reason per
exact scene ID are in [PREPOSITIONS_CONTENT_AUDIT.md](PREPOSITIONS_CONTENT_AUDIT.md).
Results: 36 CLEAR, 10 BORDERLINE, 6 ARTWORK_FIX_REQUIRED. This is source-art review,
not physical-device acceptance. All scene membership, IDs, PNGs and manifest hashes
remain unchanged; flagged scenes remain in the catalogue for a later artwork pass.

New wording: BELOW label/phrase uses “unter” / “unter der Wolke”, including dative
reference-object inflection for distractors. INSIDE label uses “in” and its phrase
remains “in der Höhle”. OUTSIDE retains standalone “draußen” and complete phrase
“außerhalb der Höhle”: “vor” would replace outside/interior contrast with foreground
direction and cannot fix ambiguous threshold art. Other reviewed correct phrases
remain unchanged, including “zwischen den beiden Steinen”, “in der Nähe des Steins”
and “weit weg vom Stein”. Full phrases drive both answer labels and Listen semantics.

Preserved all four excluded synonym pairs (IN/INSIDE, UNDER/BELOW, ON/ABOVE,
NEXT_TO/NEAR). Additionally exclude OUTSIDE/IN_FRONT_OF on cave references only,
including distractor pairs. Preserve useful opposites and BETWEEN/NEXT_TO. Seeded
shuffle mechanics and answer-order policy are unchanged; cave candidate filtering
can change subsequent new-round choices for a seed. No saved plan is regenerated.

New activity revision 3 / content 1.3. Existing revision 1/content 1.1 and revision
2/content 1.2 active/completed/pending journals retain exact authored state, order,
support, attempts and progress identities. Frozen old wording is used for strict
validation and revision-aware display/option speech/scene semantics; no silent
checkpoint rewriting or schema change. Again creates v3. V3 restore additionally
checks relation compatibility against the scene reference. Guided Listen sequencing,
highlight lifecycle, answer action and audio policy remain unchanged.

ARTWORK_FIX_REQUIRED exact IDs:

- `scene.prepositions.fish.inside`
- `scene.prepositions.turtle.inside`
- `scene.prepositions.octopus.inside`
- `scene.prepositions.seahorse.inside`
- `scene.prepositions.turtle.outside`
- `scene.prepositions.octopus.outside`

The four interiors fill the cave mouth without a clear visible threshold/set-back;
turtle/octopus outside are too similar to their inside scenes and overlap/crop the
threshold. Show the whole animal clearly inside/beyond that boundary in the future.
BORDERLINE: `scene.prepositions.snake.next_to`, `scene.prepositions.dinosaur.next_to`,
`scene.prepositions.dragon.next_to`, `scene.prepositions.crocodile.next_to` (tight crop
and foreground overlap); `scene.prepositions.dinosaur.near`,
`scene.prepositions.crocodile.near` (increase gap); `scene.prepositions.fish.outside`,
`scene.prepositions.seahorse.outside` (clearer threshold separation);
`scene.prepositions.dinosaur.far_from`, `scene.prepositions.dragon.far_from` (cropped
heads and distance framing). Per-image observations are in the audit ledger.

Added focused JVM tests for all-reference phrase/case integration, compatibility,
useful opposites, complete audit membership, forged v3 compatibility rejection,
independent frozen v2 fixtures for all 52 scenes, byte-exact active/completed restore,
pending attempt/completion deduplication, fresh v3 rounds and revision-aware guided
speech. Updated exact wording/version expectations while retaining frozen v1 tests;
Compose guided Listen tests now assert complete phrase accessibility labels in EN/DE.
Tests/builds have NOT been run for this pass. No images edited; no commit or push.

## Native Maths Add Together within 5 — 2026-10-07 (JVM/build validated)

Added separate native `ADD_TOGETHER` route and 🧩 Home entry “Add Together” /
“Zusammenzählen”, with “Combine two small groups.” / “Verbinde zwei kleine Gruppen.”.
The legacy Easy Maths ➕ card remains. Activity `activity.math.add_within_5`, skill
`skill.math.addition.within_5`, context `context.math.addition` and separate
`addition-session` journal reuse the established choice session/progress framework.
Existing Maths journals and behaviour are unchanged.

AdditionContent defines the ten ordered nonzero addend pairs with totals 2–5.
Five questions cover every total, plus a different unordered pair, avoiding mirrored
facts. Ten questions cover all ten ordered pairs exactly once (total frequencies
1/2/3/4 for 2/3/4/5, as required by the finite catalogue). Seeded finite shuffling
and largest-remaining-total selection separate consecutive totals. Four unique
canonical numeric choices include the correct total once and are shuffled.
Task IDs encode canonical left/right/total names; restore checks arithmetic,
wording, choices, skill/context and round invariants.

Reuses Subitising number IDs, repository, descriptions and DotPattern renderer.
Two equally sized dot groups surround a decorative plus sign, followed by the
prompt and numeric choices. No equation or new artwork. Each group has one
First/Second group / Erste/Zweite Gruppe semantic description; individual dots
and the plus are not separate accessibility targets. Shared title, progress,
support, actions, completion and scrolling are retained.

Prompt: “How many altogether?” / “Wie viele sind es zusammen?”. Feedback:
“Yes, [n]!” / “Ja, [n]!”, “Try again.” / “Versuch es noch einmal.”. Help:
“Put both groups together in your head and count how many there are altogether.” /
“Stell dir beide Gruppen zusammen vor und zähle, wie viele es insgesamt sind.”.
AdditionAudio uses the existing controller/engine/policy. Replay speaks only the
question, without addends or answer. Opening, restore, navigation and language
sync stay silent. DurableSessionHost preserves exact state and pending progress,
with existing failed-write redelivery, deduplication and fresh Again behaviour.

Added seven JVM tests covering facts, generation/coverage, wording, audio policy,
restoration, progress/redelivery/deduplication, Again and navigation. Three
instrumentation tests cover group semantics, numeric keyboard/D-pad activation,
1.5x narrow/short layouts, help/retry/completion/Again and audio-owner silence.
Validation: all 32 focused Maths JVM tests passed (seven Addition, seven Number
Order and six each for Subitising, Quantity Match and Compare Quantity), with zero
failures/errors/skips. assembleDebug and compileDebugAndroidTestKotlin passed.
No device/emulator was connected, so instrumentation was not run. Physical
layout/dot legibility, TalkBack/D-pad and installed EN/DE voices still need review;
no physical-device acceptance is claimed.

## Native Maths Number Order 1–5 — 2026-10-06 (JVM/build validated)

Added separate native `NUMBER_ORDER` route and ↔️ Home entry “Number Order” /
“Zahlenfolge”, with “Before, after and missing numbers.” / “Vorher, nachher und
fehlende Zahlen.”. Activity `activity.math.number_order` uses the separate
`number-order-session` journal. Existing Maths sources/journals are unchanged.
Canonical number IDs and content repository are reused from Subitising.

NumberOrderContent has BEFORE/AFTER Neighbour items and MISSING items. BEFORE
anchors are 2–5; AFTER anchors are 1–4. No wraparound. Missing stores start, length
(3 or 4), and gap index, with exactly one null tile in an ascending consecutive
sequence entirely within 1–5. Task definitions use canonical number names, e.g.
`task.math.number_order.before.four` and
`task.math.number_order.missing.one.four.three`; these encode complete presentation
state without adding checkpoint fields. Restore validates the parsed item against
all saved question content, correct answer, skill and choice identities.

Seeded finite candidate sampling selects 2 BEFORE / 2 AFTER / 1 MISSING for five
questions, or 3 / 3 / 4 for ten, without duplicate task definitions. A bounded scan
prefers a different next answer whenever available. Each question has four unique
numeric choices from 1–5, independently shuffled with the same seeded random source.
ChoiceQuestion, SessionReducer, DurableSessionHost and SessionProgressRecorder retain
exact plan/order/type/sequence, language, attempts, support, completion and pending
progress delivery. Skills are `skill.math.number_order.before`, `.after`, `.missing`
under `context.math.number_sequence`. Again uses the existing fresh-session path;
no shared schema changes or new general Maths framework.

Prompts: “What comes before [n]?” / “Was kommt vor der [n]?”, “What comes after [n]?” /
“Was kommt nach der [n]?”, “Which number is missing?” / “Welche Zahl fehlt?”. Help:
“Think about the number just before/after [n].” / “Denk an die Zahl direkt vor/nach
der [n].”, and “Read the numbers in order and find the gap.” / “Lies die Zahlen der
Reihe nach und finde die Lücke.”. Feedback: “Yes, [n]!” / “Ja, [n]!”, and “Try again.” /
“Versuch es noch einmal.”. Shared UI surrounds a large anchor numeral or static
number tiles with an underscore blank. The sequence is one semantic group, e.g.
“Sequence: 1, 2, blank, 4” / “Zahlenfolge: 1, 2, Lücke, 4”; tiles are not focusable
or tappable. The quiz scrolls; shared numeric choices and completion are reused.

NumberOrderAudio is an activity owner adapter using the existing native controller
and Android speech engine/policy. Replay speaks the question, with anchor for BEFORE/
AFTER and no sequence or answer for MISSING. Opening/restoring/navigation/language
sync stay silent. There is no independent new TTS system or sequence narration.

Added seven JVM tests for boundaries, all 25 candidate items, sequence invariants,
seeded balance/choice positions, wording, audio policy/no answer leakage, exact
restoration, all three progress skills, uncertain-write redelivery/deduplication,
fresh Again and route separation. Three instrumentation tests cover all item types,
single sequence semantics, keyboard/D-pad, 1.5x narrow/short layouts, retry/help/
completion/Again and ViewModel opening/restore/language/navigation silence.
Validation: all 25 focused Maths JVM tests passed (seven Number Order and six each
for Compare Quantity, Quantity Match and Subitising; zero failures/errors/skips).
Android test compilation and assembleDebug passed. No device/emulator was connected,
so instrumentation was not run. Physical large-text/landscape and TalkBack/D-pad
review, and installed offline EN/DE speech remain open. No physical acceptance claimed.

## Native Maths More or Fewer 1–5 — 2026-10-06 (JVM/build validated)

Added native route `COMPARE_QUANTITY`, Home entry ⚖️ “More or Fewer” / “Mehr oder
weniger” (“Compare two groups.” / “Vergleiche zwei Gruppen.”), activity
`activity.math.compare_quantity` and separate `compare-quantity-session` journal.
The bounded activity reuses ChoiceQuestion, SessionReducer, DurableSessionHost and
the shared progress recorder. It uses one honest comparison skill,
`skill.math.compare.quantity`, with `context.math.quantity`: the question always asks
which side has more, rather than claiming separately assessed fewer questions.
Task IDs encode canonical number names for left/right and LEFT_MORE/RIGHT_MORE/SAME
(e.g. `task.math.compare_quantity.four.two.left_more`), validated on restore.

Generation samples finite relation-specific lists from all 25 ordered pairs, then
shuffles the selected tasks from one seed. Five questions have a 2/2/1 left-more /
right-more / same split; ten have 4/4/2. Pairs never repeat within a round, with no
rejection loops. Answer order stays Left / Right / Same (Links / Rechts / Gleich).
The exact plan and answer order, language, attempts, Help/Replay, completion and
pending progress use the shared journal unchanged. Again uses host.newRound with a
new session ID. Actual answer attempts and completion are deduplicated through the
existing progress repository; no existing Maths journal/schema was changed.

The screen calls the existing Subitising DotPattern composable twice, with equal
square sizes and identical styling, and uses the existing canonical patterns and
quantity descriptions. No dot renderer or coordinates are duplicated. Visible side
labels and spacing identify the two groups; each group replaces descendant semantics
with one side-plus-quantity label. Individual dots are not targets. The whole quiz
scrolls; shared title/progress/text choices/support/actions/completion are reused.

Prompt: “Which side has more?” / “Welche Seite hat mehr?”. Feedback: “Yes!” / “Ja!”
and “Try again.” / “Versuch es noch einmal.”. Generic, equality-safe Help: “Look at
both groups. Compare how many dots each one has.” / “Schau dir beide Gruppen an.
Vergleiche, wie viele Punkte jede Gruppe hat.”. CompareQuantityAudio is an owner
adapter over the existing DefaultAudioController/AndroidSystemSpeechEngine, with
unchanged All/Questions/Off policy and cancellation boundaries. Replay speaks only
the question, including from completion. Opening/restoring and language sync are
silent; automated speech never uses the quantity semantics. No second TTS system.

Added six JVM tests covering all pairs, labels/wording, seeded 5/10 balance/identity,
exact partial/completed restoration, attempts/Help/Replay/language, honest progress,
uncertain-write redelivery/deduplication, fresh rounds, audio policy and navigation.
Three instrumentation tests cover both semantic groups, all answer labels/actions,
keyboard/D-pad, 1.5x narrow portrait, short landscape, retry/help/completion/Again,
and real ViewModel opening/restore/navigation/language silence with a fake engine.
Validation: all 18 focused Maths JVM tests passed (six Compare Quantity, six Quantity
Match and six Subitising; zero failures/errors/skips). Android test compilation and
assembleDebug passed. No device/emulator was connected, so instrumentation was not
run. Physical dot legibility, TalkBack/D-pad traversal, landscape and installed
offline EN/DE voices remain to be validated. No physical acceptance claimed.

## Native Maths quantity matching 1–5 — 2026-10-06 (JVM/build validated)

Added a separate `QUANTITY_MATCH` native route titled “Numbers & Groups” /
“Zahlen & Mengen”, with activity `activity.math.quantity_match` and journal
`quantity-match-session`. It reuses `SubitisingContent.numbers`, `pattern()` and
`description()` directly, so canonical quantity visuals remain single-source and
Subitising behaviour is unchanged.

`QuantityMatchContent` uses one bounded `ChoiceQuestion` representation with an
explicit `Direction`: `QUANTITY_TO_NUMERAL` uses skill
`skill.math.quantity_to_numeral`; `NUMERAL_TO_QUANTITY` uses
`skill.math.numeral_to_quantity`. Five-question plans cover all five quantities with
a deterministic 3/2 direction split. Ten-question plans repeat each quantity once
with its opposite direction, producing exact 5/5 direction balance and unique
direction/quantity task definitions. Every task has four unique canonical number IDs
and deterministic answer order.

Quantity-to-numeral shows one semantic dot group and four numeric choices. Numeral-to-
quantity shows a large numeral and four independently labelled semantic dot-pattern
choice targets. Prompts are “Which number matches this group?” /
“Welche Zahl passt zu dieser Gruppe?” and “Which group shows [n]?” /
“Welche Gruppe zeigt [n]?”. Feedback and direction-specific Help use the requested
concise EN/DE wording. The existing durable session host, progress recorder,
retry/help/completion flow and native audio boundary are reused; restoration is
silent and progress skills distinguish both directions.

Validation exposed invalid numeric task-ID segments and a mismatched definition
parser; task IDs now use canonical number names and round-trip through the corrected
parser. Direction generation uses a seeded alternating first cycle and its opposite
second cycle. Restore validation checks direction balance and target coverage.
Dot answer buttons contain only the pattern, with one bilingual accessible label;
the initial visible English text answer labels were removed.

Added focused JVM tests for shared patterns, wording, generation balance,
determinism, answer uniqueness, audio language/policy and route separation, plus
exact partial/completed restore, retry/help/language, both skill identities and
uncertain attempt/completion redelivery. Compose coverage includes both directions,
single semantic dot targets, keyboard/D-pad activation, 1.5x text, narrow layout,
short-landscape retry/help and completion.

Validation: all 12 focused Quantity Match/Subitising JVM tests passed (six each,
zero failures/errors/skips); Android test compilation and assembleDebug passed.
No emulator/device was connected, so instrumentation was not run. TalkBack/D-pad
traversal, short-landscape physical review and offline speech remain to be validated.
No physical-device acceptance is claimed.

## Native Maths subitising 1–5 — 2026-10-06 (JVM/build validated)

Added a bounded native `SUBITISING` route with activity `activity.math.subitising`,
skill `skill.math.subitising.range_1_5`, and journal `subitising-session`. The
activity reuses `ChoiceQuestion`, `SessionReducer`, `DurableSessionHost`, the shared
progress repository, native title/progress/support/action/completion components and
the existing audio controller boundary. No general Maths framework or image assets
were added.

`SubitisingContent` owns five stable semantic number IDs and fixed die-style dot
coordinates: centre, diagonal pair, diagonal three, four corners, and four corners
with centre. The Compose `DotPattern` is one semantic group with bilingual labels
such as “Three dots” / “Drei Punkte”; individual dots are not exposed as targets.
Answer choices are four unique number IDs, deterministically shuffled. Five-question
rounds use each quantity once. Ten-question rounds use each twice and adjust the
second cycle boundary to avoid an adjacent repeat without rejection sampling.

The prompt is “How many?” / “Wie viele?”. Feedback is “Yes, [n]!” / “Ja, [n]!”,
“Try again.” / “Versuch es noch einmal.”, and Help is “Look at the whole group. How
many dots can you see?” / “Schau dir die ganze Gruppe an. Wie viele Punkte siehst
du?”. Replay uses the existing owner and policy and speaks only the prompt. Restore,
retry, language changes and completion use the durable session conventions; progress
events retain the distinct activity and skill IDs and deduplicate through the shared
recorder.

Added focused JVM tests for patterns, generation, wording, exact restoration, retry,
Help, completion, progress redelivery/deduplication and audio policy. Added Compose
tests for semantic dot descriptions, keyboard activation, 1.5x text, narrow layout
and short landscape reachability. Focused Subitising JVM tests passed: 6 tests, zero
failures/errors/skips. Android test compilation and `assembleDebug` passed. No
emulator/device was connected, so instrumentation was not run. TalkBack/D-pad
traversal, landscape physical acceptance and offline speech remain to be validated;
no physical-device acceptance is claimed.

## Wilma Today / Yesterday / Tomorrow — 2026-10-06 (JVM/build validated)

Added `WilmaPhase.TODAY`, activity `activity.wilma.today`, journal `wilma-today`.
`WilmaToday` is bounded content/generation over the existing SessionReducer and
DurableSessionHost, not a new state framework. It derives yesterday/tomorrow from
WilmaContent's canonical previous/next helpers; no device date or new weekday data.
A seeded shuffled seven-anchor cycle alternates relations from a seeded starting
relation: five distinct anchors with a 3/2 split for five questions; all seven anchors,
a 5/5 split and no repeated anchor/relation pair for ten. Each task has four unique,
seeded choices with the correct weekday exactly once.

Prompts: “Today is [day]. What day was yesterday?” / “Today is [day]. What day is
tomorrow?”; “Heute ist [Tag]. Welcher Tag war gestern?” / “Heute ist [Tag]. Welcher
Tag ist morgen?”. Canonical names supply both languages. Help explains moving one
day back/forward. The existing scrollable quiz screen shows an explicit Today/Heute
anchor, full-width NativeTextChoice buttons and separate named Listen controls;
shared title/progress/support/actions/completion remain in use. Existing Wilma
strips, artwork, colours, auto-follow and quiz generators are unchanged.

Existing worker-owned host/checkpoint preserves exact plan/order, language, attempts,
support and completion; restore/retry remains silent. Existing write-ahead shared
ProgressRepository delivery records actual answer attempts with weekday IDs and
`skill.weekdays.yesterday` / `skill.weekdays.tomorrow`, plus deduplicated completion.
No journal or progress schema change. Existing selection enum names still restore;
older app versions do not know the new TODAY selection (downgrade unsupported).
WilmaAudio remains the sole owner with unchanged All/Questions/Off policy.

Added JVM mapping/wrap/wording, 100-seed 5/10-round generation and bilingual audio
policy tests. Extended durable host cases to TODAY for exact partial/answered restore,
retry/help/language/completion and failed-write redelivery/deduplication. Added Compose
EN interaction/semantics/keyboard coverage and DE 1.5× text at 320dp completion coverage.
Validation: all 24 focused Wilma JVM tests passed (zero failures/errors/skips);
Android test compilation and assembleDebug passed. The initial Android test compile
exposed missing experimental keyboard-test opt-ins, now fixed. No device/emulator
was connected, so instrumentation was not run. TalkBack/D-pad traversal, landscape
and real offline speech still need device validation; no physical acceptance claimed.

## Prepositions question/stem and guided Listen — 2026-10-06 (JVM/build validated)

Scene-derived presentation now shows “Where is the snake?” / “Wo ist die Schlange?”
and “The snake is…” / “Die Schlange ist…”, using each canonical animal subject and
its existing German nominative article. `PrepositionsWording.kt` separates question,
stem and answer phrase helpers. The former screen-local answer phrase/case formatter
was moved verbatim so display and speech share reviewed scene-specific objects and
German case handling. No scene/artwork/choice/order changes. Persisted question
payloads, revisions and validation remain unchanged to preserve exact old restores.

Main active-question Replay still passes through the existing SessionReducer/host
(same replay support accounting). Its instruction effect now drives six separate
utterances through the existing PrepositionsAudio owner/controller: question, stem,
then the four displayed phrases in order. Only a Completed callback starts the next
step; terminal failure/cancellation/suppression stops the sequence. Punctuation and
utterance boundaries provide speech separation; no guessed timing or fixed-duration
highlighting. Normal automatic instruction policy remains; no guided sequence starts
on restore. Existing first-use tutorial policy is retained, with the concise question.

Individual Listen speaks only its full scene-specific answer phrase with a period,
without submitting. Choice taps still answer immediately. A transient nullable
spoken-option ID flows from audio callbacks through the retained ViewModel to a
3dp answer outline and “Being read aloud” / “Wird vorgelesen” state description.
No Selected/correctness semantics and no checkpoint changes. Existing action,
language, mode, navigation, retry-load and close boundaries clear highlighting
and invalidate late callbacks. All/Questions permit explicit Replay/Listen; Off
suppresses them and leaves no outline.

Framework limitation: the controller exposes terminal results but no start event.
The outline begins when an answer request is dispatched to a ready engine and ends
on its terminal callback. Guided answers follow a completed question, so engine
initialisation has finished. An individual Listen requested while the engine is
initialising may speak without an outline. No global audio changes or new engine.

Added nine focused JVM tests for wording, unchanged reviewed phrases, ordered speech,
languages/policy, highlighting, synchronous and stale callbacks, cancellation,
individual Listen and learning-state invariants; three Compose tests cover EN/DE
1.5× narrow layout, outline semantics, separate Listen and keyboard answer actions,
and navigation clearing. Existing retry/help/completion tests remain applicable.
Validation on 2026-10-06: all 39 focused Prepositions JVM tests passed, including
the nine new tests; Android test compilation and `assembleDebug` passed. ADB listed
no connected devices, so instrumentation was not run. No full JVM suite or lint
ran. Emulator, offline-voice cadence and physical TalkBack/D-pad acceptance remain
pending.

## Wilma bilingual weekday learning (JVM/build validated; device acceptance pending)

Added `WilmaPhase.BILINGUAL`: “German & English weekdays” / “Wochentage auf
Deutsch & Englisch”. Two vertical Wilmas use existing head, weekday segment
and tail PNGs with paired rows: Deutsch always left, English always right,
Monday–Sunday aligned. Shared row heights accommodate wrapped labels at large
text sizes; the existing screen scroll contains the full page. Day labels and
colour cues come from canonical Wilma data and `WilmaDayColours`.

Tapping a whole segment/label invokes `WilmaViewModel.bilingualDay(day, language)`
and the existing `WilmaAudio.day` owner with a manual OPTION request. All and
Questions allow explicit taps; Off suppresses them. Opening, returning and
restoring are silent. No quiz session, progress evidence/counter, Help, Retry
answer flow, Next, whole-page Replay or completion celebration is introduced.
The existing image/save recovery controls remain available for loading failures.
Each button announces its weekday and language; decorative artwork and duplicate
label semantics are excluded. Focus follows German/English pairs down the week.

The existing browsing store persists the new phase name; no journal format or
quiz/order changes. Older binaries cannot restore the new enum name after a
downgrade. Existing horizontal strips and ordering auto-follow are untouched.

Added three JVM tests (canonical pairs/colours, alternating-language audio policy,
browse restoration), three Compose tests (EN narrow 1.5×, DE short landscape 1.5×,
keyboard activation), and one isolated-owner instrumentation test (tap routing,
policy, silent restoration and absence of quiz/progress persistence).
Validation on 2026-10-05: all 21 focused Wilma JVM tests passed (including the
three new bilingual tests), Android test compilation and `assembleDebug` passed.
ADB reported no connected devices, so instrumentation was not run. No lint or
broader JVM suite ran. Emulator and physical TalkBack/D-pad/voice/large-text
acceptance remain pending.

Artwork inspection found transparent padding around the existing circular segment
PNGs. They are reused unchanged as vertical segment-and-label stacks, with upright
heads. Review the visual continuity between segments before deciding whether a
future tighter vertical artwork treatment is warranted; no new artwork was made.

## Combined completion pair correction

Confirmed on `9feb0a3`: completion entries reused the final task's selections.
Each completed task now encodes its own canonical previous/next pair as
`season.combined.<before>.<after>`. Live attempts, journal format and UI/audio/session
behaviour are unchanged. The five-question regression assertion checks every pair
and four distinct pairs rather than five copies of the final pair. Focused
`SeasonsCombinedTest` JVM tests: all 7 passed; the subsequent validation command
reused that up-to-date result and `assembleDebug` passed. No broader tests ran.

## P2 Seasons: combined before/after reasoning — 2026-10-05 (implementation complete; validation pending)

Added the bounded `SeasonsPhase.COMBINED` marker, pure deterministic generator,
ordered Before/After state model, separate `seasons-combined-session` checkpoint,
ViewModel routing, native labelled slot UI, silent restore, Help, retryable
partial answers, and completion flow. Answers derive through
`SeasonIds.previous()` and `SeasonIds.next()`; five-question rounds cover all
four anchors before repetition. EN prompt is “Which season comes before and
after [Season]?”; German uses explicit “Davor” and “Danach” slot labels with
“Welche Jahreszeit kommt vor und nach [Season]?” The existing SeasonsAudio
owner remains the audio boundary; no new TTS controller was introduced.
Hardening after commit `ea95648`: completion readiness now uses the combined
language safely; Replay speaks the current combined prompt through
`SeasonsAudio` with the existing replay policy; Again replaces the completed
checkpoint with a fresh deterministic round; Retry is a no-op for attempts and
preserves both selections. Combined Checks now emit bounded `skill.seasons.combined`
attempt evidence with an ordered `season.combined.<before>.<after>` choice ID, and final
completion emits the existing deduplicated `CompletionEvent` identity. Focused
JVM tests, `assembleDebug`, and Android test compilation passed on 2026-10-05.
`lintDebug` reached lint analysis but failed in the local Android lint service
with the opaque `25.0.3` failure; no source lint finding was reported.
Instrumentation was not run because no emulator was connected. Physical-device
acceptance, TalkBack/D-pad review, and broader progress restoration/deduplication
coverage remain pending; existing Seasons journals and modes were not changed.

## P2 Seasons: Match season to clue — 2026-10-05 (committed and pushed)

Added `SeasonsPhase.MATCH` / “Match season to clue” using the existing small
`Sorting`/`SortingHost` membership primitive. Seasons are selectable items and the
four deterministically selected clue identities from the existing eight-entry
`SeasonsClues` catalogue are categories; `OrderedPlacement` was not used because
there is no canonical placement order. The round contains all four seasons and one
clue per season, with a seeded clue-category order and exact selected/placement state.

The native interaction is tap a season, then tap its clue. Correct matches remain
placed/locked; incorrect matches retain the selected season for retry. Help gives a
season-oriented prompt without solving the pairing. Existing `NativeActivityTitle`,
shared actions/support/completion and narrow scroll layout are reused. Each clue has
an independent Listen action through `SeasonsAudio`; selection/restoration is silent.

The separate journal is `seasons-match-session`, activity `activity.seasons.match`,
revision 1, with shared Sorting progress events and completion deduplication. Existing
CLUES, Missing Season, other Seasons modes, artwork and clue wording are unchanged.
Added focused JVM and Compose tests for deterministic four-item membership, wrong/retry/
Help, exact journal restoration, EN/DE completion, and German narrow layout. After one
test-only synchronization adjustment, full JVM tests, debug/APK-test builds, lint and
the 17-test Seasons emulator suite passed. Commit `11c217f` (`feat: add seasons clue
matching`) was pushed to `origin/main`; the working tree is clean. Physical device and
bilingual child-use review remain separate acceptance work.

## P2 Seasons: observable clues — 2026-10-05 (committed and pushed)

Started clean at `29c0503`, following the committed Missing Season mode. Added
`SeasonsPhase.CLUES` (Season clues / Jahreszeiten-Rätsel), with eight fixed EN/DE
questions, two per canonical season. Short observations use blossoms/new leaves,
dense foliage, falling leaves, and snow/ice with bare branches. Source review compared
the clues with canonical narration and all four existing illustrations; independent
bilingual/child-use review remains pending. No colour-only or temperature-only clue.

`SeasonsClues` uses CandidateTaskGenerator, SessionReducer, DurableSessionHost and
shared progress. Activity `activity.seasons.clues`, revision 1, separate journal
`seasons-clues-session`; skill `skill.seasons.clues` and the existing honest
`context.seasons.lakeside_tree`. Five/ten questions have seeded clue/choice order;
all eight candidates are visited before repetition in ten-question rounds. Five-question
rounds take the first clue for each season plus the first remaining clue from that seeded
eight-clue cycle, preserving relative order and choices and assigning final task ordinals.
This guarantees all four seasons plus one distinct second clue. Ten-question generation
is unchanged. The second Spring clue now says “Blossoms are opening on the tree, and new
green leaves are growing.” German matches: “Am Baum öffnen sich Blüten, und neue grüne
Blätter wachsen.” These refinements remain part of the unbuilt/unreleased revision 1;
no journal migration is introduced. Generation changes affect new rounds only; any
earlier development checkpoint containing the old Spring wording would fail authored
content validation and must not be silently rewritten. Restore retains exact authored tasks and support,
silently. Canonical content version and existing journal formats are unchanged.
Selection stores enum names; old selections remain readable. Downgrading after selecting
CLUES is not supported by old binaries that do not know that enum name. Future clue-copy
changes need an explicit activity revision/recovery decision, not silent regeneration.

The full short clue/question is the existing prominent, wrapping instruction and speech
content. Existing title, progress, text choices, support, actions, separate option Listen
and completion are reused. No clue-mode artwork is loaded/displayed; no audio mechanism
or policy changed. Other modes, canonical artwork and Wilma are unchanged in behavior.

Added five JVM and two Compose test cases for later execution: catalogue/answers,
determinism, altered-content rejection, durable retry/help/language/completion and progress,
audio policy, EN/DE prompt/answer flow and 1.5× German text at narrow width.
Focused JVM, build, lint and emulator validation passed as part of the later Seasons
validation. The implementation was committed in `b66eeba` and pushed to `origin/main`.
Before acceptance, run focused validation
and review wording, new mode-chip wrapping, portrait/landscapes/large text, keyboard/TalkBack,
real host mode switching/recreation and EN/DE All/Questions/Off on the target devices.

## P1 visual-system checkpoint — 2026-10-04

Verified clean main at `6e9f3c6` after `2b9f675`. Shared NativeActionButton,
NativeTextChoice, NativeSupportMessage and native completion presentation are already
adopted in their established scopes; they are not new unfinished rollout tasks.

NativeQuestionProgress takes one-based current/total, ContentLanguage and Modifier.
It owns only “Question N of M” / “Frage N von M”, headlineSmall typography and natural
wrapping. Consumers: Prepositions practice, Seasons recognition/next/before,
Vocabulary FIND/NAME and Wilma quizzes. Callers retain state derivation and placement;
`progress` / `seasons-progress` tags and completion wording are preserved. Ordering
placement counts remain custom. Wilma's quiz counter changed from default Text styling
to headlineSmall; no progress bars, percentages, animation or automatic announcements.

NativeActivityTitle takes the already-authored title and optional Modifier and uses
headlineMedium. Seasons/Jahreszeiten, Vocabulary Booster/Wortschatz and
Wilma’s Week/Wilmas Woche retain their exact wording, placement and spacing. These
three title sites had no tags. Prepositions has no equivalent activity title and remains
intentionally excluded. Prompts, Replay and navigation are not combined with titles.

Image-choice sharing was inspected and deliberately deferred: Vocabulary FIND's
centred fixed-100dp artwork, hint-only label, animal semantics and fallback differ from
Seasons ordering's 4:3 artwork, always-visible label and missing-art gate. Their filled
versus outlined interaction containers also remain caller-owned. Sharing Fit alone adds
little value; a configurable wrapper is not justified yet. No image-choice implementation
was made. Wilma colour-cued illustrated/day choices, selected borders, ordering and
auto-follow stay custom; Prepositions scene artwork is passive. Separate Listen controls,
learning/audio/session behaviour and completion are unchanged by these slices.

Recorded validation before the commits: progress slice — 365/365 JVM and 12/12 focused
emulator screen tests; title slice — 365/365 JVM and 10/10 focused emulator screen tests.
Both had zero failures/skips, successful assembleDebug/assembleDebugAndroidTest and
clean diff checks. Full instrumentation and lint were not run for these slices. This
2026-10-04 documentation sync runs no tests/builds and adds no new validation claim.

Physical S24/Fire acceptance remains separate and pending for these presentations:
EN/DE portrait, both short landscapes, 1.5× font, wrapping/scroll reachability,
TalkBack/keyboard, touch targets and disabled contrast; specifically review Wilma's
larger quiz counter. Preserve earlier accepted learning-flow results without treating
them as acceptance of the new presentation. Earlier dated entries below are historical.

## Wash Hands: semantic sequence assembly — 2026-09-30

Started clean at `95f2be9`. No commit/push. Existing Sorting, Wilma, Seasons, Follow,
animal artwork and all authored existing content remain unchanged.

Inspected WilmaOrder and SeasonsOrder: both use OrderedPlacement for correct-next-item
prefixes and retry locks. That policy cannot support free arrangement/removal before Check.
Added SequenceAssembly in the existing sequencing package and reused OrderedPlacement's
seeded scramble. No duplicate correct-prefix engine or risky Wilma migration. Future
convergence must preserve their existing policies; full twelve-month bounds/progress need
an explicit later review (current shared domain supports 2–10, this consumer exactly four).

Wash Hands / Hände waschen provides water on → wet hands → soap → rinse. Available cards
are unnumbered and individually listenable. Tap/keyboard appends; Remove returns a step;
Check evaluates the full order; wrong orders remain editable. Optional Help names the first
mismatching position only. No image/drag/animation/new reward system. Shared balloons and
Again/Home, existing audio policy and lazy owner-local engine. Opening/restore/return are
silent. Step Listen is normal content access, not hint/attempt evidence. All/Questions/Off
and busy-release versus stale-speech gating follow the established owner pattern.

Activity revision 1/content 1.1 uses its own WHS1 atomic/checksummed journal with exact
presentation, construction, Check count, last checked order, support/language/revision and
pending progress. Each explicit Check records four actual position outcomes; successful
Check adds existing completion. Partial progress delivery deduplicates after restore.
No shared checkpoint/progress schema or SortingHost change.

Focused JVM: **14/14 passed, 0 failed/errors/skips** (6 SequenceAssembly, 3 content/progress,
5 host persistence). Production and instrumentation Kotlin compile passed on the first
run; only existing MainActivity VIBRATOR_SERVICE deprecation warning. Focused emulator
**9/9 passed, 0 failed/errors/skips**, first run (4 owner/audio, 1 route/recreation,
4 EN/DE portrait/both landscape 1.5× UI cases). Keyboard setup uses InputMode.Keyboard,
asserts focus and exactly one append. Wrong Check→Hint→remove/rebuild→completion passed.
`git diff --check` and new-file whitespace checks passed. No full JVM, full instrumentation,
lint or separate APK assembly run under the owner's bounded-validation instruction.
The debug/test APKs were packaged as part of focused instrumentation. No failed runs.

Commands run:
```sh
export JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home'
export ANDROID_HOME='/Users/paulbell/Library/Android/sdk'
export ANDROID_SERIAL=emulator-5554
./gradlew testDebugUnitTest --tests '*SequenceAssemblyTest' --tests '*WashHands*' compileDebugAndroidTestKotlin --console=plain
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=com.bellfamily.bastischool.ui.washhands --console=plain
git diff --check
```

Local broad validation still required:
```sh
./gradlew testDebugUnitTest --console=plain
./gradlew connectedDebugAndroidTest --console=plain
./gradlew assembleDebug --console=plain
./gradlew lintDebug --console=plain
```

S24: audition EN/DE instruction and each independent step Listen in All/Questions/Off;
verify no auto-speech on opening/return, stale cancellation, rapid Options/Home during work,
keyboard/D-pad/TalkBack labels and exactly-one placement, portrait/both landscapes/1.5×
scrolling, incorrect Check→Help→remove/rebuild→correct, balloons/Again/Home, and true
process-restored partial/support state. No physical acceptance claimed. Text-only starter
requires working audio/adult support for a non-reader; it is not a complete hygiene routine.

## Animal Groups keyboard-test correction — 2026-09-29

Preserved the existing uncommitted Animal Groups implementation. The owner-reported
11-test run had 7 passes and 4 Selected=true failures. Each failure's semantics tree
identified the correct animal Button, with Selected=false and Focused=false. The test
requested focus without first leaving Android touch input mode. Existing Vocabulary
artwork and shared-button keyboard tests explicitly request InputMode.Keyboard.

Applied that same setup to AnimalGroupsScreenTest, asserted RequestFocus succeeds and
assertIsFocused before injecting Enter, retained assertIsSelected, and added state plus
exactly-one SortAction.Select callback assertions. Wrong→Help→correct, touch-based
remaining placements and completion assertions are unchanged. No production code,
semantics, input handler, business logic, artwork, busy/epoch handling or Colour Sort
behaviour changed; no custom key handler or duplicate selection path was needed.

Validation: focused Animal Groups instrumentation **11/11 passed, 0 failed/errors/skips**
on emulator-5554 (EN/DE, portrait/both landscapes, 1.5×). This includes owner busy/audio,
restoration, image fallback and completion coverage. `git diff --check` and changed-test
whitespace check passed. JVM was not rerun: this correction changes only test setup,
not production/domain behaviour. No full suites or lint. No commit or push.

Command:
```sh
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/paulbell/Library/Android/sdk' ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=com.bellfamily.bastischool.ui.animalgroups --console=plain
```

S24 acceptance still requires real keyboard/D-pad and TalkBack focus/activation,
selection outline, touch correction/help/completion, EN/DE and large-font orientations.
Automated keyboard injection is not a physical-keyboard or TalkBack acceptance claim.

## Animal Groups: second sorting consumer — 2026-09-29

Started clean at `31dde95` (Colour Sort committed), with Follow busy-state fix `9c5e406`.
No commit/push. No artwork or Follow behaviour changes.

Implemented four canonical animals: whale/dolphin → Water, horse/rabbit → Land.
Shared Sorting.kt is unchanged. Extracted the existing Colour Sort durable journal to
SortingHost/SortingContent, preserving CSR1 bytes and all existing colour behaviour.
Animal Groups adds content/evidence mapping, a thin durable host, owner-local image/audio
owner and native screen/Home route. AGR1 activity revision 1/content 1.1 uses the shared
progress codec, not a new progress system. Explicit tap selection/category placement,
optional EN/DE Help, immediate correction, exact partial restoration and shared balloons.
No drag, new artwork, full curriculum or future activity implementation.

Opening is deliberately silent and creates no TTS engine, even with Sound On. Explicit
speech actions use the existing lazy controller. Busy releases before any epoch-gated
speech side effect; tests specifically change visibility/language during pending work.
Canonical PNGs are sampled on a worker with a four-image owner-local cache and Fit display;
missing/corrupt images remain passive. Water/Land cues and neutral selection outlines do
not require colour recognition. Actual child recognition of cues requires physical review.

Validation budget follows the owner's instruction: no full JVM, instrumentation matrix,
full instrumentation, lint or APK assembly in this session. Focused run selected 22 JVM
cases: 10 new Animal Groups, 1 frozen Colour Sort compatibility fixture, 6 existing
Sorting and 5 existing Colour Sort host cases. First compile found test-only missing
SessionId imports and unsupported JVM ImageIO; corrected imports, left actual decoding
in the Android test and used path/signature checks in JVM. Next run: 21/22 passed; one
fixture used an invalid semantic ID before reaching the reducer. Corrected it to a
well-formed unknown ID and reran only AnimalGroupsTest: **4/4 passed, 0 failures/errors/skips**.
All 22 selected cases therefore have passing evidence across the focused runs.
`git diff --check` and new-file whitespace checks passed.
Production and Android instrumentation test sources compiled successfully; the only
reported production compile warning was the existing VIBRATOR_SERVICE deprecation.
Instrumentation tests are written/compiled, NOT executed: 4 EN/DE portrait/landscape
1.5× keyboard/tap cases, 4 owner/audio cases, 2 artwork/fallback cases and 1 native
route/recreation/language case. Do not treat these as executed physical/layout acceptance.

Local commands from repository root:
```sh
export JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home'
export ANDROID_HOME='/Users/paulbell/Library/Android/sdk'
export ANDROID_SERIAL=emulator-5554
./gradlew testDebugUnitTest --tests '*AnimalGroups*' --tests '*ColourSort*' --tests '*Sorting*' --console=plain
./gradlew testDebugUnitTest --console=plain
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=com.bellfamily.bastischool.ui.animalgroups --console=plain
./gradlew connectedDebugAndroidTest --console=plain
./gradlew assembleDebug --console=plain
./gradlew lintDebug --console=plain
git diff --check
```

S24: verify full animal visibility/Fit, Water/Land cue understanding without reading,
selected outline/TalkBack/keyboard, wrong→Help→correct, placement and completion balloons,
portrait/both short landscapes/1.5×, Sound Off/Questions/All in EN/DE, quick Options/Home/
background during work (never stuck busy), process-restored order/selection/help/placements.
No physical acceptance claimed. Historical full-suite timeout from Colour Sort remains
historical evidence, not a validation result for this new slice.

## Colour Sort: first native sorting consumer — 2026-09-29

Started from clean main `bdd939b`. This work is uncommitted; no push or physical-device
installation. Follow and canonical artwork are unchanged.

The pure `learning/sorting` primitive owns semantic membership, selection, placement,
attempt/support state and completion. It knows no colours, shapes, coordinates, audio or
storage. Colour Sort supplies two clearly distinct categories (red/blue) and four native
pictorial objects (one ball and one block in each colour). CoreContent colour names are
reused; no generated or recoloured artwork. This is a fixed four-item set, independent
of the shell's 5/10 quiz-round preference, not the full Colours curriculum.

Interaction is tap an object, then tap a colour group; keyboard activation uses the same
placement operation. Drag is intentionally deferred. Selection has an explicit border
and semantic state; wrong placement retains the selected object for immediate correction.
Optional Help names its category without placing it. Correct objects move into groups.
No timers, lives, punitive marks, score display or automatic solving.

The seeded tray order and exact partial state are durably journalled using the existing
ordering-host pattern: atomic writes, checksum, stale-writer protection, pending shared
progress events and idempotent delivery. Activity revision 1/content version 1.1 are new
Colour Sort identities; no shared checkpoint/progress schema changed. Only placements
produce attempts; Replay/Help record support without progress events. Completion requires
all four correct placements and uses shared balloons/actions. Home suspends the round;
re-entry restores it; Again starts a fresh set. Restores and background returns are silent.
Owner-local speech uses the existing lazy controller, EN/DE and Sound Off policy.

Validation so far: focused sorting/Colour Sort JVM **14/14**, full JVM **340/340**;
focused Colour Sort emulator **7/7**, all final runs zero failures/errors/skips.
assembleDebug passed; lintDebug **0 errors, 3 existing warnings, 2 informational findings**.
Full emulator regression: **104/133 completed: 103 passed, 1 failed, 0 skipped;
29 not run**. Android aborted with `keyDispatchingTimedOut` during the existing
TellMeRouteTest after long emulator delays. The same test passed **1/1** when rerun
independently. This supports an environment/timing issue but is not a full-suite pass;
a complete healthy-emulator regression remains outstanding. No unrelated test or
production code was altered to mask the failure. `git diff --check` and new-file
whitespace checks passed.
The first instrumentation compile needed the experimental keyboard-test API opt-in.
The next run was 6/7: the owner test observed a pending main-thread publication; its
synchronization and journal-fixture cleanup were corrected. Final focused rerun passed
without changing production behaviour or weakening assertions.

Commands (repository root):
```sh
export JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home'
export ANDROID_HOME='/Users/paulbell/Library/Android/sdk'
export ANDROID_SERIAL=emulator-5554
./gradlew testDebugUnitTest --tests '*Sorting*' --tests '*ColourSort*' --console=plain
./gradlew testDebugUnitTest assembleDebug lintDebug --console=plain
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=com.bellfamily.bastischool.ui.coloursort --console=plain
./gradlew connectedDebugAndroidTest --console=plain
git diff --check
```

Focused emulator coverage includes EN/DE portrait, both German short landscapes at
1.5× text, keyboard/tap completion, wrong→Help→correct, shared balloons, Home/Again,
actual shell recreation/language change, Sound Off and cold/lazy speech cancellation.
JVM coverage includes non-colour membership, stale/duplicate actions, corrupted state,
partial restore, pending-progress retry/deduplication and independent/supported evidence.

Manual emulator EN portrait inspection found all four objects, both groups and actions
visible without overlap/clipping (`/private/tmp/basti-sort-review-en.png`, local evidence
only). German/short-landscape/1.5× reachability is covered by focused instrumentation;
this is not physical visual acceptance.

S24 acceptance remains required: colour clarity, understandable group destinations and
selected state, tap/keyboard/TalkBack focus and spoken labels, portrait/both landscapes
at 1.5×, immediate self-correction, shared balloons, offline EN/DE speech/Sound Off,
Home/Options/background/recreation and process-restored partial placement. No physical
acceptance is claimed. Future Compare & Discover, Maths, Practical Life/School Skills
and Story Adventures can supply semantic categories/items; none is implemented here.

## Follow physical-acceptance corrections — 2026-09-29

Started clean at 4ccff04. No artwork, wording, shared session/audio/progress, completion
or navigation changes. No commit/push or physical-device installation.

Confirmed covers were empty opaque Material Buttons painted after the image with
matchParentSize, only while answers were enabled. There is no memory/reveal exercise:
the intended task is visible one-step animal recognition/listening. Replaced the overlay
with one labeled clickable image container and an explicit 150dp slot. Fit/ripple and
keyboard activation remain; missing art is passive, answered/retry states disable taps.

Confirmed immediate repetition: CandidateTaskGenerator shuffles unique definitions,
not unique targets; different four-animal contexts can request the same animal. The
Follow-local seeded finite selector now skips the previous target without discarding
candidates. New rounds revision 3/content 1.3; v1/v2 saved plans remain exact, including
old repeats. Retry/Replay and returning to an existing task can still repeat the current
instruction intentionally. No shared generator or checkpoint schema change.

Tests cover original-generator reproduction, 2,002 new seeded 5/10 rounds with no
adjacent targets, deterministic complete snapshots, all-target reachability, v1/v2/v3
restoration with retry/support and Again upgrade, visible image pixels, one labeled
interactive target, bounded size, missing-art passivity, EN/DE portrait/both landscape
at 1.5× text and unchanged wrong/retry/correct/Next flow.

Validation: focused `*Follow*` JVM filter **8/8** (7 Follow tests plus one existing
Seasons method matched by the filter); full JVM **326/326**; focused Follow emulator
**7/7**, all final runs zero failures/errors/skips. assembleDebug passed; lintDebug
0 errors / 3 existing warnings / 2 informational findings; git diff --check passed.
First instrumentation attempt: 3 passed / 4 failed, solely the new fallback-child tag
lookup in merged semantics. Corrected to useUnmergedTree for that diagnostic child;
the single labeled interactive target assertions remain intact. Rerun 7/7 passed.
No production fix or existing assertion was weakened in response. ADB initially needed
host execution because sandbox sockets were denied. Full app instrumentation was not
rerun: changes are Follow-local, with its complete focused package and full JVM tested.
Canonical artwork and every other asset remain unchanged (empty asset diff).

Commands (repository root):
```sh
export JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home'
export ANDROID_HOME='/Users/paulbell/Library/Android/sdk'
export ANDROID_SERIAL=emulator-5554
./gradlew testDebugUnitTest --tests '*Follow*' --console=plain
./gradlew testDebugUnitTest assembleDebug lintDebug --console=plain
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=com.bellfamily.bastischool.ui.followinstructions --console=plain
git diff --check
```

S24 checks remain: four animals visible before any tap in EN/DE; portrait/both short
landscapes and 1.5× text; correct labels/TalkBack/keyboard focus and ripple; wrong→Retry→
correct→Next; Replay/audio cancellation; resumed old round unchanged; newly started
5/10 rounds without adjacent targets. Old saved repeats are intentionally preserved,
and the first target of a separate new round is not constrained by the prior round.
No commit or push. No new work started.


## Final resumption: owner Park 9 replacement and completed validation

The previously running full emulator suite completed **122/122**, zero failures/errors/skips,
BUILD SUCCESSFUL in 22m 14s. It was not rerun. This result predates the owner's Park 9
replacement; focused replacement decode validation is recorded separately below.

The owner supplied the replacement `park_009_turn_taking.png`. It is preserved unchanged:
PNG RGB, 1448×1086, SHA-256 `e4ec416f5c06ac6e25a4342b3b0430400516061c3f8a247f990ee6eda8915a78`.
Desktop visual review shows two seated children facing each other, coherent seesaw
beam/pivot/handles, and one standing pink-clothed waiting child. Existing “Who is waiting?” /
“Wer wartet?” is retained. Park 9 is now PASS at desktop review, pending S24 acceptance.
The original BOTH finding and hash remain in AUDIT.json prior_review; original contact
sheets remain BEFORE evidence. Current classifications: 58 PASS, 23 PROMPT_FIX corrected,
0 ARTWORK_FIX/BOTH/REVIEW_REQUIRED. This does not replace physical acceptance.

Only this owner-supplied Scene Description PNG changed; the other 80 remain byte-identical.
Combined with the 21 Prepositions crops, 22 of 178 packaged rasters differ from HEAD;
156 remain unchanged. No new image was generated or edited by the agent in this resumption.
The ASCII tellme-completion-de.png extraction is invalid and is NOT visual evidence.
Completion interaction is covered by the passing focused and full instrumentation tests.

### Final replacement validation

After the owner replacement: Python **36/36**, generator freshness passed, raw asset
hygiene **175 scanned / 174 CLEAN / 1 existing ambiguous mouse / 0 errors or strong
signatures**. Targeted Android `TellMeArtworkTest#allApprovedScenesIncludingCroppedPanelsDecode`
**1/1 passed**, decoding all 81 current images; APK packaging succeeded (50s run).
The owner's Park 9 SHA-256 was rechecked unchanged after validation; other 80 Scene
Description PNGs match HEAD. No full suite rerun was necessary for this image-only update.
Earlier completed code validation: focused JVM **69/69**, full JVM **323/323**, focused
instrumentation **22/22**, full instrumentation **122/122**, all zero failures/errors/skips;
assembleDebug passed; lint **0 errors, 3 existing warnings, 2 informational findings**.
The full suite predates the owner image replacement. Final diff/whitespace check passed.
No valid completion screenshot was recovered; no visual claim relies on the ASCII file.
No commit, push, checkout, reset, clean, pull or physical-device installation was performed.

Replacement-specific commands (same JAVA_HOME / ANDROID_HOME / emulator-5554 as above):

```sh
/private/tmp/basti-ci-deps-validation/bin/python -m unittest discover -s scripts -p 'test_*.py'
/private/tmp/basti-ci-deps-validation/bin/python scripts/generate_scene_descriptions.py --check
/private/tmp/basti-ci-deps-validation/bin/python scripts/audit_asset_hygiene.py --output docs/asset-hygiene/ASSET_AUDIT_AFTER.csv
./gradlew connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.bellfamily.bastischool.ui.tellme.TellMeArtworkTest#allApprovedScenesIncludingCroppedPanelsDecode' --console=plain
git diff --check
```

Earlier checkpoint (before owner replacement):


## Physical acceptance corrections — 2026-09-29

Work remains uncommitted; no push or physical-device installation. This checkpoint
supersedes the current acceptance claims below without rewriting their history.

Prepositions: preserved and reverified the preceding 21 lossless crops (six strong
separator/sliver cases plus 15 individually reviewed gutters). Exact crops, dimensions
and hashes remain in docs/asset-hygiene/REPAIRS.json and REPAIR_REPORT.md. All retained
pixels match the baseline crop; 157 other packaged rasters remain byte-identical.
Raw audit without review overrides passes: 175 scoped images, 174 CLEAN, one existing
ambiguous mouse image, zero strong signatures/errors. All 52 Prepositions are clean.
The fixed-size rule was validation-only; PNG RGB/RGBA, minimum 1280×960 and 4:3 ±5%
replace exact 1448×1086. Fit, semantic IDs, paths, revision 2/content 1.2 and v1 restore
remain unchanged. No Prepositions production Kotlin or learning behavior changed.

Tell Me: reviewed all 81 actual production images against selected EN/DE runtime copy.
Baseline classifications: 57 PASS, 23 PROMPT_FIX (corrected), one BOTH (Park 9, open),
zero ARTWORK_FIX-only / REVIEW_REQUIRED. Full evidence, all non-PASS decisions and
all 245 normalized field replacements are in docs/tellme-visual-qa/REVIEW.md,
AUDIT.json and CONTENT_CHANGES.json. Contact sheets preserve before copy.
All 81 PNGs remain unchanged. Repository content revision is now 1.4, schema 1.
Jungle 4 / Woodland 5 missing-home objectives were explicitly narrowed to visible
locations/actions/groups, not silently defended using generation intent.

Confirmed fixes: Ocean 6 asks what the turtle is eating; Ocean 8 locates the crab in
the cave; Jungle 4 asks where the awake sloth hangs; Jungle 5 asks what the elephant
does with its trunk; Classroom 3 specifies the blue backpack. EN/DE and linked
support/models are aligned. Shared model heading: “You could say:” / “Du könntest
sagen:”. Park 9 still needs reviewed replacement artwork with coherent seesaw geometry
and exactly one waiting child. Its production image/copy are deliberately unchanged;
a wording patch would not solve the physical defect. Do not call this fully accepted.

Tell Me previously never rendered the shared balloon component. COMPLETE now supplies
a completion-only owner ID to NativeCompletionCelebration and the existing guarded
shell pop-sound path. Non-final scenes cannot trigger it. Repeat/category/Home remain
available without popping; sound/lifecycle cancellation uses existing policy. No quiz,
score, progress, microphone, TTS ownership or checkpoint changes were introduced.

Validation so far: Python 36/36; focused JVM 69/69; full JVM 323/323; focused emulator
22/22 (Tell Me, Prepositions, shared celebration), all zero failures/errors/skips.
assembleDebug passed; lintDebug: 0 errors, 3 existing warnings, 2 informational findings.
Generator check and two regenerations were byte-identical. Full emulator result will
be recorded below when complete. Diff whitespace check passes.

Initial attempts: sandbox socket denial required normal host Gradle execution; an
implicit Compose maxHeight receiver compile error was corrected by capturing the outer
constraint; one old German test expectation still said “unter dem Stein” and was
updated narrowly to the corrected “in der Höhle”. Final focused runs passed; no test
was weakened or unrelated production behavior changed.

Physical checks remain: S24 corrected prompts and relations, blue backpack/cave,
final shared balloons (including Sound Off), portrait/both landscapes and large font;
Fire acceptance separately. Desktop visual review and green automation do not prove
physical acceptance. Stop here; do not start the next feature.


## Prepositions asset hygiene closure — 2026-09-28

Started from clean `fbc7082`, matching origin/main with green Actions run 36378511734.
Reconfirmed exact 1448×1086 was validation-only: the existing sampled loader accepts
variable dimensions and Compose uses Fit in a 4:3 frame. Replaced the exact-size rule
with readable RGB/RGBA PNG, minimum 1280×960, aspect ratio 4:3 ±5% inclusive. The
largest reviewed width crop is 4.49%; manifest entries still pin each image's actual
size/mode/hash. No production Kotlin, semantic IDs/paths, wording, activity revision 2,
content 1.2, v1/content-1.1 recovery, checkpoint/journal or progress changes.

Repaired all six confirmed separators and individually reviewed/repaired all 15
cosmetic gutters. 21 PNGs changed by crop only; retained pixels match baseline exactly.
31 other Prepositions PNGs and all other asset families remain byte-identical: 157 of
178 packaged rasters unchanged in this pass. No padding/resampling/generated art.
All 21 deferred Prepositions hygiene cases are closed; zero cosmetic candidates remain
retained/deferred. Mouse is still ambiguous, outside this task. Existing original body
truncations at untouched image edges were neither worsened nor reconstructed.

Evidence and exact crops/dimensions/hashes are in the second-pass section of
[REPAIR_REPORT](docs/asset-hygiene/REPAIR_REPORT.md), appended REPAIRS.json records and
new after/PREPOSITIONS_2026_09_28 contact sheets. Original BEFORE and first-pass repair
history/contact sheets are preserved. Current validator exits 0: 174 clean, one ambiguous,
zero strong signatures/errors among 175 scoped images; all 52 Prepositions decode cleanly.

Validation: Python 35/35, generator current, focused Prepositions JVM 30/30,
full JVM 321/321, focused emulator 10/10; build passed; lint 0 errors, 3 existing warnings,
2 informational findings. Full emulator 122/122 passed, zero failures/errors/skips.
The full run spanned the session interruption and reported 8h 15m elapsed; it was not
restarted or weakened. Diff/new-text whitespace checks passed. No failed test runs. No physical S24/Fire acceptance. Owner checks remain image clarity,
Fit margins and recognition on both devices. No commit or push; no next task started.

## Asset hygiene pass — 2026-09-27

Started from clean `856303b` after Follow v1.1. Reviewed 42 edge candidates among
175 scoped rasters (178 packaged files including 3 excluded authoring references).
Repaired dog/horse top separator remnants on unchanged RGBA canvases and cropped
18 Scene Description panels/gutters without resampling. All retained pixels match
baseline exactly; 155 other scoped assets plus 3 references remain byte-identical.
No production Kotlin, content wording, manifests, session/audio/navigation or signing changes.

21 Prepositions candidates are deliberately unchanged: their fixed 1448×1086 RGB
contract prevents crop-only repair, and scenic padding would invent content. Mouse
remains ambiguous. The conservative validator intentionally still exits 1 for six
strong deferred Prepositions signatures; this is not a fully clean library claim.
See [repair report](docs/asset-hygiene/REPAIR_REPORT.md) for exact paths, crops,
hashes, dimensions, classifications, before/after sheets, commands and deferrals.

Validation: validator tests 6/6; Scene Description Python 22/22; generator current;
focused JVM 84/84; full JVM 321/321; focused emulator rendering/decode 22/22, no
failures/skips. assembleDebug passed; lint 0 errors / 3 existing warnings / 2 info.
Diff/whitespace checks passed. Initial Gradle/ADB sandbox socket errors were resolved
by approved host execution; no test assertions failed. No physical S24/Fire acceptance.
Owner authorized committing/pushing this pass. No next feature started. Owner review should cover
repaired image clarity/Fit on S24/Fire and decide how to handle Prepositions dimensions.

## Previous checkpoint — Vocabulary canonical artwork

Started clean `522f671` after Tell Me v1 was committed and pushed. Vocabulary's six
animals now use their existing canonical PNGs in Explore, FIND picture answers and NAME
prompt images. A small explicit presentation map resolves semantic IDs; the existing
ViewModel worker loads/cache-bounds the six images once on first visibility (384px max).
Compose uses Fit with the existing localized image label, answer tags and separate Listen.
Missing/corrupt assets display passive EN/DE fallback text, without changing session state.

No authored content, artwork bytes, answer ordering, scoring, support, speech, checkpoints,
progress or signing/distribution changes. Quiz versions and the historical context ID
remain stable for exact saved-round compatibility. The old migration glyph model remains
unused by the live renderer; no generic image-choice framework was introduced.

Validation: focused Vocabulary JVM 36/36; full JVM 317/317; focused emulator 14/14; full
emulator 117/117 with zero failures/skips. Build/lint passed (0 errors, 3 existing warnings,
2 informational findings); diff checks passed. No physical acceptance.
Owner checks remain animal recognition with Basti, portrait/both landscapes, large text,
TalkBack, image clarity and signed in-place upgrade/resume on S24 and Fire Max.
Changes are uncommitted; do not commit/push or start another slice automatically.

## Previous checkpoint — native Tell Me v1

Started clean `6deabf6` after the committed Prepositions expansion. Native Home now opens
Tell Me / Erzähl mal category selection. The sole content source is the unchanged bundled
81-scene repository (content 1.3): nine categories × nine ordered pictures. Dedicated
in-memory state supports TALK → MODEL → Next, bounded authored Help, collapsed adult
support and score-free completion/Again/another category. No quiz host, grading, speech,
microphone, progress events or new persistence. Options/language/configuration retain
state; Home resets it and a new process starts at category selection.

All scenes have an authored prompt; only 27 have the requested Help/model fields. For the
other 54 those optional elements are omitted. No wording is manufactured. The worker-loaded
single-scene sampled image cache preserves the complete composition with Fit; title is the
minimal image label because no authored alt exists. Source metadata, all canonical images,
other activities and signing/browser/audio/schema contracts are unchanged.

Validation: focused JVM **27/27**, final focused emulator **7/7**, full JVM **315/315**,
final full emulator **111/111**, Python **22/22**, generator freshness current. Build/lint
passed (0 errors, 3 existing warnings, 2 informational findings); tracked/new-file whitespace
checks passed. BUILD_NOTES.md records exact commands and all initial failures/corrections.
No S24/Fire physical acceptance. Check EN/DE readability/content selection, real image clarity, both landscape
orientations, larger support/system text, TalkBack and keyboard order, insets, Options,
background/configuration return, Home reset and deliberate process-death reset. No need to
reopen accepted celebration art or unrelated activity behavior. This completed slice is
ready for the owner-requested commit/push; do not start another slice automatically.

## Previous checkpoint — Prepositions artwork/content v2

Based on clean `57cf7b1`. Native Prepositions now uses the exact 52 reviewed PNG scenes
and 13 relations through an explicit non-Cartesian typed catalogue. New EN/DE strings
are the requested authored values; no grammatical substitution was necessary. New
rounds are activity revision 2/content 1.2, still seeded distinct 5/10 scenes and four
choices, with spatial contrasts and competing-variant exclusions. All PNG hashes are
unchanged. Native answer/action/listen/support/completion behavior remains intact.

Compatible v1/1.1 journals restore byte-exact tasks/choices/versions/state against the
old six-relation/24-scene contract, including pending progress and completed rounds.
Play Again starts v2. Shared journal/checkpoint schema stays 1; only Prepositions supplies
a compatible decoder through the host's new optional restorer argument. Bad journals
remain preserved. No reducer/progress/audio rewrite or history fabrication.

Artwork decodes off main via a one-scene sampled bitmap cache; Compose gets the bitmap
through one shell binding and displays uncropped 4:3 Fit with the old scene tag/description.
Calm failure text leaves learning state alone and keeps previous-version access available.
Focused JVM **29/29**, full JVM **302/302**, and focused emulator **9/9** passed.
Full emulator **104/104** passed; assembleDebug/lintDebug passed (0 lint errors,
3 existing warnings, 2 informational findings); diff/new-file whitespace checks passed.
BUILD_NOTES records exact commands, v1 coverage and the corrected test-bounds assertion.

No S24/Fire acceptance claim. Recheck new artwork/distinctions, EN/DE audio and semantics,
large-font/orientation usability, lifecycle and real same-key upgrade with a v1 round.
Work is uncommitted; do not commit/push or start another slice without instruction.

## Previous checkpoint — lazy native TTS implemented

Continued from `d94d581`, preserving uncommitted audit docs. All four native ViewModels
now pass owner-local engine factories to DefaultAudioController. Only the first current,
policy-eligible, nonempty speech request constructs Android TTS; OFF/Home/settings/
readiness/unused close do not. The first request waits through initialization without
another tap. Existing cancellation/epochs, offline EN/DE selection and QUEUE_FLUSH are
unchanged. Created engines are reused and closed with their ViewModel; no sharing,
service or singleton. The legacy MainActivity/WebView path remains entirely unchanged.

78 focused JVM and 8 actual-ViewModel instrumentation cases passed; full JVM 292/292.
Full emulator instrumentation 98/98 passed; assembleDebug and lintDebug passed
(0 errors, 3 existing warnings, 2 informational findings). Diff/whitespace checks passed;
BUILD_NOTES records commands/results. No test retry was needed.
No UI/content/session/progress/artwork/signing/browser changes. No physical S24/Fire
acceptance; remaining checks are cold-start voice latency, cancellation while initializing,
OFF/EN/DE/missing voices and actual platform resource release. User-visible wording,
policy and flow are unchanged, but first speech may now wait for cold initialization.

Audit and lazy-init work are included together in the commit “Audit and lazily initialize native TTS”.
Final review found no code defect or unrelated changes. Do not start another slice automatically.

## Previous checkpoint — native TTS ownership audit complete

2026-09-26: audited clean `d94d581` without production/test changes. MainActivity
constructs four native Activity-scoped ViewModels eagerly; each owns its own shared
controller/system-TTS adapter. The shell also owns LegacySpeech/TTS: **five clients
per normal shell**, even Home/OFF, not five proven vendor processes. Recomposition
and route changes add no engines. Navigation/background stops owned speech; native
engines close on ViewModel clearance, legacy on Activity destruction. Exact control
inventory, queue/language handling, safe paths and potential legacy error-handling
gaps are documented in NATIVE_ARCHITECTURE_SPEC's observed audit section.

No confirmed playback/leak defect found. Eager allocation is confirmed unnecessary
resource use, not proof of overlap. All native Listen controls execute through shared
policy/engine code via their ViewModel; UI owns callbacks only. The legacy bridge is
an intentional separate compatibility path. No policy/session/progress changes.

Fresh validation: **65/65 focused JVM audio tests passed**, zero failures/errors/skips;
`git diff --check` passed. Reused unchanged `d94d581` baseline evidence: 279 JVM,
90 full emulator (12 focused) green, build/lint passed (0 errors / 3 warnings / 2 info).
No full-suite rerun, emulator/device use or physical audio acceptance in this audit.
Four documentation files only; uncommitted, no push.

Actionable next slice, separately scoped: lazy native engine creation within existing
owners, with first-use/readiness/cancellation/close tests. Do not introduce a singleton
or service solely for tidiness. Vendor stop/voice behavior and actual resource release
still need S24/Fire audio/lifecycle checks; preserve the stable physical installations.
Do not begin this follow-up automatically.

## Previous checkpoint — Vocabulary NAME shared text choices

Started clean main `98c62fe`. Vocabulary NAME / “What Is It?” answers now use the
existing NativeTextChoice with the same labels, ContentIds/order, callbacks, tags,
readiness/locking gates, fill width and 72dp minimum. FIND glyph choices, hint labels,
separate Listen controls and all learning/session/progress/audio behavior are unchanged.
No new component/API or correctness states; no content/artwork/MainActivity changes.

Focused emulator tests: **12/12 passed**; full JVM **279/279 passed**. Debug build
and lint passed: **0 errors / 3 existing warnings / 2 informational findings**.
Full emulator instrumentation: **90/90 passed**, zero failures/errors/skips; no
flake/retry. Final diff and new-file whitespace checks passed. BUILD_NOTES
records exact commands and layout/interaction coverage. No physical S24/Fire acceptance
claimed; no browser source change or browser tests. Work is uncommitted; no push.

This completes the bounded Vocabulary text-choice adoption. Next separately scoped
cleanup remains the native TTS ownership audit before adding another eager platform
engine. Do not start it automatically. Image choices and Wilma's colour cues remain
custom; other visual-system and physical acceptance work remains open.

## Previous checkpoint — targeted Scene Description review complete

2026-09-25: started clean main `8e00cea`, the committed/pushed bilingual-content
slice. Direct PNG review resolved Park .07 climbing and Mountains .07 uphill wording:
seven English/German pairs now describe standing on the rope bridge and a neutral
journey along the path. See `SceneDescriptions/GERMAN_REVIEW.md` for every old/new
phrase. Other authored values, IDs, ordering, references and all images are unchanged.
Recurring German support-language review found no additional clear material correction.
All 81 scenes retain complete runtime EN/DE. Schema 1, **content revision 3**; generated
Kotlin refreshed deterministically. No consumer/UI/session/progress/audio work added.

Validation: **22 Python / 14 focused JVM / 279 full JVM passed**, no failures/errors
or skips. assembleDebug and lintDebug passed; lint **0 errors, 3 existing warnings,
2 informational findings**. Two regeneration/check cycles were byte-identical;
all 91 source files compared against HEAD showed only 14 intended scalar edits.
`git diff --check` passed. Exact commands and the initial sandbox socket denial/
successful permitted Gradle rerun are recorded in BUILD_NOTES.md. No device runs.

These two wording concerns are closed for open-ended description. Model sentences
are examples, never an automatic answer key; Farm count and narrative/cropped-panel
caveats remain. This is not new all-image acceptance or independent native-speaker
review. No UI/navigation/session/progress/audio/image/signing/browser changes.

This targeted-review work is committed and pushed on main as `fb8ff15`
(`fix: resolve scene description ambiguities`). Before the first Tell Me consumer,
finish the small remaining shared-native cleanup: Vocabulary's text-only What Is It?
choice adoption and a bounded native TTS ownership audit so another eager platform
engine is not added blindly. Tell Me remains read-only/open-ended in its first slice,
with no automatic grading or speech input. Existing S24/Fire acceptance is unchanged.

## Previous checkpoint — all Scene Description runtime text bilingual

2026-09-25: started clean main `296d6ef`. Added **1,698 German values to all 54 Wave
2/3 scenes**, plus the existing Farm Wave 1 review caution in German (1,699 total).
All 81 production scenes now have EN/DE for every present runtime teaching/support
field. Existing bilingual pair/list shapes are reused; no third schema. Generator
requires both locales; domain missing-language handling remains null, never fallback.
Scene content revision is 2, schema remains 1. Existing activity behavior is unchanged.

Verified Park 005's PNG has four yellow buckets. Exactly two English count phrases
were corrected from three to four and explicitly covered by the preservation test.
Everything else in the English catalogue, identity/order and source technical fields
is preserved. No manifest, PNG, authoring prompt/reference, UI, session, progress,
audio, navigation or signing/distribution changes.

Read `app/src/main/assets/SceneDescriptions/GERMAN_REVIEW.md` for per-category counts,
normalization, wording decisions, exact English exception, metadata file list and
limited ten-image spot-check. Park 007 climbing and Mountains 007 uphill direction
are not unambiguous in still images; owner/content review remains necessary before
constrained use. Existing Farm .01 chicken-count caution is retained. Bilingual
completeness does not prove all images pixel-perfect or replace independent language
review. Wave 1's two synonym-consolidating locale lists remain separately authored.

Validation: **22 Python tests, 12 focused JVM, 277 full JVM passed**, zero failures,
errors or skips. assembleDebug/lintDebug passed; lint **0 errors, 3 existing warnings,
2 informational findings**. Two generation/check cycles were byte-identical. Diff
and new-file whitespace checks passed. No browser/instrumentation/device run because
there is no UI/platform behavior change. Exact commands/evidence are in BUILD_NOTES.

The bilingual slice was committed and pushed as `8e00cea`. No Tell Me screen/route/ViewModel/session/progress/TTS
or speech input/evaluation was built. Next bounded slice: owner/native-speaker review
of the documented wording/visual ambiguities before a separately scoped Tell Me
consumer. Do not start it automatically. Other S24/Fire acceptance remains unchanged.


## Previous checkpoint — Scene Description catalogue, no activity UI

2026-09-25: started clean main `48ca578`. Pure `learning/scenedescription` models
and bundled repository now expose all 81 approved scenes, nine per category, in
manifest order. `ContentRepository` is unchanged. Run
`python3 scripts/generate_scene_descriptions.py` after relevant source edits, then
`--check` and the Python/JVM tests. No runtime JSON/Android dependency. See
CONTENT_DATA_SPEC.md for the exact nullable-language/query/validation contract.

All 27 Wave 1 records have bilingual learning/support text; 54 Wave 2/3 records are
English-only. German is explicitly unavailable, never fabricated/fallback. The
known wave/schema format differences are normalized; farm .01's count caution is
retained. Root asset docs now state Wave 3 / 81 scenes without rewriting historical
Wave 1 review outcomes. PNGs, manifests/metadata, existing learning/UI/audio/progress,
completed support work and signing/distribution are untouched.

Validation: 19/19 Python boundary tests, 10/10 focused JVM, 275/275 full JVM; zero
failures/errors/skips. assembleDebug/lintDebug passed; lint 0 errors, 3 existing
warnings and 2 informational findings. Generator freshness and diff/new-file
whitespace checks passed. Initial Gradle sandbox socket denial was resolved by a
permitted rerun, not code changes. No instrumentation/browser/device run was needed
for this pure content slice; see BUILD_NOTES for exact commands/evidence.

Uncommitted work only; no push. Deferred: Tell Me screen/navigation/ViewModel,
asset decoding, session/progress/audio, speech input and automatic evaluation.
Recommended next bounded slice: author/review German for the six English-only
records in one nine-scene category before designing bilingual consumption. Broader
existing S24/Fire acceptance remains separate and unchanged; no hardware claim.


## Previous checkpoint — first shared text-answer choice slice

Started clean main `93b0120`, which commits both the four-activity support rollout and larger-support-text setting. Those tasks are complete; older handoff references to uncommitted support work describe their earlier checkpoint. The highest-priority unfinished P1 remains the lightweight visual system. Added NativeTextChoice only for Prepositions and Seasons quiz text answers (recognition/next/before): neutral outlined surface, rounded geometry, 64dp minimum target and naturally wrapping labels. Preserved exact answer labels/order/IDs, callbacks, tags, gates, separate Listen and all learning/support/session behavior. No changes to Wilma, Vocabulary, artwork, completion, audio, browser, signing or distribution.

Four new component cases cover EN/DE, 1.5× portrait/both short landscapes, wrapping, target/semantics, keyboard, disabled activation, independent Listen and immunity to the larger-support-text preference. Existing consumer/regression tests are preserved. Validation evidence is in the latest BUILD_NOTES entry. Physical S24/Fire neutral-choice clarity, touch/focus/TalkBack and large-text/orientation checks remain open. No commit/push or physical S24 access. Next bounded slice: consider Vocabulary What Is It? text-choice adoption only; image choices and Wilma's colour cues require their own concrete contract. Do not jump to a new learning activity while visual-system work remains.

Final validation: affected packages 71/71; strengthened actual-display-rotation component check 4/4; full JVM 265/265 and instrumentation 86/86, zero failures/errors/skips. Debug build passed; lint 0 errors, 3 existing warnings, 2 informational findings; diff/new-file whitespace checks passed. No failed assertions or startup flake. Initial reverse-landscape screenshot was incomplete; explicit rotation wait produced a complete final capture with no production change. Reviewed EN/DE portrait and both German landscapes at 1.5× in the isolated component fixture; full device visual acceptance remains separate.

## Previous checkpoint — optional larger support text

2026-09-25: preserved the uncommitted four-consumer rollout on `952db56`. Added one Options preference: larger existing hints/retry guidance, default Off; On scales NativeSupportMessage font/line height by 25% on top of device font scaling. Shell-owned Boolean persists in the existing transitional preference store; see UX/NATIVE specs for the narrow contract. No support state, content, learning/session/progress/audio change; prompts, answers, completion, individual Listen and Wilma colour/order/follow remain intact. Shared message call signature remains unchanged.

New focused component/route tests cover EN/DE, 1.5× portrait/both short landscapes, accessibility/keyboard, default/reversion, persistence/recreation and unchanged session checkpoint. Exact validation evidence is in the latest BUILD_NOTES entry. Physical S24/Fire readability, TalkBack and preference-retention acceptance remain open. Work remains uncommitted; no push, physical S24 install or next feature.

Final validation: 7/7 focused instrumentation, 265/265 JVM, 82/82 full instrumentation; debug build passed; lint 0 errors, 3 existing warnings and 2 informational findings. Diff/new-file whitespace checks passed. Initial new-test API opt-in and keyboard Space/Enter mismatch were corrected without production workarounds or weakening existing tests; no emulator startup flake.

## Previous checkpoint — shared support text rollout complete for current activities

Started clean main `952db56`. Reused NativeSupportMessage unchanged at six additional sites: Vocabulary quiz retry/hint (Find/What is it), Wilma quiz retry/hint (Find/Before–After), and Wilma ordering retry/hint. Exact copy, visibility, tags, callbacks and enabled rules retained. Vocabulary answer-embedded hint labels, Explore/example text, prompts, correct feedback, technical errors, completion and all Wilma day/ordering/placed controls remain unchanged. No support/session/progress/audio, artwork, day colours, auto-follow, signing/versioning or navigation change.

The same small text primitive now covers all four current native activities. No category, variant or API extension is needed. Future support levels/settings are not implemented by this rollout. All work remains uncommitted; do not push. Validation and rendering evidence are in the newest BUILD_NOTES entry. Physical S24/Fire readability, contrast, TalkBack, larger text and orientation review remain open; re-test day-colour recognition with Basti separately. Next bounded P1 task: define the minimal support-settings contract for one existing concrete behavior, before any broad settings UI or new support state. No next feature started.

Validation finalized 2026-09-25: focused instrumentation 62/62; full JVM 265/265 and instrumentation 77/77, zero failures/errors/skips. Debug build and lint passed (0 errors, 3 existing warnings, 2 informational findings); diff/whitespace checks passed. Initial focused run had four new Vocabulary test-selector failures because the same word appeared in the answer and hint; the selector was corrected and the answer-label assertion retained separately. No production fix, weakened existing test, emulator startup flake or physical-device access. EN/DE 1.5× portrait/short-landscape support captures reviewed; device visual/accessibility acceptance remains separate.

## Previous checkpoint — shared support text, Seasons and Prepositions

Started clean main `a2a750e`. Added `NativeSupportMessage(text, modifier)`: one non-interactive Material3 Text node with a neutral rounded background, readable theme colour pair, 12dp padding, bodyLarge typography and unrestricted wrapping. It shares presentation only; no enum/escalation/state/actions/audio/persistence. Two existing content uses are represented: authored hints and gentle retry guidance. No distinct stronger-help state exists in these consumers, so none was added.

Six message sites migrated: Seasons quiz retry/hint (recognition and cycle modes), Seasons ordering retry/hint, Prepositions retry/hint. Exact content, visibility conditions and existing tags retained. Buttons, prompts, success feedback, technical error text, scene/answer layouts, completion, learning/session/progress/audio, artwork, Wilma colours and signing/versioning remain unchanged. Vocabulary/Wilma adoption is deferred. No commit/push.

Validation: 14/14 focused support tests, 265/265 JVM and 65/65 full emulator tests passed with zero failures/errors/skips; debug assembly passed; lint 0 errors / 3 existing warnings / 2 informational. Diff/new-file whitespace and exact consumer source-parity checks passed. No flake/retry. EN/DE portrait and German short-landscape 1.5× renders reviewed; long Seasons hints remain scrollable with full-size text and reachable actions. Exact commands and rendering limits are in the newest BUILD_NOTES entry. Physical S24/Fire readability, touch/scroll and TalkBack review remain separate from emulator evidence. Next bounded task could adopt this same small primitive for existing Vocabulary/Wilma retry/hint text after review, without new support levels or settings. No next task started.

## Previous checkpoint — action-button rollout across native activities

Started clean main `ebe76eb`; preserved the committed Seasons adoption and Wilma colour-cue fix. Prepositions, Vocabulary and Wilma non-day actions now use the existing NativeActionButton unchanged. PRIMARY: Next. SECONDARY: Replay, Help, attempt/load/save retry, Prepositions How to play and Vocabulary sentence Listen. NAVIGATION: Home and Prepositions legacy fallback (including its completion footer). Labels, callbacks, enabled expressions, tags, existing modifiers and screen order are retained. Shared geometry makes previously compact support actions fill the available width and supplies a growing 56dp minimum height.

Answer/picture cards, mode selectors, individual answer speakers, Wilma coloured strip/order choices, auto-follow and celebration are unchanged. No learning/content/session/progress/audio/navigation, signing/versioning/distribution or artwork changes. All changes remain uncommitted; do not push.

Validation: 39/39 focused instrumentation, 265/265 JVM and 51/51 final fresh-emulator instrumentation passed; debug assembly passed; lint 0 errors / 3 existing warnings / 2 informational; diff/new-file whitespace checks passed. An earlier full run was 50/51 due to an existing Compose startup failure, which did not reproduce after emulator restart; no production behavior or existing tests changed in response. Exact commands and intermediate evidence are in the newest BUILD_NOTES entry. New parameterized UI coverage exercises all three activities plus Wilma ordering in German at 1.5× text, portrait and both short landscape orientations, including accessible/keyboard activation, attempts/support, disabled/save-failure rules, labels and 56dp targets. Existing tests are preserved.

Audit: completion, Seasons, Prepositions, Vocabulary and Wilma now use the same three roles. No extra API is needed; allocated width and surrounding spacing stay caller-owned. Semantic answer/day controls intentionally remain custom. Next bounded P1 candidate: a choice/image-card contract with two concrete consumers, preserving semantic answers and Wilma colour cues; do not start it automatically. Physical S24/Fire visual, touch and TalkBack review remains outstanding; previous accepted learning/artwork/upgrade checks remain closed absent regression. Re-test Wilma colour recognition with Basti separately.

## Previous checkpoint — Wilma day-colour usability cue

Started on clean main `d0a8c94`, newer than the requested `702f00f`; preserved the committed Seasons action-button adoption. Physical child-use finding: generic weekday choices were confusing. Wilma selectable day-strip labels (Explore/Find/Before–After) and ordering choices now reinforce the established weekday colours. No prompt, correctness, generation, sequencing, restore, auto-follow, audio, progress, navigation, celebration or language-string changes.

`ui/wilma/WilmaDayColours` is a small UI-only palette keyed through CoreContent's existing weekday colourCue. No existing RGB constants were present; swatches were sampled from the committed Wilma segment PNGs, with no image changes. Black text is used on green/red/yellow/blue/orange/pink; white on purple. Minimum active contrast 5.05:1. Disabled cue is 25% over theme surface with recalculated contrasting text; disabled semantics/callback guards and existing Material/selectable focus/press/selection treatment remain. Placed labels, head/tail and Listen controls are unchanged.

Focused validation: 17 JVM and 11 Wilma emulator tests passed, including existing WilmaFollowTest. Final combined ui.common/Wilma suite: 20 passed. Full validation: 265 JVM and 39 emulator tests passed, zero failures/errors/skips; debug build passed; lint 0 errors/3 warnings/2 informational; diff/new-file whitespace checks passed. New pixel assertions wait for rendered resting fills (native feedback can outlive the Compose test clock), with palette tolerance and focus/activation coverage preserved. An intermediate existing Compose startup failure passed after an emulator-only fresh boot; no existing test changed. See newest BUILD_NOTES for exact commands, intermediate evidence and rendering limits. Tests compare rendered colour against the shared palette rather than duplicating RGB literals, and verify all seven labels, disabled/callback/keyboard behavior, German 1.5× portrait/short-landscape and wrong/retry/placement behavior.

Physically re-test with Basti to confirm the cue resolves the reported confusion; automated checks cannot establish learning benefit. Also check device colour distinction, readability, disabled/pressed/focused appearance, TalkBack and orientation/large-font behavior on S24/Fire Max. Prior accepted learning/auto-follow/artwork checks remain closed absent regression. No commit/push; no unrelated feature started.

## Previous checkpoint — shared action buttons adopted in Seasons

Started clean at `702f00f`. Only Seasons non-answer action presentation changed: Replay in Explore/quiz/order, Help and attempt Retry in quiz/order, load/save retry, Next and Home. Uses the existing NativeActionButton unchanged: SECONDARY for support/retry, PRIMARY for Next, NAVIGATION for Home. Ten call sites; all labels/callbacks/enabled expressions/test tags and existing modifiers retained. Help/retry now inherit the shared available-width text geometry and 56dp minimum; scroll hierarchy remains unchanged.

Answer/season/order cards, per-choice speaker buttons, FilterChips, placed cards, Days & Seasons hub, activity logic/state, audio/navigation/progress, completion celebration and canonical artwork are unchanged. No signing/version/distribution changes. No commit/push.

Four new Seasons UI tests cover actions/support/attempts, keyboard Replay, busy/save/image/language gates, order Retry/Help and disabled Next, plus German 1.5× text in portrait and both short landscape orientations. Validation: 21 focused tests (12 Seasons + 9 ui.common), 264 JVM and 36 full emulator tests passed, all zero failures/errors/skips. Debug build passed; lint 0 errors/3 warnings/2 information; diff/new-file whitespace checks passed. Extra emulator-only rendering run: 4 passed; reviewed German 1.5× short-landscape Next/Home captures. See latest BUILD_NOTES for exact commands and rendering limits. Shared completion and Seasons are now the two bounded consumers; do not expand into other activities automatically.

Prior S24 Seasons/Wilma/art acceptance remains closed absent regression. New button styling still needs physical visual/touch/accessibility checks on S24 and Fire Max; this is not acceptance of the wider P0 matrix. Suggested next bounded P1 slice: adopt suitable non-answer actions in Prepositions only, preserving all behavior; no new support presentation/settings or card redesign.

## Previous checkpoint — first P1 action-button slice

Started on clean main `3c52a1c`. `NativeActionButton` introduces presentation-only PRIMARY (filled), SECONDARY (tonal) and NAVIGATION (outlined) roles using the existing Material3 theme. Adopted only inside `NativeCompletionScreen`: Play again, Home, Replay and save retry. No activity/shell, learning, content, audio, persistence, navigation, reward-state, artwork or signing/version changes. No commit/push.

Buttons have 56dp minimum height, rounded 16dp corners, consistent padding, centered semibold labels that fill the allocated content width and wrap without a fixed line cap. Material owns semantics/focus/press feedback; no custom motion. Existing completion slots/tags/callbacks/enabled state and pinned reward separation remain. The new large-font tests caught mismatched paragraph/text measurement; aligning text width fixed it without weakening overflow assertions.

Validation: 9 focused Compose, 264 JVM and 32 full emulator tests passed (0 failures/errors/skips); debug build passed; lint 0 errors/3 warnings/2 information; diff/new-file whitespace checks passed. An extra emulator-only render run passed all 4 new tests. Reviewed German 1.5× text at 320×480dp and 700×240dp: no clipped labels or reward overlap. See the latest BUILD_NOTES entry for exact commands and file inventory. New styling still needs physical visual/accessibility review on S24/Fire; prior accepted Seasons/Wilma/artwork behavior remains accepted. The broader P0 acceptance matrix is unchanged. The component is available for later constrained-width adoption; next bounded visual slice could adopt Replay/Help/Retry in one ordinary activity, without changing support behavior or starting support settings.

## Previous checkpoint — P0 acceptance ledger updated, P1 planned only

Main inspected at `3efcb04` with a clean tree; implementation is committed in `d3ec6a9`. This pass changes documentation only, with no commit/push, code/assets/signing/versioning changes or new roadmap work.

Owner physically verified on S24 after the latest stable install: Seasons Explore, What Comes Next, What Comes Before, Winter → Spring and Spring ← Winter boundaries, Build the Year, wrong choices retaining correct placements, partial restore, completed restore, completion actions/celebration, and Wilma ordering auto-follow. These are closed unless a regression appears. Previous same-key upgrade and cleaned-art acceptance remain valid.

Remaining S24: both landscape orientations for these new flows, larger font, TalkBack/full accessibility, detailed EN/DE ALL/QUESTIONS/OFF and Replay sweep, pop-volume judgement, true process recreation beyond normal return/restore, and the full airplane-mode/offline-voice matrix. Fire Max still needs the concise checklist in TESTING_QA_SPEC.md. P0 implementation is in place, but the trusted-daily-build exit gate remains open until the remaining device evidence is supplied; do not label P0 complete or retire legacy fallbacks.

The regression run already started before this documentation request finished: 264 JVM, 28 emulator and 55 browser tests passed; debug build passed; lint 0 errors/3 warnings/2 informational. No repeat full run was started for these doc edits. See BUILD_NOTES.md for commands and evidence limitations.

Recommended first P1 slice (not implemented): a small shared action-button style/role component used only by NativeCompletionScreen, benefiting all four current activities. Reuse Material3/theme/ripple and the existing pinned responsive layout. Preserve callbacks, tags, enabled states, 48–56dp minimum targets, wrapping labels, accessibility and reduced-motion behavior. Do not refactor activity state, audio, progress, navigation, celebration art/motion or unrelated screens. Later P1 order stays visual system → support presentation → minimal support settings → canonical art/content → justified sequencing reuse → Follow the Instructions → semantic Memory Pairs.

## Previous implementation checkpoint — Seasons cycle progression and Wilma auto-follow

Continued the existing Seasons working tree from `5bf2383`, preserving the later `a8877b5` cleaned celebration assets and authoritative roadmap consolidation `56e837a`. No pull/reset/clean/restore/rebase/stash/discard, commit or push. No signing/version/distribution configuration or PNG changed.

Seasons now retains Explore/recognition and adds independent Next/Before 5/10 rounds plus a four-step Build the Year. All use canonical season identities/images, authored EN/DE, shared audio and durable progress. Next/Before include every cyclic boundary before repeating; distractors model unchanged season, reversed direction and a skipped step. Ordering explicitly chooses Spring as the display start of a repeating cycle. Wrong taps retain placed cards; Replay/Help are support, not failed attempts. Mode switching/Options/restore retain exact task/scramble/attempt/score/support/completion state and restore silently. Completion uses the existing optional celebration without progress coupling.

A narrow `OrderedPlacement` helper shares scramble, validation and placement rules with Wilma; authored content and bounded write-ahead journals stay activity-specific. Wilma's existing seven-step checkpoint bytes/IDs are unchanged. Progress schema-1 accepts four-step completion in addition to five/seven/ten; older app downgrade over new four-step records is unsupported. Failed deliveries remain retryable with the same keys.

Wilma's placed strip now smoothly follows a newly added segment over 400ms after layout. Initial/returned/recreated state and viewport resize position at the growing end without a scroll animation. Wrong taps do not pan. No general button/style/navigation redesign was made.

**Owner-supplied physical evidence (2026-09-23):** S24 same-key in-place upgrade succeeded from `1003901 / 1.1.39.1` to `1004501 / 1.1.45.1`, signing SHA-256 `bea808b0c0b891d73e61b739fd43f361a952d5297892318821b97ce91b553507`. Cleaned celebration artwork (`a8877b5`) is physically verified on S24. These checks are complete, not outstanding setup/cleanup work. The current task did not reinstall/wipe the attached phone. All instrumentation explicitly targets `emulator-5554`.

The existing celebration regression test caught larger replacement PNGs exceeding its decoded-image bound. Only the runtime loader now selects a power-of-two sample to keep each decoded dimension at most 200px; no artwork bytes or test bounds were changed. The narration regression now locks the original eight exact descriptions in a fixed fixture because the consolidated roadmap intentionally delegates authored content rather than embedding those sentences.

Final validation: **91 focused JVM, 8 focused Seasons UI, 3 focused Wilma UI, 5 focused celebration UI; 264 full JVM, 28 full emulator, 55 browser tests passed**. Version-policy tests **5/5**, distribution guards **6/6**; debug build passed; lint **0 errors / 3 warnings / 2 information**. Diff/added-file whitespace checks passed. All 31 files remain unstaged; no commit/push.

See the newest BUILD_NOTES.md entry for final validation, exact changed files and APK evidence. Remaining (updated 2026-09-24): broader S24 matrix and Fire Max acceptance/upgrade, EN/DE audible quality/Sound Off, subjective image size/motion, TalkBack/keyboard/large text, lifecycle/process recovery and new four-step progress retention after an ordinary signed upgrade. Missing Season/clues/matching, support settings/presentation, broader visual system and all unrelated roadmap work remain deferred.


## Previous checkpoint — shared native completion celebration

Resumed the existing uncommitted Kotlin/test implementation on `main` at `b3313a3`. Preserved that animal-art commit and the newer roadmap/README commits; no pull, reset, discard, commit or push. Signing configuration, credentials, applicationId and version allocation are unchanged.

All four native activities now share pinned completion actions and five optional balloons: Prepositions, Seasons Practice, Wilma practice/order and Vocabulary practice. Explore remains unchanged. Pop is one-shot; a brief local burst reveals a canonical dinosaur/snake/crocodile/whale/fish PNG, jumps then settles. Wilma retains its completed seven-day sequence. All images reuse the committed library unchanged; the previously implemented glyph reveal is replaced. A small Activity-scoped artwork owner decodes five images off-thread once; Compose renders only.

Reward state is ordinary in-memory Compose state, not a session/progress field. Restoring completion may reset balloons without replaying narration or adding progress. Completion is persisted independently. The optional quiet native pop tone respects ALL/QUESTIONS/OFF through a separate owned SFX policy port; Compose does not own playback and TTS ownership is unchanged. Background/navigation/new round/Replay cancel tones; animation-scale zero simplifies motion.

Validation: **9 focused JVM, 5 focused Compose, 247 full JVM, 21 full emulator and 55 browser tests passed**; build passed; lint **0 errors / 3 warnings / 2 information**. Version-policy tests **5/5** and no-secret signing guards **6/6** passed. Diff/new-file whitespace checks passed. See the newest BUILD_NOTES.md entry for exact commands, APK checksum and all 27 changed files. Remaining acceptance: S24 Ultra/Fire Max, airplane mode, five targets clear of controls/insets, calm float/pop/jump/settle/local confetti, repeated taps, EN/DE accessibility, Sound Off/Replay/missing voices, portrait/both landscapes/large fonts, Options/Back/background/return, process recreation without duplicate progress, next activity, and Fire Max same-key higher-code APK upgrade preserving settings/progress. Update: S24 same-key upgrade and corrected celebration transparency are now proven by the owner; see the current checkpoint. Assets were not altered in that historical implementation. No additional animal artwork is required for this five-animal pool. Broader animal-art integration into Vocabulary is outside this task.


## Previous checkpoint — Vocabulary validated; stable signing setup ready

Started from clean `main` at `0ac4711`, which commits native Wilma. Vocabulary work remains **uncommitted**; do not commit/push automatically or discard it. The content/audio/session/progress foundations and all previous native/legacy activities remain intact.

The Learn card now opens native Vocabulary: six canonical bilingual animal words, Explore/examples, Find the Word (spoken/written word-to-picture) and What Is It? (picture-to-word). The modes use deterministic 5/10 sessions, stable choices, Help/Retry/Replay, score locks, shared audio and separate durable host journals. Options/Back/language/recreation preserve supported state; restoration is silent. Progress effects are committed with accepted state before delivery, deduplicate after restore and remain retryable after failure. Explore has no correctness events. No new framework, engine, remote dependency or UI persistence/TTS.

Validation reconfirmed after signing changes: **33/33 focused Vocabulary, 238/238 full JVM, 16/16 emulator, 55/55 browser**; debug build/lint passed. Final lint **0 errors / 3 warnings / 2 informational findings** (no suppressions). Version-policy tests **5/5**, no-secret Gradle guard checks **6/6**, workflow YAML/shell syntax and diff checks passed. BUILD_NOTES.md records the audit, exact commands/results, file list, artifact identity and outstanding checks.


Resumed on newer `main` at `dbaa8d4`; its completion-celebration roadmap text is preserved and no celebration implementation was started. Stable APK distribution now uses the existing release type with external persistent signing credentials, fail-closed guards and bounded CI run/attempt versioning. Debug defaults remain code 1/name 1.1-dev with this machine's debug key; CI debug artifacts are explicitly debug-only. The applicationId is unchanged. Main-branch manual dispatch can produce `Basti-stable-signed-v<code>` after checks, once the owner sets `BASTI_KEYSTORE_BASE64`, `BASTI_STORE_PASSWORD`, `BASTI_KEY_ALIAS`, `BASTI_KEY_PASSWORD` secrets (README setup).

Historical setup note (superseded for the current S24 stream by the owner-reported proof in the current checkpoint): create and back up the permanent key, configure secrets, dispatch and verify its certificate, compare with installed APKs, then test a higher-code same-key install without uninstalling on S24 Ultra/Fire Max. Prior evidence proves a certificate mismatch; code bumps alone cannot fix that. One final owner-approved reinstall may be needed if the old key is unavailable, and would erase data; no export/import is implemented. No key was generated, no secrets were set, no permanent signed APK or physical upgrade was claimed. Release progress inspection is currently limited without authorised QA access; do not infer preserved/deduplicated records merely from restored UI. See TESTING_QA_SPEC for the exact acceptance procedure. All changes remain uncommitted; no push.

Next: original language-neutral illustrations for dinosaur, snake, whale, horse, crocodile and fish; bilingual/educational review; broader roadmap vocabulary and useful category/context diversity. Existing glyphs are explicit temporary migration placeholders, not final production art. No images were generated or altered. Categories practice is deferred because this starter set has only one meaningful category. This does not complete the roughly 50–80-word v1 curriculum or establish independent reading mastery.

Batch physical checks on S24 Ultra/Fire Max remain open: airplane-mode EN/DE/missing voices and Sound Off/manual Listen; image/word clarity, portrait/both landscapes, large font/insets/accessibility/touch; Options/Back/background; actual process recreation; 5/10 completion and progress after restart/normal signed upgrade. Corrupt/capacity recovery and broader retention/migration remain existing foundation limitations. No neural TTS, dashboard, new cloud service or unrelated activity migration was started.


## Previous checkpoint — native Wilma, automated validation complete

Based on clean `main` at `b607b39` (Seasons committed). New Wilma code is uncommitted. Do not commit/push automatically or discard changes.

Implemented native chooser entry, canonical seven-day Explore, Find Day, Before/After, and activity-specific tap-to-place ordering. Quiz sessions use the existing shared durable host; ordering retains exact scramble/support/retry state plus up to two pending progress effects before publishing completion. The shared progress codec now permits seven-step completion without changing its schema layout; old app downgrade over those new records is unsupported. PNGs are unchanged; head/tail are never weekday IDs or answers. Shared audio policy/cancellation is used, and restore remains silent. Today/Yesterday/Tomorrow is deferred pending an explicit anchor interaction; no clock inference exists.

Validation: **17/17 focused**, **205/205 full JVM**, **12/12 emulator Compose/navigation**, **55/55 browser**. assembleDebug/lintDebug passed; lint **0 errors / 10 warnings / 2 informational findings** (unchanged). The emulator caught a real answered-target enablement bug; it was fixed while preserving the assertion, and the full suite passed. Diff/added-file whitespace checks passed. BUILD_NOTES.md records exact commands, file inventory, APK hash, portrait emulator review and outstanding hardware matrix.

Next: batched physical validation on Samsung S24 Ultra and Fire Max, including airplane-mode EN/DE/missing voices and silence, segment colour/order/clarity, portrait/both landscapes/large fonts/insets/touch/TalkBack, Options/Back/background, true process restoration, 5/10 completion, partial/completed ordering recovery and progress after restart/normal signed upgrade. Emulator audio was disabled; no physical or audible-quality claim. Today/Yesterday/Tomorrow remains an explicitly anchored follow-up. Capacity/corrupt-file recovery and broader retention/migration remain existing foundation limitations. No neural TTS, unrelated activity migration, generic framework, cloud or dashboard work.


## Previous checkpoint — native Seasons, automated validation complete

Based on `main` at `e3bacd6`, which commits native Prepositions. Preserved the interrupted Seasons implementation and completed compilation, focused/unit/Compose/browser testing and documentation. These Seasons changes are **uncommitted**; do not commit or push automatically.

Home Days & Seasons now opens a small native chooser: Seasons Learn/Practise or the existing legacy calendar activity. Native Explore uses the four unchanged production PNGs and canonical EN/DE names/narration. Practice uses deterministic 5/10 rounds, shared state/audio, semantic choices, explicit Retry/Help, score locks and calm completion. Options/Back/recreation retain supported state; restoration is silent. Explore selection is a separate bounded checkpoint and creates no learning events; entering Learn during an unanswered quiz records help use.

The proven Prepositions journal host is now `DurableSessionHost`, with the original Prepositions adapter/path/schema preserved. Seasons keeps its own quiz journal and pending progress effect, retries uncertain writes with the same identity and deduplicates completion through shared native progress storage. Corrupt/incompatible files are retained with failure guidance/Home/legacy access. No new general queue/framework or platform behavior in the reducer/Compose.

Validation: **46/46 focused**, **188/188 full JVM**, **7/7 emulator Compose/navigation**, **55/55 browser**. assembleDebug/lintDebug passed; lint **0 errors / 10 warnings / 2 informational findings** (unchanged). Diff/added-file whitespace checks passed. BUILD_NOTES.md records exact commands, content/legacy audit, file inventory, APK hash and manual checks. All existing tests remain intact; season PNG bytes match HEAD.

Next: physically accept Seasons and Prepositions on Samsung S24 Ultra and Fire Max. Seasons checks include airplane mode, EN/DE and missing voices, OFF/Questions/All/Replay, portrait/both landscapes, large text/TalkBack/insets/image clarity, Options/Back, backgrounding, real process restoration, 5/10 completion and progress after restart/normal signed upgrade. Emulator audio was disabled; no physical acceptance claim. Pending Seasons quiz effects retry when its quiz is loaded or Retry is selected, not from a background Explore service. Foundation capacity/corrupt-file recovery and broader retention/migration remain follow-up work. Keep legacy routes until acceptance. Wilma, neural TTS, Follow the Instructions, dashboard and unrelated migrations were not started.


## Previous checkpoint — native Prepositions, automated validation complete

Based on `main` at `9b247e5`, with the earlier content/audio/session/progress foundations committed. Preserved the interrupted Prepositions work and finished the native slice. All current changes remain **uncommitted**; do not commit or push automatically.

Home Prepositions now opens Compose. The 24 animal/relation scenes, six canonical choices, EN/DE text, deterministic 5/10 rounds, stable choice order, shared reducer/score locks/support/retry, audio policy and exact session checkpoint are integrated. Options/system and visible Back/Home/re-entry preserve supported state; restoration is silent. Native drawing uses explicit relation geometry and temporary legacy animal glyphs. Explicit Retry and task-count completion follow the shared session/UX direction. The screen has an always-available legacy fallback; legacy source/assets/tests and mixed rounds remain unchanged.

`PrepositionsHost` durably saves the exact checkpoint plus at most one pending progress event before accepting a transition. A single worker delivers through the shared progress adapter/repository and saves the acknowledgement/removal. Failed/uncertain writes are retried by re-reading disk with the same dedupe identity; stale hosts cannot overwrite newer journal bytes. Save failure pauses further answers while Retry Save/Home/fallback remain usable. This closes the accepted-transition pending-effect gap for this activity only. Corrupt/incompatible journals are preserved and require recovery/legacy use; no silent deletion or broad migration/outbox framework was added.

Validation: **20/20 focused**, **167/167 full JVM**, **3/3 emulator Compose/navigation**, **55/55 browser**. assembleDebug/lintDebug passed; lint **0 errors / 10 warnings / 2 informational findings** (three new test-dependency update notices). Diff/new-file whitespace checks passed. Exact commands, test boundaries, APK hash, legacy audit and physical acceptance checklist are in BUILD_NOTES.md. No existing tests were weakened or removed.

Next: physical acceptance on Samsung S24 Ultra and Fire Max in airplane mode, including EN/DE installed and missing voices, OFF/Questions/All, Replay, all scene relations, portrait/both landscapes, larger fonts, insets, TalkBack, Options/Back, background/return, actual process recreation, 5/10 completion and progress after restart/normal signed upgrade. Emulator audio was disabled; no audible-quality or Fire compatibility claim is made. Temporary glyph artwork needs review/replacement before production art acceptance. The bounded progress store still has explicit capacity failure rather than retention/migration UI. No dashboard, Wilma/Seasons UI, Follow the Instructions, neural TTS or unrelated migration started. Legacy removal remains blocked on physical parity.

## Previous checkpoint — durable native progress foundation

Started clean on `main` at `148e756`; content, audio and session foundations are now committed. This task adds six source files and four test files under `learning/progress`, plus documentation. Changes remain **uncommitted**. Do not commit or push automatically.

The new typed attempt/completion repository records skill/context, stable task/content references, outcomes, attempts/retries and support without reducing progress to score. Attempt keys use session/task/attempt identity; completion keys use session identity, independent of delivery retries. Identical redelivery preserves one record, while conflicting payloads fail. Version-1 checksummed snapshots use locked atomic file replacement and sync, with a 10,000-event / 16 MiB bound and explicit failure results. Android storage lives in the private no-backup directory. Queries support skill/session/activity/kind, stable order and limits.

`SessionProgressRecorder` consumes existing pure effects against the frozen plan, persists on a caller-provided worker thread and returns completion delivery acknowledgements. No session/content/audio foundation or live legacy route was changed. See PROGRESS_TRACKER_SPEC.md for exact contracts and BUILD_NOTES.md for validation.

Validation: focused **31/31**, full native **147/147**, browser **55/55**; assembleDebug/lintDebug passed. Lint **0 errors / 7 warnings / 2 informational findings**. Diff and new-file whitespace checks passed. No existing test was changed or weakened.

Next: integrate delivery/retry/checkpoint ownership with the first migrated native host. Retain failed effects; a checkpoint is not a durable attempt outbox and cannot recover an effect lost before storage submission. Handle capacity/incompatible stores explicitly; future retention/schema migration and larger history storage remain work. No dashboard, rewards, Wilma/Seasons UI, Follow the Instructions, neural TTS or activity migration was started. Legacy session recovery remains separate. Physical Android storage/process/backup acceptance and existing device-specific gaps remain outstanding.

## Previous checkpoint — shared native choice-session framework

Based on `main` at `4c254c4`; the content and audio foundations are committed. Resumed and preserved the five session source files from the interrupted run, then completed the framework, tests and documentation. All current changes remain **uncommitted**; do not push automatically.

`learning/session` supplies typed identities, immutable choice tasks/plans/state, explicit reducer actions, 5/10 rounds, score locking, wrong-answer/retry policy, support tracking, deterministic bounded candidate generation and completion request/result effects. Canonical content IDs identify choices; separately authored EN/DE text supplies display/speech. Language/future settings do not replace active task identity/order. Full implementation contracts and bounds are documented in NATIVE_ARCHITECTURE_SPEC.md.

Checkpoint schema 1 stores the actual complete task plan plus progress, support/retries, index, versions and completion-delivery state in at most 100,000 bytes. It rejects malformed/incompatible data, restores without regenerating tasks and emits no old narration or completion. `LearningSession` uses the existing native audio controller through effects, cancels stale ownership, distinguishes manual Hint from Replay, preserves summary audio during completion acknowledgement and keeps speech callbacks out of learning logic.

Validation: focused **43/43**, full native **116/116**, full Playwright **55/55**; assembleDebug/lintDebug **passed**; lint **0 errors / 7 warnings / 2 informational findings**; diff/new-file whitespace checks passed. Exact commands, limits and APK identity are in BUILD_NOTES.md. No existing tests were changed or weakened.

No live route is migrated. Legacy MainActivity/ShellNavigation/WebView recovery, current content/audio foundations and assets remain unchanged. No UI, Room/progress persistence, broad settings rewrite, neural TTS, Wilma/Seasons activity or Follow the Instructions was started.

Next: minimal shared persistent progress events with attempt/session deduplication, then lifecycle/saved-state host integration and the first native migration under the roadmap. Completion effects describe one logical session completion, but explicit delivery retries require future repository deduplication; the framework does not claim exactly-once durable writes. Snapshot restoration is not a persisted attempt journal. Screen owners must suspend/close old session coordinators before replacing them, wire background/navigation cancellation and restore silently. Physical Samsung/Fire native lifecycle/process/audio acceptance and the older P0 device gaps remain outstanding.

## Previous checkpoint — shared native audio foundation

Started from clean `main` at `b6f0661`, which commits the completed content foundation. Added the separate native `audio` layer: pure policy/contracts/controller, authored content speech conversion and sanitizer, revocable owner/session/context tokens, fresh opaque request IDs, one pending/active request, response-priority replacement, cancellation and exactly-once terminal outcomes. No pure-layer Android or UI dependencies.

`AndroidSystemSpeechEngine` implements the platform boundary using application context, serialized main-thread callbacks, cached installed offline EN/DE voices, verified per-request selection and flush playback. Its pure system coordinator handles readiness, language/voice failures and stale callbacks. Shared JVM `FakeSpeechEngine` and fake-port tests provide deterministic readiness, request, cancellation and failure assertions.

Validation: focused **38/38**, full native **73/73**, full Playwright **55/55**; assembleDebug/lintDebug **passed**, lint **0 errors / 7 warnings / 2 informational findings**; diff/whitespace checks passed. Exact commands, limits and APK SHA-256 are in BUILD_NOTES.md. The source-of-truth policy is unchanged; VOICE_AUDIO_SPEC.md now documents the implemented contracts and required caller lifecycle behavior. BACKLOG.md distinguishes foundation implementation from integration/device acceptance.

Changes are intentionally **uncommitted**, with no push. Existing LegacySpeech/MainActivity, all legacy routes, content repository, assets and prior tests are untouched. No neural TTS, Voice Lab, Wilma/Seasons UI, Follow the Instructions, Prepositions migration or broad settings/session rewrite was started.

Next: shared session/settings/completion and minimal persisted progress foundations, then the first native migration under the existing roadmap. Native callers must open a fresh audio context for each task/phase/language, cancel ownership when leaving/backgrounding, close on disposal and restore silently with explicit Replay. Physical Samsung/Fire checks of this new adapter remain outstanding; no hardware compatibility claim from fakes. The wider physical P0 gaps below remain open.

## Previous checkpoint — first shared native content foundation

Continued the existing weekday implementation after HEAD advanced from `ba22b15` to `e6004e2`. Preserved the newer evidence-informed roadmap, Seasons narration/art guidance and all Wilma/Seasons assets. No pull/rebase/reset or activity migration was performed. Changes remain uncommitted for review; do not push automatically.

Pure Kotlin `learning/models` and `learning/content` now provide typed semantic IDs, required authored EN/DE text with separate display/speech, schema/revision metadata and an immutable deterministic repository. Bundled data contains seven canonical weekdays with cyclic previous/next and supplementary colour references, plus four seasons with exact roadmap descriptions and typed local image references. No Wilma artwork in weekday domain logic; no platform types or filesystem access in the repository. CONTENT_DATA_SPEC.md describes implemented contracts and error/version policies.

The six tracked `.DS_Store` files are staged for removal from Git; recreated local metadata was preserved and is ignored by `.gitignore`. No legitimate asset was removed or compressed. Existing routes/audio/UI remain unchanged. No neural TTS, Wilma/Seasons screen, Follow the Instructions or broad game work started.

Validation: focused **20/20**, full native **35/35**, full Playwright **55/55**, assembleDebug/lintDebug **passed**, lint **0 errors / 7 warnings / 2 information**, diff checks passed. Commands and APK identity are in the current BUILD_NOTES.md entry. The broader P1 item is split: this foundation is complete, while skill/vocabulary/grammar, scene/task, phonics and verb schemas/generators remain follow-up work for the first migration. Shared native audio/session/progress work and physical P0 gaps remain as documented below. The new repository is not wired to live activities yet; physical device validation was not repeated for this data-only change.

## Previous checkpoint — Samsung physical P0 checks and native landscape insets

Based on clean `main` at `6e44e10`. Physical S24 Ultra testing found that the native Options button overlapped the side navigation bar in landscape. MainActivity now uses top + horizontal safe-drawing insets. Physical measurements confirm Options/Back clear the navigation bar in portrait and both landscape directions, including system font scale 1.3.

The existing phone app had a different signing key, so it was preserved. Tests used a separate `com.bellfamily.bastischool.p0qa` package from a temporary checkout with only applicationId changed; repository package ID is unchanged. QA copy remains installed. Both packages' code/assets match apart from package identity; production-package upgrade compatibility was not established.

Samsung checks passed for quiz/lesson/library Options/Back, answered-state/score retention, rotation/font change, a confirmed background process kill and saved-task recovery, and five-/ten-question completion. The user confirmed clear English and German offline speech and Sound Off silence. Evidence names the actual Google system TTS engine, device/OS/configuration, tested APK hashes and limits in BUILD_NOTES.md. Original connectivity/display settings were restored. No user data was cleared.

Validation: full Playwright **55/55**, native unit **15/15**, assembleDebug/lintDebug **passed**, **0 lint errors / 7 warnings / 2 information**, diff check passed. No existing tests weakened. No neural TTS or animation/video work. Do not push automatically.

P0 remains open for physical Fire Max, Samsung gesture/accessibility and full content/audio/lifecycle matrices, missing voices and the differently signed production upgrade path. Do not interpret these targeted Samsung passes as exhaustive device acceptance. The software roadmap/P1 sequence is unchanged.

## Previous checkpoint — P0 native navigation/session recovery

Based on clean `main` at `a7dd258` (including completed/pushed audio, Prepositions, completion and Letters work). This task is local only; do not push automatically.

Audit confirmed native Options destroyed the learning WebView and always returned Home, and native route/session state had no recreation checkpoint. Options now retains one paused WebView and its origin. Saved-state recovery rebuilds the exact question/choices, score, answered/hint/retry state, completion or lesson return from a bounded versioned checkpoint. Language changes translate the same task; changing round length does not replace the active round. Resume is silent, and Back callbacks are coalesced/ownership-checked. Invalid recovery goes Home with a calm bilingual restart notice. Details and compatibility rules are in NATIVE_ARCHITECTURE_SPEC.md.

Validation: focused **24/24**, full Playwright **55/55**, native unit **15/15**, assembleDebug/lintDebug **passed**, lint **0 errors / 7 warnings / 2 informational findings**, `git diff --check` passed. Exact commands and APK hash: BUILD_NOTES.md, current navigation entry. Pure JVM tests cover shell route/Back/checkpoint policies; browser tests cover fresh-page checkpoint reconstruction, not Android framework recreation. No physical Samsung S24 or Amazon Fire Max testing was performed. Remaining P0 is physical acceptance: activity/Options/lesson/completion rotation, recreation and OS saved-task recovery, system/visible Back, rapid Options/Back, EN/DE offline audio, layout/insets/large text and accessibility. Force-stop/new-task recovery and durable skill history are outside this transitional checkpoint.

No animation/video assets, neural TTS or broad native migration were started. Existing learning and speech-safety tests remain. The wider priorities remain shared native content/audio/session models and minimal persistent Progress Tracker events, then the established migration/learning sequence. The historical next-task statements below describe their old checkpoints only.

## Previous checkpoint — P0 completion/reward layout

Local checkpoint: `fix: keep completion actions visible on small screens`, based on clean `ff57c8b`. Not pushed.

Ten stars now wrap as individual elements. Replay, Continue and Home appear before the reward area; optional balloons cannot cover the controls. Completion hides duplicate web/quiz chrome, resets scroll, and focuses the heading so keyboard navigation reaches the three primary controls before balloons. Continue restores the ordinary quiz layout and starts the existing new round; Home retains its native bridge behavior. Reward counts, audio policy, speech safety and the Prepositions fixes remain intact.

Validation: focused **4/4**, full Playwright **39/39**, Android build/lint **passed**, **0 lint errors / 7 warnings / 2 informational findings**. Unchanged native unit task UP-TO-DATE (**7 passed**). Tested both languages, 0/5–10/10 scores, six viewport sizes, large CSS text and reduced motion; manually inspected phone/short-landscape browser screenshots. APK assets match current source. Exact commands/hash: BUILD_NOTES.md.

Physical S24/Fire insets, system font scaling, orientations, touch/TalkBack and audio/lifecycle acceptance remain outstanding. No neural TTS or native migration started.

Next implementable P0: Letters display-case consistency and bilingual initial-letter/sound review. Keep letter names, written initial letters and actual phonics distinct. Native Options/session recovery follows; do not describe browser checks as native/device proof. Master roadmap and native migration priorities remain unchanged.

## Previous checkpoint — P0 Prepositions correctness

Local checkpoint: `fix: align preposition scenes and bilingual narration`, based on clean `231f875`. Not pushed.

The six legacy relations now share scene/reference metadata with English/German phrases. Questions narrate exactly the four displayed options in order. Correct feedback/hints agree with the actual rock, table, box or two rocks; German uses “auf dem Stein”, “unter dem Tisch”, “hinter dem Stein”, “neben dem Stein”, “in der Kiste” and “zwischen den beiden Steinen”. Scene accessibility text follows the same record. Narrow-screen next-to/between spacing and under-table placement are fixed.

Validation: focused **3/3** (48 bilingual relation/animal cases plus geometry at four viewport sizes), full Playwright **35/35**, Android assembleDebug/lintDebug **passed**, **0 lint errors / 7 warnings / 2 informational findings**. Native unit task remained UP-TO-DATE with **7 passing tests**. Browser scenes visually inspected; APK assets match source. Exact evidence/hash: BUILD_NOTES.md.

Physical S24/Fire scene, audio, touch and lifecycle checks remain outstanding. The audio policy/sanitization checkpoint is preserved. No neural TTS or native migration started. Product direction is unchanged.

At that checkpoint, the next P0 was completion/reward layouts (now addressed above), followed by Letters case/bilingual phonics and native Options/session recovery; physical validation remains required across these tasks. Native Prepositions migration and varied-context expansion remain later work.

## Previous checkpoint — P0 audio reliability

Local checkpoint: `fix: harden audio policy and TTS lifecycle`, based on clean `eb5b68e`. Committed locally only; not pushed. Preserves `f72c5f7` speech safety and `796359e` product direction.

Implemented in the existing legacy/system-TTS path:

- All: automatic questions/instructions/tutorials/lessons and feedback. Questions Only: instructional audio, no automatic praise/generic wrong feedback/completion summary. Manual Replay/options/Listen work in both. Off blocks all speech and balloon tones, including manual controls.
- Small Kotlin `LegacySpeech` helper with INITIALISING / READY / FAILED, at most one current pending request, flush/stop ownership, completion callbacks and cached installed offline EN/DE selection. Missing/network/uninstalled voices never fall through to a previous language. Parent Options offers voice-data/restart guidance.
- Owned cancellable JS narration timers; new requests, questions, feedback, navigation, language, completion and backgrounding invalidate obsolete speech. Completion Replay reads the summary. Native Back delegates to lesson-aware web navigation; departing WebViews are disposed.
- Versioned per-language tutorials marked only after current successful full playback. Interruption/failure/Off never consumes them. Durable native reset epoch works before a WebView exists. Old unversioned heard flags are deliberately not carried forward.
- Balloon tones reuse a lazy context; Off/background stops oscillators and suspends it.
- Explicit displayText/speechText praise and sanitizeForSpeech() remain intact, as does answer/speaker isolation.

Validation: focused **20/20**, full Playwright **32/32**, Kotlin unit **7/7**; assembleDebug/lintDebug **passed**, lint **0 errors / 7 warnings** (plus 2 informational findings). Android Studio JDK 21, bytecode target 17. Exact commands, warnings, APK hash and limitations are in BUILD_NOTES.md.

No physical Samsung S24 or Amazon Fire Max testing was performed. Remaining P0 audio work is physical acceptance: actual airplane-mode EN/DE output/quality, missing voices, startup readiness, silence, interruption and lifecycle/reset behavior. Browser mocks and pure JVM tests are not hardware evidence. No neural TTS, Supertonic, Piper, sherpa-onnx, Voice Lab, downloaded voices or personality packs were started.

At that checkpoint, remaining wider P0 included Prepositions scene/choice/feedback agreement (now addressed above); completion layout on narrow/short screens; Letters case and bilingual phonics review; native navigation/session recovery and physical lifecycle/layout/accessibility checks. Native Options currently returns Home and does not preserve an in-flight learning surface. Language changes regenerate the legacy current question without score changes; answered questions remain locked, including lesson return.

Continue the established product sequence after P0: shared native content/audio/session and minimal persisted skill events, Prepositions migration proof, Follow the Instructions, Vocabulary Booster, Tell Me!, shared Memory Pairs, progress dashboard, concept-focused maths and classroom skills. Today's Adventure, varied-context generalisation and Discovery Book priorities remain unchanged. Do not begin neural voice work as part of this checkpoint.

## Historical session context (2026-09-20)

Audit revision: `290ff0381e1208d512cd06e08131568fca5aee5b`; remote/CI identity was checked then, not reverified in this pass. See TECHNICAL_AUDIT.md for detailed findings. No physical S24/Fire device was attached; browser mocks did not establish real audio/native behavior.

The earlier session used `/home/paul/Projects/Basti_Learning_Adventure_V1_For_Codex`. Those paths and temporary artifacts below belong to that host and may not exist in another checkout. Prior concurrent Compose work and implementation authorization were session-specific; inspect git status/diff and source before new work.

## Historical tooling

- JDK: `/home/paul/.local/share/basti-tools/jdk17`
- Node tools: `/home/paul/.local/share/basti-tools/node-v22.14.0-linux-x64/bin`
- Browser tests: prepend Node tools to PATH, then `npm test -- --reporter=line`. Chromium needs execution outside the restricted sandbox.
- GitHub CLI: `/home/paul/.local/share/basti-tools/gh_2.101.0_linux_amd64/bin/gh`
- SDK: `/home/paul/Android/Sdk`
- Audit diagnostic scripts: `/tmp/basti-audit.cjs`, `/tmp/basti-audit-extra.cjs`; screenshot `/tmp/basti-completion-audit.png`. These are temporary evidence, not maintained tests; they may disappear.
- Sandbox DNS blocked GitHub; escalated read-only remote/CI checks succeeded. Do not mistake that for repository failure.

The audit was a review, not a completed fix pass. Do not repeat earlier claims that all animations teach their actions, all audio policies work, or all system-bar/device cases are verified.

## Follow the Instructions MVP (2026-09-27)

The first native instruction/action consumer is implemented at the `follow-instructions`
route. It presents four canonical animal image targets and authored one-step EN/DE
instructions in deterministic five- or ten-question rounds. `ChoiceQuestion` is used
as the semantic session representation (choices are object IDs); the UI deliberately
uses image taps rather than text-choice controls. Wrong taps use the existing retry
policy, correct taps require explicit Next, Replay is independent, and completion uses
the shared native completion flow. `DurableSessionHost`, existing progress recording,
lazy owner-local audio and silent restoration remain unchanged. A four-image worker
loader maps semantic IDs to committed Animals artwork and fails passively.

## Follow the Instructions v1.1 expansion (2026-09-27)

Follow now has six approved semantic objects: the original crocodile, dinosaur,
snake and fish plus canonical horse and whale. New revision-2/content-1.2 rounds
deterministically choose four-object scenes from the finite target/set catalogue,
while the host restores revision-1/content-1.1 journals against a frozen legacy
repository without changing their task IDs, object order, target or pending progress.
Play Again starts the expanded revision. Artwork remains the existing local PNG set;
no image bytes or shared schemas changed.

Focused validation: Follow JVM **4/4**, Follow instrumentation **2/2**, full JVM
**321/321**, assembleDebug and lintDebug passed (0 errors, 3 warnings, 2 informational
findings). The full emulator run was not green: it reproduced the existing Vocabulary
orientation display assertion and stopped at 59/119; no Follow test failed. Physical
S24/Fire acceptance remains open.

## Months Phase 6 — introductory Months ↔ Seasons (2026-10-12)

Starting `main`/HEAD was `9a6fa54beb9d0014f80e358af7d25cf59235e672`; fetch confirmed
`origin/main` matched and the initial worktree was clean. The Phase 6 implementation
is present but is **not marked complete** because broad automated validation did not
finish cleanly. No commit or push was made; final local HEAD remains `9a6fa54` and
remote HEAD was not changed. Physical S24/Fire/TalkBack/offline-voice acceptance is
**NOT RUN — deferred by owner**.

The new route assigns a displayed month into one of four accumulating season groups,
using the four existing Seasons images and canonical `MonthDefinition.season` map.
The fixed curriculum is meteorological Germany/Northern Hemisphere: March–May spring,
June–August summer, September–November autumn, December–February winter. The activity
uses `activity.months.seasons`, `skill.months.season`,
`context.months.season_mapping`, revision 1 and `months-seasons-session`; existing
Months and Seasons IDs/journals remain unchanged. Five- and ten-task plans use unique
month targets and balanced season counts. Replay names the task and all four season
choices; the optional bilingual full map is recorded as hint support. Wrong placement
is retryable; correct placements remain visible. Route, Options, language and Activity
recreation share the durable Months session and restore silently.

Validation evidence for this checkpoint:

- PASS — focused Months ↔ Seasons JVM suite: **4/4**.
- PASS — full JVM suite: **508/508**.
- PASS — focused API 35 emulator UI/route suite: **13/13** (EN/DE; 1×/1.5×/2×;
  portrait/short landscape; keyboard activation, retry, group guide, Options and
  Activity recreation).
- PASS — `compileDebugAndroidTestKotlin`.
- PASS — `assembleDebug` (up-to-date after successful compilation).
- PASS — `git diff --check` at checkpoint.
- FAIL/INCOMPLETE — broad emulator instrumentation began **347** tests, reached
  **140/347**, then was stopped. Six failures had occurred: two
  `NativeTextVisibilityTest` “No compose hierarchies found” cases and four
  `SupportPresentationTest` missing/visible authored-text assertions in Prepositions.
  These are outside the Months ↔ Seasons code, but were not proven pre-existing; the
  broad suite therefore is not a pass.
- FAIL/INCOMPLETE — a separate existing Months/Seasons instrumentation selection
  (`114` planned) terminated at the first Months Build the Year UI case with an
  instrumentation-process crash and empty failure stack. It did not complete.
- BLOCKED — `lintDebug` failed inside lint analysis with `25.0.3` and a UAST
  `AlreadyDisposedException`/`NoClassDefFoundError`; retrying with `--max-workers=1`
  reproduced the failure. No usable lint warning report was produced. The combined
  `assembleDebug lintDebug` command had a successful assemble task and failed lint.
- Compiler emitted the existing `MainActivity.kt` `VIBRATOR_SERVICE` deprecation
  warning; Gradle/JDK emitted restricted `System::load` warnings. No source fix was
  made for unrelated warnings.

The first emulator UI attempt identified and fixed a real implementation defect:
previous group rendering parsed month IDs from the full task-definition suffix
incorrectly. Rendering now resolves the month from the canonical task definition, and
the passing focused UI test checks that completed placements appear in their groups.

Files changed: `MonthsSeasonsMatch.kt`, `MonthsSeasonsMatchScreen.kt`,
`MonthsSeasonsMatchTest.kt`, `MonthsSeasonsMatchScreenTest.kt`,
`MonthsSeasonsMatchRouteTest.kt`, `MonthsQuestions.kt`, `MonthsViewModel.kt`,
`MonthsScreen.kt`, `MainActivity.kt`, `ShellNavigation.kt`, `MonthsNavigationTest.kt`,
README, roadmap, backlog, content/native architecture/progress/navigation/QA specs,
and this handoff. The roadmap/backlog record Phase 6 as under validation, not complete.

Next: diagnose the six broad shared-UI failures and the emulator test-process exit,
resolve the Android Lint/UAST environment failure, then rerun full unit, affected and
full instrumentation, build/lint and diff checks. Do not commit/push or claim CI until
all required checks pass. Keep physical acceptance deferred by owner.

## Months Phase 6 — validation follow-up (2026-10-12)

Preservation checkpoint: branch `main`; starting `HEAD` and `origin/main` both
`9a6fa54beb9d0014f80e358af7d25cf59235e672`. The original Phase 6 implementation and
documentation were present in the worktree; no fetch, reset, rebase, stash, clean,
commit or push was performed. Existing work was preserved. Local changes below remain
uncommitted. No GitHub Actions run was triggered. Physical-device acceptance remains
**NOT RUN — deferred by owner**.

### Reported failures and diagnosis

- `NativeTextVisibilityTest`: the two earlier “No compose hierarchies found” cases
  passed in an isolated **8/8** class run and all eight cases passed in the full run.
  The triggering event was not captured and did not reproduce; this is not evidence
  of a Phase 6 defect or a diagnosed product fix.
- `SupportPresentationTest`: the four Prepositions failures searched for exact hint
  phrases globally, although those same phrases are intentionally visible in answer
  choices. Assertions now check the `feedback` and `hint-text` nodes. Corrected class:
  **12/12 PASS**; these failures did not recur in the full suite.
- Earlier Months/Seasons instrumentation crash: not reproduced. Emulator `/data` had
  4.1 GB available; recent logcat had no fatal app/runtime or low-memory event. The
  isolated Months Build the Year screen class passed **12/12**, and the full suite
  completed **347/347** without a runner crash. No contemporaneous crash stack from
  the earlier run exists, so its initiating cause remains **unknown**.
- Lint/UAST: the failing runtime reported JDK **25.0.3** (`AlreadyDisposedException`
  while initializing/disposal of UAST). Project AGP is 8.7.3 and Java compile target
  is 17. A fresh `--no-daemon lintDebug --stacktrace` under installed JDK **17.0.20.1**
  passed. Final report: **5 warnings** (2 `UnusedAttribute` in the manifest, 2
  oversized legacy clock vectors, 1 `SetJavaScriptEnabled` in the legacy WebView) and
  2 informational `AutoboxingStateCreation` recommendations. The compiler also
  reports the existing `VIBRATOR_SERVICE` deprecation. No cache invalidation,
  dependency/toolchain changes or lint suppressions were needed.

### Full-suite results and remaining blocker

Under JDK 17.0.20.1, full JVM tests passed **508/508** (0 failures/errors/skips).
`assembleDebug`, `lintDebug`, and `compileDebugAndroidTestKotlin` passed. The full API
35 emulator run completed **347 tests: 341 passed, 6 failed, 0 skipped**. The earlier
NativeText and SupportPresentation failures were absent. All 12 Months ↔ Seasons
responsive UI cases passed, as did its route test. The six failures were:

1. Four `PrepositionsArtworkScreenTest` restoration casts: the fixture selected the
   first three relation distractors without respecting `PrepositionsContent`'s
   pairwise compatibility rule. The validator correctly returned `Rejected`; the
   fixture now selects four compatible relations. Isolated result after correction:
   **4/4 PASS**.
2. One `SeasonsMatchScreenTest` placement assertion: the test clicked season/clue
   controls while they were outside the scroll viewport. A diagnostic assertion
   showed the wrong tap never became attempt 1. The test now scrolls controls into
   view before clicking and verifies that attempt; isolated result: **1/1 PASS**.
3. One `PrepositionsArtworkTest` source-art contract assertion: `snake.next_to` was
   expected to be narrower than `snake.on`, but both committed PNGs are 1448×1086.
   Their decoded widths therefore match and the crop assertion fails. Neither asset
   is a Phase 6 change; artwork was not altered. This remains an unresolved existing
   artwork/content acceptance issue and blocks a green full-suite/release claim.

The original six-failure broad run has therefore been diagnosed: five failures were
non-reproducible runner symptoms or invalid test expectations/fixtures and are now
covered by focused passing runs; the remaining art assertion is reproducible from
the source dimensions. The complete 347-test suite has **not** been rerun after the
two later test-fixture corrections, so do not infer a revised suite-wide count from
their isolated passes. Do not commit or push while the artwork failure remains, and
do not mark Phase 6 validation complete. Physical acceptance remains explicitly
deferred.

### Final checkpoint state

- PASS: full JVM **508/508**; assemble; lint under JDK 17; Android-test compilation;
  focused Months ↔ Seasons **13/13**; Months Build the Year **12/12**; NativeText
  visibility **8/8**; corrected support presentation **12/12**; corrected Prepositions
  artwork-screen restore **4/4**; corrected Seasons match UI **1/1**.
- FAIL: last full instrumentation run **341/347**, with the one unresolved
  Prepositions source-art dimension assertion; the two test-fixture corrections were
  verified individually but not in another full run.
- PASS: `git diff --check` is to be rerun after this documentation update.
- NOT RUN: physical-device acceptance (**deferred by owner**); CI (no push).
- Release status: **BLOCKED — do not commit or push**. Current source Phase 6 remains
  implemented, but release readiness and completed full automated validation are not
  established until the source-art contract is resolved and full instrumentation is
  rerun successfully.

Next action: review the `snake.next_to`/`snake.on` crop expectation with the art owner
without replacing or altering artwork implicitly; then rerun the full required suite,
build/lint, diff check, and only if all pass commit/push and inspect CI.

## Months Phase 6 — final automated validation follow-up (2026-10-12)

The asset review confirmed the existing `snake.next_to` image shows the snake and rock
fully visible on the same ground plane with a clear gap; `snake.on` shows the snake on
top of the rock. Both intentionally use the shared 1448×1086 4:3 canvas, so raster
width was not a valid proxy for the audited visual relationship. Removed only that
dimension-based assertion from `PrepositionsArtworkTest`; retained the test's
all-scenes decode and aspect-ratio checks. No artwork or content was changed.

After the already-recorded test-fixture corrections, the full API 35 emulator
instrumentation suite completed **347/347, 0 failures, 0 skips** (37m19s), including
the previously unresolved artwork test. Its focused class also passed **3/3**. The
previously reported full JVM **508/508**, `assembleDebug`, `lintDebug` under JDK
17.0.20.1, Android-test compilation, and focused Months ↔ Seasons **13/13** remain
passing evidence; final `git diff --check` is recorded at handoff. Lint warnings remain
as listed above. Emulator success is not physical-device acceptance.

Automated validation is complete for this checkpoint; Months Phase 6 is implemented.
Physical-device acceptance remains **NOT RUN — deferred by owner**. No commit, push or
CI run has occurred yet. Recheck the complete diff and whitespace, then commit/push the
scoped work and verify remote HEAD and GitHub Actions. Do not claim physical acceptance.

## Months Phase 7 — four-month Build the Year (2026-10-14)

Started on clean `main` at `35fc1299c015ae46966f2ca5556bd3199a0a244c`, matching
`origin/main`. Added an independently selectable four-month level alongside the
unchanged three-month level. Level 2 sequences four consecutive canonical month IDs
for all twelve possible starts, including October/November/December/January,
November/December/January/February and December/January/February/March. Seeded 5/10
rounds use distinct starts and reproducible shuffled orders that are never initially
solved. The accessible interaction reuses `OrderedPlacement`, with tap-select/tap-place,
keyboard activation, hints, replay, calm retry and bilingual instruction/example.

Level 2 has its own `activity.months.build_year.four`,
`skill.months.sequence.four` and `months-build-year-four-session` persistence contract.
Level 1 keeps `activity.months.build_year.three`, `skill.months.sequence`, its existing
journal and saved-session meaning. Shared completion schema/layout is unchanged.
The selector keeps both levels directly available and does not force advancement.

Validation under isolated Eclipse Temurin JDK **17.0.20.1** and API 35
`Medium_Phone_API_35` emulator:

- PASS — full JVM suite: **513/513**, zero failures/errors/skips.
- PASS — full API 35 instrumentation: **360/360**, zero failures/skips (final run).
- PASS — focused Level 1 Compose regression **12/12**; Months route **4/4**;
  new Level 2 Compose matrix **12/12**; `SupportPresentationTest` **12/12**;
  `RemainingSupportPresentationTest` **12/12**; `NativeTextVisibilityTest` **8/8**.
- PASS — `assembleDebug`, `lintDebug` and `compileDebugAndroidTestKotlin` under JDK 17.
- PASS — all twelve starting months, cyclic wrap, deterministic unique 5/10 rounds,
  non-solved shuffles, retry/support, Level 1 identity compatibility, Level 2
  persistence/restoration and 1×/1.5×/2× EN/DE portrait/landscape UI cases covered.
- PASS — `git diff --check` after documentation updates.
- NOT RUN — physical-device acceptance, **deferred by owner**. Emulator evidence does
  not establish TalkBack quality or physical-device acceptance.

Investigation and corrections: the first full emulator run encountered a system
failure in which Package Manager and installed test packages became unavailable; an
emulator reboot restored the service. A subsequent run exposed a Months route test
asserting selection before asynchronous UI feedback, plus existing support/text tests
whose waits depended on inherited orientation or did not wait for the requested
rotation. The test fixtures now wait for the selected state and explicit orientation;
no assertions were weakened. Isolated reruns passed, followed by the full 360/360 run.
No production regression was found. JDK 17 avoided the earlier JDK 25 Lint/UAST
initialization failure. Lint retains five existing warnings (two manifest
`UnusedAttribute`, two `VectorRaster`, one legacy `SetJavaScriptEnabled`) and two
informational recommendations; Kotlin still reports the existing deprecated
`VIBRATOR_SERVICE` warning.

README, roadmap, backlog, content, architecture, progress, navigation and QA documents
now describe the independently selectable four-month level and preserve six-/twelve-
month sequencing, adaptive difficulty, advanced calendar generalisation and physical
acceptance as deferred. Release: implementation commit
`888345d8231730e2a02ff09a6c5dd8f6196bed0c` was pushed to `main`; fetched
`origin/main` matched and the worktree was clean. GitHub Actions run
[38558006810](https://github.com/paulyglocke/Basti-Learning-Adventure-Public/actions/runs/38558006810)
passed both `build` (Android APK and release guards) and `browser-tests`. The
distribution job was skipped, as expected for this workflow. CI annotations note
Node 20 action deprecations, `setup-java@v4` deprecation and the upcoming
`ubuntu-latest` migration; these did not fail the run.

## Phase 4D.1 — Seasons route-test persisted-state correction (2026-10-21)

At clean baseline `38030a4ab279fd9a5137965da3d098a93bec592b`, the reported
`seasons-order-prompt` failure reproduced. The route test had restored an already
completed ORDER session; the app correctly displayed the German completion screen
and all four placed seasons. The test incorrectly required the active-round prompt.
Updated only `SeasonsChooserRouteTest` to assert the active prompt or, for a restored
completed session, the exact completion message, placed count and four localized
season labels. No app code, saved data, session identity, journal/checkpoint format,
wheel geometry or learning behavior changed. No data was cleared.

Validation: focused route **1/1 twice**; Seasons package **42/42**; complete API 35
instrumentation **569/569**, zero failures/skips; full JVM **555/555**, zero
failures/errors/skips; debug build, Android-test build, lint and `git diff --check`
passed. Lint has 0 errors and 5 existing warnings. `/data` showed about 819 MB free;
the debug APK is 369 MB and the test APK 2.9 MB. Test APK install and full direct
runner execution succeeded. The previous connected-install
`INSTALL_FAILED_INSUFFICIENT_STORAGE` remains historically recorded; no app APK
reinstall, emulator wipe or saved-data clearing was attempted. Detailed evidence:
[`docs/seasons-phase4d1-review/REVIEW.md`](docs/seasons-phase4d1-review/REVIEW.md).

Current work is uncommitted and ready for owner review. Physical-device and
child-usability acceptance remain owner-managed; UX-ANR-01 remains separate.
## Phase 5A.1 — Colours Explore/Find (2026-10-22; uncommitted)

Added a Colours chooser and bilingual Explore Colours / Find the Colour experiences
for `colour.red`, `colour.blue`, `colour.yellow` and `colour.green`. Explore stores
only a separate selected-colour checkpoint and emits no assessed progress. Find uses
`activity.colours.find_colour`, revision 1, its own journal and shared durable
progress events. The existing Sort learning code, four red/blue objects, activity
ID/revision/content version and CSR1 journal format remain unchanged. Home now opens
the chooser; the old direct `COLOUR_SORT` route remains for saved route compatibility.

Validation: full JVM **563/563**; focused API 35 Colours/Home **12/12**; debug build,
Android-test APK compilation, lint (0 errors, 5 existing warnings) and
`git diff --check` passed. Full app instrumentation was not run. Only three actual portrait
screenshots (chooser, Explore red, Find English) are in
[`docs/colours-phase5a1-review/gallery.html`](docs/colours-phase5a1-review/gallery.html);
the requested 18-state screenshot matrix remains incomplete. The pre-existing
`ColourSortRouteTest` fixture deletes its Sort journal on the API 35 test AVD; it was
run once, so that AVD's previous Sort journal may have been replaced. No physical
device was used. All work remains uncommitted and unpushed for owner review.
