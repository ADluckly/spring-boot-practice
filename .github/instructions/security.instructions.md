---
applyTo: "src/main/resources/**/*.{properties,yaml,yml},src/main/java/**/*.java"
---

# Security Instructions

## Secrets and credentials
- Never generate or commit real secrets/tokens/keys.
- Use environment-variable placeholders in examples.
- Keep local-only settings in `application-local.properties`.

## Logging safety
- Do not log passwords, access keys, tokens, or full connection strings.
- Redact sensitive fields in error messages and logs.

## AWS client usage
- Prefer AWS SDK default credentials provider chain when possible.
- If explicit key/secret are supported for local dev, make them optional.
