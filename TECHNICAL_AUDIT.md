# Technical and product audit — 2026-09-20

Audited commit: `290ff03`, confirmed remote main. See `SESSION_HANDOFF.md` for continuation instructions and test evidence. This records the audit delivered to the user; implementation remains outstanding.

## Verdict

Working hybrid prototype with a useful Compose starting point, but not yet a reliable audio-first native app. Preserve the existing content while fixing correctness and establishing native shared components. Nine green browser tests and a green Android build do not establish native navigation, TTS, device insets, semantic animation quality or complete accessibility.

## Ownership and native migration review

- Kotlin/Compose: Activity, window configuration, top bar, home, Options, three-value screen state, SharedPreferences, TTS engine, WebView creation.
- JavaScript: seven quiz modes, question generation, translations, answers, score, tutorials, audio policy, lesson library and navigation, completion/rewards.
- HTML/CSS: legacy screen structure, responsive web layout, spatial scenes, emoji/keyframe animation, balloons.
- Settings duplicated across SharedPreferences, Compose state, JS settings and localStorage. Native settings overwrite JS; no old-install preference import. Custom number maximum exists in web Options but not native Options.
- No durable learning progress, session repository or native quiz engine.

What was done well: incremental preservation, Material 3 foundation and bright identity, scaffold body padding, blocked WebView network loads, no INTERNET permission/Play Services, bounded maths/counting, score repeat guard, CI/browser tests.

`MainActivity.kt` issues:

- Around line 139, native Back always goes home instead of invoking JS `navigateBack()`. Lesson return and system Back differ. Options does not preserve a navigation back stack.
- Around line 189, each learning entry creates a WebView without destination release/disposal; only last referenced instance destroyed at Activity destruction. Native navigation does not explicitly cancel web pending narration.
- `remember` only; no saved session or process-recreation recovery. Manifest `configChanges=orientation|screenSize|keyboardHidden` masks ordinary rotation recreation but is not state restoration.
- Every AndroidView update resends settings; unchanged language/audio setters stop speech. No readiness/versioned bridge protocol.
- TTS requests before initialization dropped. Offline matching voice preferred, but absence still calls speak with previous/default voice. Missing voice/error feedback absent. Voice enumeration on every request.
- Tutorial reset is a nullable WebView call: no-op before WebView creation, no durable reset request or confirmation.
- Vocabulary card has null mode and falls back to verbs. Play cards look enabled but return without action.
- Home uses fixed two-column rows and 142dp cards: German text and large fonts can overflow. Non-lazy scroll does not constitute tablet adaptation.
- `Color.White.value.toInt()` should use Android color conversion, not packed Compose color bits.

## Audio-first findings

`app.js` lines 231–280 and 290–311:

- All quiz modes request automatic instructions/questions in all/questions modes; every option has manual speech. Pointer speaker taps do not submit in tested flow.
- Critical: nested button inside role=button answer has bubbling Enter/Space handler. Focus correct speaker and press Enter: answer submitted and score awarded. Use sibling native/HTML buttons.
- `force=true` bypasses Sound Off for replay/options/lessons. Balloon WebAudio ignores audio settings. Old tests expect this legacy behavior; update expectations to new policy.
- Tutorials marked heard at lookup, not successful playback, including when off/interrupted. Flags not language/version-specific. Mixed uses underlying activity tutorial rather than mixed introduction.
- Delayed automatic question can interrupt answer/manual feedback. QUEUE_FLUSH limits queue buildup but does not cancel pending JS timers in every path.
- Questions-only currently includes feedback; document an explicit policy matrix.
- Home lacks spoken child guidance. Verb library still relies on reading for precise action selection; no always-visible library replay. Try-it text not in narrated lesson. Completion has no replay.
- Missing-number sequence not narrated. Maths primarily an equation rather than concrete object groups. Calendar abbreviations lack individual audio.
- Offline TTS and natural pronunciation require physical testing; browser mocks only confirm requests.

## Prepositions/content correctness

`app.js` around line 409:

- Spoken list omits in while answers include it; five spoken positions versus four random options.
- Under scene is table, in scene is box; feedback always says rock.
- Between references one rock, not two.
- German examples include `Super! der Dinosaurier ist zwischen dem Stein.`; sentence capitalization, pronoun gender, plurality/reference object incorrect.
- Derive scene/prompt/options/feedback from a single typed relation+animal+object model.

`letterWords` around line 192:

- German Schlange is /ʃ/, not simple /s/. Pferd begins with a cluster, not clean /p/.
- TTS letter name is not phoneme instruction. Need educator-reviewed phonics content and possibly bundled sound recordings.
- Animal Actions can have multiple plausible answers (rabbit/frog jumping). Curate explicit accepted answers or disambiguate.
- German instruction “Höre auf die Bewegung” awkward. Use explicit reviewed sentences where morphology is complex.
- 53 verb cards, four options, long prompts and advanced maximums create cognitive load. Use small beginner packs and parent-controlled progression.

## Animation audit

`animationForVerb()` around line 445 and CSS keyframes:

- Whole emoji transformed; no articulated limbs/jaws/wings or travelling snake wave. Extra keyframes do not meet teaching requirement.
- Eight bobbing verbs: use a trunk, nibble, chew wood, waddle, carry its baby in a pouch, graze on grass, clap its flippers, carry food.
- Eight shared sound verbs: meow, moo, oink, quack, buzz, hoot, hiss, copy sounds.
- Jump/hop and flap/flutter shared. Fetch/chase exact keys do not match “fetch a ball”/“chase mice”; new fetch sequence unused.
- Detailed water branches omit original wave element. Dam/web props decorative rather than assembled. steps(1,end) makes pose changes abrupt.

Recommendation: native layered illustrated vector/bitmap characters with body-part pivots, semantic timeline phases, props, one visible-scene clock and lifecycle cancellation. Sprite sheets for substantial silhouette changes; Lottie optional for authored decoration; no videos/heavy 3D. Reduced-motion alternative should show meaningful poses rather than an unexplained frozen frame.

First 20 existing actions: jump, hop, gallop, swim, dive, climb, slither, dig, scratch, kick, flap wings, snap jaws, stomp, roar, howl, bark, peck, fetch, build a dam, stand on one leg. Run can be a later explicit lesson. Validate actions silently before considering them done.

## Rewards and balloons

`pick()`/`finish()` around lines 499–532:

- 0, partial, perfect five and perfect ten generate matching numeric text/star characters. Repeat correct taps award once; repeated finish does not add score. Continue creates new round.
- Critical visual issue: ten-star single text item cannot wrap as intended. At 360px, star scrollWidth 470 vs container 289; clipped by finish overflow. Also reproduced at 320px. Render separate star cells.
- Unlimited retries and Next-after-correct mean completed rounds usually perfect. Stars measure completion, not independent mastery. Track attempts separately without punishment.
- Balloons optional, Continue enabled immediately, finite entrance and reduced-motion CSS exist.
- All six balloons actually same red emoji; color classes unstyled. Sound ignores mode. English-only labels. Faded buttons remain focusable. Narrow targets crowd. Disabled legacy Next remains below completion; immediate viewport visibility not guaranteed in landscape.
- Use native bounded balloon states, separate hit targets, shared sound effects and independent completion actions later. Persistent rewards need session-ID idempotency.

## Insets/device/performance/accessibility

- Scaffold body safeDrawing padding is promising for portrait. TopAppBar overrides insets to statusBars only; horizontal camera cutout/navigation protection not established for landscape.
- No obvious current double top padding; future inset-aware children should consume applied padding to avoid duplication. safeDrawing does not equal safeGestures.
- No physical S24/Fire test. Verify gesture/three-button navigation, both orientations, cutouts, large text, Activity recreation and process death.
- Fire risks: local English/German voice availability, WebView/emoji version, layout constraints, retained WebView memory. No Play Services dependency alone does not prove compatibility.
- Performance: repeated WebView creation, full innerHTML rendering, sync on recomposition, voice enumeration, eager home cards, per-pop AudioContext, CSS shadow filters/loops. No measured memory/frame baseline.
- Accessibility: nested controls, arrow-only Back, English-only labels, fixed-height text clipping, pointer-locked speakers after correctness, scene descriptions, invisible balloon focus. TalkBack/Switch Access untested.

## Feature matrix

- Implemented in WebView: Animal Actions, Numbers, Easy Maths, Prepositions, Letters, Days & Seasons, Mixed Adventure, 53 Verb Explorer lessons, completion and balloons.
- Vocabulary Booster: absent, wrong route to verbs.
- Placeholders only: Dragon Treasure Hunt, Dinosaur Rescue, Memory Pairs, Crocodile Snap, Follow the Instructions.
- Discovery Book, local learning history, eggs/stickers/unlocks: absent.

## Native framework and roadmap

Recommended packages initially within one module: app/navigation; learning/models/generators/session reducer; content/bilingual records/assets; audio/policy/TTS; progress/repositories; ui/quiz/scenes/completion; games/state machines; legacy/WebView adapter.

Use stable IDs, immutable questions, injectable RNG, session ViewModel, lifecycle-aware StateFlow, settings repository, owned/cancellable narration requests and playback callbacks. Keep Activity/WebView out of ViewModel.

1. Fix confirmed bugs; add focused regressions.
2. Native settings/audio/session/navigation ownership.
3. Shared question header, replay, sibling answer/audio controls, scene, progress, feedback, tutorials and completion.
4. Extract stable content and scoring/session reducer.
5. Native Prepositions end-to-end: six relations prove scene/audio/feedback/lifecycle/bilingual contracts.
6. Native Numbers and Maths generators with invariants.
7. Letters and Days/Seasons with reviewed content.
8. Animal Actions and Verb Explorer lessons/library.
9. Native action animations in batches.
10. Mixed from native generators.
11. Migrate legacy stored preferences/tutorials/progress as needed.
12. Remove WebView/bridge/HTML/JS only after all runtime routes, content loading, audio and data migration paths are native and tested.

Legacy home/Options are superseded in Android but initialization/applyLanguage/tests still reference DOM IDs; not safe to wholesale delete yet. JS navigateBack no longer wired to Android. Old animation branches/imports/colors are cleanup candidates. Avoid hiding invalid content IDs with random substitutions.

Per-screen removal gates: bilingual parity, audio policy/replay/tutorial, retries/scoring/completion, font/viewport layouts, native navigation and recreation tests. Keep browser tests until corresponding native tests pass.

## Future product architecture

- Games: shared scene objects, content/audio/progress/rewards; separate small game state machines (not every game is a quiz). Layered 2D/2.5D with limited parallax/particles. Order: Follow the Instructions, Memory Pairs, Treasure Hunt, Rescue, Snap. Avoid forced reaction timing.
- Vocabulary model: stable ID, pack/tags, EN/DE forms, grammar metadata, illustration, speech, explanation, example, prerequisites/difficulty. General packs and dinosaur/reptile/dragon/ocean interests share words by IDs. Start small and reviewed.
- Progress: local session IDs/results, content/skill attempts, assisted vs independent success, language/version tutorial completion, unique unlocks/Discovery entries. DataStore for settings; Room when structured history merits it. Transactional unique session rewards. No accounts/analytics/lives/streak penalties. Review Android backup policy (allowBackup is currently true) against future local-only expectations.

## Build/release/documentation

- AGP 8.7.3 / Gradle 8.9 / Kotlin 2.0.21 / JDK17 / compile+target35 / min23. Pinned wrapper checksum; npm lockfile. Build/lint and 9 browser tests pass; lint six warnings, no errors. CI green for audited commit.
- No native unit/Compose/instrumentation tests. Existing browser suite mocks speech, runs legacy home/Options, does not verify new policies/rewards/native lifecycle.
- Current debug ~8.5MB vs old tracked V1 APK ~48KB. Published v1.0.0 is not current source build. versionCode1/versionName1.0 unchanged. No production signing configured; CI ephemeral debug signatures. Reproducible byte-identical builds not established. Actions use major tags, not SHAs.
- README: outdated CSS adaptability/custom Android options/audio modes; clarify historical release vs main.
- BUILD_NOTES: stale Java architecture/settings/Back/rotation claims; verification artifacts historical, not current proof.
- MIGRATION_PHASE_1: overclaims universal inset correctness; nonexistent established dinosaur asset system referenced.
- BACKLOG: understates correctness problems; draft publishing task stale; replacing inline handlers does not remove JS-enabled lint warning.
- PROJECT_BRIEF: historical wrapper/Java references need current architecture and native target addendum.
- CODEX_START_HERE: points to deleted MainActivity.java; must link Kotlin and current audit.

## Priorities

P0: speaker key activation, prepositions, reward wrapping/actions, audio policy/TTS, lifecycle/back/tutorial reset, physical insets/device tests.
P1: native shared quiz/audio/settings/session models, Prepositions, structured content, articulated animations, native tests and docs/release correctness.
P2: games in agreed order, vocabulary starter packs, local Discovery Book.
P3: illustration refinement, visual depth, animation expansion, measured performance/accessibility polish.

No runtime dependency or HTML percentage decreased in this audit. Next recommended implementation is the bounded correctness patch, then the native Prepositions framework slice.
