# Offline meal estimates — 1.1.0

The bundled reference contains 35 common foods. It is a small deterministic lookup and portion parser, not an AI nutrition model. It does not send meal text off-device. Names, aliases and parsing rules are authored for this application.

## Sources

1. USDA FoodData Central, **SR Legacy (April 2018)**. Download page: https://fdc.nal.usda.gov/download-datasets/ . The underlying archive is `FoodData_Central_sr_legacy_food_csv_2018-04.zip`. Selected energy values (nutrient 1008, kcal per 100 g) and portion masses are used for fruit, milk, curd, rice, bread, nuts, egg, chicken breast, sugar and fats. Each bundled record includes its FDC ID.
2. Ministry of Health & Family Welfare, Government of India, **Guidance Document: Nutritional Care & Support for TB Patients in India**, prepared-food calorie tables, printed pages 91–94. https://tbcindia.mohfw.gov.in/wp-content/uploads/2023/05/Guidance-Document-Nutritional-Care-Support-for-TB-patients-in-India.pdf . Only selected numerical food/portion reference facts are used; the document's clinical dietary recommendations are not applied to this app or its user.

These are reference portions, not lab analysis of the user's meal. The app does not label a meal exact, infer body-fat percentage or set a medically prescribed calorie target.

## Conversions and assumptions

- A typed bowl/katori maps to one reference cup and is identified as an assumption.
- Gram quantities are supported only where a per-100-g reference exists. A gram value is never invented for roti, dosa or other serving-only references.
- For beverages with USDA cup masses, ml conversion assumes a 240 ml cup and is approximate. Lassi uses the documented 200 ml serving.
- A generic egg uses the boiled-egg reference. A generic chicken portion uses roasted skinless chicken breast. The review shows these assumptions.
- Tea/coffee references include the source's specified milk and sugar. Black tea/coffee have separate matches.
- Extra oil, toppings or unrecognized ingredients are not silently ignored: unresolved text blocks quick-entry saving. The user can switch to manual logging instead.
- Default portions and per-food calories are shown in the review. Total calories can be edited before save.
- An exact name from a previous entry can reuse that entry's portion; the review identifies it as the user's previous entry.

## Stored data

Quick entry saves one aggregate meal record with its entered description, reviewed total, `estimated: true`, explanatory basis and initial component breakdown. A corrected total may differ from the initial breakdown. Both are retained so the original reference basis is distinguishable from the user's review. Manual entry remains available. Repeating a saved estimate retains its estimate provenance.

The SQLite schema is unchanged. Version 1.1.0 remains compatible with 1.0.0 records and backups. Older app versions may omit estimate metadata if they import and re-export a newer backup.

## 1.3.0 additions

50 foods are now bundled. Selected protein values (nutrient **1003**, grams per 100 g) were extracted from the same official USDA SR Legacy CSV archive. For existing USDA serving references, protein uses the same portion mass implied by the recorded energy and USDA kcal/100 g. New references use explicit gram portions: Greek yogurt, boiled lentils, peanut butter, firm tofu, boiled chickpeas, dry oats, boiled mung beans, boiled red kidney beans, dates, cucumber, orange, boiled broccoli, walnuts, raw tomato and cooked Atlantic salmon. Every record retains its exact FDC ID.

32 references have sourced protein. No protein value was invented for the 18 prepared-food recipes whose energy-only reference does not supply it. Such meals may have calories while protein remains unknown. Mixed meals with any unknown component leave the meal protein blank unless the user supplies a reviewed total. Daily totals label incomplete protein coverage. Zero is a valid explicit known value; blank is not converted to zero.

Custom foods use the user's label/source and portion. A saved portion can be repeated or typed; calories and protein scale together with servings. Saving/editing a reference estimate retains its estimated status. Generic protein-shake nutrition is deliberately not assumed: products, scoop sizes and added milk differ.
