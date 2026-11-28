# Form & Strength 1.8.0 — code 16

## Scope

No external tester feedback was supplied or filed in the repository. This release improves usability and fixes issues identified during implementation; it does not claim to resolve an unseen Play pre-launch report.

Home now offers 12 shortcuts with selected-date counts or explicit missing/loading/connection states: Meals, Workouts, Activity, Water, Habits, Check-in, Progress, My foods, Meal camera, Health Connect, Reminders, You & settings. Existing energy, water, habits and movement summaries remain available. All seven habits are shown, matching the denominator; previously only four were directly accessible.

You is split into Overview, Trends and Settings. Personal setup and profile/goals remain editable; connections, reminders, backup and privacy are grouped in Settings. Published privacy/terms links are included. Optional setup no longer interrupts first use. Keyboard-operated tabs expose ARIA selection/focus; native Back resolves a user subview before Home, while open modal Back handling remains with the existing controllers.

The new home-hub controller delegates to existing feature controllers. Native resource allowlisting includes it. The preview's native-bridge injection now tolerates reordered HTML attributes, and controller asset tests inspect attributes in any order. The preview profile update preserves the optional fields that the Android store already preserves; no new production schema or permission was added.

## Verified locally

- Six Node/API regression suites: meal parsing, nutrients, activities, Health Connect calculation/state semantics, food API, supplemental assets and native script allowlist.
- Four JVM entry points: backup paths/bounds/cleanup, SDK/Gemini configuration policy, onboarding/prices/timing/photo validation, native field/import validation.
- Host SQLite schema/index preservation and existing hermetic APK build contract.
- Development browser acceptance: all 12 shortcuts, 7 habits, optional setup, historical-date meal save, profile edit preserving preferences, data reload, keyboard tabs, simulated native Back, explicit failed-load states, layouts at 320/390/430/768/1200 pixels.
- Existing browser smoke: local onboarding, typed meal, guided active/break timing and note preservation, serving-price filters and persistence.

Browser tests use synthetic records in a disposable localStorage bridge. They do not test Android SQLite, system UI, actual camera/notification delivery, Keystore or live Health Connect/Gemini.

## Release packaging

Package com.logan.formstrength; minimum API 26; compile/target API 36; schema 5. Signed APK and signed AAB use the existing release identity. Run scripts/build-bundle.sh with the documented dependency/signing environment; never commit credentials. Verify the AAB and the bundle-generated APK before distribution.

## Device and Play gates remain open

No Android device is attached and /dev/kvm is unavailable here. Real OS launch/update/data preservation, API 26–33 fallback, API 34–36 permissions/Health Connect, Android keyboard/notch/Back, Keystore, system-picker recreation, reminders/reboot/timezone and external camera flows still need Android evidence. The signed testing candidate must be uploaded to the closed/internal track and actually tested; a host pass does not establish production readiness.

Upload code 16 as a new release or update to the existing test track, keeping the same package. Do not uninstall an existing sideloaded app to update it. Play-delivered update compatibility also depends on Play App Signing retaining the expected app-signing identity.
