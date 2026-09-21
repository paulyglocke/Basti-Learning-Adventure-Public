const {test,expect}=require('@playwright/test');
const {pathToFileURL}=require('node:url');
const {resolve}=require('node:path');
const examples={
  en:[['D','Dinosaur'],['S','Snake'],['W','Whale'],['H','Horse'],['C','Crocodile'],['F','Fish']],
  de:[['D','Dinosaurier'],['S','Schlange'],['W','Wal'],['P','Pferd'],['K','Krokodil'],['F','Fisch']]
};

test.beforeEach(async({page,context})=>{
  await context.setOffline(true);
  await page.addInitScript(()=>{
    window.spoken=[];
    window.Android={speak(text,lang,id){spoken.push({text,lang,id})},stopSpeaking(){},vibrate(){}};
  });
  await page.goto(pathToFileURL(resolve('app/src/main/assets/index.html')).href);
  await page.clock.install();
});

async function task(page,index){
  await page.evaluate(index=>{
    const random=Math.random;let first=true;
    Math.random=()=>{if(first){first=false;return (index+.1)/6}return random()};
    try{renderQuestion()}finally{Math.random=random}
  },index);
}

for(const lang of ['en','de']){
  test(`every ${lang} initial-letter task has matching case, one valid answer and deliberate letter narration`,async({page})=>{
    await page.evaluate(lang=>{setLang(lang);setAudioMode('all');startGame('letters')},lang);
    for(let i=0;i<examples[lang].length;i++){
      const [letter,word]=examples[lang][i];
      await task(page,i);await page.clock.runFor(250);
      const info=await page.evaluate(()=>({
        current:state.current,score:state.score,
        labels:[...document.querySelectorAll('.answer')].map(e=>e.querySelector('span:last-child').textContent),
        keys:[...document.querySelectorAll('.answer')].map(e=>e.dataset.key),
        question:document.querySelector('.questionBox h3').textContent,narration:spoken.at(-1)
      }));
      await expect(page.locator('.letterHero')).toHaveText(letter);
      expect(info.current.kind).toBe('initialLetter');
      expect(info.labels).toHaveLength(4);expect(new Set(info.keys).size).toBe(4);
      expect(info.labels.filter(x=>x.startsWith(letter))).toEqual([word]);
      for(const label of info.labels)expect(label[0]).toBe(label[0].toLocaleUpperCase(lang));
      expect(info.narration.lang).toBe(lang);
      expect(info.narration.text).toContain(lang==='en'?`the letter ${letter}`:`dem Buchstaben ${letter}`);
      expect(info.narration.text).not.toMatch(/sound|phoneme|\bLaut(?:e|en)?\b|Buchstabenlaut/);
      expect(info.question).not.toMatch(/sound|phoneme|\bLaut(?:e|en)?\b|Buchstabenlaut/);
      const correct=page.locator(`.answer[data-key="${info.current.correct}"]`);
      await expect(correct.locator("span").last()).toHaveText(word);
      await expect(correct).toHaveAttribute("aria-label",word);
      await page.locator('.speakBtn').click();
      expect(await page.evaluate(()=>state.current)).toEqual(info.current);
      expect(await page.evaluate(()=>spoken.at(-1).text)).toBe(info.current.speak);
      // Every speaker pronounces its displayed word, without selecting an answer.
      for(const speaker of await page.locator('.answerSpeak').all()){
        const label=await speaker.getAttribute('data-speech');await speaker.click();
        expect(await page.evaluate(()=>spoken.at(-1).text)).toBe(label);
        expect(await page.evaluate(()=>state.score)).toBe(info.score);
        expect(await page.evaluate(()=>state.answered)).toBe(false);
      }
      await page.locator('#hintAction').click();
      await expect(page.locator('#feedback')).toHaveText(`💡 ${letter}: ${word}`);
      await page.locator(`.answer:not([data-key="${info.current.correct}"])`).first().click();
      expect(await page.evaluate(()=>state.score)).toBe(info.score);
      await correct.click();
      const feedback=lang==='en'?`Great! The word ${word} starts with the letter ${letter}.`:`Super! Das Wort ${word} beginnt mit dem Buchstaben ${letter}.`;
      await expect(page.locator('#feedback')).toHaveText(feedback);
      expect(await page.evaluate(()=>spoken.at(-1).text)).toBe(feedback);
      expect(await page.evaluate(()=>state.score)).toBe(info.score+1);
      await correct.evaluate(b=>b.click());
      expect(await page.evaluate(()=>state.score)).toBe(info.score+1);
    }
  });
}

test('locale packs resolve shared words with exactly one matching written initial',async({page})=>{
  const failures=await page.evaluate(()=>{
    const errors=[];
    for(const [lang,tasks] of Object.entries(initialLetterTasks))for(const task of tasks){
      const ids=[task.word,...task.distractors];
      if(ids.length!==4||new Set(ids).size!==4||ids.some(id=>!creatures[id]?.[lang]||!creatures[id]?.emoji))errors.push({lang,task,error:'references'});
      else if(ids.filter(id=>initialLetterLabel(id,lang).startsWith(task.letter)).join()!==task.word)errors.push({lang,task,error:'initial'});
    }
    return errors;
  });
  expect(failures).toEqual([]);
});

test('language switching uses the local written-letter mapping in a mixed round and preserves Replay ownership',async({page})=>{
  await page.evaluate(()=>{setAudioMode('all');startGame('mixed');state.items[0]='letters'});
  await task(page,3);await page.clock.runFor(250);
  await expect(page.locator('.letterHero')).toHaveText('H');
  await page.evaluate(()=>{scheduleSpeech('OLD ENGLISH');setLang('de')});
  await task(page,3);await page.clock.runFor(250);
  await expect(page.locator('.letterHero')).toHaveText('P');
  await expect(page.locator('.answer[data-key="horse"] span').last()).toHaveText('Pferd');
  expect(await page.evaluate(()=>spoken.some(x=>x.text==='OLD ENGLISH'))).toBe(false);
  await page.locator('.speakBtn').click();
  expect(await page.evaluate(()=>spoken.at(-1).lang)).toBe('de');
  expect(await page.evaluate(()=>state.score)).toBe(0);
});

test('corrected Letters tutorial ignores old completion but leaves other activity tutorials intact',async({page})=>{
  await page.evaluate(()=>{
    localStorage.setItem('bastiTutorial_v2_en_letters','1');
    localStorage.setItem('bastiTutorial_v2_en_count','1');
    startGame('letters');
  });
  await page.clock.runFor(250);
  expect(await page.evaluate(()=>spoken.at(-1).text)).toContain('Each picture has a word underneath.');
  expect(await page.evaluate(()=>localStorage.getItem('bastiTutorial_v3_en_letters'))).toBeNull();
  await page.evaluate(()=>speechResult(spoken.at(-1).id,true));
  expect(await page.evaluate(()=>audioGuidance.tutorial('letters'))).toBe('');
  expect(await page.evaluate(()=>audioGuidance.tutorial('count'))).toBe('');
  await page.reload();
  expect(await page.evaluate(()=>audioGuidance.tutorial('letters'))).toBe('');
  await page.evaluate(()=>setLang('de'));
  expect(await page.evaluate(()=>audioGuidance.tutorial('letters'))).toContain('Anfangsbuchstaben');
  await page.evaluate(()=>audioGuidance.reset());
  expect(await page.evaluate(()=>audioGuidance.tutorial('letters'))).not.toBe('');
  expect(await page.evaluate(()=>localStorage.getItem('bastiTutorial_v2_en_letters'))).toBeNull();
});
