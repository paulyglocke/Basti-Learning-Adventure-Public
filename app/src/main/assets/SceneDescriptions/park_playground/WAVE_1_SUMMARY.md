# Park / Playground — Wave 1

Status: COMPLETE and user-approved.

Approved production scenes:

1. `scene.park.actions.01`
   - Primary: verbs + nouns + everyday actions
   - Asset: `canonical/park_001_actions.png`

2. `scene.park.tasks.02`
   - Primary: functional tasks + social actions + everyday language
   - Asset: `canonical/park_002_tasks.png`

3. `scene.park.positions.03`
   - Primary: spatial language
   - Asset: `canonical/park_003_positions.png`

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
