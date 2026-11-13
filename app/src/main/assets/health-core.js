/* Pure reconciliation rules: absence is not zero; device baseline and approved extras are distinct. */
(function(root){
 const keys=['active','total','steps','distance','sleep'];
 function snapshot(entries,day,zone){return entries.find(e=>e.day===day&&e.zone===zone)||null}
 function metric(entry,key,permissions){if(permissions?.supported===false)return{state:'unavailable',value:null,sources:[]};if(!permissions?.[key])return{state:'permission_denied',value:null,sources:[]};const m=entry?.metrics?.[key];return m?{...m,value:typeof m.value==='number'&&Number.isFinite(m.value)&&m.value>=0?m.value:null}:{state:'not_synced',value:null,sources:[]};}
 function burn(entry,activities,permissions){const total=metric(entry,'total',permissions),active=metric(entry,'active',permissions),base=total.value!=null?total:active,kind=total.value!=null?'total':'active',extras=activities.filter(a=>a.additional===true).reduce((s,a)=>s+a.activeCalories,0);return{value:base.value==null?null:base.value+extras,baseline:base.value,extras,kind,metric:base};}
 function needsSync(entry,now=Date.now()){return !entry||!entry.readAt||!Number.isFinite(Date.parse(entry.readAt))||Date.parse(entry.readAt)>now+60000||now-Date.parse(entry.readAt)>300000||keys.some(k=>entry.metrics?.[k]?.state==='error');}
 function stateText(m){if(m.state==='permission_denied')return'Permission off';if(m.value!=null)return m.cached?'Cached · last attempt failed':'Synced';return({no_data:'No data shared',error:'Read failed',not_synced:'Not synced'})[m.state]||'Unavailable';}
 const api={keys,snapshot,metric,burn,needsSync,stateText};if(typeof module==='object'&&module.exports)module.exports=api;else root.HealthCore=api;
})(typeof globalThis!=='undefined'?globalThis:this);
