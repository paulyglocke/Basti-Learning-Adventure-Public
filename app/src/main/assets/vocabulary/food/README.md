# Food & Drink vocabulary artwork

Eight original illustrations generated with the built-in image generation tool,
using `school_actions/board/object_book.png` and the generated apple as style
references. Originals remain in the Codex generated-images library; bundled
copies are 512px JPEGs at quality 88, approximately 524 KB total.

Files: `apple.jpg`, `banana.jpg`, `bread.jpg`, `water.jpg`, `milk.jpg`,
`cheese.jpg`, `orange.jpg`, `sandwich.jpg`.

## Prompt set

Shared prompt: “Use case: illustration-story. Asset type: square vocabulary
picture for a children's learning app. Scene/backdrop: a simple warm cream
textured paper background, matching both attached references. Style/medium:
polished, realistic children's educational object illustration with tactile
subtle grain, same soft finish and tonal warmth as the references.
Composition/framing: a single object centered, large and fully visible with
comfortable margins, square image. Lighting/mood: gentle diffuse daylight and
a soft natural contact shadow. Constraints: show only the requested food or
drink item; no text, labels, letters, logos, extra objects or watermark.”

Distinct subjects:

- Apple: one fresh red apple with a short brown stem and one small green leaf.
- Banana: one ripe bright yellow banana, gently curved, with tiny brown tips.
- Bread: one small golden brown oval loaf of crusty bread, scoring marks on top.
- Water: one clear drinking glass filled with clean water, visible water line,
  no ice or garnish.
- Milk: one plain white ceramic glass filled with milk, no label or packaging.
- Cheese: one triangular yellow cheese wedge with a few small round holes,
  no wrapper.
- Orange: one whole vivid orange fruit with a small green leaf and dimpled peel.
- Sandwich: one triangular sandwich half with visible cheese and lettuce filling,
  no plate.

Runtime semantic IDs and bilingual wording live in `VocabularyContent`; asset
resolution lives in `VocabularyArtwork`. Text is rendered by the app.
