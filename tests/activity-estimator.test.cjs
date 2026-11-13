const assert=require('node:assert/strict'),a=require('../app/src/main/assets/activity-estimator.js');
let r=a.estimate('30 min walking',79);assert(r.ready);assert.equal(r.items[0].activeCalories,116);assert.equal(r.items[0].grossCalories,158);
r=a.estimate('45 min weights + 20 min cycling',79);assert(r.ready);assert.equal(r.items.length,2);assert.equal(r.items[0].met,3.5);assert.equal(r.items[1].met,4);
assert.equal(a.estimate('1 hour yoga',79).items[0].minutes,60);assert.equal(a.estimate('I walked for 30 minutes',79).items[0].minutes,30);
assert.equal(a.estimate('30 min brisk walking',79).items[0].met,4.8);assert.equal(a.estimate('30 min heavy weights',79).items[0].met,6);
for(const text of ['30 min swimming','walking 5 km','walking 30 min uphill','walking 30 min at 6km/h','0 min walking','721 min walking','-30 min walking','30 min walking + mystery','30 min walking with heavy backpack'])assert(!a.estimate(text,79).ready,text);
assert(!a.estimate('30 min walking','').ready);assert(!a.estimate('30 min walking',500).ready);assert(!a.estimate('30 min walking',NaN).ready);
for(const ref of a.refs){const r=a.estimate('20 min '+ref.aliases[0],70);assert(r.ready,ref.name);assert(r.items[0].grossCalories>=r.items[0].activeCalories)}
console.log('Passed activity parsing, MET assumptions, active/gross energy, weight and duration validation, and unknown detail safeguards.');
