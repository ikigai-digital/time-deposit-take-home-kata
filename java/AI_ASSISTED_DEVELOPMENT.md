# AI-assisted development

How AI tools were used to build the [Java solution](README.md), as asked for in requirement 6 of the [assignment](../README.md).

## Tools and setup
- **Claude Code** (Anthropic's CLI coding agent), run in the terminal next to IntelliJ IDEA.
  - Model: Claude Opus (the `opus` model setting in Claude Code).
  - Permission mode: the agent ran shell commands (Maven, Docker, git status/diff) itself and showed every change for review.
- **Project rules:** [`CLAUDE.md`](../CLAUDE.md) in the repository root. Claude Code loads it automatically into every session. It encodes:
  - the kata constraints;
  - the hexagonal package rules;
  - the money and database conventions;
  - the test commands and the rule to report real test results.
- **Custom agent:** [`.claude/agents/kata-reviewer.md`](../.claude/agents/kata-reviewer.md). It's a read-only reviewer that checks a change against the kata constraints and `CLAUDE.md`, then runs the test suite.

## Reproducing it
1. Install Claude Code (`npm install -g @anthropic-ai/claude-code`) and sign in.
2. Run `claude` in the repository root. `CLAUDE.md` and the project agent are picked up automatically.
3. Ask for a change. To review it before committing, use: *"use the kata-reviewer agent to review my changes"*.

## What was AI-assisted, and why
| Part | AI involvement | Why |
|---|---|---|
| Characterization tests for `updateBalance` | AI generated test cases; expected values were captured by running the legacy code, then reviewed | Fast and broad coverage of edge cases before touching legacy code |
| `PlanType` strategy refactor | Implemented by AI, with the developer prescribing the approach: one enum constant per plan holding its rate and eligibility rule. The final shape of the enum took a couple of review iterations | The design decision (strategy pattern as an enum) was made by the developer; the AI did the mechanical extraction from the legacy `if` chain, with the characterization tests proving behaviour stayed unchanged |
| Spring skeleton, Flyway schema and sample data | Done entirely by AI, then reviewed | Boilerplate (Spring Boot setup, docker compose, migrations, Testcontainers config) that the agent produces quickly and correctly; review focused on the schema matching the README tables |
| Application layer (ports, use cases, in-memory fakes) | Implemented by AI over a few rounds of developer feedback | The hexagonal boundaries (which ports, what each returns, transaction boundaries) were refined together; the AI wrote the code and the tests using in-memory fakes in each round |
| Persistence adapters and their Testcontainers tests | Implemented by Claude Code from a short prompt ("implement the persistence adapters"), then reviewed | Mechanical mapping code where the agent is fast; the review focused on money conversion and transaction behaviour |
| Web adapter, OpenAPI annotations, Spring wiring, end-to-end tests | Implemented by Claude Code, then reviewed | Same reasoning; the "exactly two endpoints" rule is guarded by a contract test |
| Maven wrapper, `README.md`, this document, `CLAUDE.md`, reviewer agent | Drafted by Claude Code, then edited | Documentation and setup |

Decisions were made by the developer, with the agent proposing options. These include the hexagonal layout, keeping `TimeDeposit` untouched, `POST` for the balance update, decimal money at the boundaries, and the documented assumptions. All AI output was reviewed before being kept, and every step was verified with the full test suite against a real PostgreSQL.
