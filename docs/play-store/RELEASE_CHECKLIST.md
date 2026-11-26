# Play release checklist — 1.7.1 / code 15

Prepared 26 November 2026. Package: com.logan.formstrength. Minimum API 26; target/compile API 36.

## Completed in this candidate

- API 36 target and incremented release identity.
- Zero-Gradle signed AAB builder using AAPT2 protobuf resources, bundletool and jarsigner.
- In-app and Health Connect rationale disclose both approved text and photo Gemini uploads.
- Local deletion, exported backup handling and Google processing distinctions disclosed.
- Proposed store listing and declaration worksheet included.

## Console/account gates — not completed

- Confirm developer account verification and availability of this package name.
- Enter public developer/support email; host final privacy policy at a public non-geofenced HTML URL and test it.
- Create app if absent. Enrol in Play App Signing. To preserve updates for existing sideloaded users, choose the supported workflow to import the ORIGINAL app-signing key before accepting a Google-generated different app signer. An AAB upload signature alone does not establish the signer of Play-delivered APKs. Do not place the private key in the app, repository, release archive or store listing.
- Upload the AAB to INTERNAL TESTING first and inspect Play validation/pre-launch report.
- Complete Health apps, Health Connect permission justifications, Data safety, ads, audience, content rating and app-access declarations using the worksheet and actual behavior.
- Supply listing graphics and actual device screenshots. Do not present browser mocks as verified Android screenshots.
- If this is a personal developer account created after 13 November 2023, check Console's production-access requirement: at least 12 continuously opted-in closed testers for 14 days, then apply for production access. Internal testing alone does not meet the closed-test gate.
- Submit production only after gates pass. No submission, approval or publication is implied by a local build.

## Device evidence — still required

Use dedicated test profiles with synthetic records. Record model, OS/API, version/code, source commit, observed results and relevant sanitized logs.

1. API 26–33: launch/manual food and activity; unavailable Health Connect must not crash.
2. API 34–36: update with original signer, retain meals/profile/habits, backup/import and schema; confirm blank vs zero.
3. API 36 phone and large screen: edge-to-edge, portrait/landscape, resizing, keyboard, Back/modal behavior.
4. Health Connect: grant subset, sync test-provider data, revoke populated metrics and confirm cached values clear.
5. Reminders: notification denial/grant, disabled channels, DND, reboot/timezone and duplicate-safe actions.
6. Camera: no handler/cancel/large/rotated image; recreation and URI cleanup; approve/cancel actual Gemini requests privately.
7. Gemini key/model changes, removal and failure states. Never put keys in screenshots or reports.
8. Validate Play-generated APK signer and install/update using the internal track; compare against existing sideloaded release.

## Build

Set ANDROID_PLATFORM_JAR, ANDROID_BUILD_TOOLS, ECJ_JAR, JSON_JAR, BUNDLETOOL_JAR and existing signing variables described in README. Do not commit secrets.

    npm test
    bash scripts/test-native.sh
    bash scripts/build-bundle.sh

The build refuses missing signing inputs. Verify the resulting bundle with bundletool, then generate a universal APK/APK set and inspect its manifest/signature. These are host checks and do not replace the device evidence above.

## Official references

- https://developer.android.com/google/play/requirements/target-sdk
- https://developer.android.com/build/building-cmdline
- https://developer.android.com/tools/bundletool
- https://support.google.com/googleplay/android-developer/answer/14151465
- https://support.google.com/googleplay/android-developer/answer/14738291
- https://support.google.com/googleplay/android-developer/answer/10787469
- https://support.google.com/googleplay/android-developer/answer/12991134
