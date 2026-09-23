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
A skill should only become Secure after success across multiple genuinely varied contexts, not repeated success with one memorised object pairing.

Use **interest first, ordinary context next**. For “under”, for example:
- dinosaur egg-tree
- snake-rock
- pencil-book
- shoe-chair
- crocodile-bridge

Track context IDs explicitly so repeated success in one pairing cannot by itself mark the skill Secure.

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

## Universal support ladder
Use the same broad support progression where applicable:
1. independent attempt
2. replay
3. subtle visual/verbal hint
4. stronger model/help
5. optional parent help

Support is not failure. Record the least-supportive level needed for a successful attempt and avoid harsh Wrong/Fail framing.

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
Show useful change and next steps, not grades, comparisons or vanity totals.

Prefer summaries such as:

**Going well**
- one-step instructions
- quantities 1–5
- under / next to

**Suggested focus**
- two-step instructions
- between
- describing actions

Useful trends can say things like “Needed Replay less often for two-step instructions this week.” A headline such as “723 questions answered” should not be the main measure of progress.

## Implementation direction
Establish a minimal shared progress event/storage layer immediately after the native content/session foundation, before many new native activities are built. The first version does not require the full parent dashboard.

Minimum early event data:
- stable skill ID
- outcome/result
- support level
- context ID
- session/event ID
- local persistence
- deduplication

Example:

```text
skill.spatial.under
independent_success
context = crocodile_bridge
```

Prefer shared native Kotlin models:
- SkillDefinition
- SkillProgress
- AttemptRecord
- ContextExposure
- ActivityResult
- ParentObservation

Every future activity should report progress through the same API so longitudinal data is collected from the start instead of retrofitted later.

## Implemented native event storage (schema 1)

The first implementation is `learning/progress`, separate from legacy WebView progress/recovery. It is an unwired foundation for native activities, not a dashboard or adaptation system.

- `AttemptEvent` records typed session/activity/content-version origin, task instance/definition, skill, context, difficulty, canonical chosen content ID, EN/DE language, correct/incorrect outcome, attempt number and Replay/hint/parent-help support. Retry count derives from attempt identity; support is not an incorrect outcome. No display text, personal profile, device identifier or response-time score is stored.
- `CompletionEvent` records final evidence for the session's five or ten quiz tasks, or four season-/seven weekday-ordering placement steps, including outcomes, attempts/retries and support. Completion is separate from correctness. Final evidence does not invent missing attempt history or assign the final screen language to earlier tasks. Abandonment/interruption is not represented as completion or failure.
- Attempt keys derive from session/task ordinal/attempt number; completion keys derive only from session ID, never delivery number. IDs must never be recycled. Identical delivery returns the original record; conflicting payloads or reused sessions with different activity/content versions fail explicitly.
- `StoredProgressEvent` adds a monotonically increasing acceptance sequence and injected-clock timestamp at first acceptance. Sequence, not wall-clock sorting, defines stable order when the clock changes. These are acceptance times, not precise interaction/reaction times.
- `ProgressRepository.append/read` returns typed success/failure. `ProgressQuery` supports combined skill/session/activity/event-kind filters, chronological or newest-first order and a bounded limit. Skill filtering includes relevant completion summaries; callers must not count summaries as extra attempts.

`FileProgressRepository` uses a deterministic binary snapshot, schema version 1, with a checksum and strict field/count/enum/reference-shape validation. The store is bounded to **10,000 events / 16 MiB**. Capacity exhaustion is explicit; no silent pruning occurs. Reads and writes reject corrupt, truncated, unknown-schema/record or extra-field data without partial results, reset or overwrite. There is no automatic schema migration yet; a future incompatible change requires an explicit tested migration. Historical semantic references remain readable without requiring today's content catalogue.

`AtomicProgressStorage` serializes read/deduplicate/write using process and file locks, syncs a temporary file, atomically replaces the data file, then syncs the directory. An abandoned temporary file is ignored. Failures before or after replacement are explicit; retrying the same event reconfirms durability without adding a record. `AndroidProgressRepository.create` supplies Android atomic rename/directory sync and private `noBackupFilesDir/native-progress` storage. Android excludes this location from [Auto Backup](https://developer.android.com/identity/data/autobackup); no cloud integration is added. Blocking repository operations belong on a worker thread. The bounded whole-file implementation is intentionally small; larger history/query needs should trigger a measured storage migration rather than raising bounds indefinitely or moving history into settings.

`SessionProgressRecorder` validates the existing reducer's `RecordAttempt` and `Complete` effects against the frozen plan. It maps persistence success (including duplicate success) to the current completion-delivery acknowledgement. Failure remains retryable with the original logical event key. The host dispatches acknowledgements to its serialized session owner; the reducer performs no I/O. Restored pending completion can explicitly retry with a new delivery number but the same stored completion identity. Restoring an acknowledged session does not emit completion again.

Integration is still required: hosts must retain failed attempt effects, perform worker-thread writes and coordinate checkpoints/delivery before discarding effects. Session snapshots are not a durable outbox; process death before an effect reaches storage can lose that undelivered attempt. This foundation does not claim crash-safe delivery of effects that were never submitted. No activity, reward persistence, retention UI, dashboard or legacy behavior is changed. Real Android process/filesystem and backup-policy acceptance remains outstanding.

### First delivery integration: native Prepositions

Native Prepositions now uses the repository through `SessionProgressRecorder`. Its activity-specific write-ahead journal atomically retains the next exact checkpoint and pending event before publishing an accepted answer/completion. The journal remains until idempotent delivery succeeds and the acknowledgement/removal is itself saved. Retry re-reads disk after an uncertain write. See NATIVE_ARCHITECTURE_SPEC.md for ownership, bounds and failure behavior. This resolves the undelivered-effect gap for accepted transitions in this route only; other future hosts still need a delivery contract. No dashboard, reward records or legacy import is added. Physical process interruption and normal signed-upgrade persistence checks remain required.

## Wilma delivery integration (2026-09-22)

Native Wilma recognition and before/after use the shared durable session host. Ordering emits the existing typed AttemptEvent per placement attempt and CompletionEvent with seven canonical placement records. The only event-model/codec extension accepts seven completed steps in addition to five/ten; schema-1 field layout, stable dedupe keys and existing records remain unchanged. Upgrade reads existing records without rewriting their meaning. Older app versions cannot read the newly permitted seven-step completion and fail closed; downgrade over such data is unsupported.

The ordering host retains both final-placement and completion effects before accepting completion and removes them only after deduplicated delivery and durable acknowledgement. Failed/uncertain writes are retryable on reopening/Retry with the same identities. Explore day taps are not progress; returning to Learn from active practice records support. No mastery classification, dashboard, rewards, date/device metadata or cloud behavior is added.


### Seasons cycle/ordering integration (2026-09-23)

Next/Before use the existing session recorder and durable host; Build the Year records existing typed attempt events and one completion with four canonical placement records. The model/codec now permits four completed steps as well as five/seven/ten. Schema-1 layout and dedupe identities are unchanged, and existing records remain readable. Older app builds cannot read the newly allowed four-step completion; downgrading over these records is unsupported. Ordered progress remains distinct from score/mastery; Replay/Help are support, not incorrect attempts. The activity-specific journal retains final placement and completion until durable acknowledgement, preserving the same IDs after restore/retry. Explore does not create correctness events.
