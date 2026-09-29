#!/usr/bin/env python3
"""Read-only conservative raster audit. Requires Pillow and NumPy (authoring only).

Classifications are candidates until visually reviewed. Transparent RGB is measured, but fully transparent bands cannot fail. Strong signatures require an interior near-white band and coloured pixels
on both sides; pale scenery without that signature only warns. No image writes.
"""
import argparse
import csv
import hashlib
import json
from collections import Counter
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFont, ImageOps

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'app/src/main/assets'
FAMILIES = ('Animals', 'Prepositions', 'SceneDescriptions', 'Seasons', 'Wilma')
EDGES = ('top', 'bottom', 'left', 'right')
DEPTH = 100  # Includes confirmed classroom dividers up to 86 px from the edge.

# Authoring contract, not learning/session identity. Largest reviewed edge crop
# removes 65/1448 = 4.49% of width. Keep at least 640x480 after 2x runtime sampling.
PREPOSITIONS_IMAGE_CONTRACT = {
    'format': 'PNG', 'modes': ['RGB', 'RGBA'],
    'minimum_width': 1280, 'minimum_height': 960,
    'aspect_ratio': {'width': 4, 'height': 3, 'relative_tolerance': 0.05},
}


def prepositions_image_error(image):
    if image.format != 'PNG' or image.mode not in ('RGB', 'RGBA'):
        return 'expected RGB/RGBA PNG'
    if image.width < 1280 or image.height < 960:
        return 'expected minimum Prepositions dimensions 1280x960'
    # Integer bounds avoid floating-point ambiguity at exactly +/-5% of 4:3.
    if not 19 * image.height <= 15 * image.width <= 21 * image.height:
        return 'expected Prepositions aspect ratio 4:3 +/-5%'
    return ''


def inventory(root=ASSETS):
    return sorted(p for family in FAMILIES for p in (root / family).rglob('*')
                  if p.suffix.lower() in ('.png', '.jpg', '.jpeg', '.webp')
                  and not {'reference', 'prompts'} & set(p.parts))


def bands(indices):
    groups = []
    for index in indices:
        index = int(index)
        if groups and groups[-1][1] == index:
            groups[-1][1] = index + 1
        else:
            groups.append([index, index + 1])
    return groups


def analyse(image):
    rgba = np.asarray(image.convert('RGBA'))
    white = (rgba[:, :, :3].min(axis=2) >= 240) & (rgba[:, :, 3] >= 240)
    coloured = (rgba[:, :, :3].min(axis=2) < 235) & (rgba[:, :, 3] >= 240)
    result = {}
    signatures = []
    for edge in EDGES:
        pixels = {'top': rgba[:DEPTH], 'bottom': rgba[-DEPTH:][::-1],
                  'left': rgba[:, :DEPTH].transpose(1, 0, 2),
                  'right': rgba[:, -DEPTH:][:, ::-1].transpose(1, 0, 2)}[edge]
        whites = {'top': white[:DEPTH], 'bottom': white[-DEPTH:][::-1],
                  'left': white[:, :DEPTH].T, 'right': white[:, -DEPTH:][:, ::-1].T}[edge]
        colours = {'top': coloured[:DEPTH], 'bottom': coloured[-DEPTH:][::-1],
                   'left': coloured[:, :DEPTH].T, 'right': coloured[:, -DEPTH:][:, ::-1].T}[edge]
        fractions = whites.mean(axis=1)
        raw_white = pixels[:, :, :3].min(axis=2) >= 240
        raw_fractions = raw_white.mean(axis=1)
        # Hidden RGB is measured separately; only bands with visible alpha can fail.
        full = raw_fractions >= .97
        groups = bands(np.flatnonzero(full))
        leading = groups[0][1] if groups and groups[0][0] == 0 else 0
        result[edge + '_leading_white_px'] = leading
        result[edge + '_longest_white_run_px'] = max((b-a for a,b in groups), default=0)
        result[edge + '_rgba_mean'] = json.dumps(np.round(pixels[0].mean(axis=0), 2).tolist())
        result[edge + '_white_fraction'] = round(float(fractions[0]), 6)
        result[edge + '_white_bands'] = json.dumps(groups)
        result[edge + '_raw_rgb_white_fraction'] = round(float(raw_fractions[0]), 6)
        for start, end in groups:
            # 'Beyond' is the narrow outer strip, not the intended inner panel.
            raw_coloured = pixels[:, :, :3].min(axis=2) < 235
            outer = float(raw_coloured[:start].mean()) if start else 0
            inner = float(raw_coloured[end:min(end+8, len(colours))].mean()) if end < len(colours) else 0
            visible_band = int(pixels[start:end, :, 3].max()) >= 32
            if start > 0 and end < DEPTH and outer >= .08 and inner >= .08 and visible_band:
                signatures.append(dict(edge=edge, start=start, end=end,
                                       outer_coloured_fraction=round(outer, 4), inner_coloured_fraction=round(inner, 4)))
    result['separator_signatures'] = json.dumps(signatures)
    result['classification'] = ('CONFIRMED_SEPARATOR_SLIVER' if signatures else
        'AMBIGUOUS_REVIEW_REQUIRED' if any(result[e+'_longest_white_run_px'] for e in EDGES) else 'CLEAN')
    return result


def scan(paths):
    rows = []
    seen = set()
    for path in paths:
        relative = path.relative_to(ROOT).as_posix()
        row = dict(path=relative, width='', height='', mode='', alpha_present='', sha256='', error='')
        try:
            if relative in seen:
                raise ValueError('duplicate path')
            seen.add(relative)
            data = path.read_bytes()
            row['sha256'] = hashlib.sha256(data).hexdigest()
            with Image.open(path) as image:
                image.load()
                row.update(width=image.width, height=image.height, mode=image.mode,
                           alpha_present='A' in image.getbands() or 'transparency' in image.info)
                row.update(analyse(image))
                if 'Prepositions/scenes/' in relative:
                    row['error'] = prepositions_image_error(image)
        except Exception as error:
            row.update(error=str(error), classification='AMBIGUOUS_REVIEW_REQUIRED')
        rows.append(row)
    return rows


def write_csv(rows, path):
    path.parent.mkdir(parents=True, exist_ok=True)
    fields = list(dict.fromkeys(key for row in rows for key in row))
    with path.open('w', newline='') as stream:
        writer = csv.DictWriter(stream, fieldnames=fields)
        writer.writeheader()
        writer.writerows(rows)


def sheets(rows, directory, prefix):
    directory.mkdir(parents=True, exist_ok=True)
    font = ImageFont.load_default(size=15)
    for page in range(0, len(rows), 6):
        canvas = Image.new('RGB', (1440, 1140), '#dddddd')
        draw = ImageDraw.Draw(canvas)
        for slot, row in enumerate(rows[page:page+6]):
            x, y = (slot % 3)*480, (slot//3)*570
            with Image.open(ROOT / row['path']) as source:
                preview = ImageOps.contain(source.convert('RGBA'), (460, 470))
                canvas.paste(preview, (x+10, y+65), preview)
            name = Path(row['path']).name
            draw.text((x+10, y+5), row['path'].split('/')[4], font=font, fill='black')
            # Long preposition filenames use two readable lines.
            for line, offset in ((name[:49], 24), (name[49:], 42)):
                draw.text((x+10, y+offset), line, font=font, fill='black')
            draw.text((x+10, y+545), row['classification'], font=font, fill='black')
        canvas.save(directory / f'{prefix}_{page//6+1:02d}.png')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--contact-dir', type=Path)
    parser.add_argument('--review', type=Path, help='Reviewed decisions keyed by path; raw measurements retained')
    args = parser.parse_args()
    rows = scan(inventory())
    if args.review:
        decisions = json.loads(args.review.read_text())
        for row in rows:
            row['detected_classification'] = row['classification']
            decision = decisions.get(row['path'])
            if decision:
                # A decision is valid only for the reviewed bytes.
                if decision['sha256'] == row['sha256']:
                    row['classification'] = decision['classification']
                    row['review_reason'] = decision['reason']
    write_csv(rows, args.output)
    if args.contact_dir:
        sheets([r for r in rows if r['classification'] != 'CLEAN'], args.contact_dir, args.output.stem)
    counts = Counter(row['classification'] for row in rows)
    errors = [r['path'] for r in rows if r['error']]
    print(json.dumps(dict(total=len(rows), counts=dict(counts), errors=errors), indent=2))
    return 1 if errors or counts['CONFIRMED_SEPARATOR_SLIVER'] else 0

if __name__ == '__main__':
    raise SystemExit(main())
