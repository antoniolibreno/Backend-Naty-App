# Tarefas

## 1. Leitura

- [x] 1.1 Ler `progresso/CLAUDE.md` e `usuario/CLAUDE.md`.
- [x] 1.2 Criar `ResumoProgressoResponse` e `ProximaAtividadeResponse` em `progresso/dto`.
- [x] 1.3 Criar `ProgressoService.resumir`, reusando `TrilhaProgressoMontador.sequenciar`.
- [x] 1.4 Mapear `GET /api/v1/integrantes/{integranteId}/progresso` em `ProgressoController`.

## 2. Testes

- [x] 2.1 Testar integrante sem estudo, progresso parcial e tudo concluído.
- [x] 2.2 Testar leitura de colega ativo, outra empresa, colega desativado, inexistente e sem token.
- [x] 2.3 Testar que trilha inativa fica fora da soma e da próxima atividade.
- [x] 2.4 Testar que o resumo coincide com a leitura da trilha em total, concluídas e próxima.

## 3. Documentação e fechamento

- [x] 3.1 Registrar o contrato em `progresso/CLAUDE.md`.
- [x] 3.2 Marcar 4.7 em `docs/backlog.md`.
- [x] 3.3 Rodar `./mvnw -B test-compile` e `./mvnw -B verify`.
