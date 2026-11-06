# Shared verb pose artwork

39 original animal atlases support the existing 53 bilingual verb demonstrations.
Each animal is reused across its related actions. The native debug Jump/Fly/Snap
preview reads these same main assets; the other production lessons still use the
existing WebView route.

`{animal}.webp` is an alpha-preserving, lossless atlas with 384px square cells.
`{animal}-thumb.webp` is its 192px first-pose library thumbnail. Grids and authored
frame sequences live in `../verb-motion-content.js`; lesson words and learning
state remain in the original catalogue/session adapter. No image is downloaded
or generated at runtime.

Artwork was generated/edited with the **built-in image generation tool**. The
[prompt set, references, review and provenance](../../../../../docs/verb-animation-fixes-2026-11-06/README.md)
record each animal. Crocodile reuses the approved eight-pose Snap v2. Older
four-pose prototype PNGs remain under debug assets for comparison.

`tools/prepare_verb_atlas.py` mechanically fits each source grid cell without
stretching anatomy, uses one common alpha-bounds crop for all poses to preserve
registration, supplies transparent safe margins/a shared ground baseline, and
encodes WebP with FFmpeg/cwebp. It does not paint, mask or synthesize pixels.
Original generated PNGs remain in Codex; all app-consumed assets are local here.
