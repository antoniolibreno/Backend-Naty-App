## Context

Repositorio vazio, sem `pom.xml`, sem `.gitignore` e sem codigo. Java 21, Docker e
Node disponiveis na maquina. Ver `proposal.md` secao Why para a motivacao.

Restricao central desta etapa: o projeto precisa compilar e subir sem nenhuma
logica de negocio implementada, e sem arrastar dependencia que so sera usada daqui
a tres etapas.

Estado do ecossistema verificado nesta data: o Spring Initializr so oferece a linha
4 do Spring Boot, com default 4.1.1. A linha 3.5 parou em 3.5.16 e saiu do suporte
OSS gratuito. O springdoc 3.1.0 ja depende dos modulos Boot 4 (`spring-boot-tomcat`,
`spring-boot-health`), e existe `resilience4j-spring-boot4` publicado.

## Goals / Non-Goals

**Goals:**

- Projeto compila com `./mvnw -B verify`.
- `docker compose up --build` sobe aplicacao e PostgreSQL.
- `GET /actuator/health` responde `UP` com os componentes `db` e `flyway` em `UP`.
- Arvore de pacotes completa, cada pasta com seu `CLAUDE.md`.
- Swagger UI acessivel, para que o time do Flutter enxergue o contrato.

**Non-Goals:**

- Qualquer chamada real a Naty API.
- Qualquer persistencia ou consulta de `Usuario` alem da tabela criada pela
  migration.
- Scheduler ativo, retry, mapper ou health indicator customizado funcionando.
- Autenticacao de qualquer tipo.
- Testes alem do teste de contexto gerado pelo Spring Initializr.

## Decisions

**Spring Boot 4.1.1 em vez de 3.x.** Alternativa considerada: fixar 3.5.16. Foi
descartada porque essa linha nao recebe mais patch OSS gratuito e o Initializr nao
a oferece, o que obrigaria a montar o `pom.xml` na mao para comecar ja em uma
versao sem suporte. Consequencia aceita: menos tutorial antigo aplicavel, e todo
artefato de terceiro precisa ser conferido contra Boot 4 antes de entrar.

**PostgreSQL em vez de MongoDB.** Implantacao e onboarding sao dados relacionais:
cliente, etapa, checklist, responsavel, historico. Isso vira join e relatorio de
progresso, que em documento exigiria duplicacao ou `$lookup` manual. O unico pedaco
que pede documento e o espelho do usuario vindo da Naty API, e coluna `jsonb`
resolve: payload cru na coluna, campos consultados em colunas tipadas. Alem disso a
sincronizacao quer upsert idempotente, que sai em um comando com
`ON CONFLICT DO UPDATE`, e transacao sem exigir replica set configurado.
Alternativa considerada: MongoDB, descartada pelos motivos acima.

**Flyway com `ddl-auto: validate`.** O schema e versionado em arquivo e revisavel
em diff. `ddl-auto: update` esconde alteracao de schema dentro do runtime e nao
deixa rastro. `validate` faz a aplicacao falhar cedo se entidade e tabela
divergirem.

**Migration `V1__baseline.sql` ja nesta etapa, mesmo sem entidade JPA.** Sem ela o
Flyway sobe com historico vazio e nada prova que a conexao, o usuario do banco e a
permissao de DDL funcionam. A tabela `usuario` e criada aqui e a entidade
correspondente entra na etapa que a usa.

**Dependencias so da etapa atual, com tres excecoes deliberadas.** MapStruct e
Resilience4j entram quando forem usados. springdoc, Testcontainers e Spotless foram
antecipados: springdoc porque o app Flutter e cliente externo e precisa do contrato
visivel desde o primeiro endpoint, Testcontainers porque teste de integracao
nascido com banco real evita reescrever teste depois, Spotless porque formatacao
combinada no inicio custa nada e depois custa um diff gigante.

**Stub vazio em vez de arquivo ausente.** A arvore de pacotes e o contrato de
estrutura. Git nao versiona pasta vazia, entao cada pasta precisa de pelo menos um
arquivo. Stubs sao classes vazias que compilam, sem anotacao de biblioteca ainda
nao adicionada: nada de `@Mapper`, `@Retry` ou tipo do springdoc. DTO que viraria
record vazio fica como classe vazia e vira record na etapa dona.

**`SecurityConfig` nao e criado.** Esta fase descarta autenticacao de usuario final
e o Spring Security nao esta nas dependencias. Criar uma `@Configuration` vazia com
esse nome convidaria alguem a preenche-la fora de escopo. A ausencia fica registrada
em `config/CLAUDE.md`.

**`CorsConfig` com conteudo real.** Flutter mobile nao passa por CORS, mas Flutter
Web passa, e descobrir isso na primeira integracao custa uma tarde. Origens saem de
`app.cors.origens`, com default permissivo em dev e lista fechada em prod.

**Valores padrao nas variaveis de ambiente do `application.yml`.** Variavel sem
default quebra a execucao local sem `.env` e quebra o teste de contexto do Spring.
Cada variavel ganha um default razoavel e `NATY_API_TOKEN` aceita vazio, ja que
esta etapa nao chama a API.

**`healthcheck` no servico `postgres` do compose.** Diferente de um driver que
conecta preguicosamente, o Flyway conecta na subida da aplicacao e falha se o banco
ainda nao aceita conexao. `depends_on` sozinho so ordena o start. `pg_isready` com
`condition: service_healthy` e o que torna `docker compose up` reprodutivel.

**`chmod +x mvnw` no Dockerfile.** O bit de execucao nao e confiavel em checkout
feito em outra plataforma. Sem isso o build da imagem falha com permission denied.

**OpenSpec rodando via `npx`.** O `npm install -g` falha com `EACCES` em
`/usr/local/bin`. `npx --yes @fission-ai/openspec@latest` resolve sem sudo e sem
poluir o ambiente global.

## Risks / Trade-offs

Ecossistema ainda alcancando o Boot 4 → toda dependencia nova e conferida contra a
versao antes de entrar no `pom.xml`. Ja verificado nesta etapa: springdoc 3.1.0
serve, e Testcontainers 2.x renomeou o artefato do Postgres para
`org.testcontainers:testcontainers-postgresql`, com o nome antigo devolvendo 404.

Stubs vazios envelhecem mal se as etapas seguintes atrasarem → cada `CLAUDE.md` de
pacote lista o que ainda e stub e qual etapa o preenche, para que ninguem confunda
arquivo vazio com funcionalidade quebrada.

Tabela `usuario` criada antes da entidade que a mapeia → `ddl-auto: validate` nao
reclama de tabela sem entidade, so de entidade sem tabela. O risco real e a coluna
nascer com tipo que a entidade depois nao quer. Mitigado mantendo a baseline
minima: identificador, chave da Naty, nome, email, payload e carimbos de tempo.

Spotless quebrando o build por formatacao em um projeto de faculdade → `spotless:check`
roda no `verify`, e `spotless:apply` conserta em um comando. Documentado no
`CLAUDE.md` da raiz.

**Prova de que o Flyway rodou vem do endpoint, nao do health.** O Spring Boot nao
publica health indicator de Flyway: o que existe e o endpoint `/actuator/flyway`, que
lista cada migration com seu estado. Por isso `management.endpoints.web.exposure.include`
carrega `flyway` alem de `health` e `info`, e o criterio de aceite usa esse endpoint
somado a presenca das tabelas `usuario` e `flyway_schema_history` no banco.
