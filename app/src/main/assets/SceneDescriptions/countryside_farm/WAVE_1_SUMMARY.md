# Countryside / Farm — Wave 1

Status: COMPLETE and user-approved.

Approved production scenes:

1. `scene.farm.tasks.01`
   - Primary: functional tasks + action verbs + everyday nouns
   - Asset: `canonical/farm_001_tasks.png`
   - Logged non-blocking deviation: four chickens are visible instead of the requested three.

2. `scene.farm.features.02`
   - Primary: animal nouns + adjectives + counting + comparisons
   - Asset: `canonical/farm_002_features.png`

3. `scene.farm.positions.03`
   - Primary: spatial language
   - Asset: `canonical/farm_003_positions.png`

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
Do not use Farm Scene 1 as a counting prompt for chickens because its chicken-count deviation is logged.
