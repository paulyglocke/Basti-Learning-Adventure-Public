/* Shared catalogue adapter; preserves legacy English lookup keys. */
(function(root){
const data=root.VerbCatalogue;
root.VerbMotionContent={assets:data.assets,lessons:Object.fromEntries(data.verbs.map(v=>[v.label.en,v.motion]))};
})(typeof window!=="undefined"?window:globalThis);
