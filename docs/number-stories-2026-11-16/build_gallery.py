#!/usr/bin/env python3
"""Offline index of actual Android screenshots; no mockups or generated app images."""
from pathlib import Path
from html import escape
root=Path(__file__).resolve().parent
groups={'Production phone routes':[], 'Phone component layouts':[], 'Tablet component layouts':[]}
for p in sorted((root/'captures').glob('number-stories-*.png')):
    component='-layout-' in p.name
    group=('Tablet component layouts' if '-tablet-' in p.name else 'Phone component layouts') if component else 'Production phone routes'
    caption=('Component layout test on an emulator. The construction workspace is scrolled; the starting group remains fixed. Keyboard focus may be visible.' if component else 'Actual production route on the API 35 phone emulator. The workspace may be scrolled; the starting group stays visible during construction.')
    title=p.stem.replace('number-stories-','',1).replace('-',' ')
    groups[group].append(f'<figure><h3>{escape(title)}</h3><a href="captures/{p.name}"><img loading="lazy" src="captures/{p.name}" alt="{escape(title)}"></a><figcaption>{caption}</figcaption></figure>')
(root/'index.html').write_text('''<!doctype html>
<html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Basti — Number Stories review</title>
<style>body{font-family:system-ui,sans-serif;background:#faf8ef;color:#203e4a;max-width:1400px;margin:auto;padding:24px}p{line-height:1.6;max-width:850px}a{color:#286a50}.gallery{display:grid;grid-template-columns:repeat(auto-fit,minmax(290px,1fr));gap:20px}figure{background:white;border:1px solid #d5ddce;border-radius:16px;margin:0;padding:16px}h3{font-size:1.1rem}img{width:100%;height:520px;object-fit:contain}figcaption{font-size:.9rem;line-height:1.5}</style>
<h1>Number Stories</h1><p>Build answers to addition and taking-away stories within 5/10, by the pond and at a picnic. Visible starting groups, reversible moves/Undo, explicit Check and zero, bilingual audio, saved rounds and shared balloons.</p>
<p>Genuine production-route and component screenshots are labelled separately. Tablet views use emulator overrides; physical-device and child-use acceptance remain separate.</p>
<p><a href="README.md">Implementation and validation</a> · <a href="evidence/validation.json">Evidence summary</a></p>
'''+''.join(f'<section><h2>{escape(title)} ({len(cards)})</h2><div class="gallery">'+''.join(cards)+'</div></section>' for title,cards in groups.items() if cards)+'</html>\n')
print(f'Indexed {sum(map(len,groups.values()))} genuine captures')
