# Animals & Habitats — Phase 5C.1 Foundation Audit

**Status:** audit and planning only. No activity is implemented by this review.

## Verified baseline

- Repository: `paulyglocke/Basti-Learning-Adventure-Public`; branch `main`.
- Starting local HEAD and `origin/main`: `745e14c6b9e7c102a67ca977e0291d06904fe173`.
- Starting working tree: clean; no uncommitted changes to preserve.
- Production code/resources were not changed. Nothing was committed or pushed.
- The latest Rainbow checkpoint is [Phase 5B-R3B](../rainbow-phase5br3b-review/REVIEW.md): 49 genuine API 35 screenshots; fresh JVM 579/579; focused instrumentation 11/11; full API 35 instrumentation 586/586, zero failures/errors/skips; build, Android-test APK assembly and lint passed. These are Rainbow results, not Animals validation. Physical-device, actual TalkBack, child-use and audible offline-voice acceptance remain owner-managed.

## Findings

1. The current four-object Animal Groups activity is deliberately a fixed Water/Land exercise, not a general habitat taxonomy. Its exact identity, content revision, journal and progress must remain untouched.
2. There is no single shared `AnimalDefinition` catalogue. Vocabulary has six reusable `animal.*` records; Follow Instructions has six separate `object.follow.*` records with aliases implicit in names; Animal Groups adds local dolphin/rabbit content. Legacy Animal Actions and Tell Me use still other keys/content shapes.
3. Canonical standalone art contains 28 animal/creature PNGs. Wave 1 art is roughly 282–374 px; Waves 2 and 3 are 1254×1254 transparent PNGs. The Wave 1 art docs explicitly call for in-app edge/visual review. Composite scene art is language-neutral RGB PNG at 1448×1086 and has bilingual scene metadata.
4. Ocean and Woodland are unusually well supported: each has nine approved SceneDescriptions composites, with EN/DE descriptions, prompts and visual-target notes. These are currently Tell Me scene art, not habitat activities or separable sprites.
5. Freshwater has no dedicated directory, scene pack or animal species cutouts such as frog/duck. A woodland composite contains lakeside water, but that alone cannot establish a recognizable freshwater lesson. Underground has a canonical mole cutout and woodland art showing above-ground entrances/hollows; there is **no** layered soil cross-section, root/tunnel system, ant, earthworm or badger standalone art.
6. Existing Water/Land `SortRule` is one-category-per-item. A real habitat model must allow many-to-many, typed relationships and distinguish habitat from shelter, food and movement. Do not route general relationships through that sorter.
7. There are no bundled SFX/voice recordings under `app/src/main/assets`; use the shared offline Android speech path, with child-controlled replay and the existing Sound Off policy.

## Recommended first implementation slice

Proceed to **Phase 5C.2 — Explore Habitats Foundation**, with **one active Ocean Explore scene** and the habitat chooser/route described in [Interaction Plan](INTERACTION_PLAN.md). Reuse the approved `ocean_001_reef_actions.png` composition (1448×1086 RGB) and its exact bilingual visible-target metadata, initially limiting selectable targets to those whose names and image identities can be reviewed precisely (Dolphin; decide whether the depicted turtle is sufficiently identified as a sea turtle before including it). Do not label the generic `fish` as a marine species or add the unavailable whale/shark into that composite.

The initial experience should be unscored, child-led, offline and audio-first: select a visible animal, show a non-colour selection outline, speak one short authored bilingual fact, allow replay, and return to habitat selection. Provide large accessible labeled controls as an alternative if a scene hotspot is too small. Show only implemented habitats in the chooser until later scenes are actually available; do not show inactive empty choices as if they work.

Ocean is the smallest defensible first slice because it has a coherent full-scene illustration, bilingual target annotations and a clearly distinguishable marine environment. Woodland can follow using the existing nine-scene pack; Freshwater needs dedicated composition/species review; Underground needs new commissioned layered cutaway art and an approved fact pack. Ocean does not solve the Underground asset gap.

## Future phases and dependencies

The proposed sequence is recorded in [Implementation Plan](IMPLEMENTATION_PLAN.md). Numbers 5C.2–5C.6 are provisional and must be reconciled with the canonical roadmap before work is scheduled. The earlier [Animals Expansion review](../curriculum-phase5-audit/ANIMALS_EXPANSION.md) and [priority audit](../curriculum-phase5-audit/PRIORITIES.md) remain historical planning records; preserve their explicit recommendation that any expanded Water/Land sort receives a separate identity and that its old journal remains compatible.

Critical gates before Underground work: approve a custom cutaway artwork brief, decide whether existing Mole and Rabbit cutouts visually match the new scene, source/author bilingual facts with species scope, design large hotspots/keyboard alternatives, and visually inspect portrait/landscape crops. If any of those remain unresolved, limit the first Underground prototype to Mole + roots + soil/tunnel interactions and defer other animals rather than inventing art or facts.

## Audit documents

- [Canonical animal/art inventory](ANIMAL_INVENTORY.md)
- [Habitat relationship fact catalogue](HABITAT_FACTS.md)
- [Scene designs](SCENE_DESIGNS.md)
- [Artwork gap analysis](ARTWORK_GAPS.md)
- [Explore and Match interaction plan](INTERACTION_PLAN.md)
- [Implementation and testing plan](IMPLEMENTATION_PLAN.md)

## Exact documentation files changed

- `MASTER_PRODUCT_LEARNING_ROADMAP.md`
- `SESSION_HANDOFF.md`
- `docs/curriculum-phase5-audit/ANIMALS_EXPANSION.md`
- `docs/animals-phase5c1-audit/SUMMARY.md`
- `docs/animals-phase5c1-audit/ANIMAL_INVENTORY.md`
- `docs/animals-phase5c1-audit/HABITAT_FACTS.md`
- `docs/animals-phase5c1-audit/SCENE_DESIGNS.md`
- `docs/animals-phase5c1-audit/ARTWORK_GAPS.md`
- `docs/animals-phase5c1-audit/INTERACTION_PLAN.md`
- `docs/animals-phase5c1-audit/IMPLEMENTATION_PLAN.md`

## Recommended status

**READY FOR PHASE 5C.2**, as an Ocean-only foundation slice. This is readiness to begin a bounded implementation plan, not a claim that any feature is built, validated, child-tested or ready for production. Woodland, Freshwater and Underground remain future work; the latter two have material asset/content gaps.
