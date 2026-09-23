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

Offer a recommended short daily route drawn from shared skill progress: one confidence task, one focus task, one communication/listening task and one game/reward. Modules should be approximately 3–5 minutes, with an obvious ending; the whole route is not a forced timed session. Allow stopping after any module and choosing familiar activities instead.

Use the selection/generalisation principles in PROGRESS_TRACKER_SPEC.md rather than maintaining a separate difficulty profile here. With little history, offer a reviewed beginner mix. Missed days have no penalty; avoid streak pressure or claims that a daily route must be completed.

Today’s Adventure should reduce choice overload, not become a requirement or a streak mechanic.

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
