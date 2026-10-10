# Tell Me — all-scene draft review

Revision 11 checkpoint (2026-10-10): a three-scene third-question pilot adds the C bundles below. All A/B bundles remain intact. 78 scenes have two slots; three have three, for 165 bilingual slots. Each pilot C has a distinct expressive invitation and fully paired support. Current PNGs were directly inspected; THIRD_QUESTION_PILOT.json records evidence and hashes. Native test extensions are authored but unrun; native-speaker and device acceptance remain pending.

| Exact scene ID | Third visit EN / DE | Learning objective |
| --- | --- | --- |
| scene.ocean.hiding_places.03 | Imagine the crab comes out of the cave. What might it do?<br>Stell dir vor, die Krabbe kommt aus der Höhle. Was könnte sie machen? | Imagination and storytelling |
| scene.classroom.routines.02 | What could the child do after asking for help?<br>Was könnte das Kind machen, nachdem es um Hilfe gebeten hat? | Sequencing and reasoning |
| scene.zoo.feature_clues.08 | Where might the penguin go next?<br>Wohin könnte der Pinguin als Nächstes gehen? | Predictions |

The A/B scene table remains a review of the first two visits; C appears only in the pilot table above. Historical checkpoint totals below describe their revisions.

Revision 10 checkpoint (2026-10-10): all 162 question/example/model pairs were reviewed. 121 short bilingual child examples across 73 scenes now model the requested action, position, feature, feeling, reason or expressive response instead of only naming the subject or an unrelated word. Exact replacements and all prior bundles are in CHILD_EXAMPLE_CORRECTIONS.json. Questions and all other support fields are unchanged. Names/gestures remain welcome and examples are optional. This pass derives examples from existing models; it does not add visual, native-speaker or device acceptance.

Revision 9 checkpoint (2026-10-10): 12 bundles across 11 scenes now offer useful clue examples, concrete imaginary story openings and simpler bilingual wording. Exact before/after bundles, pictured evidence and preserved slots are in CLUE_STORY_CORRECTIONS.json. Ocean .08 B moves from naming to properties; Woodland .09 B moves from personal choice to story. Models remain optional examples, and invented dialogue/events are explicitly imaginary. Current PNGs were directly inspected; native-speaker and device acceptance remain pending.

Revision 8 checkpoint (2026-10-10): 17 spatial prompts across 14 scenes now use simple EN/DE “Where…?” questions without supplying a location. Optional guidance preserves the pictured reference objects and accepts other accurate descriptions. Current PNGs were directly inspected; SPATIAL_PROMPT_CORRECTIONS.json records exact bundles and preservation hashes. Learning-objective totals remain unchanged. Android, native-speaker and device acceptance remain pending.

Revision 7 checkpoint (2026-10-10): seven action/action pairs now retain their first-visit bundle and offer prediction, pretend speech, a possible reason, visual comparison, sequencing or creative expression on the second visit. Current PNGs were directly reviewed; exact before/after bundles and picture evidence are in QUESTION_VARIETY_CORRECTIONS.json. The twelve purpose labels below are the original editorial taxonomy: simple sequencing and creative expression use its broad story label. These are not difficulty levels or runtime assessment tags.

Revision 6 checkpoint (2026-10-10): two current PNGs were directly reviewed. Woodland .06 B and Classroom .06 A now use picture-supported questions and complete matching support. See EDITORIAL_CORRECTIONS.json. The remaining draft review and build limitations below describe the original revision-5 pass.

Draft against main `8c48de18cec5183dfb136414d7bddf502b087c14`. This supersedes the two-scene draft. Owner authorized publication before Android validation; native-language and image review remain pending.

All 81 scenes now have two explicitly paired EN/DE conversation invitations and matching starter, three helpful words, model, short child example and parent guidance. There are 165 distinct question strings per language (78 scenes with two slots; three pilot scenes with three). The original teaching/reference lists remain unchanged except for the documented revision-6 corrections; these are curated slots, not rotation through every old list entry.

First displayed wording changes in 71 English and 71 German scenes; other good first prompts remain with a new alternative and matching support.

## Quantitative comparison

| Measure | Previous implementation | Complete draft |
| --- | --- | --- |
| Scene/Theme count | 81 / 9 | 81 / 9 |
| Accessible main slots per scene | 1 | 2 in 78 scenes; 3 in pilot scenes |
| Distinct initial prompts per language | 67 | 81 |
| Scenes with Help/model material | 27 | 81 |
| Immediate slot repetition on a repeat visit in the same Activity | Always | Avoided by per-scene A/B or A/B/C rotation |

Learning-purpose labels below are human editorial judgments about the prompt’s primary invitation. They are not automatic difficulty scores. Repeated use of a grammatical pattern with different meaningful pictured relationships can reinforce language; exact-string uniqueness alone is not learning acceptance.

| Purpose | Initial visits (81) | All slots (165) |
| --- | ---: | ---: |
| naming | 2 | 4 |
| actions | 11 | 16 |
| spatial | 11 | 28 |
| properties | 13 | 27 |
| counting | 6 | 8 |
| emotions | 2 | 9 |
| cause | 5 | 10 |
| prediction | 10 | 16 |
| problem | 8 | 9 |
| story | 3 | 16 |
| social | 6 | 11 |
| personal | 4 | 11 |

### Counts by theme across both slots

| Theme | naming | actions | spatial | properties | counting | emotions | cause | prediction | problem | story | social | personal |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| ocean_underwater | 0 | 2 | 4 | 5 | 1 | 0 | 0 | 3 | 0 | 3 | 0 | 1 |
| jungle_rainforest | 0 | 3 | 3 | 4 | 1 | 1 | 0 | 2 | 1 | 2 | 0 | 1 |
| woodland_forest | 0 | 2 | 4 | 5 | 1 | 1 | 1 | 1 | 2 | 1 | 0 | 0 |
| park_playground | 0 | 2 | 3 | 1 | 1 | 2 | 1 | 1 | 0 | 1 | 5 | 1 |
| sky_flying | 0 | 1 | 3 | 4 | 1 | 1 | 1 | 2 | 1 | 2 | 0 | 2 |
| mountains_alpine | 0 | 1 | 3 | 3 | 1 | 1 | 2 | 2 | 3 | 1 | 0 | 1 |
| countryside_farm | 2 | 3 | 3 | 1 | 2 | 1 | 2 | 1 | 0 | 1 | 1 | 1 |
| classroom_school | 1 | 1 | 3 | 0 | 0 | 1 | 0 | 0 | 1 | 4 | 5 | 3 |
| zoo_wildlife | 1 | 1 | 2 | 4 | 0 | 1 | 3 | 4 | 1 | 1 | 0 | 1 |

## Scene-level EN/DE review

Slot A is the first visit; B is the next entered visit in the same Activity. Each image remains unchanged. The evidence ledger has the source path, historical image hash, picture evidence and every support string. Picture evidence for 33 scenes was rechecked against current PNGs in revisions 6–9. The remaining scenes retain their original audit evidence; this does not establish new device acceptance.

Park .09 uses only the current owner-replacement review’s two seated children and sole pink-clothed waiting child. The old contact sheet is not authority for that replacement. New roleplay/feeling copy still needs current-image and S24 review.

### ocean_underwater

| Scene ID | A EN / DE | B EN / DE | Purposes A / B |
| --- | --- | --- | --- |
| scene.ocean.reef_actions.01 | What is the dolphin doing?<br>Was macht der Delfin? | What might the crab do with the shell next?<br>Was könnte die Krabbe als Nächstes mit der Muschel machen? | actions / prediction |
| scene.ocean.colours_sizes.02 | How are the turtle and the little fish different?<br>Wie unterscheiden sich die Schildkröte und die kleinen Fische? | Describe the colours of the clownfish.<br>Beschreibe die Farben des Clownfischs. | properties / properties |
| scene.ocean.hiding_places.03 | Where is the crab hiding in this picture?<br>Wo versteckt sich die Krabbe auf diesem Bild? | Where is the turtle?<br>Wo ist die Schildkröte? | spatial / spatial |
| scene.ocean.story_actions.04 | What might the crab find as it digs in the sand?<br>Was könnte die Krabbe beim Graben im Sand finden? | Imagine the octopus leaves the cave. Tell me what happens.<br>Stell dir vor, der Oktopus verlässt die Höhle. Erzähl, was passiert. | prediction / story |
| scene.ocean.big_small_numbers.05 | How many little orange fish are swimming together?<br>Wie viele kleine orange Fische schwimmen zusammen? | Tell me how the whale and the turtle compare in size.<br>Erzähl mir, wie groß der Wal und die Schildkröte im Vergleich sind. | counting / properties |
| scene.ocean.reef_routines.06 | What is the turtle eating?<br>Was frisst die Schildkröte? | Where is the octopus?<br>Wo ist der Oktopus? | actions / spatial |
| scene.ocean.helping_story.07 | What might the turtle do when it reaches the cave?<br>Was könnte die Schildkröte machen, wenn sie an der Höhle ankommt? | Where could the turtle and the blue fish go together in your story?<br>Wohin könnten die Schildkröte und die blauen Fische in deiner Geschichte zusammen schwimmen? | prediction / story |
| scene.ocean.find_and_follow.08 | Can you find the crab in the cave?<br>Findest du die Krabbe in der Höhle? | Tell me what the starfish looks like so I can find it.<br>Beschreibe den Seestern, damit ich ihn finden kann. | spatial / properties |
| scene.ocean.describing_sea_friends.09 | Tell me about the crab and its blue shell.<br>Erzähl mir von der Krabbe und ihrer blauen Muschel. | Which animal in this picture would you like to meet?<br>Welches Tier auf diesem Bild würdest du gern treffen? | properties / personal |

### jungle_rainforest

| Scene ID | A EN / DE | B EN / DE | Purposes A / B |
| --- | --- | --- | --- |
| scene.jungle.actions.01 | What is the monkey doing on the vine?<br>Was macht der Affe an der Liane? | Imagine the elephant could talk. What might it say?<br>Stell dir vor, der Elefant könnte sprechen. Was könnte er sagen? | actions / story |
| scene.jungle.colours_features.02 | What do you notice about the tiger's stripes?<br>Was fällt dir an den Streifen des Tigers auf? | How are the elephant and the frog different in size?<br>Wie unterscheiden sich der Elefant und der Frosch in der Größe? | properties / properties |
| scene.jungle.positions.03 | Where is the frog sitting in this picture?<br>Wo sitzt der Frosch auf diesem Bild? | Where is the tiger?<br>Wo ist der Tiger? | spatial / spatial |
| scene.jungle.animal_homes.04 | Where is the sloth hanging?<br>Wo hängt das Faultier? | What might the monkey do with its banana?<br>Was könnte der Affe mit seiner Banane machen? | spatial / prediction |
| scene.jungle.busy_actions.05 | What is the elephant doing with its trunk?<br>Was macht der Elefant mit seinem Rüssel? | How many monkeys are holding the vines?<br>Wie viele Affen halten sich an den Lianen fest? | actions / counting |
| scene.jungle.big_small_groups.06 | Describe the toucan's big beak.<br>Beschreibe den großen Schnabel des Tukans. | Tell me how the tiger and the frog compare.<br>Erzähl mir, wie sich der Tiger und der Frosch unterscheiden. | properties / properties |
| scene.jungle.story_actions.07 | What might the parrot do when it reaches the nest?<br>Was könnte der Papagei machen, wenn er am Nest ankommt? | Begin a little story about the jumping frog.<br>Fang eine kleine Geschichte über den springenden Frosch an. | prediction / story |
| scene.jungle.clue_finding.08 | Choose an animal. Tell me a clue so I can guess it.<br>Such dir ein Tier aus. Gib mir einen Hinweis, damit ich es erraten kann. | Describe how the sloth holds on to the vine.<br>Beschreibe, wie sich das Faultier an der Liane festhält. | problem / actions |
| scene.jungle.actions_directions.09 | Which animal would you like to act like?<br>Welches Tier würdest du gern nachmachen? | How might it feel to be splashed by the elephant's water?<br>Wie könnte es sich anfühlen, vom Wasser des Elefanten angespritzt zu werden? | personal / emotions |

### woodland_forest

| Scene ID | A EN / DE | B EN / DE | Purposes A / B |
| --- | --- | --- | --- |
| scene.woodland.features.01 | Tell me about the rabbit's ears.<br>Erzähl mir von den Ohren des Kaninchens. | How are the deer and the rabbit different in size?<br>Wie unterscheiden sich das Reh und das Kaninchen in der Größe? | properties / properties |
| scene.woodland.actions.02 | What is the squirrel carrying?<br>Was trägt das Eichhörnchen? | Why might the deer come to the water?<br>Warum könnte das Reh zum Wasser kommen? | actions / cause |
| scene.woodland.positions.03 | Tell me where the rabbit is hiding.<br>Erzähl mir, wo sich das Kaninchen versteckt. | Where is the owl?<br>Wo ist die Eule? | spatial / spatial |
| scene.woodland.autumn_prep.04 | What is on the hedgehog's back?<br>Was ist auf dem Rücken des Igels? | What is the fox doing?<br>Was macht der Fuchs? | properties / actions |
| scene.woodland.animal_homes.05 | How many squirrels are sitting together on the log?<br>Wie viele Eichhörnchen sitzen zusammen auf dem Baumstamm? | Tell me where the owl is sitting in this woodland scene.<br>Erzähl mir, wo die Eule auf diesem Waldbild sitzt. | counting / spatial |
| scene.woodland.compare_groups.06 | What do you notice about the owl's eyes?<br>Was fällt dir an den Augen der Eule auf? | Where is the fox resting?<br>Wo ruht sich der Fuchs aus? | properties / spatial |
| scene.woodland.before_rain.07 | What might the rabbit do when it starts to rain?<br>Was könnte das Kaninchen machen, wenn es anfängt zu regnen? | What can you see in the sky?<br>Was siehst du am Himmel? | prediction / properties |
| scene.woodland.homes_choices.08 | Where could the rabbit rest in this picture?<br>Wo könnte sich das Kaninchen auf diesem Bild ausruhen? | How might the hedgehog feel in the pile of leaves?<br>Wie könnte sich der Igel im Laubhaufen fühlen? | problem / emotions |
| scene.woodland.find_animals.09 | How could you describe the deer so I can find it?<br>Wie könntest du das Reh beschreiben, damit ich es finde? | Imagine the hedgehog goes for a walk. What happens in your story?<br>Stell dir vor, der Igel geht spazieren. Was passiert in deiner Geschichte? | problem / story |

### park_playground

| Scene ID | A EN / DE | B EN / DE | Purposes A / B |
| --- | --- | --- | --- |
| scene.park.actions.01 | What is the child on the slide doing?<br>Was macht das Kind auf der Rutsche? | Which activity in this playground would you like to try?<br>Was würdest du auf diesem Spielplatz gern ausprobieren? | actions / personal |
| scene.park.tasks.02 | What could the girl in pink say as she gives the red ball to the boy in blue?<br>Was könnte das Mädchen in Rosa sagen, wenn es dem Jungen in Blau den roten Ball gibt? | How could the children share the bucket and spade?<br>Wie könnten die Kinder Eimer und Schaufel teilen? | social / social |
| scene.park.positions.03 | Where is the backpack by the bench?<br>Wo ist der Rucksack bei der Bank? | Where is the kite?<br>Wo ist der Drachen? | spatial / spatial |
| scene.park.social_play.04 | How could the two children build the sandcastle together?<br>Wie könnten die beiden Kinder zusammen die Sandburg bauen? | How might the child on the swing feel?<br>Wie könnte sich das Kind auf der Schaukel fühlen? | social / emotions |
| scene.park.big_small_counting.05 | Can you tell me how many yellow buckets there are?<br>Kannst du mir erzählen, wie viele gelbe Eimer da sind? | Describe the tall slide and the long bench.<br>Beschreibe die hohe Rutsche und die lange Bank. | counting / properties |
| scene.park.routines_positions.06 | Why would you wear a helmet before riding this scooter?<br>Warum würdest du vor dem Rollerfahren einen Helm aufsetzen? | Where is the ball?<br>Wo ist der Ball? | cause / spatial |
| scene.park.playground_actions.07 | What might the child on the rope bridge do next?<br>Was könnte das Kind auf der Seilbrücke als Nächstes machen? | Tell me about the child playing in the sandbox.<br>Erzähl mir von dem Kind, das im Sandkasten spielt. | prediction / actions |
| scene.park.helping_clean_up.08 | How could the children help each other clean up?<br>Wie könnten die Kinder einander beim Aufräumen helfen? | What could happen after a child picks up the bottle?<br>Was könnte passieren, nachdem ein Kind die Flasche aufgehoben hat? | social / story |
| scene.park.turn_taking.09 | What could the child in pink say to ask for a turn?<br>Was könnte das Kind in Rosa sagen, wenn es auch mal dran sein möchte? | How might the child waiting by the seesaw feel?<br>Wie könnte sich das Kind fühlen, das neben der Wippe wartet? | social / emotions |

### sky_flying

| Scene ID | A EN / DE | B EN / DE | Purposes A / B |
| --- | --- | --- | --- |
| scene.sky.actions.01 | What is the balloon doing beside the upward arrows?<br>Was macht der Ballon neben den Pfeilen nach oben? | How does the eagle look different from the airplane?<br>Was sieht beim Adler anders aus als beim Flugzeug? | actions / properties |
| scene.sky.features.02 | Tell me about the colours on the kite.<br>Erzähl mir von den Farben des Drachens. | How are the long cloud and the round cloud different?<br>Wie unterscheiden sich die lange und die runde Wolke? | properties / properties |
| scene.sky.positions.03 | Where is the helicopter?<br>Wo ist der Hubschrauber? | Where is the balloon?<br>Wo ist der Ballon? | spatial / spatial |
| scene.sky.weather_story.04 | How is the weather different on the sunny side and the rainy side?<br>Wie unterscheidet sich das Wetter auf der sonnigen und der regnerischen Seite? | What could you do if rain came while you were flying the kite?<br>Was könntest du machen, wenn es beim Drachensteigen regnet? | properties / problem |
| scene.sky.big_small_counting.05 | How many little yellow birds are flying together?<br>Wie viele kleine gelbe Vögel fliegen zusammen? | Where might the little yellow birds fly next?<br>Wohin könnten die kleinen gelben Vögel als Nächstes fliegen? | counting / prediction |
| scene.sky.travel_directions.06 | Which flying vehicle here would you like to travel in?<br>Mit welchem Fluggerät hier würdest du gern reisen? | What is flying on the right-hand side of this travel scene?<br>Was fliegt rechts auf diesem Reisebild? | personal / spatial |
| scene.sky.weather_sequence.07 | Tell me the weather story from the sun to the rain.<br>Erzähl mir die Wettergeschichte von der Sonne bis zum Regen. | What appears after the rain in this picture story?<br>Was erscheint nach dem Regen in dieser Bildergeschichte? | story / story |
| scene.sky.flying_directions.08 | Imagine you are in the balloon. What can you see below?<br>Stell dir vor, du bist im Ballon. Was siehst du unten? | How might you feel riding high in this balloon?<br>Wie könntest du dich fühlen, wenn du hoch oben in diesem Ballon fährst? | prediction / emotions |
| scene.sky.night_sky_clues.09 | What clues in this picture tell you it is night?<br>Welche Hinweise auf diesem Bild zeigen dir, dass es Nacht ist? | Have you seen a moon like this? Tell me about it.<br>Hast du schon einen Mond wie diesen gesehen? Erzähl mir davon. | cause / personal |

### mountains_alpine

| Scene ID | A EN / DE | B EN / DE | Purposes A / B |
| --- | --- | --- | --- |
| scene.mountains.features.01 | Tell me about the mountain goat's horns.<br>Erzähl mir von den Hörnern der Bergziege. | How does the sheep look different from the marmot?<br>Wie sieht das Schaf im Vergleich zum Murmeltier aus? | properties / properties |
| scene.mountains.actions.02 | What is the goat doing on the rocks?<br>Was macht die Ziege auf den Felsen? | What could the marmot do after digging?<br>Was könnte das Murmeltier nach dem Graben machen? | actions / story |
| scene.mountains.positions.03 | Where is the rabbit?<br>Wo ist das Kaninchen? | Where are the flowers?<br>Wo sind die Blumen? | spatial / spatial |
| scene.mountains.busy_routines.04 | What might the eagle do with the twig?<br>Was könnte der Adler mit dem Zweig machen? | Why might the marmot be carrying grass?<br>Warum könnte das Murmeltier Gras tragen? | prediction / cause |
| scene.mountains.big_small_groups.05 | How many marmots are together in this mountain picture?<br>Wie viele Murmeltiere sind auf diesem Bergbild zusammen? | Describe the little white rabbit beside the larger animals.<br>Beschreibe das kleine weiße Kaninchen bei den größeren Tieren. | counting / properties |
| scene.mountains.homes_places.06 | Where could the eagle rest in this picture?<br>Wo könnte sich der Adler auf diesem Bild ausruhen? | Why might the rabbit choose a place under the bush?<br>Warum könnte das Kaninchen einen Platz unter dem Busch wählen? | problem / cause |
| scene.mountains.journey_story.07 | What might the people and dog do next along the path?<br>Was könnten die Menschen und der Hund als Nächstes am Weg machen? | What would you like to look at on this mountain walk?<br>Was würdest du dir auf diesem Bergspaziergang gern anschauen? | prediction / personal |
| scene.mountains.weather_shelter.08 | Where could the people go if it starts to snow?<br>Wohin könnten die Menschen gehen, wenn es anfängt zu schneien? | How might someone feel near the snow in this picture?<br>Wie könnte sich jemand in der Nähe des Schnees auf diesem Bild fühlen? | problem / emotions |
| scene.mountains.spatial_clues.09 | How could you cross the stream using something in this picture?<br>Wie könntest du mit etwas auf diesem Bild über den Bach kommen? | Where is the marmot?<br>Wo ist das Murmeltier? | problem / spatial |

### countryside_farm

| Scene ID | A EN / DE | B EN / DE | Purposes A / B |
| --- | --- | --- | --- |
| scene.farm.tasks.01 | What is the child doing with the watering can?<br>Was macht das Kind mit der Gießkanne? | Why do you think the child is watering the plants?<br>Warum gießt das Kind wohl die Pflanzen? | actions / cause |
| scene.farm.features.02 | Describe the cow's colours.<br>Beschreibe die Farben der Kuh. | Are there more sheep or ducklings in this picture?<br>Sind auf diesem Bild mehr Schafe oder Entenküken? | properties / counting |
| scene.farm.positions.03 | Where is the cat by the hay bale?<br>Wo ist die Katze beim Heuballen? | Where is the pig?<br>Wo ist das Schwein? | spatial / spatial |
| scene.farm.animal_actions.04 | How might the pig feel in the mud?<br>Wie könnte sich das Schwein im Matsch fühlen? | What is the chicken doing?<br>Was macht das Huhn? | emotions / actions |
| scene.farm.transport_places.05 | What might happen after the tractor carries the hay away?<br>Was könnte passieren, nachdem der Traktor das Heu weggebracht hat? | Tell me what the horse is doing in this farm picture.<br>Erzähl mir, was das Pferd auf diesem Bauernhofbild macht. | prediction / actions |
| scene.farm.garden_food.06 | What fruit is growing on the tree?<br>Welches Obst wächst am Baum? | Where do the carrots grow?<br>Wo wachsen die Karotten? | naming / spatial |
| scene.farm.morning_jobs.07 | Which farm job here would you like to help with?<br>Bei welcher Aufgabe auf diesem Bauernhofbild würdest du gern helfen? | How could you help the child feeding the chickens?<br>Wie könntest du dem Kind beim Füttern der Hühner helfen? | personal / social |
| scene.farm.farmyard_friends.08 | How many little ducklings are together near the pond?<br>Wie viele kleine Entenküken sind zusammen beim Teich? | Tell me which animal you can find in the stable.<br>Erzähl mir, welches Tier du im Stall findest. | counting / naming |
| scene.farm.animal_care.09 | How is the child with the bucket helping the cow?<br>Wie hilft das Kind mit dem Eimer der Kuh? | What could happen after the bucket of water reaches the cow?<br>Was könnte passieren, wenn der Eimer mit Wasser bei der Kuh ankommt? | cause / story |

### classroom_school

| Scene ID | A EN / DE | B EN / DE | Purposes A / B |
| --- | --- | --- | --- |
| scene.classroom.tasks.01 | What is the child doing with the open book?<br>Was macht das Kind mit dem aufgeschlagenen Buch? | What could the child make with the paper?<br>Was könnte das Kind aus dem Papier machen? | actions / story |
| scene.classroom.routines.02 | The child has a hand up. What could it say if it needs help?<br>Das Kind hebt die Hand. Was könnte es sagen, wenn es Hilfe braucht? | What could you say when you have finished your work?<br>Was könntest du sagen, wenn du mit deiner Arbeit fertig bist? | social / social |
| scene.classroom.positions.03 | Where is the blue backpack?<br>Wo ist der blaue Rucksack? | Where is the red book?<br>Wo ist das rote Buch? | spatial / spatial |
| scene.classroom.objects_places.04 | What can you see inside the red cup on the table?<br>Was siehst du im roten Becher auf dem Tisch? | Where is the teddy bear?<br>Wo ist der Teddybär? | naming / spatial |
| scene.classroom.quiet_corner.05 | How do you think the dog feels while resting?<br>Wie fühlt sich der Hund wohl beim Ausruhen? | What helps you relax when you want some quiet time?<br>Was hilft dir, dich zu entspannen, wenn du Ruhe möchtest? | emotions / personal |
| scene.classroom.art_sorting.06 | What could you use to paint a picture?<br>Womit könntest du ein Bild malen? | What would you like to draw with these art materials?<br>Was würdest du mit diesen Malsachen gern malen? | problem / personal |
| scene.classroom.school_routines.07 | What could the child say when giving the picture to the teacher?<br>Was könnte das Kind sagen, wenn es der Lehrkraft das Bild gibt? | What could happen after the child packs the bag?<br>Was könnte passieren, nachdem das Kind die Tasche gepackt hat? | social / story |
| scene.classroom.helping_cleanup.08 | What could the child do after picking up the pencils?<br>Was könnte das Kind machen, nachdem es die Stifte aufgehoben hat? | How could you help the child picking up the pencils?<br>Wie könntest du dem Kind beim Aufheben der Stifte helfen? | story / social |
| scene.classroom.end_of_day.09 | Tell me about something you do before going home from school.<br>Erzähl mir etwas, das du machst, bevor du von der Schule nach Hause gehst. | What could the child in blue say about the picture they are holding?<br>Was könnte das Kind in Blau über das Bild sagen, das es hält? | personal / social |

### zoo_wildlife

| Scene ID | A EN / DE | B EN / DE | Purposes A / B |
| --- | --- | --- | --- |
| scene.zoo.features.01 | Tell me about the giraffe's long neck.<br>Erzähl mir vom langen Hals der Giraffe. | How are the elephant and the penguin different in size?<br>Wie unterscheiden sich der Elefant und der Pinguin in der Größe? | properties / properties |
| scene.zoo.actions.02 | How is the penguin moving through the water?<br>Wie bewegt sich der Pinguin durch das Wasser? | Where might the monkey go next?<br>Wohin könnte der Affe als Nächstes gehen? | actions / prediction |
| scene.zoo.positions.03 | Where is the giraffe?<br>Wo ist die Giraffe? | Where is the zebra?<br>Wo ist das Zebra? | spatial / spatial |
| scene.zoo.habitats.04 | How is the otter's place different from the bear's place?<br>Wie unterscheidet sich der Platz des Otters vom Platz des Bären? | What might the kangaroo do next on the ground?<br>Was könnte das Känguru als Nächstes auf dem Boden machen? | properties / prediction |
| scene.zoo.feeding_time.05 | How does the elephant use its trunk to reach the leaves?<br>Wie benutzt der Elefant seinen Rüssel, um die Blätter zu erreichen? | How does the giraffe's long neck help it reach food?<br>Wie hilft der lange Hals der Giraffe, an das Futter zu kommen? | cause / cause |
| scene.zoo.positions_story.06 | What might the monkey do next on the climbing frame?<br>Was könnte der Affe als Nächstes am Klettergerüst machen? | How might the lion feel while lying on the rock?<br>Wie könnte sich der Löwe fühlen, wenn er auf dem Felsen liegt? | prediction / emotions |
| scene.zoo.habitats_story.07 | Imagine the penguin could talk to the elephant. What might it say?<br>Stell dir vor, der Pinguin könnte mit dem Elefanten sprechen. Was könnte er sagen? | Which two animals can you find beside the water?<br>Welche beiden Tiere findest du beim Wasser? | story / naming |
| scene.zoo.feature_clues.08 | Give me a clue about an animal here. Can I guess which one?<br>Gib mir einen Hinweis zu einem Tier hier. Kann ich erraten, welches du meinst? | Tell me what you notice about the zebra's stripes.<br>Erzähl mir, was dir an den Streifen des Zebras auffällt. | problem / properties |
| scene.zoo.water_shade.09 | Why might the lion rest in the shade?<br>Warum könnte sich der Löwe im Schatten ausruhen? | Which place here would you choose to rest, and why?<br>Welchen Platz hier würdest du zum Ausruhen wählen, und warum? | cause / personal |

## Learning and language review limits

The first three pictures generally retain concrete observation and description; later pictures add supported choices, reasons, short narratives, feelings and personal connections. This is a gentle editorial progression, not levels, a prerequisite system or a performance assessment. All slots have bounded support. They may invite different amounts of language, but selection is predictable and never random.

All feelings, future events and invented explanations use tentative or imaginary wording. A model is one possible response, never an answer key. Parent guidance accepts pointing, short answers, different feelings/ideas and no past personal experience. Child examples are literal authored short phrases; models and parent guidance are authored whole sentences in each language.

The picture library cannot supply every kind of narrative. This draft varies conversations using existing pictures; it does not establish that new artwork is unnecessary. The original P3 artwork recommendations remain deferred.

German is drafted for the same intention as English, but independent native-speaker acceptance is pending for every new bundle. Prioritize the relative/conditional clauses, asking-for-a-turn wording, feelings vocabulary and comparison questions. Bilingual field presence and matching bundle structure do not prove linguistic or visual semantics.

## Validation and remaining gates

26 metadata/authoring fixture checks passed; one production-image review/hash check skipped. Fixtures contain only PNG signatures in a temporary directory outside production assets. The generated catalogue is deterministic and its digest covers all 91 actual manifest/metadata files. The original English fingerprint passes with separately asserted new bundles removed.

JVM, Android compilation/APK/lint, instrumentation, actual image checks and physical acceptance have not run. The cloud proxy and missing SDK/Gradle still block those checks. No speech, scoring, progress persistence, new artwork or source publish action was added. The implementation and added Kotlin/Compose tests remain unbuilt draft code.
