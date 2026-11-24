# Compose disposal investigation

**The recorded crash was not reproduced; its cause remains open.** A permanent
bilingual Wash Hands lifecycle regression now covers the relevant state changes
and disposal paths and joins the focused CI selection. Application code and
dependencies are unchanged from `fbc7f54d60992dc69be61f1fcd591c616afb314b`.
No runtime fix is claimed. Owner requested committing and pushing this checkpoint
on 2026-11-24. Evidence records local validation before publication; hosted checks
are separate.

## Investigation and coverage

The original failure was reported while disposing a subcomposition after opening
Wash Hands in the earlier Home fixture. That does not establish the first failure,
a Wash Hands defect or a normal-navigation crash. Its [original record](../home-legacy-timeout-2026-11-24/evidence/home-diagnostic.txt)
and earlier failed-run evidence remain unchanged.

A one-off historical probe replayed the first 25 entries, including Wash Hands,
using the earlier manual-save/lifecycle sequence and original 10s web helper.
Stage/failure logging was added before teardown so an earlier exception could
be retained if cleanup also failed. This replay passed without the crash.
The probe is retained as [diagnostic source](evidence/historical-probe.kt.txt)
with [identities](evidence/probe-hashes.json) and [trace](evidence/historical-probe-logcat.txt).
It was removed from default test sources and is absent from the final test APK.

The new WashHandsLifecycleTest exercises the actual shell and asynchronous
owner in English and German. Each case covers empty/partial/incorrect/completed/
new-round work, Options recreation, background/return, Home disposal and closing
the Activity with the available-step grid mounted. Available choices change
through all grid sizes while editing. Checks include:

- Seventeen exact journal-byte preservation assertions per language (34 total).
- Six framework recreations, six background round trips and five Home disposals per language.
- Final Activity teardown with the grid mounted in both languages.
- Exactly eight checked-position attempts and one completion per case, from two explicit Checks; lifecycle/navigation must add none.
- Existing journal/progress stores isolated and restored in finally, including protection if setup only partly succeeds.

[Verified lifecycle observations](evidence/lifecycle-summary.json),
[complete runner/phase trace](evidence/final-logcat.txt).

## Validation

| Run | Result | Evidence |
| --- | --- | --- |
| Historical diagnostic sequence | 1/1 passed, 226.832s | [Raw](evidence/historical-probe-run.txt), [derived](evidence/historical-probe-run.json) |
| New EN/DE actual lifecycle cases | 2/2 passed, 151.996s | [Raw](evidence/lifecycle-focused.txt), [derived](evidence/lifecycle-focused.json) |
| Existing four Wash Hands layouts and startup | 5/5 passed, 111.106s | [Raw](evidence/regression-final.txt), [derived](evidence/regression-final.json) |
| Lifecycle test build and lint | Passed in 28s; lint 0 errors, 7 existing warnings, 2 info | [Build](evidence/lifecycle-build.log), [lint XML](evidence/lint.xml) |

All three runner selections have zero failures/errors/skips. They are separate
scoped runs with different diagnostic/final test APK identities, not one combined
run. Direct-runner JSON is labelled as derived text, not Gradle instrumentation XML.
The initial diagnostic compile failed for a missing test-rule import; [raw error](evidence/probe-build-compile-error.log)
is retained separately from the corrected [probe build](evidence/probe-build.log).

App APK SHA256 remains
`52fc7528e07b8c589f07b417c3904b45222666072591fb370e81cef28e4862b0`.
Final test APK SHA256 is
`1f50fcd2a9c756bb3c77a73d2f6be3b3b68dbef102e0265614bd84bbfffd37a7`.
[Source/artifact hashes](evidence/final-hashes.json), [installed hashes](evidence/installed-hashes.txt),
[validation summary](evidence/validation.json), [CI configuration](evidence/ci-selection.json).
The new class adds two parameter cases to the configured focused selection;
hosted execution requires a push and has not been run.

No new JVM or full Home run was needed for the test-only addition. The unchanged
runtime's 742/742 JVM evidence and unchanged Home test's 70 actual recreations
remain [baseline evidence](../home-legacy-timeout-2026-11-24/README.md), not new results.
These passes do not establish why the earlier failure happened or prove it fixed.
The [backlog follow-up](../../BACKLOG.md) remains open for a reproducible failure
with the first exception captured before teardown. No speculative app/library
change was made.

## Cleanup and acceptance boundaries

Only owned API 35 Basti_Pattern_20261111/emulator-5554 used, without wiping,
uninstalling, clearing app data or deleting snapshots. The stopped app's
no_backup/shared_prefs/app_webview directories were copied intact before testing.
At completion, tested directories were retained in
cache/compose-disposal-20261124-generated and originals restored; all 88 original
file paths and hashes match. [Before](evidence/device-data-before.txt),
[restored](evidence/device-data-restored.txt), [verification](evidence/data-restoration.json).
No active lifecycle-store backups remain.

Original 1080×2400 / density 420, airplane mode 1 and animation settings 1.0/1.0/null unchanged.
Original Medium Phone configuration hash unchanged; its userdata and physical
devices were untouched. The owned emulator was closed gracefully and process/
attached-device absence verified ([cleanup](evidence/cleanup.json)).
No physical S24/Fire, child use, actual TalkBack/switch, audible offline-voice,
new browser, hosted or full-app acceptance is claimed.
