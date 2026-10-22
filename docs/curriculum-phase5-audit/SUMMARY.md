# Phase 5 curriculum audit — owner summary

## Baseline and scope

This is a source audit of all 20 functioning Home entries at `0acc1e7a179b9e27ff7db8832b2bede057d8268a` on `main`, equal to `origin/main`. The working tree was clean before these audit documents were created. Four future catalogue cards (Dragon Treasure Hunt, Dinosaur Rescue, Memory Pairs and Crocodile Snap) remain filtered from Home and are not counted as implemented activities.

No Android build or emulator run was needed: the task changes only audit documentation. Current source, routing, records and local assets were inspected. The inventory gives exact bounded counts where content is fixed and formulas/configuration ranges where legacy content is generated. Question-choice permutations and different seeds are not counted as new concepts.

## What is strongest

- **Tell Me!** has 81 distinct bilingual picture scenes across nine categories, with open-ended prompts and optional grown-up support. It is conversation-led and intentionally has no score or speech grading.
- **Prepositions** has 52 reviewed scene examples across 13 spatial relations.
- **Days & Seasons** has three substantial native areas: Wilma’s seven-day learning, four seasons with cycle/clue/matching modes, and a 12-month curriculum with ordering, missing/between, two sequence lengths, month-season sorting and an interactive wheel.
- **Follow the Instructions** already has a clear one-step to ordered two-step progression over six illustrated animals, with replay, hints and saved sessions.
- The native platform already has concrete reusable state machines for sorting, sequencing, durable sessions, progress and offline speech. Colours, Animal Groups, Seasons matching and Wash Hands have implemented foundations, not just UI placeholders.

## Thinnest or most repetitive areas

- **Colour Sort:** four vector tokens, red/blue only, and no Explore/Find lesson before sorting.
- **Animal Groups:** four animals and two bins; good as a simple first sort but not a scientifically complete habitat model because the underlying rule allows one category per item.
- **Wash Hands:** one four-step, text-only sequence with no step illustrations or modeled consequence.
- **Vocabulary Booster:** six animal words across Explore/Find/Name; no everyday/school-object vocabulary yet.
- **Native Maths:** five useful introductory 1–5 visual-choice activities but no quantity construction/manipulation. Legacy Easy Maths reaches through 10 but is not a concrete substitute.
- **Letters:** six EN and six DE written initial-letter mappings; it teaches grapheme/word-start matching, not phonological awareness.
- **Several legacy routes** (Numbers, Easy Maths, Animal Actions, Learn Verbs, Letters, Mixed) use emoji and repeated formats. High generated numeric/action combinations do not automatically mean high concept or visual variety.

## Highest-value recommendations

1. Start **Phase 5A with Explore + Find Colour** using red, blue, yellow and green. Reuse existing bilingual color records and add no raster art in that first slice. Keep the current Home Colors card launching the existing Colour Sort until a separate route decision is reviewed.
2. Expand Animal Groups using an explicitly curated eight-animal starter set, then build a separate habitat exploration/matching model that permits multiple valid relationships. Do not include crocodiles, frogs, ducks, turtles or other multi-environment species in a forced one-answer sort.
3. Add a small shared School Objects pack consumed by Vocabulary and one-step Follow; then add a self-advocacy/help-seeking interaction and one well-authored practical routine.
4. Build a manipulation-first “make five” experience before increasing Maths number range. Treat phonological-awareness work as an independent, language-reviewed track with dedicated local audio; do not synthesize phonemes with ordinary TTS.

## Artwork that can be reused

- 28 canonical individual animal PNGs are in `app/src/main/assets/Animals/canonical/`; the first eight proposed animal-sort examples all have existing files. Existing labels/IDs are unevenly shared, so asset availability must not be mistaken for a complete shared animal data model.
- Water and land cue artwork exists at `app/src/main/res/drawable-nodpi/habitat_water.png` and `habitat_land.png`.
- Four canonical seasonal scenes, Wilma’s seven weekday segments plus head/tail, 52 Prepositions scene images and 81 Tell Me scene images are present.
- Classroom scene art contains books, pencils, scissors, bottles and other school objects in context. It does not provide a reviewed pack of isolated, consistently selectable object assets.
- No canonical isolated school-object pack, handwashing-step pictures, generalized habitat-scene library or phoneme recording set was found. Legacy emoji and contextual scene crops should not be treated as substitutes.

## Recommended Phase 5A starting task

**Bounded implementation proposal:** add a four-color Explore screen and a deterministic Find Colour session, preserve old Colour Sort IDs/checkpoints exactly, and add focused bilingual/content/session/layout tests. Do not implement a universal game engine, everyday-object pack, light/dark, patterns, mixing or new Home chooser in the first slice.

Before coding, the owner should decide whether Explore/Find should be entered from a small in-area chooser while Home still direct-launches Colour Sort, or whether the later Home route should change after at least two activities work. This audit intentionally makes no navigation change.

## Owner decisions for later work

- Which first priority is more urgent after Colours: expand the well-supported canonical-animal set, or start school-object/self-advocacy work sooner?
- Approve the proposed initial four-color set and the eight-item Water/Land animal subset as intentionally simplified introductions.
- Confirm the German wording/content review owner for school-object labels, practical routines and any phonological-awareness audio.
- Decide how shared animal aliases will map to existing Vocabulary `animal.*`, Follow `object.follow.*` and Animal Groups IDs without changing saved session identities.
- Commission isolated school objects and routine steps only when a concrete activity slice and image specification is approved.

## Status limits

This package reports source inspection, not physical-device, TalkBack, switch-access, offline-voice or child-usability acceptance. It does not claim roadmap proposals are implemented. No Kotlin, JavaScript learning logic, HTML, content assets, artwork, navigation or saved formats were modified. No tests/build were run because only Markdown files changed.
