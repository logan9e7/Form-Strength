# Form & Strength 1.3.0 — verification report

Date: 12 November 2026. Package `com.logan.formstrength`; version code 4; minimum API 26, target 35, compile API 36.

## Automated and source checks

- PASS: offline parser regression suite, including quantities/fractions, supported units, unknown ingredient safeguards and alias precedence.
- PASS: nutrition totals, known zero versus unknown protein, loss/gain goal directions, all 50 reference foods, saved portion scaling, custom names containing separators, ambiguous-name refusal.
- PASS: native JVM validation for measurements, water bounds, protein bounds/scaling, profile validation, legacy backups, extended profile/saved-food backups and rejection before import.
- PASS: native quick-update merge preserves measurements and other habits; rejects overflowing water or invalid habit names.
- PASS: JavaScript syntax, unique HTML IDs, literal DOM references and script-file existence.
- PASS: Java/DEX compilation, APK packaging, ZIP alignment, APK v2/v3 signing and matching original signer. See build output for platform deprecation warnings; no compilation errors.
- Source review: schema 2 only adds profile/library tables, using `CREATE TABLE IF NOT EXISTS`; daily check-in/food tables stay intact. Actual Android migration is pending.

## Browser flow verification

Disposable preview, demonstration data only. Browser bridge uses localStorage and is excluded from the APK.

- Water +250/+500 → 750 ml; direct sugar-free toggle; saved/reopened check-in values.
- Optional setup → save → reload preserves name, baseline and optional water/protein goals.
- `150g rice + 2 eggs` → 350 kcal / 16.6 g protein; save as reusable meal.
- Product-label shake → 120 kcal /24 g per serving; 1.5 servings →180 kcal /36 g.
- Typed saved-shake portion gives the same scaled total.
- Roti/dal gives editable calories with unknown protein; daily total explicitly shows partial coverage.
- Unrecognized sauce blocks Save rather than omitting the ingredient.
- Training session B → full check-in → quick water → reopen retains weight, workout session and other habits. A disabled-button bug was found and fixed in this flow.
- Calendar and progress render; Today/Train/Food/Progress navigation works.
- Mobile layouts reviewed at 320, 390 and 430 px. No horizontal body overflow observed at the narrow/wide widths. Browser keyboard behavior is not an Android keyboard test.

## Android runtime status: BLOCKED

`adb devices` returned an empty device list. No emulator or `/dev/kvm` is available in this workspace. No physical-phone or emulator installation/run was performed. The following are **not verified**:

| Device test | Expected result |
| --- | --- |
| Export from 1.1/1.2, install 1.3 over it | Android offers Update; old records remain; new setup/library tables initialize |
| Force stop/reopen after each kind of save | Native SQLite restores check-in, water, profile, foods and library |
| Open keyboard in each form; rotate screen | Focused field and Save remain reachable; no blocked controls |
| Android Back on each tab and on clean/dirty forms | Returns home/closes form; unsaved edits require confirmation |
| Export then import using system file picker | File writes successfully; valid backup imports atomically; repeat import does not duplicate IDs |
| Import malformed backup | No partial records are added |
| Background/resume across midnight | Today refreshes to the phone's local date; previous data remains associated with its original date |
| Process killed while editing/picking a file | Saved data survives; unsaved form edits may need re-entry; app handles cancelled/restarted picker |
| Airplane mode | Every core logging flow works without network |
| Larger text/font/display settings and TalkBack | No clipped controls; date/status names and reading order usable |

Do not label this release physically tested or Play Store ready solely from compilation, signatures or browser checks. Back up before installation and perform the above gates on the intended phone.

# 1.4.0 — current verification (13 November 2026)

Package unchanged; version code **5**; minimum API 26 for core features, Android 14/API 34+ for the platform Health Connect integration. Original signing identity retained.

- PASS: existing food/nutrition regressions plus activity parsing, aliases, durations, assumptions, active/gross estimates and unknown speed/incline/load rejection.
- PASS: JVM activity canonicalization recomputes energy instead of trusting submitted calorie values; activity backup validation; Gemini result bounds/boolean validation; AI provenance; Health Connect platform cal → kcal conversion.
- PASS: Java/DEX/resource compilation and signed APK packaging. The final artifact's bundled files were compared with source and its native resource allowlist; original certificate and version increment checked.
- PASS: browser manual flow at 390 px — two typed activities → batch save → list/totals → edit existing record; dashboard updates. Offline meal logging and profile save still work. Reload preserved demonstration records through the preview bridge.
- PASS: 320/430 px review with no horizontal body overflow. New theme, geometric logo, Eaten/Active dashboard, Move sections and source/error labels reviewed.
- PASS: unknown activity details disable Save. Unconfigured AI shows setup instructions. Browser device controls are unavailable and metrics stay blank; no successful AI/device values are fabricated for preview.
- Source review: only INTERNET plus three read-only health permissions added; core WebView network loads remain blocked. Gemini native confirmation shows the exact selected text. No bundled key, no Health Connect → AI path, and no device-reading/key fields in exported backup payloads.

**BLOCKED / not claimed:** no API key was supplied; `adb devices` again returned an empty list. Successful Gemini requests, Android Keystore operations, Health Connect permission UI/reads/revocation, band-source compatibility, actual schema-2→3 upgrade, native keyboard/picker/Back behavior and adaptive-launcher rendering have not been exercised on a phone/emulator. These are release gates, not passing tests.

Additional device cases: deny one/two/all health permissions; revoke while a read is in flight; empty data versus a reported zero; only steps/only total burn; interval crossing local midnight/DST; key removal/replacement, quota/auth/model errors and offline Gemini failure; cancelled send confirmation; response arriving after meal text/date changes; backup roundtrip of AI provenance/activity entries. Refer to `CONNECTIONS.md` before public distribution. Use a protected backend/Firebase AI Logic setup for a publisher-managed AI service; do not distribute a shared API key.

# 1.5.0 — current verification (13 November 2026)

Version code 6, schema 4, original package/signing identity.

- PASS: food/activity/tracker suites and new health reconciliation cases: total/active fallback, extras counted once, zero versus absent, denied/unavailable metrics, date+timezone matching and stale/future timestamps.
- PASS: JVM cache merge preserves failed reading timestamps; no-data/denied clears values; additional activity defaults false; local windows handle DST 23-hour days, today clamping and future-date rejection.
- PASS: browser contract fixtures (explicitly simulated, not live Health Connect) at 320/390/430 px: complete, partial permissions, cached error, no-data and reported zero. Historical meal/activity saves retain selected day; 1900 synced + 116 extra = 2016; unmarked activities are excluded. Seven-day queued/read states and progress checked. No horizontal body overflow at inspected widths.
- PASS: default browser has unavailable device controls and never presents the simulated bridge as a live connection.
- UI fixes: shared selected date, Home naming, historical water label, original cached observation versus last attempt, compact empty charts, sleep card spacing, simplified food/settings sections and contextual icons.

Native build/signature/package checks are recorded in the release workflow. No physical device/emulator attached: actual schema migration/cache persistence, native permission grants/revocation, live source accuracy, keyboard/rotation/Back and upgrade preservation remain UNVERIFIED. Browser fixture coverage does not satisfy these gates. Android 14+ required for this integration.

## 1.5.1 Home redesign
PASS: centered brand geometry and no horizontal overflow at 320/390/430 browser widths; redesigned quick meal/activity buttons open their correct forms. Existing regression suites, JS syntax, unique IDs, signed Android compilation pass. Same signer, version code 7, no schema/permission changes. Physical Android testing remains pending.

## 1.5.2 safe-area correction
Native inset owner changed: root pads by the union/max of system bars, cutout and IME; handled types are zeroed on the returned WindowInsets.Builder, rather than returning original offsets to WebView. This retains inset dispatch and avoids applying safe-area offsets twice. Phone header reduced 70→58 CSS px and redundant movement spacing removed. Compile/package and browser layout checks cover source behavior only. Actual Pixel cutout, status-bar visibility, landscape, gesture/three-button navigation and keyboard transitions must be verified on-device.

## 1.6.0 Indian food and daily tracking

PASS: existing parser/nutrition/activity/health regressions; all enabled reference default portions; quantity-prefix/suffix and word quantities; 4 rotis=320, 4 rotis+1 cup dal=520; INDB gram/serving and protein scaling, source-review blocking, unknown ingredient refusal, names containing parentheses/and/with, saved recipes. Live localhost HTTP tests cover search/detail/estimate, invalid JSON/type/length/limits and unknown IDs.

PASS: browser demo at 390px typed meal → visible 4×80 + 1×200 →520 → save in diary and My foods; water 250+500=750ml; no-added-sugar mark and 1-day streak preserved through meal save/reload. Indian food finder Poha →2 servings shows 322kcal/6.6g protein; flagged cutlet disabled. Unmarking sugar preserves water. Historical date shows its own empty water/habit/food state. 320/430 layout inspected with no horizontal body overflow. Screenshot uses demo records, not user health data.

PASS: JS syntax/DOM IDs and references, JVM native validation, Android resources/Java/DEX/signing, matching original signer. No schema, native health permission or connection changes. API server is local implementation, not a deployed public service. Native SQLite upgrade, keyboard, notch and actual live Gemini/Health Connect remain unverified on-device; adb has no attached phone/emulator.

## 1.6.1 Home restoration and meal-entry clarity
PASS: existing regression suites, syntax and DOM reference checks. Browser demo confirms original Home order with energy/movement above water/habits; existing 750ml water and no-added-sugar mark survive the UI change. Typed 4 rotis instantly fills 320kcal without a calculate button; unsupported sauce blocks saving with a review message. Calculated total placed above optional protein/search/AI. Meal save/reload verified in browser. Native schema/signature/permissions unchanged. Physical phone/keyboard/notch tests remain pending.

## 1.6.2 / PR #1 review
Automated parser/nutrition/activity/health/API suites pass, including new supplemental quantities, whey label gate, true-zero creatine, corrected salad/corn portions. Native asset allowlist test covers every HTML script; supplement JS equals API JSON. API search/detail exposes new foods. Native JVM validation and signed Android compilation pass. Figma editable screen: https://www.figma.com/design/ep6tD2yuktlliMAoeCe71H (node 2:17); Roboto text, reusable rows, no flattened screen image. Browser manual checks include typed estimate, editable total, whey review, label fallback, saved meal and narrow/wide layouts. No phone/emulator attached: Android keyboard, notch, health sync and migration remain pending.

## 1.6.3 native architecture audit fixes

PASS: all six JavaScript/API regression suites; three JVM test entry points (backup snapshots/bounds/cleanup, SDK and Gemini configuration policy, native validation/import capacity); hermetic build contract including spaces, stale output cleanup and one D8 invocation. Signed Android compilation/resource/DEX packaging passed. APK version 1.6.3/code12 retains original signing certificate (SHA-256 afb0cda109180cd55a7283ca80ab4f0b4f8b8b1704ba35274c78bb69a6a52ae5). Source formatting check passed.

Schema 5 adds the activity-day index without deleting data. The full finding-by-finding resolution is in docs/AUDIT_RESOLUTION_1.6.3.md. No physical device/emulator attached: actual upgrade/migration, picker process recreation, SQLite rollback, old-Android class loading, native Back/keyboard/notch, Keystore failures and live Health Connect/Gemini remain pending device QA. JVM tests do not constitute Android instrumentation.

## 1.6.4 audit follow-up — 20 Nov 2026

PASS (integrated source, all commands exit 0): `npm test` (six suites), `bash scripts/test-native.sh` (three JVM entry points plus host SQLite and hermetic build checks), and `bash scripts/build.sh` (unsigned 1.6.4/code13). Build emits two existing deprecation warnings for WebView file-origin security setters; no compile errors. Root's signed release gate is recorded separately after integration review.

New assertions cover corrupt/empty/short/oversized encrypted-key state and IV length, model-only key retention policy, a raw Unicode backup below the character limit but above 5 MB UTF-8, and production activity-index SQL on a populated schema 4 table. Host build checks cover custom output basename, mismatched version guards, custom key alias and password environment references, path spaces, stale cleanup and one D8 call.

Automated evidence is recorded separately from Android device acceptance. The native runner now includes host SQLite checks executing the production activity-table/index SQL, and hermetic build orchestration. These checks do not run Android SQLiteOpenHelper, framework class loading, permission UI, Keystore or Activity lifecycle.

### Remaining Android release gates

Use signed releases and a dedicated test device/profile with synthetic records. Record OS/API, APK version/code, exact steps, observed result and a reviewed, relevant logcat excerpt for failures. Do not record credentials or personal health data.

| Gate | Execution and acceptance criteria | Status |
| --- | --- | --- |
| API 26–33 class loading | Launch the release, save food/activity offline, visit Device details and try sync/settings. Android 14+ requirement must appear without crash. | Pending Android target |
| API 34+ upgrade | Install 1.6.2, seed meals/activities/check-in and export. Install current release over it without uninstall/clear. Compare IDs/values; directly inspect schema 4→5 and activity_day where test access permits. | Pending Android target |
| Bounded backup round-trip | Export/import a valid backup near the 5 MB UTF-8 limit, with multi-byte text; confirm values and reject >5 MB. Rotate during preparation and system picker; repeat success and cancellation. | Pending Android target |
| Picker recreation/process death | Enable Don't keep activities and verify actual destruction while picker foreground, then finish export. Separately kill background process (not force-stop), complete picker and confirm valid JSON; restore developer setting. | Pending Android target |
| Back navigation | Clean modal Back closes only modal; dirty modal retains text on cancelled discard. Subtab Back reaches Home; Home Back finishes; relaunch retains records. Repeat with gesture Back on API 33+. | Pending Android target |
| Gemini encrypted key/model-only | Enter a real key privately in native dialog; obtain an approved estimate. Reopen settings, leave key blank, change only accessible model ID; restart and obtain second estimate. Remove key and confirm restart shows unconfigured. Corrupt/invalidated-key failures must ask reconnect without exposing secret. | Pending real key and Android target |
| Health Connect/cache revocation | On API 34+ grant read access and sync real synthetic provider records. Revoke one populated metric, resume/restart and verify no cached numeric value in Home/Device/Progress. Distinguish missing records from zero. | Pending Android target/provider |
| Bars, cutout, IME and destruction | Inspect status/navigation contrast and safe areas on API 26/30/35, portrait/landscape, keyboard open/close and gesture/3-button navigation. Leave during AI/backup/health work; no crash or late UI delivery. | Pending Android target |

No new Android runtime pass is claimed by this follow-up. Host compilation and pure policy tests do not establish that these release flows completed.

Lead final gate: `npm test && bash scripts/test-native.sh && bash scripts/build.sh` completed with exit0 on the combined1.6.4 tree, using the original signing identity. All6 npm suites,3 JVM mains, host SQLite schema check and hermetic build check passed. Signed APK v1.6.4/code13 verified with v2/v3 signatures and original SHA-256 certificate `afb0cda109180cd55a7283ca80ab4f0b4f8b8b1704ba35274c78bb69a6a52ae5`. Exact concise output: docs/QA_GATE_1.6.4.txt. This does not change any pending Android acceptance row.

## 22 November2026: PDF iteration prerequisite

Read the supplied two-page fitness sketch and recorded scope in docs/ITERATION_1_FROM_FITNESS_PDF.md. Account sign-in is excluded; onboarding remains local. Production iteration1 implementation is held behind the eight runtime gates at the user's request.

The previous API34 software-emulator attempt reached adb online and later package-manager availability, but initial install failed while package service was absent and the next readiness attempt timed out. No instrumentation result or app-flow pass was produced. The prior emulator execution session is now unavailable; current adb probe lists no device. Sanitized evidence: docs/qa-evidence/2026-11-20-emulator-attempt.txt. All eight acceptance rows remain pending, with live-key/provider prerequisites unchanged.

Added a framework-only, same-signer instrumentation APK harness under tests/android, built by scripts/build-os-qa.sh. It compiles separately against the unmodified release and introduces no production dependency or permission. Host npm/native regressions and test-APK compilation passed during preparation. Its seed/upgrade/capacity/backup/framework stages remain UNEXECUTED on Android. See docs/ANDROID_OS_QA.md; do not install the harness on a personal profile with a real Gemini key.

## 1.7.0 — three PDF implementation slices (22 November2026)

The latest user instruction authorized implementation despite the unmet baseline device gates. Those gates remain unpassed; this section does not overwrite them.

Implemented: local Daily Goal/You/onboarding, guided sessions with separate active/rest seconds and equipment alternatives, opt-in native inexact reminders and duplicate-safe log actions, consent-gated native meal-photo Gemini pipeline and user-priced budget filters. Original signing identity, package, schema5 and dependency-free build retained. Version14/1.7.0 adds notification/boot permissions and a private URI-grant capture provider; no camera/storage/exact-alarm/DND-bypass permission.

PASS: npm six regression suites; four JVM entry points including onboarding/profile/price backup validation, structured timing, camera token path checks and local reminder/DST scheduling; host SQLite and build contracts. Android resources/Java/DEX/signing verified. Browser smoke with synthetic disposable preview at390px: all local setup steps, optional BMR explanation, meal320kcal saved, guided active/break recording, budget price persistence and max-price exclusion, reload persistence, cancelled-discard Escape on setup/player. No page errors; no horizontal overflow320/390/430. Test includes preservation of existing1500-character check-in notes when recording a session. Preview bridge is not Android SQLite.

Installed system Chromium could not launch because its process-singleton socket was restricted. A standalone Chrome headless shell from Google's testing distribution ran the browser smoke successfully. Reproducer: tests/iterations-browser.cjs with BROWSER_EXECUTABLE set to that development binary; no browser/Playwright code enters the APK.

PENDING Android acceptance: actual update/data preservation onAPI26–35, physical notch/keyboard/Back, POST_NOTIFICATIONS allow/deny/channel-disabled/DND, inexact alarm delivery/reboot/timezone/OEM battery handling, repeated log actions and transaction receipts, external camera/no-handler/cancel/large or rotated image, activity recreation and URI grant cleanup, real Gemini image request and permission-revoked Health Connect cache. A camera session restored after activity recreation cancels and asks for retake; it does not automatically resend a photo. API key and source provider records must be supplied by the tester. No new on-device PASS or Play release approval is claimed.
