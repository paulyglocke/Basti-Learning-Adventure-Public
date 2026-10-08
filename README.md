# Basti's Learning Adventure

**An offline, bilingual, manipulation-first learning adventure for school readiness.**

Basti's Learning Adventure is an Android learning environment for young children, built around calm, short and increasingly hands-on learning experiences in English and German.

The goal is not to build a collection of disconnected quizzes. The app is evolving into a coherent learning world where a child can **hear, explore, recognise, manipulate, build, sort, sequence, describe, reason and apply what they have learned**.

Dinosaurs, dragons, animals and familiar characters provide motivation and continuity while the underlying curriculum develops communication, listening, language, early maths, time, school routines, practical skills, reasoning and independence.

Core learning remains fully offline, without accounts, advertising, Google Play Services, lives, streak pressure or reaction-time scoring.

## Current implementation

The app is currently a **hybrid Kotlin/Jetpack Compose + legacy WebView** application undergoing an incremental native migration.

Substantial native learning experiences now include:

- **Prepositions** — 52 scenes across 13 relations, activity revision 3/content 1.3, guided Listen and exact v1/v2 saved-round recovery. Six cave replacements are visually resolved; ten unchanged scenes remain borderline. See the [scene audit](PREPOSITIONS_CONTENT_AUDIT.md); source-art review is not physical acceptance.
- **Seasons** — Explore, recognition, Next/Before, Build the Year, Missing Season (`MISSING`), Season Clues (`CLUES`), Match season to clue (`MATCH`) and combined Before/After (`COMBINED`).
- **Wilma's Week / Wilmas Woche** — Explore, Find Day, Before/After, weekday ordering, explicitly anchored Today/Yesterday/Tomorrow (`TODAY`) and non-quiz German/English weekday learning (`BILINGUAL`). TODAY uses a hypothetical weekday, not the device date.
- **Vocabulary Booster** — bilingual vocabulary exploration and picture/word recognition.
- **Follow the Instructions** — listening and instruction-following activities designed to grow toward richer action, placement and multi-step tasks.
- **Colour Sort** — a bounded native tap-to-place sorting set with semantic membership, support and durable progress.
- **Tell Me** — picture-led expressive-language activities encouraging description and conversation without automatic speech scoring.
- **Animal Groups / Wash Hands** — bounded animal sorting and four-step practical-life sequencing.
- **How many? / Wie viele?** — subitising stable dot patterns from 1–5.
- **Numbers & Groups / Zahlen & Mengen** — quantity → numeral and numeral → quantity matching, 1–5.
- **More or Fewer / Mehr oder weniger** — two groups of 1–5, answering Left/Right/Same to “Which side has more?”; no separate fewer-question mode.
- **Number Order / Zahlenfolge** — Before, After and Missing number questions within 1–5, without wraparound.
- **Add Together / Zusammenzählen** — two visible nonempty dot groups, totals 2–5; no zero or subtraction.

These five Maths activities are implemented visual choice activities with shared quantity data, native session/progress/audio and 5/10-question rounds. They do not yet provide object manipulation or quantity construction. Making five, number bonds, decomposition, conservation, subtraction and broader manipulation-first Maths remain future curriculum. Implementation does not imply physical-device acceptance.

These activities increasingly share native content, audio, support, deterministic session, restoration, completion and progress foundations rather than implementing separate state systems for every screen.

Legacy learning activities remain bundled locally while native replacements are developed and validated. The long-term target remains a fully native Jetpack Compose application with no runtime WebView/HTML/JavaScript dependency.

## Learning philosophy

Basti's Learning Adventure follows one central rule:

> **Difficulty should come from the learning concept — not from reading load, unclear artwork, tiny controls, arbitrary UI conventions, speed or guessing what the app wants.**

Where appropriate, learning progresses through:

**Experience → Explore → Recognise → Manipulate → Recall → Apply → Generalise**

Not every concept needs every stage.

Concept-heavy learning such as Maths can additionally follow:

**Concrete → Pictorial → Abstract**

Activities involving decisions and consequences can use:

**Predict → Choose / Act → Observe → Reflect**

The project is **Montessori-inspired**, rather than a digital Montessori programme. Useful principles include three-period teaching, isolation of difficulty, manipulation before abstraction, control of error, matching, sorting, grading, sequencing, construction, practical-life learning and child-controlled repetition.

Mistakes should normally reveal useful information or lead to calm support and another attempt rather than a punitive WRONG state.

## Reusable learning mechanics

The project increasingly treats activities as:

**content + learning rules + presentation**

built on reusable native mechanics.

The developing mechanic library includes:

- Choice
- Sequencing
- Semantic matching
- Sorting
- Grading
- Placement
- Construction
- Tracing
- Manipulative quantity
- Motion demonstration

Shared abstractions are introduced only when genuine second uses justify them. The goal is reuse without creating a speculative generic game engine.

Touch interaction should not rely exclusively on drag-and-drop when a simpler tap-to-place alternative can provide the same learning experience.

## Support and progress

Support is part of learning, not failure.

Where appropriate, activities can progress through:

**Independent → Replay → Hint → Model / Demonstration → Parent Support → Successful after support**

The long-term progress model is therefore richer than simply counting completed activities.

Progress evidence can distinguish whether a concept was recognised or recalled independently, completed with support, or successfully applied in a different context.

This allows later activities and adventures to provide evidence that a concept has been **generalised**, rather than merely memorised in one screen or illustration.

## Curriculum direction

The wider curriculum is organised around connected learning strands rather than isolated games.

### Language and communication

Vocabulary, listening, Follow the Instructions, Tell Me, sentence expansion, description, reasoning, story retelling and conversation.

A useful progression is:

**Hear → Recognise → Name → Describe → Expand → Explain**

Open-ended speech encourages communication without automatic pronunciation scoring.

### Time and calendar

The long-term progression is:

**Wilma's Week → Months → Seasons ↔ Months → Clock & Time → Calendar & Dates → Combined Time Missions**

Months will centre on a Year Wheel, month recognition, Before/Next/Between, Missing Month, progressive Build the Year activities and Months ↔ Seasons relationships.

Clock & Time will introduce a manipulable analogue clock connected bidirectionally to a digital representation through one semantic time model.

Later My Calendar activities can introduce real month grids, dates, weekdays, Today and meaningful personal landmarks.

### Numbers and Maths

Maths prioritises understanding over increasingly large numbers.

The broad progression is:

**Count → Quantity → Numeral → Composition → Operations → Patterns → Measurement → Time**

Manipulation should come before abstraction where appropriate.

Children may touch and count objects, construct quantities, combine groups, use number lines and manipulate representations before conventional equations become the primary presentation.

### Colours

Colour learning grows beyond identifying a coloured object.

The progression can include:

**Explore → Recognise → Colour + Object → Match → Sort → Grade → Follow Instructions → Patterns → Light/Dark → Colour Mixing → Generalise**

Colour activities can later reuse shared Sorting and Grading mechanics.

### Letters and sounds

A broad progression is:

**Hear → Discriminate → Identify → Trace → Connect sound and symbol → Use**

English and German phonics content must be authored and reviewed appropriately for each language rather than treated as direct translations.

### Practical Life and School Skills

Practical activities can include:

- packing a school bag;
- getting dressed;
- washing hands;
- brushing teeth;
- setting a table;
- tidying;
- classroom routines;
- asking for help;
- asking for repetition or clarification.

These should favour sequencing, placement, construction and consequence over ordinary multiple-choice presentation.

### Compare & Discover

Progression can include:

**Same/Different → Match → Sort → Grade → Multiple Attributes → Rule Switching**

Concepts can include size, length, quantity, colour intensity, shape, habitat, movement, living/extinct and other meaningful classifications.

## Generalisation

Recognising one piece of artwork is not the final goal.

Concepts should gradually move through different presentations:

**familiar themed context → different themed context → different layout → more neutral presentation → application in another activity or story**

Dinosaurs, dragons and favourite animals motivate learning, but should not become the definition of the concept being taught.

A child who understands a quantity using dinosaurs should eventually recognise the same quantity using blocks, fruit, dots or other objects.

## Story Adventures

A major long-term direction is **Story Adventures / Geschichten-Abenteuer**.

These are not intended to be passive digital storybooks or quizzes wrapped in a story. They provide a generalisation layer where learning concepts can be used naturally inside meaningful situations.

A typical reasoning loop is:

**Predict → Choose → Observe → Reflect**

An important design rule is:

> **When the app asks the child to make a choice, that choice should actually change something.**

Early choices may simply change where the story goes. Later stories can introduce immediate consequences, problem solving, delayed consequences, planning and simple perspective taking.

Stories may remember semantic facts such as finding an object, helping a character or visiting a location. Branches can diverge, produce meaningful consequences and later rejoin without requiring a large role-playing-game engine.

Story Adventures can eventually combine existing learning systems including listening, instructions, vocabulary, description, prepositions, sorting, matching, sequencing, counting, Maths, colours, time and Practical Life.

After a story, the same experience can support Beginning/Middle/End sequencing, retelling, cause-and-effect discussion and optional prompts such as:

- What happened?
- Why do you think that happened?
- What might happen next?

A small experimental Story Adventure should come before the full system to establish whether the central interaction —

**I choose → the story changes**

— is clear, enjoyable and useful.

## Story design

Early stories should remain small enough to author and validate properly.

A useful structure is:

**branch → consequence → rejoin**

with selected important choices producing genuinely different later routes or endings.

Stories can remember semantic facts such as:

`HAS_RED_KEY`
`HAS_UMBRELLA`
`HELPED_TURTLE`
`VISITED_FOREST`

These are meaningful story facts rather than scores.

Early adventures may have two or three satisfying endings without labelling them GOOD or BAD.

Replay should encourage curiosity about alternatives rather than completion pressure.

A simple Story Map may eventually show discovered routes and a small number of unexplored possibilities without introducing completion percentages.

## Story artwork and continuity

Story artwork requires stronger continuity than isolated activity artwork.

The preferred pipeline is:

**Narrative graph → Scene requirements → Visual bible → Reference sheets → Artwork → Visual-semantic audit → Integration → Physical acceptance**

Recurring characters should remain recognisable between scenes, including clothing, proportions, important objects and environmental continuity.

Automated validation can verify files, IDs, required records and other structural properties, but it cannot prove that an illustration communicates the intended meaning. Human visual review and physical-device acceptance remain part of the content pipeline.

## Familiar characters

A small recurring cast can give the learning world identity and familiarity.

Potential recurring characters include Dragon, T-Rex, Crocodile, Turtle, Frog and Eagle.

Characters should travel across the curriculum rather than permanently becoming subject mascots. Dragon does not always need to mean Maths, for example.

The learning concept remains more important than the theme carrying it.

## Artwork and motion

Final production artwork should be original, child-friendly and semantically clear.

Artwork is part of the learning contract: if a question depends on something being visible, the image must genuinely support that interpretation.

Animation follows the same principle:

> **Does the movement teach something?**

Instructional motion has priority over decorative motion.

The preferred native direction is deterministic 2D/2.5D animation built from stable artwork, layers, key poses and transforms.

Action-specific motion can eventually support Verb Explorer, Follow the Instructions, Vocabulary, movement activities and Story Adventures.

## Today's Adventure

The long-term child-facing experience is **Today's Adventure**: a short, optional route through complementary kinds of learning rather than an endless activity feed.

A session might contain:

**Confidence activity → Developing skill → Manipulation → Story chapter → Celebration**

The child can stop between activities. There are no missed-day penalties or streak requirements.

Once enough trustworthy progress evidence exists, Today's Adventure may use it to mix secure skills, developing concepts, spaced review, different contexts, communication and movement without becoming an opaque adaptive scoring system.

## Shared experience principles

Across activities:

- audio-first interaction should allow participation without independent reading;
- sessions should normally remain short and predictable;
- Sound Off must be respected consistently;
- touch targets should remain comfortable;
- relevant layouts should survive portrait, landscape and larger text;
- restoration should not unexpectedly replay speech;
- unnecessary animation should reduce during thinking;
- colour should not be the sole essential cue outside colour-learning tasks;
- child-controlled continuation is preferred after meaningful feedback;
- drag should not be mandatory where tap-to-place is sufficient;
- support should be calm and non-punitive;
- no lives, streak pressure, forced timers or reaction-speed scoring.

## Completion celebration

Native practice activities can use the shared optional completion celebration with floating balloon targets, animal reveals and brief local confetti.

Completion controls remain immediately usable. Celebration interaction is optional, non-durable and unrelated to mastery or score.

Sound Off blocks celebration audio.

## Quality and physical acceptance

Current [CI](.github/workflows/build-apk.yml) runs native JVM tests, Python content/asset/freshness checks, debug build/lint, release guards and legacy browser tests. It has no explicit Android-test compilation gate and does not execute instrumentation.

Android/Compose tests exist. The 2026-10-08 implementation checkpoint passed local Android-test compilation, but no emulator/device was connected, so instrumentation was not executed. Older emulator results apply to their recorded revisions, not automatically to newer modes. See [TESTING_QA_SPEC.md](TESTING_QA_SPEC.md) for the separate validation layers and [SESSION_HANDOFF.md](SESSION_HANDOFF.md) for dated results.

Physical acceptance remains a separate part of the product definition.

Samsung Galaxy S24 Ultra is the primary phone test target, with Amazon Fire Max providing tablet acceptance.

Physical testing covers learning semantics as well as software correctness: artwork clarity, touch interaction, audio, Sound Off, navigation, lifecycle, restoration, portrait/landscape layouts and larger text.

The remaining S24 sweep includes the newest Maths, Wilma, Seasons and Prepositions modes. Preserve the already-confirmed S24 upgrade, specific Seasons flows and Wilma auto-follow evidence in the [QA ledger](TESTING_QA_SPEC.md#current-physical-acceptance-ledger-and-fire-max-checklist--2026-09-24). Fire Max acceptance remains outstanding.

## Development direction

The roadmap increasingly asks:

> **Which reusable learning capability should the app gain next, and which activities can then use it?**

rather than simply:

> Which activity should be built next?

Examples:

**Sequencing → Weekdays, Seasons, Months, routines and stories**

**Sorting / Grading → Colours, Compare & Discover and Maths**

**Manipulation → Maths, Clock & Time and construction activities**

**Instruction / Action → Follow the Instructions, School Skills and missions**

**Semantic Matching → Memory Pairs and concept relationships**

**Choice / Consequence → Story Adventures**

This keeps new curriculum connected while avoiding premature mega-frameworks.

## Offline

The app is offline-first and core learning must remain fully offline.

All current learning content is stored in the APK and there is no INTERNET permission. Speech depends on installed English/German offline voices.

The project has no Google Play Services dependency.

## Native architecture at a glance

The native migration is built around shared foundations rather than one-off screens:

- **Content** — stable semantic IDs, required EN/DE display/speech fields, deterministic canonical repositories and validation.
- **Audio** — shared policy and ownership with cancellation, explicit language/voice failures and silent restore.
- **Sessions** — deterministic task plans, support tracking, correct-once scoring where appropriate and exact checkpoint restoration.
- **Progress** — bounded local event storage with stable dedupe keys and retryable delivery.
- **Navigation** — Compose owns native destinations while legacy WebView routes remain available during migration.
- **Offline-first** — learning requires no INTERNET permission.

See [NATIVE_ARCHITECTURE_SPEC.md](NATIVE_ARCHITECTURE_SPEC.md), [CONTENT_DATA_SPEC.md](CONTENT_DATA_SPEC.md), [VOICE_AUDIO_SPEC.md](VOICE_AUDIO_SPEC.md), [PROGRESS_TRACKER_SPEC.md](PROGRESS_TRACKER_SPEC.md) and [MASTER_PRODUCT_LEARNING_ROADMAP.md](MASTER_PRODUCT_LEARNING_ROADMAP.md) for the detailed contracts.

## Historical v1 release

The links below refer to the **historical v1.0.0 APK**, not a build of current `main`.

[Download version 1](https://github.com/paulyglocke/Basti-Learning-Adventure-Public/releases/tag/v1.0.0)
· [Historical APK in this repository](releases/v1.0.0/Basti-Learning-Adventure-V1-debug.apk)
· [Build and test results](BUILD_NOTES.md)

The historical v1.0.0 APK is debug-signed and predates much of the current native architecture and curriculum.

## Building current source

Requirements: a complete **JDK 17** (including `javac` and `jlink`), Android SDK
platform 35 and build-tools 34.0.0. Point `local.properties` at your SDK with
`sdk.dir=/your/android/sdk`, or set `ANDROID_HOME`.

```sh
./gradlew assembleDebug lintDebug
```

On Windows use `gradlew.bat`. Android Studio can import this folder directly.
The wrapper pins Gradle 8.9 and verifies its distribution checksum. The first
build needs internet to download development tools; the installed app does not.

Set `JAVA_HOME` to your complete JDK 17 installation if your default Java is
only a runtime. Keep your SDK location in the ignored `local.properties` file.

## Browser verification

Node.js 18+ is needed only for development checks, not for building/running the
Android app. Tests load the packaged files with the browser offline.

```sh
npm ci
npx playwright install chromium
npm test
```

On a minimal Linux CI host use `npx playwright install --with-deps chromium`.
The test configuration uses one browser worker to limit memory usage.

The packaged source files are `app/src/main/assets/index.html` (markup/styles)
and `app/src/main/assets/app.js` (content/behavior).
Browser tests use mocked speech and do not verify Compose, Android lifecycle or physical audio.
See [BUILD_NOTES.md](BUILD_NOTES.md) for historical verification results and the
remaining checks on Samsung and Fire devices.

## Installing the APK

For ongoing physical testing use the **stable-signed distribution** artifact described below, not a CI debug APK. Copy the APK to the device and open it, allowing that installer when Android asks, or use `adb install -r path/to/app-release.apk`. Do not uninstall or clear app data between upgrades.

Local `assembleDebug` remains available without secrets and uses the machine's standard `~/.android/debug.keystore`. Its default version is code **1**, name **1.1-dev**. CI debug artifacts are named `Basti-debug-only-<run>-<attempt>`: fresh hosted runners do not share a persistent debug key, so these are not a reliable physical upgrade stream. The application ID remains `com.bellfamily.bastischool` for both build types; do not switch debug/distribution streams on a data-bearing device.

Speech uses installed offline voices. Install English/German voice data in the
device's text-to-speech settings, then test in airplane mode. The learning app
itself does not download voice data. If audio is unavailable, the Hint button
also shows the answer to listening questions.

## Stable signing setup (one time, owner action)

1. Create a dedicated long-lived signing keystore **outside this checkout**, using Android Studio → Build → Generate Signed Bundle/APK → APK → Create new. Keep the alias and passwords private. Choose at least 25 years validity, save an encrypted offline backup of the keystore and credentials, and retain its public certificate SHA-256 fingerprint. Do not regenerate the key for subsequent builds. The wizard can be cancelled after key creation; builds below use the repository's explicit signing configuration.
2. In GitHub repository Settings → Secrets and variables → Actions, add these **repository secrets** (no secret values belong in Git, workflow YAML, issues or logs):

   | Secret | Value |
   | --- | --- |
   | `BASTI_KEYSTORE_BASE64` | Base64 encoding of the entire private keystore file |
   | `BASTI_STORE_PASSWORD` | Keystore password |
   | `BASTI_KEY_ALIAS` | Key alias selected during creation |
   | `BASTI_KEY_PASSWORD` | Private-key password |

   On macOS, `base64 -i /absolute/private/path/basti-distribution.jks | pbcopy` copies the encoding without printing it. Paste it directly into the secret form, then clear the clipboard. Base64 is encoding, not encryption. Never upload the keystore as an artifact. Keep access to repository secrets and the default branch restricted to trusted maintainers.
3. Actions → **Build Android APK** → Run workflow → branch **main** → enable **distribution**. Ordinary push/PR builds do not need or receive signing secrets. The distribution job waits for JVM/debug/lint and browser jobs, decodes the key with restrictive permissions into runner temporary storage, signs `assembleRelease`, verifies the APK signature, and removes the decoded key on exit/cleanup. Signed builds disable Gradle configuration caching and run without a persistent daemon; no credentials are echoed. Private release signing is never used for PR code.
4. Download `Basti-stable-signed-v<versionCode>`. Verify its certificate fingerprint matches the saved identity before distribution. This is a non-debuggable release APK, not a Play upload requirement; no cloud runtime or Play Services is added.

Release packaging fails clearly if any signing field is missing, the file is absent, or an explicit distribution version code greater than 1 is not supplied. It never silently falls back to debug signing or an unsigned release.

### Version policy

CI derives `versionCode = 1_000_000 + 100 × GITHUB_RUN_NUMBER + GITHUB_RUN_ATTEMPT` and `versionName = 1.1.<run>.<attempt>`. Run numbers and attempts must be positive; attempt must be 1–99; the code must not exceed **2,100,000,000**. Reruns of the same run receive distinct increasing codes, and a new run exceeds all attempts of the previous run. Range exhaustion fails rather than wrapping or reusing a code. Python unit tests cover the boundaries.

Keep this workflow's version stream stable. **Never distribute an older run's rerun after a newer run has already been installed**: dispatch a new run instead. Download/install order can differ from build order; always check that the candidate code is greater than the device's installed code. If the workflow is renamed/recreated, the repository is recreated, or local signed builds use higher codes, explicitly advance the base after checking the highest distributed code. Do not reset the counter or use `adb -d` to force a downgrade.

Local signed builds read the following environment variables, or identically named properties in private `~/.gradle/gradle.properties`: `BASTI_KEYSTORE_PATH` (absolute path), `BASTI_STORE_PASSWORD`, `BASTI_KEY_ALIAS`, `BASTI_KEY_PASSWORD`, `BASTI_VERSION_CODE` (explicitly above the installed/distributed code), and optionally `BASTI_VERSION_NAME` (default `1.1`). Environment variables take precedence. Load credentials privately; do not put passwords in `-P` command arguments/shell history or tracked Gradle properties. Then run:

```sh
./gradlew assembleRelease --no-daemon --no-configuration-cache --console=plain
```

Prefer CI as the sole distribution version allocator. Ordinary development remains `./gradlew assembleDebug`; it does not require these credentials. Avoid switching back to a lower-code debug build over a distribution installation.

### Existing installations and data

An installed app can only update with a compatible signing certificate and package ID. A higher version code cannot fix a certificate mismatch. Earlier S24 evidence recorded `INSTALL_FAILED_UPDATE_INCOMPATIBLE`; CI runner debug keys were not retained by the workflow. A private signing key cannot be recovered from the certificate in an APK.

For an installation not yet on the permanent signing stream, compare its public certificate against the intended key and look for the original keystore. If the original signing identity cannot be retained, **one final owner-approved uninstall/reinstall may be necessary**, and uninstall erases local data. There is currently no implemented cross-signature progress export/import. Do not do this silently or describe it as a data-preserving upgrade. The owner already confirmed the S24 same-key in-place upgrade from `1003901 / 1.1.39.1` to `1004501 / 1.1.45.1`; do not reopen that initial migration or replace the signing identity. Fire Max upgrade acceptance and current-mode upgrade regressions remain subject to [TESTING_QA_SPEC.md](TESTING_QA_SPEC.md). UI restoration alone does not prove file-level progress retention/deduplication. Ordinary push CI produces a debug artifact, not a stable-signed distribution or physical upgrade result.

References: [Android app signing](https://developer.android.com/studio/publish/app-signing), [Android versioning](https://developer.android.com/studio/publish/versioning), [GitHub run variables](https://docs.github.com/en/actions/reference/workflows-and-actions/variables), [GitHub secret handling](https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/use-secrets).

## Project guidance

Start with [AGENTS.md](AGENTS.md) and [CODEX_START_HERE.md](CODEX_START_HERE.md). The authoritative design files are [MASTER_PRODUCT_LEARNING_ROADMAP.md](MASTER_PRODUCT_LEARNING_ROADMAP.md), [PROGRESS_TRACKER_SPEC.md](PROGRESS_TRACKER_SPEC.md), [GAME_DESIGN_SPEC.md](GAME_DESIGN_SPEC.md), [VOICE_AUDIO_SPEC.md](VOICE_AUDIO_SPEC.md) and [ART_DIRECTION.md](ART_DIRECTION.md). See [BACKLOG.md](BACKLOG.md) for actionable work.
