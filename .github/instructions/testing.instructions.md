---
applyTo: "src/test/java/**/*.java"
---

# Testing Instructions

## Test strategy
- Use integration tests for endpoint behavior and serialization.
- Use focused unit tests for isolated business logic where useful.
- Cover happy path and at least one failure path for new logic.

## Test quality
- Keep tests deterministic and independent.
- Avoid sleeps and timing-sensitive assertions where possible.
- Assert observable behavior (status, body, side effects), not internals.

## Framework conventions
- Use JUnit 5 and Spring Boot test support already in this repo.
- Keep test names descriptive, e.g. `shouldReturn404WhenUserNotFound`.
