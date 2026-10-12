# Tell Me content variety — complete draft

Content revision 11 is validated at source revision `466315c`: full JVM suite
490/490, 32 Python content tests, generated Kotlin check, and 88 focused API 35
emulator tests pass. See [PILOT_VALIDATION.md](PILOT_VALIDATION.md) for command
scope and remaining review gaps. Independent German, exact C-bundle manual layout,
TalkBack and physical S24/Fire Max acceptance remain open; no content was changed
during validation.

The revision 10 and earlier notes below are historical checkpoints. Their local,
uncommitted and unrun status statements describe those dates, not the current tree.

## Revision 11 third-question pilot — 2026-10-10

Three pictures now have complete EN/DE C bundles: Ocean .03 imaginary crab event,
Classroom .02 next step after asking for help, Zoo .08 penguin prediction. Current
PNGs were directly inspected. [THIRD_QUESTION_PILOT.json](THIRD_QUESTION_PILOT.json)
records exact C bundles, unchanged A/B bundles, picture evidence and hashes.
The 78 other scenes retain two bundles: 165 bilingual slots total. Existing
rotation already uses each scene's bundle count; no runtime change was needed.

All 32 focused Python content checks and generated-output check pass. JVM tests
cover six complete visits across mixed two-/three-slot scenes and C support
pairing/stale callbacks/reset. API 35 instrumentation covers C through Options,
EN/DE change, Activity recreation, background/return and Home wraparound, plus
screen wrapping/scrolling and artwork loading. Independent native-speaker and
physical-device acceptance remain pending. Do not expand C beyond the pilot without
separate authorization and acceptance.

The sections below describe previous checkpoints; current counts are 78 two-slot
scenes and three three-slot scenes, with A/B/C/A rotation in the pilot.

## Revision 10 short-example checkpoint — 2026-10-10

Reviewed all 162 question/example/model pairs. Replaced 121 short EN/DE examples
across 73 scenes where naming the subject or an unrelated word did not model the
requested response. Examples now offer a brief action, location, feature, feeling,
reason, sequence or pretend utterance, derived from the existing model. Appropriate
names, quantities and already useful phrases remain. Names and gestures are still
accepted; copying an example is optional and other ideas remain welcome.

Only childExample changed within bundles. Questions, starters, words, models,
guidance, purpose totals, scene IDs, pictures and rotation remain intact.
[CHILD_EXAMPLE_CORRECTIONS.json](CHILD_EXAMPLE_CORRECTIONS.json) records the full
revision-9 bundle snapshot for all 81 scenes and exact replacements. Earlier
ledgers remain historical; preservation tests verify their explicit successors.
Image hashes are checked, but this pass claims no additional visual acceptance.

Validation: 31/31 focused Python tests, generator --check and git diff --check
passed. Android/JVM/instrumentation were not run; independent native-speaker and
physical-device acceptance remain pending. Changes are local and uncommitted.

Next checkpoint: author a small picture-reviewed pilot of third-question bundles,
with three-slot rotation/support and lifecycle validation before wider expansion.

## Revision 9 clue/story/language checkpoint — 2026-10-10

Improved 12 complete EN/DE bundles across 11 scenes: four clue invitations, four
story invitations and four wording/support corrections. Clues now model visible
details and explain the optional guessing exchange. Story openings and pretend
dialogue offer manageable first events; one word or gesture is still accepted.
Classroom .02 help-seeking is conditional, Jungle .09 avoids “Handlung”, Farm .04
removes “the one chicken”, and Farm .06 asks naturally about fruit/Obst.

All 11 current PNGs were directly inspected. Exact changes and preserved slots,
reference metadata hashes and artwork hashes are in
[CLUE_STORY_CORRECTIONS.json](CLUE_STORY_CORRECTIONS.json). Farm .06 A intentionally
supersedes the unchanged-A snapshot from revision 8; its B bundle remains intact.
Ocean .08 B now uses properties and Woodland .09 B uses story in the existing
editorial taxonomy. Counts and scene tables reflect those changes; all 162 prompts
remain distinct per language. No runtime skill/progress tags were added.

Validation: 30/30 focused Python tests, generator --check and git diff --check
passed. Android/JVM/instrumentation were not run; independent native-speaker and
device acceptance remain pending. Changes are local, uncommitted and unpublished.

Next checkpoint: improve the remaining name-only child examples and support
pairing before authoring third-question bundles.

## Revision 8 spatial checkpoint — 2026-10-10

Simplified 17 spatial questions across 14 scenes into short EN/DE “Where…?”
invitations. Removed abstract comparison wording and locations supplied in the
question. Matching adult guidance names optional reference objects, accepts other
accurate locations and keeps repetition optional. Mountains .03 guidance lets the
child choose a flower group; Farm .06 makes the apple comparison optional. Park
.06 German support uses “Kegel”. Existing starters, short examples and other
support remain paired. Other slots and reference metadata are preserved.

All 14 current PNGs were directly inspected. Exact before/after bundles and
preservation hashes are in [SPATIAL_PROMPT_CORRECTIONS.json](SPATIAL_PROMPT_CORRECTIONS.json).
Objective totals remain unchanged; 162 prompts remain distinct in each language.
Validation: 29/29 focused Python tests, generator --check and git diff --check
passed. No Android/JVM/instrumentation run; native-speaker and device acceptance
remain pending. All changes are local, uncommitted and unpublished.

Next checkpoint: audited clue/story invitations and remaining awkward bilingual
wording, before adding third-question bundles.

## Revision 7 question-variety checkpoint — 2026-10-10

Seven action/action pairs now have a distinct second-visit invitation: Ocean .01
and Zoo .02 prediction; Jungle .01 pretend dialogue; Woodland .02 a possible
reason; Sky .01 visible comparison; Mountains .02 a possible next step; Classroom
.01 creative expression. First-visit bundles and all seven scenes' reference
metadata remain intact. Complete matching EN/DE support accepts short responses,
alternative ideas and optional repetition, with hypothetical language where needed.
Current PNGs were directly inspected; QUESTION_VARIETY_CORRECTIONS.json records
before/after bundles, picture evidence and preservation hashes. The aggregate
review/counts use the original twelve purpose labels, with sequencing and creative
expression under the broad story label. These are editorial labels, not skill results.

Validation: 28/28 focused Scene Description Python tests passed, including actual
production image hashes, generated output, preserved first bundles/reference
metadata and the original English fingerprint. Generator --check and diff whitespace
checks passed.

Prior revision-6 fixes remain intact. Images, identities, ordering, navigation and
A/B rotation are unchanged. Local changes are uncommitted and unpublished. Native
German and device acceptance remain pending; Android/JVM/instrumentation are not
run for this content-only checkpoint.


## Revision 6 correction checkpoint — 2026-10-10

Based on clean main `5acf611`. Two picture contradictions are corrected locally:
Woodland .06 B uses the fox in its den; Classroom .06 A uses visible painting tools.
Matching EN/DE support and inaccurate source visual notes are updated, with unchanged
artwork, identities, ordering and rotation. EDITORIAL_CORRECTIONS.json preserves
exact reference changes without replacing the original regression fingerprint.
Validation: 27/27 Scene Description Python tests passed with real production images;
generated output check and diff whitespace checks passed. Android/JVM/instrumentation
were not run; native-speaker and device acceptance remain pending. Changes are local
and uncommitted. The publication/build statements below are historical revision-5 notes.

Based on main `8c48de18cec5183dfb136414d7bddf502b087c14`; owner authorized committing/pushing before Android validation.
The owner's request to improve the remaining 79 scenes is now included: all 81
existing pictures have two explicit EN/DE prompt/support bundles. This replaces
the previous two-scene scope. See [REVIEW.md](REVIEW.md) for every prompt, theme
counts, unchanged artwork assumptions and review limits; [CONTENT_CHANGES.json](CONTENT_CHANGES.json)
records every starter, word, model, child example, parent note and picture-evidence link.

## Behavior

Each actual scene entry selects the next Activity-lifetime slot: A, B, A.
The chosen slot survives Help/model reveal, disclosure, language/Options, recreation
and background return. Unvisited pictures consume nothing. Home resets conversation
but retains the bounded per-scene cursors until ViewModel disposal; process restart
resets both. The nine-category/nine-picture order and optional reward remain.
One “Next picture” / “Nächstes Bild” press advances immediately, with no required
model phase. Help reveals the selected sentence model along with its starter and words. All 81 scenes now offer bounded Help/model support in both slots; the grown-up
example and guidance also match that slot. No random difficulty, database, speech,
scores, answer capture or artwork is added.

## Clock feedback included

The shared Explore/Practice clock now shows minute numbers 0–59, with larger bold
multiples of five. Hours/digital hours/short-hand legend and line use dinosaur green;
minute equivalents use dragon red. Colours match the darker outlines in the existing
vector files for readable contrast. Practice digital time remains hidden until solved.
This is presentation only; angles, dragging, audio, checking and persistence are unchanged.
Rendered minute-ring readability, especially on narrow screens, needs visual QA.

## Evidence

26 isolated Python metadata/authoring checks passed, with one actual-PNG review/hash
check explicitly skipped. Generated Kotlin and the original English reference
fingerprint pass their offline fixture checks. Fixtures are outside production paths.
All 162 prompt strings are distinct in EN and DE. That is an editorial measure,
not proof of learning quality, native-speaker acceptance or visual correctness.

Android validation was attempted but blocked before Gradle could start: the wrapper
cannot obtain Gradle 8.9; the latest attempt failed at socket creation with
“Operation not permitted”. Earlier proxy checks also returned connection refused. JVM/Android/lint/
instrumentation therefore remain unrun. The text-source snapshot is not
a full checkout, has no production PNGs, and the environment cannot obtain SDK/Gradle
through its failed proxy. No APK exists for this draft. German and current-image
review remain pending, including the owner-replacement Park .09.

## Resume in a complete checkout with SDK and working network

1. Check main is still the pinned revision; review/rebase if it changed.
2. Apply the complete saved patch; retain all original artwork.
3. Run `python3 -m unittest discover -s scripts -p 'test_*.py'` and
   `python3 scripts/generate_scene_descriptions.py --check` against real assets.
4. Run `./gradlew testDebugUnitTest assembleDebug lintDebug compileDebugAndroidTestKotlin`.
5. Run Tell Me instrumentation with actual images, EN/DE, large text, repeat visits,
   one-press Next, optional Help/model, Home/Again, Options/language, recreation and background return.
   Run Clock screen/practice/route tests and visually review minute labels at all
   representative hand positions in narrow portrait, short landscape and large text.
6. Review German and question/support meaning against each current picture.
7. Once required checks pass, commit and push under the owner's existing authorization.

Owner subsequently authorized publication before Android checks. This remains
unvalidated on Android; do not describe it as a successful build or validated
device experience until CI and visual review establish that evidence.

## Publication attempt — 2026-10-10

The owner authorized pushing before Android validation. GitHub rejected both
blob/tree creation with HTTP 403 “Resource not accessible by integration”.
Nothing was committed or pushed, and no signed workflow was started. CLI
authentication also failed. Restore repository write and Actions dispatch access;
GitHub is already installed. The existing workflow signs only a manual dispatch
on main with `distribution=true`, after its build/browser checks pass.
