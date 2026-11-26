# Declaration worksheet — review before submitting

This is a source-based draft, not a submitted certification. No app account, advertising, analytics or publisher-operated backend appears in this build. Do not declare “no data collected” while optional Gemini requests can leave the device.

## Data flows

| Data | Local use | Leaves device? | Retention/deletion |
| --- | --- | --- | --- |
| Optional profile, measurements, food, water, habits and workouts | Diary, estimates, progress and routine preferences | No automatic upload; selected JSON export includes manual records | App-private SQLite; clear storage/uninstall deletes local data; user must delete separate exports |
| Health Connect active/total calories, steps, distance, sleep-session duration | Foreground display and cached daily progress | Not sent to Gemini or app servers; excluded from exports | Local snapshots; revoke permission then refresh to remove metric cache, or clear cache |
| Approved typed meal | Optional calorie/protein estimate | HTTPS to Google's Gemini API after native consent | Google API terms/project settings apply; do not claim zero retention |
| Approved meal photo | Optional calorie/protein estimate | Re-encoded/resized JPEG to Google after native consent | Private capture removed after preparation/cancel; abandoned captures pruned after 24h when cleanup runs; Google processing separate |
| User-supplied Gemini API key | Authorize user's requests | Request header to Google's API | Keystore-encrypted local preferences; excluded from JSON backup; removable through settings |
| Reminder configuration | Local inexact alarms/notification actions | No automatic upload | App-private settings; follows system permission/channels/DND |

## Data safety review

- Review Health and fitness information for typed meal content; Photos for approved camera input; assess any other relevant data type from actual accepted input and key/account use.
- Optional collection for app functionality applies to opt-in Gemini requests. HTTPS encrypts transmission.
- Do not mark ephemeral processing without checking Google's current API terms and the developer project's billing/settings.
- Evaluate Google's role and applicable sharing exceptions from actual terms. Do not infer “not shared” solely from consent.
- Local-only processing is different from off-device collection. Data safety covers optional integrations too.
- No app account exists, so account-deletion functionality is not applicable. Local data deletion and independently exported copies are described in-app. Do not promise deletion from Google's systems through local deletion.

## Health apps and permissions

Proposed health features: activity/fitness tracking and nutrition/weight management. Review the exact categories shown in Console.

| Read permission | User benefit |
| --- | --- |
| READ_ACTIVE_CALORIES_BURNED | Display provider-reported active energy separately from total energy/manual estimates |
| READ_TOTAL_CALORIES_BURNED | Display daily total energy including resting energy as the default device baseline |
| READ_STEPS | Display daily step totals from a user-approved provider |
| READ_DISTANCE | Display daily distance from a user-approved provider |
| READ_SLEEP | Display session duration, with the documented ending-date window |

No writes, heart rate, routes, background reads or historical-extension permission requested. Health Connect is Android 14+ in this implementation. Provide truthful review instructions/video if Console asks; grant/deny/revoke and populated sync are still device-test gates.

## Other app content

- Ads: no ads found.
- App access: core local features require no login. Optional AI requires the reviewer's own eligible Google API project/key; disclose this limitation rather than claim all optional features work without credentials.
- Pricing, countries, target audience, developer identity/contact and content rating: developer must confirm. No invented age, location, registration or financial details.
- Do not claim disease diagnosis/treatment, guaranteed weight loss or exact calorie accuracy.
