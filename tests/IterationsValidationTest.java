package com.logan.formstrength;
import org.json.*;
/** Pure validation only: native camera, alarm delivery and permissions require Android. */
public final class IterationsValidationTest {
 interface Task{void run()throws Exception;}
 static void check(boolean b,String label){if(!b)throw new AssertionError(label);}
 static void rejects(Task task)throws Exception{try{task.run();throw new AssertionError("Expected validation failure");}catch(IllegalArgumentException expected){}}
 public static void main(String[] args)throws Exception{
  JSONObject input=new JSONObject().put("name","QA").put("age",24).put("sex","male").put("goalMode","lose").put("goalMonths",6).put("equipment","dumbbells").put("cuisine","Indian vegetarian").put("supplements","none").put("onboardingVersion",1).put("completed",true);
  JSONObject p=LocalStore.profile(input);check(p.getInt("age")==24&&p.getString("cuisine").equals("Indian vegetarian")&&p.getInt("goalMonths")==6,"local onboarding fields preserved");
  rejects(()->LocalStore.profile(new JSONObject(input.toString()).put("age",17)));rejects(()->LocalStore.profile(new JSONObject(input.toString()).put("age",24.5)));rejects(()->LocalStore.profile(new JSONObject(input.toString()).put("goalMonths",0)));rejects(()->LocalStore.profile(new JSONObject(input.toString()).put("goalMonths",37)));rejects(()->LocalStore.profile(new JSONObject(input.toString()).put("equipment","invented")));
  JSONObject food=new JSONObject().put("id","qa-budget").put("name","Dal").put("portion","1 cup").put("calories",200).put("servings",1).put("priceINR",25);
  check(LocalStore.savedFood(food).getDouble("priceINR")==25,"user price retained");check(!LocalStore.savedFood(food).has("proteinG"),"budget does not invent protein");rejects(()->LocalStore.savedFood(new JSONObject(food.toString()).put("priceINR",-1)));
  JSONObject backup=new JSONObject().put("logs",new JSONArray()).put("foodEntries",new JSONArray()).put("profile",input).put("savedFoods",new JSONArray().put(food));JSONObject clean=LocalStore.validateBackup(backup.toString());check(clean.getJSONObject("profile").getInt("goalMonths")==6&&clean.getJSONArray("savedFoods").getJSONObject(0).getDouble("priceINR")==25,"new profile and budget survive backup validation");
  check(MealPhotoProvider.validName("meal-00000000-0000-0000-0000-000000000001.jpg"),"capture token accepted");check(!MealPhotoProvider.validName(null)&&!MealPhotoProvider.validName("../../secret.jpg")&&!MealPhotoProvider.validName("meal-%2Fsecret.jpg")&&!MealPhotoProvider.validName("/meal-00000000-0000-0000-0000-000000000001.jpg"),"unsafe capture paths rejected");
  JSONObject session=LocalStore.log(new JSONObject().put("day","2026-11-22").put("workoutActiveSeconds",90).put("workoutBreakSeconds",30));check(session.getInt("workoutActiveSeconds")==90&&session.getInt("workoutBreakSeconds")==30,"workout active and rest persist separately");rejects(()->LocalStore.log(new JSONObject().put("day","2026-11-22").put("workoutActiveSeconds",-1)));
  java.time.ZoneId zone=java.time.ZoneId.of("Asia/Kolkata");long morning=java.time.ZonedDateTime.of(2026,11,22,7,30,0,0,zone).toInstant().toEpochMilli();
  check(java.time.Instant.ofEpochMilli(ReminderScheduler.nextTime("water",morning,zone)).atZone(zone).getHour()==8,"water starts in daytime");
  check(java.time.Instant.ofEpochMilli(ReminderScheduler.nextTime("walk",morning,zone)).atZone(zone).getHour()==9,"walk daytime schedule");
  long late=java.time.ZonedDateTime.of(2026,11,22,21,0,0,0,zone).toInstant().toEpochMilli();check(java.time.Instant.ofEpochMilli(ReminderScheduler.nextTime("windup",late,zone)).atZone(zone).getDayOfMonth()==23,"windup rolls next day");
  java.time.ZoneId dst=java.time.ZoneId.of("America/New_York");long boundary=java.time.ZonedDateTime.of(2026,3,7,22,0,0,0,dst).toInstant().toEpochMilli();check(java.time.Instant.ofEpochMilli(ReminderScheduler.nextTime("water",boundary,dst)).atZone(dst).getHour()==8,"DST preserves local reminder hour");
  System.out.println("Passed local onboarding, budget backup and camera token validation.");
 }
}
