# Backend Naty App

## Proposito

Plataforma de treinamento gamificada para os clientes da Naty, que e uma plataforma de
gerenciamento de conversas dentro do WhatsApp. Os integrantes da empresa que contratou
a Naty percorrem uma trilha de atividades, cada uma com um video e um quiz, no estilo
Duolingo. Ha pontuacao, sequencia de dias, conquistas e ranking entre os integrantes da
mesma empresa. A Naty acompanha quem esta avancando e quem parou.

O backend expoe REST para um app Flutter e le usuarios da Naty API V3 para saber quem
sao os integrantes de cada empresa. O sistema nunca cadastra usuario proprio: a fonte
da verdade de quem existe e o Naty App. Todo o conteudo de treinamento e todo o
progresso sao do nosso banco, porque a Naty API nao tem nada disso.

O sistema atende varias empresas. Cada empresa e um cliente da Naty com seu proprio
token da Naty API. Usuario, progresso e ranking sao sempre filtrados por empresa.
Conteudo de treinamento e global: todas as empresas fazem a mesma trilha.

## Regra numero um

Antes de alterar qualquer arquivo dentro de um pacote, leia o `CLAUDE.md` daquele
pacote. Ele diz o que e stub, o que e decisao deliberada e o que quebra silenciosamente.
Pacote sem `CLAUDE.md` esta incompleto: escreva um antes de mexer no codigo.

## Stack

Java 21, Spring Boot 4.1.1, Maven, Spring RestClient, PostgreSQL, Spring Data JPA,
Flyway, Bean Validation, springdoc-openapi, Spotless, JUnit 5, Testcontainers
PostgreSQL, Docker Compose.

MapStruct e Resilience4j ainda nao estao no `pom.xml`. Entram na etapa que os usa.

## Comandos

```bash
./mvnw -B verify              # compila, roda spotless:check e os testes
./mvnw -B spotless:apply      # conserta formatacao antes de commitar
docker compose up --build -d  # sobe aplicacao e PostgreSQL
docker compose down -v        # derruba e limpa o volume
```

Health em `http://localhost:8080/actuator/health`, Swagger UI em
`http://localhost:8080/swagger-ui.html`.

## Regra de comentarios no codigo

Proibido comentario explicativo. Se um trecho precisa de comentario para ser entendido,
o nome da classe, do metodo ou da variavel esta errado. Corrija o nome. Vale para
Javadoc de rotina, comentario de secao, comentario que repete o que a linha ja diz e
codigo morto comentado.

Unica excecao: comentario que serve de ancora para o Claude Code entender um trecho que
o codigo sozinho nao revela, como o motivo de uma ordem de execucao nao obvia ou uma
limitacao de biblioteca. Uma linha, curtissimo.

Proibido marcador temporal ou de autoria. Nada de "mudado em ago/26", `TODO` com nome,
`FIXME` com ano, `@since`, `@author`, "alterado por" ou changelog dentro do arquivo.
Esse historico e do git.

Pendencia vira tarefa no `tasks.md` do OpenSpec ou linha na secao Estado atual do
`CLAUDE.md` do pacote. Nunca comentario no codigo.

## Convencoes

Clean Code em todas as camadas. Arquitetura em camadas simples, sem hexagonal, sem DDD
tatico completo, sem CQRS. Pacote por funcionalidade, nao por camada tecnica global.

Dominio em portugues sem sufixo desnecessario: `Usuario`, nao `UsuarioEntity`. Sufixos
permitidos: `Repository`, `Service`, `Controller`, `Response`, `Request`, `Filtro`,
`Exception`. Pacotes em minusculo, singular, sem underline.

Endpoints REST em portugues: `/api/v1/usuarios`, `/api/v1/sincronizacoes/usuarios`.

Schema do banco e do Flyway. `ddl-auto` fica em `validate` e nao muda.

## Pacotes

Existem hoje:

`config`: beans de infraestrutura, cliente HTTP, agendamento, OpenAPI e CORS.
`SecurityConfig` esta deliberadamente ausente, o motivo esta em `config/CLAUDE.md`.

`natyapi`: unico ponto que fala com a Naty API V3. DTOs espelham o JSON externo, nao o
dominio. As tres excecoes ja sao reais e formam o contrato de erro da integracao.

`usuario`: espelho local dos integrantes das empresas, e a resolucao provisoria de
identidade em `POST /api/v1/sessoes`. Nao escreve dado de integrante, so le.

`empresa`: cliente da Naty, com o token da Naty API dele. Raiz do isolamento de dados.

`trilha`: conteudo do treinamento e a leitura dele. Trilha, modulo, atividade, quiz,
pergunta e alternativa. Conteudo semeado por migration, sem CRUD.

`sincronizacao`: unico pacote com permissao de escrita em `usuario`. Orquestra
`natyapi` e `usuario`, no cron e no disparo manual.

`shared`: tratamento global de erro e utilitario transversal. Mantenha pequeno.

`health`: indicadores customizados de `/actuator/health`.

Previstos, cada um nascendo com sua propria proposta OpenSpec:

`progresso`: estado de cada integrante na trilha. Desbloqueio linear, tentativa de
quiz, correcao e nota minima.

`gamificacao`: pontos, sequencia de dias, conquistas e ranking dentro da empresa.

`acompanhamento`: visao da Naty sobre quem avancou e quem parou.

## Planejamento

Todo trabalho passa pelo OpenSpec antes do codigo, em `openspec/`. O CLI roda por
`npx --yes @fission-ai/openspec@latest`, sem instalacao global.

## Dividas conhecidas

Nao ha autenticacao. O app identifica a pessoa pelo e-mail digitado, sem verificacao.
Consequencia aceita no primeiro corte: o ranking e fraudavel e qualquer um consegue
consultar qualquer empresa. Isso e resolvido na etapa de autenticacao, que tambem
protege o CRUD de conteudo. Nao trate essa ausencia como esquecimento.
