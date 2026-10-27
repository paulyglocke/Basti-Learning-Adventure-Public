# Proposed Habitat Scene Designs

All scenes below are proposals. The repository’s existing Ocean/Woodland images are static scene illustrations used by Tell Me; no habitat tap interaction is implemented. Do not duplicate or alter those routes, asset IDs or Tell Me prompts.

## Ocean / Meer

**Existing scene candidates:** nine approved bilingual Ocean composites in `app/src/main/assets/SceneDescriptions/ocean_underwater/canonical/`, each 1448×1086 RGB PNG. For the first scene, `ocean_001_reef_actions.png` has a consistent reef/waterline composition and metadata for Dolphin, Turtle, Crab, Octopus and a three-fish group. It contains no whale or shark. Its EN/DE visual-target notes are scene-scoped; they do not create reusable animal facts or isolated sprites.

**First slice recommendation:** reuse this single scene as a fitted, language-neutral illustration. Author a small focus list from pictured identities only. Dolphin is a good first target (including the accurate “breathes air” fact). Include the turtle only after a visual/species review confirms that it is safe to call a sea turtle in this art. Do not treat the orange fish as a generic fish species or add the standalone white shark/whale cutouts over the composite without checking their art style and composition. Overlay authored, normalized tappable regions only after visual target review; provide a visible 56dp+ labeled target list if a hotspot is small or hard to focus.

**Child flow:** tap Dolphin → clear neutral outline/marker and selected name → short bilingual name/fact audio → Replay. Tapping blank water/background clears or leaves selection; it never selects an unrelated subject. No quiz feedback, scoring, animation dependency or environmental SFX is required.

**Feasibility:** **ready for a bounded static-scene Explore prototype**, conditional on target hotspot and asset visual approval. The composite art is ready to reuse; independent reef layer/water/background assets are not present. There are no bundled audio files; use offline system speech.

## Woodland / Wald

**Existing scene candidates:** nine approved bilingual Woodland composites in `app/src/main/assets/SceneDescriptions/woodland_forest/canonical/`, each 1448×1086 RGB PNG. Scene 008 (`woodland_008_homes_choices.png`) has a clear tree/grass/leaf setting, a rabbit beside a burrow, a fox beside a hollow log/den, an owl on a branch, a squirrel/stump and a hedgehog/leaves. Scene 005 is also a dense woodland/lakeside illustration with multiple animals. The scene catalog’s target/position data is bilingual but remains authored for Tell Me and spatial language.

**Proposed Explore:** use one scene at a time, preferably scene 008 for animal/home relations. Start with at most 3–4 focal targets (rabbit, fox, owl, hedgehog) and let the child discover them through labeled tap targets and subtle selected outlines. Keep the environment visually readable; avoid activating every animal at once. The fact relationship should distinguish “lives in woodland” from “may shelter in a burrow/den/tree cavity/leaves.” Use scene-grounded phrasing when the depicted animal’s species is broad.

**Cross-curriculum link:** offer short positional language only as optional authored facts: “The rabbit is next to the burrow. / Das Kaninchen ist neben dem Bau.” Do not turn this Explore screen into a Prepositions question round.

**Feasibility:** **high** for static-scene reuse. Scene 008 supports meaningful animal/home interactions without generating new art, but does not supply separate layers, a woodland general habitat registry, or canonical cutouts for fox/owl/hedgehog. An existing scene illustration can anchor a non-scored prototype; if taps prove too small, use a selectable animal list and/or commission a less crowded scene instead of inventing crops.

## Freshwater / Süßwasser

**Current asset state:** no Freshwater scene directory, pond/river background, or standalone canonical frog/duck/freshwater-fish illustration was found. `woodland_005_animal_homes.png` contains a lakeside/water edge, but its authored focus is woodland animals and the image cannot be silently repurposed as a complete freshwater ecosystem. Farm garden art shows crops/yard, not a pond. Ocean art must not stand in for freshwater.

**Future composition brief:** one quiet central-European pond or slow stream with visible shore and waterline, shallow edge, emergent/reed plants, submerged water plants, a few stones/logs and clear above-water/underwater zones. Include only reviewed, species-specific animals (e.g. common frog at pond edge and in water; one named native freshwater fish if a matching illustration is created). Show adjacent damp land so frog habitat is visibly not water-only. Avoid a species-specific claim from generic “fish” art. A static, layered illustration with tappable targets is sufficient; no new animation infrastructure is needed.

**Feasibility:** **requires new dedicated background and at least one confirmed animal visual** before a factual freshwater scene can be accepted. The existing freshwater-fish content references do not substitute for a pictured freshwater species.

## Underground / Unter der Erde — dedicated cutaway

**Current asset state:** `Animals/canonical/mole.png` is a 1254×1254 transparent RGBA full-body illustration. It has no soil, tunnel, root or entrance context. Woodland 008 shows a surface burrow entrance and fox by a hollow log; it does not show below-ground spaces. There is no soil cross-section or dedicated underground scene in the repository. Prepositions’ `under` pictures are positional scenes, not habitat/cutaway art.

**Proposed one-composition cutaway:** a continuous above/below ground cross-section. Upper zone: meadow/woodland surface with grass, one plant/tree, leaf litter and daylight. At the clear soil boundary, show one broad burrow entrance and roots passing across the boundary. Lower zone: distinguishable but natural-looking soil layers, a few broad tunnels/chambers and open room around each focal target. Show a mole within a tunnel, selected root strands, and optionally a rabbit warren entrance/home. Later layers may add a named earthworm and a species-reviewed ant nest. Keep animals large; avoid thin branching passages or a maze of tiny chambers.

**Interaction:** tapping the mole speaks its name and “A mole lives mostly underground and digs tunnels. / Ein Maulwurf lebt überwiegend unter der Erde und gräbt Gänge.” Tapping roots explains that they grow down into soil. Tapping a rabbit burrow explains that wild rabbits may use warrens for shelter; do not imply the rabbit lives only underground. Each selection is outlined/highlighted independently and has a keyboard/screen-reader equivalent.

**Art direction:** do not draw a rough substitute in code or stretch a tiny icon into a scene. Commission/produce original language-neutral art matching the established illustrated Home/Months/SceneDescriptions family. Prefer a hybrid of a painted scene plate and transparent foreground/midground layers if review confirms consistent depth and clean edges. Separate selectable animal/scene layers make hit mapping and selection outlines easier. No labels baked into images. See [artwork gaps](ARTWORK_GAPS.md) for proposed source sizes and transparency.

**Feasibility:** **not ready for implementation until art and facts are approved.** One mole cutout exists and makes the concept feasible, but the core visual—the cutaway itself—is absent. Phase 5C.3 should begin with the mole + roots + tunnel + surface entrance; add Rabbit only if art-style and fact review pass. Defer ant/earthworm/badger/fox until their standalone/scene artwork and exact relation copy are ready.

## Responsive scene rules common to all environments

- Keep the scene visible alongside selection name/fact/replay; reflow controls around it on short landscape and tablet rather than shrinking all hit areas.
- Use Fit or responsive layers, not a single aggressive Crop. Keep important animal/entrance/root targets visible in portrait and landscape.
- Minimum 56dp actionable areas (project shared target); if transparent hotspots cannot meet this, include large labeled animal controls as a full alternative.
- One image description should identify the environment; each selectable target gets one bilingual semantic label and selected state. Avoid duplicate image and button announcements.
- Do not use colour alone for selection, habitat identity or feedback. Provide strong neutral outline/marker and spoken/name text.
- The illustration remains useful without reading and without audio. Replay is child-controlled; no long narration on rapid taps.
