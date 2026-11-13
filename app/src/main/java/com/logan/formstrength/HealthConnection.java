package com.logan.formstrength;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.health.connect.*;
import android.health.connect.datatypes.*;
import android.os.OutcomeReceiver;
import org.json.*;
import java.time.*;
import java.util.*;

/** Android 14+ foreground aggregates; per-metric permission and missing-data states. */
final class HealthConnection {
 static final String ACTIVE="android.permission.health.READ_ACTIVE_CALORIES_BURNED",TOTAL="android.permission.health.READ_TOTAL_CALORIES_BURNED",STEPS="android.permission.health.READ_STEPS",DISTANCE="android.permission.health.READ_DISTANCE",SLEEP="android.permission.health.READ_SLEEP";
 static final String[] KEYS={"active","total","steps","distance","sleep"},PERMISSIONS={ACTIVE,TOTAL,STEPS,DISTANCE,SLEEP};
 interface Result {void done(JSONObject value);}
 interface Value<T>{double convert(T value);}
 private final Activity activity;
 HealthConnection(Activity activity){this.activity=activity;}
 static double toKilocalories(double calories){LocalStore.require(Double.isFinite(calories)&&calories>=0,"Invalid device energy.");return calories/1000.0;}
 boolean allowed(String p){return activity.checkSelfPermission(p)==PackageManager.PERMISSION_GRANTED;}
 JSONObject status()throws JSONException{JSONObject p=new JSONObject().put("supported",manager()!=null).put("zone",ZoneId.systemDefault().getId()).put("today",LocalDate.now().toString());for(int i=0;i<KEYS.length;i++)p.put(KEYS[i],allowed(PERMISSIONS[i]));return p;}
 HealthConnectManager manager(){return activity.getSystemService(HealthConnectManager.class);}
 void connect(){activity.requestPermissions(PERMISSIONS,303);}
 void settings(){try{activity.startActivity(new Intent(HealthConnectManager.ACTION_MANAGE_HEALTH_PERMISSIONS).putExtra(Intent.EXTRA_PACKAGE_NAME,activity.getPackageName()));}catch(Exception e){activity.startActivity(new Intent("android.health.connect.action.HEALTH_HOME_SETTINGS"));}}
 void read(String day,Result callback){try{
  JSONObject out=windows(day,ZoneId.systemDefault(),Instant.now());Instant start=Instant.parse(out.getString("periodStart")),end=Instant.parse(out.getString("periodEnd")),sleepStart=Instant.parse(out.getString("sleepStart")),sleepEnd=Instant.parse(out.getString("sleepEnd"));
  HealthConnectManager m=manager();if(m==null)throw new IllegalArgumentException("Health Connect is unavailable on this phone.");
  TimeInstantRangeFilter daily=new TimeInstantRangeFilter.Builder().setStartTime(start).setEndTime(end).build(),night=new TimeInstantRangeFilter.Builder().setStartTime(sleepStart).setEndTime(sleepEnd).build();
  readNext(m,daily,night,out,0,callback);
 }catch(Exception e){try{callback.done(new JSONObject().put("day",day).put("error",e instanceof IllegalArgumentException?e.getMessage():"Health Connect could not read this date. Check access and retry."));}catch(Exception ignored){}}}
 static JSONObject windows(String day,ZoneId zone,Instant now)throws JSONException{
  LocalDate date=LocalDate.parse(day);LocalStore.require(!date.isAfter(now.atZone(zone).toLocalDate()),"Choose today or an earlier date.");Instant start=date.atStartOfDay(zone).toInstant(),end=date.plusDays(1).atStartOfDay(zone).toInstant();if(end.isAfter(now))end=now;
  Instant sleepStart=date.minusDays(1).atTime(12,0).atZone(zone).toInstant(),sleepEnd=date.atTime(12,0).atZone(zone).toInstant();if(sleepEnd.isAfter(now))sleepEnd=now;
  return new JSONObject().put("day",day).put("zone",zone.getId()).put("readAt",now.toString()).put("periodStart",start.toString()).put("periodEnd",end.toString()).put("sleepStart",sleepStart.toString()).put("sleepEnd",sleepEnd.toString()).put("metrics",new JSONObject());
 }
 private void readNext(HealthConnectManager m,TimeInstantRangeFilter daily,TimeInstantRangeFilter night,JSONObject out,int i,Result done){
  if(i==KEYS.length){done.done(out);return;}Runnable next=()->readNext(m,daily,night,out,i+1,done);
  if(!allowed(PERMISSIONS[i])){put(out,KEYS[i],"permission_denied",null,null,"Read permission is off.");next.run();return;}
  switch(i){
   case 0:aggregate(m,daily,ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL,v->toKilocalories(v.getInCalories()),out,i,next);break;
   case 1:aggregate(m,daily,TotalCaloriesBurnedRecord.ENERGY_TOTAL,v->toKilocalories(v.getInCalories()),out,i,next);break;
   case 2:aggregate(m,daily,StepsRecord.STEPS_COUNT_TOTAL,v->v.doubleValue(),out,i,next);break;
   case 3:aggregate(m,daily,DistanceRecord.DISTANCE_TOTAL,v->v.getInMeters(),out,i,next);break;
   case 4:aggregate(m,night,SleepSessionRecord.SLEEP_DURATION_TOTAL,v->v.doubleValue()/60000.0,out,i,next);break;
  }
 }
 private <T> void aggregate(HealthConnectManager m,TimeInstantRangeFilter range,AggregationType<T> type,Value<T> conversion,JSONObject out,int i,Runnable next){try{
  m.aggregate(new AggregateRecordsRequest.Builder<T>(range).addAggregationType(type).build(),activity.getMainExecutor(),new OutcomeReceiver<AggregateRecordsResponse<T>,HealthConnectException>(){
   public void onResult(AggregateRecordsResponse<T> response){try{
    if(!allowed(PERMISSIONS[i]))put(out,KEYS[i],"permission_denied",null,null,"Read permission was removed.");
    else {T v=response.get(type);Double value=v==null?null:conversion.convert(v);LocalStore.require(value==null||Double.isFinite(value)&&value>=0,"Invalid device value.");put(out,KEYS[i],value==null?"no_data":"available",value,origins(response.getDataOrigins(type)),value==null?"The source has not shared a reading in this window.":"");}
   }catch(Exception e){put(out,KEYS[i],"error",null,null,"Could not validate this reading.");}next.run();}
   public void onError(HealthConnectException e){put(out,KEYS[i],allowed(PERMISSIONS[i])?"error":"permission_denied",null,null,"Read failed. Check source sharing, permissions or the available history window, then retry.");next.run();}
  });
 }catch(Exception e){put(out,KEYS[i],allowed(PERMISSIONS[i])?"error":"permission_denied",null,null,"Health Connect could not read this metric.");next.run();}}
 private void put(JSONObject out,String key,String state,Double value,JSONArray sources,String message){try{out.getJSONObject("metrics").put(key,new JSONObject().put("state",state).put("value",value==null?JSONObject.NULL:value).put("sources",sources==null?new JSONArray():sources).put("observedAt",out.getString("readAt")).put("message",message));}catch(Exception ignored){}}
 private JSONArray origins(Set<DataOrigin> origins){JSONArray list=new JSONArray();if(origins!=null)for(DataOrigin o:origins){String name;try{name=activity.getPackageManager().getApplicationLabel(activity.getPackageManager().getApplicationInfo(o.getPackageName(),0)).toString();}catch(Exception e){name="Connected fitness app";}boolean found=false;for(int i=0;i<list.length();i++)if(name.equals(list.optString(i)))found=true;if(!found)list.put(name);}return list;}
}
