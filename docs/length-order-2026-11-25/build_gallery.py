#!/usr/bin/env python3
"""Index genuine device captures without altering image bytes."""
from pathlib import Path
from html import escape

root=Path(__file__).resolve().parent
groups={'Production phone routes':[], 'Phone component layouts':[], 'Tablet component layouts':[]}
for p in sorted((root/'captures').glob('length-order-*.png')):
    component='-layout-' in p.name
    group=('Tablet component layouts' if '-tablet-' in p.name else 'Phone component layouts') if component else 'Production phone routes'
    caption=('Component layout test, without the production shell. Controls can be scrolled while all strips stay visible; keyboard focus may be present.' if component else 'Actual production route on the API 35 phone emulator. All strips stay visible while controls scroll.')
    title=p.stem.replace('length-order-','',1).replace('-',' ')
    groups[group].append(f'<figure><h3>{escape(title)}</h3><a href="captures/{p.name}"><img loading="lazy" src="captures/{p.name}" alt="{escape(title)}"></a><figcaption>{caption}</figcaption></figure>')
html='''<!doctype html>
<html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Basti — Length ordering review</title>
<style>body{font-family:system-ui,sans-serif;background:#faf8ef;color:#203e4a;max-width:1400px;margin:auto;padding:24px}p{line-height:1.6;max-width:850px}a{color:#286a50}.gallery{display:grid;grid-template-columns:repeat(auto-fit,minmax(290px,1fr));gap:20px}figure{background:white;border:1px solid #d5ddce;border-radius:16px;margin:0;padding:16px}h3{font-size:1.1rem}img{width:100%;height:520px;object-fit:contain}figcaption{font-size:.9rem;line-height:1.5}</style>
<h1>Order by Length — Nach Länge ordnen</h1>
<p>Arrange three distinct strips shortest-to-longest. Tap a strip on the left to append it on the right; tap a placed strip to return it. Every cell uses the same scale. Undo, explicit whole-order Check, bilingual narration, saved rounds and shared balloons.</p>
<p>Genuine production and component screenshots are labelled separately. Tablet views use emulator display overrides; physical-device and child acceptance remain separate. Strips are drawn by the native Canvas renderer; captured PNG bytes are unaltered.</p>
<p><a href="README.md">Implementation and validation</a> · <a href="evidence/validation.json">Evidence summary</a></p>
'''
html+=''.join(f'<section><h2>{escape(title)} ({len(cards)})</h2><div class="gallery">'+''.join(cards)+'</div></section>' for title,cards in groups.items() if cards)
(root/'index.html').write_text(html+'</html>\n')
print(f'Indexed {sum(map(len,groups.values()))} genuine captures')
