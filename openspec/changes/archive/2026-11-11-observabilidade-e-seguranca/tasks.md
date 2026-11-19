# Tarefas

## 1. Leitura de integrante

- [x] 1.1 Ler `usuario/CLAUDE.md` e `config/CLAUDE.md`.
- [x] 1.2 Criar `UsuarioResponse` como record e remover as classes vazias.
- [x] 1.3 Adicionar a consulta de ativos por empresa em `UsuarioRepository`.
- [x] 1.4 Criar `listarAtivosDaEmpresa` em `UsuarioService` e o `GET` em `UsuarioController`.
- [x] 1.5 Testar empresa, desativado, campos expostos, paginação e 401.

## 2. Identificador por requisição

- [x] 2.1 Criar `RequisicaoIdFiltro` antes da cadeia de segurança.
- [x] 2.2 Incluir `requisicaoId` no padrão de log.
- [x] 2.3 Testar geração, reaproveitamento, valor inválido e resposta de erro.

## 3. Log sem credencial

- [x] 3.1 Auditar `painel`, `usuario` e `shared` por log de corpo, senha, hash e token.
- [x] 3.2 Impedir que records com senha imprimam o campo em `toString`.
- [x] 3.3 Testar a saída do log em login com senha errada, login válido e chamada com token.

## 4. Actuator

- [x] 4.1 Expor `metrics` e restringir `/actuator/**` a `NATY` mantendo a saúde pública.
- [x] 4.2 Configurar o detalhe de saúde por papel em `application-prod.yml`.
- [x] 4.3 Testar métricas e detalhe para `NATY`, `ADMIN`, `INTEGRANTE` e sem token.

## 5. Retenção de sessão

- [x] 5.1 Adicionar `app.sessao.retencao` e `app.sessao.limpeza.cron` em `application.yml`,
  `application-prod.yml` e `.env.example`, com recusa de subir em prod sem o prazo.
- [x] 5.2 Criar a consulta de remoção em `SessaoRepository` e `LimpezaDeSessao`.
- [x] 5.3 Habilitar o agendamento e neutralizar o cron no perfil de teste.
- [x] 5.4 Testar expirada, revogada, dentro do prazo, válida e preservação de progresso.

## 6. Documentação e fechamento

- [x] 6.1 Registrar a retenção em `docs/regras.md` e nos `CLAUDE.md` de `usuario` e `progresso`.
- [x] 6.2 Registrar a regra de log e de identificador em `shared/CLAUDE.md` e `config/CLAUDE.md`.
- [x] 6.3 Marcar 10.7, 10.8, 10.12, 10.13, 10.14 e 10.15 em `docs/backlog.md`.
- [x] 6.4 Rodar `./mvnw -B test-compile` e `./mvnw -B verify`.
