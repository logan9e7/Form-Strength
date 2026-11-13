# Optional connections — 1.4.0

## Gemini meal estimates (personal-project connection)

1. Create/restrict your own API key in Google AI Studio. Use a dedicated Gemini-only key and check your project's quota, pricing and billing controls.
2. On the phone: Profile → Connections → Set up Gemini. A native Android dialog accepts the key and a configurable model ID. The default, `gemini-3.5-flash-lite`, is listed in Google's current model catalog; availability and quotas depend on the project.
3. In Food → Type a meal, describe the meal/portion and tap Ask Gemini. If no key is set, the Android app offers the same setup dialog without discarding meal text.
4. A native confirmation names Google, shows the exact meal text and explains API usage. Only that text and a fixed nutrition-estimation instruction/schema are sent. No weight, profile, saved diary, activity or Health Connect records are sent.
5. Review calories, protein and assumptions. Use the estimate, adjust the manual fields and Save. AI does not automatically log meals or prescribe targets.

The key is encrypted with an Android Keystore AES-GCM key in app-private preferences; it never goes through JavaScript, source control, logs or JSON backups. A key being saved is not proof that the project/model is authorized. Request failures, incomplete results, malformed JSON, schema failures and unrecognized food return a visible error/clarification. Offline references/manual entry remain usable. There is no silent switch to made-up successful AI responses.

Requests use HTTPS POST to the hardcoded `generativelanguage.googleapis.com` endpoint, key in a request header, bounded response size, connection/read timeouts and no redirects. Structured JSON output is validated again locally. Reviewed AI provenance survives records, saved foods and backups.

Official docs:
- https://ai.google.dev/gemini-api/docs/models
- https://ai.google.dev/gemini-api/docs/structured-output
- https://ai.google.dev/gemini-api/docs/api-key

**Public-release gate:** this is a user-supplied personal-project key flow, not a shared production key solution. Never compile or distribute a publisher's Gemini key in the APK. Google recommends a backend proxy or Firebase AI Logic with App Check for a managed production integration. That requires provisioning the Google/Firebase project, abuse limits and current privacy/store disclosures. Those services have not been provisioned here.

## Health Connect / fitness band

Android 14+ only for this build. Move → Device → Connect opens Android's permission screen. Users can grant any subset of active calories, total calories and steps. Manage access opens OS controls. Sync reads a selected day; missing values stay blank. The device UI does not pretend a band or Google Fit account is connected merely because permissions exist.

Sync the band using its companion app and enable its Health Connect sharing. A source may not support all fields. There is no direct Bluetooth connection or OAuth login to every band vendor in this release. Older Android versions show the feature as unavailable and retain manual logging.

The permission-usage activity/alias is declared in the manifest. It describes the data types, purpose, local handling and how to revoke access. Readings are not written to the application's database, shared with AI or included in backups. No historical/background permission is requested; older-day availability remains subject to Health Connect's normal access window.

Official docs:
- https://developer.android.com/health-and-fitness/health-connect/get-started
- https://developer.android.com/reference/android/health/connect/HealthPermissions

## Verification limits

No Gemini API key was supplied and no phone/emulator was attached. Native compilation, schema/unit validation and browser/manual-log testing are completed; successful live AI responses, encrypted-key operations on-device, Health Connect permission grants and actual band readings remain unverified. Do not label this integration live-tested or Play Store ready. Play Console health-permission declarations, privacy/data-safety updates and real-device permission-revocation/upgrade tests remain release gates.
