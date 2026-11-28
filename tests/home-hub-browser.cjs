// Development browser acceptance with a disposable local bridge, not Android runtime evidence.
const assert=require('node:assert/strict');
const {chromium}=require('playwright');
const {spawn}=require('node:child_process');
(async()=>{
 const server=spawn('node',['scripts/preview-meals.mjs','--port','4186'],{cwd:process.cwd(),stdio:'ignore'});
 let browser;
 try{
  browser=await chromium.launch({executablePath:process.env.BROWSER_EXECUTABLE||undefined,headless:true,args:['--no-sandbox','--disable-dev-shm-usage']});
  const page=await browser.newPage({viewport:{width:390,height:844}}),errors=[];
  page.on('pageerror',e=>errors.push(e.message));page.on('dialog',d=>d.accept());
  await page.goto('http://127.0.0.1:4186');
  await page.waitForFunction(()=>profileLoaded&&logsLoaded&&foodLoaded);
  assert.equal(await page.locator('#onboardingDialog').evaluate(d=>d.open),false,'Optional setup must not block first use');
  assert.equal(await page.locator('.feature-tile').count(),12);
  assert.equal(await page.locator('#todayHabits button').count(),7);
  await page.evaluate(()=>{
   const day=localDay();localStorage.setItem('form-strength-preview',JSON.stringify({
    profile:{name:'QA local profile',completed:true,onboardingVersion:1,height:173,startingWeight:79,goalWeight:72,waterGoalMl:2500,proteinGoalG:100,age:24,sex:'male',equipment:'dumbbells',goalMode:'lose',goalMonths:6},
    logs:[{day,waterMl:750,weight:78,workout:true,session:'A',workoutActiveSeconds:90,workoutBreakSeconds:30,notes:'N'.repeat(1500)}],
    foodEntries:[{id:'qa-food',day,name:'QA rice',portion:'100 g',calories:130,proteinG:2.7,servings:1,meal:'Lunch'}],
    savedFoods:[{id:'qa-portion',name:'QA label shake',portion:'1 scoop',calories:120,proteinG:24,favorite:true,priceINR:40}],activityEntries:[]
   }));
  });await page.reload();await page.waitForFunction(()=>profileLoaded&&foodLoaded);
  const home=async()=>{await page.locator('.bottom-nav [data-tab="today"]').click();await page.waitForSelector('#today:not([hidden])');};
  for(const[id,target]of [['hubFood','#foodDiaryPanel'],['openTraining','#moveRoutines'],['hubActivity','#moveActivities'],['hubProgress','#userTrends'],['hubLibrary','#foodLibraryPanel'],['hubDevice','#moveDevice'],['hubReminders','#reminderSettings'],['hubYou','#userSettings']]){
   await home();await page.locator('#'+id).click();await page.waitForSelector(target,{state:'visible'});assert.equal(await page.locator('#today').isVisible(),false,id+' must navigate');
  }
  await home();await page.click('#hubCamera');await page.waitForSelector('#foodDialog[open]');assert.equal(await page.locator('#captureMeal').isVisible(),true);await page.locator('[data-close="foodDialog"]').click();
  await page.click('#quickCheckin');await page.waitForSelector('#checkinDialog[open]');assert.equal(await page.inputValue('#notes'),'N'.repeat(1500));await page.locator('[data-close="checkinDialog"]').click();
  await page.click('#hubWater');assert.equal(await page.evaluate(()=>document.activeElement.id),'homeWaterPanel');await page.click('#water500');await page.waitForFunction(()=>logs.find(l=>l.day===selectedDay).waterMl===1250);
  await page.click('#hubHabits');assert.equal(await page.evaluate(()=>document.activeElement.id),'homeHabitsPanel');
  for(const key of ['walk','protein','vegetables']){await page.locator('[data-toggle-habit="'+key+'"]').click();await page.waitForFunction(k=>logs.find(l=>l.day===selectedDay)[k]===true,key);}
  assert.match(await page.locator('#habitCount').innerText(),/4 \/ 7/);
  assert.equal(await page.evaluate(()=>logs.find(l=>l.day===selectedDay).notes),'N'.repeat(1500));
  // Every shortcut keeps the shared historical date; no accidental log under today.
  await page.click('#dayPrev');const historical=await page.inputValue('#viewDay');await page.click('#hubFood');await page.click('#addFood');await page.fill('#mealText','4 rotis');await page.click('#saveFood');await page.waitForSelector('#foodDialog:not([open])',{state:'attached'});
  assert.equal(await page.evaluate(()=>foodEntries.find(f=>f.name==='4 rotis').day),historical);
  await home();await page.click('#openProfile');await page.waitForSelector('#userOverview:not([hidden])');assert.equal(await page.title(),'You · Form & Strength');
  // Actual keyboard operation of the tablist, then native Back resolves the subview.
  await page.focus('#userOverviewTab');await page.keyboard.press('ArrowRight');assert.equal(await page.locator('#userTrends').isVisible(),true);await page.keyboard.press('ArrowRight');assert.equal(await page.locator('#userSettings').isVisible(),true);
  assert.equal(await page.locator('#userSettings a[href="https://logan9e7.github.io/Form-Strength/"]').count(),1);
  await page.evaluate(()=>handleNativeBack());assert.equal(await page.locator('#userOverview').isVisible(),true);
  await page.click('#youSettings');await page.waitForSelector('#profileDialog[open]');await page.fill('#profileName','QA edited profile');await page.click('#saveProfile');await page.waitForSelector('#profileDialog:not([open])',{state:'attached'});
  assert.deepEqual(await page.evaluate(()=>({name:profile.name,age:profile.age,equipment:profile.equipment,months:profile.goalMonths})),{name:'QA edited profile',age:24,equipment:'dumbbells',months:6});
  await page.reload();await page.waitForFunction(()=>profileLoaded&&foodLoaded);
  assert.equal(await page.evaluate(()=>profile.name),'QA edited profile');assert.equal(await page.evaluate(()=>foodEntries.length),2);
  await home();await page.evaluate(()=>{profile.name='A very long local profile name for QA';render();});
  for(const width of [320,390,430,768,1200]){
   await page.setViewportSize({width,height:844});await home();
   assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),true,'Home overflow at '+width);
   await page.click('#hubYou');await page.waitForSelector('#userSettings:not([hidden])');
   assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),true,'Settings overflow at '+width);
  }
  if(process.env.QA_OUTPUT_DIR){
   const fs=require('node:fs');fs.mkdirSync(process.env.QA_OUTPUT_DIR,{recursive:true});await page.setViewportSize({width:390,height:844});await home();await page.screenshot({path:process.env.QA_OUTPUT_DIR+'/home.png',fullPage:true});await page.click('#openProfile');await page.screenshot({path:process.env.QA_OUTPUT_DIR+'/you.png',fullPage:true});await home();
  }
  // Failed loads must remain visibly unavailable, not be presented as empty successful diaries.
  await home();await page.evaluate(async()=>{const saved=AndroidStore.request;AndroidStore.request=(p,m,b)=>p==='/api/foods'&&m==='GET'?JSON.stringify({error:'QA read failure'}):saved(p,m,b);foodLoaded=false;await loadAll();});
  assert.match(await page.locator('#hubFoodStatus').innerText(),/not loaded/);assert.match(await page.locator('#loadError').innerText(),/QA read failure/);
  assert.deepEqual(errors,[]);
  console.log('PASS: all 12 Home shortcuts, 7 habits, optional setup, historical meal date, profile preservation, user tabs/Back, reload, failed load and 320–1200px layouts. Browser bridge only.');
 }catch(e){console.error(e);process.exitCode=1;}finally{if(browser)await browser.close();server.kill();}
})();
