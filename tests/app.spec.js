const {test, expect} = require('@playwright/test');
const {pathToFileURL} = require('node:url');
const {resolve} = require('node:path');
const appUrl = pathToFileURL(resolve('app/src/main/assets/index.html')).href;

test.beforeEach(async ({page, context}) => {
  await context.setOffline(true);
  await page.addInitScript(() => {
    window.spoken = [];
    window.Android = {speak: (text, lang) => spoken.push({text, lang}), stopSpeaking: () => {}, vibrate: () => {}};
  });
  await page.goto(appUrl);
  await page.evaluate(() => setSound(false));
});

test('language, sound, round length and number maximum survive reload offline', async ({page}) => {
  await page.locator('#optionsTop').click();
  await page.locator('#germanBtn').click();
  await page.locator('[data-len="10"]').click();
  for (const max of [10, 20, 50, 100]) {
    await page.locator(`[data-nummax="${max}"]`).click();
    await expect(page.locator('#numberMaxDisplay')).toHaveText(String(max));
  }
  await page.locator('#customNumberMax').fill('9999');
  await page.locator('#applyNumberMax').click();
  await page.reload();
  expect(await page.evaluate(() => settings)).toEqual({lang: 'de', sound: false, round: 10, numberMax: 9999});
  await expect(page.locator('#brandTitle')).toHaveText('Bastis Lernabenteuer');
  await page.locator('#optionsTop').click();
  await expect(page.locator('#soundSwitch')).toHaveAttribute('aria-checked', 'false');
  await page.locator('#englishBtn').click();
  await page.reload();
  await expect(page.locator('#brandTitle')).toHaveText("Basti's Learning Adventure");
});

test('quiz lesson button and both return buttons preserve the exact question and score', async ({page}) => {
  const errors = []; page.on('pageerror', e => errors.push(e.message));
  await page.locator('[data-mode="verbs"]').click();
  const before = await page.evaluate(() => ({html: $('gameContent').innerHTML, current: state.current, i: state.i}));
  await page.locator('.learnVerbInline button').click();
  await expect(page.locator('#verbExplorer')).toBeVisible();
  await expect(page.locator('.mainWord')).toHaveText(before.current.verb.v.en);
  await page.locator('#verbExplorerBack').click();
  await expect(page.locator('#game')).toBeVisible();
  expect(await page.evaluate(() => $('gameContent').innerHTML)).toBe(before.html);
  await page.locator(`.answer[data-key="${before.current.correct}"]`).click();
  await page.locator('.learnVerbInline button').click();
  await page.locator('.verbLessonBtn.primary').click();
  await expect(page.locator('#score')).toHaveText('1');
  await expect(page.locator('#nextAction')).toBeEnabled();
  expect(await page.evaluate(() => state.i)).toBe(before.i);
  await page.locator(`.answer[data-key="${before.current.correct}"]`).evaluate(b => b.click());
  await expect(page.locator('#score')).toHaveText('1');
  expect(errors).toEqual([]);
});

test('all 53 bilingual lessons render and replay speech including quoted explanations', async ({page}) => {
  test.setTimeout(180000);
  await page.evaluate(() => setAudioMode('all'));
  const errors = []; page.on('pageerror', e => errors.push(e.message));
  for (const lang of ['en', 'de']) {
    const count = await page.evaluate(lang => {setLang(lang);return verbQs.length}, lang);
    expect(count).toBe(53);
    await page.evaluate(() => openVerbExplorer());
    await expect(page.locator('.verbLibraryCard')).toHaveCount(53);
    for (let i = 0; i < count; i++) {
      await page.locator('.verbLibraryCard').nth(i).click();
      const lesson = await page.evaluate(i => ({title: verbQs[i].v[settings.lang], explanation: verbLessons[verbQs[i].v.en][settings.lang]}), i);
      await expect(page.locator('.mainWord')).toHaveText(lesson.title);
      await expect(page.locator('.verbInfoCard').first()).toContainText(lesson.explanation);
      expect(await page.locator('.animatedAnimal').evaluate(e => getComputedStyle(e).animationName)).not.toBe('none');
      await page.locator('#lessonListen').click();
      expect(await page.evaluate(() => spoken.at(-1).text)).toContain(lesson.explanation);
      expect(await page.evaluate(() => spoken.at(-1).lang)).toBe(lang);
      await page.locator('.verbLessonBtn.primary').click();
    }
  }
  expect(errors).toEqual([]);
});

test('phone/tablet resizing preserves answered questions and open lessons', async ({page}) => {
  await page.locator('[data-mode="verbs"]').click();
  await page.evaluate(() => document.querySelector(`.answer[data-key="${state.current.correct}"]`).click());
  const before = await page.evaluate(() => ({html: $('gameContent').innerHTML, current: state.current, score: state.score}));
  for (const viewport of [{width: 800, height: 1280}, {width: 1280, height: 800}, {width: 360, height: 800}]) {
    await page.setViewportSize(viewport);
    expect(await page.evaluate(() => ({html: $('gameContent').innerHTML, current: state.current, score: state.score}))).toEqual(before);
    await expect(page.locator('#nextAction')).toBeEnabled();
  }
  await page.locator('.learnVerbInline button').click();
  await page.setViewportSize({width: 1280, height: 800});
  await expect(page.locator('.mainWord')).toHaveText(before.current.verb.v.en);
  await page.evaluate(() => navigateBack());
  expect(await page.evaluate(() => state.score)).toBe(1);
});

test('every activity completes in both languages with retries and no negative scoring', async ({page}) => {
  test.setTimeout(180000);
  const errors = [];page.on('pageerror', e => errors.push(e.message));
  for (const lang of ['en', 'de']) {
    for (const mode of ['verbs', 'count', 'math', 'positions', 'letters', 'time', 'mixed']) {
      await page.evaluate(({lang, mode}) => {setLang(lang);setRound(5);startGame(mode)}, {lang, mode});
      for (let i = 0; i < 5; i++) {
        const correct = await page.evaluate(() => state.current.correct);
        await page.locator(`.answer:not([data-key="${correct}"])`).first().click();
        await expect(page.locator('#nextAction')).toBeDisabled();
        expect(await page.evaluate(() => state.score)).toBe(i);
        await page.locator(`.answer[data-key="${correct}"]`).click();
        await expect(page.locator('#nextAction')).toBeEnabled();
        await page.locator('#nextAction').click();
      }
      await expect(page.locator('.finish')).toBeVisible();
      expect(await page.evaluate(() => state.score)).toBe(5);
    }
  }
  expect(errors).toEqual([]);
});

test('number generators respect every ceiling, cap objects at 20, and keep maths within 10', async ({page}) => {
  const result = await page.evaluate(() => {
    const failures = [], counts = {};
    for (const lang of ['en', 'de']) for (const max of [10, 20, 50, 100, 101, 200, 9999]) {
      setLang(lang);setNumberMax(max);
      for (let i = 0; i < 150; i++) {
        renderNumberPractice();
        const choices = [...document.querySelectorAll('.answer')].map(e => +e.dataset.key);
        if (choices.length !== 4 || new Set(choices).size !== 4 || choices.some(n => n < 0 || n > max) || !choices.includes(+state.current.correct)) failures.push({max, choices, current:state.current});
        const objects = document.querySelectorAll('.countCreature').length;
        if (objects > Math.min(20,max) || (max>100 && objects>0)) failures.push({max,objects});
        renderMath();
        if (+state.current.correct > 10 || +state.current.correct < 0 || /more.*more/.test(state.current.speak)) failures.push(state.current);
      }
      counts[lang+max] = 150;
    }
    return {failures,counts};
  });
  expect(result.failures).toEqual([]);
  expect(Object.keys(result.counts)).toHaveLength(14);
});

test('small phone and tablet screens have no overflowing controls or text', async ({page}) => {
  const failures = [];
  for (const viewport of [{width:320,height:700},{width:360,height:800},{width:800,height:1280},{width:1280,height:800}]) {
    await page.setViewportSize(viewport);
    for (const lang of ['en','de']) {
      await page.evaluate(lang => {setLang(lang);goHome()},lang);
      const check = async label => {
        const overflow = await page.evaluate(() => [...document.querySelectorAll('button,h1,h2,h3,.mainWord,.verbEn,.verbDe')].filter(e => e.getClientRects().length).filter(e => {
          const r=e.getBoundingClientRect();return r.left < -1 || r.right > innerWidth+1 || e.scrollWidth>e.clientWidth+2;
        }).map(e => e.textContent));
        if(overflow.length)failures.push({viewport,lang,label,overflow});
      };
      await check('home');
      await page.locator('#optionsTop').click();await check('options');
      await page.evaluate(() => openVerbExplorer());await check('library');
      for(let i=0;i<53;i++) {await page.evaluate(i=>openVerbLesson(i),i);await check('lesson '+i)}
      for(const mode of ['verbs','count','math','positions','letters','time']) {
        await page.evaluate(mode=>startGame(mode),mode);await check(mode);
      }
    }
  }
  expect(failures).toEqual([]);
});

test('Sound Off blocks manual question speech; delayed speech cancels on exit', async ({page}) => {
  await page.locator('[data-mode="count"]').click();
  await page.waitForTimeout(300);
  expect(await page.evaluate(() => spoken)).toEqual([]);
  await page.locator('.speakBtn').click();
  expect(await page.evaluate(() => spoken)).toEqual([]);
  await page.evaluate(() => {spoken.length=0;setSound(true);startGame('verbs');goHome()});
  await page.waitForTimeout(300);
  expect(await page.evaluate(() => spoken)).toEqual([]);
});

test('invalid persisted settings recover and mixed rounds include varied activities', async ({page}) => {
  await page.evaluate(() => {localStorage.setItem('lang','xx');localStorage.setItem('round','NaN');localStorage.setItem('numberMax','NaN')});
  await page.reload();
  expect(await page.evaluate(() => ({lang:settings.lang,round:settings.round,max:settings.numberMax}))).toEqual({lang:'en',round:5,max:10});
  await page.evaluate(() => startGame('mixed'));
  expect(await page.evaluate(() => new Set(state.items).size)).toBe(5);
  await page.evaluate(() => {setRound(10);startGame('mixed')});
  expect(await page.evaluate(() => state.items.length)).toBe(10);
  expect(await page.evaluate(() => new Set(state.items).size)).toBe(6);
});

for (const lang of ['en', 'de']) {
  test(`answer audio never submits via pointer, Enter or Space (${lang})`, async ({page}) => {
    for (const mode of ['verbs','count','math','positions','letters','time','mixed']) {
      await page.evaluate(([lang,mode]) => {setLang(lang);setAudioMode('all');startGame(mode);stopSpeech();spoken.length=0}, [lang,mode]);
      const correct = await page.evaluate(() => state.current.correct);
      const answer = page.locator(`.answer[data-key="${correct}"]`);
      const speaker = answer.locator('..').locator('.answerSpeak');
      const label = await speaker.getAttribute('data-speech');
      // Real sibling buttons: no interactive ancestor and native keyboard semantics.
      expect(await speaker.evaluate(b => b.parentElement.closest('button,[role="button"]'))).toBeNull();
      for (const action of ['click','Enter','Space']) {
        await page.evaluate(() => spoken.length=0);
        if(action==='click') await speaker.click();
        else {await speaker.focus();await page.keyboard.press(action)}
        expect(await page.evaluate(() => ({score:state.score,answered:state.answered,i:state.i,spoken}))).toEqual({score:0,answered:false,i:0,spoken:[{text:label,lang}]});
        await expect(page.locator('#feedback')).toBeEmpty();
        await expect(page.locator('#nextAction')).toBeDisabled();
      }
      await answer.focus();
      await page.keyboard.press('Enter');
      await expect(page.locator('#score')).toHaveText('1');
      await expect(answer).toBeDisabled();
      // Correctness locks only answer selection; listening remains available.
      await page.evaluate(() => spoken.length=0);
      await speaker.click();
      expect(await page.evaluate(() => ({score:state.score,spoken}))).toEqual({score:1,spoken:[{text:label,lang}]});
    }
  });
}

test('Space selects an answer once; Tab visits selection then separate audio', async ({page}) => {
  await page.evaluate(() => {startGame('count');stopSpeech()});
  const key=await page.evaluate(()=>state.current.correct);
  const answer=page.locator(`.answer[data-key="${key}"]`);
  await answer.focus();await page.keyboard.press('Tab');
  await expect(answer.locator('..').locator('.answerSpeak')).toBeFocused();
  await answer.focus();await page.keyboard.press('Space');
  await expect(page.locator('#score')).toHaveText('1');
  await answer.evaluate(b=>{b.click();b.click()});
  await expect(page.locator('#score')).toHaveText('1');
});
