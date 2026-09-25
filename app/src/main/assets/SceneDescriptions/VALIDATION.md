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
All 81 records now have bilingual runtime teaching/support text. The 54 Wave 2/3
records use the existing EN/DE text/list forms; missing or blank German is rejected.
The optional domain text model still does not fall back to English. Runtime production data excludes prompts/reference/unlisted files.
Exact automated test evidence is recorded in the repository root BUILD_NOTES.md.

## German content checkpoint

54 later-wave scenes translated: 1,698 German values plus one existing Farm Wave 1
review caution (1,699 added total). Structural bilingual completeness is distinct
from visual-semantic acceptance. `GERMAN_REVIEW.md` records the inspected Park 005
bucket correction, targeted ten-image review, remaining ambiguities and scope limits.
Historical visual acceptance counts above are not expanded by translation tests.

## Final targeted review — revision 3

Park .07 and Mountains .07 were checked against their committed PNGs again. Seven
EN/DE teaching values now describe a child standing on the rope bridge and hikers
on a path, without requiring climbing or asserting uphill/downhill movement.
`GERMAN_REVIEW.md` lists every old/new phrase. No image changes were needed.
Schema remains 1; authored-content revision is 3. Structural and bilingual guarantees
remain 81 scenes / nine categories. The recurring German support-language pass found
no additional clear correction. This closes these two wording concerns for an
open-ended consumer, not independent language acceptance or a fresh visual audit of
all 81 images. Existing Farm count and narrative/cropped-panel caveats remain.
