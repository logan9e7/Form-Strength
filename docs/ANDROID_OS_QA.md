# Same-signer Android OS checks

This separate framework-only instrumentation APK tests the unmodified signed release. It adds no production runtime dependency and does not enable release debugging. Use only a **dedicated emulator/profile with synthetic data**. The seed stage adds records; framework stage temporarily saves/removes a dummy Gemini credential. Do not run it on a personal installation with a real Gemini key.

Build `scripts/build-os-qa.sh` with the documented Android tool variables and the original release signing environment (`APK_KEYSTORE`, `APK_KEYSTORE_PASSWORD`, optional alias/key password). The output is `build/android-os-qa/qa.apk`, not a public app update.

1. Install original signed 1.6.2 and the QA APK with `adb install -r`.
2. Run `adb shell am instrument -w -e stage seed com.logan.formstrength.qa/.OsGateInstrumentation`. Require all assertions and instrumentation result -1 (Activity.RESULT_OK). This seeds through production validate/import code and persists canonical records.
3. Install signed 1.6.4 **without uninstalling or clearing data**.
4. Run stage `upgrade`. Require schema5, `activity_day(day)` and identical canonical check-in/food/activity payloads.
5. Run stage `backup` for actual Android private-file 5MB snapshot roundtrip and overflow rejection. This does **not** exercise the system document picker, valid near-limit JSON import, rotation or process death.
6. Run stage `framework` for real WebView initialization, synthetic AndroidKeyStore encryption/decryption and model-only persistence, synchronous removal and clean-modal/Home hardware Back. This is **not** a live Gemini API call, dirty-form/gesture Back, Health Connect provider/revocation or all inset configurations.

A stage with a `failure` field, timeout or instrumentation result0 is a failure/incomplete run, never a PASS. Preserve exact output, OS/API/build identity and relevant sanitized crash log. Successful command transport or APK install alone is not a passing flow.
