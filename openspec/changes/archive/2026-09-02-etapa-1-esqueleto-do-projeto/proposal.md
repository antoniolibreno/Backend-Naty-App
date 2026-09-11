## Why

O repositório não tem código. Antes de qualquer integração com a Naty API ou
modelagem de domínio, precisamos de um esqueleto que compile, suba via Docker
junto com o PostgreSQL e responda no health do Actuator.

Essa etapa existe separada das demais para que as etapas seguintes começem com o
terreno pronto e possam focar só na lógica delas, sem misturar decisão de build,
estrutura de pastas e infraestrutura com regra de negocio.

## What Changes

- Projeto Maven Spring Boot 4.1.1 na raiz do repositório, Java 21, groupId
  `com.projetointegrador`, artifactId `naty-sync-service`, pacote raiz
  `com.projetointegrador.natysync`.
- Dependências do que esta etapa usa: Web, Data JPA, Validation, Actuator,
  Lombok, driver PostgreSQL, Flyway, springdoc-openapi e o starter de teste com
  Testcontainers PostgreSQL. Plugin Spotless no build. MapStruct e Resilience4j
  ficam para a etapa que os usa.
- Árvore de pacotes `config`, `natyapi`, `usuario`, `sincronizacao`, `shared` e
  `health`, cada um com seu próprio `CLAUDE.md`.
- Classes com conteúdo real nesta etapa: `NatySyncApplication`,
  `NatyApiProperties`, as três exceções de `natyapi/exception`, `CorsConfig` e as
  `@Configuration` vazias de `config`. Todo o resto entra como stub que compila,
  preenchido pela etapa dona.
- `application.yml`, `application-dev.yml` e `application-prod.yml`, com valor
  padrão em toda variável de ambiente.
- Migration Flyway `V1__baseline.sql` criando a tabela `usuario` com coluna
  `payload jsonb`.
- `Dockerfile` multi stage, `docker-compose.yml` com aplicação e PostgreSQL,
  `.dockerignore`, `.env.example`, `.gitignore` e `.editorconfig` na raiz.
- `CLAUDE.md` na raiz apontando propósito, stack, a regra de comentários e o
  resumo de cada pacote.

## Capabilities

### New Capabilities

Nenhuma. Esta etapa entrega apenas estrutura de projeto, build e infraestrutura
local, sem comportamento de negocio observável. As capabilities reais nascem nas
etapas seguintes: integração com a Naty API, consulta de usuário e sincronização.

A mudança está marcada com `skip_specs: true` no `.openspec.yaml` por isso.

### Modified Capabilities

Nenhuma. Não existe spec no projeto ainda.

## Impact

- Cria todo o build do projeto, que hoje não existe.
- Fixa PostgreSQL como banco. O payload cru da Naty API mora em coluna `jsonb`, o
  schema é do Flyway e `ddl-auto` fica em `validate`.
- `SecurityConfig` não é criado. A fase atual não tem autenticação de usuário
  final e o Spring Security não está entre as dependências. Decisão registrada em
  `config/CLAUDE.md`.
- `CorsConfig` entra com conteúdo real, para que o app Flutter Web consiga
  consumir a API. App mobile não passa por CORS.
- Nenhuma chamada real à Naty API acontece aqui. `NATY_API_TOKEN` pode ficar
  vazio e a aplicação ainda sobe.
