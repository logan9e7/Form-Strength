'use strict';
/* Home is the launcher; every shortcut reuses the existing feature controller. */
(() => {
 Object.assign(icons, {
  water:'<path d="M12 3S5 11 5 15a7 7 0 0 0 14 0c0-4-7-12-7-12Z"/>',
  book:'<path d="M4 4h7a3 3 0 0 1 3 3v14a4 4 0 0 0-4-3H4ZM20 4h-6v17a4 4 0 0 1 4-3h2Z"/>',
  camera:'<path d="M4 7h4l2-3h4l2 3h4v13H4Z"/><circle cx="12" cy="13" r="4"/>',
  bell:'<path d="M5 17h14l-2-3V9a5 5 0 0 0-10 0v5ZM10 21h4"/>',
  user:'<circle cx="12" cy="7" r="4"/><path d="M4 21v-2a8 8 0 0 1 16 0v2"/>'
 });
 document.querySelectorAll('.feature-icon[data-icon]').forEach(el => el.innerHTML=icon(el.dataset.icon));
 let userView='overview';
 const userViews=['overview','trends','settings'];
 function setUserView(view, focus=false) {
  userView=userViews.includes(view)?view:'overview';
  for(const name of userViews) {
   const active=name===userView, tab=$('user'+name[0].toUpperCase()+name.slice(1)+'Tab');
   $('user'+name[0].toUpperCase()+name.slice(1)).hidden=!active;
   tab.classList.toggle('active',active);tab.setAttribute('aria-selected',String(active));tab.tabIndex=active?0:-1;
   if(active&&focus)tab.focus();
  }
  if(userView==='settings')refreshConnections();
 }
 function openUser(view='overview', section) {
  setUserView(view);navigate('progress');
  if(section){const detail=$(section);detail.open=true;requestAnimationFrame(()=>detail.scrollIntoView({block:'start'}));}
 }
 window.openUserSection=openUser;
 document.querySelectorAll('[data-user-view]').forEach(tab=>{
  tab.onclick=()=>setUserView(tab.dataset.userView);
  tab.onkeydown=e=>{
   const index=userViews.indexOf(tab.dataset.userView);
   const next=e.key==='ArrowRight'?(index+1)%3:e.key==='ArrowLeft'?(index+2)%3:e.key==='Home'?0:e.key==='End'?2:null;
   if(next!==null){e.preventDefault();setUserView(userViews[next],true);}
  };
 });
 function jump(id){const target=$(id);target.focus({preventScroll:true});target.scrollIntoView({block:'start',behavior:matchMedia('(prefers-reduced-motion: reduce)').matches?'instant':'smooth'});}
 $('hubFood').onclick=()=>{setFoodDate(selectedDay);setFoodView('diary');navigate('food');};
 $('openTraining').onclick=()=>{setMoveView('routines');navigate('train');};
 $('hubActivity').onclick=()=>{setMoveView('activity');navigate('train');};
 $('hubWater').onclick=()=>jump('homeWaterPanel');
 $('hubHabits').onclick=()=>jump('homeHabitsPanel');
 $('hubProgress').onclick=()=>openUser('trends');
 $('hubLibrary').onclick=()=>{setFoodView('library');navigate('food');};
 $('hubCamera').onclick=()=>{setFoodDate(selectedDay);openFood();if($('foodDialog').open)$('captureMeal').focus();};
 $('hubDevice').onclick=()=>{setMoveView('device');navigate('train');};
 $('hubReminders').onclick=()=>openUser('settings','reminderSettings');
 $('hubYou').onclick=()=>openUser('settings');
 $('openProfile').onclick=$('openAbout').onclick=()=>openUser();
 $('youSettings').onclick=openProfile;
 $('profileHealth').onclick=()=>{if($('profileDialog').open){closeDialog('profileDialog');if($('profileDialog').open)return;}setMoveView('device');navigate('train');};
 const nativeBack=window.handleNativeBack;
 window.handleNativeBack=()=>{
  if(!document.querySelector('dialog[open]')&&location.hash==='#progress'&&userView!=='overview'){setUserView('overview');return;}
  return nativeBack();
 };
 function renderHub(){
  const daily=logs.find(l=>l.day===selectedDay),foods=foodEntries.filter(f=>f.day===selectedDay),moves=activityEntries.filter(e=>e.day===selectedDay);
  const status=(id,value)=>$(id).textContent=value;
  status('hubFoodStatus',!foodLoaded?'Diary not loaded':foods.length?foods.length+' meal entries':'No meals logged');
  status('hubActivityStatus',!activitiesLoaded?'Activities not loaded':moves.length?moves.length+' recorded activities':'No activities logged');
  status('hubWaterStatus',!logsLoaded?'Check-ins not loaded':daily?.waterMl!=null?rounded(daily.waterMl)+' ml recorded':'Water not logged');
  status('hubHabitStatus',!logsLoaded?'Check-ins not loaded':checks.filter(k=>daily?.[k]).length+' of 7 recorded');
  status('hubCheckinStatus',!logsLoaded?'Check-ins not loaded':daily?'Edit selected date':'Add for selected date');
  status('hubProgressStatus',!logsLoaded?'History not loaded':logs.length?logs.length+' saved check-ins':'Start your history');
  status('hubLibraryStatus',!savedFoodsLoaded?'Library not loaded':savedFoods.length?savedFoods.length+' saved portions':'Add your own portions');
  status('hubCameraStatus',connectionState.aiConfigured?'Gemini key configured':'Your Gemini key required');
  const h=healthPermissions();status('hubDeviceStatus',!h.supported?'Android 14+ required':HealthCore.keys.some(k=>h[k])?'Review readings & sync':'Read access needed');
  status('hubReminderStatus','Optional notifications');
  status('hubUserStatus',!profileLoaded?'Profile not loaded':profile.completed?'Your preferences & data':'Optional setup');
  const facts=$('userFacts');facts.replaceChildren();
  const values=[['Name',profile.name||'Not set'],['Goal',({lose:'Gradual weight reduction',gain:'Gradual weight gain',maintain:'Maintain a routine'}[profile.goalMode])||'Not set'],['Height',profile.height!=null?profile.height+' cm':'Not set'],['Equipment',({bodyweight:'Bodyweight',dumbbells:'Dumbbells',gym:'Gym'}[profile.equipment])||'Not set'],['Water goal',profile.waterGoalMl>0?rounded(profile.waterGoalMl)+' ml':'Not set'],['Protein goal',profile.proteinGoalG>0?profile.proteinGoalG+' g':'Not set']];
  if(!profileLoaded){facts.textContent='Profile has not loaded. Use Retry above.';return;}
  for(const[label,value]of values){const item=document.createElement('div'),dt=document.createElement('dt'),dd=document.createElement('dd');dt.textContent=label;dd.textContent=value;item.append(dt,dd);facts.append(item);}
 }
 const original=render;render=function(){original();renderHub();};
 const connectionChange=window.onConnectionChanged;
 window.onConnectionChanged=async()=>{await connectionChange();renderHub();};
 setUserView('overview');renderHub();
})();
