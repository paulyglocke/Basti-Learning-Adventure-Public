# Same Amount — local review

Implemented locally; final verification completed 2026-11-23. Baseline is Number Stories commit
`44e2eae398257d9d6f5933e75eded1b3154ead96`. The owner requested committing and
pushing this checkpoint on 2026-11-23. Evidence describes pre-commit local validation;
hosted checks are separate.

Home offers Same Amount / Gleich viele with within-5 and within-10 levels.
A fixed Before frame and an independently scrollable Now workspace isolate
quantity from position. Two taps move a picture to an empty place without adding
or removing anything; Undo reverses each move. A 5/10-question round has 60%
unchanged quantity and 20% each of explicit one-object removal/addition.
Dinosaurs and apples alternate; seeded facts are unique within the round.

Choose Fewer/Same/More, then Check. Only a changed checked relation records an
attempt. Moves, selection, Undo and answer selection are unscored. Wrong work
remains editable; solved work locks until explicit Next. Two independent CNS1
journals save tasks, masks, selected picture, chosen answer, Undo, support and
pending shared progress. Restored owners are silent. Shared speech modes,
progress and balloon completion are reused.

Unchanged tasks and actual changes report separate skills. Correct comparison
does not establish generalised conservation mastery or prove object movement.

## Verification

Final build after the artwork refinement: **732/732 JVM tests**, both debug APKs,
lint (0 errors, 7 existing warnings, 2 information findings). The retained XML,
source manifest and APK hashes describe this build. Final combined phone selection **11/11 passed** on those exact installed APKs,
zero failures/errors/skips, in 114.887 seconds. It covers the new route, owner and
four layouts; existing Number Stories route/owner; startup; and all 34 Home entries
in EN/DE. The new route completes 5/10-question levels with exactly 17 attempts and
two completions, including nine conservation tasks. Final tablet layouts **4/4
passed** on the same APKs, zero failures/errors/skips, in 224.874 seconds.
Both viewports cover EN 1× and DE 1.5× portrait, DE 2× and EN 1× landscape,
both ranges, both objects, unchanged groups, zero and maximum quantity,
keyboard selection/movement, Undo, Help, fixed references and >=48dp targets.

The [gallery](index.html) contains **74 genuine final captures**: 10 production
phone routes, 32 phone components and 32 tablet components. Thirteen representative
final images were visually reviewed; component captures omit the production shell.
The workspace intentionally scrolls and clips at its viewport edge while Before
stays visible. Canonical apple artwork retains its original ivory background.

[Validation summary](evidence/validation.json), [build and lint counts](evidence/build-validation.json),
[JVM XML archive](evidence/jvm-xml.zip), [lint XML](evidence/lint.xml),
[tested source hashes](evidence/tested-source.json), [installed APK hashes](evidence/installed-apks.json),
[phone runner](evidence/phone-final.txt), [tablet runner](evidence/tablet-final.txt),
[capture inventory](evidence/capture-inventory.json), [visual review](evidence/visual-review.json)
and [emulator cleanup](evidence/emulator-cleanup.json) are retained.
Runner JSON files are derived from raw direct AndroidJUnitRunner text, not Gradle
instrumentation XML. Initial build logs are retained separately; the final build
metadata records its observed result and is not a replacement raw build log.
Catalogue generation, retained verb-motion parity, source/document whitespace
and gallery integrity checks pass. Raw logs retain tool-emitted spaces verbatim.
No new browser, full-app instrumentation or hosted result is claimed.

Earlier evidence remains separate. Initial runtime passed a combined **11/11**
phone selection (new route/owner/four layouts, existing Number Stories route/
owner, startup and bilingual 34-entry Home). Visual review then found a partially
rendered countable group following Activity recreation. Global and per-cell
image-node readiness checks were explored but were not a reliable capture gate.
The board now decodes its current object once and shares that same bitmap across
all occupied Before/Now cells. A loading/retry screen blocks answer input until
it is ready. Initial checks do not validate the refined runtime.

Two earlier layout/diagnostic runs were deliberately stopped while only component
layouts were active to update the APKs; their process-crashed status records that
explicit stop. Separate post-install attempts exited before any test started;
Android events show process-startup ANRs, without an app exception in the crash
buffer. The same AVD was restarted without a wipe. The unchanged APK then launched
normally (one recorded cold launch: 1664 ms). This does not establish a root cause
or resolve broader startup/performance acceptance. Raw earlier logs remain retained.

## Acceptance boundary

Physical S24/Fire, child use, real TalkBack/switch operation, audible installed
offline voices and signed upgrade remain NOT RUN. Original Medium Phone userdata
and physical devices are not used; no wipe, uninstall or legacy removal.
Only owned API 35 Basti_Pattern_20261111/emulator-5554 was used, without snapshots.
Tablet 1600×2560/density240 overrides were reset to 1080×2400/density420;
original airplane mode 1 was preserved. The emulator exited after a graceful
close; ADB reports no attached devices. Original Medium Phone config hash is
unchanged. No local implementation, verification or emulator work remains.
