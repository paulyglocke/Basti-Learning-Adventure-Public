# Third-question pilot validation — 2026-10-10

Status: **native execution blocked; rendered layout review not performed**.
Content revision 11 remains a local, uncommitted pilot. Do not expand third
questions based on this validation attempt.

## Execution evidence

The focused offline Gradle command was attempted in the current checkout:

```sh
./gradlew testDebugUnitTest --tests com.bellfamily.bastischool.learning.tellme.TellMeQuestionRotationTest --tests com.bellfamily.bastischool.learning.tellme.TellMeFlowTest --tests com.bellfamily.bastischool.learning.scenedescription.SceneDescriptionContentTest --offline
```

It exited 1 before Gradle or any test started:

```text
ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
```

`java`, `kotlinc` and `adb` were absent from PATH. No JDK, Gradle cache or Android
SDK was found in the checked standard locations. `/opt` contains only the Node
tooling directory. No dependencies were downloaded, CI dispatched, APK produced,
commit made or push attempted. Remote baseline CI cannot validate these local edits.

The preceding checkpoint's 32 Python content tests, generated Kotlin check and
whitespace check passed. They validate content/preservation, not JVM execution,
Compose rendering or Android lifecycle behavior.

## Source inspection

TellMeScreen resolves support from scene, current language and questionIndex once,
then uses that support for prompt, starter, words, model and grown-up expansion.
Help gates starter/words/model; grown-up disclosure gates the child/model expansion
and guidance. Text wraps without a fixed line count; the picture uses Fit. These
source properties do not establish that rendered text fits at large font sizes.

The existing cursor advances modulo each scene's bundle count and is owned by the
Activity ViewModel. The authored JVM pilot test covers C pairing in both languages,
stale callbacks, wraparound and fresh-owner reset. The adventure test exercises six
complete visits for mixed two-/three-slot scenes. The new route test targets Ocean
.03 C through Help/disclosure, language/Options, recreation, background/return and
Home re-entry. All native test changes remain uncompiled and unrun here.

## Resume commands in a configured checkout

Use JDK 17, the project's Gradle 8.9 wrapper and Android SDK 35. Execute against
these local edits, not a different main snapshot. First run the focused JVM command
above (omit --offline if dependencies are not cached), then:

```sh
./gradlew assembleDebug lintDebug compileDebugAndroidTestKotlin
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.bellfamily.bastischool.ui.tellme.TellMeRouteTest
```

An attached emulator/device is required for connectedDebugAndroidTest. Also run
TellMeScreenTest and TellMeArtworkTest using the same instrumentation class filter
before reporting layout/artwork acceptance. Record test totals, failures, build
identity and device/API details. Fix actual failures before expanding the pilot.

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
