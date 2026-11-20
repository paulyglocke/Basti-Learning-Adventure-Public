# Number Stories — local review

Implemented 2026-11-16; remaining emulator validation completed 2026-11-20.
Implementation and selected local verification are complete. The owner requested
committing and pushing this checkpoint to main on 2026-11-20. Retained evidence
describes pre-commit local validation; hosted checks and physical acceptance are
separate.

Number Stories / Rechengeschichten adds native illustrated addition and taking-away
situations within 5 or 10. Basti hears a short story, changes the starting group,
checks it and chooses when to continue. Pond/dinosaur and picnic/apple settings
reuse canonical vocabulary art, with native scenery containing no extra target
objects. No new raster art or runtime HTML is introduced.

## Learning and interaction

- Four independent choices: adding/taking away × within 5/10; changes of 1–5.
- Starting reference and change stay pinned while the work/actions scroll.
- Tap empty/occupied spaces to add/remove one object; Undo retains 100 moves.
- Only Check records assessment. Same wrong count cannot resubmit via rearrangement.
- Wrong work remains editable; solved work locks until explicit Next.
- Zero is visible and spoken; equation appears only after successful Check.
- Saved 5/10 rounds have unique arithmetic facts, alternating contexts and no
  adjacent repeated result. Play again avoids immediate boundary repetition.
- EN/DE narration, manual Replay/Help, shared settings, progress and balloon rewards.

## Architecture and compatibility

`learning/numberstories` owns structured facts, a pure reducer, four checksummed
write-ahead journals and the shared lazy audio adapter. `ui/numberstories` owns
chooser, worker-confined ViewModel and Compose presentation. `QuantityWork` shares
only bounded masks/toggles/Undo with One More / One Less; its old v1 bytes remain
compatible. Vocabulary singulars/IDs/art and semantic number responses are reused.
NUMBER_STORIES_CHOOSER/GAME save validated mode/limit arguments and preserve Options.
Home has 33 entries (30 native, three retained web), Maths 17. Two new route/owner
cases join the focused hosted selection; hosted validation requires a later push.

## Verification

Local build: **719/719 JVM tests**, both debug APKs, lint with zero errors, seven
existing warnings and two information findings. Raw XML is retained in evidence.
Initial intermediate runs caught outdated Home fixtures and invalid numeric-only
progress-ID segments; both were corrected before the final successful build.
A German case-sensitive wording assertion was corrected as well.

Final API 35 phone selection: **11/11 passed**, zero failures/errors/skips.
Number Stories route/owner checks cover all four configurations in real 5/10
rounds, Options/language changes, edited/wrong/solved recreation, Undo/Help,
zero and shared balloons. Progress contains exactly 34 attempts and four
completions. Four component layout cases cover both settings in every
configuration, EN/DE at 1×/1.5×/2×, portrait/landscape, keyboard Enter, Undo,
zero, stable slots, >=48dp targets and visibility of the fixed starting frame.
The same selection passes existing One More / One Less route/owner checks,
startup/recreation and all 33 Home destinations in both languages.

Raw [phone runner output](evidence/phone-final.txt) and its
[derived summary](evidence/phone-final.json) are retained. These direct
AndroidJUnitRunner summaries are not Gradle XML reports. The final build's
[source hashes](evidence/tested-source.json) and
[APK hashes](evidence/build-validation.json) still match on resumption;
[installed APK verification](evidence/resumed-installed-apks.json) confirms both
the runtime and test APK. No production/test source changed during the resumed
validation.

Final tablet component selection: **4/4 passed**, zero failures/errors/skips,
in 510.559 seconds. All eight mode/range/setting combinations are covered in
each case: English 1× portrait/landscape, German 1.5× portrait and German 2×
landscape, with the same interaction and fixed-reference assertions as the phone.
The [raw tablet runner](evidence/tablet-final.txt) and
[derived summary](evidence/tablet-final.json) use the identical verified APKs.

The [gallery](index.html) contains **84 genuine screenshots**: 20 production
phone routes, 32 phone components and 32 tablet components. The
[capture inventory](evidence/capture-inventory.json) records dimensions and
SHA-256 hashes; 15 representative images are listed in the
[visual review](evidence/visual-review.json). Catalogue generation, retained
motion parity, source/document whitespace and gallery-link checks pass. Retained
raw build/ADB logs contain five tool-emitted trailing spaces and are preserved
verbatim; they are excluded from the source/document whitespace check. The
[validation summary](evidence/validation.json) links the retained evidence.

Earlier attempts remain available: `initial-phone-runner.txt` records 6/11
passing before taking-away screenshot names were corrected to use hyphens;
the capture helper rejected underscores. Scene drawing was also clipped to its
own bounds before the final build. The resumed sandbox ADB failure ran no tests
and is retained separately. No hosted result, new browser-suite run or full-app
instrumentation pass is claimed.

## Visual review

Representative genuine captures were inspected for the chooser, edited and
solved pond/picnic boards, corrective feedback, zero and balloon completion;
phone German 1.5×/2×; tablet English portrait and German 1.5× portrait/2×
landscape, plus English tablet landscape. The inspected samples keep counted objects distinct from the scenery,
preserve the visible starting group, and wrap instructions legibly. Long work
areas intentionally scroll; screenshots can show partially scrolled text and
controls outside the viewport. Automated layout assertions establish that those
controls remain reachable. Component captures omit the production shell and may
show keyboard focus. The apple retains its canonical ivory image background.

The gallery separates production routes from component layouts. Tablet captures
use a 1600×2560/density-240 emulator override, not physical tablet hardware. Phone
captures use 1080×2400/density 420. Screenshot dimensions alone are not Android dp.

## Acceptance boundary

Physical S24/Fire checks, child use, real TalkBack/switch access, audible installed
offline voices and signed upgrade are not established by emulator automation.
No physical device or original Medium Phone userdata is used. No wipe, uninstall
or legacy removal. Tests back up/restore their scoped journals and shared progress.
The dedicated API 35 AVD used four CPUs, 6 GB RAM, host graphics and no snapshots.
After validation its display overrides were cleared to 1080×2400/density 420,
airplane mode remained at its original 1, and it was gracefully closed. ADB then
reported no attached devices; the original Medium Phone configuration hash is
unchanged. See [cleanup evidence](evidence/emulator-cleanup.json).
No implementation or local verification work remains for this checkpoint.

[Actual-screen gallery](index.html) · [Build evidence](evidence/build-validation.json)
