# Form & Strength — offline Android companion

**1.3.0 · version code 4**. Daily food, protein, water, habits and strength routines in an offline Android app. Bundled web UI inside an Android WebView; native SQLite stores all records. No account, analytics, server, ads, runtime network access or requested permissions.

## Install as an update

1. In the current app, use Profile → Backup & restore → Back up to a file. In older versions the backup button is directly in Profile.
2. Download and open `Form-Strength-1.3.0.apk` on your Android phone. Tap **Update**, without uninstalling.
3. Open Profile to set your own starting measurements and optional goals. All fields are optional. Existing check-ins and meals remain intact.

Application ID `com.logan.formstrength` and the original signing key are unchanged. Android 8.0+ with a current System WebView is supported by the build. Device installation and native runtime behavior remain unverified; see `ANDROID_TEST_REPORT.md`.

## Daily flow

- **Today:** calories and protein logged, +250/+500 ml water, and direct toggles for no added sugar, protein shake, gym and mobility. Water and habit actions preserve other check-in fields. Tap again to undo a habit; edit the water total to correct it.
- **Food → Diary:** type foods and portions, review editable calories/protein, and save. Examples: `150g rice + 2 eggs`, `2 rotis and 1 cup dal`. 50 reference foods with Indian-food aliases and supported gram/ml/cup portions. Unsupported ingredients block saving rather than disappear.
- **Food → My foods:** create, edit, favourite, filter, log and remove saved portions. Enter a shake's actual product-label values once. Change servings when logging it. Save any reviewed meal as a repeatable portion. Exact saved names or `1.5 servings Demo whey shake` work in typed entry. Removing a saved food does not delete diary records.
- **Progress:** 14-day habit calendar, editable dates, weight chart, weekly calorie history, seven-day habit/water summary and check-in history.
- **Profile:** editable name, height, starting weight/waist, weight goal, optional water/protein goals, backup/import and privacy guidance. No hardcoded personal measurements. The app does not prescribe calorie targets.

A shake habit checkmark does not add calories; log the shake in Food. “Sugar-free” tracks **no added sugar**, not naturally occurring sugar in fruit or plain milk. Unknown protein stays absent and daily totals show when coverage is partial. A blank day does not mean zero intake. Calories and protein vary with recipes and portions.

## Storage and backups

`form_strength.db` lives in app-private internal storage. Updates with the same signing key preserve it. Uninstalling or clearing storage deletes records. Automatic Android backup is disabled; export a JSON backup before changing phones.

Schema version 2 adds `preferences` and `saved_foods` using non-destructive `CREATE TABLE IF NOT EXISTS`; existing tables/payloads stay intact. Format-2 backups include profile, saved foods, check-ins and food entries. Older exports remain accepted. Full-file validation precedes a single import transaction. Existing check-in dates, food IDs, saved-food IDs and a completed profile are retained. New saved foods merge; a missing/uncompleted profile can be restored. Exported JSON is readable, so choose a trusted destination. Older app versions may discard new fields when importing/re-exporting newer backups.

## Source layout

- `MainActivity.java`: resource allowlist, native bridge, system document picker, keyboard insets and Back handling.
- `LocalStore.java`: SQLite, profile/food validation, serialized quick updates and transactional imports.
- `app.js`: application state, navigation, check-in forms, loading and export.
- `food-diary.js`, `saved-foods.js`, `meal-estimator.js`, `food-reference.js`: food diary, library and reference parsing.
- `profile.js`, `habits.js`, `training.js`, `progress.js`, `startup.js`: focused screen controllers.
- `tracker-core.js`: pure nutrient/goal calculations used by tests.
- `tests/`: parser, nutrition, native canonicalization and backup-validation tests.

## Build and checks

Dependencies: Java 21, ECJ 3.40.0, Android API 36 platform, Build Tools 36.0.0, Python 3; no Gradle dependencies. Targets API 35, minimum API 26.

Official downloads:
- https://dl.google.com/android/repository/platform-36_r02.zip
- https://dl.google.com/android/repository/build-tools_r36_linux.zip
- https://repo.maven.apache.org/maven2/org/eclipse/jdt/ecj/3.40.0/ecj-3.40.0.jar
- Native JVM validation also uses https://repo.maven.apache.org/maven2/org/json/json/20240303/json-20240303.jar

Set `ANDROID_PLATFORM_JAR`, `ANDROID_BUILD_TOOLS`, `ECJ_JAR`. For the installable release also set `APK_KEYSTORE` and `APK_KEYSTORE_PASSWORD` securely; alias `form-strength`. Run `bash scripts/build.sh`. Never commit signing keys or passwords. Increment manifest version code/name and build output filename for each distributed release.

Run `npm test` for parser/nutrition tests. Set `JSON_JAR`, `ANDROID_PLATFORM_JAR`, `ECJ_JAR` and run `bash scripts/test-native.sh` for JVM validation tests. These do not execute the Android SQLite engine or system UI.

`npm run dev` starts a disposable browser preview. Its mock bridge stores demonstration records in browser localStorage, separately from the phone's database. It is not packaged in the APK. `/__phone?width=390` wraps the UI at a mobile width. Native import and Back/lifecycle tests require Android.
