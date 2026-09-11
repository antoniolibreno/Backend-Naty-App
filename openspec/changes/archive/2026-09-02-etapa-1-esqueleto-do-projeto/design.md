## Context

Repositório vazio, sem `pom.xml`, sem `.gitignore` e sem código. Java 21, Docker e
Node disponíveis na máquina. Ver `proposal.md` seção Why para a motivação.

Restrição central desta etapa: o projeto precisa compilar e subir sem nenhuma
lógica de negocio implementada, e sem arrastar dependência que só será usada daqui
a três etapas.

Estado do ecossistema verificado nesta data: o Spring Initializr só oferece a linha
4 do Spring Boot, com default 4.1.1. A linha 3.5 parou em 3.5.16 e saiu do suporte
OSS gratuito. O springdoc 3.1.0 já depende dos módulos Boot 4 (`spring-boot-tomcat`,
`spring-boot-health`), e existe `resilience4j-spring-boot4` publicado.

## Goals / Non-Goals

**Goals:**

- Projeto compila com `./mvnw -B verify`.
- `docker compose up --build` sobe aplicação e PostgreSQL.
- `GET /actuator/health` responde `UP` com os componentes `db` e `flyway` em `UP`.
- Árvore de pacotes completa, cada pasta com seu `CLAUDE.md`.
- Swagger UI acessível, para que o time do Flutter enxergue o contrato.

**Non-Goals:**

- Qualquer chamada real à Naty API.
- Qualquer persistência ou consulta de `Usuario` além da tabela criada pela
  migration.
- Scheduler ativo, retry, mapper ou health indicator customizado funcionando.
- Autenticação de qualquer tipo.
- Testes além do teste de contexto gerado pelo Spring Initializr.

## Decisions

**Spring Boot 4.1.1 em vez de 3.x.** Alternativa considerada: fixar 3.5.16. Foi
descartada porque essa linha não recebe mais patch OSS gratuito e o Initializr não
a oferece, o que obrigaria a montar o `pom.xml` na mão para começar já em uma
versão sem suporte. Consequência aceita: menos tutorial antigo aplicável, e todo
artefato de terceiro precisa ser conferido contra Boot 4 antes de entrar.

**PostgreSQL em vez de MongoDB.** Implantação e onboarding são dados relacionais:
cliente, etapa, checklist, responsável, histórico. Isso vira join e relatório de
progresso, que em documento exigiria duplicação ou `$lookup` manual. O único pedaço
que pede documento e o espelho do usuário vindo da Naty API, e coluna `jsonb`
resolve: payload cru na coluna, campos consultados em colunas tipadas. Além disso a
sincronização quer upsert idempotente, que sai em um comando com
`ON CONFLICT DO UPDATE`, e transação sem exigir replica set configurado.
Alternativa considerada: MongoDB, descartada pelos motivos acima.

**Flyway com `ddl-auto: validate`.** O schema é versionado em arquivo e revisável
em diff. `ddl-auto: update` esconde alteração de schema dentro do runtime e não
deixa rastro. `validate` faz a aplicação falhar cedo se entidade e tabela
divergirem.

**Migration `V1__baseline.sql` já nesta etapa, mesmo sem entidade JPA.** Sem ela o
Flyway sobe com histórico vazio e nada prova que a conexão, o usuário do banco e a
permissão de DDL funcionam. A tabela `usuario` é criada aqui e a entidade
correspondente entra na etapa que a usa.

**Dependências só da etapa atual, com três exceções deliberadas.** MapStruct e
Resilience4j entram quando forem usados. springdoc, Testcontainers e Spotless foram
antecipados: springdoc porque o app Flutter é cliente externo e precisa do contrato
visível desde o primeiro endpoint, Testcontainers porque teste de integração
nascido com banco real evita reescrever teste depois, Spotless porque formatação
combinada no início custa nada e depois custa um diff gigante.

**Stub vazio em vez de arquivo ausente.** A árvore de pacotes é o contrato de
estrutura. Git não versiona pasta vazia, então cada pasta precisa de pelo menos um
arquivo. Stubs são classes vazias que compilam, sem anotação de biblioteca ainda
não adicionada: nada de `@Mapper`, `@Retry` ou tipo do springdoc. DTO que viraria
record vazio fica como classe vazia e vira record na etapa dona.

**`SecurityConfig` não é criado.** Esta fase descarta autenticação de usuário final
e o Spring Security não está nas dependências. Criar uma `@Configuration` vazia com
esse nome convidaria alguém a preenchê-la fora de escopo. A ausência fica registrada
em `config/CLAUDE.md`.

**`CorsConfig` com conteúdo real.** Flutter mobile não passa por CORS, mas Flutter
Web passa, e descobrir isso na primeira integração custa uma tarde. Origens saem de
`app.cors.origens`, com default permissivo em dev e lista fechada em prod.

**Valores padrão nas variáveis de ambiente do `application.yml`.** Variável sem
default quebra a execução local sem `.env` e quebra o teste de contexto do Spring.
Cada variável ganha um default razoável e `NATY_API_TOKEN` aceita vazio, já que
esta etapa não chama a API.

**`healthcheck` no serviço `postgres` do compose.** Diferente de um driver que
conecta preguiçosamente, o Flyway conecta na subida da aplicação e falha se o banco
ainda não aceita conexão. `depends_on` sozinho só ordena o start. `pg_isready` com
`condition: service_healthy` e o que torna `docker compose up` reprodutível.

**`chmod +x mvnw` no Dockerfile.** O bit de execução não é confiável em checkout
feito em outra plataforma. Sem isso o build da imagem falha com permission denied.

**OpenSpec rodando via `npx`.** O `npm install -g` falha com `EACCES` em
`/usr/local/bin`. `npx --yes @fission-ai/openspec@latest` resolve sem sudo e sem
poluir o ambiente global.

## Risks / Trade-offs

Ecossistema ainda alcançando o Boot 4 → toda dependência nova é conferida contra a
versão antes de entrar no `pom.xml`. Já verificado nesta etapa: springdoc 3.1.0
serve, e Testcontainers 2.x renomeou o artefato do Postgres para
`org.testcontainers:testcontainers-postgresql`, com o nome antigo devolvendo 404.

Stubs vazios envelhecem mal se as etapas seguintes atrasarem → cada `CLAUDE.md` de
pacote lista o que ainda é stub e qual etapa o preenche, para que ninguem confunda
arquivo vazio com funcionalidade quebrada.

Tabela `usuario` criada antes da entidade que a mapeia → `ddl-auto: validate` não
reclama de tabela sem entidade, só de entidade sem tabela. O risco real é a coluna
nascer com tipo que a entidade depois não quer. Mitigado mantendo a baseline
mínima: identificador, chave da Naty, nome, email, payload e carimbos de tempo.

Spotless quebrando o build por formatação em um projeto de faculdade → `spotless:check`
roda no `verify`, e `spotless:apply` conserta em um comando. Documentado no
`CLAUDE.md` da raiz.

**Prova de que o Flyway rodou vem do endpoint, não do health.** O Spring Boot não
publica health indicator de Flyway: o que existe é o endpoint `/actuator/flyway`, que
lista cada migration com seu estado. Por isso `management.endpoints.web.exposure.include`
carrega `flyway` além de `health` e `info`, e o critério de aceite usa esse endpoint
somado a presença das tabelas `usuario` e `flyway_schema_history` no banco.
