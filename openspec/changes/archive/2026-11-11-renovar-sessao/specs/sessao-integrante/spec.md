## ADDED Requirements

### Requirement: Renovação da sessão

`POST /api/v1/sessoes/atual/renovacao` SHALL exigir token de sessão válido e SHALL emitir
um token novo com expiração de `app.sessao.expiracao` a partir do momento da renovação,
limitada pela duração máxima da sessão. A resposta SHALL ter o mesmo corpo da emissão de
sessão, com o token novo e o novo momento de expiração. O token apresentado SHALL ser
revogado na mesma operação e NÃO SHALL ser aceito na chamada seguinte. Token expirado,
revogado, inexistente ou de integrante que não pode mais entrar SHALL responder 401
`CREDENCIAL_INVALIDA`. Renovações concorrentes do mesmo token SHALL emitir no máximo um
token novo.

#### Scenario: Renovação com token válido

- **WHEN** o integrante renova a sessão com um token válido
- **THEN** o sistema devolve um token novo com expiração futura, e o token novo é aceito
  nas chamadas seguintes

#### Scenario: Token antigo recusado

- **WHEN** o integrante usa o token apresentado na renovação depois de renovar
- **THEN** o sistema responde 401 `CREDENCIAL_INVALIDA`

#### Scenario: Token expirado

- **WHEN** o cliente tenta renovar com um token cuja expiração já passou
- **THEN** o sistema responde 401 `CREDENCIAL_INVALIDA` e NÃO emite token

#### Scenario: Token revogado

- **WHEN** o cliente tenta renovar com um token revogado
- **THEN** o sistema responde 401 `CREDENCIAL_INVALIDA` e NÃO emite token

#### Scenario: Integrante desativado

- **WHEN** o integrante foi desativado e tenta renovar com um token anterior à desativação
- **THEN** o sistema responde 401 `CREDENCIAL_INVALIDA`

#### Scenario: Renovações concorrentes

- **WHEN** duas renovações do mesmo token chegam ao mesmo tempo
- **THEN** exatamente uma emite token novo e a outra responde 401 `CREDENCIAL_INVALIDA`

#### Scenario: Sem token

- **WHEN** o cliente chama a renovação sem token
- **THEN** o sistema responde 401 `CREDENCIAL_INVALIDA`

### Requirement: Duração máxima da sessão

A sessão SHALL ter duração máxima contada a partir da autenticação por e-mail e senha, e
as renovações NÃO SHALL estendê-la. A expiração de um token renovado SHALL ser o menor
entre o prazo de expiração a partir da renovação e o fim da duração máxima. Depois do fim
da duração máxima, a renovação SHALL responder 401 `CREDENCIAL_INVALIDA`. A duração máxima
SHALL ser lida de configuração de ambiente, nunca de constante em código, e a produção
SHALL recusar subir sem ela.

#### Scenario: Expiração limitada pela duração máxima

- **WHEN** o integrante renova a sessão quando falta menos que o prazo de expiração para o
  fim da duração máxima
- **THEN** a expiração do token novo é o fim da duração máxima

#### Scenario: Duração máxima esgotada

- **WHEN** o integrante tenta renovar depois do fim da duração máxima contada do login
- **THEN** o sistema responde 401 `CREDENCIAL_INVALIDA` e o app autentica de novo

#### Scenario: Revogação alcança a cadeia de renovações

- **WHEN** quem administra redefine a senha do integrante ou o desativa
- **THEN** todos os tokens do integrante, inclusive os emitidos por renovação, são recusados

#### Scenario: Produção sem duração máxima

- **WHEN** a aplicação sobe em produção sem a duração máxima configurada
- **THEN** a aplicação recusa subir
