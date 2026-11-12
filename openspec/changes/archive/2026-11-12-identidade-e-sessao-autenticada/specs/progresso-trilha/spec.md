## ADDED Requirements

### Requirement: Integrante autenticado da requisição

Toda operação de progresso SHALL ser executada em nome do integrante dono do token de
sessão apresentado na requisição. O sistema SHALL resolver, a partir desse token, o
integrante e a empresa dele. O sistema NÃO SHALL aceitar operação de progresso sem token
válido, e NÃO SHALL aceitar integrante informado pelo cliente por qualquer outro meio.

#### Scenario: Token válido

- **WHEN** o cliente envia uma operação de progresso apresentando um token de sessão
  válido
- **THEN** o sistema executa a operação em nome do integrante dono do token e da empresa
  dele

#### Scenario: Token ausente

- **WHEN** o cliente envia uma operação de progresso sem apresentar token
- **THEN** o sistema recusa a chamada, e NÃO executa a operação

#### Scenario: Token inválido ou expirado

- **WHEN** o cliente apresenta um token desconhecido, revogado ou expirado
- **THEN** o sistema recusa a chamada, e NÃO executa a operação

#### Scenario: Integrante informado pelo cliente é ignorado

- **WHEN** o cliente apresenta um token válido e informa, por qualquer outro meio, um
  identificador de integrante diferente do dono do token
- **THEN** o sistema executa a operação em nome do dono do token

## REMOVED Requirements

### Requirement: Integrante da requisição

**Reason**: a identidade vem do token de sessão verificado pela cadeia de segurança, e
não de cabeçalho preenchido pelo cliente. O requisito "Integrante autenticado da
requisição" ocupa o lugar dele.

**Migration**: os cenários de identificador ausente, malformado e inexistente perdem o
objeto, porque o cliente deixa de informar identificador. As recusas equivalentes estão
nos cenários de token ausente e de token inválido.
