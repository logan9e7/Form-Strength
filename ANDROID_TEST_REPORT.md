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
