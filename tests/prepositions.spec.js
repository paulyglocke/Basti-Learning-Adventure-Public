const {test,expect}=require('@playwright/test');
const {pathToFileURL}=require('node:url');
const {resolve}=require('node:path');

const relations=['on','under','behind','nextTo','in','between'];
const expected={
  on:{object:'rock',count:1,en:'on the rock',de:'auf dem Stein'},
  under:{object:'table',count:1,en:'under the table',de:'unter dem Tisch'},
  behind:{object:'rock',count:1,en:'behind the rock',de:'hinter dem Stein'},
  nextTo:{object:'rock',count:1,en:'next to the rock',de:'neben dem Stein'},
  in:{object:'box',count:1,en:'in the box',de:'in der Kiste'},
  between:{object:'rock',count:2,en:'between the two rocks',de:'zwischen den beiden Steinen'}
};
const subjects={en:['The snake','The dinosaur','The dragon','The crocodile'],de:['Die Schlange','Der Dinosaurier','Der Drache','Das Krokodil']};

test.beforeEach(async({page,context})=>{
  await context.setOffline(true);
  await page.addInitScript(()=>{
    window.spoken=[];
    window.Android={speak(text,lang){spoken.push({text,lang})},stopSpeaking(){},vibrate(){}};
  });
  await page.goto(pathToFileURL(resolve('app/src/main/assets/index.html')).href);
  await page.clock.install();
});

// Fix only the first two random draws (relation and animal). Choices still shuffle.
async function question(page,relation,animal=0){
  await page.evaluate(({relation,animal})=>{
    const random=Math.random;let calls=0;
    Math.random=()=>++calls===1?(relation+.1)/6:calls===2?(animal+.1)/4:random();
    try{renderQuestion()}finally{Math.random=random}
  },{relation:relations.indexOf(relation),animal});
}

for(const lang of ['en','de']){
  test(`all six relations and four animals agree across scene, choices, speech, hint and feedback (${lang})`,async({page})=>{
    await page.evaluate(lang=>{setLang(lang);setAudioMode('all');startGame('positions')},lang);
    for(const relation of relations)for(let animal=0;animal<4;animal++){
      await question(page,relation,animal);
      await page.clock.runFor(250);
      const result=await page.evaluate(()=>({
        current:state.current,
        keys:[...document.querySelectorAll('.answer')].map(b=>b.dataset.key),
        labels:[...document.querySelectorAll('.answer')].map(b=>b.textContent),
        narration:spoken.at(-1),sceneLabel:document.querySelector('.visualScene').getAttribute('aria-label')
      }));
      const sentence=`${subjects[lang][animal]} ${lang==='de'?'ist':'is'} ${expected[relation][lang]}.`;
      expect(result.current.correct).toBe(relation);
      expect(result.current.scene.object).toBe(expected[relation].object);
      expect(result.current.scene.referenceCount).toBe(expected[relation].count);
      expect(result.keys).toHaveLength(4);expect(new Set(result.keys).size).toBe(4);
      expect(result.keys).toContain(relation);
      expect(result.keys).toEqual(result.current.choices);
      expect(result.narration.text).toContain(`${lang==='de'?'Wähle':'Choose'}: ${result.labels.join(', ')}.`);
      expect(result.narration.lang).toBe(lang);
      expect(result.sceneLabel).toBe(sentence);
      await expect(page.locator(`.positionObject.${expected[relation].object}`)).toHaveCount(expected[relation].count);
      const before=await page.evaluate(()=>JSON.stringify(state.current));
      await page.locator('.speakBtn').click();
      expect(await page.evaluate(()=>spoken.at(-1).text)).toBe(result.current.speak);
      expect(await page.evaluate(()=>JSON.stringify(state.current))).toBe(before);
      await page.locator('#hintAction').click();
      await expect(page.locator('#feedback')).toHaveText(`💡 ${expected[relation][lang]}`);
      const speaker=page.locator(`.answer[data-key="${relation}"]`).locator('..').locator('.answerSpeak');
      const score=await page.evaluate(()=>state.score);
      await speaker.click();expect(await page.evaluate(()=>state.score)).toBe(score);
      expect(await page.evaluate(()=>spoken.at(-1).text)).toBe(await speaker.getAttribute('data-speech'));
      await page.locator(`.answer:not([data-key="${relation}"])`).first().click();
      expect(await page.evaluate(()=>state.score)).toBe(score);
      await page.locator(`.answer[data-key="${relation}"]`).click();
      const feedback=`${lang==='de'?'Super!':'Great!'} ${sentence}`;
      await expect(page.locator('#feedback')).toHaveText(feedback);
      expect(await page.evaluate(()=>spoken.at(-1))).toEqual({text:feedback,lang});
      expect(await page.evaluate(()=>state.score)).toBe(score+1);
    }
  });
}

test('scene geometry preserves the taught relation on narrow phones and tablets',async({page})=>{
  for(const viewport of [{width:320,height:700},{width:360,height:800},{width:800,height:1280},{width:1280,height:800}]){
    await page.setViewportSize(viewport);
    await page.evaluate(()=>{setAudioMode('off');startGame('positions')});
    for(const relation of relations){
      await question(page,relation);
      const g=await page.evaluate(()=>{
        const rect=e=>{const r=e.getBoundingClientRect();return {left:r.left,right:r.right,top:r.top,bottom:r.bottom,x:r.x+r.width/2,y:r.y+r.height/2}};
        const animal=document.querySelector('.sceneAnimal'),objects=[...document.querySelectorAll('.positionObject')],scene=document.querySelector('.visualScene');
        return {animal:rect(animal),objects:objects.map(rect),scene:rect(scene),animalZ:+getComputedStyle(animal).zIndex,objectZ:+getComputedStyle(objects[0]).zIndex,frontZ:+getComputedStyle(scene,'::after').zIndex};
      });
      const a=g.animal,o=g.objects[0];
      for(const r of [a,...g.objects]){
        expect(r.left,`${relation} left at ${viewport.width}`).toBeGreaterThanOrEqual(g.scene.left);
        expect(r.right,`${relation} right at ${viewport.width}`).toBeLessThanOrEqual(g.scene.right);
        expect(r.top).toBeGreaterThanOrEqual(g.scene.top);expect(r.bottom).toBeLessThanOrEqual(g.scene.bottom);
      }
      if(relation==='on'){expect(a.y).toBeLessThan(o.top);expect(Math.abs(a.bottom-o.top)).toBeLessThan(10)}
      if(relation==='under'){expect(a.top).toBeGreaterThan(o.bottom);expect(a.left).toBeGreaterThan(o.left+16);expect(a.right).toBeLessThan(o.right-16);expect(a.bottom).toBeLessThan(o.top+97)}
      if(relation==='behind'){expect(g.animalZ).toBeLessThan(g.objectZ);expect(a.top).toBeLessThan(o.top);expect(a.bottom).toBeGreaterThan(o.top)}
      if(relation==='nextTo')expect(a.right).toBeLessThan(o.left);
      if(relation==='in'){expect(a.left).toBeGreaterThan(o.left);expect(a.right).toBeLessThan(o.right);expect(a.y).toBeGreaterThan(o.top);expect(g.frontZ).toBeGreaterThan(g.animalZ)}
      if(relation==='between'){expect(a.left).toBeGreaterThan(o.right);expect(a.right).toBeLessThan(g.objects[1].left)}
    }
  }
});
