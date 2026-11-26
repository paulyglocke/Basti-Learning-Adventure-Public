# Order by Length — local review

Implemented from `8703c4e8e3a6fddaf1f891ac8b7b4e89ad798986` after owner approval
on 2026-11-25, with validation completed on 2026-11-26. Owner requested committing
and pushing this checkpoint on 2026-11-26. Evidence records local validation
before publication; hosted checks are separate.

Arrange three strips from shortest to longest. Tap an available strip in the
left tray to append it to the three places on the right. Tap a placed strip to
return it; later strips shift up. Undo reverses edits. The board stays fixed
while instructions/actions scroll. All source/destination cells use the same
nine-unit drawing scale, constant thickness and common start lines. A strip
moves rather than appearing in two places. Colours and tray order do not encode
the answer. Minimum length gaps keep this an introductory comparison task.

Check assesses the complete order once, after all three places are filled.
The same last wrong order cannot resubmit through removal, replacement or Undo.
Wrong work stays editable with constructive guidance; correct work locks until
Next. Saved 5/10 rounds, EN/DE Replay/Help, existing audio modes, shared progress
and balloon completion are reused. Home has 36 entries: 33 native and three
retained web; Maths has 20.

LengthOrderWork adapts the existing SequenceAssembly model. Independent LNO1
write-ahead storage retains the complete plan, work, bounded Undo, assessment,
support and pending deduplicated shared events. Fresh owners restore silently.
The comparison route/journal and Wash Hands model remain compatible. The two
length activities share the exact previous Canvas drawing math. No dependencies
or raster artwork were added.

## Verification

| Scope | Result | Evidence |
| --- | --- | --- |
| Full JVM suite, APKs and lint | 751/751; 0 lint errors, 7 existing warnings, 2 information | [Build](evidence/build2.log), [summary](evidence/build-validation.json), [JVM XML](evidence/jvm-xml.zip), [lint](evidence/lint.xml) |
| New activity, phone | 6/6 passed | [Raw runner](evidence/phone-focused2.txt), [derived summary](evidence/phone-focused2.json) |
| Existing comparison, bilingual Home and startup | 9/9 passed | [Raw runner](evidence/phone-regression.txt), [derived summary](evidence/phone-regression.json) |
| New tablet layouts | 4/4 passed | [Raw runner](evidence/tablet-layouts.txt), [derived summary](evidence/tablet-layouts.json) |

The six phone cases cover actual 5/10 production rounds, 17 attempts and two
shared completions, exact partial/wrong/solved restoration, Options/language
changes, removal/Undo, changed-order Check gating, balloons, old comparison
journal byte preservation, lazy audio ownership and four component layouts.
Layouts check 2–4–8 and 2–6–8, English 1×/German 1.5× portrait and German 2×/
English 1× landscape, normal keyboard input, minimum 48dp targets, fixed board,
equal Canvas widths within one physical pixel and reachable actions.

Eight new model/host/audio tests cover seeded generation, unique round sets,
opening avoidance, Undo/support retention, all saved phases, checksummed codec,
corrupt/uncertain/concurrent journals, pending-event semantic validation and
deduplicated completion. A navigation test and explicit Home expectations also
join the full suite. Compatibility covers the old comparison route/owner/four
layouts, all 36 Home entries in each language with framework recreation and
startup. Future CI adds the new production route/owner selection;
no hosted result is claimed.

[Installed APK and tested source identities](evidence/final-identities.json)
tie these runs to the final runtime/test artifacts. Direct-runner summaries are
strictly derived from retained raw text; they are not Gradle instrumentation XML.
The first integration build found three stale catalogue assertions in two test
files; those expectations were corrected before the successful build. Original
failure XML/logs remain. The first adb invocation was blocked by the sandbox
before running tests; its raw output remains separate. Phone phase logcat had
rolled out before archiving after the overnight pause; the original completed
runner output and test assertions remain the phone evidence.

## Screens and acceptance

[Open the gallery](index.html): 42 genuine unaltered PNGs — ten production phone,
16 phone component and 16 tablet component captures. All are newer than the
capture marker and match the device SHA-256 hashes in the [inventory](evidence/capture-inventory.json).
[Twelve final images were visually reviewed](evidence/visual-review.json), covering
production placement/retry/solved/completion and every phone/tablet layout setting.
Tablet uses display overrides 1600×2560 / density 240 on the same API 35 AVD, not a
physical tablet. Its four cases repeat the phone layouts at the tablet viewport;
these are separate scoped runs, not 19 unique tests. All completed selections have
zero failures/errors/skips. Component fixtures omit the production shell/insets;
status icons can overlap their top text and scrolled text clips at the viewport
boundary. Production captures include the real shell/insets.

Progress means a checked complete ordering of three distinct strips, not ruler
use or generalised measurement mastery. Accessibility descriptions expose
internal comparison lengths; nonvisual measured ordering does not demonstrate
visual discrimination. Physical S24/Fire, child comprehension, actual TalkBack/
switch use, audible installed offline voices and signed upgrade are NOT RUN.
No new browser, full-app instrumentation or hosted acceptance is claimed.
The historical Compose disposal crash remains open; no runtime crash fix is
claimed by this feature.

## Preservation

Only the owned API 35 Basti_Pattern_20261111/emulator-5554 is used, without wipe,
uninstall, app-data clearing or snapshot deletion. Original no_backup,
shared_prefs and app_webview directories were copied intact and all 88 file
paths/bytes verified before device tests. Original Medium Phone and physical
userdata remain untouched. Tested directories were moved intact into
cache/length-order-20261125-generated and originals moved back. All 88 original
file paths/hashes and settings match; display overrides reset to 1080×2400 / density 420,
airplane 1 and animations 1.0/1.0/null preserved. Original Medium Phone configuration
hash is unchanged. No active scoped backup remains; the historical empty backup
is retained. The owned emulator closed gracefully, with process/device absence
verified. [Restoration](evidence/data-restoration.json), [cleanup](evidence/cleanup.json).
