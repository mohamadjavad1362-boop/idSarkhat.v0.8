package com.idsarkhat.v08;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EasyTraderAccessibilityService extends AccessibilityService {
    private static final String PREFS = "idsarkhat";
    private static final String OWN_PACKAGE = "com.idsarkhat.v08";
    private static final Pattern NUMBER = Pattern.compile("(?<![\\d])(?:[0-9۰-۹]{1,3}(?:[,٬][0-9۰-۹]{3})+|[0-9۰-۹]{3,})(?![\\d])");

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        String pkg = event.getPackageName().toString();
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString("last_package", pkg).apply();

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        StringBuilder out = new StringBuilder();
        collectText(root, out);
        root.recycle();

        String text = out.toString().trim();
        if (text.isEmpty()) return;

        boolean looksLikeEasyTrader = isEasyTraderPage(text);
        if (looksLikeEasyTrader && !OWN_PACKAGE.equals(pkg)) {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putString("detected_package", pkg)
                    .putString("target_package", pkg)
                    .apply();
        }

        String target = getSharedPreferences(PREFS, MODE_PRIVATE).getString("target_package", "");
        if (target.isEmpty() || !target.equals(pkg)) return;

        String prices = extractNumbers(text);
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putString("page_text", text)
                .putString("price_candidates", prices)
                .putBoolean("easytrader_detected", looksLikeEasyTrader)
                .putLong("page_timestamp", System.currentTimeMillis())
                .apply();
    }

    private boolean isEasyTraderPage(String text) {
        String s = text.toLowerCase(Locale.ROOT);
        return s.contains("easytrader") || s.contains("ایزی") || s.contains("ایزی‌تریدر")
                || s.contains("خرید") && s.contains("فروش")
                || s.contains("قیمت") && (s.contains("حجم") || s.contains("سفارش"));
    }

    private String extractNumbers(String text) {
        Matcher m = NUMBER.matcher(normalizeDigits(text));
        StringBuilder result = new StringBuilder();
        while (m.find()) {
            if (result.length() > 0) result.append(" | ");
            result.append(m.group());
        }
        return result.toString();
    }

    private String normalizeDigits(String s) {
        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '۰' && c <= '۹') c = (char) ('0' + c - '۰');
            b.append(c);
        }
        return b.toString();
    }

    private void collectText(AccessibilityNodeInfo node, StringBuilder out) {
        if (node == null) return;
        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        if (text != null && text.length() > 0) out.append(text).append('\n');
        else if (desc != null && desc.length() > 0) out.append(desc).append('\n');

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                collectText(child, out);
                child.recycle();
            }
        }
    }

    @Override public void onInterrupt() {}
}
