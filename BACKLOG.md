# Actionable backlog

This is the near-/mid-term work queue, not the full roadmap. Direction and acceptance rules live in [MASTER_PRODUCT_LEARNING_ROADMAP.md](MASTER_PRODUCT_LEARNING_ROADMAP.md), [PROGRESS_TRACKER_SPEC.md](PROGRESS_TRACKER_SPEC.md), [GAME_DESIGN_SPEC.md](GAME_DESIGN_SPEC.md), [VOICE_AUDIO_SPEC.md](VOICE_AUDIO_SPEC.md) and [ART_DIRECTION.md](ART_DIRECTION.md).

Shared implementation contracts: [NATIVE_ARCHITECTURE_SPEC.md](NATIVE_ARCHITECTURE_SPEC.md) for native foundation/migration, [CONTENT_DATA_SPEC.md](CONTENT_DATA_SPEC.md) for models/validation, [UX_NAVIGATION_SPEC.md](UX_NAVIGATION_SPEC.md) for navigation/layouts and [TESTING_QA_SPEC.md](TESTING_QA_SPEC.md) for acceptance.

## P0 — stabilise current activities

- [x] Align legacy Prepositions scenes, spoken choices, hints and English/German feedback across all six relations; fix narrow-screen scene spacing. Automated/browser visual evidence is in BUILD_NOTES.md; physical Samsung/Fire review remains required.
- Physically confirm the implemented speech-safe feedback/boundary on S24/Fire in English/German (VOICE_AUDIO_SPEC.md); automated regression coverage is in tests/speech.spec.js.
- [x] Fix legacy completion/reward layouts: individually wrapping stars, immediately accessible Continue/Home, separate optional balloons and completion keyboard focus. Browser size/large-text coverage is in BUILD_NOTES.md; physical S24/Fire review remains required.
- Fix Letters display-case consistency; review bilingual initial-letter/sound content.
- [x] Implement legacy audio policy, TTS readiness, cancellation, missing-offline-voice handling, versioned successful tutorial completion/reset and SFX silence (VOICE_AUDIO_SPEC.md). Automated validation recorded in BUILD_NOTES.md; physical audio acceptance remains outstanding.
- Finish native navigation/session recovery, including returning from Options to the originating activity. Audio cancellation, WebView disposal, lesson-aware Back routing and durable tutorial reset are implemented; physical lifecycle/Back validation remains outstanding.
- Validate physically on S24/Fire: airplane mode, EN/DE voices, portrait/landscape, insets, large text, touch/accessibility and lifecycle; exercise 5/10-question rounds and number/calendar boundaries.

## P1 — shared native foundation
- Introduce shared native content/data models and validators for bilingual fields, phonics packs, verb lessons, scene references and valid answers.
- Establish shared native audio architecture and test doubles (VOICE_AUDIO_SPEC.md); evaluate enhanced voices separately through Voice Lab and physical auditions.
- Build reusable native quiz/session, settings and completion components with restoration support.
- Establish minimal local Progress Tracker event storage immediately after the content/session foundation: stable skill/result/support/context/session IDs, persistence and deduplication (PROGRESS_TRACKER_SPEC.md).
- Migrate Prepositions end-to-end to prove the architecture; retain legacy routes until tested native parity.
- Add native state/content/Compose tests and CI coverage, including navigation and accessibility.
- Review generated German grammar, language-specific phonics, number-zero semantics and restore native custom-number entry; consider an independent parent maths ceiling.

## P2 — first learning/game extensions
- Follow the Instructions MVP: first true native game/learning engine for one-step taps, bilingual replay, classroom language and progress events (GAME_DESIGN_SPEC.md).
- Vocabulary Booster v1: roughly 50–80 reviewed starter words using canonical shared IDs and a real route replacing the current Animal Actions fallback.
- Tell Me! / Erzähl mal v1: picture prompt → child speaks → Continue → model/expand language; no automatic speech scoring.
- Memory Pairs: one shared untimed matching engine for picture/picture, number/quantity, case, bilingual, animal/action, animal/habitat and emotion/expression pairs (GAME_DESIGN_SPEC.md).
- Progress dashboard v1: Going well, Suggested focus and useful support/independence trends rather than vanity totals.
- Native Numbers/Maths expansion: prioritise subitising, quantity, more/fewer/same, making 5, patterns, shapes and spatial/measurement concepts over large-number drill.
- Classroom / School Skills: practical German/English instructions and self-advocacy embedded in contextual scenes.
- Compare & Discover plus Discovery Book foundation, with meaningful knowledge unlocks as a primary reward.

## P3 — broader experiences and polish
- Make Today’s Adventure the central recommended child entry (confidence + focus + communication/listening + game/reward), with a secondary browse path and no streak pressure (UX_NAVIGATION_SPEC.md).
- Dragon Treasure Hunt, then Build the Bridge, Dinosaur Rescue and Crocodile Snap (GAME_DESIGN_SPEC.md); implement separately using shared systems.
- Broader custom art/animation polish, including action-specific Verb Explorer motion (ART_DIRECTION.md).
- Add more real-world/movement missions and occasional unscored “How did you know?” reasoning prompts.
- Measure performance and broaden accessibility/reduced-motion polish on both target devices.
