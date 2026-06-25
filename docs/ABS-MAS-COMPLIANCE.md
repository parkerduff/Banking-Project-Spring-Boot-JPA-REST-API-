# ABS-MAS API Playbook — Compliance Mapping

This document maps the **ABS-MAS Finance-as-a-Service: API Playbook** guidelines to how this
repository implements them, and records the gaps this change set closed.

## Summary

| # | Playbook guideline | Status | Where |
|---|--------------------|--------|-------|
| 1 | Standard, buildable project so it can be governed/CI'd | ✅ Closed | `src/main/java/...` Maven layout, `.github/workflows/ci.yml` |
| 2 | Use **nouns** as resource names, not verbs | ✅ Closed | `/accounts`, `/deposits`, `/withdrawals`, `/transfers`, `/transactions` |
| 3 | Short URLs (≤ 3 nodes) | ✅ Met | All routes ≤ 3 path nodes |
| 4 | Explicit **versioning** (release numbers) | ✅ Closed | Base path `/api/v1/bank` |
| 5 | **JSON** request/response for RESTful APIs | ✅ Closed | `@RequestBody` DTOs; JSON responses |
| 6 | Input validation / secure API coding | ✅ Closed | Jakarta Bean Validation on request DTOs |
| 7 | **Error handling** with consistent schema, no leakage | ✅ Closed | `GlobalExceptionHandler` + `ApiError` |
| 8 | **ISO 8601** date/time representation | ✅ Closed | Jackson `write-dates-as-timestamps=false`, UTC |
| 9 | Field projection ("reduce fields returned") | ✅ Closed | `?fields=` via `FieldProjection` |
| 10 | Pagination for collections | ✅ Closed | `PagedResponse`, `page`/`size` params |
| 11 | **Authentication** (OAuth2/OIDC family) | ⚠️ Baseline | HTTP Basic + roles (Spring Security); OAuth2/JWT documented as production target |
| 12 | **Authorisation** (role-based access) | ✅ Closed | `USER` reads, `ADMIN` mutations |
| 13 | **Encryption** (TLS 1.2+) | ⚠️ Documented | TLS config template in `application.properties`; HSTS header enabled |
| 14 | Decouple API contract from persistence | ✅ Closed | Response records instead of exposing JPA entities |
| 15 | Documentation drives adoption | ✅ Closed | OpenAPI 3 + Swagger UI (springdoc) |
| 16 | Governance: testing/acceptance controls | ✅ Closed | Service + MockMvc tests; CI on every push/PR |

## Gaps that existed before this change

- **The project did not build.** All `.java` files were committed at the repository root (not under
  `src/main/java`), so Maven compiled **zero** sources. A pre-built `Banking_PROJECT_OK.zip` was
  committed in place of a working source tree.
- **No authentication or authorization** on any endpoint — every operation, including money
  movement, was publicly accessible.
- **Verbs in URLs** (`/deposit/{id}/{amt}`, `/withdraw`, `/transfer`) and **no API version**.
- Inputs passed as query/form parameters (`@RequestParam`) **directly in the URL path** with **no
  validation** (e.g. negative deposit amounts were accepted).
- **Leaky error handling**: controllers returned raw `e.getMessage()` strings with a blanket
  `400 Bad Request`, instead of a consistent error schema with correct status codes (404 / 422).
- JPA entities returned directly; no pagination; date formatting not pinned to ISO 8601.

## Endpoint redesign (verbs → nouns, versioned)

| Before | After |
|--------|-------|
| `POST /api/bank/accounts/deposit?accountNumber=&amount=` | `POST /api/v1/bank/accounts/{accountNumber}/deposits` |
| `POST /api/bank/accounts/withdraw?accountNumber=&amount=` | `POST /api/v1/bank/accounts/{accountNumber}/withdrawals` |
| `POST /api/bank/accounts/transfer?fromAccount=&toAccount=` | `POST /api/v1/bank/transfers` |
| `GET /api/bank/accounts/statement/{accountNumber}` | `GET /api/v1/bank/accounts/{accountNumber}/transactions` |

## Post-review hardening

Additional fixes applied in response to automated review, strengthening the playbook's
*Secure API Coding*, *Error Handling*, and *Information Security* guidelines:

- **Collision-free identifiers**: `accountNumber` / `transactionId` now use UUIDs instead of
  `System.currentTimeMillis()`, which could collide for entities created in the same millisecond
  (notably the two transactions a transfer writes back-to-back).
- **Concurrency control**: `BankAccount` carries a JPA `@Version` field, so concurrent
  balance updates fail fast with an optimistic-lock error rather than silently losing updates.
- **Transaction description sizing**: `Transaction.description` is `@Column(length = 512)` so
  transfer notes (user text + appended transfer context) cannot overflow the column at runtime.
- **Path-variable validation**: `accountNumber` path variables are constrained
  (`@Pattern`, max length); malformed values return `400` via the global handler.
- **Reduced attack surface**: the H2 console is disabled by default (opt-in via
  `H2_CONSOLE_ENABLED`) and removed from the public allow-list; the actuator health endpoint is
  pinned to `show-details=never`; default credentials trigger a startup warning.

## Notes on remaining "baseline" items

- **Authentication**: The playbook's recommended standards are OAuth 2.0 / OpenID Connect with
  signed **JWT** access tokens. To keep this demo self-contained and testable, it ships a stateless
  HTTP Basic baseline (BCrypt-hashed, role-based, externally configurable credentials). Swapping in
  an OAuth2 Resource Server (`spring-boot-starter-oauth2-resource-server` + an issuer/JWKS) is the
  recommended production upgrade and only affects `SecurityConfig`.
- **Encryption/TLS**: TLS termination is environment-specific. A ready-to-enable `server.ssl.*`
  block is included (commented) in `application.properties`, and HSTS is sent on responses.
