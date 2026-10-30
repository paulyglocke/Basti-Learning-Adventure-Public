# Phase 5C.5B — Freshwater Explore implementation proposal

## Gate before coding

Approve the dedicated pond background and Frog/Duck cutouts described in [ARTWORK_BRIEF.md](ARTWORK_BRIEF.md), record provenance/reuse terms, and complete visual plus native-speaker German review. If a species-identifiable freshwater fish is later desired, treat it as an additive content/art review; it is not a dependency for the initial two-animal scene.

## Bounded implementation

1. Extend `HabitatExploreContent` with a stable `habitat.freshwater` identity, bilingual title/prompt, and approved Frog/Duck canonical IDs only after the canonical animal data/asset mapping is accepted.
2. Extend the existing chooser with Freshwater only when its assets are available. Preserve Ocean, Woodland, Underground and their existing IDs, targets and navigation.
3. Reuse the current habitat screen patterns: fitted 4:3 artwork, source-relative animal targets, large labeled alternatives, exactly one selected animal, bilingual name/fact and offline spoken name-plus-fact, Replay, Sound Off, Options return, Back/Home.
4. Use a small Freshwater selection state/reducer or an existing habitat selection abstraction if already appropriate. Preserve semantic selection across language changes and Activity recreation; do not add a durable journal.
5. Keep it unscored. Emit no attempts, completions, mastery records or Progress Tracker events.

No new general habitat engine, dependency, Animal Groups change or migration is proposed. Existing `MainActivity`/navigation handling and the three habitat screen/ViewModel conventions are the integration guide; inspect current implementation again before coding.

## Future tests

- JVM: stable habitat/animal identities, bilingual content, source-backed facts, supported selection only, language-independent reducer, replay utterance and explicit absence of assessment/progress behavior.
- Android API 35: chooser routing; scene decode/fitted render; image hotspot and labeled alternative; selecting Frog then Duck updates selection/fact/audio; replay; Sound Off; Options language return; recreation; Back/Home; semantics/focus; portrait, landscape, short landscape, tablet and German 2×.
- Compatibility: existing Ocean/Dolphin, Woodland/Fox/Rabbit, Underground/Mole/Wild Rabbit/Earthworm routes and facts; original Animal Groups fixed Whale/Dolphin/Horse/Rabbit pool, revision, AGR1 journal and progress IDs.
- Final validation: focused and full JVM, focused and complete API 35 suite against final code, debug/android-test assembly, lint, `git diff --check`; genuine screenshot gallery inspected at all required sizes.

## Proposed phase boundary

5C.5B is the implementation slice after asset approval. Keep creation/review of substantial art as a distinct checkpoint so that code does not depend on interim images. Phase number placement is reflected in the roadmap; this checkpoint does not mark the route implemented.

## Phase 5C.5B implementation status (2026-10-30)

Implemented with the existing four habitat screens/navigation, a dedicated
Freshwater selection reducer and Activity-owned ViewModel, and an offline
artwork loader. The active chooser order is Ocean, Woodland, Underground,
Freshwater. Selection is language-independent; the exact content and scene
coordinates are listed in `ANIMAL_FACTS.md` and `SCENE_SPEC.md`. There is no
freshwater journal, scoring or Progress Tracker event.

The review package records validation, responsive captures and the full-suite
status after final acceptance. No Fish, generic engine, navigation migration or
Animal Groups change is part of this slice.

