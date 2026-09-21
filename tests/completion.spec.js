const {test,expect}=require('@playwright/test');
const {pathToFileURL}=require('node:url');
const {resolve}=require('node:path');

test.beforeEach(async({page,context})=>{
  await context.setOffline(true);
  await page.addInitScript(()=>{
    window.spoken=[];window.homeCalls=0;
    window.Android={speak(text,lang){spoken.push({text,lang})},stopSpeaking(){},vibrate(){},onWebHome(){homeCalls++}};
  });
  await page.goto(pathToFileURL(resolve('app/src/main/assets/index.html')).href);
});

async function complete(page,{lang='en',round=10,score=10,shell=false}={}){
  await page.evaluate(({lang,round,score,shell})=>{
    setLang(lang);setAudioMode('off');setRound(round);
    if(shell){window.__shellMode=null;startShellMode('count')}else startGame('count');
    state.i=round;state.score=score;
    window.scrollTo(0,document.body.scrollHeight);finish();
  },{lang,round,score,shell});
}

test('completion wraps every earned star and immediately exposes actions across screen sizes and languages',async({page})=>{
  const failures=[];
  for(const viewport of [{width:320,height:480},{width:360,height:640},{width:568,height:320},{width:640,height:240},{width:800,height:1280},{width:1280,height:800}]){
    await page.setViewportSize(viewport);
    for(const lang of ['en','de'])for(const [round,score] of [[5,0],[5,2],[5,5],[10,10]]){
      await complete(page,{lang,round,score});
      const result=await page.evaluate(()=>{
        const box=e=>{const r=e.getBoundingClientRect();return {left:r.left,right:r.right,top:r.top,bottom:r.bottom,width:r.width,height:r.height}};
        const stars=document.querySelector('.stars'),bounds=box(stars);
        const clipped=[...stars.children].filter(e=>{const r=box(e);return r.left<bounds.left-1||r.right>bounds.right+1||r.top<bounds.top-1||r.bottom>bounds.bottom+1});
        const inaccessible=[...document.querySelectorAll('.completionActions button,.completionHeading button')].filter(e=>{
          const r=box(e),top=document.elementFromPoint(r.left+r.width/2,r.top+r.height/2);
          return r.left<0||r.right>innerWidth||r.top<0||r.bottom>innerHeight||r.height<48||!e.contains(top);
        });
        return {stars:stars.children.length,clipped:clipped.length,inaccessible:inaccessible.map(e=>e.textContent),horizontalOverflow:document.documentElement.scrollWidth>innerWidth,scrollY,rows:new Set([...stars.children].map(e=>e.offsetTop)).size};
      });
      if(result.stars!==score||result.clipped||result.inaccessible.length||result.horizontalOverflow||result.scrollY)failures.push({viewport,lang,round,score,result});
      if(viewport.width===320&&score===10)expect(result.rows).toBeGreaterThan(1);
      await expect(page.locator('#game>.actions')).toBeHidden();
      await expect(page.locator('#game>.headerRow')).toBeHidden();
      await expect(page.locator('.finish p')).toHaveText(lang==='de'?`Du hast ${score} Sterne von ${round} gesammelt.`:`You earned ${score} stars out of ${round}.`);
    }
  }
  expect(failures).toEqual([]);
});

test('large text and reduced motion retain reachable actions in the native-sized web surface',async({page})=>{
  await page.emulateMedia({reducedMotion:'reduce'});
  await page.addStyleTag({content:'html{font-size:200%}'});
  for(const viewport of [{width:320,height:480},{width:568,height:320},{width:640,height:240}]){
    await page.setViewportSize(viewport);
    await complete(page,{lang:'de',shell:true});
    const result=await page.locator('.completionActions').evaluate(e=>({bottom:e.getBoundingClientRect().bottom,height:innerHeight,overflow:document.documentElement.scrollWidth>innerWidth}));
    expect(result.bottom).toBeLessThanOrEqual(result.height);
    expect(result.overflow).toBe(false);
    await expect(page.locator('.rewardStar')).toHaveCount(10);
    await page.locator('.completionActions .homeBtn').click();
    await expect(page.locator('#home')).toBeVisible();
  }
});

test('Continue and Home work before any balloon interaction and restore ordinary quiz layout',async({page})=>{
  await page.setViewportSize({width:568,height:320});
  await page.clock.install();
  await complete(page);
  await expect(page.locator('.balloon.popped')).toHaveCount(0);
  await page.locator('.completionActions .nextBtn').click();
  expect(await page.evaluate(()=>({mode:state.mode,i:state.i,score:state.score,round:state.items.length,complete:document.body.classList.contains('completionView')})))
    .toEqual({mode:'count',i:0,score:0,round:10,complete:false});
  await expect(page.locator('#game>.actions')).toBeVisible();
  await expect(page.locator('#game>.headerRow')).toBeVisible();
  await expect(page.locator('.topbar')).toBeVisible();
  await complete(page);
  await page.locator('.completionActions .homeBtn').click();
  await expect(page.locator('#home')).toBeVisible();
  await expect(page.locator('.topbar')).toBeVisible();
  expect(await page.evaluate(()=>homeCalls)).toBe(1);
});

test('keyboard reaches Replay, Continue and Home before optional balloons; replay uses the summary',async({page})=>{
  await complete(page,{lang:'de',score:5});
  await expect(page.locator('#completionTitle')).toBeFocused();
  await page.keyboard.press('Tab');await expect(page.locator('.completionHeading .speakBtn')).toBeFocused();
  await page.evaluate(()=>setAudioMode('questions'));
  await page.keyboard.press('Enter');
  expect(await page.evaluate(()=>spoken.at(-1))).toEqual({text:'Abenteuer geschafft! Du hast 5 Sterne von 10 gesammelt.',lang:'de'});
  await page.keyboard.press('Tab');await expect(page.locator('.completionActions .nextBtn')).toBeFocused();
  await page.keyboard.press('Tab');await expect(page.locator('.completionActions .homeBtn')).toBeFocused();
  await page.keyboard.press('Enter');await expect(page.locator('#home')).toBeVisible();
});
