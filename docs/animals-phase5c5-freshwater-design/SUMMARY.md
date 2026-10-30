# Phase 5C.5A — Freshwater design and artwork readiness

**Status: READY WITH NEW ARTWORK.** This is a design checkpoint only. Freshwater is not in the active habitat chooser, and no production route or animal record was added.

## Finding

The repository contains four matching 4:3 seasonal lakeside landscapes and a generic canonical Fish asset. The landscapes communicate a temperate lake/shore and are useful style references, but they belong to the Seasons curriculum, contain no selectable frog, duck or fish, and do not show underwater habitat. The Fish illustration is recognisably a clownfish, so it must not be used to assert a freshwater species. No dedicated Freshwater scene or canonical Frog/Duck cutout was found.

The smallest defensible first roster is **Frog / Frosch** and **Duck / Ente**, represented with new approved transparent animal artwork and a dedicated pond background. Exclude Fish until the project has a named freshwater species and suitable species-identifiable art. A two-animal Explore still supports a rich comparison between water and shoreline without turning habitat into an exclusive category.

## Recommended next step

Create and review a dedicated 4:3 European pond scene plus separate Frog and Duck cutouts. Keep asset provenance and owner reuse approval with the imported files. Once those are approved, implement Phase 5C.5B using the existing habitat chooser, normalized image-space targets, labeled selection alternatives, unscored selection state, offline speech and Replay. Do not reuse or modify the Seasons resources as production habitat art.

## Evidence and limits

- Existing canonical identities: `animal.fish` only among the three preferred candidates. No canonical `animal.frog` or `animal.duck` record/art mapping exists in the inspected animal definitions and asset pack.
- Existing Lakeside images: 1448×1086 PNG, 4:3, seasonal variants of the same scene. They show a lake, tree, meadow and shore but no animals, aquatic plants or underwater view.
- Existing Fish: `Animals/canonical/fish.png`, 1254×1254 RGBA. It visibly depicts a clownfish, making it unsuitable for the proposed freshwater relationship.
- Facts: see [ANIMAL_FACTS.md](ANIMAL_FACTS.md); sources support cautious group-level statements but do not validate any future illustration's species.
- The existing app has three unscored habitats and a chooser/screen/selection pattern in `HabitatExploreContent.kt` and `HabitatExploreScreens.kt`. Extension should stay local to this architecture; no generic engine or progress/journal work is needed.

This review does not establish asset licensing beyond the repository records. The seasonal art's original creation/license record was not found in the inspected files. New art must have its creation workflow documented, must not incorporate unreviewed external images, and must receive owner approval before release.

## Phase 5C.5B implementation update (2026-10-30)

Freshwater Explore is now implemented as the fourth active habitat using a new
1448×1086 pond scene and separate transparent Frog and Duck artwork. This
supersedes the earlier “not implemented” status above while preserving the
design audit. The approved two-animal roster and exact bilingual facts in
[ANIMAL_FACTS.md](ANIMAL_FACTS.md) are in production; no Fish was added.
Artwork dimensions, creation provenance, target positions and visual findings
are recorded in
[`../animals-phase5c5-freshwater-review/REVIEW.md`](../animals-phase5c5-freshwater-review/REVIEW.md).

Automated and visual final acceptance is tracked in the implementation review.
Generated-art owner approval and physical/child accessibility acceptance
remain separate release checks. This update does not claim owner approval or
commercial reuse rights.

