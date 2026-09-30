package com.idsarkhat.v08;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.regex.*;

public class EasyTraderAccessibilityService extends AccessibilityService {
    private static final String PREFS="idsarkhat";
    private static final String EASY="ir.easytrader.orbis.m.twa";
    private static final Pattern NUMBER=Pattern.compile("(?<![\\d])(?:[0-9۰-۹]{1,3}(?:[,٬][0-9۰-۹]{3})+|[0-9۰-۹]{3,})(?![\\d])");

    @Override public void onServiceConnected(){
        super.onServiceConnected();
        getSharedPreferences(PREFS,MODE_PRIVATE).edit().putBoolean("accessibility_connected",true).apply();
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event){
        if(event==null||event.getPackageName()==null||!EASY.contentEquals(event.getPackageName())) return;
        AccessibilityNodeInfo root=getRootInActiveWindow();
        if(root==null)return;
        StringBuilder out=new StringBuilder(); collectText(root,out); root.recycle();
        String text=out.toString().trim(); if(text.isEmpty())return;
        String prices=extractNumbers(text);
        getSharedPreferences(PREFS,MODE_PRIVATE).edit()
          .putString("last_package",EASY).putString("detected_package",EASY).putString("target_package",EASY)
          .putString("page_text",text).putString("price_candidates",prices)
          .putBoolean("easytrader_detected",true).putLong("page_timestamp",System.currentTimeMillis()).apply();
    }
    private String extractNumbers(String text){
        Matcher m=NUMBER.matcher(normalizeDigits(text)); StringBuilder r=new StringBuilder();
        while(m.find()){if(r.length()>0)r.append(" | ");r.append(m.group());} return r.toString();
    }
    private String normalizeDigits(String s){
        StringBuilder b=new StringBuilder(s.length()); for(int i=0;i<s.length();i++){char c=s.charAt(i);if(c>='۰'&&c<='۹')c=(char)('0'+c-'۰');b.append(c);} return b.toString();
    }
    private void collectText(AccessibilityNodeInfo n,StringBuilder o){
        if(n==null)return; CharSequence t=n.getText(),d=n.getContentDescription();
        if(t!=null&&t.length()>0)o.append(t).append('\n'); else if(d!=null&&d.length()>0)o.append(d).append('\n');
        for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo c=n.getChild(i);if(c!=null){collectText(c,o);c.recycle();}}
    }
    @Override public void onInterrupt(){getSharedPreferences(PREFS,MODE_PRIVATE).edit().putBoolean("accessibility_connected",false).apply();}
}