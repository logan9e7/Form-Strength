# Fitness PDF: iteration boundaries and acceptance

Source: user-supplied fitness.pdf, two handwritten pages, visually reviewed 22 November2026. The user's accompanying instructions override the sketch's Account Sign-in box. This is a source-to-implementation specification, not evidence that features are implemented.

## Prerequisite

Production implementation and release are gated on the eight Android acceptance rows in ANDROID_TEST_REPORT.md. No row is passed by compilation, emulator transport-online, a timeout, or this document. Live Gemini needs an owner-entered real key; populated Health Connect revocation needs an actual provider record. The separate same-signer instrumentation harness is preparation for OS checks, not a production dependency.

## Iteration1: local screens only

| PDF intent | Agreed implementation boundary | Acceptance |
| --- | --- | --- |
| Daily Goal home | One selected-date context, daily goal summary and prominent Workout/Food destinations; retain source/missing-data labels for energy. | Both destinations open the matching sub-screen; selected date survives the round trip. No invented burn or progress values. |
| Expand/hold transitions | Tap is the primary action; restrained optional transition. Long press is not required to discover or reach anything. | Touch, keyboard and reduced-motion users can navigate. |
| Top Back on sub-screens | Visible, labelled Back control on Workout, Food and personal detail screens; preserve native modal/dirty-form behavior. | Back returns predictably without losing saved data; cancelled discard retains draft. |
| Goal screen | Local gain/maintain/loss intent, target and timeframe in months. | Validated inputs, editable later, no login or network dependency. |
| BMR calculation screen | Explain required personal inputs, estimate assumptions and purpose; explicit distinction between resting estimate and total daily burn. | No calorie prescription from missing inputs; user's baseline and formulas are reviewable before saving. Formula and bounds must be specified/tested during implementation, not inferred from handwriting. |
| Workout and diet preferences | Local available equipment, cuisine and optional supplement preferences; supplements are optional and do not automatically change nutrition totals. | Skip/none paths work; only logged foods count as eaten; preferences survive restart and export/import. |
| Why we ask footer | Context-specific reason near each onboarding step. | Every collected field has a clear use; unnecessary fields are omitted. |
| Calculation interstitial / Let's go | Brief calculation status tied to real local work; no fake percentages, forced delay or promised physiology. | Completion leads to Home; accessible status, retry/error if saving fails. |
| You tab | Consolidate personal setup, simplified daily/weekly review, workout trend and food trend; settings and backup remain reachable. | Encouraging factual copy; incomplete food/health data never becomes a confident success/failure judgement. |

Migration must retain existing profile/log/food/activity/library records. Existing completed users are not forced through onboarding again. Optional new profile fields require coordinated Java validation, JS persistence, backup roundtrip and old-backup compatibility. No new Android permission, SDK, backend, Gradle or runtime package is introduced in this slice.

## Later slices

Iteration2: one-at-a-time workout player; active work and breaks counted separately; equipment alternatives; native notification channels, API33+ permission, scheduling and receiver +log actions. Use channel/system DND behavior; no bypass. Review counts as You content; notification scheduling is not simulated in slice1.

Iteration3: native camera capture and URI granting, secure output provider implementation compatible with zero Jetpack dependencies, bounded image handling and Gemini multimodal request behind explicit consent. The sketch's camera placeholder must not look functional before this exists. Food budget filter: Low cost, High ROI with explicit price/portion provenance; no invented local prices or supplement promises.

The PDF mentions water/walk reminders, daily wind-up, future workout previews, supplements and budget food. These remain assigned to later slices rather than expanding iteration1.
