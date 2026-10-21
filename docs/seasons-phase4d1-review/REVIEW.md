# Phase 4D.1 — Seasons regression diagnosis (2026-10-21)

## Baseline and original failure

Investigation began on clean `main` at `38030a4ab279fd9a5137965da3d098a93bec592b`,
matching `origin/main`. The Phase 4D review's original report is preserved in
[`docs/vocabulary-phase4d-review/REVIEW.md`](../vocabulary-phase4d-review/REVIEW.md):
the route test failed after selecting ORDER because it could not find
`seasons-order-prompt`.

The failure reproduced independently on the API 35 `Medium_Phone_API_35` emulator.
The exact assertion was:

```text
java.lang.AssertionError: Action performScrollTo() failed.
Reason: Expected exactly '1' node but could not find any node that satisfies:
(TestTag = 'seasons-order-prompt')
```

The original stack ended at
`SeasonsChooserRouteTest.illustratedHomeChooserEveryModeOptionsBackAndSharedMonthsRoutesRemainAvailable`.
The diagnostic rerun recorded `selection.phase=ORDER`, selected season
`season.autumn`, `busy=false`, `saveFailed=false`, and `imageFailed=false`. The
ordering state was present. The screenshot and unmerged Compose hierarchy showed
the valid ORDER completion screen: “Abenteuer geschafft!”, the completion message,
“4 von 4 Jahreszeiten eingeordnet”, and the four placed labels in canonical order:
Frühling, Sommer, Herbst, Winter. The active-round prompt is intentionally absent
from this completed-session screen. The captured frame is
[`order-assertion-failure.png`](evidence/order-assertion-failure.png) (1080×2400).

Selected hierarchy evidence from that failing assertion:

```text
Tag: 'seasons-complete'
Text: 'Du hast die Jahreszeiten geordnet! Nach dem Winter kommt wieder der Frühling.'
Tag: 'seasons-placed-count'
Text: '4 von 4 Jahreszeiten eingeordnet'
Tag: 'placed-season.spring'   Text: '1. Frühling'
Tag: 'placed-season.summer'   Text: '2. Sommer'
Tag: 'placed-season.autumn'   Text: '3. Herbst'
Tag: 'placed-season.winter'   Text: '4. Winter'
```

## Root cause and correction

This was a persisted-session assumption in the route test. The test reused the
emulator's valid, completed ORDER journal, but always asserted the tag used only by
an active order round. `SeasonsViewModel.openPhase(ORDER)` correctly restored the
existing session through `SeasonsOrderHost`; `SeasonsScreen` correctly rendered its
completion UI. Route dispatch, lifecycle, async settling, and application content
were not defective. No application state was cleared to make the test pass.

`SeasonsChooserRouteTest` now checks the user-visible ORDER result for either state:
an active session must render its prompt; a restored completed session must render
the exact localized completion message, completed placement count, and ordered
season labels. This retains a meaningful destination/content assertion and leaves
saved-session behavior intact. The fix touches no production source, session IDs,
journals, checkpoints, progress formats, learning rules, artwork or wheel geometry.

The corrected route test passed twice in succession against the same persisted
completed ORDER session. The full Seasons package passed 42/42, and the complete
API 35 instrumentation suite passed 569/569 with zero failures and zero skips.

## Emulator storage

At inspection, `/data` reported 5.8 GB total, about 4.8 GB used and 819–820 MB
available (diskstats: 839,124 KB free). The debug APK is 369 MB and the Android-test
APK is 2.9 MB. This confirms the emulator was close to full for a connected install
of the large debug APK; it explains why `INSTALL_FAILED_INSUFFICIENT_STORAGE` is
credible, although the package manager's earlier install trace was not retained to
prove its exact temporary-space calculation. Direct test-APK installation succeeded.
The app APK was already installed; no app reinstall, data clear, session deletion,
AVD wipe or package change was attempted. The full suite then ran directly through
the installed runner.

## Validation

- Original focused test before correction: **1 test, 1 failure**, reproduced with
  the missing active-round prompt assertion.
- Corrected focused test: **1/1 passed**, twice consecutively.
- Complete Seasons instrumentation package: **42/42 passed**, zero failures/skips.
- Complete API 35 instrumentation suite: **569/569 passed**, zero failures/skips.
- Full JVM suite, forced execution: **555/555 passed**, zero failures/errors/skips.
- `assembleDebug`: passed.
- `assembleDebugAndroidTest`: passed.
- `lintDebug`: passed, 0 errors and 5 existing warnings (2 `UnusedAttribute`, 2
  `VectorRaster`, 1 `SetJavaScriptEnabled`).
- `git diff --check`: passed after the implementation and documentation updates.

Physical-device, child-usability, TalkBack, switch-access and offline-voice
acceptance remain owner-managed. UX-ANR-01 remains separate. The earlier Phase 4D
failure remains documented as a real test failure at that time; this follow-up
records its cause and correction without rewriting that history.
