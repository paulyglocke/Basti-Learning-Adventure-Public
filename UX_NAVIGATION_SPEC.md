# UX and Navigation Specification

## Role and baseline

This document owns child/parent navigation, information organisation and shared interaction behavior. [MASTER_PRODUCT_LEARNING_ROADMAP.md](MASTER_PRODUCT_LEARNING_ROADMAP.md) owns curriculum; [GAME_DESIGN_SPEC.md](GAME_DESIGN_SPEC.md) owns game mechanics; [PROGRESS_TRACKER_SPEC.md](PROGRESS_TRACKER_SPEC.md) owns skill states and adaptation. Use [VOICE_AUDIO_SPEC.md](VOICE_AUDIO_SPEC.md) for speech policy, [ART_DIRECTION.md](ART_DIRECTION.md) for visuals and [TESTING_QA_SPEC.md](TESTING_QA_SPEC.md) for acceptance checks.

UX Refresh Phase 3 integrates the approved illustrated Home with six bilingual browsing categories and All Activities. Nineteen existing entries belong to categories; Mixed Adventure remains the twentieth in All Activities. Colours launches its sole activity directly. The original catalogue, filtered after its historical positional grouping, still supplies labels and dispatch identities; the four unavailable games remain hidden, not deleted. See [UX_HOME_INTEGRATION.md](docs/UX_HOME_INTEGRATION.md) for the complete mapping and current validation status. Native and legacy learning destinations remain unchanged.

The illustrated Home owns its compact Grown-ups control and category Back/Home controls, without a duplicate shell header. Native Options and learning destinations retain their existing header. Home preserves the Phase 0 32sp/40sp greeting line-height correction, natural wrapping and scrolling. Available width and font scale choose columns; learning-screen colours and typography are not migrated.

Category/All Activities Back returns to illustrated Home. Options returns to the exact browsing page; language changes retain that page. Activity-root Back deliberately retains its existing safe Home destination, not a new category parent. Nested learning Back and explicit Home contracts remain unchanged. Browsing pages and supported scroll state survive recreation; no learning checkpoint or progress identity changes.

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

The first implemented slice is `NativeActionButton(label, role, onClick, modifier, enabled)`, adopted inside `NativeCompletionScreen` and non-answer actions in Seasons, Prepositions, Vocabulary and Wilma. PRIMARY is filled (Play again), SECONDARY tonal (Replay/save retry), NAVIGATION outlined (Home). Roles are presentation only; labels, callbacks, enabled state and semantics remain caller-owned. Material3 supplies theme colors, button semantics, keyboard/focus behavior and restrained press feedback. Shared geometry is a 56dp minimum height that grows for wrapped text, 16dp rounded corners and 16dp horizontal/10dp vertical padding. Labels use the existing labelLarge typography with semibold weight and centered wrapping; no fixed line limit, custom animation or new theme is introduced. Completion actions remain pinned above the optional reward area. Seasons maps Next to PRIMARY, Replay/Help/attempt retry/load-save retry to SECONDARY and Home to NAVIGATION. Existing tags, callbacks and enabled conditions stay unchanged. Answer/season/order cards, per-choice speakers, mode chips and the Days & Seasons hub are excluded. Prepositions also maps How to play to SECONDARY and its legacy fallback link to NAVIGATION; Vocabulary maps sentence Listen to SECONDARY. Wilma day-strip/ordering controls retain their canonical colour cues and existing selection semantics. The shared button fills its allocated width; row widths and surrounding spacing remain caller-owned. Tell Me also uses these unchanged roles for Next picture/Again, optional Help/grown-up disclosure, and category/Home navigation. No extra role or API extension is required.

### Shared text-answer choices

`NativeTextChoice(label, onClick, modifier, enabled)` is the first bounded choice-card slice, used for Prepositions answers, Seasons recognition/next/before answers and Vocabulary NAME / “What Is It?” text answers. It is a neutral outlined Material3 button with surface/onSurface colours, 16dp corners, 64dp minimum height, 16dp horizontal/12dp vertical padding and semibold, centred bodyLarge labels. Labels fill their allocated width and wrap without a line/height cap. Material supplies disabled, press, focus and Button semantics. Caller-owned row widths, choice order, identity, callbacks, tags and enabled gates remain unchanged; adjacent Listen is a separate activation target.

This primitive represents text answers, not progression/navigation actions or a correctness state machine. Existing retry/correct feedback stays separate; no selected/correct/wrong styling or labels are invented. The larger-support-text setting does not resize answer labels. Vocabulary retains its existing 72dp minimum and two-column layout; separate Listen remains independently enabled after answer locking. Seasons Explore/order cards, Vocabulary FIND picture choices and Explore selectors, and Wilma's colour-cued controls remain custom. Image-card variants and additional state presentations require a demonstrated consumer rather than speculative API options.

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

Days & Seasons opens a Months activity chooser. Its featured Explore choice opens
the existing interactive Year Wheel directly; the chooser also keeps every shipped
Months practice mode reachable without a new curriculum level. Explore is a
separate presentation route. Back from Explore returns to the chooser, while Back
from the chooser returns to Days & Seasons. Existing saved `MONTHS` destinations
continue to restore as Explore. Quiz learning alternatives still open the Year
Wheel, and Months Between uses its contextual wheel route so its visible return
action restores the same question. Build Year's level chooser and the Seasons Wheel
are direct chooser destinations; leaving either returns to the chooser. These
presentation routes add no progress identity, journal, checkpoint or saved-progress
format.

Seasons retains its existing activity owner and nine modes: Explore, Practise,
What comes next?, What comes before?, Missing season, Season clues, Build the
Year, Match season to clue, and Before and after. Days & Seasons opens a dedicated
Seasons chooser; its featured Explore choice opens the existing Explore phase in
one selection, while every practice/ordering/matching phase remains a direct
chooser destination. Back from an activity returns to the chooser; Back from the
chooser returns to Days & Seasons. Explicit Home still returns to illustrated
Home. Options returns to the exact chooser or activity route, and language changes
retain the active selection. The chooser is a presentation route only: Seasons
selection, phase owners, journals, checkpoints and saved-progress formats remain
unchanged. Months-owned Sort Months into Seasons and Seasons & Months Wheel
routes remain independent and unchanged; no duplicate Seasons activity is added.

The illustrated Words & Talking catalogue opens a bilingual Vocabulary chooser.
Its primary Explore action and both practice modes open the existing Vocabulary
activity route in one selection. Back from an activity returns to the chooser;
chooser Back returns to its Home catalogue, and the chooser's explicit Home
action returns to illustrated Home. Options retains the exact chooser or activity
as its origin through language changes and recreation. Older saved `VOCABULARY`
routes still restore directly into the activity. Exploration selection remains
in the existing selection store, while Find and What Is It? keep their independent
journal/checkpoint owners. Visiting the chooser does not create a round, regenerate
a question, or alter saved progress formats.

Days & Seasons opens a Wilma activity chooser. Its featured Explore option opens
the existing Wilma screen directly, while Find the day, Before and after, Put the
week in order, Today/yesterday/tomorrow, and German & English weekdays remain
individually reachable. Each phase continues to use the existing Wilma route and
ViewModel. Back from a phase returns to the chooser; chooser Back returns to Days
& Seasons, and explicit Home still reaches illustrated Home. Options returns to
the exact chooser/activity route, and language changes retain the existing
selection. The chooser is presentation-only: `WilmaSelectionStore`, quiz journals,
ordering journal, audio owner, day order/colors and saved progress formats are
unchanged. Old saved `WILMA` routes still restore directly into the activity.

Months Build the Year begins with a level chooser: Level 1 orders three consecutive
months and Level 2 orders four. Both remain independently selectable; completion of
Level 1 does not auto-advance. Level 2 uses deterministic 5/10 question rounds with
unique four-month cyclic windows, including December/January wraparound. It reuses
the shared ordered-placement mechanic: choose a shuffled month tile, then activate
the next numbered empty position.
This tap-select/tap-place path is the required accessible interaction, not a drag
fallback. Position semantics identify whether the slot is empty/filled and expose
the placement action; keyboard activation follows the same state transition. Return,
Options and recreation preserve the activity-owned durable checkpoint and restore
silently.

Months Between presents two named consecutive endpoints around an explicit unknown
middle position; the question and its speech name both endpoints. Endpoints remain
visible while choosing one of four month answers. Its optional Year Wheel is a
quiz-linked lesson: visiting it records hint support but no answer, and Back or the
visible return action restores the exact question, endpoints, language and attempts.
The Between route owns a separate durable quiz journal and does not change Missing
Month task, activity, skill or journal identities.

Months ↔ Seasons is a separate Months destination with four illustrated season
groups. The current named month is assigned with one accessible tap; earlier correct
placements remain visible in their groups, and the optional full month/season guide
is opened without leaving the task. Replay, retry, language changes, Options, Back,
Home and Activity recreation follow the shared Months session owner; restoration is
silent and does not create an attempt. Season destinations are labelled controls
with keyboard/switch activation, not drag-only drop targets. Physical navigation,
TalkBack and touch acceptance remain separate from emulator coverage.

The optional Interactive Seasons & Months Wheel is a separate route from the
existing Year Wheel and Months ↔ Seasons quiz. Its central season artwork is fixed;
only the 12-segment month ring rotates. Dragging snaps to 30° steps, with accessible
previous/next controls and tap-to-speak month labels. Match checks the exact solved
alignment; Explore allows browsing and an optional solved-alignment demonstration.
Back returns to the Months activity list, Options/language and recreation preserve
the independent wheel checkpoint, and restore is silent. The single fixed pointer is
15° clockwise from twelve o'clock and points inward at a month-segment centre. Its
selected month uses the same segment-centre geometry as rendering; at solved rotation
it identifies March. This wheel is unscored and emits no shared progress events.
Explore also offers Show/Hide Weather, Change Weather and a separate weather Listen
control. Selection follows the snapped pointer month; changing months resets the
default weather and cancels prior speech. Weather visibility and the chosen alternative
restore silently. Match removes weather effects and the month–season/weather aid;
these controls introduce no additional route.

Follow the Instructions now opens a bilingual two-level chooser from Home. Level 1
retains the former one-step route; Level 2 is an ordered two-action task. Both are
directly selectable, and Back returns to the chooser without discarding the active
level's saved round; Back again returns Home. Options and language changes preserve
the exact task, including a completed first step. The pictured example and Listen
control introduce the two-step interaction. Replay repeats the *whole* instruction
at either step; acknowledging step one must not separately announce the second
animal. Four large named picture controls support touch, keyboard and screen-reader
activation. Hint is optional and does not name the target. A correct second action
reveals Next; advancement is child-controlled. Completion uses the shared flow.

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
category visits its nine pictures in authored order. One primary “Next picture” /
“Nächstes Bild” button advances in one press; there is no intermediate model phase.
Neither Next nor completion means an answer was accepted or evaluated. Completion
shows Again, Choose another adventure and Home, without a score or mastery statement.
After those actions it includes the existing optional `NativeCompletionCelebration`;
only the ninth Next picture press makes it available. Popping is never
required to leave. The shared shell owns optional pop sound with its existing Sound Off/
foreground guards. No Tell Me narration is introduced.

For all 81 scenes, TALK shows the generic invitation plus the
selected bilingual bundle's prompt. Optional Help uses that same bundle's starter,
words and model. Repeat visits alternate deterministically; the chosen slot does
not change during a visit. New process starts reset to the first slot.
The reference fallback for uncurated fixtures shows the first localized QUESTIONS line, otherwise
STARTER_PROMPTS, otherwise EXPANSION_PROMPTS. Optional Help reveals the first sentence
starter and up to three WORDS_TO_MODEL lines. Help also shows the first localized example,
otherwise MODELLING_EXAMPLES, under “You could say:” / “Du könntest sagen:”.
This introduces an optional example, not an expected answer. Missing optional fields are omitted, never synthesized or
borrowed from the other language. All 81 scenes now have bounded, explicitly authored
Help/model material in both slots. The collapsed grown-up section shows the selected
bundle's parent guidance and its short child example expanded into the matching model.
The model remains only one possible response. Parent notes accept pointing, single
words, different feelings/ideas and absent personal experience without requiring repetition.
Review cautions and target lists are not child feedback.

The full artwork composition uses Fit. The title supplies a minimal image label because
there is no authored alt description. Text wraps without fixed text height, actions use
shared 56dp minimum targets, and the whole scene scrolls in short landscape/large text.
Support is passive, has no live announcement, and retains the existing larger-support-text
setting. Language/Options preserve semantic state; the next picture resets expanded
support. Home ends the in-memory conversation. Process-death persistence and speech are
not part of v1; physical TalkBack, visual clarity and inset acceptance remain outstanding.

## Colour Sort v1

One native Home card (“Colour Sort” / “Farben sortieren”) opens COLOUR_SORT. Home/back
retains the durable set; Options and configuration recreation retain selection and
placements, updating language in place. The current slice is four native ball/block
tokens and two colour groups. A selected item gains a strong neutral outline and
selected semantics; tapping a category places it. Wrong placements retain selection
for immediate correction, show calm support and leave both destination colours visible.
Help is optional and never solves the item. Correct items move from the tray into their
category; all four trigger shared completion with Replay, Again, Home and optional balloons.

Tap/keyboard/TalkBack is the complete interaction. Drag is not implemented in this slice.
Each actionable item/group is one meaningful semantic target, with natural EN/DE names,
large bounds and no ID-derived labels. Category containers remain visually distinct
from selectable tokens. Text wraps and the whole scene scrolls in short landscape at
large font. Colour is the explicit learning target here, not an app-wide accessibility
exception. Sound Off remains respected. Physical colour recognition/ergonomics and
TalkBack acceptance remain separate from emulator tests.

## Animal Groups v1

One native Home entry “Animal Groups” / “Tiere zuordnen” opens ANIMAL_GROUPS. No discovery
hub or Home redesign. Four canonical animals are the main visual targets; Water/Land
use labels plus distinct wave/ground cues, not colour-only meaning. Tap an animal then
its group; a thicker neutral outline and selected semantics identify the selection.
Each animal is one interactive target with a localized name; its Fit image is decorative
inside that target, avoiding duplicate announcements. Missing art is passive, never an
invisible answer. Optional Help precedes the available animals in reading order.

Wrong placement leaves the same item available for immediate correction. Help remains
optional. Correct placement removes it from the tray and displays it in its group.
Groups expose localized labels and placed counts. Scrollable two-column layout and
wrapping text preserve access at large font/short height. Keyboard activation shares
the tap operation. Drag is deferred. Opening is silent; Listen again/selection/Help
request authored speech, respecting Sound Off. Home/back retains the exact puzzle;
Again and shared completion/balloons follow the sorting contract.

## Wash Hands v1 (2026-09-30)

One native Home entry “Wash Hands” / “Hände waschen” opens WASH_HANDS, without a Practical
Life hub. Unnumbered available step cards each offer append and a separate localized Listen.
Tap/keyboard appends to the next open numbered position; constructed positions expose
Step/Schritt 1–4, Listen and Remove/Zurücklegen. No drag or selected-colour convention.
Removal compacts the remaining sequence and returns the removed item to its original tray
ordering. Check is enabled only with all four placed; wrong orders stay visible/editable.
Help points to one position. Correct Check reaches shared completion/balloons with Again/Home.

The vertical layout scrolls; wrapped button text grows naturally with 56dp minimum targets.
Listen labels identify their step without an extra content description; they never place
items. Keyboard tests establish Keyboard input mode, verify focus and exact single append.
Opening is silent; explicit Listen/Replay/Help use existing speech policy. Sound Off preserves
visual controls but a non-reader needs spoken/adult support to understand this text-only
starter. Home/back and Options preserve work; process restoration uses the exact journal.

## Owner clock presentation feedback — 2026-10-10 (draft, validation pending)

The shared Explore/Make This Time clock face draws minute numbers 0–59 in an outer
ring, with larger bold five-minute landmarks and an inner green 1–12 hour ring.
The supplied dinosaur outline green (#284F26) and dragon outline red (#7A1D18) link
face numbers, corresponding hand lines, digital hour/minute spans and labelled
short-hour/long-minute legend. Colour is reinforced by words and hand length.
Minute labels have a white halo where decorative artwork crosses the ring. They
remain canvas decoration inside the single existing accessible clock target.
Practice still exposes digital time only after a correct Check. The semantic time,
drag geometry, snapping, task selection, speech, attempts and persistence are unchanged.
Narrow-screen label readability and overlap at representative times require rendered
EN/DE large-text portrait/landscape review before declaring visual acceptance.

# Colours matching and four-colour Sort routes — Phase 5A.2 (2026-10-23)

The production Home Colours entry still opens `COLOURS_CHOOSER`. Explore and Find
retain `COLOURS_EXPLORE` and `COLOURS_FIND`. Match uses the distinct
`COLOURS_MATCH` destination; Four-Colour Sort uses `FOUR_COLOUR_SORT`. Sort from
the chooser continues to use `COLOUR_SORT_FROM_CHOOSER`, while the historical
`COLOUR_SORT` destination still restores the original two-colour activity directly.
All five chooser destinations return to the chooser on Back; chooser Back returns
to native Home, explicit Home returns to illustrated Home, and Options restores the
originating route. No persisted route identity was renamed or repurposed.
# Colours chooser routes — Phase 5A.1 (2026-10-22)

The production Home Colours entry opens `COLOURS_CHOOSER`. The new `COLOURS_EXPLORE`
and `COLOURS_FIND` destinations return to that chooser on Back. Sort launched from
the chooser uses `COLOUR_SORT_FROM_CHOOSER` so Back returns to the chooser. The
pre-existing `COLOUR_SORT` route remains a direct Colour Sort destination for saved
route compatibility. Chooser Back returns to native Home; explicit Home returns to
illustrated Home. Options remains an overlay that restores its originating route.
No route identifier or persisted destination format was migrated.

# Animals & Nature — Phase 5C.2–5C.4 Explore Habitats

The illustrated Home’s Animals & Nature category includes **Explore Habitats**
alongside the existing Animal Actions and Animal Groups activities. Explore
Habitats offers Ocean, Woodland and Underground as active choices. Ocean opens a
native, unscored scene with the canonical `animal.dolphin` target. Woodland opens
`woodland_008_homes_choices.png` with canonical `animal.rabbit` and stable
`animal.fox` targets. Underground uses a fitted 4:3 cutaway with separately
layered `animal.mole`, `animal.rabbit` and `animal.earthworm`. All scenes provide
image-relative hit regions, large labeled alternatives, selected outlines,
bilingual names/facts, replay, Back to habitat chooser and Home. Options overlays
and language changes restore the current route and selected identity. Sound Off
is respected by the shared offline audio controller. Explore routes emit no
attempts, completion, journals or Progress Tracker identities. Original Animal
Groups routing and its historical saved-session contract remain unchanged.
Freshwater and habitat matching are not offered as active routes.
