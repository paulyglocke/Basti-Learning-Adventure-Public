# Native Architecture Specification

## Role and scope

This document owns native boundaries, state ownership, persistence and migration gates. [MASTER_PRODUCT_LEARNING_ROADMAP.md](MASTER_PRODUCT_LEARNING_ROADMAP.md) owns product direction; [CONTENT_DATA_SPEC.md](CONTENT_DATA_SPEC.md) owns shared schemas; [UX_NAVIGATION_SPEC.md](UX_NAVIGATION_SPEC.md) owns navigation behavior; [TESTING_QA_SPEC.md](TESTING_QA_SPEC.md) owns verification. Domain behavior remains in [PROGRESS_TRACKER_SPEC.md](PROGRESS_TRACKER_SPEC.md), [GAME_DESIGN_SPEC.md](GAME_DESIGN_SPEC.md) and [VOICE_AUDIO_SPEC.md](VOICE_AUDIO_SPEC.md).

The target is fully native Android with Jetpack Compose runtime UI and native learning logic, audio, progress and rewards. The final architecture has no runtime WebView, HTML or JavaScript dependency. It is a single-user, fully offline app for Samsung S24 and Amazon Fire Max, with English/German and no Google Play Services requirement.

These are implementation requirements, not claims that the target already exists. Keep one app module initially; packages are conceptual boundaries, not a requirement for a framework, dependency-injection library or separate Gradle modules.

## Current baseline and migration

At Documentation Pass 2, [MainActivity.kt](app/src/main/java/com/bellfamily/bastischool/MainActivity.kt) owns Compose home/Options/top bar, enum navigation, remembered screen state, SharedPreferences, WebView hosting and Android TTS. [app.js](app/src/main/assets/app.js) and [index.html](app/src/main/assets/index.html) own all seven quiz modes, 53 Verb Explorer lessons, tutorials, scoring/completion and animations. Settings are mirrored into web state/localStorage. There is no shared native session engine or persistent skill tracker.

Migrate incrementally: stabilise known correctness issues, introduce only the shared foundations needed for one activity, and prove a native Prepositions slice before broadening. Follow the Instructions remains the first new native **game**, per GAME_DESIGN_SPEC.md; this does not displace Prepositions as the first migrated learning activity. BACKLOG.md owns execution order.

Keep legacy routes usable during migration. An adapter may translate old IDs/settings into native contracts, but new native activities must not call JavaScript for learning logic. Do not delete legacy DOM, data or initialization merely because a native shell hides it; other routes/tests may depend on it.

## Responsibilities and dependency direction

| Conceptual area | Responsibility |
| --- | --- |
| `app/navigation` | Destination/back stack and route IDs; wire screen owners and dependencies. Activity owns platform/window integration, not quiz rules. |
| `learning/models` | Immutable domain types, stable content/skill/task IDs and result contracts; independent of Compose, Activity, WebView and storage engines. |
| `learning/content` | Validated bundled content and deterministic task generators; resolve canonical content through a repository. |
| `learning/session` | Learning activity state transitions: current task, attempts/support, retries, score, session phase and completion request. |
| `learning/progress` | Shared local progress repository, attempt history and summaries; classification/adaptation follows PROGRESS_TRACKER_SPEC.md. |
| `audio` | Shared controller, policy, owned requests and SpeechEngine implementations; engine selection and quality follow VOICE_AUDIO_SPEC.md. |
| `games` | Separate small state machines for each mechanic; share content/audio/progress/rewards, without forcing every game into a quiz reducer. |
| `rewards` | Persistent unlock eligibility and deduplicated awards; completion presentation does not own award writes. |
| `ui/common` | Reusable instruction/replay, answer/audio, hints, feedback, completion and adaptive layout components. |
| `ui/activities` | Compose rendering and screen ViewModels/state holders; translate user actions into session/game events. |
| `legacy` | Temporary WebView host/bridge, settings/content adapters and explicit disposal. No dependency from pure native domain logic into this area. |

A shared SettingsRepository owns validated preferences; it may sit beside app wiring rather than requiring another module. Repositories hide storage/content loading. UI calls state holders; state holders coordinate pure session/game logic and repository/audio interfaces. Platform adapters implement those interfaces. Domain code must not depend on rendering or database details. Use ordinary constructor injection and test doubles where useful.

## State and generators

- Expose immutable UI state through StateFlow or an equivalent observable state holder, collected with lifecycle awareness. UI renders state and sends explicit events.
- Use a screen/session-scoped ViewModel for state that must survive configuration changes. Local visual state can remain in Compose. Do not retain an Activity, View or WebView in a ViewModel.
- Keep the current task, ordered choices, answer state and support flags stable across recomposition. Never generate a new question or write progress as a rendering side effect.
- Make generators deterministic for a given content version, parameters and injected random seed/source. Inject a clock where recency or timestamps affect decisions. Bounded candidate selection must report invalid content, not loop indefinitely or silently substitute unrelated items.
- Use stable semantic IDs from CONTENT_DATA_SPEC.md. Create a session ID once at session start, task-instance IDs within it and attempt-event IDs for submissions. A template ID alone does not identify a repeated question attempt.
- Handle rapid/repeated events in the reducer/state machine. A solved task cannot score twice; Replay is not an answer attempt; hints and parent help remain observable support.

## Persistence, progress and rewards

Use DataStore for small settings such as language, audio mode, reduced motion, parent difficulty/session preferences and versioned tutorial status. Migrate relevant SharedPreferences/localStorage values deliberately: validate them, define precedence and record successful import so it is not repeated. Preserve existing preferences until import is verified.

Use Room when structured attempts, context history, progress queries and durable rewards require it; do not put an ever-growing history blob in settings. Bundled content can initially be typed Kotlin records or validated local data files. No remote content service is required.

Every new/migrated activity reports through the shared progress API, with skill IDs, language, context, difficulty and support metadata. Completion score is not evidence of independent mastery. A small persistent progress foundation is sufficient initially; a full dashboard/adaptive scheduler need not block the first migration.

Progress writes must deduplicate by event ID; completion by session ID; rewards by session ID plus reward ID. Persist completion and awards atomically where they share storage, or use a durable retryable operation with uniqueness constraints. Retrying after interruption must neither lose recorded completion nor award twice. Navigation/recomposition must never be the sole trigger for persistence. Expose pending/failed writes for safe retry rather than silently claiming saved progress.

Keep learning history local, without accounts or analytics. Before storing it, review Android backup behavior (the current manifest permits backup) against the local-only requirement and exclude learning records from cloud backup.

## Lifecycle and session recovery

- Rotation/window resizing preserves task identity, choices, score, attempts and current lesson return target. Manifest configuration handling alone is not proof of recovery.
- Leaving a destination or backgrounding cancels its pending/active speech, delayed events and decorative animation work. Use request ownership/session identity to ignore stale callbacks. Pause visual motion with lifecycle; resume from logical state without replaying awards.
- On return, show the current instruction and Replay without draining an old speech queue. Tutorial completion should reflect successful playback, with activity/language/version identity; durable reset must work before a learning screen exists.
- Save small route/session recovery identifiers with saved state; store durable results in repositories. Do not try to serialize platform objects.
- For early migrations, full mid-task process-death recovery may be deferred. Declare and test the policy: restore a compatible checkpoint when available, otherwise return to Home with a calm restart option. Already committed progress/rewards must survive and remain deduplicated. Do not claim an interrupted task was completed.
- Later checkpoints should include content/schema version, task identity or sufficient deterministic inputs, phase and support state. Incompatible checkpoints must safely restart rather than reconstruct a different question under the same attempt ID.
- During transition, give each WebView an explicit destination lifetime and release it when no longer needed; cancel bridge work and pending narration before disposal.

### Current legacy recovery implementation (P0)

The shell now owns a small `ShellNavigation` route with an Options origin. Options retains one hidden, paused WebView; Home disposes it. Both Back controls delegate to the lesson-aware legacy path. A pending Back is coalesced, and Options taps are ignored until that Back settles; owner checks and a revision gate reject obsolete callbacks.

The legacy bridge synchronously publishes a bounded JSON checkpoint into a per-WebView mailbox. `onSaveInstanceState` saves that checkpoint and the native route without waiting for asynchronous JavaScript evaluation. On recreation, a fresh WebView restores the compatible checkpoint silently. It includes the activity/round order, question index, score, answered/feedback state, lesson return target, screen and question-generation random draws/current number range. The selected verb record retains its load-time distractors. This is a transitional adapter, not the future native session engine or durable Progress Tracker.

`LEGACY_SESSION_VERSION` covers both the schema and generator/content compatibility: bump it when generation draw order, indexed lesson content or saved field meaning changes. Checkpoints are limited to 100,000 characters. Missing, oversized, malformed or incompatible recovery returns to native Home with bilingual restart guidance. Saved Android task state is the recovery boundary; force-stop/new-task recovery and persisted learning history are not provided. Optional balloon animation/pop timing is not durable session state. Changing language translates the same question and preserves score/answer lock; round/range changes affect subsequent generation. Return/restoration never automatically replays narration or consumes a tutorial. Real device recreation/process/Back acceptance remains required.

## Legacy removal gate

For each route, record a parity checklist and evidence under TESTING_QA_SPEC.md before removal:

- All relevant content and English/German behavior work, including lesson return and settings.
- Audio policy, speech-safe text, Replay, tutorial playback/reset and cancellation pass.
- Questions/scenes/feedback agree; answers, retries, support and scoring are correct.
- Completion, Continue/Home and shared progress/reward integration work without duplicate writes.
- Compact phone and tablet layouts, portrait/landscape, large text, short height and insets pass QA.
- Configuration change and the declared process-recreation policy pass; required physical S24/Fire checks are recorded.
- Relevant native tests pass; legacy Playwright coverage remains until equivalent behavior has native coverage and the legacy route is retired.

Parity preserves useful functionality, not known bugs. Record deliberate fixes and explain any behavior change. Remove the final WebView/bridge/assets only after all routes, data imports, content loading and audio dependencies are native and tested. No runtime migration is performed by this specification pass.
