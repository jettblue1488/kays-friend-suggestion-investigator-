package com.kays.friendsuggestions;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import java.util.HashSet;
import java.util.Set;

/** Reads display names only after optional Android contact permission. */
public final class ContactMatcher {
    private ContactMatcher() { }
    public static Set<String> names(Context ctx) {
        Set<String> result = new HashSet<>();
        if (ctx.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
            return result;
        String[] projection = { ContactsContract.Contacts.DISPLAY_NAME_PRIMARY };
        try (Cursor c = ctx.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                 projection, null, null, null)) {
            if (c == null) return result;
            while (c.moveToNext()) {
                String name = c.getString(0);
                if (name != null && !name.trim().isEmpty())
                    result.add(SuggestionRules.normalize(name));
            }
        } catch (SecurityException ignored) { }
        return result;
    }
}
