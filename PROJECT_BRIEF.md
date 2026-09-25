# Basti's Learning Adventure — Project Brief

This brief summarises the product and current constraints. Long-term direction belongs to [MASTER_PRODUCT_LEARNING_ROADMAP.md](MASTER_PRODUCT_LEARNING_ROADMAP.md) and its domain specs; the actionable queue is [BACKLOG.md](BACKLOG.md).

## 1. Purpose

Basti's Learning Adventure is an offline Android learning app intended to help a child prepare for starting primary school in Germany.

The app should feel like a friendly children's game rather than homework. It should build confidence through short, repeatable activities, clear feedback, large touch targets, spoken instructions, and familiar themes.

The target child is approximately 6–7 years old. Keep vocabulary, explanations, instructions, and visual complexity age-appropriate. Do not make exercises unnecessarily advanced just because the app technically can support them.

## 2. Core design principles

- Audio-first: core activities should be usable without independent reading.
- Child-friendly first: simple wording, large controls, clear visual hierarchy.
- No timers or pressure.
- Wrong answers should allow another try; avoid punishment or negative scoring.
- Short sessions: configurable 5 or 10 questions per round.
- Reinforce understanding, not guessing.
- Fully offline once installed.
- English and German are both first-class languages.
- Settings should persist locally.
- One adaptive app for phones and tablets rather than separate codebases.
- Avoid Google Play Services so the APK can also run on Amazon Fire OS.
- Keep dependencies minimal and robust.

## 3. Target devices

Primary phone devices:
- Samsung Galaxy S24-series Android phones.

Primary tablet device:
- Amazon Fire Max tablet / Fire OS.

The UI must adapt automatically:
- Phone: compact portrait-friendly layout, typically 2-column answer grids where appropriate.
- Tablet / landscape: larger cards, wider spacing, more columns, less scrolling, better use of horizontal space.

Touch targets should be comfortably usable by a young child.

## 4. Language options

There must be a dedicated Options screen with:
- English
- German

Changing language should update the whole UI, not only questions.

The selected language must persist between launches.

Current speech uses Android system TTS with installed offline voices. Long-term shared native audio and voice quality follow [VOICE_AUDIO_SPEC.md](VOICE_AUDIO_SPEC.md), retaining system TTS as a fallback:

- English: suitable English locale.
- German: de-DE.

Native Options currently offers all/questions/off audio modes and tutorial reset. Replay and sound-policy reliability remain backlog work; see VOICE_AUDIO_SPEC.md for the intended audio rules.

## 5. Child interests / visual theme

Use these interests heavily to make learning engaging:
- dragons
- dinosaurs
- snakes
- crocodiles
- alligators
- whales
- sharks

Other familiar animals can also appear.

The app should look polished and cheerful, with a nature/adventure theme. Avoid a sterile worksheet appearance. Custom illustrations will replace temporary emoji over time, following ART_DIRECTION.md; games target native 2D/2.5D.

## 6. Home screen / learning areas

The current source is hybrid Compose + legacy WebView. Home and Options are native. Four substantial learning areas now run natively in Compose:

1. Vocabulary Booster / Wortschatz
2. Where is it? / Wo ist es? (Prepositions)
3. Seasons / Jahreszeiten
4. Wilma’s Week / Wilmas Woche

Legacy bundled routes remain available for Animal Actions, Numbers, Easy Maths, Letters, Learn Verbs / Verb Explorer, Mixed Adventure and remaining calendar content until tested native parity allows retirement.

Mixed Adventure should combine different exercise types so the child cannot simply learn one repetitive answer pattern.

## 7. Number learning

### Parent-selectable number ceiling

Options should provide presets:
- 1–10
- 1–20
- 1–50
- 1–100

There should also be a custom maximum for future learning, currently intended to support up to 9,999.

The legacy web Options and generators support custom maxima up to 9,999; current native Options exposes only 10/20/50/100 presets. Restoring custom entry is backlog work.

This means the app can grow with the child without rebuilding it.

### Important pedagogical rule

Do NOT represent large numbers by dumping 70–100 animal icons on screen.

Adapt exercises to the selected level.

Useful number exercises include:
- visual object counting, mainly up to ~20
- recognising a spoken number
- choosing a displayed number
- number before
- number after
- missing number in a sequence
- biggest / smallest number
- tens and ones for higher two-digit levels

Example:
- 47, 48, ?, 50
- Which number comes after 39?
- Which number is biggest: 27, 72, 44, 19?
- Which number has 4 tens and 2 ones?

## 8. Maths

For V1, number recognition can go much higher than arithmetic difficulty.

Keep Easy Maths conservative initially:
- addition within 10
- subtraction within 10
- preferably visual examples using animals

Do NOT automatically make arithmetic harder just because the number-learning ceiling is set to 100.

Future versions can introduce an independent maths difficulty setting.

## 9. Prepositions

Teach simple spatial language visually.

Current required set:
- in / in
- on / auf
- under / unter
- behind / hinter
- next to / neben
- between / zwischen

Use animals and simple objects such as rocks, boxes, trees, etc.

Examples:
- The snake is under the rock.
- The crocodile is next to the tree.
- The dragon is between two rocks.

The exercise should teach meaning visually rather than rely on text alone.

## 10. Letters

V1 focuses on initial letters/sounds rather than spelling whole words.

Examples:
- S → Snake / Schlange
- D → Dinosaur / Dinosaurier
- W → Whale / Wal
- K → Krokodil in German

Match display case until uppercase/lowercase matching is explicitly taught; current case inconsistencies remain backlog work. Keep these activities visually simple. Expand the letter pool over time rather than forcing every letter immediately.

## 11. Days and seasons

Days of the week:
- Monday–Sunday
- Montag–Sonntag

Exercise types:
- What comes before X?
- What comes after X?

Seasons:
- spring / Frühling
- summer / Sommer
- autumn / Herbst
- winter / Winter

Use visual seasonal cues such as sun, snow, leaves, flowers, etc.

## 12. Animal verbs / actions quiz

The verb quiz should teach common, concrete verbs that a young child can understand.

The current project contains 53 child-friendly actions/verbs.

Examples include:
- fly / fliegen
- jump / springen
- hop / hüpfen
- swim / schwimmen
- dive / tauchen
- climb / klettern
- slither / schlängeln
- gallop / galoppieren
- bark / bellen
- meow / miauen
- roar / brüllen
- moo / muhen
- oink / grunzen
- quack / quaken
- buzz / summen
- hoot
- howl / heulen
- hiss / zischen
- peck / picken
- dig / graben
- scratch / kratzen
- kick / treten
- wag its tail
- flap its wings
- stand on one leg
- build a dam
- spin a web
- use a trunk
- reach high leaves
- walk sideways
- squirt ink
- float on its back
- copy sounds
- sleep upside down
- snap its jaws
- stomp
- nibble
- chew wood
- waddle
- paddle
- curl into a ball
- carry a baby in a pouch
- pounce
- graze
- clap flippers
- chase mice
- fetch a ball
- crawl slowly
- flutter
- carry food
- breathe fire (dragon theme)
- stomp like a dinosaur
- stretch a long neck

Keep future additions concrete and age-appropriate. Avoid obscure dictionary vocabulary unless there is a strong teaching reason.

## 13. Verb Explorer — a key V1 feature

V1 must include a dedicated Learn Verbs / Verb Explorer section.

The purpose is to teach the verb BEFORE or alongside testing it, so the child is not merely memorising quiz answers.

Every verb used in the quiz should ideally have a matching lesson.

Each lesson should include:
- large verb word in the selected language
- translation into the other language
- short, child-friendly explanation
- simple example sentence
- spoken audio / TTS
- a visually clear looping animation
- a "Try it!" prompt encouraging the child to imitate the action or sound where appropriate

Example:

Jump / springen
- Explanation: "Jump means going up from the ground."
- Example: "The rabbit can jump."
- Try it: "Can you jump too?"

Slither / schlängeln
- Explanation: "Slither means moving like a snake."
- Example: "The snake slithers across the ground."

Avoid adult dictionary wording.

## 14. Verb lesson access from quizzes

A verb quiz question must have a clear "Learn this verb" / "Dieses Verb lernen" action.

If the child does not know a verb, they should be able to open its lesson, watch/listen, then return to the SAME quiz question.

Do not force them to guess.

## 15. Animations

The goal is polished, cheerful, looping animation suitable for a children's learning app.

The legacy learning surface currently uses local CSS animations for lightweight offline operation. Actual rendering on Samsung S24 and Fire OS still needs physical validation.

Animation families include concepts such as:
- jumping / bouncing
- flying
- swimming
- slithering / sliding
- running
- sound-making / pulsing
- stomping
- rocking
- bobbing
- stretching
- dragon fire

The current implementation is a functional V1, not necessarily the final art style.

When improving animation quality:
- preserve full offline operation
- avoid requiring Google Play Services
- avoid remote web assets
- keep APK size reasonable
- prefer smooth, looping, child-friendly movement
- use clear visual motion that genuinely explains the verb
- do not make the animation visually noisy

The native target uses action-specific animation and custom art under ART_DIRECTION.md. Keep the existing lessons working until their native replacements have tested parity.

## 16. Scoring and feedback

Current display feedback includes temporary emoji, for example:
- Great! 🌟
- Correct! ⭐
- Well done! 🐉
- Super! 🌟
- Richtig! ⭐

Spoken feedback must use deliberate speech-safe text without decorative glyphs (VOICE_AUDIO_SPEC.md); current leakage is P0 work.

Wrong answers should say something like:
- Try again!
- Nochmal versuchen!

Do not deduct points for mistakes.

End-of-round feedback should be encouraging and simple.

## 17. Accessibility / usability

- Large text where possible.
- Large touch targets.
- Good contrast.
- Do not rely on colour alone for correctness.
- Spoken question support.
- Avoid long paragraphs in the child UI.
- Avoid tiny controls.
- Keep navigation shallow and obvious.

## 18. Current technical architecture

The app is hybrid Compose + legacy WebView. Kotlin owns the native home, Options, top bar, shell navigation, transitional SharedPreferences, retained WebView hosting and Android TTS integration. Native Prepositions, Seasons, Wilma’s Week and Vocabulary Booster use shared typed content, audio, deterministic session/restoration and durable local progress-event foundations. The 81-scene Scene Description catalogue is also implemented as a typed, validated native content boundary for future Tell Me / Erzähl mal work.

Legacy HTML/JavaScript still owns unmigrated quiz modes, the 53 Verb Explorer lessons, tutorials and their legacy scoring/completion/animation behavior. The shared native progress foundation is implemented, but the parent-facing Progress dashboard and broader adaptation/classification experience are not.

Important files include:
- app/src/main/java/com/bellfamily/bastischool/MainActivity.kt
- app/src/main/assets/index.html
- app/src/main/assets/app.js
- app/src/main/AndroidManifest.xml
- Gradle project files
- .github/workflows/build-apk.yml

The long-term target is fully native Compose with no runtime WebView/HTML/JavaScript dependency. Migrate incrementally; remove legacy routes only after equivalent native content, behavior and tests establish parity.

All core content must remain local and fully offline, without Google Play Services. Samsung S24 and Amazon Fire Max remain the physical validation targets; compatibility is not established by a successful build alone.

## 19. GitHub / build goal

Repository:
paulyglocke/Basti-Learning-Adventure-Public

The repo contains the Android Studio project.

There is a GitHub Actions workflow intended to build a debug APK.

Expected debug APK path:
app/build/outputs/apk/debug/app-debug.apk

Build commands and historical test evidence are in BUILD_NOTES.md; run checks appropriate to the change under AGENTS.md.

## 20. Current priorities

See BACKLOG.md for the P0–P3 queue. Stabilise current correctness, audio, navigation and device layouts before migrating activities incrementally.

School readiness, communication/listening, progress interpretation/dashboarding and adaptive selection remain broader product goals defined by MASTER_PRODUCT_LEARNING_ROADMAP.md and PROGRESS_TRACKER_SPEC.md. The underlying native progress-event storage already exists; those higher-level product experiences do not yet.

## 21. Future ideas — not required to block V1

Potential later school-readiness modules include:
- colours
- shapes
- more / fewer
- bigger / smaller
- longer / shorter
- sequencing: first / next / then / last
- memory games
- one-, two-, and three-step listening instructions
- emotions
- weather
- yesterday / today / tomorrow
- rhyming
- syllables
- phonological awareness
- classroom routines
- social phrases
- independent school routines

These are useful future directions but should not prevent shipping a stable V1.

## 22. Instructions for coding agents

Follow AGENTS.md for mandatory operating rules, source inspection and verification. Preserve the useful learning content above while following the master specs for future direction.
