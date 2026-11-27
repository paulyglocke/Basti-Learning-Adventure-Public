# Batched physical acceptance — preparation

Prepared 2026-11-26 under [consolidation checkpoint 1](CONSOLIDATION_PLAN.md).
Status: NOT RUN; owner-managed. No physical-device connection, installation or
distribution is implied by preparing this plan. Preserve prior named S24 passes
in [the QA ledger](../TESTING_QA_SPEC.md); review the current candidate's unverified
scope rather than repeating accepted artwork cleanup or historical upgrade tests.

## Candidate and record

Use one candidate after its required hosted checks pass, with exact commit,
package, versionCode/name, APK SHA-256 and permanent signing-certificate fingerprint.
Use the established stable-signed distribution stream. Independently record the
installed baseline identity and device/OS before any upgrade. Never uninstall,
clear data, downgrade or change signing identity to make an upgrade succeed.
Do not trigger distribution while the Android gate is red.

Samsung S24 and Fire Max require separate records. For each, record orientation,
navigation mode, font scale, EN/DE, audio mode, TTS engine/voice and offline voice
data. Keep actual outcomes, first failures and evidence links; blank rows are
pending. Mark unavailable hardware and inaccessible records as blocked explicitly.

## Representative batches

| Batch | Representative scope | Required observations |
| --- | --- | --- |
| Install/upgrade/offline | Preserve a known partial native round, settings and one legacy round; upgrade in place, launch in airplane mode | Same certificate/increasing version, retained settings and declared restore behavior, no network stalls. Preserve visible and inspectable durable evidence separately. |
| Home/Options/navigation | Six categories, All Activities, Numbers/Easy Maths/Mixed, native→Options→return, Back/Home | Correct destinations, no reading-dependent navigation, reachable controls/insets in portrait and both landscapes, exact declared restoration. |
| Audio/support | EN/DE, ALL/QUESTIONS/OFF, Replay/Help, interrupted question, background/return, unavailable offline voice | Clear audible wording, current language, correct silence/cancellation, usable missing-voice recovery. A fake engine or metadata check cannot close this batch. |
| Quantity construction/application | Counting Tray, one Build5/10 board, One More/Less, Number Stories, Same Amount | Child understands what can move/change, reversible edits, calm retry, explicit Check/Next, no loss/duplicate evidence on interruptions where inspectable. |
| Length/pattern/memory | Longer/Shorter, Order by Length, Pattern Builder, one Memory board | Geometry/picture meaning, distinction between exploration and assessment, target comfort, Help strategy, completion/navigation. |
| Communication/listening/routines | Tell Me, one Vocabulary mode, Follow/School Skills, Classroom Helper, Wash Hands | Natural EN/DE, clear artwork/actions, independent interaction versus required grown-up help. Spoken expression and real-world routine completion stay ungraded. |
| Time/spatial/world | Unverified Wilma/Seasons/Months/wheel/Clock modes, Prepositions v4, one habitat flow and Colours | Clear meanings and safe controls, long German labels, changing orientation/font scale, existing semantic distinctions preserved. |
| Accessibility/performance | Representative choice, manipulation, open-ended and completion screens | Actual TalkBack focus/labels/separate speaker actions, keyboard/switch where practical, large text/contrast, cold/warm start, image completeness, memory/responsiveness and safe system-bar layout. |

These are batch families, not a claim that one representative proves every mode.
Record the exact exercised modes in [the activity inventory](ACTIVITY_ACCEPTANCE.md)
or a linked per-device result; schedule remaining modes explicitly.

## Child-use observations

Observe whether Basti can choose an activity, understand the first prompt, recover
from a mistake, ask for/use help and finish or leave independently. Note boredom,
confusing choices, repeated reading assistance and artwork misunderstandings.
Shorten/split the review as needed; there is no compulsory duration or completion.
Use observations to choose refinements and later curriculum work, not a child grade.

## Inspection blocker

The release build is non-debuggable and there is no parent progress export yet.
If event IDs/journals cannot be inspected through an authorized method, mark
file-level preservation and duplicate-event checks BLOCKED. Visible restored UI
alone does not prove durable history or deduplication. A separately identified
debug QA build is supplementary evidence, not signed-release acceptance.

## Result template

| Field | Result |
| --- | --- |
| Candidate commit/APK hash/version/certificate | Pending green candidate |
| Device/OS/installed baseline | Pending owner review |
| Language/audio engine/voice/offline data | Pending |
| Mode/orientation/font/navigation combinations exercised | Pending |
| Passes with observations/evidence | Pending |
| Failures with first-error evidence/repro steps | Pending |
| Blocked/not-run checks and reason | Hardware/audio/child/assistive paths not run in this preparation |
| Refinement priority and next review | Pending observations |
