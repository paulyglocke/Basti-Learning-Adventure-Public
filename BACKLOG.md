# Actionable backlog

This is the execution queue, not the full curriculum. Product direction lives in [MASTER_PRODUCT_LEARNING_ROADMAP.md](MASTER_PRODUCT_LEARNING_ROADMAP.md). Detailed contracts live in the specialist specs linked there.

Completed implementation history and validation evidence belong in [BUILD_NOTES.md](BUILD_NOTES.md) and [SESSION_HANDOFF.md](SESSION_HANDOFF.md), not as a growing list of checked-off backlog items.

## Established baseline

Already implemented and not active backlog unless a regression is found:
- shared native content IDs/repository foundation
- shared native audio controller/system-TTS boundary
- deterministic native session/checkpoint framework
- durable deduplicated native progress-event storage
- native Prepositions
- native Seasons Explore/recognition, Next/Before and Build the Year (shared placement rules with Wilma; physical functional acceptance pending)
- native Wilma Explore/Find Day/Before-After/Ordering, with ordering viewport auto-follow (physical motion acceptance pending)
- native Vocabulary starter slice
- shared native completion celebration across the four native activities (automated evidence in BUILD_NOTES.md; physical acceptance remains below)
- stable external distribution signing and monotonic CI versioning
- Samsung landscape safe-inset fix
- legacy correctness/audio/session fixes already marked complete in historical notes

## P0 — trusted daily build

- Physically accept the implemented shared native completion celebration: five balloons, one-time pop, canonical animal reveal, brief local confetti, Sound Off-aware SFX, immediate Continue/Home, no progress/session coupling. Cleaned celebration artwork is physically verified on S24 (`a8877b5`); do not repeat transparency cleanup. Remaining motion/audio/accessibility checks and Fire Max acceptance are separate.
- S24 same-key in-place upgrade is proven: 1003901 / 1.1.39.1 → 1004501 / 1.1.45.1 (owner-reported physical evidence in BUILD_NOTES.md). Keep signing/versioning as maintenance; perform the equivalent Fire Max upgrade check.
- Run one batched S24 Ultra acceptance sweep across Prepositions, Seasons, Wilma and Vocabulary:
  - airplane-mode EN/DE and missing-voice behavior
  - Sound Off / Replay
  - portrait + both landscapes
  - large text / insets / touch / basic accessibility
  - Options/Back/background/return
  - true process recreation
  - 5/10 completion
  - progress after restart and signed upgrade
- Run the equivalent Fire Max sweep.
- Fix critical defects found by those sweeps; avoid unrelated feature work inside P0.
- Preserve the proven permanent signing identity and monotonic versioning during all future builds.

## P1 — shared native experience

- Introduce a lightweight shared visual design system before many more native screens are built:
  - primary action
  - secondary/replay/help action
  - navigation action
  - image/choice card
  - activity header
  - progress marker
  - completion layout
  - shared spacing/typography/press/motion rules
- Create a canonical original animal-art library, starting with assets that immediately replace current placeholders and can be reused across activities.
- Replace temporary animal visuals in Vocabulary and review temporary native animal representations elsewhere.
- Reuse the small Wilma/Seasons `OrderedPlacement` helper for future concrete sequencing consumers; extend it only for a demonstrated need.
- Build **Follow the Instructions MVP** as the reusable instruction/action engine:
  - one-step tap instructions first
  - bilingual Replay
  - shared audio/progress/session contracts
  - responsive phone/tablet layouts
- Extend that engine later rather than creating separate frameworks for School Skills, Remember the Mission, inhibition/rule switching and compatible Move & Learn prompts.
- Build Memory Pairs on a reusable semantic matching model when that work starts.
- Extend content/data schemas only for concrete activity needs; avoid speculative framework work.
- Review German grammar, language-specific phonics, number-zero semantics and native custom-number entry when their relevant activities are touched.

## P2 — core school-readiness curriculum

- **Seasons expansion**:
  - Missing season
  - identify season from observable clue
  - match season to clue
  - later combined before/after reasoning
- School Skills as an early Follow-the-Instructions content pack: classroom instructions plus help-seeking/self-advocacy.
- Grow Vocabulary toward roughly 50–80 reviewed words through shared curriculum additions rather than one monolithic vocabulary task.
- Tell Me! / Erzähl mal v1 with parent/child Continue and model/expand language; no automatic pronunciation scoring.
- Memory Pairs variants: picture/picture, word/picture, number/quantity, case, bilingual, animal/action, animal/habitat, emotion/expression and season/clue where educationally appropriate.
- Conceptual native Maths: subitising, quantity, more/fewer/same, making 5, part-whole, one more/less, conservation, number stories, patterns, shapes and spatial/measurement concepts.
- Letters & Sounds / phonological-awareness progression, keeping written letter matching distinct from true phoneme instruction.
- Wilma Today/Yesterday/Tomorrow using an explicit established anchor; no silent device-date inference.
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
