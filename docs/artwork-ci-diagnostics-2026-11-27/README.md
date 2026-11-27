# Artwork publication and first-error diagnostics — 2026-11-27

Follow-up to [consolidation failure triage](../consolidation-2026-11-26/FAILURE_TRIAGE.md),
approved by the owner's “go whats next?”. Baseline `8e3407f`; earlier uncommitted
plan/inventory work is preserved. Local verification is complete; hosted confirmation
is pending publication. No commit/push was requested for this task.

## Problem and correction

Diagnostic-only tests on the original runtime reproduced actual blank artwork in
Same Amount and Wash Hands: the affected selection passed only 3/5. A temporary
trace found 100 of 161 bitmap publications on worker threads after successful
decoding. A controlled regression then failed before the correction because the
publication callback lacked the main Looper. The trace itself changed timing and
passed 5/5; that result is retained separately and is not the fix.

`loadArtworkForUi` keeps decoding on IO and explicitly publishes on Main.immediate,
including when its caller uses an unconfined dispatcher. BundledArtwork and the
Same Amount board loader share this boundary. Cancellation rejects obsolete results.
Temporary trace logging was removed. See [diagnosis and scope](ROOT_CAUSE.md).

## First-error evidence and CI

The original 10s artwork bounds, exact/minimum counts, merged-tree choices and later
displayed/behavior assertions remain. A failed wait records phase/tag, expected/last
count, original exception, a cached unmerged tree, bounded Activity/window observation
and a best-effort genuine display PNG. Nullable accessibility observations have an
explicit reason. Diagnostic errors are supplementary; the original failure wins.
`activities.fontScale` records Android configuration, not any component-local
Compose density override (Wash Hands tests use a local large-text override).

The Python CI wrapper collects external-files/ci-diagnostics before emulator teardown,
bounds adb probe/pull to 15s/30s, and preserves Gradle's exit code. It refuses
connected-device Gradle tests unless only the selected ready emulator is attached.
The original 24 CI classes retain relative order; two new classes add four cases,
for 26 classes / 40 expected cases. Reports are always uploaded with existing results.

## Verification

| Check | Result / evidence |
| --- | --- |
| Publication regression before / after | 1/2 before (main-thread assertion fails), 2/2 after; cancellation passes both. [Before](evidence/local-publication-before.txt), [after](evidence/local-publication.json). |
| Original runtime affected routes/layouts | 3/5; genuine missing images. [Runner](evidence/local-affected-before-trace.txt), [first failures](evidence/before-trace-diagnostics/ci-diagnostics/). |
| Corrected affected selection | 5/5: Same Amount production route plus four Wash Hands layouts. [Strict summary](evidence/local-affected.json). |
| Complete configured selection | 40/40, zero failures/errors/skips, 207.781s. [Raw runner](evidence/local-configured.txt), [strict summary](evidence/local-configured.json). Includes both new classes and original 36 cases; counts overlap the focused runs. |
| Full JVM / both APKs / lint | 751/751; assemblies pass; lint 0 errors, 7 warnings, 2 information. [Build](evidence/build7-fix.log), [summary](evidence/build-validation.json), original JVM XML ZIP/lint XML retained. |
| Python runner checks | 7/7: exit preservation, bounded collection, absent/failed diagnostics, filesystem errors, additional/offline devices. [Result](evidence/runner-python-verified.txt). |
| Artifact identity | Final built and installed app/test hashes match. [Built](evidence/built-apks.json), [installed](evidence/local-installed-identities.json), [tested sources](evidence/tested-source.json). |
| Cleanup | All 88 original app-file paths/bytes and settings restored; generated data retained intact; owned emulator closed. [Restoration](evidence/device-restoration.json), [shutdown](evidence/device-cleanup.json). |

The diagnostic fixture deliberately requests missing artwork and catches the original
timeout; its `diagnostics-self-test-*` reports are induced faults, not production
failures. Early sandbox/build errors and the initial fixture's nullable-JSON failure
are retained. Corrected diagnostics passed 2/2 independently and within the final 40.
The final pulled diagnostics directory accumulates earlier reports as well as the
final fixture report; its old production failures are not new failures in the 40/40 run.

Two existing route captures were pulled without alteration. The [German chooser](captures/conservation-chooser-de.png)
visibly contains three dinosaurs. Despite its filename, the [English capture](captures/conservation-chooser-en.png)
contains the Home catalogue, so it is not accepted as English chooser visual evidence.
The runner's artwork assertions passed independently; screenshot timing in the
existing capture helper needs separate review before reusing these files as a gallery.
These pulls were not separately device-hash verified; the integrity manifest records
the local bytes only. Original failure screenshots remain the stronger visual diagnosis.

## Environment and limits

Owned Basti_Pattern_20261111/API 35/arm64 only, headless SwiftShader software rendering,
1080×1920 / density 420, animations 0/0/0. Hosted Linux x86_64/google_apis is separate.
A connected physical phone was never targeted; local tests used explicitly targeted
raw AndroidJUnitRunner, not connected Gradle. No wipe, uninstall or app-data clear.
Original 88 app files were copied intact/hash-verified before execution, restored
afterward, with generated directories retained in cache/artwork-diagnostics-20261127-generated.
Original geometry/settings and Medium Phone config hash match; no owned emulator remains.

This demonstrates a publication-confinement defect and a locally passing candidate
correction. It does not establish that every historical hosted timeout had this exact
cause. Hosted Android remains OPEN until the candidate runs there. Historical Compose
SlotTable disposal and the separate emulator-system watchdog remain separate open
investigations. Physical/child/TalkBack/switch/audible-voice acceptance remains pending.
