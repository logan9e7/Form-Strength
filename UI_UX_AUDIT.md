# UI/UX audit and implementation — 1.3.0

Reviewed 12 November 2026. Scope: Today, training, food diary, typed/manual entry, saved foods, progress, check-in and profile/setup. Preview observations use a browser bridge, not the Android runtime.

## Findings resolved

| Finding | Implemented change | Verification |
| --- | --- | --- |
| Personal identity/measurements were compiled into the interface | Optional persistent profile; generic first-run states; editable baseline/goal | Browser setup/save/reload; native validation |
| Weight occupied the prime dashboard space while food/water required navigation | Food/protein hero, direct +250/+500 ml water, four habit toggles | Browser clicks and saved state |
| Water buttons became disabled after saving a check-in | Shared busy-state synchronization restores quick controls in the save/delete finally path | Reproduced, fixed, retested check-in → water → reopen |
| Protein-shake habit could be mistaken for nutrient logging | Separate habit tile and explicit “Log shake calories” route to label-based saved foods | Browser saved shake portion scaled 1.5× |
| Food records could not hold protein | Optional per-serving protein, totals scaled with portions, editable typed-meal protein | Native and JS tests; browser 120 kcal/24 g ×1.5 =180 kcal/36 g |
| Unknown recipe nutrients could look like zero | Unknown protein omitted; partial daily coverage labelled | Browser unknown-protein meal; native tests |
| Limited repeat-food flow | My foods with search, favourites, create/edit/remove, log portion, and typed references | Browser create/save/repeat/type; parser tests |
| Saved foods could share a name, making typing ambiguous | Typed ambiguous saved names require selecting or renaming a portion | Parser regression test |
| No compact view of habit consistency | Fourteen-day calendar with date/status accessible names; tap to edit | Browser 390/430 px review |
| Dense screens and small food actions | Diary/My foods sections, 44 px primary controls, stacked mobile forms, readable hints and focus states | Mobile previews at 320, 390, 430 px; no horizontal body overflow observed |
| Backup excluded new settings/library | Validated format 2, legacy support, profile preservation and library merge | JVM canonicalization/roundtrip tests; native system picker still pending |
| Screen controllers were mixed together | Separate food, saved foods, profile, habits, training, progress and startup modules | Syntax/DOM-reference checks and browser navigation |

## Design decisions

Forest green, warm off-white and lime form one consistent palette. The default dashboard prioritizes the daily task, with weight secondary. No decorative stock imagery or external fonts. Food and check-in forms use native input types and full-width primary actions. Major actions have explicit loading/disabled states and surfaced errors. Estimated/unknown data is described rather than presented as exact. Calendar cells use standard compact calendar sizing rather than claiming every cell is 44 px wide.

Profile and guidance are progressive disclosure rather than permanent homepage content. No arbitrary calorie deficit or automatic protein prescription. Logging a water amount is distinct from achieving an optional personal target. The calendar does not claim that an unlogged day was a failure or that zero sugar was chemically measured.

## Competition reference

Official product documentation was reviewed, rather than claiming hands-on competitor testing:
- Cronometer documents diary, custom foods/recipes, repeat items and nutrition tracking: https://support.cronometer.com/hc/en-us/articles/360018171731-Diary-Overview and https://support.cronometer.com/hc/en-us/articles/360018239492-Foods-Overview
- Healthify documents meal and macro tracking: https://www.healthifyme.com/app/

This release targets a narrower advantage: private offline use, quick daily actions, editable reference portions and no account friction. It does **not** establish overall superiority. The 50-food offline catalog is much smaller than established products; there is no barcode scanner, photo recognition, comprehensive micronutrient analysis or clinical coaching. Compare logging completion time, correction rate and one-week retention with real users before making competitive claims.

## Remaining gates

No Android phone/emulator was attached. Keyboard resize, native Back, system document pickers, lifecycle recovery and actual upgrade/database migration still require the device checks in `ANDROID_TEST_REPORT.md`. Browser localStorage persistence is not proof of native SQLite persistence.
