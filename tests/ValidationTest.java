package com.logan.formstrength;
import org.json.JSONObject;
public class ValidationTest {
 static void check(boolean value,String msg){if(!value)throw new AssertionError(msg);}
 static void rejects(Runnable fn,String label){try{fn.run();throw new AssertionError(label);}catch(IllegalArgumentException expected){}}
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
  System.out.println("Passed native validation: canonical fields, dates, ranges, fractions and backup validation.");
 }
}
