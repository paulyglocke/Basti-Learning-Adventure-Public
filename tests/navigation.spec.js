const {test,expect}=require('@playwright/test');
const {pathToFileURL}=require('node:url');
const {resolve}=require('node:path');
const url=pathToFileURL(resolve('app/src/main/assets/index.html')).href;
const config=lang=>({lang,audioMode:'all',round:5,numberMax:10,tutorialResetEpoch:0});

test.beforeEach(async({page,context})=>{
  await context.setOffline(true);
  await page.addInitScript(()=>{
    window.spoken=[];window.saved=null;
    window.Android={speak(text,lang,id){spoken.push({text,lang,id})},stopSpeaking(){},vibrate(){},saveSession(json){saved=json}};
  });
  await page.goto(url);await page.clock.install();
});
async function view(page){return page.evaluate(()=>({
 state:JSON.parse(JSON.stringify(state)),html:$('gameContent').innerHTML,feedback:$('feedback').textContent,
 title:$('gameTitle').textContent,next:$('nextAction').disabled,screen:activeScreen,lesson:activeLesson
}))}
async function reboot(page,snapshot,lang='en',active=true){
 await page.reload();
 expect(await page.evaluate(({snapshot,config,active})=>bootLegacy('mixed',config,snapshot,active),{snapshot,config:config(lang),active})).toBe(true);
 await page.clock.runFor(500);
 expect(await page.evaluate(()=>spoken)).toEqual([]);
}

for(const lang of ['en','de']){
 test(`every quiz restores its exact unanswered and answered question after a fresh page (${lang})`,async({page})=>{
  for(const mode of ['verbs','count','math','positions','letters','time','mixed'])for(const answered of [false,true]){
   await page.evaluate(({mode,lang})=>{setLang(lang);setAudioMode('all');startGame(mode)}, {mode,lang});
   await page.locator('#hintAction').click();
   if(answered)await page.evaluate(()=>document.querySelector(`.answer[data-key="${state.current.correct}"]`).click());
   const before=await view(page),snapshot=await page.evaluate(()=>saved);
   expect(snapshot).not.toBeNull();
   await reboot(page,snapshot,lang);
   expect(await view(page)).toEqual(before);
   if(answered){
    await page.evaluate(()=>pick(document.querySelector('.answer'),state.current.correct));
    expect(await page.evaluate(()=>state.score)).toBe(before.state.score);
   }
   await page.locator('.speakBtn').click();
   expect(await page.evaluate(()=>spoken.at(-1).text)).toBe(before.state.current.speak);
  }
 });

 test(`Options round-trips preserve live quizzes and lesson return (${lang})`,async({page})=>{
  await page.evaluate(lang=>{setLang(lang);setAudioMode('all');startGame('verbs')},lang);
  await page.evaluate(()=>document.querySelector(`.answer[data-key="${state.current.correct}"]`).click());
  const quiz=await view(page);
  await page.locator('.learnVerbInline button').click();
  const lesson=await page.locator('#verbExplorerContent').innerHTML();
  for(let n=0;n<3;n++){
   await page.evaluate(()=>{spoken=[];openOptions();openOptions()});
   await page.clock.runFor(500);
   expect(await page.evaluate(()=>spoken)).toEqual([]);
   await page.evaluate(()=>navigateBack());
   await expect(page.locator('#verbExplorer')).toBeVisible();
   expect(await page.locator('#verbExplorerContent').innerHTML()).toBe(lesson);
  }
  const snapshot=await page.evaluate(()=>serializeLegacySession());
  await reboot(page,snapshot,lang);
  await page.locator('#lessonListen').click();
  expect(await page.evaluate(()=>spoken.at(-1).lang)).toBe(lang);
  await page.evaluate(()=>navigateBack());
  expect(await view(page)).toEqual(quiz);
  await page.evaluate(()=>navigateBack());await expect(page.locator('#home')).toBeVisible();
  expect(await page.evaluate(()=>navigateBack())).toBe(false);
 });
}

test('Home Options, library Options and restored Options return to their exact origin',async({page})=>{
 await page.locator('#optionsTop').click();await page.locator('#optionsBack').click();
 await expect(page.locator('#home')).toBeVisible();
 await page.evaluate(()=>openVerbExplorer());
 await page.locator('#optionsTop').click();
 const snapshot=await page.evaluate(()=>serializeLegacySession());
 await reboot(page,snapshot);
 await expect(page.locator('#options')).toBeVisible();
 await page.evaluate(()=>navigateBack());await expect(page.locator('.verbLibraryGrid')).toBeVisible();
 await page.locator('.verbLibraryCard').nth(15).click();
 await page.evaluate(()=>openOptions());
 await reboot(page,await page.evaluate(()=>serializeLegacySession()));
 await page.locator('#optionsBack').click();await expect(page.locator('#lessonListen')).toBeVisible();
 await page.evaluate(()=>navigateBack());await expect(page.locator('.verbLibraryGrid')).toBeVisible();
});

test('native Options suspension suppresses delayed speech and restores a hidden checkpoint without auto narration',async({page})=>{
 await page.evaluate(config=>bootLegacy('verbs',config),config('en'));
 await page.clock.runFor(250);
 expect(await page.evaluate(()=>spoken.length)).toBe(1); // Fresh entry still narrates once.
 await page.evaluate(()=>{spoken=[];scheduleSpeech('OBSOLETE');setShellActive(false)});
 const before=await view(page),snapshot=await page.evaluate(()=>saved);
 await page.clock.runFor(500);expect(await page.evaluate(()=>spoken)).toEqual([]);
 await reboot(page,snapshot,'en',false);
 await page.evaluate(()=>{audioGuidance.replay();speakOption('blocked');setShellActive(true);setShellActive(true)});
 await page.clock.runFor(500);expect(await page.evaluate(()=>spoken)).toEqual([]);
 expect(await view(page)).toEqual(before);
 await page.locator('.speakBtn').click();expect(await page.evaluate(()=>spoken.length)).toBe(1);
});

test('Options changes language without changing question identity, choice order, score or current number range',async({page})=>{
 for(const mode of ['verbs','count','math','positions','letters','time']){
  await page.evaluate(mode=>{setLang('en');setRound(5);setNumberMax(100);startGame(mode);document.querySelector(`.answer[data-key="${state.current.correct}"]`).click();setShellActive(false);spoken=[]},mode);
  const before=await page.evaluate(()=>({correct:state.current.correct,score:state.score,draws:state.questionRandom,choices:[...document.querySelectorAll('.answer')].map(b=>b.dataset.key)}));
  await page.evaluate(config=>applyShellSettings({...config,numberMax:10,round:10}),config('de'));
  await page.evaluate(()=>setShellActive(true));await page.clock.runFor(300);
  expect(await page.evaluate(()=>spoken)).toEqual([]);
  expect(await page.evaluate(()=>({correct:state.current.correct,score:state.score,draws:state.questionRandom,choices:[...document.querySelectorAll('.answer')].map(b=>b.dataset.key)}))).toEqual(before);
  expect(await page.evaluate(()=>({answered:state.answered,round:state.items.length,range:state.questionNumberMax,lang:settings.lang}))).toEqual({answered:true,round:5,range:100,lang:'de'});
  await page.locator('.speakBtn').click();expect(await page.evaluate(()=>spoken.at(-1).lang)).toBe('de');
 }
});

test('completion restores silently with the same stars and Continue starts just one fresh round',async({page})=>{
 await page.evaluate(()=>{startGame('count');state.i=5;state.score=5;finish()});
 const before=await view(page);
 await reboot(page,await page.evaluate(()=>saved));
 expect(await view(page)).toEqual(before);
 await expect(page.locator('.rewardStar')).toHaveCount(5);
 await page.locator('.completionActions .nextBtn').click();
 expect(await page.evaluate(()=>({i:state.i,score:state.score,complete:state.complete}))).toEqual({i:0,score:0,complete:false});
 await page.evaluate(()=>$('nextAction').click());
 expect(await page.evaluate(()=>state.i)).toBe(0);
});

test('incompatible or malformed checkpoints are rejected without resuming speech or awarding answers',async({page})=>{
 await page.evaluate(()=>startGame('count'));
 const snapshot=await page.evaluate(()=>JSON.parse(serializeLegacySession()));
 for(const bad of ['{',JSON.stringify({...snapshot,version:999}),JSON.stringify({...snapshot,state:{...snapshot.state,complete:true}}),JSON.stringify({...snapshot,state:{...snapshot.state,score:100}}),JSON.stringify({...snapshot,state:{...snapshot.state,questionRandom:[]}})]){
  await page.reload();
  expect(await page.evaluate(({bad,config})=>bootLegacy('count',config,bad,true),{bad,config:config('en')})).toBe(false);
  await page.clock.runFor(500);expect(await page.evaluate(()=>spoken)).toEqual([]);
  await expect(page.locator('#home')).toBeVisible();
 }
});

for(const lang of ['en','de'])test(`mid-round Options and recovery preserve ten-question progress and retry state (${lang})`,async({page})=>{
 await page.evaluate(lang=>{setLang(lang);setRound(10);startGame('mixed');for(let n=0;n<4;n++){document.querySelector(`.answer[data-key="${state.current.correct}"]`).click();$('nextAction').click()}},lang);
 await page.evaluate(()=>[...document.querySelectorAll('.answer')].find(b=>b.dataset.key!==state.current.correct).click());
 await page.clock.runFor(500);
 const before=await view(page);
 expect(before.state.i).toBe(4);expect(before.state.score).toBe(4);expect(before.state.feedback.kind).toBe('wrong');
 for(let n=0;n<3;n++){
  await page.evaluate(()=>{spoken=[];openOptions()});await page.evaluate(()=>navigateBack());
  expect(await view(page)).toEqual(before);await page.clock.runFor(500);expect(await page.evaluate(()=>spoken)).toEqual([]);
 }
 await reboot(page,await page.evaluate(()=>serializeLegacySession()),lang);
 expect(await view(page)).toEqual(before);
 await page.evaluate(()=>{const b=document.querySelector(`.answer[data-key="${state.current.correct}"]`);b.click();b.click()});
 expect(await page.evaluate(()=>state.score)).toBe(5);
 await page.evaluate(()=>{openOptions();spoken=[];setLang(settings.lang==='en'?'de':'en')});
 await page.clock.runFor(500);expect(await page.evaluate(()=>spoken)).toEqual([]);
 await page.evaluate(()=>navigateBack());
 expect(await page.evaluate(()=>({i:state.i,score:state.score,answered:state.answered}))).toEqual({i:4,score:5,answered:true});
});
