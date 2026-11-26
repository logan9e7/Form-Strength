# 1.7.0 — three PDF iterations

- New Daily Goal home, local-only onboarding with optional resting estimate, Workout/Food transitions, top Back and You review/settings.
- Guided workout player, immediate equipment alternatives, active/break timers, pause on background and saved session timing.
- Opt-in native water/walk/wind-up channels and inexact scheduling; Android13+ permission; duplicate-safe local log actions; wind-up opens You.
- Secure camera capture, bounded EXIF-normalized metadata-free image, explicit Gemini sharing consent and editable meal review. Interrupted/restored camera requires retake.
- User-entered INR serving prices and cost/protein-value budget filtering with unknown nutrients kept unknown.
- Existing SQLite schema5/data/signing identity retained; old editors preserve optional profile, price and timing fields. No sign-in or production dependency added.

Implementation is available for device QA. Prior eight Android acceptance gates remain unpassed; no live camera, Gemini, permission delivery, reboot alarm or real Health Connect success is claimed.

# 1.6.4 — audit follow-through

- Keep API30 insets/system-bar and API33 Back symbols in guarded helper classes; older Health Connect calls now return unavailable without throwing. Add narrowly scoped Health Connect visibility queries while keeping exact package IDs for hidden origins.
- Preserve pending import-picker state, honor an explicitly unhandled JavaScript Back event, and guard JavaScript delivery during shutdown. Cancel active Gemini work and dismiss its settings dialog when the activity closes.
- Validate saved Gemini ciphertext/IV and require a usable existing key before a model-only change. Reading a lost key never generates a replacement; corrupt/invalidated keys prompt reconnection. Clear remains synchronous.
- Abort failed SQLite writes instead of reporting partial success. Saved-food import probes only incoming unique IDs inside the transaction; backup validation enforces UTF-8 bytes as well as character bounds.
- Add safe output-name and version guards, optional separate key password, ciphertext/UTF-8 regressions and host SQLite migration/index checks. Core architecture remains zero Gradle and zero third-party runtime dependencies.

Device gates remain separate from host checks: API26–33 class loading, Android SQLiteOpenHelper upgrade, real picker recreation, system Back/insets, Keystore and live integrations still require observed Android results.

# 1.6.3 — native reliability audit fixes

- Isolate Health Connect framework classes behind an API34 guard and stop late callbacks.
- Prepare/export/read/import backups on the worker executor. Keep pending export snapshots in app-private cache with a small saved-state token across picker recreation.
- Harden AI confirmation/busy cleanup, native Back fallback and shutdown ordering.
- Allow model-only Gemini changes; persist key removal before reporting success.
- Schema5 adds an activity-date index; unique saved-food additions respect the 1,000-item cap inside import/save transactions.
- Clean build intermediates, quote source paths and run D8 once. New backup/configuration/build contract tests.
- Same package/signing identity; physical-device validation remains pending.

# 1.6.2 — reviewed meal entry refinement

- Reviewed PR #1: retain 16 supplemental references and product-label safeguards. Correct salad model, raw-corn label and cauliflower portion.
- Fix native asset allowlist so supplement foods load in Android; keep API lookup and estimation catalogs aligned. Add parity/asset-contract and API regressions.
- Figma-designed meal entry: one editable total, concise portion breakdown, optional nutrition details, navy/cyan actions and responsive typography.
- Preserve the energy-first Home layout and existing records. Same package, signer and schema. Physical Android testing remains pending.

# 1.6.1 — restore energy-first Home

- Restore the 1.5.2 Home arrangement: energy and movement first, then water and compact habits. Remove prominent sugar card without deleting habit/water records.
- Keep the expanded offline catalog/API and live typed-meal calculations. Put calculation status and editable total directly below the entered meal; food search and Gemini remain secondary.
- Same signer, package, schema and permissions; version code 10.

# 1.6.0 — Indian meals and daily priorities

- Expanded catalog to 1,070 references: 1,014 INDB recipes, 50 legacy foods, 6 transparent ingredient models. 144 suspicious source references require review.
- Indian food/portion finder; visible quantity × per-unit calories; exact quantity scaling, source attribution and unknown-food safeguards.
- Local REST reference API and canonical stored catalog; existing SQLite My foods API retains personal recipes. Optional reviewed Gemini estimates unchanged.
- Prominent water and no-added-sugar cards, one-tap tracking, 7-day count and selected-date streak; no inferred sugar status from meal text.
- Same package/signing identity, version code 9; schema and health permissions unchanged.

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

## 1.7.1 — Play upload candidate (26 November 2026)

- Target API 36; version code 15; original application/signing identity retained.
- Add signed Android App Bundle build path without Gradle.
- Correct in-app and Health Connect privacy wording for optional Gemini photo uploads, Google processing and local/exported data deletion.
- Add Play listing, privacy-policy draft, data-flow/declaration worksheet and release/device gates.
- Packaging candidate only: no new Android runtime pass, Console submission or publication is claimed.
