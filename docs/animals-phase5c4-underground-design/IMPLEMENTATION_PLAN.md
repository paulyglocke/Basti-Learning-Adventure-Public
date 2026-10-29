# Proposed next phase — Underground Explore implementation

**Status update:** the bounded implementation described below has now been completed as Phase 5C.4 with generated art. The gate items that remain (owner art/rights review and native-speaker approval) are listed in the current [implementation review](../animals-phase5c4-underground-review/REVIEW.md). The details below are the original proposal and acceptance intent; they should not be read as claiming that editable source art, physical-device acceptance or a complete app-wide API 35 run exist.

This is a bounded proposal for a future implementation phase after Phase 5C.4A artwork approval. It is not permission to begin or an architecture decision. Preserve the current `HabitatExploreContent` and Explore route contracts; do not build a generic habitat engine.

## Gate 0 — Artwork and content approval

1. Approve creator/rights provenance for the scene and new Earthworm layer.
2. Verify Mole and Rabbit rendering against the scene at actual phone scale; use existing canonical assets only if their style/edge quality is acceptable. Keep existing files unchanged.
3. Review final English/German labels and facts with the content owner/native speaker.
4. Freeze scene source dimensions, image-relative animal boxes, and selected-state visual treatment.

No navigation or production code changes before this gate is satisfied.

## Minimal implementation slice

- Add `habitat.underground` and selected animal content definitions to the existing habitat content module, with stable semantic IDs and bilingual names/facts. `animal.rabbit` already appears in the two current activities but is not a globally centralized animal registry. Add `animal.mole` and `animal.earthworm` as IDs in this habitat's small reviewed content only; do not invent a shared animal framework.
- Import the opaque scene background and transparent animal layers under the existing offline assets convention. Record dimensions, checksums/source provenance and licenses in the project asset record.
- Add one focused renderer/screen following Ocean/Woodland Compose patterns: fitted 4:3 background, placed cutouts, normalized source-image targets, selected outline, label/fact, Replay, and large labeled animal alternatives. If accessibility or small-target constraints make image tap unreliable, the labeled control is an equal primary route.
- Add an Underground selection ViewModel modeled on the existing lightweight Habitat Explore owner. Keep the selected semantic animal across normal Activity recreation. Do not add durable journals, scoring, assessment events or Progress Tracker records. OS process-death persistence is not required by this design and should not be claimed unless separately implemented and tested.
- Add Underground as an active chooser item only when the scene works end to end. Keep Ocean and Woodland routes, return destinations, Options, language changes, Sound Off, Home and Back intact.
- Reuse the existing offline `DefaultAudioController`: speak selected bilingual animal name plus one short fact only after explicit selection; Replay repeats current content; entry remains silent; Sound Off remains silent and does not reset selection.

## Responsive and accessibility review

Fit the complete background without distortion and account for letterboxing when translating normalized hotspot coordinates. Allow page scrolling on short landscape and 2× German rather than shrinking animal targets. Validate phone portrait/landscape, short landscape, tablet portrait/landscape and German 1.5×/2×. Keep Alternatives, Replay, Back, Home and Options reachable. Expose names, selected state and Replay through Compose semantics; provide a visible non-colour outline and keyboard focus where practical. Actual TalkBack, switch access, physical-device and child-use acceptance stay owner-managed.

## Focused verification

JVM tests should cover stable identities, bilingual copy, relationship types, the no-assessed-progress contract, selection/replacement, language-independent selection, replay mapping and asset mapping.

API 35 tests should cover chooser navigation, scene decode, each animal hotspot and labeled alternative, hotspot alignment under `ContentScale.Fit`, selection switching, EN/DE, Replay, Sound Off, Options return, Activity recreation, Back/Home, responsive viewports and accessibility semantics. Include Ocean and Woodland route regressions plus Animal Groups identity/journal compatibility; do not change its original four-animal exercise.

Capture genuine emulator screenshots and visually inspect all animal targets. Run focused and full JVM suites, relevant/final API 35 instrumentation, debug and Android-test assembly, lint and `git diff --check`. Run the complete API suite when production code changes affect shared navigation/state/layout contracts; do not claim results from before a final production change.

## Explicit non-goals

No Freshwater scene, habitat matching, scoring, Animal Groups expansion, journal migration, shared generalized animal engine, activity changes to Prepositions, new audio engine, third-party artwork, remote assets or modifications to Ocean/Woodland behavior.
