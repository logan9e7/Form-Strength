package com.logan.formstrength;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Locale;

/** App-private SQLite. No network, analytics, accounts or cloud backup. */
final class LocalStore extends SQLiteOpenHelper {
    LocalStore(Context context) { super(context, "form_strength.db", null, 3); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE daily_logs (day TEXT PRIMARY KEY NOT NULL, payload TEXT NOT NULL)");
        db.execSQL("CREATE TABLE calorie_entries (id TEXT PRIMARY KEY NOT NULL, day TEXT NOT NULL, payload TEXT NOT NULL)");
        db.execSQL("CREATE INDEX food_day ON calorie_entries(day)");
        createPreferences(db);
        createActivities(db);
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion<2)createPreferences(db);
        if(oldVersion<3)createActivities(db);
    }
    private static void createActivities(SQLiteDatabase db){db.execSQL("CREATE TABLE IF NOT EXISTS activity_entries (id TEXT PRIMARY KEY NOT NULL, day TEXT NOT NULL, payload TEXT NOT NULL)");}
    private static void createPreferences(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS preferences (id TEXT PRIMARY KEY NOT NULL, payload TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS saved_foods (id TEXT PRIMARY KEY NOT NULL, payload TEXT NOT NULL)");
    }
    static JSONObject activity(JSONObject data) throws JSONException {
        JSONObject p=new JSONObject().put("id",foodId(data)).put("day",day(data)).put("name",text(data,"name",120,true));
        double minutes=number(data,"minutes",0.1,720),met=number(data,"met",1,30),kg=number(data,"weightKg",30,250);
        p.put("minutes",minutes).put("met",met).put("weightKg",kg).put("activeCalories",Math.round((met-1)*3.5*kg/200*minutes)).put("grossCalories",Math.round(met*3.5*kg/200*minutes));
        p.put("description",text(data,"description",240,false)).put("code",text(data,"code",20,false)).put("source","Adult Compendium 2024 · MET estimate");return p;
    }
    private void putActivity(SQLiteDatabase db,JSONObject p,int conflict) throws JSONException {
        ContentValues v=new ContentValues();v.put("id",p.getString("id"));v.put("day",p.getString("day"));v.put("payload",p.toString());db.insertWithOnConflict("activity_entries",null,v,conflict);
    }
    static JSONObject profile(JSONObject data) throws JSONException {
        JSONObject p=new JSONObject();p.put("name",text(data,"name",40,false));
        String[] keys={"height","startingWeight","startingWaist","goalWeight","waterGoalMl","proteinGoalG"};
        double[] mins={80,30,40,30,0,0},maxes={250,250,200,250,10000,500};
        for(int i=0;i<keys.length;i++){String k=keys[i];if(data.has(k)&&!data.isNull(k)&&!"".equals(data.optString(k)))p.put(k,number(data,k,mins[i],maxes[i]));}
        p.put("completed",Boolean.TRUE.equals(data.opt("completed")));return p;
    }
    static JSONObject savedFood(JSONObject data) throws JSONException {
        JSONObject copy=new JSONObject(data.toString()).put("day","2000-01-01").put("meal","Snacks");
        JSONObject p=food(copy);p.remove("day");p.remove("meal");
        p.put("favorite",Boolean.TRUE.equals(data.opt("favorite")));return p;
    }
    static void require(boolean condition, String message) { if (!condition) throw new IllegalArgumentException(message); }
    static String text(JSONObject data, String key, int max, boolean required) {
        Object raw=data.opt(key);
        require(raw==null || raw==JSONObject.NULL || raw instanceof String, "Check " + key + ".");
        String value=data.optString(key, "").trim();
        require(value.length()<=max && (!required || !value.isEmpty()), "Check " + key + ".");
        return value;
    }
    static String day(JSONObject data) {
        String value=text(data,"day",10,true);
        require(value.matches("\\d{4}-\\d{2}-\\d{2}"),"Choose a valid date.");
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd",Locale.US);format.setLenient(false);
        try { require(format.format(format.parse(value)).equals(value),"Choose a valid date."); }
        catch(java.text.ParseException e){throw new IllegalArgumentException("Choose a valid date.");}
        return value;
    }
    static double number(JSONObject data,String key,double min,double max) {
        Object raw=data.opt(key);
        require(raw instanceof Number || raw instanceof String,"Check "+key+".");
        require(!(raw instanceof String) || !((String)raw).trim().isEmpty(),"Check "+key+".");
        double n=data.optDouble(key,Double.NaN);
        require(!Double.isNaN(n) && !Double.isInfinite(n) && n>=min && n<=max,"Check "+key+".");
        return n;
    }
    static String foodId(JSONObject data){String id=text(data,"id",80,true);require(id.matches("[a-zA-Z0-9-]{1,80}"),"Invalid food entry ID.");return id;}
    static JSONObject log(JSONObject data) throws JSONException {
        JSONObject p=new JSONObject();p.put("day",day(data));
        String[] keys={"weight","waist","steps","sleep","waterMl"};double[] mins={30,40,0,0,0},maxes={250,200,100000,24,20000};
        for(int i=0;i<keys.length;i++){String k=keys[i];if(data.has(k)&&!data.isNull(k)&&!"".equals(data.optString(k)))p.put(k,number(data,k,mins[i],maxes[i]));}
        String[] checks={"workout","posture","walk","protein","vegetables","sugarFree","proteinShake"};
        for(String k:checks)p.put(k,Boolean.TRUE.equals(data.opt(k)));
        String session=text(data,"session",1,false);if(!session.matches("[ABC]"))session="A";
        p.put("session",session);p.put("notes",text(data,"notes",1500,false));return p;
    }
    static JSONObject patchLog(JSONObject existing,JSONObject change) throws JSONException {
        JSONObject merged=new JSONObject(existing.toString());merged.put("day",day(change));
        if(change.has("waterDelta")){double delta=number(change,"waterDelta",1,2000);merged.put("waterMl",existing.optDouble("waterMl",0)+delta);}
        if(change.has("toggle")){String key=text(change,"toggle",20,true);require(key.matches("sugarFree|proteinShake|workout|posture|walk|protein|vegetables"),"Unknown habit.");merged.put(key,!existing.optBoolean(key,false));}
        require(change.has("waterDelta")||change.has("toggle"),"Choose an action.");return log(merged);
    }
    static JSONObject food(JSONObject data) throws JSONException {
        JSONObject p=new JSONObject();p.put("id",foodId(data));p.put("day",day(data));
        p.put("name",text(data,"name",120,true));p.put("portion",text(data,"portion",100,true));
        String meal=text(data,"meal",12,true);require(meal.matches("Breakfast|Lunch|Dinner|Snacks"),"Choose a valid meal.");p.put("meal",meal);
        double c=number(data,"calories",0,10000),s=number(data,"servings",0.01,100);require(c*s<=20000,"Check calories and servings.");p.put("calories",c);p.put("servings",s);
        if(data.has("proteinG")&&!data.isNull("proteinG")&&!"".equals(data.optString("proteinG"))){double protein=number(data,"proteinG",0,1000);require(protein*s<=1000,"Check protein and servings.");p.put("proteinG",protein);}
        if(Boolean.TRUE.equals(data.opt("estimated"))){
            p.put("estimated",true);p.put("estimateBasis",text(data,"estimateBasis",500,false));
            JSONArray components=data.optJSONArray("components");if(components!=null){require(components.length()<=20,"Too many foods in one meal.");JSONArray clean=new JSONArray();for(int i=0;i<components.length();i++){JSONObject component=components.getJSONObject(i);clean.put(new JSONObject().put("label",text(component,"label",180,true)).put("calories",number(component,"calories",0,20000)).put("source",text(component,"source",120,true)));}p.put("components",clean);}
        }
        if(Boolean.TRUE.equals(data.opt("aiEstimated"))){p.put("estimated",true);p.put("aiEstimated",true);p.put("aiModel",text(data,"aiModel",80,false));p.put("aiAssumptions",text(data,"aiAssumptions",1000,false));}
        return p;
    }
    private void putLog(SQLiteDatabase db,JSONObject p,int conflict) throws JSONException{
        ContentValues v=new ContentValues();v.put("day",p.getString("day"));v.put("payload",p.toString());db.insertWithOnConflict("daily_logs",null,v,conflict);
    }
    private void putFood(SQLiteDatabase db,JSONObject p,int conflict) throws JSONException{
        ContentValues v=new ContentValues();v.put("id",p.getString("id"));v.put("day",p.getString("day"));v.put("payload",p.toString());db.insertWithOnConflict("calorie_entries",null,v,conflict);
    }
    synchronized JSONArray all(String table) throws JSONException{
        require(table.equals("daily_logs")||table.equals("calorie_entries")||table.equals("saved_foods")||table.equals("activity_entries"),"Invalid records.");
        JSONArray rows=new JSONArray();try(Cursor c=getReadableDatabase().query(table,new String[]{"payload"},null,null,null,null,table.equals("daily_logs")?"day ASC":table.equals("saved_foods")?"id ASC":"day ASC,id ASC")){while(c.moveToNext())rows.put(new JSONObject(c.getString(0)));}return rows;
    }
    synchronized JSONObject request(String path,String method,String body) throws JSONException{
        if(path.equals("/api/activities")){
            if(method.equals("GET"))return new JSONObject().put("entries",all("activity_entries"));
            require(body!=null&&body.length()<=20000,"Activity entry too large.");JSONObject d=new JSONObject(body);SQLiteDatabase db=getWritableDatabase();
            if(method.equals("DELETE")){db.delete("activity_entries","id=?",new String[]{foodId(d)});return new JSONObject().put("ok",true);}
            require(method.equals("POST"),"Invalid operation.");JSONArray input=d.getJSONArray("entries");require(input.length()>0&&input.length()<=10,"Choose 1–10 activities.");JSONArray clean=new JSONArray();for(int i=0;i<input.length();i++)clean.put(activity(input.getJSONObject(i)));
            db.beginTransaction();try{for(int i=0;i<clean.length();i++)putActivity(db,clean.getJSONObject(i),SQLiteDatabase.CONFLICT_REPLACE);db.setTransactionSuccessful();}finally{db.endTransaction();}
            return new JSONObject().put("entries",clean);
        }
        if(path.equals("/api/profile")) {
            if(method.equals("GET"))return new JSONObject().put("profile",getProfile());
            require(method.equals("PUT"),"Invalid operation.");require(body!=null&&body.length()<=4000,"Profile too large.");
            JSONObject p=profile(new JSONObject(body));putPayload(getWritableDatabase(),"preferences","profile",p,SQLiteDatabase.CONFLICT_REPLACE);return new JSONObject().put("profile",p);
        }
        if(path.equals("/api/saved-foods")) {
            if(method.equals("GET"))return new JSONObject().put("foods",all("saved_foods"));
            require(body!=null&&body.length()<=20000,"Entry too large.");JSONObject d=new JSONObject(body);
            if(method.equals("DELETE")){getWritableDatabase().delete("saved_foods","id=?",new String[]{foodId(d)});return new JSONObject().put("ok",true);}
            require(method.equals("PUT"),"Invalid operation.");JSONObject p=savedFood(d);JSONArray saved=all("saved_foods");boolean exists=false;for(int i=0;i<saved.length();i++)if(saved.getJSONObject(i).getString("id").equals(p.getString("id")))exists=true;require(exists||saved.length()<1000,"My foods is full (1000 foods). Remove a saved food first.");
            putPayload(getWritableDatabase(),"saved_foods",p.getString("id"),p,SQLiteDatabase.CONFLICT_REPLACE);return new JSONObject().put("entry",p);
        }
        boolean isLog=path.equals("/api/logs");require(isLog||path.equals("/api/foods"),"Unknown operation.");
        if(method.equals("GET"))return new JSONObject().put(isLog?"logs":"entries",all(isLog?"daily_logs":"calorie_entries"));
        require(body!=null&&body.length()<=20000,"Entry is too large.");JSONObject data=new JSONObject(body);SQLiteDatabase db=getWritableDatabase();
        if(method.equals("DELETE")){if(isLog)db.delete("daily_logs","day=?",new String[]{day(data)});else db.delete("calorie_entries","id=?",new String[]{foodId(data)});return new JSONObject().put("ok",true);}
        if(isLog&&method.equals("PATCH")){
            JSONObject current=new JSONObject();String date=day(data);
            try(Cursor c=db.query("daily_logs",new String[]{"payload"},"day=?",new String[]{date},null,null,null)){if(c.moveToFirst())current=new JSONObject(c.getString(0));}
            JSONObject patched=patchLog(current,data);putLog(db,patched,SQLiteDatabase.CONFLICT_REPLACE);return new JSONObject().put("entry",patched);
        }
        require(method.equals("PUT"),"Invalid operation.");JSONObject p=isLog?log(data):food(data);
        if(isLog)putLog(db,p,SQLiteDatabase.CONFLICT_REPLACE);else putFood(db,p,SQLiteDatabase.CONFLICT_REPLACE);
        return new JSONObject().put("ok",true).put("entry",p);
    }
    private void putPayload(SQLiteDatabase db,String table,String id,JSONObject p,int conflict){
        ContentValues v=new ContentValues();v.put("id",id);v.put("payload",p.toString());
        require(db.insertWithOnConflict(table,null,v,conflict)!=-1||conflict==SQLiteDatabase.CONFLICT_IGNORE,"Could not save record.");
    }
    private JSONObject getProfile() throws JSONException {
        try(Cursor c=getReadableDatabase().query("preferences",new String[]{"payload"},"id=?",new String[]{"profile"},null,null,null)) {return c.moveToFirst()?new JSONObject(c.getString(0)):new JSONObject();}
    }
    static JSONObject validateBackup(String raw) throws JSONException{
        require(raw.length()<=5*1024*1024,"Backup is too large (maximum 5 MB).");JSONObject root=new JSONObject(raw);
        JSONArray logs=root.optJSONArray("logs"),foods=root.optJSONArray("foodEntries");require(logs!=null&&foods!=null,"Choose a Form & Strength JSON export with check-ins and food entries.");
        require(logs.length()<=5000&&foods.length()<=10000,"Backup has too many entries.");
        JSONArray validLogs=new JSONArray(),validFoods=new JSONArray();for(int i=0;i<logs.length();i++)validLogs.put(log(logs.getJSONObject(i)));for(int i=0;i<foods.length();i++)validFoods.put(food(foods.getJSONObject(i)));
        JSONObject valid=new JSONObject().put("logs",validLogs).put("foodEntries",validFoods);
        if(root.has("profile")){require(root.optJSONObject("profile")!=null,"Invalid profile.");valid.put("profile",profile(root.getJSONObject("profile")));}
        if(root.has("savedFoods")){JSONArray foodsSaved=root.optJSONArray("savedFoods");require(foodsSaved!=null&&foodsSaved.length()<=1000,"Invalid saved foods.");JSONArray clean=new JSONArray();for(int i=0;i<foodsSaved.length();i++)clean.put(savedFood(foodsSaved.getJSONObject(i)));valid.put("savedFoods",clean);}
        if(root.has("activityEntries")){JSONArray entries=root.optJSONArray("activityEntries");require(entries!=null&&entries.length()<=10000,"Invalid activity entries.");JSONArray clean=new JSONArray();for(int i=0;i<entries.length();i++)clean.put(activity(entries.getJSONObject(i)));valid.put("activityEntries",clean);}
        return valid;
    }
    synchronized String importBackup(JSONObject backup) throws JSONException{
        SQLiteDatabase db=getWritableDatabase();int before=all("daily_logs").length()+all("calorie_entries").length()+all("saved_foods").length()+all("activity_entries").length();db.beginTransaction();try{
            JSONArray logs=backup.getJSONArray("logs"),foods=backup.getJSONArray("foodEntries");
            for(int i=0;i<logs.length();i++)putLog(db,logs.getJSONObject(i),SQLiteDatabase.CONFLICT_IGNORE);
            for(int i=0;i<foods.length();i++)putFood(db,foods.getJSONObject(i),SQLiteDatabase.CONFLICT_IGNORE);
            if(backup.has("savedFoods")){JSONArray saved=backup.getJSONArray("savedFoods");for(int i=0;i<saved.length();i++){JSONObject p=saved.getJSONObject(i);putPayload(db,"saved_foods",p.getString("id"),p,SQLiteDatabase.CONFLICT_IGNORE);}}
            if(backup.has("profile")&&!getProfile().optBoolean("completed",false))putPayload(db,"preferences","profile",backup.getJSONObject("profile"),SQLiteDatabase.CONFLICT_REPLACE);
            if(backup.has("activityEntries")){JSONArray entries=backup.getJSONArray("activityEntries");for(int i=0;i<entries.length();i++)putActivity(db,entries.getJSONObject(i),SQLiteDatabase.CONFLICT_IGNORE);}
            db.setTransactionSuccessful();
        }finally{db.endTransaction();}
        int added=all("daily_logs").length()+all("calorie_entries").length()+all("saved_foods").length()+all("activity_entries").length()-before;
        return "Imported "+added+" new entries. Existing entries were kept. Saved foods were merged; an existing completed profile was kept.";
    }
}
