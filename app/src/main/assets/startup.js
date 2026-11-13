'use strict';
let renderedDay=localDay();
function refreshDay(){const day=localDay();$('todayDate').textContent=new Date().toLocaleDateString(undefined,{weekday:'long',month:'short',day:'numeric'});$('day').max=$('foodDay').max=$('activityDay').max=$('deviceDay').max=day;if(day!==renderedDay){healthReading=null;healthRequestId=null;clearTimeout(healthTimer);renderHealthReading();if($('activityDay').value===renderedDay)$('activityDay').value=day;if($('deviceDay').value===renderedDay)$('deviceDay').value=day;if($('foodDay').value===renderedDay)$('foodDay').value=day;renderedDay=day;render();}}
window.onNativeResume=()=>{refreshDay();refreshConnections();};document.addEventListener('visibilitychange',()=>{if(!document.hidden)refreshDay()});setInterval(refreshDay,60000);
refreshDay();render();syncButtons();refreshConnections();loadAll();
