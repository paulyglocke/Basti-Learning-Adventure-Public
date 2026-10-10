# Months v1 — Architecture research and implementation plan

The recommended first slice is **Months Explore: canonical bilingual content, an interactive Year Wheel, accessible month controls, existing TTS, and silent restoration**. Build the quiz modes incrementally on the existing session infrastructure afterward.

Inspected local `main` at `f3e3a106197b1254693f47e12bf0ec5c7d5a59c6`. The working tree is clean. No files changed, tests run, commits or pushes made.

### 1. Inspection findings and reuse

The roadmap explicitly calls for Explore, Find, Before/Next, Missing, progressive ordering and Months ↔ Seasons. It also reserves “Between” and broader calendar representations for further progression. See [Months roadmap](MASTER_PRODUCT_LEARNING_ROADMAP.md#months).

| Existing implementation | Recommended reuse |
|---|---|
| `ContentId`, `ContentText`, `LocalizedText`, `BundledContentRepository` | Stable semantic identities, separate display/speech text, validated local content |
| [SeasonsCycle](app/src/main/java/com/bellfamily/bastischool/learning/seasons/SeasonsCycle.kt#L15) | Derive preceding/following answers from canonical order |
| `SeasonsMissing` | One-gap questions, authored validation and separate activity identity |
| [DurableSessionHost](app/src/main/java/com/bellfamily/bastischool/learning/session/DurableSessionHost.kt#L10) | Exact checkpoints, pending progress delivery, stale-writer protection, Retry Save and fresh Again |
| `SessionReducer`, `SessionProgressRecorder` | Answer attempts, Retry, Help, language changes, completion and deduplication |
| Wilma/Seasons selection stores | Small atomic Explore checkpoint without learning evidence |
| `WilmaAudio`, `DefaultAudioController` | Thin activity adapter, existing engine/policy, cancellation and silent restore |
| Clock presentation | Semantic-state-driven geometry, Canvas presentation and accessible alternatives—not clock time or drag calculations |
| Shared native components | Title, question progress, choices, support, actions and completion |
| `OrderedPlacement` | Correct-prefix tap-to-place ordering |
| `SequenceAssembly` | Reserve for a later freely editable build-and-check mode; unnecessary for the proposed v1 interaction |

Three limits matter:

- `ChoiceQuestion` accepts **2–8 choices**, so a twelve-answer Find question is not a direct fit.
- `OrderedPlacement` and `SequenceAssembly` accept **2–10 items**.
- Task ordinals stop at ten, and completion evidence accepts only **4, 5, 7, 8 or 10 tasks**. Thus **3-, 6- and 12-month ordering boards need deliberate progress changes**, not just a larger UI.

These are explicit constraints, not confirmed defects. See [session bounds](app/src/main/java/com/bellfamily/bastischool/learning/session/SessionModels.kt#L18), [placement bounds](app/src/main/java/com/bellfamily/bastischool/learning/sequencing/OrderedPlacement.kt#L16), and [completion bounds](app/src/main/java/com/bellfamily/bastischool/learning/progress/ProgressModels.kt#L68).

### 2. Canonical month model

Use an explicit January–December list of semantic IDs:

`month.january` through `month.december`.

Never derive order from translated labels, enum ordinals, device locale or today’s date.

| English | German |
|---|---|
| January | Januar |
| February | Februar |
| March | März |
| April | April |
| May | Mai |
| June | Juni |
| July | Juli |
| August | August |
| September | September |
| October | Oktober |
| November | November |
| December | Dezember |

Provide:

- `previous(month)` and `next(month)` with cyclic wraparound.
- `season(month)` returning existing `SeasonIds`.
- Explicit ordered slices for missing/order tasks.
- Full month names for display and speech.

Keep the Months content pack independently versioned. Reuse existing season definitions without bumping `CoreContent` and invalidating unrelated saved rounds.

Meteorological mapping:

| Season | Months |
|---|---|
| Winter | December, January, February |
| Spring | March, April, May |
| Summer | June, July, August |
| Autumn | September, October, November |

Parent-facing wording should explain that these are meteorological teaching seasons used in Germany; actual weather and astronomical season boundaries differ. No date engine is needed.

### 3. Modes and learning progression

Use one Months route, with a small mode selector:

`EXPLORE`, `FIND`, `RELATIONS`, `MISSING`, `ORDER`, `SEASONS`.

Recommend progression, but do not lock children into it or infer mastery from browsing.

| Mode | Interaction and wording | Session approach |
|---|---|---|
| **Explore / Entdecken** | Select a month; highlight it and speak its name. “Choose a month.” / “Wähle einen Monat.” | No questions, score or completion |
| **Find the Month / Finde den Monat** | “Find March.” / “Finde März.” Spoken instruction with visible equivalent; four month choices | Standard 5/10 questions |
| **Before and Next / Davor und danach** | “Which month comes immediately before March?” / “Welcher Monat kommt direkt vor März?” Corresponding “after” / “nach” questions | Mixed directions; 3/2 or 2/3 in five questions, 5/5 in ten |
| **Missing Month / Fehlender Monat** | “Which month is missing?” / “Welcher Monat fehlt?” Three or four positions, exactly one gap | Standard 5/10 questions |
| **Build the Year / Das Jahr ordnen** | Tap the next month into the next slot; correct placements remain | One board of 3, 4, 6 or 12 placements—not an artificial five-question round |
| **Months and Seasons / Monate und Jahreszeiten** | “Which season does January belong to?” / “Zu welcher Jahreszeit gehört Januar?” Four season choices | Standard 5/10 questions |

For Find, use four accessible choices rather than silently expanding the shared quiz model to twelve. The Year Wheel remains the activity’s central representation; full-wheel recognition can be a later, explicitly reviewed challenge.

For ordering, recommend **correct-prefix placement**, matching Wilma and Seasons:

- Clearly identify the starting month for partial sequences.
- Full-year boards begin with January.
- Wrong choices leave already placed months intact.
- Retry and Help do not count as new answer attempts.
- No dragging required.

Difficulty should be explicit and frozen for the round:

- Missing: three-month non-wrapping sequences first; later four positions, varied gap positions and December/January crossings.
- Ordering: 3 → 4 → 6 → 12.
- Do not introduce an adaptive difficulty engine in v1.

### 4. Deterministic generation

Use finite candidate catalogues and seeded selection/shuffling.

Rules:

- Four unique choices, correct answer exactly once.
- Correct answer positions shuffled independently of content.
- No repeated task definitions within a round when the pool permits.
- Before/Next includes balanced directions and tested boundary candidates.
- Missing task identity encodes sequence start, length and gap position.
- Months/Seasons includes every season in a five-question round.
- Distractors include plausible neighbouring months, not only obviously unrelated options.

The shared candidate generator handles ordinary selection and answer shuffling. Where it does not guarantee direction or season balance, add a small Months-specific planner returning the same `SessionPlan`; do not alter generation for existing activities.

A five- or ten-question round cannot cover twelve distinct target months. Document this honestly: Explore exposes all twelve, while quiz rounds sample the catalogue. Cross-session guaranteed coverage would need additional durable scheduling and is not essential v1.

### 5. Year Wheel design

Use a stationary twelve-segment ring:

- January at the top; chronological order clockwise.
- December visibly adjacent to January.
- Four season regions; winter continues across the year boundary.
- Selected month identified by outline and a full-name readout, not colour alone.
- Season labels or a legend supplement colour.
- No decorative assets required initially.

**Do not rely on twelve wedges as the only controls.** At an illustrative 280dp diameter, each wedge has only about 52dp of arc at a 100dp radius, and much less nearer the centre. Full German labels and enlarged fonts make these targets unreliable.

Recommended companion:

- Twelve full-name month buttons in chronological order below the wheel.
- Responsive columns, falling back to one column for narrow/large-text layouts.
- Content-driven heights and existing minimum touch targets.
- Taps update the same selected `MonthId` as wheel taps.
- On narrow screens, compact wheel labels may be abbreviated; full names remain available outside the wheel.
- No rotated full-length text that becomes difficult to read.

Accessibility:

- One descriptive wheel node plus the accessible month-button collection.
- Avoid announcing duplicate copies of all twelve controls.
- Selected state exposed semantically.
- Predictable January–December keyboard/TalkBack order.
- Pointer hit-testing may select wedges, but assistive users never need precise circular targeting.

In quizzes, do not display a complete answer-revealing sequence or month-to-season legend by default. Showing the teaching wheel through Help should be recorded as support.

### 6. State, restoration, progress and audio

**Explore**

Persist selected month and mode using the existing bounded atomic browsing-store pattern. Language/audio remain owned by existing app settings. All highlights and geometry derive from semantic selection.

No mastery or attempt evidence for selection, listening or visiting the screen.

**Choice modes**

Separate identities and journals, for example:

- `activity.months.find`
- `activity.months.relations`
- `activity.months.missing`
- `activity.months.seasons`

Reuse `DurableSessionHost` unchanged. Store the exact task plan, answer order, language, attempts, support and completion. Restore the snapshot rather than regenerate from its seed.

Suggested skills distinguish recognition, before, after, missing and season association. Context distinguishes the representation actually used; do not claim generalisation from repeated use of one wheel.

**Ordering**

Use a small Months adapter around `OrderedPlacement` and the existing ordering journal pattern.

Before implementing all board sizes, narrowly extend:

- Placement capacity to twelve.
- Task ordinal capacity to twelve.
- Completion counts to include 3, 6 and 12.
- Corresponding codec validation and regression tests.

Retain strict activity-specific validation and existing serialized field layouts where possible. Old records must remain readable; document that older app versions may reject newly introduced record sizes. Do not pad boards with fictional tasks or split a completed twelve-month year into misleading evidence.

**Audio**

Reuse one owner-local existing controller, not another TTS system:

- Explicit month tap: manual option speech.
- Replay: current instruction.
- New question: existing instruction policy.
- All: normal eligible speech.
- Questions: instructional automatic speech and permitted explicit listening.
- Off: silence, including manual Listen.
- Navigation, language changes and backgrounding cancel obsolete speech.
- Opening/restoration remains silent.

Missing offline voices must show existing recovery guidance without blocking visual use.

### 7. Proposed files

Create files only as their consumer is implemented.

| Proposed file | Responsibility |
|---|---|
| `learning/models/MonthDefinition.kt` | Typed bilingual month record compatible with existing content definitions |
| `learning/months/MonthContent.kt` | IDs, canonical order, neighbours, season mapping, local repository |
| `learning/months/MonthsWording.kt` | Shared prompts, hints, feedback and accessibility wording |
| `learning/months/MonthsSelectionStore.kt` | Explore/mode checkpoint |
| `learning/months/MonthsAudio.kt` | Thin adapter over existing controller |
| `ui/months/MonthsViewModel.kt` | Serialized work, mode ownership, settings and lifecycle |
| `ui/months/MonthsScreen.kt` | Mode navigation and shared native presentation |
| `ui/months/YearWheel.kt` | Derived geometry, rendering and pointer selection |
| `learning/months/MonthsQuestions.kt` | Choice catalogues, balanced generation and strict validators |
| `learning/months/MonthsOrder.kt` | Months-specific ordering contract |
| `learning/months/MonthsOrderHost.kt` | Durable ordering checkpoint and progress delivery |

Add narrowly scoped shell/navigation wiring. Prefer entry through the existing Days & Seasons area rather than adding another competing Home destination.

### 8. Test and acceptance strategy

| Layer | Required coverage |
|---|---|
| Content | Twelve unique IDs/names; all previous/next mappings; both year boundaries; exact season partition |
| Generation | Determinism, balance, unique choices/tasks, correct answer once, valid gaps, sensible distractors |
| Reducer | Wrong → Retry → correct, Help independent of attempts, preserved placements, completion and fresh Again |
| Persistence | Exact partial/completed restore, language/support, corrupt rejection, stale writers, failed-write redelivery and deduplication |
| Shared regression | Existing Wilma/Seasons ordering and old progress records after any bounds extension |
| Audio | EN/DE requests, policy matrix, silence on restore, cancellation and missing voices |
| Compose | Actual text bounds, wheel selection, companion controls, keyboard activation, scrolling and completion |
| Layout | EN/DE, narrow portrait, both landscapes, tablet; 1×, 1.5× and 2× fonts |
| Physical | S24 and Fire Max touch ergonomics, TalkBack, offline voices, interruption/restore and child-use clarity |

Run focused JVM tests, affected shared tests, Android-test compilation, focused emulator instrumentation, full JVM/build/lint before delivery.

Compilation is not instrumentation execution; emulator results are not physical acceptance. Preserve the existing acceptance ledger rather than extending historical S24 passes to Months. See [QA acceptance rules](TESTING_QA_SPEC.md#current-physical-acceptance-ledger-and-fire-max-checklist--2026-09-24).

### 9. Implementation phases and review decisions

1. **Explore foundation:** canonical content, Year Wheel, companion controls, audio, browsing persistence and route.
2. **Recognition and relationships:** Find plus Before/Next, four-choice durable sessions.
3. **Missing Month:** explicit difficulty levels and sequence semantics.
4. **Build the Year:** reviewed bounds/progress extension, then 3/4/6/12 boards.
5. **Months and Seasons:** association questions and parent-facing convention.
6. **Acceptance:** cross-mode regression and physical-device review.

The main review decisions are:

- Accept four-choice Find for v1 rather than twelve simultaneous quiz answers.
- Accept companion controls as essential on small screens.
- Choose correct-prefix ordering rather than freely editable construction.
- Review the shared progress-bound extension before ordering implementation.
- Confirm readable wheel labels and season cues with Basti on actual devices.

Optional later enhancements: seasonal artwork, Between, bidirectional season-to-month selection, drag ordering, birthday landmarks, calendar grids, broader representation generalisation and adaptive scheduling.

**First-slice acceptance:** all twelve months selectable through touch and accessible controls; wheel and full-name readout always agree; EN/DE listening follows policy; selection survives recreation silently; enlarged text remains readable; no quiz/progress evidence is fabricated. This proves the new representation without prematurely expanding shared session infrastructure.
