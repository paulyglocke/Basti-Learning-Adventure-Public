# Freshwater facts

**Phase 5C.5A design status:** proposed drafts. **Phase 5C.5B implementation:**
Frog and Duck have been implemented using the reviewed wording below. The
English/German strings below are the exact production copy; no Fish is included.

These are short design drafts informed by the Phase 5C.1 fact rules. The bilingual text is not yet production content: a German native-speaker review and a final pictured-species/context review are required before implementation.

| Proposed animal | English | German | Relationship and limits | Evidence |
|---|---|---|---|---|
| Frog / Frosch | “Many frogs lay eggs in water and also use damp places on land.” | “Viele Frösche legen ihre Eier im Wasser ab und nutzen auch feuchte Plätze an Land.” | Retains the already reviewed 5C.1 wording. This describes breeding and terrestrial use; it does not imply that adult frogs remain in ponds or that every species uses the same habitat. | [Freshwater Habitats Trust — Common Frog](https://freshwaterhabitats.org.uk/species/common-frog/); [Natural History Museum — frog life cycle](https://www.nhm.ac.uk/discover/frogspawn-tadpoles-and-froglets.html) |
| Duck / Ente | “Many ducks swim on ponds and lakes.” | “Viele Enten schwimmen auf Teichen und Seen.” | A movement/context fact, not an exclusive habitat claim. Ducks can use a range of wetlands, including salt/brackish contexts; “many” avoids claiming every duck is a freshwater animal. | [LBV — Stockente](https://www.lbv.de/ratgeber/naturwissen/artenportraits/detail/stockente/) confirms Mallards are found on standing and slow-moving waters and also in protected sea bays; [Cornell Lab — Mallard](https://www.allaboutbirds.org/guide/mallard) describes Mallards at ponds and lakes. |

The frog fact is retained from `docs/animals-phase5c1-audit/HABITAT_FACTS.md`; use it instead of an unsupported blanket “frogs live in ponds” sentence. The German is grammatically correct and the duck translation is natural. Both are short enough for speech, but this audit is not a substitute for the required owner/native-speaker audio review.

## Fish decision

Do not include generic `animal.fish` in the first scene. Its bundled picture is visually a clownfish and the canonical identity says only “Fish / Fisch”. The Phase 5C.1 catalogue specifically requires a named freshwater species for an animal fact. Future inclusion should identify a local fish species, review its range/ecology and obtain species-appropriate art. A broadly worded “some fish live in freshwater” fact could explain the habitat contrast but would not make this clownfish illustration a freshwater animal.

## Later relationship data

If the future pack needs structured facts, follow the existing 5C.1 conceptual record: stable animal ID, habitat ID, relation type (such as `BREEDS_IN`, `MOVES_ON`, or `LIVES_IN`), bilingual fact, species scope, age note, evidence source and explicit scored-match suitability. Keep these distinctions authored; do not derive them from the selected Explore scene or movement alone.

