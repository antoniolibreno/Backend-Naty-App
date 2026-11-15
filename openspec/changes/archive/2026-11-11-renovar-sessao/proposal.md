# Proposal

## Why

A sessão expira no prazo de `app.sessao.expiracao` e o app só consegue obter token novo
autenticando de novo. Quem abre o app a cada poucos dias é obrigado a digitar a senha sem
motivo de segurança, e a alternativa, um prazo de expiração muito longo, mantém token
vazado válido por semanas.

## What Changes

- `POST /api/v1/sessoes/atual/renovacao`: com token válido, emite token novo, revoga o
  token apresentado e devolve o mesmo corpo da emissão.
- Duração máxima da sessão contada a partir do login: renovar não estende além dela, e
  depois dela o app autentica de novo. O prazo vem de `app.sessao.duracao-maxima`.
- A sessão guarda o instante de início do login, que a renovação herda.

## Capabilities

### New Capabilities

### Modified Capabilities

- `sessao-integrante`: renovação do token e duração máxima da sessão.

## Impact

- `usuario` (controller, serviço, entidade, repositório), nova migration em `db/migration`,
  `application.yml`, `application-prod.yml`, `.env.example`.
- `CLAUDE.md` raiz, `usuario/CLAUDE.md` e `docs/regras.md` descrevem a renovação com duração
  máxima. `docs/backlog.md`: item 3.8.
