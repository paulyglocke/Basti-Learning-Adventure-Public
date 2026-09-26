# UX and Navigation Specification

## Role and baseline

This document owns child/parent navigation, information organisation and shared interaction behavior. [MASTER_PRODUCT_LEARNING_ROADMAP.md](MASTER_PRODUCT_LEARNING_ROADMAP.md) owns curriculum; [GAME_DESIGN_SPEC.md](GAME_DESIGN_SPEC.md) owns game mechanics; [PROGRESS_TRACKER_SPEC.md](PROGRESS_TRACKER_SPEC.md) owns skill states and adaptation. Use [VOICE_AUDIO_SPEC.md](VOICE_AUDIO_SPEC.md) for speech policy, [ART_DIRECTION.md](ART_DIRECTION.md) for visuals and [TESTING_QA_SPEC.md](TESTING_QA_SPEC.md) for acceptance checks.

The current Compose home has Learn, Practice and Play groups, native Options, and legacy WebView activities. All five Play cards are inactive. Vocabulary Booster now opens its native six-word animal starter slice (Explore, word-to-picture and picture-to-word); Animal Actions remains separately available. The structure below is the intended native UX, not a claim that these destinations are implemented. Introduce it incrementally under [NATIVE_ARCHITECTURE_SPEC.md](NATIVE_ARCHITECTURE_SPEC.md), preserving working routes until tested parity.

## Home and progressive disclosure

Long-term organisation:

| Area | Child-facing purpose |
| --- | --- |
| LEARN | Explore words, letters, numbers and concepts before practising. |
| TALK & LISTEN | Speaking, describing, conversation and listening activities. |
| PRACTISE | Short familiar skill practice and mixed sessions. |
| PLAY | Distinct native 2D/2.5D games using shared learning systems. |
| SCHOOL SKILLS | Classroom routines, help-seeking and self-regulation. |
| DISCOVER | Compare & Discover, animal knowledge and Discovery Book. |

These are navigation groupings, not a second curriculum taxonomy or six mandatory rows of cards. Show a small set of useful choices and a clear way to browse more. Start with implemented, age-appropriate content; progressively reveal further packs/activities. Use familiar pictures and concise bilingual labels. Do not show enabled cards that do nothing or route to unrelated content. During transition, unavailable entries should be clearly non-interactive or omitted without removing working activities.

The long-term child home should prioritise one large **Today’s Adventure** entry, with a secondary **Choose Something Else** / browse path. This should reduce choice overload without hiding the wider library.

## Today’s Adventure

Introduce this in two stages.

### v1 — curated route
Offer a short reviewed route without requiring an adaptive algorithm: for example one confidence/familiar task, one listening/focus task, one learning task and one playful task. Rotate from implemented content. Modules remain optional and approximately 3–5 minutes; the child may stop after any module or choose something else.

### v2 — progress-informed route
Once enough native activities produce useful progress evidence, use the selection/generalisation principles in PROGRESS_TRACKER_SPEC.md to choose sensible next activities. Do not maintain a second difficulty profile here.

In both versions, missed days have no penalty, there is no streak pressure, and Today’s Adventure reduces choice overload rather than becoming a requirement.

## Shared native visual design system

Establish a lightweight reusable visual system before adding many more native screens. This is not a separate UI framework; it is a small set of consistent Compose components and tokens.

Prioritise:
- primary action buttons for Start/Continue
- secondary controls for Replay/Help
- navigation controls for Home/Back
- child-facing image/choice cards with clear neutral, selected, correct, retry and disabled states
- activity headers and short prompt hierarchy
- small calm progress indicators such as eggs, footprints or mission steps
- the shared completion layout
- consistent spacing, corner treatment, typography hierarchy, touch feedback and restrained animation timing

Activities may keep their own visual identity—Seasons should still feel like Seasons and Wilma like Wilma—but controls should behave and read consistently. The system should reduce later retrofit work, not force every learning activity into the same generic quiz appearance.

The first implemented slice is `NativeActionButton(label, role, onClick, modifier, enabled)`, adopted inside `NativeCompletionScreen` and non-answer actions in Seasons, Prepositions, Vocabulary and Wilma. PRIMARY is filled (Play again), SECONDARY tonal (Replay/save retry), NAVIGATION outlined (Home). Roles are presentation only; labels, callbacks, enabled state and semantics remain caller-owned. Material3 supplies theme colors, button semantics, keyboard/focus behavior and restrained press feedback. Shared geometry is a 56dp minimum height that grows for wrapped text, 16dp rounded corners and 16dp horizontal/10dp vertical padding. Labels use the existing labelLarge typography with semibold weight and centered wrapping; no fixed line limit, custom animation or new theme is introduced. Completion actions remain pinned above the optional reward area. Seasons maps Next to PRIMARY, Replay/Help/attempt retry/load-save retry to SECONDARY and Home to NAVIGATION. Existing tags, callbacks and enabled conditions stay unchanged. Answer/season/order cards, per-choice speakers, mode chips and the Days & Seasons hub are excluded. Prepositions also maps How to play to SECONDARY and its legacy fallback link to NAVIGATION; Vocabulary maps sentence Listen to SECONDARY. Wilma day-strip/ordering controls retain their canonical colour cues and existing selection semantics. The shared button fills its allocated width; row widths and surrounding spacing remain caller-owned. Tell Me also uses these unchanged roles for Continue/Next/Again, optional Help/grown-up disclosure, and category/Home navigation. No extra role or API extension is required.

### Shared text-answer choices

`NativeTextChoice(label, onClick, modifier, enabled)` is the first bounded choice-card slice, used for Prepositions answers, Seasons recognition/next/before answers and Vocabulary NAME / “What Is It?” text answers. It is a neutral outlined Material3 button with surface/onSurface colours, 16dp corners, 64dp minimum height, 16dp horizontal/12dp vertical padding and semibold, centred bodyLarge labels. Labels fill their allocated width and wrap without a line/height cap. Material supplies disabled, press, focus and Button semantics. Caller-owned row widths, choice order, identity, callbacks, tags and enabled gates remain unchanged; adjacent Listen is a separate activation target.

This primitive represents text answers, not progression/navigation actions or a correctness state machine. Existing retry/correct feedback stays separate; no selected/correct/wrong styling or labels are invented. The larger-support-text setting does not resize answer labels. Vocabulary retains its existing 72dp minimum and two-column layout; separate Listen remains independently enabled after answer locking. Seasons Explore/order cards, Vocabulary FIND picture/glyph choices and Explore selectors, and Wilma's colour-cued controls remain custom. Image-card variants and additional state presentations require a demonstrated consumer rather than speculative API options.

### Shared support text presentation

`NativeSupportMessage(text, modifier)` presents existing authored hints and gentle retry guidance in Seasons, Prepositions, Vocabulary and Wilma. Tell Me also uses it for explicitly revealed sentence starters and helpful words. These are authored support-text uses, not new support levels: the component deliberately has no category enum, state, action slots or escalation logic. They use a neutral Material3 surfaceVariant/onSurfaceVariant pair, rounded 16dp background and 12dp padding; bodyLarge text wraps at the allocated width with no height or line cap. The authored wording communicates meaning without relying on colour. Prompts, correct feedback, technical errors and completion remain separate.

A single non-interactive Text node preserves caller tags and natural screen-reader reading order, without redundant content descriptions, keyboard focus, live-region announcements or motion. Visibility, exact EN/DE wording, hint retention and retry timing remain activity-owned; NativeActionButton still owns separate Replay/Help/Retry/Next/Home controls. The shared component introduces no stronger-help state or parent-help workflow. Existing support/progress and audio contracts are unchanged. Rollout of this text-only slice is complete for current native activities; this does not implement future support levels/settings. Vocabulary hint-revealed answer labels and Wilma day/ordering/placed controls remain activity-specific and are not support-message containers.

### First support setting: larger support text

Options offers “Larger hints and retry guidance” / “Größere Hinweise und Hilfetexte”, default Off. On scales only NativeSupportMessage body text and line height by 1.25; Android font scaling still applies. Off restores the theme's unchanged bodyLarge style. This applies to already-visible hints/retry guidance in Prepositions, Seasons, Vocabulary and Wilma, including ordering guidance, and Tell Me’s revealed starter/words cards. It neither reveals hints nor changes when/how support is requested or recorded.

The preference is presentation-only and takes effect on return from Options, including during an active task. It is not session/checkpoint/progress data. Prompts, correct feedback, technical errors, Explore/examples, answer-embedded hint labels, individual Listen controls, completion and Wilma day controls/auto-follow remain unchanged. The labelled switch row is one keyboard/screen-reader control with a minimum 56dp target; text wraps and screens retain scrolling without reducing font size. No diagnosis labels, support levels, automatic announcements or audio effects are introduced.

## Activity flow

Home → activity intro → Listen → Try/Play → feedback → next task or completion → Home/Continue.

The predictable micro-flow is **Listen → Try/Play → Celebrate → Finished**. A short intro teaches the interaction without requiring reading. Tutorials can be replayed; returning users should not face lengthy repeated introductions. Wrong attempts offer constructive feedback, Replay and hints, then another try. Celebration is brief and optional to interact with.

| Control / state | Required behavior |
| --- | --- |
| Back | System Back and visible Back have the same meaning: dismiss an overlay first, then return to the previous logical destination. From a quiz-linked lesson, return to the same question/answer state. From parent settings, return to the originating screen. From an activity root, return Home; at Home, use normal Android exit behavior. |
| Home | Explicitly returns to Home from an activity or completion, cancels stale speech and follows the documented session retention/restart policy. Leaving is never scored as failure. |
| Replay | A large, consistently placed control near the instruction/scene; available during attempts and on completion. Replays the current meaningful instruction or summary without changing the question, selecting an answer or awarding score. |
| Option/object speaker | Separate semantic and touch target from answer selection. Pointer, keyboard and accessibility activation must only request pronunciation; keep replay usable after a correct selection. |
| Hint | Offers constructive support without deduction or negative labels. Preserve the task and record support for progress; help is not failure. |
| Next / Continue | Advances only through a valid state transition, once per activation. After completion, Continue starts the next recommended module or another round as labelled, with a new session identity. It must not silently create an endless loop. |
| Completion | Calm acknowledgement, accessible summary/replay, and immediately usable Continue and Home. Do not require balloon popping, animation completion or reward interaction to leave. |

The implemented shared native completion layout keeps Play again, Home and Replay outside the scrollable reward/summary area. Short landscape reduces the reward space rather than placing navigation below balloons. Optional animal reveals may reset after leaving/recreating completion; completion and saved progress do not reset. Existing mode/fallback controls and Wilma's completed sequence remain below the reward.

Navigation must not strand the child in loading, missing-content or audio-failure states. Show a simple retry/return action and retain committed progress. Long-term audio requests follow the shared audio policy; Replay availability does not mean bypassing an explicitly chosen Sound Off setting. Resolve the detailed all/questions/off policy in VOICE_AUDIO_SPEC.md before implementation, and test that contract consistently across screens.

Changing language updates visible and spoken content together, stops stale narration and preserves task identity where meaning remains valid. If a language-specific task cannot be translated in place (for example an initial-sound question), restart that task explicitly without recording a wrong answer. Configuration/recovery behavior follows NATIVE_ARCHITECTURE_SPEC.md.

Use a consistent support ladder where applicable: independent attempt → Replay → subtle visual/verbal hint → stronger model/help → optional parent help. Support is useful progress information, not failure.

## Child-facing progress and scoring
Future native activities should emphasise task/mission progress, effort and completion rather than a prominent numerical score. Prefer **Mission 3 of 5** over **Score 3 / 5**. Completion rewards should not require perfect answers.

Internal progress may still record independent success, Replay, hints, retries and parent help. A child who completes every item after support should not be shown a misleading “perfect mastery” score. The legacy score may remain during migration until native parity allows safe replacement.

## Parent areas

Provide a distinct parent entry, separate from the child's activity choices, with a deliberate adult access step. This is protection against accidental settings changes, not an account/login system. Do not require reading or an adult challenge for ordinary child navigation.

Parent destinations include:

- Progress dashboard and Suggested Focus, using the supportive states and 3–4 focus areas defined in PROGRESS_TRACKER_SPEC.md.
- Difficulty/adaptive controls and optional session length; the current 5/10-question preference can coexist with approximate module duration, without a countdown.
- Language, audio and tested voice choices; technical engine diagnostics belong in a parent/developer Voice Lab, not the child home.
- Tutorial replay/reset controls with clear scope and durable effect; distinguish these from any future progress reset. A destructive progress reset needs explicit parent confirmation.
- Future “Seen outside app” observations and parent-help recording without grading the child.

Parent settings/history stay local and offline, with no Google Play Services requirement. Do not display grades, comparisons with other children or a deficit list in the child flow.

## Reading independence and emotional UX

Core activities should work through pictures, spoken guidance, large targets and minimal required reading in both English and German. Make the action understandable before asking for an answer. Meaningful custom pictures receive useful semantic descriptions; decoration does not need narration. Follow ART_DIRECTION.md for replacing temporary emoji.

Use large, separated child-friendly hit targets; avoid nested interactive controls, precision gestures and colour-only meaning. Drag tasks should offer an accessible alternative where practical. When audio is unavailable, preserve visual access and provide a parent-facing recovery path; do not claim a listening-only activity is fully usable without working speech.

No lives, punishment, “failed” states, stressful countdowns or loss streaks. Encourage trying, listening and asking for help. Support does not remove earned rewards. Open-ended speaking invites communication without automatic speech scoring. Game-specific retries/rewards remain in GAME_DESIGN_SPEC.md.

## Device and accessibility behavior

Design for Samsung S24-class phones and Amazon Fire Max tablets, portrait and landscape. Reflow controls and scenes using available space; do not merely scale a fixed phone layout. Large fonts and longer German labels must not clip essential content.

Apply system-bar/cutout insets consistently without double padding. In short landscape height, keep Replay, answers and completion actions reachable; allow controlled scrolling or a different arrangement instead of shrinking targets. Parent keyboards must not obscure settings actions. Check both gesture and button navigation where available.

Maintain logical TalkBack/keyboard focus order and distinct labels for selection, speaker, Back and Home. Respect reduced motion without hiding learning meaning. Physical and automated layout/accessibility evidence is required as defined in TESTING_QA_SPEC.md.


## Tell Me / Erzähl mal v1

One Home entry opens native category selection; there is no legacy Tell Me route to
replace. The nine category controls retain repository order and localized names. Each
category visits its nine pictures in authored order with explicit TALK → MODEL → Next.
Neither Continue nor completion means an answer was accepted or evaluated. Completion
shows Again, Choose another adventure and Home, without a score or mastery statement.

TALK shows the generic invitation plus the first localized QUESTIONS line, otherwise
STARTER_PROMPTS, otherwise EXPANSION_PROMPTS. Optional Help reveals the first sentence
starter and up to three WORDS_TO_MODEL lines. MODEL shows the first localized example,
otherwise MODELLING_EXAMPLES. Missing optional fields are omitted, never synthesized or
borrowed from the other language. Currently all 81 scenes have prompts; only the 27 Wave 1
scenes have this bounded Help/model material. The other 54 still use explicit MODEL/Next
without invented examples. The collapsed grown-up section shows principle/focus and at
most one authored expansion pair. Review cautions and target lists are not child feedback.

The full artwork composition uses Fit. The title supplies a minimal image label because
there is no authored alt description. Text wraps without fixed text height, actions use
shared 56dp minimum targets, and the whole scene scrolls in short landscape/large text.
Support is passive, has no live announcement, and retains the existing larger-support-text
setting. Language/Options preserve semantic state; the next picture resets expanded
support. Home ends the in-memory conversation. Process-death persistence and speech are
not part of v1; physical TalkBack, visual clarity and inset acceptance remain outstanding.
