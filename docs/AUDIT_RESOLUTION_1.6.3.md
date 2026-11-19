# v1.6.2 audit resolution — v1.6.3

The supplied HTML audit was checked against source rather than accepted verbatim. Four agents owned native lifecycle/backups, Health Connect/Gemini, SQLite, and build tooling. Root reviewed their integration.

| Finding | Resolution / evidence |
| --- | --- |
| Alleged guaranteed API26–33 health crash | **Not established by original code**: every MainActivity entry already checked SDK34. Added defense in depth anyway: API-independent HealthConnection facade, explicit unsupported state, platform types only in HealthConnectionApi34. JVM boundary checks cover 26/33/34/36; actual old-Android runtime remains unverified. |
| Gemini model-only update | Blank key retains existing encrypted key when configured; first setup requires key. Settings policy tests cover both paths and invalid input. |
| Export lost during picker recreation | BackupFiles stores bounded atomic snapshot in app-private cache; saved state contains only a validated filename, not a multi-megabyte Bundle. Stale snapshots expire after 24h; cancel/success removes active snapshot. Filesystem tests cover roundtrip, size limits, unsafe names and cleanup. OS process/picker recreation still needs phone testing. |
| Backup I/O on UI thread | Backup file reads/writes, validation and transactional imports run on the worker executor. Only picker/dialog and JS feedback run on UI. Recreated picker callbacks reserve backupBusy. |
| AI busy lock | Dialog failure, cancel/dismiss, rejected tasks and task completion release aiBusy; activity closure cancels UI delivery. |
| Back never exits | **Incorrect for normal flow**: original JS called AndroidStore.exit on Home. Native fallback now finishes if the JS handler is absent/throws or evaluateJavascript fails, while preserving dirty-dialog confirmation. |
| Store close versus callbacks | Destroy flag rejects new work; pending health cache writes are queued off UI and guarded. Already submitted jobs drain before monitor-protected SQLite close. Dialog/JS callbacks are lifecycle guarded. |
| Saved-food import bypasses cap | SQL counts and unique-ID preflight run in the write transaction before any import writes. Duplicate-only restores/updates are allowed at the cap (including grandfathered excess); new additions are blocked. Tests cover 999+1, 999+2, 1000+1 and duplicates. |
| Activity day index / repeated parsing | Additive schema4→5 migration creates activity_day index. Count operations use SQL COUNT(*) rather than parsing JSON payloads twice. |
| Stale artifacts / D8 batching / spaces | Build cleans bounded known intermediate paths, derives output name/min API from manifest, uses ECJ response files and one sorted class JAR for one D8 invocation. Hermetic test exercises paths with spaces and failed-build cleanup. |
| Async key removal | clear uses commit and reports failure; secret text is cleared on dialog dismiss. |
| Source-app package visibility | Exact package ID fallback avoids ambiguous generic source names without broader package queries. Friendly labels remain best-effort. |
| No per-use Keystore authentication | Deliberate: key is non-exportable/app-private and requests require explicit native consent. No unsolicited biometric/app-lock change introduced. |

## Verification limits

Pure JVM and hermetic build tests are not Android instrumentation. No attached phone/emulator or KVM is available. Actual SQLite migration/atomic rollback, process death while picking files, native Back/keyboard/notch, old-Android class loading, encrypted-key operations and real Health Connect/Gemini operations remain unverified. The release is an installable update for device QA, not Play production approval.
