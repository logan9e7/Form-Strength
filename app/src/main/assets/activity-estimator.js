/* Adult Compendium 2024 MET references. See ACTIVITY_SOURCES.md. */
(function(root){
 const refs=[
 ['walk-slow','Walking · slow',2.8,'17152',['slow walking','slow walk']],['walk-brisk','Walking · brisk',4.8,'17200',['brisk walking','brisk walk']],['walk','Walking · moderate',3.8,'17190',['moderate walking','moderate walk','walking','walked','walk']],
 ['run','Running · 8 km/h',8.5,'12030',['running','ran','run']],['jog','Jogging · general',7.5,'12020',['jogging','jog']],
 ['weights','Weights · moderate',3.5,'02054',['moderate weights','weightlifting','weight training','weight lifting','lifting weights','strength training','weights','gym']],['weights-hard','Weights · vigorous',6,'02050',['vigorous weights','heavy weights']],
 ['cycle','Cycling · leisure',4,'01010',['cycling','bicycling','bike','cycle']],['cycle-moderate','Cycling · moderate',7,'01016',['moderate cycling']],
 ['yoga','Yoga · general',2.3,'02175',['yoga']],['stretch','Stretching · mild',2.3,'02101',['stretching','mobility']],['pilates','Pilates · general',2.8,'02105',['pilates']],
 ['stairs','Stair climbing · general',6.8,'17131',['stair climbing','stairs']],['clean','House cleaning · moderate',3.3,'05030',['house cleaning','cleaning']],['mop','Mopping · moderate',3.5,'05021',['mopping']],
 ['rope','Rope skipping · general',11,'02068',['rope skipping','skipping rope','jump rope']],['elliptical','Elliptical · moderate',5,'02048',['elliptical']],['bodyweight','Bodyweight · general',3,'02056',['bodyweight','body weight exercises']],['hiit','HIIT · moderate',7,'02210',['hiit']]
 ].map(([id,name,met,code,aliases])=>({id,name,met,code,aliases}));
 const esc=s=>s.replace(/[.*+?^${}()|[\]\\]/g,'\\$&');
 function calories(met,kg,minutes){if(![met,kg,minutes].every(Number.isFinite)||met<1||met>30||kg<30||kg>250||minutes<=0||minutes>720)throw Error('Check weight, duration and activity intensity.');return {activeCalories:Math.round((met-1)*3.5*kg/200*minutes),grossCalories:Math.round(met*3.5*kg/200*minutes)};}
 function estimate(input,weight){const text=String(input||'').toLowerCase().trim().replace(/[.!?]$/,''),items=[],issues=[];const kg=Number(weight);if(!text)return{ready:false,items,issues};if(!Number.isFinite(kg)||kg<30||kg>250)issues.push('Enter the body weight used for this estimate.');
  for(const part of text.split(/[,;+\n]|\band\b/).map(s=>s.trim()).filter(Boolean)){
   const duration=[...part.matchAll(/(\d+(?:\.\d+)?|\.\d+)\s*(hours?|hrs?|h|minutes?|mins?|min|m)\b/g)];
   if(!duration.length){issues.push('Add a duration, for example “30 min walking”.');continue}
   let minutes=0,remaining=part;for(const d of duration){minutes+=Number(d[1])*(/^(h|hr|hour)/.test(d[2])?60:1);remaining=remaining.replace(d[0],' ')}remaining=remaining.replace(/^(?:i\s+)?(?:did\s+|went\s+for\s+(?:a\s+)?)?/,'').replace(/\bfor\b/g,'').replace(/\s+/g,' ').trim();
   const ref=refs.flatMap(r=>r.aliases.map(a=>({r,a}))).sort((a,b)=>b.a.length-a.a.length).find(({a})=>new RegExp('^'+esc(a)+'$').test(remaining))?.r;
   if(!ref){issues.push('Choose a listed activity or describe it more simply: “'+remaining+'”. Speed, incline, reps and distance need a matching reference; they are not guessed.');continue}
   if(minutes<=0||minutes>720){issues.push('Use 0.1–720 minutes per activity.');continue}
   if(!Number.isFinite(kg)||kg<30||kg>250)continue;
   items.push({...ref,description:part,minutes,weightKg:kg,...calories(ref.met,kg,minutes)});
  }
  if(items.length>10)issues.push('Log at most 10 activities together.');return{items,issues,ready:items.length>0&&!issues.length};
 }
 const api={refs,estimate,calories};if(typeof module==='object'&&module.exports)module.exports=api;else root.ActivityEstimator=api;
})(typeof globalThis!=='undefined'?globalThis:this);
