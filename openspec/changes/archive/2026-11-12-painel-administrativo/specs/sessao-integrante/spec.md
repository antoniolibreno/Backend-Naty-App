## MODIFIED Requirements

### Requirement: Autenticação por e-mail e senha

O sistema SHALL aceitar um e-mail e uma senha, verificar a senha contra a credencial
guardada e, quando a verificação passar, devolver o identificador do integrante, o
identificador da empresa dele, o nome e um token de sessão com o momento em que ele
expira. A comparação de e-mail SHALL ignorar diferença entre maiúsculas e minúsculas e
espaços nas pontas.

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
- **THEN** o sistema devolve o identificador do integrante, o da empresa, o nome, um
  token de sessão e o momento de expiração dele

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

### Requirement: Identidade de toda chamada vem do token

Toda operação que depende de saber quem chama SHALL obter o integrante, a empresa e o
papel a partir do token de sessão apresentado na requisição. O sistema NÃO SHALL aceitar o
integrante, a empresa ou o papel informados pelo cliente no corpo, na URL ou em cabeçalho
próprio.

Os papéis SHALL ser `INTEGRANTE`, `ADMIN` e `NATY`. Chamada autenticada sem o papel que a
rota exige SHALL ser recusada com 403 e código `ACESSO_NEGADO`, no formato único de erro.

Token de integrante desativado ou de empresa inativa SHALL ser recusado como token
desconhecido.

São públicas apenas a emissão de sessão, a verificação de saúde da aplicação e a
documentação da API em desenvolvimento.

#### Scenario: Token válido

- **WHEN** o cliente apresenta um token de sessão válido
- **THEN** o sistema executa a operação em nome do integrante dono do token e da empresa
  dele

#### Scenario: Token ausente

- **WHEN** o cliente chama uma operação protegida sem apresentar token
- **THEN** o sistema recusa a chamada e NÃO executa a operação

#### Scenario: Token desconhecido

- **WHEN** o cliente apresenta um token que não corresponde a nenhuma sessão
- **THEN** o sistema recusa a chamada e NÃO executa a operação

#### Scenario: Papel insuficiente

- **WHEN** o cliente apresenta um token válido numa rota que exige outro papel
- **THEN** o sistema devolve 403 com código `ACESSO_NEGADO` e NÃO executa a operação

#### Scenario: Integrante desativado com sessão aberta

- **WHEN** o cliente apresenta o token de um integrante desativado depois da emissão
- **THEN** o sistema recusa a chamada com 401

#### Scenario: Empresa inativa com sessão aberta

- **WHEN** o cliente apresenta o token de um integrante cuja empresa está inativa
- **THEN** o sistema recusa a chamada com 401

#### Scenario: Emissão de sessão é pública

- **WHEN** o cliente chama a emissão de sessão sem apresentar token
- **THEN** o sistema processa a autenticação normalmente

### Requirement: Sessão expira e é revogável

A sessão SHALL ter um momento de expiração, e o sistema NÃO SHALL aceitar token expirado.
O sistema SHALL oferecer a revogação da sessão em uso, e token revogado NÃO SHALL ser
aceito na chamada seguinte.

Redefinir a senha e desativar o integrante SHALL revogar todas as sessões dele. Desativar
a empresa SHALL revogar todas as sessões dos integrantes dela. Reativar NÃO SHALL tornar
válido token revogado. A revogação SHALL valer mesmo com chamadas concorrentes do token
revogado, e o registro do último acesso NÃO SHALL desfazê-la.

O tempo de expiração SHALL ser lido de configuração de ambiente, nunca de constante em
código.

A sessão SHALL guardar o momento do último acesso do integrante.

#### Scenario: Token expirado

- **WHEN** o cliente apresenta um token cuja expiração já passou
- **THEN** o sistema recusa a chamada e NÃO executa a operação

#### Scenario: Revogação

- **WHEN** o integrante revoga a sessão em uso
- **THEN** a chamada seguinte com aquele token é recusada

#### Scenario: Revogação pela redefinição de senha

- **WHEN** quem administra redefine a senha de um integrante
- **THEN** todos os tokens emitidos antes para aquele integrante são recusados

#### Scenario: Último acesso registrado

- **WHEN** o integrante usa uma sessão válida
- **THEN** o sistema registra o momento do acesso na própria sessão, e NÃO no cadastro do
  integrante
