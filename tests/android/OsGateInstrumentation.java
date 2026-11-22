package com.logan.formstrength.qa;
import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.os.*;
import org.json.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
/** Same-signer OS tests against the unmodified release. Only synthetic dedicated QA data. */
public final class OsGateInstrumentation extends Instrumentation {
 private Bundle args;private StringBuilder results=new StringBuilder();
 public void onCreate(Bundle b){super.onCreate(b);args=b;start();}
 private void ok(boolean b,String text){if(!b)throw new AssertionError(text);results.append("PASS ").append(text).append('\n');}
 private Class<?> cls(String name)throws Exception{return getTargetContext().getClassLoader().loadClass("com.logan.formstrength."+name);}
 private Object call(Object obj,String name,Class<?>[] types,Object...v)throws Exception{
  Method m=(obj instanceof Class?(Class<?>)obj:obj.getClass()).getDeclaredMethod(name,types);m.setAccessible(true);
  try{return m.invoke(obj instanceof Class?null:obj,v);}catch(InvocationTargetException e){Throwable t=e.getCause();if(t instanceof Exception)throw (Exception)t;if(t instanceof Error)throw (Error)t;throw e;}
 }
 private Object store()throws Exception{Constructor<?> c=cls("LocalStore").getDeclaredConstructor(Context.class);c.setAccessible(true);return c.newInstance(getTargetContext());}
 private JSONObject request(Object s,String path)throws Exception{return (JSONObject)call(s,"request",new Class[]{String.class,String.class,String.class},path,"GET",null);}
 private String snapshot(Object s)throws Exception{return request(s,"/api/logs").toString()+request(s,"/api/foods")+request(s,"/api/activities");}
 private void write(File f,String text)throws Exception{try(FileOutputStream o=new FileOutputStream(f)){o.write(text.getBytes(StandardCharsets.UTF_8));}}
 private String read(File f)throws Exception{try(FileInputStream i=new FileInputStream(f);ByteArrayOutputStream o=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=i.read(b))!=-1)o.write(b,0,n);return o.toString("UTF-8");}}
 private String js(final android.webkit.WebView web,final String script)throws Exception{
  java.util.concurrent.CountDownLatch latch=new java.util.concurrent.CountDownLatch(1);String[] result={null};
  runOnMainSync(()->web.evaluateJavascript(script,value->{result[0]=value;latch.countDown();}));
  if(!latch.await(30,java.util.concurrent.TimeUnit.SECONDS))throw new AssertionError("JavaScript callback timeout");return result[0];
 }
 private void framework()throws Exception{
  Intent intent=new Intent().setClassName("com.logan.formstrength","com.logan.formstrength.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  Activity a=startActivitySync(intent);ok(a!=null,"release MainActivity launched on Android");
  Field f=a.getClass().getDeclaredField("web");f.setAccessible(true);android.webkit.WebView web=(android.webkit.WebView)f.get(a);
  boolean ready=false;for(int i=0;i<30;i++){if("true".equals(js(web,"typeof window.handleNativeBack==='function'"))){ready=true;break;}Thread.sleep(1000);}ok(ready,"bundled WebView application initialized");
  Constructor<?> constructor=cls("GeminiMealClient").getDeclaredConstructor(Activity.class);constructor.setAccessible(true);Object client=constructor.newInstance(a);
  call(client,"clear",new Class[]{});
  String dummy="synthetic_qa_key_not_a_real_api_key";
  call(client,"updateSettings",new Class[]{String.class,String.class},dummy,"gemini-qa-first");
  ok(Boolean.TRUE.equals(call(client,"configured",new Class[]{})),"Android encrypted connection stored");
  Object reopened=constructor.newInstance(a);ok(dummy.equals(call(reopened,"secret",new Class[]{})),"AndroidKeyStore decrypt after client recreation");
  call(reopened,"updateSettings",new Class[]{String.class,String.class},"","gemini-qa-second");
  Object again=constructor.newInstance(a);ok(dummy.equals(call(again,"secret",new Class[]{}))&&"gemini-qa-second".equals(call(again,"model",new Class[]{})),"model-only persistence retains decryptable key");
  call(again,"clear",new Class[]{});ok(!Boolean.TRUE.equals(call(again,"configured",new Class[]{})),"synchronous key removal observed");
  Constructor<?> hc=cls("HealthConnection").getDeclaredConstructor(Activity.class);hc.setAccessible(true);Object health=hc.newInstance(a);
  JSONObject status=(JSONObject)call(health,"status",new Class[]{});results.append("INFO HealthConnect permission status ").append(status.toString()).append('\n');
  js(web,"document.querySelectorAll('dialog[open]').forEach(d=>d.close());document.getElementById('quickFood').click();");
  ok("true".equals(js(web,"document.getElementById('foodDialog').open")),"clean meal modal opened");
  sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK);Thread.sleep(1000);
  ok("false".equals(js(web,"document.getElementById('foodDialog').open")),"system Back closes clean modal");
  sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK);Thread.sleep(1000);ok(a.isFinishing()||a.isDestroyed(),"system Back on Home finishes activity");
 }
 public void onStart(){Bundle out=new Bundle();try{
  String stage=args.getString("stage","upgrade");results.append("Android API ").append(Build.VERSION.SDK_INT).append(" stage ").append(stage).append('\n');
  Object s=store();SQLiteDatabase db=((SQLiteOpenHelper)s).getWritableDatabase();File expected=new File(getTargetContext().getFilesDir(),"qa-before-upgrade.txt");
  if(stage.equals("seed")){
   ok(db.getVersion()==4,"baseline schema4");
   String raw="{\"logs\":[{\"day\":\"2026-11-18\",\"waterMl\":750}],\"foodEntries\":[{\"id\":\"qa-food\",\"day\":\"2026-11-18\",\"name\":\"4 rotis\",\"meal\":\"Lunch\",\"portion\":\"1 roti\",\"calories\":80,\"servings\":4}],\"activityEntries\":[{\"id\":\"qa-walk\",\"day\":\"2026-11-18\",\"name\":\"Walking\",\"minutes\":30,\"met\":3.5,\"weightKg\":79}],\"savedFoods\":[]}";
   JSONObject valid=(JSONObject)call(cls("LocalStore"),"validateBackup",new Class[]{String.class},raw);call(s,"importBackup",new Class[]{JSONObject.class},valid);
   ok(request(s,"/api/foods").getJSONArray("entries").length()==1,"seed meal stored through production import");write(expected,snapshot(s));ok(expected.length()>0,"canonical baseline persisted");
  }else{
   ok(db.getVersion()==5,"Android SQLiteOpenHelper schema5");
   try(Cursor c=db.rawQuery("PRAGMA index_info(activity_day)",null)){ok(c.moveToFirst()&&"day".equals(c.getString(2)),"activity_day index on day");}
   if(stage.equals("upgrade")){ok(expected.exists(),"baseline exists across APK replacement");ok(snapshot(s).equals(read(expected)),"food activity and check-in payloads unchanged after upgrade");}
   if(stage.equals("capacity")){
    JSONArray saved=new JSONArray();for(int i=0;i<1000;i++)saved.put(new JSONObject().put("id","qa-saved-"+i).put("name","Synthetic food "+i).put("portion","1 serving").put("calories",80).put("servings",1));
    JSONObject batch=new JSONObject().put("logs",new JSONArray()).put("foodEntries",new JSONArray()).put("savedFoods",saved);
    JSONObject valid=(JSONObject)call(cls("LocalStore"),"validateBackup",new Class[]{String.class},batch.toString());call(s,"importBackup",new Class[]{JSONObject.class},valid);
    ok(request(s,"/api/saved-foods").getJSONArray("foods").length()==1000,"Android import accepts exactly1000 saved foods");
    String before=snapshot(s);JSONObject overflow=new JSONObject().put("logs",new JSONArray().put(new JSONObject().put("day","2026-11-17").put("waterMl",100))).put("foodEntries",new JSONArray()).put("savedFoods",new JSONArray().put(new JSONObject(saved.getJSONObject(0).toString()).put("id","qa-overflow")));
    JSONObject clean=(JSONObject)call(cls("LocalStore"),"validateBackup",new Class[]{String.class},overflow.toString());boolean rejected=false;try{call(s,"importBackup",new Class[]{JSONObject.class},clean);}catch(IllegalArgumentException e){rejected=true;}ok(rejected,"Android import rejects1001st saved food");ok(snapshot(s).equals(before),"overflow import leaves other tables unchanged");
    call(s,"importBackup",new Class[]{JSONObject.class},valid);ok(request(s,"/api/saved-foods").getJSONArray("foods").length()==1000,"duplicate restore allowed at capacity");
   }
   if(stage.equals("backup")){
    Class<?> files=cls("BackupFiles");File dir=new File(getTargetContext().getCacheDir(),"os-qa");String name="export-00000000-0000-0000-0000-000000000001.json";
    char[] a=new char[5*1024*1024];java.util.Arrays.fill(a,'a');String exact=new String(a);call(files,"create",new Class[]{File.class,String.class,String.class},dir,name,exact);
    ByteArrayOutputStream bytes=new ByteArrayOutputStream();call(files,"copyTo",new Class[]{File.class,String.class,OutputStream.class},dir,name,bytes);ok(bytes.size()==a.length,"5MB snapshot filesystem roundtrip on Android");
    boolean rejected=false;try{call(files,"create",new Class[]{File.class,String.class,String.class},dir,name,exact+"x");}catch(IllegalArgumentException e){rejected=true;}ok(rejected,"over5MB snapshot rejected on Android");call(files,"delete",new Class[]{File.class,String.class},dir,name);
   }
  }
  ((SQLiteOpenHelper)s).close();if(stage.equals("framework"))framework();out.putString("results",results.toString());finish(Activity.RESULT_OK,out);
 }catch(Throwable t){out.putString("results",results.toString());out.putString("failure",t.toString());finish(Activity.RESULT_CANCELED,out);}}
}
