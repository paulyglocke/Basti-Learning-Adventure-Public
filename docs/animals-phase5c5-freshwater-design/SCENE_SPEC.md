# Freshwater scene composition

## Scene intent

Depict one recognizable central-European freshwater pond/lakeside with both shore and water visible. It must not read as an ocean reef or as an undifferentiated blue background. Use calm, clear illustration and a small number of strong forms consistent with the app's Seasons, Ocean, Woodland and Underground art. Freshwater is a connected environment: the frog can be pictured at the wet shoreline, while a duck may swim on the pond and also stand on land in nature. Do not visually imply that either is confined to water.

## Proposed master composition

- Logical canvas: **2048×1536 px, 4:3**, no stretching; preserve original master and export a lossless PNG suitable for bundled offline use.
- Upper ~35%: sky and distant temperate meadow/woodland. No tropical palms or mountains that obscure the pond.
- Middle ~25%: irregular but readable shoreline, low grasses/reeds, a few stones/logs; waterline crosses the image around normalized `y=0.48–0.56` and remains visible.
- Lower ~40%: clear freshwater body with subdued blue/green water, a few submerged plants/stones, and visible depth cue. Do not add a fish to the first release without species-backed art.
- Plants can include region-appropriate reeds, rushes and a small number of lily pads. Avoid dense foreground foliage over animal controls.
- One dedicated background is preferable to season variants. Seasons artwork remains owned by the Seasons activity and is only a reference for style/composition.

## Animal placement and target planning

Use separately prepared transparent cutouts composited over the background to keep targets aligned to the visible animals. Proposed zones are guides for art production, not final validated hit coordinates:

| Animal | Visual placement | Provisional normalized target on full 2048×1536 composition |
|---|---|---|
| Frog | At the near shoreline, visibly touching or adjacent to water; large enough to recognize | `x=.16–.38, y=.42–.68` |
| Duck | Floating at the pond surface, away from shoreline controls and UI overlay | `x=.60–.84, y=.43–.68` |

Targets should enclose the actual animal silhouette, be disjoint where possible, and map from the fitted image rectangle into image-relative coordinates (not the whole screen). Final positions must be derived from the approved art and checked at phone portrait, phone landscape and tablet sizes. Where direct tapping remains hard, keep the existing large labeled animal alternatives as first-class controls. Selection indication should include a visible outline/shape and a selected label/state, never color alone.

These provisional coordinates do not claim a hotspot has been tested. UI placement, labels and replay belong outside the key animal silhouettes; test German 2× text scale and short landscape scrolling before acceptance.

## Phase 5C.5B rendered composition

The production scene uses a task-generated 1448×1086 (4:3) RGB background,
fitted without stretching, with canonical `animal.frog` and `animal.duck` cutouts
layered independently. Production source-relative overlay frames are:

| Animal | Normalized image frame `(left, top, right, bottom)` | Placement |
|---|---|---|
| Frog | `(0.07, 0.43, 0.33, 0.75)` | Shoreline, left; body and feet sit at the water's edge |
| Duck | `(0.60, 0.34, 0.93, 0.74)` | Floating on pond surface, right |

These are the rendered layer and hotspot frames, mapped against the fitted
image bounds rather than the screen. Captured phone portrait, phone landscape,
short landscape and tablet-size emulator layouts confirm the scene keeps its
ratio and both targets remain over the visible animal artwork. Large labeled
Frog/Duck buttons provide an alternative. See the screenshot gallery and review
for the inspected captures and the scroll behavior at short height.

