# Phase 5C.4A — Underground scene specification

## Composition

Create one coherent landscape rather than a surface picture placed above an unrelated soil diagram. Recommended master canvas: **2048×1536 px, 4:3 landscape**, language-neutral, with no baked-in labels or buttons. Compose as one base image plus transparent animal layers; preserve the same coordinates across phone and tablet layouts.

### Proportions and landmarks

- Ground/grass boundary: around **36% from the top** (`y ≈ 0.36`). Allow a gentle natural edge rather than a ruler-straight split.
- Above-ground zone: approximately **36%** of canvas height. Include grass, a small selection of flowers/plants, a tree or bush, visible roots crossing the boundary, and a readable entrance to a burrow. Keep the surface calm so the lower cutaway has enough visual weight.
- Below-ground zone: approximately **64%** of canvas height. Show warm, clearly distinct soil with one or two broad layers, a connected tunnel network, a roomy rabbit chamber and an open mole passage. Roots should visibly continue from the plant into the soil.
- Keep the ground line and entrance visible at small screen sizes. Avoid tiny branches, narrow corridors, dense stipple, tiny stones, or decorative underground clutter that competes with animal silhouettes.

### Animal staging

Use three non-overlapping, individually placed transparent cutouts:

1. **Mole:** in a broad lower-left/middle tunnel, facing into the scene. Do not imply the mole has to use the rabbit's warren.
2. **Wild rabbit:** in a distinct chamber on the lower right, with a continuous passage to a visible entrance above. The scene may show the rabbit just inside the chamber to make “shelter” clear; do not present it as permanently confined underground.
3. **Earthworm:** in a broad, uncluttered soil strip near visible roots, distinct from the mole tunnel and rabbit chamber. Depict it as a soil animal, not as a vertebrate tunnel-digger.

These are layout assignments, not final coordinates. Final transparent cutout boxes and touch bounds must be measured against the approved production export. Do not set interaction targets until composited art is visually approved.

## Responsive layout and hit targets

Display the complete 4:3 illustration using fit scaling; never stretch it. The native page may scroll vertically on short or enlarged-text screens. Keep the instruction and scene heading visible before the illustration; keep the labeled animal alternatives and Replay reachable without requiring a child to find a hidden secondary panel. On wide screens, use additional space around the fitted art rather than enlarging animals inconsistently.

After the exact artwork is placed, record each animal's normalized **image-relative** rectangle `(left, top, right, bottom)` in the scene content definition. Convert those fractions using the actual fitted-image content rectangle, including letterbox offsets; do not scale them against the whole Compose container when the image does not fill it. Test inside/outside points against both the visible silhouette and the semantic control bounds. No hit rectangle may activate an unrelated animal or artwork. If an animal is too small for direct tapping, its large localized labeled button remains an equivalent selection path.

Selected state needs a visible outline or halo, the localized animal name and fact, and a selected semantic state. Do not communicate selection only with soil colour or a glow that disappears in grayscale. Image hotspots should be at least the project's practical touch size; if a natural image-relative box cannot meet it, enlarge the interaction bounds only into visually safe space and preserve disjoint targets. Accessible alternatives remain the reliable path for small Earthworm art.

## Layout approval checklist for the later implementation phase

- Phone portrait: surface and subterranean layers both recognizable; all three labeled alternatives reachable.
- Phone landscape and short landscape: first viewport communicates that this is an above/below scene; scroll for content only where needed, without hiding all instructions.
- German at 1.5× and 2×: readable names/facts; no clipped controls; natural scroll accepted.
- Tablet portrait and landscape: preserve the 4:3 artwork, avoid stretching or oversized empty UI, and retain comfortable animal buttons.
- Validate tap geometry after `ContentScale.Fit` in each viewport and compare semantic hotspot bounds against the same normalized source rectangles.
