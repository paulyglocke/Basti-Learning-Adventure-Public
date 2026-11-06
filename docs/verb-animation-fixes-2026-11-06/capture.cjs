// Genuine production-player screenshots. Run offline after all atlases are prepared.
const {chromium}=require('playwright');
const fs=require('node:fs');
const path=require('node:path');
const {pathToFileURL}=require('node:url');
const root=path.resolve(__dirname,'../..'), out=__dirname;
const esc=s=>String(s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
(async()=>{
 const browser=await chromium.launch({headless:true});
 try {
  const context=await browser.newContext({viewport:{width:760,height:900},offline:true});
  const page=await context.newPage(), errors=[];
  page.on('pageerror',e=>errors.push(e.message));
  await page.addInitScript(()=>{window.Android={speak(){},stopSpeaking(){},vibrate(){}};});
  await page.goto(pathToFileURL(path.join(root,'app/src/main/assets/index.html')).href);
  await page.evaluate(()=>setAudioMode('off'));
  const lessons=await page.evaluate(()=>verbQs.map((q,i)=>({index:i,en:q.v.en,de:q.v.de,...VerbMotionContent.lessons[q.v.en]})));
  if(lessons.length!==53)throw Error('Expected all 53 lessons');
  const available=process.argv.includes('--available');
  const review=await context.newPage();
  fs.mkdirSync(path.join(out,'contacts'),{recursive:true});
  const inventory=[];
  for(const lesson of lessons){
   if(available&&!fs.existsSync(path.join(root,'app/src/main/assets/VerbMotion',lesson.animal+'.webp')))continue;
   await page.evaluate(i=>{setLang('en');openVerbLesson(i,false);},lesson.index);
   await page.waitForFunction(()=>VerbMotion.state()?.ready);
   const frames=[];
   for(const fraction of [0,.14,.28,.42,.56,.70,.84,.98]){
    const pose=await page.evaluate(p=>VerbMotion.review(p),fraction);
    frames.push({fraction,pose,image:(await page.locator('.verbAnimStage').screenshot()).toString('base64')});
   }
   await review.setViewportSize({width:1200,height:470});
   await review.setContent(`<html><style>*{box-sizing:border-box}body{margin:0;background:white;font:16px system-ui;color:#203e4a}h1{font-size:22px;margin:12px}main{display:grid;grid-template-columns:repeat(4,300px)}figure{margin:0}img{display:block;width:300px;height:169px;object-fit:contain}figcaption{padding:4px 12px;font-size:13px}</style><h1>${lesson.index+1}. ${esc(lesson.en)} · ${esc(lesson.de)}</h1><main>${frames.map(f=>`<figure><img src="data:image/png;base64,${f.image}"><figcaption>${Math.round(f.fraction*100)}% · pose ${f.pose.frame}</figcaption></figure>`).join('')}</main><p>Production Canvas player, offline desktop Chromium; real screenshots.</p></html>`);
   const filename=String(lesson.index+1).padStart(2,'0')+'-'+lesson.en.replace(/[^a-z]+/g,'-')+'.png';
   await review.screenshot({path:path.join(out,'contacts',filename),fullPage:true});
   await page.evaluate(i=>{setLang('de');openVerbLesson(i,false);},lesson.index);
   await page.waitForFunction(()=>VerbMotion.state()?.ready);
   const actual=await page.locator('.mainWord').textContent();
   if(actual!==lesson.de)throw Error('German mapping changed: '+lesson.en);
   inventory.push({...lesson,contact:'contacts/'+filename,samples:frames.map(({image,...f})=>f)});
   if(inventory.length%10===0)process.stdout.write(`Captured ${inventory.length}/53\n`);
  }
  if(errors.length)throw Error(errors.join('\n'));
  fs.writeFileSync(path.join(out,'inventory.json'),JSON.stringify({method:'Eight deterministic production-player screenshots per lesson, offline, English/German checked.',errors,lessons:inventory},null,2)+'\n');
  fs.writeFileSync(path.join(out,'index.html'),`<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Verb animation repairs</title><style>body{margin:24px;font:17px system-ui;color:#203e4a;background:#faf8ef}article{margin:24px 0;padding:16px;background:white;border-radius:16px}img{display:block;max-width:1200px;width:100%}h2{font-size:22px}p{max-width:900px;line-height:1.5}</style><h1>Verb animation repairs · ${inventory.length}/53 lessons</h1><p>Eight genuine samples per cycle from the production player. Offline Chromium rendering; Android device validation is recorded separately. <a href="README.md">Review notes and artwork provenance</a> · <a href="../verb-animation-audit-2026-11-05/index.html">Before</a></p>${inventory.map(x=>`<article><h2>${x.index+1}. ${esc(x.en)} · ${esc(x.de)}</h2><a href="${x.contact}"><img loading="lazy" src="${x.contact}" alt="Eight samples for ${esc(x.en)}"></a></article>`).join('')}</html>`);
  process.stdout.write(`Captured ${inventory.length}/53. Browser errors: ${errors.length}\n`);
 }finally{await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1;});
