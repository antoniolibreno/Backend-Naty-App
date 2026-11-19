# Tarefas

## 1. Modelo e configuração

- [x] 1.1 Ler `usuario/CLAUDE.md` e `config/CLAUDE.md`.
- [x] 1.2 Escrever a migration de `sessao.iniciada_em`.
- [x] 1.3 Mapear `iniciadaEm` em `Sessao` e preenchê-lo no login.
- [x] 1.4 Adicionar `app.sessao.duracao-maxima` em `application.yml`, `application-prod.yml` e `.env.example`.

## 2. Renovação

- [x] 2.1 Criar `SessaoService.renovar` com validação, duração máxima e revogação condicionada.
- [x] 2.2 Mapear `POST /api/v1/sessoes/atual/renovacao` em `SessaoController`.

## 3. Testes

- [x] 3.1 Testar renovação válida, token antigo recusado e expiração limitada pela duração máxima.
- [x] 3.2 Testar duração máxima esgotada, token expirado, revogado, desativado e sem token.
- [x] 3.3 Testar renovações concorrentes do mesmo token.
- [x] 3.4 Testar que troca de senha revoga o token renovado.
- [x] 3.5 Testar que produção não tem padrão para a duração máxima.

## 4. Documentação e fechamento

- [x] 4.1 Trocar "a sessão não renova" pela regra de renovação em `CLAUDE.md` raiz, `usuario/CLAUDE.md` e `docs/regras.md`.
- [x] 4.2 Marcar 3.8 em `docs/backlog.md`.
- [x] 4.3 Rodar `./mvnw -B test-compile` e `./mvnw -B verify`.
