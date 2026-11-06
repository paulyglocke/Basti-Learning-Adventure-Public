// Render each existing lesson at eight deterministic points in its real CSS cycle.
// Historical a990f34 harness; current source uses ../verb-animation-fixes-2026-11-06/capture.cjs.
// Review harness only: no application code, preferences or native journals are changed.
const {chromium}=require('playwright');
const fs=require('node:fs');
const path=require('node:path');
const {pathToFileURL}=require('node:url');
const root=path.resolve(__dirname,'../..');
const out=__dirname;
const escape=s=>String(s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));

(async()=>{
  const browser=await chromium.launch({headless:true});
  try {
    const context=await browser.newContext({viewport:{width:700,height:900},offline:true});
    const page=await context.newPage();
    const errors=[];page.on('pageerror',e=>errors.push(e.message));
    await page.addInitScript(()=>{window.Android={speak(){},stopSpeaking(){},vibrate(){}};});
    await page.goto(pathToFileURL(path.join(root,'app/src/main/assets/index.html')).href);
    await page.evaluate(()=>setAudioMode('off'));
    const count=await page.evaluate(()=>verbQs.length);
    if(count!==53)throw Error(`Expected 53 lessons, found ${count}`);
    fs.mkdirSync(path.join(out,'contacts'),{recursive:true});
    const review=await context.newPage();
    const inventory=[];
    for(let i=0;i<count;i++){
      await page.evaluate(i=>{setLang('en');openVerbLesson(i,false);},i);
      const metadata=await page.evaluate(i=>{
        const q=verbQs[i],e=document.querySelector('.animatedAnimal'),stage=document.querySelector('.verbAnimStage');
        const cs=getComputedStyle(e),a=animationForVerb(q.v.en);
        return {index:i+1,en:q.v.en,de:q.v.de,animal:q.a,animationClass:a.cls,animation:cs.animationName,
          duration:parseFloat(cs.animationDuration)*1000,easing:cs.animationTimingFunction,scene:a.scene,
          extras:a.extra,waterWave:!!stage.querySelector('.waterWave'),background:getComputedStyle(stage).backgroundImage,
          glyph:e.textContent,example:document.querySelectorAll('.verbInfoCard')[1].textContent,
          animatedChildCount:e.querySelectorAll('*').length};
      },i);
      if(metadata.animation==='none')throw Error(`Missing animation for ${metadata.en}`);
      const frames=[];
      for(const fraction of [0,.14,.28,.42,.56,.70,.84,.98]){
        await page.evaluate(({fraction,duration})=>{
          const stage=document.querySelector('.verbAnimStage');
          stage.getAnimations({subtree:true}).forEach(a=>{a.pause();a.currentTime=fraction*duration;});
        },{fraction,duration:metadata.duration});
        frames.push((await page.locator('.verbAnimStage').screenshot({animations:'allow'})).toString('base64'));
      }
      // A contact sheet contains only genuine browser screenshots of the existing stage.
      await review.setViewportSize({width:1200,height:570});
      await review.setContent(`<html><style>*{box-sizing:border-box}body{margin:0;background:#fff;font:18px system-ui;color:#203e4a}h1{font-size:23px;margin:12px}main{display:grid;grid-template-columns:repeat(4,300px);gap:0}figure{margin:0}img{display:block;width:300px;height:190px;object-fit:contain}figcaption{padding:5px 12px;font-size:14px}</style><h1>${i+1}. ${escape(metadata.en)} · ${escape(metadata.de)} — ${escape(metadata.animation)}</h1><main>${frames.map((f,j)=>`<figure><img src="data:image/png;base64,${f}"><figcaption>${[0,14,28,42,56,70,84,98][j]}% of cycle</figcaption></figure>`).join('')}</main><p>Desktop Chromium reference rendering; platform emoji appearance differs from Android. No new artwork.</p></html>`);
      const filename=String(i+1).padStart(2,'0')+'-'+metadata.en.replace(/[^a-z]+/g,'-')+'.png';
      await review.screenshot({path:path.join(out,'contacts',filename),fullPage:true});
      metadata.contact='contacts/'+filename;
      // Language changes must preserve the same visual action, not map via translated strings.
      const de=await page.evaluate(i=>{setLang('de');openVerbLesson(i,false);return {
        title:document.querySelector('.mainWord').textContent,
        animation:getComputedStyle(document.querySelector('.animatedAnimal')).animationName};},i);
      if(de.title!==metadata.de || de.animation!==metadata.animation)throw Error(`Language mapping changed for ${metadata.en}`);
      inventory.push(metadata);
      if((i+1)%10===0)process.stdout.write(`Captured ${i+1}/${count}\n`);
    }
    if(errors.length)throw Error(errors.join('\n'));
    fs.writeFileSync(path.join(out,'inventory.json'),JSON.stringify({source:'a990f34 + local debug prototype',
      method:'All 53 existing lessons rendered at 8 points of their real CSS cycle; EN/DE mapping checked; speech disabled.',
      browser:'Desktop Chromium / platform emoji, not Android visual acceptance',errors,lessons:inventory},null,2)+'\n');
    const groups=Object.fromEntries([...new Set(inventory.map(x=>x.animation))].map(a=>[a,inventory.filter(x=>x.animation===a).map(x=>x.en)]));
    process.stdout.write(JSON.stringify({count,groups},null,2)+'\n');
    fs.writeFileSync(path.join(out,'index.html'),`<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Verb animation review — 53 lessons</title><style>body{margin:24px;font:17px system-ui;color:#203e4a;background:#faf8ef}article{margin:28px 0;padding:16px;background:white;border:1px solid #ddd;border-radius:16px}img{display:block;max-width:1200px;width:100%}h2{font-size:22px}p{max-width:900px;line-height:1.5}a{color:#28694f}</style><h1>All 53 legacy verb animations</h1><p>Eight deterministic samples per CSS cycle. These are screenshots of the current browser rendering, not proposed artwork or Android acceptance. English/German titles and animation mapping were checked for every lesson. <a href="REVIEW.md">Read the findings and priorities</a> · <a href="../verb-motion-preview/index.html">Three native previews</a>.</p>${inventory.map(x=>`<article id="verb-${x.index}"><h2>${x.index}. ${escape(x.en)} · ${escape(x.de)}</h2><p>${escape(x.animal)} · ${escape(x.animation)} · ${x.duration}ms · ${escape(x.easing)}</p><a href="${x.contact}"><img loading="lazy" src="${x.contact}" alt="Eight actual animation samples for ${escape(x.en)}"></a></article>`).join('')}</html>`);
  } finally {await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1;});
