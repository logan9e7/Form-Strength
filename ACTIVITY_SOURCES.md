# Activity estimates and device calorie semantics — 1.4

## Sources

Selected numeric MET references from the **2024 Adult Compendium of Physical Activities**, for adults aged 19–59:

- Walking: https://pacompendium.com/walking/ — codes 17152 (slow, 2.8), 17190 (moderate, 3.8), 17200 (brisk, 4.8), 17131 (stair climbing, 6.8).
- Running: https://pacompendium.com/running/ — 12030 (5.0–5.2 mph, 8.5), 12020 (general jogging, 7.5).
- Conditioning: https://pacompendium.com/conditioning-exercise/ — 02054 (multiple-exercise resistance training, 3.5), 02050 (vigorous lifting, 6.0), 02175 (yoga general, 2.3), 02101 (mild stretching, 2.3), 02105 (Pilates general, 2.8), 02068 (rope skipping general, 11.0), 02048 (elliptical moderate, 5.0), 02056 (bodyweight general, 3.0), 02210 (HIIT moderate, 7.0).
- Bicycling: https://pacompendium.com/bicycling/ — 01010 (leisure under 10 mph, 4.0), 01016 (self-selected moderate, 7.0).
- Household: https://pacompendium.com/home-activities/ — 05030 (moderate house cleaning, 3.3), 05021 (moderate mopping, 3.5).

## Calculation

Gross kcal = MET × 3.5 × weight in kg / 200 × minutes.
Active kcal = max(MET − 1, 0) × 3.5 × kg / 200 × minutes.

These are population estimates, not measured expenditure, individualized resting metabolism, a clinical prescription or a calorie allowance. For example, 30 minutes of moderate walking at 79 kg gives approximately 158 gross kcal or 116 active kcal. Rest, effort, efficiency, fitness, temperature and technique change actual burn. Resistance training should use active time rather than counting long rests at the lifting MET. No burn is inferred from exercise reps, distance or dumbbell weight alone.

The parser only accepts supported activity aliases and explicit durations. Unsupported speed/incline/load details block saving rather than being discarded. Multiple activities are separate records saved in one transaction. Editing replaces the same ID. Record weight and MET values remain as recorded when the profile changes.

## Device data

Android 14+ platform Health Connect API, `HealthConnectManager.aggregate`:
https://developer.android.com/reference/android/health/connect/HealthConnectManager

Only three read permissions: active calories, total calories and steps. No writes, routes, heart rate, background access or extended history. Aggregate requests use local calendar-day boundaries and the current instant for today's partial day. The framework Energy type reports **calories**, which this app divides by 1000 to display **kcal**. This differs from AndroidX's convenient `inKilocalories` API.

Null/missing results stay unknown, including when a source shares only some fields. A real reported zero remains zero. Manual active calories are never added to Health Connect active calories. On Today, an available device active value takes display priority and is labelled Device; manual totals remain in Move → Activity. Device total burn includes rest and is never added to device active burn or subtracted from food as a prescribed deficit.

Readings are foreground, user-triggered and held only in memory. Access changes and local-day changes clear affected readings; stale request IDs are ignored. No Health Connect data is sent to Gemini or exported. A source app must actually write the relevant types; Bluetooth pairing alone is insufficient.

Google Fit migration: https://developer.android.com/health-and-fitness/health-connect/migration/fit/faq . New mobile integration uses Health Connect instead of the retiring Fit APIs. Older Android versions retain manual logging and Gemini, but this build does not implement the Android 13 Health Connect APK client.
