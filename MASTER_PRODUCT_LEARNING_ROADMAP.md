# Basti’s Learning Adventure — Master Product & Learning Roadmap

### Purpose

Basti’s Learning Adventure is an **offline, bilingual, manipulation-first school-readiness environment** built around short, calm and visually rich learning experiences in English and German.

The goal is not to accumulate disconnected quizzes. The app should increasingly feel like a coherent learning world where a child can:

**hear → explore → recognise → manipulate → build → sort → sequence → describe → reason → apply**

Dinosaurs, dragons, animals and familiar characters provide motivation and continuity, while the underlying curriculum develops communication, listening, language, early maths, time, school routines, practical skills, reasoning, independence and confidence.

The long-term product identity is:

> **An offline, bilingual, manipulation-first school-readiness environment where a child hears, explores, builds, sorts, sequences, moves things, discovers relationships, receives calm support and eventually combines skills in meaningful adventures.**

Interests and characters carry the learning. They do not replace the learning.

---

## Core product principles

- Audio-first where appropriate; a child should not need independent reading to participate in core activities.
- Short, predictable learning experiences, normally around 3–5 minutes.
- Fully offline core learning.
- No Google Play Services dependency.
- No advertising, accounts or cloud requirement for core learning.
- No lives, punishment, loss streaks, compulsory daily streaks or reaction-speed pressure.
- Native Android / Jetpack Compose is the long-term runtime target.
- Continue removing WebView/HTML/JavaScript only after tested native parity exists.
- Native 2D / 2.5D presentation by default.
- Do not introduce true 3D without a demonstrated learning or product need.
- Custom original artwork for final production; temporary placeholders are not final curriculum.
- One clear learning job at a time.
- Support is part of learning, not failure.
- Generalise concepts across contexts rather than rewarding memorisation of one image or screen.
- Reuse semantic content, artwork, audio and learning systems where appropriate.
- Movement and real-world tasks are valid learning modes, not rewards for losing focus.
- Open-ended speech should encourage communication without automatic pronunciation scoring.
- Reduce irrelevant animation during thinking.
- Motion should teach, guide, demonstrate consequence or celebrate.
- Child-controlled continuation is preferred after meaningful feedback.
- Shared abstractions should normally appear only when a second genuine consumer justifies them.
- Physical-device acceptance remains part of the feature definition.

A central rule is:

> **Difficulty should come from the learning concept — not from reading load, unclear artwork, tiny controls, arbitrary UI conventions, speed, memory of interface conventions or guessing what the app wants.**

---

## Roadmap operating model

This roadmap owns:

- product direction;
- curriculum direction;
- learning progression;
- major shared mechanics;
- relative development sequencing;
- long-term product identity.

Detailed implementation contracts remain in specialist specifications such as:

- `CONTENT_DATA_SPEC.md`
- `NATIVE_ARCHITECTURE_SPEC.md`
- `VOICE_AUDIO_SPEC.md`
- `PROGRESS_TRACKER_SPEC.md`
- `UX_NAVIGATION_SPEC.md`
- `GAME_DESIGN_SPEC.md`
- `ART_DIRECTION.md`
- `TESTING_QA_SPEC.md`

Do not duplicate low-level contracts here when another specification already owns them.

The roadmap increasingly asks:

> **Which reusable learning capability should the app gain next, and which activities can then use it?**

rather than only:

> Which activity should be built next?

This distinction is central to future development.

---

## Shared learning grammar

### Primary progression

Where appropriate:

**Experience → Explore → Recognise → Manipulate → Recall → Apply → Generalise**

Not every activity needs every stage.

#### Experience

Encounter the concept in a clear, low-pressure form.

#### Explore

Hear, touch, inspect, move or replay examples.

#### Recognise

Find or identify the concept when requested.

#### Manipulate

Act on the concept by moving, sorting, building, ordering, tracing or changing something.

#### Recall

Produce or select the concept with fewer cues.

#### Apply

Use the concept to solve a meaningful task.

#### Generalise

Use the concept successfully in a new presentation, activity or context.

### Concrete to abstract

For Maths and other concept-heavy areas:

**Concrete → Pictorial → Abstract**

Do not rush from quantity to symbols simply because symbols are easier to implement.

### Choice and consequence

For decision-making, story and problem-solving experiences:

**Predict → Choose / Act → Observe → Reflect**

Predictions may be unscored when more than one outcome is reasonable.

---

## Montessori-inspired learning principles

The app may use ideas inspired by Montessori education without claiming to be a Montessori programme.

Useful principles include:

- three-period teaching;
- isolation of difficulty;
- manipulation before abstraction;
- control of error;
- matching;
- sorting;
- grading;
- sequencing;
- construction;
- practical-life learning;
- demonstration before complex instructions;
- child-controlled repetition;
- increasing independence.

### Three-period teaching

A useful broad pattern is:

1. **Experience / Naming**
2. **Recognition / Exploration**
3. **Recall / Expression**

The recognition stage may be much longer than the recall stage.

If recall is difficult, returning calmly to recognition is preferable to repeatedly presenting failure.

### Control of error

Where practical, the material or interaction itself should help reveal a mismatch.

For example, if a bridge requires five planks and only four are placed, the remaining physical gap communicates useful information.

Prefer this over a large punitive red X where the learning concept allows it.

---

## Support model

The broad support ladder is:

**Independent → Replay → Hint → Model / Demonstration → Parent Support → Successful after support**

Not every activity requires every level.

Progress should record the **least support required for successful evidence** where practical.

Support should never be presented as punishment.

Examples of useful evidence include:

- recognised independently;
- recalled independently;
- succeeded after replay;
- succeeded after hint;
- succeeded after model;
- succeeded with parent support;
- applied successfully in another context.

A supported success remains a success.

---

## Generalisation

Recognising one piece of artwork is not the final goal.

Where appropriate, concepts should move through:

**familiar themed context → different themed context → different layout → more neutral presentation → application in another activity or story**

For example, a child who understands a quantity using dinosaurs should eventually recognise the same quantity using blocks, fruit, dots or other objects.

Interests are motivational presentation layers.

They must not become the definition of the concept.

---

## Learning pillars

1. Talk & Communicate
2. Listen & Follow
3. Letters & Sounds
4. Words & Vocabulary
5. Numbers & Maths
6. Time & Calendar
7. Colours
8. Practical Life & School Skills
9. Think & Solve
10. Compare & Discover
11. World Around Me
12. Move & Learn
13. Story Adventures
14. Play
15. Discovery Book

These pillars can share content and mechanics. They are not required to become separate technical engines.

---

## Talk & Communicate

Current and future activities include:

- Tell Me
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

A broad progression is:

**Hear → Recognise → Name → Describe → Expand → Explain**

Model and expand language naturally.

Example:

**“Snake” → “A green snake” → “The green snake is slithering.”**

The goal is richer communication, not speech scoring.

Tell Me should remain open-ended. Do not introduce automatic pronunciation or expressive-language correctness scoring.

Optional reasoning prompts may include:

- How did you know?
- Why do you think that?
- Can you explain?
- What happened?
- What might happen next?

These are conversation opportunities, not automatic correctness tests.

---

## Listen & Follow

Follow the Instructions is now an established native learning area and the first major consumer of the instruction/action direction.

Progression can grow through:

1. one-step object instructions;
2. adjective + object instructions;
3. action instructions;
4. two-step instructions;
5. spatial placement;
6. multi-attribute placement;
7. mixed action + placement;
8. delayed instructions;
9. simple inhibition;
10. mission-style instructions.

Reuse compatible instruction/action semantics for:

- Follow the Instructions;
- School Skills;
- Remember the Mission;
- Focus & Flex;
- Practical Life;
- selected Move & Learn tasks;
- Story Adventures.

Do not create difficulty by hiding essential targets, using ambiguous artwork or requiring memory of an unexplained UI phase.

If an activity intentionally becomes a memory task, demonstrate and communicate that phase clearly.

---

## Letters & Sounds

Keep letter names, graphemes and phonemes conceptually separate.

A broad progression is:

**Hear → Discriminate → Identify → Trace → Connect sound and symbol → Use**

Phonological-awareness progression may include:

1. environmental and animal sounds;
2. rhyme;
3. syllables;
4. same initial sound;
5. identify an initial sound;
6. reviewed grapheme ↔ phoneme relationships;
7. tracing and sound-symbol connection;
8. application in simple words.

Critical rule:

**grapheme ≠ phoneme ≠ letter name**

Use normal TTS for suitable words and sentences.

Isolated phonemes should use reviewed language-specific audio rather than Android letter-name TTS.

English and German phonics content must be authored and reviewed independently.

---

## Words & Vocabulary

Grow the shared vocabulary naturally through curriculum development rather than one giant vocabulary project.

Core areas include:

- school;
- home;
- people;
- food;
- clothes;
- body;
- feelings;
- weather;
- transport;
- colours;
- shapes;
- positions;
- actions;
- describing words;
- opposites.

Interest areas include:

- dinosaurs;
- dragons;
- snakes;
- Komodo dragons;
- crocodiles/alligators;
- sharks;
- whales;
- sea animals;
- prehistoric animals.

Reusable concepts should favour stable semantic IDs and shared canonical records.

Where appropriate, a reusable concept can contain:

- EN/DE labels;
- spoken form;
- illustration;
- explanation;
- example;
- category;
- semantic relationships;
- progress/context metadata.

Vocabulary should increasingly be reused inside instructions, Maths, Colours, stories and real-world missions.

---

## Numbers & Maths

Maths should prioritise concepts over ever-larger numerals.

The broad progression is:

**Count → Quantity → Numeral → Composition → Operations → Patterns → Measurement → Time**

Useful concepts include:

- subitising 1–3, then 1–5;
- number ↔ quantity;
- more / fewer / same;
- one more / one less;
- making 5;
- early number bonds;
- part-whole composition/decomposition;
- conservation of quantity;
- meaningful number stories;
- number recognition;
- number order;
- before / after;
- missing numbers;
- concrete addition/subtraction;
- estimate then count/check;
- patterns;
- shapes;
- spatial reasoning;
- measurement;
- simple charts/data;
- time.

Implementation checkpoint — 2026-10-08: five bounded native activities already cover
subitising 1–5 (How many?), both number ↔ quantity directions (Numbers & Groups),
Left/Right/Same comparisons to “Which side has more?” (More or Fewer), BEFORE/AFTER/
MISSING without wraparound (Number Order), and combining two visible nonempty groups
with totals 2–5 (Add Together). All support deterministic 5/10 rounds. These are
visual choice activities; they do not establish a manipulative quantity framework,
separate fewer-question assessment, subtraction, or mastery/generalisation.
Physical acceptance remains separate. The wider concept list above remains the
curriculum direction, not a claim that every concept is implemented.

### Manipulative quantity

Maths should increasingly support manipulation.

Example progression:

**objects → touch/count → quantity → numeral → combine groups → symbolic representation**

A Dinosaur Counting Tray could begin with familiar motivating objects but later generalise to neutral objects.

Useful mechanics include:

- tap counting;
- object grouping;
- quantity construction;
- number-line movement;
- T-Rex stomp counting;
- pattern reproduction;
- combining/separating groups;
- simple measurement.

Procedural generation should remain deterministic and should appear only when enough real activities justify a shared generator.

---

## Time & Calendar

The long-term curriculum progression is:

**Wilma Weekdays → Months → Seasons ↔ Months → Clock & Time → Calendar & Dates → Combined Time Missions**

### Wilma Weekdays / Wochentage

Use the familiar Wilma caterpillar.

Fixed familiar day colours:

- Monday / Montag — green
- Tuesday / Dienstag — red
- Wednesday / Mittwoch — yellow
- Thursday / Donnerstag — blue
- Friday / Freitag — purple
- Saturday / Samstag — orange
- Sunday / Sonntag — pink

Progression:

- Explore / hear;
- Find Day;
- Before / After;
- order seven days;
- today / yesterday / tomorrow with an explicit visual and spoken anchor.

Implementation checkpoint — 2026-10-08: all five Wilma progression stages above are implemented, including `TODAY` with a hypothetical explicit anchor and no device-date inference. `BILINGUAL` adds non-quiz German/English weekday comparison and tap-to-speak. New-mode physical acceptance remains open; preserve the earlier accepted ordering auto-follow evidence.

Colour is a familiar cue, never the sole identifier.

### Seasons / Jahreszeiten

Cycle:

**Spring → Summer → Autumn → Winter → Spring**

Progression:

1. Explore / recognise;
2. What comes next?;
3. What comes before?;
4. Build the Year;
5. Missing Season;
6. observable clue recognition;
7. season ↔ clue matching;
8. combined before/after;
9. Months ↔ Seasons.

Implementation checkpoint — 2026-10-08: stages 1–8 are implemented, including `MISSING`, `CLUES`, `MATCH` and `COMBINED`. Months ↔ Seasons and broader generalisation remain future work. Earlier S24 acceptance applies only to the flows named in the QA ledger, not automatically to these later modes.

Teach the conventional display order while making the cyclic relationship explicit.

### Months

Months should become a proper learning strand rather than a static list.

#### Year Wheel

The central representation should be a **12-segment Year Wheel** with four broad season regions.

The wheel should make the December → January relationship visually obvious.

Season boundaries are conventional teaching regions rather than claims about exact weather dates.

#### Progression

- Explore / Listen;
- Find Month;
- What comes next?;
- What comes before?;
- Between;
- Missing Month;
- Build the Year with 3 months;
- Build with 4;
- Build with 6;
- Build all 12;
- Months ↔ Seasons;
- generalise from wheel to horizontal sequence/cards/calendar.

Personal landmarks can later support orientation but should not define a month universally.

### Clock & Time

Clock & Time should be strongly manipulation-first.

#### Core interaction

Use one semantic `ClockTime` source of truth.

A manipulable analogue clock and digital representation should remain synchronised:

**Analogue ↔ Digital**

Moving the minute hand should correctly move the hour hand continuously.

Changing the digital time should update the analogue representation.

#### Early exploration

Before formal telling-time questions:

- move the long hand;
- observe minutes changing;
- complete a revolution;
- observe the hour advance;
- observe the hour hand move gradually.

#### Progression

1. whole hours;
2. half hours;
3. quarter past / quarter to;
4. five-minute intervals;
5. mixed time;
6. routine/contextual time.

#### Future modes

- Make This Time;
- What Time Is It?;
- Digital → Analogue;
- Fix the Clock;
- Match the Clocks;
- construct the clock face;
- routine/time missions.

Clock-face construction can begin with:

**12 / 6 / 3 / 9 → remaining numerals → hands**

Different clock-face themes can later generalise recognition.

Small dinosaurs or dragons may accompany the hands, but the conventional clock hands must remain unambiguous.

English and German spoken time should be authored appropriately rather than mechanically translated.

### My Calendar

Later work can introduce:

- real month grids;
- weekdays;
- dates;
- Today;
- yesterday/tomorrow;
- meaningful personal landmarks;
- calendar construction;
- combined weekday/month/date/time missions.

---

## Colours

Colour learning should combine language and reasoning.

Core colours can include:

- red / rot;
- blue / blau;
- yellow / gelb;
- green / grün;
- orange / orange;
- purple / lila;
- pink / rosa;
- brown / braun;
- black / schwarz;
- white / weiß;
- grey / grau.

Broad progression:

**Explore → Recognise → Colour + Object → Match → Sort → Grade → Follow Colour Instructions → Patterns → Light/Dark → Colour Mixing → Generalise**

### Colour matching

Begin with clearly distinguishable colours.

Later match identical shades and generalise from abstract colour samples to ordinary objects.

### Colour sorting

Sorting can establish the first serious reusable Sorting consumer.

Examples:

- sort objects by colour;
- find all red objects;
- place objects into matching groups;
- later switch the sorting rule.

### Colour grading

Montessori-inspired colour-tablet ideas can support:

- identical matching;
- lighter/darker;
- light → dark ordering;
- generalisation to ordinary objects.

### Colour mixing

Later expansion can demonstrate selected colour relationships through controlled interactive presentation.

Do not require knowledge of ambiguous digital shades.

Outside explicit colour-learning tasks, colour should not be the sole essential cue.

---

## Practical Life & School Skills

Practical Life should become a first-class curriculum area.

Potential activities include:

- Pack the School Bag;
- Getting Dressed;
- Wash Hands;
- Brush Teeth;
- Set the Table;
- Tidying;
- classroom routines.

Prefer:

- sequencing;
- placement;
- construction;
- demonstration;
- consequence;
- control of error.

Avoid turning practical routines into ordinary four-answer quizzes where direct manipulation communicates the concept better.

### School Skills

Useful language includes:

- sit down / stand up;
- listen / look / wait;
- open / close the book;
- take / put away the pencil;
- raise your hand;
- line up;
- your turn / my turn;
- finished / again;
- I don’t understand;
- Can you say that again?;
- Can you help me?;
- Which one?;
- Where should I put it?;
- It’s too loud.;
- Can I have a break?

Prefer contextual scenes and meaningful routines over isolated flashcards.

---

## Feelings, communication and self-advocacy

Useful concepts include:

- happy;
- sad;
- angry;
- worried;
- scared;
- excited;
- frustrated;
- tired;
- surprised.

Use these to support communication and self-advocacy.

Avoid implying that one social response is universally correct.

Potential language includes:

- I need help.
- I don’t understand.
- Please say it again.
- It’s too loud.
- I need a break.

---

## Think & Solve / Focus & Flex

Use playful, non-clinical practice for:

- working memory;
- inhibition;
- rule use;
- flexible switching;
- sequencing;
- classification;
- simple planning.

Possible patterns:

- Do What I Say;
- Opposite Game;
- Rule Switch;
- Freeze & Move;
- Remember the Mission;
- Plan the Mission;
- True or Silly?;
- Spot the Mistake;
- Sort & Switch.

Do not present these as ADHD treatment or generic brain training.

---

## Compare & Discover

Compare & Discover should grow beyond fact questions into a manipulation and reasoning strand.

Progression:

**Same / Different → Match → Sort → Grade → Multiple Attributes → Rule Switching**

Possible attributes include:

- bigger / smaller;
- longer / shorter;
- more / fewer;
- lighter / darker;
- heavier / lighter;
- faster / slower;
- shape;
- colour;
- habitat;
- land / water / air;
- flies / swims / walks;
- extinct / living;
- animal classification.

Precise factual claims belong in shared canonical data rather than hardcoded question prose.

---

## Reusable learning mechanics

Before creating another custom activity architecture, ask whether the learning goal fits an existing or planned mechanic.

The shared mechanic catalogue is:

1. Choice
2. Sequencing
3. Semantic Matching
4. Sorting
5. Grading
6. Placement
7. Construction
8. Tracing
9. Manipulative Quantity
10. Motion Demonstration

Additional activity patterns can compose these mechanics rather than becoming giant universal engines.

### Choice

Use for recognition, decisions and story agency.

Meaningful distractors are preferred over random unrelated choices where appropriate.

### Sequencing

Current consumers include Wilma and Seasons.

Future consumers include:

- Months;
- numbers;
- Practical Life;
- story events;
- routines;
- calendar construction.

Tap-to-place should remain available even if drag is later added.

### Semantic Matching

Relationships must use stable semantics, not equality of translated display strings.

Possible relationships include:

- picture ↔ picture;
- picture ↔ word;
- EN ↔ DE;
- number ↔ quantity;
- uppercase ↔ lowercase;
- animal ↔ action;
- animal ↔ habitat;
- emotion ↔ expression;
- season ↔ clue.

Memory Pairs can become one presentation of this shared capability.

### Sorting

Possible sorting dimensions include:

- colour;
- size;
- habitat;
- shape;
- flies/swims;
- season;
- living/extinct;
- school categories.

Begin with one explicit rule.

Later introduce rule switching.

### Grading

Possible ordered dimensions include:

- smallest → biggest;
- shortest → longest;
- lightest → darkest;
- fewest → most;
- lowest → highest;
- earlier → later.

### Placement

Useful for:

- prepositions;
- Follow the Instructions;
- Practical Life;
- School Skills;
- story interactions.

### Construction

Useful for:

- quantities;
- patterns;
- bridges;
- clock faces;
- calendars;
- routines;
- simple scenes.

### Tracing

Useful for:

- letters;
- pre-writing;
- selected paths/shapes.

Tracing should not become precision scoring inappropriate for a young child.

### Manipulative Quantity

Shared Maths capability for:

- counting;
- grouping;
- combining;
- separating;
- composition;
- number bonds;
- simple operations.

### Motion Demonstration

Reusable semantic movement for actions and instructions.

Motion should teach something observable.

---

## Animation and motion strategy

The preferred animation direction is:

**stable source artwork → constrained key poses/layers → deterministic transforms → smooth native rendering**

Do not rely on dozens of independently generated frames when character anatomy, proportions or object identity can drift between frames.

A useful experimental harness may include:

- stable source layers;
- key poses;
- deterministic interpolation;
- preview;
- contact sheet;
- loop validation.

Good early instructional-motion candidates include:

- eagle flying;
- fish swimming;
- snake slithering;
- sloth hanging;
- frog jumping;
- rabbit hopping;
- dolphin swimming/jumping;
- elephant spraying.

Complex articulated walking, such as a T-Rex walk cycle, requires stricter reference work and should not be the first architecture proof.

Priority:

**Instructional animation → Interaction consequence → Celebration**

The deciding question is:

> **Does the movement teach something?**

---

## Verb Explorer

Verb Explorer should evolve toward action-specific native motion.

Potential early verbs:

- jump;
- fly;
- swim;
- slither;
- hop;
- hang;
- spray;
- dig;
- carry;
- snap.

The same semantic motion capability may later support:

- Vocabulary;
- Follow the Instructions;
- Tell Me;
- Story Adventures.

Do not build a large motion framework until a genuine second consumer exists.

---

## Story Adventures / Geschichten-Abenteuer

Story Adventures are a major long-term generalisation layer.

They should not be passive digital storybooks or ordinary quizzes with decorative narrative.

The child should participate in the story.

Core loop:

**Predict → Choose → Observe → Reflect**

A central agency rule is:

> **When the app asks the child to make a choice, the choice must actually change something.**

### Learning progression

Story Adventures can develop:

1. narrative comprehension;
2. choice and agency;
3. prediction;
4. immediate cause/effect;
5. sequencing;
6. problem solving;
7. delayed consequence;
8. planning;
9. perspective taking;
10. expressive language.

### Choice progression

#### Preference

Example:

**Forest or beach?**

Both choices are enjoyable.

The learning is:

**I choose → the story changes.**

#### Immediate consequence

The choice produces an obvious next-scene difference.

#### Problem solving

Use information in the scene to choose an action.

#### Delayed consequence

An earlier decision matters later.

#### Social consequence

A choice affects another character.

Avoid GOOD/BAD moral scoring.

#### Planning

Use information already learned to prepare for a later problem.

#### Multiple consequences

A choice may affect:

- location;
- object;
- relationship;
- dialogue;
- route;
- ending.

### Story facts

Use simple semantic facts rather than hidden scores.

Examples:

`HAS_UMBRELLA`
`HAS_RED_KEY`
`HELPED_TURTLE`
`VISITED_FOREST`

These facts can affect later transitions, dialogue and endings.

### Branching strategy

Avoid uncontrolled branch explosion.

Prefer:

**branch → consequence → rejoin**

where appropriate.

Important decisions may remain divergent and lead to distinct endings.

Early stories can have two or three satisfying endings.

Do not label endings GOOD or BAD.

### Replay and discovery

Replay should encourage curiosity.

Avoid completion percentages and pressure to discover every branch.

A simple Story Map may eventually show:

- discovered routes;
- selected known events;
- a small number of mystery alternatives.

### Curriculum integration

Stories should orchestrate existing systems rather than reimplement them.

A story can naturally include:

- Follow the Instructions;
- Prepositions;
- Vocabulary;
- Colours;
- Sorting;
- Sequencing;
- Maths;
- Months;
- Clock & Time;
- Practical Life;
- School Skills;
- reasoning.

### Story sequencing and retelling

After completion:

**Beginning → Middle → End**

can become a sequencing activity.

Later:

- four/five-scene sequencing;
- cause/effect;
- What happened?;
- Why?;
- What happened next?;
- What might happen next?;
- story retelling.

Open-ended responses remain unscored.

### Make a Story

A later activity may allow constrained construction:

**Who → Where → Problem → Events → Ending**

Prefer authored combinations over uncontrolled generative content.

### Record My Story

A possible future local feature can allow the child to record a retelling.

Do not automatically score pronunciation or expressive quality.

---

## Story architecture direction

Story content should remain data-driven and semantically explicit.

A possible conceptual model includes:

- `StoryDefinition`
- `NarrativeNode`
- `ChoiceNode`
- `InteractionNode`
- `ReflectionNode`
- `EndingNode`
- `StoryFact`

Transitions can depend on story facts.

Story systems should orchestrate existing:

- audio;
- progress;
- sorting;
- sequencing;
- Clock;
- instructions;
- navigation;
- restoration.

Do not duplicate those systems inside the story engine.

### Story validation

Build-time validation should eventually detect:

- unreachable nodes;
- missing copy;
- missing images;
- invalid transition targets;
- impossible conditions;
- missing endings;
- orphan branches;
- duplicate IDs;
- invalid facts;
- missing language data.

Automated traversal tests should verify:

- reachable routes;
- all valid routes terminate;
- no unintended loops;
- valid assets;
- valid EN/DE content;
- restoration;
- navigation;
- Sound Off;
- progress semantics.

---

## Story artwork pipeline

Story artwork requires stronger continuity than isolated activity images.

Preferred pipeline:

**Concept → Narrative graph → Scene list → Visual bible → Reference sheets → Artwork → Visual-semantic audit → Integration → Physical acceptance**

A visual bible should define important recurring character and environment properties.

Scene review classifications may include:

- PASS
- ARTWORK_FIX
- COPY_FIX
- CONTINUITY_FIX
- REVIEW_REQUIRED

Automated validation cannot prove visual-semantic correctness.

Human review remains mandatory where meaning depends on artwork.

Static 4:3 artwork is acceptable for early Story Adventures.

Subtle deterministic native motion may be added later.

Story comprehension must not depend on decorative animation.

---

## Familiar recurring cast

A small original recurring cast can provide familiarity and identity.

Potential characters include:

- Dragon;
- T-Rex;
- Crocodile;
- Turtle;
- Frog;
- Eagle.

Characters should appear across curriculum areas rather than permanently mapping to one subject.

The concept remains primary.

---

## Today’s Adventure

Today’s Adventure is the long-term primary child-facing route.

### Early curated version

No adaptive algorithm is required.

A short route might contain:

**Confidence activity → Developing skill → Manipulation → Story chapter → Celebration**

The child can stop after any module.

No missed-day penalty.

No streak requirement.

### Later progress-informed version

Once enough trustworthy evidence exists:

- mix secure and developing skills;
- schedule spaced review;
- vary presentation/context;
- include communication;
- include manipulation;
- include movement;
- include generalisation;
- avoid repetitive drilling.

Do not turn this into an opaque adaptive score.

---

## Discovery Book

Discovery Book should be a presentation layer over shared knowledge, not a parallel fact database.

Animals, vocabulary, actions, habitats and facts learned elsewhere can surface naturally.

Story Adventures may later unlock contextual discoveries without turning Discovery Book into a reward economy.

---

## Move & Learn

Movement should remain a regular learning mode.

Examples:

- T-Rex Counting;
- Pteranodon Directions;
- Dragon Number Bonds;
- Snake Rhythm;
- Freeze Animals;
- classroom movement instructions;
- real-world find missions.

Sensor scoring is not required.

A parent/child Continue action is sufficient.

Any future sensor enhancement must leave the activity usable without the sensor.

---

## Completion celebration

The shared native completion celebration is part of the native experience.

Requirements:

- optional playful interaction;
- controls remain usable;
- no score/mastery coupling;
- reward interaction is non-durable;
- Sound Off blocks celebration audio;
- use clean canonical artwork;
- keep motion calm.

Future improvement:

- deterministic selection of five distinct animals;
- same completed round reproduces the same assignments;
- new rounds vary;
- no duplicate within one celebration.

---

## Accessibility and interaction principles

Across the app:

- comfortable touch targets;
- audio-first where appropriate;
- Sound Off respected everywhere;
- portrait and landscape resilience;
- larger-text resilience;
- no essential colour-only cue outside explicit colour learning;
- reduced irrelevant motion during thinking;
- deterministic restoration;
- no unexpected speech replay after restoration;
- no drag-only interaction when tap-to-place is sufficient;
- visible focus/interaction states where appropriate;
- clear semantic artwork;
- no learning difficulty created by UI ambiguity.

Support settings should use experience-based language rather than diagnostic labels.

Possible future settings:

- Reduced motion;
- Calm celebrations;
- Help sooner;
- Simpler/literal instructions;
- Visual steps;
- Larger controls/text.

Introduce settings only when concrete features need them.

---

## Progress evidence

Progress should increasingly represent concept evidence rather than simple activity counts.

Possible semantic evidence:

`COLOUR_RED_RECOGNISED`
`COLOUR_RED_APPLIED_IN_CONTEXT`
`MONTH_MARCH_NEXT_INDEPENDENT`
`CLOCK_HALF_HOUR_WITH_HINT`

These are illustrative concepts, not a requirement for those exact identifiers.

Evidence should distinguish where useful:

- recognition;
- recall;
- manipulation;
- application;
- generalisation;
- support level.

Story Adventures are particularly valuable as a future source of **generalisation evidence**.

Do not build a large parent dashboard before the curriculum produces enough meaningful evidence to summarise.

---

## Game direction

Ordinary activities remain native 2D Compose.

Larger adventures may use layered/parallax/isometric 2.5D presentation where useful.

Do not adopt Unity or another heavy 3D engine without a specific demonstrated requirement.

Responsive animation is welcome.

Reaction-time pressure is not.

### Memory Pairs

A reusable semantic-matching presentation.

No move-count or completion-time pressure.

### Dinosaur Rescue

Mission-led learning can combine:

- counting;
- instructions;
- prepositions;
- quantity;
- colour;
- vocabulary;
- planning.

### Dragon Treasure Hunt

Can combine:

- instructions;
- matching;
- numbers;
- sorting;
- memory;
- simple planning.

### Build the Bridge

Construction and control of error.

If five planks are needed and four are placed, the remaining gap communicates “one more”.

### Crocodile Snap

Fast-feeling recognition and satisfying animation without countdown or reaction-time scoring.

### Other later ideas

- Mosasaurus Ocean Hunt
- Pteranodon Letter Flight
- Snake Path
- Feed the T-Rex
- Dinosaur Café
- Dragon Doctor
- Komodo Expedition
- Dragon Paint
- Make a Scene

---

## Development priorities

### P0 — Trusted daily build

The current native baseline should remain trustworthy while expansion resumes.

Current priorities:

- preserve green automated validation;
- run a broad Samsung S24 Ultra full-app acceptance/debug sweep;
- run equivalent Fire Max acceptance;
- fix meaningful crashes, persistence, audio ownership, accessibility, lifecycle, semantic-artwork and layout defects discovered by those sweeps;
- preserve stable signing and in-place upgrade behaviour;
- keep visual-semantic QA separate from automated structural validation.

Already established native areas include:

- Prepositions activity revision 3/content 1.3 (exact v1/v2 restoration; six cave fixes and ten subsequent replacements source-reviewed as resolved; all 52 CLEAR, physical acceptance separate);
- Seasons through combined before/after;
- Wilma’s Week including TODAY/BILINGUAL;
- five bounded native Maths activities from subitising through addition within 5;
- Colour Sort, Animal Groups and Wash Hands;
- Vocabulary Booster;
- Follow the Instructions;
- Tell Me;
- shared completion;
- shared audio/session/progress foundations.

The [QA ledger](TESTING_QA_SPEC.md) preserves confirmed S24 upgrade/flow evidence and separates remaining S24 checks from outstanding Fire Max acceptance. Source-art review and green CI do not close those hardware gates.

**P0 is ongoing product hygiene rather than a blocker preventing all new curriculum work.**

---

## P1 — Shared native learning platform

Build reusable capabilities only as real consumers require them.

Priority capabilities:

1. **Lightweight visual design system**
   - buttons;
   - cards;
   - headers;
   - progress;
   - support presentation;
   - completion;
   - typography;
   - spacing;
   - touch targets.

2. **Shared support presentation**
   - Replay;
   - Hint;
   - Model/Demonstration;
   - Parent Support where appropriate.

3. **Support settings**
   - only as concrete consumers require them.

4. **Sequencing**
   - continue reuse beyond Wilma/Seasons as Months and routines require it.

5. **Semantic Matching**
   - establish through a real second consumer such as Memory Pairs.

6. **Sorting**
   - already shared by Colour Sort, Animal Groups and Seasons MATCH; extend through concrete future Colours / Compare & Discover consumers.

7. **Grading**
   - establish when Colour grading or size/quantity grading requires it.

8. **Placement**
   - reuse instruction/preposition interaction where practical.

9. **Construction**
   - establish through a real curriculum consumer.

10. **Manipulative Quantity**
    - establish through conceptual Maths.

11. **Tracing**
    - establish when Letters & Sounds / pre-writing requires it.

12. **Motion Demonstration**
    - establish through a small instructional-animation proof before broad rollout.

Shared buttons, titles, quiz progress, support messages and completion are already implemented; remaining design work needs a bounded consumer. Native UI regression execution and an explicit Android-test CI gate remain validation work, separate from those presentation components.

Do not build speculative mega-frameworks.

---

## P2 — Core school-readiness curriculum

Use shared capabilities to add breadth.

Priority areas include:

- remaining Seasons generalisation and Months ↔ Seasons beyond implemented MISSING/CLUES/MATCH/COMBINED;
- Months v1;
- Colours v1;
- conceptual manipulation-first Maths beyond the five implemented visual-choice activities;
- Practical Life v1;
- School Skills;
- broader Vocabulary;
- Letters & Sounds foundation;
- Compare & Discover;
- Memory Pairs / semantic matching;
- Focus & Flex variants;
- feelings and self-advocacy;
- Clock & Time v1;
- continued Follow the Instructions progression;
- continued Tell Me / expressive-language progression.

### Months v1

Aim for:

- Year Wheel;
- Explore/Listen;
- Find Month;
- Next/Before;
- Missing Month;
- progressive Build the Year;
- Months ↔ Seasons;
- EN/DE audio.

### Colours v1

Aim for:

- Explore;
- Find Colour;
- Colour + Object;
- Matching;
- Sorting;
- Follow Colour Instructions;
- early generalisation.

Later expansion:

- grading;
- patterns;
- light/dark;
- mixing.

### Clock & Time v1

Start small:

- semantic time model;
- analogue/digital synchronisation;
- manipulable hands;
- whole hours;
- half hours;
- EN/DE speech;
- restoration;
- accessibility;
- physical-device acceptance.

Do not begin with every telling-time convention.

### Experimental Story Adventure

P2 may include a **small experimental Story Adventure prototype**.

Its purpose is not to build the full story engine.

It should answer one product question:

> **Does the child understand and enjoy “I choose → the story changes”?**

Keep the prototype deliberately small.

---

## P3 — Adventure & generalisation

Use the mature learning platform to create richer integrated experiences.

Priority areas:

- full Story Adventures;
- My Calendar;
- advanced Clock & Time;
- combined time missions;
- advanced Colours;
- Dragon Paint;
- advanced sorting/grading/rule switching;
- story sequencing and retelling;
- constrained Make a Story;
- Dinosaur Rescue;
- Dragon Treasure Hunt;
- Build the Bridge;
- Crocodile Snap;
- Today’s Adventure;
- Discovery Book;
- native/action-specific Verb Explorer;
- richer deterministic animation;
- Move & Learn missions;
- broader original artwork;
- coordinated Home redesign;
- branding/icon/native splash;
- accessibility/performance polish.

The splash must use normal Android native splash behaviour, add no artificial delay and transition directly to Home.

---

## Internal milestones

### Milestone A — Trusted Native Baseline

Native core learning areas, shared content/audio/session/progress foundations, stable update stream, automated validation and repeatable physical acceptance.

### Milestone B — Manipulation-First School Readiness

Months, Colours, conceptual Maths, Practical Life, School Skills, broader Vocabulary, early Clock & Time, Letters & Sounds and reusable Sorting/Grading/Construction capabilities.

### Milestone C — Connected Learning World

Compare & Discover, richer expressive language, semantic matching, Focus & Flex, feelings/self-advocacy, Discovery Book foundations and the experimental Story Adventure.

### Milestone D — Adventure & Generalisation

Full Story Adventures, My Calendar, combined missions, Today’s Adventure, mature games, richer animation, cohesive 2D/2.5D presentation and broad cross-device acceptance.

---

## Delivery strategy

Prefer:

- batched physical acceptance over repeatedly testing one tiny feature in isolation;
- concept reuse over standalone content packs;
- canonical semantic content over duplicated strings;
- deterministic generation over ad-hoc randomness;
- exact restore over approximate recreation;
- child-controlled continuation;
- manipulation where it teaches better than multiple choice;
- control of error where practical;
- meaningful distractors;
- generalisation across contexts;
- mission/story integration after the underlying concept is established.

Do not allow “reusable architecture” to become an excuse for speculative abstraction.

---

## Artwork and visual-semantic QA

Artwork is part of the learning contract.

If a question depends on an object, relationship, action, quantity or expression being visible, the artwork must genuinely support that interpretation.

Automated tests can prove:

- file presence;
- decoding;
- dimensions;
- hashes;
- metadata consistency;
- semantic references.

They cannot prove:

- that an animal looks like the intended animal;
- that an action is visibly occurring;
- that a relationship is unambiguous;
- that a child will interpret the scene as intended.

Therefore visual-semantic review and physical acceptance remain required.

The Tell Me visual-QA process should inform future artwork-heavy curriculum.

---

## Open-source reference principles

External projects may be studied for ideas, but Basti’s specifications and architecture remain authoritative.

Useful reference areas include:

- progressive support;
- accessibility;
- adaptive Compose layouts;
- semantic matching;
- Concrete/Pictorial/Abstract progression;
- offline learning architecture;
- curriculum/activity taxonomy;
- manipulative interaction patterns.

Licensing discipline:

- permissively licensed code may be adapted only with required notices/attribution;
- GPL/unlicensed projects remain idea/reference sources unless explicitly approved;
- external artwork/content is not copied merely because a repository is public;
- do not transplant another app’s architecture wholesale.

---

## Agent development strategy

Different agents should be used according to the risk and nature of the task.

### Astra

Prefer Astra for work where architectural mistakes are expensive:

- semantic models;
- state machines;
- persistence;
- restoration;
- deterministic generation;
- progress semantics;
- audio ownership;
- lifecycle;
- shared mechanics;
- Months canonical sequencing;
- Clock time model and geometry;
- analogue/digital synchronisation;
- Sorting/Grading architecture;
- Story graph architecture;
- validation;
- difficult regressions;
- deep test design.

Astra should investigate the existing architecture/specifications/tests before proposing a new system.

Prefer the smallest compatible design.

Do not introduce parallel state, progress or audio architectures.

### Luna

Prefer Luna for bounded implementation after the contract is established:

- Compose presentation;
- Year Wheel UI;
- Clock UI against an approved semantic model;
- Colour Explorer;
- Sorting presentation;
- Practical Life content wiring;
- approved animation transforms;
- Story node presentation;
- content packs;
- accessibility polish;
- component/UI tests.

Luna should not redesign approved architecture unless a concrete implementation problem proves the contract insufficient.

### Standard task structure

Substantial agent prompts should normally specify:

**Goal → Learning contract → Existing systems to reuse → Behaviour → Support behaviour → Progress evidence → Audio → Accessibility → State/restore → Visual requirements → Out of scope → Tests → Validation → Git**

Shared rules:

- existing Basti specifications remain authoritative;
- inspect before changing;
- preserve current architecture;
- no timers/lives/streak pressure;
- no second state/progress/audio architecture;
- use semantic relationships rather than translated-string equality;
- prefer meaningful distractors;
- 2D/2.5D by default;
- support reduced motion;
- physical acceptance is not implied by green automated tests;
- do not commit or push unless explicitly requested.

---

## Near-term decision framework

When choosing the next task, prefer work that does at least one of the following:

1. closes a meaningful current-product defect;
2. creates a reusable capability needed by an imminent second consumer;
3. adds a high-value school-readiness concept;
4. improves manipulation over quiz-only interaction;
5. creates useful progress/generalisation evidence;
6. makes future Story Adventures easier without prematurely building the full story engine.

The next feature should not be selected merely because it is easy to code.

The product should continue moving from:

**question → choose → correct/wrong**

toward:

**experience → explore → manipulate → notice → correct → recall → apply → generalise**

while preserving the calm, reliable and child-friendly experience already established.
