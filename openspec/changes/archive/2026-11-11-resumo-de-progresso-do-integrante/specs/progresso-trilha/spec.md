## ADDED Requirements

### Requirement: Resumo de progresso do integrante

`GET /api/v1/integrantes/{integranteId}/progresso` SHALL devolver o identificador do
integrante, o total de atividades, quantas ele concluiu, o percentual concluído e a próxima
atividade a estudar, somando todas as trilhas ativas. O percentual SHALL ser a divisão
inteira de concluídas por total vezes cem, e zero quando não há atividade. A próxima
atividade SHALL ser a primeira não concluída, na ordem das trilhas ativas e, dentro da
trilha, a mesma atividade que a leitura da trilha indica como próxima, e SHALL conter o
identificador da atividade, o da trilha e o título. Sem atividade pendente, a próxima
atividade SHALL ser nula. Trilha inativa NÃO SHALL entrar na soma nem ser indicada como
próxima. A resposta NÃO SHALL conter módulos nem atividades.

#### Scenario: Progresso parcial

- **WHEN** o integrante que concluiu parte das atividades lê o próprio resumo
- **THEN** a resposta traz o total, o número de concluídas, o percentual e a próxima
  atividade com o identificador da trilha dela

#### Scenario: Integrante sem estudo

- **WHEN** o integrante sem nenhuma atividade concluída lê o próprio resumo
- **THEN** a resposta traz zero concluídas, percentual zero e a primeira atividade da
  primeira trilha ativa como próxima

#### Scenario: Tudo concluído

- **WHEN** o integrante que concluiu todas as atividades das trilhas ativas lê o resumo
- **THEN** o percentual é cem e a próxima atividade é nula

#### Scenario: Trilha inativa

- **WHEN** existe trilha inativa com atividades
- **THEN** o total, as concluídas e a próxima atividade ignoram essa trilha

### Requirement: Resumo restrito à empresa de quem chamou

O resumo de progresso SHALL exigir token de sessão válido e SHALL ser lido apenas para a
conta ativa da empresa do dono do token. Integrante de outra empresa, integrante
desativado e identificador inexistente SHALL responder 404 com o mesmo código
`RECURSO_NAO_ENCONTRADO`.

#### Scenario: Colega da mesma empresa

- **WHEN** o integrante lê o resumo de um colega ativo da mesma empresa
- **THEN** o sistema devolve o resumo do colega, e não o do próprio integrante

#### Scenario: Integrante de outra empresa

- **WHEN** o integrante lê o resumo de um integrante de outra empresa
- **THEN** o sistema responde 404 `RECURSO_NAO_ENCONTRADO`

#### Scenario: Colega desativado

- **WHEN** o integrante lê o resumo de um colega desativado da mesma empresa
- **THEN** o sistema responde 404 `RECURSO_NAO_ENCONTRADO`

#### Scenario: Sem token

- **WHEN** o cliente lê um resumo sem token
- **THEN** o sistema responde 401 `CREDENCIAL_INVALIDA`
