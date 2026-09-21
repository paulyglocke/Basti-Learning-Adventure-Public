const {test, expect} = require('@playwright/test');
const {pathToFileURL} = require('node:url');
const {resolve} = require('node:path');

const praise = {
  en: [['Great! 🌟','Great!'], ['Yes! 🎉','Yes!'], ['Correct! ⭐','Correct!'], ['Well done! 🐉','Well done!']],
  de: [['Super! 🌟','Super!'], ['Ja! 🎉','Ja!'], ['Richtig! ⭐','Richtig!'], ['Sehr gut! 🐉','Sehr gut!']]
};

test.beforeEach(async ({page, context}) => {
  await context.setOffline(true);
  await page.addInitScript(() => {
    window.spoken=[];
    window.Android={speak:(text,lang)=>spoken.push({text,lang}),stopSpeaking:()=>{},vibrate:()=>{}};
  });
  await page.goto(pathToFileURL(resolve('app/src/main/assets/index.html')).href);
});

for (const lang of ['en','de']) {
  test(`praise retains all decorated displays and sends explicit clean speech (${lang})`, async ({page}) => {
    for (let i=0;i<praise[lang].length;i++) {
      const result=await page.evaluate(({lang,i})=>{
        setLang(lang);setAudioMode('all');startGame('count');stopSpeech();spoken.length=0;
        const entries=T[lang].great;
        // Select each real authored entry, independent of random question generation.
        T[lang].great=[entries[i]];
        document.querySelector(`.answer[data-key="${state.current.correct}"]`).click();
        T[lang].great=entries;
        return {display:$('feedback').textContent,spoken,score:state.score};
      },{lang,i});
      expect(result).toEqual({display:praise[lang][i][0],spoken:[{text:praise[lang][i][1],lang}],score:1});
    }
  });

  test(`tutorial, question, replay, options, wrong feedback, completion and lessons use safe boundary (${lang})`, async ({page}) => {
    await page.clock.install();
    await page.evaluate(lang=>{
      setLang(lang);setAudioMode('all');audioGuidance.reset();
      T[lang].tutorials.count='🎉 Tutorial!';startGame('count');
      state.current.speak='🔊 42?';
      scheduleSpeech(`${T[lang].tutorials.count} ${state.current.speak}`);
    },lang);
    await page.clock.runFor(250);
    expect(await page.evaluate(()=>spoken.at(-1))).toEqual({text:'Tutorial! 42?',lang});
    await page.locator('.speakBtn').click();
    expect(await page.evaluate(()=>spoken.at(-1))).toEqual({text:'42?',lang});
    await page.locator('.answerSpeak').first().evaluate(b=>{b.dataset.speech='🐊 Krokodil';b.click()});
    expect(await page.evaluate(()=>({spoken:spoken.at(-1),score:state.score,answered:state.answered})))
      .toEqual({spoken:{text:'Krokodil',lang},score:0,answered:false});
    await page.evaluate(()=>document.querySelector(`.answer:not([data-key="${state.current.correct}"])`).click());
    expect(await page.evaluate(()=>spoken.at(-1).text)).toBe(lang==='en'?'Not quite. Try again.':'Noch nicht. Versuch es noch einmal.');
    await page.evaluate(()=>{
      T[settings.lang].finish+=' 🎉';T[settings.lang].scoreText+=' ⭐';
      state.score=5;finish();
    });
    await page.clock.runFor(250);
    expect(await page.evaluate(()=>spoken.at(-1))).toEqual({text:lang==='en'?'Adventure complete! You earned 5 stars out of 5.':'Abenteuer geschafft! Du hast 5 Sterne von 5 gesammelt.',lang});
    await expect(page.locator('.finish h2')).toContainText('🎉');
    await expect(page.locator('.stars')).toHaveText('⭐⭐⭐⭐⭐');
    await page.evaluate(()=>{
      openVerbLesson(0);stopSpeech();spoken.length=0;
    });
    await page.locator('#lessonListen').click();
    expect(await page.evaluate(()=>spoken.at(-1).text)).toContain(await page.evaluate(()=>verbLessons[verbQs[0].v.en][settings.lang]));
  });
}

test('boundary strips decoration/sequences while preserving German, numbers, punctuation and mathematical text', async ({page}) => {
  const cases=[
    ...praise.en,...praise.de,
    ['  Grüße, süße Füße! ÄÖÜ äöü ß 42, 9.999 — „Ja“?  ','Grüße, süße Füße! ÄÖÜ äöü ß 42, 9.999 — „Ja“?'],
    ['Ja 🎉! Weiter ⭐, bitte.','Ja! Weiter, bitte.'],
    ['👩🏽‍🏫 Gut! 🇩🇪 ❤️ ☆ ★ → 🔊','Gut!'],
    ['1️⃣ + 2 = 3; 5 − 2; 2 × 3; #1 * 4','1 + 2 = 3; 5 − 2; 2 × 3; #1 * 4'],
    ['eins🐊zwei\n\t drei','eins zwei drei'],
    ['🐉 🎉 ⭐️','']
  ];
  const results=await page.evaluate(cases=>{
    setLang('de');setAudioMode('all');
    return cases.map(([input])=>{spoken.length=0;speak(input);return [...spoken]});
  },cases);
  expect(results).toEqual(cases.map(([,text])=>text?[{text,lang:'de'}]:[]));
});

test('browser speech fallback uses the same sanitized boundary and skips decoration-only requests', async ({page}) => {
  const results=await page.evaluate(()=>{
    delete window.Android;
    window.SpeechSynthesisUtterance=function(text){this.text=text};
    Object.defineProperty(window,'speechSynthesis',{value:{cancel(){},speak(u){spoken.push({text:u.text,lang:u.lang})}}});
    setLang('de');setAudioMode('all');speak('Ja! 🎉');speak('🐉');
    return spoken;
  });
  expect(results).toEqual([{text:'Ja!',lang:'de-DE'}]);
});

test('sanitization preserves existing all/questions/off and forced manual speech decisions', async ({page}) => {
  const results=await page.evaluate(()=>{
    setLang('en');
    return ['all','questions','off'].map(mode=>{
      setAudioMode(mode);spoken.length=0;
      audioGuidance.say('Question? 🔊','question');
      audioGuidance.say('Great! 🌟','feedback');
      audioGuidance.say('Lesson 🐉','vocab');
      speakOption('Crocodile 🐊'); // Existing force semantics intentionally retained.
      return spoken.map(x=>x.text);
    });
  });
  expect(results).toEqual([
    ['Question?','Great!','Lesson','Crocodile'],
    ['Question?','Great!','Crocodile'],
    ['Crocodile']
  ]);
});
