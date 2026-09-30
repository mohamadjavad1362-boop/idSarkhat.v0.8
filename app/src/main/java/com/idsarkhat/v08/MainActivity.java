package com.idsarkhat.v08;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.*;
import android.view.accessibility.AccessibilityManager;
import android.content.Context;

public class MainActivity extends Activity {
    private static final String EASY="ir.easytrader.orbis.m.twa";
    TextView status,packageText,pageText; SharedPreferences prefs;
    @Override public void onCreate(Bundle b){
        super.onCreate(b); setContentView(R.layout.activity_main);
        status=findViewById(R.id.status); packageText=findViewById(R.id.packageText); pageText=findViewById(R.id.pageText);
        prefs=getSharedPreferences("idsarkhat",MODE_PRIVATE);
        findViewById(R.id.accessibility).setOnClickListener(v->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        findViewById(R.id.register).setOnClickListener(v->{prefs.edit().putString("target_package",EASY).apply();status.setText("EasyTrader به‌عنوان برنامه هدف ثبت شد.");readData();});
        readData();
    }
    @Override protected void onResume(){super.onResume();readData();}
    private void readData(){
        boolean enabled=isAccessibilityEnabled();
        boolean detected=prefs.getBoolean("easytrader_detected",false);
        long ts=prefs.getLong("page_timestamp",0L);
        packageText.setText("بسته EasyTrader: "+EASY+"\nAccessibility: "+(enabled?"فعال":"غیرفعال")+"\nEasyTrader تشخیص داده شد: "+(detected?"بله":"خیر")+"\nآخرین دریافت: "+(ts==0?"—":new java.text.SimpleDateFormat("HH:mm:ss",java.util.Locale.US).format(new java.util.Date(ts))));
        String data=prefs.getString("page_text","");
        String prices=prefs.getString("price_candidates","");
        pageText.setText((prices.isEmpty()?"قیمت عددی هنوز استخراج نشده است.":"قیمت/اعداد: "+prices)+"\n\n"+(data.isEmpty()?"متن صفحه هنوز دریافت نشده است.":data));
        status.setText(enabled&&detected&&!data.isEmpty()?"✓ اتصال و خواندن صفحه EasyTrader برقرار است؛ ارسال سفارش همچنان غیرفعال است.":enabled?"✓ Accessibility فعال است؛ حالا EasyTrader را باز و روی صفحه موردنظر نگه دار.":"⚠ Accessibility هنوز فعال نیست؛ دکمه بالا را بزن و idSarkhat را فعال کن.");
    }
    private boolean isAccessibilityEnabled(){
        AccessibilityManager m=(AccessibilityManager)getSystemService(Context.ACCESSIBILITY_SERVICE);
        if(m==null)return false;
        String enabled=m.getEnabledAccessibilityServiceList(AccessibilityManager.FEEDBACK_ALL_MASK).toString();
        return enabled.contains(getPackageName()+"/.EasyTraderAccessibilityService")||enabled.contains(getPackageName()+"/"+getPackageName()+".EasyTraderAccessibilityService");
    }
}