package com.kays.friendsuggestions;

import android.view.accessibility.AccessibilityNodeInfo;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Heuristic only: collects names next to visible Follow / Add controls. */
public final class SuggestionExtractor {
    private SuggestionExtractor() { }
    public static final class Person {
        public final String name, handle;
        public Person(String name, String handle) { this.name = name; this.handle = handle; }
    }
    public static List<Person> extract(AccessibilityNodeInfo root) {
        List<Person> result = new ArrayList<>();
        if (root == null || !heading(root, 0)) return result;
        scan(root, result, new HashSet<>(), 0);
        return result;
    }
    private static String read(AccessibilityNodeInfo node) {
        if (node == null) return "";
        CharSequence t = node.getText();
        if (t != null && t.length() > 0) return t.toString().trim();
        CharSequence d = node.getContentDescription();
        return d == null ? "" : d.toString().trim();
    }
    private static boolean heading(AccessibilityNodeInfo node, int level) {
        if (node == null || level > 35) return false;
        if (SuggestionRules.suggestionHeading(read(node))) return true;
        for (int i=0;i<Math.min(80,node.getChildCount());i++)
            if (heading(node.getChild(i), level+1)) return true;
        return false;
    }
    private static void scan(AccessibilityNodeInfo node, List<Person> result, Set<String> seen, int level) {
        if (node == null || level > 35 || result.size() >= 35) return;
        if (SuggestionRules.addControl(read(node))) {
            Person p = nearButton(node);
            if (p != null) {
                String id = SuggestionRules.normalize(p.handle.isEmpty() ? p.name : p.handle);
                if (!id.isEmpty() && seen.add(id)) result.add(p);
            }
        }
        for (int i=0;i<Math.min(80,node.getChildCount());i++)
            scan(node.getChild(i), result, seen, level+1);
    }
    private static Person nearButton(AccessibilityNodeInfo button) {
        AccessibilityNodeInfo parent = button.getParent();
        for (int level=0;parent!=null && level<4;level++,parent=parent.getParent()) {
            List<String> values = new ArrayList<>();
            collect(parent, values, 0);
            List<String> names = new ArrayList<>();
            for (String s: values)
                if (SuggestionRules.possibleName(s) && !names.contains(s)) names.add(s);
            if (names.size() > 0 && names.size() <= 3 && values.size() <= 16) {
                String name = names.get(0), handle = "";
                for (String s : names) if (s.startsWith("@")) handle = s;
                if (name.startsWith("@") && names.size() > 1) name = names.get(1);
                return new Person(name, handle);
            }
        }
        return null;
    }
    private static void collect(AccessibilityNodeInfo node, List<String> values, int level) {
        if (node == null || level > 6 || values.size() >= 20) return;
        String s = read(node);
        if (!s.isEmpty() && !values.contains(s)) values.add(s);
        for (int i=0;i<Math.min(35,node.getChildCount());i++)
            collect(node.getChild(i), values, level+1);
    }
}
