const assert=require('node:assert/strict');const {estimate,search,foods}=require('../app/src/main/assets/meal-estimator.js');
for(const [input,calories]of [['2 rotis and 1 cup dal',360],['2roti 1cup dal',360],['two rotis + half a cup dal',260],['2 idli, 1 cup sambar',260],['150g rice + 2 eggs',350],['rice 150g',195],['1/2 cup dal',100],['1½ cups dal',300],['1 1/2 cups dal',300],['one and a half cups dal',300],['1 bowl dal',200],['2 slices bread',154],['200ml lassi',110],['lassi',110],['2 samosas',400]]){const r=estimate(input);assert(r.ready,input+': '+JSON.stringify(r.issues));assert.equal(r.total,calories,input)}
for(const input of ['1 cup biryani','1 cup dal + mystery food','raw rice','2 kg idli','rice with unknown sauce','0 roti','-2 roti','1/0 cup dal','2 plates dal','fried eggs','1 large dosa','100000 cups rice'])assert(!estimate(input).ready,input);
assert(!estimate('').ready);assert.equal(estimate('2 masala dosa').items[0].id,'masala-dosa');assert.equal(estimate('black tea').items[0].id,'173227');assert.equal(estimate('chicken curry').items[0].id,'chicken-curry');
assert(estimate('dal').items[0].label.includes('1 cup'));assert(estimate('1 bowl dal').items[0].assumptions.includes('Bowl'));
assert.equal(search('rot')[0].name,'Roti');assert(foods.length>=30);assert(!estimate('rice 100g extra').ready);

for(const [input,calories]of [['1 scoop whey protein',106],['30 g whey protein powder',106],['5 g creatine',0],['creatine monohydrate',0],['2 slices brown bread',161],['1 bowl salad',38],['100 g carrot',41],['100 g spinach',23],['100 g capsicum',20],['1 cup sweet corn',132]]){const result=estimate(input);assert(result.ready,input+': '+JSON.stringify(result.issues));assert.equal(result.total,calories,input)}
assert(estimate('1 scoop whey protein').items[0].proteinG>23);
assert.equal(estimate('5 g creatine').items[0].proteinG,0);
assert(estimate('1 bowl salad').items[0].assumptions.includes('dressing'));
assert(search('whey')[0].name.includes('Whey'));
assert(search('brown bread')[0].name.includes('brown bread')||search('brown bread')[0].name.includes('Brown'));

console.log('Passed offline meal parser: aliases, combinations, fractions, units, precedence, defaults and unresolved-food safeguards.');
