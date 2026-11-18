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

## 1.6.0 Indian catalog

1,070 records: 50 established references + 1,014 INDB recipes + 6 explicitly authored ingredient models. Source: Anuvaad Solutions, **Indian Nutrient Databank (INDB), November 2024 workbook**: https://www.anuvaad.org.in/wp-content/uploads/2020/07/Anuvaad_INDB_2024.11.xlsx . Portal and methodology: https://www.anuvaad.org.in/indian-nutrient-databank/ and https://www.anuvaad.org.in/methodology/ . Research: Vijayakumar A, Dubasi HB, Awasthi A, Jaacks LM. Development of an Indian Food Composition Database. Current Developments in Nutrition. 2024;103790. INDB derives recipes from IFCT 2017/2004, CoFID and USDA; these are recipe calculations, not laboratory measurements of each user's food. Selected numerical kcal/protein facts and identifiers are used with attribution; source recipe prose is not copied.

Gram energy/protein uses source per-100-g values divided by 100. Source-serving values are separate and retained only with an explicit serving unit, non-extreme total, and implied mass between 1–600 g. Count equivalents are enabled only for count-style source units. Bowl/cup/glass/plate are not interchanged for INDB records. A source serving with no safe count/volume mapping is logged as a serving or grams. Missing protein is not replaced with zero.

144 rows are flagged `reviewRequired` by conservative app heuristics (energy >500 kcal/100g or source serving >1200 kcal). Their estimates are blocked rather than corrected silently. This heuristic is not a full nutrition audit; other source values may still differ from real preparations. Names/aliases that collide are omitted from the ambiguous alias set; established legacy matches take precedence.

Six app ingredient models fill naming gaps: chicken/egg biryani, curd rice, chole, egg bhurji and rajma curry. Each model lists the weighed ingredients, USDA IDs through the original references, total serving mass and derived kcal/protein. These illustrative recipes exclude unmodelled seasoning and are labelled accordingly. They are not claimed to be standard regional recipes.

Roti remains the existing 80-kcal plain-piece reference to preserve established logging; 4 rotis =320 kcal before extra ghee. An alternative recipe/size can have a different number. Existing diary records are never recomputed when the catalog grows. User-reviewed meals and product-label portions can be saved in My foods and scaled later. No universal claim that every Indian food/brand/portion is covered.


## Supplemental foods (2026-11)

The supplemental catalog is maintained separately in `data/food-supplements.json` and loaded offline before the meal parser. It adds a generic whey-powder search match, pure creatine monohydrate, whole-wheat/brown bread, an explicitly approximate undressed mixed salad, and common raw vegetables (carrot, lettuce, spinach, cabbage, capsicum, onion, beetroot, cauliflower, green beans, peas, sweet corn and zucchini).

Whey is deliberately marked `reviewRequired`: a generic scoop is not a safe brand-specific estimate. Enter calories and protein from the tub's nutrition panel using **From a label**, or save the product in **My foods** with its actual scoop weight. Creatine's zero-calorie entry applies only to pure, unflavoured creatine monohydrate; blends and flavoured products require their own label. Brown bread uses a generic whole-wheat reference and an approximate 32 g slice. Mixed salad is a rough raw-vegetable mix with no dressing; add oil, dressing, cheese, seeds or croutons separately. Vegetable entries are raw unless their names say otherwise; cooked portions and recipes differ.

These are broad generic references, not a claim that every food, recipe or product brand is covered. Use package labels for branded products and adjust portions. Unknown or review-required entries must not be silently converted into precise-looking totals.
