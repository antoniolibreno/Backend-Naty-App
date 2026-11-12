# Spec Delta

## MODIFIED Requirements

### Requirement: Autenticação por e-mail e senha

O sistema SHALL aceitar um e-mail e uma senha, verificar a senha contra a credencial
guardada e, quando a verificação passar, devolver o identificador do integrante, o
identificador da empresa dele, o nome, o papel da conta e um token de sessão com o momento
em que ele expira. A comparação de e-mail SHALL ignorar diferença entre maiúsculas e
minúsculas e espaços nas pontas.

A senha SHALL ser guardada apenas como hash. O sistema NÃO SHALL guardar a senha em
texto, NÃO SHALL devolvê-la em resposta e NÃO SHALL registrá-la em log ou em mensagem de
erro. O token de sessão SHALL obedecer à mesma regra.

O erro de e-mail não cadastrado e o erro de senha incorreta SHALL ser indistinguíveis
para quem chama, para a resposta não revelar quais e-mails existem.

Integrante desativado e integrante de empresa inativa NÃO SHALL obter sessão, e a recusa
SHALL ser a mesma da senha incorreta. Toda recusa SHALL passar pela mesma verificação de
hash, para o tempo de resposta não revelar quais e-mails existem.

#### Scenario: E-mail cadastrado com senha correta

- **WHEN** o cliente envia um e-mail que pertence a um integrante ativo, com a senha
  correta
- **THEN** o sistema devolve o identificador do integrante, o da empresa, o nome, o papel,
  um token de sessão e o momento de expiração dele

#### Scenario: Papel na resposta

- **WHEN** uma conta `NATY`, uma conta `ADMIN` e uma conta `INTEGRANTE` autenticam com a
  senha correta
- **THEN** cada resposta traz o campo `papel` com `NATY`, `ADMIN` e `INTEGRANTE`,
  respectivamente

#### Scenario: E-mail com maiúsculas e espaços

- **WHEN** o cliente envia o mesmo e-mail com letras maiúsculas e espaços nas pontas,
  com a senha correta
- **THEN** o sistema resolve o mesmo integrante

#### Scenario: Senha incorreta

- **WHEN** o cliente envia um e-mail cadastrado com senha incorreta
- **THEN** o sistema recusa a autenticação e NÃO emite token

#### Scenario: E-mail não cadastrado

- **WHEN** o cliente envia um e-mail que não pertence a nenhum integrante
- **THEN** o sistema recusa a autenticação com a mesma resposta que devolve para senha
  incorreta

#### Scenario: Integrante desativado

- **WHEN** um integrante desativado envia a senha correta
- **THEN** o sistema recusa a autenticação e NÃO emite token

#### Scenario: Integrante de empresa inativa

- **WHEN** um integrante ativo de uma empresa inativa envia a senha correta
- **THEN** o sistema recusa a autenticação com a mesma resposta que devolve para senha
  incorreta

#### Scenario: E-mail ausente ou inválido

- **WHEN** o cliente envia corpo sem e-mail, com e-mail vazio ou em formato inválido
- **THEN** o sistema devolve erro de validação

#### Scenario: Senha ausente ou vazia

- **WHEN** o cliente envia corpo sem senha ou com senha vazia
- **THEN** o sistema devolve erro de validação

#### Scenario: Senha não retorna na resposta

- **WHEN** a autenticação ocorre com sucesso
- **THEN** a resposta NÃO contém a senha enviada, em nenhuma forma

### Requirement: Renovação da sessão

`POST /api/v1/sessoes/atual/renovacao` SHALL exigir token de sessão válido e SHALL emitir
um token novo com expiração de `app.sessao.expiracao` a partir do momento da renovação,
limitada pela duração máxima da sessão. A resposta SHALL ter o mesmo corpo da emissão de
sessão, com o token novo, o novo momento de expiração e o papel atual da conta. O token
apresentado SHALL ser revogado na mesma operação e NÃO SHALL ser aceito na chamada seguinte.
Token expirado, revogado, inexistente ou de integrante que não pode mais entrar SHALL
responder 401 `CREDENCIAL_INVALIDA`. Renovações concorrentes do mesmo token SHALL emitir no
máximo um token novo.

#### Scenario: Renovação com token válido

- **WHEN** o integrante renova a sessão com um token válido
- **THEN** o sistema devolve um token novo com expiração futura e o papel da conta, e o
  token novo é aceito nas chamadas seguintes

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
