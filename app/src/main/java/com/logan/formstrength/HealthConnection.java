package com.logan.formstrength;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.health.connect.*;
import android.health.connect.datatypes.*;
import android.health.connect.datatypes.units.Energy;
import android.os.OutcomeReceiver;
import org.json.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.Executor;

/** Loaded only on Android 14+. Read-only; foreground sync; no network or persisted health cache. */
final class HealthConnection {
 static double toKilocalories(double calories){LocalStore.require(Double.isFinite(calories)&&calories>=0,"Invalid device energy.");return calories/1000.0;}
 static final String ACTIVE="android.permission.health.READ_ACTIVE_CALORIES_BURNED",TOTAL="android.permission.health.READ_TOTAL_CALORIES_BURNED",STEPS="android.permission.health.READ_STEPS";
 interface Result {void done(JSONObject value);}
 private final Activity activity;
 HealthConnection(Activity activity){this.activity=activity;}
 boolean allowed(String p){return activity.checkSelfPermission(p)==PackageManager.PERMISSION_GRANTED;}
 JSONObject status() throws JSONException {return new JSONObject().put("supported",manager()!=null).put("active",allowed(ACTIVE)).put("total",allowed(TOTAL)).put("steps",allowed(STEPS));}
 HealthConnectManager manager(){return activity.getSystemService(HealthConnectManager.class);}
 void connect(){activity.requestPermissions(new String[]{ACTIVE,TOTAL,STEPS},303);}
 void settings(){try{activity.startActivity(new Intent(HealthConnectManager.ACTION_MANAGE_HEALTH_PERMISSIONS).putExtra(Intent.EXTRA_PACKAGE_NAME,activity.getPackageName()));}catch(Exception e){activity.startActivity(new Intent("android.health.connect.action.HEALTH_HOME_SETTINGS"));}}
 void read(String day,Result callback){
  try{
   LocalDate date=LocalDate.parse(day);ZoneId zone=ZoneId.systemDefault();Instant start=date.atStartOfDay(zone).toInstant(),end=date.plusDays(1).atStartOfDay(zone).toInstant();if(end.isAfter(Instant.now()))end=Instant.now();
   if(!start.isBefore(end))throw new IllegalArgumentException("Choose today or an earlier date.");
   TimeInstantRangeFilter range=new TimeInstantRangeFilter.Builder().setStartTime(start).setEndTime(end).build();
   JSONObject out=new JSONObject().put("day",day).put("zone",zone.getId()).put("readAt",Instant.now().toString()).put("activeKcal",JSONObject.NULL).put("totalKcal",JSONObject.NULL).put("steps",JSONObject.NULL).put("sources",new JSONArray()).put("errors",new JSONArray());
   HealthConnectManager manager=manager();if(manager==null)throw new IllegalArgumentException("Health Connect is unavailable on this phone.");
   boolean active=allowed(ACTIVE),total=allowed(TOTAL);Executor main=activity.getMainExecutor();
   if(!active&&!total){readSteps(manager,range,out,callback);return;}
   AggregateRecordsRequest.Builder<Energy> builder=new AggregateRecordsRequest.Builder<>(range);
   if(active)builder.addAggregationType(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL);if(total)builder.addAggregationType(TotalCaloriesBurnedRecord.ENERGY_TOTAL);
   manager.aggregate(builder.build(),main,new OutcomeReceiver<AggregateRecordsResponse<Energy>,HealthConnectException>(){
    public void onResult(AggregateRecordsResponse<Energy> response){try{
      if(active){Energy v=response.get(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL);if(v!=null)out.put("activeKcal",toKilocalories(v.getInCalories()));origins(out,response.getDataOrigins(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL));}
      if(total){Energy v=response.get(TotalCaloriesBurnedRecord.ENERGY_TOTAL);if(v!=null)out.put("totalKcal",toKilocalories(v.getInCalories()));origins(out,response.getDataOrigins(TotalCaloriesBurnedRecord.ENERGY_TOTAL));}
     }catch(Exception e){error(out,"Could not read calorie values.");}readSteps(manager,range,out,callback);}
    public void onError(HealthConnectException e){error(out,"Calorie read failed. Check permissions, sync your band app and retry.");readSteps(manager,range,out,callback);}
   });
  }catch(Exception e){try{callback.done(new JSONObject().put("day",day).put("error",e instanceof IllegalArgumentException?e.getMessage():"Health Connect read failed. Check permissions and retry."));}catch(JSONException ignored){}}
 }
 private void readSteps(HealthConnectManager manager,TimeInstantRangeFilter range,JSONObject out,Result callback){
  if(!allowed(STEPS)){callback.done(out);return;}
  try{AggregateRecordsRequest<Long> request=new AggregateRecordsRequest.Builder<Long>(range).addAggregationType(StepsRecord.STEPS_COUNT_TOTAL).build();
   manager.aggregate(request,activity.getMainExecutor(),new OutcomeReceiver<AggregateRecordsResponse<Long>,HealthConnectException>(){
    public void onResult(AggregateRecordsResponse<Long> r){try{Long value=r.get(StepsRecord.STEPS_COUNT_TOTAL);if(value!=null)out.put("steps",value);origins(out,r.getDataOrigins(StepsRecord.STEPS_COUNT_TOTAL));}catch(Exception e){error(out,"Could not read steps.");}callback.done(out);}
    public void onError(HealthConnectException e){error(out,"Step read failed. Check permissions and retry.");callback.done(out);}
   });
  }catch(Exception e){error(out,"Step read failed.");callback.done(out);}
 }
 private void origins(JSONObject out,Set<DataOrigin> origins)throws JSONException{if(origins==null)return;JSONArray list=out.getJSONArray("sources");for(DataOrigin o:origins){String name;try{name=activity.getPackageManager().getApplicationLabel(activity.getPackageManager().getApplicationInfo(o.getPackageName(),0)).toString();}catch(Exception e){name="Connected fitness app";}boolean exists=false;for(int i=0;i<list.length();i++)if(name.equals(list.getString(i)))exists=true;if(!exists)list.put(name);}}
 private static void error(JSONObject out,String message){out.optJSONArray("errors").put(message);}
}
