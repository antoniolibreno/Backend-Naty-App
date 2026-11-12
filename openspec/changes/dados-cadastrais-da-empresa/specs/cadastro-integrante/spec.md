# Spec Delta

## ADDED Requirements

### Requirement: Telefone do integrante

O cadastro e a alteração de integrante SHALL aceitar `telefone` opcional. O sistema SHALL
guardá-lo só com dígitos, aceitar de 10 a 13 dígitos e recusar outro tamanho com 400
`FALHA_DE_VALIDACAO`. A alteração sem `telefone` SHALL manter o valor atual, e a resposta de
integrante SHALL devolver `telefone`, nulo quando não informado.

#### Scenario: Cadastro sem telefone

- **WHEN** um `ADMIN` ou `NATY` cadastra um integrante sem `telefone`
- **THEN** o sistema cria a conta e devolve `telefone` nulo

#### Scenario: Telefone com máscara

- **WHEN** um `ADMIN` ou `NATY` cadastra um integrante com o telefone `(11) 98765-4321`
- **THEN** o sistema guarda e devolve `11987654321`

#### Scenario: Telefone fora do tamanho

- **WHEN** um `ADMIN` ou `NATY` envia um telefone com 9 ou com 14 dígitos
- **THEN** o sistema devolve 400 com código `FALHA_DE_VALIDACAO` e não grava a conta

#### Scenario: Alteração sem telefone

- **WHEN** um `ADMIN` ou `NATY` altera um integrante que tem telefone, sem enviar `telefone`
- **THEN** o sistema mantém o telefone atual

### Requirement: Empresa sempre com ADMIN ativo

Toda empresa SHALL ter ao menos uma conta `ADMIN` ativa. Desativar a conta ou alterar o
papel da conta que é o último `ADMIN` ativo da empresa SHALL ser recusado com 409 e código
`ULTIMO_ADMINISTRADOR`, no formato único de erro, e a conta SHALL ficar como estava.
Desativar a empresa inteira NÃO SHALL ser afetado por essa regra.

#### Scenario: Desativar o último ADMIN

- **WHEN** um `NATY` desativa a única conta `ADMIN` ativa da empresa
- **THEN** o sistema devolve 409 com código `ULTIMO_ADMINISTRADOR` e a conta continua ativa

#### Scenario: ADMIN desativa a própria conta

- **WHEN** o único `ADMIN` ativo da empresa tenta desativar a própria conta
- **THEN** o sistema devolve 403 `ACESSO_NEGADO`, porque conta `ADMIN` só é administrada pelo
  papel `NATY`, e a conta continua ativa

#### Scenario: Rebaixar o último ADMIN

- **WHEN** um `NATY` altera o papel da única conta `ADMIN` ativa da empresa para
  `INTEGRANTE`
- **THEN** o sistema devolve 409 com código `ULTIMO_ADMINISTRADOR` e o papel não muda

#### Scenario: Desativar um ADMIN havendo outro ativo

- **WHEN** um `NATY` desativa uma conta `ADMIN` da empresa e existe outra conta `ADMIN`
  ativa na mesma empresa
- **THEN** o sistema desativa a conta e devolve 204

#### Scenario: Outro ADMIN inativo não conta

- **WHEN** um `NATY` desativa a única conta `ADMIN` ativa de uma empresa que tem outra conta
  `ADMIN` já desativada
- **THEN** o sistema devolve 409 com código `ULTIMO_ADMINISTRADOR`

#### Scenario: Desativar a empresa inteira

- **WHEN** um `NATY` desativa uma empresa que tem uma única conta `ADMIN` ativa
- **THEN** o sistema desativa a empresa e devolve 200
