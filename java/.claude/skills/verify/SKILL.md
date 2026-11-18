---
name: verify
description: Run the full Maven build with all tests (unit + Testcontainers) and report the result. Use before every commit and whenever the solution needs to be proven to work.
argument-hint: "[optional Maven args, e.g. -Dtest=TimeDepositControllerTest]"
allowed-tools: Bash(mvn *) Bash(docker ps*)
---

Verify the solution:

1. Check that Docker is running with `docker ps` (Testcontainers needs it). If it is not, stop and tell the user.
2. Run `mvn -f java/pom.xml clean verify $ARGUMENTS`.
3. **Green:** report the test summary (tests run / failures / errors / skipped) and the build time. Nothing else.
4. **Red:** for each failing test report
   - the test name and the assertion or exception,
   - the root cause, found by reading the code under test (not guessed from the message),
   - the proposed fix.

   Do not apply fixes unless the user asks. Never propose weakening, skipping or deleting a test unless the test
   itself is wrong — and then say explicitly why.
