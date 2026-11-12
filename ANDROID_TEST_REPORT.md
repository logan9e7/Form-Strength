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
