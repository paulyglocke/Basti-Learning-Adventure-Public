# GAME_DESIGN_SPEC.md

# Basti’s Learning Adventure — Game Design Specification

## 1. Purpose

This document defines the shared design and implementation direction for the game layer of **Basti’s Learning Adventure**.

The games must support the wider learning roadmap rather than becoming isolated mini-apps. Each game should feel distinct, but they should share the same content, audio, progress, rewards, accessibility, and visual systems.

This specification should be read together with:

- `AGENTS.md`
- `MASTER_PRODUCT_LEARNING_ROADMAP.md`
- `PROGRESS_TRACKER_SPEC.md`
- `ART_DIRECTION.md`

Shared runtime boundaries and migration gates live in [NATIVE_ARCHITECTURE_SPEC.md](NATIVE_ARCHITECTURE_SPEC.md); canonical schemas/IDs in [CONTENT_DATA_SPEC.md](CONTENT_DATA_SPEC.md); navigation in [UX_NAVIGATION_SPEC.md](UX_NAVIGATION_SPEC.md); acceptance gates in [TESTING_QA_SPEC.md](TESTING_QA_SPEC.md). Package/type/short skill-ID examples below are conceptual and map to those shared contracts.

The roadmap defines **what the app should teach**.

This document defines **how the games should turn those learning goals into playable experiences**.

---

# 2. Core game philosophy

The games should feel playful, adventurous, visual, and rewarding without becoming stressful.

The design principles are:

- no lives
- no forced timers
- no loss streaks
- no punishment
- no “you failed” states
- no requirement to read independently
- no requirement for fast reaction time
- no advertising
- no cloud dependency
- no account requirement
- fully offline
- English/German support
- large child-friendly touch targets
- clear audio replay
- calm feedback
- short sessions
- predictable structure
- custom illustrated production artwork
- native 2D/2.5D presentation
- gradual difficulty
- skill-based progress
- shared learning content

The app should reward trying, listening, communicating, remembering, reasoning, and completing activities.

A mistake should normally lead to:

> replay → hint → another try

rather than:

> error → penalty

---

# 3. Games should share systems, not identical mechanics

Do **not** force every game into a generic multiple-choice quiz engine.

Different games need different state machines.

For example:

- Memory Pairs needs card state and matching logic.
- Follow the Instructions needs instruction/action state.
- Dragon Treasure Hunt needs mission/scene state.
- Dinosaur Rescue needs story/progression state.
- Build the Bridge needs construction state.
- Crocodile Snap needs recognition/selection state.

However, all games should share:

- content
- vocabulary
- animal definitions
- scene objects
- audio
- progress tracking
- rewards
- settings
- bilingual text
- skill definitions
- accessibility behavior
- common completion flow

---

# 4. Recommended shared native architecture

Long-term target: native Android with Jetpack Compose.

Suggested conceptual packages:

```text
learning/
  models/
  content/
  skills/
  progress/

audio/
  AudioController
  SpeechRequest
  AudioPolicy

games/
  common/
  followinstructions/
  memorypairs/
  treasurehunt/
  buildbridge/
  dinosaurrescue/
  crocodilesnap/

rewards/
  RewardRepository
  DiscoveryUnlocks

ui/
  scenes/
  controls/
  feedback/
  completion/
```

The exact package structure may evolve, but the separation of responsibilities should remain.

---

# 5. Shared game services

## AudioController

Responsibilities:

- English/German TTS
- play instruction
- replay instruction
- speak object names
- speak feedback
- cancel stale speech
- respect sound settings
- respect questions-only/all/off policy
- support lifecycle cancellation
- avoid speech overlap

Audio should be owned natively long-term.

---

## ProgressRepository

Every game reports skill-level results through the same progress API.

Games should not create isolated progress systems.

Example result:

```text
skill = spatial.under
result = success_after_replay
context = egg_tree
game = dragon_treasure_hunt
```

---

## ContentRepository

Shared data should include:

- animals
- vocabulary
- colours
- shapes
- numbers
- school objects
- environments
- weather
- habitats
- actions
- animal facts
- bilingual forms

The same crocodile definition should be usable in:

- Follow the Instructions
- Memory Pairs
- Dragon Treasure Hunt
- Compare & Discover
- Discovery Book

---

## SettingsRepository

Shared game settings should include:

- language
- audio mode
- reduced motion
- difficulty/adaptive mode
- round/session length
- parent preferences

---

## RewardRepository

Rewards should be shared across games.

Potential rewards:

- dragon eggs
- animal stickers
- Discovery Book entries
- unlocked character illustrations
- simple celebratory animations

Rewards must not be removed because of mistakes.

---

# 6. Shared scene model

Games should use shared scene data rather than independently generating text and visuals.

Conceptually:

```kotlin
data class SceneObject(
    val id: String,
    val type: SceneObjectType,
    val contentId: String,
    val imageAsset: String
)
```

Example:

```kotlin
SceneObject(
    id = "crocodile",
    type = ANIMAL,
    contentId = "animal.crocodile",
    imageAsset = "crocodile_01"
)
```

An instance in a scene may include:

```kotlin
data class SceneItem(
    val instanceId: String,
    val objectId: String,
    val position: ScenePosition,
    val attributes: Set<Attribute>
)
```

For example:

```text
object: crocodile
position: LEFT
attributes:
  GREEN
  LARGE
```

Instructions, visuals, answers, and feedback should all derive from the same scene data.

This is a critical architectural rule.

It prevents bugs where:

- the picture shows one object
- the question describes another
- feedback mentions a third

---

# 7. Shared skill model

Games should refer to stable skill IDs.

Examples:

```text
listening.one_step
listening.two_step
listening.three_step
listening.inhibition

working_memory.one_item
working_memory.two_items
working_memory.three_items

spatial.in
spatial.on
spatial.under
spatial.behind
spatial.next_to
spatial.between
spatial.left
spatial.right

numbers.quantity_1_3
numbers.quantity_1_5
numbers.quantity_1_10

colours.red
colours.blue
colours.green

language.naming
language.describe_picture
language.full_sentence

letters.upper_lower_D

reasoning.classification
reasoning.sequence
reasoning.pattern
```

The game does not own these definitions.

They belong to the shared learning system.

---

# 8. Shared result model

A game action should report more than correct/incorrect.

Suggested outcomes:

```text
INDEPENDENT_SUCCESS
SUCCESS_AFTER_REPLAY
SUCCESS_AFTER_HINT
SUCCESS_AFTER_PARENT_HELP
INCORRECT_ATTEMPT
SKIPPED
```

Useful metadata:

- skill IDs
- language
- scene/context
- difficulty
- game ID
- support used
- session ID
- item ID

Do not use response speed as a simple measure of ability.

---

# 9. Shared session design

Most game sessions should take approximately **3–5 minutes**.

Common rhythm:

```text
Welcome
↓
Listen
↓
Try
↓
Positive feedback
↓
Next mission
↓
Celebrate
↓
Finished
```

Sessions should have obvious endings.

Avoid endless randomized play by default.

---

# 10. Shared difficulty philosophy

Difficulty should increasingly be **skill-driven**, not simply:

```text
Level 1
Level 2
Level 3
```

Example profile:

```text
Listening:
  two-step = Practising

Spatial:
  under = Secure
  between = Learning

Numbers:
  quantity 1–5 = Secure
  quantity 6–10 = New
```

A generated mission could therefore use:

- a secure number concept
- familiar animals
- familiar colours
- one currently-learning spatial concept

Example:

> “Find TWO eggs and put them BETWEEN the rocks.”

This isolates the harder skill instead of making everything difficult simultaneously.

---

# 11. Confidence + challenge balance

Do not build a session entirely from weak areas.

Recommended adaptive mix:

- 1 confidence task
- 1 current focus task
- 1 communication/listening task
- 1 playful/rewarding task

This principle also powers `Today’s Adventure`.

---

# 12. Generalisation

A skill should appear across different contexts.

Example for `under`:

- snake under rock
- egg under tree
- pencil under book
- crocodile under bridge

Avoid teaching fixed picture associations.

Games should help determine whether a skill generalises.

---

# 13. Game 1 — Follow the Instructions

## Role

This should be the **first native game implemented**.

It is both a game and a reusable learning engine.

Primary skills:

- listening comprehension
- working memory
- spatial language
- colours
- object recognition
- numbers
- sequencing
- inhibitory control
- classroom language

---

## Core interaction

A scene contains several large tappable objects.

Example scene:

- snake
- crocodile
- tree
- rock
- eggs

The app speaks:

> “Touch the snake.”

Basti taps the snake.

Feedback:

> “Yes! You found the snake.”

A large Replay control must always be available.

No reading should be required.

---

## Difficulty progression

### Stage A — single object

> “Touch the dinosaur.”

Skills:
- listening.one_step
- vocabulary.dinosaur

---

### Stage B — attribute + object

> “Touch the green dinosaur.”

Skills:
- listening.one_step
- colours.green
- vocabulary.dinosaur

---

### Stage C — two actions

> “Touch the snake, then the crocodile.”

Skills:
- listening.two_step
- working_memory.two_items

---

### Stage D — spatial action

> “Put the egg under the tree.”

Skills:
- listening.spatial
- spatial.under

---

### Stage E — multi-attribute spatial instruction

> “Put the small red egg next to the dragon.”

Skills:
- size.small
- colours.red
- spatial.next_to
- listening.multi_attribute

---

### Stage F — multi-step mission

> “Touch the crocodile, pick up the egg, then put it under the tree.”

Skills:
- listening.three_step
- working_memory.three_items
- spatial.under

---

### Stage G — inhibition

> “Don’t touch the dragon. Touch the dinosaur.”

Skills:
- listening.inhibition
- executive_control.inhibition

Introduce this only when appropriate.

---

## Mistake behavior

First wrong attempt:

> “Let’s hear that again.”

Replay instruction.

Next support step:

> “Look for the green crocodile.”

Optional visual highlighting may appear.

Record support level rather than punishment.

---

## Follow the Instructions MVP

First implementation should contain only:

- 3–4 scene objects
- one-step tap instructions
- English/German
- native replay
- correct feedback
- gentle wrong feedback
- progress event reporting
- responsive Compose layout
- placeholder/static art acceptable initially

Do **not** attempt all advanced levels in the MVP.

After MVP:

1. attribute + object
2. two-step
3. drag/place
4. prepositions
5. three-step
6. inhibition

---

# 14. Game 2 — Memory Pairs

## Role

Second recommended game.

Technically simpler than the adventure games and highly reusable.

Primary skills may include:

- visual memory
- vocabulary
- bilingual vocabulary
- numbers
- phonics
- animal knowledge
- emotions
- habitats

---

## Board sizes

Start small:

- 4 cards
- 6 cards
- 8 cards

Larger boards only later.

No timer.

---

## Pair types

### Picture ↔ picture

Introductory matching.

---

### Picture ↔ word

Example:

crocodile illustration ↔ `crocodile`

Use only when appropriate to reading level.

---

### English ↔ German

`crocodile` ↔ `Krokodil`

---

### Quantity ↔ numeral

four dots ↔ `4`

---

### Uppercase ↔ lowercase

`D` ↔ `d`

---

### Animal ↔ habitat

shark ↔ ocean

---

### Animal ↔ action

snake ↔ slither

---

### Emotion ↔ expression

worried ↔ worried dragon illustration

---

## Audio

When a card is revealed:

> “Crocodile.”

or:

> “Krokodil.”

Matched pair:

> “You found a pair!”

Face-up cards may provide an individual replay control.

---

## Progress

Do not grade primarily by number of flips.

Record:

- exposures
- correct matches
- independent match
- support use if applicable
- skill/context

Memory Pairs is for learning and retrieval, not speed competition.

---

# 15. Game 3 — Dragon Treasure Hunt

## Role

First game intended to feel like a true adventure.

Primary skills:

- listening
- numbers
- colours
- spatial language
- vocabulary
- sequencing
- working memory
- simple problem solving

---

## Visual style

Layered native 2D/2.5D.

Possible scene layers:

- distant castle
- sky
- hills
- river
- bridge
- trees
- rocks
- foreground grass
- dragon
- treasure objects

Potential polish:

- subtle parallax
- small particles
- lightweight scene animations
- gentle camera shift

No true 3D required.

---

## Core concept

The dragon needs help finding treasure.

Example:

> “Find TWO red gems.”

After selection:

> “Put them UNDER the bridge.”

This combines:

- quantity
- colour
- listening
- spatial language

---

## Mission structure

A round may consist of approximately three objectives.

Example:

1. Find the blue key.
2. Put it next to the dragon.
3. Open the treasure chest.

The goal is a mini-story rather than unrelated questions.

---

## Future mission types

- Find three green gems.
- Find the gem behind the rock.
- Find something beginning with S.
- Bring the dragon something round.
- Find the smaller egg.
- Put two objects between the rocks.
- Follow a two-step treasure clue.

---

## Implementation dependency

Do not build Treasure Hunt first.

It should reuse systems established by:

- Follow the Instructions
- shared scenes
- drag/drop
- audio
- progress
- rewards

---

# 16. Game 4 — Build the Bridge

## Role

Spatial reasoning and construction game.

Primary skills:

- shapes
- spatial reasoning
- patterns
- quantity
- working memory
- planning
- measurement concepts

---

## Story

A dinosaur or dragon needs to cross a stream.

Basti helps build the bridge.

---

## Early levels

Fit shapes into matching holes:

- circle
- triangle
- square

---

## Pattern levels

Example:

```text
square → triangle → square → ?
```

---

## Instruction levels

> “Put the long block under the bridge.”

Later:

> “Use two small blocks and one long block.”

---

## Future mechanics

- reproduce a model
- repair a broken bridge
- choose the correct length
- choose stronger/larger pieces
- create simple symmetrical structures

No physics simulation is required for early versions.

---

# 17. Game 5 — Dinosaur Rescue

## Role

A more story-driven mixed-learning game.

The dinosaur is blocked by a harmless obstacle.

Basti solves tasks to help.

---

## Emotional design

Never:

> “You failed to rescue the dinosaur.”

Instead:

- dinosaur waits
- helper character encourages
- hints become available
- progress resumes when solved

Structure:

> try → help → progress

---

## Example sequence

Scene:

T-Rex behind a fallen tree.

Narration:

> “Oh no! The T-Rex needs our help.”

Task:

> “Which shape fits the bridge?”

Once completed:

> “Great! Now we can reach the T-Rex.”

---

## Possible challenge types

### Listening

> “Move the red rock.”

### Counting

> “We need three logs.”

### Concrete maths

two logs + one log

### Patterns

red → blue → red → blue → ?

### Spatial language

> “Put the key under the stone.”

### Sequencing

bridge → key → gate

---

## Story structure

A rescue might contain 3–5 small stages.

Each stage advances the environment.

This should feel more like a tiny adventure than a worksheet.

---

# 18. Game 6 — Crocodile Snap

## Role

Recognition and fun reinforcement.

Do **not** make this primarily a speed/reaction test.

The “snap” should be the crocodile’s reward animation.

---

## Core version

Three choices appear.

The app asks:

> “Which animal can slither?”

Basti chooses the snake.

The crocodile performs a fun snap animation.

---

## Possible content

### Vocabulary

> “Which one is the Komodo dragon?”

### Numbers

> “Which group has four?”

### Letters

> “Which word starts with D?”

### Comparisons

> “Which animal is bigger?”

### True or Silly

> “Crocodiles live in water.”

---

## Future inhibition mode

Cards may appear one at a time.

Prompt:

> “Wait for the dinosaur.”

Basti waits and taps the correct target.

No countdown.

No “too slow”.

This may eventually exercise attention and inhibition gently.

---

# 19. Shared completion and rewards

All games should use the same completion component.

Possible completion elements:

- praise message
- stars or calm progress indicator
- optional balloons
- new Discovery Book unlock
- dragon egg
- sticker
- Continue
- Home

Completion navigation must always remain accessible immediately.

Do not make reward interaction mandatory.

---

# 20. Discovery Book integration

The best rewards should add meaningful content.

Examples:

- shark activities unlock Whale Shark
- reptile activities unlock Komodo Dragon
- snake activities unlock King Cobra
- dinosaur activities unlock T-Rex fact card

Discovery entries should support:

- custom image
- English/German name
- spoken fact
- short child-friendly fact
- replay

---

# 21. Progress reporting example

A Dragon Treasure Hunt round might produce:

```text
listening.one_step
  independent_success

numbers.quantity_1_5
  independent_success

colours.red
  independent_success

spatial.under
  success_after_replay

working_memory.two_step
  success_after_hint
```

Do not store only:

```text
Treasure Hunt score = 80%
```

The individual skills are what matter.

---

# 22. Adaptive mission generation

Long-term game generators should choose content based on progress.

Example:

Known/secure:

- red
- two
- egg

Learning:

- between

Generated mission:

> “Put TWO red eggs BETWEEN the rocks.”

This is preferable to combining several new skills at once.

---

# 23. Today’s Adventure integration

Game selection should eventually feed into `Today’s Adventure`.

Example daily flow:

### Talk
Describe the Pteranodon.

### Listen
Two-step Follow the Instructions.

### Learn
Subitising 1–5.

### Play
Dragon Treasure Hunt.

Another day may use Memory Pairs or Build the Bridge.

This gives variety without overwhelming the child with a giant menu.

---

# 24. Input interaction rules

Games may use:

- tap
- drag/drop
- card flip
- simple sequencing
- object placement

Every drag-based task should have an accessible alternative where practical.

Touch targets should generally be approximately 44dp or larger, with larger child-facing targets preferred.

Avoid:

- tiny icons
- precision dragging
- complex gestures
- swipe-only mechanics
- multi-touch requirements

---

# 25. Audio rules

Every core game must work audio-first.

Requirements:

- automatic spoken instruction
- visible Replay button
- answer/object pronunciation where appropriate
- feedback narration
- no accidental answer submission from audio controls
- no speech overlap
- stop stale audio when leaving a game
- language follows app language

Future games must use the shared native audio system rather than inventing game-specific speech logic.

---

# 26. Bilingual content rules

English and German content should share stable semantic IDs but may require separately authored phrasing.

Do not generate German sentences mechanically where grammar/morphology matters.

For example:

- articles
- gender
- dative
- plural forms
- word order

should be explicitly reviewed in content data.

---

# 27. Accessibility

Games should support:

- large targets
- readable layouts
- portrait/landscape
- phone/tablet
- system insets
- reduced motion
- meaningful content descriptions where appropriate
- strong contrast
- no colour-only distinction
- no reading requirement for core play

Future TalkBack/Switch Access testing should be part of native QA.

---

# 28. Animation rules

Animation should communicate meaning, not merely decorate.

Good examples:

- crocodile jaw genuinely opens/closes for Snap
- dragon reaches toward treasure
- egg visibly moves under bridge
- bridge pieces visibly assemble
- dinosaur reacts to rescue progress

Use:

- Compose animations
- layered illustrations
- sprite sheets where appropriate

Avoid heavy 3D engines and video.

---

# 29. Device targets

Games must be designed for:

- Samsung S24-class phone
- Amazon Fire Max tablet
- portrait
- landscape

Layouts should reflow instead of simply scaling the phone layout.

No critical controls should be hidden by:

- status bars
- navigation bars
- display cutouts
- keyboard
- short landscape height

---

# 30. Performance targets

Keep games lightweight.

Avoid:

- multiple heavy rendering surfaces
- unnecessary WebViews
- large video assets
- uncontrolled particle counts
- continuous expensive animations

Prefer:

- bounded animation state
- lifecycle-aware cancellation
- cached local assets
- native Compose rendering

---

# 31. Implementation order

Recommended order:

1. Follow the Instructions
2. Memory Pairs
3. Dragon Treasure Hunt
4. Build the Bridge
5. Dinosaur Rescue
6. Crocodile Snap

Reasoning:

- Follow the Instructions establishes audio, scene, skill, and progress foundations.
- Memory Pairs provides a reusable second state machine with modest complexity.
- Treasure Hunt reuses scene/instruction/drag systems.
- Build the Bridge adds construction/spatial mechanics.
- Dinosaur Rescue combines several shared systems into a story game.
- Crocodile Snap then becomes a flexible reinforcement mode.

---

# 32. First implementation milestone

Do not ask an agent to build all games at once.

The first implementation milestone should be:

## Native Follow the Instructions MVP

Required:

- Compose-only runtime screen
- 3–4 scene objects
- one-step tap instructions
- English/German
- Android TTS via shared audio layer
- replay
- gentle correct/wrong feedback
- skill-level progress event model
- responsive phone/tablet layout
- portrait/landscape awareness
- no WebView for this game
- tests for state logic

Not yet required:

- drag/drop
- advanced multi-step missions
- full custom artwork
- inhibitory commands
- complex rewards
- adaptive generation

Prove the architecture first.

---

# 33. Testing expectations

Each game should eventually have:

## State tests
Examples:

- correct target advances
- wrong target does not advance incorrectly
- replay does not count as an attempt
- duplicate taps do not award duplicate progress
- session completion recorded once

## Content tests
Examples:

- referenced scene objects exist
- answer IDs map to actual objects
- bilingual strings exist
- generated mission is solvable
- no impossible duplicate constraints

## Compose UI tests
Examples:

- Replay button works
- correct target can be selected
- navigation works
- large text does not hide controls
- landscape keeps actions visible

## Device QA
Physical testing on:
- Samsung S24
- Amazon Fire Max

---

# 34. Save/recovery behavior

Game sessions should eventually survive ordinary lifecycle changes where practical.

At minimum:

- rotation should not destroy game logic unexpectedly
- leaving screen should cancel speech
- duplicate completion should not double-award progress
- progress writes should be idempotent by session ID

Full mid-game process-death recovery can come later if needed.

---

# 35. Non-goals for the first game phase

Do not:

- implement every roadmap game simultaneously
- build a heavy game engine
- introduce Unity
- build true 3D
- add online services
- add cloud speech
- create account systems
- create competitive leaderboards
- add timed scoring
- add streak punishment
- remove legacy learning activities before native parity exists

---

# 36. Definition of success

The game system is succeeding if:

- Basti can play without needing to read
- games feel different from one another
- they reuse shared learning data
- they produce meaningful skill progress
- weak skills can be revisited naturally
- mastered skills appear in new contexts
- mistakes stay low-pressure
- sessions stay short and understandable
- English/German both work properly
- games support the wider school-readiness plan
- new games become easier to build because shared systems already exist

---

# 37. Long-term vision

The end goal is not a collection of disconnected mini-games.

It is a coherent learning world in which:

- the same characters recur
- the same vocabulary is reinforced
- progress carries across activities
- Discovery Book rewards reflect learning
- Today’s Adventure chooses sensible next activities
- difficulty adapts gently
- games reinforce talking, listening, maths, school readiness and curiosity

The games should make learning feel like an adventure while the underlying system quietly tracks what has been practised, what is becoming secure, and what would be useful to practise next.
