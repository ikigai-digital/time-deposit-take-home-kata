# CLAUDE.md

Project memory for Claude Code. Loaded automatically at the start of every session (main agent and subagents).

## Project

XA Bank **time deposit kata** (take-home exercise). A Spring Boot service with exactly two endpoints:

- `POST /api/time-deposits/balance-updates` — starts the monthly interest update of **all** deposits in the background
  (chunked, parallel, one run at a time; run status stored in `balance_update_processes`).
- `GET /api/time-deposits?page=&size=` — all deposits with their withdrawals, page by page.

The kata requirements are in `README.md` at the repository root. Read it before any non-trivial task.

## Kata contract (non-negotiable)

These come from `README.md`.

1. **No breaking changes to the shared `TimeDeposit` class** (`domain/model/TimeDeposit.java`).
2. **Do not change the signature of `TimeDepositCalculator.updateBalance(List<TimeDeposit>)`** and keep its behaviour
   (interest results must stay identical) — tests protect the behaviour.
3. **Exactly two API endpoints.** Do not add endpoints (no status/health/admin endpoints in `TimeDepositApi`).
4. GET response schema: `id`, `planType`, `balance`, `days`, `withdrawals`.
5. Interest rules: basic 1%, student 3% (none after 1 year), premium 5% (starts after 45 days); no interest in the
   first 30 days for any plan. Monthly interest = annual rate / 12.
6. In case of ambiguity: make a logical assumption and **justify it in a code comment**.

## Stack

Java 21 · Spring Boot 4.1 · Hibernate 7 / Spring Data JPA · PostgreSQL · Liquibase · springdoc-openapi ·
Lombok · JUnit 5 + AssertJ + Mockito · Testcontainers · Awaitility.

## Commands

The Maven project lives in `java/`. Run everything from the repository root:

```bash
mvn -f java/pom.xml clean verify                    # full build, all tests (Docker must be running)
mvn -f java/pom.xml test -Dtest=TimeDepositCalculatorTest   # one test class
mvn -f java/pom.xml test-compile                    # fast compile check (what the Stop hook runs)
mvn -f java/pom.xml spring-boot:run -Dspring-boot.run.profiles=dev   # "dev" starts PostgreSQL via docker compose
```

Swagger UI: http://localhost:8080/time-deposit-service/swagger-ui.html

## Architecture (hexagonal)

Base package `org.ikigaidigital.timedeposit`:

| Package | Contains | Rules |
|---|---|---|
| `api` | `TimeDepositApi` (interface + OpenAPI annotations), `TimeDepositController`, `api.dto` (`*Dto` records, `PageDto`), `api.mapper` | Talks only to domain services. Never exposes entities or Spring Data `Page`. |
| `domain` | `BalanceUpdateService`, `TimeDepositQueryService` (+ `Impl`), repository **interfaces**, `TimeDepositCalculator`, `interest.calculator.policy` (one policy per plan), `domain.model` | No JPA, no Spring Data types, no SQL. |
| `infrastructure` | `*RepositoryImpl` (implement domain interfaces), `*JpaRepository` (Spring Data + JPQL), `entity` (`*Entity`), `entity.projection` (DTO projections), `mapper` | The only place that knows about JPA/Hibernate. |
| `config` | `@Configuration`, `@ConfigurationProperties` (`balance-update.*`), executor bean, OpenAPI | Wiring only. |

New interest plan = new `InterestPolicy` implementation; no changes to existing policies (open/closed).

## Key design decisions (do not undo without discussing)

- **One run at a time**: guarded by the database (partial unique index: one `IN_PROGRESS` row), not by JVM locks.
- **Update in chunks**: ids loaded once, split into `balance-update.chunk-size` chunks, processed on a bounded
  platform-thread pool (`balance-update.threads` ≤ Hikari pool size). One transaction per chunk,
  `SELECT … FOR UPDATE` on the chunk, JDBC batching (`hibernate.jdbc.batch_size`) for the UPDATEs.
- **GET paging**: one SQL statement per page — window function (`row_number()`) picks the page of deposits,
  `LEFT JOIN` withdrawals into a flat DTO projection, grouped in Java. Never combine a collection `JOIN FETCH`
  with `Pageable` (in-memory paging, HHH90003004).
- Money: `BigDecimal` with scale 2 (`HALF_UP`) everywhere outside the kata's `TimeDeposit` (which uses `Double`).

## Conventions

- Records for DTOs and read models; Lombok only for entities and constructor injection.
- Tests: `// given // when // then`, `implements WithAssertions`, method names `shouldXxxWhenYyy`.
  Positive **and** negative cases for every change.
- Database behaviour is tested against PostgreSQL via Testcontainers (`TestcontainersConfiguration`), never H2.
- Code that commits its own transactions (chunk updates, `tryStart`) is tested with
  `@Transactional(propagation = NOT_SUPPORTED)` and explicit cleanup.
- Performance claims (no N+1, batching) are proven with Hibernate statistics (`getPrepareStatementCount`).
- Liquibase: changesets `001`–`003` are applied — **never edit them**; add `004-…` instead.

## Workflow

1. **Plan first** for anything with 3+ steps or a design decision (plan mode). State assumptions.
2. **Implement** the smallest change that solves the task (delegate bigger Java work to `spring-boot-java-engineer`).
   Tests for it come from `test-engineer`: an agent that did not write the code tests it. When packages or layers
   change, `architecture-guardian` updates the ArchUnit rules (`ArchitectureTest`).
3. **Verify** with `/verify` — never report a task as done without a green build. The Stop hook blocks finishing
   while the code does not compile.
4. **Independent review** with `/review` (fresh-context `code-reviewer`; it did not write the code).
5. Human reviews the diff and commits. **Atomic commits**, Conventional Commits style
   (`feat(api): …`, `test(infra): …`, `refactor(domain): …`). Claude never pushes.

## Principles

- **Simplicity first**: minimal diff, no speculative abstractions (YAGNI). Ask "would a senior reviewer approve this?"
- **Root causes, no workarounds**: never weaken, skip or delete a test to get a green build.
- **Verify claims**: check library behaviour against official docs or a test, not memory
  (e.g. Hibernate 7 changed `StatelessSession` batching).
- When corrected by the human, record the lesson in agent memory so it does not happen again.
