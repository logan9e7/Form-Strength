# Meal entry refinement — PR #1

Editable Figma: https://www.figma.com/design/ep6tD2yuktlliMAoeCe71H?node-id=2-17

The original PR-linked Figma file was not editable with this connection. A new editable Android design was created: 390px auto-layout view, Roboto typography, navy/cyan brand variables, reusable ingredient rows and no flattened UI image.

The form prioritises a meal description, one editable kcal total, then compact quantity × reference-calorie rows. Protein/source details, reference search, AI and category are secondary disclosures. The Save action remains reachable in the scrollable sheet. Unknown or flagged ingredients prevent deterministic saving. User edits to the total are labelled without rewriting the reference breakdown.

Product-label whey is the intended path: enter the tub values, save in My foods, then type servings of that saved name. Pure creatine zero is explicit and is not applied to flavoured blends. Home remains in its earlier energy-first order.

Review fixes: native supplemental script allowlist; API search/detail catalog parity; numerical salad model, raw-corn label and cauliflower cup consistency; accessible kcal field name, corrected in-app version and source limitations.

Validation: npm test; scripts/test-native.sh; signed APK/resource/DEX compilation; exact bundled assets and zip alignment. Browser demo tested known/unknown foods, editable total, label whey save/scaling, and 320/390/430 widths. Real Android keyboard, notch, health sync and upgrade tests remain pending. This is not Play production approval or a completed AAB release.
