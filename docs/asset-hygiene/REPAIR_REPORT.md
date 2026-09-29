# Production asset hygiene

Current status: the 2026-09-28 second pass below closes all 21 deferred Prepositions
issues. Validator now exits 0; mouse is the sole remaining ambiguous image. The first
pass and its original BEFORE/contact evidence are retained as historical records.

# First pass — 2026-09-27

Baseline: `856303b` (`feat: generalize Follow instructions scenes`). Owner subsequently authorized commit/push.

## Inventory and decisions

178 packaged raster files in the five requested roots: 175 scoped images scanned plus
3 authoring-only Animals/reference PNGs excluded from runtime analysis and verified
byte-identical. No corrupt files or dimension errors. CLEAN means no detected edge
signature, not a claim that every scene is semantically perfect.

| Classification | Before visual review | After repairs |
|---|---:|---:|
| CLEAN | 133 | 153 |
| COSMETIC_EDGE_GUTTER | 27 | 15 |
| CONFIRMED_SEPARATOR_SLIVER | 14 | 6 |
| AMBIGUOUS_REVIEW_REQUIRED | 1 | 1 |

The raw detector initially flagged 42 candidates (14 strong, 28 ambiguous); visual
review classified 27 ambiguous candidates as extraction gutters. All 42 candidate
images were inspected on labeled contact sheets; dog/horse alpha, preposition spacing,
classroom bottom fragments and sky panels received targeted review. `before/` keeps
raw detector labels; CSVs include reviewed classifications. `after/` shows the same
42 images for comparison, including unchanged candidates.

20 files repaired. All retained pixels compare exactly with their original crop.
155 other scoped files and all 3 excluded references are byte-identical to baseline.
No resampling, stretching, recolouring, regeneration, inpainting or background cleanup.
All PNG modes and existing alpha behavior preserved. No Kotlin production changes,
manifest/semantic mapping changes, authored text changes or session/audio changes.

SceneDescriptions have no fixed pixel-dimension runtime/test contract; their loader
uses sampled decoding and Fit. These 18 repairs retain clean cropped dimensions rather
than synthesizing scenic padding. Dog/horse preserve dimensions with transparent top
padding on their existing RGBA canvases. Their older residual backgrounds are not
restyled. Intended panel people/animals/actions/counts and internal weather panels stay
intact; only extraction material is removed. This is targeted edge review, not renewed
acceptance of all earlier authored visual/content claims.

## Repairs

Crop order is top / bottom / left / right, in original pixels. Each retained panel was
visually compared before/after; no meaningful intended subject was removed. RGB scene
edges occasionally already truncate subjects in the original; repairs do not extend
those original truncations. Full machine-readable records: `REPAIRS.json`.

### `app/src/main/assets/Animals/canonical/dog.png`

- CONFIRMED_SEPARATOR_SLIVER; crop 16/0/0/0.
- [305, 332] → [305, 332]; RGBA → RGBA.
- Old SHA-256: `233e88caf8d3218dcdfe703c3da63c825ae829f7b9021af2b19f8d10ffa20e38`
- New SHA-256: `4520ef83b9fe7408afa92bf8bc9a63e2f693999afe9b7e25df195825069be065`
- Top partially transparent separator remnant removed; retained body/ears/feet/tail pixels unchanged; transparent padding on existing RGBA canvas.

### `app/src/main/assets/Animals/canonical/horse.png`

- CONFIRMED_SEPARATOR_SLIVER; crop 16/0/0/0.
- [345, 332] → [345, 332]; RGBA → RGBA.
- Old SHA-256: `b5ee0af5a930df9f13e69a8910fad1859389128f83f8103552f34437104cf835`
- New SHA-256: `896bbbf870961337b58dc7524fdb4334cf8cfd2f173f30eb9cb780b4460b9049`
- Top partially transparent separator remnant removed; retained body/ears/feet/tail pixels unchanged; transparent padding on existing RGBA canvas.

### `app/src/main/assets/SceneDescriptions/classroom_school/canonical/classroom_004_objects_places.png`

- CONFIRMED_SEPARATOR_SLIVER; crop 10/86/0/18.
- [1448, 1086] → [1430, 990]; RGB → RGB.
- Old SHA-256: `a874cc28f619e105ddc8b5051efbdc8fec3cbc480af4dde68ef2d2c5e951243a`
- New SHA-256: `9ba210b9b9da5dc95ded810f1324a4b859bdda10b6691d3fedb0b322a0799b62`
- Bottom adjacent-panel fragment and divider removed; Only outer extraction gutters cropped; intended scene objects/actions retained. No padding, resampling or scenic fill.

### `app/src/main/assets/SceneDescriptions/classroom_school/canonical/classroom_005_quiet_corner.png`

- CONFIRMED_SEPARATOR_SLIVER; crop 11/86/6/6.
- [1448, 1086] → [1436, 989]; RGB → RGB.
- Old SHA-256: `6dff0869d939ab2135609b4311afd01d2f233280449e3943bbca9853222ef9dd`
- New SHA-256: `c14206c804166bcb8f945f69366f4402fcab604f07f6daf5110cfc2d1294f246`
- Bottom adjacent-panel fragment and divider removed; Only outer extraction gutters cropped; intended scene objects/actions retained. No padding, resampling or scenic fill.

### `app/src/main/assets/SceneDescriptions/classroom_school/canonical/classroom_006_art_sorting.png`

- CONFIRMED_SEPARATOR_SLIVER; crop 11/86/18/0.
- [1448, 1086] → [1430, 989]; RGB → RGB.
- Old SHA-256: `acdfb2167ab26b40961651f413f960f272c844d3f6e61831c66abf9346f1e772`
- New SHA-256: `211ed0a27c9571984a51a4edd51ae021c90c669947c55946c724d53417636133`
- Bottom adjacent-panel fragment and divider removed; Only outer extraction gutters cropped; intended scene objects/actions retained. No padding, resampling or scenic fill.

### `app/src/main/assets/SceneDescriptions/countryside_farm/canonical/farm_004_animal_actions.png`

- COSMETIC_EDGE_GUTTER; crop 0/13/0/17.
- [1448, 1086] → [1431, 1073]; RGB → RGB.
- Old SHA-256: `8295612a858afee52971502ac06d0cf18c157b9749bbb92233ab310df1692218`
- New SHA-256: `d891cf333e94ed6ee832697e5442327477fef1b80384114c78cf2a3d445f9a00`
- Only outer extraction gutters cropped; intended scene objects/actions retained. No padding, resampling or scenic fill.

### `app/src/main/assets/SceneDescriptions/countryside_farm/canonical/farm_005_transport_and_places.png`

- COSMETIC_EDGE_GUTTER; crop 0/13/6/6.
- [1448, 1086] → [1436, 1073]; RGB → RGB.
- Old SHA-256: `448552b43463443b6fb72d4acb0c94c77c2aa8ede26fba332c17d153c6e04756`
- New SHA-256: `b04cffc6a366bb8f03e1ec14dd2fbcf11dda75af3ffd6ef879222f3a2f401cde`
- Only outer extraction gutters cropped; intended scene objects/actions retained. No padding, resampling or scenic fill.

### `app/src/main/assets/SceneDescriptions/countryside_farm/canonical/farm_006_garden_food.png`

- COSMETIC_EDGE_GUTTER; crop 0/13/18/0.
- [1448, 1086] → [1430, 1073]; RGB → RGB.
- Old SHA-256: `26bb650985f3e969c0e77c7b8cc9b48f159744a645ecdc38757353a5e10fb31d`
- New SHA-256: `60c1e78f649716ea35e101789a19501b13fb4105a5451070aa2622ca59163357`
- Only outer extraction gutters cropped; intended scene objects/actions retained. No padding, resampling or scenic fill.

### `app/src/main/assets/SceneDescriptions/mountains_alpine/canonical/mountains_007_journey_story.png`

- COSMETIC_EDGE_GUTTER; crop 0/0/0/12.
- [1448, 1086] → [1436, 1086]; RGB → RGB.
- Old SHA-256: `33d58f137877378425000bce3b9018a065c1034b797a87b5196a2344a6f23eaa`
- New SHA-256: `dd758b07dcd56cbcaaf5b03bf94ec8a1628fa46010805e2325978dd78540fefe`
- Only outer extraction gutters cropped; intended scene objects/actions retained. No padding, resampling or scenic fill.

### `app/src/main/assets/SceneDescriptions/mountains_alpine/canonical/mountains_008_weather_shelter.png`

- COSMETIC_EDGE_GUTTER; crop 0/0/7/6.
- [1448, 1086] → [1435, 1086]; RGB → RGB.
- Old SHA-256: `cf7a5473eebfe7ff2891b125ddb8172bdd818bee0b0fcf1ca650e56471616d82`
- New SHA-256: `1c226f9d77c969ee476b660740ef3a95aed7750df4fa96ce11b636454993e025`
- Only outer extraction gutters cropped; intended scene objects/actions retained. No padding, resampling or scenic fill.

### `app/src/main/assets/SceneDescriptions/mountains_alpine/canonical/mountains_009_spatial_clues.png`

- COSMETIC_EDGE_GUTTER; crop 0/0/15/0.
- [1448, 1086] → [1433, 1086]; RGB → RGB.
- Old SHA-256: `c19907de70275410b4857f3860d382cbd0b769cf6ef282565ad14eac2c17f517`
- New SHA-256: `b49a47a7e916a4f19f22dffc89a7c271c24527ff0ff9f8fa78857e395a12c17c`
- Only outer extraction gutters cropped; intended scene objects/actions retained. No padding, resampling or scenic fill.

### `app/src/main/assets/SceneDescriptions/park_playground/canonical/park_007_playground_actions.png`

- COSMETIC_EDGE_GUTTER; crop 0/7/0/12.
- [1448, 1086] → [1436, 1079]; RGB → RGB.
- Old SHA-256: `1b93ed6b599a0ad326163879b1959168da0217e659f5dfb5a5e9aeea758e2811`
- New SHA-256: `16047ebf344ad64e603311195966dde02e94482d7bb8edf2754bef0c8a11c493`
- Only outer extraction gutters cropped; intended scene objects/actions retained. No padding, resampling or scenic fill.

### `app/src/main/assets/SceneDescriptions/park_playground/canonical/park_008_helping_clean_up.png`

- COSMETIC_EDGE_GUTTER; crop 0/7/7/6.
- [1448, 1086] → [1435, 1079]; RGB → RGB.
- Old SHA-256: `ff9a9dbab3602c65da6e691ae9fe474bb62b6c6e393b07bc68c9c29c66b4e67e`
- New SHA-256: `bc6994bbd47ca9ca5afdb0d3a347e128e518371835e160711865f036537d5c10`
- Only outer extraction gutters cropped; intended scene objects/actions retained. No padding, resampling or scenic fill.

### `app/src/main/assets/SceneDescriptions/park_playground/canonical/park_009_turn_taking.png`

- COSMETIC_EDGE_GUTTER; crop 0/8/15/0.
- [1448, 1086] → [1433, 1078]; RGB → RGB.
- Old SHA-256: `7991d436f2e95d137b014f641aedbadd885fedbdfc20fda66ff3d51ed5ffeeb8`
- New SHA-256: `451d66e0c580e3080d27edda85db24b25ccba887b7b53e56f9ca350b506914e5`
- Only outer extraction gutters cropped; intended scene objects/actions retained. No padding, resampling or scenic fill.

### `app/src/main/assets/SceneDescriptions/sky_flying/canonical/sky_007_weather_sequence.png`

- CONFIRMED_SEPARATOR_SLIVER; crop 8/40/0/12.
- [1448, 1086] → [1436, 1038]; RGB → RGB.
- Old SHA-256: `dbb7e81d281dbe373090b3fad5898b5bf5baf4a7173f915a0d57458680bbe0e6`
- New SHA-256: `663c635d0437442d5fbbcd04f410e68c5888f6d0bcdd3099d9ca773c8b0a1166`
- Bottom adjacent-panel fragment and divider removed; Only outer extraction gutters cropped; intended scene objects/actions retained. No padding, resampling or scenic fill.

### `app/src/main/assets/SceneDescriptions/sky_flying/canonical/sky_008_flying_directions.png`

- CONFIRMED_SEPARATOR_SLIVER; crop 8/40/8/6.
- [1448, 1086] → [1434, 1038]; RGB → RGB.
- Old SHA-256: `8cc8843d6a92804c2ee5148a79cec3938f628e1f7574931b6b845c4a9aa3c6c6`
- New SHA-256: `137a8954bfc500a3fb4a457433b35c936df0664e8ed80907b0c168bd1f0fa183`
- Bottom adjacent-panel fragment and divider removed; Only outer extraction gutters cropped; intended scene objects/actions retained. No padding, resampling or scenic fill.

### `app/src/main/assets/SceneDescriptions/sky_flying/canonical/sky_009_night_sky_clues.png`

- CONFIRMED_SEPARATOR_SLIVER; crop 9/39/15/0.
- [1448, 1086] → [1433, 1038]; RGB → RGB.
- Old SHA-256: `848851956bb9074bfdf5d0b70aea1de4292c6e5b3f97f2fc1e3e4e09522f5366`
- New SHA-256: `8baf3b3a0ebec2fc62be66ce364f917308d6158bf5281c0900f3634255ef251a`
- Bottom adjacent-panel fragment and divider removed; Only outer extraction gutters cropped; intended scene objects/actions retained. No padding, resampling or scenic fill.

### `app/src/main/assets/SceneDescriptions/zoo_wildlife/canonical/zoo_004_habitats.png`

- COSMETIC_EDGE_GUTTER; crop 0/0/0/17.
- [1448, 1086] → [1431, 1086]; RGB → RGB.
- Old SHA-256: `07f385a938581351721b8475eec5b76e8c583b9e47cb5606afd74ccd8625d7c1`
- New SHA-256: `8d140a2f260da199e555a2a502b569c8386b7bdc563d80697b75669c8985245b`
- Only outer extraction gutters cropped; intended scene objects/actions retained. No padding, resampling or scenic fill.

### `app/src/main/assets/SceneDescriptions/zoo_wildlife/canonical/zoo_005_feeding_time.png`

- COSMETIC_EDGE_GUTTER; crop 0/0/6/6.
- [1448, 1086] → [1436, 1086]; RGB → RGB.
- Old SHA-256: `cdac1be694e650f10790e46934ca16cfff8b72e7abc19526a9ac8f00c503bde0`
- New SHA-256: `e8b363685ffc2d08f5bfa9203783e1170e11aac108cd3bb4c400a873765f6162`
- Only outer extraction gutters cropped; intended scene objects/actions retained. No padding, resampling or scenic fill.

### `app/src/main/assets/SceneDescriptions/zoo_wildlife/canonical/zoo_006_positions_story.png`

- COSMETIC_EDGE_GUTTER; crop 0/0/18/0.
- [1448, 1086] → [1430, 1086]; RGB → RGB.
- Old SHA-256: `1c3eed033a616221130671ed5d9260b5b9b6a6453c2d724b86a13fff42697470`
- New SHA-256: `6e55d4acc0a2849f908547df0ea6a1b495fdc9fa2cee42d9b98fb89785f4277b`
- Only outer extraction gutters cropped; intended scene objects/actions retained. No padding, resampling or scenic fill.

## Reviewed but deliberately unchanged / deferred

All 21 Prepositions candidates retain their bytes. Their manifest and JVM tests require
1448×1086 RGB. Cropping alone breaks that contract; padding their varied forest/water
edges would invent scenery or add a new matte. Neither is authorized here. In particular,
the four NEXT TO panels remain contaminated: removing their right divider is feasible,
but restoring dimensions honestly is not. No spacing cue or spatial relationship was
changed. All 52 Prepositions files therefore remain exactly as reviewed on baseline.
A subsequent owner decision on the canonical dimension contract is needed before repair.

Mouse has a pale/transparent edge without a confirmed adjacent panel. Background
canonicalization is outside scope; leave it for owner review.

- `app/src/main/assets/Animals/canonical/mouse.png` — AMBIGUOUS_REVIEW_REQUIRED.
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_crocodile_far_from.png` — COSMETIC_EDGE_GUTTER.
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_crocodile_in_front_of.png` — CONFIRMED_SEPARATOR_SLIVER.
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_crocodile_near.png` — CONFIRMED_SEPARATOR_SLIVER.
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_crocodile_next_to.png` — CONFIRMED_SEPARATOR_SLIVER.
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_dinosaur_between.png` — COSMETIC_EDGE_GUTTER.
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_dinosaur_far_from.png` — COSMETIC_EDGE_GUTTER.
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_dinosaur_in_front_of.png` — COSMETIC_EDGE_GUTTER.
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_dinosaur_next_to.png` — CONFIRMED_SEPARATOR_SLIVER.
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_dragon_in_front_of.png` — COSMETIC_EDGE_GUTTER.
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_dragon_near.png` — COSMETIC_EDGE_GUTTER.
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_dragon_next_to.png` — CONFIRMED_SEPARATOR_SLIVER.
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_fish_inside.png` — COSMETIC_EDGE_GUTTER.
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_fish_outside.png` — COSMETIC_EDGE_GUTTER.
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_octopus_inside.png` — COSMETIC_EDGE_GUTTER.
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_octopus_outside.png` — COSMETIC_EDGE_GUTTER.
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_seahorse_inside.png` — COSMETIC_EDGE_GUTTER.
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_seahorse_outside.png` — COSMETIC_EDGE_GUTTER.
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_snake_between.png` — COSMETIC_EDGE_GUTTER.
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_snake_far_from.png` — COSMETIC_EDGE_GUTTER.
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_snake_next_to.png` — CONFIRMED_SEPARATOR_SLIVER.
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_turtle_outside.png` — COSMETIC_EDGE_GUTTER.

## Validator

Authoring-only dependencies: Pillow 12.3.0, NumPy 2.5.3 (isolated venv; no app dependency).
`audit_asset_hygiene.py` measures all edges, near-white runs/bands within 100px (the
requested ~40px missed real classroom/Prepositions dividers), visible alpha and coloured
pixels beyond a band. Pale scenery only warns; strong signatures or corrupt/dimension
errors exit 1. Hash-bound review decisions label known gutters without suppressing strong
failures. This heuristic can flag legitimate panel borders; manual review is required.

```sh
/private/tmp/basti-asset-audit-venv/bin/python scripts/audit_asset_hygiene.py \
  --output docs/asset-hygiene/ASSET_AUDIT_AFTER.csv \
  --review docs/asset-hygiene/REVIEW_DECISIONS.json
/private/tmp/basti-asset-audit-venv/bin/python -m unittest discover -s scripts -p test_asset_hygiene.py
python3 -m unittest discover -s scripts -p test_scene_descriptions.py
python3 scripts/generate_scene_descriptions.py --check
```

Expected current validator exit: **1**, because six strong Prepositions signatures remain
explicitly deferred. It is not a clean asset-library certification. No corrupt images.

## Validation

- Validator synthetic tests: 6/6 passed.
- Scene Description Python tests: 22/22 passed; generator freshness passed (9 categories / 81 scenes).
- Focused JVM: 84/84 passed, zero failures/errors/skips.
- Full JVM: 321/321 passed, zero failures/errors/skips.
- assembleDebug passed; lintDebug: 0 errors, 3 existing warnings, 2 informational findings.
- Focused emulator instrumentation: 22/22 passed, zero failures/errors/skips. Includes all 81 Tell Me assets decoded, Follow canonical-pool decoding, and Prepositions/Tell Me/Vocabulary/Follow screen coverage.
- `git diff --check` and new text-file whitespace checks passed.
- No physical S24/Fire acceptance. Owner visual review on real displays remains necessary.

Android command environment:

```sh
export JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home'
export ANDROID_HOME='/Users/paulbell/Library/Android/sdk'
export ANDROID_SERIAL=emulator-5554
./gradlew testDebugUnitTest --tests '*Prepositions*' --tests '*SceneDescription*' --tests '*Vocabulary*' --tests '*Follow*' --console=plain
./gradlew testDebugUnitTest assembleDebug lintDebug --console=plain
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.bellfamily.bastischool.ui.prepositions.PrepositionsArtworkTest,com.bellfamily.bastischool.ui.prepositions.PrepositionsArtworkScreenTest,com.bellfamily.bastischool.ui.tellme.TellMeArtworkTest,com.bellfamily.bastischool.ui.tellme.TellMeScreenTest,com.bellfamily.bastischool.ui.vocabulary.VocabularyArtworkLoaderTest,com.bellfamily.bastischool.ui.vocabulary.VocabularyArtworkScreenTest,com.bellfamily.bastischool.ui.followinstructions.FollowArtworkTest,com.bellfamily.bastischool.ui.followinstructions.FollowScreenTest --console=plain
git diff --check
```

Initial Gradle/ADB sandbox attempts failed before tests with socket permission errors;
rerun with approved host access. Initial Pillow installation required network approval;
all image analysis then used the isolated venv (system Python lacks Pillow). No tests
or production behavior were changed to bypass infrastructure errors.


## Complete changed-file inventory

- `BUILD_NOTES.md`
- `SESSION_HANDOFF.md`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/followinstructions/FollowArtworkTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/tellme/TellMeArtworkTest.kt`
- `app/src/main/assets/Animals/canonical/dog.png`
- `app/src/main/assets/Animals/canonical/horse.png`
- `app/src/main/assets/SceneDescriptions/classroom_school/canonical/classroom_004_objects_places.png`
- `app/src/main/assets/SceneDescriptions/classroom_school/canonical/classroom_005_quiet_corner.png`
- `app/src/main/assets/SceneDescriptions/classroom_school/canonical/classroom_006_art_sorting.png`
- `app/src/main/assets/SceneDescriptions/countryside_farm/canonical/farm_004_animal_actions.png`
- `app/src/main/assets/SceneDescriptions/countryside_farm/canonical/farm_005_transport_and_places.png`
- `app/src/main/assets/SceneDescriptions/countryside_farm/canonical/farm_006_garden_food.png`
- `app/src/main/assets/SceneDescriptions/mountains_alpine/canonical/mountains_007_journey_story.png`
- `app/src/main/assets/SceneDescriptions/mountains_alpine/canonical/mountains_008_weather_shelter.png`
- `app/src/main/assets/SceneDescriptions/mountains_alpine/canonical/mountains_009_spatial_clues.png`
- `app/src/main/assets/SceneDescriptions/park_playground/canonical/park_007_playground_actions.png`
- `app/src/main/assets/SceneDescriptions/park_playground/canonical/park_008_helping_clean_up.png`
- `app/src/main/assets/SceneDescriptions/park_playground/canonical/park_009_turn_taking.png`
- `app/src/main/assets/SceneDescriptions/sky_flying/canonical/sky_007_weather_sequence.png`
- `app/src/main/assets/SceneDescriptions/sky_flying/canonical/sky_008_flying_directions.png`
- `app/src/main/assets/SceneDescriptions/sky_flying/canonical/sky_009_night_sky_clues.png`
- `app/src/main/assets/SceneDescriptions/zoo_wildlife/canonical/zoo_004_habitats.png`
- `app/src/main/assets/SceneDescriptions/zoo_wildlife/canonical/zoo_005_feeding_time.png`
- `app/src/main/assets/SceneDescriptions/zoo_wildlife/canonical/zoo_006_positions_story.png`
- `docs/asset-hygiene/ASSET_AUDIT_AFTER.csv`
- `docs/asset-hygiene/ASSET_AUDIT_BEFORE.csv`
- `docs/asset-hygiene/REPAIRS.json`
- `docs/asset-hygiene/REPAIR_REPORT.md`
- `docs/asset-hygiene/REVIEW_DECISIONS.json`
- `docs/asset-hygiene/after/ASSET_AUDIT_AFTER_01.png`
- `docs/asset-hygiene/after/ASSET_AUDIT_AFTER_02.png`
- `docs/asset-hygiene/after/ASSET_AUDIT_AFTER_03.png`
- `docs/asset-hygiene/after/ASSET_AUDIT_AFTER_04.png`
- `docs/asset-hygiene/after/ASSET_AUDIT_AFTER_05.png`
- `docs/asset-hygiene/after/ASSET_AUDIT_AFTER_06.png`
- `docs/asset-hygiene/after/ASSET_AUDIT_AFTER_07.png`
- `docs/asset-hygiene/before/ASSET_AUDIT_BEFORE_01.png`
- `docs/asset-hygiene/before/ASSET_AUDIT_BEFORE_02.png`
- `docs/asset-hygiene/before/ASSET_AUDIT_BEFORE_03.png`
- `docs/asset-hygiene/before/ASSET_AUDIT_BEFORE_04.png`
- `docs/asset-hygiene/before/ASSET_AUDIT_BEFORE_05.png`
- `docs/asset-hygiene/before/ASSET_AUDIT_BEFORE_06.png`
- `docs/asset-hygiene/before/ASSET_AUDIT_BEFORE_07.png`
- `scripts/audit_asset_hygiene.py`
- `scripts/test_asset_hygiene.py`

# Second pass — Prepositions cleanup, 2026-09-28

Baseline: `fbc7082558029b22dcfb9305d37d6f3f106a2c87`, clean main matching origin/main.
GitHub Actions [36378511734](https://github.com/paulyglocke/Basti-Learning-Adventure-Public/actions/runs/36378511734)
was green before editing. No commit/push in this pass.

## Contract assessment and change

Previous rule: **exact 1448×1086 RGB**. It appeared in the art manifest's canonical_size,
PrepositionsExpansionTest, the hygiene validator and documented authoring expectations.
It was not a runtime or learning identity requirement. PrepositionsArtworkLoader simply
uses BitmapFactory with inSampleSize=2; PositionSceneImage uses ContentScale.Fit inside
its unchanged 4:3 frame. No runtime pixel-coordinate semantics or fixed-size indexing.
The historical procedural PositionGeometry is not the artwork renderer.

New rule: **readable PNG, RGB/RGBA, width ≥1280, height ≥960, aspect ratio within ±5%
of 4:3**, inclusive (19/15 ≤ width/height ≤ 21/15). The largest safe crop removes
65/1448 = 4.49% of width; 5% is a narrow margin over that measured requirement. The
minimum retains at least 640×480 after the existing half-resolution decode. Actual
repaired dimensions range from 1383×1086 to 1448×1083. No resizing or padding.
Manifest image_contract replaces canonical_size; each record still pins actual dimensions,
mode and SHA-256. Art revision 3/schema 1 remain the same catalogue; this is an edge
cleanup, not a new depiction/content revision. Tests freeze the pre-cleanup ordered
semantic/path projection and validate all 52 records/PNG decodes/contract/hashes.

Dimensions/hashes are not serialized into session/task identity. PrepositionsContent.restore
and validate compare semantic questions/text/revisions, not bitmap properties. No production
Kotlin changed. Activity revision 2, content 1.2, accepted legacy activity revision 1/content
1.1, checkpoint schema 1 and journal schema remain unchanged. Existing v1 recovery tests
exercise unanswered, retry, pending attempt, completed/pending completion and Play Again.
Rendering coverage also re-encodes/restores the current repaired semantic scene unchanged.

## Individual review and outcomes

Six confirmed separator/sliver files repaired. All 15 cosmetic candidates individually
reviewed with full-scene previews and edge strips: **15 REPAIRED_COSMETIC_GUTTER,
0 INTENTIONAL_EDGE_RETAINED, 0 AMBIGUOUS_DEFERRED** within this Prepositions candidate set.
Each has a straight extraction band inconsistent with the adjacent forest/water panel;
none is legitimate pale sky/water. Mouse remains ambiguous outside this task.

All 21 retained crops compare pixel-for-pixel against baseline; mode stays RGB. No
resampling, stretching, padding, inpainting, recolouring or regenerated content. Relative
spacing, depth/occlusion, animal/object counts and intended relations remain unchanged.
No meaningful part of the intended panel was removed. Existing truncation at untouched
image boundaries (e.g. dinosaur feet, dragon head, crocodile foreground) is unchanged;
this pass does not claim to reconstruct full bodies missing from original art.

NEXT TO keeps animal/rock spacing, removing only the right separator and adjacent panel.
IN FRONT OF keeps the foreground animal and background rock; NEAR keeps the inter-object
gap. FAR FROM retains foreground rock/background animal depth. BETWEEN retains both rocks.
INSIDE/OUTSIDE retain animal/cave boundaries and spatial placement.

Original BEFORE CSV and first-pass before/after contact sheets remain byte-identical.
Current AFTER CSV now has **174 CLEAN / 1 AMBIGUOUS / 0 strong signatures / 0 gutters**
across 175 scoped images. All 52 Prepositions images are detector-clean. The 3 excluded
Animals authoring references remain unchanged. **21 changed / 157 unchanged among 178
packaged rasters** relative to this pass's baseline (31/52 Prepositions unchanged).
Animals, SceneDescriptions/Tell Me, Seasons and Wilma bytes are all unchanged.
Previous repair records are retained; new entries include pass/baseline identifiers.
REVIEW_DECISIONS keeps prior deferred decisions nested as history, bound to their old hashes.
New contact sheets: `after/PREPOSITIONS_2026_09_28_01.png` through `_04.png`.

## Exact second-pass repair evidence

Crop values are top / bottom / left / right in original pixels. Every original is
1448×1086 RGB; every result is RGB. Full paths and hashes also live in REPAIRS.json.

### `app/src/main/assets/Prepositions/scenes/scene_prepositions_crocodile_far_from.png`

- Outcome: REPAIRED_COSMETIC_GUTTER. Crop: 0/0/0/7.
- Dimensions: 1448×1086 → 1441×1086.
- Old SHA-256: `762390b6e10b5c6e45bad66b1b4e0d1ea3370c400755ed286a49f71b82be91b1`
- New SHA-256: `965cfeb3a5ef469065a0df43a427198c9d3d355c051546b3b385ed0801b6407b`
- Individually reviewed straight extraction gutter removed. Intended far_from panel retained pixel-for-pixel; subject/object count, relative spacing and depth cues unchanged. No padding/resampling. Original truncation at untouched edges is unchanged.

### `app/src/main/assets/Prepositions/scenes/scene_prepositions_crocodile_in_front_of.png`

- Outcome: REPAIRED_SEPARATOR_SLIVER. Crop: 0/0/35/4.
- Dimensions: 1448×1086 → 1409×1086.
- Old SHA-256: `078cacffd7cd46b68ef24c8dc9771aed93d5b8c9075f0b8af0edca36b5d036d9`
- New SHA-256: `0d9723ec81c0a87cbcd4d3b3f1ecab8ee47dd98e4dbc864741f7803142638f41`
- Reviewed separator and adjacent-panel sliver removed. Intended in_front_of panel retained pixel-for-pixel; subject/object count, relative spacing and depth cues unchanged. No padding/resampling. Original truncation at untouched edges is unchanged.

### `app/src/main/assets/Prepositions/scenes/scene_prepositions_crocodile_near.png`

- Outcome: REPAIRED_SEPARATOR_SLIVER. Crop: 0/0/34/0.
- Dimensions: 1448×1086 → 1414×1086.
- Old SHA-256: `70161bdb082f0bc0160beb671785c65ab1a0be99274b511c0cfe2d39eefe33d8`
- New SHA-256: `aa51ff1def3a5af94632a77cd817d345bab9c35e8438b576ed7da971cf077997`
- Reviewed separator and adjacent-panel sliver removed. Intended near panel retained pixel-for-pixel; subject/object count, relative spacing and depth cues unchanged. No padding/resampling. Original truncation at untouched edges is unchanged.

### `app/src/main/assets/Prepositions/scenes/scene_prepositions_crocodile_next_to.png`

- Outcome: REPAIRED_SEPARATOR_SLIVER. Crop: 0/0/0/65.
- Dimensions: 1448×1086 → 1383×1086.
- Old SHA-256: `6344e29654cc468ded95f0f828a839cc1c3873c7e61359271ff7a4a5bd5eb34c`
- New SHA-256: `fac7033cd527e43388b706272703373e52b92c910dd0592923d36ff2d8ad22be`
- Reviewed separator and adjacent-panel sliver removed. Intended next_to panel retained pixel-for-pixel; subject/object count, relative spacing and depth cues unchanged. No padding/resampling. Original truncation at untouched edges is unchanged.

### `app/src/main/assets/Prepositions/scenes/scene_prepositions_dinosaur_between.png`

- Outcome: REPAIRED_COSMETIC_GUTTER. Crop: 0/3/0/0.
- Dimensions: 1448×1086 → 1448×1083.
- Old SHA-256: `48fda61a3abc41ffe9425d0e4c17d7b7b8f594f2eac4b609f7152f45e9396721`
- New SHA-256: `df1d42e277b68540292f228265abc6c88d456c4cd6bcf87a747ff3fab1e5a8d0`
- Individually reviewed straight extraction gutter removed. Intended between panel retained pixel-for-pixel; subject/object count, relative spacing and depth cues unchanged. No padding/resampling. Original truncation at untouched edges is unchanged.

### `app/src/main/assets/Prepositions/scenes/scene_prepositions_dinosaur_far_from.png`

- Outcome: REPAIRED_COSMETIC_GUTTER. Crop: 0/0/10/0.
- Dimensions: 1448×1086 → 1438×1086.
- Old SHA-256: `855984cd91ca80dc649119c051460315188c4b114b658f7a223729dc4af7d342`
- New SHA-256: `5c685110be27bed0773fe67ff39861cb99a8a4ff1863c35d01ffdc5ff17249df`
- Individually reviewed straight extraction gutter removed. Intended far_from panel retained pixel-for-pixel; subject/object count, relative spacing and depth cues unchanged. No padding/resampling. Original truncation at untouched edges is unchanged.

### `app/src/main/assets/Prepositions/scenes/scene_prepositions_dinosaur_in_front_of.png`

- Outcome: REPAIRED_COSMETIC_GUTTER. Crop: 0/0/18/0.
- Dimensions: 1448×1086 → 1430×1086.
- Old SHA-256: `add0674be28dd495d6b4d140837d27a211441d927309b614d63e56fd46011088`
- New SHA-256: `6cbc02a4aae9b29877f91096e428ec1bd629de439a826de50880feb62f99df96`
- Individually reviewed straight extraction gutter removed. Intended in_front_of panel retained pixel-for-pixel; subject/object count, relative spacing and depth cues unchanged. No padding/resampling. Original truncation at untouched edges is unchanged.

### `app/src/main/assets/Prepositions/scenes/scene_prepositions_dinosaur_next_to.png`

- Outcome: REPAIRED_SEPARATOR_SLIVER. Crop: 0/0/0/65.
- Dimensions: 1448×1086 → 1383×1086.
- Old SHA-256: `f37621e923ed5a6a1e5b62953e8a03a4ce7be65e0b68be213ee424b077af1503`
- New SHA-256: `0af85871af1f92deec1b2135d102c2ead314a3f444eef51e6378869f0a0703fa`
- Reviewed separator and adjacent-panel sliver removed. Intended next_to panel retained pixel-for-pixel; subject/object count, relative spacing and depth cues unchanged. No padding/resampling. Original truncation at untouched edges is unchanged.

### `app/src/main/assets/Prepositions/scenes/scene_prepositions_dragon_in_front_of.png`

- Outcome: REPAIRED_COSMETIC_GUTTER. Crop: 0/0/22/0.
- Dimensions: 1448×1086 → 1426×1086.
- Old SHA-256: `f6b4a699f6560571885d20948ee715c35181841e8e44b83013a657a599da5ddd`
- New SHA-256: `6d14dd4b5a6bec99eaa19fc11c0449c176151e0fec3a6a961505148d6a1d38bd`
- Individually reviewed straight extraction gutter removed. Intended in_front_of panel retained pixel-for-pixel; subject/object count, relative spacing and depth cues unchanged. No padding/resampling. Original truncation at untouched edges is unchanged.

### `app/src/main/assets/Prepositions/scenes/scene_prepositions_dragon_near.png`

- Outcome: REPAIRED_COSMETIC_GUTTER. Crop: 0/0/22/0.
- Dimensions: 1448×1086 → 1426×1086.
- Old SHA-256: `c0ba0dfe9b33ee9db5bc639be170cb0bf26bc8996de83a8db8007546db379428`
- New SHA-256: `3b9ba321ce3cdb1cecc5cacd4b8ab17b9c7defbec9f6695a51c032643374eb19`
- Individually reviewed straight extraction gutter removed. Intended near panel retained pixel-for-pixel; subject/object count, relative spacing and depth cues unchanged. No padding/resampling. Original truncation at untouched edges is unchanged.

### `app/src/main/assets/Prepositions/scenes/scene_prepositions_dragon_next_to.png`

- Outcome: REPAIRED_SEPARATOR_SLIVER. Crop: 0/0/0/65.
- Dimensions: 1448×1086 → 1383×1086.
- Old SHA-256: `035c00678f82c4722f44ca174d096dd5cede5519d3ecabad89b4642e76ba0306`
- New SHA-256: `e856bafc90695def333fc0e66555d85b7a2915666b98bacc2874e7c5f366f347`
- Reviewed separator and adjacent-panel sliver removed. Intended next_to panel retained pixel-for-pixel; subject/object count, relative spacing and depth cues unchanged. No padding/resampling. Original truncation at untouched edges is unchanged.

### `app/src/main/assets/Prepositions/scenes/scene_prepositions_fish_inside.png`

- Outcome: REPAIRED_COSMETIC_GUTTER. Crop: 0/0/4/7.
- Dimensions: 1448×1086 → 1437×1086.
- Old SHA-256: `16c7b935b90fe07dcf46cde965794282aca181544155cbf5301a11155194e3e6`
- New SHA-256: `feee11d2c0008a0452548be70cb3b8117c9e3aea06478e95a433d0e7b8fb5baa`
- Individually reviewed straight extraction gutter removed. Intended inside panel retained pixel-for-pixel; subject/object count, relative spacing and depth cues unchanged. No padding/resampling. Original truncation at untouched edges is unchanged.

### `app/src/main/assets/Prepositions/scenes/scene_prepositions_fish_outside.png`

- Outcome: REPAIRED_COSMETIC_GUTTER. Crop: 0/0/5/8.
- Dimensions: 1448×1086 → 1435×1086.
- Old SHA-256: `49c1d8f08bf8ccf981bcdfdb59c7e3ef18bab85102949a21b1bf1a25ed2b2928`
- New SHA-256: `d428e1c07e09d2bc43faff0c0dce4d38ae862f84c5cd90ae7cfad6c208a237f8`
- Individually reviewed straight extraction gutter removed. Intended outside panel retained pixel-for-pixel; subject/object count, relative spacing and depth cues unchanged. No padding/resampling. Original truncation at untouched edges is unchanged.

### `app/src/main/assets/Prepositions/scenes/scene_prepositions_octopus_inside.png`

- Outcome: REPAIRED_COSMETIC_GUTTER. Crop: 0/0/9/0.
- Dimensions: 1448×1086 → 1439×1086.
- Old SHA-256: `b10b46c3f41fc3685abd18e99293169b6574505e680a719de77539620b94aaca`
- New SHA-256: `9163d3061dc877f9e5128e2e0ec1b580395df2d4ded61e4969892307541330dd`
- Individually reviewed straight extraction gutter removed. Intended inside panel retained pixel-for-pixel; subject/object count, relative spacing and depth cues unchanged. No padding/resampling. Original truncation at untouched edges is unchanged.

### `app/src/main/assets/Prepositions/scenes/scene_prepositions_octopus_outside.png`

- Outcome: REPAIRED_COSMETIC_GUTTER. Crop: 0/0/8/0.
- Dimensions: 1448×1086 → 1440×1086.
- Old SHA-256: `12d2938be2b5338dc55757d379b63b48fae06e8592e4df4fe589204f61a52a5e`
- New SHA-256: `0623d105dfd366ca44af45a93354f2d654fdb687b7776ce99532a3146c38b622`
- Individually reviewed straight extraction gutter removed. Intended outside panel retained pixel-for-pixel; subject/object count, relative spacing and depth cues unchanged. No padding/resampling. Original truncation at untouched edges is unchanged.

### `app/src/main/assets/Prepositions/scenes/scene_prepositions_seahorse_inside.png`

- Outcome: REPAIRED_COSMETIC_GUTTER. Crop: 0/0/29/11.
- Dimensions: 1448×1086 → 1408×1086.
- Old SHA-256: `8ea7b76d6436e6dcb454687297093b34cd349224337a1513bc4762e6d86afe1e`
- New SHA-256: `12a0f37bcafda9cfa2f84a819756211d7f1f9f89539c97dc5b12fd8045691f4a`
- Individually reviewed straight extraction gutter removed. Intended inside panel retained pixel-for-pixel; subject/object count, relative spacing and depth cues unchanged. No padding/resampling. Original truncation at untouched edges is unchanged.

### `app/src/main/assets/Prepositions/scenes/scene_prepositions_seahorse_outside.png`

- Outcome: REPAIRED_COSMETIC_GUTTER. Crop: 0/0/33/7.
- Dimensions: 1448×1086 → 1408×1086.
- Old SHA-256: `7b953c86aa8a9545f5e23d98e4555eb11d7c7723728dcba864cff6daeafb2598`
- New SHA-256: `20e35addb1753295ec438466df660a2fb5abace04eeb2116b1ba3557a8b17c87`
- Individually reviewed straight extraction gutter removed. Intended outside panel retained pixel-for-pixel; subject/object count, relative spacing and depth cues unchanged. No padding/resampling. Original truncation at untouched edges is unchanged.

### `app/src/main/assets/Prepositions/scenes/scene_prepositions_snake_between.png`

- Outcome: REPAIRED_COSMETIC_GUTTER. Crop: 0/3/0/0.
- Dimensions: 1448×1086 → 1448×1083.
- Old SHA-256: `ecdb3e1345cad2d827cd10e8dc6d94fb8f5ade1c32d85981058b952b6236a5f1`
- New SHA-256: `2f64d96d9ae9a9b13902fbf4439e43ee37a91132114d0027e455999c6be1c7fb`
- Individually reviewed straight extraction gutter removed. Intended between panel retained pixel-for-pixel; subject/object count, relative spacing and depth cues unchanged. No padding/resampling. Original truncation at untouched edges is unchanged.

### `app/src/main/assets/Prepositions/scenes/scene_prepositions_snake_far_from.png`

- Outcome: REPAIRED_COSMETIC_GUTTER. Crop: 0/0/12/0.
- Dimensions: 1448×1086 → 1436×1086.
- Old SHA-256: `4695a3715f454aee6121a0e74164893de27709f0d43aced6aa536f2423a1d937`
- New SHA-256: `e1180a5302560de7e5b82b952afb047e1fcbe7b5e645cf9ef7369c1ca31265c8`
- Individually reviewed straight extraction gutter removed. Intended far_from panel retained pixel-for-pixel; subject/object count, relative spacing and depth cues unchanged. No padding/resampling. Original truncation at untouched edges is unchanged.

### `app/src/main/assets/Prepositions/scenes/scene_prepositions_snake_next_to.png`

- Outcome: REPAIRED_SEPARATOR_SLIVER. Crop: 0/0/0/65.
- Dimensions: 1448×1086 → 1383×1086.
- Old SHA-256: `72addcf37b5538e910ebae51e188afc2babab6a94548ade971dead1a9a910346`
- New SHA-256: `d27b5c2219f9f6320440dd1b6c829610db371686f737a822a8f974eff39b688e`
- Reviewed separator and adjacent-panel sliver removed. Intended next_to panel retained pixel-for-pixel; subject/object count, relative spacing and depth cues unchanged. No padding/resampling. Original truncation at untouched edges is unchanged.

### `app/src/main/assets/Prepositions/scenes/scene_prepositions_turtle_outside.png`

- Outcome: REPAIRED_COSMETIC_GUTTER. Crop: 0/0/0/4.
- Dimensions: 1448×1086 → 1444×1086.
- Old SHA-256: `f73fb3c04a90a273b50e113250be284064418a424d24050a65a6b0a0d1cf35da`
- New SHA-256: `cc85db1c003ff1fc55d945b886c49d2f633c07f05d98b85e9422e63483e01626`
- Individually reviewed straight extraction gutter removed. Intended outside panel retained pixel-for-pixel; subject/object count, relative spacing and depth cues unchanged. No padding/resampling. Original truncation at untouched edges is unchanged.

## Second-pass validation

Python 35/35; generator current; focused JVM 30/30; full JVM 321/321; focused emulator
10/10; full emulator 122/122, zero failures/errors/skips. Build passed; lint 0 errors /
3 existing warnings / 2 info. Full instrumentation spanned the session interruption and
reported 8h 15m elapsed; no restart or test weakening. No test failures in this pass.
Diff and new-text whitespace checks passed. See BUILD_NOTES for commands.
Physical S24/Fire visual review remains unperformed. No authored wording, answer generation,
production UI, session/progress/audio/navigation, dependency or CI/signing changes.

## Second-pass changed files

- `BUILD_NOTES.md`
- `CONTENT_DATA_SPEC.md`
- `NATIVE_ARCHITECTURE_SPEC.md`
- `SESSION_HANDOFF.md`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/prepositions/PrepositionsArtworkScreenTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/prepositions/PrepositionsArtworkTest.kt`
- `app/src/main/assets/Prepositions/metadata/prepositions_art_manifest.json`
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_crocodile_far_from.png`
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_crocodile_in_front_of.png`
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_crocodile_near.png`
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_crocodile_next_to.png`
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_dinosaur_between.png`
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_dinosaur_far_from.png`
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_dinosaur_in_front_of.png`
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_dinosaur_next_to.png`
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_dragon_in_front_of.png`
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_dragon_near.png`
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_dragon_next_to.png`
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_fish_inside.png`
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_fish_outside.png`
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_octopus_inside.png`
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_octopus_outside.png`
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_seahorse_inside.png`
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_seahorse_outside.png`
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_snake_between.png`
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_snake_far_from.png`
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_snake_next_to.png`
- `app/src/main/assets/Prepositions/scenes/scene_prepositions_turtle_outside.png`
- `app/src/test/java/com/bellfamily/bastischool/learning/prepositions/PrepositionsExpansionTest.kt`
- `docs/asset-hygiene/ASSET_AUDIT_AFTER.csv`
- `docs/asset-hygiene/REPAIRS.json`
- `docs/asset-hygiene/REPAIR_REPORT.md`
- `docs/asset-hygiene/REVIEW_DECISIONS.json`
- `docs/asset-hygiene/after/PREPOSITIONS_2026_09_28_01.png`
- `docs/asset-hygiene/after/PREPOSITIONS_2026_09_28_02.png`
- `docs/asset-hygiene/after/PREPOSITIONS_2026_09_28_03.png`
- `docs/asset-hygiene/after/PREPOSITIONS_2026_09_28_04.png`
- `scripts/audit_asset_hygiene.py`
- `scripts/test_asset_hygiene.py`

## Physical-acceptance recheck — 2026-09-29

The six strong Prepositions signatures and all 15 individually reviewed gutter repairs
from the preceding pass were reverified, not cropped again. Raw audit **without review
overrides** exits 0: 174 CLEAN, one existing ambiguous mouse, zero strong failures
among 175 scoped images. Retained pixels of all 21 PNGs equal the original crop;
157 other packaged rasters are byte-identical. No Scene Description PNG changed in
the separate [Tell Me semantic review](../tellme-visual-qa/REVIEW.md). Original BEFORE
evidence and first-pass history remain intact. See BUILD_NOTES.md for combined validation.

### Subsequent owner-supplied Park 9 replacement

After the successful full regression run, the owner replaced Park 9 outside the agent
repair workflow. This is not a crop-only repair and is not added to REPAIRS.json.
Current ASSET_AUDIT_AFTER.csv reflects that PNG: 1448×1086 RGB, raw audit CLEAN.
The other 80 Scene Description PNGs remain unchanged. See the current Tell Me audit
for old/new fingerprints and visual review. Prepositions crop evidence is unaffected.
