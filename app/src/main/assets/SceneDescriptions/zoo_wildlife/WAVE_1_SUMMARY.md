# Zoo / Wildlife Park — Wave 1

Status: COMPLETE and user-approved.

Approved production scenes:

1. `scene.zoo.features.01`
   - Primary: animal nouns + adjectives + visible features + comparisons
   - Asset: `canonical/zoo_001_features.png`

2. `scene.zoo.actions.02`
   - Primary: action verbs + animal nouns
   - Asset: `canonical/zoo_002_actions.png`

3. `scene.zoo.positions.03`
   - Primary: spatial language
   - Asset: `canonical/zoo_003_positions.png`

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
