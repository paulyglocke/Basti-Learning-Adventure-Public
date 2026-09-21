# Actionable backlog

This is the near-/mid-term work queue, not the full roadmap. Direction and acceptance rules live in [MASTER_PRODUCT_LEARNING_ROADMAP.md](MASTER_PRODUCT_LEARNING_ROADMAP.md), [PROGRESS_TRACKER_SPEC.md](PROGRESS_TRACKER_SPEC.md), [GAME_DESIGN_SPEC.md](GAME_DESIGN_SPEC.md), [VOICE_AUDIO_SPEC.md](VOICE_AUDIO_SPEC.md) and [ART_DIRECTION.md](ART_DIRECTION.md).

Shared implementation contracts: [NATIVE_ARCHITECTURE_SPEC.md](NATIVE_ARCHITECTURE_SPEC.md) for native foundation/migration, [CONTENT_DATA_SPEC.md](CONTENT_DATA_SPEC.md) for models/validation, [UX_NAVIGATION_SPEC.md](UX_NAVIGATION_SPEC.md) for navigation/layouts and [TESTING_QA_SPEC.md](TESTING_QA_SPEC.md) for acceptance.

## P0 — stabilise current activities

- Finish Prepositions correctness: align scenes, spoken choices and English/German feedback.
- Physically confirm the implemented speech-safe feedback/boundary on S24/Fire in English/German (VOICE_AUDIO_SPEC.md); automated regression coverage is in tests/speech.spec.js.
- Fix completion/reward layouts: wrapping stars and immediately accessible actions on narrow/short screens.
- Fix Letters display-case consistency; review bilingual initial-letter/sound content.
- Enforce audio policy and TTS readiness, cancellation and missing-offline-voice handling (VOICE_AUDIO_SPEC.md).
- Repair native Back, WebView lifecycle and durable tutorial reset without losing lesson return behavior.
- Validate physically on S24/Fire: airplane mode, EN/DE voices, portrait/landscape, insets, large text, touch/accessibility and lifecycle; exercise 5/10-question rounds and number/calendar boundaries.

## P1 — shared native foundation

- Establish shared native audio architecture and test doubles (VOICE_AUDIO_SPEC.md); evaluate enhanced voices separately through Voice Lab and physical auditions.
- Introduce shared native content/data models and validators for bilingual fields, verb lessons, scene references and valid answers.
- Establish local Progress Tracker foundation and shared skill/result API (PROGRESS_TRACKER_SPEC.md).
- Build reusable native quiz/session, settings and completion components with restoration support.
- Migrate Prepositions end-to-end; retain legacy routes until tested native parity.
- Add native state/content/Compose tests and CI coverage, including navigation and accessibility.
- Review generated German grammar, number-zero semantics and restore native custom-number entry; consider an independent parent maths ceiling.

## P2 — first learning/game extensions

- Follow the Instructions MVP: one-step taps, bilingual replay and progress events (GAME_DESIGN_SPEC.md).
- Memory Pairs: small untimed boards using shared content/audio/progress (GAME_DESIGN_SPEC.md).
- Vocabulary Booster: reviewed starter packs and a real route replacing the current Animal Actions fallback.
- Compare & Discover: illustrated comparisons using shared animal facts (MASTER_PRODUCT_LEARNING_ROADMAP.md).
- Discovery Book foundation: local entries and meaningful learning unlocks.

## P3 — broader experiences and polish

- Dragon Treasure Hunt, then Build the Bridge, Dinosaur Rescue and Crocodile Snap (GAME_DESIGN_SPEC.md); implement separately using shared systems.
- Broader custom art/animation polish, including action-specific Verb Explorer motion (ART_DIRECTION.md).
- Today’s Adventure/adaptive selection driven by skill progress (PROGRESS_TRACKER_SPEC.md).
- Measure performance and broaden accessibility/reduced-motion polish on both target devices.

Answer/audio activation isolation was completed before this pass; retain its regression coverage. Build/test history belongs in BUILD_NOTES.md, not this queue.
