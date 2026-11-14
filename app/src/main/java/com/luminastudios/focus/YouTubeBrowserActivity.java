package com.luminastudios.focus;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.*;
import android.widget.*;
import java.io.ByteArrayInputStream;
import java.util.*;
public final class YouTubeBrowserActivity extends Activity {
 private WebView browser; private volatile boolean filtering=true; private TextView status;
 private static final Set<String> BLOCKED=new HashSet<>(Arrays.asList("doubleclick.net","googlesyndication.com","googleadservices.com","adservice.google.com","ads.youtube.com","analytics.google.com"));
 private static boolean blockedHost(String host){if(host==null)return false;host=host.toLowerCase(Locale.ROOT);for(String d:BLOCKED)if(host.equals(d)||host.endsWith("."+d))return true;return false;}
 @Override public void onCreate(Bundle state){super.onCreate(state);
 LinearLayout layout=new LinearLayout(this);layout.setOrientation(1);LinearLayout controls=new LinearLayout(this);
 Button home=new Button(this);home.setText("YouTube Home");home.setOnClickListener(v->browser.loadUrl("https://m.youtube.com/"));controls.addView(home,new LinearLayout.LayoutParams(0,-2,1));
 Button toggle=new Button(this);toggle.setText("Filtering ON");toggle.setOnClickListener(v->{filtering=!filtering;toggle.setText(filtering?"Filtering ON":"Filtering OFF");status.setText(filtering?"Known ad/tracker domains filtered":"Filtering paused");browser.reload();});controls.addView(toggle,new LinearLayout.LayoutParams(0,-2,1));
 layout.addView(controls);status=new TextView(this);status.setText("Known ad/tracker domains filtered; video ads may still appear");status.setPadding(16,8,16,8);layout.addView(status);
 browser=new WebView(this);layout.addView(browser,new LinearLayout.LayoutParams(-1,0,1));setContentView(layout);
 WebSettings settings=browser.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);settings.setMediaPlaybackRequiresUserGesture(true);settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);
 browser.setWebViewClient(new WebViewClient(){
 @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){String scheme=request.getUrl().getScheme();return !("https".equalsIgnoreCase(scheme)||"http".equalsIgnoreCase(scheme));}
 @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request){String scheme=request.getUrl().getScheme();if(filtering&&("https".equalsIgnoreCase(scheme)||"http".equalsIgnoreCase(scheme))&&blockedHost(request.getUrl().getHost()))return new WebResourceResponse("text/plain","UTF-8",403,"Blocked",Collections.emptyMap(),new ByteArrayInputStream(new byte[0]));return null;}
 });browser.loadUrl("https://m.youtube.com/");
 }
 @Override public void onBackPressed(){if(browser!=null&&browser.canGoBack())browser.goBack();else super.onBackPressed();}
 @Override protected void onDestroy(){if(browser!=null){browser.destroy();browser=null;}super.onDestroy();}
}