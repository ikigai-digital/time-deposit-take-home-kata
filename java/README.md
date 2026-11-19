# XA Bank Time Deposit Service

Spring Boot service for the XA Bank time deposit kata. It exposes exactly two endpoints:

| Method | Endpoint | What it does |
|---|---|---|
| `POST` | `/api/time-deposits/balance-updates` | Starts the monthly interest update of **all** time deposits (asynchronous) |
| `GET` | `/api/time-deposits?page=0&size=100` | Lists all time deposits with their withdrawals, page by page |


## Tech stack

Java 21 · Spring Boot 4.1 · Spring Data JPA / Hibernate 7 · PostgreSQL · Liquibase · springdoc-openapi (OpenAPI 3.1) ·
Lombok · JUnit 5 · AssertJ · Mockito · Testcontainers · Awaitility

## Prerequisites

- Java 21
- Maven 3.9+
- Docker, running. The `dev` profile starts PostgreSQL with Docker Compose, and the tests use Testcontainers.

## Running the service

### 1. Set the database credentials

The `dev` profile starts PostgreSQL from `docker-compose.yaml` and connects to it with the credentials in
`src/main/resources/application-dev.yaml`. Before the first run, set `username` and `password` in
`application-dev.yaml` to the values of `POSTGRES_USER` and `POSTGRES_PASSWORD` from `docker-compose.yaml`:

```yaml
# src/main/resources/application-dev.yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/time_deposit
    username: <POSTGRES_USER from docker-compose.yaml>
    password: <POSTGRES_PASSWORD from docker-compose.yaml>
```

### 2. Start the service with the `dev` profile

Run it from the directory that contains `pom.xml` and `docker-compose.yaml`:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Spring Boot starts the PostgreSQL container, and Liquibase creates the tables. The service listens on port `8080`
under the context path `/time-deposit-service`.

### 3. Open Swagger UI

**http://localhost:8080/time-deposit-service/swagger-ui/index.html**

To call an endpoint from Swagger UI:
1. Expand the endpoint under **Time deposits**.
2. Click **Try it out**, fill in the parameters if needed (for `GET`: `page`, `size`), and click **Execute**.
3. The response code and body are shown below the request.

The OpenAPI contract is at http://localhost:8080/time-deposit-service/v3/api-docs.

## Sample data (optional)

The database starts empty. To try the endpoints with data, run this SQL against the `time_deposit` database,
for example from the IntelliJ Database tool using the credentials above:

```sql
INSERT INTO time_deposits (id, plan_type, days, balance) VALUES
    (1, 'basic',   60,  1000.00),
    (2, 'student', 100, 2000.00),
    (3, 'premium', 90,  3000.00);

INSERT INTO withdrawals (id, time_deposit_id, amount, date) VALUES
    (1, 1, 100.00, DATE '2026-02-01'),
    (2, 1,  50.00, DATE '2026-01-10'),
    (3, 3, 300.00, DATE '2026-03-05');
```

## Calling the API with curl

All examples use the sample data above.

### List time deposits

```bash
curl -s "http://localhost:8080/time-deposit-service/api/time-deposits?page=0&size=2"
```

`200 OK`:

```json
{
  "content": [
    {
      "id": 1,
      "planType": "basic",
      "balance": 1000.00,
      "days": 60,
      "withdrawals": [
        { "id": 2, "amount": 50.00, "date": "2026-01-10" },
        { "id": 1, "amount": 100.00, "date": "2026-02-01" }
      ]
    },
    {
      "id": 2,
      "planType": "student",
      "balance": 2000.00,
      "days": 100,
      "withdrawals": []
    }
  ],
  "page": 0,
  "size": 2,
  "totalElements": 3,
  "totalPages": 2
}
```

| Parameter | Default | Meaning |
|---|---|---|
| `page` | `0` | Zero-based page number |
| `size` | `100` | Number of time deposits per page |

Deposits are ordered by `id`; the withdrawals of each deposit are ordered by `date`.

### Start the monthly interest update

```bash
curl -i -X POST "http://localhost:8080/time-deposit-service/api/time-deposits/balance-updates"
```

`202 Accepted`: the update has started in the background.

```json
{ "processId": 1, "status": "IN_PROGRESS", "message": "Balance update started" }
```

`409 Conflict`: an update is already running, and nothing new was started.

```json
{ "processId": null, "status": "IN_PROGRESS", "message": "Balance update is already in progress" }
```

After the update finishes (instant for the sample data), `GET` shows the new balances:

```bash
curl -s "http://localhost:8080/time-deposit-service/api/time-deposits?page=0&size=100"
```

| Deposit | Plan | Days | Balance before | Monthly interest | Balance after |
|---|---|---|---|---|---|
| 1 | basic | 60 | 1000.00 | 1000 × 1% / 12 = 0.83 | **1000.83** |
| 2 | student | 100 | 2000.00 | 2000 × 3% / 12 = 5.00 | **2005.00** |
| 3 | premium | 90 | 3000.00 | 3000 × 5% / 12 = 12.50 | **3012.50** |

The status of every run (`IN_PROGRESS`, `COMPLETED`, `FAILED`) is stored in the `balance_update_processes` table.

## Interest rules

| Plan | Annual rate (monthly = rate / 12) | Rule |
|---|---|---|
| basic | 1% | after 30 days |
| student | 3% | after 30 days, no interest after 1 year (365 days) |
| premium | 5% | starts after 45 days |

No plan earns interest in the first 30 days. Each plan is a separate `InterestPolicy`, so a new plan is a new class,
with no changes to existing ones.

## Running the tests

```bash
mvn clean verify
```

Docker must be running: repository, end-to-end and concurrency tests run against a real PostgreSQL in
Testcontainers.

## Architecture

Hexagonal (ports and adapters), base package `org.ikigaidigital.timedeposit`:

| Package | Responsibility |
|---|---|
| `api` | REST controller, OpenAPI contract (`TimeDepositApi`), DTOs, `PageDto` |
| `domain` | Use cases (`BalanceUpdateService`, `TimeDepositQueryService`), interest calculation and policies, repository interfaces, domain model. No JPA. |
| `infrastructure` | JPA entities, Spring Data repositories (JPQL), implementations of the domain repository interfaces |
| `config` | Configuration properties (`balance-update.*`), executor, OpenAPI |

## Design decisions and assumptions

- **The balance update is asynchronous (`202 Accepted`).** Updating every deposit can take longer than an HTTP
  request should. The kata allows only two endpoints, so there is no status endpoint: the run status is stored in
  the database, and calling `POST` again tells you whether a run is still in progress.
- **Only one update runs at a time, guarded by the database.** A partial unique index allows a single
  `IN_PROGRESS` row in `balance_update_processes`, so the guard also works across several instances of the service.
  A second concurrent `POST` gets `409 Conflict`.
- **Chunked, parallel processing.** Deposit ids are split into chunks (`balance-update.chunk-size`, default 1000),
  processed on a bounded thread pool (`balance-update.threads`, default 4). Each chunk runs in its own transaction
  with `SELECT … FOR UPDATE`, so concurrent changes are not lost, and its UPDATEs are sent as JDBC batches.
- **The GET endpoint is paged.** A single list of all deposits with all withdrawals would grow with the table.
  Each page is loaded with one SQL statement: a window function picks the page of deposits, and their withdrawals
  are joined into a DTO projection, so there is no N+1 problem.

## AI-assisted development

This project was developed with Claude as an AI pair programmer:

- **Claude (chat)** was used for design discussions and research: the concurrency guard, the chunked update,
  JPA paging without N+1, JDBC batching vs `StatelessSession`, and the test strategy. All design decisions were
  made and reviewed by me.
- **Claude Code** is set up in the repository root and versioned with the code, so the setup is reproducible:
  - `CLAUDE.md`: project rules (kata constraints, architecture, conventions, workflow);
  - `.claude/agents/`: `spring-boot-java-engineer`, `test-engineer`, `architecture-guardian` (ArchUnit rules),
    and a read-only `code-reviewer`;
  - `.claude/skills/`: `/verify` (full build and tests), `/review` (independent review of the diff), `/kata-check`
    (checks the solution against the assignment);
  - `.claude/settings.json`: permissions, and a hook that prevents Claude from finishing while the code does not
    compile.

Every AI-generated change was reviewed, compiled and tested before it was committed.
