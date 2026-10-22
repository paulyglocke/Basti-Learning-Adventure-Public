# Phase 5 curriculum expansion roadmap

The sequence below weighs school-readiness value and breadth of gap against current code reuse, artwork readiness, persistence risk and ability to validate a bounded slice. Scores are qualitative (1 low, 5 high); “risk” is higher when compatibility or scientific/content review is harder.

| Rank | Work area | Learning value | Gap breadth | Reuse/readiness | Complexity | Persistence/content risk | Decision |
|---:|---|---:|---:|---:|---:|---:|---|
| 1 | **5A Colours foundation** | 3 | 5 | 5 | 2 | 2 | Keep first. It is the clearest small content gap, shares seven color IDs and an established sort primitive, and can be tested without new raster art in its first slice. |
| 2 | **5B Animals & Habitats** | 4 | 5 | 4 | 3 | 4 | Keep second, but make the first bounded increment an 8-item exclusive starter sort, then separately create a multi-association habitat model. Canonical art exists for the first eight. Scientific semantics and saved sort compatibility require care. |
| 3 | **5C School & Everyday Skills** | 5 | 5 | 4 | 3 | 3 | Strong school-readiness value; retain as a focused pack after defining shared school-object records/artwork. It depends on real reusable objects and careful handling of flexible routines. Could move ahead of later animal facts if owner prioritizes immediate classroom independence. |
| 4 | **5D Maths & Language Depth** | 5 | 5 | 3 | 4 | 4 | Split into separate reviewable slices: quantity construction first; then language-specific phonological awareness only after reviewed audio/data. This should not become one combined implementation. |

The suggested order remains a reasonable delivery order, with one adjustment: treat 5C as two tracks. A small School Objects + one-step listening pack is a high-value candidate to begin immediately after 5A if school independence is more urgent than extending Animal Groups. The larger animal habitat/facts work should remain behind a content-model/science review gate. Do not implement 5A–5D as broad multi-feature phases.

## 5A — Colours Learning World

### 5A.1 Explore and Find (recommended first task)

- Four reviewed colors: red/rot, blue/blau, yellow/gelb, green/grün.
- Explore is untimed, child-controlled and spoken on manual tap; Find asks for a named color against visible labeled patches.
- Keep current `Colour Sort` Home route direct-launching its existing red/blue four-token activity during this slice. Do not count a chooser or placeholders as functionality.
- Use existing `colour.*` IDs but add new task/activity IDs and versioned session plans. Do not change `activity.colours.sort` or its journal decoder.
- Tests: exactly four canonical colors; bilingual label/speech resolution; correct target present; deterministic seeded tasks; unique choices; replay/help/retry; language switch; no rendering-triggered speech; old/new checkpoint isolation; Compose semantics and 1×/2× layout.

### 5A.2 Match and expand Sort

- Add exact swatch matching, then an 8-token sort pack (2 examples/color). Keep tap-to-place as the accessible path.
- Either use an entirely new `activity.colours.sort.expanded` identity or implement an explicit v1 restoration adapter. Do not extend the current four-item array and hope the old snapshot validates.
- Tests include restoration of the four-object v1 checkpoint, exact 8-ID mapping, duplicate taps, wrong placement correction, category labels and saved progress isolation.

### 5A.3 Everyday transfer

- Author 12 isolated object/color records (3 per color) with clear single dominant colors and separately test color recognition and object naming.
- Reuse a shared school-object model only if Vocabulary or Follow also uses the same objects. Review artwork at phone scale and in German screen-reader wording.
- Add a separate activity/content identity; preserve previous task snapshots. Tests bind each image ID to the intended color and verify no distractor is visually ambiguous.

### 5A.4 Grading, patterns and mixing

- Light/dark and color mixing follow palette/device/language/science review. Pattern work may proceed after the four colors are known but remains its own identity. No more than one new mechanic in each slice.

## 5B — Animals & Habitats

### 5B.1 Expand the introductory sort without corrupting saved sessions

- Curated initial set: whale, dolphin, fish, great white shark → Water; horse, rabbit, dog, cat → Land. Four assets are already in the current sort; four additional canonical PNGs exist.
- Keep phrasing explicitly introductory. Exclude turtle, crocodile/alligator, frog, duck and anaconda from exclusive categories.
- New activity ID/content plan is preferred. If the existing identity is retained, an owner-approved migration design must demonstrate exact restore of old state before any implementation starts.
- Tests: fixed eight-item reviewed map; language labels; semantic/audio descriptions; no category-only color meaning; v1 restore; new 8-item round/persistence; Home/Options restoration.

### 5B.2 Shared animal records

- Establish only a minimum model: stable shared animal ID, localized display/speech labels, canonical asset ID, and optional separately typed/reviewed habitat, movement, feature, and fact relations with provenance.
- Provide aliases for Vocabulary `animal.*`, Animal Groups local IDs, Follow `object.follow.*`, and legacy keys as needed. Do not rewrite journal definitions or legacy WebView checkpoints.
- Tests verify alias resolution and unchanged restore for existing Vocabulary, Follow and Animal Groups state; content validation checks each optional relation's labels/source/allowed cardinality.

### 5B.3 Explore habitats, then matching

- Start with contextual habitats (ocean, woodland, farm/grassland, polar shore, wetland). Prefer approved scenes where clear; add focused habitat artwork only when a scene cannot teach the distinction.
- Model relations as many-to-many. A child can explore, tap an animal to hear a fact, or match it with all authored valid habitat contexts.
- Do not pass general habitat relations through current one-category `Sorting`. Add a focused multi-match mechanic only when the activity proves its contract.
- Tests: multi-valid answers, restored association set, specific vs general habitat prompts, image/content provenance, German factual review and no “Air” habitat.

### 5B.4 Movement, features and prehistoric comparison

- Build one observable movement mini-pack (e.g. four depicted actions) separately from habitat.
- Add visible feature matching only if canonical art resolves the feature at target size; commission closeups where needed.
- Add living/extinct prehistoric comparisons only with species-correct authoring (Pteranodon is a pterosaur; Mosasaurus a marine reptile; Mammoth a mammal). These may use canonical wave 2/3 art after semantic review.

## 5C — School & Everyday Skills

### 5C.1 School-object content pack

- Reuse the proposed eight bilingual words and produce six individually selectable pictures first. Build one vocabulary use and one Follow one-step use from the same semantic records.
- Add classroom-object instruction as a separate content/activity revision, not an in-place transformation of the six-animal pool. Preserve `activity.follow.instructions` revisions 1–4 plans and old choice sets.
- Tests: image/word identity across both consumers, English/German case/articles, 4-choice validity, 5/10 determinism, silent exact restore and one-step interaction semantics.

### 5C.2 Classroom language and self-advocacy

- Add explicit listening/exploration for help, repetition, clarification and break language; no automatic scoring of the child’s speech.
- Keep phrases available visually/spoken in Options language and offer a contextual model. Test the child can replay/choose phrase without affecting a separate task attempt.

### 5C.3 Practical routines

- Add one routine only after authoring its complete image set and determining whether there is one meaningful order. Brush teeth may suit sequencing after health-content review; packing a bag is better as a flexible checklist/place-and-confirm activity. Avoid multiplying routine cards before the first new routine has device and child review.
- Preserve the `activity.practical.wash_hands` revision 1 four-step journal and progress identity. Use a new routine ID/contract. Tests cover order flexibility where intended, outcomes, replay, removals, restoration and failure recovery.

## 5D — Maths & Language Depth (separate tracks)

### 5D.M Quantity construction

- Add a tap-to-place counter tray and five-frame. First objective: construct quantity 5 and see four nonempty partitions (1+4, 2+3, 3+2, 4+1), with rearrangement preserving the total.
- Reuse stable `number.one`–`number.five` identities, `CoreContent` or current quantity IDs as appropriate, shared progress/audio and the proven append/remove pattern only if semantics fit. Do not change the existing static Add Together tasks.
- Tests: conservation, combination states, no double count, keyboard/tap access, deterministic prompts, exact save/restore, bilingual narration and progress evidence. Later one-more/less and subtraction should use visible object changes and remain separate validated tasks.

### 5D.L Phonological awareness

- First author a separately reviewed English/German listening prototype for rhyme or syllables, rather than beginning with isolated phonemes.
- Do not synthesize isolated sounds with letter-name TTS. If adding sound-symbol tasks, use language-specific reviewed audio and a distinct content version. Keep written initial-letter practice labeled as such.
- Tests validate locale-specific task/audio mappings, minimal pairs/rhymes, no cross-locale leakage, replay/cancellation, and old legacy WebView state compatibility.

## Cross-phase acceptance and compatibility gates

Every implementation slice must name existing and new activity IDs, activity revisions, content versions, journal/checkpoint formats, progress skill/context IDs, bilingual strings, audio owner/trigger, route/Options return and legacy compatibility. A content expansion that changes a saved task meaning should use a separate identity/versioned decoder rather than silently replacing old content. Add route/UI tests for existing Home categories and run relevant native and legacy suites. User-facing acceptance still requires actual offline speech, supported-device layout, accessibility and child usability; automated content checks cannot replace those reviews.
