# Proposal

## Why

Em produção não há como responder "está lento onde" nem seguir uma chamada do app até o
erro, o detalhe de saúde do banco aparece para qualquer token autenticado, a tabela de
sessão cresce sem limite e a tela de ranking não tem de onde ler os integrantes da
empresa. A autenticação, o painel e a gamificação já existem, então o que falta é operar
o sistema com segurança antes de abrir novas rotas.

## What Changes

- Endpoint de métricas do Actuator com métricas de requisição e de banco, restrito ao
  papel `NATY`.
- Identificador por requisição: aceito no cabeçalho `X-Request-Id` ou gerado, devolvido na
  resposta e presente em toda linha de log da chamada.
- Log sem credencial: senha, hash de senha e token de sessão não aparecem em log nem em
  mensagem de erro.
- Detalhe de saúde e demais endpoints do Actuator restritos ao papel `NATY`. A saúde sem
  detalhe segue pública.
- Limpeza periódica de sessão expirada ou revogada há mais de um prazo configurável.
  Tentativa de quiz é guardada sem prazo.
- `GET /api/v1/usuarios`: lista paginada dos integrantes ativos da empresa de quem chama,
  com identificador, nome e papel.

## Capabilities

### New Capabilities

- `observabilidade-operacional`: métricas, correlação de log por requisição, log sem
  credencial e acesso restrito ao Actuator.
- `leitura-de-integrantes`: leitura paginada dos integrantes ativos da própria empresa.

### Modified Capabilities

- `sessao-integrante`: sessão expirada ou revogada além do prazo de retenção é removida.

## Impact

- `config` (cadeia de segurança, filtro de requisição), `usuario` (controller, serviço,
  limpeza de sessão), `application.yml`, `application-prod.yml`, `.env.example`.
- Sem migration nova e sem dependência nova no `pom.xml`.
- `docs/regras.md` e `CLAUDE.md` de `usuario`, `progresso`, `config` e `shared`.
- `docs/backlog.md`: itens 10.7, 10.8, 10.12, 10.13, 10.14 e 10.15.
