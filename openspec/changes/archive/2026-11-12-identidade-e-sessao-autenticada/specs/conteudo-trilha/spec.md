## ADDED Requirements

### Requirement: Leitura de conteúdo exige identidade autenticada

Toda consulta de conteúdo do treinamento SHALL exigir token de sessão válido. O sistema
NÃO SHALL devolver trilha, módulo, atividade ou quiz para quem não apresenta token.

Essa exigência NÃO altera o conteúdo devolvido: ele continua global e idêntico para todas
as empresas.

#### Scenario: Consulta com token válido

- **WHEN** um integrante autenticado consulta a trilha, uma atividade ou o quiz de uma
  atividade
- **THEN** o sistema devolve o conteúdo

#### Scenario: Consulta sem token

- **WHEN** alguém consulta qualquer rota de conteúdo sem apresentar token
- **THEN** o sistema recusa a chamada, e NÃO devolve conteúdo

#### Scenario: Conteúdo continua idêntico entre empresas

- **WHEN** integrantes autenticados de duas empresas distintas consultam a mesma trilha
- **THEN** ambos recebem exatamente o mesmo conteúdo
