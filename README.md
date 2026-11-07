# Kay's Friend Suggestion Inspector (Android)

A free, local-first Android proof-of-concept that **observes friend suggestions visible on your screen** in TikTok, Snapchat and Facebook, after you explicitly choose the social app and turn on a time-limited scan. It never requires account passwords and does not use an external backend.

## What works

- A platform picker that opens the official social app **already signed in on your device**.
- A user-consented Accessibility service that only reads the selected app while a 15-minute session is active. It does not navigate or click on your behalf.
- Heuristic detection of names next to Follow/Add controls on visible suggestion screens (requires the social app to expose that text to Android Accessibility).
- A local history with first/last observed timestamps, count and platform.
- Optional exact display-name matching against the phone's contact **names** (not phones/emails).
- Possible cross-platform name/handle matches, clearly marked unverified.
- Readable report, manual Stop, automatic session expiry, and Delete All.

## What **does not** work / cannot be inferred

- It cannot reach inside social apps' private storage, auth tokens, recommendation algorithms or server-side suggestion logs, nor enumerate all suggested accounts while those lists are not visible.
- It **cannot tell the definitive reason** an individual account was suggested. A name match does not prove a phone number or email address caused it.
- It cannot infer past physical proximity, a person's location, an encounter time/date, or who owns a nearby device from a social suggestion.
- Cross-platform same-names or handles might belong to different people.
- The extraction logic is a conservative **prototype**. Some versions of TikTok, Snapchat and Facebook expose no suitable Accessibility text or change their layouts; those screens may produce zero results or occasional inaccurate names. Review each result before treating it as evidence. The app has not been tested against installed production social apps in this environment.
- The service cannot identify whether you are actually signed in: it simply opens the installed official app and relies on its existing account session.

## Installation (through GitHub Actions)

1. Create a new GitHub repository (for example `Kays-Friend-Suggestion-Inspector`).
2. Upload **all files and folders** from this project, including `.github/workflows/build-apk.yml` (hidden folders may require Git or desktop file handling).
3. In your repo, open **Actions > Build Kay's Suggestion Inspector APK > Run workflow** if an automatic push build has not already started.
4. Wait for a **successful** build. Open that workflow run and download the `Kays-Friend-Suggestion-Inspector-APK` artifact ZIP.
5. Extract `app-debug.apk` from the ZIP, copy it to your Android device if needed, open it, and approve installation from that trusted source.
6. Launch the app. Tap **Scan TikTok**, **Scan Snapchat**, or **Scan Facebook**. Read and consent to the Accessibility disclosure, enable Kay's scanner in Android's Accessibility settings, then return and choose the platform again.
7. In the social app, open **Suggested accounts / Find Friends / People You May Know** and scroll; switch back to Kay's inspector to see captured results.
8. Optionally allow READ_CONTACTS permission to compare names. Turn off the Accessibility service in Android settings when you are finished.

**No release APK is included.** The supplied GitHub workflow compiles a free debug APK. A local Android SDK/Gradle toolchain or GitHub Actions is required to compile it; this source package has not been compiled in this environment.

## Privacy and permission details

The Accessibility feature is **not** an accessibility aid for disabilities. It must not claim `isAccessibilityTool=true` if you publish it. For Play Store publication, provide a prominent in-app disclosure, consent, a policy declaration, and a compliant Data Safety / privacy policy. The app includes an opt-in dialog and the service is limited to the selected package and user-started window. No INTERNET permission is declared; no analytics or third-party libraries are bundled. All observations are stored in app-private SharedPreferences with Android's normal app sandbox. You can delete them in-app.

For contacts, it reads **display names only** after explicit permission. The Accessibility service does not intentionally collect messages, passwords, or non-selected apps, although Android's Accessibility permission is broad; grant it only if you understand this access.

## Stack

- Plain Java 17, Android Gradle Plugin 8.7.3, Gradle 8.10.2, compileSdk 35, targetSdk 34, minSdk 26.
- No external libraries, cloud accounts, paid services, trackers, screenshots, or network endpoints.
- GitHub Actions builds `:app:assembleDebug` and uploads the APK as a workflow artifact.

## References / platform behavior

- TikTok: https://support.tiktok.com/en/account-and-privacy/account-privacy-settings/suggested-accounts
- Snapchat: https://help.snapchat.com/hc/en-us/articles/35382503314580-What-is-Find-Friends
- Android Accessibility: https://developer.android.com/reference/android/accessibilityservice/AccessibilityService
- Google Play Accessibility Policy: https://support.google.com/googleplay/android-developer/answer/10964491