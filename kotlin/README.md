# XA Bank Time Deposit — Kotlin solution

Solution for the time deposit refactoring kata, built with Kotlin 1.9, Spring Boot 3.3, JPA,
PostgreSQL, Flyway, Testcontainers and a contract-first OpenAPI specification.

## Prerequisites

- JDK 17 and Maven.
- Docker only for the integration tests (Testcontainers starts PostgreSQL).

## Run the application

```bash
cd kotlin
mvn spring-boot:run
```

Starts the app on http://localhost:8080 with the `local` profile: in-memory H2 in PostgreSQL mode, Flyway
creates the schema and loads sample data (`src/main/resources/db/sample-data`). Data is reset on every start.

Without a profile the app uses PostgreSQL (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, default
`jdbc:postgresql://localhost:5432/timedeposits`) and loads no sample data.

## Run the tests

```bash
cd kotlin
mvn verify
```

The integration tests run against real PostgreSQL started by Testcontainers (Docker must be running).
Covered: calculator characterization tests, unit tests for the application and web layers, end-to-end
tests (HTTP → application → JPA → PostgreSQL).

## Using the API (Swagger)

The contract lives in [`src/main/resources/static/openapi.yaml`](src/main/resources/static/openapi.yaml)
and is served at http://localhost:8080/openapi.yaml.

1. Open **Swagger UI**: http://localhost:8080/swagger-ui.html (static page from the `swagger-ui` webjar that loads
   `openapi.yaml`; no API docs are generated from code, so the app exposes only the two contract endpoints)
2. **`GET /time-deposits`** → *Try it out* → *Execute*: returns one page
   `{items, page, size, totalElements, totalPages}`; `items` are deposits ordered by id with
   `id`, `planType`, `balance`, `days`, `withdrawals`. Parameters: `page` (zero-based, default 0) and
   `size` (default 100, max 1000).
3. **`POST /time-deposits/balance-updates`** → *Try it out* → *Execute*: applies this month's interest
   to every deposit that has not received it yet (`204 No Content`). Calling it again in the same month
   changes nothing.
4. Run `GET /time-deposits` again to see the new balances.

Same with curl:

```bash
curl "http://localhost:8080/time-deposits?page=0&size=2"
curl "http://localhost:8080/time-deposits?page=1&size=2"
curl -X POST http://localhost:8080/time-deposits/balance-updates
```

## Design

### Hexagonal architecture

```
adapter.inbound.web            REST: implements TimeDepositsApi generated from openapi.yaml
        │
application.port.inbound       GetTimeDepositsQuery, UpdateAllBalancesUseCase
application.service            use case implementations
application.port.outbound      TimeDepositRepository (balance update), TimeDepositPageRepository (GET),
                               InterestPlanRepository (plans)
        │
adapter.outbound.persistence   Spring Data JPA, implements the outbound ports

domain (framework-free)        TimeDeposit, TimeDepositCalculator, TimeDepositRecord, TimeDepositDetails,
                               TimeDepositPage, Withdrawal, interest.* (InterestPolicy strategies)
```

The domain stays in the original `org.ikigaidigital` package because moving `TimeDeposit` would be a
breaking change.

### Interest calculation (Strategy)

- `InterestPolicy` — one strategy per plan type, picked by `TimeDepositCalculator` via a map lookup.
- `MonthlyRateInterestPolicy` — "yearly rate paid monthly", configured by rate and day limits. The plans
  (`basic`, `student`, `premium`) are rows of the `interestPlans` table and are read on every balance
  update, so a new plan or a rate change is a data change.
- A rule of a different shape = a new `InterestPolicy`, no change to the calculator (Open/Closed).
  Duplicate policies for one plan type are rejected. The calculator takes the policy list via its constructor.
- `monthlyInterest(deposit): BigDecimal` exposes the rounded interest; `updateBalance` adds exactly this
  amount, so its behaviour is unchanged.
- `updateBalance` signature and behaviour are unchanged. This is proven by characterization tests written
  **before** the refactoring (`TimeDepositCalculatorTest`), including the floating-point rounding quirk
  (`1818.00 * 0.01 / 12` rounds to `1.51`, not `1.52`, because the interest is computed in `Double`).

### Database

Flyway migration `V1__create_time_deposit_tables.sql` creates `timeDeposits` and `withdrawals` with the
column names from the requirements (camelCase, quoted identifiers). `V2` adds `lastInterestMonth`
(see idempotency below), `V3` creates `interestPlans` with the three plans. Hibernate runs with `ddl-auto: validate`.

### Scalability and idempotency

- **GET**: page-based pagination ordered by the primary key; a page costs three queries regardless of
  its size: the page, the total count and the page's withdrawals (one `IN` query, no N+1).
- **POST**: batches of `time-deposits.balance-update.batch-size` (default 500). Each batch is one
  transaction: select deposits not updated this month `FOR UPDATE SKIP LOCKED`, add the interest to each
  one in code, save them. Repeats until no deposit is left.
- **Two identical requests**: `SKIP LOCKED` makes them take different rows, so they share the work instead
  of waiting; `lastInterestMonth` stops a deposit from being picked again after its batch committed. Each
  deposit gets the interest once; a repeated call in the same month changes nothing; a failed batch rolls
  back and is picked up by the next call. Covered by integration tests, including parallel calls.

### Assumptions

All assumptions are listed here (not in code comments).

**Data and storage**
- **Database**: the requirements do not name one; PostgreSQL is used, matching the Testcontainers setup.
  Local runs use in-memory H2 in PostgreSQL mode so that `mvn spring-boot:run` needs nothing else; tests
  always run on real PostgreSQL.
- **Table and column names** follow the requirements literally (camelCase), hence quoted identifiers.
- **"Store all time deposit plans in a database"** covers the plan rules: rates and day limits live in the
  `interestPlans` table and are read on every balance update.
- **No plan defaults in code**: the original no-arg `TimeDepositCalculator()` is removed, callers pass the
  policies (the application builds them from `interestPlans`). The requirements only protect `TimeDeposit`
  and the `updateBalance` signature; `updateBalance` behaviour with the original rules is unchanged.
- **Rate changes apply to existing deposits**: plans are read on every balance update, so changing a rate in
  `interestPlans` also changes the interest of deposits opened before the change. This may contradict the
  terms those deposits were opened with; assumed acceptable. Fixing the terms per deposit would need the
  rate (or a plan version) stored on the deposit.
- **Money**: `BigDecimal` everywhere outside the shared `TimeDeposit`, stored as `DECIMAL(19,2)`. The only
  `Double` conversion is `BigDecimal → TimeDeposit` as input to the interest calculation; the calculator
  returns the interest as `BigDecimal` cents that is added to the `BigDecimal` balance, so stored and returned
  balances never go through `Double` and keep full precision (tested with `90071992547409.93`).
- **Withdrawals** are read-only data for the GET endpoint; they do not affect interest (not required).
  They are kept outside `TimeDeposit` (read model `TimeDepositDetails`) so the shared class is unchanged.

**Interest rules**
- **Unknown plan types** earn no interest, as in the original implementation.
- **Student plan "no interest after 1 year"** = from day 366 on, as in the original implementation.
- **Interest is monthly**: applied at most once per deposit per calendar month (UTC), tracked in
  `lastInterestMonth`.
- **`days` is not advanced** by the update — the original calculator does not do it and the requirements do
  not ask for it.

**Balance update endpoint**
- `POST /time-deposits/balance-updates` (it triggers a run rather than replacing a resource), returns `204`.
- **It must be called once a month by an external scheduler** (cron, CI job, etc.). A missed month is not
  caught up: the next call credits only the current month, the interest of the skipped month is lost.
  Catching up would need `days` to move with time, which the current model does not do.
- **A deposit without history** (`lastInterestMonth` is empty) gets only the current month's interest.
- **Concurrent calls** are safe: each batch locks its rows with `FOR UPDATE SKIP LOCKED`, so parallel calls
  split the deposits, and `lastInterestMonth` keeps each deposit from being credited twice in a month.

**GET endpoint**
- **Pagination**: GET returns a page (default 100, max 1000) with paging metadata next to `items`; each item
  has exactly the required schema. Out-of-range `page`/`size` are clamped instead of rejected (input
  validation is out of scope).

## AI-assisted development

### Setup

| Tool | Role |
|------|------|
| [Claude Code](https://claude.com/claude-code) (CLI, Windows) | coding agent: reads repo, edits files, runs Maven, Docker and git |
| Model: Claude Opus 5.5 (`claude-opus-5-5`) | LLM behind the agent |
| [Context7 MCP server](https://github.com/upstash/context7) | fetches current library docs (used for openapi-generator `kotlin-spring` options) |
| [`CLAUDE.md`](CLAUDE.md) | project rules loaded into every agent session (constraints, architecture, workflow) |
| Global rule `~/.claude/rules/context7.md` | "use Context7 for any library/framework question instead of memory" |
| caveman plugin | terse agent output to save tokens; no effect on code |

Reproduce: install Claude Code, add the Context7 MCP server (`claude mcp add context7 -- npx -y @upstash/context7-mcp`),
open a terminal in `kotlin/` and run `claude`. `CLAUDE.md` is picked up automatically.

Prompt used to start: *"read the readme and do the assignment, it must be in Kotlin"*.

### Agent workflow

1. Agent read the requirements and the existing Kotlin code, checked the toolchain.
2. Wrote characterization tests for the untouched calculator first — this caught the `Double` rounding
   quirk, which was then pinned as expected behaviour.
3. Refactored the calculator to strategies with the characterization suite as the safety net.
4. Built the contract, layers and adapters step by step, compiling after each step, then ran the
   Testcontainers tests and a manual smoke test (`mvn spring-boot:run` + curl + Swagger UI).
5. Review feedback applied: no code comments, assumptions only in this README; simpler balance update with
   `FOR UPDATE SKIP LOCKED`; plans in the database; paged GET; local run on H2 via `mvn spring-boot:run`.
6. Commits kept few and focused (see `git log`).

### What was AI-assisted and why

- **Generated by the agent**: characterization tests, the calculator refactoring, OpenAPI contract,
  Maven setup, persistence/web adapters, Flyway scripts, tests, this README.
  Reason: mostly well-known patterns and boilerplate where an agent is fast, and every step is
  verified automatically (unit tests, Testcontainers tests, smoke test).
- **Author's part**: defining the task and the preferred stack, the rules in `CLAUDE.md`, reviewing
  the commits and the assumptions listed above.
- Issues the agent hit and fixed on its own: the generator produces invalid Kotlin for packages named
  `in`/`out` (renamed to `inbound`/`outbound`); a characterization test expectation that assumed
  "mathematical" rounding was corrected to the real `Double` behaviour.
