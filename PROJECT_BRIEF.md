# Basti's Learning Adventure — Project Brief

## 1. Purpose

Basti's Learning Adventure is an offline Android learning app intended to help a child prepare for starting primary school in Germany.

The app should feel like a friendly children's game rather than homework. It should build confidence through short, repeatable activities, clear feedback, large touch targets, spoken instructions, and familiar themes.

The target child is approximately 6–7 years old. Keep vocabulary, explanations, instructions, and visual complexity age-appropriate. Do not make exercises unnecessarily advanced just because the app technically can support them.

## 2. Core design principles

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

Text-to-speech should use the device's local TTS engine where possible:
- English: suitable English locale.
- German: de-DE.

There should also be an option to turn spoken questions on/off, while retaining a speaker button to replay a question or lesson manually.

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

The app should look polished and cheerful, with a nature/adventure theme. Avoid a sterile worksheet appearance.

## 6. Home screen / learning areas

The current V1 concept includes:

1. Animal Actions / Tier-Aktionen
2. Numbers / Zahlen
3. Easy Maths / Einfache Mathe
4. Where is it? / Wo ist es? (prepositions)
5. Letters / Buchstaben
6. Days & Seasons / Tage & Jahreszeiten
7. Learn Verbs / Verben lernen (Verb Explorer)
8. Mixed Adventure / Gemischtes Abenteuer
9. Options / Optionen

Mixed Adventure should combine different exercise types so the child cannot simply learn one repetitive answer pattern.

## 7. Number learning

### Parent-selectable number ceiling

Options should provide presets:
- 1–10
- 1–20
- 1–50
- 1–100

There should also be a custom maximum for future learning, currently intended to support up to 9,999.

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
- S → snake / Schlange
- D → dinosaur / Dinosaurier
- W → whale / Wal
- K → Krokodil in German

Keep these activities visually simple. Expand the letter pool over time rather than forcing every letter immediately.

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

Current V1 uses offline CSS/local animations so it remains lightweight and works on both Samsung Android and Fire OS without network access.

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

A future upgrade could use locally bundled vector/Lottie-style animations if licensing and offline packaging are clean, but V1 should remain buildable and reliable first.

## 16. Scoring and feedback

Correct answers should produce friendly positive feedback such as:
- Great! 🌟
- Correct! ⭐
- Well done! 🐉
- Super! 🌟
- Richtig! ⭐

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

## 18. Technical architecture of current V1

The current project is a small native Android wrapper around a locally bundled HTML/CSS/JavaScript learning app.

Important files include:
- app/src/main/assets/index.html
- app/src/main/java/com/bellfamily/bastischool/MainActivity.java
- app/src/main/AndroidManifest.xml
- Gradle project files
- .github/workflows/build-apk.yml

The WebView approach is intentional for rapid iteration and a shared responsive phone/tablet UI.

The app should:
- load all core content locally
- expose local Android TTS to the web UI where useful
- not require internet permission for learning content
- work without Google Play Services

Do not rewrite the app into a completely different framework unless there is a clear technical reason and the migration preserves functionality.

## 19. GitHub / build goal

Repository:
paulyglocke/Basti-Learning-Adventure-Public

The repo should eventually contain the full Android Studio project.

There is a GitHub Actions workflow intended to build a debug APK.

Expected debug APK path:
app/build/outputs/apk/debug/app-debug.apk

Codex should run/build the project and fix Gradle, SDK, Java, manifest, WebView, or workflow errors it discovers.

## 20. Current priorities

For the first installable V1, prioritise in this order:

1. Project builds successfully in Android Studio / CI.
2. App launches successfully on Android.
3. Phone and tablet layouts work.
4. English/German switching works and persists.
5. Core activities function without crashes.
6. Number setting up to 100+ works as designed.
7. Verb quiz works.
8. Verb Explorer lessons work for every quiz verb.
9. Looping animations render smoothly offline.
10. Audio/TTS works where available.
11. APK is easy to sideload to Samsung and Fire tablet.
12. Polish animation/art quality without destabilising the core app.

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

When modifying this project:

- Read this document before making major product decisions.
- Inspect existing implementation before replacing it.
- Preserve the bilingual and offline requirements.
- Preserve Fire OS compatibility.
- Keep content suitable for a 6–7 year old.
- Prefer incremental, testable changes.
- Run the build after significant changes.
- Fix build errors rather than merely describing them.
- Do not remove existing learning modes without a clear reason.
- If changing learning content, explain why the change is age-appropriate.
- Treat polished animation as important, but reliability and clarity come first.
