# Ocean / Underwater — Wave 1

Status: COMPLETE and user-approved.

Approved production scenes:

1. `scene.ocean.reef_actions.01`
   - Primary: verbs + nouns
   - Asset: `canonical/ocean_001_reef_actions.png`

2. `scene.ocean.colours_sizes.02`
   - Primary: adjectives + comparisons + counting
   - Asset: `canonical/ocean_002_colours_sizes.png`

3. `scene.ocean.hiding_places.03`
   - Primary: spatial language / prepositions
   - Asset: `canonical/ocean_003_hiding_places.png`

Each scene has:
- a canonical PNG
- a generation brief in `prompts/`
- structured EN/DE metadata in `metadata/`
- adult support questions
- sentence starters
- modelling and expansion examples
- a recorded visual acceptance audit

Integration rule:
Use only scenes listed in `approvedScenes` in `manifest.json` as production assets.
Do not infer additional accepted child answers solely from the example sentences; examples are scaffolding, not an exhaustive grading contract.
