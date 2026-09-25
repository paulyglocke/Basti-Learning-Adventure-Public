# Visual Review

Review date: 2026-09-25

Current library checkpoint: **Wave 3 complete, 81 approved scenes across nine
categories (nine each)**. See `SANITY_CHECK.md` and `WAVE_3_STATUS.md`.
The detailed review below preserves the historical 27-scene Wave 1 evidence;
it does not claim a new visual review of all 81 scenes during native integration.

## Historical Wave 1 scope

The 27 Wave 1 canonical illustrations were reviewed against:
- the specific learning task for that scene
- focal subject counts
- obvious anatomy / duplication problems
- clarity of actions, features or spatial relationships
- likely readability at phone size
- obvious conflicts with the authored generation brief

## Historical Wave 1 result

### Fully acceptable for the intended task

- Ocean 1 — Reef Actions
- Ocean 2 — Colours and Sizes
- Ocean 3 — Hiding Places
- Jungle 1 — Jungle Actions
- Jungle 2 — Colours and Features
- Jungle 3 — Jungle Positions
- Woodland 1 — Forest Animals and Features
- Woodland 2 — Forest Actions
- Woodland 3 — Hiding and Positions
- Park 1 — Playground Actions
- Park 2 — Helping and Everyday Tasks
- Park 3 — Playground Positions
- Sky 1 — Flying Actions
- Sky 2 — Colours, Sizes and Comparisons
- Sky 3 — High, Low and Around
- Mountains 1 — Alpine Features and Comparisons
- Mountains 2 — Alpine Actions
- Mountains 3 — Alpine Positions (replacement approved; sheep/below-hill relationship clarified)
- Countryside 2 — Farm Animals, Colours and Counting
- Countryside 3 — Around the Farm
- Classroom 1 — Learning Tasks
- Classroom 2 — Routines, Listening and Helping
- Classroom 3 — Classroom Positions and Instructions
- Zoo 1 — Wildlife Features & Comparisons
- Zoo 2 — Wildlife Actions
- Zoo 3 — Around the Wildlife Park

### Task-acceptable with a logged non-blocking deviation

**Countryside 1 — Farm Jobs and Tasks (`scene.farm.tasks.01`)**

All five farm jobs are clear: watering, collecting eggs, feeding chickens,
carrying vegetables and sweeping. The generated image contains **four chickens**
rather than the requested three. Chicken counting is not a learning target in
this scene, so the picture remains suitable for the functional-language task.
Do not use this scene to ask "How many chickens?"

## Previously resolved items

- Ocean 1 was regenerated to fix the turtle anatomy issue.
- Park 2 was regenerated to make the sharing interaction explicit.
- Mountains 3 was regenerated so the sheep now reads clearly as BELOW the hill.

## Historical Wave 1 overall

- Canonical scene files: **27**
- Fully acceptable: **26**
- Task-acceptable with logged non-blocking deviation: **1**
- Revision recommended: **0**

Final in-app acceptance should still check crop behaviour and subject readability
on the target phone.


- Batch 2 auto-check completed for park_playground, sky_flying and mountains_alpine; all new images are present and readable.
