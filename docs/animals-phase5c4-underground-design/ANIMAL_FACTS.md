# Phase 5C.4A — Underground animal facts

## Scope and wording

The Phase 5C.1 habitat catalogue is the content authority. The short English/German lines below retain its reviewed age-level wording; they describe distinct relationships and do not make Underground an exclusive habitat class. Source links below support the biological scope. They do not prove that an illustration depicts the described animal/context; that is a separate art approval.

Native-speaker review remains required before release. The Earthworm source material also cautions that species occupy different layers and ecological groupings; the short child-facing fact avoids a universal depth claim.

| Proposed stable animal ID | Label EN / DE | Relationship | Approved Explore fact EN / DE | Caveat |
|---|---|---|---|---|
| `animal.mole` (new habitat content ID; no shared registry yet) | Mole / Maulwurf | Lives mostly underground; digs tunnels | “A mole lives mostly underground and digs tunnels.” / “Ein Maulwurf lebt überwiegend unter der Erde und gräbt Gänge.” | “Mostly” avoids saying it never surfaces. The cited audit source is specifically the European mole (*Talpa europaea*); do not claim the generic artwork is a scientifically exact species portrait. [PTES mole guide](https://ptes.org/going-underground-the-extraordinary-life-of-a-mole/). |
| `animal.rabbit` (already used in Animal Groups and Habitat Explore; no shared registry) | Wild Rabbit / Wildkaninchen (scene label review required) | Uses underground warren as shelter | “Wild rabbits can use underground warrens for shelter.” / “Wildkaninchen können unterirdische Baue als Schutz nutzen.” | Keep “wild” and “can”. Do not say rabbits live only underground or apply the relationship to domestic pet rabbits. A visible warren context is needed. [Woodland Trust animal homes](https://www.woodlandtrust.org.uk/blog/animal-tracks-id-kids/). |
| `animal.earthworm` (new proposed ID) | Earthworm / Regenwurm | Moves through soil; species use varied soil layers | “Earthworms move through soil.” / “Regenwürmer bewegen sich durch den Boden.” | This is a movement/soil-use fact, not a claim that all earthworms live deep underground. Different species have surface-litter, soil and deeper-burrowing lifestyles. [Natural History Museum, life in soil](https://www.nhm.ac.uk/discover/life-in-soil.html); [Natural History Museum, earthworm lifestyles](https://www.nhm.ac.uk/discover/news/2024/april/organic-compost-diverse-gardens-best-for-uk-earthworms.html). |

## Candidate assessment and deferrals

- **Mole:** strongest first selection. The catalogue has a reviewed fact and matching standalone cutout. It is the only animal in the initial roster described as living mostly underground and digging its tunnels.
- **Wild rabbit:** include as a shelter relationship. Keep the visible entrance/chamber connection and the fact's wild/can wording. Its current standalone picture is generically labeled Rabbit; localized display may use “Wild Rabbit / Wildkaninchen” only after content owner review confirms the scene/context and naming decision.
- **Earthworm:** include only if an individually visible, sufficiently large approved illustration can be made. It enriches the soil layer but should not be placed inside the mole tunnel as if it were a worm's home; the scene can show it moving through soil.
- **Badger:** defer. The existing catalogue's fact is “Badgers use underground setts. They also come above ground to look for food.” / “Dachse nutzen unterirdische Baue. Zur Nahrungssuche kommen sie auch an die Oberfläche.” There is no canonical cutout. Do not call its sett a rabbit warren.
- **Ant:** defer. The catalogue says only some species build nests in soil; other ants nest elsewhere. The generic identity/art does not name a species, and no cutout exists.
- **Fox:** defer from this scene's first version. Existing Woodland has an activity-local `animal.fox` scene target but no canonical standalone Fox PNG. The reviewed fact says foxes live in many habitats and may use an underground den to raise cubs. Do not imply that every fox lives underground or duplicate the Woodland composite fox as a cutout.

## Relationship semantics for implementation

Keep these typed meanings separate even if the implementation stores a compact content record:

- Mole → `lives_mostly_in` underground; `digs` tunnels.
- Wild Rabbit → `shelters_in` an underground warren.
- Earthworm → `moves_through` soil; depth/layer depends on kind/species.

The initial Explore is descriptive and unscored. No answer classification, exclusive habitat choice or Progress Tracker evidence is needed. Store the reviewed bilingual fact with the scene animal definition; do not change Animal Groups' Water/Land rules or IDs.
