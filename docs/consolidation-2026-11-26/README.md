# Consolidation checkpoint — 2026-11-26/27

Owner approved saving the [plan](../CONSOLIDATION_PLAN.md) and starting checkpoint 1.
Baseline `8e3407f7f22eec7e1622855214c2bf2e58a19990`; initially clean main/origin/main.
No agents used. No commit/push requested for these changes.

## First work completed

- Saved the four-checkpoint plan and linked the approved sequence from the master
  roadmap and backlog, including the small curated Today’s Adventure reprioritization.
- Reconciled all 36 live Home entries against source cards, category/dispatch mapping
  and independent Home route expectations: 33 native, three web, Maths 20.
  [Current activity/acceptance inventory](../ACTIVITY_ACCEPTANCE.md).
- Corrected stale README/brief/backlog summaries, including Prepositions revision 4,
  implemented number-bond/conservation/story starters and current hosted status.
- Prepared the [batched physical review](../DEVICE_ACCEPTANCE_BATCH.md), preserving
  named older S24 passes and making pending/blocked owner-managed checks explicit.
- Retrieved original hosted reports/logs and located both failures at specific
  artwork-readiness waits. Added a repeatable strict report inspector; existing
  application and Android-test sources/APKs remain unchanged.

## Hosted evidence

| Run / source | Build | Browser | Android |
| --- | --- | --- | --- |
| [212 / 8703c4e](https://github.com/paulyglocke/Basti-Learning-Adventure-Public/actions/runs/45405409410) | Passed | Passed | 33/34 passed; final Same Amount chooser artwork wait |
| [213 / 8e3407f](https://github.com/paulyglocke/Basti-Learning-Adventure-Public/actions/runs/45839921923) | Passed | Passed | 35/36 passed; Wash Hands German portrait artwork wait |

[Detailed triage and next engineering action](FAILURE_TRIAGE.md).
[Run 212 derived XML summary](evidence/hosted-212-summary.json),
[run 213 derived XML summary](evidence/hosted-213-summary.json).
Both report ZIPs are intact and SHA256-match the GitHub artifact digests; decoded
job text and original suite/case logcat bytes are retained. Connector-provided
archive links returned 403 from the workspace; authenticated GitHub archive download
succeeded. No downloadable signed URLs or private credentials are saved.

Same Amount passed run 213; that is a later observation, not a diagnosed correction.
Both failures remain separate from the historical unreproduced Compose SlotTable
failure. No assertion relaxation, timeout increase or runtime fix is claimed.
The Android gate remains OPEN. Build/browser success is not a passing entire workflow.

## Local diagnostic attempt

The existing app/test artifacts were run on the owned API 35 arm64 AVD at hosted
Pixel 2 dimensions/density with animations disabled. The five-case selection was
interrupted: Same Amount Espresso idling timeout, English Wash Hands teardown
failure, German portrait start, then runner SYSTEM HAS CRASHED. No cases completed
successfully. Adb process exit 0 is not interpreted as a pass.

The preserved crash buffer identifies a fatal `HardwareBuffer.finalize()` watchdog
in the emulator system process and subsequent DeadSystemException in system apps.
This is not a reproduction of either hosted artwork wait or proof of an app defect.
See [original runner](evidence/local-baseline-five.txt),
[crash buffer](evidence/local-crash-buffer.txt) and
[artifact identities](evidence/local-installed-identities.json).
No new full JVM/build/browser or physical acceptance is claimed; source is unchanged.

## Preservation and cleanup

Only owned Basti_Pattern_20261111/emulator-5554 was used, without wipe, uninstall,
app-data clearing, downgrade or snapshot deletion. Original no_backup/shared_prefs/
app_webview were copied intact before the attempt; all 88 original paths/bytes were
verified on restore. Generated data is retained in cache/consolidation-20261126-generated.
Display 1080×2400 / density 420, airplane 1, animations 1.0/1.0/null and original rotation
settings are restored/preserved. Original Medium Phone config SHA remains
c4ab33993685273776e9286aee0b948e48192f5ab61046d564447d7a12e7034c.
The sole historical empty length-test-backup is retained; no new active scoped
backup remained. Original Medium Phone and physical-device userdata were untouched.
Owned emulator closed gracefully; process/device absence verified.
[Data/settings restoration](evidence/device-restoration.json),
[cleanup](evidence/device-cleanup.json).

## Checkpoint verification

[Validation record](evidence/validation.json): five report-inspection checks passed
(two original archives/counts/failures/digests and three rejection cases), all tested
app/Android-test source hashes remain unchanged, new document links/whitespace pass.
The local five-case device attempt remains unsuccessful. The inspector is evidence
processing, not new activity-test execution. Raw logs/XML/ZIPs retain original
whitespace and bytes; formatting checks do not rewrite them.

## Remaining checkpoint 1 work

- Add verified first-error artwork/semantics/window diagnostics before teardown,
  preserving current bounds and assertions; reproduce on a healthy software-rendered
  session and configured hosted selection when publication is authorized.
- Diagnose/correct the demonstrated cause and establish a reliable green hosted gate.
- Decide the broader Home/layout CI coverage after the focused gate is dependable.
- Execute the signed-candidate physical batch through the owner; pending hardware
  does not stop independent automated consolidation.

Checkpoint 1 remains active. Saved plan, inventory, documentation reconciliation,
report triage and device-review preparation are reviewable local changes.
