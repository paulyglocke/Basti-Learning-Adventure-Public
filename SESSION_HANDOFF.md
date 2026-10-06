# Session handoff — 2026-09-27

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
