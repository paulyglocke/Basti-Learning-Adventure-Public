# Art Direction & Asset Rules

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
