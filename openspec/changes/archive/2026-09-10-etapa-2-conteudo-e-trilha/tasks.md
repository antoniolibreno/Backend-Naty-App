## 1. Dependência e configuração

- [x] 1.1 Adicionar MapStruct ao `pom.xml` com `mapstruct` e `mapstruct-processor`, incluindo `lombok-mapstruct-binding` em `annotationProcessorPaths` depois do Lombok, e verificar com `./mvnw -B compile` que o build passa
- [x] 1.2 Apontar `spring.flyway.locations` para `classpath:db/migration` no `application.yml` e para `classpath:db/migration,classpath:db/seed-dev` no `application-dev.yml`, deixando `application-prod.yml` sem seed de desenvolvimento
- [x] 1.3 Verificar com `grep` que `application-prod.yml` não referência `seed-dev` em lugar nenhum

## 2. Migrations

- [x] 2.1 Escrever `V2__conteudo_e_empresa.sql` criando `empresa` e alterando `usuario` com `empresa_id` não nulo, `perfil`, `status`, `ultimo_acesso_naty` e `sincronizado_em`, trocando o índice único de `naty_id` para o par `(empresa_id, naty_id)` e criando único em `(empresa_id, lower(email))`
- [x] 2.2 Acrescentar em `V2` as tabelas `trilha`, `modulo`, `atividade`, `quiz`, `pergunta` e `alternativa`, com chave estrangeira e índice de ordenação em cada nível
- [x] 2.3 Escrever `V3__seed_conteudo_exemplo.sql` com UUID fixo: uma trilha, dois módulos, três atividades por módulo, um quiz por atividade, quatro perguntas por quiz e quatro alternativas por pergunta, exatamente uma marcada como correta
- [x] 2.4 Escrever `db/seed-dev/R__seed_empresa_exemplo.sql` com uma empresa fictícia e ao menos três integrantes de e-mail conhecido
- [x] 2.5 Verificar com `docker compose up -d` no perfil `dev` que `/actuator/flyway` lista `V2` e `V3` com estado `SUCCESS`
- [x] 2.6 Verificar com `psql` que exatamente uma alternativa por pergunta tem `correta` verdadeiro

## 3. Entidades e repositórios

- [x] 3.1 Criar o pacote `empresa` com a entidade `Empresa` e `EmpresaRepository`, e verificar com `./mvnw -B compile` que compila
- [x] 3.2 Transformar `Usuario` em entidade JPA real ligada a `Empresa`, com os campos novos, e fazer `UsuarioRepository` estender `JpaRepository`
- [x] 3.3 Criar o pacote `trilha` com as entidades `Trilha`, `Modulo`, `Atividade`, `Quiz`, `Pergunta` e `Alternativa`, e seus repositórios
- [x] 3.4 Verificar com `./mvnw -B verify` que o teste de contexto sobe, provando que `ddl-auto: validate` aceita o mapeamento contra o schema migrado

## 4. Leitura de conteúdo

- [x] 4.1 Criar os DTOs de resposta de trilha, módulo e atividade, garantindo que o DTO de alternativa não possua campo de resposta correta
- [x] 4.2 Criar os mappers MapStruct de entidade para DTO, cobrindo o aninhamento de trilha, módulo e atividade
- [x] 4.3 Implementar `TrilhaService` e `TrilhaController` com `GET /api/v1/trilhas` listando só trilha ativa em ordem crescente
- [x] 4.4 Implementar `GET /api/v1/trilhas/{trilhaId}` devolvendo módulos e atividades aninhados e ordenados, e erro de recurso não encontrado quando a trilha não existe
- [x] 4.5 Implementar `GET /api/v1/atividades/{atividadeId}` devolvendo os dados da atividade e tratando vídeo ausente como caso normal
- [x] 4.6 Implementar `GET /api/v1/atividades/{atividadeId}/quiz` devolvendo perguntas, alternativas e nota mínima, e erro de recurso não encontrado quando a atividade não tem quiz

## 5. Sessão do integrante

- [x] 5.1 Criar a requisição de sessão com validação de e-mail obrigatório e formato válido
- [x] 5.2 Implementar `POST /api/v1/sessoes` resolvendo o integrante por e-mail normalizado em minúsculas e sem espaços nas pontas, devolvendo identificador do integrante, da empresa e o nome
- [x] 5.3 Devolver erro de recurso não encontrado quando o e-mail não pertence a nenhum integrante
- [x] 5.4 Marcar a operação como provisória e sem autenticação na documentação OpenAPI, e verificar em `/v3/api-docs` que a marcação aparece

## 6. Tratamento de erro

- [x] 6.1 Preencher `ApiExceptionHandler` e `ErroResposta` em `shared` para traduzir recurso não encontrado em 404 e falha de validação em 400, com o mesmo formato de corpo em toda a API
- [x] 6.2 Verificar com `curl` que uma trilha inexistente devolve 404 no formato de `ErroResposta`

## 7. Documentação de contexto

- [x] 7.1 Escrever `empresa/CLAUDE.md` com responsabilidade, contratos, decisões, armadilhas e estado atual
- [x] 7.2 Escrever `trilha/CLAUDE.md`, registrando que o conteúdo é global, que o gabarito nunca sai em DTO e em qual migration o conteúdo real substitui o exemplo
- [x] 7.3 Atualizar `usuario/CLAUDE.md` e `shared/CLAUDE.md` retirando da seção Estado atual o que deixou de ser stub
- [x] 7.4 Atualizar a seção Pacotes do `CLAUDE.md` da raiz movendo `empresa` e `trilha` de previstos para existentes

## 8. Testes

- [x] 8.1 Escrever teste de integração com Testcontainers verificando que o seed de conteúdo aplicou: uma trilha, dois módulos e seis atividades
- [x] 8.2 Escrever teste verificando que o payload de quiz não contém, em nenhum campo, a indicação de alternativa correta
- [x] 8.3 Escrever teste do detalhe de trilha verificando a ordem de módulos e atividades
- [x] 8.4 Escrever teste de sessão cobrindo e-mail válido, e-mail com maiúsculas e espaços, e-mail inexistente e e-mail inválido
- [x] 8.5 Escrever teste verificando que trilha inativa não aparece na listagem

## 9. Verificação

- [x] 9.1 `./mvnw -B verify` compila, passa `spotless:check` e passa todos os testes
- [x] 9.2 `docker compose up --build -d` sobe e `/actuator/flyway` lista `V1`, `V2` e `V3` com estado `SUCCESS`
- [x] 9.3 `curl -s localhost:8080/api/v1/trilhas` devolve a trilha de exemplo
- [x] 9.4 `curl -s localhost:8080/api/v1/atividades/{id}/quiz` devolve quatro perguntas e nenhuma marcação de resposta correta
- [x] 9.5 `curl` em `POST /api/v1/sessoes` com um e-mail semeado devolve identificador de integrante e de empresa
- [x] 9.6 `grep -rnE '^[[:space:]]*(//|/\*|\*[^/])' src/main/java` não encontra comentário explicativo
- [x] 9.7 `docker compose down -v` limpa o volume
