## 1. Projeto base

- [x] 1.1 Gerar o projeto pelo Spring Initializr na raiz do repositorio (Maven, Java 21, Boot 4.1.1, groupId `com.projetointegrador`, artifactId `naty-sync-service`, pacote `com.projetointegrador.natysync`, dependencias web, data-jpa, validation, actuator, lombok, postgresql, flyway) e verificar que `pom.xml`, `mvnw` e `src/` existem na raiz
- [x] 1.2 Conferir no `pom.xml` que o parent e `4.1.1` e que `java.version` e `21`
- [x] 1.3 Adicionar ao `pom.xml` o `flyway-database-postgresql`, o `springdoc-openapi-starter-webmvc-ui` na versao `3.1.0`, o `spring-boot-testcontainers` e o `org.testcontainers:testcontainers-postgresql`, verificando com `./mvnw -B dependency:resolve` que tudo baixa
- [x] 1.4 Adicionar o `spotless-maven-plugin` `3.10.1` amarrado a fase `verify` e verificar que `./mvnw -B spotless:apply` roda sem erro
- [x] 1.5 Renomear a classe principal gerada para `NatySyncApplication` e verificar que `./mvnw -B compile` passa
- [x] 1.6 Criar `.gitignore` cobrindo `target/`, `.env`, `*.log` e `HELP.md`, e verificar com `git status` que nenhum desses aparece
- [x] 1.7 Criar `.editorconfig` com indentacao, charset e final de linha, e verificar que `./mvnw -B spotless:check` continua passando
- [x] 1.8 Manter um `README.md` minimo, ja que o README completo e entregavel de etapa posterior

## 2. Estrutura de pacotes

- [x] 2.1 Criar `config/` com `RestClientConfig`, `SchedulerConfig` e `OpenApiConfig` como `@Configuration` vazias e `CorsConfig` com conteudo real lendo `app.cors.origens`, sem `SecurityConfig`
- [x] 2.2 Criar `natyapi/` com `NatyApiProperties` real anotada com `@ConfigurationProperties`, `NatyApiClient` stub, `dto/` com stubs e `exception/` com `NatyApiException`, `NatyAuthenticationException` e `NatyRateLimitException` reais
- [x] 2.3 Criar `usuario/` com `Usuario`, `UsuarioRepository`, `UsuarioService`, `UsuarioController`, `UsuarioMapper` e `dto/` como stubs
- [x] 2.4 Criar `sincronizacao/` com `SincronizacaoUsuarioService`, `SincronizacaoScheduler` e `SincronizacaoController` como stubs
- [x] 2.5 Criar `shared/exception/` e `shared/util/` com stubs
- [x] 2.6 Criar `health/` com `NatyApiHealthIndicator` stub
- [x] 2.7 Verificar com `./mvnw -B compile` que nenhum stub referencia biblioteca fora das dependencias desta etapa
- [x] 2.8 Verificar com `grep -rn "//\|/\*" src/main/java` que nenhum arquivo tem comentario explicativo, marcador temporal ou de autoria

## 3. Documentacao de contexto

- [x] 3.1 Escrever `CLAUDE.md` em cada um dos seis pacotes com as secoes Responsabilidade, Contratos, Decisoes, Armadilhas e Estado atual, verificando com `find src/main/java -type d -mindepth 1 -maxdepth 1` que nenhuma pasta ficou sem
- [x] 3.2 Registrar em `config/CLAUDE.md` que `SecurityConfig` foi deliberadamente omitido e por que
- [x] 3.3 Escrever o `CLAUDE.md` da raiz com proposito, stack, comandos de build, a regra de comentarios, a regra de ler o `CLAUDE.md` do pacote antes de alterar e um paragrafo por pacote
- [x] 3.4 Anotar no `CLAUDE.md` da raiz o pacote `implantacao` como previsto para etapa futura, sem cria-lo

## 4. Configuracao

- [x] 4.1 Escrever `application.yml` com `spring.datasource`, `spring.jpa.hibernate.ddl-auto: validate`, `spring.flyway`, `naty.api`, `sincronizacao`, `management.endpoints` expondo `health`, `info` e `flyway`, `springdoc` e `app.cors`, todas com valor padrao nas variaveis de ambiente
- [x] 4.2 Escrever `application-dev.yml` e `application-prod.yml` minimos, so com o que diverge
- [x] 4.3 Escrever `src/main/resources/db/migration/V1__baseline.sql` criando a tabela `usuario` com identificador, `naty_id` unico, nome, email, `payload jsonb` e carimbos de tempo

## 5. Docker

- [x] 5.1 Escrever o `Dockerfile` multi stage com `chmod +x mvnw` e `-B` nas invocacoes do Maven
- [x] 5.2 Escrever o `docker-compose.yml` com os servicos `app` e `postgres`, volume `postgres_data` e `healthcheck` com `pg_isready` mais `depends_on: condition: service_healthy`
- [x] 5.3 Escrever `.dockerignore` e `.env.example`

## 6. Verificacao

- [x] 6.1 `./mvnw -B verify` compila, passa o `spotless:check` e passa o teste de contexto do Spring
- [x] 6.2 `docker compose up --build -d` sobe os dois servicos
- [x] 6.3 `curl -s localhost:8080/actuator/health` responde `UP` com o componente `db` em `UP`, e `curl -s localhost:8080/actuator/flyway` lista a migration `V1` com estado `SUCCESS`
- [x] 6.4 `docker compose exec postgres psql -U natysync -d natysync -c "\dt"` lista `usuario` e `flyway_schema_history`
- [x] 6.5 `curl -s -o /dev/null -w "%{http_code}" localhost:8080/swagger-ui.html` responde 200 ou 302
- [x] 6.6 `docker compose logs app` sem stack trace
- [x] 6.7 `docker compose down -v` limpa o volume
