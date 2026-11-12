# Proposal

## Why

O app precisa mostrar "continuar de onde parou" na Home sem baixar a trilha inteira com
módulos e atividades. Só a leitura completa da trilha informa percentual e próxima
atividade, e ela exige o identificador da trilha e carrega o conteúdo todo.

## What Changes

- `GET /api/v1/integrantes/{integranteId}/progresso` devolve o total de atividades, as
  concluídas, o percentual concluído e a próxima atividade, somando todas as trilhas ativas.
- A leitura vale para a própria conta e para colega ativo da mesma empresa, com a mesma regra
  de 404 do perfil de gamificação.

## Capabilities

### New Capabilities

### Modified Capabilities

- `progresso-trilha`: leitura do resumo de progresso do integrante e a restrição de quem a lê.

## Impact

- `progresso` (controller, serviço, DTO), sem migration e sem dependência nova.
- `progresso/CLAUDE.md` e `docs/backlog.md` (item 4.7).
