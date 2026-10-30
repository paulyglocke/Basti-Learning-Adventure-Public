# Match Animals to Habitats — reviewed starter question bank

**Activity:** `activity.discover.habitat_matching`
**Activity revision:** 1
**Content version:** 1.1
**Skill:** `skill.discover.animal_habitat_matching`
**Pack size:** seven questions, randomized order and answer-card order per session seed.
**Assessment:** one tap per question; incorrect answers allow explicit retry; a correct answer advances only after Continue.

## Authoring rule

The prompts ask about the illustrated lessons or a specific relationship. They do
not claim that the animal belongs exclusively to one of the four app scenes. A
question stores its own non-empty `acceptedAnswers` set, frozen in its durable
checkpoint. The Rabbit scene question has two accepted answers because the app
teaches Rabbit in both Woodland and Underground. Either answer is a correct
response to “Choose a place where we met rabbits.” It does not claim those are
the only environments rabbits can use.

Each question has at least one distractor. Most have two large visual options;
the multi-answer Rabbit scene question has three so that it can contain two
valid choices and one distractor. Habitat previews use the existing canonical
scene images, fitted into small cropped cards and accompanied by bilingual
labels.

## Canonical identities and visual choices

| ID | Canonical animal | Animal image | Habitat options |
|---|---|---|---|
| `habitat.ocean` | — | — | `SceneDescriptions/ocean_underwater/canonical/ocean_001_reef_actions.png` |
| `habitat.woodland` | — | — | `SceneDescriptions/woodland_forest/canonical/woodland_008_homes_choices.png` |
| `habitat.underground` | — | — | `SceneDescriptions/underground/canonical/underground_001_cutaway.png` |
| `habitat.freshwater` | — | — | `SceneDescriptions/freshwater_pond/canonical/freshwater_001_pond.png` |
| `animal.dolphin` | Dolphin / Delfin | `Animals/canonical/dolphin.png` | |
| `animal.rabbit` | Rabbit / Kaninchen | `Animals/canonical/rabbit.png` | |
| `animal.mole` | Mole / Maulwurf | `Animals/canonical/mole.png` | |
| `animal.earthworm` | Earthworm / Regenwurm | `Animals/canonical/earthworm.png` | |
| `animal.frog` | Frog / Frosch | `Animals/canonical/frog.png` | |
| `animal.duck` | Duck / Ente | `Animals/canonical/duck.png` | |

The generic clownfish image is excluded from Freshwater questions. Fox is also
omitted: the existing woodland composite depicts Fox, but there is no separate
canonical Fox cutout in the reviewed six-animal image set used here.

## Questions

| Question ID / task definition | Animal | Prompt EN / DE | Visual options | Accepted answer IDs | Rationale and source fact |
|---|---|---|---|---|---|
| `dolphin_scene` / `task.animals.habitat.dolphin_scene` | Dolphin | “Where did we explore the dolphin?” / “Wo haben wir den Delfin entdeckt?” | Ocean, Woodland | `habitat.ocean` | The prompt refers to the app’s pictured marine scene. Source: “Dolphins live in the sea.” / “Delfine leben im Meer.” It does not claim dolphins only live in marine environments. |
| `rabbit_scenes` / `task.animals.habitat.rabbit_scenes` | Rabbit | “Choose a place where we met rabbits.” / “Wähle einen Ort, an dem wir Kaninchen getroffen haben.” | Woodland, Underground, Ocean | `habitat.woodland`, `habitat.underground` | Rabbit is explored in both scenes. Source relationship retained from the audit: “Wild rabbits can use underground warrens for shelter.” / “Wildkaninchen können unterirdische Baue als Schutz nutzen.” Woodland is a scene association; Underground is supported as a shelter context. |
| `mole_tunnels` / `task.animals.habitat.mole_tunnels` | Mole | “Where does the mole dig tunnels?” / “Wo gräbt der Maulwurf seine Gänge?” | Underground, Ocean | `habitat.underground` | Tests the reviewed tunnel relationship, rather than making a claim about every place a mole may visit. Source: “A mole lives mostly underground and digs tunnels.” / “Ein Maulwurf lebt überwiegend unter der Erde und gräbt Gänge.” |
| `earthworm_scene` / `task.animals.habitat.earthworm_scene` | Earthworm | “Where did we explore the earthworm moving through soil?” / “Wo haben wir den Regenwurm entdeckt, der sich durch den Boden bewegt?” | Underground, Woodland | `habitat.underground` | Refers to the app’s soil cutaway. It does not imply all earthworms live at one depth. Source: “Earthworms move through soil.” / “Regenwürmer bewegen sich durch den Boden.” |
| `frog_scene` / `task.animals.habitat.frog_scene` | Frog | “Where did we explore the frog?” / “Wo haben wir den Frosch entdeckt?” | Freshwater, Ocean | `habitat.freshwater` | This is a scene-recall prompt. Source fact also describes damp land use: “Many frogs lay eggs in water and also use damp places on land.” / “Viele Frösche legen ihre Eier im Wasser ab und nutzen auch feuchte Plätze an Land.” It does not classify frogs as water-only. |
| `duck_scene` / `task.animals.habitat.duck_scene` | Duck | “Where did we explore the duck?” / “Wo haben wir die Ente entdeckt?” | Freshwater, Woodland | `habitat.freshwater` | Refers to the app’s pond lesson. Source: “Many ducks swim on ponds and lakes.” / “Viele Enten schwimmen auf Teichen und Seen.” The wording does not claim that ducks use only freshwater. |
| `rabbit_shelter` / `task.animals.habitat.rabbit_shelter` | Rabbit | “Where can wild rabbits use a warren for shelter?” / “Wo können Wildkaninchen einen Bau als Schutz nutzen?” | Underground, Freshwater | `habitat.underground` | Explicitly asks about shelter, not an exclusive habitat. Source: “Wild rabbits can use underground warrens for shelter.” / “Wildkaninchen können unterirdische Baue als Schutz nutzen.” |

## Feedback and support

Correct feedback by question:

| Question | EN | DE |
|---|---|---|
| Dolphin scene | Yes! We explored the dolphin in the ocean. | Ja! Wir haben den Delfin im Meer entdeckt. |
| Rabbit scenes | Yes! We explored rabbits in both places. | Ja! Wir haben Kaninchen an beiden Orten entdeckt. |
| Mole tunnels | Yes! The mole digs tunnels underground. | Ja! Der Maulwurf gräbt Gänge unter der Erde. |
| Earthworm scene | Yes! We explored the earthworm in the underground scene. | Ja! Wir haben den Regenwurm in der Untergrund-Szene entdeckt. |
| Frog scene | Yes! We explored the frog at a freshwater pond. | Ja! Wir haben den Frosch an einem Süßwasserteich entdeckt. |
| Duck scene | Yes! We explored the duck by a freshwater pond. | Ja! Wir haben die Ente an einem Süßwasserteich entdeckt. |
| Rabbit shelter | Yes! Wild rabbits can shelter in underground warrens. | Ja! Wildkaninchen können in unterirdischen Bauen Schutz finden. |

Every question uses the same neutral retry feedback:

- EN: “Not that place. Try another one.”
- DE: “Dieser Ort passt nicht. Probiere einen anderen.”

Question-specific correct feedback names the relation or the in-app scene. For
Rabbit’s multi-answer scene prompt it says “Yes! We explored rabbits in both
places.” / “Ja! Wir haben Kaninchen an beiden Orten entdeckt.” It remains true
for either accepted selection. The optional Help action offers “Think about the
place we explored.” / “Denk an den Ort, den wir entdeckt haben.”

These source facts and relationship limits are from the Phase 5C.1 reviewed
[habitat fact catalogue](../animals-phase5c1-audit/HABITAT_FACTS.md) and the
production bilingual copy recorded in the Phase 5C.2–5C.5 habitat reviews. No
question uses the old Animal Groups Water/Land rule as a general ecology model.
