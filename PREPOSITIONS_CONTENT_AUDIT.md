# Prepositions wording and visual distinction audit — 2026-10-07

Scope: all 52 original PNGs in art revision 3 were opened and visually inspected,
with the authored manifest and native mappings. This is a source-art review, not
physical-device acceptance. The original review left PNGs and metadata unchanged.
On 2026-10-08 six cave replacements, followed by ten remaining borderline
replacements, were integrated and visually re-reviewed, including comparison sets.
Those artwork passes changed only the supplied PNG bytes and their manifest metadata,
plus audit/test expectations. Membership, paths and semantic IDs remain unchanged.
CLEAR means the intended cue is visible; BORDERLINE means plausible but
insufficiently robust for the strict teaching contract; ARTWORK_FIX_REQUIRED means
wording/code cannot establish the required spatial relation. No flagged scene is
silently removed from rounds. This ledger supersedes earlier blanket “reviewed”
claims for the specific distinction criteria below.

## Relation contract and final correct-answer phrases

These are canonical scene phrases; distractors inflect the actual scene reference.
Labels are not complete answers. OUTSIDE retains the standalone label “draußen”,
FAR_FROM “weit weg”; answer buttons and individual Listen semantics use full phrases.
INSIDE now uses label “in”; BELOW uses “unter”. Shared German words do not merge IDs.

| Relation | English | German | Required visual cue |
|---|---|---|---|
| ON | on the rock | auf dem Stein | Contact/support: animal rests on the top of the rock; no air gap. |
| UNDER | under the table | unter dem Tisch | Directly beneath the tabletop, within its horizontal footprint and framed by its legs. |
| BEHIND | behind the rock | hinter dem Stein | Rock occludes a substantial part of the animal; depth cannot depend on vertical placement alone. |
| NEXT_TO | next to the rock | neben dem Stein | Immediately beside the rock with a very small gap; show both ground positions, avoiding foreground overlap. |
| IN | in the box | in der Kiste | Container: front, back and side walls surround the animal; front rim occludes the lower body. |
| BETWEEN | between the two rocks | zwischen den beiden Steinen | Two equally salient rocks flank an animal centred in their gap; neither rock is incidental. |
| ABOVE | above the cloud | über der Wolke | Higher than cloud with an obvious continuous band of sky between silhouettes; no support/contact. |
| BELOW | below the cloud | unter der Wolke | Lower than cloud with visible air separation; no shelter/surface, unlike UNDER/table. |
| INSIDE | inside the cave | in der Höhle | Place/interior: entire animal clearly set back beyond the entrance; visible threshold and interior depth, not merely framed by the mouth. |
| OUTSIDE | outside the cave | außerhalb der Höhle | Entire animal clearly beyond the cave threshold; visible empty entrance/interior supplies the contrast. Avoid threshold overlap. |
| IN_FRONT_OF | in front of the rock | vor dem Stein | Animal occludes the rock on its foreground side; use overlapping silhouettes, not just above/below placement. |
| NEAR | near the rock | in der Nähe des Steins | Noticeable empty space separates animal and rock, still in the same local area. Exaggerate the separation from NEXT_TO, not a few pixels. |
| FAR_FROM | far from the rock | weit weg vom Stein | Substantial empty ground separates animal and rock; scale/depth reinforce distance. Keep both visible; distinguish instantly from NEAR. |

OUTSIDE decision: retain “außerhalb der Höhle” to express not being inside, rather
than encode foreground direction as “vor”. All four OUTSIDE scenes now have clear
threshold separation following both replacement batches. “Vor der Höhle” is easy German but
would collapse the taught answer into IN_FRONT_OF, and does not describe every
possible outside position. INSIDE answers always use “in der Höhle”, never
“drinnen der Höhle”. BELOW now takes dative (unter dem Stein/Tisch, unter der
Wolke/Kiste/Höhle), not the old genitive. NEAR and OUTSIDE correctly retain genitive;
FAR_FROM uses dative and contracts von dem to vom. BETWEEN retains “den beiden”.

## Distractor contract

Keep IN/INSIDE, UNDER/BELOW, ON/ABOVE and NEXT_TO/NEAR incompatible, including
among distractors. On cave references additionally exclude OUTSIDE/IN_FRONT_OF:
both could truthfully describe the current foreground cave animals. This is
reference-specific, not a global removal of directional contrasts. Keep
NEAR/FAR_FROM, BEHIND/IN_FRONT_OF, ABOVE/BELOW, ON/UNDER and INSIDE/OUTSIDE available.
BETWEEN/NEXT_TO stays available: both reference rocks appear in the phrase and
are visually salient. No distance-word exclusion can repair an ambiguous image.
The mathematical meanings of some spatial relations overlap; this bounded activity
teaches the explicit authored cues, not universal mutual exclusion of all relations.

## Full scene ledger

Exact IDs below are the manifest IDs. Paths follow the unchanged native mapping.

| Scene ID | Classification | Observation / next artwork action |
|---|---|---|
| `scene.prepositions.snake.on` | CLEAR | Body/feet rest directly on the rock surface; support is explicit. |
| `scene.prepositions.dinosaur.on` | CLEAR | Body/feet rest directly on the rock surface; support is explicit. |
| `scene.prepositions.dragon.on` | CLEAR | Body/feet rest directly on the rock surface; support is explicit. |
| `scene.prepositions.crocodile.on` | CLEAR | Body/feet rest directly on the rock surface; support is explicit. |
| `scene.prepositions.snake.under` | CLEAR | Tabletop and legs clearly enclose the animal underneath. |
| `scene.prepositions.dinosaur.under` | CLEAR | Tabletop and legs clearly enclose the animal underneath. |
| `scene.prepositions.dragon.under` | CLEAR | Tabletop and legs clearly enclose the animal underneath. |
| `scene.prepositions.crocodile.under` | CLEAR | Tabletop and legs clearly enclose the animal underneath. |
| `scene.prepositions.snake.behind` | CLEAR | Rock hides the middle/lower body; visible animal parts emerge behind its silhouette. |
| `scene.prepositions.dinosaur.behind` | CLEAR | Rock hides the middle/lower body; visible animal parts emerge behind its silhouette. |
| `scene.prepositions.dragon.behind` | CLEAR | Rock hides the middle/lower body; visible animal parts emerge behind its silhouette. |
| `scene.prepositions.crocodile.behind` | CLEAR | Rock hides the middle/lower body; visible animal parts emerge behind its silhouette. |
| `scene.prepositions.snake.next_to` | CLEAR | Replaced and visually re-reviewed 2026-10-08: Animal and rock sit side-by-side on the same ground plane with a narrow visible gap; neither silhouette overlaps or hides the other. Previously BORDERLINE: Very close beside rock, but tight source crop hides ground/body and foreground overlap also suggests IN_FRONT_OF. Reframe with a visible small side gap. |
| `scene.prepositions.dinosaur.next_to` | CLEAR | Replaced and visually re-reviewed 2026-10-08: Animal and rock sit side-by-side on the same ground plane with a narrow visible gap; neither silhouette overlaps or hides the other. Previously BORDERLINE: Very close beside rock, but tight source crop hides ground/body and foreground overlap also suggests IN_FRONT_OF. Reframe with a visible small side gap. |
| `scene.prepositions.dragon.next_to` | CLEAR | Replaced and visually re-reviewed 2026-10-08: Animal and rock sit side-by-side on the same ground plane with a narrow visible gap; neither silhouette overlaps or hides the other. Previously BORDERLINE: Very close beside rock, but tight source crop hides ground/body and foreground overlap also suggests IN_FRONT_OF. Reframe with a visible small side gap. |
| `scene.prepositions.crocodile.next_to` | CLEAR | Replaced and visually re-reviewed 2026-10-08: Animal and rock sit side-by-side on the same ground plane with a narrow visible gap; neither silhouette overlaps or hides the other. Previously BORDERLINE: Very close beside rock, but tight source crop hides ground/body and foreground overlap also suggests IN_FRONT_OF. Reframe with a visible small side gap. |
| `scene.prepositions.snake.in` | CLEAR | Box front rim hides lower body and side/back walls enclose it. |
| `scene.prepositions.dinosaur.in` | CLEAR | Box front rim hides lower body and side/back walls enclose it. |
| `scene.prepositions.dragon.in` | CLEAR | Box front rim hides lower body and side/back walls enclose it. |
| `scene.prepositions.crocodile.in` | CLEAR | Box front rim hides lower body and side/back walls enclose it. |
| `scene.prepositions.snake.between` | CLEAR | Two prominent flanking rocks and centred animal clearly establish the gap. |
| `scene.prepositions.dinosaur.between` | CLEAR | Two prominent flanking rocks and centred animal clearly establish the gap. |
| `scene.prepositions.dragon.between` | CLEAR | Two prominent flanking rocks and centred animal clearly establish the gap. |
| `scene.prepositions.crocodile.between` | CLEAR | Two prominent flanking rocks and centred animal clearly establish the gap. |
| `scene.prepositions.bird.above` | CLEAR | Unbroken blue sky separates the flying animal from the cloud below. |
| `scene.prepositions.bee.above` | CLEAR | Unbroken blue sky separates the flying animal from the cloud below. |
| `scene.prepositions.butterfly.above` | CLEAR | Unbroken blue sky separates the flying animal from the cloud below. |
| `scene.prepositions.dragon.above` | CLEAR | Unbroken blue sky separates the flying animal from the cloud below. |
| `scene.prepositions.bird.below` | CLEAR | Unbroken blue sky separates the animal from the higher cloud; no shelter cue. |
| `scene.prepositions.bee.below` | CLEAR | Unbroken blue sky separates the animal from the higher cloud; no shelter cue. |
| `scene.prepositions.butterfly.below` | CLEAR | Unbroken blue sky separates the animal from the higher cloud; no shelter cue. |
| `scene.prepositions.dragon.below` | CLEAR | Unbroken blue sky separates the animal from the higher cloud; no shelter cue. |
| `scene.prepositions.fish.inside` | CLEAR | Replaced and visually re-reviewed 2026-10-08: Whole animal set back within the dark interior, with visible floor before the threshold. Previously ARTWORK_FIX_REQUIRED: Animal fills the cave mouth; bottom/threshold is cropped. Dark backdrop alone cannot prove it is set back inside. Show whole animal beyond a visible threshold/interior floor. |
| `scene.prepositions.turtle.inside` | CLEAR | Replaced and visually re-reviewed 2026-10-08: Whole animal set back within the dark interior, with visible floor before the threshold. Previously ARTWORK_FIX_REQUIRED: Animal fills the cave mouth; bottom/threshold is cropped. Dark backdrop alone cannot prove it is set back inside. Show whole animal beyond a visible threshold/interior floor. |
| `scene.prepositions.octopus.inside` | CLEAR | Replaced and visually re-reviewed 2026-10-08: Whole animal set back within the dark interior, with visible floor before the threshold. Previously ARTWORK_FIX_REQUIRED: Animal fills the cave mouth; bottom/threshold is cropped. Dark backdrop alone cannot prove it is set back inside. Show whole animal beyond a visible threshold/interior floor. |
| `scene.prepositions.seahorse.inside` | CLEAR | Replaced and visually re-reviewed 2026-10-08: Whole animal set back within the dark interior, with visible floor before the threshold. Previously ARTWORK_FIX_REQUIRED: Animal fills the cave mouth; bottom/threshold is cropped. Dark backdrop alone cannot prove it is set back inside. Show whole animal beyond a visible threshold/interior floor. |
| `scene.prepositions.fish.outside` | CLEAR | Replaced and visually re-reviewed 2026-10-08: Whole animal is outside with clear space to the threshold; the dark entrance/interior remains empty and unobstructed. Previously BORDERLINE: Animal overlaps the cave mouth/rim in foreground, but the threshold separation is not explicit. Show whole silhouette clear of entrance. |
| `scene.prepositions.turtle.outside` | CLEAR | Replaced and visually re-reviewed 2026-10-08: Whole animal outside and separated from the entrance; dark interior remains visible and no body part crosses the threshold. Previously ARTWORK_FIX_REQUIRED: Turtle fills and overlaps the entrance, very similar to turtle.inside; threshold/body separation is not unambiguous. Move full animal outside a visible entrance. |
| `scene.prepositions.octopus.outside` | CLEAR | Replaced and visually re-reviewed 2026-10-08: Whole animal outside and separated from the entrance; dark interior remains visible and no body part crosses the threshold. Previously ARTWORK_FIX_REQUIRED: Lower body and threshold are cropped while animal overlaps the dark mouth; reads as at the entrance, not unmistakably outside. Show whole animal clear of threshold. |
| `scene.prepositions.seahorse.outside` | CLEAR | Replaced and visually re-reviewed 2026-10-08: Whole animal is outside with clear space to the threshold; the dark entrance/interior remains empty and unobstructed. Previously BORDERLINE: Animal overlaps the cave mouth/rim in foreground, but the threshold separation is not explicit. Show whole silhouette clear of entrance. |
| `scene.prepositions.snake.in_front_of` | CLEAR | Animal silhouette clearly covers the broad rock face; depth cue survives the tight lower crop. |
| `scene.prepositions.dinosaur.in_front_of` | CLEAR | Animal silhouette clearly covers the broad rock face; depth cue survives the tight lower crop. |
| `scene.prepositions.dragon.in_front_of` | CLEAR | Animal silhouette clearly covers the broad rock face; depth cue survives the tight lower crop. |
| `scene.prepositions.crocodile.in_front_of` | CLEAR | Animal silhouette clearly covers the broad rock face; depth cue survives the tight lower crop. |
| `scene.prepositions.snake.near` | CLEAR | Broad visible grass gap separates the animal and rock, wider than in the replacement NEXT_TO scene. |
| `scene.prepositions.dinosaur.near` | CLEAR | Replaced and visually re-reviewed 2026-10-08: Prominent animal and rock share a local area with an obvious mid-sized grass gap, substantially wider than the matching NEXT_TO scene. Previously BORDERLINE: Grass gap is visible but small relative to the body; still plausibly beside the rock. Widen it substantially for the strict NEAR/NEXT_TO contract. |
| `scene.prepositions.dragon.near` | CLEAR | Broad visible grass gap separates the animal and rock, wider than in the replacement NEXT_TO scene. |
| `scene.prepositions.crocodile.near` | CLEAR | Replaced and visually re-reviewed 2026-10-08: Prominent animal and rock share a local area with an obvious mid-sized grass gap, substantially wider than the matching NEXT_TO scene. Previously BORDERLINE: Snout approaches the rock and grass gap is modest relative to body length; could be read as NEXT_TO. Increase empty separation. |
| `scene.prepositions.snake.far_from` | CLEAR | Foreground rock and smaller distant animal separated by a broad ground plane. |
| `scene.prepositions.dinosaur.far_from` | CLEAR | Replaced and visually re-reviewed 2026-10-08: Full recognisable animal silhouette and rock are widely separated across open ground; head, feet, tail and wings where applicable remain in frame. Previously BORDERLINE: Ground depth conveys distance, but top of head is cropped and animal remains large. Reframe whole animal with stronger distance contrast. |
| `scene.prepositions.dragon.far_from` | CLEAR | Replaced and visually re-reviewed 2026-10-08: Full recognisable animal silhouette and rock are widely separated across open ground; head, feet, tail and wings where applicable remain in frame. Previously BORDERLINE: Ground depth conveys distance, but head/horns are cropped and animal remains large. Reframe whole animal with stronger distance contrast. |
| `scene.prepositions.crocodile.far_from` | CLEAR | Foreground rock and smaller distant animal separated by a broad ground plane. |

Original totals: 36 CLEAR, 10 BORDERLINE, 6 ARTWORK_FIX_REQUIRED.
Intermediate totals after six cave replacements: 42 CLEAR, 10 BORDERLINE, 0 ARTWORK_FIX_REQUIRED.
Final totals after ten further replacements: **52 CLEAR, 0 BORDERLINE, 0 ARTWORK_FIX_REQUIRED**.
All ten former BORDERLINE observations remain in the ledger as history. Both batches decode as
1448 × 1086 RGB PNGs (4:3); manifest SHA-256 values are calculated from supplied bytes.

## Comparison-set review — 2026-10-08

- Dinosaur NEXT_TO / NEAR / FAR_FROM: a narrow side gap, a broad local grass gap,
  then a much larger open span across the scene. Reduced subject scale in FAR_FROM
  reinforces distance; the entire dinosaur and rock remain visible and recognisable.
- Crocodile NEXT_TO / NEAR: the narrow snout-to-rock gap becomes a substantially wider
  band of open ground. Neither image uses overlap or occlusion to imply position.
- Snake and dragon NEXT_TO: clear small gaps on a common ground plane; no support,
  silhouette overlap or foreground/background hiding. Dragon FAR_FROM shows its full
  horns, wings, feet and tail, with a broad empty span to the rock.
- Fish / turtle / octopus / seahorse OUTSIDE: reviewed together using the new fish and
  seahorse images and unchanged accepted turtle/octopus replacements. All animals
  are fully outside, with visible empty cave interiors and no threshold crossing.

These are source-art comparison judgments, not pixel-distance thresholds or evidence
of child mastery. Physical phone/tablet legibility, especially the smaller FAR_FROM
subjects, and learner interpretation still require the established device review.

## Durable compatibility and guided speech

New rounds use activity revision 3 / content 1.3. Revision 1 / content 1.1 and
revision 2 / content 1.2 restore against their exact authored wording. Frozen v2
exceptions retain “unterhalb”/“drinnen” only for old rounds; they are not v3 content.
Active old rounds keep task/choice order, answers, support, attempts, language,
pending evidence and old question/hint/feedback. Completed old rounds likewise
retain their plan and deduplicated progress. Again creates revision 3. No journal
schema change, checkpoint rewriting or regeneration occurs. Validation still checks
all authored question fields; v3 additionally checks pair compatibility for the
scene reference. Old rounds are not retrospectively rejected by new restrictions.

Display, scene semantics and spoken option phrases select wording by saved activity
revision, so an old BELOW round does not mix “unter” buttons with “unterhalb” hints.
The guided question → stem → four visible phrases sequence, callbacks, highlighting,
answer actions and All/Questions/Off policy are unchanged. Individual Listen labels
now identify the same complete phrase they speak. “Wo ist die Schlange?” / “Die
Schlange ist…” and canonical EN subject generation remain unchanged.

Tests added/updated cover v3 noun cases and phrases, all scene/choice combinations,
compatibility and useful contrasts, complete ledger membership, frozen v1/v2
restore, old active/completed/pending journals, new-round version and guided speech.
Validation results for the combined content/artwork pass are recorded in SESSION_HANDOFF.md.
Source-art review does not constitute physical-device acceptance.
