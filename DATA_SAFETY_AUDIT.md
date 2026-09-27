# Google Play Data Safety Audit

**Application:** Islamic Challenge (التحدي الإسلامي)  
**Package:** `com.aistudio.islamicchallenge.qzwv`  
**Version:** 1.0.0 (versionCode 1)  
**Date:** September 24, 2026  

---

## Executive Summary
This audit rigorously examines the codebase, manifest declarations, libraries, and runtime architecture of **Islamic Challenge**.

- **Network Access:** `android.permission.INTERNET` is **NOT** declared in `AndroidManifest.xml`.
- **Remote Telemetry:** Zero analytics, zero ad networks, zero crash-reporting servers.
- **External Sharing:** Zero data transmitted off the device.
- **Local Storage:** 100% on-device local SQLite (Android Room Database v3).

---

## Play Console Data Safety Questionnaire Mapping

| Question on Play Console | Response |
| :--- | :--- |
| **Does your app collect or share any user data?** | **No** |
| **Is all user data collected by your app encrypted in transit?** | **N/A** (No data is transmitted over the network) |
| **Do you provide a way for users to request data deletion?** | **Yes** (Built-in In-App Reset Progress + OS app data wipe) |

---

## Detailed Data Safety Matrix

| Data Type | Collected? | Stored? | Shared? | Storage Location | Purpose | Required / Optional | Third-Party Access |
| :--- | :---: | :---: | :---: | :--- | :--- | :---: | :---: |
| **Name / Identity** | **NO** | **NO** | **NO** | None | N/A | N/A | None |
| **Email Address** | **NO** | **NO** | **NO** | None | N/A | N/A | None |
| **Phone Number** | **NO** | **NO** | **NO** | None | N/A | N/A | None |
| **User IDs / Account** | **NO** | **NO** | **NO** | None | N/A | N/A | None |
| **Precise Location** | **NO** | **NO** | **NO** | None | N/A | N/A | None |
| **Approximate Location** | **NO** | **NO** | **NO** | None | N/A | N/A | None |
| **Financial / Payment** | **NO** | **NO** | **NO** | None | N/A | N/A | None |
| **Photos / Media** | **NO** | **NO** | **NO** | None | N/A | N/A | None |
| **Contacts / SMS** | **NO** | **NO** | **NO** | None | N/A | N/A | None |
| **Audio / Microphone** | **NO** | **NO** | **NO** | None | N/A | N/A | None |
| **Advertising ID** | **NO** | **NO** | **NO** | None | N/A | N/A | None |
| **Crash Logs / Diagnostics** | **NO** | **NO** | **NO** | None | N/A | N/A | None |
| **Game Progress (XP, Coins, Level)** | **NO** | **YES** (Locally) | **NO** | On-Device SQLite (Room) | App functionality & progression | Required for gameplay | None |
| **Category Progress & Stats** | **NO** | **YES** (Locally) | **NO** | On-Device SQLite (Room) | Performance analytics for user | Required for gameplay | None |
| **Unlocked Achievements** | **NO** | **YES** (Locally) | **NO** | On-Device SQLite (Room) | Game rewards & milestones | Required for gameplay | None |
| **User Preferences (Sound, Theme, Lang)** | **NO** | **YES** (Locally) | **NO** | On-Device SQLite (Room) | UI Customization & Accessibility | Required for gameplay | None |

---

## Technical Audit Findings
1. **Manifest Inspection:**
   - Permissions declared: `android.permission.VIBRATE` only.
   - Zero dangerous or privacy-sensitive permissions.
   - Zero internet permissions.
2. **Dependencies Inspection:**
   - No advertising SDKs (AdMob, Unity, AppLovin, etc.).
   - No tracking or analytics SDKs (Firebase Analytics, Mixpanel, Adjust, etc.).
3. **Database Architecture:**
   - Room Database v3 stores entities: `player_profile`, `category_progress`, `unlocked_achievements`, and `user_settings`.
   - File stored in application internal sandbox directory (`/data/data/com.aistudio.islamicchallenge.qzwv/databases/`).
4. **Data Deletion Mechanism:**
   - Supported via `PlayerRepository.resetAllProgress()`, which executes atomic deletion of profile metrics, category progress, and achievements.

---

## Conclusion
The application meets the highest standards for Google Play Data Safety, allowing the developer to declare **"No user data is collected or shared"** in the Google Play Console declaration form.
