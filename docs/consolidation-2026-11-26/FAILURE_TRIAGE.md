# Hosted Android failures — first-step triage

Inspected 2026-11-26/27. Application/test baseline remains `8e3407f`.
Status: root causes OPEN; no runtime change, assertion relaxation or timeout increase.

## Run 212 — Same Amount final chooser

- Source `8703c4e`; Android 33/34, build/browser passed.
- The original XML locates the 10s failure at `ConservationCapture.kt:13`, called
  from `ConservationRouteTest.kt:83`: the final German chooser capture after both
  rounds, while waiting for exactly three dinosaur artwork nodes.
- The completed game and levels-navigation operations precede that capture. This
  is not the board readiness wait at `ConservationCapture.kt:11` and is not a
  failed quantity/progress assertion.
- Original archive: [run 212 ZIP](evidence/hosted-212-reports.zip);
  [suite XML](evidence/hosted-212-failure.xml),
  [case logcat](evidence/hosted-212-failure-logcat.txt),
  [derived summary](evidence/hosted-212-summary.json).
- Same Amount passed run 213. That later observation does not establish why the
  earlier chooser lacked the expected semantics or whether decoding, visibility,
  scheduling or fixture state caused it.

## Run 213 — Wash Hands German portrait

- Source `8e3407f`; Android 35/36, build/browser passed.
- The original XML locates the 10s failure at `WashHandsScreenTest.kt:61`: waiting
  for an image tag inside an available-step card, before the subsequent displayed
  image assertion and Listen action. Case is `[GERMAN-1]`, at component font 1.5×.
- The current logs do not identify which step image was awaited or include its
  unmerged semantics tree. They contain no corresponding decode error or reproduced
  SlotTable exception establishing a cause.
- Original archive: [run 213 ZIP](evidence/hosted-213-reports.zip);
  [suite XML](evidence/hosted-213-failure.xml),
  [case logcat](evidence/hosted-213-failure-logcat.txt),
  [derived summary](evidence/hosted-213-summary.json).

## Local attempt — interrupted, not a reproduction/pass

The owned API 35 arm64 AVD used the existing unchanged local app/test APKs, with
hosted-profile display 1080×1920 / density 420 and animations 0/0/0. It retained host GPU,
the existing Play-store system image and existing data; it is not the hosted Linux
x86_64/google_apis/software-renderer environment.

The five-case selection did not complete. Same Amount reported an Espresso
Compose-idling timeout; English Wash Hands reported a teardown STOPPED→DESTROYED
timeout; German portrait started, then the runner reported SYSTEM HAS CRASHED.
No case reported a successful completion. Adb returned process exit 0 despite
the unsuccessful selection, so that exit code is not a passing test result.
[Original runner](evidence/local-baseline-five.txt).

The [crash buffer](evidence/local-crash-buffer.txt) records a fatal exception in
the emulator system process: `HardwareBuffer.finalize()` exceeded 10s, followed by
DeadSystemException in system applications. That establishes an emulator-system
failure during this attempt; it does not attribute the hosted waits, earlier
idling timeout, or historical Compose disposal crash to an application defect.
The host pause/elapsed-time discontinuity does not provide a performance benchmark.
[Available test/system logcat](evidence/local-test-logcat.txt),
[unchanged installed identities](evidence/local-installed-identities.json).

Original 88 app file paths/bytes and settings were backed up and restored; generated
data is retained intact. The original Medium Phone configuration is unchanged.
Cleanup evidence is linked from the checkpoint review.

## Next bounded engineering action

1. Instrument the two existing artwork waits with phase/step/tag/expected-count,
   actual count, unmerged semantics tree, active Activity/window and genuine screenshot
   on first failure, before Activity teardown. Diagnostic failures must never replace
   the original assertion/exception. Preserve the current 10s bound and all assertions.
2. Verify those diagnostics independently, then run the unchanged behavioral paths
   on a fresh no-wipe software-rendered disposable session and the configured hosted
   selection when publication is authorized. Retain the previous class/parameter order.
3. Separate actual missing artwork, off-screen semantics, scheduling/cancellation and
   fixture transitions from runner/system health. Correct only a demonstrated cause.
4. Close the gate only with diagnosed correction and relevant local/hosted evidence.
   Do not relabel these failures as benign flakiness from a successful retry.

## Re-inspecting the original archives

```sh
python3 docs/consolidation-2026-11-26/inspect_hosted_reports.py docs/consolidation-2026-11-26/evidence/hosted-212-reports.zip /private/tmp/basti-hosted-212-summary.json --expected-tests 34
python3 docs/consolidation-2026-11-26/inspect_hosted_reports.py docs/consolidation-2026-11-26/evidence/hosted-213-reports.zip /private/tmp/basti-hosted-213-summary.json --expected-tests 36
```

The inspector rejects absent suites, inconsistent/incomplete counts and oversized
suite files. It reads original output XML rather than duplicate HTML-report copies.
Successful inspection is not successful test execution; failures remain explicit.
