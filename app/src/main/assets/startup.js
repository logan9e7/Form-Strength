'use strict';
let renderedDay=localDay();
function refreshDay(){const now=localDay();if(now!==renderedDay){const wasToday=selectedDay===renderedDay;renderedDay=now;if(wasToday)changeDay(now);else renderDateContext()}$('day').max=now;renderDateContext();}
window.onNativeResume=async()=>{refreshConnections();refreshDay();try{healthEntries=(await request('/api/health-snapshots')).entries;}catch{}render();maybeSyncHealth();};
document.addEventListener('visibilitychange',()=>{if(!document.hidden){refreshConnections();refreshDay();maybeSyncHealth()}});setInterval(refreshDay,60000);
refreshConnections();refreshDay();render();syncButtons();loadAll();
function resizeSheets(){const height=window.visualViewport?.height||window.innerHeight;document.documentElement.style.setProperty('--visible-height',Math.floor(height)+'px');const el=document.activeElement;if(el?.closest?.('dialog[open]')&&/INPUT|TEXTAREA|SELECT/.test(el.tagName))requestAnimationFrame(()=>el.scrollIntoView({block:'nearest',behavior:'instant'}));}
window.visualViewport?.addEventListener('resize',resizeSheets);window.addEventListener('resize',resizeSheets);resizeSheets();
