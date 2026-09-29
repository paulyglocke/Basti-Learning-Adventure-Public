# Tell Me physical-acceptance review — 2026-09-29

## Final resumption: owner Park 9 replacement and completed validation

The previously running full emulator suite completed **122/122**, zero failures/errors/skips,
BUILD SUCCESSFUL in 22m 14s. It was not rerun. This result predates the owner's Park 9
replacement; focused replacement decode validation is recorded separately below.

The owner supplied the replacement `park_009_turn_taking.png`. It is preserved unchanged:
PNG RGB, 1448×1086, SHA-256 `e4ec416f5c06ac6e25a4342b3b0430400516061c3f8a247f990ee6eda8915a78`.
Desktop visual review shows two seated children facing each other, coherent seesaw
beam/pivot/handles, and one standing pink-clothed waiting child. Existing “Who is waiting?” /
“Wer wartet?” is retained. Park 9 is now PASS at desktop review, pending S24 acceptance.
The original BOTH finding and hash remain in AUDIT.json prior_review; original contact
sheets remain BEFORE evidence. Current classifications: 58 PASS, 23 PROMPT_FIX corrected,
0 ARTWORK_FIX/BOTH/REVIEW_REQUIRED. This does not replace physical acceptance.

Only this owner-supplied Scene Description PNG changed; the other 80 remain byte-identical.
Combined with the 21 Prepositions crops, 22 of 178 packaged rasters differ from HEAD;
156 remain unchanged. No new image was generated or edited by the agent in this resumption.
The ASCII tellme-completion-de.png extraction is invalid and is NOT visual evidence.
Completion interaction is covered by the passing focused and full instrumentation tests.

### Final replacement validation

After the owner replacement: Python **36/36**, generator freshness passed, raw asset
hygiene **175 scanned / 174 CLEAN / 1 existing ambiguous mouse / 0 errors or strong
signatures**. Targeted Android `TellMeArtworkTest#allApprovedScenesIncludingCroppedPanelsDecode`
**1/1 passed**, decoding all 81 current images; APK packaging succeeded (50s run).
The owner's Park 9 SHA-256 was rechecked unchanged after validation; other 80 Scene
Description PNGs match HEAD. No full suite rerun was necessary for this image-only update.
Earlier completed code validation: focused JVM **69/69**, full JVM **323/323**, focused
instrumentation **22/22**, full instrumentation **122/122**, all zero failures/errors/skips;
assembleDebug passed; lint **0 errors, 3 existing warnings, 2 informational findings**.
The full suite predates the owner image replacement. Final diff/whitespace check passed.
No valid completion screenshot was recovered; no visual claim relies on the ASCII file.
No commit, push, checkout, reset, clean, pull or physical-device installation was performed.

Replacement-specific commands (same JAVA_HOME / ANDROID_HOME / emulator-5554 as above):

```sh
/private/tmp/basti-ci-deps-validation/bin/python -m unittest discover -s scripts -p 'test_*.py'
/private/tmp/basti-ci-deps-validation/bin/python scripts/generate_scene_descriptions.py --check
/private/tmp/basti-ci-deps-validation/bin/python scripts/audit_asset_hygiene.py --output docs/asset-hygiene/ASSET_AUDIT_AFTER.csv
./gradlew connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.bellfamily.bastischool.ui.tellme.TellMeArtworkTest#allApprovedScenesIncludingCroppedPanelsDecode' --console=plain
git diff --check
```

Earlier checkpoint (before owner replacement):


All 81 production images were viewed alongside the exact EN/DE text selected by `TellMeFlow.support`; the seven physical findings were also inspected at full image resolution. Contact sheets show **before** copy. `AUDIT.json` records every scene, image hash, before/after live copy, decision and reason. `CONTENT_CHANGES.json` records **every normalized runtime field change**, including both languages and non-displayed authored support. No Scene Description PNG changed.

## Outcome

Scene-specific baseline findings: **{'PASS': 57, 'PROMPT_FIX': 23, 'BOTH': 1}**; ARTWORK_FIX alone 0, REVIEW_REQUIRED 0. The 23 PROMPT_FIX records were corrected; Park 9 remains BOTH/open. Shared model-heading wording is counted once separately, not as 27 duplicate scene defects.

PASS means the reviewed image supports the live prompt/model; it is not a new answer key, independent native-speaker approval, a pixel-perfect guarantee or S24 acceptance. Tell Me remains open-ended. Questions such as “What can you see?” intentionally admit multiple valid descriptions. No right/wrong evaluation was added.

## Open owner decision: Park 9

`scene.park.turn_taking.09`: the pink-standing child and the yellow child holding toys can both plausibly be waiting. The right seesaw handle/seat relationship and beam/pivot perspective are physically incoherent. Leave the current PNG and source untouched: a sentence substitution would hide, not solve, the artwork defect. Before physical acceptance, replace/review this one scene with coherent beam, central pivot, attached handles and naturally seated children; exactly one other child must be visibly waiting, with no competing waiting referent. Review the resulting EN/DE prompt against the replacement. No speculative AI replacement was generated in this pass. This is an explicit remaining production limitation.

## Learning-intent corrections

Jungle 4 and Woodland 5 were authored as animal-home scenes but the committed pictures do not show the described homes/actions. Their purposes are explicitly narrowed to visible animal locations/actions or groups, preserving open-ended naming and sentence expansion. IDs, titles, category order and assets remain stable; this is recorded content revision **1.4**, schema 1, not a silent claim that missing homes exist. Several older `visualAudit` arrays describe generation intent; affected source records now explicitly warn that those historical notes are not acceptance evidence. Review those against this ledger, not as proof of image contents.

## Every non-PASS scene

- **scene.ocean.hiding_places.03 — PROMPT_FIX**: Crab is inside a rock opening; cave is clearer than under a rock. Correct model, starter and associated crab descriptions in both languages. Paired authored corrections; see CONTENT_CHANGES.json.
- **scene.ocean.story_actions.04 — PROMPT_FIX**: Current image has crab digging, octopus in cave and blue fish; older metadata instead described absent chest, shells and starfish. Align those runtime targets/support; retain open first prompt. Paired authored corrections; see CONTENT_CHANGES.json.
- **scene.ocean.reef_routines.06 — PROMPT_FIX**: No animal hides or digs; turtle holds seaweed in mouth, octopus is beside open chest, two fish above. Replace hiding question and false cave/digging/count claims. Paired authored corrections; see CONTENT_CHANGES.json.
- **scene.ocean.helping_story.07 — PROMPT_FIX**: Both turtle and crab carry things. Name the turtle in the first question; remove unsupported waiting/helping assertions about the fish by the cave. Paired authored corrections; see CONTENT_CHANGES.json.
- **scene.ocean.find_and_follow.08 — PROMPT_FIX**: Crab is inside a cave opening, not predominantly under a freestanding rock. Correct all linked under-rock instructions in EN/DE. Paired authored corrections; see CONTENT_CHANGES.json.
- **scene.jungle.animal_homes.04 — PROMPT_FIX**: Sloth has open eyes and hangs below branch; no nest/toucan/tree-hole scene as older metadata claimed. Replace sleep question and erroneous models/targets with visible animal positions/actions. Original homes premise not fulfilled; explicit purpose correction recorded. Paired authored corrections; see CONTENT_CHANGES.json.
- **scene.jungle.busy_actions.05 — PROMPT_FIX**: Elephant has raised trunk without spray; parrot perches, frog sits on rock, three monkeys hold vines, no sloth/banana. Correct action question and mismatched models/targets. Paired authored corrections; see CONTENT_CHANGES.json.
- **scene.jungle.big_small_groups.06 — PROMPT_FIX**: Tiger, frog, sloth, parrot, toucan and one monkey are visible; absent elephant and three-monkey count in unused authored models/targets corrected. Paired authored corrections; see CONTENT_CHANGES.json.
- **scene.jungle.story_actions.07 — PROMPT_FIX**: Monkey holds banana; parrot carries leaf and sloth grips branch. Specify monkey rather than an ambiguous who-holds question. Paired authored corrections; see CONTENT_CHANGES.json.
- **scene.woodland.autumn_prep.04 — PROMPT_FIX**: Squirrel holds acorns while hedgehog has a leaf on its back. Ask what the squirrel carries rather than assume one carrier. Paired authored corrections; see CONTENT_CHANGES.json.
- **scene.woodland.animal_homes.05 — PROMPT_FIX**: Owl is on branch, three squirrels on log, rabbit beside deer and fox seated in grass. Old hollow/den/storing scene not shown; correct models/questions and explicitly reframe purpose to visible locations/groups. Paired authored corrections; see CONTENT_CHANGES.json.
- **scene.woodland.compare_groups.06 — PROMPT_FIX**: Only one squirrel is visible. Correct three-squirrel authored model and count term; first size question retained. Paired authored corrections; see CONTENT_CHANGES.json.
- **scene.park.tasks.02 — PROMPT_FIX**: Several girls/boys occur in scene. Specify pink-clothed girl and blue-clothed boy at the red ball; sharing model remains an example, not a required utterance. Paired authored corrections; see CONTENT_CHANGES.json.
- **scene.park.turn_taking.09 — BOTH**: Two standing children can plausibly be waiting. Seesaw right handle/seat attachment and beam/pivot perspective are incoherent. Wording cannot cure geometry; leave art AND content unchanged pending reviewed replacement with one clear waiting child. Owner artwork replacement/review required; do not claim acceptance.
- **scene.mountains.spatial_clues.09 — PROMPT_FIX**: Goat horns and eagle wings occupy similar image heights; highest is perspective-dependent. Ask which animal flies above the stream instead. Paired authored corrections; see CONTENT_CHANGES.json.
- **scene.farm.animal_actions.04 — PROMPT_FIX**: Only one chicken pecks grain. Correct plural chicken wording in models/support; open first animal-naming question unchanged. Paired authored corrections; see CONTENT_CHANGES.json.
- **scene.farm.farmyard_friends.08 — PROMPT_FIX**: Cow and horse size cannot be ranked confidently across depth/partial body views. Ask which animal stands in stable; horse is clear. Paired authored corrections; see CONTENT_CHANGES.json.
- **scene.farm.animal_care.09 — PROMPT_FIX**: Need for water cannot be read from a still and applies to many animals. Ask which animal is beside water trough; cow is visibly adjacent. Paired authored corrections; see CONTENT_CHANGES.json.
- **scene.classroom.tasks.01 — PROMPT_FIX**: One child reads open book while another handles book at shelf. Specify open book in question; existing model is optional language, not an answer key. Paired authored corrections; see CONTENT_CHANGES.json.
- **scene.classroom.positions.03 — PROMPT_FIX**: Blue backpack under left desk and green bag foreground both fit generic bag referent. Qualify blue backpack and red book in green bag in linked EN/DE text. Paired authored corrections; see CONTENT_CHANGES.json.
- **scene.classroom.objects_places.04 — PROMPT_FIX**: Several writing implements on table/in cup. Identify red cup of pencils; correct model from lying pencils to pencils in cup on table. Paired authored corrections; see CONTENT_CHANGES.json.
- **scene.classroom.end_of_day.09 — PROMPT_FIX**: One child packs green backpack; other children hold/show pictures, none visibly puts a book away. Correct linked unsupported book/ waiting claims, keep first packing prompt. Paired authored corrections; see CONTENT_CHANGES.json.
- **scene.zoo.feeding_time.05 — PROMPT_FIX**: Elephant and giraffe both touch leaves. Ask what giraffe reaches for rather than assume one leaf-eater. Paired authored corrections; see CONTENT_CHANGES.json.
- **scene.zoo.habitats_story.07 — PROMPT_FIX**: Penguin AND elephant are beside water. Use plural animals rather than singular presumed referent. Paired authored corrections; see CONTENT_CHANGES.json.

## Exact EN/DE changes

The following ledger includes all runtime changes, even those not currently selected for display. List replacements are recorded as complete before/after lists so removed unsupported facts remain auditable. Historical English-preservation fingerprint tests reverse only these asserted changes, then apply the earlier documented corrections.

### scene.ocean.hiding_places.03

- `examples.en.0`: "The crab is under the rock." → "The crab is in the cave."
- `examples.de.0`: "Die Krabbe ist unter dem Stein." → "Die Krabbe ist in der Höhle."
- `groups.1.1.en.1`: "Which animal is under the rock?" → "Which animal is in the cave?"
- `groups.1.1.de.1`: "Welches Tier ist unter dem Stein?" → "Welches Tier ist in der Höhle?"
- `groups.2.1.en.0`: "The ___ is under..." → "The ___ is in..."
- `groups.2.1.de.0`: "Die/Der ___ ist unter..." → "Die/Der ___ ist in..."
- `groups.3.1.en.0`: "The crab is hiding under the rock." → "The crab is in the cave."
- `groups.3.1.de.0`: "Die Krabbe versteckt sich unter dem Stein." → "Die Krabbe ist in der Höhle."
- `expansions.0.0.en`: "Crab under." → "Crab in cave."
- `expansions.0.0.de`: "Krabbe unter." → "Krabbe in Höhle."
- `expansions.0.1.en`: "Yes, the crab is under the rock." → "Yes, the crab is in the cave."
- `expansions.0.1.de`: "Ja, die Krabbe ist unter dem Stein." → "Ja, die Krabbe ist in der Höhle."
### scene.ocean.story_actions.04

- `targets.0.1.2.en`: "hermit crab" → "crab"
- `targets.0.1.2.de`: "Einsiedlerkrebs" → "Krabbe"
- `targets.0.1.3.en`: "shell" → "sand"
- `targets.0.1.3.de`: "Muschel" → "Sand"
- `targets.0.1.4.en`: "starfish" → "cave"
- `targets.0.1.4.de`: "Seestern" → "Höhle"
- `targets.0.1.7.en`: "treasure chest" → "coral"
- `targets.0.1.7.de`: "Schatztruhe" → "Koralle"
- `targets.1.1`: [{"en": "eating", "de": "fressen"}, {"en": "swimming", "de": "schwimmen"}, {"en": "opening", "de": "öffnen"}, {"en": "looking", "de": "schauen"}, {"en": "choosing", "de": "auswählen"}, {"en": "watching", "de": "beobachten"}] → [{"en": "swimming", "de": "schwimmen"}, {"en": "digging", "de": "graben"}]
- `targets.2.1`: [{"en": "big", "de": "groß"}, {"en": "small", "de": "klein"}, {"en": "orange", "de": "orange"}, {"en": "purple", "de": "lila"}, {"en": "green", "de": "grün"}, {"en": "open", "de": "offen"}] → [{"en": "big", "de": "groß"}, {"en": "small", "de": "klein"}, {"en": "orange", "de": "orange"}, {"en": "blue", "de": "blau"}, {"en": "green", "de": "grün"}]
- `targets.3.1.0.en`: "The turtle is eating seaweed." → "The turtle is swimming."
- `targets.3.1.0.de`: "Die Schildkröte frisst Seegras." → "Die Schildkröte schwimmt."
- `targets.3.1.1.en`: "The octopus is opening the treasure chest." → "The octopus is in the cave."
- `targets.3.1.1.de`: "Der Oktopus öffnet die Schatztruhe." → "Der Oktopus ist in der Höhle."
- `targets.3.1.2.en`: "The hermit crab is looking at the shells." → "The crab is digging in the sand."
- `targets.3.1.2.de`: "Der Einsiedlerkrebs schaut sich die Muscheln an." → "Die Krabbe gräbt im Sand."
- `targets.3.1.3.en`: "The starfish is on the rock." → "Three blue fish are swimming together."
- `targets.3.1.3.de`: "Der Seestern ist auf dem Stein." → "Drei blaue Fische schwimmen zusammen."
- `groups.0.1.en.1`: "Who is doing something?" → "What is the crab doing?"
- `groups.0.1.en.2`: "Can you tell me where the starfish is?" → "Where is the octopus?"
- `groups.0.1.en.3`: "Which animal is near the treasure chest?" → "Which animal is in the cave?"
- `groups.0.1.de.1`: "Wer macht etwas?" → "Was macht die Krabbe?"
- `groups.0.1.de.2`: "Kannst du mir sagen, wo der Seestern ist?" → "Wo ist der Oktopus?"
- `groups.0.1.de.3`: "Welches Tier ist in der Nähe der Schatztruhe?" → "Welches Tier ist in der Höhle?"
- `groups.1.1.en.2`: "Which shells are big and small?" → "How many blue fish can you see?"
- `groups.1.1.en.3`: "What might the hermit crab choose next?" → "What might the crab find in the sand?"
- `groups.1.1.de.2`: "Welche Muscheln sind groß und welche klein?" → "Wie viele blaue Fische siehst du?"
- `groups.1.1.de.3`: "Was könnte sich der Einsiedlerkrebs als Nächstes aussuchen?" → "Was könnte die Krabbe im Sand finden?"
### scene.ocean.reef_routines.06

- `targets.0.1`: [{"en": "turtle", "de": "Schildkröte"}, {"en": "crab", "de": "Krabbe"}, {"en": "octopus", "de": "Oktopus"}, {"en": "cave", "de": "Höhle"}, {"en": "seahorse", "de": "Seepferdchen"}, {"en": "coral", "de": "Koralle"}, {"en": "fish", "de": "Fisch"}, {"en": "seaweed", "de": "Seegras"}] → [{"en": "turtle", "de": "Schildkröte"}, {"en": "crab", "de": "Krabbe"}, {"en": "octopus", "de": "Oktopus"}, {"en": "shell", "de": "Muschel"}, {"en": "chest", "de": "Truhe"}, {"en": "seahorse", "de": "Seepferdchen"}, {"en": "coral", "de": "Koralle"}, {"en": "fish", "de": "Fisch"}, {"en": "seaweed", "de": "Seegras"}]
- `targets.1.1`: [{"en": "eating", "de": "fressen"}, {"en": "digging", "de": "graben"}, {"en": "hiding", "de": "sich verstecken"}, {"en": "swimming", "de": "schwimmen"}] → [{"en": "eating", "de": "fressen"}, {"en": "swimming", "de": "schwimmen"}]
- `targets.3.1.1.en`: "The crab is digging in the sand." → "The crab is beside the shells."
- `targets.3.1.1.de`: "Die Krabbe gräbt im Sand." → "Die Krabbe ist neben den Muscheln."
- `targets.3.1.2.en`: "The octopus is hiding in the cave." → "The octopus is beside the open chest."
- `targets.3.1.2.de`: "Der Oktopus versteckt sich in der Höhle." → "Der Oktopus ist neben der offenen Truhe."
- `targets.3.1.4.en`: "The three fish are swimming together." → "Two fish are swimming above the octopus."
- `targets.3.1.4.de`: "Die drei Fische schwimmen zusammen." → "Zwei Fische schwimmen über dem Oktopus."
- `focus.en`: "Support simple description plus light functional questions like who is hiding or who is working." → "Describe what the animals are doing and where they are."
- `focus.de`: "Einfache Beschreibungen unterstützen und leichte Fragen stellen, etwa wer sich versteckt oder wer arbeitet." → "Beschreiben, was die Tiere machen und wo sie sind."
- `groups.0.1.en.0`: "Who is hiding?" → "What is the turtle eating?"
- `groups.0.1.en.1`: "Who is digging?" → "Where is the crab?"
- `groups.0.1.en.3`: "How many blue fish are there?" → "How many fish are above the octopus?"
- `groups.0.1.en.4`: "What is the turtle eating?" → "What is beside the crab?"
- `groups.0.1.de.0`: "Wer versteckt sich?" → "Was frisst die Schildkröte?"
- `groups.0.1.de.1`: "Wer gräbt?" → "Wo ist die Krabbe?"
- `groups.0.1.de.3`: "Wie viele blaue Fische sind da?" → "Wie viele Fische sind über dem Oktopus?"
- `groups.0.1.de.4`: "Was frisst die Schildkröte?" → "Was ist neben der Krabbe?"
### scene.ocean.helping_story.07

- `targets.3.1.1.en`: "The clownfish is waiting by the cave." → "The clownfish is by the cave."
- `targets.3.1.1.de`: "Der Clownfisch wartet bei der Höhle." → "Der Clownfisch ist bei der Höhle."
- `focus.en`: "Talk about what is happening first, what might happen next, and make longer sentences about who is helping at the reef." → "Describe what the animals are carrying and where they are. Talk about what might happen next."
- `focus.de`: "Darüber sprechen, was zuerst passiert und was als Nächstes passieren könnte. Längere Sätze darüber bilden, wer am Riff hilft." → "Beschreiben, was die Tiere tragen und wo sie sind. Darüber sprechen, was als Nächstes passieren könnte."
- `groups.0.1.en.0`: "Who is bringing something?" → "What is the turtle carrying?"
- `groups.0.1.en.1`: "Who is waiting near the cave?" → "Which fish is by the cave?"
- `groups.0.1.de.0`: "Wer bringt etwas?" → "Was trägt die Schildkröte?"
- `groups.0.1.de.1`: "Wer wartet in der Nähe der Höhle?" → "Welcher Fisch ist bei der Höhle?"
- `groups.1.1.en.2`: "Who is helping and who is waiting?" → "What are the animals near the cave doing?"
- `groups.1.1.de.2`: "Wer hilft und wer wartet?" → "Was machen die Tiere bei der Höhle?"
### scene.ocean.find_and_follow.08

- `targets.3.1.0.en`: "Find the crab under the rock." → "Find the crab in the cave."
- `targets.3.1.0.de`: "Finde die Krabbe unter dem Stein." → "Finde die Krabbe in der Höhle."
- `groups.0.1.en.0`: "Can you find the crab under the rock?" → "Can you find the crab in the cave?"
- `groups.0.1.de.0`: "Findest du die Krabbe unter dem Stein?" → "Findest du die Krabbe in der Höhle?"
- `groups.1.1.en.1`: "Find the animal behind the coral and then the animal under the rock." → "Find the animal behind the coral and then the animal in the cave."
- `groups.1.1.de.1`: "Finde das Tier hinter der Koralle und dann das Tier unter dem Stein." → "Finde das Tier hinter der Koralle und dann das Tier in der Höhle."
### scene.jungle.animal_homes.04

- `purpose.en`: "Animal homes, shelter and gentle inference questions." → "Describe where rainforest animals are and what they are doing."
- `purpose.de`: "Tierwohnungen, Schutz und behutsame Fragen zum Weiterdenken." → "Beschreiben, wo die Tiere im Regenwald sind und was sie machen."
- `targets.0.1.3.en`: "tree hole" → "banana"
- `targets.0.1.3.de`: "Baumhöhle" → "Banane"
- `targets.0.1.6.en`: "toucan" → "elephant"
- `targets.0.1.6.de`: "Tukan" → "Elefant"
- `targets.0.1.7.en`: "nest" → "river"
- `targets.0.1.7.de`: "Nest" → "Fluss"
- `targets.1.1.0.en`: "sleeping" → "hanging"
- `targets.1.1.0.de`: "schlafen" → "hängen"
- `targets.1.1.1.en`: "looking" → "holding"
- `targets.1.1.1.de`: "schauen" → "halten"
- `targets.1.1.2.en`: "carrying" → "flying"
- `targets.1.1.2.de`: "tragen" → "fliegen"
- `targets.1.1.3.en`: "drinking" → "jumping"
- `targets.1.1.3.de`: "trinken" → "springen"
- `targets.1.1.4.en`: "sitting" → "walking"
- `targets.1.1.4.de`: "sitzen" → "gehen"
- `targets.3.1.0.en`: "The sloth is sleeping on the branch." → "The sloth is hanging from the branch."
- `targets.3.1.0.de`: "Das Faultier schläft auf dem Ast." → "Das Faultier hängt am Ast."
- `targets.3.1.1.en`: "The monkey is looking into the tree hole." → "The monkey is holding a banana."
- `targets.3.1.1.de`: "Der Affe schaut in die Baumhöhle." → "Der Affe hält eine Banane."
- `targets.3.1.2.en`: "The parrot is carrying a leaf." → "The parrot is flying."
- `targets.3.1.2.de`: "Der Papagei trägt ein Blatt." → "Der Papagei fliegt."
- `targets.3.1.3.en`: "The toucan is next to the nest." → "The frog is jumping over the leaves."
- `targets.3.1.3.de`: "Der Tukan ist neben dem Nest." → "Der Frosch springt über die Blätter."
- `targets.3.1.4.en`: "The tiger is drinking water." → "The tiger is walking through the grass."
- `targets.3.1.4.de`: "Der Tiger trinkt Wasser." → "Der Tiger geht durch das Gras."
- `focus.en`: "Talk about where animals rest, hide, or make homes." → "Describe where the animals are in the rainforest."
- `focus.de`: "Darüber sprechen, wo Tiere sich ausruhen, sich verstecken oder ihr Zuhause einrichten." → "Beschreiben, wo die Tiere im Regenwald sind."
- `groups.0.1.en.0`: "Which animal is sleeping?" → "Where is the sloth hanging?"
- `groups.0.1.en.1`: "Where is the monkey looking?" → "What is the monkey holding?"
- `groups.0.1.en.2`: "Who has a nest nearby?" → "Which animal is flying?"
- `groups.0.1.en.3`: "Who is under the leaf?" → "What is the frog doing?"
- `groups.0.1.de.0`: "Welches Tier schläft?" → "Wo hängt das Faultier?"
- `groups.0.1.de.1`: "Wohin schaut der Affe?" → "Was hält der Affe?"
- `groups.0.1.de.2`: "Bei wem ist ein Nest in der Nähe?" → "Welches Tier fliegt?"
- `groups.0.1.de.3`: "Wer ist unter dem Blatt?" → "Was macht der Frosch?"
- `groups.1.1.en.0`: "Which place looks safe and dry?" → "Where is the elephant?"
- `groups.1.1.en.1`: "What might the parrot do with the leaf next?" → "What might the monkey do with the banana?"
- `groups.1.1.de.0`: "Welcher Platz sieht sicher und trocken aus?" → "Wo ist der Elefant?"
- `groups.1.1.de.1`: "Was könnte der Papagei als Nächstes mit dem Blatt machen?" → "Was könnte der Affe mit der Banane machen?"
### scene.jungle.busy_actions.05

- `targets.0.1.1.en`: "banana" → "vine"
- `targets.0.1.1.de`: "Banane" → "Liane"
- `targets.0.1.5.en`: "sloth" → "toucan"
- `targets.0.1.5.de`: "Faultier" → "Tukan"
- `targets.1.1`: [{"en": "holding", "de": "halten"}, {"en": "spraying", "de": "spritzen"}, {"en": "jumping", "de": "springen"}, {"en": "flying", "de": "fliegen"}, {"en": "hanging", "de": "hängen"}, {"en": "walking", "de": "gehen"}] → [{"en": "holding", "de": "halten"}, {"en": "lifting", "de": "heben"}, {"en": "sitting", "de": "sitzen"}, {"en": "hanging", "de": "hängen"}, {"en": "standing", "de": "stehen"}]
- `targets.3.1.0.en`: "The monkey is holding a banana." → "The monkeys are holding the vines."
- `targets.3.1.0.de`: "Der Affe hält eine Banane." → "Die Affen halten sich an den Lianen fest."
- `targets.3.1.1.en`: "The elephant is spraying water." → "The elephant is lifting its trunk."
- `targets.3.1.1.de`: "Der Elefant spritzt Wasser." → "Der Elefant hebt den Rüssel."
- `targets.3.1.2.en`: "The frog is jumping." → "The frog is on the rock."
- `targets.3.1.2.de`: "Der Frosch springt." → "Der Frosch ist auf dem Stein."
- `targets.3.1.3.en`: "The parrot is flying." → "The parrot is on a branch."
- `targets.3.1.3.de`: "Der Papagei fliegt." → "Der Papagei ist auf einem Ast."
- `targets.3.1.4.en`: "The sloth is hanging from the branch." → "The toucan is on a branch."
- `targets.3.1.4.de`: "Das Faultier hängt am Ast." → "Der Tukan ist auf einem Ast."
- `targets.3.1.5.en`: "The tiger is walking." → "The tiger is standing on the path."
- `targets.3.1.5.de`: "Der Tiger geht." → "Der Tiger steht auf dem Weg."
- `groups.0.1.en.0`: "Who is spraying water?" → "What is the elephant doing with its trunk?"
- `groups.0.1.en.1`: "Which animal is flying?" → "Where is the parrot?"
- `groups.0.1.en.2`: "What is the monkey holding?" → "What are the monkeys holding?"
- `groups.0.1.en.3`: "Can you find the jumping animal?" → "Where is the frog?"
- `groups.0.1.en.4`: "Which animal is walking through the grass?" → "Which animal is standing on the path?"
- `groups.0.1.de.0`: "Wer spritzt Wasser?" → "Was macht der Elefant mit seinem Rüssel?"
- `groups.0.1.de.1`: "Welches Tier fliegt?" → "Wo ist der Papagei?"
- `groups.0.1.de.2`: "Was hält der Affe?" → "Woran halten sich die Affen fest?"
- `groups.0.1.de.3`: "Findest du das Tier, das springt?" → "Wo ist der Frosch?"
- `groups.0.1.de.4`: "Welches Tier geht durch das Gras?" → "Welches Tier steht auf dem Weg?"
### scene.jungle.big_small_groups.06

- `targets.0.1.0.en`: "elephant" → "sloth"
- `targets.0.1.0.de`: "Elefant" → "Faultier"
- `targets.2.1.2.en`: "grey" → "brown"
- `targets.2.1.2.de`: "grau" → "braun"
- `targets.2.1.5.en`: "three" → "one"
- `targets.2.1.5.de`: "drei" → "eins"
- `targets.3.1.0.en`: "The elephant is big." → "The tiger is big."
- `targets.3.1.0.de`: "Der Elefant ist groß." → "Der Tiger ist groß."
- `targets.3.1.4.en`: "There are three monkeys." → "One monkey is by the tree hollow."
- `targets.3.1.4.de`: "Da sind drei Affen." → "Ein Affe ist bei der Baumhöhle."
- `groups.1.1.en.0`: "Compare the elephant and the frog." → "Compare the tiger and the frog."
- `groups.1.1.en.1`: "Tell me a sentence with three." → "Tell me a sentence about the monkey."
- `groups.1.1.de.0`: "Vergleiche den Elefanten und den Frosch." → "Vergleiche den Tiger und den Frosch."
- `groups.1.1.de.1`: "Sag mir einen Satz mit „drei“." → "Sag mir einen Satz über den Affen."
### scene.jungle.story_actions.07

- `groups.0.1.en.0`: "Who is holding something?" → "What is the monkey holding?"
- `groups.0.1.de.0`: "Wer hält etwas?" → "Was hält der Affe?"
### scene.woodland.autumn_prep.04

- `groups.0.1.en.0`: "Who is carrying something?" → "What is the squirrel carrying?"
- `groups.0.1.de.0`: "Wer trägt etwas?" → "Was trägt das Eichhörnchen?"
### scene.woodland.animal_homes.05

- `purpose.en`: "Talk about where woodland animals live, rest or store food." → "Describe where woodland animals are and compare visible groups."
- `purpose.de`: "Darüber sprechen, wo Waldtiere wohnen, sich ausruhen oder Futter aufbewahren." → "Beschreiben, wo die Waldtiere sind, und sichtbare Gruppen vergleichen."
- `targets.0.1`: [{"en": "owl", "de": "Eule"}, {"en": "tree hollow", "de": "Baumhöhle"}, {"en": "squirrel", "de": "Eichhörnchen"}, {"en": "stump", "de": "Baumstumpf"}, {"en": "acorns", "de": "Eicheln"}, {"en": "rabbit", "de": "Kaninchen"}, {"en": "burrow", "de": "Bau"}, {"en": "hedgehog", "de": "Igel"}, {"en": "leaf pile", "de": "Laubhaufen"}, {"en": "fox", "de": "Fuchs"}, {"en": "den", "de": "Bau"}, {"en": "deer", "de": "Reh"}, {"en": "stream", "de": "Bach"}] → [{"en": "owl", "de": "Eule"}, {"en": "branch", "de": "Ast"}, {"en": "squirrel", "de": "Eichhörnchen"}, {"en": "log", "de": "Baumstamm"}, {"en": "rabbit", "de": "Kaninchen"}, {"en": "hedgehog", "de": "Igel"}, {"en": "grass", "de": "Gras"}, {"en": "fox", "de": "Fuchs"}, {"en": "deer", "de": "Reh"}]
- `targets.1.1`: [{"en": "sitting", "de": "sitzen"}, {"en": "storing", "de": "aufbewahren"}, {"en": "entering", "de": "hineingehen"}, {"en": "resting", "de": "sich ausruhen"}, {"en": "looking", "de": "schauen"}] → [{"en": "sitting", "de": "sitzen"}, {"en": "standing", "de": "stehen"}, {"en": "looking", "de": "schauen"}]
- `targets.2.1`: [{"en": "inside", "de": "drinnen"}, {"en": "next to", "de": "neben"}, {"en": "under", "de": "unter"}, {"en": "near", "de": "in der Nähe"}] → [{"en": "on", "de": "auf"}, {"en": "next to", "de": "neben"}, {"en": "near", "de": "nahe bei"}]
- `targets.3.1.0.en`: "The owl is in the tree hollow." → "The owl is on the branch."
- `targets.3.1.0.de`: "Die Eule ist in der Baumhöhle." → "Die Eule ist auf dem Ast."
- `targets.3.1.1.en`: "The squirrel is putting an acorn into the stump." → "Three squirrels are on the log."
- `targets.3.1.1.de`: "Das Eichhörnchen legt eine Eichel in den Baumstumpf." → "Drei Eichhörnchen sind auf dem Baumstamm."
- `targets.3.1.2.en`: "The rabbit is going into the burrow." → "The rabbit is beside the deer."
- `targets.3.1.2.de`: "Das Kaninchen geht in den Bau." → "Das Kaninchen ist neben dem Reh."
- `targets.3.1.3.en`: "The fox is resting in the den." → "The fox is sitting on the grass."
- `targets.3.1.3.de`: "Der Fuchs ruht sich im Bau aus." → "Der Fuchs sitzt im Gras."
- `targets.3.1.4.en`: "The hedgehog is near the leaves." → "The hedgehog is near the fox."
- `targets.3.1.4.de`: "Der Igel ist in der Nähe der Blätter." → "Der Igel ist in der Nähe des Fuchses."
- `focus.en`: "Prompt 'where' questions and simple home-related inference." → "Describe where the animals are and compare their places in the picture."
- `focus.de`: "Wo-Fragen stellen und einfache Überlegungen zu Tierwohnungen anregen." → "Beschreiben, wo die Tiere sind, und ihre Plätze im Bild vergleichen."
- `groups.0.1.en.1`: "Where is the rabbit going?" → "Where is the rabbit?"
- `groups.0.1.en.2`: "Which animal is in the den?" → "Which animal has a bushy orange tail?"
- `groups.0.1.en.3`: "What is the squirrel storing?" → "How many squirrels are on the log?"
- `groups.0.1.en.4`: "Which animal is near the leaf pile?" → "Which animal is near the fox?"
- `groups.0.1.de.1`: "Wohin geht das Kaninchen?" → "Wo ist das Kaninchen?"
- `groups.0.1.de.2`: "Welches Tier ist im Bau?" → "Welches Tier hat einen buschigen orangefarbenen Schwanz?"
- `groups.0.1.de.3`: "Was bewahrt das Eichhörnchen auf?" → "Wie viele Eichhörnchen sind auf dem Baumstamm?"
- `groups.0.1.de.4`: "Welches Tier ist in der Nähe des Laubhaufens?" → "Welches Tier ist in der Nähe des Fuchses?"
- `groups.1.1.en.0`: "Who lives in a hole in the tree?" → "Tell me about the squirrels on the log."
- `groups.1.1.en.1`: "Can you tell me which animal is by the water?" → "What is the owl sitting on?"
- `groups.1.1.de.0`: "Wer wohnt in einer Baumhöhle?" → "Erzähl mir von den Eichhörnchen auf dem Baumstamm."
- `groups.1.1.de.1`: "Kannst du mir sagen, welches Tier am Wasser ist?" → "Worauf sitzt die Eule?"
### scene.woodland.compare_groups.06

- `targets.0.1.5.en`: "squirrels" → "squirrel"
- `targets.2.1.2.en`: "three" → "one"
- `targets.2.1.2.de`: "drei" → "eins"
- `targets.3.1.4.en`: "There are three squirrels." → "There is one squirrel."
- `targets.3.1.4.de`: "Da sind drei Eichhörnchen." → "Da ist ein Eichhörnchen."
### scene.park.tasks.02

- `groups.1.1.en.0`: "What is the girl giving to the boy?" → "What is the girl in pink giving to the boy in blue?"
- `groups.1.1.de.0`: "Was gibt das Mädchen dem Jungen?" → "Was gibt das Mädchen in Rosa dem Jungen in Blau?"
### scene.mountains.spatial_clues.09

- `groups.0.1.en.0`: "Which animal is highest?" → "Which animal is flying above the stream?"
- `groups.0.1.de.0`: "Welches Tier ist am höchsten?" → "Welches Tier fliegt über dem Bach?"
### scene.farm.animal_actions.04

- `targets.0.1.2.en`: "chickens" → "chicken"
- `targets.0.1.2.de`: "Hühner" → "Huhn"
- `targets.3.1.2.en`: "The chickens are pecking the grain." → "The chicken is pecking the grain."
- `targets.3.1.2.de`: "Die Hühner picken die Körner." → "Das Huhn pickt die Körner."
- `groups.0.1.en.2`: "What are the chickens doing?" → "What is the chicken doing?"
- `groups.0.1.de.2`: "Was machen die Hühner?" → "Was macht das Huhn?"
- `groups.1.1.en.1`: "Why might the chickens be pecking the ground?" → "What is the chicken pecking?"
- `groups.1.1.de.1`: "Warum könnten die Hühner auf dem Boden picken?" → "Was pickt das Huhn?"
### scene.farm.farmyard_friends.08

- `groups.0.1.en.0`: "Which animal is the biggest?" → "Which animal is standing in the stable?"
- `groups.0.1.de.0`: "Welches Tier ist am größten?" → "Welches Tier steht im Stall?"
### scene.farm.animal_care.09

- `groups.0.1.en.0`: "Who needs water?" → "Which animal is beside the water trough?"
- `groups.0.1.de.0`: "Wer braucht Wasser?" → "Welches Tier steht neben dem Wassertrog?"
### scene.classroom.tasks.01

- `groups.1.1.en.0`: "What is the child doing with the book?" → "What is the child doing with the open book?"
- `groups.1.1.de.0`: "Was macht das Kind mit dem Buch?" → "Was macht das Kind mit dem aufgeschlagenen Buch?"
### scene.classroom.positions.03

- `examples.en.0`: "The backpack is under the desk." → "The blue backpack is under the desk."
- `examples.en.2`: "The book is in the bag." → "The red book is in the green bag."
- `examples.de.0`: "Der Rucksack ist unter dem Tisch." → "Der blaue Rucksack ist unter dem Tisch."
- `examples.de.2`: "Das Buch ist in der Tasche." → "Das rote Buch ist in der grünen Tasche."
- `groups.1.1.en.0`: "Where is the backpack?" → "Where is the blue backpack?"
- `groups.1.1.en.2`: "Where is the book?" → "Where is the red book?"
- `groups.1.1.de.0`: "Wo ist der Rucksack?" → "Wo ist der blaue Rucksack?"
- `groups.1.1.de.2`: "Wo ist das Buch?" → "Wo ist das rote Buch?"
- `groups.2.1.en.0`: "Put the backpack under the desk." → "Put the blue backpack under the desk."
- `expansions.0.1.en`: "Yes, the backpack is under the desk." → "Yes, the blue backpack is under the desk."
- `expansions.0.1.de`: "Ja, der Rucksack ist unter dem Tisch." → "Ja, der blaue Rucksack ist unter dem Tisch."
### scene.classroom.objects_places.04

- `targets.3.1.0.en`: "The pencils are on the table." → "The pencils are in the red cup on the table."
- `targets.3.1.0.de`: "Die Bleistifte liegen auf dem Tisch." → "Die Stifte sind im roten Becher auf dem Tisch."
- `groups.0.1.en.0`: "Where are the pencils?" → "Where is the red cup of pencils?"
- `groups.0.1.de.0`: "Wo sind die Bleistifte?" → "Wo steht der rote Becher mit den Stiften?"
### scene.classroom.end_of_day.09

- `targets.1.1.0.de`: "bekommen" → "entgegennehmen"
- `targets.1.1.2.en`: "waiting" → "standing"
- `targets.1.1.2.de`: "warten" → "stehen"
- `targets.1.1.4.en`: "putting away" → "holding"
- `targets.1.1.4.de`: "wegräumen" → "halten"
- `targets.3.1.1.en`: "The child is waiting near the teacher." → "The child is standing near the teacher."
- `targets.3.1.1.de`: "Das Kind wartet bei der Lehrkraft." → "Das Kind steht bei der Lehrkraft."
- `targets.3.1.2.en`: "The child is putting the book away." → "The child is holding a picture."
- `targets.3.1.2.de`: "Das Kind räumt das Buch weg." → "Das Kind hält ein Bild."
- `groups.0.1.en.3`: "Where does the book go?" → "Who is holding a picture?"
- `groups.0.1.de.3`: "Wohin kommt das Buch?" → "Wer hält ein Bild?"
- `groups.1.1.en.1`: "First point to the child packing the backpack, then point to the child putting the book away." → "First point to the child packing the backpack, then point to the child in blue holding a picture."
- `groups.1.1.de.1`: "Zeige zuerst auf das Kind, das den Rucksack packt, und dann auf das Kind, das das Buch wegräumt." → "Zeige zuerst auf das Kind, das den Rucksack packt, und dann auf das Kind in Blau, das ein Bild hält."
### scene.zoo.feeding_time.05

- `groups.0.1.en.0`: "Who is eating leaves?" → "What is the giraffe reaching for?"
- `groups.0.1.de.0`: "Wer frisst Blätter?" → "Wonach streckt sich die Giraffe?"
### scene.zoo.habitats_story.07

- `groups.0.1.en.0`: "Which animal is by the water?" → "Which animals are by the water?"
- `groups.0.1.de.0`: "Welches Tier ist am Wasser?" → "Welche Tiere sind am Wasser?"

## Shared completion/copy fixes

The old Tell Me COMPLETE branch rendered only text/actions; it never called `NativeCompletionCelebration`. It now uses that shared component, guarded by a completion-only owner ID. All three exit/repeat actions remain before the optional reward; no pop is required. The shell routes pop intents through existing foreground/owner/Sound Off guards and cancels on Again/category selection/Home. No speech or progress is introduced. The shared MODEL heading is now **You could say:** / **Du könntest sagen:**, which explicitly introduces an optional example.

## Validation

See BUILD_NOTES.md for final command/count evidence. Visual judgment and the unresolved Park 9 replacement cannot be proven by the automated tests. Re-test corrected prompts, blue backpack, cave language, final balloons, Sound Off, portrait/landscape and large text on S24; Fire acceptance remains separate.
