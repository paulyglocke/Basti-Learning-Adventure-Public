# App sweep — first implementation pass

Source baseline: `680f9b2`; owner authorized the prioritized repairs and visual
improvements after reviewing the [audit](../app-sweep-2026-11-05.md).

## Implemented

- Animal Vocabulary can recover old `vocabulary-find` / `vocabulary-name`
  journals when its current category-specific store has no record. The existing
  session host validates before promotion. Current records take precedence;
  corrupt records are preserved, and the original legacy store is never changed.
  Tests cover answered rounds, retained support/choices/language, progress
  deduplication, corrupt records and uncertain commit recovery.
- Habitat Matching retains both `all` and `category:ANIMALS` return origins when
  Options is recreated. Real production navigation tests exercise both paths.
- New Classroom Things rounds visit all eight word targets before repeating,
  avoid adjacent repeats and vary choices on necessary repeats. Existing
  classroom checkpoints, including their older repeated-target plans, restore
  byte-for-byte without revision/schema changes.
- Every one of the 27 Home activities now has its own artwork descriptor.
  Existing animal/scene art is reused; native diagrams distinguish the Maths
  exercises. Category banners still identify root Home sections. Labels,
  accessible titles, dispatch and Back behavior remain caller-owned.
- Asset thumbnails and sequencing pictures share an 8 MiB background decode
  cache, with bounded images and safe missing/corrupt-asset handling.
- Wash Hands now has four original illustrated, clickable choices. The child can
  select the picture; Listen stays separate. Chosen steps retain their picture,
  spoken label and Remove action. Normal phone width uses two columns; larger
  text uses one. Sequence IDs/order, support, retries, progress and completion
  remain the existing authored four-step activity.
- Options explains that Number level applies to Numbers and number questions
  inside Mixed Adventure. The Follow home description now covers classroom
  routines rather than describing only the introductory one/two-step levels.

## Artwork

Generated with the built-in image generation tool and saved in
`app/src/main/assets/wash_hands/`: `water_on.jpg`, `wet.jpg`, `soap.jpg`,
`rinse.jpg`. Four 512px JPEGs total approximately 400 KiB. The
[asset README](../../app/src/main/assets/wash_hands/README.md) records the full
shared prompt and the step-specific requests. The images were visually inspected
for distinguishable handle/water/foam/rinsing actions; they are instructional
pictures for the existing app slice, not a new real-world hygiene curriculum.

## Final verification

- Full JVM suite: **642 passed**, zero failures/errors/skips.
- Focused API 35 suite: **13 passed**, zero failures/skips. Covers repaired
  navigation, all new file assets and cache bounds, Home EN/DE callbacks,
  normal-phone pictures, EN/DE 1.5× text, portrait/both landscape sequencing,
  keyboard, Listen/Undo/Help/retry/completion, real app startup and the existing
  Food/animal Vocabulary production flows.
- `testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug
  connectedDebugAndroidTest` passed with the focused class selection recorded
  in the corresponding test sources. Lint: zero errors, seven existing warnings.
- `git diff --check` passed. No baseline file or ignored failures were added.
- The initial emulator pass had two fixture problems: a category assertion used
  a tag that was not present on the category page, and the normal-phone visual
  test inherited the preceding landscape orientation. Assertions now check the
  actual browse content; the normal visual fixture explicitly requests portrait
  and scrolls each picture into view. The final complete 13-test pass supersedes
  that initial run. Screenshots use synchronized Compose capture rather than a
  premature OS capture of placeholders.
- Physical S24/Fire touch, TalkBack, audible voice quality and signed upgrades
  are not inferred from these tests. No production-signed APK was generated here.

## Visual review

[Browse gallery](gallery.html). The five images are genuine final-source API 35
emulator captures. No mockups or generated app-screen images are used.

The five HTML Home entries remain available. Shared native Verbs/Animal Actions
is the next migration project; it must retain the current 53-lesson curriculum,
review missing action artwork and preserve quiz/lesson/session behavior before
replacing either route.
