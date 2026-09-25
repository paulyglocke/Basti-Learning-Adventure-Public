# German teaching/support review — SceneDescriptions

Date: 2026-09-25. Bilingual-authoring baseline: `296d6ef`; final targeted-review baseline: `8e00cea`.

## Bilingual-authoring scope and counts (revision 2)

- **81 approved scenes / 9 categories / 9 scenes each** structurally reviewed.
- **54 Wave 2/3 scenes** received authored German for every existing runtime English
  teaching/support value. New wording was reviewed scene by scene for meaning,
  articles/cases, noun capitalisation, verb forms, number agreement and natural prompts.
- **1,698 German text fields authored** in those 54 scenes. A field here means one
  scalar text value or one list entry, counting repeated words in each authored context;
  it does not mean a JSON container or a unique dictionary word.
- **1 additional German field** translates the existing runtime farm Wave 1 scene 1
  review caution. Total newly authored German values: **1,699**.
- The 27 Wave 1 scenes retain their existing bilingual teaching/support copy.
  They were the style/reference baseline, not a new wholesale language rewrite.
- The catalogue has **3,397 English / 3,395 German scene text/list-entry values**
  (excludes technical IDs/focus tags and category labels). Every runtime text field
  has both locales. Two existing Wave 1 words-to-model lists consolidate synonyms:
  Ocean .02 small/little → klein; Sky .03 below/under → unter. No concept is missing.
  Those independently authored lists remain unchanged and must not be zipped into
  positional translation pairs.
- This is authored-content review by the coding assistant, **not independent
  native-speaker/owner acceptance**. Automated checks establish completeness and
  preservation, not educational quality or perfect German by themselves.

| Category | Wave 2/3 scenes translated | German values added |
| --- | ---: | ---: |
| ocean_underwater | 6 | 205 |
| jungle_rainforest | 6 | 204 |
| woodland_forest | 6 | 213 |
| park_playground | 6 | 180 |
| sky_flying | 6 | 175 |
| mountains_alpine | 6 | 184 |
| countryside_farm | 6 | 187 |
| classroom_school | 6 | 176 |
| zoo_wildlife | 6 | 174 |
| Total | 54 | 1,698 |

## Minimal normalization, no third format

Wave 2/3 scalar text becomes the already-supported `{en, de}` object. Target-language
list entries use the same pair shape as Wave 1. Adult prompt lists become the existing
`{en: [...], de: [...]}` shape. Purpose/focus and the farm review reason use those same
text pairs. No fields, objectives, prompt sequences or answers were invented.
The generator already parsed these shapes; its production validation now requires
both languages for every present runtime text field. The optional pure text model
still returns null for unavailable German, never English fallback. Scene pack schema
stays 1; content revision advances to 2. No manifests, IDs, ordering, paths, PNGs,
authoring prompts or runtime activities changed.

## Natural German choices and consistency

- “might” → “könnte”, not a certain prediction; “looks” → “sieht … aus” / “scheint”.
- “first / then / after / before” → “zuerst / dann / danach / davor”, with natural
  subordinate clauses using “bevor” or “nachdem” where appropriate.
- “next to” → “neben”; locations use dative (“unter dem Stein”, “hinter der Koralle”).
  Movement uses the intended destination (“in den Bau”, “unter den Busch”).
- Above/below relative elevations use “oberhalb/unterhalb” where direct vertical
  alignment is not required; ordinary pictured relations retain “über/unter”.
- “take turns” → “sich abwechseln”; “Who goes next?” → “Wer ist als Nächstes dran?”;
  “tidy up / put away” → “aufräumen / wegräumen”; “help” → “helfen”.
- Animal eating is “fressen”; child eating would be “essen”. Bare vocabulary verbs
  are normally infinitives, while complete model sentences are naturally conjugated.
- “Can you make a sentence …?” → “Kannst du einen Satz … sagen?” rather than a
  literal “machen”. Models invite expansion; no exact repetition is demanded.
- Sky “balloon” is “Heißluftballon” in vocabulary and “Ballon” in sentences, consistent
  with the established Sky art/concept. “kite” is “Drachen”, “hang glider” is
  “Hängegleiter”, keeping the flying objects distinct.
- “teacher” remains gender-neutral “Lehrkraft”; no gender is inferred from a picture.
- “picking vegetables” → “Gemüse ernten”; “inside the gate” → “im Gatter” in the
  farm enclosure context. “perching” → “sitzen” without inventing an unmentioned perch.

## Revision 2 English preservation and verified source-error exception

English teaching/support content was **not rewritten for style or translation**.
A normalized English catalogue fingerprint, captured at `296d6ef`, is checked in
unit tests along with identities, references and authored ordering.

One source error was verified against the committed PNG and corrected in exactly
**two English fields** of `scene.park.big_small_counting.05`:

1. `targetLanguage.sentenceModels[3]`: “There are three yellow buckets.” →
   “There are four yellow buckets.”
2. `adultSupport.expansionPrompts[1]`: “Can you make a sentence with three yellow
   buckets?” → “Can you make a sentence with four yellow buckets?”

The PNG shows one yellow bucket beside the child plus three more in a row: four.
German says “vier” in both corresponding fields. At revision 2, tests reversed only these two
explicit corrections before matching the original English fingerprint. No other
English runtime value changed in that slice. Revision 3 adds the seven explicitly
asserted exceptions listed below, preserving the same original fingerprint. No asset
or manifest changed.

## Source ambiguities retained and visual spot-check limits

Directly inspected **10 PNGs**: Park 005 and Wave 3 Park/Sky/Mountains 007–009.
This was a targeted content check, not a fresh visual acceptance sweep of all 81.

- **Farm .01:** existing logged four-versus-three-chickens deviation remains; chicken
  counting is not its teaching goal. Existing English caution is unchanged and now
  also available in German. Do not use it for a chicken-count question.
- **Park .05:** bucket mismatch verified and corrected as documented above.
- **Park .07:** resolved in revision 3 below: runtime support describes the visible
  position on the rope bridge without requiring climbing. Other plausible child
  descriptions remain valid; the model is not an answer key.
- **Park .08/.09:** clean-up/turn-taking themes are supported, but a still cannot
  prove the temporal sequence or who is next. Predictions remain open-ended.
- **Sky .07:** visible weather panels support a depicted sequence, not a universal
  rule that every rain shower ends in a rainbow. A single prominent eagle is visible;
  the generic plural vocabulary “birds” remains source-authored, not a count claim.
- **Sky .08:** airplane-below-helicopter relation is visible; top cropping makes
  “highest” a less precise prompt. Do not invent a fixed correct answer.
- **Sky .09:** illustrated night-sky objects are present; the composition is not
  evidence of realistic shared altitude or flight conditions.
- **Mountains .07:** resolved in revision 3 below: hikers/path are visible, but
  uphill versus downhill is not unambiguous. Runtime wording is direction-neutral;
  no constrained direction question remains.
- **Mountains .08:** weather panels are visible; timing is narrative, not measured.
- **Mountains .09:** marmot is lower in the picture than the high-rock animal, but
  not directly beneath it. German uses “unterhalb”, not a stronger “direkt darunter”.
  Multiple horned animals make a bare “goat” reference less precise than the authored
  high-rock clue. Do not strengthen the source to a species-specific label.
- Some cropped panels retain borders/edge fragments. No image was edited, cropped,
  regenerated or newly declared pixel-perfect.
- Source grouping is not a grammar ontology: “three” and spatial words occur under
  adjectives; Mountains .04 lists “mountain” there (German “in den Bergen”, a location
  phrase). Jungle .07 repeats “leaf”. These are preserved and noted, not silently
  reclassified/deduplicated. Meaning does not depend on those grammatical buckets.

No unreviewed visual fact was invented to fill a translation gap. Remaining
ambiguities need owner/content review; the catalogue is bilingual, but that does
not turn its example sentences into automatic grading rules.

## Final targeted visual/language review (revision 3)

Reopened the two committed PNGs directly at `8e00cea`; neither needs image changes.
Park .07 shows the child's feet on the rope bridge, not unambiguous climbing.
Standing on the bridge is a defensible model; crossing/walking is also a possible
child description and must not be rejected. The visible climbing frame remains
`climbing frame / Klettergerüst` in the noun list; an object name does not claim an action.
Mountains .07 shows hikers on a path, without proving uphill or downhill travel.
The existing open-ended “What might they see next?” prediction remains unchanged.

Exactly **seven English values and seven aligned German values** changed in two
metadata files. Indices below are zero-based. No other runtime wording changed.

| Scene / field | English before → after | German before → after |
| --- | --- | --- |
| Park .07 `targetLanguage.verbs[2]` | climbing → standing | klettern → stehen |
| Park .07 `targetLanguage.sentenceModels[2]` | The child is climbing. → The child is standing on the rope bridge. | Das Kind klettert. → Das Kind steht auf der Seilbrücke. |
| Park .07 `adultSupport.starterPrompts[2]` | Who is climbing? → Who is on the rope bridge? | Wer klettert? → Wer ist auf der Seilbrücke? |
| Mountains .07 `targetLanguage.verbs[1]` | climbing → hiking | klettern → wandern |
| Mountains .07 `targetLanguage.sentenceModels[3]` | They are going up the mountain. → They are walking together on the path. | Sie gehen den Berg hinauf. → Sie gehen zusammen auf dem Weg. |
| Mountains .07 `adultSupport.starterPrompts[3]` | Are they going up or down? → Who is on the path? | Gehen sie hinauf oder hinunter? → Wer ist auf dem Weg? |
| Mountains .07 `adultSupport.expansionPrompts[0]` | Tell me the journey from the bottom of the path upward. → Tell me about their journey along the path. | Erzähl mir von der Wanderung, vom unteren Ende des Weges nach oben. → Erzähl mir von ihrer Wanderung auf dem Weg. |

Secondary review read the German adult-support groups, sentence starters, models
and expansion pairs across all nine categories, with a targeted check of recurring
sequence/spatial/comparison, helping/turn-taking/cleanup, classroom and path wording.
“zuerst / dann / danach”, “bevor / nachdem”, “als Nächstes”, “neben / hinter / vor”,
“größer / kleiner”, “sich abwechseln”, “helfen” and “aufräumen / wegräumen” remain
contextually authored. Why/because language remains open-ended rather than supplying
an unpictured cause. No further clear material German correction was identified;
existing natural wording and deliberately incomplete child utterances/starters stay.
This is not an independent native-speaker acceptance or an all-image visual audit.

Schema stays **1**; content revision increases **2 → 3**, as required for authored
meaning changes. Both locales remain complete for all 81 scenes. IDs, counts,
manifest order, paths, PNGs, all other source fields and authoring files are unchanged.
The generator/model API is unchanged. The library is ready for a bounded open-ended
Tell Me consumer; existing visual caveats above remain, not fixed-answer rules.

## Validation

See the current BUILD_NOTES.md entry for exact commands/counts. Automated checks
cover 81 scenes, assets, IDs/order, bilingual availability for every runtime text,
missing/blank German rejection in both later waves, English preservation with the
explicit revision 2 and 3 corrections, safe unknown lookups and authoring-only exclusion.
Two successive regeneration/check cycles must be byte-identical. No UI/device,
TTS, speech-input or automatic-grading acceptance is claimed by this content work.

## Metadata files updated

Paths below are relative to `app/src/main/assets/SceneDescriptions/`. The farm .01
entry adds only German for the existing runtime review caution; the other 54 entries
complete Wave 2/3 teaching/support text.

- `classroom_school/metadata/classroom_004_objects_places.json`
- `classroom_school/metadata/classroom_005_quiet_corner.json`
- `classroom_school/metadata/classroom_006_art_sorting.json`
- `classroom_school/metadata/classroom_007_school_routines.json`
- `classroom_school/metadata/classroom_008_helping_cleanup.json`
- `classroom_school/metadata/classroom_009_end_of_day.json`
- `countryside_farm/metadata/farm_001_tasks.json`
- `countryside_farm/metadata/farm_004_animal_actions.json`
- `countryside_farm/metadata/farm_005_transport_and_places.json`
- `countryside_farm/metadata/farm_006_garden_food.json`
- `countryside_farm/metadata/farm_007_morning_jobs.json`
- `countryside_farm/metadata/farm_008_farmyard_friends.json`
- `countryside_farm/metadata/farm_009_animal_care.json`
- `jungle_rainforest/metadata/jungle_004_animal_homes.json`
- `jungle_rainforest/metadata/jungle_005_busy_actions.json`
- `jungle_rainforest/metadata/jungle_006_big_small_groups.json`
- `jungle_rainforest/metadata/jungle_007_story_actions.json`
- `jungle_rainforest/metadata/jungle_008_clue_finding.json`
- `jungle_rainforest/metadata/jungle_009_actions_directions.json`
- `mountains_alpine/metadata/mountains_004_busy_routines.json`
- `mountains_alpine/metadata/mountains_005_big_small_groups.json`
- `mountains_alpine/metadata/mountains_006_homes_places.json`
- `mountains_alpine/metadata/mountains_007_journey_story.json`
- `mountains_alpine/metadata/mountains_008_weather_shelter.json`
- `mountains_alpine/metadata/mountains_009_spatial_clues.json`
- `ocean_underwater/metadata/ocean_004_story_actions.json`
- `ocean_underwater/metadata/ocean_005_big_small_numbers.json`
- `ocean_underwater/metadata/ocean_006_reef_routines.json`
- `ocean_underwater/metadata/ocean_007_helping_story.json`
- `ocean_underwater/metadata/ocean_008_find_and_follow.json`
- `ocean_underwater/metadata/ocean_009_describing_sea_friends.json`
- `park_playground/metadata/park_004_social_play.json`
- `park_playground/metadata/park_005_big_small_counting.json`
- `park_playground/metadata/park_006_routines_positions.json`
- `park_playground/metadata/park_007_playground_actions.json`
- `park_playground/metadata/park_008_helping_clean_up.json`
- `park_playground/metadata/park_009_turn_taking.json`
- `sky_flying/metadata/sky_004_weather_story.json`
- `sky_flying/metadata/sky_005_big_small_counting.json`
- `sky_flying/metadata/sky_006_travel_directions.json`
- `sky_flying/metadata/sky_007_weather_sequence.json`
- `sky_flying/metadata/sky_008_flying_directions.json`
- `sky_flying/metadata/sky_009_night_sky_clues.json`
- `woodland_forest/metadata/woodland_004_autumn_prep.json`
- `woodland_forest/metadata/woodland_005_animal_homes.json`
- `woodland_forest/metadata/woodland_006_compare_groups.json`
- `woodland_forest/metadata/woodland_007_before_rain.json`
- `woodland_forest/metadata/woodland_008_homes_choices.json`
- `woodland_forest/metadata/woodland_009_find_animals.json`
- `zoo_wildlife/metadata/zoo_004_habitats.json`
- `zoo_wildlife/metadata/zoo_005_feeding_time.json`
- `zoo_wildlife/metadata/zoo_006_positions_story.json`
- `zoo_wildlife/metadata/zoo_007_habitats.json`
- `zoo_wildlife/metadata/zoo_008_feature_clues.json`
- `zoo_wildlife/metadata/zoo_009_water_and_shade.json`
