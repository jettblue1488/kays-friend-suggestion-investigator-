package com.kays.friendsuggestions;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public final class SuggestionRules {
    private SuggestionRules() { }
    private static final Set<String> EXCLUDED = new HashSet<>(Arrays.asList(
        "follow","following","add","add friend","add friends","add back","accept",
        "see all","view profile","more","close","dismiss","next","search",
        "find friends","quick add","suggested for you","suggested accounts",
        "suggested friends","people you may know","people you might know",
        "discover people","friends","contacts","mutual friends","mutuals",
        "message","profile","home","inbox","for you","your friends","connect",
        "facebook","tiktok","snapchat","invite","invite friends","new"
    ));
    public static String normalize(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]", "");
    }
    public static boolean suggestionHeading(String s) {
        if (s == null) return false;
        String v = s.toLowerCase(Locale.ROOT);
        return v.contains("people you may know") || v.contains("people you might know")
            || v.contains("suggested for you") || v.contains("suggested accounts")
            || v.contains("suggested friends") || v.contains("find friends")
            || v.contains("quick add") || v.contains("discover people");
    }
    public static boolean addControl(String s) {
        if (s == null) return false;
        String v = s.trim().toLowerCase(Locale.ROOT);
        return v.equals("follow") || v.equals("+ follow") || v.equals("add")
            || v.equals("+ add") || v.equals("add friend") || v.equals("add friends")
            || v.equals("add back") || v.equals("follow back") || v.equals("accept");
    }
    public static boolean possibleName(String s) {
        if (s == null) return false;
        String v = s.trim();
        String low = v.toLowerCase(Locale.ROOT);
        if (v.length() < 2 || v.length() > 56 || EXCLUDED.contains(low)) return false;
        if (v.contains("\n") || low.contains("http") || low.contains("mutual")
            || low.contains("suggested") || low.contains("followers")
            || low.contains("following you") || low.contains("from your contacts")) return false;
        if (v.matches("[0-9 ,.+/-]+")) return false;
        return v.matches("[@\\p{L}0-9_ .'-]+") && normalize(v).length() >= 2;
    }
    public static boolean sameName(String name1, String handle1, String name2, String handle2) {
        String h1 = normalize(handle1), h2 = normalize(handle2);
        if (!h1.isEmpty() && !h2.isEmpty() && h1.equals(h2)) return true;
        String n1 = normalize(name1), n2 = normalize(name2);
        return n1.length() >= 5 && n1.equals(n2);
    }
}
