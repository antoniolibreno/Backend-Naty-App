## Why

O projeto tem esqueleto, banco e Docker, mas nenhuma regra de negocio. Nenhum
endpoint devolve dado de verdade e o app Flutter não tem contra o que programar.

O produto é uma plataforma de treinamento gamificada: os integrantes da empresa que
contratou a Naty percorrem uma trilha de atividades, cada uma com um vídeo e um quiz.
Antes de existir progresso, pontuação ou ranking, precisa existir o conteúdo que a
pessoa percorre e o modelo que representa quem ela é.

Esta etapa entrega esse chão. Ela para exatamente antes do progresso do usuário, que
é a etapa seguinte, para que o diff conte uma história só: o que existe para ser
aprendido, e não o que cada pessoa já aprendeu.

## What Changes

- Entidades novas: `Empresa`, `Trilha`, `Modulo`, `Atividade`, `Quiz`, `Pergunta` e
  `Alternativa`.
- `Usuario` deixa de ser stub e vira entidade JPA real, ganhando `empresa_id`
  obrigatório, `perfil`, `status`, `ultimo_acesso_naty` e `sincronizado_em`.
- Migration `V2` cria as tabelas de conteúdo e altera `usuario`. O índice único de
  `usuario` passa de `naty_id` sozinho para o par `(empresa_id, naty_id)`.
  **BREAKING** para qualquer linha já gravada, mas a tabela está vazia em todo
  ambiente.
- Migration `V3` semeia o conteúdo de exemplo com UUID fixo: uma trilha, dois
  módulos, três atividades por módulo, um quiz por atividade com quatro perguntas de
  quatro alternativas cada.
- Seed de desenvolvimento separado em `db/seed-dev`, com uma empresa fictícia e seus
  integrantes, apontado por `spring.flyway.locations` apenas no perfil `dev`.
- Pacotes novos `empresa` e `trilha`, cada um com seu `CLAUDE.md`.
- Endpoints de leitura: `GET /api/v1/trilhas`, `GET /api/v1/trilhas/{trilhaId}`,
  `GET /api/v1/atividades/{atividadeId}` e
  `GET /api/v1/atividades/{atividadeId}/quiz`.
- `POST /api/v1/sessoes` resolve o e-mail digitado em `usuarioId` e `empresaId`.
- MapStruct entra no `pom.xml`, porque agora existe mapeamento de entidade para DTO.
- Teste de integração com Testcontainers cobrindo o seed aplicado e os endpoints.

## Capabilities

### New Capabilities

- `conteudo-trilha`: consulta do conteúdo do treinamento. Lista de trilhas, detalhe
  de uma trilha com seus módulos e atividades aninhados, detalhe de atividade e o
  quiz de uma atividade sem o gabarito.
- `sessao-integrante`: resolução de identidade do integrante a partir do e-mail
  digitado, devolvendo o identificador dele e o da empresa. Não é autenticação.

### Modified Capabilities

Nenhuma. Não existe spec no projeto ainda.

## Impact

- Cria o modelo de domínio inteiro do conteúdo. Etapas seguintes de progresso,
  gamificação e acompanhamento penduram nele.
- `Usuario` ganha `empresa_id` obrigatório. Nenhuma consulta de usuário roda sem
  filtro de empresa a partir daqui.
- O gabarito nunca sai no payload de quiz. Correção acontece no servidor, na etapa
  seguinte. Um `correta: true` vazando aqui inutiliza a etapa 3 inteira.
- `POST /api/v1/sessoes` aceita qualquer e-mail que exista na base, sem verificar que
  a pessoa é mesmo ela. Dívida conhecida e deliberada, já registrada no `CLAUDE.md`
  da raiz, resolvida na etapa de autenticação.
- Conteúdo de treinamento é global, não por empresa. Nenhuma tabela de conteúdo tem
  `empresa_id`.
- Nenhuma escrita de conteúdo pela API. Sem CRUD administrativo enquanto não houver
  autenticação para proteger.
