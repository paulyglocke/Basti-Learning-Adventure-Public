# Proposed implementation sequence and verification plan

Phase numbering is resolved for future work: **Phase 5C — Animals & Habitats**, beginning with 5C.1 audit and 5C.2 Ocean Explore. Older `docs/curriculum-phase5-audit/PRIORITIES.md` references to 5B describe historical planning and are preserved. Each stage should be independently testable and stop if required art/facts are not ready. This audit itself implemented no stage.

**Status update (2026-10-29):** Phase 5C.2 implemented Ocean Explore and Phase 5C.3 implemented Woodland Explore. Phase 5C.4A completed the Underground design checkpoint; its approved-for-implementation asset package and current feature validation are recorded in the [design package](../animals-phase5c4-underground-design/SUMMARY.md) and [5C.4 review](../animals-phase5c4-underground-review/REVIEW.md). The original proposal's sequencing is adjusted below: Underground is Phase 5C.4; Freshwater and other additional habitats are Phase 5C.5; matching and final regression follow.

## Phase 5C.2 — Explore Habitats Foundation (delivered)

**Scope:** add the future Explore Habitats entry under existing Animals & Nature navigation, a habitat chooser with Ocean as the only active option, and one complete static Ocean Explore scene using `ocean_001_reef_actions.png` plus exact bilingual scene target notes. Activate only Dolphin initially; consider sea turtle only after exact image/species review. Add sourced bilingual fact, selected outline/state, Replay, labeled target alternative, Back to chooser and Home/Options/language behavior.

**No score/progress:** keep Explore unscored and do not make the existing Animal Groups journal a browser checkpoint. Use ephemeral/session UI state unless an implementation design demonstrates a genuine need for a separate bounded browsing restore checkpoint.

**Dependencies/gate:** visually approve the art and target region, confirm all local target labels/fact source/German, use default offline audio and 56dp controls. If artwork hit regions are too small/ambiguous, use named controls or stop for better art instead of guessing coordinates.

**Focused tests:** content and asset-path completeness; EN/DE text; source-required fact coverage; scene target uniqueness; tap/hotspot ↔ selected target agreement; tap outside target no-op; neutral selection marker; Sound Off and language change; Replay/cancellation; keyboard/accessibility semantics; chooser/scene/back/Home/Options routes; portrait/landscape and 1.5×/2× layout. Regression: Animal Groups route/state/content/journal and existing Vocabulary/Tell Me/Follow route tests. Do not run full instrumentation solely for docs; implementation changes require relevant suites.

## Phase 5C.3 — Woodland Explore (delivered)

**Scope delivered:** extend the chooser with Woodland and implement a child-led,
unscored scene using the approved `woodland_008_homes_choices.png` composite.
Rabbit and Fox are represented with source-relative targets, accessible labeled
alternatives, selected outlines, bilingual reviewed facts and replay.

**Review:** see the [Phase 5C.3 visual and implementation review](../animals-phase5c3-woodland-review/REVIEW.md).

## Phase 5C.4 — Underground cutaway scene

**Scope:** build the shared cutaway with surface, soil, roots, broad tunnels and visible entrance. The current scene uses Mole, Wild Rabbit and Earthworm layers. Keep selection non-scored, bilingual, accessible and visually explainable without reading.

**Dependencies/gate:** visual review the generated layered artwork, confirm rights/provenance, and complete native-speaker German review before release. Verify high-contrast boundary, portrait/landscape fit and independent hotspots at large target sizes. Earthworm is now represented by a dedicated generated asset; do not include Ant, Badger or Fox until exact artwork and content are ready.

**Focused tests:** rendered layer/asset mappings and visual screenshot review; each semantic target and selected state; above/below/inside/next-to/between relations; reachability, font scaling, reduced-motion/static behavior; offline replay/Sound Off; all Animals & Nature route regressions; no change to Prepositions contracts.

## Phase 5C.5 — Freshwater and additional habitats

**Scope:** add Freshwater only after dedicated scene art plus species-identified visuals are available. Consider other habitats only with reviewed artwork and facts. Extend the chooser as each habitat becomes functional. Avoid duplicating Ocean, Woodland or Underground; reuse scene composition and canonical art only after style/context review.

**Dependencies/gate:** authored target geometry, source-backed facts and native-speaker-reviewed EN/DE for each new environment; responsive artwork/focus controls; provenance/licensing; no generic fish/frog/duck claims unsupported by pictured species. An existing Woodland composite can support scene-specific non-scored Explore; Freshwater cannot be approved by substituting an Ocean scene or generic fish PNG.

**Focused tests:** scene mapping and target/fact pairs per habitat; EN/DE chooser and per-scene completeness; no blank/inactive habitat options; no cross-habitat stale selection/audio; all prior phases’ routes/layout/accessibility; image decoding and visual-semantic review.

## Phase 5C.6 — Habitat matching

**Scope:** introduce a distinct scored Match activity only after several scene/fact records are stable. Select a contextual one-answer prompt or a true multi-valid-answer task. Do not use the existing one-category Water/Land sorter for general relations.

**Dependencies/gate:** relationship records include evidence, species scope, age suitability, relationship type and explicit accepted-set policy; each task’s art shows the claimed context; new Activity/Skill/Context IDs and revision/session contract reviewed; progress deduplication and support semantics specified.

**Focused tests:** unique IDs, all and only valid answers, multiple valid associations, no forced single habitat, no Air-as-habitat, deterministic plans, wrong-answer retry/support, exact session restore, language switch, attempt/completion event deduplication, corrupted journal behavior, old Animal Groups revision-1 exact restoration/progress fixtures.

## Phase 5C.7 — Regression and visual acceptance

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
