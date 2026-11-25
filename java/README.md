# XA Bank Time Deposit – Java solution

Spring Boot 4 · Java 17 · PostgreSQL 17 · Flyway · springdoc-openapi (Swagger) · Testcontainers · hexagonal architecture.

The assignment is in the [root README](../README.md). This document covers how to run the solution and how it is designed. How AI was used to build it is described in [AI_ASSISTED_DEVELOPMENT.md](AI_ASSISTED_DEVELOPMENT.md).

## Running it

### Prerequisites
- JDK 17 or newer (`JAVA_HOME` set)
- Docker (for the local database and for the integration tests)

Maven itself does not need to be installed. The Maven wrapper (`./mvnw`) downloads it.

### 1. Start the database
```bash
cd java
docker compose -f src/main/resources/database/postgres-compose.yaml up -d --wait
```
This starts PostgreSQL on `localhost:5432` (database, user and password are all `timedeposit`; you can override them with `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`).

### 2. Start the application
```bash
./mvnw spring-boot:run
```
On startup, Flyway creates the schema and loads 7 sample time deposits with withdrawals. The app listens on `http://localhost:8080`.

### 3. Trigger the endpoints with Swagger
Open **http://localhost:8080/swagger-ui.html**.

| Endpoint | What it does | Response |
|---|---|---|
| `GET /time-deposits` | Lists all time deposits (`id`, `planType`, `balance`, `days`, `withdrawals`) | `200` with a JSON array |
| `POST /time-deposits/balance-updates` | Applies one month of interest to every time deposit | `204`, no body |

In Swagger UI: expand an endpoint, click **Try it out**, then **Execute**. A typical check:
1. `GET /time-deposits` shows the starting balances (e.g. deposit 1: `10000.00`).
2. `POST /time-deposits/balance-updates` returns `204`.
3. `GET /time-deposits` again shows deposit 1 at `10008.33`, deposit 3 at `5012.50` and deposit 6 at `20083.33`. The others don't change; why is documented in `src/main/resources/db/sample-data/V2__sample_time_deposits.sql`.

The same with curl:
```bash
curl -s localhost:8080/time-deposits
curl -s -X POST -i localhost:8080/time-deposits/balance-updates
```

The **OpenAPI contract** is served at `/v3/api-docs` (JSON) and `/v3/api-docs.yaml` (YAML).

The balance update is **not idempotent**: each POST adds another month of interest. To reset the data, run `docker compose -f src/main/resources/database/postgres-compose.yaml down -v` and start again.

### Running the tests
```bash
./mvnw test
```
Integration tests start their own PostgreSQL through Testcontainers, so the compose database doesn't need to be running. Without Docker, those tests are skipped instead of failing.

## Design

```
adapter.in.web            REST controller + response DTOs (OpenAPI annotations)
        │ uses
application.port.in       GetAllTimeDepositsUseCase, UpdateAllBalancesUseCase
application.service       use case implementations (transaction boundaries)
        │ uses
application.port.out      TimeDepositRepository, WithdrawalRepository
        ▲ implemented by
adapter.out.persistence   JPA entities, Spring Data repositories, port adapters
domain (org.ikigaidigital) TimeDeposit, Withdrawal, PlanType, TimeDepositCalculator
config                    Spring wiring of the core, OpenAPI metadata
```

- **Hexagonal architecture.**
  - Dependencies point inwards.
  - The domain and application layers have no Spring stereotypes; beans are created in `config.ApplicationConfiguration`.
  - JPA entities and web DTOs stay inside their adapters.
- **Interest calculation.**
  - Each plan is a `PlanType` constant that holds its rate and eligibility rule (strategy pattern).
  - A new plan, or a more complex rule, is a new constant or method. The calculator stays unchanged (open/closed principle).
- **Legacy constraints.**
  - `TimeDeposit` and the `updateBalance` signature are unchanged.
  - Characterization tests (`TimeDepositCalculatorTest`) were written *before* refactoring to lock in the existing behaviour.
- **Money.**
  - The legacy domain uses `Double`; everything new (`Withdrawal`, the database, JSON) uses decimals.
  - Converting from `Double` happens at the adapter boundaries with `BigDecimal.valueOf` and HALF_UP rounding to 2 places, so clients see `10008.33`, never `10008.329999999998`.
- **Database.**
  - The schema is owned by Flyway; Hibernate only validates it.
  - Table and column names are snake_case versions of the README names (`timeDeposits` → `time_deposits`). The reason is explained in the migration.

### Assumptions
The ambiguous points are documented in code comments, next to the code they affect:
- **Interest is monthly.** The rate is annual and applied as rate / 12 per update. This keeps the legacy behaviour.
- **Unknown plan types earn no interest.** This also keeps the legacy behaviour, so the database has no CHECK constraint on plan type.
- **Withdrawals are informational.** The README doesn't say they affect interest.
- **The update endpoint is `POST`.** It's a command that isn't idempotent, and it returns no body because the README allows only two endpoints.
- **Only the balance is written back.** `saveAll` changes only the balance; plan type and days are never overwritten.

## AI-assisted development
The tools, setup, configuration, and which parts were AI-assisted and why: see [AI_ASSISTED_DEVELOPMENT.md](AI_ASSISTED_DEVELOPMENT.md).
