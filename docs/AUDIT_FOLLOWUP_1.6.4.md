# Audit follow-up: 1.6.4

Three agents owned platform/lifecycle, storage/AI security, and build/tests. The lead reviewed the combined interfaces and ran the final verification gate. The zero-Gradle build, minSdk26/targetSdk35 and dependency-free runtime are preserved.

| Area | Additional correction |
| --- | --- |
| Older Android | Health Connect facade connect/settings return safely when unsupported; read includes canonical unsupported flags. API30 window and API33 navigation symbols are confined to guarded helper classes. |
| Package visibility | Narrow Health Connect package/intent queries enable labels for declaring clients. Hidden or nonconforming origins retain the exact package ID. There is no QUERY_ALL_PACKAGES permission. |
| Activity lifetime | Pending import-picker state is saved as well as pending export filename. JavaScript delivery is guarded; explicit unhandled Back falls back to finish. Destruction cancels the Gemini connection and dismisses its secret-entry dialog. |
| Backup lifetime | Bulk I/O/validation/import remain off UI. A prepared snapshot survives picker recreation without placing large payloads in Bundle. Destruction before picker launch cancels preparation; an unreferenced private snapshot expires after24h. This boundary is not arbitrary process-death completion. |
| Encryption | Stored cipher/IV shape is checked; reads never generate a missing AES key. A model-only update verifies decryptability. Invalidated/corrupt state produces a reconnect message without silently discarding it. Preference removal remains synchronous. |
| Storage | All insert paths now reject failed writes; IGNORE succeeds only for a confirmed duplicate. Capacity preflight queries incoming unique IDs through the primary key, not every existing saved ID. UTF-8 byte bounds match the file I/O limit. |
| Build | Optional version-name/code guards must match manifest; output basename is validated; key password can differ from store password. Cleaning, quoted ECJ response files and a single D8 archive invocation remain covered. |
| Regression | JVM cipher/IV and UTF-8 bounds, cap boundaries and helper policy; host SQLite production-schema/index SQL; hermetic build behavior. Host SQL is not Android SQLiteOpenHelper instrumentation. |

Full-history GET/export routes intentionally preserve their existing response and all user records; they can still consume memory proportional to retained history. Pagination would require coordinated UI/API work and is not claimed here.

## Acceptance boundaries

See ANDROID_TEST_REPORT.md for exact commands/results and the device checklist. Compilation and host tests do not prove API26 class loading, Android schema migration/rollback, system picker recreation, Back/insets, Keystore or live integration correctness. No new Android runtime PASS is claimed. The earlier emulator transport-online observation was not an app test result.

Android references used for scoped declarations: https://developer.android.com/health-and-fitness/health-connect/get-started and https://developer.android.com/training/package-visibility/declaring .
