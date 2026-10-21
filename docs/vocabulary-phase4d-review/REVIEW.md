# Vocabulary Booster Phase 4D review

## Inventory and existing behavior

Vocabulary is a native Compose screen owned by `VocabularyViewModel`; it is not a
WebView or a set of child Activities. Illustrated Home → Words & Talking →
Vocabulary Booster now opens the new chooser route. The chooser launches each
mode on the existing `VOCABULARY` route in one selection.

| Mode | Existing label (EN / DE) | Destination and state owner |
|---|---|---|
| Explore | Learn words / Wörter kennenlernen | `VOCABULARY`; browsing selection in `vocabulary-selection`; no progress journal |
| Find | Find the word / Finde das Wort | `VOCABULARY`; `VocabularyViewModel`, `vocabulary-find` journal/checkpoint |
| Picture to word | What is it? / Was ist das? | `VOCABULARY`; `VocabularyViewModel`, independent `vocabulary-name` journal/checkpoint |

The six existing animal concepts are Dinosaur/Dinosaurier, Snake/Schlange,
Whale/Wal, Horse/Pferd, Crocodile/Krokodil and Fish/Fisch. Their stable IDs and
existing English/German words, examples and prompts come from
`VocabularyContent`; `VocabularyArtwork` maps each ID to its existing canonical
image under `Animals/canonical`. Inspection found every mapped asset in the app
assets folder. No mapping, word, translation, order, question or art file changed.

Explore selects an animal, speaks its name, provides Listen again and sentence
Listen, and invites the child to use the word in a sentence. Find speaks a word
prompt and offers four picture answers with separate per-answer Listen, Replay,
Help, retry feedback and completion. What Is It? presents the prompt picture and
four text answers with separate Listen, Replay, Help, retry feedback and
completion. The screen uses the existing `native-vocabulary` audio owner and
existing offline speech configuration. The app preference controls whether
prompts are narrated. Options returns to the originating chooser or activity;
language changes keep the active route and update the current quiz's language.

## UX findings and changes

Before Phase 4D, all three mode selectors appeared above the main learning
content. Explore then placed six word buttons ahead of the selected animal image,
word and listening controls; the picture was fixed at 160 dp. This made a child
scroll through activity and vocabulary choices before reaching the main visual
lesson.

The new compact chooser features Explore and exposes both existing practice
modes directly. Explore now puts the selected canonical image first, then its
word, replay, spoken example and child-language prompt; the six word choices
follow that learning content. The image uses fitted artwork and adapts its
display height to phone/tablet width. The practice screen retains its own
question-first hierarchy. Text-answer buttons keep their 72 dp minimum and now
have an explicit keyboard focus target with Enter/Space activation through the
same answer callback. A neutral warm background and the established vocabulary
surface/typography tokens replace the competing blue treatment.

No words, translations, image paths, activity IDs, scoring, difficulty, question
generation, feedback, audio policy, saved-session ownership, journal/checkpoint
formats or progress events were changed. The six items remain in the same
canonical content collection. Explore remains unscored. The two quiz modes keep
independent journals. The activity chooser adds only the `VOCABULARY_CHOOSER`
navigation state; old saved `VOCABULARY` routes still restore directly to the
activity.

## Visual evidence

The gallery and capture script are linked from [`index.html`](index.html) and
[`capture.py`](capture.py). Screenshots are from the API 35 emulator and actual
MainActivity routes. Captures cover English and German chooser, German 2×,
Explore in both languages, both practice modes, phone landscape and short
landscape, tablet portrait/landscape, Options return, and a saved session after
Activity recreation. All 14 PNG dimensions were checked against their intended
viewport: phone 1080×2400, landscape 2400×1080, short landscape 1920×1080,
tablet portrait 1600×2560, and tablet landscape 2560×1600.

The gallery's before/after comparison describes the baseline hierarchy and
shows the final emulator captures. A baseline screenshot was not captured, so
the comparison is not a paired image comparison. The final Explore images show
the canonical animal picture before the six-word selector, with word and
listening/example controls directly alongside it on tablet.

Visual inspection found the chooser's Explore card clearly primary, with both
practice activities directly available. The fitted illustrations remain large
and uncropped. On a phone at 2×, the German chooser wraps naturally and scrolls;
the second practice card extends below the first viewport but is reachable by
scrolling. Phone landscape uses a vertical learning flow, while tablet Explore
places a large image on the left and the word, replay, example and all six
animal choices on the right. Short landscape is a genuine 1920×1080 frame. No
clipped text, overlapping controls, or artwork distortion was visible in the
reviewed captures. Explore language and illustration remain readable; detailed
TalkBack and physical-device acceptance are still outstanding.

## Validation

- Focused Vocabulary JVM content/art/audio/navigation tests: 35/35 passed.
- Full JVM suite: 555/555 passed, 0 skipped.
- Focused Vocabulary API 35 instrumentation: 14/14 passed, covering chooser,
  screen behavior, keyboard/text choices, artwork and production
  navigation/restoration.
- Shared Home/Months/Seasons/Wilma/Tell Me API 35 regressions: 14/15 passed.
  One unrelated `SeasonsChooserRouteTest.illustratedHomeChooserEveryModeOptionsBackAndSharedMonthsRoutesRemainAvailable`
  failure reproduces alone: after selecting `ORDER`, the test cannot find
  `seasons-order-prompt` (line 72). No Seasons code or assertion was changed in
  this task; the failure remains unresolved and is not reported as passing.
- Complete app-wide API 35 instrumentation: not completed. The Gradle connected
  install previously failed with `INSTALL_FAILED_INSUFFICIENT_STORAGE`; direct
  focused API 35 runs were used. An earlier broad runner attempt was stopped
  before completion and is not counted as a pass.
- Debug build, Android-test compilation and lint: passed. Lint reports 0 errors
  and 5 existing warnings (two manifest API-level attributes, two vector-size
  advisories, and the existing WebView JavaScript setting).
- `git diff --check`: passed.

The first connected Gradle instrumentation attempt could not install the 353 MB
debug APK (`INSTALL_FAILED_INSUFFICIENT_STORAGE`) and ran zero tests. An earlier
targeted run had three stale fixture failures after the Explore view stopped
using quiz-only state and the inline mode chips were removed; the fixtures were
corrected without weakening learning assertions. Direct APK installation and
`am instrument` then produced the final focused results above. No emulator app
data was cleared; existing app data was retained across debug APK replacement.

Phase 4C housekeeping: the stale “uncommitted” status is corrected in
`docs/wilma-phase4c-review/REVIEW.md`; it now references commit
`24de69103af3d79dc1966eb93b3bfd49b8ec2556`. Its speech-condition note states
that the full 567-test run preceded restoration of the `!failed` guard and that
the later focused Wilma audio test does not cover that ViewModel persistence
failure branch. No speech behavior was changed here.

## Outstanding acceptance and risks

Physical-device acceptance, actual TalkBack, switch access, child usability and
offline-voice acceptance remain owner-deferred; none is claimed as passed.
UX-ANR-01 remains separate. Screenshot review cannot establish real-world
pronunciation quality. Existing lint warnings remain documented in the final
validation summary. The isolated Seasons test failure and incomplete broad
instrumentation run are outstanding. This work is uncommitted and unpushed for
owner review.
