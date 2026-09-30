package com.idsarkhat.v08;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.*;

public class MainActivity extends Activity {
    TextView status, packageText, pageText;
    SharedPreferences prefs;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        status = findViewById(R.id.status);
        packageText = findViewById(R.id.packageText);
        pageText = findViewById(R.id.pageText);
        prefs = getSharedPreferences("idsarkhat", MODE_PRIVATE);

        findViewById(R.id.accessibility).setOnClickListener(v ->
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));

        findViewById(R.id.register).setOnClickListener(v -> {
            String last = prefs.getString("last_package", "");
            if (last.isEmpty()) {
                status.setText("ابتدا Accessibility را فعال کن و وارد EasyTrader شو.");
            } else {
                prefs.edit().putString("target_package", last).apply();
                status.setText("برنامه هدف ثبت شد: " + last);
                packageText.setText("EasyTrader هدف: " + last);
            }
        });

        findViewById(R.id.read).setOnClickListener(v -> readData());
        readData();
    }

    private void readData() {
        String last = prefs.getString("last_package", "—");
        String target = prefs.getString("target_package", "—");
        String data = prefs.getString("page_text", "");
        packageText.setText("آخرین برنامه: " + last + "\nبرنامه هدف: " + target);
        pageText.setText(data.isEmpty() ? "هنوز داده‌ای از صفحه هدف دریافت نشده است." : data);
        status.setText(data.isEmpty() ? "منتظر داده از EasyTrader" : "داده صفحه دریافت شد");
    }
}