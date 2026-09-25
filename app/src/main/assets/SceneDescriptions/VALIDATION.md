# Scene Description Asset Validation

Sweep date: 2026-09-25

## Structural result

**PASS**

- Authored categories checked: **9**
- Canonical scene files / manifest entries checked: **81**
- JSON files parse successfully.
- Manifest asset / metadata / prompt references resolve.
- Scene IDs are unique.
- Root/category production manifests and Wave 3 status agree on 81 scenes.
- Adult-support metadata is present.
- Canonical images remain 4:3 production assets.

## Historical Wave 1 visual-task result (27 scenes)

- **26 fully acceptable**
- **1 task-acceptable with a non-blocking deviation**
  - Farm 1: four chickens instead of three; quantity is not a task target
- **0 revisions outstanding**

Resolved during review:
- Ocean 1 turtle anatomy
- Park 2 sharing clarity
- Mountains 3 sheep / below-hill spatial relationship

## Current Wave 3 category checkpoint

- Ocean / Underwater: 9/9
- Jungle / Rainforest: 9/9
- Woodland / Forest: 9/9
- Park / Playground: 9/9
- Sky / Flying Adventure: 9/9
- Mountains / Alpine Nature: 9/9
- Countryside / Farm: 9/9; Scene 1 has one logged non-blocking count deviation
- Classroom / School: **9/9 acceptable**
- Zoo / Wildlife Park: **9/9 acceptable**

Wave 3 status:
**Complete across all nine categories: 81 approved / 81 unique production scenes.**
See `SANITY_CHECK.md` and `WAVE_3_STATUS.md`. The historical visual-task counts
above describe only Wave 1, not a new visual review of all 81 scenes.


## Historical Batch 2 validation
- Passed JSON, reference and image checks.

## Native data boundary

The generator validates all manifest-selected image/metadata references, IDs,
approval, wave and available authored language/support fields before bundling Kotlin.
Wave 1 has 27 bilingual records; Waves 2/3 have 54 English-only records. Missing
German is explicit. Runtime production data excludes prompts/reference/unlisted files.
Exact automated test evidence is recorded in the repository root BUILD_NOTES.md.
