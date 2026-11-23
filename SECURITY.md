# Security & privacy

Calculator is designed for offline use. It requests no internet, location, storage or contacts permissions.

- Expressions are evaluated by a bounded mathematical parser; no eval, scripting engine or remote execution is used.
- Calculation history, favorites and preferences are stored in the app-private SharedPreferences area. Android backups are disabled.
- The history list is capped at 500 entries; users can clear it in Privacy & Data.
- Currency conversion uses only the manually entered rate. No live rates are fetched.
- No analytics, ad SDKs, login, accounts, network telemetry or third-party services are integrated.
- Financial estimates are illustrative, not professional financial advice.
- Report vulnerabilities by opening a GitHub private security advisory when available rather than posting confidential data publicly.
