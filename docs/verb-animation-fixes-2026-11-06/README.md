# All 53 verb animation repairs — 2026-11-06

Implemented the owner-authorized findings from the [complete audit](../verb-animation-audit-2026-11-05/REVIEW.md).
The [after gallery](index.html) contains eight genuine production-player captures
for every lesson: **424 samples / 53 contact sheets**, with all English/German
mappings checked and zero browser page errors. Every contact sheet was visually
reviewed. [Before gallery](../verb-animation-audit-2026-11-05/index.html).

## Result

- All 53 lesson animals now use original authored poses rather than fixed emoji.
  39 reusable animal atlases provide actual wing, jaw, tail, trunk and limb
  movement; related actions reuse the same animal identity.
- Native Eagle now has eight wing poses with stable body placement. Native Rabbit
  has eight jump poses, including push-off, tucked flight and landing compression,
  with a visible grounded recovery. Approved Crocodile Snap v2 retains its eight
  poses, wide hold, 80ms closure and small recoil.
- All five water lessons show water. The full-body penguin enters below the
  waterline; the duck paddles at the surface; the otter floats belly-up.
- Bat feet meet a supporting branch and the complete hanging animal stays inside
  the stage. Climbing and scratching use visible supports; pecking/grazing/reaching
  meet their targets. Spider work grows a supported web.
- Digging displaces soil into a visible hole; ink expands from the octopus's
  siphon; the beaver progressively builds a barrier with rising upstream water.
  Food, carrot, wood, trunk-held branch and joey/pouch relationships are visible.
- Chase and Fetch have exact mappings. The mouse stays ahead of the running cat;
  Fetch shows approach, pickup, return carrying the ball and delivery.
- Watch again/Pause and four bilingual picture steps are available. Android
  animation-scale zero and browser reduced motion force static steps. Each
  demonstration finishes and holds its result; background/navigation pause it.
  Stale image loads cannot restart playback after suspension.
- Sound lessons include explicit grown-up modelling support, open by default for
  Copy Sounds. Existing shared speech and Sound Off remain authoritative; these
  instructions do not pretend that TTS is a recorded animal call.

## Scope and architecture

This repairs the existing lesson presentation in place. All 53 bilingual lesson
definitions, quiz generation, distractors, checkpoint schema, saved question,
return destination, score and progress behavior remain owned by the existing
catalogue/session code. No new assessed activity or progress identity was added.

`verb-motion-content.js` is an explicit presentation map. `verb-motion.js` owns
one disposable Canvas player, bounded source rectangles, authored timing and
supporting scene props. Related lesson animals load one shared atlas at a time;
library cards use small lazy thumbnails. There is no global decoded atlas cache.
The three debug Compose previews consume the same main WebP assets. Full native
53-lesson parity remains future work; no working curriculum was deleted.

## Artwork and prompts

Mode: **built-in image generation and image editing**, not CLI/API fallback.
Final runtime files and thumbnails are saved in
[`app/src/main/assets/VerbMotion/`](../../app/src/main/assets/VerbMotion/).
Each `{animal}-generation.json` here contains the exact final generation prompt,
selected generated source, grid and any corrective edit prompt. Originals remain
under Codex generated_images. Mechanical grid/crop/encoding normalization uses
[`tools/prepare_verb_atlas.py`](../../tools/prepare_verb_atlas.py); it preserves
aspect, pose registration and alpha rather than redrawing anatomy.

Canonical rabbit/eagle/dog/cat/horse/duck/frog/monkey/snake/dragon/dinosaur images
were exact identity references. Other animals used canonical rabbit as a style
reference only, with species anatomy specified separately. Crocodile reuses
[Snap v2 and its exact prompt](../../app/src/debug/assets/verb_motion/SNAP_V2_PROMPT.md).
Background extraction corrected sheets with paper/grid/haze; targeted edits
removed the snail's extra facial eyes and supplied the crab's eight walking legs.
All generated outputs and final rendered actions were inspected.

## Validation

- Full JVM suite: **649/649**, zero failures/errors.
- Full offline browser suite: **63/63**. Covers all 53 EN/DE lessons, speech,
  phone/tablet layout, quiz return, saved state, Options, keyboard, reduced motion,
  pause, finite playback, load failure/retry and delayed decode suspension.
- Native debug preview, API 35 emulator: **7/7**, including real bitmap alpha,
  all source cells, motion pixels, pause/background, recreation, EN/DE and large
  text/landscape picture steps.
- Production Android validation: **2/2**; every lesson opens in EN/DE, all four
  picture steps render, all 39 atlases/264 poses decode, background return stays
  paused, and Back returns through the library to Home. The final combined run
  passed **9/9**, zero failures/errors/skips, on API 35 with four CPUs/6 GB RAM.
- Both debug APKs assemble; lint passes with **0 errors / 7 existing warnings**.
- The final screenshot inventory records all 53 mappings, source poses, scene
  modes and sample positions. Android screenshots/runner evidence are separate
  from desktop Chromium captures. The seven clean Android route screenshots are
  in [Android gallery](android/index.html); their asynchronous phases are UI examples. The browser
  sheets supply the deterministic phase comparisons. Final counts, APK/source
  hashes and runner reports are in [evidence/validation.json](evidence/validation.json).

The first browser run passed 61/63: two pre-existing raw-HTML equality assertions
did not allow the new intentional Pause status on entering Options. They now
assert unchanged lesson identity/content and an exact frozen post-pause DOM/frame;
the final full 63-test run passes. The first production Android attempt timed out
on the helper's 10-second cold-WebView wait; the new test explicitly waits for
the real page/bridge boot before DOM assertions. A second attempt queried before
the WebView attached, and a third completed all 106 lesson checks before asserting
Home too early after asynchronous Back. The test now tolerates attachment and
waits for the actual Home result. Earlier reports are retained in `evidence/`.
The initial one-CPU emulator also showed a System UI hang; the final clean run
used four CPUs, 6 GB RAM and a cold start with host graphics. No physical device,
`pm clear`, wipe-data or snapshot deletion was used; Gradle managed debug test
APK installations/uninstallations on the disposable emulator. Temporary airplane
mode was restored to its original value 0 after capture. The
[native preview gallery](../verb-motion-preview/index.html) also has refreshed,
visually checked Jump/Fly/Snap recordings using the shared production artwork;
original four-pose clips remain available for comparison.

Physical S24/Fire, child comprehension, actual TalkBack/switch access and audible
installed offline voice quality were not established by these automated checks.
Owner subsequently authorized committing and pushing the completed changes.
No signed phone release or deployment was requested.
