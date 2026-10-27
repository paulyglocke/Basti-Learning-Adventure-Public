# Proposed implementation sequence and verification plan

Phase numbering is resolved for future work: **Phase 5C — Animals & Habitats**, beginning with 5C.1 audit and 5C.2 Ocean Explore. Older `docs/curriculum-phase5-audit/PRIORITIES.md` references to 5B describe historical planning and are preserved. Each stage should be independently testable and stop if required art/facts are not ready. This audit itself implemented no stage.

**Status update (2026-10-27):** Phase 5C.2 was subsequently implemented as an Ocean-only Explore activity. The actual delivered scope, validation and remaining acceptance are recorded in the [Phase 5C.2 review](../animals-phase5c2-ocean-review/REVIEW.md). The stage descriptions below preserve the original Phase 5C.1 proposal and should be read alongside that implementation review.

## Phase 5C.2 — Explore Habitats Foundation (recommended next)

**Scope:** add the future Explore Habitats entry under existing Animals & Nature navigation, a habitat chooser with Ocean as the only active option, and one complete static Ocean Explore scene using `ocean_001_reef_actions.png` plus exact bilingual scene target notes. Activate only Dolphin initially; consider sea turtle only after exact image/species review. Add sourced bilingual fact, selected outline/state, Replay, labeled target alternative, Back to chooser and Home/Options/language behavior.

**No score/progress:** keep Explore unscored and do not make the existing Animal Groups journal a browser checkpoint. Use ephemeral/session UI state unless an implementation design demonstrates a genuine need for a separate bounded browsing restore checkpoint.

**Dependencies/gate:** visually approve the art and target region, confirm all local target labels/fact source/German, use default offline audio and 56dp controls. If artwork hit regions are too small/ambiguous, use named controls or stop for better art instead of guessing coordinates.

**Focused tests:** content and asset-path completeness; EN/DE text; source-required fact coverage; scene target uniqueness; tap/hotspot ↔ selected target agreement; tap outside target no-op; neutral selection marker; Sound Off and language change; Replay/cancellation; keyboard/accessibility semantics; chooser/scene/back/Home/Options routes; portrait/landscape and 1.5×/2× layout. Regression: Animal Groups route/state/content/journal and existing Vocabulary/Tell Me/Follow route tests. Do not run full instrumentation solely for docs; implementation changes require relevant suites.

## Phase 5C.3 — Underground cutaway scene

**Scope:** after artwork and fact approval, build one shared cutaway with surface, soil, roots, broad tunnel and visible entrance. Start with Mole, roots and soil/tunnel parts. Add Rabbit as a shelter relation only if the transparent cutout style and art fit are accepted. Keep selection non-scored, bilingual, accessible and visually explainable without reading.

**Dependencies/gate:** original layered art brief approved; high-contrast ground boundary; portrait and landscape composition; independent hotspots at large target sizes; source-scoped facts; German review; image license/provenance recorded. Do not include Earthworm, Ant, Badger or Fox until their exact art and content are ready.

**Focused tests:** rendered layer/asset mappings and visual screenshot review; each semantic target and selected state; above/below/inside/next-to/between relations; reachability, font scaling, reduced-motion/static behavior; offline replay/Sound Off; all Animals & Nature route regressions; no change to Prepositions contracts.

## Phase 5C.4 — Additional habitats

**Scope:** add Woodland using an approved SceneDescriptions composition (start review with `woodland_008_homes_choices.png`) and Freshwater only after dedicated scene art plus species-identified visuals are available. Extend the chooser as each habitat becomes functional. Avoid duplicating Ocean/Underground; reuse scene composition and canonical art only after style/context review.

**Dependencies/gate:** authored target geometry, source-backed facts and native-speaker-reviewed EN/DE for each new environment; responsive artwork/focus controls; provenance/licensing; no generic fish/frog/duck claims unsupported by pictured species. An existing Woodland composite can support scene-specific non-scored Explore; Freshwater cannot be approved by substituting an Ocean scene or generic fish PNG.

**Focused tests:** scene mapping and target/fact pairs per habitat; EN/DE chooser and per-scene completeness; no blank/inactive habitat options; no cross-habitat stale selection/audio; all prior phases’ routes/layout/accessibility; image decoding and visual-semantic review.

## Phase 5C.5 — Habitat matching

**Scope:** introduce a distinct scored Match activity only after several scene/fact records are stable. Select a contextual one-answer prompt or a true multi-valid-answer task. Do not use the existing one-category Water/Land sorter for general relations.

**Dependencies/gate:** relationship records include evidence, species scope, age suitability, relationship type and explicit accepted-set policy; each task’s art shows the claimed context; new Activity/Skill/Context IDs and revision/session contract reviewed; progress deduplication and support semantics specified.

**Focused tests:** unique IDs, all and only valid answers, multiple valid associations, no forced single habitat, no Air-as-habitat, deterministic plans, wrong-answer retry/support, exact session restore, language switch, attempt/completion event deduplication, corrupted journal behavior, old Animal Groups revision-1 exact restoration/progress fixtures.

## Phase 5C.6 — Regression and visual acceptance

Review every implemented habitat in English and German on phone portrait, phone landscape/short landscape, tablet-sized portrait/landscape emulator windows, enlarged font and text scale. Inspect screenshots individually, including selected targets/facts and matching feedback/restoration. Check focus traversal, semantics, no color-only state, 56dp controls, insets, scrolling, no horizontal clipping, proper image Fit/crop and language-change routes.

Run the fresh full JVM suite and relevant focused navigation/content/audio/accessibility/persistence tests, then `assembleDebug`, `assembleDebugAndroidTest`, `lintDebug`, and the complete API 35 instrumentation suite when the production changes affect the broad acceptance baseline. Capture exact pass/failure/skip counts and preserve XML/log artifacts. Automated tests and emulator screenshots do not establish physical-child, real TalkBack/switch access, device audio or physical phone/tablet acceptance; track those separately as owner-managed QA.

## Cross-phase compatibility checklist

- No changes to `activity.discover.animal_groups`, revision 1/content 1.1, AGR1, its exact four-object pool, Water/Land map, decoder, `skill.discover.animal_habitat`, or `context.discover.water_land`.
- No rewrites of Animal Actions legacy keys/`verbs` journal/checkpoint or its localStorage/bridge.
- No rewriting Vocabulary six-item content/Find and Name journals/progress or Follow historical revisions/pools/journals.
- Do not promote Tell Me scene metadata into universal animal facts; its scenes/prompts/IDs stay unchanged.
- Do not edit Prepositions wording/pools/journals while borrowing preposition learning examples.
- No Progress Tracker schema change, no recycled IDs, no session migration assumed without implementation audit.
- No new dependencies, internet content, runtime network requirement, duplicated speech engine or unnecessary shared framework.
