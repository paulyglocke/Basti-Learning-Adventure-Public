# Home legacy readiness and recovery verification

The corrected Home verification passes on the unchanged Longer / Shorter app
from `7992003c342a9434eb7a270437b23bb28eb44471`. This follow-up changes two
Android test files and documentation. No app code or dependencies changed.
Owner requested committing and pushing this checkpoint on 2026-11-24.
The evidence records local validation before publication; hosted checks are separate.

## What changed

The old helper started its ten-second DOM deadline when Compose idled, before
asynchronous WebView document loading had finished. It could also submit another
JavaScript probe while an earlier callback remained outstanding. It now waits
separately for document loading (bounded 30 seconds), then retains the ten-second
live DOM check and its mode, screen, nonempty text and positive-size assertions.
Only one JavaScript probe can be outstanding. Phase/entry/language diagnostics
identify loading failures separately from rendered-content failures.

The Home test captures the saved Bundle from real framework Activity recreation
instead of directly calling onSaveInstanceState and manually cycling lifecycle.
All saved-route/web-mode assertions remain. Every entry is actually recreated;
each retained web route additionally restores the exact displayed question and
serialized question state. Navigation still uses the real Home cards and Back.

## Final verification

**12/12 API 35 cases passed**, no failures/errors/skips, in 605.11 seconds.
[Raw runner](evidence/final-selection.txt), [derived summary](evidence/final-selection.json),
[per-entry diagnostics](evidence/final-logcat.txt), [summary](evidence/validation.json).

| Cases | Coverage |
| --- | --- |
| Home EN/DE (2) | All35 entries in each language, all 70 actual recreations and saved destinations; Numbers, Easy Maths and Mixed Adventure restore exact text/question state in both languages |
| Numbers web recovery (1) | Options, exact question and repeated recreation |
| Longer / Shorter (2) | Production 5/10 route, state/progress/reward flow and lazy silent audio owner |
| Same Amount (2) | Production route and owner regression |
| Startup (1) | Actual shell launch, recreation and background return |
| Wash Hands components (4) | English/German portrait and both German landscapes, keyboard/build/check/Undo/Help/completion |

[Build and lint](evidence/readiness-build.log) passed in 29s: 0 lint errors,
7 existing warnings and 2 informational findings ([XML](evidence/lint.xml)).
The runtime APK remains byte-identical:
`52fc7528e07b8c589f07b417c3904b45222666072591fb370e81cef28e4862b0`.
The final test APK is
`a8c05ba106017aad5767d6695c2bd2118ff65b26c124930dc0ae61ae27938ea9`.
[Source/artifact hashes](evidence/final-hashes.json), [installed verification](evidence/installed-hashes.json).
No new JVM run was needed for test-only changes; the unchanged runtime's 742/742
JVM evidence remains in the [Length checkpoint](../length-comparison-2026-11-23/README.md).
The maximum observed final document wait was2888ms; total DOM readiness3700ms.
These [timings](evidence/readiness-summary.json) are observations, not performance acceptance.

## Earlier failures and remaining uncertainty

The [initial diagnostic run](evidence/home-diagnostic.txt) rendered all three
English web entries, then crashed during Compose SlotTable disposal after opening
Wash Hands. German did not start. [Crash/entry trace](evidence/diagnostic-logcat.txt),
[diagnostic summary](evidence/diagnostic-summary.json), [initial build](evidence/diagnostic-build.log),
[initial identities](evidence/diagnostic-hashes.json).
This was a separate failure from the prior DOM timeout, not a two-case pass.

An [earlier filtered timing excerpt](evidence/original-load-timing-excerpt.txt)
shows a renderer launching/loading amid long GC/frame delays near the old timeout.
The full private bugreport stays outside the repository. The specific reason for
that slowdown and the separate disposal crash have not been established.
Neither recurred in the final selection. The fixture changes correct synchronization
and use framework lifecycle; they do not prove a runtime crash or general startup
fix. The disposal-crash follow-up remains in [BACKLOG](../../BACKLOG.md).
Earlier Length review failures/evidence remain intact.

## Cleanup and acceptance boundaries

Only owned Basti_Pattern_20261111/emulator-5554 was used. The stopped app's
no_backup, shared_prefs and app_webview directories were copied intact before
testing, then restored with all 88 original file paths/hashes matching exactly.
Generated test data remains intact in cache/home-timeout-20261124-generated.
[Before](evidence/device-data-before.txt), [restored](evidence/device-data-restored.txt),
[restoration verification](evidence/data-restoration.json).

Display 1080×2400 / density 420, airplane mode 1 and animation settings 1.0/1.0/null remained unchanged.
Original Medium Phone configuration hash is unchanged. The owned emulator was
closed gracefully; process and attached-device absence verified ([cleanup](evidence/cleanup.json)).
No wipe, uninstall, app clear or snapshot deletion. Original Medium Phone and
physical-device userdata were untouched. No new browser, hosted, full-app,
physical S24/Fire, child-use, actual TalkBack/switch or audible offline-voice
acceptance is claimed.
