# Phone startup failure — 2026-11-04

The owner confirmed installation of `Basti-stable-signed-v1019501`.
GitHub run [41893986946](https://github.com/paulyglocke/Basti-Learning-Adventure-Public/actions/runs/41893986946)
built that APK from `ca4ac5b`, before the startup fix in `9ace496`.

## Cause

`MainActivity.onCreate` constructs `FollowViewModel`, which initializes the
School Actions, two-action, three-action and Classroom Helper repositories.
In the distributed source those four repositories construct `ContentVersion(1, 0)`.
The shared model rejects revision zero, throwing during Activity startup.
The earlier emulator crash trace identifies `SchoolActionsContent.<clinit>`
through `FollowViewModel` and `MainActivity.onCreate`. The fix to revision 1
already exists on main in `9ace496`; it is absent from v1019501.

The installed phone's own crash log has not been captured. The confirmed APK
source and reproduced initializer failure explain the reported behavior.

## Additional findings and repairs

- The Vocabulary artwork JVM test compared the complete artwork registry with
  only the original six animal words. Added categories caused the newer build
  job to fail. The animal contract now checks its subset; a separate check
  verifies every registered asset and semantic ID.
- Android test compilation failed because an animal-only test still accessed
  the now-nullable legacy visual without asserting that it exists.
- VocabularyScreen had an indentation lint error. Explicit braces now make the
  Name-only image condition clear while retaining choices in both quiz modes.
- Artwork loader tests assumed six images. Their read/cache checks now derive
  the count from the registry and retain missing/corrupt-image isolation checks.
- CI previously executed isolated Prepositions Compose screens without
  launching MainActivity. It now includes the real shell startup, recreation
  and background-return test, plus artwork decoding checks. A JVM regression
  also initializes all five school repositories before APK packaging.
- The latest remote instrumentation job separately failed while connecting to
  its hosted emulator. Local emulator results must not be reported as a new
  successful GitHub run.

## Delivery boundary

v1019501 remains the broken signed APK. Publishing commits alone does not
replace it: after repaired CI passes, dispatch the stable signing workflow on
main to produce a newer APK with the same certificate. Install that APK over
the existing phone app; do not uninstall or clear saved data.

Uncommitted Food & Drink work was present when this investigation began and
has been preserved. Current local validation includes that pending work.

## Local validation

- Full JVM suite: 630 tests, zero failures/errors/skips.
- API 35 emulator: `MainActivityStartupTest` and `VocabularyArtworkLoaderTest`,
  three tests, zero failures/skips. This executes the real MainActivity and
  verifies all registered assets decode within the cache bounds.
- `testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug` passed;
  lint has zero errors and seven warnings. All Android test sources compile.
- Reports: `app/build/reports/tests/testDebugUnitTest/` and
  `app/build/reports/androidTests/connected/debug/`.
- This is local debug/emulator evidence. No new production-signed APK or
  successful phone upgrade has been produced during this investigation.
