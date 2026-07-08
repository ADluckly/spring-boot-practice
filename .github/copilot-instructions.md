# Copilot Instructions for spring-boot-demo

This repository is a Java 17 Spring Boot backend project managed with Maven.

## Project baseline
- Use Java 17 language features only.
- Build and test with Maven (`./mvnw`).
- Base package: `com.example.spring_boot_demo`.
- Layering convention:
  - `controller`: HTTP API layer.
  - `service`: business logic.
  - `repository`: persistence access.
  - `dto`: request/response payloads.
  - `model`: entities/domain objects.
  - `exception`: centralized error handling.

## Coding rules
- Keep controllers thin and delegate logic to services.
- Validate request DTOs with Jakarta Validation and return clear errors.
- Prefer constructor injection for Spring beans.
- Do not hardcode credentials, secrets, endpoints, or local paths.
- Read sensitive values from environment variables or profile config.
- Keep methods short and focused; extract helpers for repeated logic.

## Security and config
- Never commit real secrets in `*.properties` or `*.yaml` files.
- Treat `src/main/resources/application-local.properties` as local-only.
- For AWS clients, prefer default credential chain fallback when key/secret is absent.

## Testing expectations
- Add or update tests for non-trivial changes.
- For HTTP behavior, prefer integration tests in `src/test/java/...`.
- Keep tests deterministic; avoid external network calls.

## Response style for code generation
- When adding a feature, generate:
  1. DTOs (if needed)
  2. Service logic
  3. Controller endpoint
  4. Exception handling updates
  5. Tests
