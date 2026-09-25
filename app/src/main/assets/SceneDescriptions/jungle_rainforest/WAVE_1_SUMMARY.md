# Jungle / Rainforest — Wave 1

Status: COMPLETE and user-approved.

Approved production scenes:

1. `scene.jungle.actions.01`
   - Primary: verbs + nouns
   - Asset: `canonical/jungle_001_actions.png`

2. `scene.jungle.colours_features.02`
   - Primary: adjectives + visible features + comparisons
   - Asset: `canonical/jungle_002_colours_features.png`

3. `scene.jungle.positions.03`
   - Primary: spatial language
   - Asset: `canonical/jungle_003_positions.png`

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
