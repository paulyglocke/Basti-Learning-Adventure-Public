# Basti’s Learning Adventure — Master Product & Learning Roadmap

## Purpose

A calm, audio-first, visually rich school-readiness app focused on communication, listening, language, early maths, school routines, self-regulation, curiosity and confidence.

The app should increasingly feel like a coherent learning adventure rather than a collection of quizzes: short activities, familiar characters and animals, shared learning systems, meaningful missions, calm support, and progression that works fully offline.

## Core principles

- Audio-first; a child should be able to complete activities without reading independently.
- Short, predictable 3–5 minute activities.
- No lives, forced timers, punishment, loss streaks or reaction-speed pressure.
- Fully offline; no Google Play Services dependency.
- Native Android / Jetpack Compose; continue removing runtime WebView/HTML/JavaScript dependencies as native replacements become trusted.
- Native 2D / 2.5D presentation. Do not introduce a true 3D engine without a concrete learning/product need.
- Custom original artwork in final production; emoji are temporary placeholders only.
- One clear learning job at a time.
- Calm, immediate feedback. Mistakes normally lead to replay, support and another try rather than penalties.
- Support is not failure. Record how much help was needed rather than treating supported success as incorrect.
- Generalise skills across varied contexts rather than drilling one memorised pairing.
- Reuse shared semantic content, art, audio and learning systems instead of rebuilding them per activity.
- Movement and real-world tasks are regular learning modes, not rewards for losing focus.
- Open-ended speech encourages talking without automatic pronunciation scoring.
- During thinking moments, reduce irrelevant animation. Motion should explain, guide or celebrate.
- Build shared abstractions only when a second real use needs them.

## Roadmap operating model

This roadmap owns product direction, learning priorities and sequencing. Detailed implementation contracts remain in the specialist specifications:

- `CONTENT_DATA_SPEC.md`
- `NATIVE_ARCHITECTURE_SPEC.md`
- `VOICE_AUDIO_SPEC.md`
- `PROGRESS_TRACKER_SPEC.md`
- `UX_NAVIGATION_SPEC.md`
- `GAME_DESIGN_SPEC.md`
- `ART_DIRECTION.md`
- `TESTING_QA_SPEC.md`

Do not duplicate low-level contracts here when a specialist spec already owns them.

## Shared learning grammar

Where appropriate, curriculum should follow:

**Explore / Hear → Recognise / Find → Recall / Use → Generalise**

For early maths and other concept-heavy areas, also use:

**Concrete → Pictorial → Abstract**

For supported practice, use the universal support ladder:

**Independent attempt → Replay → subtle verbal/visual hint → stronger model/help → optional parent help**

Progress should record the least support needed for successful completion.

For generated choice tasks, prefer **pedagogically meaningful distractors** over random unrelated answers. Wrong choices should represent plausible misunderstandings when the skill allows this.

## Learning pillars

1. Talk & Communicate
2. Listen & Follow
3. Letters & Sounds
4. Words & Vocabulary
5. Numbers & Maths
6. Think & Solve
7. World Around Me
8. School Skills
9. Compare & Discover
10. Move & Learn
11. Play
12. Discovery Book

## Talk & Communicate

Priority activities:
- Let’s Talk / Erzähl mal
- Tell Me More
- Sentence Builder
- Describe It
- Question Time
- Story Retell
- Finish My Sentence
- Conversation Starter
- Say a Little More
- What’s in the Bag?
- Yesterday / Tomorrow Talk
- My Turn / Your Turn
- Listen for the Sound

Model and expand language naturally. For example: “Snake” → “A green snake” → “The green snake is slithering.” The goal is richer communication, not speech scoring.

## Listen & Follow

Progress from:
1. one-step object instructions
2. adjective + object instructions
3. two-step instructions
4. spatial placement
5. multi-attribute placement
6. mixed action + placement
7. simple inhibition instructions

Reuse the same instruction/action system for Remember the Mission, School Skills, Focus & Flex and compatible movement tasks.

## Letters & Sounds

Keep written letter work separate from phonics.

Phonological-awareness progression:
1. environmental and animal sounds
2. rhyme
3. syllables
4. same initial sound
5. identify an initial sound
6. reviewed grapheme ↔ phoneme links

Critical rule:

**grapheme ≠ phoneme ≠ letter name**

Use normal TTS for words/sentences where appropriate. Isolated phonemes should use reviewed language-specific recorded clips rather than Android letter-name TTS.

English and German phonics content must be reviewed independently. A good English example is not automatically a good German example.

## Words & Vocabulary

Grow toward roughly 50–80 high-value reviewed shared words through normal curriculum development rather than one giant vocabulary project.

Core areas:
- school
- home
- people
- food
- clothes
- body
- feelings
- weather
- transport
- colours
- shapes
- positions
- actions
- describing words
- opposites

Interest areas:
- dinosaurs
- dragons
- snakes
- Komodo dragons
- crocodiles/alligators
- sharks
- whales
- sea animals
- prehistoric sea reptiles

Each reusable content item should support stable semantic ID, EN/DE labels, illustration, spoken form, simple explanation, example sentence, category and progress/context metadata.

## Numbers & Maths

Prioritise concepts over ever-larger numerals:

- subitising 1–3, then 1–5
- number ↔ quantity
- more / fewer / same
- making 5 and early number bonds
- part-whole composition/decomposition
- one more / one less
- conservation of quantity
- meaningful number stories
- counting and number recognition
- number order
- before / after
- missing numbers
- concrete addition/subtraction
- estimate then count/check
- patterns
- shapes
- spatial reasoning
- measurement concepts
- simple charts/data

Useful future mechanics:
- tap counting
- T-Rex stomp counting
- number line movement
- object grouping
- build/construct quantities
- simple pattern reproduction
- concrete → pictorial → numeral progression

Procedural generation should be deterministic and introduced only when enough Maths activities exist to justify a shared generator.

## Think & Solve / Focus & Flex

Use playful, non-clinical practice for working memory, inhibition, rule use and flexible switching.

Patterns:
- Do What I Say
- Opposite Game
- Rule Switch
- Freeze & Move
- Remember the Mission
- Plan the Mission
- sequencing
- classification
- True or Silly?
- Spot the Mistake
- Sort & Switch

Do not present these as ADHD treatment or generic “brain training”.

## World Around Me

Areas:
- time
- weather
- home
- school
- playground
- nature
- habitats
- transport
- people who help us
- belonging / My World
- simple science investigations

### Wilma Weekdays / Wochentage

Use the familiar Kita Wilma caterpillar.

Fixed day colours:
- Monday / Montag — green
- Tuesday / Dienstag — red
- Wednesday / Mittwoch — yellow
- Thursday / Donnerstag — blue
- Friday / Freitag — purple
- Saturday / Samstag — orange
- Sunday / Sonntag — pink

Progression:
- explore/hear days
- find requested day
- before/after
- order seven days
- today/yesterday/tomorrow with an explicit visual/spoken anchor

Colour is a familiar cue, never the sole identifier.

### Seasons / Jahreszeiten

Use the existing canonical lakeside assets and observable seasonal clues.

Cycle:
**Spring → Summer → Autumn → Winter → Spring**

Progression:
1. Explore / recognise
2. What comes next?
3. What comes before?
4. Build the Year
5. Missing season
6. Identify from observable clues
7. Match season to clue
8. Combined before/after

Treat Spring → Summer → Autumn → Winter as the conventional display sequence while explicitly teaching that seasons are cyclic.

## School Skills

Prioritise practical classroom language and self-advocacy:

- sit down / stand up
- listen / look / wait
- open / close book
- take / put away pencil
- raise hand
- line up
- your turn / my turn
- finished / again
- I don’t understand
- Can you say that again?
- Can you help me?
- Which one?
- Where should I put it?
- It’s too loud.
- Can I have a break?

Prefer contextual Follow-the-Instructions scenes over isolated flashcards.

## Feelings & self-regulation

Include:
- happy
- sad
- angry
- worried
- scared
- excited
- frustrated
- tired
- surprised

Use My Turn / Your Turn, Stop–Think–Choose and self-advocacy language. Avoid implying that one social response is always universally correct.

## Compare & Discover

Use shared animal/knowledge records for:
- bigger / smaller
- longer / shorter
- faster / slower
- heavier / lighter
- venomous / non-venomous
- land / water / air
- extinct / living
- habitat
- classification

Precise facts belong in shared data, not hardcoded question prose.

## Reusable learning systems

### Sequencing

Use the proven Wilma → Seasons tap-to-place model for:
- weekdays
- seasons
- numbers
- routines
- simple stories

Drag may enhance later but must never be the only interaction.

### Instruction / action

Follow the Instructions is the first major consumer.

Grow deliberately into:
- multi-step actions
- spatial placement
- delayed instructions
- inhibition
- Remember the Mission
- Focus & Flex
- selected Move & Learn prompts

### Semantic matching

Memory Pairs should match stable semantic relationships, not display strings.

Future variants:
- picture ↔ picture
- picture ↔ word
- EN ↔ DE
- number ↔ quantity
- uppercase ↔ lowercase
- animal ↔ action
- animal ↔ habitat
- emotion ↔ expression
- season ↔ clue

### Shared support presentation

Build one consistent way to expose:
- Replay
- Need help?
- visual/concrete hint
- stronger model
- optional parent help

Do not make every activity invent its own help UI.

### Shared support settings

Introduce only concrete settings needed by real features:
- Reduced motion
- Calm celebrations
- Help sooner
- Simpler/literal instructions
- Visual steps
- Larger controls/text where useful

Use experience-based labels, not diagnosis-labelled modes.

### Shared knowledge

Vocabulary, Discovery Book, Compare & Discover, games and rewards should consume the same canonical content records and artwork.

## Game mechanic catalogue

Before inventing another custom activity architecture, check whether the learning goal fits an established mechanic:

- choice
- semantic matching
- ordering
- sequence recall
- tap counting
- number line
- trace path
- place / move object
- sort / classify
- odd one out
- build / construct
- mission / exploration
- sound identification
- movement mission

These are reusable mechanics, not one giant generic game engine.

## Game direction

Ordinary activities should remain native 2D Compose.

Larger adventure experiences may use layered/parallax/isometric **2.5D** presentation where it adds value.

Do not adopt Unity, a true 3D engine or heavy real-time 3D rendering without a specific demonstrated requirement.

Responsive animation is welcome; reaction-time pressure is not.

### Memory Pairs

A workhorse shared game using semantic matching. No move-count or completion-time pressure.

### Dinosaur Rescue

Mission-led learning rather than visible quizzing. Example:
- find the missing egg
- count objects
- follow spatial instructions
- execute two-step missions
- combine vocabulary, colour, quantity and listening

### Dragon Treasure Hunt

Use instructions, matching, numbers, sorting, memory and simple planning inside a coherent mission.

### Build the Bridge

Use construction as feedback. If five planks are needed and four are placed, show the missing space and model “one more” rather than displaying a punitive error state.

### Crocodile Snap

Fast-feeling recognition with satisfying animation, but no countdown or reaction-time scoring.

### Other later game ideas

- Mosasaurus Ocean Hunt
- Pteranodon Letter Flight
- Snake Path
- Feed the T-Rex
- Dinosaur Café
- Dragon Doctor
- Komodo Expedition
- Make a Scene

## Move & Learn

Use movement regularly.

Examples:
- T-Rex Counting
- Pteranodon Directions
- Dragon Number Bonds
- Snake Rhythm
- Freeze Animals
- classroom movement instructions
- real-world find missions

Sensor scoring is not required. A parent/child Continue action is enough.

Optional future sensor/tap enhancement may be explored only when the same activity remains usable without it.

## Verb Explorer

Move toward native action-specific animation:
- jump
- slither
- dig
- snap
- build
- fly
- swim

Prioritise 15–20 common verbs first. Local video may remain appropriate where it demonstrably teaches the movement better.

## Predictable activity structure

Preferred flow:

**Listen → Try → Support if needed → Celebrate → Finished**

Use small concrete progress cues where useful. Avoid daily streak pressure.

Child-controlled continuation is preferred after meaningful feedback; do not rapidly auto-advance just because an answer was correct.

## Completion celebration

The shared native completion celebration is part of the accepted native experience.

Requirements:
- optional playful interaction
- controls always remain usable
- no score/mastery coupling
- reward interaction is non-durable
- Sound Off blocks celebration audio
- use clean canonical animal art
- keep motion calm
- do not re-investigate already-fixed artwork unless a regression appears

Future improvement:
- select five distinct animals deterministically from the canonical available animal pool per completed round/session
- same completed round reproduces the same assignments
- new rounds vary naturally
- no duplicate within one celebration

## Today’s Adventure

Long-term primary child-facing entry.

### v1 — Curated route

No adaptive algorithm required.

A short route should normally contain:
- 1 confidence task
- 1 focus/listening task
- 1 communication/learning task
- 1 game/movement task

The child may stop after any module. No missed-day penalties.

### v2 — Progress-informed

Once enough evidence exists:
- mix secure and developing skills
- schedule spaced review
- vary contexts
- avoid repetitive drilling
- include movement and communication regularly

## Discovery Book

Treat Discovery Book as a presentation layer over shared knowledge, not a parallel fact database.

Animals, vocabulary, actions, habitats and facts learned elsewhere should naturally surface here.

# Development priorities

## P0 — Trusted daily build

Finish and trust the current native core before broad expansion.

Current goals:
- complete the in-progress Seasons next/before/Build-the-Year work
- fix Wilma ordering auto-follow/auto-shift
- preserve the already-fixed completion artwork
- record the proven same-key S24 upgrade in existing docs
- run one complete Samsung S24 Ultra acceptance/debug sweep
- run the equivalent Fire Max acceptance sweep
- fix only meaningful crashes, persistence, audio ownership, accessibility, lifecycle and layout defects found by those sweeps

Already proven:
- stable signing identity
- same-key higher-version S24 in-place upgrade
- shared native completion celebration
- corrected celebration animal artwork on physical S24

Once S24 and Fire acceptance pass, signing/versioning becomes maintenance infrastructure.

**P0 exit condition:** current native activities are trustworthy enough for regular daily use on both target devices.

## P1 — Shared native experience

Build the small reusable systems that make later curriculum cheaper and more consistent.

1. **Lightweight visual design system**
   - primary / secondary / navigation buttons
   - choice/image cards
   - activity headers
   - progress markers
   - feedback
   - completion layout
   - spacing / typography
   - restrained press and motion feedback
   - comfortable 48–56dp touch targets

2. **Shared support presentation**
   - Replay
   - Hint
   - stronger model/help
   - parent-help path where appropriate
   - integrate with existing progress/support evidence

3. **Minimal support-settings contract**
   - reduced motion
   - calm celebrations
   - help sooner
   - simpler/literal prompts
   - visual steps
   - larger controls/text where useful

4. **Canonical art/content completion**
   - finish remaining animal assets
   - keep one canonical reusable asset/record per concept where practical
   - generalise celebration pool to deterministic five-distinct-animal selection

5. **Sequencing reuse**
   - finish the Wilma → Seasons extraction only as far as the second real use justifies

6. **Follow the Instructions**
   - establish the shared instruction/action engine

7. **Memory Pairs**
   - establish semantic matching relationships and the first reusable matching UI

8. Extend schemas only when concrete second uses require it.

Do not build speculative mega-frameworks.

## P2 — Core school-readiness curriculum

Use the P1 systems to add breadth.

Priority order:
- finish remaining Seasons progression: missing season, clue recognition, clue matching, combined before/after
- Follow the Instructions + practical School Skills
- vocabulary growth through all new curriculum
- Tell Me! / expressive-language v1
- Memory Pairs variants
- conceptual native Maths
- Letters & Sounds / phonological awareness
- Wilma today/yesterday/tomorrow with explicit anchor
- Focus & Flex variants
- feelings and self-advocacy
- Compare & Discover

Maths should use Concrete → Pictorial → Abstract where appropriate.

Choice tasks should use meaningful distractors where practical.

Phonics should use reviewed phoneme audio and language-specific mappings.

Keep Progress Tracker collecting useful evidence, but delay a large parent dashboard until the curriculum is broad enough for its summaries to be meaningful.

## P3 — Learning world and polished adventure

Turn the learning core into a coherent child-facing adventure.

Priority areas:
- Today’s Adventure v1
- Today’s Adventure v2 after enough evidence exists
- Discovery Book
- Dinosaur Rescue
- Dragon Treasure Hunt
- Build the Bridge
- Crocodile Snap
- number-line and tap-counting Maths mechanics
- sequence-recall activities
- tracing / pre-writing mechanic where useful
- native/action-specific Verb Explorer polish
- real-world and Move & Learn missions
- broader original illustration and 2.5D/parallax/isometric polish where useful
- full accessibility/reduced-motion/performance polish
- coordinated Home redesign, branding, icon and native splash

The splash must use Android native splash behavior, add no artificial wait and transition directly to Home.

# Internal milestones

## Milestone A — Native Core

Trusted native Prepositions, Seasons, Wilma and Vocabulary; shared content/audio/session/progress foundations; stable update stream; shared completion; physical S24 + Fire acceptance.

## Milestone B — School Ready

Follow Instructions, practical School Skills, richer Seasons, broader Vocabulary and early conceptual Maths.

## Milestone C — Learning World

Tell Me!, Memory/matching, Focus & Flex, feelings/self-advocacy, Compare & Discover and Discovery Book foundations.

## Milestone D — Polished Adventure

Today’s Adventure, mission-led games, cohesive 2D/2.5D presentation, app-wide original art/animation, accessibility/performance polish, splash/branding and final cross-device acceptance.

# Delivery strategy

Prefer batched physical acceptance over repeatedly testing one feature in isolation.

Prefer content reuse over standalone packs.

Prefer one canonical art/content record per reusable concept.

Prefer deterministic generators and exact restore over ad-hoc randomness.

Prefer child-controlled continuation after meaningful feedback.

Prefer game mechanics that embed learning in a mission over obvious quiz wrappers where practical.

## Open-source reference principles

External projects may be studied for ideas, but Basti’s specifications and architecture remain authoritative.

Useful reference areas:
- **Kids Math Pup Tutor** — progressive help, accessibility, adaptive Compose layouts
- **Multiply** — plausible distractors, generator ideas, StateFlow + one-shot effects
- **Joju Learn** — semantic match groups, Concrete/Pictorial/Abstract, Explore/Recognise/Recall
- **AI4Kids Android** — child UI ideas and phoneme/audio design reference only
- **z.Mantra** — accessible touch/audio/movement interaction ideas
- **Oppia Android** — mature offline learning/state/testing reference
- **eduActiv8** — curriculum/activity taxonomy reference

Licensing discipline:
- permissively licensed code may be adapted only with required notices/attribution
- GPL/unlicensed projects are idea/reference sources unless explicitly approved
- external artwork/content is not copied merely because the repository is public
- do not transplant another app’s architecture wholesale

## Agent prompt guidance

Future Astra prompts should normally emphasise:
- architecture and state
- deterministic generation
- durable restore
- semantic IDs
- progress/support integration
- audio ownership
- lifecycle
- reusable sequencing/matching/instruction models
- phoneme/content architecture
- difficult regression fixes

Future Luna prompts should normally emphasise:
- bounded Compose UI implementation
- visual design-system components
- adaptive layouts
- accessibility
- Memory Pairs presentation after the model exists
- content packs
- bounded game presentation
- UI/component tests

Shared prompt rules:
- existing Basti specs remain authoritative
- preserve current architecture
- no timers/lives/streak pressure
- no second state/progress/audio architecture
- meaningful distractors where applicable
- semantic matching, never localized-string equality
- 2D/2.5D by default
- support reduced motion
- do not commit/push unless explicitly requested

## Reasoning prompts

Occasionally add unscored follow-ups such as:
- “How did you know?”
- “Why do you think that?”
- “Can you explain?”

These encourage reasoning and expressive language and must never become automatic correctness scores.
