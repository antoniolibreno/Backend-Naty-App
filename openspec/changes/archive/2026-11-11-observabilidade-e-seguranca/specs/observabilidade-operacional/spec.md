## ADDED Requirements

### Requirement: Métricas de requisição e de banco

O sistema SHALL expor métricas de requisição HTTP e de conexão com o banco pelo endpoint
de métricas do Actuator. O endpoint SHALL exigir token de sessão de conta `NATY`.

#### Scenario: Métricas para NATY

- **WHEN** uma conta `NATY` consulta `/actuator/metrics`
- **THEN** o sistema devolve a lista de métricas, incluindo as de requisição HTTP e de
  conexão com o banco

#### Scenario: Métricas negadas a outro papel

- **WHEN** um `ADMIN` ou um `INTEGRANTE` consulta `/actuator/metrics`
- **THEN** o sistema responde 403 `ACESSO_NEGADO`

#### Scenario: Métricas sem token

- **WHEN** o cliente consulta `/actuator/metrics` sem token
- **THEN** o sistema responde 401 `CREDENCIAL_INVALIDA`

### Requirement: Detalhe do Actuator restrito a NATY

O sistema SHALL manter `/actuator/health` público e sem detalhe. O detalhe de saúde e todo
outro endpoint do Actuator SHALL exigir conta `NATY`.

#### Scenario: Saúde pública sem detalhe

- **WHEN** o cliente sem token consulta `/actuator/health`
- **THEN** o sistema devolve só o status geral, sem estado de banco nem de componente

#### Scenario: Detalhe de saúde para NATY

- **WHEN** uma conta `NATY` consulta `/actuator/health`
- **THEN** o sistema devolve o detalhe dos componentes

#### Scenario: Detalhe negado a integrante

- **WHEN** um `INTEGRANTE` consulta `/actuator/health`
- **THEN** o sistema devolve só o status geral, sem detalhe

### Requirement: Identificador por requisição

Toda resposta SHALL carregar o cabeçalho `X-Request-Id`, inclusive as de erro e as de
401 e 403. O sistema SHALL reaproveitar o valor enviado pelo cliente quando ele tiver de 8
a 64 caracteres entre letras, dígitos e hífen, e SHALL gerar um valor novo nos demais
casos. Toda linha de log escrita durante a chamada SHALL conter o identificador.

#### Scenario: Identificador gerado

- **WHEN** o cliente chama qualquer rota sem `X-Request-Id`
- **THEN** a resposta traz `X-Request-Id` com um valor novo

#### Scenario: Identificador do cliente reaproveitado

- **WHEN** o cliente envia `X-Request-Id: abc12345-def`
- **THEN** a resposta devolve `X-Request-Id: abc12345-def` e as linhas de log da chamada
  contêm esse valor

#### Scenario: Valor inválido descartado

- **WHEN** o cliente envia `X-Request-Id` com espaço, quebra de linha ou mais de 64
  caracteres
- **THEN** o sistema ignora o valor enviado e devolve um identificador gerado

#### Scenario: Identificador em resposta de erro

- **WHEN** a chamada termina em 401, 403, 404 ou 500
- **THEN** a resposta traz `X-Request-Id`

### Requirement: Log sem credencial

Senha, hash de senha e token de sessão NÃO SHALL aparecer em log, em mensagem de erro nem
em resposta. O sistema NÃO SHALL registrar corpo de requisição nem o cabeçalho
`Authorization`.

#### Scenario: Login com senha errada

- **WHEN** o cliente autentica com uma senha incorreta
- **THEN** nenhuma linha de log produzida pela chamada contém a senha enviada

#### Scenario: Login válido

- **WHEN** o cliente autentica com sucesso
- **THEN** nenhuma linha de log produzida pela chamada contém a senha nem o token emitido

#### Scenario: Chamada autenticada

- **WHEN** o cliente chama uma rota com `Authorization: Bearer <token>`
- **THEN** nenhuma linha de log produzida pela chamada contém o token
