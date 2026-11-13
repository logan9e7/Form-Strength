'use strict';
const $=id=>document.getElementById(id),tabs=['today','train','food','progress'];
const icons={arrow:'<path d="M5 19 19 5M5 5h14v14"/>',edit:'<path d="m15 4 5 5-11 11H4v-5zM13 6l5 5"/>',home:'<path d="m3 10 9-7 9 7v10a1 1 0 0 1-1 1h-5v-7H9v7H4a1 1 0 0 1-1-1z"/>',gym:'<path d="m5 8 11 11M8 5l11 11M3 10l7-7M14 21l7-7M4 15l-2-2M13 2l2 2M9 20l2 2M20 9l2 2"/>',food:'<path d="M5 3v7m3-7v7M2 3v5a3 3 0 0 0 6 0M5 11v10M16 3c-4 5-4 9 0 9h3M19 3v18"/>',chart:'<path d="M4 3v17h17M8 15l4-5 4 2 5-7"/>',check:'<path d="m5 12 4 4L19 6"/><rect x="2" y="2" width="20" height="20" rx="5"/>'};
const icon=name=>'<svg class="icon" viewBox="0 0 24 24" aria-hidden="true">'+icons[name]+'</svg>';
document.querySelectorAll('[data-icon]').forEach(el=>el.innerHTML=icon(el.dataset.icon));
const localDay=(d=new Date())=>d.getFullYear()+'-'+String(d.getMonth()+1).padStart(2,'0')+'-'+String(d.getDate()).padStart(2,'0');
const fmtDate=(day,options={month:'short',day:'numeric'})=>new Date(day+'T12:00:00').toLocaleDateString(undefined,options);
const validDay=d=>/^\d{4}-\d{2}-\d{2}$/.test(d)&&!Number.isNaN(Date.parse(d))&&new Date(d).toISOString().slice(0,10)===d;
const rounded=n=>Math.round(n).toLocaleString();
let logs=[],foodEntries=[],logsLoaded=false,foodLoaded=false,selectedSession='A',logDirty=false,foodDirty=false,logBusy=false,foodBusy=false,deleteBusy=false,foodDeleteBusy=false,toastTimer;
let activityEntries=[],activitiesLoaded=false;
let profile={},savedFoods=[],profileLoaded=false,savedFoodsLoaded=false,profileDirty=false,savedFoodDirty=false,profileBusy=false,savedFoodBusy=false,quickBusy=false;
const fields=['weight','waist','steps','sleep','notes','session','waterMl'],checks=['workout','posture','walk','protein','vegetables','sugarFree','proteinShake'];
function notify(text){$('toast').textContent=text;$('toast').hidden=false;clearTimeout(toastTimer);toastTimer=setTimeout(()=>$('toast').hidden=true,3500)}
function showMessage(id,text,error=false){$(id).textContent=text;$(id).style.color=error?'#9b4032':''}
function showScreen(name){name=tabs.includes(name)?name:'today';tabs.forEach(t=>$(t).hidden=t!==name);document.querySelectorAll('[data-tab]').forEach(b=>{b.classList.toggle('active',b.dataset.tab===name);b.dataset.tab===name?b.setAttribute('aria-current','page'):b.removeAttribute('aria-current')});document.title=(name==='food'?'Food':name==='train'?'Move':name[0].toUpperCase()+name.slice(1))+' · Form & Strength';window.scrollTo({top:0,behavior:'instant'})}
function navigate(name){if(location.hash!=='#'+name)location.hash=name;else showScreen(name)}
window.addEventListener('hashchange',()=>showScreen(location.hash.slice(1)));document.querySelectorAll('[data-tab]').forEach(b=>b.onclick=()=>navigate(b.dataset.tab));showScreen(location.hash.slice(1));
function openDialog(id){if(!$(id).open){$(id).showModal();document.body.style.overflow='hidden'}}
function isDirty(id){return id==='checkinDialog'?logDirty:id==='foodDialog'?foodDirty:id==='profileDialog'?profileDirty:id==='savedFoodDialog'?savedFoodDirty:id==='activityDialog'?activityDirty:false}
function isBusy(id){return id==='checkinDialog'?logBusy||deleteBusy:id==='foodDialog'?foodBusy:id==='profileDialog'?profileBusy:id==='savedFoodDialog'?savedFoodBusy:id==='activityDialog'?activityBusy:false}
function closeDialog(id,force=false){if(isBusy(id)&&!force)return;if(!force&&isDirty(id)&&!confirm('Discard the changes in this form?'))return;$(id).close();if(id==='checkinDialog')logDirty=false;if(id==='foodDialog')foodDirty=false;if(id==='profileDialog')profileDirty=false;if(id==='savedFoodDialog')savedFoodDirty=false;if(id==='activityDialog')activityDirty=false;document.body.style.overflow=document.querySelector('dialog[open]')?'hidden':''}
document.querySelectorAll('[data-close]').forEach(b=>b.onclick=()=>closeDialog(b.dataset.close));document.querySelectorAll('dialog').forEach(d=>{d.addEventListener('cancel',e=>{e.preventDefault();closeDialog(d.id)});d.addEventListener('click',e=>{if(e.target===d){const r=d.getBoundingClientRect();if(e.clientX<r.left||e.clientX>r.right||e.clientY<r.top||e.clientY>r.bottom)closeDialog(d.id)}})});
window.addEventListener('beforeunload',e=>{if(logDirty||foodDirty||profileDirty||savedFoodDirty||activityDirty){e.preventDefault();e.returnValue=''}});
async function request(path,method='GET',data){
 if(!window.AndroidStore)throw Error('Open this tracker using the Android app.');
 const response=JSON.parse(AndroidStore.request(path,method,data?JSON.stringify(data):''));
 if(response.error)throw Error(response.error);return response;
}
function syncButtons(){$('save').disabled=!logsLoaded||logBusy||deleteBusy;$('delete').disabled=!logsLoaded||logBusy||deleteBusy;$('saveFood').disabled=!foodLoaded||foodBusy||(foodMode==='quick'&&!quickEstimate.ready);$('save').textContent=logBusy?'Saving…':'Save check-in';$('saveFood').textContent=foodBusy?'Saving…':$('foodId').value?'Update food':foodMode==='quick'?'Save meal':'Save food';syncQuickButtons()}

async function loadAll(){
 const results=await Promise.allSettled([request('/api/logs'),request('/api/foods'),request('/api/profile'),request('/api/saved-foods'),request('/api/activities')]);const errors=[];
 const labels=['Check-ins','Food diary','Profile','Saved foods','Activities'];results.forEach((r,i)=>{if(r.status==='rejected'){errors.push(labels[i]+': '+r.reason.message);return}if(i===0){logs=r.value.logs;logsLoaded=true}if(i===1){foodEntries=r.value.entries;foodLoaded=true}if(i===2){profile=r.value.profile;profileLoaded=true}if(i===3){savedFoods=r.value.foods;savedFoodsLoaded=true}if(i===4){activityEntries=r.value.entries;activitiesLoaded=true}});
 refreshFoodIndex();$('loadError').hidden=!errors.length;$('loadErrorText').textContent=errors.join(' ');render();syncButtons();syncQuickButtons();
}
$('retry').onclick=async()=>{$('retry').disabled=true;$('retry').textContent='Loading…';await loadAll();$('retry').disabled=false;$('retry').textContent='Retry loading'};
function populateLog(day){$('day').value=day;const l=logs.find(l=>l.day===day)||{};fields.forEach(k=>$(k).value=l[k]??(k==='session'?'A':''));checks.forEach(k=>$(k).checked=!!l[k]);$('delete').hidden=!l.day;logDirty=false;showMessage('message',l.day?'Saved check-in loaded.':'All measurements are optional.');$('session').disabled=!$('workout').checked;syncButtons()}
function openLog(day=localDay(),extra={}){if(!logsLoaded){notify('Check-ins are still loading. Use Retry if loading failed.');return}populateLog(day);for(const[k,v]of Object.entries(extra)){if(checks.includes(k))$(k).checked=v;else $(k).value=v;logDirty=true}$('session').disabled=!$('workout').checked;openDialog('checkinDialog')}
document.querySelectorAll('[data-water]').forEach(b=>b.onclick=()=>{const value=Number($('waterMl').value||0)+Number(b.dataset.water);if(value>20000){notify('Check your daily water total.');return}$('waterMl').value=value;logDirty=true});
$('logForm').addEventListener('input',event=>{if(event.target.id!=='day')logDirty=true;$('session').disabled=!$('workout').checked});let logSelectedDay=localDay();$('day').value=logSelectedDay;$('day').max=localDay();$('day').onfocus=()=>logSelectedDay=$('day').value;$('day').onchange=()=>{const value=$('day').value;if(!validDay(value)||value>localDay()){$('day').value=logSelectedDay;return}if(logDirty&&!confirm('Discard changes and open another date?')){$('day').value=logSelectedDay;return}populateLog(value);logSelectedDay=value};
$('logForm').onsubmit=async e=>{e.preventDefault();if(logBusy||!logsLoaded)return;const day=$('day').value;if(!validDay(day)||day>localDay()){showMessage('message','Choose today or an earlier date.',true);return}const d={day};fields.forEach(k=>d[k]=$(k).value);checks.forEach(k=>d[k]=$(k).checked);logBusy=true;syncButtons();showMessage('message','Saving on this phone…');try{const result=await request('/api/logs','PUT',d);logs=logs.filter(l=>l.day!==day);logs.push(result.entry);logs.sort((a,b)=>a.day.localeCompare(b.day));logDirty=false;render();closeDialog('checkinDialog',true);notify('Check-in saved. One step forward.')}catch(e){showMessage('message',e.message,true)}finally{logBusy=false;syncButtons()}};
$('delete').onclick=async()=>{if(deleteBusy||!confirm('Delete the check-in for '+fmtDate($('day').value)+'? This cannot be undone.'))return;deleteBusy=true;syncButtons();try{const day=$('day').value;await request('/api/logs','DELETE',{day});logs=logs.filter(l=>l.day!==day);logDirty=false;render();closeDialog('checkinDialog',true);notify('Check-in deleted.')}catch(e){showMessage('message',e.message,true)}finally{deleteBusy=false;syncButtons()}};
$('quickCheckin').onclick=$('editHabits').onclick=$('logMeasurement').onclick=()=>openLog();$('openCalories').onclick=()=>{setFoodDate(localDay());setFoodView('diary');navigate('food')};$('openTraining').onclick=()=>navigate('train');
const habitLabels={workout:'Gym session',posture:'Posture & mobility',walk:'Intentional movement',protein:'Protein with meals',vegetables:'Vegetables & fruit',sugarFree:'No added sugar',proteinShake:'Protein shake'};
function latest(list,key){return [...list].filter(l=>typeof l[key]==='number').sort((a,b)=>a.day.localeCompare(b.day)).at(-1)}
function renderHome(){
 const last=latest(logs,'weight'),w=last?.weight??profile.startingWeight;
 $('currentWeight').textContent=w??'—';const target=profile.goalWeight;
 $('goalLabel').textContent=target!=null?(profile.startingWeight!=null?profile.startingWeight+' → ':'Goal · ')+target+' kg':'No weight goal set';
 $('remaining').textContent=w!=null&&target!=null?(Math.abs(w-target)<.05?'At your goal':Math.abs(w-target).toFixed(1)+' kg from goal'):'Your own pace';
 const percent=TrackerCore.goal(profile.startingWeight,w,target);$('goalTrack').hidden=percent===null;$('goalFill').style.width=(percent||0)+'%';$('goalTrack').setAttribute('aria-valuenow',Math.round(percent||0));
 $('lastWeight').textContent=last?'Last weigh-in · '+fmtDate(last.day):w!=null?'Your starting measurement':'Log a measurement when you’re ready.';
 const start=new Date(localDay()+'T12:00:00');start.setDate(start.getDate()-((start.getDay()+6)%7));const count=logs.filter(l=>l.workout&&l.day>=localDay(start)&&l.day<=localDay()).length;
 $('weekWorkouts').textContent='Gym habit · '+count+' '+(count===1?'day':'days')+' this week';
 const totals=TrackerCore.nutrients(foodEntries.filter(e=>e.day===localDay()));$('todayCalories').innerHTML=(foodLoaded?(totals.count?rounded(totals.calories):'—'):'…')+' <span>kcal</span>';
 $('todayProtein').textContent=totals.known?totals.protein+' g protein'+(totals.unknown?' · partial':''):'Protein not logged';
 $('todayFoodCount').textContent=foodLoaded?(totals.count?totals.count+' '+(totals.count===1?'meal entry':'meal entries'):'Your first meal starts here'):'Loading diary…';
 renderProfileSummary();renderDailyActions();renderBurnSummary();
}
function render(){renderHome();renderFood();renderProgress();renderCalendar();renderSavedFoods();renderActivities()}
function exportRecords(){
 if(!logsLoaded||!foodLoaded||!profileLoaded||!savedFoodsLoaded||!activitiesLoaded){notify('Load all records before exporting. Use Retry if needed.');return}
 const backup={formatVersion:3,exportedAt:new Date().toISOString(),profile,savedFoods,logs,foodEntries,activityEntries};
 AndroidStore.exportBackup(JSON.stringify(backup,null,2));
}
$('export').onclick=$('exportProfile').onclick=exportRecords;
$('importBackup').onclick=()=>{if(logDirty||foodDirty||profileDirty||savedFoodDirty||activityDirty){notify('Save or discard your open form before importing.');return}AndroidStore.importBackup()};
window.onNativeStatus=async event=>{showMessage('exportStatus',event.message);notify(event.message);if(event.reload)await loadAll()};
window.handleNativeBack=()=>{const dialog=document.querySelector('dialog[open]');if(dialog){closeDialog(dialog.id);return}if(location.hash&&location.hash!=='#today'){navigate('today');return}AndroidStore.exit()};
$('openProfile').onclick=$('openAbout').onclick=()=>openProfile();
