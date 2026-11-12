/* Pure tracking calculations; shared by the UI and tests. */
(function(root){
 function nutrients(entries){let calories=0,protein=0,known=0;for(const e of entries){calories+=e.calories*e.servings;if(typeof e.proteinG==='number'){protein+=e.proteinG*e.servings;known++}}return{calories,protein:Math.round(protein*10)/10,known,unknown:entries.length-known,count:entries.length};}
 function goal(start,current,target){if(![start,current,target].every(n=>typeof n==='number')||start===target)return null;return Math.max(0,Math.min(100,(current-start)/(target-start)*100));}
 function savedReference(e){return{id:'saved-'+e.id,name:e.name,aliases:[e.name.toLowerCase()],defaultUnit:'serving',defaultQty:1,units:{serving:e.calories*e.servings},proteinUnits:typeof e.proteinG==='number'?{serving:e.proteinG*e.servings}:undefined,note:'Your saved portion: '+e.servings+' × '+e.portion+'. Review the amount.',source:'Your saved food'};}
 const api={nutrients,goal,savedReference};if(typeof module==='object'&&module.exports)module.exports=api;else root.TrackerCore=api;
})(typeof globalThis!=='undefined'?globalThis:this);
