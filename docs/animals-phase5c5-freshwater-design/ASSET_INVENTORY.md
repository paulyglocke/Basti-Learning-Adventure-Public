# Freshwater asset inventory

## Candidate animal art

| Candidate | Canonical ID/art | Verified characteristics | Current use / suitability | Decision |
|---|---|---|---|---|
| Frog / Frosch | No canonical standalone asset or ID found in `app/src/main/assets/Animals/canonical/` or the inspected canonical animal records | No dimensions, alpha or animal-art provenance to report | No approved frog interaction art | Requires new transparent cutout and reviewed semantic record |
| Duck / Ente | No canonical standalone asset or ID found in the inspected canonical animal pack | No dimensions, alpha or animal-art provenance to report | No approved duck interaction art | Requires new transparent cutout and reviewed semantic record |
| Fish / Fisch | `animal.fish`; `app/src/main/assets/Animals/canonical/fish.png` | 1254×1254 PNG; transparent RGBA per Wave 1 pack README; visually a bright orange-and-white clownfish | Vocabulary, Follow the Instructions, Animal Actions and Prepositions content; marine appearance | Existing art is real but not suitable evidence for freshwater species; omit from initial Freshwater roster |

Animal pack README says Wave 1 individual illustrations were isolated from an approved generated sheet and asks for in-app edge review. The repository does not record the image-generation tool, exact prompt, editable source, or a rights grant for this historical Wave 1 sheet. Do not infer licensing from “approved”.

## Environmental art

| Path | Dimensions / ratio | What is depicted | Existing owner/use | Freshwater suitability |
|---|---:|---|---|---|
| `app/src/main/assets/Seasons/blossoming_lakeside_spring_meadow.png` | 1448×1086 / 4:3 | Spring tree, meadow, lake, shore | `SeasonContent` Spring; four-image same-place seasonal comparison | Useful style/composition reference; no selectable animal or underwater detail |
| `app/src/main/assets/Seasons/sunny_summer_lakeside_meadow.png` | 1448×1086 / 4:3 | Summer tree, meadow, lake, shore | `SeasonContent` Summer | Useful style reference; no selectable animal or underwater detail |
| `app/src/main/assets/Seasons/autumn_tree_by_the_lakeside.png` | 1448×1086 / 4:3 | Autumn tree, meadow, lake, shore | `SeasonContent` Autumn | Useful style reference; no selectable animal or underwater detail |
| `app/src/main/assets/Seasons/snowy_lakeside_meadow_with_bare_tree.png` | 1448×1086 / 4:3 | Winter tree, snowy meadow, partly frozen lake | `SeasonContent` Winter | Useful style reference only; seasonal snow/ice should not be an invariant pond scene |

All four were visually inspected. `ART_DIRECTION.md` says the matching lakeside/tree composition is intentional for comparing seasons; `SeasonContent.kt` maps them to the existing Seasons activity. No separate provenance/license record for these images was found in `ART_DIRECTION.md`, `SeasonContent.kt`, or the searched Seasons documentation. Keep those files unchanged and do not repurpose their seasonal semantics as habitat content.

The SceneDescriptions manifest contains Ocean and Woodland environments; no Freshwater/Pond/River scene entry was found. No pond-specific plant, reed, lily-pad or underwater asset set was identified in the searched asset paths.

## Provenance requirements for future art

Record the creation method/tool, prompt summary or authored brief, source files, editing steps, export format/dimensions, and whether any external source material was used. Preserve editable masters when available. Obtain owner approval for app use and record any relevant license terms; do not state commercial reuse rights without documentary evidence.

## Phase 5C.5B implementation inventory update

The design-time inventory above remains an accurate historical record of what
existed before implementation. 5C.5B added the following independent assets:

| Path | Dimensions / mode | Use |
|---|---:|---|
| `app/src/main/assets/SceneDescriptions/freshwater_pond/canonical/freshwater_001_pond.png` | 1448×1086 RGB, opaque, 4:3 | Fitted European pond background |
| `app/src/main/assets/Animals/canonical/frog.png` | 1312×1199 RGBA with alpha | Interactive Frog cutout |
| `app/src/main/assets/Animals/canonical/duck.png` | 1312×1199 RGBA with alpha | Interactive Duck cutout |

The animal records are added to the Animals manifest; the scene pack has its
own category and manifest. Provenance and owner approval limitations are
recorded in the implementation review and `Animals/README-freshwater.md`.

