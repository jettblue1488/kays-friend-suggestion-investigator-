package com.kays.friendsuggestions;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

/** Never scans in the background without an active, user-chosen window. */
public class SuggestionAccessibilityService extends AccessibilityService {
    private long lastCapture = 0;
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || !ScanSession.consented(this)) return;
        CharSequence pkg = event.getPackageName();
        if (pkg == null || !ScanSession.accepts(this, pkg.toString())) return;
        long now = System.currentTimeMillis();
        if (now - lastCapture < 900) return;
        lastCapture = now;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        List<SuggestionExtractor.Person> people = SuggestionExtractor.extract(root);
        if (!people.isEmpty()) ObservationStore.record(this, ScanSession.platform(this), people);
    }
    @Override public void onInterrupt() { }
}
