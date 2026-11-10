# Art Direction & Asset Rules

## Memory Pairs starter (2026-11-09)

Animal cards reuse the six canonical Vocabulary PNGs. Card backs, dot groups and
the distinct Home matching thumbnail are native Canvas illustrations. Hidden
faces do not leak animal identity through decorative differences. Matching is
reinforced by readable Pair found text and border treatment, not colour alone.
No generated replacement animals or extra raster assets are needed for this slice.


## Production rule
Avoid emoji as final production artwork.
Emoji may remain temporarily as migration placeholders only.

## Core recurring characters
- T-Rex
- Velociraptor
- Pteranodon
- Mosasaurus
- dragon
- snake
- Komodo dragon
- crocodile
- shark
- whale

## Canonical reusable animal library

Create one coherent set of original, language-neutral recurring animal/creature illustrations before generating isolated replacements activity by activity.

Initial reusable subjects:
- T-Rex
- Velociraptor
- Pteranodon
- Mosasaurus
- dragon
- snake
- Komodo dragon
- crocodile
- shark
- whale
- dinosaur/general dinosaur where a species-specific subject is not intended
- fish
- horse where retained by the reviewed Vocabulary starter content

A canonical illustration should be reusable across Vocabulary, Prepositions, completion rewards, Follow the Instructions, Memory/matching, Compare & Discover, Discovery Book and future games where the same semantic subject is appropriate.

Create activity-specific poses or action frames when they teach meaning—such as slithering, flying, snapping or swimming—but do not independently redesign the same animal for every screen. Stable semantic content IDs remain separate from asset filenames so artwork can improve without changing learning identity.

The [verb motion repair](docs/verb-animation-fixes-2026-11-06/README.md) uses
39 reusable animal pose atlases across all 53 existing lessons, following owner
approval of the audit repairs. Related actions share one animal identity; body
parts articulate and scene props show contact/results. Native Jump/Fly/Snap
previews consume the same production WebP assets. Historical four-pose PNGs stay
in debug assets for comparison. The full native Verb Explorer/Animal Actions renderer now reuses these same 39 atlases, scene props and four authored teaching steps. Build 5/10 use native illustrated eggs/nests rather than emoji. Physical/child acceptance remains separate.

Prioritise replacement of explicit temporary glyph/emoji placeholders before producing decorative variants.

## Recurring learning mascot — Wilma
Wilma is the weekday caterpillar used to reinforce the Kita's existing day-colour routine.

Asset contract:
- source assets: `app/src/main/assets/Wilma/`
- language-neutral artwork; never bake Monday/Montag or other labels into the PNGs
- one decorative head, seven equal day body segments, then a decorative tail
- segment order is fixed: green, red, yellow, blue, purple, orange, pink
- semantic mapping is Monday/Montag, Tuesday/Dienstag, Wednesday/Mittwoch, Thursday/Donnerstag, Friday/Freitag, Saturday/Samstag, Sunday/Sonntag
- keep the committed Wilma artwork visually consistent when adding expressions or replacement assets
- prefer assembling/highlighting segments in UI code so one art set serves English and German
- colour is supplementary: visible labels, position and spoken names must also identify the day

### Wilma selectable day cues (2026-09-24)

A physical child-use report found generic day choices confusing. Wilma's selectable strip labels (Explore/Find/Before–After) and ordering buttons now reinforce the existing day-colour routine. `ui/wilma/WilmaDayColours` resolves each weekday's canonical `colourCue` from CoreContent; it does not maintain a second day mapping. No RGB palette previously existed in code, so UI swatches were sampled from the committed segment PNGs (representative most-frequent opaque saturated pixels, four-pixel sampling), without changing images:

| Cue | UI swatch | Label |
| --- | --- | --- |
| green | #8ECA3F | black |
| red | #FB2032 | black |
| yellow | #FDDA0B | black |
| blue | #06B5FA | black |
| purple | #9E40D6 | white |
| orange | #FC7507 | black |
| pink | #FC559E | black |

This is a small Wilma UI palette, not an app-wide colour system. Normal labels choose the higher-contrast black/white foreground (at least 4.5:1; sampled active minimum 5.05:1). Disabled containers blend 25% cue over the theme surface, retaining day association without looking like active choices; text contrast is recalculated. Existing Material/selectable press/focus behavior, disabled semantics and selection border remain. Written/spoken weekday names and semantic IDs remain authoritative: colour neither defines correctness nor replaces the label. Placed/read-only labels, head/tail, per-day Listen controls, learning state and auto-follow are unchanged. Physical recognition benefit and device accessibility still require a child/device review.

## Supporting packs
- school objects
- home/everyday objects
- weather
- shapes
- feelings
- habitats
- landscapes
- classroom
- rewards
- traffic-light/self-regulation graphics
- treasure/gems/eggs
- bridges/rocks/trees

## Style
- colourful
- friendly
- polished
- not babyish
- consistent proportions and line style
- coherent palette
- readable silhouettes
- phone and tablet friendly

## Games
Native 2D/2.5D:
- layered scenes
- parallax
- sprites
- shadows
- particles
- smooth Compose animation

Avoid heavy 3D engines and video-heavy assets.

## Formats
- WebP for static illustrations
- vectors for simple icons/shapes
- layered bitmaps or sprite sheets for animation

## Accessibility
- meaningful images get semantics/content descriptions where appropriate
- do not rely on colour alone
- strong contrast
- reduced-motion support

## Seasons / Jahreszeiten asset set

The canonical season illustrations are stored under:

- `app/src/main/assets/Seasons/blossoming_lakeside_spring_meadow.png`
- `app/src/main/assets/Seasons/sunny_summer_lakeside_meadow.png`
- `app/src/main/assets/Seasons/autumn_tree_by_the_lakeside.png`
- `app/src/main/assets/Seasons/snowy_lakeside_meadow_with_bare_tree.png`

They intentionally use the same lakeside/tree composition so seasonal change is easy to compare.

Recognition requirements:

- **Spring:** sparse/fresh new green foliage plus clearly visible flowers/blossom.
- **Summer:** dense, mature, full green canopy and lush vegetation.
- **Autumn:** strong orange/red/yellow foliage and visible falling leaves.
- **Winter:** bare branches, snow/ice and no foliage.

Spring and summer must remain distinguishable at small app-card sizes. Do not depend on subtle colour or brightness differences alone.

The images remain language-neutral. Season names, descriptions, accessibility text and speech are supplied by app content/code.

Production assets may later be converted to appropriately sized high-quality WebP after visual comparison on the target Samsung and Fire devices.
