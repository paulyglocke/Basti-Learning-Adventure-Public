# Content and Data Specification

## Role

This document owns canonical content identity, schema contracts and validation. [MASTER_PRODUCT_LEARNING_ROADMAP.md](MASTER_PRODUCT_LEARNING_ROADMAP.md) defines what to teach; [NATIVE_ARCHITECTURE_SPEC.md](NATIVE_ARCHITECTURE_SPEC.md) defines storage/runtime ownership. Progress meaning belongs to [PROGRESS_TRACKER_SPEC.md](PROGRESS_TRACKER_SPEC.md), speech policy to [VOICE_AUDIO_SPEC.md](VOICE_AUDIO_SPEC.md), and artwork rules to [ART_DIRECTION.md](ART_DIRECTION.md).

Presentation must derive from structured data. Do not independently hardcode a fact, picture arrangement, question, correct answer and feedback. The broader contracts below remain targets for incremental extraction from existing JavaScript arrays; the first native subset is described next. Start with the fields needed by the first migrated activity, not a general-purpose authoring platform.

## Implemented native foundation (2026-09-22)

The pure Kotlin `learning/models` and `learning/content` packages now provide semantic `ContentId`/`AssetId`, `ContentLanguage`, required `LocalizedText`, separate display/speech `ContentText`, positive schema/revision `ContentVersion`, and immutable colour, weekday and season definitions. `CoreContent.repository()` creates a validated offline snapshot with seven colours, seven weekdays, four seasons and four local image references. No activity consumes it yet; legacy content/routes are unchanged.

`ContentRepository.find()` returns null for an unknown content ID; before/after queries reject unknown/non-weekday IDs. General enumeration is sorted by semantic ID, while `weekdays()` explicitly returns Monday–Sunday and `seasons()` Spring–Winter. Weekday previous/next derives cyclically from the validated sequence. Weekday colour references are supplementary metadata, never identity. Weekdays do not refer to Wilma artwork.

Season names (display and short name speech) are separate from `spokenDescription`, whose EN/DE values exactly reproduce the master roadmap narration. `illustration: AssetId` resolves to an asset-relative path under `Seasons/`; the committed language-neutral PNGs remain unchanged. Asset IDs remain stable when filenames change. Domain code does not load files or render images. A future screen may choose to play the description; this layer does not imply automatic long narration for quiz questions or infer a real-calendar date.

Construction rejects blank bilingual fields and malformed identifiers/versions/paths. Repository validation rejects duplicate content/asset IDs, incomplete or noncanonical weekday/season sets, invalid weekday ordering, missing/wrong-type colour references and unresolved image references. Returned lists and source-input snapshots are immutable. File presence/PNG signatures and exact season narration are checked in unit tests against the committed assets/roadmap; the pure validator checks references, not filesystem existence. Future platform loaders must handle asset-read failures.

This is schema 1 / content revision 1. Persist the version alongside future saved references; changes to authored meaning/order/assets require an explicit revision, and incompatible schema changes require a schema increment and a restoration/migration decision. IDs must not be recycled. Skill registries, vocabulary/grammar, scenes, tasks, phonics, verb lessons, generators and progress persistence remain separate follow-up work; this subset does not claim those validation gates are complete.

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
