package com.logan.formstrength;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.HashMap;
import java.util.Locale;

/** Android shell for bundled offline UI, native SQLite and system document backup. */
public final class MainActivity extends Activity {
    private static final String ORIGIN="https://appassets.androidplatform.net";
    private static final int EXPORT=201,IMPORT=202,CAMERA_MEAL=204;
    private String cameraName,cameraRequest;private boolean cameraPicker,pageReady,restoredCapture;private JSONObject deferredCameraResult;
    private WebView web;private LocalStore store;private volatile String pendingExportName;private GeminiMealClient gemini;
    private volatile boolean destroyed,pendingExportPicker,pendingImportPicker;private boolean backPending;
    private final java.util.concurrent.atomic.AtomicBoolean backupBusy=new java.util.concurrent.atomic.AtomicBoolean(false);
    private final ArrayList<AlertDialog> nativeDialogs=new ArrayList<>();
    private final java.util.concurrent.ExecutorService io=java.util.concurrent.Executors.newSingleThreadExecutor();
    private final java.util.concurrent.atomic.AtomicBoolean aiBusy=new java.util.concurrent.atomic.AtomicBoolean(false);
    @Override public void onCreate(Bundle state){
        super.onCreate(state);store=new LocalStore(this);gemini=new GeminiMealClient(this);
        if(state!=null){String name=state.getString("pendingExportName");if(BackupFiles.validName(name)){pendingExportName=name;pendingExportPicker=true;backupBusy.set(true);}pendingImportPicker=state.getBoolean("pendingImportPicker",false);if(pendingImportPicker)backupBusy.set(true);}
        if(state!=null){String photo=state.getString("cameraName"),request=state.getString("cameraRequest");if(MealPhotoProvider.validName(photo)&&request!=null&&request.matches("[A-Za-z0-9-]{1,80}")){cameraName=photo;cameraRequest=request;cameraPicker=true;restoredCapture=true;aiBusy.set(true);}}
        final String retainedPhoto=cameraName;submitIo(()->MealPhotoProvider.prune(this,retainedPhoto));
        final String retained=pendingExportName;submitIo(()->BackupFiles.prune(backupDirectory(),retained,System.currentTimeMillis()));
        FrameLayout root=new FrameLayout(this);root.setBackgroundColor(Color.rgb(244,247,252));
        web=new WebView(this);web.setBackgroundColor(Color.rgb(244,247,252));root.addView(web,new FrameLayout.LayoutParams(-1,-1));setContentView(root);
        configureSystemBars(this,root);
        WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);settings.setAllowFileAccessFromFileURLs(false);settings.setAllowUniversalAccessFromFileURLs(false);settings.setBlockNetworkLoads(true);settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);settings.setJavaScriptCanOpenWindowsAutomatically(false);settings.setSupportMultipleWindows(false);
        WebView.setWebContentsDebuggingEnabled(false);web.addJavascriptInterface(new Bridge(),"AndroidStore");
        web.setWebViewClient(new WebViewClient(){
            @Override public void onPageFinished(WebView view,String url){pageReady=true;if(deferredCameraResult!=null){JSONObject value=deferredCameraResult;deferredCameraResult=null;sendJson("onCameraMealResult",value);}}
            @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request){
                Uri u=request.getUrl();String path=u.getPath();
                if(!"https".equals(u.getScheme())||!"appassets.androidplatform.net".equals(u.getHost())||path==null)return blocked();
                if(path.equals("/"))path="/index.html";
                if(!path.matches("/(index\\.html|home-hub\\.js|iterations\\.js|app\\.js|training\\.js|food-diary\\.js|progress\\.js|tracker-core\\.js|profile\\.js|habits\\.js|saved-foods\\.js|startup\\.js|activities\\.js|activity-estimator\\.js|health-core\\.js|health-sync\\.js|day-context\\.js|connections\\.js|food-reference\\.js|food-supplements\\.js|meal-estimator\\.js|app\\.css|theme\\.css|icon-192\\.png|icon-512\\.png)"))return blocked();
                try{String type=path.endsWith(".js")?"text/javascript":path.endsWith(".css")?"text/css":path.endsWith(".png")?"image/png":"text/html";
                    HashMap<String,String> headers=new HashMap<>();headers.put("Content-Security-Policy","default-src 'none'; script-src 'self'; style-src 'self'; img-src 'self'; connect-src 'none'; frame-src 'none'; object-src 'none'; base-uri 'none'; form-action 'none'");headers.put("X-Content-Type-Options","nosniff");
                    return new WebResourceResponse(type,"UTF-8",200,"OK",headers,getAssets().open(path.substring(1)));
                }catch(Exception e){return blocked();}
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){Uri u=request.getUrl();if("https".equals(u.getScheme())&&"appassets.androidplatform.net".equals(u.getHost())&&"/".equals(u.getPath()))return false;if("https".equals(u.getScheme())){try{startActivity(new Intent(Intent.ACTION_VIEW,u));}catch(Exception e){Toast.makeText(MainActivity.this,"No browser available.",Toast.LENGTH_SHORT).show();}}return true;}
        });
        web.setWebChromeClient(new WebChromeClient(){
            @Override public boolean onJsAlert(WebView v,String url,String message,JsResult result){showJsDialog(message,result,false);return true;}
            @Override public boolean onJsConfirm(WebView v,String url,String message,JsResult result){showJsDialog(message,result,true);return true;}
        });
        if(Build.VERSION.SDK_INT>=33)Api33Navigation.register(this);
        web.loadUrl(ORIGIN+"/");
    }
    /** Keep newer framework symbols out of the API26-loaded shell's method bodies. */
    static void configureSystemBars(Activity activity,View root){
        if(Build.VERSION.SDK_INT>=30)Api30SystemBars.configure(activity,root);
        else configureLegacySystemBars(activity);
    }
    @SuppressWarnings("deprecation")
    private static void configureLegacySystemBars(Activity activity){
        activity.getWindow().setStatusBarColor(Color.rgb(244,247,252));
        activity.getWindow().setNavigationBarColor(Color.rgb(244,247,252));
        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
    }
    @android.annotation.TargetApi(30)
    private static final class Api30SystemBars {
        @SuppressWarnings("deprecation")
        static void configure(Activity activity,View root){
            // API35 enforces edge-to-edge and ignores the deprecated bar colors.
            if(Build.VERSION.SDK_INT<35){
                activity.getWindow().setStatusBarColor(Color.rgb(244,247,252));
                activity.getWindow().setNavigationBarColor(Color.rgb(244,247,252));
                activity.getWindow().setDecorFitsSystemWindows(false);
            }
            android.view.WindowInsetsController controller=activity.getWindow().getInsetsController();
            if(controller!=null){int light=android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;controller.setSystemBarsAppearance(light,light);}
            root.setOnApplyWindowInsetsListener((view,insets)->{
                int handled=WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout()|WindowInsets.Type.ime();
                android.graphics.Insets safe=insets.getInsets(handled);
                view.setPadding(safe.left,safe.top,safe.right,safe.bottom);
                return new WindowInsets.Builder(insets).setInsets(handled,android.graphics.Insets.NONE)
                    .setInsetsIgnoringVisibility(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout(),android.graphics.Insets.NONE)
                    .setDisplayCutout(null).build();
            });
            root.requestApplyInsets();
        }
    }
    @android.annotation.TargetApi(33)
    private static final class Api33Navigation {
        static void register(MainActivity activity){activity.getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,activity::handleBack);}
    }
    private WebResourceResponse blocked(){return new WebResourceResponse("text/plain","UTF-8",403,"Forbidden",null,new ByteArrayInputStream(new byte[0]));}
    private boolean alive(){return !destroyed&&!isFinishing()&&!isDestroyed();}
    private File backupDirectory(){return new File(getFilesDir(),"pending-backups");}
    private boolean submitIo(Runnable work){if(destroyed)return false;try{io.execute(work);return true;}catch(java.util.concurrent.RejectedExecutionException e){return false;}}
    private boolean showDialog(AlertDialog dialog,Runnable dismissed){
        if(!alive()){if(dismissed!=null)dismissed.run();return false;}
        nativeDialogs.add(dialog);dialog.setOnDismissListener(d->{nativeDialogs.remove(dialog);if(dismissed!=null)dismissed.run();});
        try{dialog.show();return true;}catch(RuntimeException e){nativeDialogs.remove(dialog);if(dismissed!=null)dismissed.run();return false;}
    }
    private void showJsDialog(String message,JsResult result,boolean confirm){
        java.util.concurrent.atomic.AtomicBoolean answered=new java.util.concurrent.atomic.AtomicBoolean(false);
        AlertDialog.Builder builder=new AlertDialog.Builder(this).setTitle("Form & Strength").setMessage(message)
            .setPositiveButton(confirm?"Continue":"OK",(d,w)->{if(answered.compareAndSet(false,true))result.confirm();});
        if(confirm)builder.setNegativeButton("Cancel",(d,w)->{if(answered.compareAndSet(false,true))result.cancel();});
        showDialog(builder.create(),()->{if(answered.compareAndSet(false,true))result.cancel();});
    }
    private void handleBack(){
        if(!alive()||backPending)return;if(web==null){finish();return;}backPending=true;
        try{web.evaluateJavascript("(function(){try{if(typeof window.handleNativeBack==='function'){return window.handleNativeBack()!==false;}}catch(e){}return false;})()",result->{backPending=false;if(alive()&&!"true".equals(result))finish();});}
        catch(RuntimeException e){backPending=false;if(alive())finish();}
    }
    @Override public void onBackPressed(){handleBack();}
    private boolean deliverJs(String script){
        if(!alive()||web==null)return false;
        try{web.evaluateJavascript(script,null);return true;}catch(RuntimeException e){return false;}
    }
    private void event(String message,boolean reload){runOnUiThread(()->{if(!alive())return;try{JSONObject d=new JSONObject().put("message",message).put("reload",reload);if(!deliverJs("window.onNativeStatus && window.onNativeStatus("+d.toString()+")"))Toast.makeText(this,message,Toast.LENGTH_LONG).show();}catch(Exception e){if(alive())Toast.makeText(this,message,Toast.LENGTH_LONG).show();}});}
    private void clearPendingExport(String name){BackupFiles.delete(backupDirectory(),name);if(name!=null&&name.equals(pendingExportName)){pendingExportName=null;pendingExportPicker=false;}backupBusy.set(false);}
    private void launchExportPicker(String name){
        if(!alive())return;
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("application/json").putExtra(Intent.EXTRA_TITLE,"form-strength-backup-"+new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new java.util.Date())+".json");
        try{startActivityForResult(i,EXPORT);pendingExportPicker=true;}catch(Exception e){submitIo(()->clearPendingExport(name));event("No file picker available.",false);}
    }
    private void cameraReply(String request,String error){try{cameraResult(new JSONObject().put("requestId",request).put("error",error));}catch(Exception ignored){}}
    private void cameraResult(JSONObject value){runOnUiThread(()->{if(!alive())return;if(pageReady)sendJson("onCameraMealResult",value);else deferredCameraResult=value;});}
    private void releaseCamera(String name){try{if(MealPhotoProvider.validName(name))revokeUriPermission(MealPhotoProvider.uri(name),Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);}catch(RuntimeException ignored){}MealPhotoProvider.remove(this,name);if(name!=null&&name.equals(cameraName)){cameraName=null;cameraRequest=null;cameraPicker=false;}}
    private void launchCamera(String name,String request){
        if(!alive()){releaseCamera(name);aiBusy.set(false);return;}
        try{Uri uri=MealPhotoProvider.uri(name);Intent capture=new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).putExtra(android.provider.MediaStore.EXTRA_OUTPUT,uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);capture.setClipData(android.content.ClipData.newRawUri("Meal photo",uri));startActivityForResult(capture,CAMERA_MEAL);cameraPicker=true;}
        catch(RuntimeException e){releaseCamera(name);aiBusy.set(false);cameraReply(request,"No camera app is available. Type your meal instead.");}
    }
    private void confirmPhoto(byte[] jpeg,String request){
        if(!alive()){aiBusy.set(false);return;}java.util.concurrent.atomic.AtomicBoolean sent=new java.util.concurrent.atomic.AtomicBoolean(false);
        try{AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Send this meal photo to Gemini?").setMessage("A resized photo, with location metadata removed, will be sent to Google's Gemini API using your key. Check that no faces or private information are visible. Your diary and Health Connect records are not sent. API charges and quotas depend on your project. Estimates need your portion review; nothing is logged automatically.").setPositiveButton("Send photo",(d,w)->{sent.set(true);if(!submitIo(()->{try{if(destroyed)return;JSONObject result=gemini.estimateImage(jpeg);result.put("requestId",request);cameraResult(result);}catch(Exception e){cameraReply(request,e instanceof IllegalArgumentException?e.getMessage():"Could not estimate this photo. Check your connection or type the meal.");}finally{java.util.Arrays.fill(jpeg,(byte)0);aiBusy.set(false);}})){java.util.Arrays.fill(jpeg,(byte)0);aiBusy.set(false);cameraReply(request,"Photo request interrupted. Retake the photo.");}}).setNegativeButton("Cancel",null).create();
        showDialog(dialog,()->{if(!sent.get()){java.util.Arrays.fill(jpeg,(byte)0);aiBusy.set(false);cameraReply(request,"Photo cancelled. Nothing was sent or logged.");}});
        }catch(RuntimeException e){java.util.Arrays.fill(jpeg,(byte)0);aiBusy.set(false);cameraReply(request,"Could not open photo consent. Nothing was sent.");}
    }
    public final class Bridge {
        @JavascriptInterface public void captureMeal(String request){
            if(request==null||!request.matches("[A-Za-z0-9-]{1,80}")||destroyed)return;
            if(!gemini.configured()){cameraReply(request,"Set up your Gemini key in You settings before estimating a photo.");return;}
            if(!aiBusy.compareAndSet(false,true)){cameraReply(request,"An AI or camera request is already in progress.");return;}
            final String name="meal-"+UUID.randomUUID().toString()+".jpg";cameraName=name;cameraRequest=request;
            if(!submitIo(()->{try{MealPhotoProvider.prepare(MainActivity.this,name);runOnUiThread(()->launchCamera(name,request));}catch(Exception e){releaseCamera(name);aiBusy.set(false);cameraReply(request,"Could not prepare the camera. Check free storage and retry.");}})){aiBusy.set(false);releaseCamera(name);cameraReply(request,"Camera interrupted. Reopen the app and retry.");}
        }
        @JavascriptInterface public String reminderStatus(){return ReminderScheduler.status(MainActivity.this).toString();}
        @JavascriptInterface public String consumeLaunchDestination(){synchronized(MainActivity.this){Intent intent=getIntent();if(intent!=null&&intent.getBooleanExtra("openYou",false)){intent.removeExtra("openYou");return "you";}return "";}}
        @JavascriptInterface public void configureReminders(boolean water,boolean walk,boolean windup){if(!submitIo(()->{try{ReminderScheduler.configure(MainActivity.this,water,walk,windup);runOnUiThread(()->{if(!alive())return;if(Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=android.content.pm.PackageManager.PERMISSION_GRANTED&&(water||walk||windup))requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},305);sendJson("onReminderSettings",ReminderScheduler.status(MainActivity.this));});}catch(RuntimeException e){event("Could not update reminders. Check notification settings.",false);}}))event("Reminder settings interrupted. Reopen and retry.",false);}

        @JavascriptInterface public String request(String path,String method,String body){synchronized(store){try{if(destroyed)throw new IllegalStateException("Activity closed.");if(path.equals("/api/health-snapshots")){if(method.equals("DELETE")){store.clearHealthCache();return new JSONObject().put("ok",true).toString();}LocalStore.require(method.equals("GET"),"Read-only health cache.");JSONObject permissions=new HealthConnection(MainActivity.this).status();return new JSONObject().put("entries",store.healthSnapshots(permissions)).toString();}return store.request(path,method,body).toString();}catch(Exception e){try{return new JSONObject().put("error",e instanceof IllegalArgumentException?e.getMessage():"Could not read or save this entry. Please check the fields and retry.").toString();}catch(Exception ignored){return "{\"error\":\"Storage error\"}";}}}}
        @JavascriptInterface public void exportBackup(String json){
            if(json==null||json.length()>BackupFiles.MAX_BYTES){event("Backup is too large to export.",false);return;}
            if(!backupBusy.compareAndSet(false,true)){event("A backup operation is already in progress.",false);return;}
            final String name="export-"+UUID.randomUUID().toString()+".json";pendingExportName=name;pendingExportPicker=false;event("Preparing backup…",false);
            if(!submitIo(()->{try{BackupFiles.create(backupDirectory(),name,json);runOnUiThread(()->launchExportPicker(name));}
                catch(Exception e){clearPendingExport(name);event(e instanceof IllegalArgumentException?e.getMessage():"Could not prepare backup. Please retry.",false);}})){pendingExportName=null;backupBusy.set(false);event("Backup is unavailable while the app closes. Reopen and retry.",false);}
        }
        @JavascriptInterface public void importBackup(){
            if(!backupBusy.compareAndSet(false,true)){event("A backup operation is already in progress.",false);return;}
            runOnUiThread(()->{if(!alive()){backupBusy.set(false);return;}Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*").putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"application/json","text/plain","application/octet-stream"});try{startActivityForResult(i,IMPORT);pendingImportPicker=true;}catch(Exception e){pendingImportPicker=false;backupBusy.set(false);event("No file picker available.",false);}});
        }
        @JavascriptInterface public String connectionStatus(){try{JSONObject out=new JSONObject().put("aiConfigured",gemini.configured()).put("model",gemini.model());JSONObject health=new HealthConnection(MainActivity.this).status();return out.put("health",health).toString();}catch(Exception e){return "{\"aiConfigured\":false,\"health\":{\"supported\":false}}";}}
        @JavascriptInterface public void configureGemini(){runOnUiThread(()->{if(alive())try{gemini.configure(()->sendJson("onConnectionChanged",new JSONObject()));}catch(RuntimeException e){event("Could not open Gemini settings. Reopen the app and retry.",false);}});}
        @JavascriptInterface public void connectHealth(){runOnUiThread(()->{if(!alive())return;if(Build.VERSION.SDK_INT>=34){try{new HealthConnection(MainActivity.this).connect();}catch(Exception e){event("Could not open Health Connect permissions. Check Android Settings.",false);}}else event("Device sync requires Android 14 or newer in this build.",false);});}
        @JavascriptInterface public void healthSettings(){runOnUiThread(()->{if(!alive())return;if(Build.VERSION.SDK_INT>=34){try{new HealthConnection(MainActivity.this).settings();}catch(Exception e){event("Open Android Settings and search for Health Connect.",false);}}else event("Device sync requires Android 14 or newer in this build.",false);});}
        @JavascriptInterface public void readHealth(String day,String id){
            if(id==null||!id.matches("[A-Za-z0-9-]{1,80}"))return;
            runOnUiThread(()->{if(!alive())return;if(Build.VERSION.SDK_INT<34){healthReply(id,"Device sync requires Android 14 or newer.");return;}
                try{new HealthConnection(MainActivity.this).read(day,value->{if(destroyed)return;if(!submitIo(()->{
                    if(destroyed)return;try{JSONObject saved; synchronized(store){if(destroyed)return;saved=value.has("metrics")?store.cacheHealth(value):value;}saved.put("requestId",id);sendJson("onHealthResult",saved);}
                    catch(Exception error){healthReply(id,"Readings could not be saved locally. Check available storage and retry.");}
                }))healthReply(id,"Sync interrupted. Reopen the app and retry.");});}
                catch(RuntimeException error){healthReply(id,"Could not read Health Connect. Check access and retry.");}
            });
        }
        @JavascriptInterface public void estimateMeal(String text,String id){
            if(id==null||!id.matches("[A-Za-z0-9-]{1,80}"))return;
            if(text==null||text.trim().isEmpty()||text.length()>120){aiReply(id,"Enter a meal up to 120 characters.");return;}
            if(destroyed)return;if(!aiBusy.compareAndSet(false,true)){aiReply(id,"An AI request is already in progress.");return;}
            runOnUiThread(()->{
                if(!alive()){aiBusy.set(false);return;}
                java.util.concurrent.atomic.AtomicBoolean sent=new java.util.concurrent.atomic.AtomicBoolean(false);
                try{
                    AlertDialog dialog=new AlertDialog.Builder(MainActivity.this).setTitle("Send meal to Google Gemini?")
                        .setMessage("Only this meal text will be sent to Google's Gemini API using your project. Your diary, weight and Health Connect data are not sent. API charges or quotas depend on your project.\n\n"+text)
                        .setPositiveButton("Send meal",(d,w)->{
                            sent.set(true);
                            if(!submitIo(()->{try{if(destroyed)return;sendJson("onAiStarted",new JSONObject().put("requestId",id));JSONObject value=gemini.estimate(text);value.put("requestId",id);sendJson("onAiResult",value);}
                                catch(Exception e){aiReply(id,e instanceof IllegalArgumentException?e.getMessage():"Could not get an AI estimate. Check your connection and retry.");}
                                finally{aiBusy.set(false);}})){aiBusy.set(false);aiReply(id,"AI request interrupted. Reopen the app and retry.");}
                        }).setNegativeButton("Cancel",null).create();
                    showDialog(dialog,()->{if(!sent.get()){aiBusy.set(false);aiReply(id,"AI request cancelled. Nothing was sent.");}});
                }catch(RuntimeException e){aiBusy.set(false);aiReply(id,"Could not open AI confirmation. Nothing was sent; retry.");}
            });
        }
        @JavascriptInterface public void exit(){runOnUiThread(()->{if(alive())finish();});}
    }
    @Override protected void onSaveInstanceState(Bundle state){
        String name=pendingExportName;if(pendingExportPicker&&BackupFiles.validName(name))state.putString("pendingExportName",name);state.putBoolean("pendingImportPicker",pendingImportPicker);
        if(cameraPicker&&MealPhotoProvider.validName(cameraName)){state.putString("cameraName",cameraName);state.putString("cameraRequest",cameraRequest);}
        super.onSaveInstanceState(state);
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==CAMERA_MEAL){final String name=cameraName,request=cameraRequest;cameraPicker=false;if(restoredCapture){restoredCapture=false;releaseCamera(name);aiBusy.set(false);cameraReply(request,"The app restarted while the camera was open. Nothing was sent or logged. Please reopen the meal form and retake the photo.");return;}if(resultCode!=RESULT_OK||!MealPhotoProvider.validName(name)){releaseCamera(name);aiBusy.set(false);cameraReply(request,"Camera cancelled. Nothing was sent or logged.");return;}
            try{revokeUriPermission(MealPhotoProvider.uri(name),Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);}catch(RuntimeException ignored){}
            if(!submitIo(()->{try{byte[] jpeg=MealPhotoProvider.image(this,name);runOnUiThread(()->confirmPhoto(jpeg,request));}catch(Exception e){aiBusy.set(false);cameraReply(request,e.getMessage()==null?"Could not read the photo. Retake it.":e.getMessage());}finally{releaseCamera(name);}})){releaseCamera(name);aiBusy.set(false);cameraReply(request,"Photo interrupted. Retake it.");}return;}
        if(requestCode!=EXPORT&&requestCode!=IMPORT)return;if(requestCode==IMPORT)pendingImportPicker=false;
        if(resultCode!=RESULT_OK||data==null||data.getData()==null){
            if(requestCode==EXPORT){String name=pendingExportName;if(!submitIo(()->clearPendingExport(name)))backupBusy.set(false);}else backupBusy.set(false);
            event("File selection cancelled. Your records are unchanged.",false);return;
        }
        // Imports can return to a newly recreated Activity, whose busy flag starts clear.
        backupBusy.set(true);
        final Uri uri=data.getData();final String name=pendingExportName;
        if(requestCode==EXPORT){
            if(!submitIo(()->{try{if(!BackupFiles.validName(name))throw new IllegalStateException("No pending backup.");try(OutputStream out=getContentResolver().openOutputStream(uri,"wt")){if(out==null)throw new IllegalStateException();BackupFiles.copyTo(backupDirectory(),name,out);}event("Backup saved. Keep this file somewhere safe.",false);}
                catch(Exception e){event("Could not save backup. Reopen export and try another location.",false);}
                finally{clearPendingExport(name);}}))backupBusy.set(false);
            return;
        }
        event("Checking backup…",false);
        if(!submitIo(()->{
            try{String raw;try(InputStream in=getContentResolver().openInputStream(uri)){raw=BackupFiles.read(in);}
                final JSONObject backup=LocalStore.validateBackup(raw);
                runOnUiThread(()->confirmImport(backup));
            }catch(Exception e){backupBusy.set(false);event(e instanceof IllegalArgumentException?e.getMessage():"This is not a valid Form & Strength backup.",false);}
        }))backupBusy.set(false);
    }
    private void confirmImport(JSONObject backup){
        if(!alive()){backupBusy.set(false);return;}
        java.util.concurrent.atomic.AtomicBoolean accepted=new java.util.concurrent.atomic.AtomicBoolean(false);
        try{int logs=backup.getJSONArray("logs").length(),foods=backup.getJSONArray("foodEntries").length();
            AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Import backup?").setMessage(logs+" check-ins and "+foods+" food entries found. New records and saved foods will be added. Existing records and a completed profile will be kept.")
                .setPositiveButton("Import",(d,w)->{accepted.set(true);if(!submitIo(()->{
                    try{String message; synchronized(store){if(destroyed)return;message=store.importBackup(backup);}event(message,true);}
                    catch(Exception e){event("Import failed. No records were changed.",false);}finally{backupBusy.set(false);}
                }))backupBusy.set(false);}).setNegativeButton("Cancel",null).create();
            showDialog(dialog,()->{if(!accepted.get())backupBusy.set(false);});
        }catch(Exception e){backupBusy.set(false);event("Could not show the import confirmation. Retry the import.",false);}
    }
    private void sendJson(String function,JSONObject value){runOnUiThread(()->deliverJs("window."+function+" && window."+function+"("+value.toString()+")"));}
    private void aiReply(String id,String error){try{sendJson("onAiResult",new JSONObject().put("requestId",id).put("error",error));}catch(Exception ignored){}}
    private void healthReply(String id,String error){try{sendJson("onHealthResult",new JSONObject().put("requestId",id).put("error",error));}catch(Exception ignored){}}
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] results){super.onRequestPermissionsResult(requestCode,permissions,results);if(requestCode==303)sendJson("onConnectionChanged",new JSONObject());if(requestCode==305)sendJson("onReminderSettings",ReminderScheduler.status(this));}
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);synchronized(this){setIntent(intent);}deliverJs("window.onNativeResume && window.onNativeResume()");deliverJs("window.onIterationDestination && window.onIterationDestination()");}
    @Override protected void onResume(){super.onResume();deliverJs("window.onNativeResume && window.onNativeResume()");}
    @Override protected void onDestroy(){
        destroyed=true;aiBusy.set(false);backPending=false;gemini.cancel();
        for(AlertDialog dialog:new ArrayList<>(nativeDialogs))try{dialog.dismiss();}catch(RuntimeException ignored){}nativeDialogs.clear();
        if(web!=null){web.removeJavascriptInterface("AndroidStore");web.destroy();web=null;}
        // Drain already submitted work, then close under the same monitor used by bridge/cache access.
        // New health callbacks are rejected by destroyed before they can reopen SQLite.
        Runnable closeStore=()->{synchronized(store){store.close();}};
        try{io.execute(closeStore);}catch(java.util.concurrent.RejectedExecutionException e){new Thread(closeStore,"form-strength-store-close").start();}
        finally{io.shutdown();}
        super.onDestroy();
    }
}
