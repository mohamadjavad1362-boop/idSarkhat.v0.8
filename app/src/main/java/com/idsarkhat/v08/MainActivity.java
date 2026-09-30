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
            if (last.isEmpty() || getPackageName().equals(last)) {
                status.setText("ابتدا Accessibility را فعال کن و داخل EasyTrader بمان، سپس دوباره ثبت را بزن.");
                return;
            }
            prefs.edit().putString("target_package", last).apply();
            status.setText("برنامه هدف ثبت شد: " + last);
            readData();
        });

        findViewById(R.id.read).setOnClickListener(v -> readData());
        readData();
    }

    @Override protected void onResume() {
        super.onResume();
        readData();
    }

    private void readData() {
        String last = prefs.getString("last_package", "—");
        String target = prefs.getString("target_package", "—");
        String detected = prefs.getString("detected_package", "—");
        String prices = prefs.getString("price_candidates", "");
        String data = prefs.getString("page_text", "");
        boolean detectedOk = prefs.getBoolean("easytrader_detected", false);
        long ts = prefs.getLong("page_timestamp", 0L);

        packageText.setText("آخرین برنامه: " + last
                + "\nبرنامه هدف: " + target
                + "\nEasyTrader تشخیص داده شد: " + (detectedOk ? "بله" : "خیر")
                + "\nبسته تشخیص‌داده‌شده: " + detected
                + "\nآخرین دریافت: " + (ts == 0 ? "—" : new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(new java.util.Date(ts))));

        String priceLine = prices.isEmpty() ? "قیمت عددی هنوز استخراج نشده است." : "اعداد/قیمت‌های شناسایی‌شده: " + prices;
        pageText.setText(priceLine + "\n\n" + (data.isEmpty() ? "متن صفحه هنوز دریافت نشده است." : data));
        status.setText(detectedOk && !data.isEmpty()
                ? "✓ صفحه EasyTrader دریافت شد؛ مرحله ارسال سفارش هنوز غیرفعال است."
                : "منتظر شناسایی صفحه EasyTrader و دریافت داده...");
    }
}
