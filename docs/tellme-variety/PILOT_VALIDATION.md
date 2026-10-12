# Third-question pilot validation — refreshed 2026-10-12

At source revision `466315c` (which contains Tell Me content revision 11),
automated native validation **passes**. This supersedes the blocked first attempt
below. The C pilot remains limited to Ocean .03, Classroom .02 and Zoo .08; no
educational content was changed during this validation checkpoint.

## Current validation evidence

- Full JVM suite: **490 passed**, zero failures/errors/skips. Includes Tell Me
  question rotation, complete flow and Scene Description content validation.
- Scene Description Python suite: **32 passed**; generator check confirms 9
  categories / 81 approved scenes and current generated Kotlin.
- API 35 emulator instrumentation: **88 passed**, zero failures/skips. Tell Me
  coverage: 2 route/lifecycle tests, 8 bilingual/layout screen cases and 3
  artwork-loader tests. Route coverage enters C, opens Help and grown-up support,
  switches language through Options, recreates the Activity, backgrounds/returns,
  returns Home and verifies the next visit rotates to A. Layout tests exercise
  bilingual prompt/help/model wrapping, scrolling and control reachability in
  portrait and both landscape orientations at 1.5× font scale. This is emulator
  evidence, not physical-device or TalkBack acceptance.
- `assembleDebug` and `lintDebug`: **PASS**. Lint has zero errors, five existing
  warnings and two informational findings, all outside Tell Me/Months changes.

Still open: independent German naturalness, manual visual inspection of the exact
C bundles at narrow/short/large-font layouts, TalkBack audition, physical S24/Fire
Max acceptance and installed offline-voice quality. Emulator checks do not close
those human reviews.

## Historical first execution attempt (2026-10-10)

The focused offline Gradle command was attempted in the current checkout:

```sh
./gradlew testDebugUnitTest --tests com.bellfamily.bastischool.learning.tellme.TellMeQuestionRotationTest --tests com.bellfamily.bastischool.learning.tellme.TellMeFlowTest --tests com.bellfamily.bastischool.learning.scenedescription.SceneDescriptionContentTest --offline
```

At that earlier checkpoint it exited 1 before Gradle or any test started:

```text
ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
```

`java`, `kotlinc` and `adb` were absent from PATH. That earlier limitation is
superseded by the validation above and does not describe the current toolchain.

The preceding checkpoint's Python and generator evidence did not validate JVM,
Compose rendering or Android lifecycle behavior; those checks have since run as
listed above.

## Source inspection

TellMeScreen resolves support from scene, current language and questionIndex once,
then uses that support for prompt, starter, words, model and grown-up expansion.
Help gates starter/words/model; grown-up disclosure gates the child/model expansion
and guidance. Text wraps without a fixed line count; the picture uses Fit. These
source properties do not establish that rendered text fits at large font sizes.

The existing cursor advances modulo each scene's bundle count and is owned by the
Activity ViewModel. Passing JVM pilot tests cover C pairing in both languages,
stale callbacks, wraparound, six complete visits across mixed two-/three-slot scenes
and fresh-owner reset. The passing route test targets Ocean .03 C through
Help/disclosure, language/Options, recreation, background/return and Home re-entry.

## Resume commands in a configured checkout

For repeat validation use JDK 17, the project's Gradle 8.9 wrapper and Android SDK
35. At source revision `466315c`, these commands were executed successfully:

```sh
./gradlew assembleDebug lintDebug compileDebugAndroidTestKotlin
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.bellfamily.bastischool.ui.tellme.TellMeRouteTest,com.bellfamily.bastischool.ui.tellme.TellMeScreenTest,com.bellfamily.bastischool.ui.tellme.TellMeArtworkTest
```

An attached emulator/device is required for connectedDebugAndroidTest. Record test
totals, failures, build identity and device/API details. Fix actual failures before
expanding the pilot.

## Rendered bilingual acceptance

Review Ocean .03, Classroom .02 and Zoo .08 on their third entered visit. Verify
the exact C prompt plus optional starter, three words, model, short child example
and adult guidance against THIRD_QUESTION_PILOT.json. Confirm a fourth visit returns
to A and an ordinary two-slot scene still returns A/B/A.

Capture each pilot in EN and DE with Help and grown-up support open: narrow portrait,
short landscape and large font (1× and 2×); include the larger-hints option. Check
wrapped text, scrolling, reachable Next/Home/disclosure controls and full picture
composition. Review TalkBack focus/readout and confirm support remains C through
Options, language switch, recreation and background/return. A new process starts
at category selection and a fresh owner starts at A, under the existing contract.

Obtain independent German review and physical S24/Fire acceptance separately.
Imaginary events and feelings stay optional conversation ideas, never answer keys.
