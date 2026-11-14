package com.luminastudios.callguard;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

public class Hub extends Activity {
    private LinearLayout page;
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private GradientDrawable glass(int top,int bottom,int radius) {
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{top,bottom});
        d.setCornerRadius(dp(radius)); d.setStroke(dp(1),0x486f9ac7); return d;
    }
    private TextView type(String value,int size,int color,boolean bold) {
        TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);
        t.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));return t;
    }
    private void card(String title,String description,String target,boolean video) {
        LinearLayout item=new LinearLayout(this);item.setOrientation(1);
        item.setBackground(glass(0xff193858,0xff102740,20));item.setPadding(dp(20),dp(15),dp(20),dp(16));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(10);page.addView(item,lp);
        item.addView(type(title,20,Color.WHITE,true));item.addView(type(description,15,0xffb7d2eb,false));
        item.setFocusable(true);item.setOnClickListener(v->{Intent i=new Intent().setClassName(this,target);
            if(video)i.putExtra("youtube",true);startActivity(i);});
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);getWindow().setStatusBarColor(0xff09192c);getWindow().setNavigationBarColor(0xff09192c);
        ScrollView scroll=new ScrollView(this);scroll.setBackground(glass(0xff163652,0xff071427,0));
        scroll.setOnApplyWindowInsetsListener((v,i)->{v.setPadding(i.getSystemWindowInsetLeft(),i.getSystemWindowInsetTop(),i.getSystemWindowInsetRight(),i.getSystemWindowInsetBottom());return i;});
        page=new LinearLayout(this);page.setOrientation(1);page.setPadding(dp(20),dp(26),dp(20),dp(22));scroll.addView(page);
        TextView title=type("Lumina Focus",36,Color.WHITE,true);title.setPadding(0,0,0,dp(22));page.addView(title);
        Button share=new Button(this);
        share.setText("Share app");share.setAllCaps(false);share.setTextColor(Color.WHITE);
        share.setBackground(glass(0xff295c80,0xff173756,16));
        LinearLayout.LayoutParams shareLayout=new LinearLayout.LayoutParams(-1,dp(48));
        shareLayout.bottomMargin=dp(16);page.addView(share,shareLayout);
        share.setOnClickListener(v->AppShare.start(this,share));
        card("Focus & protection","Focus sessions, schedules and app controls","com.lumina.focus.Home",false);
        card("Privacy & VPN","Network protection and Tor settings","com.lumina.focus.Privacy",false);
        card("Private browser","Browse with the original Lumina media tools","com.lumina.focus.Reader",false);
        card("YouTube","Watch with automatic ad skipping and muting","com.luminastudios.callguard.FocusBrowser",true);
        card("Private chat","Encrypted, disappearing texts and photos","com.luminastudios.callguard.PrivateChat",false);
        card("Call Guard","Spam protection, blocked numbers and call settings","com.lumina.focus.CallGuard",false);
        card("Phone & messages","Call, compose a text or check a suspicious message","com.lumina.focus.Communicate",false);
        setContentView(scroll);
    }
}
