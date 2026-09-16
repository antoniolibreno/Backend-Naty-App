# sessao-integrante Specification

## Purpose
Resolve qual integrante está usando o aplicativo a partir do e-mail que ele digita,
devolvendo o identificador dele e o da empresa a que pertence. É o que permite saber de
quem é o progresso em um sistema sem autenticação.

## Requirements

### Requirement: Resolução de integrante por e-mail

O sistema SHALL aceitar um e-mail e uma senha, e devolver o identificador do integrante,
o identificador da empresa dele e o nome, quando existir integrante com aquele e-mail. A
comparação de e-mail SHALL ignorar diferença entre maiúsculas e minúsculas e espaços nas
pontas.

A senha SHALL ser exigida no corpo da requisição e SHALL ser descartada em seguida. O
sistema NÃO SHALL verificar a senha, NÃO SHALL guardá-la e NÃO SHALL registrá-la em log
ou em mensagem de erro. Qualquer senha não vazia SHALL ser aceita.

#### Scenario: E-mail cadastrado

- **WHEN** o cliente envia um e-mail que pertence a um integrante existente, com senha
  preenchida
- **THEN** o sistema devolve o identificador do integrante, o da empresa e o nome

#### Scenario: E-mail com maiúsculas e espaços

- **WHEN** o cliente envia o mesmo e-mail com letras maiúsculas e espaços nas pontas
- **THEN** o sistema resolve o mesmo integrante

#### Scenario: Qualquer senha é aceita

- **WHEN** o cliente envia um e-mail cadastrado com duas senhas diferentes entre si
- **THEN** o sistema resolve o mesmo integrante nas duas vezes

#### Scenario: E-mail não cadastrado

- **WHEN** o cliente envia um e-mail que não pertence a nenhum integrante
- **THEN** o sistema devolve erro de recurso não encontrado

#### Scenario: E-mail ausente ou inválido

- **WHEN** o cliente envia corpo sem e-mail, com e-mail vazio ou em formato inválido
- **THEN** o sistema devolve erro de validação

#### Scenario: Senha ausente ou vazia

- **WHEN** o cliente envia corpo sem senha ou com senha vazia
- **THEN** o sistema devolve erro de validação

### Requirement: Resolução não é autenticação

A resolução de integrante NÃO constitui autenticação. O sistema NÃO SHALL emitir
credencial, token de sessão ou cookie a partir dela. A senha exigida no corpo NÃO SHALL
ser tratada como segredo verificado. Essa limitação SHALL estar visível na documentação
da API.

#### Scenario: Nenhuma credencial emitida

- **WHEN** a resolução de integrante ocorre com sucesso
- **THEN** a resposta NÃO contém token, credencial ou cookie de sessão

#### Scenario: Senha não retorna na resposta

- **WHEN** a resolução de integrante ocorre com sucesso
- **THEN** a resposta NÃO contém a senha enviada, em nenhuma forma

#### Scenario: Limitação documentada

- **WHEN** alguém consulta a documentação da API
- **THEN** a operação de resolução de integrante está marcada como não autenticada, e a
  senha está marcada como aceita sem verificação
