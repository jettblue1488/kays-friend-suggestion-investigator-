package com.kays.friendsuggestions;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Stores only observed names, handles, platforms and timestamps on this phone. */
public final class ObservationStore {
    private static final String PREF = "local_observations";
    private static final int LIMIT = 1000;
    private ObservationStore() { }
    public static final class Entry {
        public String platform, name, handle;
        public long firstSeen, lastSeen;
        public int count;
    }
    public static synchronized List<Entry> read(Context context) {
        String raw = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString("records", "[]");
        List<Entry> result = new ArrayList<>();
        try {
            JSONArray items = new JSONArray(raw);
            for (int i = 0; i < items.length(); i++) {
                JSONObject object = items.optJSONObject(i);
                if (object == null) continue;
                Entry e = new Entry();
                e.platform = object.optString("p", "");
                e.name = object.optString("n", "");
                e.handle = object.optString("h", "");
                e.firstSeen = object.optLong("f", 0);
                e.lastSeen = object.optLong("l", 0);
                e.count = object.optInt("c", 1);
                result.add(e);
            }
        } catch (JSONException ignored) { }
        result.sort(Comparator.comparingLong((Entry e) -> e.lastSeen).reversed());
        return result;
    }
    public static synchronized void record(Context context, String platform,
                                            List<SuggestionExtractor.Person> people) {
        if (people.isEmpty() || !ScanSession.active(context)) return;
        long now = System.currentTimeMillis();
        List<Entry> current = read(context);
        for (SuggestionExtractor.Person person : people) {
            Entry existing = null;
            for (Entry e : current) {
                if (e.platform.equals(platform) &&
                    SuggestionRules.sameName(e.name, e.handle, person.name, person.handle)) {
                    existing = e; break;
                }
            }
            if (existing == null) {
                Entry e = new Entry();
                e.platform = platform;
                e.name = person.name;
                e.handle = person.handle;
                e.firstSeen = now;
                e.lastSeen = now;
                e.count = 1;
                current.add(e);
            } else {
                if (now - existing.lastSeen > 30_000L) existing.count++;
                existing.lastSeen = now;
                if (existing.handle.isEmpty()) existing.handle = person.handle;
            }
        }
        current.sort(Comparator.comparingLong((Entry e) -> e.lastSeen).reversed());
        JSONArray array = new JSONArray();
        for (int i = 0; i < Math.min(LIMIT, current.size()); i++) {
            Entry e = current.get(i);
            JSONObject object = new JSONObject();
            try {
                object.put("p", e.platform);
                object.put("n", e.name);
                object.put("h", e.handle);
                object.put("f", e.firstSeen);
                object.put("l", e.lastSeen);
                object.put("c", e.count);
                array.put(object);
            } catch (JSONException ignored) { }
        }
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString("records", array.toString()).apply();
    }
    public static synchronized void clear(Context context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply();
        ScanSession.stop(context);
    }
}
