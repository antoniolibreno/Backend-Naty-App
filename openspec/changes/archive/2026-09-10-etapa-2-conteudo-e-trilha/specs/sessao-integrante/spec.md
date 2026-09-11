## Purpose

Resolve qual integrante está usando o aplicativo a partir do e-mail que ele digita,
devolvendo o identificador dele e o da empresa a que pertence. Existe porque a Naty
API não oferece autenticação e o primeiro corte do produto precisa saber de quem é o
progresso, mesmo sem login.

## ADDED Requirements

### Requirement: Resolução de integrante por e-mail

O sistema SHALL aceitar um e-mail e devolver o identificador do integrante, o
identificador da empresa dele e o nome, quando existir integrante com aquele e-mail.
A comparação de e-mail SHALL ignorar diferença entre maiúsculas e minúsculas e
espaços nas pontas.

#### Scenario: E-mail cadastrado

- **WHEN** o cliente envia um e-mail que pertence a um integrante existente
- **THEN** o sistema devolve o identificador do integrante, o da empresa e o nome

#### Scenario: E-mail com maiúsculas e espaços

- **WHEN** o cliente envia o mesmo e-mail com letras maiúsculas e espaços nas pontas
- **THEN** o sistema resolve o mesmo integrante

#### Scenario: E-mail não cadastrado

- **WHEN** o cliente envia um e-mail que não pertence a nenhum integrante
- **THEN** o sistema devolve erro de recurso não encontrado

#### Scenario: E-mail ausente ou inválido

- **WHEN** o cliente envia corpo sem e-mail, com e-mail vazio ou em formato inválido
- **THEN** o sistema devolve erro de validação

### Requirement: Resolução não é autenticação

A resolução de integrante NÃO constitui autenticação. O sistema NÃO SHALL emitir
credencial, token de sessão ou cookie a partir dela, e NÃO SHALL exigir segredo do
solicitante. Essa limitação SHALL estar visível na documentação da API.

#### Scenario: Nenhuma credencial emitida

- **WHEN** a resolução de integrante ocorre com sucesso
- **THEN** a resposta NÃO contém token, credencial ou cookie de sessão

#### Scenario: Limitação documentada

- **WHEN** alguém consulta a documentação da API
- **THEN** a operação de resolução de integrante está marcada como provisória e sem
  autenticação
