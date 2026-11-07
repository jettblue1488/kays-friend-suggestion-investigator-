package com.kays.friendsuggestions;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(12,17,31);
    private static final int CARD = Color.rgb(27,37,57);
    private static final int WHITE = Color.WHITE;
    private static final int MUTED = Color.rgb(174,187,208);
    private static final int MINT = Color.rgb(74,225,200);
    private static final int BLUE = Color.rgb(74,115,240);
    private static final String[] PLATFORMS = {"TikTok", "Snapchat", "Facebook"};
    private static final String[] PACKAGES = {
        "com.zhiliaoapp.musically", "com.snapchat.android", "com.facebook.katana"
    };
    private LinearLayout root;
    private String filter = "All";
    private boolean resultsOpen = false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        home();
    }
    @Override protected void onResume() {
        super.onResume();
        if (root != null) { if (resultsOpen) results(); else home(); }
    }
    @Override public void onBackPressed() {
        if (resultsOpen) { resultsOpen = false; home(); }
        else super.onBackPressed();
    }
    private int dp(int value) {
        return (int)(value * getResources().getDisplayMetrics().density + .5f);
    }
    private GradientDrawable shape(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(14));
        return d;
    }
    private void gap(int height) {
        View v = new View(this);
        root.addView(v, new LinearLayout.LayoutParams(1,dp(height)));
    }
    private TextView label(LinearLayout parent, String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setLineSpacing(dp(3),1f);
        if (bold) view.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        parent.addView(view);
        return view;
    }
    private LinearLayout card() {
        LinearLayout view = new LinearLayout(this);
        view.setOrientation(LinearLayout.VERTICAL);
        view.setPadding(dp(16),dp(16),dp(16),dp(16));
        view.setBackground(shape(CARD));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        p.bottomMargin = dp(14);
        root.addView(view,p);
        return view;
    }
    private Button button(String text, int color, Runnable action) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(15);
        b.setTextAllCaps(false);
        b.setTextColor(color == MINT ? BG : WHITE);
        b.setBackground(shape(color));
        b.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,dp(52));
        p.bottomMargin = dp(10);
        root.addView(b,p);
        return b;
    }
    private void screen(String title, String subtitle) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20),dp(30),dp(20),dp(34));
        scroll.addView(root);
        setContentView(scroll);
        label(root,"KAY'S  /  PRIVATE ON-DEVICE TOOLS",11,MINT,true);
        gap(10);
        label(root,title,27,WHITE,true);
        gap(8);
        label(root,subtitle,14,MUTED,false);
        gap(20);
    }
    private void home() {
        resultsOpen = false;
        screen("Friend Suggestion Inspector",
            "Choose a social app already signed in on your phone. Observe only friend suggestions visible on its screen.");
        LinearLayout state = card();
        label(state,ScanSession.active(this)
            ? "● SCAN ACTIVE: " + ScanSession.platform(this)
            : "● SCANNER IDLE",14,MINT,true);
        label(state,ScanSession.active(this)
            ? "Selected app only. Your scan automatically ends after 15 minutes."
            : "No screen information is being recorded until you start a session.",
            13,MUTED,false);
        for (int i=0;i<PLATFORMS.length;i++) {
            final int selected = i;
            button("Scan " + PLATFORMS[i] + " suggestions",BLUE,()->startScan(selected));
        }
        if (ScanSession.active(this))
            button("Stop scan",Color.rgb(122,62,76),()->{ ScanSession.stop(this);home(); });
        button("View results and evidence",MINT,()->{ filter="All";results(); });
        button("Enable optional contact-name matches",CARD,this::requestContacts);
        LinearLayout guide=card();
        label(guide,"How to scan",18,WHITE,true);
        label(guide,"1. Tap a platform above.\n2. If prompted, enable Kay's scanner in Android Accessibility settings.\n3. Tap the platform again.\n4. In the social app, open its friend suggestions and scroll.\n5. Return to Kay's Inspector to view observations.",14,MUTED,false);
        LinearLayout warning=card();
        label(warning,"What can this app really identify?",18,WHITE,true);
        label(warning,"Possible matching contact display names and similar names on other apps. It cannot access the platform's hidden recommendation logic, identify why an individual was suggested, or reconstruct when two people were physically nearby.",13,MUTED,false);
        button("Delete all saved observations",Color.rgb(102,51,65),this::confirmDelete);
    }
    private void showDisclosure(int index) {
        new AlertDialog.Builder(this)
            .setTitle("Screen observation permission")
            .setMessage("Kay's Inspector uses Android Accessibility access. This is a powerful permission that may allow an app to read screen content. This tool uses it only during your explicit 15-minute scan of the selected TikTok, Snapchat or Facebook app, to look for visible friend-suggestion names and handles. It does not read chat history, passwords or other app screens by design. Observations stay on your phone; no Internet access is requested. You can stop a scan or revoke Accessibility access any time. Continue?")
            .setNegativeButton("Cancel",null)
            .setPositiveButton("I agree", (d,w)->{ ScanSession.consent(this);startScan(index); })
            .show();
    }
    private boolean accessibilityEnabled() {
        AccessibilityManager manager=(AccessibilityManager)getSystemService(ACCESSIBILITY_SERVICE);
        if (manager == null) return false;
        for (AccessibilityServiceInfo service : manager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK)) {
            if (service.getResolveInfo() != null &&
                service.getResolveInfo().serviceInfo != null &&
                getPackageName().equals(service.getResolveInfo().serviceInfo.packageName) &&
                SuggestionAccessibilityService.class.getName().equals(
                    service.getResolveInfo().serviceInfo.name)) return true;
        }
        return false;
    }
    private void startScan(int index) {
        if (!ScanSession.consented(this)) { showDisclosure(index);return; }
        if (!accessibilityEnabled()) {
            new AlertDialog.Builder(this).setTitle("Enable the scanner")
                .setMessage("Turn on 'Kay's visible-suggestion scanner' in Android Accessibility settings, then return here and tap Scan again. This grants broad system Accessibility permission, but the scanner's code processes only the selected app while a timed scan is active.")
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Open settings",(d,w)->
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)))
                .show();
            return;
        }
        String pkg=PACKAGES[index];
        Intent launch=getPackageManager().getLaunchIntentForPackage(pkg);
        if (launch == null && index==0) {
            pkg="com.ss.android.ugc.trill";
            launch=getPackageManager().getLaunchIntentForPackage(pkg);
        }
        if (launch == null) {
            new AlertDialog.Builder(this).setTitle(PLATFORMS[index]+" not found")
                .setMessage("Install the official social app and sign in first. The scanner only observes the app on your device.")
                .setPositiveButton("OK",null).show();
            return;
        }
        ScanSession.start(this,PLATFORMS[index],pkg);
        startActivity(launch);
    }
    private void requestContacts() {
        new AlertDialog.Builder(this).setTitle("Optional contact-name matching")
            .setMessage("Allow the inspector to compare your phone's saved contact display names against observed suggestion names. It does not read phone numbers, emails, or transmit contacts. A name match is not proof of the social platform's reason for suggesting someone.")
            .setNegativeButton("Not now",null)
            .setPositiveButton("Continue",(d,w)->{
                if (checkSelfPermission(Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},200);
                } else {
                    new AlertDialog.Builder(this).setMessage("Contact-name matches are enabled.")
                        .setPositiveButton("OK",null).show();
                }
            }).show();
    }
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] results) {
        super.onRequestPermissionsResult(requestCode,permissions,results);
        if (requestCode==200) home();
    }
    private String date(long millis) {
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT,
            Locale.getDefault()).format(new Date(millis));
    }
    private String crossMatch(ObservationStore.Entry e,List<ObservationStore.Entry> all) {
        StringBuilder found=new StringBuilder();
        for (ObservationStore.Entry other:all) {
            if (e==other || e.platform.equals(other.platform)) continue;
            if (!SuggestionRules.sameName(e.name,e.handle,other.name,other.handle)) continue;
            if (found.indexOf(other.platform)<0) {
                if (found.length()>0) found.append(", ");
                found.append(other.platform);
            }
        }
        return found.toString();
    }
    private void results() {
        resultsOpen=true;
        List<ObservationStore.Entry> entries=ObservationStore.read(this);
        screen("Saved suggestion observations",
            "These are observations from your screen, not records of the platform's decision-making or past encounters.");
        button("← Back to scan",CARD,()->{ resultsOpen=false;home(); });
        LinearLayout summary=card();
        label(summary,entries.size()+" saved suggestion profiles",21,MINT,true);
        label(summary,"First and last seen mean when this app observed a suggestion on screen—not when or where you encountered a person.",12,MUTED,false);
        LinearLayout filters=card();
        label(filters,"Filter: "+filter,14,WHITE,true);
        for (String platform:new String[]{"All","TikTok","Snapchat","Facebook"}) {
            Button chip=new Button(this);
            chip.setText(platform);
            chip.setAllCaps(false);
            chip.setTextColor(WHITE);
            chip.setBackground(shape(filter.equals(platform)?BLUE:BG));
            chip.setOnClickListener(v->{filter=platform;results();});
            filters.addView(chip);
        }
        Set<String> contacts=ContactMatcher.names(this);
        boolean contactPermission=checkSelfPermission(Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED;
        int visible=0;
        for (ObservationStore.Entry e:entries) {
            if (!filter.equals("All") && !filter.equals(e.platform)) continue;
            visible++;
            String cross=crossMatch(e,entries);
            boolean contactMatch=contacts.contains(SuggestionRules.normalize(e.name));
            LinearLayout item=card();
            label(item,e.name,18,WHITE,true);
            if (!TextUtils.isEmpty(e.handle)) label(item,e.handle,13,MUTED,false);
            label(item,e.platform+" · First observed "+date(e.firstSeen),12,MUTED,false);
            label(item,"Last observed "+date(e.lastSeen)+" · "+e.count+" scan sightings",12,MUTED,false);
            label(item,contactMatch?"Contact: matching saved display name (unverified)"
                :(contactPermission?"Contact: no exact display-name match":"Contact: permission not granted"),
                12,contactMatch?MINT:MUTED,false);
            label(item,cross.isEmpty()?"Other networks: no matching observed name"
                :"Other networks: possible match on "+cross,12,cross.isEmpty()?MUTED:MINT,false);
            label(item,"Proximity: unknown · Actual suggestion cause: unavailable",12,MUTED,false);
            item.setOnClickListener(v->details(e,contactMatch,cross,contactPermission));
        }
        if (visible==0) {
            LinearLayout empty=card();
            label(empty,"No suggestions captured",18,WHITE,true);
            label(empty,"Start a scan, open the social app's suggestions page and scroll. Some screens do not expose enough text for this prototype.",14,MUTED,false);
        }
        button("Share report as text",CARD,()->share(entries));
        button("Delete saved results",Color.rgb(102,51,65),this::confirmDelete);
    }
    private void details(ObservationStore.Entry e,boolean contact,String cross,boolean permitted) {
        String evidence="Observed profile: "+e.name+
            (e.handle.isEmpty()?"":" ("+e.handle+")")+
            "\nPlatform: "+e.platform+
            "\nFirst observed: "+date(e.firstSeen)+
            "\nLast observed: "+date(e.lastSeen)+
            "\n\nCONTACTS\n"+
            (contact?"Same display name as a saved phone contact. This does not prove a phone-number or email connection."
                :(permitted?"No exact saved contact-name match. This does not rule out contact-based suggestions."
                    :"Contacts were not checked because permission is off."))+
            "\n\nOTHER SOCIAL APPS\n"+
            (cross.isEmpty()?"No similar observed profile yet."
                :"Similar name or handle observed on "+cross+". This does not establish that they are the same person.")+
            "\n\nPHYSICAL PROXIMITY\nUnknown: the app has no verified record of the two people being near each other, with a date or time."+
            "\n\nACTUAL RECOMMENDATION REASON\nNot available from Facebook, Snapchat or TikTok via this inspector.";
        new AlertDialog.Builder(this).setTitle("Evidence, not guesses")
            .setMessage(evidence).setPositiveButton("Close",null).show();
    }
    private void share(List<ObservationStore.Entry> entries) {
        StringBuilder report=new StringBuilder("Kay's Friend Suggestion Inspector\n"+
            "On-screen observations only, not proof of why anyone was suggested.\n\n");
        for (ObservationStore.Entry e:entries) {
            if (!filter.equals("All") && !filter.equals(e.platform)) continue;
            report.append(e.platform).append(" · ").append(e.name).append(" ")
                .append(e.handle).append("\nFirst observed: ").append(date(e.firstSeen))
                .append("\nLast observed: ").append(date(e.lastSeen)).append("\n\n");
        }
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,report.toString());
        startActivity(Intent.createChooser(intent,"Share only if you choose"));
    }
    private void confirmDelete() {
        new AlertDialog.Builder(this).setTitle("Delete all saved observations?")
            .setMessage("Delete all names and timestamps from Kay's Inspector? This will not change your social-media accounts.")
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Delete",(d,w)->{
                ObservationStore.clear(this);
                if (resultsOpen) results(); else home();
            }).show();
    }
}
