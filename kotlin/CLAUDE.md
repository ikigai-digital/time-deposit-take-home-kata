# CLAUDE.md — agent rules for the Kotlin solution

Project instructions loaded automatically by Claude Code when working in `kotlin/`.

## Context
- Take-home kata: XA Bank time deposits. Requirements: `../README.md`.
- Stack: Kotlin 1.9, Spring Boot 3.3, Maven, PostgreSQL, Flyway, JPA, Testcontainers, OpenAPI (contract-first).

## Hard constraints (never break)
- `org.ikigaidigital.TimeDeposit`: do not change fields, types, package or constructor.
- `TimeDepositCalculator.updateBalance(xs: List<TimeDeposit>)`: signature and behaviour fixed.
  `TimeDepositCalculatorTest` is the characterization suite — it must stay green unchanged
  unless a test is added. Any refactoring of the calculator runs it first and last.
- Exactly two endpoints: `GET /time-deposits`, `POST /time-deposits/balance-updates`. No others.
- No input validation / error handling work (out of scope per requirements).

## Architecture (hexagonal)
- Domain (framework-free): `org.ikigaidigital` root + `org.ikigaidigital.interest`.
- Application: `application.port.inbound` (use cases), `application.port.outbound` (repositories), `application.service`.
- Adapters: `adapter.inbound.web` (implements generated `TimeDepositsApi`), `adapter.outbound.persistence` (Spring Data JPA).
- Dependencies point inwards only. Domain never imports Spring/JPA.
- Interest plans live in the `interestPlans` table. A rule of a new shape = new `InterestPolicy`; no `if/else` on plan types.

## Contract-first
- Edit `src/main/resources/static/openapi.yaml` first; the API interface and DTOs are generated
  (`mvn generate-sources`). Never hand-write controllers that bypass the generated interface.
- Package names must not contain Kotlin keywords (`in`, `out` broke the generator → `inbound`/`outbound`).

## Workflow
- Run: `mvn spring-boot:run` (profile `local`, in-memory H2). Tests: `mvn verify` (Testcontainers PostgreSQL, needs Docker).
- Fetch library docs through the Context7 MCP server instead of relying on memory.
- Money: `BigDecimal` everywhere; `Double` only as input to the calculator via `TimeDeposit` (one conversion in `TimeDepositRecord`).
- No code comments. Assumptions are listed only in README.md ("Assumptions").
- Balance update: batch = one transaction, `FOR UPDATE SKIP LOCKED`, update deposits one by one in code; once per deposit per month. GET stays paginated (`page`/`size`, response `{items, page, size, totalElements, totalPages}`).
