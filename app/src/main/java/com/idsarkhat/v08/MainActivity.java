package com.idsarkhat.v08;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String EASY = "ir.easytrader.orbis.m.twa";
    private static final String PREFS = "idsarkhat";

    private TextView status, packageText, pageText;
    private SharedPreferences prefs;
    private final Handler handler = new Handler();

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        status = findViewById(R.id.status);
        packageText = findViewById(R.id.packageText);
        pageText = findViewById(R.id.pageText);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        findViewById(R.id.accessibility).setOnClickListener(v -> {
            if (isAccessibilityEnabled()) {
                status.setText("✓ دسترسی خواندن صفحه idSarkhat فعال است.");
            } else {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                status.setText("صفحه دسترسی‌ها باز شد؛ idSarkhat را در بخش برنامه‌های نصب‌شده فعال کن.");
            }
        });

        findViewById(R.id.register).setOnClickListener(v -> connectToEasyTrader());

        findViewById(R.id.read).setOnClickListener(v -> {
            if (!isAccessibilityEnabled()) {
                status.setText("⚠ ابتدا دسترسی «خواندن صفحه» را فعال کن.");
                return;
            }

            if (!isEasyTraderInstalled()) {
                status.setText("⚠ EasyTrader روی گوشی پیدا نشد.");
                return;
            }

            prefs.edit()
                    .putString("target_package", EASY)
                    .apply();

            readData();

            if (!hasFreshData()) {
                status.setText("در انتظار صفحه EasyTrader و دریافت داده... EasyTrader را باز و صفحه نماد را نمایش بده.");
            }
        });

        readData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(this::readData, 350);
    }

    private void connectToEasyTrader() {
        if (!isAccessibilityEnabled()) {
            status.setText("⚠ ابتدا مرحله ۱ را انجام بده و دسترسی خواندن صفحه idSarkhat را فعال کن.");
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            return;
        }

        if (!isEasyTraderInstalled()) {
            status.setText("⚠ EasyTrader روی گوشی نصب نیست یا قابل شناسایی نیست.");
            return;
        }

        prefs.edit()
                .putString("target_package", EASY)
                .putBoolean("connection_requested", true)
                .apply();

        status.setText("در حال راه‌اندازی اتصال به EasyTrader...");

        try {
            Intent launch = getPackageManager().getLaunchIntentForPackage(EASY);
            if (launch == null) {
                status.setText("⚠ EasyTrader نصب است اما صفحه راه‌اندازی آن پیدا نشد.");
                return;
            }

            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launch);

            status.setText("✓ EasyTrader باز شد؛ منتظر شناسایی صفحه و دریافت داده...");

            handler.postDelayed(() -> {
                readData();
                if (hasFreshData()) {
                    status.setText("✓ اتصال به EasyTrader برقرار شد و صفحه شناسایی شد.");
                } else {
                    status.setText("در انتظار شناسایی صفحه EasyTrader و دریافت داده...");
                }
            }, 1200);

        } catch (Exception e) {
            status.setText("⚠ راه‌اندازی EasyTrader انجام نشد: " + e.getClass().getSimpleName());
        }
    }

    private void readData() {
        boolean enabled = isAccessibilityEnabled();
        boolean installed = isEasyTraderInstalled();
        boolean detected = prefs.getBoolean("easytrader_detected", false);
        long ts = prefs.getLong("page_timestamp", 0L);

        String time = ts == 0
                ? "—"
                : new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date(ts));

        packageText.setText(
                "بسته EasyTrader: " + EASY
                        + "\nنصب بودن EasyTrader: " + (installed ? "بله" : "خیر")
                        + "\nAccessibility: " + (enabled ? "فعال" : "غیرفعال")
                        + "\nEasyTrader تشخیص داده شد: " + (detected ? "بله" : "خیر")
                        + "\nآخرین دریافت: " + time
        );

        String data = prefs.getString("page_text", "");
        String prices = prefs.getString("price_candidates", "");

        StringBuilder market = new StringBuilder();
        if (prices.isEmpty()) {
            market.append("قیمت عددی هنوز استخراج نشده است.");
        } else {
            market.append("قیمت/اعداد: ").append(prices);
        }

        market.append("\n\n");
        if (data.isEmpty()) {
            market.append("متن صفحه هنوز دریافت نشده است.");
        } else {
            market.append(data);
        }

        pageText.setText(market.toString());

        if (!enabled) {
            status.setText("⚠ Accessibility هنوز فعال نیست؛ مرحله ۱ را انجام بده.");
        } else if (!installed) {
            status.setText("⚠ EasyTrader روی گوشی پیدا نشد.");
        } else if (hasFreshData()) {
            status.setText("✓ اتصال و خواندن صفحه EasyTrader برقرار است؛ ارسال سفارش هنوز غیرفعال است.");
        } else if (prefs.getBoolean("connection_requested", false)) {
            status.setText("در انتظار شناسایی صفحه EasyTrader و دریافت داده...");
        } else {
            status.setText("✓ Accessibility فعال است؛ برای اتصال، «شناسایی و ثبت EasyTrader» را بزن.");
        }
    }

    private boolean hasFreshData() {
        long ts = prefs.getLong("page_timestamp", 0L);
        String data = prefs.getString("page_text", "");
        String detected = prefs.getString("detected_package", "");
        return ts > 0 && !data.trim().isEmpty() && EASY.equals(detected);
    }

    private boolean isEasyTraderInstalled() {
        try {
            getPackageManager().getPackageInfo(EASY, PackageManager.PackageInfoFlags.of(0));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isAccessibilityEnabled() {
        AccessibilityManager manager =
                (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);

        if (manager == null) return false;

        String enabled = manager
                .getEnabledAccessibilityServiceList(AccessibilityManager.FEEDBACK_ALL_MASK)
                .toString();

        return enabled.contains(getPackageName() + "/.EasyTraderAccessibilityService")
                || enabled.contains(getPackageName() + "/" + getPackageName() + ".EasyTraderAccessibilityService");
    }
}
