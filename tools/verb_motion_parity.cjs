// Frozen comparison samples from the retained, reviewed web presentation.
// The native runtime does not execute this file or JavaScript.
const fs=require('fs'),path=require('path'),vm=require('vm');
const root=path.resolve(__dirname,'..'),context={document:{addEventListener(){}}};context.window=context;vm.createContext(context);
for(const file of ['verb-catalogue.js','verb-motion-content.js','verb-motion.js'])vm.runInContext(fs.readFileSync(path.join(root,'app/src/main/assets',file),'utf8'),context);
const lines=['id\tphase\tframe\tx\tbase\tsize\tflip'];
for(const v of context.VerbCatalogue.verbs)for(const p of [0,.12,.16,.2,.28,.38,.48,.56,.68,.74,.84,.94,.999]){
 const a=context.VerbMotion.pose(v.motion,p);lines.push([v.id,p,a.frame,a.x,a.base,a.size,a.flip].join('\t'));
}
const output=path.join(root,'content/verb-motion-parity.tsv'),text=lines.join('\n')+'\n';
if(process.argv.includes('--check')){if(fs.readFileSync(output,'utf8')!==text)throw Error('Regenerate verb-motion-parity.tsv');}else fs.writeFileSync(output,text);
