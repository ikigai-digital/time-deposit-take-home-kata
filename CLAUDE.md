# CLAUDE.md

Project rules for Claude Code (and other AI coding assistants) working in this repository.

## Scope
- Only the `java/` module is being developed. Leave the other language folders (`c#`, `kotlin`, `python`, `typescript`) untouched.
- Requirements live in the root `README.md`; the solution's own docs are in `java/README.md`.

## Hard constraints (from the kata)
- `TimeDeposit` is shared: no breaking changes to it (fields, constructor, getters, setter).
- `TimeDepositCalculator.updateBalance(List<TimeDeposit>)`: signature and behaviour must not change.
  `TimeDepositCalculatorTest` contains characterization tests that pin the legacy behaviour; they must keep passing unchanged.
- Exactly two REST endpoints: `GET /time-deposits` and `POST /time-deposits/balance-updates`.
  `TimeDepositApiIntegrationTest` guards this via the generated OpenAPI contract. Do not add endpoints.
- Handling invalid input or exceptions is out of scope. Do not add validation layers.
- Where requirements are ambiguous, make a logical assumption and document it in a code comment, next to the code it affects.

## Architecture (hexagonal)
- `org.ikigaidigital` holds the domain: `TimeDeposit`, `Withdrawal`, `PlanType`, `TimeDepositCalculator`.
- `application.port.in` holds the use cases, `application.port.out` the repository ports, and `application.service` the use case implementations.
- `adapter.in.web` is the REST controller and its response DTOs (OpenAPI annotations live here).
- `adapter.out.persistence` holds the JPA entities, Spring Data repositories and port implementations.
- `config` does the Spring wiring of the core.
- Domain and application code does not use Spring stereotypes (`@Service`, `@Component`). Beans are created in `config`. `@Transactional` is the only allowed Spring annotation there.
- JPA entities never leave the persistence adapter, and DTOs never leave the web adapter.
- Money: new code uses `BigDecimal`. The legacy `Double` balance is converted at the adapter boundaries and rounded HALF_UP to 2 decimals.
- New interest rules are added as a `PlanType` constant (strategy per plan), not as `if`/`switch` branches in the calculator.

## Database
- Flyway owns the schema (`src/main/resources/db/migration`). Hibernate only validates it (`ddl-auto: validate`).
- Never edit an applied migration. Add a new `V<n>__*.sql` instead.
- Sample data lives in `db/sample-data` and is documented with the balances expected after one update.

## Tests
- Run with `cd java && ./mvnw test` (JDK 17+). Docker must be running for the Testcontainers tests. Without Docker they are skipped, not failed.
- Unit tests for the domain and application use in-memory fakes (`InMemory*Repository`), not mocks.
- Integration tests import `PostgresTestcontainer` (one shared container through the Spring context cache) and are `@Transactional`, so they roll back. Never leave changed sample data behind for other tests.
- AssertJ for assertions. Write tests first, or alongside the change. Every behaviour change comes with a test.

## Workflow
- Small, atomic commits with a message stating the intent (e.g. `persistence adapters: ...`).
- Before declaring work done, run the full test suite and report the actual result.
- Use the `kata-reviewer` agent (`.claude/agents/kata-reviewer.md`) to review a change against these rules before committing.
