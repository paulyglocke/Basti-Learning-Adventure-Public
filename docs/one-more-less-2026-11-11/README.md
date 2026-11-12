# One More / One Less — native starter

Baseline: `4ff84245018f59e5b407a8727b34f6b31fae36ab`. Work began on 11 November;
implementation and selected local verification completed on 12 November 2026.
The owner subsequently authorized committing and pushing this checkpoint to main
on 12 November. Retained evidence describes pre-commit local validation; hosted
checks for the new commit are separate.

[Screenshot gallery](index.html) · [Exact validation and capture inventory](evidence/validation.json)

Four choices isolate Add one and Take one away within 5/10. A fixed starting frame
stays visible while a separate work frame scrolls. Taps change one occupied/empty
space, Undo restores up to 100 moves, and only changed Check submissions create
attempts. Equivalent rearrangements of an unchanged wrong count cannot re-score.
Wrong work remains editable; success locks and shows a derived equation until Next.
Zero is an explicitly empty group, including one minus one and zero plus one.
Help explains the operation without filling the board or awarding success.

Canonical Vocabulary IDs/labels/artwork and NumberBonds number IDs are reused.
Seeded 5/10 rounds balance starting amounts, vary animals, never repeat an identical
question and avoid adjacent repeated starts/animals. Play again changes the plan
and avoids an immediate round-boundary repetition. Four independent atomic journals
retain plans, masks, undo history, checked counts/support and pending shared progress.
Options/language/recreation preserve the board and a saved round's length. Fresh
owners restore silently. Speech follows shared lazy ALL/QUESTIONS/OFF policy;
completion uses shared balloons. No new raster artwork, browser runtime or codec.

Adding routes reached the JVM method-size limit in MainActivity's large composable.
The six Quantity Change/Pattern/Memory chooser/game render branches were extracted
into a private composable taking the existing screen/padding/language. Existing
Pattern/Memory production routes, startup and bilingual Home passed in the selected
regression run. Home now has 32 entries: 29 native and three retained web routes.

| Check | Result |
| --- | --- |
| Full JVM | **704/704**, zero failures/errors/skips |
| Debug app/test APKs | Built successfully |
| Lint | 0 errors, 7 existing warnings, 2 informational findings |
| New activity route/owner | **2/2** in the combined run |
| Final phone component layouts | **4/4**, zero failures/errors/skips |
| Final tablet component layouts | **4/4**, zero failures/errors/skips |
| Catalogue generation/motion parity | Passed |

The production route completes all four choices in 5/10/5/10-question rounds,
with real Options/language changes, partial/wrong/locked recreation, Undo, Help,
zero, equations, navigation, completion replay and shared balloon reveals. It
verifies exactly 34 attempts (30 correct plus four intentional retries) and four
shared completions. The owner test covers lazy Sound Off, late configuration
results, background cancellation and exact silent fresh-owner restoration.

Final layout cases cover all four configurations at both starting-amount boundaries,
EN/DE, 1×/1.5×/2×, portrait/landscape, keyboard input, Undo, explicit zero, constant
slot size, >=48dp targets and pinned references while controls scroll. These are
component tests using the test theme; the gallery labels them separately from
actual production routes. All 54 genuine captures are indexed: 22 production phone,
16 final phone components and 16 final tablet components. Representative chooser,
partial/solved/zero/reward, final phone 2×, tablet ordinary text and tablet German 2×
screenshots were visually inspected. This does not claim individual review of every
capture. Phone dimensions are 1080×2400/2400×1080; tablet display overrides are
1600×2560/2560×1600.

The combined selection was **33/34**, with one existing German portrait Wash Hands
image wait timeout. Its unchanged isolated rerun passed **4/4** on the same APKs.
The original report is retained; no root cause/fix or clean single 34-case pass is
claimed. The selection includes all 26 configured focused-CI cases plus new layouts,
Counting Tray and bilingual 32-entry Home. It is not full app instrumentation or
new hosted CI evidence. No new browser result is claimed; web runtime is unchanged.

The first tablet run was **3/4**: a test sampled portrait tile width before the
landscape window settled. Only its fixture changed: it now waits for actual decor
rotation/geometry and measured Compose orientation before measuring. Original
constant-size, target, keyboard and pinned-reference assertions remain intact.
Final phone/tablet **4/4 each** use that fixture. Initial and final test APK/source
identities are recorded separately; runtime source and app APK are exactly the same
throughout, including the installed app APK hash checked directly on the emulator.

Several final Gradle attempts ran no app tests: a DDMLib property timeout, Android
package-service unavailability after emulator system failure, and stalled discovery
that was canceled. Those logs/reports are retained. The same persistent AVD was
restarted without wiping data. Direct AndroidJUnitRunner then completed both final
four-case layout runs. Raw runner status text and clearly labelled derived JSON
summaries preserve the results; they are not presented as Gradle-generated XML.
The application and compiled assertions were unchanged by the runner fallback.

Testing used only the separate API 35 `Basti_Pattern_20261111` AVD, with four CPUs,
6 GB RAM and 12 GB data. Display was restored to 1080×2400/density 420, airplane
mode restored to original 0, and the emulator gracefully closed; adb reports no
attached device. Original Medium Phone configuration hash is unchanged, original
userdata was untouched, and no physical device or data wipe was used. See
[cleanup](evidence/emulator-cleanup.json), [initial artifact](evidence/instrumented-artifact.json),
[final fixture artifact](evidence/layout-final-artifact.json), [raw JVM ZIP](evidence/jvm-results.zip),
[combined phone report](evidence/phone-combined-initial.xml), [Wash Hands rerun](evidence/wash-hands-recheck.xml),
[final phone summary](evidence/phone-layout-final-summary.json) and
[final tablet summary](evidence/tablet-layout-final-summary.json).
Retained text logs normalize trailing whitespace; report content is unchanged.

Physical S24/Fire, actual TalkBack/switch use, child comprehension, installed offline
voice quality and signed in-place upgrade acceptance remain NOT RUN — owner-managed
checks. These emulator results do not authorize legacy removal.
