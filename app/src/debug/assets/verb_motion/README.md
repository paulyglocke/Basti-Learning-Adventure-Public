# Verb motion preview artwork

Historical source study. The active native preview now reads the shared main
`VerbMotion/rabbit.webp`, `eagle.webp` and `croc.webp` atlases. Rabbit has eight jump
poses within its 16-pose shared sheet; Eagle has eight wing poses. The PNGs below
remain for provenance/comparison. See the [production prompt set](../../../../../docs/verb-animation-fixes-2026-11-06/README.md).

Built-in image generation tool; source identity references are the existing canonical rabbit, eagle and crocodile PNGs. These debug-only atlases are review assets, not accepted replacement curriculum.

Rabbit/eagle use four matched poses in equal 2×2 grids at 1024px. The original
`snap.png` remains as a comparison. The active crocodile is `snap-v2.png`: eight
poses in a 4×2 grid, normalized to 1600×800 (400px cells). Originals remain in
Codex generated_images. Runtime preserves source aspect and does not morph frames.
The [stronger snap prompt](SNAP_V2_PROMPT.md) records the follow-up edit.

## Shared prompt

Use case: illustration-story. Asset type: a transparent 2 by 2 animation key-pose atlas for a native offline children's verb demonstration. Use the provided canonical animal as the identity/style reference: preserve its colours, facial features, markings and friendly polished illustrated finish, adapting to a clear full-body side view facing RIGHT. Canvas square, four equally sized square cells, exact 2 columns and 2 rows, no gutters or panel borders. In each cell, one complete isolated animal fully contained with generous transparent margin; consistent camera, head size, anatomy and scale across all cells. Do not draw a background, floor, shadows, labels, numbers, arrows, objects, extra animals or motion lines. Genuine transparent background. This is a tightly matched set of four deliberately authored key poses of the SAME animal, not four redesigns. Anatomically coherent limbs, no extra/missing/duplicated limbs.

## jump

The golden brown rabbit, pink inner ears, white chest and tail from the reference demonstrates JUMP. Keep a side profile with both visible long ears and consistent eye. Top-left: crouch low with hind legs folded, front feet touching ground. Top-right: powerful takeoff, hind legs extended backwards/down, forelegs stretching forward, body elongated diagonally upward. Bottom-left: airborne with both pairs of feet clearly off the ground, legs tucked under body. Bottom-right: landing low with forefeet ahead touching down and hind legs tucked, ears following the motion. Align all animals on the same notional baseline near the bottom of each cell; keep exact body/head size, do not stretch the entire animal to fake movement.

## fly

The white-headed brown eagle with golden beak and talons from the reference demonstrates FLY. All four poses are airborne in the SAME strict right-facing side/three-quarter profile, body horizontal, white tail to left, head to right, feet tucked beneath body. Body and head remain at exactly the SAME central position and scale in every cell. Top-left: both wings lifted high above back in an upstroke. Top-right: both wings extending horizontally out from shoulders, wing tips clearly visible. Bottom-left: both wings swept downward below belly in a downstroke, feathers spread. Bottom-right: both wings lifted halfway upward, intermediate return stroke. The wing geometry must change clearly at the shoulders and bend naturally. No standing pose, no hovering upright like a person, no detached wings, no limb duplication. Allow enough space for whole wing silhouette in every cell.

## snap

The green crocodile with yellow belly, darker green spots, back ridges and brown eye from the reference demonstrates SNAP ITS JAWS. All four poses are exactly the same strict right-facing side profile standing on all four feet, tail left and long snout right, camera and entire body fixed. The jaw hinge is behind the mouth, not at the nose. Keep the eye, head top, feet and body at exactly identical positions across cells; only the LOWER JAW rotates at its hinge, with continuous neck connection. Top-left: mouth fully closed, upper/lower snouts meet. Top-right: lower jaw slightly lowered, mouth partly open. Bottom-left: lower jaw lowered clearly, mouth wide open, tongue and rounded teeth visible. Bottom-right: mouth fully closed again, identical to top-left. Keep friendly, no aggression, no prey, no violence, no scary exaggerated teeth. No changes to face, limbs or body between poses.
