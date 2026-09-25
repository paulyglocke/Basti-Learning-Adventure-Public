# Mountains / Alpine Nature — Wave 1

Status: COMPLETE and user-approved.

Approved production scenes:

1. `scene.mountains.features.01`
   - Primary: nouns + adjectives + visible features + comparisons
   - Asset: `canonical/mountains_001_features.png`

2. `scene.mountains.actions.02`
   - Primary: action verbs + animal nouns
   - Asset: `canonical/mountains_002_actions.png`

3. `scene.mountains.positions.03`
   - Primary: spatial language
   - Asset: `canonical/mountains_003_positions.png`

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
