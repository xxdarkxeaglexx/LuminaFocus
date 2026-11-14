package com.luminastudios.callguard;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.*;
import java.io.IOException;

public class PrivateChat extends Activity {
    private static final String PAGE = "https://appassets.androidplatform.net/chat.html";
    private WebView chat;
    private ValueCallback<Uri[]> picker;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        chat = new WebView(this);
        chat.setOnApplyWindowInsetsListener((v, i) -> {
            v.setPadding(i.getSystemWindowInsetLeft(), i.getSystemWindowInsetTop(),
                    i.getSystemWindowInsetRight(), i.getSystemWindowInsetBottom());
            return i;
        });
        setContentView(chat);
        WebSettings settings = chat.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        chat.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                return true;
            }
            @Override public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
                if (PAGE.equals(r.getUrl().toString())) {
                    try {
                        return new WebResourceResponse("text/html", "UTF-8", getAssets().open("chat.html"));
                    } catch (IOException ignored) { }
                }
                return new WebResourceResponse("text/plain", "UTF-8", null);
            }
        });
        chat.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> callback,
                                                       FileChooserParams params) {
                if (picker != null) picker.onReceiveValue(null);
                picker = callback;
                Intent pick = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*")
                        .addCategory(Intent.CATEGORY_OPENABLE);
                try { startActivityForResult(pick, 42); }
                catch (android.content.ActivityNotFoundException e) {
                    picker.onReceiveValue(null); picker = null;
                }
                return true;
            }
        });
        chat.loadUrl(PAGE);
    }
    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == 42 && picker != null) {
            Uri uri = result == RESULT_OK && data != null ? data.getData() : null;
            picker.onReceiveValue(uri != null && "content".equals(uri.getScheme()) ?
                    new Uri[]{uri} : null);
            picker = null;
        }
    }
    @Override protected void onDestroy() {
        if (picker != null) picker.onReceiveValue(null);
        chat.destroy();
        super.onDestroy();
    }
}
