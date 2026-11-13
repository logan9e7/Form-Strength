# 1.5.2 — single safe-area owner

- Native shell reserves the maximum status-bar, display-cutout and keyboard insets once; zeroes handled inset types before WebView dispatch to prevent duplicate padding.
- Compact 58px phone header, tighter date/energy spacing and reduced gap before movement.
- Same signer, package, data schema and permissions. Physical notch/keyboard/rotation testing remains pending.

# 1.5.1 — centered identity and redesigned Home

- Centered wordmark with balanced logo/profile controls.
- Compact energy card, paired meal/activity actions, movement section and expandable burn details.
- Smaller bottom navigation and consistent spacing.
- Data schema and permissions unchanged.

# 1.5.0 — reliable daily health context

- Steps, distance, active/total burn and sleep-session duration through Health Connect.
- Foreground auto-sync, seven-day refresh and persistent local cache with explicit timestamps/states.
- Synced burn by default; explicitly additional manual activities only.
- Shared date/timezone, missing versus zero states, compact progress, clearer food/settings hierarchy and consistent spacing.
- Same signing identity and additive schema-4 migration. Live Android/fitness-band tests remain pending; see ANDROID_TEST_REPORT.md.

# 1.3.0 — a faster daily companion

- Rebuilt Today around food/protein, direct water additions and four one-tap habits.
- Added personal setup; removed the previous user's hardcoded measurements and identity.
- Added optional per-serving protein, scaled totals, partial-coverage labels and 50 reference foods (32 include sourced protein).
- Added My foods: product-label/custom portions, favourites, search, repeat logging, edit/remove, and typed saved-food references.
- Added a 14-day habit calendar, accessible date labels and editable check-ins.
- Improved touch controls, sheet/form layout, responsive cards, empty states, contrast, focus styling and safe-area spacing. Split screen controllers into focused files.
- Schema 2 adds profile and saved-food tables without removing old data. Backup format 2 includes them; legacy backups remain supported.
- Android version code 4; same package/signing key. Back up and install over 1.0–1.2; do not uninstall.

Validation and remaining physical-device gates: `ANDROID_TEST_REPORT.md`. UI/UX findings and competition scope: `UI_UX_AUDIT.md`.

# 1.4.0 — Momentum theme, movement and optional connections

- New navy/electric-blue/coral theme, geometric logo and adaptive Android launcher icon.
- Eaten/active dashboard; Move sections for typed activities, routines and device data.
- Type explicit activities/durations, review MET/weight assumptions, save/edit/delete estimates. Show active versus gross burn; never infer burn from reps alone.
- Optional Gemini meal estimation with native personal-key setup, encrypted local storage, explicit send confirmation, structured response validation and reviewed AI provenance.
- Android 14+ platform Health Connect read integration for active calories, total burn and steps, with partial-permission/missing-data states. Source fitness app support is required.
- Schema 3 adds activity records; backup format 3 includes them. Existing records remain untouched, keys and device readings are excluded.
- New INTERNET and three optional health read permissions. Core logging remains offline; WebView network access stays blocked. Only an approved Gemini request uses native HTTPS.
- Version code 5; same package and original signing key. Back up and install as an update.

Live Gemini and Health Connect/band operation still require device/project testing; see `CONNECTIONS.md` and the appended 1.4 verification report.
