Steps taken: 
1. There are no tests, the one exists is asserting 1 == 1 which brings none of a value. Adding tests is a must. Doing it as a first step. Asked AI to help out with it. 
2. Having test coverage, I'll focus on refactoring the updateBalance method to make it more readable. 
3. Introducing Spring framework
4. Database: Flyway schema (`time_deposits`, `withdrawals`) and sample data, verified with Testcontainers.
5. Application layer (hexagonal): use cases (inbound ports), repository ports (outbound), services tested with in-memory fakes.
6. Persistence adapters: JPA entities kept apart from the domain; only the balance is written back, rounded to cents.
7. Web adapter: exactly two endpoints (`GET /time-deposits`, `POST /time-deposits/balance-updates`), OpenAPI contract via springdoc, end-to-end tests against PostgreSQL.
8. Maven wrapper, solution README (run instructions, design), AI usage write-up (`AI_ASSISTED_DEVELOPMENT.md`), `CLAUDE.md` and a project reviewer agent.


Notes:
 - Description of the interest calculation might be misleading. There is no mention that calculations are done monthly. AFAIK it is a standard approach in banking.    