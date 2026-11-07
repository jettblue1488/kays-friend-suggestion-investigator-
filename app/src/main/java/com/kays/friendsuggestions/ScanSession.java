package com.kays.friendsuggestions;

import android.content.Context;
import android.content.SharedPreferences;

/** A scanner session is active only for 15 minutes after a deliberate user action. */
public final class ScanSession {
    private static final String KEY = "scan_session";
    private static final long DURATION = 15L * 60_000L;
    private ScanSession() { }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(KEY, Context.MODE_PRIVATE);
    }
    public static void start(Context ctx, String platform, String pkg) {
        prefs(ctx).edit().putString("platform", platform).putString("package", pkg)
            .putLong("expires", System.currentTimeMillis() + DURATION).apply();
    }
    public static void stop(Context ctx) {
        prefs(ctx).edit().remove("expires").remove("platform").remove("package").apply();
    }
    public static boolean active(Context ctx) {
        return prefs(ctx).getLong("expires", 0) > System.currentTimeMillis();
    }
    public static String platform(Context ctx) {
        return prefs(ctx).getString("platform", "");
    }
    public static boolean accepts(Context ctx, String pkg) {
        if (!active(ctx) || pkg == null) return false;
        String selected = prefs(ctx).getString("package", "");
        return pkg.equals(selected) || (selected.equals("com.zhiliaoapp.musically")
            && pkg.equals("com.ss.android.ugc.trill"));
    }
    public static boolean consented(Context ctx) {
        return prefs(ctx).getBoolean("consent", false);
    }
    public static void consent(Context ctx) {
        prefs(ctx).edit().putBoolean("consent", true).apply();
    }
}
