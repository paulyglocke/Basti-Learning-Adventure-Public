# Other high-value curriculum expansions and content reuse

## Vocabulary Booster

### Current evidence

`VocabularyContent` contains six records: **Dinosaur/Dinosaurier, Snake/Schlange, Whale/Wal, Horse/Pferd, Crocodile/Krokodil, Fish/Fisch**. Each record has one bilingual name, one short bilingual example sentence and one Find prompt. Explore lets the child browse; FIND is word→picture; NAME is picture→word. The six pictures are canonical assets and the two quiz modes each have six targets. Vocabulary is therefore a functioning but deliberately small animal starter set, not a broad vocabulary curriculum.

### Recommended first content pack

Add a **School Things pack of 8 words**, not 50–80 words at once:

| Proposed English / German | Why it is useful | Asset status |
|---|---|---|
| backpack / Rucksack | daily routine and Follow instructions | No individual canonical object image found. |
| book / Buch | class instruction and packing | Classroom scenes show books in context; no isolated canonical asset. |
| pencil / Bleistift | common instruction and school use | Classroom scene records mention pencils; no isolated asset. |
| eraser / Radiergummi | useful paired school object | No reviewed isolated asset located. |
| scissors / Schere | classroom task; child-safe depiction required | Scenes include scissors, but no reusable isolated asset. |
| glue stick / Klebestift | class-art context | No reviewed isolated asset located. |
| lunchbox / Brotdose | familiar everyday/school object | No isolated canonical asset located. |
| water bottle / Trinkflasche | school and practical-life context | Scene descriptions use water bottles; no isolated asset. |

This pack supports Vocabulary Explore/Find/Name and can later give Follow instructions real school targets. Add only bilingual labels, deliberate speech forms, one example phrase and target-object records; do not create a huge thematic word list. Food, clothing, body parts, feelings, household words and actions remain later packs. Feelings need functional language and optional expression, not a picture/name quiz alone.

### Overlap and compatibility

Animal names already overlap Animal Groups, Follow and legacy Animal Actions. Existing `animal.*` IDs are stable for the six Vocabulary words; do not create `school.book` and `object.follow.book` as unrelated facts if both activities consume one school-object concept. Keep category membership and image mapping in the shared content definition; activity-specific question text/support stays with its activity. Preserve current `activity.vocabulary.find` and `.name`, revision 1/content 1.1, saved plans and 6-item validation. Add the school pack as new records with a content version and explicit old-plan restore rules, or use a separate activity/content identity. Do not alter old checkpoints so a prior six-animal session silently becomes an eight-school-object session.

## Follow the Instructions and School Skills

Current code has exactly **six canonical animal objects** (crocodile, dinosaur, snake, fish, horse, whale). Level 1 targets each animal once in the candidate set; it speaks one instruction and offers four illustrated choices. Level 2 has the 30 ordered pairs of two distinct animals, supports two taps in order and offers four choices containing both targets. Both levels support 5/10 saved rounds, replay, hints, staged feedback and restoration. The current pool is animal-themed and the instruction is “touch/choose” the named animal; colour/object/spatial instructions are not present.

Recommended sequence:

1. **School object listening (one-step, six objects):** after the 8-word pack exists, start with six focal objects (e.g. book, pencil, backpack, scissors, glue stick, water bottle). Ask one spoken object instruction and select the pictured object. Introduce objects and Listen before a saved quiz round. Bilingual terms must be checked for grammar/case in sentence context, as current Follow has explicit `germanObject` articles.
2. **Attribute + object (one-step):** use a small set of 4–6 duplicated object kinds in clearly distinct colors. Ask “Touch the red book.” This teaches the combined adjective/object target. It must not be an unstructured color quiz or use similar colors before the color curriculum exists. Need object-color combinations and visually distinguishable assets.
3. **Two-step classroom mission:** use the existing ordered two-tap pattern after one-step targets are understood, with two distinct objects (“first touch the book, then touch the pencil”). Explicitly communicate first/next; do not imply every two-step result measures working memory alone.
4. **Spatial placement:** later place an object in/on/under a clearly pictured desk/box. This is a new action/placement mechanic; do not force target tapping to masquerade as spatial placement.
5. **Help-seeking and repetition:** add a supported communication experience (“I need help”, “Can you say that again?”, “I don’t understand”). This should let a child hear/choose or practice the useful phrase without scoring speaking, speech speed or pronunciation. It is an independence/self-advocacy goal, not another distractor quiz.
6. **Three-step actions:** defer until the 1–2 step classroom set is physically reviewed. Treat as an explicit sequential routine, not an automatic “hard” mode; provide replay, visible step state and gentle recovery.

Keep one-step object identification separate from multi-step working-memory demands. Persist new school-object tasks under a compatible content/activity revision; the current Follow contract has revisions 1–4 adapters and content 1.1–1.4 compatibility. Do not reuse its `object.follow.*` IDs for Vocabulary without a deliberate alias/shared-record decision; current records share pictures but are not a common vocabulary model.

## Practical Life

Wash Hands currently has four text-only steps: turn on water → wet hands → wash with soap → rinse. It is a useful sequence-assembly mechanic, with append/remove, Check, Help, Listen and durable state. The sequence does not illustrate the actions or explain the reason for washing; it should be treated as a first mechanical slice.

| Routine | Suggested first content shape | Is one fixed order accurate? | Artwork / implementation need |
|---|---|---|---|
| Brush teeth | 5–6 picture steps (get brush, add toothpaste with adult-supported amount, brush, spit/rinse per reviewed guidance, put brush away). | A simple instructional order is possible, but health guidance and “rinse” wording should be checked with owner; do not imply all dentists use identical steps. | 5–6 distinct close-up actions; toothbrush/toothpaste/water art. Not present as a sequence in current app. |
| Getting dressed | 4–6 clothing/put-on actions for a weather/context prompt. | Often more than one valid order; use a context (e.g. socks before shoes) or placement checklist rather than score one rigid global order. | Clothing item/action illustrations; gender-neutral, varied bodies; no new art present. |
| Pack a school bag | Objects selected and placed against a simple visual plan. | Not naturally a single sequence; items can be packed in several orders. Use a checklist/placement mechanic, then close/confirm bag, rather than a single correct permutation. | School object cutouts and bag interior; overlaps Vocabulary/Follow school pack. |
| Set a table | Place a small set of items relative to a plate/place setting. | Some placement relations matter; allow culturally/contextually valid variants. Do not assume one universal table layout. | Table/plate/cup/cutlery picture assets and a place-setting context. |
| Tidy toys | Match objects to their storage places, then optionally sequence a clean-up routine. | Many valid orders; outcome/state is more important than exact sequence. | Familiar toy/storage scene; can reuse a reviewed Tell Me-style room only if objects are clear and separately targetable. |

Build each as its own content-led routine. Reuse SequenceAssembly when a genuinely fixed order is meaningful; use Placement or an outcome/checklist state for flexible routines. Do not add five routines by copying four labels into the same sequence reducer.

## Maths

### Current scope and overlap

Five native activities cover: subitising 1–5 (five fixed dot patterns), both number↔quantity directions (ten task definitions), more-than comparison including equality (25 ordered group pairs; no separate fewer question), before/after/missing within 1–5 (25 definitions), and addition with two nonempty groups totaling 2–5 (10 ordered facts / 6 unordered facts). All are 5/10 visual-choice sessions and use native-rendered dots/numerals. The common sequence is sound as a bounded introductory set but does not yet implement a manipulative-quantity model.

Legacy Numbers overlaps recognition/order with configurable values up to 9999 and supports count, hear, before/after, missing, biggest/smallest and tens/ones depending on level. Legacy Easy Maths has 83 numeric addition/subtraction facts through 10 and animal emoji stories. These broaden numeric range but do not replace the native 1–5 progression: they do not add tap-counting/manipulative construction or controlled progression evidence.

### High-value progression

1. **Build a quantity / make five:** begin with 5 identical manipulable counters, add/move one at a time, support tap-to-place, and make 5 in two visible groups. The initial nonempty ordered decompositions are 1+4, 2+3, 3+2 and 4+1 (four orderings; three unordered partitions). Then allow rearranging/recombining while preserving total. This adds a real concrete→pictorial bridge and does not duplicate Add Together, which asks for a result from two static visible groups.
2. **Part-whole and one more/one less:** use a stable five-frame or number path; make the relationship observable before a numeral prompt. Avoid two separately branded games if they reuse the same exact mechanic without added instructional value.
3. **Early subtraction:** remove objects from a visible quantity, first within 5, with before/after cardinality explicit; do not introduce a zero answer until the meaning and UI are validated.
4. **Number patterns, shapes, measurement:** later concepts need their own reviewed examples and interaction rules; changing number artwork does not constitute a new skill. Measurement requires consistent units/scale, not merely “big/small” illustration choices.

Use a separate `activity.math.*` identity for construction, an explicit task/content version, and the existing stable number IDs 1–5. Test conservation, addition/removal, repeat taps, accessibility alternatives, task generation, save/restore, and number-to-quantity semantic relations. Do not mutate existing `activity.math.add_within_5` plans or progress meanings.

## Letters & Sounds

The current legacy Letters task displays one uppercase written letter and asks which pictured animal word begins with that letter. English has six records; German has six locale-authored records and changes Horse→Pferd to P and Crocodile→Krokodil to K. Each task has four word choices and audio access. It is a grapheme/orthographic initial-letter match, not explicit phonological awareness.

Future work must distinguish:

- **Letter name:** spoken name of the written symbol (e.g. “bee” for B in English; “Be” for B in German).
- **Grapheme:** written letter(s) used to represent sound in a particular language and word.
- **Phoneme:** an individual speech sound; normal Android TTS for a letter name is not an acceptable isolated-phoneme source.

An appropriate precursor can be environmental/animal sound listening, rhyme or syllable clapping, then same-initial-sound discrimination using recorded reviewed examples. English and German need independent word/audio sets and mappings: phoneme inventories, letter names, spelling-to-sound relations, digraphs and syllabification differ. Do not translate English tasks mechanically to German. Before development, commission a native speaker/early-literacy review and language-specific recorded phoneme prompts or a reviewed local audio pack. Testing needs phoneme asset presence, bilingual mapping checks, minimal-pair audit, replay/cancellation and no false claims of automatic pronunciation scoring.

## Content reuse opportunities

| Concept/record | Existing consumers and IDs | Reuse opportunity | Boundary / compatibility requirement |
|---|---|---|---|
| Canonical animal image/name | Vocabulary `animal.dinosaur`, `.snake`, `.whale`, `.horse`, `.crocodile`, `.fish`; Animal Groups locally uses `.whale`, `.dolphin`, `.horse`, `.rabbit`; Follow owns `object.follow.*`; legacy Actions uses JS string keys. | One shared animal record can support Vocabulary, Groups, Follow, action naming and later facts, avoiding duplicate bilingual names/image selectors. At least three concrete consumers already exist. | Introduce aliases/adapters per old ID and preserve saved `object.follow.*`/activity plans. Do not rename content IDs in existing journals. Shared record must not imply facts not reviewed for each species. |
| Animal habitat/movement/features | Group local Water/Land fields; 53 legacy action records; scene-specific Tell Me vocabulary and prose. No shared structured metadata. | Add small typed lists for habitat relations, observable movement/action IDs and features with provenance only when expanding Animals and at least one second activity. | Habitat is many-to-many. Keep action-specific scene observations separate from general capability/fact claims. Existing one-category sorter cannot consume arbitrary many-to-many relations. |
| Colors | `CoreContent` has seven `colour.*` bilingual definitions; Colour Sort uses red/blue; Wilma uses seven weekday color cue IDs; Tell Me has color terms in scene records. | Reuse canonical names/IDs across new color activities, Vocabulary and instruction content. | Weekday color is not a generalized child mastery/progress record; preserve semantic day IDs and never make color alone determine correctness. |
| Weekdays/seasons/months | `CoreContent`, `WeekdayIds`, `SeasonContent`, `MonthIds` use canonical IDs/order and shared date content. | Reuse current date/season records for calendar and combined missions. | Preserve cycle, meteorological Germany season mapping, ordered IDs, wheel geometry and all saved session plans. |
| School objects / instructions | Tell Me classroom scene strings and visual contexts; Follow has six animal semantic objects; Vocabulary lacks school objects; no standalone object asset/data pack. | Build a reviewed school-object pack consumed by Vocabulary and Follow. | One canonical object identity/image/name should be reused; scene object mentions are not automatically isolateable/selectable asset records. New objects require new content version with old round restore. |
| Spatial relations | Prepositions has typed relations and 52 scenes; Tell Me target terms mention placements; Follow does not place objects. | Reuse relation definitions in a later spatial Follow / scene reasoning activity. | Preserve Prepositions relation IDs/wording revision behavior; composition of art should not imply the same relation if geometry differs. |
| Sequencing | `OrderedPlacement` is used in Wilma/Seasons/Months; `SequenceAssembly` powers Wash Hands. | Keep fixed-order routines separate from flexible packing/tidying outcomes; reuse only after a second true consumer. | Existing routine checkpoint compatibility must be explicit; flexible order cannot be represented as a single fixed target list. |
| Story Adventures | Roadmap proposes branching choice/consequence; no functioning Story Adventure Home entry or production story graph exists. Tell Me is a picture-led conversation with no scored choice/branch. | Later use canonical animal/object/fact IDs in a small experimental story where choice changes a visible outcome. | Do not count Tell Me as the implemented story engine. Preserve open-ended talk and avoid a speculative universal game engine. |

## Artwork gap summary

- **Available/reusable:** 28 canonical individual animal PNGs under `Animals/canonical`; 81 Tell Me contextual images; 52 Prepositions scenes; four Seasons scene images; seven Wilma segments plus head/tail; two habitat cues at `res/drawable-nodpi/habitat_water.png` and `habitat_land.png`.
- **Available but not a substitute for a semantic object pack:** Classroom SceneDescription art shows books, pencils, scissors, bottles and other items in contexts. Isolated item shape, exact image ID, color and tappable bounds are not guaranteed by scene artwork.
- **Temporary/legacy:** legacy Animal Actions, Learn Verbs, Letters, Numbers and Easy Maths use emoji subjects and CSS/HTML scenes; they are not approved individual illustrations.
- **Missing for proposed learning:** 12 isolated everyday-color object images; 6–8 school-object cutouts; handwashing and each new practical routine step illustrations; habitat scene/context art tailored to clearly depict specific environments; feature close-ups if the canonical cutout does not show the relevant trait; recorded English/German phonemes for any phonological-awareness phase.
- **Requires review before reuse:** wave 1 isolated PNG edge quality in-app; all animal images for small target readability; any habitat inference from a single static cutout; color patches/shades on real devices; science and language statements.
