package com.logan.formstrength;
import org.json.JSONObject;
public class ValidationTest {
 static void check(boolean value,String msg){if(!value)throw new AssertionError(msg);}
 interface Action {void run() throws Exception;}
 static void rejects(Action fn,String label)throws Exception{try{fn.run();throw new AssertionError(label);}catch(IllegalArgumentException expected){}}
 public static void main(String[] args)throws Exception{
  JSONObject log=LocalStore.log(new JSONObject("{\"day\":\"2026-11-10\",\"weight\":\"78.5\",\"waist\":80,\"workout\":true,\"session\":\"B\"}"));
  check(log.getDouble("weight")==78.5,"weight canonicalized");check(log.getBoolean("workout"),"workout");check(!log.getBoolean("walk"),"unchecked defaults");
  rejects(()->LocalStore.day(new JSONObject().put("day","2026-02-30")),"impossible date");
  rejects(()->LocalStore.number(new JSONObject().put("weight",-3),"weight",30,250),"negative weight");
  rejects(()->LocalStore.number(new JSONObject().put("servings",0),"servings",0.01,100),"zero servings");
  log.put("waterMl",1750).put("sugarFree",true).put("proteinShake",true);log=LocalStore.log(log);
  check(log.getDouble("waterMl")==1750 && log.getBoolean("sugarFree") && log.getBoolean("proteinShake"),"daily trackers retained");
  check(!LocalStore.log(new JSONObject().put("day","2026-11-10")).has("waterMl"),"old backups leave water unlogged");
  rejects(()->LocalStore.log(new JSONObject().put("day","2026-11-10").put("waterMl",-1)),"negative water");
  rejects(()->LocalStore.log(new JSONObject().put("day","2026-11-10").put("waterMl",20001)),"excess water");
  JSONObject food=LocalStore.food(new JSONObject("{\"id\":\"stable-1\",\"day\":\"2026-11-10\",\"name\":\"Example\",\"meal\":\"Lunch\",\"portion\":\"100 g\",\"servings\":1.5,\"calories\":120}"));
  check(food.getDouble("servings")*food.getDouble("calories")==180,"calorie total");
  food.put("estimated",true).put("estimateBasis","Reference total adjusted").put("components",new org.json.JSONArray().put(new JSONObject().put("label","1 cup dal").put("calories",200).put("source","Reference")));
  food=LocalStore.food(food);check(food.getBoolean("estimated"),"estimate flag retained");check(food.getJSONArray("components").length()==1,"estimate breakdown retained");
  JSONObject backup=LocalStore.validateBackup(new JSONObject().put("logs",new org.json.JSONArray().put(log)).put("foodEntries",new org.json.JSONArray().put(food)).toString());
  check(backup.getJSONArray("logs").length()==1,"check-in import");check(backup.getJSONArray("logs").getJSONObject(0).getDouble("waterMl")==1750,"water backup roundtrip");check(backup.getJSONArray("logs").getJSONObject(0).getBoolean("sugarFree"),"habit backup roundtrip");check(backup.getJSONArray("foodEntries").length()==1,"food import");
  try{LocalStore.validateBackup("{\"logs\":[],\"foodEntries\":[{}]}");throw new AssertionError("invalid backup accepted");}catch(IllegalArgumentException expected){}
  JSONObject rich=LocalStore.food(new JSONObject().put("id","protein-1").put("day","2026-11-12").put("name","Shake").put("portion","1 scoop").put("meal","Snacks").put("calories",120).put("servings",2).put("proteinG",24));
  check(rich.getDouble("proteinG")==24,"protein survives canonicalization");
  rejects(()->LocalStore.food(new JSONObject(rich.toString()).put("proteinG",-1)),"negative protein");
  rejects(()->LocalStore.food(new JSONObject(rich.toString()).put("proteinG",600)),"total protein bound");
  check(!LocalStore.food(new JSONObject(rich.toString()).put("proteinG",JSONObject.NULL)).has("proteinG"),"unknown protein is not zero");
  JSONObject setup=LocalStore.profile(new JSONObject().put("name","Demo").put("startingWeight",79).put("goalWeight",75).put("waterGoalMl",2500).put("proteinGoalG",100).put("completed",true));
  check(setup.getInt("waterGoalMl")==2500,"profile values retained");
  check(!LocalStore.profile(new JSONObject()).has("startingWeight"),"fresh profile has no personal default");
  rejects(()->LocalStore.profile(new JSONObject().put("waterGoalMl",-1)),"negative water goal");
  rejects(()->LocalStore.profile(new JSONObject().put("height","NaN")),"invalid height");
  JSONObject saved=LocalStore.savedFood(new JSONObject(rich.toString()).put("favorite",true));check(saved.getBoolean("favorite")&&!saved.has("day"),"saved food has independent identity");
  JSONObject extended=LocalStore.validateBackup(new JSONObject().put("profile",setup).put("savedFoods",new org.json.JSONArray().put(saved)).put("logs",new org.json.JSONArray().put(log)).put("foodEntries",new org.json.JSONArray().put(rich)).toString());
  check(extended.getJSONObject("profile").getBoolean("completed"),"profile backup");check(extended.getJSONArray("savedFoods").getJSONObject(0).getDouble("proteinG")==24,"saved food backup protein");
  rejects(()->LocalStore.validateBackup(new JSONObject().put("logs",new org.json.JSONArray()).put("foodEntries",new org.json.JSONArray()).put("savedFoods","bad").toString()),"bad saved foods rejected before import");
  JSONObject patch=LocalStore.patchLog(log,new JSONObject().put("day",log.getString("day")).put("waterDelta",250));
  check(patch.getDouble("waterMl")==2000&&patch.getDouble("weight")==78.5&&patch.getBoolean("sugarFree"),"quick water preserves unrelated measurements and habits");
  patch=LocalStore.patchLog(patch,new JSONObject().put("day",log.getString("day")).put("toggle","sugarFree"));check(!patch.getBoolean("sugarFree")&&patch.getBoolean("proteinShake"),"habit toggle preserves others");
  rejects(()->LocalStore.patchLog(new JSONObject().put("waterMl",20000),new JSONObject().put("day","2026-11-12").put("waterDelta",250)),"water overflow rejected");
  final JSONObject logForPatch=log;
  rejects(()->LocalStore.patchLog(logForPatch,new JSONObject().put("day","2026-11-12").put("toggle","notes")),"invalid habit rejected");
  JSONObject activity=LocalStore.activity(new JSONObject().put("id","walk-1").put("day","2026-11-13").put("name","Walking").put("minutes",30).put("met",3.8).put("weightKg",79).put("activeCalories",9999));
  check(activity.getLong("activeCalories")==116&&activity.getLong("grossCalories")==158,"recompute activity burn instead of trusting submitted calories");
  rejects(()->LocalStore.activity(new JSONObject(activity.toString()).put("minutes",0)),"zero activity minutes");
  rejects(()->LocalStore.activity(new JSONObject(activity.toString()).put("met",99)),"invalid MET");
  JSONObject activityBackup=LocalStore.validateBackup(new JSONObject().put("logs",new org.json.JSONArray()).put("foodEntries",new org.json.JSONArray()).put("activityEntries",new org.json.JSONArray().put(activity)).toString());check(activityBackup.getJSONArray("activityEntries").length()==1,"activity backup roundtrip");
  JSONObject ai=GeminiMealClient.validate(new JSONObject().put("recognized",true).put("calories",600).put("proteinG",25).put("assumptions","One plate, moderate oil"));check(ai.getDouble("calories")==600,"AI schema canonicalized");
  rejects(()->GeminiMealClient.validate(new JSONObject().put("recognized",true).put("calories",-5)),"negative AI estimate");
  rejects(()->GeminiMealClient.validate(new JSONObject().put("recognized","true").put("calories",500)),"AI boolean must be boolean");
  check(!GeminiMealClient.validate(new JSONObject().put("recognized",false).put("message","What portion?")).has("calories"),"unrecognized AI has no numeric estimate");
  check(HealthConnection.toKilocalories(123000)==123,"Health Connect platform calories convert to kcal");
  JSONObject aiFood=LocalStore.food(new JSONObject(rich.toString()).put("estimated",true).put("aiEstimated",true).put("aiModel","gemini-3.5-flash-lite").put("aiAssumptions","Label-sized portion"));check(aiFood.getBoolean("aiEstimated"),"AI provenance retained");
  JSONObject metricOld=new JSONObject().put("state","available").put("value",123).put("observedAt","old").put("sources",new org.json.JSONArray().put("Band"));
  JSONObject oldMetrics=new JSONObject(),newMetrics=new JSONObject();for(String k:new String[]{"active","total","steps","distance","sleep"}){oldMetrics.put(k,new JSONObject(metricOld.toString()));newMetrics.put(k,new JSONObject().put("state","error").put("value",JSONObject.NULL).put("observedAt","new"));}
  JSONObject previous=new JSONObject().put("metrics",oldMetrics),incoming=new JSONObject().put("metrics",newMetrics);
  JSONObject merged=LocalStore.mergeHealth(previous,incoming);check(merged.getJSONObject("metrics").getJSONObject("active").getDouble("value")==123,"read error retains cached value");check(merged.getJSONObject("metrics").getJSONObject("active").getString("observedAt").equals("old"),"cached error keeps old timestamp");
  newMetrics.put("active",new JSONObject().put("state","permission_denied").put("value",JSONObject.NULL));newMetrics.put("total",new JSONObject().put("state","no_data").put("value",JSONObject.NULL));merged=LocalStore.mergeHealth(previous,incoming);check(merged.getJSONObject("metrics").getJSONObject("active").isNull("value"),"revoked permission cannot retain cached value");check(merged.getJSONObject("metrics").getJSONObject("total").isNull("value"),"no-data clears old value rather than repeating it");
  check(LocalStore.activity(new JSONObject(activity.toString()).put("additional",true)).getBoolean("additional"),"explicit extra burn retained");check(!LocalStore.activity(activity).getBoolean("additional"),"old activity logs do not become extra burn");
  JSONObject spring=HealthConnection.windows("2026-03-08",java.time.ZoneId.of("America/New_York"),java.time.Instant.parse("2026-03-10T12:00:00Z"));check(java.time.Duration.between(java.time.Instant.parse(spring.getString("periodStart")),java.time.Instant.parse(spring.getString("periodEnd"))).toHours()==23,"DST day uses calendar boundaries, not 24 fixed hours");
  JSONObject todayWindow=HealthConnection.windows("2026-11-13",java.time.ZoneId.of("Asia/Kolkata"),java.time.Instant.parse("2026-11-13T04:30:00Z"));check(todayWindow.getString("periodEnd").equals("2026-11-13T04:30:00Z"),"today ends at read time");check(todayWindow.getString("sleepEnd").equals(todayWindow.getString("periodEnd")),"incomplete sleep window clamped to read time");
  rejects(()->HealthConnection.windows("2026-11-14",java.time.ZoneId.of("Asia/Kolkata"),java.time.Instant.parse("2026-11-13T04:30:00Z")),"future read date rejected");
  System.out.println("Passed native validation: canonical fields, dates, ranges, fractions and backup validation.");
 }
}
