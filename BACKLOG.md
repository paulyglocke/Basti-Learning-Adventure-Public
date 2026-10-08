# Actionable backlog

This is the execution queue, not the full curriculum. Product direction lives in [MASTER_PRODUCT_LEARNING_ROADMAP.md](MASTER_PRODUCT_LEARNING_ROADMAP.md). Detailed contracts live in the specialist specs linked there.

Completed implementation history and validation evidence belong in [BUILD_NOTES.md](BUILD_NOTES.md) and [SESSION_HANDOFF.md](SESSION_HANDOFF.md), not as a growing list of checked-off backlog items.

## Established baseline

Status reconciled against implementation `ccc6423` on 2026-10-08. “Implemented” does not mean physically accepted; current validation layers and device gaps are in [TESTING_QA_SPEC.md](TESTING_QA_SPEC.md).

Already implemented and not active implementation backlog unless a regression is found:

- shared native content IDs/repository foundation
- shared native audio controller/system-TTS boundary
- deterministic native session/checkpoint framework
- durable deduplicated native progress-event storage
- native Prepositions activity revision 3/content 1.3, with exact v1/v2 recovery and guided Listen
- native Seasons Explore/recognition, Next/Before, Build the Year, MISSING, CLUES, MATCH and COMBINED (only the specifically recorded earlier flows are owner-verified on S24; newer modes await acceptance)
- native Wilma Explore/Find Day/Before-After/Ordering, TODAY with an explicit hypothetical anchor, and non-quiz BILINGUAL learning (ordering auto-follow owner-verified on S24; TODAY/BILINGUAL and Fire acceptance pending)
- native Maths 1–5: How many? (Subitising), Numbers & Groups (both matching directions), More or Fewer (Left/Right/Same to a more question), Number Order (BEFORE/AFTER/MISSING), and Add Together (nonzero addends, totals 2–5). These are visual choice activities, not quantity-construction/manipulation modes.
- native Vocabulary starter slice
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

- Close the native UI validation gap using existing affected Compose/owner/navigation suites on the designated emulator. Add explicit Android-test compilation and bounded emulator regression coverage to CI in a separate task. Current CI runs JVM/Python/browser/build/lint checks, not instrumentation; local compilation is not execution.
- Resolve the ten documented BORDERLINE Prepositions scenes through targeted visual/learner review against [PREPOSITIONS_CONTENT_AUDIT.md](PREPOSITIONS_CONTENT_AUDIT.md). Record accepted-with-reason or artwork-fix decisions per scene; elevate confirmed teaching defects to P0. Do not regenerate the six resolved cave replacements or label all borderline scenes confirmed defects.

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
- Prepositions uses all 52 scene PNGs across 13 relations, activity revision 3/content 1.3, with exact v1/1.1 and v2/1.2 recovery. BELOW uses “unter”, INSIDE “in”; OUTSIDE retains “außerhalb der Höhle”. The six cave replacements are resolved by source-art review: 42 CLEAR, ten unchanged BORDERLINE, zero current ARTWORK_FIX_REQUIRED. Physical S24/Fire clarity, guided Listen, bilingual distinctions and old-round upgrade acceptance remain open; the bounded borderline review is listed above.
- Reuse the small Wilma/Seasons `OrderedPlacement` helper for future concrete sequencing consumers; extend it only for a demonstrated need.
- **Follow the Instructions v1.1 implemented (2026-09-27):** native one-step bilingual tap instructions now draw four-object scenes from six approved animals (crocodile, dinosaur, snake, fish, horse and whale), with deterministic target/set variation and revision-1 checkpoint recovery. Physical S24/Fire acceptance remains open. Future multi-step, attribute, spatial and inhibition variants remain deferred.
- Extend that engine later rather than creating separate frameworks for School Skills, Remember the Mission, inhibition/rule switching and compatible Move & Learn prompts.
- Colour Sort establishes the first semantic sorting primitive with four red/blue native tokens, tap-to-place, durable restoration and shared progress/completion. Physical S24 acceptance remains pending. Future consumers must bring real content; drag, expanded colours and other curricula are not implemented.
- Build Memory Pairs on a reusable semantic matching model when that work starts.
- Extend content/data schemas only for concrete activity needs; avoid speculative framework work.
- Review German grammar, language-specific phonics, number-zero semantics and native custom-number entry when their relevant activities are touched.

## P2 — core school-readiness curriculum

- Further Seasons generalisation and Months ↔ Seasons. MISSING/CLUES/MATCH/COMBINED are already implemented; their remaining validation belongs above, not in a duplicate implementation queue.
- School Skills as an early Follow-the-Instructions content pack: classroom instructions plus help-seeking/self-advocacy.
- Grow Vocabulary toward roughly 50–80 reviewed words through shared curriculum additions rather than one monolithic vocabulary task.
- Tell Me! / Erzähl mal v1: **native text/artwork consumer implemented; physical acceptance pending.** Home → nine authored categories → nine ordered pictures, explicit TALK/MODEL/Next, optional authored Help and collapsed grown-up support, calm score-free completion. No speech input/TTS/grading/progress fabrication. Configuration/Options retain the conversation; Home/new process resets to category selection. Typed manifest-only catalogue of 81 approved scenes / nine categories remains the sole source. All 81 now have authored EN/DE runtime teaching/support data. The targeted Park .07 climbing and Mountains .07 uphill ambiguities are closed in Scene Description content revision 3; independent native-speaker/all-image acceptance and the documented non-blocking caveats remain separate review work. See SceneDescriptions/GERMAN_REVIEW.md, CONTENT_DATA_SPEC.md and BUILD_NOTES.md.
- Memory Pairs variants: picture/picture, word/picture, number/quantity, case, bilingual, animal/action, animal/habitat, emotion/expression and season/clue where educationally appropriate.
- Future manipulation-first Maths: construct/group quantities, making 5, number bonds, part-whole decomposition, one more/less, conservation, subtraction, number stories, patterns, shapes and spatial/measurement concepts. Extend beyond the five bounded 1–5 activities already listed in the baseline; do not reimplement them.
- Letters & Sounds / phonological-awareness progression, keeping written letter matching distinct from true phoneme instruction.
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
- Native/action-specific Verb Explorer polish.
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
