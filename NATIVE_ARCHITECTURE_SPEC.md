# Native Architecture Specification

## Role and scope

This document owns native boundaries, state ownership, persistence and migration gates. [MASTER_PRODUCT_LEARNING_ROADMAP.md](MASTER_PRODUCT_LEARNING_ROADMAP.md) owns product direction; [CONTENT_DATA_SPEC.md](CONTENT_DATA_SPEC.md) owns shared schemas; [UX_NAVIGATION_SPEC.md](UX_NAVIGATION_SPEC.md) owns navigation behavior; [TESTING_QA_SPEC.md](TESTING_QA_SPEC.md) owns verification. Domain behavior remains in [PROGRESS_TRACKER_SPEC.md](PROGRESS_TRACKER_SPEC.md), [GAME_DESIGN_SPEC.md](GAME_DESIGN_SPEC.md) and [VOICE_AUDIO_SPEC.md](VOICE_AUDIO_SPEC.md).

The target is fully native Android with Jetpack Compose runtime UI and native learning logic, audio, progress and rewards. The final architecture has no runtime WebView, HTML or JavaScript dependency. It is a single-user, fully offline app for Samsung S24 and Amazon Fire Max, with English/German and no Google Play Services requirement.

These are implementation requirements, not claims that the target already exists. Keep one app module initially; packages are conceptual boundaries, not a requirement for a framework, dependency-injection library or separate Gradle modules.

## Current baseline and migration

At Documentation Pass 2, [MainActivity.kt](app/src/main/java/com/bellfamily/bastischool/MainActivity.kt) owned Compose home/Options/top bar, enum navigation, remembered screen state, SharedPreferences, WebView hosting and Android TTS, while [app.js](app/src/main/assets/app.js) and [index.html](app/src/main/assets/index.html) owned all learning activities. That paragraph is a historical migration baseline, not current implementation reality.

Current source still uses the same hybrid shell, but shared native content/audio/session/progress foundations are implemented and four substantial learning areas now run end to end in Compose: Prepositions, Seasons, Wilma’s Week and Vocabulary Booster. Shared completion/action/support presentation and deterministic restoration/progress delivery are also implemented. The typed 81-scene Scene Description catalogue now also drives native Tell Me / Erzähl mal, an open-ended text/artwork conversation outside the quiz framework. Legacy HTML/JavaScript remains only for unmigrated routes and must stay usable until tested native parity exists. Physical S24/Fire acceptance remains tracked separately in TESTING_QA_SPEC.md.

Migrate incrementally from this current baseline. Add or extend shared foundations only for demonstrated consumers, preserve working legacy routes until parity is tested, and avoid broad rewrites. Follow the Instructions remains the first planned new reusable instruction/action **game**; Tell Me / Erzähl mal is a separate P2 expressive-language activity. BACKLOG.md owns execution order.

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

## Owner-local lazy native TTS (2026-09-26)

The four speech-owning native ViewModels pass an engine factory to DefaultAudioController.
The factory is invoked once, only for a current policy-eligible request with nonempty
sanitized speech. Construction, readiness reads, context changes, settings changes,
OFF/suppressed requests and unused disposal do not initialize native Android TTS.
Before creation, readiness remains INITIALISING; the first request is retained through
real engine initialization and plays without a second tap. Existing replacement,
owner/language/policy cancellation and stale-result checks govern that same request.

The engine stays local to its existing Activity-scoped ViewModel and is reused after
first use; close shuts it down only if created. Initialization/construction failure is
explicit and does not repeatedly allocate engines. The Application-only ViewModel
constructor remains available to AndroidViewModelFactory; the optional factory seam
supports owner-level tests without vendor TTS. No Compose, service, singleton, global
arbiter or cross-owner sharing is introduced. Legacy MainActivity/WebView speech is
unchanged: Home still creates its one legacy client, but no native clients. Up to four
native clients can accumulate normally after eligible use and remain until owner clear.
Cold first speech may wait for platform initialization; wording/policy/flow are unchanged.

## Observed TTS ownership audit (2026-09-26, `d94d581`)

This records the **pre-lazy baseline** at `d94d581`, not the current allocation behavior
or a new ownership design. The section above supersedes eager-construction findings.
Speech policy stays in VOICE_AUDIO_SPEC.md. No production code changed during the audit.

### Construction and lifetime

- `MainActivity.onCreate` eagerly obtains all four AndroidViewModels through
  `ViewModelProvider(this)`: Prepositions, Seasons, Wilma and Vocabulary. Each field
  initializer builds its activity-specific audio adapter → `DefaultAudioController`
  → `AndroidSystemSpeechEngine` → `SystemSpeechEngine` → private `AndroidSystemTtsPort`.
  The port's `initialise()` constructs `TextToSpeech(applicationContext)` immediately.
- `MainActivity.onCreate` separately constructs `TextToSpeech(this)` for `LegacySpeech`
  and the WebView bridge. These are the only two Android construction sites in source.
- **Five TTS client objects are normally alive per shell/ViewModelStore**, already on
  Home and even with OFF selected. This is not five separate vendor engine processes.
  There is no app-wide singleton or process-wide maximum: separate Activity stores
  can allocate their own set, and platform connection teardown is asynchronous.
- Native owners are Activity-scoped ViewModels, not destination-scoped Compose owners.
  Recomposition and ordinary route changes create no new client. Configuration
  recreation retains the four ViewModels; the old Activity releases its legacy client
  and the new Activity creates a replacement. Route exit/background cancels speech
  but deliberately does not close the retained native engines.
- `MainActivity.changeRoute`, `onPause`, `onResume` drive `setVisible`; Options hides
  every native activity. ViewModels increment an epoch and audio adapters revoke their
  owner/context. Return is silent. New tasks/phases/language/settings invalidate old
  worker/audio results; main-thread delivery checks visibility, epoch and language.
- Each ViewModel's `onCleared()` calls `audio.close()` → controller/engine/port close →
  stop + shutdown, clears cached platform voices and removes pending handler callbacks.
  Only application context is retained by native TTS. `MainActivity.onDestroy` stops
  legacy speech via WebView disposal, then shuts down/nulls its legacy TTS. Actual
  Android process termination relies on OS cleanup, not guaranteed onDestroy calls.
- No Listen composable owns a TTS object through remember/DisposableEffect. The shell's
  WebView DisposableEffect disposes that WebView and stops legacy speech; it does not
  own native ViewModel engines. Shared completion decoration owns no TTS either.

### Current user-triggered speech inventory

All native controls below call shell-wired ViewModel methods, then the four small
activity audio adapters and the same shared controller/system-engine implementation.
The shared implementation is reused; the engine *instance* is not shared.

| Surface | Controls and execution path |
| --- | --- |
| Prepositions | `replay` → action(Replay); `speaker-<id>` → option(id); How to play → introduction(); shared completion Replay → action(Replay). Tutorial success alone marks it heard, with reset/language/revision guards. |
| Seasons | Explore selection → select → canonical description; `seasons-replay` in Explore, quiz and ordering → replay(); quiz `speaker-<id>` and ordering `order-speaker-<id>` → option(id); shared completion Replay → replay(). |
| Wilma | Explore day selection → select → day name; `wilma-replay` for Explore/practice/order → replay(); quiz strip `wilma-speaker-<id>` and ordering `order-speaker-<id>` → option(id); shared completion Replay → replay(). |
| Vocabulary | Explore word selection → select → word; `vocabulary-replay` in Explore/practice → replay(); `vocabulary-example` → example(); FIND/NAME `speaker-<id>` → option(id); shared completion Replay → replay(). |
| Shared UI | NativeActionButton/NativeCompletionScreen render Replay and invoke callbacks only. NativeTextChoice invokes answer callbacks only; adjacent option speakers are independent. NativeSupportMessage is passive text. Answers may cause authored automatic feedback through reducers, not an option-Listen callback. |
| Home / Days & Seasons hub / Options | No Listen/audition control. Tutorial reset changes future tutorial eligibility, not immediate speech. Options audio-status text reflects the legacy engine; individual native screens expose their own failure state. |
| Legacy WebView | `app.js` qHeader Replay and completion Replay → audioGuidance.replay; answer/object speakers → speakOption; Verb Explorer lesson Listen → audioGuidance.say. All go through speak → Android.speak → MainActivity.AppBridge → LegacySpeech. Browser speechSynthesis is only the no-Android-bridge development fallback. |

### Language, queue and stale-result protections

Native and legacy caches prefer installed offline en-GB/de-DE, otherwise another
installed voice of the requested language; no intentional cross-language/network
fallback. Native requests select authored speech using typed ContentLanguage and set
and verify the cached voice before every utterance. Missing/failed selection yields
explicit failure and no speech. Native platform callbacks are posted to main and
checked against request/context identity; activity adapters add revision guards.
Installed voice changes require recreating the engine (an app restart, not merely a
Compose recomposition or retained-ViewModel configuration recreation).

Each controller/legacy helper retains at most one current/pending request. New Listen
or Replay replaces it; both Android paths use QUEUE_FLUSH, never QUEUE_ADD. Native
workers are serialized; busy controls reject extra work, and epoch guards drop stale
narration after route/task/language changes. Legacy JS cancels its owned 220ms timer,
checks shell/restore state and request serial, and the bridge checks current WebView,
WEB route, foreground and OFF. ALL/QUESTIONS semantics live in the shared native
controller and, separately, legacy JS; the bridge independently enforces OFF.
There is **no global cross-controller arbiter**; route visibility/cancellation makes
one destination eligible. Do not treat QUEUE_FLUSH as a global mutex across clients.

### Assessment and limits

- **Safe by inspected control flow and fake-engine tests:** recomposition does not
  allocate TTS; explicit route/background cancellation, silent restore, repeated
  activation replacement, native language checks, stale/duplicate callback rejection,
  terminal/idempotent controller close and native shutdown forwarding.
- **Confirmed resource characteristic, potential issue:** all five clients initialize
  eagerly, including unused activities and Sound Off. This is unnecessary allocation,
  not evidence of an unbounded leak or proven audible overlap. A bounded lazy native
  engine-creation follow-up could reduce it without a singleton/service redesign.
- **Potential legacy robustness gaps:** the bridge trusts setVoice's success code
  without reading back the selected voice, ignores stop's result, and does not check
  listener/rate setup results. Native code checks voice selection and treats failed
  stopping as terminal. No vendor failure reproducing wrong-language/overlap was
  observed; do not claim identical robustness or silently rewrite legacy behavior.
- **No confirmed playback/lifecycle defect found** in this audit. Retaining a stopped
  engine until ViewModel clearance is distinct from leaking a destroyed Activity.
  Native failures do not mutate score/progress; Prepositions tutorial completion is
  the intentional guarded speech-success side effect.
- Pure tests simulate ports, not Android service binding, vendor callbacks or real
  shutdown/resource counts. Existing Compose tests establish UI callback separation,
  route/recreation behavior and layouts, often with audio OFF; they do not establish
  audible cancellation. No new test was needed to duplicate these existing contracts.
  Physical EN↔DE, missing/offline voices, rapid Replay/navigation, background/recreate,
  actual stop latency and retained-resource behavior remain separate S24/Fire checks.

## Implemented native choice-session foundation (2026-09-22)

`learning/session` now provides a pure choice-question foundation, separate from the live legacy adapter. It does not migrate a screen or force sequence/placement/open-ended games into a quiz. One app module and ordinary constructor injection remain sufficient.

- `SessionModels`: typed session, activity, task-definition, task-instance, attempt, skill/context and completion-delivery IDs. Task instances are session + ordinal, distinct from reusable semantic definitions. A frozen `SessionPlan` owns the activity revision, content version, 5/10-task policy, complete ordered tasks and authored bilingual instruction/feedback/hint/completion text. Choices/correct answers use canonical `ContentId`, never display strings. Required content references resolve through `ContentRepository`; skill/context IDs have validated syntax but no full skill registry is introduced yet.
- `SessionReducer`: explicit answer, retry, hint, parent-help, Replay, Next, language and completion-result actions. State snapshots and their lists are immutable. Correct answers lock and score once. RETRY wrong answers wait for an explicit Retry without changing the task; LOCK wrong answers finish the task without deducting score. Attempt IDs reject repeated submissions; Retry identifies the wrong attempt; Next identifies the task being left. New settings affect future plans only. Language changes preserve semantic task identity, choice order, answer state and score and cancel old speech silently. This subset assumes equivalent authored task meaning in EN/DE; language-specific phonics needs a separate deliberate policy later.
- Replay and hints are requested support, not answer attempts. Support persists across retries; Replay after a locked answer cannot retroactively change independence evidence. Attempt effects carry event/task/session identity, primary skill, learning context, difficulty, language, chosen content, correctness and support. Audio success does not prove an answer or mastery. A future ViewModel can publish dispatched snapshots with lifecycle-aware observation; no Compose/ViewModel or platform lifecycle wiring is added here.
- Completing the configured round emits one logical `Complete` request on the final valid Next. Its durable deduplication key is the session ID, while a delivery number distinguishes acknowledgements/retries. Pending, acknowledged and failed delivery state remain separate from the completed learning phase. Explicit retry uses the previous delivery ID, so a duplicated retry action or old acknowledgement is ignored. Restoration emits no completion request. A host may explicitly retry an uncertain pending/failed delivery; the future repository must deduplicate by session ID. There is no claim of exactly-once durable writes without that persistence boundary, and no reward writes occur here.
- `CandidateTaskGenerator` accepts an injected seed and 1–256 finite reviewed candidates. It sorts by semantic task-definition ID, shuffles with a seeded JDK Random/Fisher–Yates sequence, visits a complete shuffled candidate cycle before repeating, and shuffles each task's choices once. Five/ten tasks require bounded work, with explicit rejection for empty, duplicate, missing-reference or invalid content. There is no unrelated fallback or rejection-sampling loop. Activities may instead supply a validated deterministic plan through the small generator contract. Generation is never called by rendering or restoration.
- `SessionCheckpoint` schema 1 is a platform-free binary value capped at **100,000 bytes**, not Java object serialization. It stores the complete task snapshot, content/activity versions, session/task IDs, ordered choices/correct identity, bilingual text, policy, language, current index, per-task answer/attempt/retry/support state, score consistency value and completion/delivery phase. Decode validates magic/schema, expected activity/revision/content version, references, count/string bounds, enum/boolean values, progress/score/phase consistency and trailing/truncated data. Malformed, incompatible and oversized inputs return typed rejection without partial state. No seed is rerun and no platform object/audio token is serialized. Bump the checkpoint schema for incompatible representation changes, activity revision for task interpretation/policy changes, and content revision for authored content changes. The host must offer a calm restart after rejection; no navigation is performed by the codec.
- `LearningSession` is a tested pure coordinator abstraction for reducer + audio ownership, but it is not the current production host used by the four live native activities. Production choice activities currently use `DurableSessionHost` for serialized checkpoint/progress delivery plus activity-specific audio adapters/ViewModels for narration and lifecycle ownership. Keep `LearningSession` as reusable foundation only where a concrete consumer benefits from it; do not assume a new activity must adopt it merely because it exists. In either pattern, audio cannot mutate answer/score/completion, obsolete owned speech is cancelled at task/language/navigation/background boundaries, and restore/resume is silent until explicit Replay or a new instruction. Native audio policy remains in VOICE_AUDIO_SPEC.md.

Checkpoint limits also constrain authoring: 2–8 unique choices per question, at most 1,000 UTF-16 code units per authored text field and 16,000 across a whole plan, and bounded semantic identifiers. Invalid plans are rejected before use; no truncation changes question meaning. Completed and answered checkpoints retain their locks and do not award again. Checkpoints are recovery snapshots, not a durable attempt journal: returned attempt effects must be delivered through shared persistent event storage, with host crash/retry handling, before activity migration acceptance.

Legacy WebView/session recovery below remains unchanged and separate. No Room, settings migration, UI, saved-state host integration or physical process-recreation proof is included in this foundation. Automated evidence and remaining Samsung/Fire checks are recorded in BUILD_NOTES.md.

## Persistence, progress and rewards

The first native persistence boundary is implemented in `learning/progress`: pure typed events/repository results and `SessionProgressRecorder`, a bounded deterministic file codec/repository, injected atomic file operations, and an Android factory using private no-backup storage. See PROGRESS_TRACKER_SPEC.md for schema, limits, deduplication, failure and delivery contracts. No dependency or live route is added. Reducers remain pure; native hosts must execute persistence effects off the main thread and retain failed deliveries. Legacy recovery stays separate. Whole-file storage is the initial bounded implementation; structured history growth can justify Room later.

Use DataStore for small settings such as language, audio mode, reduced motion, parent difficulty/session preferences and versioned tutorial status. Migrate relevant SharedPreferences/localStorage values deliberately: validate them, define precedence and record successful import so it is not repeated. Preserve existing preferences until import is verified.

Bounded transitional exception: the first support-text size preference reuses the existing shell-owned `basti_shell` SharedPreferences Boolean `largerSupportText` (absent = false), alongside current Options preferences. The shell supplies a presentation-only `LocalLargerSupportText` value to NativeSupportMessage; activities/reducers do not read storage. It is not mirrored to the WebView or serialized into learning checkpoints/progress. This avoids a second settings store or a broad migration for one Boolean; include this key in the eventual deliberate DataStore migration above. Existing SharedPreferences write semantics apply; this is not a durable learning-event queue.

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

## First live native route: Prepositions (2026-09-22)

The Home Prepositions card opens `ShellScreen.PREPOSITIONS`. Its Activity-scoped `PrepositionsViewModel` retains the round through Options/configuration changes; Compose renders immutable shared session state and sends actions. The shared pure reducer owns attempts, retries, support, score locks and completion. An explicit Retry control follows the shared RETRY policy; no deduction occurs. Task-count progress and calm completion replace prominent star/score presentation under UX_NAVIGATION_SPEC.md, while internal scores remain intact. Completion now uses the shared optional native celebration described below.

`PrepositionsHost` serializes reducer transitions and durable progress delivery on one worker. Before accepting a transition, it atomically writes the exact shared session checkpoint plus at most one pending attempt/completion event to a version-1 checksummed journal under `noBackupFilesDir/prepositions-session`. This bounded activity-specific journal (150,000-byte decode limit) closes the unsubmitted-effect gap for accepted native Prepositions transitions; it is not a general job framework. Progress is then delivered through `SessionProgressRecorder`/`ProgressRepository`, and a new journal removes the event and records completion acknowledgement. Failure leaves the pending identity retryable. Uncertain commits require re-reading the journal, never overwriting it from stale memory. Each atomic write compares the current bytes with the last loaded journal; an older host cannot overwrite a newer host’s accepted transition. A pre-commit failure does not accept the answer; a post-commit failure restores the committed task/effect. Further answers pause on failure while Retry Save, Home and legacy fallback remain available.

Options, Home and backgrounding cancel native owned audio. Returning/restoring is silent. Home retains the native round; re-entering resumes it, including after a cold launch. Play again is available after completion, uses current future-round settings and creates a fresh session ID. Saved Android route state restores the native destination/Options origin; the file journal owns task recovery. Missing journals start a new round; malformed/incompatible journals are preserved and present calm Retry/Home/legacy controls, rather than silently deleting evidence. Do not claim an interrupted unaccepted tap was a committed attempt.

The legacy `positions` WebView route, mixed rounds, legacy checkpoints, JavaScript and browser tests are retained unchanged. The native screen offers “Use previous version”; this starts the separate legacy route and does not translate native state into JavaScript. Other activities remain legacy. No shared audio/session reducer rewrite, Room, DI framework, network or broad migration framework is added. Physical S24/Fire parity remains required before removal of the fallback.

### Prepositions artwork and compatible content upgrade (2026-09-26)

New Prepositions rounds use activity revision 2/content 1.2. PrepositionsContent.restore
accepts only that contract or the original activity revision 1/content 1.1, then checks
all saved questions against the corresponding authored contract. DurableSessionHost has
one optional checkpoint-restorer argument; its default preserves strict existing decoding
for every other consumer. Journal/checkpoint schemas, pending-event verification, atomic
writes and stale-writer checks are unchanged. Valid old rounds preserve their exact
snapshot/version through completion; Play Again uses v2. Incompatible data is not deleted.

The ViewModel's existing worker loads the explicit scene PNG through a one-scene bitmap
cache at half linear resolution (724×543). Compose receives only the bitmap, shows the
complete 4:3 image with Fit and retains position-scene plus the localized description.
Language changes and recomposition do not decode again. Missing/corrupt data produces a
calm bilingual unavailable-picture message with the existing previous-version action;
it changes no answer, score, progress or audio policy. Failed loads are cached for that
scene's residence too; reopening the owner or changing scene permits a later attempt.
No generic image framework, runtime JSON parsing or asset modification is introduced.

## Second native route: Seasons (2026-09-22)

The Days & Seasons Home card opens a small native chooser: Seasons Learn/Practise or the existing legacy Days & Seasons route. Native Seasons Back returns to that chooser; chooser Back returns Home. Options retains its originating native surface. No Wilma placeholder activity is introduced, and legacy mixed/calendar behavior remains available.

`SeasonsViewModel` coordinates canonical season content, shared sessions/audio and worker-thread persistence. Explore selection and phase use a separate version-1 bounded (256-byte) atomic browsing checkpoint under `noBackupFilesDir/seasons-selection`; opening/selecting pictures creates no learning event. Practice uses 5/10 tasks, stable `season.*` choices, `skill.seasons.<season>.recognise` and one honest `context.seasons.lakeside_tree` context. Repeated scenes do not pretend to demonstrate generalisation across contexts. Entering Learn from an unanswered quiz records hint support without an attempt. Language does not change season/task identity or order. Production PNGs are decoded off main, cached at most two at a time and shown without cropping.

`DurableSessionHost` is the narrowly extracted Prepositions journal/delivery implementation. Injected activity identity/revision, repository, generator and authored-plan validator let Seasons reuse the same accepted-transition, pending-effect, stale-writer and retry guarantees. The Prepositions adapter preserves its existing path and schema; there is no journal migration or new job framework. Seasons uses its own `noBackupFilesDir/seasons-session` journal and the shared native progress repository. Pending quiz delivery is retried when that quiz is loaded or Retry is selected; browsing alone does not run a background delivery service. Corrupt/incompatible data is retained and reported, with Home/legacy access rather than silent replacement.

Explore selection explicitly requests canonical manual explanation speech; Replay requests the same authored narration. First opening/restoration is silent. Practice requests authored automatic instructions, policy-gated feedback and explicit Replay/help/option speech through the shared controller. OFF blocks all of these. Navigation/backgrounding/language changes revoke current ownership; callbacks cannot change learning state. No direct TTS or filesystem calls occur in Compose, and the pure reducer remains unchanged.

## Native Wilma’s Week (2026-09-22)

The existing Days & Seasons chooser now includes Wilma, Seasons and legacy calendar access. `ShellScreen.WILMA` has the same Options-origin and Back-to-chooser contract as Seasons. No legacy route is removed. `WilmaViewModel` is the single worker/main-thread owner; Compose sends actions and never accesses files/TTS. Original PNGs decode off main with in-memory sampling; files remain unchanged.

Explore stores selected semantic day and phase in a separate bounded version-1 browsing checkpoint (`noBackupFilesDir/wilma-selection`). Find Day and Before/After have independent `DurableSessionHost` journals (`wilma-find`, `wilma-relations`) and use unchanged 5/10 choice sessions. Leaving an unanswered practice task for Learn records help, without an incorrect attempt. Language changes preserve every identity and order. Resume/restore/retry is silent; explicit day/Replay or a new task can request shared policy-controlled speech. Owner revocation and runtime epochs reject stale audio callbacks.

Seven-day ordering has a small pure `WilmaOrderState`/reducer. Its saved actual scramble contains each weekday exactly once; the placed prefix derives from correct steps in canonical order. An action carries its original task/attempt identity, preventing stale/double placements. Wrong attempts retain all placed days, offer explicit Retry and record support separately. A completed week is seven placement steps, with no change to the shared quiz round lengths.

`WilmaOrderHost` applies the proven write-ahead pattern to this specific state. Its checksummed schema-1 journal (`wilma-order`, maximum 32,000 bytes) records content/activity versions, session/language, scramble, seven answer/attempt/retry/support states, completion acknowledgement and at most two pending progress events: the final placement and completion. The state/effects commit before publication; pending delivery blocks more learning actions, retries read disk first and compare last-loaded bytes to prevent stale-writer overwrite. Shared progress keys deduplicate uncertain delivery. Completion persists once; restore emits no narration or fresh completion. Corrupt/incompatible files remain preserved with Retry/Home/legacy access. No generic queue, ordering framework or quiz-reducer extension is introduced.

Today/Yesterday/Tomorrow is deliberately deferred; it needs an explicit anchor interaction and authored teaching support. There is no clock-derived learning answer. Physical segment/accessibility/voice/process acceptance remains outstanding. The owner has since proven the S24 same-key upgrade; Fire Max upgrade acceptance remains outstanding.


## Native Vocabulary Booster starter slice (2026-09-22)

The existing Learn card now opens `ShellScreen.VOCABULARY`; Back returns Home and Options retains that native origin. Other native and legacy routes remain unchanged. `VocabularyViewModel` owns one serial worker and shared-audio owner; Compose only renders and sends actions. No new framework or foundation rewrite is required.

Explore stores selected semantic item and phase in `noBackupFilesDir/vocabulary-selection`, a bounded version-1 browsing checkpoint, without progress events. Find the Word combines spoken/written word-to-picture recognition; What Is It? provides picture-to-word naming. Each uses the unchanged `DurableSessionHost` with a separate journal (`vocabulary-find`, `vocabulary-name`), frozen 5/10 plan, attempt/support state and exact task restoration. Switching modes preserves both rounds. Help reveals names on Find picture choices; entering Explore from an unanswered round records help separately from attempts.

Progress effects are saved with the next checkpoint before publication and delivered through the shared repository. Failed or uncertain writes pause learning actions; retry reloads the journal with the same deduplication identity. Loaded journals are retried together; an unopened practice journal is loaded/retried when that mode is entered. No background delivery service is added. Corrupt/incompatible files remain untouched with Retry/Home available; broader repair/retention policy remains a follow-up. Completed rounds retain their acknowledgement and cannot award twice after restore.

Shared audio receives authored words, examples, questions and feedback. Language/mode/navigation/background boundaries cancel owned speech. Restore/retry is silent until an explicit Listen/Replay or new task. The system engine's matching offline-language requirement remains unchanged. Vocabulary has no WebView, direct TTS or filesystem work in Compose/reducer; no remote services, new engine or neural speech work.


## APK identity and upgrade boundary (2026-09-23)

`com.bellfamily.bastischool` is stable across debug and release. Debug uses the ordinary machine-local SDK key and is not the distribution identity. Physical distribution uses the existing release build type with externally supplied persistent signing credentials and an explicit increasing version code; missing credentials/version fail before release packaging. CI allocates bounded run/attempt codes and only exposes private signing to manually requested main-branch distribution after validation. Setup/version-allocation details live in README.md, and install-over-data acceptance lives in TESTING_QA_SPEC.md.

Same-key upgrades retain Android app-private storage, including no-backup native journals/progress, subject to compatible app schemas and device validation. Uninstall erases that storage; different certificates cannot be fixed by version changes. This work adds no key rotation, cross-signature data migration/export, storage schema change or cloud backup. Never wipe progress merely to make an incompatible APK install.


## Shared native completion presentation

`ui/common/NativeCompletionScreen` pins Play again/Home/Replay above an optional scrollable reward area. Prepositions, Seasons Practice, Wilma practice/ordering and Vocabulary practice share it; Explore does not enter this boundary. Activity hosts retain responsibility for durable completion and failed-effect retry.

`CelebrationState` owns exactly five stable in-memory slots with deterministic semantic animal assignments per completion ID. Only FLOATING → REVEALING → SETTLED is permitted. It emits no learning actions or progress events and is never included in a checkpoint. Composition disposal/process recreation may reset this optional presentation without changing the restored learning result. Wilma's finished ordering remains available below the reward.

`CelebrationArtViewModel` decodes five existing canonical animal PNGs on a worker, using power-of-two sampling to at most 200 pixels per dimension, once per Activity-scoped owner. Compose receives the resulting bounded image map through `LocalCelebrationArt`; it performs no asset I/O. Images are fitted, not cropped; semantic EN/DE names reuse Vocabulary content. A missing image falls back to its localized name, not a replacement glyph. The files in the canonical animal library remain unchanged.

One shared float clock and at most five bounded reveal animations/eight local confetti pieces per reveal remain inside clipped reward bounds. Backgrounding and Android animation-scale zero stop/simplify motion; leaving composition disposes animations. The host owns optional `CelebrationSound`/`PopSoundEngine` lifecycle separately from speech (VOICE_AUDIO_SPEC.md). No persistence schema, session reducer, engine dependency or distribution-signing contract changes.


## Seasons cycle progression and sequencing reuse (2026-09-23)

The original `activity.seasons` recognition plan, revision and `seasons-session` journal remain compatible. NEXT and BEFORE are separate `activity.seasons.next` / `activity.seasons.before` five/ten-question sessions using the existing `DurableSessionHost`; their journals are `seasons-next-session` / `seasons-before-session`. Each mode retains its own exact round when switching modes. The selection checkpoint still uses schema 1 and stores enum names; old EXPLORE/PRACTICE selections read unchanged. Restoration/retry/language synchronization is silent and never regenerates a task.

`SeasonsOrder` uses four activity-specific placement steps with authored bilingual prompts, attempt/support/retry state, deterministic scramble and explicit spring-as-display-start teaching. `SeasonsOrderHost` applies the existing Wilma write-ahead pattern in a separate bounded checksummed journal (`seasons-order-session`, magic SOR1, activity/content versions). State and up to two final-placement/completion effects commit before publication; stale writers fail, pending delivery blocks new attempts, retry rereads before idempotent delivery. No filesystem/audio work occurs in Compose or the reducer. Broken journals are preserved, with Retry/Home available; no destructive automatic recovery.

Only `OrderedPlacement` is extracted into `learning/sequencing`: canonical-set/step validation, deterministic non-identity scramble and one accepted placement transition. Wilma and Seasons both use it. Their authored prompts, task/skill IDs, action envelopes and journals remain activity-specific; Wilma's IDs, seed behavior and serialized seven-step format do not change. This is not a generic game engine, queue or new session framework.

The Seasons host supplies bounded cached canonical images off-thread. Relation questions show the anchor season, not the correct answer's illustration. Ordering uses accessible tap-to-place cards and retained placed cards. All completed modes use the existing shared celebration/actions; Explore does not. Reward state remains non-durable and independent of progress.

Wilma's placed-day strip follows growth with a cancellable 400ms horizontal scroll after layout. Initial/recreated presentation and viewport-size changes position at the growing end without a scroll animation; wrong attempts/recompositions do not initiate movement. This is presentation only, independent of ordering state/checkpoints. Explore/Find/Before-After strips retain their prior manual-scroll behavior. No general navigation/button redesign.

## Scene Description content boundary (implemented 2026-09-25)

Tell Me uses a pure `learning/scenedescription` catalogue;
see CONTENT_DATA_SPEC.md for identities, language availability and query behavior.
A standard-library authoring script validates manifest-selected source data and emits
checked-in Kotlin, matching the existing bundled-content approach without adding a
runtime JSON dependency. The general ContentRepository and current activities are unchanged.
Platform hosts resolve LocalImageAsset paths and decode off the main thread;
Compose must not parse metadata. The content foundation itself owns no platform loader,
ViewModel, route, session, progress, speech or grading behavior. Authoring prompts/references never enter
the generated production catalogue. Source paths/hash provide reproducible validation,
not a new persistence mechanism.


## Tell Me open-ended consumer (2026-09-26)

`learning/tellme/TellMeFlow` uses `BundledSceneDescriptions.repository()` directly.
`TellMeState` holds category, scene index, TALK/MODEL/COMPLETE stage and two support
expansion flags. It has no answer, attempt, score, skill result or progress event.
Continue/Next carries the expected scene and stage so obsolete activations cannot skip
pictures. Selection preserves manifest order; language is a render input, not task identity.

The Activity-scoped `TellMeViewModel` retains conversation state across configuration
recreation, Options and background return. Home/back-to-Home resets it; category selection
and Again are explicit. There is deliberately no durable conversation checkpoint: a new
process starts at category selection, even if the shell restores the Tell Me destination.
No ChoiceTask, SessionReducer, DurableSessionHost or progress schema is involved.

A Tell-Me-specific single-worker loader resolves typed local image paths, reads bounds,
and power-of-two samples to at most 1024 pixels per dimension. It caches one current scene
(including a failed decode), closes streams, and posts results only for the current request
epoch. Home/replacement/disposal invalidates stale results; owner disposal shuts the worker
down. Compose receives a bitmap, fits its full composition within the available viewport height
(at most 420dp), and does no asset or metadata I/O. Missing
art is passive localized text; it never advances the conversation. The catalogue contains
no authored visual-alt field, so the localized title is the minimal image semantic label.

Tell Me v1 has no speech contract and creates no speech engine, microphone path or TTS
request. Authored teaching/display data is not silently treated as speech-ready content.
Legacy and other native audio/session/progress ownership remains unchanged.
