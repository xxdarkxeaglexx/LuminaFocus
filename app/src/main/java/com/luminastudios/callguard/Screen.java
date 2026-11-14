package com.luminastudios.callguard;
import android.telecom.*;
import android.content.*;
import android.os.Build;
public class Screen extends CallScreeningService {
 public void onScreenCall(Call.Details d){
  if(d.getCallDirection()!=Call.Details.DIRECTION_INCOMING)return;
  String raw=d.getHandle()==null?"":d.getHandle().getSchemeSpecificPart();
  String num=Guard.key(raw==null?"":raw);int reports=Guard.count(this,num);
  SharedPreferences p=Guard.prefs(this);String rule=num.isEmpty()?"":p.getString("rule"+num,"");
  int recorded=Guard.lookup(this,num,true);
  String reason=Guard.screeningReason(rule,num,reports,recorded,p.getBoolean("auto",true),p.getBoolean("callback",true),p.getBoolean("robo",true),p.getBoolean("trusted",false));
  boolean blocked=!reason.isEmpty();
  respondToCall(d,new CallResponse.Builder().setDisallowCall(blocked).setRejectCall(blocked).build());
  int status=Build.VERSION.SDK_INT>=30?d.getCallerNumberVerificationStatus():0;
  String check=status==Connection.VERIFICATION_STATUS_PASSED?"Carrier verified the number":status==Connection.VERIFICATION_STATUS_FAILED?"Carrier verification failed":"Carrier verification unavailable";
  if(blocked)check+=" • "+reason;
  if(p.getBoolean("saveHistory",false)) Guard.log(this,num.isEmpty()?(raw==null?"Unavailable":raw):num,reports,check,blocked,d.getCreationTimeMillis());
 }
}
