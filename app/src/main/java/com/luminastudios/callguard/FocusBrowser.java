package com.luminastudios.callguard;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.*;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.*;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;

/** Lightweight in-app browser; domain filtering is best-effort, not a YouTube ad bypass. */
public class FocusBrowser extends Activity {
 private WebView browser;
 private EditText address;
 private final Set<String> blocked = new HashSet<>();
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  String[] hosts={"doubleclick.net","googlesyndication.com","googleadservices.com","adservice.google.com","adsrvr.org","taboola.com","outbrain.com","scorecardresearch.com","criteo.com","pubmatic.com","rubiconproject.com","openx.net","quantserve.com","zedo.com"};
  for(String h:hosts)blocked.add(h);
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(0xff10253b);
  LinearLayout toolbar=new LinearLayout(this);address=new EditText(this);address.setSingleLine(true);address.setTextColor(0xffffffff);address.setHintTextColor(0xffb4c5d6);address.setHint("Search or enter website");address.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_URI);toolbar.addView(address,new LinearLayout.LayoutParams(0,-2,1));
  Button go=new Button(this);go.setText("Go");toolbar.addView(go);root.addView(toolbar);
  TextView notice=new TextView(this);notice.setText("Focus protection ON · Some ads, including YouTube video ads, may remain");notice.setTextColor(0xffc3e9e3);notice.setPadding(12,4,12,8);root.addView(notice);
  browser=new WebView(this);root.addView(browser,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
  WebSettings settings=browser.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);settings.setSupportMultipleWindows(false);settings.setJavaScriptCanOpenWindowsAutomatically(false);settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
  browser.setWebChromeClient(new WebChromeClient(){@Override public boolean onCreateWindow(WebView view,boolean dialog,boolean userGesture,android.os.Message resultMsg){return false;}});
  browser.setWebViewClient(new WebViewClient(){
   @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request){
    android.net.Uri uri=request.getUrl();String host=uri.getHost();if(host!=null){host=host.toLowerCase(Locale.ROOT);String path=uri.getPath(); if (!request.isForMainFrame() && (host.equals("youtube.com") || host.endsWith(".youtube.com")) && path!=null && (path.startsWith("/pagead/") || path.startsWith("/api/stats/ads"))) return new WebResourceResponse("text/plain","UTF-8",new java.io.ByteArrayInputStream(new byte[0]));for(String domain:blocked)if(host.equals(domain)||host.endsWith("."+domain))return new WebResourceResponse("text/plain","UTF-8",new java.io.ByteArrayInputStream(new byte[0]));}return null;
   }
   @Override public void onPageStarted(WebView view,String url,Bitmap favicon){address.setText(url);}
   @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){String scheme=request.getUrl().getScheme();return !("https".equalsIgnoreCase(scheme)||"http".equalsIgnoreCase(scheme));}
  });
  go.setOnClickListener(v->navigate());address.setOnEditorActionListener((v,id,event)->{navigate();return true;});browser.loadUrl(getIntent().getBooleanExtra("youtube",false)?"https://m.youtube.com":"https://www.google.com");
 }
 private void navigate(){String s=address.getText().toString().trim();if(s.isEmpty())return;if(!s.contains("://")){if(s.contains(" ")||!s.contains("."))s="https://www.google.com/search?q="+android.net.Uri.encode(s);else s="https://"+s;}if(s.startsWith("https://")||s.startsWith("http://"))browser.loadUrl(s);}
 @Override public void onBackPressed(){if(browser!=null&&browser.canGoBack())browser.goBack();else super.onBackPressed();}
 @Override protected void onDestroy(){if(browser!=null){browser.destroy();browser=null;}super.onDestroy();}
}
