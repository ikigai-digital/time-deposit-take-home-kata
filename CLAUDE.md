# Project rules for AI agents

These rules are loaded automatically by Claude Code in this repository.

## Scope

- The solution lives in `java/`. Do not change the other language folders.
- Exactly two REST endpoints: `GET /time-deposits` and `POST /time-deposits/balance-updates`. Never add another endpoint.

## Hard constraints

- `org.ikigaidigital.TimeDeposit` is a shared class: do not change its fields, constructor or methods.
- `TimeDepositCalculator.updateBalance(List<TimeDeposit>)` keeps its signature, the no-argument constructor and its exact numeric behaviour (double arithmetic, interest rounded HALF_UP to 2 decimals).
- `LegacyTimeDepositCalculator` in test sources is a verbatim copy of the original algorithm. Never edit it: it is the reference for the characterization tests.

## Architecture

- Hexagonal: `domain` (no framework code) -> `application` (use cases and ports) -> `adapter/in/rest`, `adapter/out/persistence`. Adapters depend on ports, never the other way round.
- Contract first: change `java/src/main/resources/static/openapi/time-deposits.yaml`, then implement the generated `TimeDepositsApi` interface. Do not hand-write DTOs.
- New interest rules go into a new `InterestPolicy` implementation registered in `InterestCalculationConfiguration`, not into `TimeDepositCalculator`.
- Schema changes go through a new Flyway migration in `db/migration`. Never edit an applied migration.

## Code style

- No comments of any kind in code, config or tests. Names carry the meaning, and assumptions are documented in `java/README.md`.
- Names say what a thing does without opening it (`interestPoliciesByPlanType`, not `map`). No `e`, `ex`, `t` variables.
- Catch only specific exceptions and keep try blocks to the throwing call.
- Tests: behaviour names (`shouldApplyMonthlyInterest...`) and a plain-sentence `@DisplayName`.

## Workflow

1. Before writing code, describe the change per file and wait for approval.
2. Change behaviour test first. For refactoring, keep the characterization tests green.
3. Run `./mvnw verify` in `java/` (needs a running Docker engine) before reporting a task as done, and report failures as they are.
4. One logical change per commit, English message in the imperative mood. Never push without an explicit approval.
