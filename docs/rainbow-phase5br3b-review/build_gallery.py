#!/usr/bin/env python3
"""Build the local HTML gallery and verified PNG metadata for this review."""

import html
import json
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parent
SHOTS = ROOT / "screenshots"

GROUPS = {
    "Colours navigation": ["chooser-en-phone.png", "chooser-de-phone.png", "options-return-de.png",
                           "options-return-sound-off.png", "options-return-chooser-de.png"],
    "Rainbow Explore": ["explore-en-phone-initial.png", "explore-de-phone-initial.png",
                        "explore-en-red-selected.png", "explore-en-orange-selected.png",
                        "explore-en-indigo-selected.png", "explore-en-violet-selected.png",
                        "explore-de-orange-selected.png"],
    "Rainbow Order — Starter": ["starter-en-initial.png", "starter-en-partial.png", "starter-en-completed.png",
                                "order-starter-en-initial-phone.png",
                                "starter-de-initial.png", "starter-de-partial.png", "starter-de-completed.png",
                                "starter-de-wrong-feedback.png", "starter-de-completed-retry.png"],
    "Rainbow Order — Full": ["full-en-initial.png", "full-en-partial.png", "full-en-wrong-feedback.png",
                             "full-en-help.png", "full-en-completed.png", "full-de-initial.png",
                             "full-de-partial.png", "full-de-wrong-feedback.png", "full-de-help.png",
                             "full-de-completed.png"],
    "Bilingual comparison": ["chooser-en-phone.png", "chooser-de-phone.png", "explore-en-phone-initial.png",
                             "explore-de-phone-initial.png", "starter-en-initial.png", "starter-de-initial.png",
                             "full-en-initial.png", "full-de-initial.png"],
    "Responsive layouts": ["explore-phone-landscape.png", "explore-short-landscape.png",
                           "explore-tablet-portrait-emulator.png", "explore-tablet-landscape-emulator.png",
                           "explore-phone-150-text.png", "explore-german-200-text.png",
                           "order-phone-landscape.png", "order-short-landscape-before-compact-fix.png",
                           "order-short-landscape.png", "order-tablet-portrait-emulator.png",
                           "order-tablet-landscape-emulator.png", "order-phone-150-text.png",
                           "order-german-200-text.png"],
    "Restoration and Options return": ["starter-de-before-restore.png", "starter-de-after-restore.png",
                                       "starter-completed-after-restore.png", "full-en-before-restore.png",
                                       "full-en-after-restore.png", "options-return-de.png",
                                       "options-return-sound-off.png", "options-return-chooser-de.png"],
}

OVERRIDES = {
    "order-starter-en-initial-phone.png": "Rainbow Order — Starter; English initial state.",
    "order-short-landscape-before-compact-fix.png": "Rainbow Order — Full; English; before the compact-height fix, instruction clipped at the bottom of the first viewport.",
    "order-short-landscape.png": "Rainbow Order — Full; English; fresh initial state after the compact-height fix, with the complete instruction and both level choices in the first viewport.",
    "starter-de-initial.png": "Rainbow Order — Starter; German; fresh initial state with seven-colour model, four empty destinations and shuffled Starter pieces.",
    "starter-de-partial.png": "Rainbow Order — Starter; German; partial state with Red placed and Yellow, Blue and Violet remaining.",
    "starter-de-before-restore.png": "Rainbow Order — Starter; German; one Red placement immediately before force-stop and relaunch.",
    "starter-de-after-restore.png": "Rainbow Order — Starter; German; the same Red placement after force-stop, relaunch and reopening the route.",
    "full-en-before-restore.png": "Rainbow Order — Full; English; Red and Orange placed immediately before force-stop and relaunch.",
    "full-en-after-restore.png": "Rainbow Order — Full; English; the same two placements after force-stop, relaunch and reopening the route.",
}


def metadata(filename: str) -> dict:
    path = SHOTS / filename
    data = path.read_bytes()
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError(f"Not a PNG: {path}")
    width, height = struct.unpack(">II", data[16:24])
    lower = filename.lower()
    language = "German" if ("-de-" in lower or lower.endswith("-de.png") or "german" in lower
                             or filename in {"starter-completed-after-restore.png", "options-return-sound-off.png"}) else "English"
    if "200-text" in lower:
        scale = 2.0
    elif "150-text" in lower:
        scale = 1.5
    else:
        scale = 1.0
    if "tablet-portrait" in lower:
        viewport = f"tablet portrait emulator viewport, {width}×{height}px (not physical tablet testing)"
    elif "tablet-landscape" in lower:
        viewport = f"tablet landscape emulator viewport, {width}×{height}px (not physical tablet testing)"
    elif "short-landscape" in lower:
        viewport = f"short landscape emulator viewport, {width}×{height}px"
    elif width > height:
        viewport = f"phone landscape emulator viewport, {width}×{height}px"
    else:
        viewport = f"phone portrait emulator viewport, {width}×{height}px"
    density = 280 if "tablet" in lower else 420
    width_dp = round(width * 160 / density)
    height_dp = round(height * 160 / density)
    viewport += f", {width_dp}×{height_dp}dp at {density}dpi"
    state = "initial"
    for token, label in [("partial", "partial"), ("completed", "completed"), ("help", "Help"),
                         ("wrong-feedback", "wrong-answer feedback"), ("restore", "restoration"),
                         ("selected", "selected colour"), ("150-text", "responsive 1.5× text"),
                         ("200-text", "responsive 2× text"), ("landscape", "landscape"),
                         ("tablet", "tablet-sized emulator viewport"), ("sound-off", "Sound Off selected"),
                         ("chooser", "chooser / return")]:
        if token in lower:
            state = label
    screen = ("Colours chooser" if "chooser" in lower else
              "Rainbow Explore" if lower.startswith("explore") else
              "Rainbow Order — Starter" if lower.startswith("starter") or "order-starter" in lower else
              "Rainbow Order — Full" if lower.startswith(("full", "order-")) else "Options")
    caption = (f"{OVERRIDES[filename]} Viewport: {viewport}; font scale {scale:g}×."
               if filename in OVERRIDES else
               f"{screen}; {language}; {state} state, {viewport}, {scale:g}× font scale.")
    if "explore-en-phone-initial" == lower.removesuffix(".png"):
        caption = f"Rainbow Explore; English initial state, {viewport}, 1× font scale. Seven conventional curved bands are visible in Red-to-Violet order; the selected Red has both a marker and a matching outlined colour control."
    return {"file": f"screenshots/{filename}", "screen": screen, "language": language,
            "state": state, "viewport": viewport, "fontScale": scale,
            "width": width, "height": height, "densityDpi": density,
            "widthDp": width_dp, "heightDp": height_dp, "caption": caption}


all_names = sorted({name for names in GROUPS.values() for name in names})
manifest = [metadata(name) for name in all_names]
(ROOT / "gallery-manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n")

subsets = {
    "screenshots.json": manifest,
    "screenshots-landscape.json": [item for item in manifest if "landscape" in item["viewport"]],
    "explore-responsive.json": [item for item in manifest if item["screen"] == "Rainbow Explore"],
    "starter-en.json": [item for item in manifest if item["screen"] == "Rainbow Order — Starter" and item["language"] == "English"],
    "short-order.json": [item for item in manifest if item["screen"] == "Rainbow Order — Full" and "short landscape" in item["viewport"]],
}
for filename, items in subsets.items():
    (ROOT / filename).write_text(json.dumps(items, ensure_ascii=False, indent=2) + "\n")

sections = []
for heading, names in GROUPS.items():
    figures = []
    for name in names:
        item = metadata(name)
        figures.append(
            f'<figure><a href="{html.escape(item["file"])}"><img loading="lazy" '
            f'src="{html.escape(item["file"])}" alt="{html.escape(item["caption"])}"></a>'
            f'<figcaption>{html.escape(item["caption"])}</figcaption></figure>')
    sections.append(f'<section><h2>{html.escape(heading)}</h2><div class="grid">{"".join(figures)}</div></section>')

page = """<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Rainbow Phase 5B-R3B visual review</title>
<style>
:root{font-family:system-ui,sans-serif;color:#20202a;background:#faf7ef}body{max-width:1440px;margin:auto;padding:1rem}
h1{margin-bottom:.25rem}header p{max-width:75ch;color:#394550}section{margin:2rem 0}h2{border-bottom:2px solid #d8c9ef;padding-bottom:.4rem}
.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(min(100%,300px),1fr));gap:1rem}
figure{margin:0;background:white;border:1px solid #b7b3ad;border-radius:12px;overflow:hidden;display:flex;flex-direction:column}
figure a{display:block;background:#eee;text-align:center}img{display:block;width:100%;height:auto;max-height:760px;object-fit:contain}
figcaption{padding:.7rem;line-height:1.4;font-size:.94rem}small{color:#4b5563}
</style></head><body><header><h1>Rainbow visual acceptance gallery</h1>
<p>49 genuine API 35 emulator screenshots, captured from rendered app screens with ADB screencap. Tablet-sized views are emulator display overrides, not physical tablet tests. Each image links to its original PNG.</p>
<p>Responsive checks include phone portrait, phone landscape, short landscape, tablet-sized portrait/landscape, English 1.5× text and German 2× text. See <a href="REVIEW.md">the review report</a> for interaction, accessibility and validation findings.</p></header>
""" + "\n".join(sections) + "\n</body></html>\n"
(ROOT / "gallery.html").write_text(page)
print(f"Wrote gallery.html and gallery-manifest.json for {len(all_names)} distinct genuine PNGs")
