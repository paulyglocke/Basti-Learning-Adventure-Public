const {test,expect}=require('@playwright/test');
const {pathToFileURL}=require('node:url');
const {resolve}=require('node:path');

test.beforeEach(async ({page,context})=>{
  await context.setOffline(true);
  await page.addInitScript(()=>{
    window.spoken=[];window.stops=0;window.playing=[];
    window.Android={speak(text,lang,id){spoken.push({text,lang,id});playing.push(id)},stopSpeaking(){stops++;playing=[]},vibrate(){}};
  });
  await page.goto(pathToFileURL(resolve('app/src/main/assets/index.html')).href);
  await page.clock.install();
});

for(const mode of ['all','questions','off']) {
  test(`${mode}: automatic question, feedback and all manual controls`,async({page})=>{
    await page.evaluate(mode=>{setAudioMode(mode);startGame('count')},mode);
    await page.clock.runFor(250);
    expect(await page.evaluate(()=>spoken.length)).toBe(mode==='off'?0:1);
    await page.evaluate(()=>{spoken=[];document.querySelector(`.answer[data-key="${state.current.correct}"]`).click()});
    expect(await page.evaluate(()=>spoken.length)).toBe(mode==='all'?1:0);
    await page.evaluate(()=>spoken=[]);
    await page.locator('.speakBtn').click();
    await page.locator('.answerSpeak').first().click();
    await page.evaluate(()=>{openVerbLesson(0);stopSpeech()});
    await page.locator('#lessonListen').click();
    expect(await page.evaluate(()=>spoken.length)).toBe(mode==='off'?0:3);
  });
}

test('Replay restarts current instruction; repeated Replay and option taps replace playback',async({page})=>{
  await page.evaluate(()=>{startGame('count');stopSpeech();spoken=[];stops=0});
  const instruction=await page.evaluate(()=>state.current.speak);
  for(let i=0;i<3;i++)await page.locator('.speakBtn').click();
  expect(await page.evaluate(()=>spoken.map(x=>x.text))).toEqual([instruction,instruction,instruction]);
  expect(await page.evaluate(()=>playing.length)).toBe(1);
  for(let i=0;i<3;i++)await page.locator('.answerSpeak').first().click();
  expect(await page.evaluate(()=>({playing:playing.length,stops,score:state.score}))).toEqual({playing:1,stops:6,score:0});
  await page.clock.runFor(500);
  expect(await page.evaluate(()=>spoken.length)).toBe(6);
});

test('new feedback cancels delayed question even when feedback is muted',async({page})=>{
  for(const mode of ['all','questions']){
    await page.evaluate(mode=>{setAudioMode(mode);spoken=[];startGame('count');document.querySelector(`.answer[data-key="${state.current.correct}"]`).click()},mode);
    await page.clock.runFor(500);
    expect(await page.evaluate(()=>spoken.length)).toBe(mode==='all'?1:0);
  }
});

test('new question, Home, Back, lesson navigation and completion cancel obsolete speech',async({page})=>{
  for(const action of ['goHome()','navigateBack()','openVerbLesson(0)','finish()','renderQuestion()','suspendAudio()']){
    await page.evaluate(action=>{setAudioMode('all');startGame('count');scheduleSpeech('OBSOLETE');spoken=[];(0,eval)(action)},action);
    await page.clock.runFor(500);
    expect(await page.evaluate(()=>spoken.some(x=>x.text.includes('OBSOLETE')))).toBe(false);
    await page.evaluate(()=>stopSpeech());
    expect(await page.evaluate(()=>playing)).toEqual([]);
  }
});

test('language boundary cancels English and restarts a German question and Replay',async({page})=>{
  await page.evaluate(()=>{startGame('count');scheduleSpeech('OLD ENGLISH');setLang('de')});
  await page.clock.runFor(500);
  await page.locator('.speakBtn').click();
  expect(await page.evaluate(()=>spoken.map(x=>x.lang))).toEqual(['de','de']);
  expect(await page.evaluate(()=>spoken.some(x=>x.text.includes('OLD ENGLISH')))).toBe(false);
  expect(await page.evaluate(()=>spoken.at(-1).text)).toBe(await page.evaluate(()=>state.current.speak));
});

test('tutorial completion requires current successful playback, separated by language and version',async({page})=>{
  await page.evaluate(()=>{setAudioMode('off');audioGuidance.reset();startGame('count')});
  await page.clock.runFor(500);
  expect(await page.evaluate(()=>localStorage.getItem(audioGuidance.tutorialKey('count')))).toBeNull();
  await page.evaluate(()=>{setAudioMode('all');startGame('count')});
  await page.clock.runFor(500);
  expect(await page.evaluate(()=>localStorage.getItem(audioGuidance.tutorialKey('count')))).toBeNull();
  await page.evaluate(()=>{const id=spoken.at(-1).id;goHome();speechResult(id,true)});
  expect(await page.evaluate(()=>localStorage.getItem(audioGuidance.tutorialKey('count')))).toBeNull();
  await page.evaluate(()=>startGame('count'));await page.clock.runFor(500);
  await page.evaluate(()=>speechResult(spoken.at(-1).id,false));
  expect(await page.evaluate(()=>localStorage.getItem(audioGuidance.tutorialKey('count')))).toBeNull();
  await page.evaluate(()=>startGame('count'));await page.clock.runFor(500);
  await page.evaluate(()=>speechResult(spoken.at(-1).id,true));
  expect(await page.evaluate(()=>audioGuidance.tutorial('count'))).toBe('');
  await page.evaluate(()=>setLang('de'));
  expect(await page.evaluate(()=>audioGuidance.tutorial('count'))).not.toBe('');
  await page.clock.runFor(500);
  await page.evaluate(()=>speechResult(spoken.at(-1).id,true));
  await page.reload();
  expect(await page.evaluate(()=>audioGuidance.tutorial('count'))).toBe('');
});

test('tutorial reset is durable, invalidates callbacks and native reset epoch applies once',async({page})=>{
  await page.evaluate(()=>startGame('count'));await page.clock.runFor(500);
  await page.evaluate(()=>{const id=spoken.at(-1).id;audioGuidance.reset();speechResult(id,true);syncTutorialReset(3)});
  expect(await page.evaluate(()=>audioGuidance.tutorial('count'))).not.toBe('');
  await page.reload();
  expect(await page.evaluate(()=>audioGuidance.tutorial('count'))).not.toBe('');
  await page.evaluate(()=>{localStorage.setItem(audioGuidance.tutorialKey('count'),'1');syncTutorialReset(3)});
  expect(await page.evaluate(()=>audioGuidance.tutorial('count'))).toBe('');
  await page.evaluate(()=>syncTutorialReset(4));
  expect(await page.evaluate(()=>audioGuidance.tutorial('count'))).not.toBe('');
});

test('balloon audio respects Off and reuses one context when enabled',async({page})=>{
  await page.evaluate(()=>{
    window.contexts=0;window.tones=0;window.suspends=0;
    window.AudioContext=class {
      constructor(){contexts++;this.currentTime=0;this.state='running'}
      createOscillator(){return {frequency:{setValueAtTime(){},exponentialRampToValueAtTime(){}},connect(){return {connect(){}}},start(){tones++},stop(){},disconnect(){}}}
      createGain(){return {gain:{setValueAtTime(){},exponentialRampToValueAtTime(){}},disconnect(){}}}
      suspend(){suspends++;return Promise.resolve()}
    };
    setAudioMode('off');startGame('count');finish();
  });
  await page.locator('.balloon').first().click();
  expect(await page.evaluate(()=>({contexts,tones}))).toEqual({contexts:0,tones:0});
  await page.evaluate(()=>setAudioMode('all'));
  await page.locator('.balloon').nth(1).click();await page.locator('.balloon').nth(2).click();
  expect(await page.evaluate(()=>({contexts,tones}))).toEqual({contexts:1,tones:2});
  await page.evaluate(()=>setAudioMode('off'));
  await page.locator('.balloon').nth(3).click();
  expect(await page.evaluate(()=>({contexts,tones,suspends}))).toEqual({contexts:1,tones:2,suspends:1});
});

test('Questions Only presents instructional tutorials and lessons, but mutes wrong feedback and completion',async({page})=>{
  await page.evaluate(()=>{setAudioMode('questions');startGame('count')});await page.clock.runFor(250);
  expect(await page.evaluate(()=>spoken[0].text)).toContain(await page.evaluate(()=>T.en.tutorials.count));
  await page.evaluate(()=>{spoken=[];document.querySelector(`.answer:not([data-key="${state.current.correct}"])`).click();finish()});
  await page.clock.runFor(250);
  expect(await page.evaluate(()=>spoken)).toEqual([]);
  await page.locator('.speakBtn').click();
  expect(await page.evaluate(()=>spoken.at(-1).text)).toContain('Adventure complete!');
  await page.evaluate(()=>{spoken=[];openVerbLesson(0)});await page.clock.runFor(250);
  expect(await page.evaluate(()=>spoken[0].text)).toContain(await page.evaluate(()=>verbLessons[verbQs[0].v.en].en));
});

test('changing language inside an answered quiz lesson preserves score and prevents a second award',async({page})=>{
  await page.evaluate(()=>{
    startGame('verbs');document.querySelector(`.answer[data-key="${state.current.correct}"]`).click();
    openVerbLesson(0,true);setLang('de');backFromLesson();
  });
  await page.locator('.speakBtn').click();
  expect(await page.evaluate(()=>spoken.at(-1).lang)).toBe('de');
  expect(await page.evaluate(()=>({score:state.score,answered:state.answered}))).toEqual({score:1,answered:true});
  await page.evaluate(()=>document.querySelector(`.answer[data-key="${state.current.correct}"]`).click());
  expect(await page.evaluate(()=>state.score)).toBe(1);
  await expect(page.locator('#nextAction')).toBeEnabled();
});

test('browser fallback never selects remote or previous-language voices',async({page})=>{
  const result=await page.evaluate(()=>{
    delete window.Android;
    window.SpeechSynthesisUtterance=function(text){this.text=text};
    Object.defineProperty(window,'speechSynthesis',{value:{
      cancel(){},getVoices(){return [{lang:'en-GB',localService:true},{lang:'de-DE',localService:false}]},
      speak(u){spoken.push({text:u.text,lang:u.lang})}
    }});
    speak('Hello');setLang('de');speak('Hallo');return spoken;
  });
  expect(result).toEqual([{text:'Hello',lang:'en-GB'}]);
});
