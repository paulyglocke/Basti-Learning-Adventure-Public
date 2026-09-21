# Session handoff — 2026-09-21

## Current repository checkpoint — 2026-09-21

Current remote `main` includes:

- `f72c5f7` — `fix: prevent decorative emoji from leaking into TTS`
- `796359e` — `docs: refine learning priorities and daily experience`

The latest documentation refinement updated:
- MASTER_PRODUCT_LEARNING_ROADMAP.md
- PROGRESS_TRACKER_SPEC.md
- UX_NAVIGATION_SPEC.md
- GAME_DESIGN_SPEC.md
- CONTENT_DATA_SPEC.md
- BACKLOG.md

No runtime code changed in the plan-refinement commit.

Important revised priorities:
- Today’s Adventure becomes the eventual primary child entry, with a secondary browse path.
- Minimal persisted skill-progress events move into the early native foundation.
- Prepositions remains the first end-to-end native migration proof.
- Follow the Instructions remains the first true native game/learning engine.
- Vocabulary Booster v1 moves earlier.
- Tell Me! / expressive-language v1 moves earlier.
- Memory Pairs is a reusable shared matching engine.
- Early maths prioritises subitising, quantity, patterns and shapes over large-number drill.
- English and German phonics are language-specific.
- Classroom/self-advocacy language is raised in priority.
- Discovery Book knowledge unlocks are a major reward direction.
- Generalisation must use varied contexts rather than repeated object pairings.

Immediate next implementation task:
P0 Audio Reliability Pass — enforce all/questions/off policy, TTS readiness and offline voice handling, stale-speech/lifecycle cancellation, tutorial reliability and SFX policy. Do not start Supertonic/Piper/neural TTS yet.

After that, continue remaining P0 correctness/layout/navigation work before entering the native foundation sequence.

## Current session — P0 Voice/TTS cleanup

Root cause: `pick()` passed decorated praise directly through `audioGuidance.say()` → `speak()` → `Android.speak()` → Android TextToSpeech. All questions/instructions, tutorials, Replay, option speakers, feedback, completion and Verb Explorer already converge on that same JavaScript function. Hints are visual only; balloon decorations are not narrated (their WebAudio tones are separate).

All eight English/German praise entries now pair `displayText` with explicit `speechText`; visible emoji remain. `sanitizeForSpeech()` runs inside `speak()` after the existing audio policy check and before either Android bridge or browser fallback. It removes pictographic/decorative ranges and emoji joiners/selectors/tags, normalizes whitespace/punctuation spacing and skips empty output while retaining ordinary numbers, punctuation, German characters and mathematical text. Semantic icons must still receive authored speech, not inferred emoji names.

No Kotlin duplicate sanitizer: the current native engine has only the legacy bridge caller. Future native speech must move this boundary into the shared audio controller. MainActivity, index.html, content generation, scoring, navigation and the speech engine are unchanged. Supertonic/Piper, Voice Lab and neural integration have not started.

Seven new tests in tests/speech.spec.js cover every praise entry, both-language speech routes, decorated completion, Unicode/number preservation, empty requests, browser fallback and unchanged mode/force behavior. Existing pointer/Enter/Space answer-speaker isolation tests remain intact.

Validation results will be recorded in BUILD_NOTES.md after the browser run. Android assembleDebug/lintDebug passed with Android Studio JDK 21 (Java/Kotlin compilation target 17), 0 lint errors / 7 warnings; APK assets match current source. Physical S24/Fire speech validation remains outstanding.

Remaining P0 audio work: define/enforce all/questions/off and manual Replay semantics; balloon sound policy; TTS readiness/missing offline voices; stale speech/lifecycle cancellation; reliable language/version-aware tutorials/reset. Current forced manual speech still bypasses Off and questions mode includes feedback, deliberately unchanged here. P1 remains the shared native audio controller/engine abstraction and test doubles, followed separately by physical voice auditions. See BACKLOG.md and VOICE_AUDIO_SPEC.md.

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
