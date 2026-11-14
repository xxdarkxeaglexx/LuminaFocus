package com.luminastudios.focus;
import android.app.*;import android.content.*;import android.os.*;import android.graphics.Color;import android.view.*;import android.widget.*;
public final class MainActivity extends Activity {
 private TextView clock; private CountDownTimer timer;
 @Override public void onCreate(Bundle state){super.onCreate(state);
 LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(30,50,30,20);l.setBackgroundColor(Color.rgb(10,25,52));
 TextView title=new TextView(this);title.setText("LUMINA FOCUS");title.setTextColor(Color.WHITE);title.setTextSize(28);l.addView(title);
 TextView info=new TextView(this);info.setText("Privacy-first focus and music browser\nAd filtering is best-effort; no guaranteed YouTube ad removal.");info.setTextColor(Color.LTGRAY);info.setPadding(0,20,0,30);l.addView(info);
 Button youtube=new Button(this);youtube.setText("Open YouTube Music Browser");youtube.setOnClickListener(v->startActivity(new Intent(this,YouTubeBrowserActivity.class)));l.addView(youtube);
 clock=new TextView(this);clock.setText("Focus timer: ready");clock.setTextColor(Color.WHITE);clock.setTextSize(22);clock.setPadding(0,35,0,15);l.addView(clock);
 Button focus=new Button(this);focus.setText("Start 25-minute focus session");focus.setOnClickListener(v->{if(timer!=null)timer.cancel();timer=new CountDownTimer(25*60*1000,1000){public void onTick(long ms){clock.setText(String.format(java.util.Locale.US,"Focus: %02d:%02d",ms/60000,(ms/1000)%60));}public void onFinish(){clock.setText("Focus complete!");}}.start();});l.addView(focus);
 Button stop=new Button(this);stop.setText("Stop timer");stop.setOnClickListener(v->{if(timer!=null)timer.cancel();clock.setText("Focus timer: ready");});l.addView(stop);setContentView(l);
 }
 @Override protected void onDestroy(){if(timer!=null)timer.cancel();super.onDestroy();}
}