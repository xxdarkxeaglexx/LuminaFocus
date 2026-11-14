package com.luminastudios.callguard;
import android.app.*;
import android.app.role.RoleManager;
import android.content.*;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.text.DateFormat;
import java.util.*;
import org.json.*;

public class Home extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {
 final int ink=0xff172d2c, muted=0xff50625e, teal=0xff14574f, mint=0xffc7efda;
 String pendingRecording;
 LinearLayout page,group; SharedPreferences prefs; RoleManager roles;
 int dp(int n){return Math.round(getResources().getDisplayMetrics().density*n);}
 public void onCreate(Bundle b){super.onCreate(b);if(b!=null)pendingRecording=b.getString("pendingRecording");prefs=Guard.prefs(this);roles=getSystemService(RoleManager.class);getWindow().setStatusBarColor(teal);}
 protected void onResume(){super.onResume();prefs.registerOnSharedPreferenceChangeListener(this);draw();}
 protected void onPause(){prefs.unregisterOnSharedPreferenceChangeListener(this);super.onPause();}
 public void onSharedPreferenceChanged(SharedPreferences p,String k){if(k.equals("recent"))draw();}
 GradientDrawable bg(int color){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(18));return d;}
 void card(int color){group=new LinearLayout(this);group.setOrientation(1);group.setPadding(dp(18),dp(14),dp(18),dp(14));group.setBackground(bg(color));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(12);page.addView(group,lp);}
 TextView label(String s,int size,int color,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);if(bold)v.setTypeface(Typeface.DEFAULT,1);v.setPadding(0,dp(3),0,dp(5));group.addView(v);return v;}
 void action(String s,Runnable f){Button b=new Button(this);b.setAllCaps(false);b.setText(s);b.setTextColor(teal);group.addView(b);b.setOnClickListener(v->f.run());}
 void draw(){
  cleanAudio();
  ScrollView scroll=new ScrollView(this);scroll.setOnApplyWindowInsetsListener((view,insets)->{view.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});scroll.setBackgroundColor(0xffedf3ef);page=new LinearLayout(this);page.setOrientation(1);page.setPadding(dp(16),dp(20),dp(16),dp(12));scroll.addView(page);setContentView(scroll);
  boolean available=roles.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING),on=available&&roles.isRoleHeld(RoleManager.ROLE_CALL_SCREENING);
  card(teal);label("LUMINA STUDIOS",12,mint,true);label("Lumina Focus",34,-1,true);label(on?"Call screening is active":available?"Call screening is off":"Screening unavailable on this device",19,-1,true);
  label("Screens incoming numbers outside your contacts when Android supplies caller ID.",15,-1,false);
  label("Android shares eligible caller numbers with Call Guard to check local rules. Recording links and call history are optional.",14,-1,false);
  action("Privacy & permissions",this::privacy);
  if(!on&&available)action("Turn on protection",()->startActivityForResult(roles.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING),1));
  card(-1);label("Lumina Focus · Private Browser",21,ink,true);
  label("Browse with tracker, popup, and common ad-domain blocking. YouTube video ads may still appear.",14,muted,false);
  action("Open protected browser",()->startActivity(new Intent(this,FocusBrowser.class)));
  action("Watch YouTube",()->startActivity(new Intent(this,FocusBrowser.class).putExtra("youtube",true)));
  card(-1);label("Private chat",21,ink,true);
  label("Pair two nearby phones for encrypted, disappearing texts and photos.",14,muted,false);
  action("Open private chat",()->startActivity(new Intent(this,PrivateChat.class)));
  card(-1);label("Robocall blocking",21,ink,true);
  option("Block reported robocall numbers","robo",true);
  label("One recorded-message complaint in the bundled FTC data is enough to block a screened number. Your allow list takes priority.",14,muted,false);
  option("Trusted callers only","trusted",false);
  label("Reject every screened call unless you have allowed the number. Saved contacts can ring. New callers, including employers and appointment reminders, may be blocked.",14,muted,false);
  card(-1);label("Call recordings",21,ink,true);
  label("On supported phones, answer a call and tap Record in Phone. Participants hear an announcement. To link a saved audio file, open the screened call below and choose Actions.",14,muted,false);
  action("Open Phone",()->{try{startActivity(new Intent(Intent.ACTION_DIAL));}catch(ActivityNotFoundException e){Toast.makeText(this,"Open your Phone app",Toast.LENGTH_LONG).show();}});
  card(-1);label("Spam protection",21,ink,true);Switch toggle=new Switch(this);toggle.setText("Block repeatedly reported numbers");toggle.setTextColor(ink);toggle.setMinHeight(dp(48));group.addView(toggle);toggle.setChecked(prefs.getBoolean("auto",true));toggle.setOnCheckedChangeListener((b,v)->prefs.edit().putBoolean("auto",v).apply());
  label("Blocks a number after 3 complaints in the bundled FTC data. Your allow list takes priority. Caller ID can be spoofed.",14,muted,false);
  Switch cb=new Switch(this);cb.setText("Block invalid US/Canada number formats");cb.setTextColor(ink);cb.setMinHeight(dp(48));group.addView(cb);cb.setChecked(prefs.getBoolean("callback",true));cb.setOnCheckedChangeListener((b,v)->prefs.edit().putBoolean("callback",v).apply());
  label("If a callback reaches a disconnected line, open the number here and mark it disconnected to block future calls. A valid-looking number can still be out of service.",14,muted,false);
  action("Set up hidden-number blocking",()->new AlertDialog.Builder(this).setTitle("Block private calls").setMessage("In Phone settings, open Blocked numbers and turn on Unknown. This setting is controlled by your phone. Visible numbers outside your contacts can still ring.").setNegativeButton("Close",null).setPositiveButton("Open blocked numbers",(d,w)->{try{startActivity(getSystemService(android.telecom.TelecomManager.class).createManageBlockedNumbersIntent());}catch(RuntimeException e){new AlertDialog.Builder(this).setMessage("Open Phone → Settings → Blocked numbers → Unknown.").setPositiveButton("OK",null).show();}}).show());
  action("Look up a number",()->lookup());action("Manage blocked & allowed numbers",()->rules());
  card(-1);label("Recent screened calls",21,ink,true);option("Save call history on this phone","saveHistory",false);label("Saves the number, time and screening result for up to 50 calls.",14,muted,false);JSONArray calls=Guard.history(this);
  if(calls.length()==0)label("Screened calls will appear here. Tap a call to see why it rang or was blocked, or to report it.",15,muted,false);
  for(int i=0;i<calls.length();i++){JSONObject j=calls.optJSONObject(i);if(j==null)continue;String n=j.optString("n");long t=j.optLong("t");boolean blocked=j.optBoolean("b");action((blocked?"Blocked":"Allowed")+"   "+n+"\n"+date(t),()->details(n,t,j.optString("v"),blocked?"Blocked":"Allowed"));}
  if(calls.length()>0)action("Clear call history",()->new AlertDialog.Builder(this).setMessage("Clear all screened calls from this phone?").setNegativeButton("Cancel",null).setPositiveButton("Clear",(d,w)->prefs.edit().remove("recent").apply()).show());
  card(0xffe1ebe5);label("Offline complaint data",16,ink,true);label("FTC files published Oct 30–Nov 12, 2026. This version uses a fixed snapshot. Reports describe the displayed number and do not establish who called. Call history stays on your phone.",14,muted,false);
 }
 void option(String title,String key,boolean initial){
  Switch sw=new Switch(this);sw.setText(title);sw.setTextColor(ink);sw.setMinHeight(dp(48));sw.setChecked(prefs.getBoolean(key,initial));group.addView(sw);
  sw.setOnCheckedChangeListener((b,v)->prefs.edit().putBoolean(key,v).apply());
 }
 protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putString("pendingRecording",pendingRecording);}
 String audioKey(String n,long t){return "audio/"+t+"/"+n;}
 void pickAudio(String key){
  Intent pick=new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("audio/*");
  pendingRecording=key;
  try{startActivityForResult(pick,81);}catch(ActivityNotFoundException e){pendingRecording=null;Toast.makeText(this,"File picker unavailable",1).show();}
 }
 protected void onActivityResult(int request,int result,Intent data){
  super.onActivityResult(request,result,data);
  if(request!=81)return;
  String key=pendingRecording;pendingRecording=null;
  if(result!=RESULT_OK||key==null||data==null||data.getData()==null)return;
  Uri uri=data.getData();
  try{getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);String old=prefs.getString(key,"");prefs.edit().putString(key,uri.toString()).apply();releaseUnused(old);Toast.makeText(this,"Recording linked",0).show();}
  catch(SecurityException e){Toast.makeText(this,"Choose a saved audio file in Files",1).show();}
 }
 void releaseUnused(String uri){
  if(!uri.isEmpty()&&!prefs.getAll().containsValue(uri))try{getContentResolver().releasePersistableUriPermission(Uri.parse(uri),Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(SecurityException ignored){}
 }
 void privacy(){
  try(java.io.InputStream input=getAssets().open("privacy.txt");java.util.Scanner scanner=new java.util.Scanner(input,"UTF-8")){
   new AlertDialog.Builder(this).setTitle("Privacy & permissions").setMessage(scanner.useDelimiter("\\A").next()).setPositiveButton("Done",null).show();
  }catch(java.io.IOException e){Toast.makeText(this,"Privacy notice unavailable",1).show();}
 }
 void cleanAudio(){
  HashSet<String> live=new HashSet<>();JSONArray recent=Guard.history(this);
  for(int i=0;i<recent.length();i++){JSONObject j=recent.optJSONObject(i);if(j!=null)live.add(audioKey(j.optString("n"),j.optLong("t")));}
  for(String k:prefs.getAll().keySet())if(k.startsWith("audio/")&&!live.contains(k)){String uri=prefs.getString(k,"");prefs.edit().remove(k).apply();releaseUnused(uri);}
 }
 String date(long t){return DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT).format(new Date(t));}
 void lookup(){EditText e=new EditText(this);e.setSingleLine();e.setInputType(InputType.TYPE_CLASS_PHONE);e.setHint("Phone number with area code");AlertDialog d=new AlertDialog.Builder(this).setTitle("Look up a number").setView(e).setPositiveButton("Look up",null).setNegativeButton("Cancel",null).create();d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{String n=Guard.key(e.getText().toString());if(n.isEmpty()){e.setError("Enter 10 US digits or an international number starting with +");return;}d.dismiss();details(n,0,"","");}));d.show();}
 void details(String n,long t,String carrier,String outcome){
  String key=Guard.key(n);int c=Guard.count(this,key);String rule=prefs.getString("rule"+key,"");
  String info=(t>0?outcome+" • "+date(t)+"\n"+carrier+"\n\n":"")+(key.startsWith("+1")?(c==0?"No match in the FTC snapshot":c+" complaint"+(c==1?"":"s")+" in the FTC snapshot"):"Outside US complaint lookup coverage")+(Guard.invalidCallback(key)?"\nInvalid North American number format":"")+"\nPersonal rule: "+(rule.isEmpty()?"Default":rule);
  ArrayList<String> opts=new ArrayList<>();if(!key.isEmpty()){opts.add("Mark disconnected & block");opts.add("Block this number");opts.add("Always allow this number");if(!rule.isEmpty())opts.add("Remove personal rule");}opts.add("Report unwanted call to FTC");
  String audio=audioKey(n,t),uri=prefs.getString(audio,"");
  if(t>0){opts.add("Link saved recording");if(!uri.isEmpty())opts.add("Play recording");}
  int rb=Guard.lookup(this,key,true);if(rb>0)info+="\nRecorded-message complaints: "+rb;
  // Buttons are below the summary so they remain accessible on small screens.
  new AlertDialog.Builder(this).setTitle(n).setMessage(info).setNegativeButton("Close",null).setPositiveButton("Actions",(d,w)->new AlertDialog.Builder(this).setTitle(n).setItems(opts.toArray(new String[0]),(a,i)->{String choice=opts.get(i);if(choice.startsWith("Link")){pickAudio(audio);return;}
  if(choice.startsWith("Play")){try{startActivity(new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(uri),"audio/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));}catch(RuntimeException e){Toast.makeText(this,"Unable to open audio. Check the file or link it again.",1).show();}return;}
  if(choice.startsWith("Report")){report(n,t);return;}SharedPreferences.Editor ed=prefs.edit();if(choice.startsWith("Remove"))ed.remove("rule"+key);else ed.putString("rule"+key,choice.startsWith("Mark")?"disconnected":choice.startsWith("Block")?"block":"allow");ed.apply();draw();Toast.makeText(this,"Personal rule updated",0).show();}).show()).show();
 }
 void rules(){ArrayList<String> nums=new ArrayList<>();for(String s:prefs.getAll().keySet())if(s.startsWith("rule"))nums.add(s.substring(4));Collections.sort(nums);String[] rows=new String[nums.size()];for(int i=0;i<rows.length;i++)rows[i]=nums.get(i)+"  ·  "+prefs.getString("rule"+nums.get(i),"");AlertDialog.Builder d=new AlertDialog.Builder(this).setTitle("Personal rules").setNegativeButton("Close",null).setPositiveButton("Add number",(x,w)->lookup());if(rows.length==0)d.setMessage("Add a number to block it or always let it ring.");else d.setItems(rows,(x,i)->details(nums.get(i),0,"",""));d.show();}
 void report(String n,long t){String summary="Displayed number: "+n+(t>0?"\nCall received: "+date(t)+" ("+TimeZone.getDefault().getID()+")":"");new AlertDialog.Builder(this).setTitle("Report an unwanted call").setMessage(summary+"\n\nCopy these details and open the FTC form. Review and submit the report there.").setNegativeButton("Cancel",null).setPositiveButton("Copy & open FTC",(x,w)->{getSystemService(android.content.ClipboardManager.class).setPrimaryClip(ClipData.newPlainText("Call details",summary));try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.donotcall.gov/report.html")));}catch(ActivityNotFoundException e){new AlertDialog.Builder(this).setMessage("Details copied. Visit donotcall.gov/report.html in a browser.").setPositiveButton("Done",null).show();}}).show();}
}
