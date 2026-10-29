# Artwork brief — Underground / Unter der Erde

**Fulfilment update (Phase 5C.4):** the app-ready 4:3 background and a transparent Earthworm layer have been generated and added at the paths in the [implementation review](../animals-phase5c4-underground-review/REVIEW.md). The source brief below records the requested composition and acceptance checks. It remains the owner-review checklist; layered editable source and final owner rights/art approval have not been provided or claimed.

## Commission and readiness

**Required before implementation:** a new approved scene and at least one new animal cutout for Earthworm if the proposed three-animal roster is retained. This task does not create, generate or acquire any art. The scene should be commissioned/reviewed before the habitat becomes visible in navigation.

## Deliverable

Produce a **2048×1536 px, 4:3, language-neutral** master scene background as the primary export, with no text, labels, UI controls, selectable animals or logos embedded. Deliver a layered source file and optimized app-ready PNG/WebP background. Also deliver separately transparent PNG animal layers for any subjects not reused from approved canonical art. Keep a 2× export or equivalent resolution headroom for tablet/large-screen fit; preserve the same scene coordinate system and aspect ratio.

### Scene content

- One continuous central-European woodland/meadow edge above and soil cutaway below.
- Approx. 36% height above ground and 64% below; the ground boundary is readable but organic.
- Above ground: grass, a small plant/flower group, one tree or bush, visible trunk/plant base, a clearly visible animal-home entrance.
- Below: recognizable soil with at most two gentle layers; large connected tunnels/chambers; clearly visible tree roots extending from above; no narrow, maze-like paths.
- Provide three clear, non-overlapping staging areas: a roomy mole tunnel, a separate rabbit chamber linked to an entrance, and an uncluttered soil patch where an Earthworm can be placed.
- The background itself must remain understandable with all animal layers hidden. Do not depict the full animals baked into the base scene.
- Avoid labels, letters, numbers, scientific diagrams, tiny decorative creatures, menacing faces, sharp tools, deep black voids, or realistic distressing cutaway details.

### Animal layers

- First evaluate reuse of `Animals/canonical/mole.png` (1254×1254 transparent RGBA) and `rabbit.png` (282×327 transparent RGBA) against the final background. Do not redraw/repaint them without owner art approval. If either fails style/resolution at actual phone scale, specify and approve a replacement canonical asset rather than silently editing the file.
- Create a separate friendly Earthworm / Regenwurm cutout, language-neutral, transparent RGBA, with a clear silhouette at small screen scale and no background or baked-in shadow that conflicts with placement. A second pose is optional and out of scope for first Explore.
- Keep animals anatomically readable and consistent with the project's friendly, polished, not-babyish illustration family. Preserve clear contrast against the soil while retaining a warm natural palette.
- Separate optional selection outline from the art; the app supplies selected-state feedback and accessibility semantics.

## Layer/source and rights requirements

Supply editable layered source (for example, layered PSD, Krita, or equivalent) and exported assets. Name layers semantically: `surface`, `ground-boundary`, `soil-base`, `soil-layer-*`, `roots`, `tunnel-main`, `rabbit-chamber`, `entrance`, `animal-*. Keep animal layers separable. Record creator, creation date, source/reference material, tool/workflow, and an explicit license/ownership statement that permits offline use and redistribution inside this application. Do not paste third-party image material or prompts/assets with unverified terms into the repository.

Transparent background is required for individual animal cutouts. The environment background should be opaque RGB or optimized opaque WebP; transparent soil cutaway export is not required. No runtime network assets or dependency on the editable-source tool may be introduced.

## Acceptance criteria for artwork approval

1. The surface and underground are visibly connected as the same landscape.
2. Tree/plant roots cross the ground boundary; a visible entrance connects above and below.
3. Mole tunnel and rabbit chamber are distinct, roomy and correctly connected; earthworm staging does not imply it occupies the mole's tunnel.
4. The lower scene remains at least visually co-equal to the surface on phone portrait.
5. Mole, Rabbit and Earthworm remain recognizable at actual app rendered size; tap regions can be placed without overlapping unrelated subjects.
6. Background and each animal have documented size, format, transparency, source and reuse rights.
7. No text or UI is baked into artwork; contrast supports outline/highlight feedback without relying on colour alone.
8. The master fits 4:3 without cropping any intended interactive subject on phone/tablet layouts.
9. Art owner approves any needed adaptation or replacement of existing Mole/Rabbit art.
