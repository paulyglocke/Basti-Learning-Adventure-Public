# Native verbs, concrete number bonds and CI repair

Implementation started 2026-11-07 from `9397155` and completed locally on
2026-11-09, following the owner's request to handle all three recommendations together. [Review gallery](index.html).

## Implemented

- Repaired the failed `SweepRepairsRouteTest` Options lookup. It now waits for the
  actual Habitat Matching destination, uses the stable Options semantics tag and
  waits for the recreated overlay before Back. Both original browse origins remain
  covered; no navigation check is removed or ignored.
- Native Learn Verbs/Verb Explorer covers all 53 actions: selected word,
  translation, explanation, example, Try it, offline speech, original action poses,
  scene props, water, grown-up sound modelling, finite playback, Pause and four
  bilingual picture steps. System animation-scale zero forces pictures. Navigation,
  settings and backgrounding cancel speech and motion; restored lessons stay silent.
- Native Animal Actions uses the same semantic catalogue and the existing durable
  choice host. New 5/10 rounds use distinct questions and target animals, retain
  exact choices through lessons/settings/recreation and write shared progress.
  A supported retry replaces the old new-question guessing flow for new native
  rounds. Separate speakers never submit answers. Completion uses the app's shared
  balloons, with Sound Off respected and immediate navigation.
- Build 5 and Build 10 add concrete part-whole construction. Starting eggs stay
  fixed; added eggs can be removed. Only Check records an attempt. Successful work
  shows the equation and explicit Next. Separate seeded 5/10 rounds, level journals,
  exact placement restoration, Replay/Help support, pending-write recovery and
  idempotent shared progress/completion preserve the existing Make 5 and tray.

## Content, compatibility and audio

[`content/verbs.json`](../../content/verbs.json) is the authored source. The
[generator](../../tools/generate_verbs.py) validates references/frames and produces
both typed Kotlin records and the retained web adapter; CI rejects drift. Native
routes do not load or execute JavaScript. All 106 bilingual example sentences
are explicit in the source and preserve the original lesson wording. All 39 atlases are reused from the prior
approved repair, with no new image-generation request or runtime download.
[Content authoring notes](../../content/README.md).

Old saved WEB verb bundles continue to use the retained legacy implementation.
Their indexed catalogue order, selected question/distractors, answer lock, score,
lessons and tutorials remain compatible; Mixed Adventure still uses that route.
No old web score becomes invented native attempt history. Numbers, Easy Maths and
Mixed Adventure remain three web Home destinations; the Home inventory now has
29 entries, 26 native and three web. Do not delete the compatibility path before
its separate physical/legacy removal gate.

Native introductions are language/version/reset-epoch specific and consumed only
after successful current playback. Sound Off, cancellation, stale callbacks and
reset epochs never consume an unheard introduction. Existing lazy audio controllers
and installed offline system voices remain authoritative. Sound demonstrations
invite adult modelling rather than claiming TTS is a recorded animal sound.
Older settings containing only `sound=false` also remain silent in all new native
activities; an explicit audio-mode preference takes precedence.

## Validation record

- Full JVM suite: **663/663 passed**, zero failures/errors/skips. Both debug APK
  assemblies and lint pass: zero errors, seven existing warnings and two
  informational findings. The legacy Sound Off fallback has a regression check.
- Retained browser suite: **63/63 passed**, including after the shared catalogue
  and explicit bilingual examples were extracted.
- Final combined API 35 run: **30/30 passed**, zero failures/errors/skips. This
  includes the 20-case CI selection, Home routes, all 53 lessons in both languages,
  four-step presentation, lifecycle/recreation, exact question restoration,
  number-bond journals/rewards, audio ownership, source artwork decoding, renderer
  pixel/bounds checks and four phone layout configurations.
- Separate tablet layout run: **4/4 passed**, covering both languages, large text,
  portrait/landscape, control reachability, minimum targets and keyboard use.
- Initial route run: **5/5 passed**. An earlier visual run passed **6/7**; Compose's
  recursive list-scrolling diagnostics failed in the all-lesson fixture. It now
  uses direct scroll-to-index semantics and changes language through actual Options
  inside one Activity. A later combined run passed **29/30**; the existing Wash
  Hands layout fixture timed out after requesting orientation on an Activity that
  could recreate during asynchronous artwork loading. It now uses the existing
  layout-test Activity and requests orientation on the UI thread. All original
  assertions remain, and the final combined run passes. Earlier reports are retained.
- Generated catalogue, 689-sample native/legacy motion parity and whitespace checks
  pass. [Validation summary](evidence/validation.json) records final counts, source
  hashes, APK identity, packaged-asset equality and evidence inventory. The gallery
  contains 38 UI captures and 53 renderer storyboards.

The all-53 storyboards are real Android Canvas exports from the same native renderer,
with captions added by the test. They are not UI screenshots. Files labelled
`native-lesson`, `animal-actions`, `build-` or `layout-` are actual emulator/UI
captures. Layout examples can be scrolled views; their captions say so. The native
689-sample frame/path comparison matches the retained reviewed web player; the
small Fetch cycle-boundary floating-point discrepancy found by that test is fixed.

The original AVD lacked installation space, so validation uses a separate 12 GB
API 35 emulator with four CPUs/6 GB RAM and host graphics. Its startup/ADB failures
are infrastructure outcomes, separate from assertion results. No physical phone,
`pm clear`, wipe-data or snapshot deletion was used. The temporary airplane
setting is restored to its original 0; display overrides are reset to the physical
1080×2400 / 420 dpi configuration, and our test emulator is closed. The original
AVD is preserved. Gradle managed disposable test APK installation/uninstallation.

Physical S24/Fire, actual TalkBack/switch interaction, child comprehension, installed
voice quality and signed in-place upgrades remain owner-managed acceptance. This
work does not create a signed phone release, publish anything, commit or push.
Hosted GitHub CI has not run on this new local work; the failed navigation lookup
and complete current CI selection have been verified locally.
