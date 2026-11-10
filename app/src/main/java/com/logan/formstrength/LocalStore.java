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
    LocalStore(Context context) { super(context, "form_strength.db", null, 1); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE daily_logs (day TEXT PRIMARY KEY NOT NULL, payload TEXT NOT NULL)");
        db.execSQL("CREATE TABLE calorie_entries (id TEXT PRIMARY KEY NOT NULL, day TEXT NOT NULL, payload TEXT NOT NULL)");
        db.execSQL("CREATE INDEX food_day ON calorie_entries(day)");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Append explicit, non-destructive migrations when the schema changes.
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
        String[] keys={"weight","waist","steps","sleep"};double[] mins={30,40,0,0},maxes={250,200,100000,24};
        for(int i=0;i<keys.length;i++){String k=keys[i];if(data.has(k)&&!data.isNull(k)&&!"".equals(data.optString(k)))p.put(k,number(data,k,mins[i],maxes[i]));}
        String[] checks={"workout","posture","walk","protein","vegetables"};
        for(String k:checks)p.put(k,Boolean.TRUE.equals(data.opt(k)));
        String session=text(data,"session",1,false);if(!session.matches("[ABC]"))session="A";
        p.put("session",session);p.put("notes",text(data,"notes",1500,false));return p;
    }
    static JSONObject food(JSONObject data) throws JSONException {
        JSONObject p=new JSONObject();p.put("id",foodId(data));p.put("day",day(data));
        p.put("name",text(data,"name",120,true));p.put("portion",text(data,"portion",100,true));
        String meal=text(data,"meal",12,true);require(meal.matches("Breakfast|Lunch|Dinner|Snacks"),"Choose a valid meal.");p.put("meal",meal);
        double c=number(data,"calories",0,10000),s=number(data,"servings",0.01,100);require(c*s<=20000,"Check calories and servings.");p.put("calories",c);p.put("servings",s);return p;
    }
    private void putLog(SQLiteDatabase db,JSONObject p,int conflict) throws JSONException{
        ContentValues v=new ContentValues();v.put("day",p.getString("day"));v.put("payload",p.toString());db.insertWithOnConflict("daily_logs",null,v,conflict);
    }
    private void putFood(SQLiteDatabase db,JSONObject p,int conflict) throws JSONException{
        ContentValues v=new ContentValues();v.put("id",p.getString("id"));v.put("day",p.getString("day"));v.put("payload",p.toString());db.insertWithOnConflict("calorie_entries",null,v,conflict);
    }
    synchronized JSONArray all(String table) throws JSONException{
        require(table.equals("daily_logs")||table.equals("calorie_entries"),"Invalid records.");
        JSONArray rows=new JSONArray();try(Cursor c=getReadableDatabase().query(table,new String[]{"payload"},null,null,null,null,table.equals("daily_logs")?"day ASC":"day ASC,id ASC")){while(c.moveToNext())rows.put(new JSONObject(c.getString(0)));}return rows;
    }
    synchronized JSONObject request(String path,String method,String body) throws JSONException{
        boolean isLog=path.equals("/api/logs");require(isLog||path.equals("/api/foods"),"Unknown operation.");
        if(method.equals("GET"))return new JSONObject().put(isLog?"logs":"entries",all(isLog?"daily_logs":"calorie_entries"));
        require(body!=null&&body.length()<=20000,"Entry is too large.");JSONObject data=new JSONObject(body);SQLiteDatabase db=getWritableDatabase();
        if(method.equals("DELETE")){if(isLog)db.delete("daily_logs","day=?",new String[]{day(data)});else db.delete("calorie_entries","id=?",new String[]{foodId(data)});return new JSONObject().put("ok",true);}
        require(method.equals("PUT"),"Invalid operation.");JSONObject p=isLog?log(data):food(data);
        if(isLog)putLog(db,p,SQLiteDatabase.CONFLICT_REPLACE);else putFood(db,p,SQLiteDatabase.CONFLICT_REPLACE);
        return new JSONObject().put("ok",true).put("entry",p);
    }
    static JSONObject validateBackup(String raw) throws JSONException{
        require(raw.length()<=5*1024*1024,"Backup is too large (maximum 5 MB).");JSONObject root=new JSONObject(raw);
        JSONArray logs=root.optJSONArray("logs"),foods=root.optJSONArray("foodEntries");require(logs!=null&&foods!=null,"Choose a Form & Strength JSON export with check-ins and food entries.");
        require(logs.length()<=5000&&foods.length()<=10000,"Backup has too many entries.");
        JSONArray validLogs=new JSONArray(),validFoods=new JSONArray();for(int i=0;i<logs.length();i++)validLogs.put(log(logs.getJSONObject(i)));for(int i=0;i<foods.length();i++)validFoods.put(food(foods.getJSONObject(i)));
        return new JSONObject().put("logs",validLogs).put("foodEntries",validFoods);
    }
    synchronized String importBackup(JSONObject backup) throws JSONException{
        SQLiteDatabase db=getWritableDatabase();int before=all("daily_logs").length()+all("calorie_entries").length();db.beginTransaction();try{
            JSONArray logs=backup.getJSONArray("logs"),foods=backup.getJSONArray("foodEntries");
            for(int i=0;i<logs.length();i++)putLog(db,logs.getJSONObject(i),SQLiteDatabase.CONFLICT_IGNORE);
            for(int i=0;i<foods.length();i++)putFood(db,foods.getJSONObject(i),SQLiteDatabase.CONFLICT_IGNORE);
            db.setTransactionSuccessful();
        }finally{db.endTransaction();}
        int added=all("daily_logs").length()+all("calorie_entries").length()-before;
        return "Imported "+added+" new entries. Existing entries were kept.";
    }
}
