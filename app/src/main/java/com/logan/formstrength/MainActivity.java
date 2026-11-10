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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;

/** Android shell for bundled offline UI, native SQLite and system document backup. */
public final class MainActivity extends Activity {
    private static final String ORIGIN="https://appassets.androidplatform.net";
    private static final int EXPORT=201,IMPORT=202;
    private WebView web;private LocalStore store;private String exportData;
    @Override public void onCreate(Bundle state){
        super.onCreate(state);store=new LocalStore(this);
        FrameLayout root=new FrameLayout(this);root.setBackgroundColor(Color.rgb(245,246,240));
        web=new WebView(this);web.setBackgroundColor(Color.rgb(245,246,240));root.addView(web,new FrameLayout.LayoutParams(-1,-1));setContentView(root);
        getWindow().setStatusBarColor(Color.rgb(245,246,240));getWindow().setNavigationBarColor(Color.rgb(245,246,240));getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        if(Build.VERSION.SDK_INT>=30){getWindow().setDecorFitsSystemWindows(false);root.setOnApplyWindowInsetsListener((view,insets)->{android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()),ime=insets.getInsets(WindowInsets.Type.ime());view.setPadding(bars.left,bars.top,bars.right,Math.max(bars.bottom,ime.bottom));return insets;});}
        WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);settings.setAllowFileAccessFromFileURLs(false);settings.setAllowUniversalAccessFromFileURLs(false);settings.setBlockNetworkLoads(true);settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);settings.setJavaScriptCanOpenWindowsAutomatically(false);settings.setSupportMultipleWindows(false);
        WebView.setWebContentsDebuggingEnabled(false);web.addJavascriptInterface(new Bridge(),"AndroidStore");
        web.setWebViewClient(new WebViewClient(){
            @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request){
                Uri u=request.getUrl();String path=u.getPath();
                if(!"https".equals(u.getScheme())||!"appassets.androidplatform.net".equals(u.getHost())||path==null)return blocked();
                if(path.equals("/"))path="/index.html";
                if(!path.matches("/(index\\.html|app\\.js|app\\.css|icon-192\\.png|icon-512\\.png)"))return blocked();
                try{String type=path.endsWith(".js")?"text/javascript":path.endsWith(".css")?"text/css":path.endsWith(".png")?"image/png":"text/html";
                    HashMap<String,String> headers=new HashMap<>();headers.put("Content-Security-Policy","default-src 'none'; script-src 'self'; style-src 'self'; img-src 'self'; connect-src 'none'; frame-src 'none'; object-src 'none'; base-uri 'none'; form-action 'none'");headers.put("X-Content-Type-Options","nosniff");
                    return new WebResourceResponse(type,"UTF-8",200,"OK",headers,getAssets().open(path.substring(1)));
                }catch(Exception e){return blocked();}
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){Uri u=request.getUrl();if("https".equals(u.getScheme())&&"appassets.androidplatform.net".equals(u.getHost())&&"/".equals(u.getPath()))return false;if("https".equals(u.getScheme())){try{startActivity(new Intent(Intent.ACTION_VIEW,u));}catch(Exception e){Toast.makeText(MainActivity.this,"No browser available.",Toast.LENGTH_SHORT).show();}}return true;}
        });
        web.setWebChromeClient(new WebChromeClient(){
            @Override public boolean onJsAlert(WebView v,String url,String message,JsResult result){new AlertDialog.Builder(MainActivity.this).setTitle("Form & Strength").setMessage(message).setPositiveButton("OK",(d,w)->result.confirm()).setOnCancelListener(d->result.cancel()).show();return true;}
            @Override public boolean onJsConfirm(WebView v,String url,String message,JsResult result){new AlertDialog.Builder(MainActivity.this).setTitle("Form & Strength").setMessage(message).setPositiveButton("Continue",(d,w)->result.confirm()).setNegativeButton("Cancel",(d,w)->result.cancel()).setOnCancelListener(d->result.cancel()).show();return true;}
        });
        if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,()->handleBack());
        web.loadUrl(ORIGIN+"/");
    }
    private WebResourceResponse blocked(){return new WebResourceResponse("text/plain","UTF-8",403,"Forbidden",null,new ByteArrayInputStream(new byte[0]));}
    private void handleBack(){web.evaluateJavascript("window.handleNativeBack && window.handleNativeBack()",null);}
    @Override public void onBackPressed(){handleBack();}
    private void event(String message,boolean reload){runOnUiThread(()->{try{JSONObject d=new JSONObject().put("message",message).put("reload",reload);web.evaluateJavascript("window.onNativeStatus && window.onNativeStatus("+d.toString()+")",null);}catch(Exception e){Toast.makeText(this,message,Toast.LENGTH_LONG).show();}});}
    public final class Bridge {
        @JavascriptInterface public String request(String path,String method,String body){try{return store.request(path,method,body).toString();}catch(Exception e){try{return new JSONObject().put("error",e instanceof IllegalArgumentException?e.getMessage():"Could not read or save this entry. Please check the fields and retry.").toString();}catch(Exception ignored){return "{\"error\":\"Storage error\"}";}}}
        @JavascriptInterface public void exportBackup(String json){if(json==null||json.length()>5*1024*1024){event("Backup is too large to export.",false);return;}runOnUiThread(()->{exportData=json;Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("application/json").putExtra(Intent.EXTRA_TITLE,"form-strength-backup-"+new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new java.util.Date())+".json");try{startActivityForResult(i,EXPORT);}catch(Exception e){event("No file picker available.",false);}});}
        @JavascriptInterface public void importBackup(){runOnUiThread(()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*").putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"application/json","text/plain","application/octet-stream"});try{startActivityForResult(i,IMPORT);}catch(Exception e){event("No file picker available.",false);}});}
        @JavascriptInterface public void exit(){runOnUiThread(()->finish());}
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(requestCode!=EXPORT&&requestCode!=IMPORT)return;
        if(resultCode!=RESULT_OK||data==null||data.getData()==null){if(requestCode==EXPORT)exportData=null;event("File selection cancelled. Your records are unchanged.",false);return;}
        Uri uri=data.getData();if(requestCode==EXPORT){try{if(exportData==null)throw new IllegalStateException("Reopen export after an app restart.");try(OutputStream out=getContentResolver().openOutputStream(uri,"wt")){if(out==null)throw new IllegalStateException();out.write(exportData.getBytes(StandardCharsets.UTF_8));}event("Backup saved. Keep this file somewhere safe.",false);}catch(Exception e){event("Could not save backup. Please try another location.",false);}finally{exportData=null;}return;}
        try{String raw;try(InputStream in=getContentResolver().openInputStream(uri);ByteArrayOutputStream out=new ByteArrayOutputStream()){if(in==null)throw new IllegalArgumentException("Cannot open this file.");byte[] bytes=new byte[8192];int n;while((n=in.read(bytes))!=-1){if(out.size()+n>5*1024*1024)throw new IllegalArgumentException("Maximum backup size is 5 MB.");out.write(bytes,0,n);}raw=out.toString("UTF-8");}
            final JSONObject backup=LocalStore.validateBackup(raw);int logs=backup.getJSONArray("logs").length(),foods=backup.getJSONArray("foodEntries").length();
            new AlertDialog.Builder(this).setTitle("Import backup?").setMessage(logs+" check-ins and "+foods+" food entries found. New records will be added. Existing dates and food IDs will be kept unchanged.").setPositiveButton("Import",(d,w)->{try{event(store.importBackup(backup),true);}catch(Exception e){event("Import failed. No records were changed.",false);}}).setNegativeButton("Cancel",null).show();
        }catch(Exception e){event(e instanceof IllegalArgumentException?e.getMessage():"This is not a valid Form & Strength backup.",false);}
    }
    @Override protected void onDestroy(){if(web!=null){web.removeJavascriptInterface("AndroidStore");web.destroy();}if(store!=null)store.close();super.onDestroy();}
}
