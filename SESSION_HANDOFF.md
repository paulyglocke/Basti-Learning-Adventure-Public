# Session handoff — 2026-09-23

## Current checkpoint — Wilma day-colour usability cue

Started on clean main `d0a8c94`, newer than the requested `702f00f`; preserved the committed Seasons action-button adoption. Physical child-use finding: generic weekday choices were confusing. Wilma selectable day-strip labels (Explore/Find/Before–After) and ordering choices now reinforce the established weekday colours. No prompt, correctness, generation, sequencing, restore, auto-follow, audio, progress, navigation, celebration or language-string changes.

`ui/wilma/WilmaDayColours` is a small UI-only palette keyed through CoreContent's existing weekday colourCue. No existing RGB constants were present; swatches were sampled from the committed Wilma segment PNGs, with no image changes. Black text is used on green/red/yellow/blue/orange/pink; white on purple. Minimum active contrast 5.05:1. Disabled cue is 25% over theme surface with recalculated contrasting text; disabled semantics/callback guards and existing Material/selectable focus/press/selection treatment remain. Placed labels, head/tail and Listen controls are unchanged.

Focused validation: 17 JVM and 11 Wilma emulator tests passed, including existing WilmaFollowTest. Final combined ui.common/Wilma suite: 20 passed. Full validation: 265 JVM and 39 emulator tests passed, zero failures/errors/skips; debug build passed; lint 0 errors/3 warnings/2 informational; diff/new-file whitespace checks passed. New pixel assertions wait for rendered resting fills (native feedback can outlive the Compose test clock), with palette tolerance and focus/activation coverage preserved. An intermediate existing Compose startup failure passed after an emulator-only fresh boot; no existing test changed. See newest BUILD_NOTES for exact commands, intermediate evidence and rendering limits. Tests compare rendered colour against the shared palette rather than duplicating RGB literals, and verify all seven labels, disabled/callback/keyboard behavior, German 1.5× portrait/short-landscape and wrong/retry/placement behavior.

Physically re-test with Basti to confirm the cue resolves the reported confusion; automated checks cannot establish learning benefit. Also check device colour distinction, readability, disabled/pressed/focused appearance, TalkBack and orientation/large-font behavior on S24/Fire Max. Prior accepted learning/auto-follow/artwork checks remain closed absent regression. No commit/push; no unrelated feature started.

## Previous checkpoint — shared action buttons adopted in Seasons

Started clean at `702f00f`. Only Seasons non-answer action presentation changed: Replay in Explore/quiz/order, Help and attempt Retry in quiz/order, load/save retry, Next and Home. Uses the existing NativeActionButton unchanged: SECONDARY for support/retry, PRIMARY for Next, NAVIGATION for Home. Ten call sites; all labels/callbacks/enabled expressions/test tags and existing modifiers retained. Help/retry now inherit the shared available-width text geometry and 56dp minimum; scroll hierarchy remains unchanged.

Answer/season/order cards, per-choice speaker buttons, FilterChips, placed cards, Days & Seasons hub, activity logic/state, audio/navigation/progress, completion celebration and canonical artwork are unchanged. No signing/version/distribution changes. No commit/push.

Four new Seasons UI tests cover actions/support/attempts, keyboard Replay, busy/save/image/language gates, order Retry/Help and disabled Next, plus German 1.5× text in portrait and both short landscape orientations. Validation: 21 focused tests (12 Seasons + 9 ui.common), 264 JVM and 36 full emulator tests passed, all zero failures/errors/skips. Debug build passed; lint 0 errors/3 warnings/2 information; diff/new-file whitespace checks passed. Extra emulator-only rendering run: 4 passed; reviewed German 1.5× short-landscape Next/Home captures. See latest BUILD_NOTES for exact commands and rendering limits. Shared completion and Seasons are now the two bounded consumers; do not expand into other activities automatically.

Prior S24 Seasons/Wilma/art acceptance remains closed absent regression. New button styling still needs physical visual/touch/accessibility checks on S24 and Fire Max; this is not acceptance of the wider P0 matrix. Suggested next bounded P1 slice: adopt suitable non-answer actions in Prepositions only, preserving all behavior; no new support presentation/settings or card redesign.

## Previous checkpoint — first P1 action-button slice

Started on clean main `3c52a1c`. `NativeActionButton` introduces presentation-only PRIMARY (filled), SECONDARY (tonal) and NAVIGATION (outlined) roles using the existing Material3 theme. Adopted only inside `NativeCompletionScreen`: Play again, Home, Replay and save retry. No activity/shell, learning, content, audio, persistence, navigation, reward-state, artwork or signing/version changes. No commit/push.

Buttons have 56dp minimum height, rounded 16dp corners, consistent padding, centered semibold labels that fill the allocated content width and wrap without a fixed line cap. Material owns semantics/focus/press feedback; no custom motion. Existing completion slots/tags/callbacks/enabled state and pinned reward separation remain. The new large-font tests caught mismatched paragraph/text measurement; aligning text width fixed it without weakening overflow assertions.

Validation: 9 focused Compose, 264 JVM and 32 full emulator tests passed (0 failures/errors/skips); debug build passed; lint 0 errors/3 warnings/2 information; diff/new-file whitespace checks passed. An extra emulator-only render run passed all 4 new tests. Reviewed German 1.5× text at 320×480dp and 700×240dp: no clipped labels or reward overlap. See the latest BUILD_NOTES entry for exact commands and file inventory. New styling still needs physical visual/accessibility review on S24/Fire; prior accepted Seasons/Wilma/artwork behavior remains accepted. The broader P0 acceptance matrix is unchanged. The component is available for later constrained-width adoption; next bounded visual slice could adopt Replay/Help/Retry in one ordinary activity, without changing support behavior or starting support settings.

## Previous checkpoint — P0 acceptance ledger updated, P1 planned only

Main inspected at `3efcb04` with a clean tree; implementation is committed in `d3ec6a9`. This pass changes documentation only, with no commit/push, code/assets/signing/versioning changes or new roadmap work.

Owner physically verified on S24 after the latest stable install: Seasons Explore, What Comes Next, What Comes Before, Winter → Spring and Spring ← Winter boundaries, Build the Year, wrong choices retaining correct placements, partial restore, completed restore, completion actions/celebration, and Wilma ordering auto-follow. These are closed unless a regression appears. Previous same-key upgrade and cleaned-art acceptance remain valid.

Remaining S24: both landscape orientations for these new flows, larger font, TalkBack/full accessibility, detailed EN/DE ALL/QUESTIONS/OFF and Replay sweep, pop-volume judgement, true process recreation beyond normal return/restore, and the full airplane-mode/offline-voice matrix. Fire Max still needs the concise checklist in TESTING_QA_SPEC.md. P0 implementation is in place, but the trusted-daily-build exit gate remains open until the remaining device evidence is supplied; do not label P0 complete or retire legacy fallbacks.

The regression run already started before this documentation request finished: 264 JVM, 28 emulator and 55 browser tests passed; debug build passed; lint 0 errors/3 warnings/2 informational. No repeat full run was started for these doc edits. See BUILD_NOTES.md for commands and evidence limitations.

Recommended first P1 slice (not implemented): a small shared action-button style/role component used only by NativeCompletionScreen, benefiting all four current activities. Reuse Material3/theme/ripple and the existing pinned responsive layout. Preserve callbacks, tags, enabled states, 48–56dp minimum targets, wrapping labels, accessibility and reduced-motion behavior. Do not refactor activity state, audio, progress, navigation, celebration art/motion or unrelated screens. Later P1 order stays visual system → support presentation → minimal support settings → canonical art/content → justified sequencing reuse → Follow the Instructions → semantic Memory Pairs.

## Previous implementation checkpoint — Seasons cycle progression and Wilma auto-follow

Continued the existing Seasons working tree from `5bf2383`, preserving the later `a8877b5` cleaned celebration assets and authoritative roadmap consolidation `56e837a`. No pull/reset/clean/restore/rebase/stash/discard, commit or push. No signing/version/distribution configuration or PNG changed.

Seasons now retains Explore/recognition and adds independent Next/Before 5/10 rounds plus a four-step Build the Year. All use canonical season identities/images, authored EN/DE, shared audio and durable progress. Next/Before include every cyclic boundary before repeating; distractors model unchanged season, reversed direction and a skipped step. Ordering explicitly chooses Spring as the display start of a repeating cycle. Wrong taps retain placed cards; Replay/Help are support, not failed attempts. Mode switching/Options/restore retain exact task/scramble/attempt/score/support/completion state and restore silently. Completion uses the existing optional celebration without progress coupling.

A narrow `OrderedPlacement` helper shares scramble, validation and placement rules with Wilma; authored content and bounded write-ahead journals stay activity-specific. Wilma's existing seven-step checkpoint bytes/IDs are unchanged. Progress schema-1 accepts four-step completion in addition to five/seven/ten; older app downgrade over new four-step records is unsupported. Failed deliveries remain retryable with the same keys.

Wilma's placed strip now smoothly follows a newly added segment over 400ms after layout. Initial/returned/recreated state and viewport resize position at the growing end without a scroll animation. Wrong taps do not pan. No general button/style/navigation redesign was made.

**Owner-supplied physical evidence (2026-09-23):** S24 same-key in-place upgrade succeeded from `1003901 / 1.1.39.1` to `1004501 / 1.1.45.1`, signing SHA-256 `bea808b0c0b891d73e61b739fd43f361a952d5297892318821b97ce91b553507`. Cleaned celebration artwork (`a8877b5`) is physically verified on S24. These checks are complete, not outstanding setup/cleanup work. The current task did not reinstall/wipe the attached phone. All instrumentation explicitly targets `emulator-5554`.

The existing celebration regression test caught larger replacement PNGs exceeding its decoded-image bound. Only the runtime loader now selects a power-of-two sample to keep each decoded dimension at most 200px; no artwork bytes or test bounds were changed. The narration regression now locks the original eight exact descriptions in a fixed fixture because the consolidated roadmap intentionally delegates authored content rather than embedding those sentences.

Final validation: **91 focused JVM, 8 focused Seasons UI, 3 focused Wilma UI, 5 focused celebration UI; 264 full JVM, 28 full emulator, 55 browser tests passed**. Version-policy tests **5/5**, distribution guards **6/6**; debug build passed; lint **0 errors / 3 warnings / 2 information**. Diff/added-file whitespace checks passed. All 31 files remain unstaged; no commit/push.

See the newest BUILD_NOTES.md entry for final validation, exact changed files and APK evidence. Remaining (updated 2026-09-24): broader S24 matrix and Fire Max acceptance/upgrade, EN/DE audible quality/Sound Off, subjective image size/motion, TalkBack/keyboard/large text, lifecycle/process recovery and new four-step progress retention after an ordinary signed upgrade. Missing Season/clues/matching, support settings/presentation, broader visual system and all unrelated roadmap work remain deferred.


## Previous checkpoint — shared native completion celebration

Resumed the existing uncommitted Kotlin/test implementation on `main` at `b3313a3`. Preserved that animal-art commit and the newer roadmap/README commits; no pull, reset, discard, commit or push. Signing configuration, credentials, applicationId and version allocation are unchanged.

All four native activities now share pinned completion actions and five optional balloons: Prepositions, Seasons Practice, Wilma practice/order and Vocabulary practice. Explore remains unchanged. Pop is one-shot; a brief local burst reveals a canonical dinosaur/snake/crocodile/whale/fish PNG, jumps then settles. Wilma retains its completed seven-day sequence. All images reuse the committed library unchanged; the previously implemented glyph reveal is replaced. A small Activity-scoped artwork owner decodes five images off-thread once; Compose renders only.

Reward state is ordinary in-memory Compose state, not a session/progress field. Restoring completion may reset balloons without replaying narration or adding progress. Completion is persisted independently. The optional quiet native pop tone respects ALL/QUESTIONS/OFF through a separate owned SFX policy port; Compose does not own playback and TTS ownership is unchanged. Background/navigation/new round/Replay cancel tones; animation-scale zero simplifies motion.

Validation: **9 focused JVM, 5 focused Compose, 247 full JVM, 21 full emulator and 55 browser tests passed**; build passed; lint **0 errors / 3 warnings / 2 information**. Version-policy tests **5/5** and no-secret signing guards **6/6** passed. Diff/new-file whitespace checks passed. See the newest BUILD_NOTES.md entry for exact commands, APK checksum and all 27 changed files. Remaining acceptance: S24 Ultra/Fire Max, airplane mode, five targets clear of controls/insets, calm float/pop/jump/settle/local confetti, repeated taps, EN/DE accessibility, Sound Off/Replay/missing voices, portrait/both landscapes/large fonts, Options/Back/background/return, process recreation without duplicate progress, next activity, and Fire Max same-key higher-code APK upgrade preserving settings/progress. Update: S24 same-key upgrade and corrected celebration transparency are now proven by the owner; see the current checkpoint. Assets were not altered in that historical implementation. No additional animal artwork is required for this five-animal pool. Broader animal-art integration into Vocabulary is outside this task.


## Previous checkpoint — Vocabulary validated; stable signing setup ready

Started from clean `main` at `0ac4711`, which commits native Wilma. Vocabulary work remains **uncommitted**; do not commit/push automatically or discard it. The content/audio/session/progress foundations and all previous native/legacy activities remain intact.

The Learn card now opens native Vocabulary: six canonical bilingual animal words, Explore/examples, Find the Word (spoken/written word-to-picture) and What Is It? (picture-to-word). The modes use deterministic 5/10 sessions, stable choices, Help/Retry/Replay, score locks, shared audio and separate durable host journals. Options/Back/language/recreation preserve supported state; restoration is silent. Progress effects are committed with accepted state before delivery, deduplicate after restore and remain retryable after failure. Explore has no correctness events. No new framework, engine, remote dependency or UI persistence/TTS.

Validation reconfirmed after signing changes: **33/33 focused Vocabulary, 238/238 full JVM, 16/16 emulator, 55/55 browser**; debug build/lint passed. Final lint **0 errors / 3 warnings / 2 informational findings** (no suppressions). Version-policy tests **5/5**, no-secret Gradle guard checks **6/6**, workflow YAML/shell syntax and diff checks passed. BUILD_NOTES.md records the audit, exact commands/results, file list, artifact identity and outstanding checks.


Resumed on newer `main` at `dbaa8d4`; its completion-celebration roadmap text is preserved and no celebration implementation was started. Stable APK distribution now uses the existing release type with external persistent signing credentials, fail-closed guards and bounded CI run/attempt versioning. Debug defaults remain code 1/name 1.1-dev with this machine's debug key; CI debug artifacts are explicitly debug-only. The applicationId is unchanged. Main-branch manual dispatch can produce `Basti-stable-signed-v<code>` after checks, once the owner sets `BASTI_KEYSTORE_BASE64`, `BASTI_STORE_PASSWORD`, `BASTI_KEY_ALIAS`, `BASTI_KEY_PASSWORD` secrets (README setup).

Historical setup note (superseded for the current S24 stream by the owner-reported proof in the current checkpoint): create and back up the permanent key, configure secrets, dispatch and verify its certificate, compare with installed APKs, then test a higher-code same-key install without uninstalling on S24 Ultra/Fire Max. Prior evidence proves a certificate mismatch; code bumps alone cannot fix that. One final owner-approved reinstall may be needed if the old key is unavailable, and would erase data; no export/import is implemented. No key was generated, no secrets were set, no permanent signed APK or physical upgrade was claimed. Release progress inspection is currently limited without authorised QA access; do not infer preserved/deduplicated records merely from restored UI. See TESTING_QA_SPEC for the exact acceptance procedure. All changes remain uncommitted; no push.

Next: original language-neutral illustrations for dinosaur, snake, whale, horse, crocodile and fish; bilingual/educational review; broader roadmap vocabulary and useful category/context diversity. Existing glyphs are explicit temporary migration placeholders, not final production art. No images were generated or altered. Categories practice is deferred because this starter set has only one meaningful category. This does not complete the roughly 50–80-word v1 curriculum or establish independent reading mastery.

Batch physical checks on S24 Ultra/Fire Max remain open: airplane-mode EN/DE/missing voices and Sound Off/manual Listen; image/word clarity, portrait/both landscapes, large font/insets/accessibility/touch; Options/Back/background; actual process recreation; 5/10 completion and progress after restart/normal signed upgrade. Corrupt/capacity recovery and broader retention/migration remain existing foundation limitations. No neural TTS, dashboard, new cloud service or unrelated activity migration was started.


## Previous checkpoint — native Wilma, automated validation complete

Based on clean `main` at `b607b39` (Seasons committed). New Wilma code is uncommitted. Do not commit/push automatically or discard changes.

Implemented native chooser entry, canonical seven-day Explore, Find Day, Before/After, and activity-specific tap-to-place ordering. Quiz sessions use the existing shared durable host; ordering retains exact scramble/support/retry state plus up to two pending progress effects before publishing completion. The shared progress codec now permits seven-step completion without changing its schema layout; old app downgrade over those new records is unsupported. PNGs are unchanged; head/tail are never weekday IDs or answers. Shared audio policy/cancellation is used, and restore remains silent. Today/Yesterday/Tomorrow is deferred pending an explicit anchor interaction; no clock inference exists.

Validation: **17/17 focused**, **205/205 full JVM**, **12/12 emulator Compose/navigation**, **55/55 browser**. assembleDebug/lintDebug passed; lint **0 errors / 10 warnings / 2 informational findings** (unchanged). The emulator caught a real answered-target enablement bug; it was fixed while preserving the assertion, and the full suite passed. Diff/added-file whitespace checks passed. BUILD_NOTES.md records exact commands, file inventory, APK hash, portrait emulator review and outstanding hardware matrix.

Next: batched physical validation on Samsung S24 Ultra and Fire Max, including airplane-mode EN/DE/missing voices and silence, segment colour/order/clarity, portrait/both landscapes/large fonts/insets/touch/TalkBack, Options/Back/background, true process restoration, 5/10 completion, partial/completed ordering recovery and progress after restart/normal signed upgrade. Emulator audio was disabled; no physical or audible-quality claim. Today/Yesterday/Tomorrow remains an explicitly anchored follow-up. Capacity/corrupt-file recovery and broader retention/migration remain existing foundation limitations. No neural TTS, unrelated activity migration, generic framework, cloud or dashboard work.


## Previous checkpoint — native Seasons, automated validation complete

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
