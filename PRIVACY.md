# Privacy overview (source-project draft)

This application is designed for on-device use only. It does not request INTERNET permission, send data to any server, or use third-party analytics.

**User started scans:** The user enables Android Accessibility settings and explicitly starts a 15-minute scan for one selected app (TikTok, Snapchat or Facebook). The service reads only screen-accessible text to extract visible suggestion names and handles, then stores platform, first and last observation timestamps and count locally. It does not capture content from other apps during a scan, navigate within the selected app, or read authentication credentials.

**Optional contacts:** With a separate Android permission, it reads local contact display names for exact-name comparison, without reading phone numbers or email addresses.

**Storage and deletion:** App-private on-device preferences; use 'Delete all saved observations' in the app or uninstall it to remove the data. Users can disable the Accessibility service in Android settings.

**No inference:** Suggestion causes, cross-platform identity, actual personal proximity and encounter history are not established by this tool. The results interface explicitly distinguishes evidence from unverified possibilities.

If published, adapt this document to applicable requirements, complete the Google Play Accessibility declaration and ensure a complete user-facing privacy policy. This is a project draft, not legal advice.