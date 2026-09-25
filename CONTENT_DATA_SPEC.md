# Content and Data Specification

## Role

This document owns canonical content identity, schema contracts and validation. [MASTER_PRODUCT_LEARNING_ROADMAP.md](MASTER_PRODUCT_LEARNING_ROADMAP.md) defines what to teach; [NATIVE_ARCHITECTURE_SPEC.md](NATIVE_ARCHITECTURE_SPEC.md) defines storage/runtime ownership. Progress meaning belongs to [PROGRESS_TRACKER_SPEC.md](PROGRESS_TRACKER_SPEC.md), speech policy to [VOICE_AUDIO_SPEC.md](VOICE_AUDIO_SPEC.md), and artwork rules to [ART_DIRECTION.md](ART_DIRECTION.md).

Presentation must derive from structured data. Do not independently hardcode a fact, picture arrangement, question, correct answer and feedback. The broader contracts below remain targets for incremental extraction from existing JavaScript arrays; the first native subset is described next. Start with the fields needed by the first migrated activity, not a general-purpose authoring platform.

## Implemented native foundation (2026-09-22)

The pure Kotlin `learning/models` and `learning/content` packages now provide semantic `ContentId`/`AssetId`, `ContentLanguage`, required `LocalizedText`, separate display/speech `ContentText`, positive schema/revision `ContentVersion`, and immutable colour, weekday and season definitions. `CoreContent.repository()` creates a validated offline snapshot with seven colours, seven weekdays, four seasons and four local image references. No activity consumes it yet; legacy content/routes are unchanged.

`ContentRepository.find()` returns null for an unknown content ID; before/after queries reject unknown/non-weekday IDs. General enumeration is sorted by semantic ID, while `weekdays()` explicitly returns Monday–Sunday and `seasons()` Spring–Winter. Weekday previous/next derives cyclically from the validated sequence. Weekday colour references are supplementary metadata, never identity. Weekdays do not refer to Wilma artwork.

Season names (display and short name speech) are separate from `spokenDescription`, whose EN/DE values exactly reproduce the master roadmap narration. `illustration: AssetId` resolves to an asset-relative path under `Seasons/`; the committed language-neutral PNGs remain unchanged. Asset IDs remain stable when filenames change. Domain code does not load files or render images. A future screen may choose to play the description; this layer does not imply automatic long narration for quiz questions or infer a real-calendar date.

Construction rejects blank bilingual fields and malformed identifiers/versions/paths. Repository validation rejects duplicate content/asset IDs, incomplete or noncanonical weekday/season sets, invalid weekday ordering, missing/wrong-type colour references and unresolved image references. Returned lists and source-input snapshots are immutable. File presence/PNG signatures and exact season narration are checked in unit tests against the committed assets and fixed authored narration fixture; the pure validator checks references, not filesystem existence. Future platform loaders must handle asset-read failures.

This is schema 1 / content revision 1. Persist the version alongside future saved references; changes to authored meaning/order/assets require an explicit revision, and incompatible schema changes require a schema increment and a restoration/migration decision. IDs must not be recycled. Skill registries, vocabulary/grammar, scenes, tasks, phonics, verb lessons, generators and progress persistence remain separate follow-up work; this subset does not claim those validation gates are complete.

## Implemented Scene Description catalogue (2026-09-25)

`learning/scenedescription` is a small pure Kotlin boundary for the committed
SceneDescriptions pack, separate from the quiz-oriented `ContentRepository` (unchanged).
`SceneId` preserves authored IDs such as `scene.ocean.reef_actions.01`; the existing
ContentId grammar is not broadened to accommodate numeric final segments.
`SceneCategoryId` preserves root category IDs. Semantic image AssetIds derive from
scene IDs, independently of filenames, and reuse `LocalImageAsset` for local paths.

`BundledSceneDescriptions.repository()` supplies an immutable schema 1/revision 2
snapshot: `categories()` in root-manifest order, `scenes(category)` in category-manifest
order, `all()`, `find(sceneId)` and `image(sceneId)`. Unknown scene/image IDs return
null; unknown categories return an empty list. Records include approved status, wave,
source metadata path, authored title/purpose or learning focus, typed target-language
groups, examples and adult-support groups/expansion pairs where present. A source
review caution preserves the farm chicken-count limitation; it is not a child prompt.

`SceneText` and `SceneLines` retain explicit unavailable-language handling (null,
never fallback), but the approved production pack now requires complete EN/DE for
all present runtime text. The 54 Wave 2/3 records have authored German for titles,
purpose, target language and adult support; Wave 1's 27 bilingual records remain,
with its existing Farm review caution now bilingual too. Existing scalar/list fields
use the already-supported EN/DE forms, not a third metadata schema. Locale lists
remain separately authored lists, not forced positional translations. Learning-focus
keys are technical tags and are not translated. No absent examples or objectives are
invented. Teaching/reference data are **not** speech-ready ContentText or accepted-answer lists.

Content revision 2 records German completion and two verified Park .05 English
counting corrections (three → four visible yellow buckets); schema remains 1.
All other existing English runtime text, IDs, asset paths and manifest ordering are
preserved by a regression fingerprint. See SceneDescriptions/GERMAN_REVIEW.md for
exact counts, natural-language choices, source ambiguities and limited visual checks.
Bilingual completeness does not establish visual-semantic correctness or independent
native-speaker acceptance. Visible-target graphs and visual audit/prompt/reference
material remain authoring-only; source review cautions are adult reference data.

The standard-library generator `scripts/generate_scene_descriptions.py` follows only
root/category manifests and approved metadata, validates IDs, schema/wave, canonical
PNG/metadata references and known teaching fields, then emits bundled Kotlin. It
rejects malformed/duplicate/missing references before writing output. `--check` verifies
the checked-in output; JVM source-fingerprint checks detect stale manifests/metadata.
The generator is not a runtime parser or a general CMS. Pure repository construction
also rejects duplicate IDs/assets, unresolved categories and invalid record fields.
No Android/Compose/filesystem dependency enters the domain model. Revisions must be
bumped when authored meaning/order changes before any future persisted consumer ships.

Tell Me UI, asset decoding, independent language/visual acceptance, sessions, audio mapping, progress,
speech input and automatic answer evaluation are not implemented by this catalogue.

## Identity and versioning

Use stable, language-independent semantic IDs, for example `animal.crocodile`, `animal.mosasaurus`, `verb.slither`, `colour.red` and `skill.spatial.under`. IDs must not depend on display spelling, translation, list position or asset filename. Content IDs and skill IDs are different: `colour.red` is content, while recognising red may reference `skill.colours.red`.

The shorter skill names in GAME_DESIGN_SPEC.md and PROGRESS_TRACKER_SPEC.md are conceptual examples. The native registry uses the `skill.` prefix and explicitly maps aliases such as `spatial.under` to `skill.spatial.under`. Likewise, decide one canonical ID for synonymous names such as beside/next_to. Never create duplicate progress records by silently treating aliases as new skills.

Keep definition IDs distinct from scene-instance IDs and generated task-instance IDs. Include content-pack/schema versions in persisted references where changes affect replay or interpretation. Renames require an explicit alias/migration; never recycle an old ID for another meaning.

## Core records

These are conceptual data types, not fixed Kotlin constructors. Prefer small immutable records and typed enums/IDs over unrelated string maps.

| Type | Minimum responsibility / fields |
| --- | --- |
| `LocalizedText` | Authored English and German display text for one semantic item. Required locales must be nonempty; missing German must not silently fall back to English. |
| Speech text | Paired localized speech text, separate from decorated display text, following VOICE_AUDIO_SPEC.md. Plain strings may explicitly supply both roles. Optional language-specific pronunciation override reference. Accessibility labels are authored separately where their purpose differs. |
| `VocabularyItem` | ID, bilingual names, speech, illustration asset ID, short explanation/example, category/tags, difficulty, skill references and grammar metadata when needed. |
| `Animal` | ID linked to vocabulary, classification/habitat references, action IDs, fact/measurement references and custom asset IDs. Fictional creatures must be marked as such; do not give dragon fantasy facts scientific status. |
| `Verb` / `Action` | Stable ID, bilingual forms, explanation/example/try-it prompt, eligible subject IDs, skill references and semantic animation/asset reference. A quiz action must resolve to a lesson; avoid string matching on English labels. |
| `SkillDefinition` | ID, bilingual parent label, domain, prerequisites and context expectations. Mastery/support/recency rules remain in PROGRESS_TRACKER_SPEC.md. |
| `SceneObject` | Reusable object definition: ID, object type, content reference, asset ID and semantic label reference. |
| `SceneItem` | Unique instance ID, object-definition ID, attributes such as colour/size/count and semantic placement/relations. Rendering coordinates do not replace relation meaning. |
| `Question` / `Task` | Definition and instance IDs, task kind, skill IDs, language/content version, scene, instruction/feedback template references, ordered choices or valid actions, accepted answer rule, hints, context IDs and difficulty. |
| `Fact` | ID, subject, typed predicate/value or reviewed assertion, qualifiers/context, optional provenance note and localized explanation template. |
| `Measurement` | Numeric value or bounded range, unit, metric, subject/context, approximation/uncertainty when applicable. |
| `ComparisonData` | Subject IDs, metric, measurement references, comparison context and tie/uncertainty policy; no independent answer string. |
| Difficulty metadata | Skill-specific constraints such as number range, instruction steps, option count or distractor similarity, plus prerequisites/tags. Do not equate speed with ability. |
| Content pack | ID/version, purpose, member IDs, prerequisites, age/learning scope and asset dependencies. Packs reference shared records instead of cloning them. |

A task kind can be choice, sequence, placement, matching or open-ended communication. Do not force game state machines or speaking prompts into a single correct-answer quiz schema. Open-ended tasks use participation/parent observation where appropriate, not automatic pronunciation scoring.

## Weekday/calendar content

Use stable language-independent weekday IDs:

| ID | English | German | Wilma cue |
| --- | --- | --- | --- |
| `day.monday` | Monday | Montag | green |
| `day.tuesday` | Tuesday | Dienstag | red |
| `day.wednesday` | Wednesday | Mittwoch | yellow |
| `day.thursday` | Thursday | Donnerstag | blue |
| `day.friday` | Friday | Freitag | purple |
| `day.saturday` | Saturday | Samstag | orange |
| `day.sunday` | Sunday | Sonntag | pink |

The order above is canonical and cyclic. Before/after and yesterday/tomorrow tasks should derive from that ordered data rather than hardcoding language strings into question logic.

Wilma is a presentation model over this data, not the identity of a weekday. Her head and tail are decorative; exactly seven body segments correspond to the seven weekday IDs in canonical order. Current source artwork lives under `app/src/main/assets/Wilma/`. Do not encode English/German text into those images.

The Kita-aligned colour is a familiar retrieval cue, but colour must not be the only carrier of meaning. Presentation and accessibility should also expose segment position, localized visible text and spoken day name. A future content model may store the cue as a typed colour reference, but code must not infer that every green object means Monday.

For real-calendar prompts such as “today”, “yesterday” and “tomorrow”, derive the date/day from the relevant local calendar context. For simulated practice tasks, explicitly establish the pretend/current day in the task state so the answer is deterministic.

## Bilingual and speech authoring

English and German share semantic IDs and task meaning, but phrasing is authored separately where grammar matters. Store German noun gender, singular/plural forms, articles and required case forms when templates need them; verbs may need reviewed inflections. Metadata is not permission to mechanically generate complex German feedback. Use reviewed whole sentences or constrained templates with validated forms.

Keep letter names, phonemes and initial-letter tasks distinct. Match uppercase/lowercase display unless case matching is the learning objective. Authors must review misleading initial sounds/clusters; TTS letter names are not automatically phonics instruction.

Generated narration uses deliberate semantic wording for numbers, operators and relations. Decorative emoji, stars or UI glyphs do not become speech content. The final speech boundary still sanitizes defensively as specified in VOICE_AUDIO_SPEC.md. Asset IDs resolve to local custom artwork; temporary migration placeholders must be explicit. Asset formats/style remain solely in ART_DIRECTION.md.

Phonics content is language-specific even when the underlying vocabulary item is shared. English S → snake does not imply German S → Schlange is an equivalent simple /s/ lesson. Keep letter names, graphemes, phonemes and initial-sound tasks distinct, use separate reviewed phonics packs where needed, and reject misleading clusters/examples. Reviewed recorded phoneme clips may be preferable to ordinary TTS for isolated sounds.

## Facts and comparisons

Fact values live once in data. Renderers and bilingual sentence templates derive their values and answers from those records; do not copy numeric facts into question strings. A reviewed verbal assertion can be stored as a fact, but numeric/comparison logic requires typed values.

Measurements require explicit units and compatible dimensions. Normalize compatible units before comparing; never compare mass to length. Include relevant context such as adult/juvenile, typical/maximum, environment or estimated extinct-animal range. Overlapping ranges or equal values must not produce an unjustified unique answer.

Superlatives such as fastest/heaviest require a defined metric, comparison group and context. Avoid vague “most dangerous” claims. A provenance note may include source title/reference, review date and uncertainty; it is authoring metadata, not a runtime network dependency. Review surprising or changing facts before shipping, and simplify language without inventing certainty.

## One task, one scene meaning

The question, visual, spoken instruction, ordered answer choices and feedback must all derive from the same task/scene model. Localized templates consume that model; they do not create competing facts.

For example, an `under` task binds a crocodile instance, a table instance and their relation. The renderer places the crocodile under that table; the answer rule accepts under; EN/DE speech and feedback name that same animal and table. A between task requires two reference instances. Spoken choices, when enumerated, must match the actual displayed options and order.

Persist or retain the generated task while answering. A recomposition, Replay or hint must not resample the scene/choices. Derive feedback from the selected action and expected relation, not a generic hardcoded rock sentence. Context IDs describe meaningful learning variation (objects/setting), not random coordinates; progress can then measure generalisation.

Content generation should deliberately vary contexts—for example interest-led dinosaur egg under tree followed by pencil under book, shoe under chair and crocodile under bridge—so mastery cannot arise from memorising one recurring picture pairing.

## Validation contract

Automate structural validation and generator invariants under [TESTING_QA_SPEC.md](TESTING_QA_SPEC.md):

- Unique canonical IDs; all content, skill, prerequisite, scene, template and asset references resolve. Detect prerequisite cycles where they would make content unreachable.
- Required bilingual display/speech fields and relevant German grammar forms exist; templates bind valid arguments. Preserve umlauts/ß and meaningful punctuation.
- Scene instance IDs are unique; relations have valid participants/cardinality; referenced objects can be rendered and interacted with.
- Every generated closed task is solvable, with its accepted answer/action available. Choice IDs are unique; duplicate or contradictory options are rejected. If several answers are plausible, accept them explicitly or disambiguate the task.
- Numeric facts are finite and plausible for their defined constraints; units and comparison contexts are compatible; ties/unknown values cannot masquerade as unique correct answers.
- Difficulty constraints and pack prerequisites are satisfied. Generation has bounded failure behavior with useful authoring diagnostics.
- Every quiz verb has its intended lesson; custom assets and speech references exist locally.

Human review remains required for age appropriateness, factual credibility, natural German/English, clear phonics and whether artwork actually teaches the intended concept. A valid schema alone does not make learning content correct.

## Native Prepositions slice (2026-09-22)

`PrepositionsContent` supplies an activity-local validated `ContentRepository` using the shared `PrepositionDefinition`, `ContentId`, `ContentText` and `ContentVersion` contracts. It does not change the calendar pack. The six canonical choice IDs are `position.in`, `position.on`, `position.under`, `position.behind`, `position.next_to`, `position.between`; skill IDs use the matching `skill.spatial.*` suffixes. Legacy `nextTo` maps semantically to `next_to`, not a second skill.

Twenty-four semantic scenes/tasks combine snake, dinosaur, dragon and crocodile with those relations. Scene IDs use `scene.prepositions.<animal>.<relation>`; task definitions use `task.prepositions.<animal>.<relation>`. A context identifies the animal/reference-object pairing, not coordinates. The one authored relation record owns the correct choice, rock/table/box reference and count, bilingual phrase and feedback. German subjects/articles and dative phrases are authored separately. `between` requires two rocks; `under` uses the table, not a rock.

There are no legacy Prepositions PNGs to migrate. `ReferenceObject` and `PositionGeometry` are native drawing contracts for rock/table/box and their layer order; animal glyphs remain explicit temporary migration placeholders. No season/Wilma image is repurposed. A scene ID is independent of its drawing or future asset path.

The finite seeded generator selects five or ten distinct scenes and four distinct choices per task, including the answer, then constructs bilingual instructions enumerating that exact choice order. Task instance IDs come from the frozen session and ordinal. Missing vocabulary/invalid choices reject generation. Restoration validates saved questions against the authored scene and pack revision, without rerunning the seed. This activity pack is schema 1 / revision 1, with activity revision 1; incompatible meaning/geometry changes require a deliberate revision and recovery decision. The wider context/generalisation curriculum remains future work; these 24 familiar scenes do not prove mastery.

## Native Wilma weekday presentation (2026-09-22)

`WilmaContent` consumes the unchanged CoreContent weekday IDs, names, colour references and cyclic previous/next operations. It maps each canonical weekday to its committed `Wilma/day_segment_<day>_<colour>.png`; head, tail and full-reference paths have no weekday ContentId and cannot become answers. PNG paths/presence and exact bilingual mapping are tested. Labels remain rendered text; no PNG is changed.

Find Day and Before/After tasks have separate authored EN/DE templates and `task.wilma.<find|before|after>.<anchor>` IDs. Answers derive from the weekday cycle. Question choice order remains canonical to preserve Wilma’s physical sequence; seeded generation varies questions, not body order. Recognition uses `skill.weekdays.recognise.<day>`, relations use `skill.weekdays.before/after`, and context is honestly `context.weekdays.wilma`, not seven invented generalisation contexts. Activity revision 1 validates restored task wording/answers against current authoring.

Ordering uses seven positions, `task.wilma.order.<day>`, `skill.weekdays.sequence` and `context.weekdays.wilma_order`. Its actual scrambled candidate sequence is persisted, with canonical correct placements. This is an activity-specific sequence task rather than a five/ten quiz round. Explore produces no attempt records. Today/Yesterday/Tomorrow remains follow-up requiring an explicit anchor; this implementation never reads a device date to choose a weekday.


## Native Vocabulary starter slice (2026-09-22)

`VocabularyDefinition` adds canonical ID, bilingual `ContentText`, a required semantic category reference, separately authored find prompt and short example, and an explicit `TemporaryAnimalVisual`. `VocabularyCategory` supplies bilingual category text. `VocabularyPack` validates duplicate/missing category references, unique items, required bilingual fields and a minimum four-item candidate pool. A visual has a checked semantic mapping, not a filename used as identity. No image path is fabricated: the repository has no standalone illustrations for these words yet.

The first six records reuse the legacy Letters/animal vocabulary: `animal.dinosaur` (Dinosaur / Dinosaurier), `animal.snake` (Snake / Schlange), `animal.whale` (Whale / Wal), `animal.horse` (Horse / Pferd), `animal.crocodile` (Crocodile / Krokodil) and `animal.fish` (Fish / Fisch). All reference `category.animals`. Legacy `croc` maps conceptually to `animal.crocodile`; legacy arrays and routes are not rewritten. Original language-neutral illustrations for all six remain required before production artwork acceptance. Temporary glyphs stay separate from speech and can later be replaced without recycling semantic IDs.

The pack is schema 1 / revision 1, independent of CoreContent's calendar version. `activity.vocabulary.find` and `activity.vocabulary.name` each use activity revision 1. Question/skill IDs distinguish `word_to_picture` and `picture_to_word`, with the animal identity as the final component. The honest context is `context.vocabulary.temporary_animal_visual`; repeated exposure to one glyph is not evidence of generalisation or independent reading mastery. Category metadata does not imply a category quiz or a general animal ontology.

A finite seeded six-item cycle supplies five/ten tasks and four unique animal choices including the answer exactly once. Saved tasks contain their actual order; restore validates authored text/semantic mapping rather than regenerating from a seed. English/German prompts (including German articles) are authored separately. Examples model short animal/action sentences; Explore invites an unscored sentence of the child's own. No runtime translation, phoneme claim, automatic speech scoring or broad curriculum expansion is introduced.


### Seasons cycle task content (2026-09-23)

`SeasonIds.next/previous` derive cyclic neighbors from the existing explicit Spring–Summer–Autumn–Winter order, rejecting unknown IDs. `SeasonsCycle` authors NEXT/BEFORE prompts and feedback independently in EN/DE, preserving canonical names, narration and PNG references. Four candidates per direction guarantee every boundary is visited before repetition in a five/ten-question round. Choice order is seeded and frozen. The three distractors are the anchor itself (recognition instead of relation), the reverse neighbor (direction confusion), and the opposite season (skipping a step); no unrelated content substitutes for a season.

Build the Year uses `activity.seasons.order`, `task.seasons.order.<season>`, `skill.seasons.sequence` and `context.seasons.year_order`. Its introduction explicitly chooses Spring as the start of this display of a cycle, and its completion reiterates Winter → Spring. Relation skills are `skill.seasons.next/before` with `context.seasons.year_cycle`. No new weather/holiday stereotypes, missing-season or clue-matching content is added. The consolidated roadmap delegates exact authored narration to canonical content; all eight original spoken descriptions remain locked by an exact regression fixture.
