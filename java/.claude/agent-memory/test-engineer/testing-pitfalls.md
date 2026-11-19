---
name: testing-pitfalls
description: Test setup pitfalls hit in this project (Spring Boot 4 slices, nested repos, seeding)
metadata:
  type: feedback
---

- A nested `interface ... extends JpaRepository` inside a `@DataJpaTest` class is NOT registered as a bean. Seed tables the production code has no repository for (withdrawals) with an autowired `JdbcTemplate` instead.
- `@DataJpaTest` package is `org.springframework.boot.data.jpa.test.autoconfigure` (Boot 4), not `...boot.test.autoconfigure.orm.jpa`.
- Surefire output of a Spring failure is very noisy; filter with `grep -E 'Tests run:|BUILD|ERROR\] +[A-Za-z]'` and read `target/surefire-reports/*.txt`.
- GET /api/time-deposits has no validation of page/size although OpenAPI declares 400 (found 2026-11-18; test `TimeDepositControllerPagingValidationTest` left failing on purpose).
