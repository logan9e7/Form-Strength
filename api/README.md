# Food reference API

Run `npm run food-api` with Node 20+; default address `http://127.0.0.1:4180`. No dependencies or paid API required. This is a local API implementation, not a hosted public endpoint. The Android APK bundles the same food references and parser so logging works offline without this server.

| Method/path | Behavior |
| --- | --- |
| GET `/v1/foods?q=roti&limit=20` | Search stored references; limit 1–100. Response includes catalog version and total count. |
| GET `/v1/foods/roti` | One food, unit calories, protein when known, source, portion notes and review flag. |
| POST `/v1/estimate` | JSON `{ "meal": "4 rotis + 1 cup dal" }`; returns each quantity × unit calories, editable total, issues and ready flag. |

`data/food-catalog.json` stores reference details. `scripts/import-indb.py` generates it and the APK's JS reference from the official INDB download plus legacy facts and explicitly authored ingredient models. The import uses openpyxl only during development. Re-run with the source workbook path; input SHA-256 and source URL live in `data/catalog-provenance.json`.

Personal foods are saved on the phone through the existing native SQLite `/api/saved-foods` CRUD API, not to this public reference catalog. A saved 1-roti portion with a reviewed value can be typed as `4 servings My roti`; calories and protein scale together. Food diary/habits/water remain local and included in user-controlled backups. This server neither receives those logs nor requests AI. No API key is embedded.

AI is the existing optional Gemini personal-key flow: explicitly approve sending the meal text, review the response, then save a reusable portion. It does not silently overwrite reference data. Unknown ingredients block deterministic saving; flagged source recipes require review. Missing protein remains unknown. Named regional recipes are estimates, not all possible dishes or exact analysis of a user's plate.

Before any future public hosting, add appropriate service-level rate limiting, monitoring and authentication for user-specific writes. The current API binds to localhost and provides reference reads/estimates only. No public service has been provisioned.
