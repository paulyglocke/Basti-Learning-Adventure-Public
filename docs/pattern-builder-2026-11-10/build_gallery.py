#!/usr/bin/env python3
"""Offline index of actual Android screenshots; no mockups or generated app images."""
from pathlib import Path
from html import escape
root=Path(__file__).resolve().parent
cards=[]
for p in sorted((root/'captures').glob('pattern-*.png')):
    component='-layout-' in p.name
    caption=('Component layout test on an emulator. The construction workspace is scrolled; the example remains fixed. Keyboard focus may be visible.' if component else 'Actual production route on the API 35 phone emulator. The workspace may be scrolled; the example stays visible during construction.')
    title=p.stem.replace('pattern-','',1).replace('-',' ')
    cards.append(f'<figure><h2>{escape(title)}</h2><a href="captures/{p.name}"><img loading="lazy" src="captures/{p.name}" alt="{escape(title)}"></a><figcaption>{caption}</figcaption></figure>')
(root/'index.html').write_text('''<!doctype html>
<html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Basti — Pattern Builder review</title>
<style>body{font-family:system-ui,sans-serif;background:#faf8ef;color:#203e4a;max-width:1400px;margin:auto;padding:24px}p{line-height:1.6;max-width:850px}a{color:#286a50}.gallery{display:grid;grid-template-columns:repeat(auto-fit,minmax(290px,1fr));gap:20px}figure{background:white;border:1px solid #d5ddce;border-radius:16px;margin:0;padding:16px}h2{font-size:1.1rem}img{width:100%;height:520px;object-fit:contain}figcaption{font-size:.9rem;line-height:1.5}</style>
<h1>Pattern Builder</h1><p>AB, AAB and ABC with animals and shapes. Visible examples, reversible construction, explicit Check, bilingual audio, saved rounds and shared balloon rewards.</p>
<p>Genuine production-route and component screenshots are labelled separately. Tablet views use emulator overrides; physical-device and child-use acceptance remain separate.</p>
<p><a href="README.md">Implementation and validation</a> · <a href="evidence/validation.json">Evidence summary</a></p>
<div class="gallery">'''+''.join(cards)+'</div></html>\n')
print(f'Indexed {len(cards)} genuine captures')
