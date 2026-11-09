#!/usr/bin/env python3
"""Build an offline review index over genuine test exports and captured UI."""
from pathlib import Path
import html,json
ROOT=Path(__file__).resolve().parent
DATA=json.loads((ROOT.parents[1]/'content/verbs.json').read_text())
def esc(value):return html.escape(str(value),quote=True)
def figure(path,title,caption):
 return f'<figure><h3>{esc(title)}</h3><a href="captures/{esc(path.name)}"><img loading="lazy" src="captures/{esc(path.name)}" alt="{esc(title)}"></a><figcaption>{esc(caption)}</figcaption></figure>'
ui=[]
labels={
 'animal-actions-en':'Animal Actions · English',
 'animal-actions-de':'Animal Actions · Deutsch',
 'animal-actions-complete':'Shared completion · Deutsch',
 'build-5-partial':'Build 5 · English · editable board',
 'build-5-solved':'Build 5 · Deutsch · part-whole equation',
 'build-10-partial':'Build 10 · English · editable board',
 'build-10-solved':'Build 10 · Deutsch · part-whole equation',
}
for path in sorted((ROOT/'captures').glob('*.png')):
 name=path.stem
 if name.startswith('storyboard-'):continue
 if name.startswith('layout-'):
  if '-phone-' not in name and '-tablet-' not in name:continue
  title=name.replace('layout-','').replace('-',' ')
  caption='Component layout test, scrolled to the board or lesson controls. Keyboard focus shading may be visible. All controls are separately checked for reachability.'
 elif name.startswith('native-lesson-'):
  index=int(name.rsplit('-',1)[1])-1;verb=DATA['verbs'][index]
  title=verb['label']['en']+' · native lesson'
  caption='Actual production route in English, scrolled to the picture steps.'
 else:
  title=labels.get(name,name.replace('-',' '));caption='Actual production route on the phone-sized API 35 emulator.'
 ui.append(figure(path,title,caption))
verbs=[]
for i,verb in enumerate(DATA['verbs'],1):
 path=ROOT/'captures'/f'storyboard-{i}.png'
 if not path.is_file():raise RuntimeError(f'Missing native export: {path.name}')
 verbs.append(figure(path,f"{i}. {verb['label']['en']} / {verb['label']['de']}",
  'Four authored teaching phases exported by the Android production scene renderer. This is a renderer storyboard; production UI captures are shown separately above.'))
page='''<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Basti — native verbs and number bonds</title><style>
:root{font-family:system-ui,sans-serif;color:#203e4a;background:#faf8ef}body{max-width:1440px;margin:auto;padding:24px}h1{font-size:clamp(2rem,5vw,3.6rem)}p{max-width:900px;line-height:1.6}a{color:#2b6b52}nav{display:flex;gap:20px;flex-wrap:wrap}section{margin:40px 0}.ui{display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:20px}.actions{display:grid;grid-template-columns:repeat(auto-fit,minmax(420px,1fr));gap:24px}figure{margin:0;padding:16px;background:white;border:1px solid #d5ddce;border-radius:16px}figure img{display:block;max-width:100%;height:auto;width:100%;object-fit:contain}.ui img{height:480px}h3{margin:0 0 12px}figcaption{font-size:.9rem;line-height:1.5;margin-top:12px}input{font:inherit;padding:12px;width:min(90%,600px);border:1px solid #76968c;border-radius:10px;margin:20px 0}@media(max-width:500px){.actions{grid-template-columns:1fr}body{padding:16px}}
</style><h1>Native verbs and number bonds</h1>
<p>All 53 verb lessons and Animal Actions now have native Android presentation. Build 5 and Build 10 add reversible, checked egg construction. The existing artwork, shared progress and completion rewards are reused.</p>
<nav><a href="#ui">Native UI</a><a href="#actions">53 action storyboards</a><a href="README.md">Implementation and validation</a><a href="evidence/validation.json">Evidence summary</a></nav>
<section id="ui"><h2>Native UI</h2><p>Production-route screenshots and component layout captures cover both languages, phone/tablet emulation, large text and short landscape. Captions identify scrolled views and component-only examples.</p><div class="ui">'''+''.join(ui)+'''</div></section><section id="actions"><h2>All 53 native action storyboards</h2><p>The Android test exports the same four teaching phases used by the production renderer. Every storyboard was visually reviewed.</p><label>Find an action <input id="filter" type="search" placeholder="Jump, springen, water…"></label><div class="actions">'''+''.join(verbs)+'''</div></section>
<script>document.getElementById('filter').addEventListener('input',event=>{const q=event.target.value.toLocaleLowerCase();document.querySelectorAll('.actions figure').forEach(card=>{card.hidden=!card.textContent.toLocaleLowerCase().includes(q)})});</script></html>'''
(ROOT/'index.html').write_text(page+'\n')
print(f'{len(ui)} UI captures and {len(verbs)} Android storyboards indexed.')
