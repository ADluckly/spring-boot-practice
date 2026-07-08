---
name: spring-feature-skill
description: Template skill for implementing standard Spring Boot API features in this repository.
---

# Spring Feature Skill Template

Use this skill when adding or modifying API behavior in the project.

## Checklist
1. Define or update request/response DTOs.
2. Implement service-layer business logic.
3. Keep controller focused on HTTP concerns.
4. Add/adjust exception mapping where needed.
5. Add tests (integration preferred for API behavior).

## Repository conventions
- Base package: `com.example.spring_boot_demo`
- Build: `./mvnw`
- Java: 17
- Sensitive config stays out of tracked properties files.

## Validation gate
Run:

```bash
./mvnw -q -DskipTests compile
./mvnw test
```

Only mark complete if build/tests pass or clearly report remaining failures.
