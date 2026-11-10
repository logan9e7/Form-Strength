# 1.1.0 — simpler meal logging

- Type a meal description and review an editable offline calorie estimate.
- 35 selected common-food references, including Indian prepared foods, with aliases, fractional servings, cups, bowls and supported gram/ml portions.
- Show recognized foods, assumed portions and source basis; block saving when part of the input is unresolved.
- One-tap recent-meal fill for repeat logging, with manual calorie entry still available.
- Preserve estimate provenance in records and backups. No SQLite schema change or new permissions.
- Android version code 2; same package and signing key as 1.0.0. Install over the current app, without uninstalling. Back up first.

Validation: parser test suite, native validation and backup metadata tests, Java/DEX compilation, ZIP alignment, manifest and v2/v3 APK signatures pass. The new meal UI was not browser-verified because preview access was blocked; physical Android testing remains unverified.
