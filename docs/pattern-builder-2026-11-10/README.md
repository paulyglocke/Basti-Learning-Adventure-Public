# Pattern Builder — native starter

Baseline: `456ab28`. Owner approved Pattern Builder after the Memory Pairs push.
Implementation and selected local verification completed on 11 November 2026.
The owner subsequently authorized committing and pushing this checkpoint to main
on 11 November. Retained validation and artifact identity describe the pre-commit
state; hosted checks for the new commit are separate.
The previous hosted run 42793753994 on the baseline passed all required jobs.

[Screenshot gallery](index.html) · [Exact validation and capture inventory](evidence/validation.json)

AB, AAB and ABC levels alternate animal and shape contexts in seeded 5/10 rounds.
Two full example groups are pinned above the workspace on narrow screens and
beside it on wider screens while the child builds the next group from
reusable picture choices. Placements/removals/Undo and separate pronunciation
controls are unscored; only a complete changed Check records an attempt. Wrong
work remains editable, successful work locks, and Next is explicit. Help explains
the repeating group without filling the board. No timer or attempt grade.

Animal symbols reuse six canonical Vocabulary IDs, words and artwork. Circle,
square and triangle are typed semantic symbols rendered with native Canvas in one
shared colour; colour is not the matching rule. No new raster assets or web runtime.
Pattern repetition deliberately permits repeated symbols, while the existing
unique-item SequenceAssembly/OrderedPlacement contracts remain unchanged.

The exact tasks, choice order, placements, last checked answer, support and pending
shared progress live in three level journals. Options/language/recreation preserve
the board; fresh-owner restore is silent. Replay/Help and speech follow the shared
lazy controller and all/questions/off policy. Completion reuses shared balloons.

Each round alternates contexts, has no identical question repeats, and uses unique
animal combinations. Play again changes the plan and avoids an immediate identical
question across the round boundary. Separate rule skills and actual animal/shape
contexts record app interactions, not mastery claims.

Verification:

| Check | Result |
| --- | --- |
| Full JVM suite | **689/689**, zero failures/errors/skips |
| Debug app and instrumentation APKs | Both built successfully |
| Lint | 0 errors, 7 existing warnings, 2 informational findings |
| Final API 35 phone selection | **10/10**, zero failures/errors/skips |
| Separate tablet layouts | **4/4**, zero failures/errors/skips |
| Authored/generated catalogue and motion parity | Passed |

The phone selection covers the actual production route, owner/lifecycle, four
layout configurations, startup and three existing sweep cases. It completes
AB/AAB/ABC in 5/5/10-question rounds, checks partial/wrong/locked recreation,
real Options/language changes, Help and pronunciation, then verifies exactly
23 shared attempts (including three intentional retries), three completions and
balloon rewards. Layout tests exercise all three rules and both contexts in EN/DE,
1×/1.5×/2× text, portrait/landscape, keyboard and >=48dp targets. The pinned example
is checked after scrolling to the work controls. The owner test verifies lazy
Sound Off, rejected late configuration results, background cancellation and silent
fresh-owner recovery.

The tablet run repeats the four layout configurations on the same source and APKs.
All 49 genuine final screenshots are indexed: 17 production-route phone captures,
16 phone component captures and 16 tablet component captures. Component tests use
the test theme and are labelled separately. Representative chooser, partial and
retry boards, animal/shape solutions, reward, ordinary-text tablet and German 2×
phone/tablet captures were visually reviewed; this does not claim individual
review of every capture. Phone screenshots are 1080×2400/2400×1080; tablet
overrides are 1600×2560/2560×1600.

Physical S24/Fire, actual TalkBack/switch, child comprehension, installed offline
voice quality and signed in-place upgrade acceptance remain owner-managed checks.
These emulator results do not authorize legacy removal.

Review provenance: work began on 10 November and resumed on 11 November. The
earlier disposable emulator lost its temporary backing directory and could no
longer launch. Its failed attempts ran no app assertions; those reports and the
successful pre-layout-refinement run are retained in `evidence/`. Final checks
use an independent persistent `Basti_Pattern_20261111` API 35 AVD with 12 GB data,
four virtual CPUs and 6 GB RAM. Only the original hardware configuration was
copied; original emulator userdata was not copied or changed. Tablet captures
use display overrides on this test emulator, not a physical tablet.
After testing, display overrides were reset to 1080×2400/density 420, airplane
mode restored to its original 0, and the test emulator gracefully closed. The
original Medium Phone hardware configuration hash is unchanged; original
userdata and physical devices were untouched. The lost earlier disposable state
could not be restored. [Cleanup evidence](evidence/emulator-cleanup.json).

The retained [JVM summary](evidence/jvm-summary.json), [raw JVM reports](evidence/jvm-results.zip),
[build log](evidence/package-build-lint-run.txt), [lint findings](evidence/lint-summary.json)
and [tested source/APK identity](evidence/instrumented-artifact.json) make the
scope of these checks explicit. The configured CI selection adds the two new
route/owner tests to the previous 22 cases. A local selected run does not stand
in for all 24 CI cases or the complete app instrumentation suite. No new browser
run is claimed because retained web runtime source is unchanged.
Retained text logs normalize trailing whitespace; report content is unchanged.
