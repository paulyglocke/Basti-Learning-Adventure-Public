// Bounded legacy-only checkpoint. No WebView/DOM objects, timers or speech queue
// are serialized. Question random draws reproduce the exact generated scene and
// choice order; the original verb record preserves load-time fallback distractors.
const LEGACY_SESSION_VERSION=1;
const legacyModes=["verbs","count","math","positions","letters","time","mixed"];
function openOptions(){
 if(activeScreen==="options")return;
 optionsReturn={screen:activeScreen,scroll:scrollY};
 suspendAudio();show("options");applyLanguage();checkpointSession();
}
function closeOptions(){
 if(activeScreen!=="options")return;
 const origin=optionsReturn||{screen:"home",scroll:0};optionsReturn=null;
 show(origin.screen);applyLanguage();
 document.body.classList.toggle("completionView",origin.screen==="game"&&!!state.complete);
 window.scrollTo(0,origin.scroll);checkpointSession();
}
function setShellActive(active){
 if(shellSuspended===!active)return;
 shellSuspended=!active;suspendAudio();
 document.body.classList.toggle("sessionSuspended",!active);
 checkpointSession();
}
function serializeLegacySession(){
 return JSON.stringify({version:LEGACY_SESSION_VERSION,lang:settings.lang,
  screen:activeScreen,lesson:activeLesson,optionsReturn,state,scroll:scrollY});
}
function publishSession(){
 if(restoringSession)return;
 try{if(window.Android?.saveSession)Android.saveSession(serializeLegacySession())}catch(e){}
}
function validLegacySession(s){
 const screens=["home","game","verbExplorer","options"];
 if(s?.version!==LEGACY_SESSION_VERSION||!["en","de"].includes(s.lang)||!screens.includes(s.screen))return false;
 if(s.optionsReturn&&(!screens.slice(0,3).includes(s.optionsReturn.screen)||!Number.isFinite(s.optionsReturn.scroll)))return false;
 if(s.screen==="options"&&!s.optionsReturn)return false;
 const q=s.state;
 if(!q||!Array.isArray(q.items)||q.items.length>10||q.items.some(x=>!legacyModes.includes(x)||x==="mixed"))return false;
 if(!Number.isInteger(q.i)||q.i<0||q.i>q.items.length||!Number.isInteger(q.score)||q.score<0||q.score>q.items.length||typeof q.answered!=="boolean")return false;
 const needsQuiz=s.screen==="game"||s.optionsReturn?.screen==="game"||s.lesson?.fromQuiz;
 if(needsQuiz&&(!legacyModes.includes(q.mode)||![5,10].includes(q.items.length)))return false;
 if(q.items.length){
  if(typeof q.complete!=="boolean"||q.complete!==(q.i===q.items.length)||q.score>q.i+(q.answered?1:0))return false;
  if(!legacyModes.includes(q.mode)||!q.current||typeof q.current.speak!=="string")return false;
  if(!q.complete&&(!Array.isArray(q.questionRandom)||q.questionRandom.length>2048||q.questionRandom.some(n=>!Number.isFinite(n)||n<0||n>=1)||!Number.isInteger(q.questionNumberMax)||q.questionNumberMax<10||q.questionNumberMax>9999))return false;
 }
 if(s.lesson&&(!Number.isInteger(s.lesson.idx)||!verbQs[s.lesson.idx]||typeof s.lesson.fromQuiz!=="boolean"))return false;
 return Number.isFinite(s.scroll)&&s.scroll>=0;
}
function restoreLegacySession(json){
 if(typeof json!=="string"||json.length>100000)return false;
 let saved;try{saved=JSON.parse(json)}catch(e){return false}
 if(!validLegacySession(saved))return false;
 const language=settings.lang;
 restoringSession=true;
 try{
  suspendAudio();state=saved.state;activeLesson=null;optionsReturn=saved.optionsReturn;
  // Use the current selected language, preserving question draws, range and score.
  if(state.items.length){if(state.complete)finish();else renderQuestion(true)}
  if(saved.lesson)openVerbLesson(saved.lesson.idx,saved.lesson.fromQuiz);
  else if(saved.screen==="verbExplorer"||saved.optionsReturn?.screen==="verbExplorer")renderVerbLibrary();
  show(saved.screen);applyLanguage();
  document.body.classList.toggle("completionView",saved.screen==="game"&&!!state.complete);
  window.scrollTo(0,saved.scroll);
  return true;
 }catch(e){
  state={mode:null,items:[],i:0,score:0,answered:false,current:null};activeLesson=null;optionsReturn=null;show("home");return false;
 }finally{settings.lang=language;restoringSession=false;stopSpeech();checkpointSession()}
}
function applyShellSettings(config){
 const muted=restoringSession;restoringSession=true;
 try{
  syncTutorialReset(config.tutorialResetEpoch);
  setAudioMode(config.audioMode);setLang(config.lang);setRound(config.round);setNumberMax(config.numberMax);
 }finally{restoringSession=muted;stopSpeech();checkpointSession()}
}
function bootLegacy(mode,config,snapshot=null,active=true){
 restoringSession=true;
 setShellActive(active);applyShellSettings(config);
 if(snapshot!==null){
  window.__shellMode=mode;document.querySelector(".topbar").style.display="none";
  if(!restoreLegacySession(snapshot)){restoringSession=false;return false;}
 }else {restoringSession=false;startShellMode(mode)}
 restoringSession=false;checkpointSession();return true;
}
document.addEventListener("click",()=>checkpointSession());
document.addEventListener("visibilitychange",()=>{if(document.hidden){suspendAudio();checkpointSession()}});
