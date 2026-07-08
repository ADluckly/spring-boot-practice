---
applyTo: "src/main/java/**/*.java"
---

# Spring Architecture Instructions

Follow existing package boundaries in `com.example.spring_boot_demo`.

## API and service design
- Controllers only handle HTTP mapping, validation, and response shaping.
- Services contain business logic and orchestration.
- Repositories only access persistence and avoid business branching.

## DTO and model usage
- Do not expose JPA entities directly from controllers.
- Use request/response DTOs for API contracts.
- Keep DTO names explicit (`CreateXxxRequest`, `XxxResponse`).

## Error handling
- Prefer domain-specific exceptions mapped by global exception handler.
- Keep error messages user-readable and non-sensitive.

## Code consistency
- Reuse existing naming style in nearby files.
- Prefer small, composable private methods in service classes.
