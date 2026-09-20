# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

The **issuer** of the JWTs every other service validates. Also manages users and profiles.

| | |
|---|---|
| Port | `9002` |
| Context path | `/autenticacao-service` |
| Role required | `ROLE_ADMIN (except public routes)` |

Part of a personal microservices system; sibling repos live at `../sistema-*`. The API gateway fronts it at `https://sistema-backend.guilhermejr.net/autenticacao-service`.

## Security is hand-rolled here, not from the library

Unlike the five domain services, this one does **not** use `seguranca-jwt`. It has its own `config/security` package, because it issues tokens and its `UserDetailsImpl` is backed by the database.

An important difference: `AuthenticationJwtFilter` **ignores the `perfis` claim** and reloads the user from the database by the token's subject. Authorities therefore come from `usuarios_perfis`, not from the token. Forging a token with different roles changes nothing.

`WebSecurityConfig` carries `@EnableMethodSecurity` — without it the `@PreAuthorize("hasAnyRole('ADMIN')")` on the controllers is silently ignored and any authenticated user reaches `/usuarios`.

Public routes are listed in `LISTA_BRANCA`: `/login`, `/refresh-token`, `/esqueci-minha-senha` and `/actuator/health`. Note the stale `/trocar-senha` entry — the real mapping is `/usuarios/trocar-senha`, so that entry never matches.

Because the whole `UsuarioController` is annotated `ROLE_ADMIN`, `PUT /usuarios/trocar-senha` is admin-only too. A non-admin user cannot change their own password.

## Token format

`subject` is the user id; claims are `nome`, `email` and `perfis` (comma-separated). Signed with HS512 using `sistema.auth.jwtSecret`, which must be Base64 decoding to at least 64 bytes.

Changing that secret invalidates every token in circulation and requires deploying all six validating services together.

## Business rules

- Passwords are stored with BCrypt.
- Three failed logins **deactivate** the account; a successful login resets the counter and records the last access in UTC.

## Database migrations

PostgreSQL with Flyway, migrations in `src/main/resources/db/migration`.

`spring-boot-flyway` is declared in `pom.xml` and **must stay there**. In Spring Boot 4 the Flyway auto-configuration moved into that separate module; with only `flyway-core` on the classpath the service starts normally, logs nothing, and applies no migrations at all.

## Actuator

`/actuator/health` is public and returns the status only (`show-details: never`, set in the shared config repo). Everything else under `/actuator/**` requires HTTP Basic with `ROLE_ACTUATOR`, whose credentials come from Vault.

## Configuration comes from outside

This service stores almost no configuration of its own. `application.yml` only bootstraps `spring.config.import`, which pulls from:

- **Vault** — `secret/application` (shared: `JWTSecret`, actuator credentials, mail, AWS) and `secret/<service-name>` (its own DB credentials)
- **Config Server** — `server.port`, `server.servlet.context-path`, datasource, JPA settings

Both must be reachable or the service will not start.

`VAULT_TOKEN` is required and **has no default**. Without it Spring sends the literal string `${VAULT_TOKEN}` to Vault, gets a 403 that Spring Cloud Vault swallows (`fail-fast` is off), and the startup fails much later with a misleading `${someProperty} is malformed`. If you are chasing a confusing startup error, check `VAULT_TOKEN` first.

## Building and running

Java **21 only**. The Homebrew default JDK on this machine is newer and will break the build:

```bash
export JAVA_HOME=/Users/guilhermejr/Library/Java/JavaVirtualMachines/openjdk-21.0.2/Contents/Home
./mvnw clean package
VAULT_TOKEN=<token> java -jar target/*.jar --spring.profiles.active=dev
```

Do not raise `java.version` past 21 while ModelMapper is a dependency — the ByteBuddy bundled in it cannot generate classes on JDK 24+, and the app dies building its mappers with an `UnsupportedOperationException` that does not name the real cause.

## Deploying

`git push origin main` **is** the deploy. A `post-receive` hook on the VPS checks out, runs `mvn clean package` inside a throwaway `maven:3.9-amazoncorretto-21` container, builds the image and restarts it via docker compose. There is no separate release step.

Because the build happens on the VPS, any dependency from a private repository needs credentials **there**, not locally — the hook mounts `/home/guilhermejr/.m2` and passes `GITHUB_TOKEN`.

The `Dockerfile` only copies a prebuilt jar; it carries a `HEALTHCHECK` that polls `/actuator/health`.
