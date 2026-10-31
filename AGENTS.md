# Project instructions

## Scope and approach

- Read the assignment and implementation guide in [README.md](README.md) before changing code. Work on Java and directly related documentation.
- State material assumptions. Ask when an ambiguity could change the result; keep justified assumptions in relevant comments.
- Prefer the smallest change that satisfies the requirement. Avoid speculative features, single-use abstractions, and unrelated refactoring.
- Match existing style and preserve existing user changes. Remove only code made obsolete by the current change.
- Define concrete success criteria and appropriate verification before non-trivial work. Report actual results and remaining limitations.

## Compatibility and business behavior

- Keep the shared `TimeDeposit` package/public API unchanged, along with the calculator's no-argument constructor and `updateBalance(List<TimeDeposit>)` signature.
- Preserve the functioning calculator despite ambiguous rate wording: basic days >30 uses `balance * 0.01 / 12`; student days 31–365 uses `balance * 0.03 / 12`; premium days >45 uses `balance * 0.05 / 12`.
- Retain case-sensitive plan matching, zero interest for unknown plans, double arithmetic, and `new BigDecimal(interest).setScale(2, HALF_UP)`. Round interest only, then mutate the existing objects; do not advance days or change IDs, names, membership, or order.
- Repeated updates use current balances. Withdrawal rows are history and cause no additional deductions.

## Architecture and persistence

- Keep plain interest policies, one application service, one application-owned storage port, one JDBC adapter, and one HTTP controller. Share plain read records with HTTP responses.
- Keep SQL in the adapter and the complete read → calculate → save transaction in the service. Calculation reads lock rows in ID order until commit/rollback; normal GET reads do not request those row locks.
- Use Flyway migrations, parameterized SQL, balance-only writes, PostgreSQL `NUMERIC`, and `BigDecimal` for stored/read values. Convert at the shared calculator's Double boundary.
- Expose exactly two business operations: `GET /time-deposits` returns a deposit array; bodyless `POST /time-deposits/update-balances` returns 204 with no body. Swagger/OpenAPI routes are documentation infrastructure under the documented assumption.
- Do not add unrequested creation APIs, authentication, scheduling, validation/exception layers, duplicate models, or ORM infrastructure.

## Verification and delivery

- For bug fixes, add a focused regression test and observe the failure before the fix. Keep calculator characterization cases when refactoring. Prefer real HTTP/PostgreSQL tests for boundary behavior.
- From `java/`, run `.\mvnw.cmd test` for unit tests, `.\mvnw.cmd verify` for unit plus integration tests, or `.\mvnw.cmd package` for the executable JAR with unit tests. POSIX equivalents use `sh ./mvnw`.
- JDK 17+ is required. Full verification needs a running Docker-compatible runtime; Testcontainers starts its own PostgreSQL database separately from optional manual Compose setup. Do not silently skip integration tests.
- Choose checks appropriate to the change. Compilation alone does not establish SQL execution, HTTP behavior, rollback, or browser interaction. Do not present integration tests added after implementation as test-first development.
- Keep `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` external and real secrets out of the repository. Clean up only temporary resources created for verification.
- Preserve Git history and existing changes. Do not commit, push, publish, or send messages without user instructions; follow the assignment's fork/submission requirements.
