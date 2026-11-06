const {test,expect}=require('@playwright/test');
const {pathToFileURL}=require('node:url');
const {resolve}=require('node:path');
const appUrl=pathToFileURL(resolve('app/src/main/assets/index.html')).href;
test.beforeEach(async({page,context})=>{
 await context.setOffline(true);
 await page.addInitScript(()=>{window.spoken=[];window.Android={speak:(text,lang)=>spoken.push({text,lang}),stopSpeaking(){},vibrate(){}};});
 await page.goto(appUrl);await page.evaluate(()=>setAudioMode('off'));
});
async function open(page,key){await page.evaluate(key=>openVerbLesson(verbQs.findIndex(q=>q.v.en===key),false),key);await expect(page.locator('.verb-motion-stage')).toHaveAttribute('data-state','ready');}

test('real pose pixels move, pause holds them, and navigation cancels playback',async({page})=>{
 await open(page,'fly');
 await page.locator('[data-motion="play"]').click();
 await page.waitForTimeout(120);const moving=await page.locator('.verb-motion-canvas').screenshot();
 await page.waitForTimeout(650);expect((await page.locator('.verb-motion-canvas').screenshot()).equals(moving)).toBe(false);
 await page.locator('[data-motion="pause"]').click();
 const paused=await page.locator('.verb-motion-canvas').screenshot();await page.waitForTimeout(350);
 expect((await page.locator('.verb-motion-canvas').screenshot()).equals(paused)).toBe(true);
 await page.locator('[data-motion="play"]').click();
 await page.evaluate(()=>goHome());
 expect(await page.evaluate(()=>VerbMotion.state().playing)).toBe(false);
});

test('suspending before image decode prevents delayed autoplay',async({page})=>{
 await page.evaluate(()=>{openVerbLesson(0,false);suspendAudio();});
 await expect(page.locator('.verb-motion-stage')).toHaveAttribute('data-state','ready');
 await page.waitForTimeout(100);
 expect(await page.evaluate(()=>VerbMotion.state().playing)).toBe(false);
});

test('a demonstration finishes and holds its result until Watch again',async({page})=>{
 await open(page,'fly');await page.locator('[data-motion="play"]').click();
 await expect.poll(()=>page.evaluate(()=>VerbMotion.state().playing)).toBe(false);
 const result=await page.locator('.verb-motion-canvas').screenshot();await page.waitForTimeout(200);
 expect((await page.locator('.verb-motion-canvas').screenshot()).equals(result)).toBe(true);
 await page.locator('[data-motion="play"]').click();
 expect(await page.evaluate(()=>VerbMotion.state().playing)).toBe(true);
});

for(const platform of ['browser','android'])test(`reduced motion gives four static bilingual steps (${platform})`,async({page})=>{
 if(platform==='browser')await page.emulateMedia({reducedMotion:'reduce'});
 else await page.evaluate(()=>Android.reducedMotion=()=>true);
 for(const lang of ['en','de']){
  await page.evaluate(lang=>setLang(lang),lang);await open(page,'jump');
  await expect(page.locator('.verb-picture-steps figure')).toHaveCount(4);
  await expect(page.locator('.verb-picture-steps figcaption').nth(2)).toContainText(lang==='en'?'In the air':'In der Luft');
  await expect(page.locator('[data-motion="play"]')).toBeDisabled();
  const still=await page.locator('.verb-picture-steps').screenshot();await page.waitForTimeout(150);
  expect((await page.locator('.verb-picture-steps').screenshot()).equals(still)).toBe(true);
 }
});

test('a failed picture load has a working retry and back navigation',async({page})=>{
 await page.evaluate(()=>{window.originalEagle=VerbMotionContent.assets.eagle.path;VerbMotionContent.assets.eagle.path='missing-verb-test.webp';openVerbLesson(0,false);});
 await expect(page.locator('.verb-motion-stage')).toHaveAttribute('data-state','error');
 await expect(page.locator('[data-motion="retry"]')).toBeVisible();
 await page.evaluate(()=>VerbMotionContent.assets.eagle.path=originalEagle);
 await page.locator('[data-motion="retry"]').click();
 await expect(page.locator('.verb-motion-stage')).toHaveAttribute('data-state','ready');
 await page.locator('#verbExplorerBack').click();
 await expect(page.locator('.verbLibraryCard')).toHaveCount(53);
});

test('picture controls work by keyboard and sound modelling respects Off',async({page})=>{
 await open(page,'bark');
 await page.locator('[data-motion="pictures"]').focus();await page.keyboard.press('Enter');
 await expect(page.locator('.verb-picture-steps figure')).toHaveCount(4);
 await page.locator('[data-motion="pictures"]').focus();await page.keyboard.press('Space');
 await expect(page.locator('.verb-motion-canvas')).toBeVisible();
 await page.locator('.verb-model-guide summary').click();
 await page.locator('#verbModelListen').click();
 expect(await page.evaluate(()=>spoken)).toEqual([]);
 await page.evaluate(()=>setAudioMode('all'));
 await page.locator('#verbModelListen').click();
 expect(await page.evaluate(()=>spoken.at(-1).text)).toContain('Grown-up');
});
