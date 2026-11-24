# Longer / Shorter — local review

Implemented from Same Amount baseline `63b271189ff379162e253b29df7824a66bdd77f5`.
Owner requested committing and pushing this checkpoint on 2026-11-24.
The evidence below records local validation before publication; hosted checks are separate.

Select either strip, move it left/right or directly to the common starting
line, and compare bottom B with top A. Both rows use the same drawing scale.
Selection, movement and Undo are unscored. Explicit Check requires aligned
starts and a changed answer after an incorrect attempt. Correct work locks
until Next. Seeded saved 5/10 rounds balance shorter/same/longer relations,
vary offsets and colours independently, and avoid repeating the previous
opening/final pair at the next opening.

An independent checksummed atomic LNC1 journal saves the plan, exact positions,
selection, Undo, answers/support and pending deduplicated shared progress.
Fresh owners restore silently. Existing bilingual audio modes, Help/Replay,
progress and balloon completion are reused. Home has 35 entries: 32 native,
three retained web; Maths has 19. No new raster artwork or dependencies.

## Validation

| Scope | Result | Evidence |
| --- | --- | --- |
| Full JVM suite, both APKs, lint | 742/742; lint 0 errors, 7 existing warnings, 2 informational findings | [Build 6](evidence/build6-final.log), [summary](evidence/build-validation.json), [JVM XML](evidence/jvm-xml.zip), [lint XML](evidence/lint.xml) |
| Final app, focused phone selection | 9/9 passed | [Raw runner](evidence/phone-final.txt), [derived summary](evidence/phone-final.json) |
| Test-only capture readiness build | Passed; runtime APK unchanged | [Build 7](evidence/build7-capture.log), [scope](evidence/capture-scope.json) |
| Phone capture replay (route + four layouts) | 5/5 passed | [Raw runner](evidence/phone-capture-final.txt), [derived summary](evidence/phone-capture-final.json) |
| Final tablet layouts | 4/4 passed | [Raw runner](evidence/tablet-final.txt), [derived summary](evidence/tablet-final.json) |

All selections above have zero failures, errors or skips. The five phone replay cases repeat part of the nine-case selection; the four tablet cases repeat the layouts at a tablet viewport. These are separate scoped runs, not eighteen distinct cases. Tablet views use 1600×2560 / density 240 display overrides on the same API 35 AVD.

The nine phone cases cover the actual 5/10 production route (17 attempts,
two completions), exact recreation/Options/language restoration, Undo,
changed-answer retry, shared balloons, lazy silent audio ownership, four
layouts, existing Same Amount route/owner and startup. Layouts check both
unequal length boundaries, equal minimum/maximum lengths, EN 1× / DE 1.5×
portrait and DE 2× / EN 1× landscape, keyboard input, setup gating, shared
scale, fixed strip visibility, solved locking and minimum 48dp targets.

The final runtime APK SHA-256 is
`52fc7528e07b8c589f07b417c3904b45222666072591fb370e81cef28e4862b0`.
The nine-case run used the [pre-capture test artifact](evidence/before-capture-apk-hashes.json).
The capture helper subsequently added expected-control, presented-frame and
foreground-package checks, plus scrolling solved Next into view. These are
test-only changes; all behaviour assertions remain. [Current source](evidence/tested-source.json)
and [APK identities](evidence/apk-hashes.json) identify the capture replay.
Direct-runner summaries are derived from retained text, not Gradle instrumentation XML.
Catalogue generation and retained verb-motion parity checks passed.

## Screenshots

[Open the gallery](index.html): 74 genuine unaltered device PNGs (10 production
phone, 32 phone components, 32 tablet components). All were created after the
final capture marker by the final capture test APK; [inventory](evidence/capture-inventory.json)
records dimensions and verified device/local hashes. [Twelve final images were
visually reviewed](evidence/visual-review.json), covering movement, retry,
solved feedback, balloons and each phone/tablet layout setting. Component
captures omit the production shell. Text can clip at the scrolling viewport
boundary while both strips remain fixed; these are not whole-page screenshots.

## Open broader Home failure

A separate all-Home run passed German Home (all 35 entries) but failed English
in a retained-WebView DOM wait. An English software-renderer retry also failed;
the duplicate German case was explicitly stopped. [Host run](evidence/home-host-attempt.txt),
[summary](evidence/home-host-attempt.json), [software attempt](evidence/home-software-attempt-interrupted.txt).
Root cause is unconfirmed and no legacy fix is claimed. This remains an open
P0 in [BACKLOG](../../BACKLOG.md); the nine native passes do not close it.

## Review and acceptance boundaries

Screen-reader descriptions supply lengths in internal comparison units, not
centimetres. Using those descriptions is not evidence of visual length
discrimination. Physical S24/Fire, actual TalkBack/switch operation, child
comprehension, audible installed offline voices and signed upgrade are NOT RUN.
No new browser, full-app instrumentation or hosted result is claimed.

Initial failures remain retained separately: a test syntax error; two JVM
failures that exposed same-seed repeat plans (fixed before the final 742/742);
a fixed-control scroll fixture failure; a Launcher ANR interruption; and
zero-test startup exits. Earlier before-copy route/tablet passes do not validate
the final copy. The earlier interrupted eleven-case selection completed eight
passes and one fixture failure, then was stopped during English Home; it is
not a clean eleven-case pass. One screenshot caught a pending-write frame;
[the original](evidence/initial-captures/pending-frame-before-capture-readiness.png)
is retained separately from the refreshed captures.

Only the owned API 35 Basti_Pattern_20261111 AVD was used, without wiping,
uninstalling, clearing app data or deleting snapshots. Initial scoped backup
was verified empty; interrupted synthetic stores were retained intact in app
cache and their original absence restored. Original pre-first-interruption
shell preferences were not separately backed up. The language left by the
stopped duplicate German Home case was restored to its preceding English
value; other entries were preserved ([details](evidence/settings-restoration.json)).
Original Medium Phone and physical-device userdata were untouched.

Final cleanup: display overrides cleared to 1080×2400 / density 420,
airplane mode 1 and animation settings unchanged, original Medium Phone config
hash unchanged. No active scoped test backups remain; the historical empty
backup and interrupted synthetic stores are retained for audit. The existing
length checkpoint predating the final tests was preserved; initial absence
restoration refers only to the earlier interruption recovery. The owned AVD
was closed gracefully; process and attached-device absence were verified.
See [cleanup record](evidence/emulator-cleanup.json).
