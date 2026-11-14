package com.luminastudios.callguard;
import android.content.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import org.json.*;

class Guard {
 static String key(String input){
  String s=input.trim().replaceAll("[() .-]","");
  if(s.matches("[0-9]{10}")) return "+1"+s;
  if(s.matches("1[0-9]{10}")) return "+"+s;
  return s.matches("\\+[1-9][0-9]{6,14}")?s:"";
 }
 static SharedPreferences prefs(Context c){return c.getSharedPreferences("guard",0);}
 static ByteBuffer index,roboIndex;
 static synchronized int count(Context c,String number){
  return lookup(c,number,false);
 }
 static synchronized int lookup(Context c,String number,boolean robo){
  ByteBuffer table=robo?roboIndex:index;
  if(!number.matches("\\+1[0-9]{10}"))return 0;
  try {
   if(table==null)try(InputStream in=c.getAssets().open(robo?"robocalls.bin":"reports.bin"); ByteArrayOutputStream out=new ByteArrayOutputStream()){
    byte[] buf=new byte[16384]; int n; while((n=in.read(buf))!=-1)out.write(buf,0,n); table=ByteBuffer.wrap(out.toByteArray());if(robo)roboIndex=table;else index=table;
   }
   long target=Long.parseLong(number.substring(2)); int lo=0,hi=table.limit()/12-1;
   while(lo<=hi){int mid=(lo+hi)>>>1;long found=table.getLong(mid*12);if(found==target)return table.getInt(mid*12+8);if(found<target)lo=mid+1;else hi=mid-1;}
  }catch(IOException ignored){} return 0;
 }
 static boolean invalidCallback(String n){
  // NANP area codes and exchanges both start with digits 2 through 9.
  return n.matches("\\+1[0-9]{10}") && (n.charAt(2)<'2' || n.charAt(5)<'2');
 }
 static boolean callbackBlock(String rule,String number,boolean enabled){
  return rule.equals("disconnected") || (enabled && !rule.equals("allow") && invalidCallback(number));
 }
 static boolean block(String rule,int count,boolean auto){return rule.equals("block")||(!rule.equals("allow")&&auto&&count>=3);}
 static String screeningReason(String rule, String number, int complaints, int recorded, boolean auto, boolean callbacks, boolean filterRobo, boolean trustedOnly){
  if(rule.equals("allow"))return "";
  if(rule.equals("block"))return "Personal block list";
  if(rule.equals("disconnected"))return "Previously marked disconnected";
  if(callbacks && invalidCallback(number))return "Invalid callback format";
  if(filterRobo && recorded>0)return "Reported as a recorded message or robocall";
  if(auto && complaints>=3)return "Repeated complaints";
  return trustedOnly?"Trusted callers only":"";
 }
 static JSONArray history(Context c){try{return new JSONArray(prefs(c).getString("recent","[]"));}catch(JSONException e){return new JSONArray();}}
 static void log(Context c,String num,int reports,String check,boolean blocked,long time){
  try{JSONArray old=history(c),fresh=new JSONArray();fresh.put(new JSONObject().put("n",num).put("c",reports).put("v",check).put("b",blocked).put("t",time));
   for(int i=0;i<Math.min(49,old.length());i++)fresh.put(old.get(i));prefs(c).edit().putString("recent",fresh.toString()).apply();}catch(JSONException ignored){}
 }
}
