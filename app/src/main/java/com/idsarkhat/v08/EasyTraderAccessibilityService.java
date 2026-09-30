package com.idsarkhat.v08;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class EasyTraderAccessibilityService extends AccessibilityService {
    private static final String PREFS = "idsarkhat";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        String pkg = event.getPackageName() == null ? "" : event.getPackageName().toString();
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString("last_package", pkg).apply();

        String target = getSharedPreferences(PREFS, MODE_PRIVATE).getString("target_package", "");
        if (target.isEmpty() || !target.equals(pkg)) return;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        StringBuilder out = new StringBuilder();
        collectText(root, out);
        root.recycle();

        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putString("page_text", out.toString().trim())
                .putLong("page_timestamp", System.currentTimeMillis())
                .apply();
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
