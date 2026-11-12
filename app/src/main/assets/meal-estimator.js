/* Deterministic offline parser. Unknown foods/units remain unresolved, never silently omitted. */
(function(root){
 const foods=typeof module==='object'&&module.exports?require('./food-reference.js'):root.FOOD_REFERENCE;
 const aliasMap=new Map();let activeFoods=foods;
 function indexFoods(saved=[]){activeFoods=[...foods,...saved];aliasMap.clear();for(const food of activeFoods)for(const alias of food.aliases)aliasMap.set(normalize(alias),food);aliasPattern=[...aliasMap.keys()].sort((a,b)=>b.length-a.length).map(escape).join('|');}

 const escape=s=>s.replace(/[.*+?^${}()|[\]\\]/g,'\\$&');
 let aliasPattern='';indexFoods();
 function normalize(input){let s=String(input||'').toLowerCase().trim();
  s=s.replace(/(\d+)?\s*([½¼¾])/g,(_,whole,f)=>String(Number(whole||0)+({'½':.5,'¼':.25,'¾':.75}[f])));
  s=s.replace(/(\d+)\s+(\d+)\/(\d+)/g,(_,w,a,b)=>Number(b)?String(Number(w)+Number(a)/Number(b)):'invalid fraction');
  s=s.replace(/(\d+)\/(\d+)/g,(_,a,b)=>Number(b)?String(Number(a)/Number(b)):'invalid fraction');
  s=s.replace(/one and a half/g,'1.5').replace(/half (?:a |an )?/g,'0.5 ').replace(/quarter (?:a |an )?/g,'0.25 ');
  const numbers={one:1,two:2,three:3,four:4,five:5,six:6,seven:7,eight:8,nine:9,ten:10};
  s=s.replace(/\b(one|two|three|four|five|six|seven|eight|nine|ten)\b/g,w=>String(numbers[w]));
  return s.replace(/(\d)([a-z])/g,'$1 $2').replace(/([a-z])(\d)/g,'$1 $2').replace(/\s+/g,' ').trim();
 }
 const units={g:'g',gm:'g',gms:'g',gram:'g',grams:'g',kg:'kg',kilogram:'kg',kilograms:'kg',ml:'ml',millilitre:'ml',millilitres:'ml',milliliter:'ml',milliliters:'ml',l:'l',litre:'l',liter:'l',litres:'l',liters:'l',cup:'cup',cups:'cup',bowl:'bowl',bowls:'bowl',katori:'bowl',katoris:'bowl',tsp:'tsp',teaspoon:'tsp',teaspoons:'tsp',tbsp:'tbsp',tablespoon:'tbsp',tablespoons:'tbsp',piece:'piece',pieces:'piece',pc:'piece',pcs:'piece',no:'piece',nos:'piece',slice:'slice',slices:'slice',glass:'glass',glasses:'glass',serving:'serving',servings:'serving',scoop:'scoop',scoops:'scoop'};
 function quantity(fragment,food){let text=fragment.trim().replace(/\bof\b/g,'').replace(/^an?\b/,'1').replace(/\s+/g,' ').trim();
  if(!text)return {qty:food.defaultQty,unit:food.defaultUnit,assumed:true};
  const m=text.match(/^(?:(\d+(?:\.\d+)?|\.\d+)\s*)?([a-z]+)?$/);if(!m)return{error:'Check the portion for '+food.name+'. Use a number and unit.'};
  const qty=m[1]?Number(m[1]):1;let unit=m[2]?units[m[2]]:food.defaultUnit;let factor=1,assumption='';
  if(!unit)return{error:'Unknown portion “'+(m[2]||text)+'” for '+food.name+'.'};
  if(unit==='kg'){unit='g';factor=1000}if(unit==='l'){unit='ml';factor=1000}if(unit==='bowl'){unit='cup';assumption='Bowl/katori assumed to be 1 reference cup.'}
  if(!Number.isFinite(qty)||qty<=0||qty>10000)return{error:'Use a positive portion for '+food.name+'.'};
  if(food.units[unit]===undefined)return{error:food.name+' does not have a '+unit+' reference. Use '+food.defaultUnit+' or log manually.'};
  return {qty:qty*factor,unit,assumed:!m[1]||!m[2],assumption};
 }
 function estimate(input){const text=normalize(input),items=[],issues=[];if(!text)return{items,issues,total:0,ready:false};
  const duplicated=activeFoods.filter(f=>String(f.id).startsWith('saved-')&&new RegExp('(?:^|\\b)'+escape(normalize(f.name))+'(?=$|\\b)').test(text));if(duplicated.length>1)return{items,issues:['Multiple saved foods have this name. Choose the portion in My foods or rename one.'],total:0,ready:false};
  // Bare "with" is a separator; all non-food text must still parse as a supported quantity.
  const custom=activeFoods.filter(f=>String(f.id).startsWith('saved-')).find(f=>normalize(f.name)===text);
  const chunks=custom?[text]:text.split(/[,;+\n]|\b(?:and|with)\b/).map(x=>x.trim()).filter(Boolean);
  for(const chunk of chunks){const matches=custom?[{0:text,1:normalize(custom.name),index:0}]:[...chunk.matchAll(new RegExp('\\b('+aliasPattern+')\\b','g'))];
   if(!matches.length){issues.push('Not recognized: “'+chunk+'”. Use manual entry for this food.');continue}
   let previous=0;
   for(let i=0;i<matches.length;i++){const match=matches[i],food=aliasMap.get(match[1]),prefix=chunk.slice(previous,match.index).trim(),end=match.index+match[0].length,tail=i===matches.length-1?chunk.slice(end).trim():'';
    previous=end;let q;
    if(prefix&&tail){issues.push('Check the wording around '+food.name+'. Put the portion before the food.');continue}
    q=quantity(prefix||tail,food);if(q.error){issues.push(q.error);continue}
    const calories=q.qty*food.units[q.unit];if(!Number.isFinite(calories)||calories>20000){issues.push('Portion is too large for '+food.name+'.');continue}
    const assumptions=[q.assumption,food.note].filter(Boolean).join(' ');
    items.push({id:food.id,name:food.name,quantity:q.qty,unit:q.unit,calories,proteinG:food.proteinUnits?.[q.unit]===undefined?null:q.qty*food.proteinUnits[q.unit],source:food.source,assumptions,label:q.qty+' '+q.unit+(['g','ml','tsp','tbsp'].includes(q.unit)||q.qty===1?'':'s')+' '+food.name});
   }
  }
  const total=items.reduce((s,i)=>s+i.calories,0);if(items.length>20)issues.push('Please split this into smaller meals (maximum 20 foods).');
  if(total>10000)issues.push('Please check this total. Split very large meals into separate entries.');
  return {items,issues,total:Math.round(total),proteinG:items.length&&items.every(i=>i.proteinG!=null)?Math.round(items.reduce((s,i)=>s+i.proteinG,0)*10)/10:null,knownProteinG:Math.round(items.reduce((s,i)=>s+(i.proteinG||0),0)*10)/10,ready:items.length>0&&!issues.length};
 }
 function search(input){const q=normalize(input);return activeFoods.filter(f=>f.aliases.some(a=>a.includes(q))).slice(0,6).map(f=>({name:f.name,text:f.defaultQty+' '+f.defaultUnit+' '+f.aliases[0]}));}
 const api={estimate,normalize,search,foods,indexFoods};if(typeof module==='object'&&module.exports)module.exports=api;else root.MealEstimator=api;
})(typeof globalThis!=='undefined'?globalThis:this);
