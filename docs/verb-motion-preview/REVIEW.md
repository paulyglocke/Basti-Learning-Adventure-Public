# Native verb movement preview

## Current update — 2026-11-06

The [53-lesson presentation repair](../verb-animation-fixes-2026-11-06/README.md)
is complete locally. These three debug Compose previews now consume the same main
WebP atlases: eight jump poses within the sixteen-pose rabbit sheet, eight eagle
wing poses and the approved eight-pose crocodile. The historical proof below
describes the earlier debug PNGs; it is not the current asset layout.

The gallery's Jump/Fly/Snap clips were refreshed on API 35 at 720×1600 and visually
checked across sampled frames: actual crouch/flight/landing, wing strokes and jaw
opening/closure are present. Durations are 8.39s, 8.37s and 7.74s respectively.
The capture harness waits for enabled playback controls and validates duration
before replacing clips. Original four-pose clips remain as `*-v1.mp4` comparisons.
The unchanged German crocodile picture steps are retained as a genuine screenshot.

Final verification: **649 JVM / 63 browser / 9 combined emulator tests passed**;
both debug APKs assemble; lint has **0 errors / 7 existing warnings**. Full runner
evidence is in the repair review. Production remains on its existing WebView
curriculum route; this is not a full native migration. Owner subsequently authorized
committing and pushing the completed work.

## Historical three-verb proof

Baseline: `a990f34`. Owner approved a three-verb visual proof before the full
53-lesson Verbs/Animal Actions migration. This is an explicit-launch **debug-only**
activity, matching the existing Home/design showcase pattern. No production route,
legacy lesson, question plan, preference or progress journal is replaced.

## Learning and visual contract

- Jump uses the existing rabbit subject: crouch → push off → airborne → land.
- Fly uses the existing eagle subject: wings up → out → down → lift again.
- Snap its jaws uses the existing crocodile subject: rest → focus/anticipation →
  two opening poses → wide-open hold → two fast closing poses → firm closure.
  Closing is deliberately faster than opening, without impact effects,
  prey or aggressive sound.
- Rabbit/eagle use 1024px transparent PNG atlases with four matched key poses.
  Crocodile v2 uses eight matched poses in `snap-v2.png` (1600×800; 4×2 grid).
  The canonical animal pictures were the identity references. The built-in image
  generation tool was used; the complete [prompt set](../../app/src/debug/assets/verb_motion/README.md)
  is retained with the assets. The originals remain in Codex generated_images.
- Compose crops the fixed poses and follows a deterministic timeline. It does not
  generate frames at runtime, morph between unrelated animals or introduce a general
  animation framework. Native geometry adds stationary ground/air context.
- Playback is child-controlled, loops until paused and restarts on Watch again.
  Pause holds the current frame. Backgrounding stops playback and speech; returning
  stays paused, including pause/resume transitions between Compose frames.
- Picture steps are always selectable. System animation-scale zero forces the
  still-picture version and removes the motion clock. Labels and explanations are
  bilingual; the drawing has a stable description rather than rapidly changing
  screen-reader announcements.
- Listen is separate from motion and uses the existing lazy shared native audio
  controller and offline system-engine adapter. ALL/QUESTIONS allow manual Listen;
  OFF remains silent. Replacement/language/exit/background cancel old requests.
  Restoration is silent. The preview never infers practice, correctness or mastery
  from watching and writes no Progress Tracker events.

## Review artifacts

[Open gallery](index.html). `native/*.mp4` are eight-second genuine emulator
recordings, not generated screen mockups. Posters and the German picture-step
image are genuine screenshots. Videos intentionally have no captured narration.
`capture.py` reproduces the recordings from an installed current debug APK on an
emulator and restores the prior motion/rotation settings; it never clears app data.

Launch after installing a debug build:

```sh
adb -s emulator-5554 shell am start -n com.bellfamily.bastischool/.ui.verbmotion.VerbMotionActivity --es verb jump
```

Optional extras: `--es verb fly`, `--es verb snap`, `--es language de`.
The activity and Compose code live under `src/debug`, so they are not included
in the release source set or linked from child-facing production Home. Artwork is
now shared from `src/main`; the earlier PNGs remain under `src/debug` for history.

## Acceptance boundary

This is a key-pose sprite study with smooth native travel/height changes, not an
accepted articulated animation system. Small pose-to-pose body/head alignment
variation remains a review consideration, especially for the eagle and crocodile.
The full loops need owner/child review for whether the action is understandable
without reading. Physical S24/Fire, actual TalkBack and audible offline voice
quality are not established by screenshots, fake speech or emulator checks.
Independent German language review remains separate. Do not replace either legacy
route or scale to 53 lessons on the strength of build success alone.

Development review found and corrected an initially invisible paused eagle and
clipping of the airborne rabbit's ears. The first emulator run also exposed two
fixture timing errors: the manually frozen test clock did not advance async image
composition, and the close assertion ran before Android finished destroying the
Activity. Both fixtures now wait for the actual state; no check is ignored.

## Final-source verification

The results below record the original three-verb proof. The stronger snap follow-up
and its fresh verification are recorded separately at the end.

- Full JVM suite: **648 passed**, zero failures/errors/skips, including six new
  motion/content/audio cases. Tests check the jump arc and invisible loop reset,
  wing-pose changes, faster snap closing, deterministic bounded cycles, visible
  initial animals, PNG headers/bilingual stages, lazy Sound Off and cancellation.
- API 35 / Android 15: **8 passed**, zero failures. This includes actual pixel
  changes for all three demonstrations; a top-edge check for the airborne rabbit;
  Pause, background, silent return and rapid between-frame lifecycle transitions;
  live system reduced motion; all four actual atlas cells; selection/language
  recreation; EN/DE 1×/1.5×/2× portrait/landscape controls; keyboard Listen; Close;
  and the existing real MainActivity startup regression. The
  [final runner output](emulator-test-output.txt) is retained.
- `./gradlew testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug`
  passed. Lint: **0 errors, 7 existing warnings**. Python capture syntax and
  `git diff --check` passed.
- The focused emulator run used the installed current debug and Android-test APKs
  with `adb shell am instrument -w -r -e class` selecting
  `VerbMotionScreenTest`, `VerbMotionLayoutTest`, `VerbMotionActivityTest`, and
  `MainActivityStartupTest`. No full app-wide instrumentation or signed build was
  run. The current checkpoint is local and uncommitted; no commit/push was requested
  for this visual-review task.

## Stronger snap follow-up — 2026-11-05

Owner requested a more expressive and visibly stronger action, with more frames.
The crocodile now has eight authored poses rather than four, with focused brows,
a much wider mouth and a brief closed-jaw squint. The deterministic 2.8s native
cycle opens over 750ms, holds the widest pose for 400ms, then closes in 80ms.
A small forward nudge and damped recoil settle back to the original position;
feet remain at the same ground height. There is no camera shake or added sound.

Picture steps remain four concise stages using physical atlas frames 0/2/4/7.
Their logical accessibility/test identities remain 0/1/2/3. The atlas renderer
now accepts per-sample grids and preserves source aspect. Eight-pose decoding,
all-cells pixel coverage, timing, deterministic bounds and return-to-rest checks
cover the change. Existing Pause, lifecycle, silent restore and reduced-motion
behavior remains in the same owner.

The original `snap.png` and `native/snap-v1.mp4` are retained for comparison in
the gallery. The [complete v2 prompt](../../app/src/debug/assets/verb_motion/SNAP_V2_PROMPT.md)
records the built-in image-tool edit and saved asset. This remains a local debug
review prototype; no legacy lesson, question, progress or release route is replaced.

Fresh v2 verification: **649 JVM tests passed** (zero failures/errors/skips),
**8 focused API 35 emulator tests passed**, both debug APK builds passed, and lint
reports **0 errors / 7 existing warnings**. The
[v2 runner output](emulator-test-output-v2.txt) records all eight cases. The built
APK's atlas was checked as 1600×800 before installation. The source generator's
1774×887 output was normalized to the divisible 4×2 grid, and the debug APK was
reassembled after that normalization; the emulator tested that final asset.
`git diff --check` passes. No production source, signing or progress migration is
part of this follow-up; the review checkpoint remains uncommitted.
