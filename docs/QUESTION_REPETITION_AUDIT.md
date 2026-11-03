# Question and target repetition audit

Scope: generated tasks inside one learning round. Repeating a concept across a
later round is still useful practice. A “repeat” here means the same authored
task identity or correct target appearing twice in one round, unless a bounded
content pool is documented below.

## Safeguards

| Task family | Current round safeguard | Repeat behavior |
| --- | --- | --- |
| Shared candidate-based quizzes (Vocabulary, Seasons recognition/clues, Wilma Find/Relations) | `CandidateTaskGenerator` rejects duplicate authored task IDs, shuffles each complete candidate cycle, and does not repeat a task until that cycle is exhausted. | A 10-task round can repeat after the pool is exhausted: e.g. 6 Vocabulary targets and 4 Seasons recognition targets. |
| Letters — which animal starts with this written letter? | New native `LettersContent` supplies 19 reviewed animal targets. It selects no replacement within the round; runtime validation rejects repeated target animals. Every option set has exactly one matching initial in each language. | No repeated target or task in either 5- or 10-question rounds. English and German initials are authored separately (Horse/Pferd H/P; Crocodile/Krokodil C/K). |
| Prepositions | Shuffles 52 authored scene tasks, then takes 5 or 10. Existing content validation and tests assert unique question IDs per round. | No repeated scene/question within a round. Two choices per question are intentional. |
| Addition, Add Bigger Numbers, Take Away, Mixed Addition, More or Fewer, Number Order, Months question banks, Months ↔ Seasons | Each generator selects distinct facts, anchors, starts or definitions and validates the generated set. | No repeated authored task within a round; number/answer values may recur when the underlying question is different. |
| Make 5 | Balances the missing quantities 1–4 and prevents adjacent repeats. | A 5-question round repeats one amount; a 10-question round repeats each amount two or three times. This is deliberate short-range practice from a four-item number-bond set. |
| Follow the Instructions, one step | Avoids consecutive repeated targets; its pool has six animals. | A 10-task round can revisit an animal after other targets have appeared. |
| Follow the Instructions, two steps | Requires two different animals within a task and distinct task pairs; tests cover uniqueness. | An animal can appear in more than one pair in the same round; the ordered pair is the learning task. |
| Subitising | A 10-task round visits 1–5 twice, shuffling the second pass and avoiding a same-number boundary repeat. | Each quantity intentionally appears twice in a 10-task round because only five quantities are taught here. |
| Numbers & Groups | Uses ten distinct tasks: each quantity once in each direction. | A number appears in both directions, which are different questions. |
| Match Colours | Each round visits every colour before starting another shuffled cycle. | A 10-task round repeats some colours after all seven have appeared; target shape/context changes. |
| Wilma Today | Cycles seven weekdays while alternating yesterday/tomorrow. | A weekday can recur with the opposite relation; the weekday/relation pair does not repeat in the round. |
| Habitat Matching | Uses seven unique authored question IDs and validates the full fixed set. | Rabbit is intentionally the target of two different questions (the two pictured contexts and underground shelter). This is an activity-specific target-repeat exception, not a duplicate question. |
| Legacy written-letter questions in Mixed Adventure / restored standalone routes | A shuffled index queue is stored in the existing round checkpoint. It completes each six-question language pack before cycling and prevents an identical boundary repeat; older checkpoints without the queue retain their frozen random-draw restore behavior. | No repeated letter target until all six legacy targets have appeared. The new native Home route has the expanded 19-animal pool. Other legacy task families still sample some content with replacement. |
| Other legacy tasks (Numbers, Easy Maths, Animal Actions, legacy time and Mixed Adventure) | Checkpoints freeze each current question and random draws for restore, but there is no round-wide used-task catalogue. | Some exact questions/animals can recur within a round. This is a known migration gap; these routes remain legacy compatibility code, except Mixed Adventure still links the families. |

## Regression coverage

- `TaskGeneratorTest` verifies that a complete candidate cycle is exhausted before
  any authored question repeats.
- `LettersContentTest` covers both languages, both round lengths and 101 seeds;
  it checks unique task IDs, unique target animals and exactly one correct initial
  among the four choices.
- Activity-specific generator tests remain the source of truth for the other
  families; see the matching `learning/*` test packages. Repetition after a
  bounded pool is exhausted is a declared practice policy, not a random accident.

## Prepositions clarification

Prepositions is already native and complete for its current reviewed slice: 13
spatial relations and 52 illustrated scenes. Each question presents two answer
choices, not two total relations or scenes. New rounds use unique scenes.
