# Verb animation review — completed 2026-11-06

Historical before-state audit. Owner-authorized repairs are documented in the
[53-lesson implementation review](../verb-animation-fixes-2026-11-06/README.md).
Use that review/gallery for current behavior; this document preserves the findings.

Reviewed all **53 existing verb lessons**, plus the three native representations
of Jump, Fly and Snap. The request was to review expression and animation clarity;
this audit changes no lesson, animation, artwork, preference or progress data.
The existing local native prototype work remains separate and uncommitted.

[Browse all 53 contact sheets](index.html) · [Native preview gallery](../verb-motion-preview/index.html)

## Main recommendations

1. **Upgrade native Fly next:** use roughly 6–8 registered wing poses or separate
   articulated wings. The current four poses communicate flying, but the wing
   transitions are abrupt and the head/body shift slightly between drawings.
   Anchor the torso/head first; extra independently redrawn frames could increase
   drift. More facial expression is less useful here than clear wing mechanics.
2. **Refine native Jump after Fly:** the action already reads clearly. Add a
   transition through push-off and one landing-compression/recovery pose, aiming
   initially at about six useful poses. A brief focused look before takeoff and
   relaxed recovery could help; an exaggerated face on every frame would distract.
   The fade/reset is also visible and can be improved when the loop is polished.
3. **Keep Snap v2 as the current prototype benchmark:** eight poses, deliberate
   opening, wide hold, fast closure, brief squint and small recoil make the action
   more legible than v1. No additional frames are a priority now. Registration
   polish and child/device review remain, rather than another wholesale redraw.
4. **Prioritise genuine moving parts in older lessons:** wag the tail while the
   dog stays still; extend the horse's leg for Kick; lift and plant a foot for
   Stomp; open the mouth and change head posture for Roar/Howl/Bark. Adding more
   whole-animal wobble frames would preserve the current ambiguity.
5. **Show the action's object/result:** Dig needs displaced soil/a hole, Squirt Ink
   needs an ink cloud, Fetch needs a ball that is picked up and returned, and
   Carry needs a visible carried object. These are content/presentation gaps,
   not simply low frame counts.

Pose counts here are starting points for a small visual proof, not production
asset quotas or approval to generate hundreds of independent frames. Follow the
roadmap's stable artwork → key poses/layers → deterministic native motion direction.

## Confirmed gaps in the older HTML presentation

### Water is missing from five water lessons

Swim in the sea, Dive underwater, Squirt ink, Float on its back and Paddle on a
pond all receive the `water` scene class, but the detailed-animation branch inserts
no `.waterWave` element. Their computed background remains the sky/green-ground
gradient. The rendered contact sheets confirm the green ground and coral decoration
without the intended water layer. Repair the medium/waterline before judging
whether a swimming or diving pose is sufficient.

This is confirmed in the inspected source and desktop rendering; a fresh physical
Android rendering was not performed by this audit.

### The hanging bat is clipped

In [Sleep upside down](index.html#verb-34), the rotated bat is partly outside the
bottom of the stage throughout the sampled cycle. It has no visible supporting
branch/attachment. The transform origin and clipping need repair; more frames
would not make the missing hanging relationship clear. A good hanging pose and a
clear support point could need very little motion.

### Several lessons share generic motion instead of demonstrating their action

- Eight use the same generic bob: Use a trunk, Nibble, Chew wood, Waddle, Carry its
  baby in a pouch, Graze on grass, Clap its flippers and Carry food.
- Eight sound lessons share the same scale pulse: Meow, Moo, Oink, Quack, Buzz,
  Hoot, Hiss and Copy sounds. Bark, Roar and Howl have separate tracks, but still
  transform a fixed glyph rather than articulating a mouth/head.
- Every legacy animal is a fixed emoji with zero animated child parts. The 53
  lessons resolve to 34 CSS animation tracks. Some glyph poses already convey a
  useful concept, such as a flamingo balancing or an otter lying on its back;
  that does not establish meaningful articulation for the other actions.
- `chase mice` and `fetch a ball` miss the exact `chase` / `fetch` keys in the
  detailed map and fall back to the generic run track. Fixing dispatch alone
  would still leave the mouse/ball and the fetch return sequence absent.

### Some sound verbs need an audible model as well as expression

The lesson Listen action reads the word, explanation and example through TTS.
It does not request an authored animal-call clip or a source/imitated sound pair;
no bundled MP3/WAV/OGG/M4A animal-call assets were found. A mouth/head pose can
support Bark/Roar/Howl, but cannot alone distinguish all sound verbs. Copy sounds
particularly needs an audible source then an imitation, or a clearly supported
adult modelling interaction. This is a review recommendation, not a new audio
engine or a claim about physical voice quality.

## Per-verb review

The priorities below describe the **legacy presentation**. High means the action,
object, body part or context is missing/ambiguous; Medium means the idea has a
usable cue but needs refinement; Low means a good pose/context and restrained
motion can communicate the concept. None is a claim of physical child acceptance.
Native counterparts are assessed separately above.

| # | Verb | Priority | What to improve |
|---|---|---|---|
| 1 | Fly | Medium | Fixed airborne glyph travels; use registered wing strokes. Native four-pose version is already a better basis, with 6–8 poses/layers the next refinement. |
| 2 | Jump | Medium | Whole-glyph bounce lacks articulated legs. Native version reads well; refine push-off and landing rather than adding expression everywhere. |
| 3 | Hop | High | Frog is a head glyph, so leg push-off/landing cannot be shown. Full-body frog with crouch, extension, flight and landing. |
| 4 | Swim in the sea | High | Missing water layer and no tail/body propulsion. Establish water, then use a restrained swimming cycle. |
| 5 | Dive underwater | High | Penguin head rotates downward over green ground. Full-body tuck, entry through a visible waterline, underwater continuation. |
| 6 | Climb | High | Monkey translates beside a tree. Show alternating hand/foot grips and an actual support relationship. |
| 7 | Slither | High | A coiled snake glyph slides/tilts intact. Use an elongated body with a travelling bend; keep the head path controlled. |
| 8 | Gallop | High | Whole horse stretches and rocks; legs stay fixed. Use coherent contact, extension and recovery poses rather than body scaling. |
| 9 | Bark | High | Body rocks without a mouth change. Alert head/ear anticipation, brief jaw opening, short effort and recovery; audible model also helps. |
| 10 | Meow | High | Same scale pulse as other sounds. Small mouth opening and recognisable cat head posture, supported by a sound/model. |
| 11 | Roar | High | Lion face scales and bobs. Inhale/head lift, wider mouth, sustained exhalation and relaxed recovery; about 6–8 matched poses may help. |
| 12 | Moo | Medium | Generic pulse does not distinguish the sound. Subtle mouth/head poses and a useful audible model; many frames are unnecessary. |
| 13 | Oink | Medium | Generic pulse. Small snout/mouth movement and a matching sound/model matter more than body bounce. |
| 14 | Quack | Medium | Duck scales without bill movement. Open/close bill with a sound/model, using a short matched sequence. |
| 15 | Buzz | Medium | Bee body pulses; wings stay fixed. Light wing vibration and an audible buzz; extra facial expression has little value. |
| 16 | Hoot | Medium | Owl pulses in a night scene. Small beak/throat movement and a sound/model; keep the body calm. |
| 17 | Howl | High | Wolf head rotates without an articulated muzzle. Raise the snout, shape the mouth, hold the sound posture and recover. |
| 18 | Hiss | Medium | Scaling snake does not distinguish hissing from movement. Head/mouth/tongue cue and an audible model; avoid unnecessary full-body motion. |
| 19 | Peck | High | Chicken head rotates with no contact target. Show beak reaching a visible seed/ground target, contact and withdrawal. |
| 20 | Dig | High | Rabbit rotates; no paw strokes, displaced earth or hole. Alternate the forepaws and show an observable digging result. |
| 21 | Scratch | High | Cat rocks intact. Lift a paw, touch a clear scratching surface, stroke and recover. |
| 22 | Kick | High | Whole horse tilts; leg never extends. Weight shift, raised leg, quick extension and recovery with stable body proportions. |
| 23 | Wag its tail | High | Entire dog rotates. Keep the head/torso planted and move the tail independently; a small set of tail poses or a layer is enough. |
| 24 | Flap its wings | High | Parrot glyph stretches vertically. Animate wings at the shoulders, with torso/head stable; reuse the native wing-motion lessons. |
| 25 | Stand on one leg | Low | Rendered flamingo already has a useful one-leg cue. Use clear supporting/lifted legs in canonical art and a held balance pose; many extra frames would add little. |
| 26 | Build a dam | High | Beaver moves near floating log/water icons; no barrier is built. Place logs progressively and show the resulting barrier/water relationship. |
| 27 | Spin a web | High | Spider moves around an already complete web icon. Grow anchored strands connected to the spider's work. |
| 28 | Use a trunk | High | Elephant only bobs. Show trunk reach, grasp of a visible object, lift and placement. |
| 29 | Reach high leaves | High | Whole giraffe stretches; no leaf target at its mouth. Keep legs/body stable, articulate neck/head and show reachable leaves. |
| 30 | Walk sideways | Medium | Side-to-side travel provides a cue, but crab legs remain fixed. Add alternating contacts and a steady sideways path. |
| 31 | Squirt ink | High | Octopus tilts without emitting ink, and the water layer is absent. Show a clear ink cloud expanding from the correct source. |
| 32 | Float on its back | High | Otter glyph already suggests the right pose, but water is missing. Establish the surface/buoyancy; only gentle bobbing or a few poses are needed. |
| 33 | Copy sounds | High | Parrot scales; there is no audible source/response pair. Demonstrate an original sound followed by imitation, with listening/speaking posture cues. |
| 34 | Sleep upside down | High | Bat is clipped and lacks a visible attachment. Repair framing and show feet gripping a support with a calm hanging/sleeping pose. |
| 35 | Snap its jaws | High | Legacy glyph deforms without closing its jaw. Native eight-pose v2 already supplies the appropriate improved proof; no further frame expansion is a priority. |
| 36 | Stomp | High | Elephant's whole body rises/falls. Show a foot lift, planted contact and settling weight, with deliberate timing. |
| 37 | Nibble | High | Rabbit bobs with no food or bite changes. Show held food and several small jaw/bite movements, distinct from a large snap. |
| 38 | Chew wood | High | Beaver bobs without wood contact. Show incisors meeting a visible piece of wood and repeated small gnawing movements. |
| 39 | Waddle | High | Penguin head bobs with no feet/body gait. Full-body short alternating steps and modest side-to-side weight shifts. |
| 40 | Paddle on a pond | High | Duck translates/rocks over green ground. Waterline plus alternating feet below the surface and restrained wake. |
| 41 | Curl into a ball | High | Hedgehog only shrinks. Tuck head/feet and change into a genuinely round curled silhouette. |
| 42 | Carry its baby in a pouch | High | Kangaroo bobs; baby/pouch relationship is not clearly demonstrated. Make the joey visibly seated in the pouch; a steady held pose may need little motion. |
| 43 | Pounce | High | Whole tiger translates/scales. Crouch, focused anticipation, leg extension, forward flight and planted landing at a clear landing spot. |
| 44 | Graze on grass | High | Cow bobs without lowering its head to grass. Head-down contact, small chewing motion and visible grass. |
| 45 | Clap its flippers | High | Seal bobs; flippers never meet. Show separated flippers, inward movement, contact and separation. |
| 46 | Chase mice | High | Falls back to generic running, with no mouse. Show a mouse ahead and a following path, plus actual leg motion. |
| 47 | Fetch a ball | High | Falls back to generic running; no ball, pickup or return. Show outward travel, pickup and return carrying the ball. |
| 48 | Crawl slowly | Low | Slow snail translation is a usable starting cue. Prefer a steady foot/glide path and gentle feeler motion over a large frame set. |
| 49 | Flutter | Medium | Same transform track as the perched parrot; wings do not articulate. Use light, quick butterfly wing changes and a small irregular flight path. |
| 50 | Carry food | High | Ant bobs without food. Make the held food visible and keep its grip/position stable while moving. |
| 51 | Breathe fire | Medium | Flame icon pulses separately from a fixed dragon. Anchor emission to the mouth and add inhale/exhale/recovery poses; retain the story/fantasy explanation. |
| 52 | Stomp like a dinosaur | High | Dinosaur body bounces instead of showing planted foot action. Prove a clear lift/contact/settle with stable references; avoid expanding into a complex walk cycle first. |
| 53 | Stretch its long neck | High | Entire sauropod stretches vertically, including body/legs. Animate neck articulation with torso and foot positions fixed. |

## Suggested implementation order

First refine the **native eagle**, then the **native rabbit landing/push-off**.
For the next native examples, **tail wagging, a foot stomp/kick and a mouth-led
roar/howl** are useful small studies of independent parts and expression.
Keep **slithering and digging** high in the migration queue, with the right
source poses and visible result. Repair water/framing/dispatch gaps as explicit
correctness work. Build/fetch/carry/web demonstrations need a short object/action
sequence rather than a larger pile of unrelated animal frames.

Preserve the existing lessons until native equivalents retain the current bilingual
content and learning flow. This pass supplies the review and priorities; it performs
no migration or artwork replacement.

## Evidence and limits

- Source baseline: `a990f34`, with the separate local debug preview. Production
  `app.js` / `index.html` were read, not modified by this review.
- `capture.cjs` rendered all 53 existing lessons at 0/14/28/42/56/70/84/98% of
  their real CSS cycle: **424 browser stage samples**, assembled into **53 contact
  sheets**. All 53 English/German title and animation mappings were checked.
  The completed capture run recorded **zero page errors**.
- [inventory.json](inventory.json) records classes, actual animation names,
  duration/easing, scene props, computed backgrounds and glyphs. The source mapping
  is in [app.js](../../app/src/main/assets/app.js), and keyframes/scene CSS in
  [index.html](../../app/src/main/assets/index.html).
- Native Jump/Fly/Snap v2 were reviewed from the genuine emulator recordings and
  sampled contact frames, plus their authored poses/timelines. This audit did not
  run a fresh device installation or replace the previous prototype test evidence.
- Desktop Chromium uses platform emoji; glyph appearance and exact clipping can
  differ on Android. The browser bat finding must be checked on the target device.
  CSS whole-glyph transforms, missing water elements/props and dispatch gaps are
  source-confirmed independently of that font difference.
- This review does not establish audible speech/call quality, child comprehension,
  physical S24/Fire acceptance or actual TalkBack usability. No new unit/emulator
  suite was needed for documentation and a read-only capture harness.
