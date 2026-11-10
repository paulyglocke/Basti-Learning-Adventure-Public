# Memory Pairs — native starter

Implementation baseline: `2ff8820`. Started 2026-11-09; final verification
continued on 2026-11-10. Work is local; no commit or push is requested.

## Implemented

Memory Pairs opens from Home → Animals & Nature or All Activities. Animal pictures
and number ↔ quantity matching share one semantic matching state machine. Each
mode offers 4, 6 or 8 cards (2, 3 or 4 distinct pairs), independently of the global
5/10-question preference. The animal mode reuses the six existing Vocabulary
records/canonical PNGs. Number mode uses quantities 1–5 and native dot groups.

Cards reveal on tap or keyboard activation. A second reveal is one pair-selection
attempt; tapping a revealed/found card cannot submit again. Two different cards
stay visible until the child chooses to turn them back over. Matching cards remain
found; Continue acknowledges feedback, and the last Continue completes the board.
No timer, flip grade, penalty, automatic hide deadline or compulsory motion.
Cards reserve their face/Listen space before a reveal so flips never move the
remaining targets. At ordinary text sizes larger phone boards use three columns;
wider screens use up to four, while large text stays scroll-reachable.

Show me a pair models a relationship without fabricating a correct attempt.
The child turns the model back over and finds it; later success retains Hint
support. Listen is separate from card selection. The board has readable EN/DE
instructions, labels, individual face-up Listen and independent Replay.

Each of the six configurations owns a checksummed write-ahead journal. Exact card
order, face-up turn, found pairs, exposures and support restore silently. Options,
language and recreation preserve the board; Back returns to the chooser. Play
again selects a different pair set and deck from the preceding board. Changing
board size does not overwrite another configuration's unfinished work.

Pair attempts and deduplicated completion use the existing shared Progress Tracker,
with separate picture-memory and number–quantity skills and board-size contexts.
Per-card exposures stay in the board journal, not fabricated attempt events. No
mastery, reading skill or vocabulary production is inferred. The shared progress
codec accepts two-task completion records for the smallest board, alongside every
previously supported size; its wire format/version is unchanged.

Audio uses the existing lazy controller, shared all/questions/off policy and offline
system voices. Returning/restoring is silent; new boards narrate instructions,
reveals name cards and manual Replay reads the current phase or completion summary.
Home/Options/background/language changes cancel current speech. The shared balloon
reward and sound policy remain authoritative. No new bitmap art, TTS engine or
runtime web dependency is introduced.

## Verification

- Full JVM: **675/675**, zero failures/errors/skips, including all six seeded
  configurations, exact phase recovery, model/Replay evidence, failed write and
  uncertain completion recovery, semantic navigation and old progress compatibility.
- Browser: **63/63**, zero failures/skips. Both debug APK assemblies and lint pass:
  zero errors, seven existing warnings and two informational findings.
- Final focused API 35: **6/6**, zero failures/errors/skips. One production test
  completes all six configurations, including first-flip/mismatch/model restoration,
  real Options language change, independent Listen and shared balloon pop. Owner
  coverage checks lazy Sound Off, fresh-owner silent recovery and config-switch
  cancellation. Four layout cases check EN/DE, 1×/1.5×/2×, portrait/landscape,
  keyboard, >=48dp targets and constant card size through reveal.
- Existing startup/sweep/verbs/bonds/Wash Hands/Vocabulary/Prepositions and bilingual
  30-entry Home regressions: **22/22 passed** within the retained combined run.
  That run was **27/28** overall because the new route's screenshot hit Compose's
  PixelCopy timeout. Runtime APK and source are byte-identical to the clean final
  focused run; only the capture helper changed. No behavior assertion was removed.
- Initial focused run: **5/6**. Completion Replay failed an unnecessary test-only
  scroll operation on a fixed visible button; the helper now clicks fixed controls
  directly. An actual visual issue found in review—card heights moving on reveal—
  was repaired and has layout regression checks. Initial reports remain alongside
  the final reports. New screenshots use Android's actual display screenshot API
  after Compose/platform idle, avoiding PixelCopy's short callback deadline.
- Tablet layout run: **4/4 passed**, zero failures/errors/skips, with the same
  two eight-card modes, language/font/orientation and keyboard checks.

The [gallery](index.html) contains **41 genuine screenshots**: 25 production-route
views, eight phone components and eight tablet components. Final source/APK hashes,
report totals and capture inventory are in [validation.json](evidence/validation.json).

The dedicated API 35 emulator used 4 CPUs, 6 GB RAM, host graphics and 12 GB data.
Its display overrides are reset to physical 1080×2400 / 420 dpi, airplane mode is
restored to original 0, and it is closed. The original AVD and physical devices
were untouched. Gradle managed disposable debug/test APK installation/uninstallation.

These are selected instrumentation checks, not a new full-app instrumentation run.
The gallery distinguishes production-route captures from component tests, labels
scrolled views and uses actual emulator screenshots. Tablet views are emulator
configuration overrides. Physical S24/Fire, actual TalkBack/switch use, child
comprehension, installed offline voice quality and signed upgrades remain
owner-managed acceptance; they are not claimed as passed.

## CI maintenance found during this checkpoint

The previous pushed checkpoint's Android and build jobs passed. Its browser job
failed before tests because `ubuntu-latest` moved to Ubuntu 26.04, unsupported by
locked Playwright 1.51.1. The browser job now pins Ubuntu 24.04, which the installed
Playwright platform/dependency definitions explicitly support. No dependency upgrade
or test removal. The new Memory Pairs route/owner checks join the focused Android
CI selection. Hosted confirmation of these changes requires a future push.
