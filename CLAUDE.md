# Backend Naty App

## Propósito

Plataforma de treinamento gamificada para os clientes da Naty, que é uma plataforma de
gerenciamento de conversas dentro do WhatsApp. Os integrantes da empresa que contratou
a Naty percorrem uma trilha de atividades, cada uma com um vídeo e um quiz, no estilo
Duolingo. Há pontuação, sequência de dias, conquistas e ranking entre os integrantes da
mesma empresa. A Naty acompanha quem está avançando e quem parou.

O backend expõe REST para um app Flutter e lê usuários da Naty API V3 para saber quem
são os integrantes de cada empresa. O sistema nunca cadastra usuário próprio: a fonte
da verdade de quem existe é o Naty App. Todo o conteúdo de treinamento e todo o
progresso são do nosso banco, porque a Naty API não tem nada disso.

O sistema atende várias empresas. Cada empresa é um cliente da Naty com seu próprio
token da Naty API. Usuário, progresso e ranking são sempre filtrados por empresa.
Conteúdo de treinamento é global: todas as empresas fazem a mesma trilha.

## Regra número um

Antes de alterar qualquer arquivo dentro de um pacote, leia o `CLAUDE.md` daquele
pacote. Ele diz o que é stub, o que é decisão deliberada e o que quebra silenciosamente.
Pacote sem `CLAUDE.md` está incompleto: escreva um antes de mexer no código.

## Stack

Java 21, Spring Boot 4.1.1, Maven, Spring RestClient, PostgreSQL, Spring Data JPA,
Flyway, MapStruct, Bean Validation, springdoc-openapi, Spotless, JUnit 5,
Testcontainers PostgreSQL, Docker Compose.

Resilience4j ainda não está no `pom.xml`. Entra na etapa que o usa.

## Comandos

```bash
./mvnw -B verify              # compila, roda spotless:check e os testes
./mvnw -B spotless:apply      # conserta formatacao antes de commitar
docker compose up --build -d  # sobe aplicacao e PostgreSQL
docker compose down -v        # derruba e limpa o volume
```

Health em `http://localhost:8080/actuator/health`, Swagger UI em
`http://localhost:8080/swagger-ui.html`.

## Regra de comentários no código

Proibido comentário explicativo. Se um trecho precisa de comentário para ser entendido,
o nome da classe, do método ou da variável está errado. Corrija o nome. Vale para
Javadoc de rotina, comentário de seção, comentário que repete o que a linha já diz e
código morto comentado.

Única exceção: comentário que serve de âncora para o Claude Code entender um trecho que
o código sozinho não revela, como o motivo de uma ordem de execução não óbvia ou uma
limitação de biblioteca. Uma linha, curtíssimo.

Proibido marcador temporal ou de autoria. Nada de "mudado em ago/26", `TODO` com nome,
`FIXME` com ano, `@since`, `@author`, "alterado por" ou changelog dentro do arquivo.
Esse histórico é do git.

Pendência vira tarefa no `tasks.md` do OpenSpec ou linha na seção Estado atual do
`CLAUDE.md` do pacote. Nunca comentário no código.

## Convenções

Clean Code em todas as camadas. Arquitetura em camadas simples, sem hexagonal, sem DDD
tático completo, sem CQRS. Pacote por funcionalidade, não por camada técnica global.

Domínio em português sem sufixo desnecessário: `Usuario`, não `UsuarioEntity`. Sufixos
permitidos: `Repository`, `Service`, `Controller`, `Response`, `Request`, `Filtro`,
`Exception`. Pacotes em minúsculo, singular, sem underline.

Endpoints REST em português: `/api/v1/usuarios`, `/api/v1/sincronizacoes/usuarios`.

Schema do banco é do Flyway. `ddl-auto` fica em `validate` e não muda.

## Pacotes

Existem hoje:

`config`: beans de infraestrutura, cliente HTTP, agendamento, OpenAPI e CORS.
`SecurityConfig` está deliberadamente ausente, o motivo está em `config/CLAUDE.md`.

`natyapi`: único ponto que fala com a Naty API V3. DTOs espelham o JSON externo, não o
domínio. As três exceções já são reais e formam o contrato de erro da integração.

`usuario`: espelho local dos integrantes das empresas, e a resolução provisória de
identidade em `POST /api/v1/sessoes`. Não escreve dado de integrante, só lê.

`empresa`: cliente da Naty, com o token da Naty API dele. Raiz do isolamento de dados.

`trilha`: conteúdo do treinamento e a leitura dele. Trilha, módulo, atividade, quiz,
pergunta e alternativa. Conteúdo semeado por migration, sem CRUD.

`sincronizacao`: único pacote com permissão de escrita em `usuario`. Orquestra
`natyapi` e `usuario`, no cron e no disparo manual.

`shared`: tratamento global de erro e utilitário transversal. Mantenha pequeno.

`health`: indicadores customizados de `/actuator/health`.

`progresso`: estado de cada integrante na trilha. Desbloqueio linear e conclusão de
atividade. Tentativa de quiz, correção e nota mínima entram na etapa do quiz.

Previstos, cada um nascendo com sua própria proposta OpenSpec:

`gamificacao`: pontos, sequência de dias, conquistas e ranking dentro da empresa.

`acompanhamento`: visão da Naty sobre quem avançou e quem parou.

## Planejamento

Todo trabalho passa pelo OpenSpec antes do código, em `openspec/`. O CLI roda por
`npx --yes @fission-ai/openspec@latest`, sem instalação global.

## Dívidas conhecidas

Não há autenticação. O app identifica a pessoa pelo e-mail digitado, sem verificação.
Consequência aceita no primeiro corte: o ranking é fraudável e qualquer um consegue
consultar qualquer empresa. Isso é resolvido na etapa de autenticação, que também
protege o CRUD de conteúdo. Não trate essa ausência como esquecimento.

`POST /api/v1/sessoes` exige senha no corpo e a descarta. Ela não é verificada, não é
guardada e não é registrada em log. Existe para o app já mandar o corpo definitivo antes
da etapa de autenticação. Qualquer senha não vazia é aceita.

As operações de progresso identificam o integrante pelo cabeçalho `X-Integrante-Id`, que
é forjável: qualquer um marca atividade como concluída em nome de outro. Aceito enquanto
não existe pontuação nem ranking a fraudar. Quando a autenticação entrar, muda só
`usuario/IntegranteArgumentResolver`, e nenhuma rota nem chamada do app é reescrita.

Atividade conclui com o vídeo assistido, tenha ela quiz ou não. Temporário: exigir
aprovação no quiz antes de a tentativa de quiz existir travaria a trilha no primeiro nó.
A regra muda na etapa do quiz.
