## Why

O repositorio nao tem codigo. Antes de qualquer integracao com a Naty API ou
modelagem de dominio, precisamos de um esqueleto que compile, suba via Docker
junto com o PostgreSQL e responda no health do Actuator.

Essa etapa existe separada das demais para que as etapas seguintes comecem com o
terreno pronto e possam focar so na logica delas, sem misturar decisao de build,
estrutura de pastas e infraestrutura com regra de negocio.

## What Changes

- Projeto Maven Spring Boot 4.1.1 na raiz do repositorio, Java 21, groupId
  `com.projetointegrador`, artifactId `naty-sync-service`, pacote raiz
  `com.projetointegrador.natysync`.
- Dependencias do que esta etapa usa: Web, Data JPA, Validation, Actuator,
  Lombok, driver PostgreSQL, Flyway, springdoc-openapi e o starter de teste com
  Testcontainers PostgreSQL. Plugin Spotless no build. MapStruct e Resilience4j
  ficam para a etapa que os usa.
- Arvore de pacotes `config`, `natyapi`, `usuario`, `sincronizacao`, `shared` e
  `health`, cada um com seu proprio `CLAUDE.md`.
- Classes com conteudo real nesta etapa: `NatySyncApplication`,
  `NatyApiProperties`, as tres excecoes de `natyapi/exception`, `CorsConfig` e as
  `@Configuration` vazias de `config`. Todo o resto entra como stub que compila,
  preenchido pela etapa dona.
- `application.yml`, `application-dev.yml` e `application-prod.yml`, com valor
  padrao em toda variavel de ambiente.
- Migration Flyway `V1__baseline.sql` criando a tabela `usuario` com coluna
  `payload jsonb`.
- `Dockerfile` multi stage, `docker-compose.yml` com aplicacao e PostgreSQL,
  `.dockerignore`, `.env.example`, `.gitignore` e `.editorconfig` na raiz.
- `CLAUDE.md` na raiz apontando proposito, stack, a regra de comentarios e o
  resumo de cada pacote.

## Capabilities

### New Capabilities

Nenhuma. Esta etapa entrega apenas estrutura de projeto, build e infraestrutura
local, sem comportamento de negocio observavel. As capabilities reais nascem nas
etapas seguintes: integracao com a Naty API, consulta de usuario e sincronizacao.

A mudanca esta marcada com `skip_specs: true` no `.openspec.yaml` por isso.

### Modified Capabilities

Nenhuma. Nao existe spec no projeto ainda.

## Impact

- Cria todo o build do projeto, que hoje nao existe.
- Fixa PostgreSQL como banco. O payload cru da Naty API mora em coluna `jsonb`, o
  schema e do Flyway e `ddl-auto` fica em `validate`.
- `SecurityConfig` nao e criado. A fase atual nao tem autenticacao de usuario
  final e o Spring Security nao esta entre as dependencias. Decisao registrada em
  `config/CLAUDE.md`.
- `CorsConfig` entra com conteudo real, para que o app Flutter Web consiga
  consumir a API. App mobile nao passa por CORS.
- Nenhuma chamada real a Naty API acontece aqui. `NATY_API_TOKEN` pode ficar
  vazio e a aplicacao ainda sobe.
