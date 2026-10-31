# AI-assisted development

## Tools and setup

Used **Codex Desktop** for repository inspection, implementation, tests, documentation, and code review, with human direction and review. PowerShell, Git, and `rg` supported local development. One read-only reviewer agent assessed the implementation; its findings were checked against the code and runtime behavior before correction.

The application is Java 17/Spring Boot with PostgreSQL, Flyway, and JDBC. Maven Wrapper pins Maven 3.9.15. Testcontainers provides the integration database; Docker Compose supports optional manual execution. No model API, agent framework, or AI credential is needed to build or run the application.

Project-owned custom instructions are included in [AGENTS.md](AGENTS.md). They require preserving the original APIs/calculator, keeping changes small, recording assumptions, testing behavior, and reporting verified results. Session guidance also emphasized planning, surgical changes, test-first bug fixes, and verification before completion. Host-installed workflow aids included `executing-plans`, `karpathy-guidelines`, `test-driven-development`, and `verification-before-completion`; they are development aids, not application dependencies.

The assistant model revision and Desktop app build were not pinned. No separate project-owned model-configuration or custom system-prompt file was introduced. The repository captures reusable project rules and commands; it does not guarantee identical generated output from another assistant installation.

## Assistance and engineering decisions

| Area | AI assistance and purpose |
|---|---|
| Existing behavior | Characterization tests established eligibility boundaries, rounding, repeated calls, and preserved object state before calculator extraction |
| Design | Compared persistence options and simplified the design to a service/storage port/JDBC adapter with shared read records for two operations |
| Implementation | Assisted with interest policies, application coordination, JDBC mapping, transactions, HTTP/OpenAPI, migrations, and external configuration |
| Verification | Assisted with real PostgreSQL/HTTP tests, executable packaging, startup/restart checks, sample loading, and Swagger browser interaction |
| Review and correction | Identified a concurrent lost update, reproduced it, added a failing regression, then corrected the calculation query with row locking |
| Documentation/tooling | Prepared run instructions, project rules, the wrapper, Compose configuration, sample SQL, and the Bruno request collection |

Human review directed scope, challenged unnecessary complexity, approved design decisions, and examined explanations and changes. Automated checks were executed through the assistant's tools; this is not a claim that every command was independently rerun by a person.

The original 21 calculator characterization tests preceded the refactor. The service, adapter, and controller preceded the initial integration suite, whose first run passed; that work was integration verification rather than demonstrated test-first implementation. The later concurrency correction followed a verified failing-test → minimal-fix → passing-test sequence: two successful overlapping requests initially left `1201` from `1200`, while the required two credits produce `1202`. The regression passed unchanged after adding `FOR UPDATE` within the service transaction, and the reviewer rechecked the fix.

## Repeatable workflow and verification

Read `README.md` and `AGENTS.md`, identify the intended behavior and constraints, implement a focused change, and run appropriate checks. An example prompt for continuing development is: “Read the project instructions and assignment, preserve the existing calculator behavior and two-operation contract, make the smallest necessary change, and report the checks you actually run.”

From `java/`:

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify
```

The first command runs 21 calculator tests; the second also runs 9 PostgreSQL/HTTP integration tests. Full verification requires Docker and fails if it is unavailable. On POSIX use `sh ./mvnw test` and `sh ./mvnw verify`. First use requires network access for Maven/dependencies. The [README](README.md) contains environment settings, startup, sample loading, and Swagger instructions.

Recorded checks include all 30 tests passing, real migration/constraint checks, precise decimal/date reads, persisted updates, unchanged metadata/history, rollback after successful writes, sequential and concurrent calculations, and the OpenAPI contract. Standalone JAR startup/restart, Compose data retention, rerunnable sample SQL, README commands, and Swagger GET → POST → GET interaction were also exercised.

The Bruno collection was verified separately with CLI 4.3.0 in its default safe sandbox against empty and sample databases. GET → POST → GET passed its status/body/shape checks, with the expected persisted balances and unchanged history. This request-tooling addition did not change Java code or rerun the Java suite; the 30-test result above belongs to the preceding implementation verification. Bruno/Node.js remain optional API-testing tools.

Verification used Windows/PowerShell and Docker Desktop, with Java 17 bytecode compiled on a newer JDK. The POSIX wrapper passed syntax checking; a Unix runtime build and a separate JDK 17 runtime test were not performed. Reports are regenerated under ignored `java/target/`; they are build artifacts rather than source documentation. The preserved Double calculation boundary and excluded invalid-input handling remain deliberate compatibility/scope limits described in the README.
