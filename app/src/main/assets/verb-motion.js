/* Small presentation-only sprite player. Learning state, speech and progress stay in app.js. */
(function(root){
'use strict';
const C=root.VerbMotionContent;
let active=null;
const clamp=(v,a=0,b=1)=>Math.max(a,Math.min(b,v));
const mix=(a,b,t)=>a+(b-a)*t;
const smooth=t=>t*t*(3-2*t);
const waterModes=new Set(['swim','dive','ink','float','paddle']);
const soundModes=new Set(['sound','perchSound','buzz','copy','hiss']);
const phaseLabels={
 hiss:[['Mouth closed','Tongue out','Hold the sound posture','Rest'],['Maul geschlossen','Zunge heraus','Haltung halten','Ausruhen']],
 jump:[['Crouch','Push off','In the air','Land'],['In die Hocke gehen','Abstoßen','In der Luft','Landen']],
 hop:[['Crouch','Push off','Hop','Land'],['Ducken','Abstoßen','Hüpfen','Landen']],
 snap:[['Closed','Opening','Wide open','Snap shut'],['Geschlossen','Öffnen','Weit geöffnet','Zuschnappen']],
 fly:[['Wings up','Wings move','Wings down','Wings lift'],['Flügel oben','Flügel bewegen','Flügel unten','Flügel anheben']],
 flap:[['Wings up','Wings out','Wings down','Lift again'],['Flügel oben','Flügel ausgestreckt','Flügel unten','Wieder anheben']],
 flutter:[['Wings open','Wings closing','Wings together','Open again'],['Flügel offen','Flügel schließen','Flügel zusammen','Wieder öffnen']],
 dig:[['Reach','Scoop','Move the soil','Dig again'],['Ausstrecken','Schaufeln','Erde bewegen','Weitergraben']],
 fetch:[['Go to the ball','Pick it up','Carry it back','Bring it back'],['Zum Ball laufen','Aufnehmen','Zurücktragen','Zurückbringen']],
 ink:[['Ready','Squeeze','Release ink','Recover'],['Bereit','Zusammenziehen','Tinte ausstoßen','Entspannen']],
 dam:[['Carry a log','Place it','Build the barrier','Water slows'],['Holz tragen','Ablegen','Damm bauen','Wasser wird gebremst']],
 web:[['Start a thread','Attach it','Add strands','A web grows'],['Faden beginnen','Befestigen','Fäden ergänzen','Ein Netz entsteht']],
 hang:[['Hold on with the feet','Hang upside down','Rest','Sleep'],['Mit den Füßen festhalten','Kopfüber hängen','Ausruhen','Schlafen']],
 stomp:[['Lift a foot','Raise it','Plant it firmly','Settle'],['Fuß anheben','Hochheben','Fest aufsetzen','Gewicht verlagern']],
 kick:[['Stand firmly','Lift the leg','Extend the leg','Put it down'],['Sicher stehen','Bein anheben','Bein ausstrecken','Absetzen']],
 curl:[['Standing','Tuck in','Curl up','A round ball'],['Stehen','Einziehen','Einrollen','Eine runde Kugel']],
 copy:[['Listen to a sound','Listen carefully','Copy the sound','Try together'],['Ein Geräusch hören','Gut zuhören','Geräusch nachmachen','Gemeinsam versuchen']],
 pouch:[['A baby in the pouch','The pouch supports it','Stay together','Carry the baby'],['Ein Baby im Beutel','Der Beutel hält es','Zusammenbleiben','Das Baby tragen']]
};
const genericLabels=[['Watch the beginning','Watch the movement','Watch the action','Watch the result'],['Schau auf den Anfang','Schau auf die Bewegung','Schau auf die Aktion','Schau auf das Ergebnis']];
Object.assign(phaseLabels,{
 "sound": [
  [
   "Ready",
   "Mouth opens",
   "Make the sound together",
   "Rest"
  ],
  [
   "Bereit",
   "Maul öffnen",
   "Geräusch gemeinsam machen",
   "Ausruhen"
  ]
 ],
 "perchSound": [
  [
   "Perch",
   "Beak opens",
   "Make the sound together",
   "Rest"
  ],
  [
   "Sitzen",
   "Schnabel öffnen",
   "Geräusch gemeinsam machen",
   "Ausruhen"
  ]
 ],
 "buzz": [
  [
   "Wings lift",
   "Wings move",
   "Wings lower",
   "Lift again"
  ],
  [
   "Flügel anheben",
   "Flügel bewegen",
   "Flügel senken",
   "Wieder anheben"
  ]
 ],
 "swim": [
  [
   "Tail moves",
   "Push through water",
   "Tail returns",
   "Swim on"
  ],
  [
   "Schwanz bewegen",
   "Durchs Wasser schieben",
   "Schwanz zurückführen",
   "Weiterschwimmen"
  ]
 ],
 "dive": [
  [
   "At the edge",
   "Lean forward",
   "Enter the water",
   "Under the water"
  ],
  [
   "Am Rand",
   "Vorbeugen",
   "Ins Wasser eintauchen",
   "Unter Wasser"
  ]
 ],
 "climb": [
  [
   "Hold the trunk",
   "Reach higher",
   "Step up",
   "Grip again"
  ],
  [
   "Stamm festhalten",
   "Höher greifen",
   "Hochsteigen",
   "Wieder festhalten"
  ]
 ],
 "slither": [
  [
   "Bend the body",
   "The bend travels",
   "Push along",
   "Glide forward"
  ],
  [
   "Körper biegen",
   "Die Biegung wandert",
   "Vorwärtsschieben",
   "Vorwärtsgleiten"
  ]
 ],
 "run": [
  [
   "Gather the feet",
   "Push off",
   "Extend the legs",
   "Land"
  ],
  [
   "Beine anziehen",
   "Abstoßen",
   "Beine strecken",
   "Landen"
  ]
 ],
 "peck": [
  [
   "Head up",
   "Lower the beak",
   "Touch the seeds",
   "Lift the head"
  ],
  [
   "Kopf oben",
   "Schnabel senken",
   "Körner berühren",
   "Kopf anheben"
  ]
 ],
 "scratch": [
  [
   "Paw rests",
   "Lift the paw",
   "Touch the post",
   "Draw the paw down"
  ],
  [
   "Pfote ruht",
   "Pfote anheben",
   "Stamm berühren",
   "Pfote nach unten ziehen"
  ]
 ],
 "wag": [
  [
   "Tail lifts",
   "Tail moves across",
   "Tail bends",
   "Tail returns"
  ],
  [
   "Schwanz anheben",
   "Schwanz zur Seite bewegen",
   "Schwanz biegen",
   "Schwanz zurückbewegen"
  ]
 ],
 "balance": [
  [
   "One foot supports",
   "Other leg folds",
   "Hold steady",
   "Keep balancing"
  ],
  [
   "Ein Fuß trägt",
   "Anderes Bein anwinkeln",
   "Ruhig halten",
   "Gleichgewicht halten"
  ]
 ],
 "trunk": [
  [
   "Reach with the trunk",
   "Grip the branch",
   "Lift the branch",
   "Put it down"
  ],
  [
   "Rüssel ausstrecken",
   "Ast greifen",
   "Ast anheben",
   "Ast ablegen"
  ]
 ],
 "leaves": [
  [
   "Feet stay planted",
   "Lift the head",
   "Reach the leaves",
   "Nibble"
  ],
  [
   "Füße bleiben stehen",
   "Kopf anheben",
   "Blätter erreichen",
   "Knabbern"
  ]
 ],
 "sideways": [
  [
   "Face forward",
   "Step to the side",
   "Other legs step",
   "Move sideways"
  ],
  [
   "Nach vorn schauen",
   "Zur Seite treten",
   "Andere Beine bewegen",
   "Seitwärts gehen"
  ]
 ],
 "float": [
  [
   "Lie on the back",
   "Nose above water",
   "Water supports the body",
   "Rest"
  ],
  [
   "Auf dem Rücken liegen",
   "Nase über Wasser",
   "Wasser trägt den Körper",
   "Ausruhen"
  ]
 ],
 "eat": [
  [
   "Hold the food",
   "Take a bite",
   "Chew",
   "Bite again"
  ],
  [
   "Futter halten",
   "Abbeißen",
   "Kauen",
   "Wieder abbeißen"
  ]
 ],
 "waddle": [
  [
   "One foot steps",
   "Shift the weight",
   "Other foot steps",
   "Step again"
  ],
  [
   "Ein Fuß tritt vor",
   "Gewicht verlagern",
   "Anderer Fuß tritt vor",
   "Weitergehen"
  ]
 ],
 "pounce": [
  [
   "Crouch",
   "Push off",
   "Reach forward",
   "Land"
  ],
  [
   "Ducken",
   "Abstoßen",
   "Nach vorn strecken",
   "Landen"
  ]
 ],
 "graze": [
  [
   "Find the grass",
   "Lower the head",
   "Bite the grass",
   "Chew"
  ],
  [
   "Gras finden",
   "Kopf senken",
   "Gras abbeißen",
   "Kauen"
  ]
 ],
 "clap": [
  [
   "Flippers apart",
   "Bring them closer",
   "Flippers touch",
   "Separate again"
  ],
  [
   "Flossen auseinander",
   "Zusammenführen",
   "Flossen berühren sich",
   "Wieder auseinander"
  ]
 ],
 "chase": [
  [
   "Mouse ahead",
   "Run after it",
   "Follow its path",
   "Keep following"
  ],
  [
   "Maus voraus",
   "Hinterherlaufen",
   "Dem Weg folgen",
   "Weiter folgen"
  ]
 ],
 "crawl": [
  [
   "Foot on the ground",
   "Glide forward",
   "Feelers move",
   "Move slowly"
  ],
  [
   "Fuß am Boden",
   "Vorwärtsgleiten",
   "Fühler bewegen",
   "Langsam bewegen"
  ]
 ],
 "carry": [
  [
   "Hold the food",
   "Keep a steady grip",
   "Carry it along",
   "Keep holding"
  ],
  [
   "Futter halten",
   "Gut festhalten",
   "Weitertragen",
   "Weiter festhalten"
  ]
 ],
 "fire": [
  [
   "Breathe in",
   "Open the mouth",
   "Breathe out fire",
   "Rest"
  ],
  [
   "Einatmen",
   "Maul öffnen",
   "Feuer ausatmen",
   "Ausruhen"
  ]
 ],
 "dinostomp": [
  [
   "Lift a foot",
   "Raise it",
   "Plant it firmly",
   "Settle"
  ],
  [
   "Fuß anheben",
   "Hochheben",
   "Fest aufsetzen",
   "Gewicht verlagern"
  ]
 ],
 "neck": [
  [
   "Stand still",
   "Lift the neck",
   "Reach upwards",
   "Relax"
  ],
  [
   "Still stehen",
   "Hals anheben",
   "Nach oben strecken",
   "Entspannen"
  ]
 ]
});
function labels(cfg,lang){return (phaseLabels[cfg.mode]||genericLabels)[lang==='de'?1:0];}
function t(lang,en,de){return lang==='de'?de:en;}
function escape(s){return String(s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));}
function frameAt(cfg,p){
 if(cfg.mode==='snap')return p<350/2800?0:p<600/2800?1:p<850/2800?2:p<1100/2800?3:p<1500/2800?4:p<1540/2800?5:p<1580/2800?6:p<1780/2800?7:0;
 if(cfg.mode==='hop')return cfg.frames[p<.12?0:p<.20?1:p<.28?2:p<.38?3:p<.62?4:p<.72?5:p<.82?6:7];
 if(cfg.mode==='jump')return cfg.frames[p<.12?0:p<.20?1:p<.25?2:p<.32?3:p<.42?4:p<.61?5:p<.70?6:p<.81?7:0];
 if(cfg.mode==='curl')return cfg.frames[p<.68?Math.min(6,Math.floor(p/.68*7)):p<.9?6:7];
 if(cfg.mode==='fetch')return p<.30?cfg.frames[Math.floor(p/.30*18)%6]:p<.4?14:p<.48?15:p<.88?cfg.frames[Math.floor((p-.48)/.4*18)%6]:p<.96?14:15;
 if(cfg.mode==='stomp'&&cfg.animal==='dinosaur')return cfg.frames[p<.15?0:p<.32?1:p<.5?2:3];
 if(cfg.mode==='stomp')return cfg.frames[p<.2?0:p<.5?1:p<.7?2:3];
 return cfg.frames[Math.min(cfg.frames.length-1,Math.floor(p*cfg.frames.length))];
}
function stillTimes(cfg){
 return cfg.mode==='snap'?[0,600/2800,1300/2800,1640/2800]:
 cfg.mode==='jump'||cfg.mode==='hop'?[.14,.28,.51,.74]:cfg.mode==='fetch'?[.12,.38,.68,.94]:[0,.28,.56,.84];
}
function pose(cfg,p){
 let x=320,base=300,size=270,flip=false;
 if(cfg.mode==='jump'||cfg.mode==='hop'){
  size=220;const u=clamp((p-.20)/.5);base-=Math.sin(Math.PI*u)*(cfg.mode==='hop'?58:80);
 } else if(['fly','flap','flutter','buzz'].includes(cfg.mode)){
  size=260;base=285;
 } else if(waterModes.has(cfg.mode)){
  size=240;base=295;
  if(cfg.mode==='ink'){size=205;base=346;}
  if(cfg.mode==='dive'){size=170;x=180+170*clamp((p-.15)/.65);base=165+125*clamp((p-.2)/.6);}
  if(cfg.mode==='float'){size=255;base=175+Math.sin(p*Math.PI*2)*3;}
  if(cfg.mode==='paddle')base=225;
  if(cfg.mode==='swim'||cfg.mode==='paddle')x=mix(180,460,smooth(p));
 } else if(cfg.mode==='hang'){size=215;base=225;}
 else if(cfg.mode==='pouch'){size=330;}
 else if(cfg.mode==='perchSound'){base=280;}
 else if(cfg.mode==='pounce'){
  size=250;const u=clamp((p-.2)/.5);x=mix(200,440,smooth(u));base-=Math.sin(u*Math.PI)*65;
 }
 else if(cfg.mode==='climb'){size=230;x=355;base=315-100*p;}
 else if(cfg.mode==='dam'){size=250;x=285;}
 else if(cfg.mode==='web'){size=190;base=280;}
 else if(cfg.mode==='fetch'){
  size=235;
  if(p<.3)x=mix(170,450,smooth(p/.3));
  else if(p<.48)x=450;
  else if(p<.88){x=mix(450,170,smooth((p-.48)/.4));flip=true;}
  else {x=170;flip=true;}
 } else if(['run','chase','carry','crawl','waddle','sideways','slither','swim','paddle'].includes(cfg.mode)){
  x=mix(170,cfg.mode==='chase'?430:470,smooth(p));
 } else if(cfg.mode==='snap'){
  const ms=p*2800;
  if(ms>=350&&ms<1500)x-=640*.014*smooth(clamp((ms-350)/750));
  else if(ms>=1500&&ms<1580)x=640*(.486+.038*smooth((ms-1500)/80));
  else if(ms>=1580&&ms<2080){const u=(ms-1580)/500;x=640*(.5+.024*Math.exp(-10*u)*Math.cos(2*Math.PI*u));}
 }
 return {x,base,size,flip,frame:frameAt(cfg,p)};
}
function ellipse(ctx,x,y,rx,ry,colour){ctx.fillStyle=colour;ctx.beginPath();ctx.ellipse(x,y,rx,ry,0,0,Math.PI*2);ctx.fill();}
function line(ctx,x1,y1,x2,y2,colour,width=3){ctx.strokeStyle=colour;ctx.lineWidth=width;ctx.lineCap='round';ctx.beginPath();ctx.moveTo(x1,y1);ctx.lineTo(x2,y2);ctx.stroke();}
function log(ctx,x,y,w=85){ctx.fillStyle='#946540';ctx.fillRect(x,y-8,w,16);ellipse(ctx,x,y,8,8,'#C6A478');ellipse(ctx,x+w,y,8,8,'#B18B5D');line(ctx,x+12,y-3,x+w-8,y-3,'#704A2C',2);}
function ball(ctx,x,y){ellipse(ctx,x,y,17,17,'#E3A945');ctx.strokeStyle='#9B6423';ctx.lineWidth=2;ctx.beginPath();ctx.arc(x,y,17,0,Math.PI*2);ctx.stroke();line(ctx,x-12,y-12,x+12,y+12,'#FFF1B8',3);}
function leaf(ctx,x,y,scale=1){ctx.save();ctx.translate(x,y);ctx.scale(scale,scale);ellipse(ctx,0,0,15,7,'#5E9B50');line(ctx,-12,0,13,0,'#376A38',1.5);ctx.restore();}
function mouse(ctx,x,y){ellipse(ctx,x,y,17,9,'#89939E');ellipse(ctx,x+12,y-8,7,7,'#89939E');ellipse(ctx,x+12,y-8,4,4,'#E8B1B9');ellipse(ctx,x+20,y-1,2,2,'#26394A');ctx.strokeStyle='#89939E';ctx.lineWidth=3;ctx.beginPath();ctx.moveTo(x-15,y);ctx.quadraticCurveTo(x-40,y-17,x-40,y);ctx.stroke();}
function scene(ctx,cfg,p,a,foreground=false){
 const water=waterModes.has(cfg.mode);
 if(!foreground){
  ctx.fillStyle=cfg.mode==='hang'||cfg.mode==='perchSound'?'#243D63':water?'#DDF0FA':'#FFF5DF';ctx.fillRect(0,0,640,360);
  if(water){ctx.fillStyle='#82C9E4';ctx.fillRect(0,165,640,195);line(ctx,0,165,640,165,'#F6FDFF',3);}
  else if(!['fly','flap','flutter','buzz','hang','web','perchSound'].includes(cfg.mode)){
   ctx.fillStyle='#DCE8BA';ctx.fillRect(0,300,640,60);line(ctx,0,300,640,300,'#80975B',2);
  }
  if(['fly','flap','flutter','buzz'].includes(cfg.mode)){ellipse(ctx,110,70,45,10,'#FFF');ellipse(ctx,520,110,45,10,'#FFF');}
  if(cfg.mode==='dive'){ctx.fillStyle='#99A49D';ctx.beginPath();ctx.moveTo(95,165);ctx.lineTo(165,140);ctx.lineTo(215,165);ctx.lineTo(220,205);ctx.lineTo(90,205);ctx.closePath();ctx.fill();}
  if(cfg.mode==='hang'||cfg.mode==='perchSound'){
   line(ctx,130,cfg.mode==='hang'?68:280,510,cfg.mode==='hang'?68:280,'#76543F',14);
   ellipse(ctx,550,55,20,20,'#FFF1BF');
  }
  if(cfg.mode==='copy')line(ctx,210,280,430,280,'#956C48',10);
  if(cfg.mode==='climb'||cfg.mode==='scratch'){
   const x=cfg.mode==='climb'?430:420;
   line(ctx,x,45,x,318,'#956C48',35);
   if(cfg.mode==='climb'){line(ctx,x,95,520,65,'#956C48',14);ellipse(ctx,485,45,70,25,'#91B66A');}
  }
  if(cfg.mode==='leaves'){
   line(ctx,390,90,570,90,'#906B46',8);leaf(ctx,397,90);leaf(ctx,425,78);leaf(ctx,451,96);
  }
  if(cfg.mode==='graze'){for(let i=0;i<9;i++)line(ctx,385+i*7,300,381+i*7,282-(i%3)*4,'#5B994B',3);}
  if(cfg.mode==='peck'){[0,1,2].forEach(i=>ellipse(ctx,390+i*9,296,4,2,'#C99542'));}
  if(cfg.mode==='dig'){
   ellipse(ctx,420,303,38,6+clamp(p*2)*9,'#796046');
   for(let i=0;i<6;i++){const u=(p*3+i/6)%1;ellipse(ctx,408-u*65,285-Math.sin(u*Math.PI)*38,4,3,'#AA8661');}
  }
  if(cfg.mode==='dam'){
   const built=clamp(p/.84),waterline=270-45*built;
   ctx.fillStyle='#9FD7E8';ctx.fillRect(365,waterline,275,306-waterline);
   ctx.fillRect(310,286,55,20);
   for(let row=0;row<3;row++)for(let i=0;i<4;i++){
    const x=450+i*48-(p*110)%48;
    line(ctx,x,waterline+10+row*9,x+25,waterline+10+row*9,'#E2F7FC',2);
   }
   if(built<.8)line(ctx,315,294,350-25*built,294,'#E2F7FC',2);
   const n=Math.min(5,Math.floor(p*6));for(let i=0;i<n;i++)log(ctx,365,286-i*16,65);
  }
  if(cfg.mode==='web'){
   line(ctx,170,320,195,170,'#9A7752',7);line(ctx,195,170,232,103,'#9A7752',5);line(ctx,195,170,232,237,'#9A7752',5);
   line(ctx,470,320,445,170,'#9A7752',7);line(ctx,445,170,408,103,'#9A7752',5);line(ctx,445,170,408,237,'#9A7752',5);
   const centre={x:320,y:170},n=Math.floor(clamp(p/.8)*24);
   for(let i=0;i<8;i++)if(i<n)line(ctx,centre.x,centre.y,centre.x+Math.cos(i*Math.PI/4)*125,centre.y+Math.sin(i*Math.PI/4)*95,'#9BABB4',2);
   for(let ring=1;ring<=3;ring++){const r=ring/3;for(let i=0;i<8;i++)if(8+(ring-1)*5+i*.6<n){const t1=i*Math.PI/4,t2=(i+1)*Math.PI/4;line(ctx,320+Math.cos(t1)*125*r,170+Math.sin(t1)*95*r,320+Math.cos(t2)*125*r,170+Math.sin(t2)*95*r,'#9BABB4',1.5);}}
  }
  if(cfg.mode==='fetch'&&p<.48)ball(ctx,472,283);
  if(cfg.mode==='fetch'&&p>.93)ball(ctx,105,283);
  if(cfg.mode==='chase')mouse(ctx,a.x+a.size*.52,292);
 }
 if(foreground){
  if(cfg.mode==='ink'&&p>.27){
   const u=clamp((p-.27)/.5);ctx.globalAlpha=(p>.85?(1-p)/.15:1)*.85;
   [0,1,2,3,4].forEach(i=>ellipse(ctx,a.x+a.size*.25+u*70+i*9,a.base-a.size*.30+Math.sin(i*2)*u*24,8+u*(14+i*2),7+u*(12+i*2),'#26333C'));ctx.globalAlpha=1;
  }
  if(cfg.mode==='fire'&&p>.25&&p<.75){
   const u=Math.sin((p-.25)/.5*Math.PI),x=a.frame===1?392:417,y=167;
   ctx.fillStyle='#EC9540';ctx.beginPath();ctx.moveTo(x,y);ctx.quadraticCurveTo(x+70*u,y-40*u,x+115*u,y);ctx.quadraticCurveTo(x+60*u,y+35*u,x,y);ctx.fill();
   ctx.fillStyle='#FFE5A0';ctx.beginPath();ctx.moveTo(x,y);ctx.lineTo(x+80*u,y);ctx.lineTo(x+25*u,y+12*u);ctx.fill();
  }
  if(cfg.mode==='fetch'&&p>=.48&&p<.93)ball(ctx,a.x-a.size*.32,a.base-a.size*.50);
  if(cfg.mode==='web')line(ctx,a.x-a.size*.2,a.base-a.size*.2,320+Math.cos(p*Math.PI*2)*90,170+Math.sin(p*Math.PI*2)*70,'#8399A6',1.5);
  if(cfg.mode==='stomp'&&p>.48&&p<.7){const u=(p-.48)/.22;ctx.globalAlpha=1-u;ellipse(ctx,340,301,10+u*28,3,'#B9AA81');ctx.globalAlpha=1;}
  if(water){ctx.fillStyle='rgba(103,190,220,.16)';ctx.fillRect(0,190,640,170);}
 }
}
function draw(canvas,cfg,image,asset,p,forcedFrame){
 const ctx=canvas.getContext('2d');if(!ctx)return;
 const W=canvas.width,H=canvas.height,scale=Math.min(W/640,H/360);
 ctx.setTransform(1,0,0,1,0,0);ctx.fillStyle=waterModes.has(cfg.mode)?'#DDF0FA':'#FFF5DF';ctx.fillRect(0,0,W,H);
 ctx.translate((W-640*scale)/2,(H-360*scale)/2);ctx.scale(scale,scale);
 const a=pose(cfg,p);if(forcedFrame!==undefined)a.frame=forcedFrame;
 scene(ctx,cfg,p,a);
 const cw=image.naturalWidth/asset.columns,ch=image.naturalHeight/asset.rows;
 const top=cfg.mode==='hang'?68-a.size*.02:a.base-a.size*.85;
 ctx.save();ctx.translate(a.x,top);if(a.flip)ctx.scale(-1,1);
 ctx.drawImage(image,(a.frame%asset.columns)*cw,Math.floor(a.frame/asset.columns)*ch,cw,ch,-a.size/2,0,a.size,a.size);ctx.restore();
 scene(ctx,cfg,p,a,true);
 return a;
}
function markup(key,lang,description){
 const cfg=C.lessons[key];if(!cfg)throw Error('Missing authored verb motion: '+key);
 return `<div class="verbAnimStage verb-motion-stage" data-verb="${escape(key)}" data-state="loading"><canvas class="animatedAnimal verb-motion-canvas" width="1280" height="720" role="img" aria-label="${escape(description)}"></canvas><div class="verb-picture-steps hidden"></div></div><p class="verb-motion-status" role="status">${t(lang,'Opening pictures…','Bilder werden geöffnet…')}</p><div class="verb-motion-controls"><button type="button" data-motion="play">${t(lang,'Watch again','Noch einmal ansehen')}</button><button type="button" data-motion="pause">${t(lang,'Pause','Pause')}</button><button type="button" data-motion="pictures">${t(lang,'Picture steps','Bildschritte')}</button><button type="button" data-motion="retry" hidden>${t(lang,'Try again','Erneut versuchen')}</button></div>`;
}
class Player {
 constructor(host,key,lang,options){
  this.host=host;this.cfg=C.lessons[key];this.asset=C.assets[this.cfg.animal];this.lang=lang;this.options=options;
  this.stage=host.querySelector('.verb-motion-stage');this.canvas=host.querySelector('.verb-motion-canvas');this.steps=host.querySelector('.verb-picture-steps');this.status=host.querySelector('.verb-motion-status');
  this.elapsed=0;this.last=null;this.raf=0;this.playing=false;this.disposed=false;this.pictureMode=false;this.loaded=false;this.autoplayPending=options.autoplay;
  this.media=root.matchMedia('(prefers-reduced-motion: reduce)');this.onMedia=()=>{this.pause();this.setPictures(this.reduced());};
  if(this.media.addEventListener)this.media.addEventListener('change',this.onMedia);else this.media.addListener(this.onMedia);
  host.querySelector('[data-motion="play"]').onclick=()=>{options.interrupt();this.elapsed=0;this.start();};
  host.querySelector('[data-motion="pause"]').onclick=()=>{this.pause();options.interrupt();};
  host.querySelector('[data-motion="pictures"]').onclick=()=>{options.interrupt();this.setPictures(!this.pictureMode);};
  host.querySelector('[data-motion="retry"]').onclick=()=>this.load();
  this.controls();this.load();
 }
 reduced(){return this.media.matches||!!root.Android?.reducedMotion?.();}
 message(text){if(!this.disposed)this.status.textContent=text;}
 load(){
  this.pause(false);this.stage.dataset.state='loading';this.image=new Image();const requested=this.image;
  requested.onload=()=>{
   if(this.disposed||requested!==this.image)return;
   const a=this.asset;if(requested.naturalWidth%a.columns||requested.naturalHeight%a.rows||requested.naturalWidth>2048||requested.naturalHeight>2048){this.failure();return;}
   this.loaded=true;this.stage.dataset.state='ready';this.host.querySelector('[data-motion="retry"]').hidden=true;
   const autoplay=this.autoplayPending;this.autoplayPending=false;this.setPictures(this.reduced());
   if(autoplay&&!this.reduced()&&this.options.visible())this.start();
  };
  requested.onerror=()=>{if(!this.disposed&&requested===this.image)this.failure();};
  requested.src=this.asset.path;
 }
 failure(){this.loaded=false;this.stage.dataset.state='error';this.message(t(this.lang,'The pictures could not open. Try again or go back.','Die Bilder konnten nicht geöffnet werden. Versuche es erneut oder gehe zurück.'));this.host.querySelector('[data-motion="retry"]').hidden=false;this.controls();}
 controls(){
  this.host.querySelector('[data-motion="play"]').disabled=!this.loaded||this.pictureMode||this.reduced();
  this.host.querySelector('[data-motion="pause"]').disabled=!this.playing;
  this.host.querySelector('[data-motion="pictures"]').disabled=!this.loaded||this.reduced();
  this.host.querySelector('[data-motion="pictures"]').textContent=this.pictureMode?t(this.lang,'Show movement','Bewegung zeigen'):t(this.lang,'Picture steps','Bildschritte');
 }
 render(){if(!this.loaded||this.disposed)return;const p=this.elapsed/this.cfg.duration;const a=draw(this.canvas,this.cfg,this.image,this.asset,p);this.stage.dataset.frame=String(a?.frame);this.stage.dataset.phase=String(p);}
 start(){
  if(!this.loaded||this.disposed||this.pictureMode||this.reduced()||document.hidden||!this.options.visible())return;
  this.playing=true;this.last=null;cancelAnimationFrame(this.raf);this.message(t(this.lang,'Watch the movement.','Schau dir die Bewegung an.'));this.controls();
  const tick=now=>{
   if(!this.playing||this.disposed)return;
   if(this.last!==null)this.elapsed=Math.min(this.cfg.duration-1,this.elapsed+now-this.last);
   this.last=now;this.render();
   if(this.elapsed>=this.cfg.duration-1){this.pause();this.message(t(this.lang,'Watch again whenever you like.','Du kannst die Bewegung jederzeit noch einmal ansehen.'));return;}
   this.raf=requestAnimationFrame(tick);
  };
  this.raf=requestAnimationFrame(tick);
 }
 pause(cancelAutoplay=true){if(cancelAutoplay)this.autoplayPending=false;this.playing=false;cancelAnimationFrame(this.raf);this.last=null;if(this.loaded){this.message(t(this.lang,'Paused. Watch again whenever you like.','Pausiert. Du kannst die Bewegung jederzeit noch einmal ansehen.'));this.controls();}}
 setPictures(value){
  this.pause();this.pictureMode=value||this.reduced();this.canvas.classList.toggle('hidden',this.pictureMode);this.steps.classList.toggle('hidden',!this.pictureMode);this.stage.classList.toggle('showing-pictures',this.pictureMode);
  if(this.loaded&&this.pictureMode){
   const names=labels(this.cfg,this.lang),times=stillTimes(this.cfg);
   this.steps.replaceChildren();
   this.cfg.still.forEach((frame,i)=>{const figure=document.createElement('figure'),canvas=document.createElement('canvas'),caption=document.createElement('figcaption');canvas.width=640;canvas.height=360;canvas.setAttribute('role','img');canvas.setAttribute('aria-label',names[i]);caption.textContent=`${i+1}. ${names[i]}`;figure.append(canvas,caption);this.steps.append(figure);draw(canvas,this.cfg,this.image,this.asset,times[i],frame);});
   this.message(t(this.lang,'Explore the picture steps.','Schau dir die Bildschritte an.'));
  } else this.render();
  this.controls();
 }
 seek(fraction){this.pause();this.elapsed=clamp(fraction,0,.9999)*this.cfg.duration;this.render();return pose(this.cfg,this.elapsed/this.cfg.duration);}
 dispose(){this.pause();this.disposed=true;if(this.media.removeEventListener)this.media.removeEventListener('change',this.onMedia);else this.media.removeListener(this.onMedia);if(this.image){this.image.onload=this.image.onerror=null;this.image=null;}}
}
root.VerbMotion={
 markup,
 mount(host,key,lang,options={}){if(active)active.dispose();active=new Player(host,key,lang,{autoplay:false,interrupt(){},visible:()=>true,...options});return active;},
 suspend(){active?.pause();if(active?.loaded&&active.reduced())active.setPictures(true);},
 unmount(){active?.dispose();active=null;},
 soundLesson:key=>soundModes.has(C.lessons[key]?.mode),
 guide(lang){return t(lang,'Grown-up, make the animal’s sound for Basti. Basti, listen carefully. Now try making that sound together.','Mach Basti als erwachsene Begleitperson das Tiergeräusch vor. Basti, hör gut zu. Versucht jetzt gemeinsam, das Geräusch nachzumachen.');},
 review(fraction){return active?.seek(fraction);},
 state(){return active?{ready:!!active.loaded,playing:active.playing,pictures:active.pictureMode,key:active.cfg.id,frame:frameAt(active.cfg,active.elapsed/active.cfg.duration)}:null;},
 // Pure seams for structural/cadence verification; no session/progress state is exposed.
 pose,frameAt
};
document.addEventListener('visibilitychange',()=>{if(document.hidden)active?.pause();});
})(window);
