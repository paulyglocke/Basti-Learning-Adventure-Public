# Phase 5C.4A — Underground asset inventory

**Snapshot note:** tables below record repository contents at the 5C.4A audit baseline `f07c3959e93bbbc6c21017100c4090e1c82bf72d`. Phase 5C.4 has since added a generated scene and Earthworm image; see the current [implementation review](../animals-phase5c4-underground-review/REVIEW.md) and adjacent app-asset README/manifest records.

## Verified reusable artwork

| Candidate | Exact path | Canonical ID status | Dimensions/type | Style and current use | Underground suitability |
|---|---|---|---|---|---|
| Mole / Maulwurf | `app/src/main/assets/Animals/canonical/mole.png` | `animal.mole` is a proposed stable ID; not in a shared registry or current habitat activity | 1254×1254 PNG, transparent RGBA; manifest-wave3.json agrees | Wave 3 friendly whole-animal cutout, no label. Visually inspected: clear mole silhouette with digging forepaws; not placed in a scene today. | **Reusable with art review.** Strong first target. It is a cutout on transparent canvas, not a tunnel cross-section. Confirm crop/padding at intended rendered size. |
| Rabbit / Kaninchen | `app/src/main/assets/Animals/canonical/rabbit.png` | `animal.rabbit` is used by Animal Groups and Habitat Explore, but there is no global canonical registry | 282×327 PNG, transparent RGBA; manifest.json agrees | Wave 1 isolated art; Woodland scene metadata separately shows Rabbit and Fox. Visually inspected: recognizable friendly rabbit. | **Reusable with adaptation/scale review.** Much smaller and older pack than Mole. Check edge quality and rendered size. It depicts a generic rabbit; the “wild” scope comes from fact/context, not a distinct asset ID. |
| Deer (style reference only) | `app/src/main/assets/Animals/canonical/deer.png` | Proposed `animal.deer`, no shared registry | 1254×1254 PNG, transparent RGBA | Wave 3 whole-animal art; no Underground role. | Not an Underground target. Optional pack-style reference only. |
| Woodland surface composite (reference only) | `app/src/main/assets/SceneDescriptions/woodland_forest/canonical/woodland_008_homes_choices.png` | Scene `scene.woodland.homes_choices.08` | 1448×1086 RGB PNG, opaque | Visually inspected in Phase 5C.3; includes woodland floor, plants, Rabbit, Fox and above-ground den/warren context. | **Not suitable as the cutaway.** It has no visible soil interior, roots or underground passages. May inform surface palette and composition only. Do not crop it to imply subterranean details. |

## Candidate art not present as standalone canonical files

`app/src/main/assets/Animals/canonical/` contains 28 PNGs. It includes `mole.png` and `rabbit.png`; it does **not** include `fox.png`, `badger.png`, `earthworm.png` or `ant.png`. Those animals appear in authored text, scene descriptions, or other composites, but that does not make a reusable transparent cutout available. In particular:

- Woodland composite fox art is embedded in the full scene and cannot be moved or cleanly reused as an independent Underground target.
- Badger and Earthworm are mentioned in the Phase 5C.1 audit as scene/legacy subjects, not as canonical standalone assets.
- No standalone ant artwork was found.
- No existing underground environment, soil cross-section, tunnel, chamber or root PNG/vector asset was found in the inspected animal and Woodland packs.

## Provenance, licensing and reuse

The Wave 1 and Wave 3 READMEs describe the assets as approved, generated/isolated pack artwork and give intended use/style characteristics. The pack manifests record filenames, pixel dimensions, alpha and mode. They do not include per-file creator, source prompt, license/SPDX identifier or third-party rights statement. This audit therefore records the files' repository provenance only; it does not invent a license grant. Before external commissioning or reuse outside the app, preserve the source/creator and usage rights in the project asset record. Do not download or introduce third-party art during the implementation phase without rights review.

## Readiness

- **Ready to reuse after visual approval:** Mole cutout.
- **Reusable with minor adaptation/scale review:** Rabbit cutout.
- **Requires new artwork:** unified surface/soil cutaway background and Earthworm cutout if the three-animal roster proceeds.
- **Not ready for initial interaction:** embedded Fox, Badger, Ant; none has an appropriate standalone target in the current asset library.
