const assert=require('node:assert/strict'),fs=require('node:fs');
assert.deepEqual(require('../data/food-supplements.json'),require('../app/src/main/assets/food-supplements.js'),'API and app supplement facts must match');
const html=fs.readFileSync('app/src/main/assets/index.html','utf8'),java=fs.readFileSync('app/src/main/java/com/logan/formstrength/MainActivity.java','utf8');
const literal=java.match(/path\.matches\(("(?:[^"\\]|\\.)*")\)/)[1];const allowed=new RegExp('^'+JSON.parse(literal)+'$');
for(const [,src]of html.matchAll(/<script src="([^"]+)"/g)){assert(allowed.test(src),'Native WebView blocks '+src);assert(fs.existsSync('app/src/main/assets'+src),src)}
const ids=[...html.matchAll(/\bid="([^"]+)"/g)].map(x=>x[1]);assert.equal(new Set(ids).size,ids.length);
console.log('Passed supplemental data parity, script existence, native WebView allowlist and DOM uniqueness.');
