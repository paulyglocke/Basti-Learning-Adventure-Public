# Basti’s Learning Adventure — Master Product & Learning Roadmap

## Purpose
A calm, audio-first, visually rich school-readiness app focused on communication, listening, language, early maths, school routines, self-regulation, curiosity and confidence.

## Core principles
- Audio-first; a child should be able to complete activities without reading.
- Short, predictable 3–5 minute sessions.
- No lives, forced timers, punishment or loss streaks.
- Custom illustrated assets in final production; emoji only as temporary placeholders.
- Native 2D/2.5D visual style, not true 3D.
- Fully offline, no Google Play Services dependency.
- Long-term target: fully native Android/Jetpack Compose with no runtime WebView/HTML/JavaScript dependency.
- Reuse shared native data models rather than duplicating facts and vocabulary.
- Explain mistakes constructively.
- Generalise skills across many contexts.
- Include real-world and movement activities.
- Open-ended speech tasks should encourage talking without automatic pronunciation scoring.

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
- Let’s Talk / Erzähl mal
- Tell Me More
- Sentence Builder
- Describe It
- Question Time
- Story Retell
- Finish My Sentence
- Conversation Starter

## Listen & Follow
Progression:
1. Touch the snake.
2. Touch the green snake.
3. Touch the snake, then the crocodile.
4. Put the egg under the tree.
5. Put the small red egg next to the Komodo dragon.
6. Touch the crocodile, then put the egg under the rock.
7. Don’t touch the dinosaur. Touch the snake.

Also:
- Remember the Mission
- one/two/three-step instructions
- delayed instructions
- spatial instructions
- inhibition instructions

## Letters & Sounds
- letter recognition
- letter sounds
- beginning sounds
- same-initial-sound grouping
- uppercase/lowercase matching
- syllables
- rhyme
- phonological awareness

Until uppercase/lowercase is explicitly taught, match display case:
D → Dinosaur, not D → dinosaur.

Treat phonics as language-specific content. Shared vocabulary does not imply that the same item is a good phonics example in English and German: English S → snake does not make German S → Schlange a simple /s/ lesson. Keep letter names, graphemes, phonemes and initial-sound tasks distinct, use reviewed language-specific phonics packs, and consider reviewed recorded phoneme clips rather than ordinary TTS for isolated sounds.

## Vocabulary Booster
Core packs:
school, home, people, food, clothes, body, feelings, weather, transport, colours, shapes, positions, actions, describing words, opposites.

Interest packs:
dinosaurs, dragons, snakes, Komodo dragons, crocodiles/alligators, sharks, whales, sea animals, prehistoric sea reptiles.

Each item should support:
English, German, custom illustration, TTS, simple explanation, example sentence, category, difficulty, progress metadata.

Start v1 deliberately small and reviewed: roughly 50–80 high-value shared words, including school objects, classroom/action words, core positions, describing words/opposites, feelings, colours/shapes and motivating animals. Reuse the same canonical content IDs across learning activities and games.

## Numbers & Maths
Near-term learning should prioritise concepts over ever-larger numerals:
- subitising 1–3, then 1–5
- number ↔ quantity
- more/fewer/same
- making 5 and simple number bonds
- counting and number recognition
- number order
- before/after
- missing numbers
- concrete addition/subtraction
- estimation
- patterns
- shapes
- spatial reasoning
- measurement concepts
- simple charts/data

Higher number recognition can remain available as the child grows, but it should not dominate the core early-maths experience.

## Think & Solve
- sequencing
- Plan the Mission
- Sort & Switch
- classification
- True or Silly?
- Spot the Mistake
- flexible thinking
- simple planning/problem solving

## World Around Me
- time
- weather
- home
- school
- playground
- nature
- habitats
- transport
- people who help us
- My World / belonging
- simple science investigations

### Wilma Weekdays / Wochentage
Use Wilma, the familiar Kita weekday caterpillar, as a recurring bridge between home/app learning and the classroom routine. Wilma's head is only the character head; the seven body segments represent the days in this fixed Kita-aligned order: Monday/Montag = green, Tuesday/Dienstag = red, Wednesday/Mittwoch = yellow, Thursday/Donnerstag = blue, Friday/Freitag = purple, Saturday/Samstag = orange and Sunday/Sonntag = pink.

Learning progression:
- explore Wilma and hear each day name
- tap the requested day from spoken English/German
- identify days from their place in the weekly sequence
- practise before/after and first/last in the cycle
- order the seven days
- introduce today/yesterday/tomorrow with explicit visual and spoken support

The colour mapping is a familiar teaching cue, not the sole identifier. Always combine it with segment position, spoken day name and localized visible text. Keep artwork language-neutral; English/German labels and speech belong to app content.

## School Skills
Teach:
- sit down, stand up, listen, look, wait
- open/close book
- take/put away pencil
- raise hand
- line up
- your turn/my turn
- finished/again
- I don’t understand
- Can you say that again?
- Can you help me?
- Which one?
- Where should I put it?
- It’s too loud.
- Can I have a break?

Prioritise practical classroom language and self-advocacy early, preferably in contextual scenes and Follow the Instructions rather than isolated flashcards.

Useful German examples include: Hör zu; Schau mal; Setz dich hin; Steh auf; Warte bitte; Hol deinen Stift; Leg das Buch auf den Tisch; Stell dich an; Ich verstehe das nicht; Kannst du das noch einmal sagen?; Kannst du mir helfen?; Wo soll ich das hinlegen?; Es ist mir zu laut; Kann ich eine Pause machen?

## Feelings & self-regulation
- happy, sad, angry, worried, scared, excited, frustrated, tired, surprised
- My Turn / Your Turn
- Stop–Think–Choose
- calm self-advocacy language
- avoid treating social situations as if only one response is always correct

## Compare & Discover
Use custom illustrated animal comparisons:
- bigger/smaller
- longer/shorter
- faster/slower
- heavier/lighter
- venomous/non-venomous
- land/water/air
- extinct/living
- habitat and classification

Core animal groups:
T-Rex, Velociraptor, Pteranodon, Mosasaurus, dragon, snake, Komodo dragon, crocodile, shark, whale.

Precise facts should live in shared data, not hardcoded question text.
Avoid vague claims like “most dangerous” unless the comparison is explicitly defined.

## Exploration & play
- Explore First, Explain Later
- Make a Scene
- Dinosaur Café
- Dragon Doctor
- Komodo Expedition
- I Spy
- Move & Learn
- Real-World Missions
- Build the Bridge

Bring simple real-world and movement missions forward. Examples: find something red; find something longer than your hand; find three soft things; look outside and identify the weather; stomp like a T-Rex five times; stand on one leg; clap a rhythm; jump three times. No camera or sensor scoring is required; a parent/child Continue action is enough. Where useful, alternate screen tasks with movement or real-world interaction.

## Games
- Follow the Instructions
- Memory Pairs
- Dragon Treasure Hunt
- Dinosaur Rescue
- Crocodile Snap
- Build the Bridge
- later: Mosasaurus Ocean Hunt, Pteranodon Letter Flight, Snake Path, Feed the T-Rex

All should be polished native 2D/2.5D experiences.

## Verb Explorer
Replace generic emoji/CSS motion over time with native action-specific animation.
Examples:
- jump: crouch → launch → airborne → land → recover
- slither: travelling S-wave
- dig: scoop → dirt movement → repeat
- snap: open → pause → snap → recoil
- build: pick up → carry → place

Prioritise 15–20 common verbs first.

## Predictable activity structure
Listen → Try → Celebrate → Finished

## Today’s Adventure
Make this a central long-term child-facing entry rather than a late add-on. The home should eventually emphasise one large Today’s Adventure action, with a secondary Choose Something Else / browse path so the full library remains available without overwhelming the child.

A recommended route contains:
- 1 confidence task
- 1 focus task
- 1 communication/listening task
- 1 fun game/reward task

Individual modules remain short (about 3–5 minutes), the child may stop after any module, and missed days never create penalties or streak pressure. Progress data should drive selection.

## Development phases
1. Finish current P0 correctness, audio and lifecycle work.
2. Establish native foundations: shared content model, audio controller, session/question framework and minimal persisted progress-event storage.
3. Migrate Prepositions end-to-end to prove the architecture.
4. Build Follow the Instructions MVP as the first true native game/learning engine.
5. Build Vocabulary Booster v1 using the shared content model.
6. Add Tell Me! / expressive-language v1, with parent/child Continue rather than automatic speech scoring. Tell Me More may expand “Snake” → “A green snake” → “The green snake is slithering” → “The long green snake is slithering under the tree.”
7. Build Memory Pairs on the shared matching engine.
8. Add Progress dashboard v1.
9. Expand native Numbers/Maths around subitising, quantities, patterns and shapes.
10. Build Classroom / School Skills.
11. Build Compare & Discover plus Discovery Book.
12. Build larger games: Dragon Treasure Hunt, Build the Bridge, Dinosaur Rescue and Crocodile Snap.

This ordering is guidance, not a ban on small opportunistic P0/P1 fixes.

## Reasoning prompts
Occasionally add unscored follow-ups such as “How did you know?”, “Why do you think that?” or “Can you explain?” after suitable tasks. These prompts encourage reasoning and expressive language and must not become automatic correctness scores.
