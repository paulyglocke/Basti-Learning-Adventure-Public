# Actionable backlog

This is the execution queue, not the full curriculum. Product direction lives in [MASTER_PRODUCT_LEARNING_ROADMAP.md](MASTER_PRODUCT_LEARNING_ROADMAP.md). Detailed contracts live in the specialist specs linked there.

Completed implementation history and validation evidence belong in [BUILD_NOTES.md](BUILD_NOTES.md) and [SESSION_HANDOFF.md](SESSION_HANDOFF.md), not as a growing list of checked-off backlog items.

## Established baseline

Status reconciled against `35fc129` on 2026-10-14 and updated for Months Phase 7. “Implemented” does not mean physically accepted; current validation layers and device gaps are in [TESTING_QA_SPEC.md](TESTING_QA_SPEC.md).

Already implemented and not active implementation backlog unless a regression is found:

- shared native content IDs/repository foundation
- shared native audio controller/system-TTS boundary
- deterministic native session/checkpoint framework
- durable deduplicated native progress-event storage
- native Prepositions activity revision 3/content 1.3, with exact v1/v2 recovery and guided Listen
- native Seasons Explore/recognition, Next/Before, Build the Year, MISSING, CLUES, MATCH and COMBINED (only the specifically recorded earlier flows are owner-verified on S24; newer modes await acceptance)
- native Months Year Wheel Explore/Listen, Find Month, Before/Next, Missing Month, Between, selectable three- and four-month Build the Year, and introductory Months ↔ Seasons grouping. Phase 7 automated validation is complete; physical-device acceptance is **NOT RUN — deferred by owner**. Evidence is recorded in SESSION_HANDOFF.md.
- Interactive Seasons & Months Wheel is implemented as a distinct Explore/Match route with fixed centre artwork, a snapping Canvas ring and a separate revisioned browsing checkpoint. Focused JVM and API 35 automated coverage pass; solved-state English/German screenshots were inspected. Physical-device acceptance is **NOT RUN — deferred by owner**.
- Seasonal Weather Exploration Phase 2 extends that wheel's Explore mode with native quadrant-clipped effects, curated alternatives, bilingual manual speech and static reduced-motion presentation. No new route or quiz/progress identity. See SESSION_HANDOFF.md for current validation/review status; physical-device acceptance remains deferred.
- native Wilma Explore/Find Day/Before-After/Ordering, TODAY with an explicit hypothetical anchor, and non-quiz BILINGUAL learning (ordering auto-follow owner-verified on S24; TODAY/BILINGUAL and Fire acceptance pending)
- native Maths 1–5: How many? (Subitising), Numbers & Groups (both matching directions), More or Fewer (Left/Right/Same to a more question), Number Order (BEFORE/AFTER/MISSING), Add Together (totals 2–5), Add Bigger Numbers (addends 3–10, totals 6–20), Take Away (within 5, including zero remainder), Mixed Addition (balanced beginner/challenge rounds), Make 5 (complete five-frame number bonds), and Dinosaur Counting Tray (tap-to-build quantity construction, 1–5). Each activity has its own skill and journal. The tray is the first manipulation mode; Build 5/Build 10 add concrete number bonds as separate activities; broader object/context generalisation remains future work.
- native Vocabulary starter slice: six animal words plus eight-word illustrated Classroom Things and Food & Drink sets, each with Explore/Find/Name and separate quiz journals. Food rounds visit each word before repeating, avoid adjacent repeats, and change answer choices for repeated words in ten-question rounds.
- Classroom Things now uses the same target-cycle policy for new rounds; saved older plans still restore unchanged. Old animal journal names are recovered when a current category-specific record is absent.
- Activity-specific thumbnails and four illustrated Wash Hands choices are implemented. The latter retains the existing four-step sequence and saved progress identities. See the sweep implementation review for current verification and physical acceptance gaps.
- shared native completion celebration reused across native practice activities (automated evidence in BUILD_NOTES.md; physical acceptance remains below)
- stable external distribution signing and monotonic CI versioning
- Samsung landscape safe-inset fix
- legacy correctness/audio/session fixes already marked complete in historical notes

- Animal Groups: second shared-sorting consumer implemented (four canonical animals, Water/Land); included in the latest full JVM validation; current physical acceptance remains pending, and instrumentation evidence must be tied to its tested revision. No full Compare & Discover curriculum.

- Wash Hands: bounded four-step build/check/remove sequencing implemented; reuses existing ordering shuffle, with separate whole-sequence control-of-error. Included in the latest full JVM validation; physical acceptance remains pending, with instrumentation evidence tied to its tested revision. No full Practical Life curriculum.

## P0 — trusted daily build

- Physically accept the implemented shared native completion celebration: five balloons, one-time pop, canonical animal reveal, brief local confetti, Sound Off-aware SFX, immediate Continue/Home, no progress/session coupling. Cleaned celebration artwork is physically verified on S24 (`a8877b5`); do not repeat transparency cleanup. Remaining motion/audio/accessibility checks and Fire Max acceptance are separate.
- S24 same-key in-place upgrade is proven: 1003901 / 1.1.39.1 → 1004501 / 1.1.45.1 (owner-reported physical evidence in BUILD_NOTES.md). Keep signing/versioning as maintenance; perform the equivalent Fire Max upgrade check.
- Finish the unverified S24 Ultra matrix across the current native baseline, including the five Maths activities, Wilma TODAY/BILINGUAL, Seasons MISSING/CLUES/MATCH/COMBINED and Prepositions v3/guided Listen. Follow the expanded TESTING_QA_SPEC checklist; specifically owner-passed earlier Seasons flows/normal restore/completion and Wilma auto-follow remain closed unless a regression is found:
  - airplane-mode EN/DE and missing-voice behavior
  - Sound Off / Replay
  - portrait + both landscapes
  - large text / insets / touch / basic accessibility
  - Options/Back/background/return
  - true process recreation
  - 5/10 completion
  - progress after restart and signed upgrade
- Run the Fire Max checklist in TESTING_QA_SPEC.md, including same-key upgrade/data retention. Fire acceptance is still open.
- Wilma child-use finding: generic day choices were confusing. Selectable strip labels and ordering choices now reinforce canonical weekday colours with readable/disabled states; physically re-test recognition with Basti and check S24/Fire contrast/focus. No learning logic or auto-follow change.
- Fix critical defects found by those sweeps; avoid unrelated feature work inside P0.
- Preserve the proven permanent signing identity and monotonic versioning during all future builds.

## P1 — shared native experience

- CI includes explicit Android-test assembly/compilation, catalogue drift checks, startup, sweep navigation, Vocabulary/Prepositions/Wash Hands and the new native verb/number-bond production routes. The current source passes the complete job selection locally; hosted status changes only after a new push. Wider automated and physical S24/Fire acceptance remain tracked separately in the QA ledger.

- Lightweight shared visual-system baseline (initial rollout 2026-10-04; also reused by the native Maths activities):
  - primary, secondary/replay/help and navigation actions: shared NativeActionButton
  - text choices: shared NativeTextChoice; image-choice sharing reviewed and deferred
  - matching activity titles: shared NativeActivityTitle
  - active quiz counters: shared NativeQuestionProgress; ordering counts remain custom
  - completion layout: shared NativeCompletionScreen and existing celebration
  - broader spacing/typography/press/motion work requires a concrete bounded consumer
- Shared action buttons now cover native completion, Seasons, Prepositions, Vocabulary and Wilma non-day actions. PRIMARY/SECONDARY/NAVIGATION remain sufficient; caller-owned labels/tags/callbacks/enabled rules are preserved. Answer choices use their own presentation contract rather than action roles; mode selectors, individual option speakers and Wilma colour-cued day choices stay custom. Physical styling review remains separate from accepted learning flows. Image-choice sharing is intentionally deferred following the review below; do not begin a global rewrite.
- First text-answer choice slice is implemented in Prepositions and Seasons recognition/next/before: NativeTextChoice shares neutral geometry, wrapping and Material interaction semantics; callbacks/locking and separate Listen remain caller-owned. Physically review the neutral choice/forward-action distinction and large-text scrolling. Vocabulary's text-only What Is It? / NAME choices now also use NativeTextChoice with the existing 72dp minimum; FIND canonical-image choices and separate Listen stay custom. The bounded title and question-progress slices are now implemented; broader visual work is not blanket-complete. Do not genericize Wilma colour cues or invent correctness states.
- NativeQuestionProgress (`2b9f675`) owns only EN/DE “Question N of M” / “Frage N von M”, headlineSmall typography and natural wrapping. Callers provide one-based current/total, language and Modifier. Adopted in active Prepositions, Seasons recognition/next/before, Vocabulary FIND/NAME and Wilma quizzes; existing tags/placement preserved. Ordering counts, completion and session state remain outside the component.
- NativeActivityTitle (`6e9f3c6`) owns only headlineMedium presentation for a caller-authored string and optional Modifier. Seasons, Vocabulary and Wilma use it with unchanged EN/DE wording/placement. Prepositions has no equivalent title and is intentionally excluded; no title was invented. Prompts and other headings remain separate.
- Image-choice review: Vocabulary FIND uses a centred fixed-100dp image, hint-only visible label and retained animal semantics/fallback inside a filled Button; Seasons ordering uses 4:3 artwork, always-visible names and different missing-art gating inside an OutlinedButton. Fit alone does not justify a new wrapper; preserving these differences would require more configuration than useful shared presentation. No component was added. Revisit only with a demonstrated common contract. Wilma illustrated/day choices, colours, selected borders and auto-follow remain custom; Prepositions scenes remain passive.
- Physical S24/Fire acceptance remains open independently of implementation/test completion: EN/DE, portrait/both landscapes, 1.5× text, reachable controls, TalkBack/keyboard and disabled contrast. In particular review Wilma's quiz counter now using headlineSmall. Do not infer physical acceptance from emulator results.
- Native Android TTS ownership audit completed (2026-09-26): five eager clients per shell (four native ViewModels plus legacy), explicit stop/close paths and no confirmed playback leak/overlap defect. See NATIVE_ARCHITECTURE_SPEC for evidence and legacy robustness limits. Those four original native owners were migrated to controller-level lazy engine factories; this is historical scope, not the current total owner count: Home/OFF/settings/disposal alone allocate no native TTS, first eligible speech initializes once and retains the request, and reuse/cancellation/close stay owner-local. Legacy shell TTS is unchanged. Preserve this mechanism for future consumers; physical cold-start/resource checks remain separate. This is infrastructure cleanup, not a new audio engine or policy rewrite.
- Shared support text rollout is implemented across Seasons, Prepositions, Vocabulary and Wilma: authored retry guidance and hints use the unchanged NativeSupportMessage. This bounded presentation slice is complete; physically review calmness, readability and screen-reader flow. Answer-embedded hints and Wilma colour cues remain custom. No stronger-help/parent-help state or workflow is invented. The wider ladder remains independent attempt → Replay → subtle hint → stronger model/help → optional parent help; supported success remains success.
- First minimal support setting: Options can enlarge existing NativeSupportMessage hints/retry guidance by 25% (default Off), independently of learning/support accounting and in addition to device font scaling. Validate physical readability/scrolling on S24/Fire. Further settings require a concrete consumer; no diagnosis-labelled modes or speculative support states. The one Boolean remains in the transitional shell preference store pending the planned DataStore migration.
- Complete/reuse the existing canonical animal-art library; do not recreate accepted art.
- Vocabulary now uses the six existing canonical animal PNGs in Explore/FIND/NAME; temporary glyph rendering is removed. Physical recognition, clarity, landscape/large-text and accessibility review on S24/Fire remain. Review other temporary native representations only in later bounded work.
- Prepositions uses all 52 scene PNGs across 13 relations, activity revision 3/content 1.3, with exact v1/1.1 and v2/1.2 recovery. BELOW uses “unter”, INSIDE “in”; OUTSIDE retains “außerhalb der Höhle”. The six cave fixes and ten later borderline replacements are resolved by source-art review: 52 CLEAR, zero BORDERLINE or ARTWORK_FIX_REQUIRED; original observations remain in the audit. Physical S24/Fire clarity, guided Listen, bilingual distinctions and old-round upgrade acceptance remain open, including the replacement distance/threshold distinctions in the device matrix.
- Reuse the small Wilma/Seasons `OrderedPlacement` helper for future concrete sequencing consumers; extend it only for a demonstrated need.
- **Follow the Instructions introductory levels implemented (2026-10-14):** direct Level 1 one-step and Level 2 ordered two-step selection use four illustrated choices from the six approved animals, bilingual offline speech, deterministic 5/10 rounds, replay/hints and separate saved progress. New Level 1 rounds correct German accusative articles at revision 4/content 1.4; older checkpoints retain their historical wording. Three-step, adjective/colour-object, spatial, multi-scene, adaptive and advanced listening variants remain deferred. Physical-device acceptance is **NOT RUN — deferred by owner**.
- **School Skills starter (2026-11-04):** the Follow chooser includes contextual help-seeking phrases, classroom action picture matching, two-/three-actions-in-order challenges, and a touch-based Classroom Helper with 12 item/place instructions. Activities have separate saved progress identities; progress records app interactions only, not real-world behaviour or physical task completion. Physical-device acceptance is **NOT RUN — deferred by owner**.
- Extend that engine later rather than creating separate frameworks for School Skills, Remember the Mission, inhibition/rule switching and compatible Move & Learn prompts.
- Colour Sort establishes the first semantic sorting primitive with four red/blue native tokens, tap-to-place, durable restoration and shared progress/completion. Physical S24 acceptance remains pending. Future consumers must bring real content; drag, expanded colours and other curricula are not implemented.
- Memory Pairs starter is implemented: animal picture pairs plus numeral ↔ quantity,
  4/6/8-card boards, one semantic matching model, exact saved turns, model help and
  shared progress/rewards. Verification/physical acceptance is tracked in the
  [Memory Pairs review](docs/memory-pairs-2026-11-09/README.md).
- Extend content/data schemas only for concrete activity needs; avoid speculative framework work.
- Review German grammar, language-specific phonics, number-zero semantics and native custom-number entry when their relevant activities are touched.

## P2 — core school-readiness curriculum

- Further Seasons generalisation and advanced Months ↔ Seasons transfer. MISSING/CLUES/MATCH/COMBINED and introductory Months ↔ Seasons grouping are implemented; physical acceptance remains tracked in the QA ledger.
- Remaining Months progression: six-/twelve-month Build the Year levels; adaptive progression; and wheel-to-sequence/calendar generalisation. Three- and four-month Build the Year levels and Between are implemented; physical acceptance is **NOT RUN — deferred by owner**.
- Review Classroom Helper on-device for tap-target size, TalkBack flow and child usability. Consider more action pairs only after that review; keep object selection reversible, wrong-item exploration unscored, and location attempts clearly recoverable.
- Grow Vocabulary toward roughly 50–80 reviewed words through shared curriculum additions rather than one monolithic vocabulary task.
- Tell Me! / Erzähl mal v1: **native text/artwork consumer implemented; automated pilot validation passes, physical acceptance pending.** Home → nine authored categories → nine ordered pictures, explicit TALK/Next, optional authored Help and collapsed grown-up support, calm score-free completion. No speech input/TTS/grading/progress fabrication. Options/recreation retain the conversation; Home/new process resets to category selection. Content revision 11 adds C only to Ocean .03, Classroom .02 and Zoo .08; A/B/C/A rotation is validated, and the other 78 scenes retain A/B/A. API 35 route/layout tests and JVM content/rotation tests pass at `466315c`. Independent German and physical S24/Fire Max acceptance remain open; see `docs/tellme-variety/PILOT_VALIDATION.md`.
- Further Memory Pairs variants: word/picture, letter case, bilingual, animal/action,
  animal/habitat, emotion/expression and season/clue where educationally appropriate.
  Picture/picture and number/quantity starter boards are implemented; do not duplicate them.
- Future manipulation-first Maths: generalise the Dinosaur Counting Tray and concrete Build 5/10 boards to other objects and contexts, then explore conservation, number stories, richer pattern tasks, shapes and spatial/measurement concepts. Do not duplicate the existing construction boards or choice-based activities.
- One More / One Less starter is implemented: separate add-one/take-one-away
  construction within 5/10, explicit zero, reversible edits/Undo, saved rounds and
  shared audio/progress/balloons. Broader objects, conservation and number stories
  remain future curriculum. Current verification/physical gaps:
  [review](docs/one-more-less-2026-11-11/README.md).
- Pattern Builder starter is implemented: AB/AAB/ABC with reversible animal/shape
  construction, visible examples, explicit Check, independent saved rounds and
  shared progress/audio/balloons. Extend later to missing-middle/free creation or
  story applications; do not duplicate the starter. Current verification and
  physical acceptance are tracked in the [review](docs/pattern-builder-2026-11-10/README.md).
- Extend native Animal First Letters into a reviewed Letters & Sounds progression, keeping written letter matching distinct from true phoneme instruction. The current native written-initial activity uses canonical animal art and unique 5/10 target rounds; rhyme, phoneme work, tracing and grapheme↔phoneme content remain future curriculum.
- Focus & Flex variants built on shared instruction/rule systems.
- Feelings and calm self-advocacy language.
- Compare & Discover backed by shared animal/fact data.
- Continue collecting progress evidence; build the parent Progress dashboard only once enough native curriculum exists for useful summaries.

## P3 — product experience and broader play

- Today’s Adventure v1: curated short route with a secondary browse path and no streak pressure.
- Today’s Adventure v2: progress-informed recommendations after evidence coverage is broad enough.
- Discovery Book as a presentation layer over shared knowledge/content rather than a duplicate fact database.
- Dragon Treasure Hunt.
- Build the Bridge.
- Dinosaur Rescue.
- Crocodile Snap.
- Native Verb Explorer/Animal Actions implementation is complete in the current
  working tree, using all 53 shared lessons and existing animation artwork. Validate
  and physically review the combined checkpoint before retiring historical WEB
  bundle compatibility or migrating the remaining Numbers/Easy Maths/Mixed routes.
- Concrete Build 5/Build 10 are separate new number-bond activities. Generalise to
  other objects/contexts later; do not duplicate these boards or replace Make 5/tray
  journals. Current automated/physical evidence is in the combined review.
- Review the repaired lessons and [native preview](docs/verb-motion-preview/index.html)
  with Basti on S24/Fire; child comprehension, actual TalkBack and installed
  offline voice quality remain owner-managed acceptance.
- Broader original illustration/animation polish.
- Real-world and Move & Learn missions.
- Final app-wide visual polish: Home, buttons/cards, typography, spacing, transitions, feedback states and cohesive decorative motifs.
- Coordinated app icon/branding and proper native splash/launch screen with no artificial delay.
- Broader accessibility, reduced-motion and performance tuning on both target devices.

## Internal milestones

- **A — Native Core:** current native activities + shared foundations + stable update stream + completion celebration.
- **B — School Ready:** richer Seasons + Follow Instructions/School Skills + broader Vocabulary + early Maths.
- **C — Learning World:** Tell Me + Memory/matching + Focus & Flex + feelings + Compare/Discover + Discovery Book foundation.
- **D — Polished Adventure:** Today’s Adventure + larger games + cohesive art/design/branding + final cross-device polish.
