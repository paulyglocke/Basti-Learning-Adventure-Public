# Phase 5C.4A — Underground habitat design checkpoint

**Design checkpoint readiness:** READY WITH NEW APPROVED ARTWORK. At the time of 5C.4A no existing asset depicted a connected surface-to-soil cutaway; the design package specified the art needed before implementation.

## Follow-on status — Phase 5C.4

A generated 1448×1086 4:3 cutaway background and transparent Earthworm layer now implement the design recommendation; the existing Mole and Rabbit PNGs are reused as separate foreground layers. Underground is present as the third working Explore Habitats choice with bilingual selection, image-relative hit targets, accessible name buttons, offline speech/replay and no scored progress. The image generation method and paths are in the asset READMEs. Visual captures, test results and remaining owner-review gates are recorded in the [Phase 5C.4 implementation review](../animals-phase5c4-underground-review/REVIEW.md). Owner review of generated art/provenance and German wording, plus full API 35 app-suite acceptance, remain outstanding.

## Verified baseline

- Repository: `paulyglocke/Basti-Learning-Adventure-Public`, branch `main`.
- Starting local HEAD and `origin/main`: `f07c3959e93bbbc6c21017100c4090e1c82bf72d`.
- The worktree was clean at task start.
- Ocean and Woodland Explore are implemented. Woodland uses `animal.rabbit` and its activity-local `animal.fox` scene identity. There is no shared canonical animal registry.
- No Kotlin, app resource, route, test or image changes are part of this design checkpoint.

## Recommendation

Use **Option C, a hybrid composition**: commission one language-neutral 4:3 cross-section background with the surface, soil, roots, entrance, tunnels and chambers painted as a continuous scene; place separately selectable animal cutouts into designed spaces. Reuse the existing Mole PNG and, subject to phone-size/art-style acceptance, Rabbit PNG. Commission an Earthworm cutout in the same review if three animals are approved for the first scene. Keep animals separate from the background so art-relative hit regions, selection outlines and scale can be adjusted without repainting the environment.

The initial roster should be **Mole, Wild Rabbit, Earthworm**. Mole and Rabbit meet the requested priority; Earthworm adds a clear soil-dwelling/movement relationship. Facts and caveats are in [Animal Facts](ANIMAL_FACTS.md). Do not include Fox or Badger in the first scene: Fox has no standalone canonical art and needs a distinct shelter context; Badger has no standalone canonical art and a sett adds more visual complexity than this first cutaway needs. Ant is deferred because there is no canonical cutout and its underground-nesting claim must remain species-qualified.

At the checkpoint, the next step was to produce artwork from the [Artwork Brief](ARTWORK_BRIEF.md), then implement the bounded slice in [Implementation Plan](IMPLEMENTATION_PLAN.md). That work is now represented by the linked 5C.4 review; existing Ocean and Woodland screens remain intact.

## Learning and interaction

One scene should make “above” and “below” legible without reading: grass and a plant/tree above a clearly drawn ground line; roots, roomy tunnels and chambers below; a visible entrance connects the levels. The Mole is shown in a tunnel, a Wild Rabbit in a chamber connected to an entrance, and an Earthworm in soil. Tapping an animal (or its large labeled alternative) selects it, shows its bilingual name and fact, speaks through existing offline audio, highlights it with a non-colour cue, and offers Replay. Language, Sound Off, Options and Back follow the existing Explore contract. No quiz, score, assessment journal or Progress Tracker record is created.

Optional Prepositions reinforcement can use short contextual phrases such as “The mole is under the ground.” / “Der Maulwurf ist unter der Erde.” and “The rabbit is inside the burrow.” / “Das Kaninchen ist im Bau.” The existing Prepositions activity remains unchanged.

## Scope and remaining gates

This design package remains the historical 5C.4A proposal, not a finalized architecture decision. The generated assets now have app-local provenance notes, but the owner should review asset rights and final suitability. Native-speaker approval of German wording remains outstanding. Animal positions and image-relative hit bounds have been calibrated and visually checked across phone and emulator viewport overrides; see the 5C.4 review. Physical-device and child-use acceptance remain owner-managed.

The original Animal Groups four-animal Water/Land set, revision, AGR1 journal and progress identity remain compatibility constraints. Underground stays absent from the functioning chooser until its implementation phase passes those checks.

## Evidence

The canonical pack contains 28 standalone PNGs, but only Mole and Rabbit from the proposed roster are present. Mole is a 1254×1254 transparent RGBA Wave 3 image; Rabbit is a 282×327 transparent RGBA Wave 1 image. Neither is an underground scene. No standalone Fox, Badger, Earthworm or Ant art, soil cross-section, root layer, tunnel layer or underground chamber art was found. The nine Woodland scenes do show above-ground vegetation and a rabbit/warren context, but do not reveal underground interiors. Exact paths and provenance notes are in [Asset Inventory](ASSET_INVENTORY.md).

The source catalogue's relationship and fact shortlist was checked against its cited sources. A European mole spends almost all its time underground and digs tunnel networks; rabbit warrens are shelters, not proof rabbits live only underground; earthworm species use different soil layers and ways of life. The proposed short lines preserve those distinctions. Sources and wording are recorded in [Animal Facts](ANIMAL_FACTS.md).

## Documents

- [Asset Inventory](ASSET_INVENTORY.md)
- [Scene Specification](SCENE_SPEC.md)
- [Animal Facts](ANIMAL_FACTS.md)
- [Artwork Brief](ARTWORK_BRIEF.md)
- [Implementation Plan](IMPLEMENTATION_PLAN.md)
