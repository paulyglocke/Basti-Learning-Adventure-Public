# Basti School — Scene Description Assets

Repository destination:

`app/src/main/assets/SceneDescriptions/`

This folder contains authored cartoon scenes for the future scene-description /
"Tell Me! / Erzähl mal" experience and any later activity that reuses the same
scene knowledge.

## Core learning idea

A scene is not just a pretty illustration. Each image is planned around explicit
language opportunities so Basti can describe:

- **nouns** — who and what is visible
- **verbs** — what a character or object is doing
- **adjectives** — visible properties and comparisons
- **spatial language** — where something is in relation to something else
- **tasks / purposeful actions** — especially in school, home and other
  functional settings
- **sentence expansion** — moving from one-word answers toward
  who + action + object/location/property

Images should support several correct descriptions. Do not design scenes around
one exact sentence that a child must reproduce.

## Category IDs and current status

1. `ocean_underwater` — Wave 3 complete (9 approved scenes)
2. `jungle_rainforest` — Wave 3 complete (9 approved scenes)
3. `woodland_forest` — Wave 3 complete (9 approved scenes)
4. `park_playground` — Wave 3 complete (9 approved scenes)
5. `sky_flying` — Wave 3 complete (9 approved scenes)
6. `mountains_alpine` — Wave 3 complete (9 approved scenes)
7. `countryside_farm` — Wave 3 complete (9 approved scenes)
8. `classroom_school` — Wave 3 complete (9 approved scenes); functional tasks are the primary focus
9. `zoo_wildlife` — Wave 3 complete (9 approved scenes)

All nine categories are present. Each category manifest lists nine production scenes;
files outside those approved lists are not runtime content.

## Folder conventions

Each authored category contains:

- `canonical/` — approved production scene PNGs
- `metadata/` — one authored JSON sidecar per canonical scene
- `reference/` — prototypes/reference images; never silently promote to canonical
- `prompts/` — generation/revision prompt used for each approved scene
- `manifest.json` — category-level index of approved scenes
- `scene_plan.json` — planned scene set and language targets
- `README.md` — category-specific visual and learning rules
- `WAVE_1_SUMMARY.md` — concise implementation/handoff status

### Stable IDs

Use semantic IDs such as `scene.ocean.reef_actions.01`.
Do not use translated display text as identity.

Filename pairing:

- `canonical/ocean_001_reef_actions.png`
- `metadata/ocean_001_reef_actions.json`
- `prompts/ocean_001_reef_actions.md`

Image filenames are language-neutral. English/German language data belongs in
metadata/content, not inside the pixels.

## Image rules

- bright, friendly, polished 2D cartoon style suitable for a young child
- 4:3 landscape for the current scene set
- no text, captions, letters, labels, logos or UI baked into the image
- actions must be visually unambiguous
- important subjects must remain identifiable on a phone
- avoid excessive background clutter
- do not duplicate or morph focal animals
- avoid impossible anatomy when the body/action itself is a teaching target
- use clear relative positions when spatial language is a target
- use visible evidence for adjectives; do not require guessing internal state
- generated images are reviewed assets, not automatically accepted production art

## Language/content rules

The 81 approved scenes have two authored metadata formats:

- Wave 1 (27 scenes): bilingual titles, target language, example child descriptions
  and adult-support lists/expansions; primary/secondary learning focus.
- Waves 2/3 (54 scenes): English titles, purpose, target words/sentence models and
  adult-support focus/starter/expansion prompts. German is not yet authored.
- Visual audits remain authoring evidence. Do not infer missing translations,
  example sentences or a machine-gradable answer set.

Adult support follows a simple principle: accept the child's answer first, then
model one small step more language without requiring exact repetition.

Example descriptions are teaching/reference material, not an exhaustive set of
acceptable child responses. A different valid description must not be treated
as wrong merely because it does not match an example sentence.

## Astra integration notes

When this work reaches implementation:

1. Inspect the current content/data and native architecture specs first.
2. Reuse existing typed IDs/content repositories rather than inventing a second
   content system.
3. Keep image assets separate from localized strings.
4. Load/decode artwork off the main thread following existing native asset patterns.
5. Do not add automatic pronunciation scoring or open-ended speech grading as a
   side effect of integrating these assets.
6. Preserve offline operation.
7. Treat `reference/` and `prompts/` as authoring material, not canonical runtime evidence.
8. Only scenes explicitly listed in a category `manifest.json` are production scenes.
9. Do not change existing learning activities just because these assets exist.
10. Update relevant product/content/art specs when the first scene activity is implemented.

## Current checkpoint

Wave 3 is complete: **9 categories × 9 approved scenes = 81 production scenes**.
See `manifest.json`, `WAVE_3_STATUS.md` and `SANITY_CHECK.md` for the current
checkpoint. Wave 1 summaries and the detailed Wave 1 visual review are historical.

The pure native scene catalogue is generated from the root/category manifests and
referenced metadata by `scripts/generate_scene_descriptions.py`. It exposes only
approved canonical scenes; no runtime JSON parsing or UI is introduced. German
lookups for the 54 English-only records return unavailable, never English fallback.
See CONTENT_DATA_SPEC.md at the repository root for the typed contract.
