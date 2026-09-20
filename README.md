# autenticacao-service

Microsserviço de **autenticação e gestão de usuários** do sistema. É quem **emite** os tokens JWT que todos os outros serviços validam.

## Stack

| Item | Versão |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.1 |
| Spring Cloud | 2025.1.3 |

| Porta | Context path | Perfil exigido |
|---|---|---|
| 9002 | `/autenticacao-service/` | `ROLE_ADMIN` (exceto rotas públicas) |

## Endpoints

**Rotas públicas** (não exigem token):
| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/login` | autentica e devolve `token` + `refreshToken` |
| `POST` | `/refresh-token` | renova o par de tokens |
| `POST` | `/esqueci-minha-senha` | dispara o e-mail de recuperação via `notificacao-service` |

**Rotas protegidas** (exigem `ROLE_ADMIN`):

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/usuarios` | lista paginada de usuários |
| `POST` | `/usuarios` | cadastra um usuário |
| `GET` | `/usuarios/{id}` | busca um usuário |
| `PUT` | `/usuarios/{id}` | atualiza nome e perfis |
| `PUT` | `/usuarios/{id}/alterar-status` | ativa/inativa o usuário |
| `PUT` | `/usuarios/trocar-senha` | troca a senha do usuário logado |
| `GET` | `/perfis` | lista os perfis disponíveis |

## Regras de negócio

- Senhas são gravadas com **BCrypt**.
- Após **3 tentativas** de login malsucedidas, a conta é **inativada** automaticamente.
- Um login bem-sucedido zera o contador e registra o último acesso (UTC).

## Emissão do JWT

O token carrega as claims `nome`, `email` e `perfis` (lista separada por vírgula), com `subject` = id do usuário. É assinado em **HS512**.

> **Atenção ao segredo:** desde a migração para o jjwt 0.13, `JWTSecret` (em `secret/application` no Vault) precisa ser uma string **Base64** que decodifique para **no mínimo 64 bytes**. Gere um com `openssl rand -base64 64`. Como esse segredo é compartilhado com todos os serviços que validam tokens, **trocá-lo exige subir todos juntos** e invalida os tokens em circulação.

## Banco de dados

PostgreSQL, com schema versionado por **Flyway** (migrations em `src/main/resources/db/migration`):

- `V001__Inicial.sql`
- `V002__novo_perfil.sql`
- `V003__perfil_saude.sql`

As migrations rodam automaticamente no startup.

> No Spring Boot 4 a autoconfiguração do Flyway passou a viver no módulo `spring-boot-flyway`. Sem essa dependência o Flyway é ignorado **em silêncio** — a aplicação sobe normalmente e nenhuma migration é aplicada. Ela está declarada no `pom.xml`; não remova.

## Integrações

| Serviço | Para quê |
|---|---|
| `notificacao-service` | envio do link de recuperação de senha (via OpenFeign) |
| Eureka | registro e descoberta |

## Configuração

A aplicação não guarda configuração própria: ela busca tudo no arranque, via `spring.config.import`.

| Origem | O que vem de lá |
|---|---|
| **Vault** (`secret/application`) | segredos compartilhados: `JWTSecret`, credenciais de e-mail, AWS, Eureka |
| **Vault** (`secret/<nome-do-serviço>`) | segredos próprios, como as credenciais do banco |
| **Config Server** | `server.port`, `context-path`, datasource e demais propriedades |

### Variável de ambiente obrigatória

| Variável | Para que serve |
|---|---|
| `VAULT_TOKEN` | token de acesso ao Vault |

`VAULT_TOKEN` **não tem valor padrão**. Sem ela, o Spring envia a string literal `${VAULT_TOKEN}` ao Vault, recebe `403` e — como `spring.cloud.vault.fail-fast` vem desligado — o erro só aparece bem depois, disfarçado de placeholder não resolvido (`${...} is malformed`). Se quiser que a falha apareça na hora, ligue `spring.cloud.vault.fail-fast: true`.

Também são necessários `VAULT_HOST`, `VAULT_PORT` e `VAULT_SCHEME` quando o Vault não está em `localhost:8200` via `http`, e `CONFIG_SERVER_USER` / `CONFIG_SERVER_PASS` nos serviços que leem do Config Server.

## Como executar

```bash
# build
./mvnw clean package

# execução
VAULT_TOKEN=<seu-token> java -jar target/autenticacao-service-*.jar --spring.profiles.active=dev
```

> **Dependências no ar:** este serviço só sobe com o **Vault**, o **Config Server** e o **Eureka** disponíveis, além do seu banco PostgreSQL.

A aplicação sobe em `http://localhost:9002/autenticacao-service/`.

### Docker

O `Dockerfile` espera o jar já na raiz do projeto, com o nome `sistema-autenticacao-service.jar`:

```bash
./mvnw clean package
cp target/autenticacao-service-*.jar sistema-autenticacao-service.jar

docker build \
  --build-arg VAULT_HOST=<host> \
  --build-arg VAULT_TOKEN=<token> \
  --build-arg CONFIG_SERVER_USER=<usuario> \
  --build-arg CONFIG_SERVER_PASS=<senha> \
  -t autenticacao-service .
```
