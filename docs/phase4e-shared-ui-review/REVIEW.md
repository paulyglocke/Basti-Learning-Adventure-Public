# Phase 4E — Shared Learning Screen Polish

Baseline: `472cfebc261d3745c51c0ddc7785275d88608b0a` (`main`). The baseline was
clean and matched `origin/main`. This review is limited to shared shell controls
and repeated activity headings; it does not change learning content or activity
logic.

## Audit findings

| Area | Existing inconsistency | Affected screens | Severity | Proposed minimal correction |
|---|---|---|---|---|
| Shell Back/Options | Production shell buttons did not state the documented 56dp minimum. Back was visually only `←` and had no localized accessible name. | All non-Home native and legacy screens using `NativeScreenHeader` | P2 | Add 56dp minimum dimensions, a localized Back description, and stable semantics tags without changing actions or visible Options wording. |
| Activity headings | `NativeActivityTitle` rendered text without heading semantics. | Its native consumers, including Months, Seasons, Wilma, Vocabulary, Tell Me, Clock, Numbers, and Follow screens | P2 | Mark the existing title node as a semantic heading; preserve its text, style, and caller placement. |
| Repeated headings | The shell and body showed the same title on active Clock, Add Together, More or Fewer, Number Order, Numbers & Groups, How many?, Follow levels, and Follow one-step screens. Completion views omit the body title, so the shell title remains useful there. | Those eight active routes | P2 | Hide only the exact duplicate shell title while the activity body owns the heading; restore the shell title on completion. |
| Shared action buttons, support and completion | Existing `NativeActionButton`, `NativeSupportMessage`, and `NativeCompletionScreen` already satisfy their documented wrapping, target, scroll, and feedback contracts. No verified shared defect found in these components. | Seasons, Prepositions, Vocabulary, Wilma, Tell Me, arithmetic and completion screens | — | No change. Existing component and consumer tests remain in force. |
| Learning artwork | Clock and other reviewed content retained its canonical artwork and aspect behavior. No shared artwork scaling defect was established in this bounded pass. | Clock, Seasons, Months, Wilma, Vocabulary, Prepositions, Follow Instructions | — | No change. |

## Changes

- The shell Back and Options buttons now have a 56dp minimum width and height.
- Back exposes `Back` / `Zurück` to accessibility services. Stable `shell-back` and
  `shell-options` tags make the production controls testable.
- `NativeActivityTitle` now sets heading semantics. Its words, typography, and
  layout remain caller-owned.
- The shell suppresses only titles duplicated by an active body heading on the
  eight listed routes. Completion views keep the shell’s activity context.
- Callbacks, Back/Home/Options routing, route IDs, session journals, checkpoints,
  content, scoring, audio/TTS, and saved-progress formats are unchanged.

## Visual review

Actual API 35 emulator screenshots are in [the gallery](gallery.html). The before
and after Clock Explore captures show the shell Back target meeting the documented
minimum and the duplicate body title disappearing. The wheel artwork and controls
are otherwise unchanged. English and German phone portrait, German 2×, genuine
1280×600 short landscape, and 1600×2560 / 2560×1600 tablet captures were inspected.

The Clock screen remains scrollable in short landscape. At German 2× and 600px
height, the learning wheel is below the initial viewport because the shell controls,
body title, practice action, and instruction wrap at the system scale. The test
screen uses its existing vertical scrolling to keep the wheel and lower controls
reachable. Changing this Clock-specific content layout is outside the shared-
component correction and remains a separate refinement if owner review finds it
necessary. Tablet landscape uses the screen width for action buttons while keeping
the clock at its existing readable maximum.

Screenshot dimensions:

| Capture | Pixels | Orientation |
|---|---:|---|
| Phone portrait | 1080×2400 | Portrait |
| Short landscape | 1280×600 | Landscape |
| Tablet portrait | 1600×2560 | Portrait |
| Tablet landscape | 2560×1600 | Landscape |

## Validation

The prior full API 35 baseline was 569/569. Phase 4E adds two navigation/title
tests; the complete API 35 suite passed **571/571**.

- Focused `HomeProductionNavigationTest`: **7/7** passed on the isolated API 35 AVD,
  including the final Follow one-step heading assertion.
- Full JVM suite: **555/555**, zero failures/errors/skips.
- `assembleDebug`: passed.
- `assembleDebugAndroidTest`: passed.
- `lintDebug`: passed with **0 errors, 5 warnings, and 2 informational findings**.
  The warnings are existing manifest compatibility attributes (2), clock-hand
  vector dimensions (2), and the existing WebView JavaScript setting (1). The
  two informational findings are existing `mutableStateOf` recommendations.
- `git diff --check`: passed.

The first connected instrumentation attempt on the original AVD failed before
running tests with `INSTALL_FAILED_INSUFFICIENT_STORAGE` while replacing the
353MiB debug APK. `/data` reported 1.2GB free; install-location mode was `auto`
and no stale staged sessions or useful `/data/local/tmp` install artifacts were
present. A safe non-streaming replacement install failed the same way. No app
data was cleared. A separate API 35 AVD was provisioned under `/private/tmp`;
the original AVD was shut down normally after its focused run, preserving its
data. The complete suite passed on the isolated AVD.

## Remaining acceptance and recommendation

Physical-device, actual TalkBack and switch-access, child usability, and offline
voice acceptance remain owner-managed. `UX-ANR-01` remains separate. The short-
landscape, large-text Clock wheel placement is visible only after scrolling and
is a deferred screen-specific layout refinement. **Recommendation: READY TO
COMMIT** after owner review; this task remains uncommitted and unpushed.

## Changed-file inventory

- `app/src/androidTest/java/com/bellfamily/bastischool/HomeProductionNavigationTest.kt`
- `app/src/main/java/com/bellfamily/bastischool/MainActivity.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/common/NativeActivityTitle.kt`
- `docs/phase4e-shared-ui-review/REVIEW.md`
- `docs/phase4e-shared-ui-review/gallery.html`
- `docs/phase4e-shared-ui-review/before/clock-en-phone.png`
- `docs/phase4e-shared-ui-review/after/clock-en-phone.png`
- `docs/phase4e-shared-ui-review/after/clock-de-phone.png`
- `docs/phase4e-shared-ui-review/after/clock-de-2x.png`
- `docs/phase4e-shared-ui-review/after/clock-de-short-landscape.png`
- `docs/phase4e-shared-ui-review/after/clock-de-2x-short-landscape.png`
- `docs/phase4e-shared-ui-review/after/clock-tablet-portrait.png`
- `docs/phase4e-shared-ui-review/after/clock-tablet-landscape.png`
