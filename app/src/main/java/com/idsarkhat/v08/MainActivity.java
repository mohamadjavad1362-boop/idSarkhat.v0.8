package com.idsarkhat.v08;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.widget.*;
import android.view.*;

public class MainActivity extends Activity {
    WebView web; TextView status; EditText url;
    @Override public void onCreate(Bundle b){ super.onCreate(b); setContentView(R.layout.activity_main);
        status=findViewById(R.id.status); url=findViewById(R.id.url); web=findViewById(R.id.web);
        WebSettings s=web.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setSupportZoom(true);
        web.setWebViewClient(new WebViewClient(){ @Override public void onPageFinished(WebView v,String u){ detect(u); }});
        findViewById(R.id.open).setOnClickListener(v->{String u=url.getText().toString().trim(); if(!u.startsWith("http")) u="https://"+u; web.loadUrl(u);});
    }
    void detect(String u){ boolean easy=u.toLowerCase().contains("easytrader")||u.toLowerCase().contains("emofid"); status.setText(easy?"صفحه EasyTrader شناسایی شد: دریافت قیمت آماده آزمایش":"صفحه باز شد؛ EasyTrader هنوز شناسایی نشده است"); }
    @Override public void onBackPressed(){ if(web.canGoBack()) web.goBack(); else super.onBackPressed(); }
}
