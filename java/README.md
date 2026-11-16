# Time Deposit API (Java solution)

Spring Boot 3.5 service on Java 17 that stores time deposits in PostgreSQL. It has exactly two REST endpoints: one returns all time deposits, the other applies one month of interest to every balance.

## Prerequisites

- JDK 17 or newer
- A running Docker engine (Docker Desktop on Windows/macOS). It is used both for running the app (Docker Compose) and for the integration tests (Testcontainers).

Maven does not need to be installed: the project comes with the Maven Wrapper (`mvnw` / `mvnw.cmd`).

## Run the tests

```bash
cd java
./mvnw verify
```

`verify` runs:

- 628 characterization tests in `TimeDepositCalculatorTest`. They compare the refactored calculator with a verbatim copy of the original implementation (`LegacyTimeDepositCalculator`, test sources only) across all plan types, boundary day counts and balances, and assert exact `double` equality. They also check a set of hand-calculated expected balances.
- Unit tests for the interest policies and the application service.
- `TimeDepositApiIntegrationTest`: both endpoints end to end against PostgreSQL 16 started by Testcontainers, including Flyway migrations.

## Run the application

```bash
cd java
./mvnw spring-boot:run
```

`spring-boot-docker-compose` starts PostgreSQL from `compose.yaml` and wires the datasource to it automatically. The container port is published on a random free host port, so a local PostgreSQL on 5432 does not conflict. Flyway creates the schema and inserts sample data (6 deposits, 4 withdrawals).

To use an existing database instead, start the app with `-Dspring.docker.compose.enabled=false` and set `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`.

## Trigger the endpoints with Swagger

1. Start the application (see above).
2. Open Swagger UI: <http://localhost:8080/swagger-ui/index.html>. It loads the OpenAPI contract at <http://localhost:8080/openapi/time-deposits.yaml>.
3. **Get all time deposits:** expand `GET /time-deposits`, click **Try it out**, then **Execute**. The response lists every deposit with `id`, `planType`, `balance`, `days` and `withdrawals`.
4. **Update all balances:** expand `POST /time-deposits/balance-updates`, click **Try it out**, then **Execute**. The response is `204 No Content`.
5. Run `GET /time-deposits` again to see the new balances.

With the sample data, one balance update gives:

| id | plan | days | balance before | balance after | reason |
|---:|---|---:|---:|---:|---|
| 1 | basic | 45 | 10000.00 | 10008.33 | 1% / 12 |
| 2 | basic | 20 | 5000.00 | 5000.00 | first 30 days, no interest |
| 3 | student | 200 | 20000.00 | 20050.00 | 3% / 12 |
| 4 | student | 400 | 15000.00 | 15000.00 | no interest after 1 year |
| 5 | premium | 40 | 30000.00 | 30000.00 | interest starts after 45 days |
| 6 | premium | 120 | 1234567.00 | 1239711.03 | 5% / 12 |

The same calls with curl:

```bash
curl http://localhost:8080/time-deposits
```

```bash
curl -X POST -i http://localhost:8080/time-deposits/balance-updates
```

## Architecture

Hexagonal (ports and adapters), contract first:

```
org.ikigaidigital
├── TimeDeposit, TimeDepositCalculator      shared classes, public API unchanged
├── domain
│   ├── Withdrawal
│   └── interest                            InterestPolicy strategy + one class per plan
├── application
│   ├── port/in                             GetAllTimeDepositsUseCase, UpdateAllBalancesUseCase
│   ├── port/out                            TimeDepositRepository, WithdrawalRepository
│   └── TimeDepositService                  implements both use cases
├── adapter
│   ├── in/rest                             controller implementing the generated TimeDepositsApi
│   └── out/persistence                     JPA entities, Spring Data repositories, port adapters
└── config                                  wiring of interest policies into the calculator
```

- **OpenAPI contract:** `src/main/resources/static/openapi/time-deposits.yaml`. `openapi-generator-maven-plugin` generates the `TimeDepositsApi` interface and the DTOs at build time, and the controller implements that interface. The same file is served to Swagger UI, so the documentation always matches the code.
- **Interest calculation:** `TimeDepositCalculator.updateBalance(List<TimeDeposit>)` keeps its signature, its no-argument constructor and its exact arithmetic. It now looks up an `InterestPolicy` by plan type (Strategy pattern) instead of an if/else chain. `MonthlyRateInterestPolicy` is a template for the existing plans: it owns the shared "no interest in the first 30 days" rule and the `rate / 12` formula, and each plan adds only its own restriction (`StudentInterestPolicy`: until day 365, `PremiumInterestPolicy`: from day 46).
- **Adding a plan:** implement `InterestPolicy` (or extend `MonthlyRateInterestPolicy`) and register it as a bean in `InterestCalculationConfiguration`. The calculator, the service and the adapters do not change (Open/Closed principle).
- **Persistence:** Flyway owns the schema (`db/migration`), and Hibernate only validates it (`ddl-auto: validate`). The balance update runs in one transaction: load all deposits, apply the calculator, write the new balances back.

## Assumptions

The task asks for assumptions to be justified in code comments. This codebase follows a no-comments rule, so they are collected here instead.

1. **Language:** the Java version of the kata was chosen. The other language folders are left as they were.
2. **Table and column names:** the README names tables `timeDeposits` / `withdrawals` and columns such as `planType` and `timeDepositId`. They are created in PostgreSQL snake_case (`time_deposits`, `plan_type`, `time_deposit_id`), because unquoted camelCase identifiers are folded to lower case in PostgreSQL and quoted ones are awkward to use. The JSON field names follow the README exactly.
3. **Balance update endpoint:** `POST /time-deposits/balance-updates` with `204 No Content`. Every call applies another month of interest, so the operation is neither safe nor idempotent, which rules out `PUT` and `PATCH`. It has no request body, because the README asks to update all deposits.
4. **Behaviour preservation:** `updateBalance` is the specification. The refactored code keeps the original `double` arithmetic (`balance * rate / 12`), rounds only the interest (`HALF_UP`, 2 decimals) and adds it to the unrounded balance. Unknown plan types get no interest, exactly as before. The characterization tests prove this.
5. **"No interest after 1 year" for students** means interest up to day 365 inclusive (the original `days < 366`). **"Interest starts after 45 days" for premium** means from day 46 (the original `days > 45`).
6. **Money in storage and API:** balances are `NUMERIC(19,2)` in the database and decimal numbers with 2 fraction digits in JSON. The shared `TimeDeposit` class must keep its `Double` balance, so the conversion to and from `BigDecimal` happens in the adapters (`HALF_UP`, scale 2).
7. **Withdrawals** are read only. The README does not say they affect interest, so the calculator ignores them, as the original did. The `withdrawals` items in the response contain `id`, `amount` and `date`. `timeDepositId` is omitted, because each withdrawal is already nested under its deposit.
8. **`days`** is stored as given and not increased by the balance update, because the original calculator does not change it either.
9. **Sample data** is inserted by a Flyway migration (`V2`) so both endpoints can be tried right after start.
10. **Scale:** the balance update loads all deposits in one transaction, which fits the kata. For millions of rows it would be processed in pages or batches.
