## Purpose

Resolve qual integrante esta usando o aplicativo a partir do e-mail que ele digita,
devolvendo o identificador dele e o da empresa a que pertence. Existe porque a Naty
API nao oferece autenticacao e o primeiro corte do produto precisa saber de quem e o
progresso, mesmo sem login.

## ADDED Requirements

### Requirement: Resolucao de integrante por e-mail

O sistema SHALL aceitar um e-mail e devolver o identificador do integrante, o
identificador da empresa dele e o nome, quando existir integrante com aquele e-mail.
A comparacao de e-mail SHALL ignorar diferenca entre maiusculas e minusculas e
espacos nas pontas.

#### Scenario: E-mail cadastrado

- **WHEN** o cliente envia um e-mail que pertence a um integrante existente
- **THEN** o sistema devolve o identificador do integrante, o da empresa e o nome

#### Scenario: E-mail com maiusculas e espacos

- **WHEN** o cliente envia o mesmo e-mail com letras maiusculas e espacos nas pontas
- **THEN** o sistema resolve o mesmo integrante

#### Scenario: E-mail nao cadastrado

- **WHEN** o cliente envia um e-mail que nao pertence a nenhum integrante
- **THEN** o sistema devolve erro de recurso nao encontrado

#### Scenario: E-mail ausente ou invalido

- **WHEN** o cliente envia corpo sem e-mail, com e-mail vazio ou em formato invalido
- **THEN** o sistema devolve erro de validacao

### Requirement: Resolucao nao e autenticacao

A resolucao de integrante NAO constitui autenticacao. O sistema NAO SHALL emitir
credencial, token de sessao ou cookie a partir dela, e NAO SHALL exigir segredo do
solicitante. Essa limitacao SHALL estar visivel na documentacao da API.

#### Scenario: Nenhuma credencial emitida

- **WHEN** a resolucao de integrante ocorre com sucesso
- **THEN** a resposta NAO contem token, credencial ou cookie de sessao

#### Scenario: Limitacao documentada

- **WHEN** alguem consulta a documentacao da API
- **THEN** a operacao de resolucao de integrante esta marcada como provisoria e sem
  autenticacao
