# Explore Habitats and future Match interaction plan

## Navigation proposal

Reuse the current Home catalogue rather than redesigning Home or creating a second Animals & Nature hub:

**Home → Animals & Nature / Tiere & Natur → Explore Habitats / Lebensräume entdecken → habitat chooser → selected habitat scene.**

The current category already contains Animal Actions and Animal Groups. Add the future Explore Habitats activity as a destination in that existing category. Inside it, provide the implemented habitat choices. Phase 5C.2 should show only the one working Ocean scene; later releases add a choice when its scene and facts are ready. Do not display a disabled “coming soon” tile as if the experience works.

Navigation contracts for a later implementation:

- From a scene, Back returns to the habitat chooser; chooser Back returns to Animals & Nature; Home remains explicit.
- Options returns to the exact chooser or scene and preserves the selected habitat/animal where current app route conventions allow.
- Language change retains the selected habitat and target; names/facts switch together. All audio is child-controlled on selection/Replay and uses the shared offline native speech policy/Sound Off behavior.
- Explore is unscored and does not create Progress Tracker attempt/completion events. Scene selection/highlighting is browsing, not an answer.
- Keep current Animal Groups, Animal Actions, Vocabulary, Tell Me and Follow routes/journals intact. Do not repurpose their activity IDs or saved content as habitat state.

## Explore scene interaction

1. Enter the chooser silently; give each habitat a large bilingual name plus a clear visual cue.
2. Choose Ocean. Show one fitted static scene and a short spoken/readable prompt such as “Tap a sea friend to learn about it. / Tippe auf ein Meerestier und erfahre mehr.”
3. Tap a precisely reviewed subject. Add a strong neutral outline/marker and selected semantics, then speak its bilingual name followed by one short, sourced fact. Do not use hue alone.
4. Keep Replay visible and labeled, repeat only the selected name/fact, respect Sound Off, and do not queue long descriptions on rapid taps.
5. A target change replaces the old highlight and cancels stale speech. Tapping empty scenery does not select an unrelated animal.
6. Provide a visible list of animal-name buttons as a touch/keyboard/screen-reader alternative to hotspots. Use 56dp minimum targets; avoid overlapping hit regions. Each scene target should be one meaningful accessibility node, not duplicate decorative image announcements.
7. Provide a simple “Back to habitats / Zurück zu den Lebensräumen” control. No right/wrong feedback, score, timer or compulsory reading.

For the first Ocean image, the metadata identifies five focal targets, but only some should be activated in the first content slice. Dolphin is the safest first focus. Before enabling the turtle, confirm the depicted species supports “sea turtle” wording; do not rely only on a generic Turtle cutout ID. Avoid treating generic Fish as marine/freshwater species. Whale and shark do not occur in `ocean_001` and should not be overlaid into that composite by assumption.

## Accessibility, device and audio requirements

- Large labeled buttons/alternatives meet at least the project’s 56dp target convention; a visual subject may have a smaller drawn silhouette if its activation area remains generous and unambiguous.
- Scene description identifies the environment. Animal semantics announce bilingual name and selected state; facts must not be colour-only.
- Keyboard traversal reaches the chooser, every animal target, Replay, Back and Home. Enter/Space activates selection. Focus visibly follows selection. Review TalkBack and switch access separately; Compose semantics alone are not device acceptance.
- Scene uses responsive reflow for phone portrait/landscape, short landscape, tablet portrait/landscape and enlarged text. Fit or responsive layers preserve the whole important scene; no hidden essential prompts or inaccessible controls.
- Use the shared audio controller/owner and default audio modes. Do not add network voices, bundled speech, online service or habitat-specific TTS. No audio assets/SFX are present in the current asset tree.
- Use gentle selection feedback only. No automatic environmental animation/SFX is needed for the first stage; if reduced-motion settings apply to a later animated layer, provide static scene behavior.

## Future activity: Match Animals to Habitats / Tiere und Lebensräume zuordnen

Plan this only after several reviewed habitat scenes and relation facts exist. It is a separate scored activity, not an Explore mode toggle.

### Suitable prompt shapes

- **Context-specific one-answer choice:** “Which picture shows the ocean?” / “Welches Bild zeigt das Meer?” Unique answer is the pictured habitat, not a claim that an animal belongs nowhere else.
- **Explicit ‘could use’ multi-answer task:** show one reviewed animal and accept every authored valid habitat among the presented choices. Completion happens only when the whole valid answer set is handled.
- **Shelter relation:** “Where could this rabbit shelter?” can accept burrow/warren and other authored options if the prompt/context supports them.

Do not ask generic “Where does this animal live?” with one fixed answer if the animal uses several habitats. Do not use Land/Water/Air as exclusive bins. Existing `Sorting.SortRule` is incompatible with general many-to-many facts; use an existing semantic matching capability only if its accepted-set contract fits, otherwise build a bounded focused matching reducer rather than a speculative shared framework.

### Persistence and progress proposal

- New independent activity ID/revision/content version and journal. Candidate IDs such as `activity.discover.habitat_match` and a dedicated `skill.discover.habitat_association` are provisional; freeze them only after the exact task/prompt meaning is reviewed.
- Use shared `ProgressRepository`/`ProgressCodec` and stable task/session/event IDs; give each question a habitat-specific context ID so repeated success on one animal/image cannot establish generalisation.
- Freeze prompt, accepted answer set, language, order and support state in the durable snapshot. Restore exact task and accepted answers silently; retry pending progress effects idempotently under established host/journal rules.
- Keep `activity.discover.animal_groups` revision 1, its four-item decoder/journal and `skill.discover.animal_habitat`/`context.discover.water_land` events unchanged. The existing Water/Land skill must not silently accumulate new ecology semantics.

## Prepositions links (reuse content ideas, never the activity)

Use accurate complete sentences that state the relationship clearly. These are future optional spoken models, not changes to Prepositions:

| English | German |
|---|---|
| The mole is under the ground. | Der Maulwurf ist unter der Erde. |
| The rabbit is inside the burrow. | Das Kaninchen ist im Bau. |
| The fox is outside the den. | Der Fuchs ist außerhalb des Baus. |
| The rabbit is next to the burrow entrance. | Das Kaninchen ist neben dem Eingang des Baus. |
| The mole tunnel runs between the roots. | Der Maulwurfgang verläuft zwischen den Wurzeln. |
| The leaves are above the soil. | Die Blätter wachsen über der Erde. |

Have a German reviewer confirm age-natural phrasing, especially context-sensitive uses of *Bau*, *unter der Erde*, *im Bau* and *außerhalb des Baus*. No existing Prepositions activity content, journal or test should be edited by this curriculum proposal.
