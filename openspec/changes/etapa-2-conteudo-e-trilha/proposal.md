## Why

O projeto tem esqueleto, banco e Docker, mas nenhuma regra de negocio. Nenhum
endpoint devolve dado de verdade e o app Flutter nao tem contra o que programar.

O produto e uma plataforma de treinamento gamificada: os integrantes da empresa que
contratou a Naty percorrem uma trilha de atividades, cada uma com um video e um quiz.
Antes de existir progresso, pontuacao ou ranking, precisa existir o conteudo que a
pessoa percorre e o modelo que representa quem ela e.

Esta etapa entrega esse chao. Ela para exatamente antes do progresso do usuario, que
e a etapa seguinte, para que o diff conte uma historia so: o que existe para ser
aprendido, e nao o que cada pessoa ja aprendeu.

## What Changes

- Entidades novas: `Empresa`, `Trilha`, `Modulo`, `Atividade`, `Quiz`, `Pergunta` e
  `Alternativa`.
- `Usuario` deixa de ser stub e vira entidade JPA real, ganhando `empresa_id`
  obrigatorio, `perfil`, `status`, `ultimo_acesso_naty` e `sincronizado_em`.
- Migration `V2` cria as tabelas de conteudo e altera `usuario`. O indice unico de
  `usuario` passa de `naty_id` sozinho para o par `(empresa_id, naty_id)`.
  **BREAKING** para qualquer linha ja gravada, mas a tabela esta vazia em todo
  ambiente.
- Migration `V3` semeia o conteudo de exemplo com UUID fixo: uma trilha, dois
  modulos, tres atividades por modulo, um quiz por atividade com quatro perguntas de
  quatro alternativas cada.
- Seed de desenvolvimento separado em `db/seed-dev`, com uma empresa ficticia e seus
  integrantes, apontado por `spring.flyway.locations` apenas no perfil `dev`.
- Pacotes novos `empresa` e `trilha`, cada um com seu `CLAUDE.md`.
- Endpoints de leitura: `GET /api/v1/trilhas`, `GET /api/v1/trilhas/{trilhaId}`,
  `GET /api/v1/atividades/{atividadeId}` e
  `GET /api/v1/atividades/{atividadeId}/quiz`.
- `POST /api/v1/sessoes` resolve o e-mail digitado em `usuarioId` e `empresaId`.
- MapStruct entra no `pom.xml`, porque agora existe mapeamento de entidade para DTO.
- Teste de integracao com Testcontainers cobrindo o seed aplicado e os endpoints.

## Capabilities

### New Capabilities

- `conteudo-trilha`: consulta do conteudo do treinamento. Lista de trilhas, detalhe
  de uma trilha com seus modulos e atividades aninhados, detalhe de atividade e o
  quiz de uma atividade sem o gabarito.
- `sessao-integrante`: resolucao de identidade do integrante a partir do e-mail
  digitado, devolvendo o identificador dele e o da empresa. Nao e autenticacao.

### Modified Capabilities

Nenhuma. Nao existe spec no projeto ainda.

## Impact

- Cria o modelo de dominio inteiro do conteudo. Etapas seguintes de progresso,
  gamificacao e acompanhamento penduram nele.
- `Usuario` ganha `empresa_id` obrigatorio. Nenhuma consulta de usuario roda sem
  filtro de empresa a partir daqui.
- O gabarito nunca sai no payload de quiz. Correcao acontece no servidor, na etapa
  seguinte. Um `correta: true` vazando aqui inutiliza a etapa 3 inteira.
- `POST /api/v1/sessoes` aceita qualquer e-mail que exista na base, sem verificar que
  a pessoa e mesmo ela. Divida conhecida e deliberada, ja registrada no `CLAUDE.md`
  da raiz, resolvida na etapa de autenticacao.
- Conteudo de treinamento e global, nao por empresa. Nenhuma tabela de conteudo tem
  `empresa_id`.
- Nenhuma escrita de conteudo pela API. Sem CRUD administrativo enquanto nao houver
  autenticacao para proteger.
