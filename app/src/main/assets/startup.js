'use strict';
let renderedDay=localDay();
function refreshDay(){const day=localDay();$('todayDate').textContent=new Date().toLocaleDateString(undefined,{weekday:'long',month:'short',day:'numeric'});$('day').max=$('foodDay').max=day;if(day!==renderedDay){if($('foodDay').value===renderedDay)$('foodDay').value=day;renderedDay=day;render();}}
window.onNativeResume=()=>{refreshDay();};document.addEventListener('visibilitychange',()=>{if(!document.hidden)refreshDay()});setInterval(refreshDay,60000);
refreshDay();render();syncButtons();loadAll();
