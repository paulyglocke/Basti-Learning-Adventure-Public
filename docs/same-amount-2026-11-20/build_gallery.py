#!/usr/bin/env python3
"""Index genuine device captures without altering image bytes."""
from pathlib import Path
from html import escape

root=Path(__file__).resolve().parent
groups={'Production phone routes':[], 'Phone component layouts':[], 'Tablet component layouts':[]}
for p in sorted((root/'captures').glob('conservation-*.png')):
    component='-layout-' in p.name
    group=('Tablet component layouts' if '-tablet-' in p.name else 'Phone component layouts') if component else 'Production phone routes'
    caption=('Component layout test, without the production shell. The workspace can be scrolled while Before stays visible; keyboard focus may be present.' if component else 'Actual production route on the API 35 phone emulator. Before stays visible while the workspace scrolls.')
    title=p.stem.replace('conservation-','',1).replace('-',' ')
    groups[group].append(f'<figure><h3>{escape(title)}</h3><a href="captures/{p.name}"><img loading="lazy" src="captures/{p.name}" alt="{escape(title)}"></a><figcaption>{caption}</figcaption></figure>')
html='''<!doctype html>
<html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Basti — Same Amount review</title>
<style>body{font-family:system-ui,sans-serif;background:#faf8ef;color:#203e4a;max-width:1400px;margin:auto;padding:24px}p{line-height:1.6;max-width:850px}a{color:#286a50}.gallery{display:grid;grid-template-columns:repeat(auto-fit,minmax(290px,1fr));gap:20px}figure{background:white;border:1px solid #d5ddce;border-radius:16px;margin:0;padding:16px}h3{font-size:1.1rem}img{width:100%;height:520px;object-fit:contain}figcaption{font-size:.9rem;line-height:1.5}</style>
<h1>Same Amount / Gleich viele</h1>
<p>Move pictures without changing how many there are. Compare Now with a fixed Before group, including occasional real one-object additions/removals. Reversible two-tap movement, Undo, explicit Check, bilingual narration, saved rounds and shared balloons.</p>
<p>Genuine production and component screenshots are labelled separately. Tablet views use emulator display overrides; physical-device and child acceptance remain separate. Captures wait for the expected artwork before saving.</p>
<p><a href="README.md">Implementation and validation</a> · <a href="evidence/validation.json">Evidence summary</a></p>
'''
html+=''.join(f'<section><h2>{escape(title)} ({len(cards)})</h2><div class="gallery">'+''.join(cards)+'</div></section>' for title,cards in groups.items() if cards)
(root/'index.html').write_text(html+'</html>\n')
print(f'Indexed {sum(map(len,groups.values()))} genuine captures')
