# Session handoff — 2026-09-22

## Current checkpoint — native Seasons, automated validation complete

Based on `main` at `e3bacd6`, which commits native Prepositions. Preserved the interrupted Seasons implementation and completed compilation, focused/unit/Compose/browser testing and documentation. These Seasons changes are **uncommitted**; do not commit or push automatically.

Home Days & Seasons now opens a small native chooser: Seasons Learn/Practise or the existing legacy calendar activity. Native Explore uses the four unchanged production PNGs and canonical EN/DE names/narration. Practice uses deterministic 5/10 rounds, shared state/audio, semantic choices, explicit Retry/Help, score locks and calm completion. Options/Back/recreation retain supported state; restoration is silent. Explore selection is a separate bounded checkpoint and creates no learning events; entering Learn during an unanswered quiz records help use.

The proven Prepositions journal host is now `DurableSessionHost`, with the original Prepositions adapter/path/schema preserved. Seasons keeps its own quiz journal and pending progress effect, retries uncertain writes with the same identity and deduplicates completion through shared native progress storage. Corrupt/incompatible files are retained with failure guidance/Home/legacy access. No new general queue/framework or platform behavior in the reducer/Compose.

Validation: **46/46 focused**, **188/188 full JVM**, **7/7 emulator Compose/navigation**, **55/55 browser**. assembleDebug/lintDebug passed; lint **0 errors / 10 warnings / 2 informational findings** (unchanged). Diff/added-file whitespace checks passed. BUILD_NOTES.md records exact commands, content/legacy audit, file inventory, APK hash and manual checks. All existing tests remain intact; season PNG bytes match HEAD.

Next: physically accept Seasons and Prepositions on Samsung S24 Ultra and Fire Max. Seasons checks include airplane mode, EN/DE and missing voices, OFF/Questions/All/Replay, portrait/both landscapes, large text/TalkBack/insets/image clarity, Options/Back, backgrounding, real process restoration, 5/10 completion and progress after restart/normal signed upgrade. Emulator audio was disabled; no physical acceptance claim. Pending Seasons quiz effects retry when its quiz is loaded or Retry is selected, not from a background Explore service. Foundation capacity/corrupt-file recovery and broader retention/migration remain follow-up work. Keep legacy routes until acceptance. Wilma, neural TTS, Follow the Instructions, dashboard and unrelated migrations were not started.


## Previous checkpoint — native Prepositions, automated validation complete

Based on `main` at `9b247e5`, with the earlier content/audio/session/progress foundations committed. Preserved the interrupted Prepositions work and finished the native slice. All current changes remain **uncommitted**; do not commit or push automatically.

Home Prepositions now opens Compose. The 24 animal/relation scenes, six canonical choices, EN/DE text, deterministic 5/10 rounds, stable choice order, shared reducer/score locks/support/retry, audio policy and exact session checkpoint are integrated. Options/system and visible Back/Home/re-entry preserve supported state; restoration is silent. Native drawing uses explicit relation geometry and temporary legacy animal glyphs. Explicit Retry and task-count completion follow the shared session/UX direction. The screen has an always-available legacy fallback; legacy source/assets/tests and mixed rounds remain unchanged.

`PrepositionsHost` durably saves the exact checkpoint plus at most one pending progress event before accepting a transition. A single worker delivers through the shared progress adapter/repository and saves the acknowledgement/removal. Failed/uncertain writes are retried by re-reading disk with the same dedupe identity; stale hosts cannot overwrite newer journal bytes. Save failure pauses further answers while Retry Save/Home/fallback remain usable. This closes the accepted-transition pending-effect gap for this activity only. Corrupt/incompatible journals are preserved and require recovery/legacy use; no silent deletion or broad migration/outbox framework was added.

Validation: **20/20 focused**, **167/167 full JVM**, **3/3 emulator Compose/navigation**, **55/55 browser**. assembleDebug/lintDebug passed; lint **0 errors / 10 warnings / 2 informational findings** (three new test-dependency update notices). Diff/new-file whitespace checks passed. Exact commands, test boundaries, APK hash, legacy audit and physical acceptance checklist are in BUILD_NOTES.md. No existing tests were weakened or removed.

Next: physical acceptance on Samsung S24 Ultra and Fire Max in airplane mode, including EN/DE installed and missing voices, OFF/Questions/All, Replay, all scene relations, portrait/both landscapes, larger fonts, insets, TalkBack, Options/Back, background/return, actual process recreation, 5/10 completion and progress after restart/normal signed upgrade. Emulator audio was disabled; no audible-quality or Fire compatibility claim is made. Temporary glyph artwork needs review/replacement before production art acceptance. The bounded progress store still has explicit capacity failure rather than retention/migration UI. No dashboard, Wilma/Seasons UI, Follow the Instructions, neural TTS or unrelated migration started. Legacy removal remains blocked on physical parity.

## Previous checkpoint — durable native progress foundation

Started clean on `main` at `148e756`; content, audio and session foundations are now committed. This task adds six source files and four test files under `learning/progress`, plus documentation. Changes remain **uncommitted**. Do not commit or push automatically.

The new typed attempt/completion repository records skill/context, stable task/content references, outcomes, attempts/retries and support without reducing progress to score. Attempt keys use session/task/attempt identity; completion keys use session identity, independent of delivery retries. Identical redelivery preserves one record, while conflicting payloads fail. Version-1 checksummed snapshots use locked atomic file replacement and sync, with a 10,000-event / 16 MiB bound and explicit failure results. Android storage lives in the private no-backup directory. Queries support skill/session/activity/kind, stable order and limits.

`SessionProgressRecorder` consumes existing pure effects against the frozen plan, persists on a caller-provided worker thread and returns completion delivery acknowledgements. No session/content/audio foundation or live legacy route was changed. See PROGRESS_TRACKER_SPEC.md for exact contracts and BUILD_NOTES.md for validation.

Validation: focused **31/31**, full native **147/147**, browser **55/55**; assembleDebug/lintDebug passed. Lint **0 errors / 7 warnings / 2 informational findings**. Diff and new-file whitespace checks passed. No existing test was changed or weakened.

Next: integrate delivery/retry/checkpoint ownership with the first migrated native host. Retain failed effects; a checkpoint is not a durable attempt outbox and cannot recover an effect lost before storage submission. Handle capacity/incompatible stores explicitly; future retention/schema migration and larger history storage remain work. No dashboard, rewards, Wilma/Seasons UI, Follow the Instructions, neural TTS or activity migration was started. Legacy session recovery remains separate. Physical Android storage/process/backup acceptance and existing device-specific gaps remain outstanding.

## Previous checkpoint — shared native choice-session framework

Based on `main` at `4c254c4`; the content and audio foundations are committed. Resumed and preserved the five session source files from the interrupted run, then completed the framework, tests and documentation. All current changes remain **uncommitted**; do not push automatically.

`learning/session` supplies typed identities, immutable choice tasks/plans/state, explicit reducer actions, 5/10 rounds, score locking, wrong-answer/retry policy, support tracking, deterministic bounded candidate generation and completion request/result effects. Canonical content IDs identify choices; separately authored EN/DE text supplies display/speech. Language/future settings do not replace active task identity/order. Full implementation contracts and bounds are documented in NATIVE_ARCHITECTURE_SPEC.md.

Checkpoint schema 1 stores the actual complete task plan plus progress, support/retries, index, versions and completion-delivery state in at most 100,000 bytes. It rejects malformed/incompatible data, restores without regenerating tasks and emits no old narration or completion. `LearningSession` uses the existing native audio controller through effects, cancels stale ownership, distinguishes manual Hint from Replay, preserves summary audio during completion acknowledgement and keeps speech callbacks out of learning logic.

Validation: focused **43/43**, full native **116/116**, full Playwright **55/55**; assembleDebug/lintDebug **passed**; lint **0 errors / 7 warnings / 2 informational findings**; diff/new-file whitespace checks passed. Exact commands, limits and APK identity are in BUILD_NOTES.md. No existing tests were changed or weakened.

No live route is migrated. Legacy MainActivity/ShellNavigation/WebView recovery, current content/audio foundations and assets remain unchanged. No UI, Room/progress persistence, broad settings rewrite, neural TTS, Wilma/Seasons activity or Follow the Instructions was started.

Next: minimal shared persistent progress events with attempt/session deduplication, then lifecycle/saved-state host integration and the first native migration under the roadmap. Completion effects describe one logical session completion, but explicit delivery retries require future repository deduplication; the framework does not claim exactly-once durable writes. Snapshot restoration is not a persisted attempt journal. Screen owners must suspend/close old session coordinators before replacing them, wire background/navigation cancellation and restore silently. Physical Samsung/Fire native lifecycle/process/audio acceptance and the older P0 device gaps remain outstanding.

## Previous checkpoint — shared native audio foundation

Started from clean `main` at `b6f0661`, which commits the completed content foundation. Added the separate native `audio` layer: pure policy/contracts/controller, authored content speech conversion and sanitizer, revocable owner/session/context tokens, fresh opaque request IDs, one pending/active request, response-priority replacement, cancellation and exactly-once terminal outcomes. No pure-layer Android or UI dependencies.

`AndroidSystemSpeechEngine` implements the platform boundary using application context, serialized main-thread callbacks, cached installed offline EN/DE voices, verified per-request selection and flush playback. Its pure system coordinator handles readiness, language/voice failures and stale callbacks. Shared JVM `FakeSpeechEngine` and fake-port tests provide deterministic readiness, request, cancellation and failure assertions.

Validation: focused **38/38**, full native **73/73**, full Playwright **55/55**; assembleDebug/lintDebug **passed**, lint **0 errors / 7 warnings / 2 informational findings**; diff/whitespace checks passed. Exact commands, limits and APK SHA-256 are in BUILD_NOTES.md. The source-of-truth policy is unchanged; VOICE_AUDIO_SPEC.md now documents the implemented contracts and required caller lifecycle behavior. BACKLOG.md distinguishes foundation implementation from integration/device acceptance.

Changes are intentionally **uncommitted**, with no push. Existing LegacySpeech/MainActivity, all legacy routes, content repository, assets and prior tests are untouched. No neural TTS, Voice Lab, Wilma/Seasons UI, Follow the Instructions, Prepositions migration or broad settings/session rewrite was started.

Next: shared session/settings/completion and minimal persisted progress foundations, then the first native migration under the existing roadmap. Native callers must open a fresh audio context for each task/phase/language, cancel ownership when leaving/backgrounding, close on disposal and restore silently with explicit Replay. Physical Samsung/Fire checks of this new adapter remain outstanding; no hardware compatibility claim from fakes. The wider physical P0 gaps below remain open.

## Previous checkpoint — first shared native content foundation

Continued the existing weekday implementation after HEAD advanced from `ba22b15` to `e6004e2`. Preserved the newer evidence-informed roadmap, Seasons narration/art guidance and all Wilma/Seasons assets. No pull/rebase/reset or activity migration was performed. Changes remain uncommitted for review; do not push automatically.

Pure Kotlin `learning/models` and `learning/content` now provide typed semantic IDs, required authored EN/DE text with separate display/speech, schema/revision metadata and an immutable deterministic repository. Bundled data contains seven canonical weekdays with cyclic previous/next and supplementary colour references, plus four seasons with exact roadmap descriptions and typed local image references. No Wilma artwork in weekday domain logic; no platform types or filesystem access in the repository. CONTENT_DATA_SPEC.md describes implemented contracts and error/version policies.

The six tracked `.DS_Store` files are staged for removal from Git; recreated local metadata was preserved and is ignored by `.gitignore`. No legitimate asset was removed or compressed. Existing routes/audio/UI remain unchanged. No neural TTS, Wilma/Seasons screen, Follow the Instructions or broad game work started.

Validation: focused **20/20**, full native **35/35**, full Playwright **55/55**, assembleDebug/lintDebug **passed**, lint **0 errors / 7 warnings / 2 information**, diff checks passed. Commands and APK identity are in the current BUILD_NOTES.md entry. The broader P1 item is split: this foundation is complete, while skill/vocabulary/grammar, scene/task, phonics and verb schemas/generators remain follow-up work for the first migration. Shared native audio/session/progress work and physical P0 gaps remain as documented below. The new repository is not wired to live activities yet; physical device validation was not repeated for this data-only change.

## Previous checkpoint — Samsung physical P0 checks and native landscape insets

Based on clean `main` at `6e44e10`. Physical S24 Ultra testing found that the native Options button overlapped the side navigation bar in landscape. MainActivity now uses top + horizontal safe-drawing insets. Physical measurements confirm Options/Back clear the navigation bar in portrait and both landscape directions, including system font scale 1.3.

The existing phone app had a different signing key, so it was preserved. Tests used a separate `com.bellfamily.bastischool.p0qa` package from a temporary checkout with only applicationId changed; repository package ID is unchanged. QA copy remains installed. Both packages' code/assets match apart from package identity; production-package upgrade compatibility was not established.

Samsung checks passed for quiz/lesson/library Options/Back, answered-state/score retention, rotation/font change, a confirmed background process kill and saved-task recovery, and five-/ten-question completion. The user confirmed clear English and German offline speech and Sound Off silence. Evidence names the actual Google system TTS engine, device/OS/configuration, tested APK hashes and limits in BUILD_NOTES.md. Original connectivity/display settings were restored. No user data was cleared.

Validation: full Playwright **55/55**, native unit **15/15**, assembleDebug/lintDebug **passed**, **0 lint errors / 7 warnings / 2 information**, diff check passed. No existing tests weakened. No neural TTS or animation/video work. Do not push automatically.

P0 remains open for physical Fire Max, Samsung gesture/accessibility and full content/audio/lifecycle matrices, missing voices and the differently signed production upgrade path. Do not interpret these targeted Samsung passes as exhaustive device acceptance. The software roadmap/P1 sequence is unchanged.

## Previous checkpoint — P0 native navigation/session recovery

Based on clean `main` at `a7dd258` (including completed/pushed audio, Prepositions, completion and Letters work). This task is local only; do not push automatically.

Audit confirmed native Options destroyed the learning WebView and always returned Home, and native route/session state had no recreation checkpoint. Options now retains one paused WebView and its origin. Saved-state recovery rebuilds the exact question/choices, score, answered/hint/retry state, completion or lesson return from a bounded versioned checkpoint. Language changes translate the same task; changing round length does not replace the active round. Resume is silent, and Back callbacks are coalesced/ownership-checked. Invalid recovery goes Home with a calm bilingual restart notice. Details and compatibility rules are in NATIVE_ARCHITECTURE_SPEC.md.

Validation: focused **24/24**, full Playwright **55/55**, native unit **15/15**, assembleDebug/lintDebug **passed**, lint **0 errors / 7 warnings / 2 informational findings**, `git diff --check` passed. Exact commands and APK hash: BUILD_NOTES.md, current navigation entry. Pure JVM tests cover shell route/Back/checkpoint policies; browser tests cover fresh-page checkpoint reconstruction, not Android framework recreation. No physical Samsung S24 or Amazon Fire Max testing was performed. Remaining P0 is physical acceptance: activity/Options/lesson/completion rotation, recreation and OS saved-task recovery, system/visible Back, rapid Options/Back, EN/DE offline audio, layout/insets/large text and accessibility. Force-stop/new-task recovery and durable skill history are outside this transitional checkpoint.

No animation/video assets, neural TTS or broad native migration were started. Existing learning and speech-safety tests remain. The wider priorities remain shared native content/audio/session models and minimal persistent Progress Tracker events, then the established migration/learning sequence. The historical next-task statements below describe their old checkpoints only.

## Previous checkpoint — P0 completion/reward layout

Local checkpoint: `fix: keep completion actions visible on small screens`, based on clean `ff57c8b`. Not pushed.

Ten stars now wrap as individual elements. Replay, Continue and Home appear before the reward area; optional balloons cannot cover the controls. Completion hides duplicate web/quiz chrome, resets scroll, and focuses the heading so keyboard navigation reaches the three primary controls before balloons. Continue restores the ordinary quiz layout and starts the existing new round; Home retains its native bridge behavior. Reward counts, audio policy, speech safety and the Prepositions fixes remain intact.

Validation: focused **4/4**, full Playwright **39/39**, Android build/lint **passed**, **0 lint errors / 7 warnings / 2 informational findings**. Unchanged native unit task UP-TO-DATE (**7 passed**). Tested both languages, 0/5–10/10 scores, six viewport sizes, large CSS text and reduced motion; manually inspected phone/short-landscape browser screenshots. APK assets match current source. Exact commands/hash: BUILD_NOTES.md.

Physical S24/Fire insets, system font scaling, orientations, touch/TalkBack and audio/lifecycle acceptance remain outstanding. No neural TTS or native migration started.

Next implementable P0: Letters display-case consistency and bilingual initial-letter/sound review. Keep letter names, written initial letters and actual phonics distinct. Native Options/session recovery follows; do not describe browser checks as native/device proof. Master roadmap and native migration priorities remain unchanged.

## Previous checkpoint — P0 Prepositions correctness

Local checkpoint: `fix: align preposition scenes and bilingual narration`, based on clean `231f875`. Not pushed.

The six legacy relations now share scene/reference metadata with English/German phrases. Questions narrate exactly the four displayed options in order. Correct feedback/hints agree with the actual rock, table, box or two rocks; German uses “auf dem Stein”, “unter dem Tisch”, “hinter dem Stein”, “neben dem Stein”, “in der Kiste” and “zwischen den beiden Steinen”. Scene accessibility text follows the same record. Narrow-screen next-to/between spacing and under-table placement are fixed.

Validation: focused **3/3** (48 bilingual relation/animal cases plus geometry at four viewport sizes), full Playwright **35/35**, Android assembleDebug/lintDebug **passed**, **0 lint errors / 7 warnings / 2 informational findings**. Native unit task remained UP-TO-DATE with **7 passing tests**. Browser scenes visually inspected; APK assets match source. Exact evidence/hash: BUILD_NOTES.md.

Physical S24/Fire scene, audio, touch and lifecycle checks remain outstanding. The audio policy/sanitization checkpoint is preserved. No neural TTS or native migration started. Product direction is unchanged.

At that checkpoint, the next P0 was completion/reward layouts (now addressed above), followed by Letters case/bilingual phonics and native Options/session recovery; physical validation remains required across these tasks. Native Prepositions migration and varied-context expansion remain later work.

## Previous checkpoint — P0 audio reliability

Local checkpoint: `fix: harden audio policy and TTS lifecycle`, based on clean `eb5b68e`. Committed locally only; not pushed. Preserves `f72c5f7` speech safety and `796359e` product direction.

Implemented in the existing legacy/system-TTS path:

- All: automatic questions/instructions/tutorials/lessons and feedback. Questions Only: instructional audio, no automatic praise/generic wrong feedback/completion summary. Manual Replay/options/Listen work in both. Off blocks all speech and balloon tones, including manual controls.
- Small Kotlin `LegacySpeech` helper with INITIALISING / READY / FAILED, at most one current pending request, flush/stop ownership, completion callbacks and cached installed offline EN/DE selection. Missing/network/uninstalled voices never fall through to a previous language. Parent Options offers voice-data/restart guidance.
- Owned cancellable JS narration timers; new requests, questions, feedback, navigation, language, completion and backgrounding invalidate obsolete speech. Completion Replay reads the summary. Native Back delegates to lesson-aware web navigation; departing WebViews are disposed.
- Versioned per-language tutorials marked only after current successful full playback. Interruption/failure/Off never consumes them. Durable native reset epoch works before a WebView exists. Old unversioned heard flags are deliberately not carried forward.
- Balloon tones reuse a lazy context; Off/background stops oscillators and suspends it.
- Explicit displayText/speechText praise and sanitizeForSpeech() remain intact, as does answer/speaker isolation.

Validation: focused **20/20**, full Playwright **32/32**, Kotlin unit **7/7**; assembleDebug/lintDebug **passed**, lint **0 errors / 7 warnings** (plus 2 informational findings). Android Studio JDK 21, bytecode target 17. Exact commands, warnings, APK hash and limitations are in BUILD_NOTES.md.

No physical Samsung S24 or Amazon Fire Max testing was performed. Remaining P0 audio work is physical acceptance: actual airplane-mode EN/DE output/quality, missing voices, startup readiness, silence, interruption and lifecycle/reset behavior. Browser mocks and pure JVM tests are not hardware evidence. No neural TTS, Supertonic, Piper, sherpa-onnx, Voice Lab, downloaded voices or personality packs were started.

At that checkpoint, remaining wider P0 included Prepositions scene/choice/feedback agreement (now addressed above); completion layout on narrow/short screens; Letters case and bilingual phonics review; native navigation/session recovery and physical lifecycle/layout/accessibility checks. Native Options currently returns Home and does not preserve an in-flight learning surface. Language changes regenerate the legacy current question without score changes; answered questions remain locked, including lesson return.

Continue the established product sequence after P0: shared native content/audio/session and minimal persisted skill events, Prepositions migration proof, Follow the Instructions, Vocabulary Booster, Tell Me!, shared Memory Pairs, progress dashboard, concept-focused maths and classroom skills. Today's Adventure, varied-context generalisation and Discovery Book priorities remain unchanged. Do not begin neural voice work as part of this checkpoint.

## Historical session context (2026-09-20)

Audit revision: `290ff0381e1208d512cd06e08131568fca5aee5b`; remote/CI identity was checked then, not reverified in this pass. See TECHNICAL_AUDIT.md for detailed findings. No physical S24/Fire device was attached; browser mocks did not establish real audio/native behavior.

The earlier session used `/home/paul/Projects/Basti_Learning_Adventure_V1_For_Codex`. Those paths and temporary artifacts below belong to that host and may not exist in another checkout. Prior concurrent Compose work and implementation authorization were session-specific; inspect git status/diff and source before new work.

## Historical tooling

- JDK: `/home/paul/.local/share/basti-tools/jdk17`
- Node tools: `/home/paul/.local/share/basti-tools/node-v22.14.0-linux-x64/bin`
- Browser tests: prepend Node tools to PATH, then `npm test -- --reporter=line`. Chromium needs execution outside the restricted sandbox.
- GitHub CLI: `/home/paul/.local/share/basti-tools/gh_2.101.0_linux_amd64/bin/gh`
- SDK: `/home/paul/Android/Sdk`
- Audit diagnostic scripts: `/tmp/basti-audit.cjs`, `/tmp/basti-audit-extra.cjs`; screenshot `/tmp/basti-completion-audit.png`. These are temporary evidence, not maintained tests; they may disappear.
- Sandbox DNS blocked GitHub; escalated read-only remote/CI checks succeeded. Do not mistake that for repository failure.

The audit was a review, not a completed fix pass. Do not repeat earlier claims that all animations teach their actions, all audio policies work, or all system-bar/device cases are verified.
