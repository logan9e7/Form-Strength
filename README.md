# Form & Strength — offline Android app

An installable Android application with bundled UI and native SQLite storage. No login, backend, analytics, runtime network access, Neon or Render. The APK does not declare INTERNET or storage permissions.

## Install
1. Download `Form-Strength-1.0.0.apk` on your Android phone.
2. Open it in Files or Downloads. If Android asks, allow that downloader/file manager to install this app, then select Install.
3. Open **Form & Strength** from the launcher. No browser installation or account is required.

Android 8.0+ with a current Android System WebView is supported by the build. This build is intended for the user's Pixel 8. It has been compiled and signature-verified but has not been installed on a physical device or emulator in this environment.

## Features
- Daily weight, waist, steps, sleep, gym, mobility and food-habit check-ins.
- Gym A/B/C routines and a gentle mobility routine.
- Food diary: portions, calories, daily totals, meal grouping, history and edit/delete.
- Weight trend, waist summary and seven-day calorie history.
- Native Android back navigation, safe areas, resize behavior for the keyboard and native confirmations.
- System document-picker export/import. No broad storage permission.

## Where records live
`form_strength.db` is stored in the app's private internal storage. Other apps cannot directly read it on a normal, non-rooted phone. Records persist across closing/reopening the app and across compatible signed updates. They are removed when you uninstall or clear app storage. Automatic Android app backup is disabled. Use Profile → **Back up to a file** before changing phones or uninstalling. The exported JSON is a readable backup; choose a location you trust. A chosen cloud document provider may upload that exported file at your discretion.

## Move records from the web app
1. In the existing website, open Profile → Export all my records (or Progress → Export).
2. Download its JSON export to the phone.
3. In the APK, open Profile → Import a backup / web-app export.
4. Choose the JSON file and confirm the import summary.

Import validates the whole file first. New entries are added in one SQLite transaction. Existing check-in dates and food IDs are preserved unchanged; repeated imports do not duplicate them. Import never executes file contents. Profile baseline is the personalized baseline of this build, not imported account settings.

## Source layout
- `app/src/main/java/com/logan/formstrength/MainActivity.java`: Android WebView shell, strict bundled-resource routing, native document picker, back navigation.
- `LocalStore.java`: SQLite schema, CRUD, field validation and transactional import.
- `app/src/main/assets/`: bundled markup, styles, JS and icons. The JS calls the native bridge; it does not call a server.
- `tests/ValidationTest.java`: runnable JVM tests for native validation/import parsing.
- `scripts/build.sh`: reproducible SDK-tools build (Java → DEX → aligned/signed APK).

This repository contains the offline Android edition. The earlier web app remains separate. The UI includes the personalized starting profile; no saved check-ins, food-diary records, database files or signing secrets are included in the repository.

## Build
Dependencies: Java 21 runtime, Eclipse ECJ 3.40.0 compiler JAR, Android platform API 36, Android Build Tools 36.0.0, Python 3. No Gradle dependencies.

Official artifact URLs:
- https://dl.google.com/android/repository/platform-36_r02.zip
- https://dl.google.com/android/repository/build-tools_r36_linux.zip
- https://repo.maven.apache.org/maven2/org/eclipse/jdt/ecj/3.40.0/ecj-3.40.0.jar

Set `ANDROID_PLATFORM_JAR`, `ANDROID_BUILD_TOOLS` and `ECJ_JAR` to their extracted local paths, then run `bash scripts/build.sh`.

For a signed, update-compatible release also set `APK_KEYSTORE` to the preserved PKCS12 keystore and `APK_KEYSTORE_PASSWORD` securely in your environment. Do not put either the key or its password in this repository. The key alias is `form-strength`. A separate private signing backup accompanies this delivery. Increment the manifest's versionCode/versionName and rebuild before distributing updates. Installing updates with a different key will fail; uninstalling first removes local data.

## Verification completed
- Native Java compiles against API 36; D8 compiles DEX with minimum API 26.
- Android packaging, four-byte ZIP alignment and APK v2/v3 signature checks pass.
- APK manifest confirms no requested permissions and a launcher activity.
- Native tests check numeric ranges, date validity, canonical fields, fractional calories and full-file backup validation.
- Bundled JavaScript parses. UI is based on the previously tested mobile layout (360–430 px).
- No Android device/emulator runtime test was available. Native rendering, keyboard, file-picker and lifecycle behavior still need first-device verification.
