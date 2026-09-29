# Backend Naty App

## Propósito

Plataforma de treinamento gamificada para os clientes da Naty, que é uma plataforma de
gerenciamento de conversas dentro do WhatsApp. Os integrantes da empresa que contratou
a Naty percorrem uma trilha de atividades, cada uma com um vídeo e um quiz, no estilo
Duolingo. Há pontuação, sequência de dias, conquistas e ranking entre os integrantes da
mesma empresa. A Naty acompanha quem está avançando e quem parou.

O backend expõe REST para um app Flutter e para um painel administrativo. O painel é a
fonte da verdade de quem existe: empresa, integrante e credencial nascem lá. A exceção é a
conta `NATY`, que nasce do bootstrap ou do seed. O sistema
não depende de nenhum serviço externo para subir.

O sistema atende várias empresas. Usuário, progresso e ranking são sempre filtrados por
empresa. Conteúdo de treinamento é global: todas as empresas fazem a mesma trilha.

## Regra número um

Antes de alterar qualquer arquivo dentro de um pacote, leia o `CLAUDE.md` daquele
pacote. Ele diz o que é decisão deliberada e o que quebra silenciosamente. Pacote sem
`CLAUDE.md` está incompleto: escreva um antes de mexer no código.

## Stack

Java 21, Spring Boot 4.1.1, Maven, PostgreSQL, Spring Data JPA, Flyway, MapStruct,
Bean Validation, springdoc-openapi, Spotless, JUnit 5, Testcontainers PostgreSQL,
Docker Compose.

Dependência entra no `pom.xml` apenas na proposta que realmente a usa. Spring Security
sustenta a cadeia de sessão opaca, sem biblioteca de token.

## Comandos

```bash
./mvnw -B verify              # compila, roda spotless:check e os testes
./mvnw -B spotless:apply      # conserta formatacao antes de commitar
docker compose up --build -d  # sobe aplicacao e PostgreSQL
docker compose down -v        # derruba e limpa o volume
```

Health em `http://localhost:8080/actuator/health`, Swagger UI em
`http://localhost:8080/swagger-ui.html`.

## Regra de escrita dos documentos

Documento enuncia a regra que vale, nunca a história de como se chegou nela. Vale para
este arquivo, para o `CLAUDE.md` de cada pacote, para `docs/`, para `README.md`, para
`openspec/` e para todo texto que sai no Swagger.

Proibido verbo de mudança: removeu, saiu, deixa de valer, passou a ser, nasce no lugar,
substituído por. Proibido referência a etapa ou a numeração de tarefa. Proibido marcador
de tempo: hoje, ainda, por ora, nesta etapa, temporário, provisório. Proibido contagem
de alteração.

Escreva a propriedade que vale agora. "O cabeçalho `X-Integrante-Id` é forjável" no
lugar de "o cabeçalho é forjável enquanto não existir autenticação". O aviso não se
perde, muda de forma. Histórico é do git.

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

Pendência não vira comentário no código nem linha de `CLAUDE.md`. Ela vira item de
`docs/backlog.md` ou tarefa no `tasks.md` da proposta OpenSpec em andamento.

## Convenções

Clean Code em todas as camadas. Arquitetura em camadas simples, sem hexagonal, sem DDD
tático completo, sem CQRS. Pacote por funcionalidade, não por camada técnica global.

Domínio em português sem sufixo desnecessário: `Usuario`, não `UsuarioEntity`. Sufixos
permitidos: `Repository`, `Service`, `Controller`, `Response`, `Request`, `Filtro`,
`Exception`. Pacotes em minúsculo, singular, sem underline.

Endpoints REST em português: `/api/v1/usuarios`, `/api/v1/atividades/{id}/quiz`.

Schema do banco é do Flyway. `ddl-auto` fica em `validate` e não muda.

Toda FK nova para `usuario` ou para `empresa` leva `on delete cascade`, e toda coluna nova
`not null` nessas duas tabelas leva default. A limpeza dos testes e o seed repetível apagam
e inserem nelas por SQL, e sem isso quebram todos os testes de uma vez. Papel novo em
`Papel` exige migration que refaz `usuario_papel_check`.

Mensagem de commit, descrição de pull request e qualquer texto publicado no repositório
não levam atribuição de ferramenta: nem trailer `Co-Authored-By`, nem linha de geração
assistida, nem link de sessão. A autoria é só de quem assina o commit. A regra vale para
qualquer agente que trabalhe aqui e está imposta em `.claude/settings.json`, com
`attribution.commit`, `attribution.pr` e `attribution.sessionUrl` desligados.

## Pacotes

`config`: cadeia de segurança e regras de papel, OpenAPI, CORS e resolvedor de argumento.

`usuario`: integrantes das empresas, papel e a resolução de identidade em
`POST /api/v1/sessoes`. Só lê o cadastro; a escrita pertence ao `painel`.

`empresa`: cliente da Naty, com fuso horário, e raiz do isolamento de dados.

`painel`: cadastro de empresa e de integrante sob `/api/v1/painel/**` e o bootstrap da
primeira conta `NATY`. Único pacote que escreve em `usuario` e em `empresa`.

`trilha`: conteúdo do treinamento e a leitura dele. Trilha, módulo, atividade, quiz,
pergunta e alternativa. Conteúdo semeado por migration, sem CRUD.

`progresso`: estado de cada integrante na trilha, com desbloqueio linear e conclusão de
atividade.

`shared`: tratamento global de erro, corpo de página e utilitário transversal. Mantenha
pequeno.

Pacote novo nasce com sua própria proposta OpenSpec e com o seu `CLAUDE.md`.

## Planejamento

Todo trabalho passa pelo OpenSpec antes do código, em `openspec/`. O CLI roda por
`npx --yes @fission-ai/openspec@latest`, sem instalação global.

O backlog vive em `docs/backlog.md`. A exportação para ferramenta de card é gerada sob
demanda e não é versionada.

### Quem é fonte da verdade de quê

Cada documento responde a uma pergunta e não invade a do vizinho.

| Documento | Pergunta | Fonte da verdade de |
|---|---|---|
| `docs/regras.md` | Por que o sistema é assim? | Invariantes de desenho, inclusive do que não está construído |
| `openspec/specs/` | O que o sistema faz? | Contrato observável do construído e verificado |
| `CLAUDE.md` raiz e de pacote | Onde mexo e o que quebra? | Mapa do código, limites aceitos, armadilhas |
| `docs/backlog.md` | O que falta? | Delta entre `docs/regras.md` e `openspec/specs/` |
| `README.md` | O que é isto? | O construído, para quem chega de fora |

Divergência entre `docs/regras.md` e `openspec/specs/` não é erro de documento, é item de
backlog. Spec não é editada para acertar com a regra fora de uma proposta; regra não é
rebaixada para caber no construído.

## Limites do sistema

Empresa e integrante nascem pelo painel. A primeira conta `NATY` nasce do seed em
desenvolvimento e do bootstrap por variável de ambiente em produção. Produção recusa subir
sem conta `NATY` e sem as variáveis do bootstrap, e qualquer perfil fora de `dev` recusa
subir se encontrar uma conta do seed.

A sessão não renova. Quando o token expira, o app autentica de novo.

Os papéis são `INTEGRANTE`, `ADMIN` e `NATY`, sem hierarquia entre eles. Só as rotas de
`/api/v1/painel/**` exigem papel. `ADMIN` administra as contas `INTEGRANTE` da própria
empresa. `NATY` administra empresas e as contas `INTEGRANTE` e `ADMIN` de qualquer empresa.
Conta `NATY` não é administrada pelo painel. Conteúdo de treinamento não tem rota de
escrita, e quando tiver ela exige `NATY`, porque o conteúdo é global.

Integrante e empresa nunca somem por desativação. Integrante não tem rota de exclusão, e
empresa só é excluída sem integrante.

Atividade conclui quando o vídeo é marcado como assistido, tenha ela quiz ou não. O quiz
é apenas leitura: nenhuma tentativa é recebida, corrigida ou guardada, e `quiz.nota_minima`
não é aplicada a nada.

`atividade.video_url` é nulo em todo o conteúdo semeado. A atividade mostra só a imagem.

Não existe pontuação, sequência de dias, conquista nem ranking. `atividade.xp` é
devolvido na leitura porque a tela mostra o valor, e nada o soma.
