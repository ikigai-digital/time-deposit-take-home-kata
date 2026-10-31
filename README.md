# Time Deposit Refactoring Kata - Take-Home Assignment

## Java implementation guide

The Java implementation in `java/` preserves the shared `TimeDeposit` API and calculator behavior, adds PostgreSQL persistence, and exposes two business operations. The original assignment is retained below this guide.

### Prerequisites and test commands

- JDK 17 or newer, with Java available to the build.
- Network access on first use to download the pinned Maven distribution and dependencies.
- A running Docker-compatible container runtime for integration tests.
- Docker Compose for the optional manual PostgreSQL setup below.

Run commands from the Java project folder:

```text
cd java
```

| Purpose | Windows PowerShell | POSIX shell | Docker needed? |
|---|---|---|---|
| Unit tests | `.\mvnw.cmd test` | `sh ./mvnw test` | No |
| Full verification | `.\mvnw.cmd verify` | `sh ./mvnw verify` | Yes |
| Build the executable JAR with unit tests | `.\mvnw.cmd package` | `sh ./mvnw package` | No |

The wrapper pins Maven 3.9.15; no separate Maven installation is required. There are 21 calculator tests and 9 integration tests. Failsafe runs the `*IT` suite during `verify`; it starts its own temporary PostgreSQL database, independently of Compose, and fails if the runtime is unavailable rather than silently skipping. Integration tests exercise migration, real HTTP/SQL, decimal/date mapping, updates, history preservation, rollback, concurrency, and the OpenAPI contract.

The suite includes simultaneous updates that must retain both interest credits.

Windows wrapper commands are verified. The generated POSIX script passed syntax checking; a Unix runtime build has not been performed in this environment.

### Start PostgreSQL and the application

Choose a local database password in the example below. Set `DB_PORT` to another available port, such as `5433`, if `5432` is already in use.

Windows PowerShell, from `java/`:

```powershell
$env:DB_USERNAME = "kata"
$env:DB_PASSWORD = "choose-a-local-password"
$env:DB_PORT = "5432"
$env:DB_URL = "jdbc:postgresql://localhost:$($env:DB_PORT)/time_deposits"

docker compose up -d --wait
.\mvnw.cmd package
java -jar .\target\time-deposit-kata-1.0-SNAPSHOT.jar
```

POSIX shell equivalent:

```sh
export DB_USERNAME=kata
export DB_PASSWORD='choose-a-local-password'
export DB_PORT=5432
export DB_URL="jdbc:postgresql://localhost:${DB_PORT}/time_deposits"

docker compose up -d --wait
sh ./mvnw package
java -jar ./target/time-deposit-kata-1.0-SNAPSHOT.jar
```

The application runs in the foreground on port 8080. Keep that terminal open. Flyway applies the migration at startup; no manual table creation is needed. If the HTTP port is occupied, append `--server.port=8081` to the launch command and use that port in the URLs below.

Compose starts only PostgreSQL 17, bound to localhost. Its named volume retains data across container recreation. Credentials initialize a fresh volume; reuse those credentials when reusing the database. The application reads `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` from its launching environment. An existing fresh PostgreSQL database can be used instead: set those variables appropriately and omit the Compose command.

### Load optional examples

After the app has started successfully, open a second terminal in `java/` and repeat the same environment-variable settings there so Compose resolves the same connection configuration.

PowerShell:

```powershell
Get-Content -Raw .\sample-data.sql |
    docker compose exec -T postgres psql -U $env:DB_USERNAME -d time_deposits -v ON_ERROR_STOP=1
```

POSIX shell:

```sh
docker compose exec -T postgres psql -U "$DB_USERNAME" -d time_deposits -v ON_ERROR_STOP=1 < sample-data.sql
```

The script inserts six deposits and three withdrawals with fixed example IDs. Repeating it skips existing IDs and does not reset credited balances or withdrawal history. It is loaded manually, never automatically at startup.

On a fresh sample database, one update produces:

| ID | Plan | Days | Initial balance | Balance after one update |
|---|---|---:|---:|---:|
| 1 | basic | 31 | 1200.00 | 1201.00 |
| 2 | student | 365 | 1200.00 | 1203.00 |
| 3 | premium | 46 | 1200.00 | 1205.00 |
| 4 | basic | 30 | 1200.00 | 1200.00 |
| 5 | student | 366 | 1200.00 | 1200.00 |
| 6 | premium | 45 | 1200.00 | 1200.00 |

### Call the two operations with Bruno

The [Bruno collection](bruno/) contains one `.bru` file per endpoint and a `Local` environment:

- `Get Time Deposits.bru`: `GET /time-deposits`, with status and response-shape checks.
- `Update Balances.bru`: bodyless `POST /time-deposits/update-balances`, with 204/empty-response checks.

After starting the application, use **Open Collection** in Bruno and select the repository's `bruno/` folder, which contains `bruno.json`. Select **Local**, whose `baseUrl` is `http://localhost:8080`. Change that variable if using another HTTP port.

Send **Get Time Deposits**, then **Update Balances**, then **Get Time Deposits** again. A fresh empty database returns `[]`; load the optional examples above to see changing balances. The request's tests check its response automatically. Repeated updates can credit interest again.

If the Bruno CLI is installed, run from the repository's `bruno/` folder:

```text
bru run --env Local
```

That collection run executes GET followed by POST, so it changes balances. To include the final GET in one CLI invocation, use:

```text
bru run "Get Time Deposits.bru" "Update Balances.bru" "Get Time Deposits.bru" --env Local
```

The collection and GET → POST → GET command were verified with Bruno CLI 4.3.0 against empty and sample databases. See the [Bruno CLI reference](https://docs.usebruno.com/bru-cli/run/overview) for environment overrides and other options. Bruno is optional; the Java build does not require Node.js or Bruno.

### Call the two operations through Swagger

Open [Swagger UI](http://localhost:8080/swagger-ui/index.html). The generated contract is at [OpenAPI JSON](http://localhost:8080/v3/api-docs).

1. Expand `GET /time-deposits`, choose **Try it out**, then **Execute**. Expect `200` with an array; a new database without sample data returns `[]`.
2. Expand `POST /time-deposits/update-balances`, choose **Try it out**, then **Execute**. It needs no request body and returns `204` with no response body.
3. Execute GET again to see the persisted balances. Repeated POST calls apply another calculation.

| Operation | Response |
|---|---|
| `GET /time-deposits` | `200`, deposit array with `id`, `planType`, `balance`, `days`, `withdrawals` |
| `POST /time-deposits/update-balances` | `204`, after updating all balances |

Each withdrawal contains `id`, `amount`, and an ISO calendar `date`. Deposits and their withdrawals are ordered by ID; missing history is an empty array.

Stop the app with Ctrl+C. `docker compose down` stops/removes the manual database container and network while retaining its data volume. Start it again with `docker compose up -d --wait`.

### Architecture and assumptions

- **Domain:** plain Java interest policies and the existing calculator. The calculator retains its original package for compatibility, default constructor, and `updateBalance(List<TimeDeposit>)` signature.
- **Application:** one service coordinates retrieval or read → calculate → save; one storage interface defines its needs. View records are shared by reads and HTTP responses.
- **Adapters:** a controller translates HTTP; one JdbcTemplate adapter implements the storage interface. PostgreSQL-specific SQL stays in that adapter.
- **Transactions:** the managed service wraps the complete balance update. Calculation reads lock rows in ID order until commit/rollback, so overlapping update requests retain both interest credits. Integration tests verify concurrent updates and rollback after successful writes followed by a database failure.

JDBC was chosen for the small set of explicit read/update queries. JPA is a valid alternative, but is not preferred over JDBC in the assignment. The service depends on its storage port rather than JDBC types.

To preserve the original functioning calculator despite the README's ambiguous monthly-rate wording, each update retains `balance * rate / 12`: basic 1% after day 30, student 3% on days 31–365, premium 5% after day 45. The division is consistent with distributing an annual rate monthly, but the original developer's intention is not established.

Plan matching remains case-sensitive, unknown plans earn no interest, and each call uses current balances without advancing days or enforcing elapsed months. Existing double arithmetic and `new BigDecimal(interest).setScale(2, HALF_UP)` are preserved; only interest is rounded. PostgreSQL stores decimal amounts without imposing another fixed scale; the shared Double model is used at the calculation boundary.

Withdrawals are historical data and cause no additional deductions. IDs are supplied by the manual SQL/test fixtures. The two-endpoint limit is interpreted as two business operations; Swagger/OpenAPI and framework infrastructure routes are excluded. No creation API, authentication, scheduling, custom validation, or exception layer is added.

### AI-assisted development

[AI_WORKFLOW.md](AI_WORKFLOW.md) describes the tools, assistance, review process, and verification. [AGENTS.md](AGENTS.md) contains the project instructions for coding agents.

---

## XA Bank Time Deposit

### Context
A junior developer implemented domain logic for a time deposit system but did not complete the API functionality. Your task is to refactor the existing codebase to implement all required functionalities based on the provided business requirements, ensuring no breaking changes occur.

### Requirements

1. **API Endpoints**:
    - Create a RESTful API endpoint to update the balances of all time deposits in the database.
    - Create a RESTful API endpoint to retrieve all time deposits.
        - The GET endpoint should return a list of all time deposits with the following schema:
            - `id`
            - `planType`
            - `balance`
            - `days`
            - `withdrawals`

2. **Database Setup**:
    - Store all time deposit plans in a database.
    - Define the following tables:
        - `timeDeposits`:
            - `id`: Integer (primary key)
            - `planType`: String (required)
            - `days`: Integer (required)
            - `balance`: Decimal (required)
        - `withdrawals`:
            - `id`: Integer (primary key)
            - `timeDepositId`: Integer (foreign key, required)
            - `amount`: Decimal (required)
            - `date`: Date (required)

3. **Interest Calculation**:
    - Implement logic to calculate monthly interest based on the plan type:
        - **Basic Plan**: 1% interest
        - **Student Plan**: 3% interest (no interest after 1 year)
        - **Premium Plan**: 5% interest (interest starts after 45 days)
    - No interest is applied for the first 30 days for any existing plans.

4. **Refactoring Constraints**:
    - Do not introduce breaking changes to the shared `TimeDeposit` class or modify the `updateBalance` method signature.
    - Ensure the design is extensible to accommodate future complexities in interest calculations.

5. **Code Quality**:
    - Adhere to SOLID principles, design patterns, and clean code practices where applicable.

6. **AI-Assisted Development**:
    - Set up an AI harness or agent workflow and use it throughout the development for this take-home exercise.
    - Briefly document the tools and setup used (e.g., LLMs, coding assistants, agentic frameworks, configuration).
    - Ensure your AI setup is practical and reproducible.
    - Include any custom rules, system prompts, or agent configurations used.
    - Include a brief summary of which parts of the solution were AI-assisted and why.

### Important Guidelines
- The existing `TimeDepositCalculator.updateBalance` method is functioning correctly. Ensure its behavior remains unchanged after refactoring.
- The final solution must include **exactly two API endpoints**. Do not develop additional endpoints.
- **Do not** create a pull request or a new branch in the ikigai-digital repository. Instead, fork the repository into your own GitHub repository and develop the solution there.
- Handling invalid input or exceptions is not required.
- Use any tools, frameworks, or libraries you find suitable.
- In case of ambiguity, make logical assumptions and justify them in code comments.

### Preferred Stack
- Use an OpenAPI Swagger contract.
- Embrace Hexagonal Architecture.
- Follow atomic commit practices.
- Utilize testcontainers.
- Leverage AI-assisted development tools for code generation, testing, and refactoring.

### Submission Instructions
- Provide clear instructions on how to trigger the endpoints using the Swagger contract.
- Email the link to your public GitHub repository.
