package com.logan.formstrength;

import android.app.Activity;
import android.os.Build;
import org.json.*;
import java.time.*;

/** API-independent entry point; Android 14 framework symbols live only in the guarded helper. */
final class HealthConnection {
 static final String ACTIVE="android.permission.health.READ_ACTIVE_CALORIES_BURNED",TOTAL="android.permission.health.READ_TOTAL_CALORIES_BURNED",STEPS="android.permission.health.READ_STEPS",DISTANCE="android.permission.health.READ_DISTANCE",SLEEP="android.permission.health.READ_SLEEP";
 static final String[] KEYS={"active","total","steps","distance","sleep"},PERMISSIONS={ACTIVE,TOTAL,STEPS,DISTANCE,SLEEP};
 interface Result {void done(JSONObject value);}
 private final Activity activity;
 HealthConnection(Activity activity){this.activity=activity;}
 static double toKilocalories(double calories){LocalStore.require(Double.isFinite(calories)&&calories>=0,"Invalid device energy.");return calories/1000.0;}
 static boolean platformSupported(int sdk){return sdk>=34;}
 static JSONObject unsupportedStatus()throws JSONException{
  JSONObject out=new JSONObject().put("supported",false).put("zone",ZoneId.systemDefault().getId()).put("today",LocalDate.now().toString());
  for(String key:KEYS)out.put(key,false);
  return out;
 }
 JSONObject status()throws JSONException{
  if(!platformSupported(Build.VERSION.SDK_INT))return unsupportedStatus();
  return new HealthConnectionApi34(activity).status();
 }
 void connect(){
  LocalStore.require(platformSupported(Build.VERSION.SDK_INT),"Device sync requires Android 14 or newer.");
  new HealthConnectionApi34(activity).connect();
 }
 void settings(){
  LocalStore.require(platformSupported(Build.VERSION.SDK_INT),"Device sync requires Android 14 or newer.");
  new HealthConnectionApi34(activity).settings();
 }
 void read(String day,Result callback){
  if(!platformSupported(Build.VERSION.SDK_INT)){
   try{callback.done(new JSONObject().put("supported",false).put("day",day).put("error","Device sync requires Android 14 or newer."));}catch(JSONException ignored){}
   return;
  }
  new HealthConnectionApi34(activity).read(day,callback);
 }
 static JSONObject windows(String day,ZoneId zone,Instant now)throws JSONException{
  LocalDate date=LocalDate.parse(day);LocalStore.require(!date.isAfter(now.atZone(zone).toLocalDate()),"Choose today or an earlier date.");Instant start=date.atStartOfDay(zone).toInstant(),end=date.plusDays(1).atStartOfDay(zone).toInstant();if(end.isAfter(now))end=now;
  Instant sleepStart=date.minusDays(1).atTime(12,0).atZone(zone).toInstant(),sleepEnd=date.atTime(12,0).atZone(zone).toInstant();if(sleepEnd.isAfter(now))sleepEnd=now;
  return new JSONObject().put("day",day).put("zone",zone.getId()).put("readAt",now.toString()).put("periodStart",start.toString()).put("periodEnd",end.toString()).put("sleepStart",sleepStart.toString()).put("sleepEnd",sleepEnd.toString()).put("metrics",new JSONObject());
 }
}
