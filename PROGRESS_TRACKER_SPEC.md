# Progress Tracker Specification

## Goal
Answer:
1. What has Basti practised?
2. What can he do reliably?
3. What needs more practice?
4. What should the app offer next?

Local/offline only. No account, cloud analytics or comparison with other children.

## Track skills, not just activity scores
Example hierarchy:

Language:
- naming
- adjective+noun
- subject+verb
- full_sentence
- describe_picture
- answer_where
- answer_why
- retell_story

Listening:
- one_step_instruction
- two_step_instruction
- three_step_instruction
- spatial_instruction
- delayed_instruction
- inhibition_instruction

Letters:
- recognise_D
- sound_D
- initial_D
- uppercase_lowercase_D

Numbers:
- recognise_1_to_10
- count_1_to_10
- subitise_1_to_3
- subitise_1_to_5
- more_less
- addition_to_5

Spatial:
- in
- on
- under
- behind
- beside
- between
- left
- right

## Parent-facing states
- New
- Learning
- Practising
- Going well
- Secure
- Ready for more

Avoid negative labels like weak/bad/behind.

## Suggested per-skill data
- attemptCount
- independentSuccesses
- successWithReplay
- successWithHint
- incorrectAttempts
- lastPractised
- recentResults
- difficultyLevel
- contextsSeen
- languagesSeen
- optional parentHelpUsed

Do not use response time as a simple quality score.

## Independence
Differentiate:
- correct immediately
- correct after replay
- correct after visual hint
- correct after parent help

Support is not failure.

## Generalisation
A skill should only become Secure after success across multiple contexts.

Example for “under”:
- snake-rock
- dragon-table
- pencil-book
- crocodile-bridge

## Spaced review
Bring secure skills back periodically:
- next day
- several days later
- following week

## Recency weighting
Recent results should matter more than old mistakes.

## Speaking progress
Avoid automatic pronunciation scoring initially.
Track:
- attempted
- word
- short phrase
- sentence
- longer description

No recording storage required.

## Parent observation
Optional:
Seen outside app ✓

## Suggested Focus
Recommend only 3–4 current areas, e.g.:
- two-step spoken instructions
- “between”
- describing pictures with a verb
- quantities 4–5

## Today’s Adventure
Daily mix:
- 1 confidence task
- 1 focus task
- 1 communication task
- 1 game/reward task

## Parent dashboard
Show useful summaries, not grades or comparisons.

## Implementation direction
Prefer shared native Kotlin models:
- SkillDefinition
- SkillProgress
- AttemptRecord
- ContextExposure
- ActivityResult
- ParentObservation

Every future activity should report progress through the same API.
