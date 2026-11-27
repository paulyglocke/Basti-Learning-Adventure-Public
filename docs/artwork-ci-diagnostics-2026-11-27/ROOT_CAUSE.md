# Missing artwork: diagnosis and candidate correction

## Observed defect

The unmodified runtime APK with diagnostic-only tests completed the affected selection
with three passes and two failures. The first-failure screenshots and unmerged trees
show actual blank picture slots: two dinosaurs instead of three in the Same Amount
chooser, and a missing wet-hands picture in a Wash Hands landscape layout. These are
not merely hidden accessibility nodes or a need for a larger timeout.

[Original runner](evidence/local-affected-before-trace.txt),
[original image/tree/JSON reports](evidence/before-trace-diagnostics/ci-diagnostics/).
The local failing phase/parameter differs from the historical hosted failure, so this
reproduces the shared missing-artwork problem rather than the exact original CI run.

## Publication evidence

A temporary runtime trace recorded successful decoding followed by state publication
from worker threads: 100 of 161 publication events had thread IDs different from the
application main thread. The caller’s effect dispatcher did not always confine the
continuation to main. The traced five-case run passed; logging perturbs scheduling,
so that pass is not a correction or proof the problem was harmless.

[Verbatim trace](evidence/trace-final.txt),
[derived thread observations](evidence/trace-publication-analysis.json),
[temporary trace source](evidence/BundledArtwork.trace.kt.txt),
[trace artifact identities](evidence/trace-identities.json).
Temporary runtime trace logging was removed before the candidate correction.

The existing load/publish operation was extracted without adding a main handoff and
tested with an unconfined caller and controlled worker completion. The new regression
fails deterministically: the publication callback has no main Looper. The separate
cancellation case passes, proving the missing guarantee is publication confinement,
not a deliberate late-result acceptance policy.

[Fail-before result](evidence/local-publication-before.txt),
[exact pre-correction helper](evidence/ArtworkLoad.before.kt.txt),
[pre-correction identities](evidence/publication-before-identities.json).

## Correction

`loadArtworkForUi` decodes on Dispatchers.IO and publishes through
Dispatchers.Main.immediate, independent of its caller’s dispatcher. Cancellation
continues to reject a result from an obsolete effect. BundledArtwork and the Same
Amount board loader use that shared boundary; the board’s loading/failure reset is
also confined to main. The bounded bitmap cache, asset keys, UI layout, content,
journals, audio policy and assessment behavior are preserved.

The original 10s readiness bounds/count predicates and later behavior assertions
remain. First-error diagnostics and the CI collection wrapper stay available to
investigate future failures; no retry or silent replacement substitutes for success.

## Verification gate

Local validation is complete: publication regression 1/2 before and 2/2 after,
affected routes/layouts 5/5 after, complete configured selection 40/40, full JVM
751/751, both APKs and lint passed. Python CI-wrapper checks passed 7/7. Final
built/installed artifact identities match; all 88 original app files/settings were
restored and the owned emulator closed. See [results and raw evidence](README.md).

The controlled regression establishes publication confinement, while the full run
supports this candidate correction for the missing-artwork symptoms. Neither proves
every historical timeout had the same cause; the exact earlier hosted execution was
not traced. Hosted success requires a later authorized push. Historical Compose SlotTable
disposal and the separate emulator-system watchdog failure are not declared fixed
by this artwork correction. Physical and audible-voice acceptance remain separate.
