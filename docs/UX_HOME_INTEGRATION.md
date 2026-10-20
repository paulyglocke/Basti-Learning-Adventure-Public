# UX Refresh Phase 3 — production illustrated Home

Baseline: `d0679a8ec227ccae41ba42eed6690fde5fa47784`, main.
Implementation is uncommitted and ready for review. Do not commit or push.

## Architecture and preserved boundaries

The approved Phase 2B presentation is shared from `ui/home`, not copied into a
second implementation. Its cream/pastel theme is scoped to Home. Existing native
Options, learning screens, routes, content, ViewModels, speech, journals and
legacy assets are unchanged. No dependencies or illustrations are added.
The old flat Home renderer is replaced; the original catalogue remains authoritative.

Home has six bilingual categories and All Activities. Category cards with one
entry launch that entry directly (currently Colours). Production contains no
preview notices and uses native Options rather than prototype preferences.
Debug-only preview settings and activity demonstrations remain inspectable.

## Complete route mapping

| Category | Original entry / unchanged destination |
|---|---|
| Numbers & Maths / Zahlen & Rechnen | Numbers → WEB/count; How many? → SUBITISING; Numbers & Groups → QUANTITY_MATCH; More or Fewer → COMPARE_QUANTITY; Number Order → NUMBER_ORDER; Add Together → ADD_TOGETHER; Easy Maths → WEB/math |
| Words & Talking / Wörter & Erzählen | Tell Me → TELL_ME; Vocabulary Booster → VOCABULARY; Learn Verbs → WEB/verbExplorer; Letters → WEB/letters; Prepositions → PREPOSITIONS (positions mode) |
| Time & Seasons / Zeit & Jahreszeiten | Days & Seasons → DAYS_SEASONS; Clock & Time → CLOCK |
| Animals & Nature / Tiere & Natur | Animal Actions → WEB/verbs; Animal Groups → ANIMAL_GROUPS |
| Colours / Farben | Colour Sort → COLOUR_SORT, directly from category card |
| Everyday Skills / Alltag | Wash Hands → WASH_HANDS; Follow the Instructions → FOLLOW_LEVELS |
| All Activities / Alle Aktivitäten | All 19 above plus Mixed Adventure → WEB/mixed, in original catalogue order |

No unfinished games, duplicates, Today's Adventure or new progress categories.
Days & Seasons retains Wilma, Seasons, Months and legacy calendar access.

## Navigation and compatibility

- Category/All Activities Back and Home return to the root illustrated Home.
- Grown-ups opens existing Options; Back restores the originating browsing page
  and scroll. Language changes preserve context.
- Activity-root Back retains the existing **Home**, not category-parent, contract.
  This deliberate limitation avoids changing learning/session restoration semantics.
- Existing nested Back remains activity-owned: Months modes/Between contextual
  wheel, Build the Year levels, Seasons/Wilma, Clock practice, Follow levels and
  legacy library/lesson navigation. Explicit Home retains existing behavior.
- A validated `homePage` string is additive Android Bundle presentation state.
  Old/malformed values fall back to root; it is ignored for learning destinations.
  No journal, activity identity, content revision or progress migration occurs.
- Shared saveable presentation state retains each browsing page's scroll while
  Options/learning destinations are displayed. New task/force-stop recovery is
  not a new persistent navigation promise.

## Assets, accessibility and known limitations

Reuse canonical dragon, dinosaur/counting blocks, dragon speech bubble, seasons
centre, whale and art-table compositions. Images use Fit and are decorative in
semantics; labelled Material controls supply click/keyboard behavior. No new
automatic narration. Home keeps 32sp/40sp greeting typography, minimum 56dp
controls, natural wrapping and bounded adaptive columns.
At large text, category navigation reflows into two rows and the native Options
toolbar places its title above the controls without shrinking text.

Everyday Skills still uses the approved temporary classroom routine illustration;
a dedicated single-action handwashing/practical-routine asset remains needed.
The German 2× **debug preview-settings** compound-word wrap is still outstanding;
production uses different, existing native Options copy. Do not conflate them.
UX-ANR-01 stays open and separate; original diagnostics remain untouched.
Physical-device acceptance is **NOT RUN — deferred by owner**. Actual TalkBack,
switch-access and child usability acceptance are not claimed.

## Validation and visual review

Isolated layout fixtures use debug-only `ui/testing/LayoutTestActivity`, which
handles orientation/screen-size changes like MainActivity. The generic empty
ComponentActivity could be replaced by a pending rotation after the test installed
its content, leaving no Compose hierarchy. This was reproduced and recorded in
the continuation logs. The host change preserves the existing assertions and
timeouts. Real MainActivity route/Options/recreation coverage is unchanged;
the layout host is non-exported and absent from release builds.

Evidence folder: `/private/tmp/basti-home-resume-20261019/`. Fresh JDK 17 full
JVM run: **555/555**, zero failures/errors/skips. `assembleDebug`,
`assembleDebugAndroidTest` and `lintDebug` passed; lint reports five warnings and
two informational findings. API 35 focused Home matrix: **86/86**; production
route/saved-session matrix: **24/24**; final typography rerun: **12/12**.
Browser **55/55** is historical evidence for unchanged web sources, not rerun in
this continuation. The whole instrumentation suite was not run.

The final gallery contains 23 production MainActivity captures across EN/DE,
phone/tablet, portrait/landscape, 1×/2× text, categories, All Activities,
Options/return and native/legacy activity dispatch. Visual review found and
resolved two large-text toolbar wraps; the refreshed captures show the corrected
layout. Gallery: `/private/tmp/basti-home-resume-20261019/index.html`.

The API 35 emulator was offline during automated tests. Its saved airplane,
Wi-Fi, mobile-data and physical display size/density settings were restored
afterward. `LayoutTestActivity` is debug-only and non-exported. Automated results
do not establish physical S24/Fire behavior, actual TalkBack or switch-access
usability, child comprehension, or offline voice quality. Those checks remain
open; physical-device acceptance was deferred by the owner.
