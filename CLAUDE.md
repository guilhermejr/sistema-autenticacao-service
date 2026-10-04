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

Public routes are listed in `LISTA_BRANCA`: `/login`, `/login/dois-fatores`, `/refresh-token`, `/esqueci-minha-senha` and `/actuator/health`. Note the stale `/trocar-senha` entry — the real mapping is `/usuarios/trocar-senha`, so that entry never matches.

Because the whole `UsuarioController` is annotated `ROLE_ADMIN`, `PUT /usuarios/trocar-senha` is admin-only too. A non-admin user cannot change their own password.

## Token format

`subject` is the user id; claims are `nome`, `email` and `perfis` (comma-separated). Signed with HS512 using `sistema.auth.jwtSecret`, which must be Base64 decoding to at least 64 bytes.

Changing that secret invalidates every token in circulation and requires deploying all six validating services together.

## Business rules

- Passwords are stored with BCrypt.
- Three failed logins **deactivate** the account; a successful login resets the counter and records the last access in UTC.

## Two-factor authentication (TOTP / Google Authenticator)

Opt-in per user. `TotpService` implements RFC 6238 by hand (HMAC-SHA1, 30 s steps, 6 digits, ±1 step tolerance) — no extra dependency. The secret is stored in `usuarios.dois_fatores_segredo` as Base64 (not encrypted); it is shown to the user in Base32 only during setup.

- **Login with 2FA on is two calls.** `POST /login` with a correct password returns `{ doisFatores: true, tokenDoisFatores }` instead of tokens; `POST /login/dois-fatores` with that token and the `codigo` returns the usual `JWTResponde`. The frontend tells the two apart by `doisFatores`.
- **`tokenDoisFatores` is signed with a key derived from `jwtSecret`** (`HMAC-SHA512(secret, "dois-fatores")`), never with the main key. The other services (`seguranca-jwt`) build the user from token claims alone, without the database, so a challenge token signed with the main key would work there as an access token without the code. It lives 5 minutes.
- **A correct password does not reset `tentativaLogin` while 2FA is on.** Only the accepted code does. Otherwise password-then-wrong-code could be repeated forever. Wrong codes count toward the same 3-strike deactivation, both at login and at `POST /dois-fatores/desativar`.
- **Replay:** `dois_fatores_ultimo_passo` keeps the last accepted step; a code from that step or an earlier one is refused.
- `LoginService.loginDoisFatores` is deliberately **not** `@Transactional`: the exception for a wrong code would roll back the attempt counter.
- Self-service endpoints, any authenticated user: `GET /dois-fatores` (status), `POST /dois-fatores/configurar` (new pending secret + `otpauth://` URI; refused while active), `POST /dois-fatores/ativar` and `POST /dois-fatores/desativar` (both take `{ codigo }`).

There are no recovery codes. A user who loses the phone is recovered in the database:

```sql
UPDATE usuarios SET dois_fatores_ativo = false, dois_fatores_segredo = NULL, dois_fatores_ultimo_passo = NULL,
                    tentativa_login = 0, ativo = true
 WHERE email = '...';
```

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
mvn clean package   # the repo has mvnw but not .mvn/wrapper, so the wrapper does not run
VAULT_TOKEN=<token> java -jar target/*.jar --spring.profiles.active=dev
```

Do not raise `java.version` past 21 while ModelMapper is a dependency — the ByteBuddy bundled in it cannot generate classes on JDK 24+, and the app dies building its mappers with an `UnsupportedOperationException` that does not name the real cause.

## Deploying

`git push origin main` **is** the deploy. A `post-receive` hook on the VPS checks out, runs `mvn clean package` inside a throwaway `maven:3.9-amazoncorretto-21` container, builds the image and restarts it via docker compose. There is no separate release step.

Because the build happens on the VPS, any dependency from a private repository needs credentials **there**, not locally — the hook mounts `/home/guilhermejr/.m2` and passes `GITHUB_TOKEN`.

The `Dockerfile` only copies a prebuilt jar; it carries a `HEALTHCHECK` that polls `/actuator/health`.
