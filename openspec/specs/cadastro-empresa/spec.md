# cadastro-empresa Specification

## Purpose
Cadastra as empresas clientes da Naty, com o fuso horário em que a virada do dia
acontece para elas, e cria a primeira conta capaz de fazer esse cadastro. Só o time da
Naty administra empresas.

## Requirements

### Requirement: Cadastro de empresa pelo papel NATY

O sistema SHALL oferecer listagem paginada, detalhe, criação, alteração e exclusão de
empresa em `/api/v1/painel/empresas`. Só o papel `NATY` SHALL alcançar essas operações.

A empresa SHALL ter nome, indicador de ativa e fuso horário. O fuso horário SHALL ser um
identificador de região reconhecido, como `America/Sao_Paulo`, e o sistema SHALL recusar
valor desconhecido com erro de validação.

O identificador e os carimbos de criação e de alteração SHALL ser gerados pelo sistema,
nunca informados pelo cliente.

#### Scenario: Criação de empresa

- **WHEN** um `NATY` envia nome, indicador de ativa e fuso horário válidos
- **THEN** o sistema cria a empresa e devolve 201 com identificador, nome, ativa, fuso
  horário e os carimbos de data

#### Scenario: Fuso horário desconhecido

- **WHEN** um `NATY` envia um fuso horário que não é identificador de região reconhecido
- **THEN** o sistema devolve erro de validação e NÃO cria a empresa

#### Scenario: Listagem paginada

- **WHEN** um `NATY` lista empresas informando `pagina` e `tamanho`
- **THEN** o sistema devolve a página pedida, com o total de itens e o total de páginas

#### Scenario: Papel sem alcance

- **WHEN** um `ADMIN` ou um `INTEGRANTE` chama qualquer operação de cadastro de empresa
- **THEN** o sistema recusa com 403 e código `ACESSO_NEGADO`

### Requirement: Exclusão de empresa sem vínculo

O sistema SHALL excluir apenas empresa sem integrante vinculado. Empresa com integrante
SHALL ser recusada com 409 e código `EMPRESA_COM_VINCULOS`, no formato único de erro.

#### Scenario: Empresa sem integrante

- **WHEN** um `NATY` exclui uma empresa sem integrante
- **THEN** o sistema remove a empresa e devolve 204

#### Scenario: Empresa com integrante

- **WHEN** um `NATY` exclui uma empresa que tem integrante
- **THEN** o sistema devolve 409 com código `EMPRESA_COM_VINCULOS` e NÃO remove nada

### Requirement: Desativação de empresa

Empresa inativa SHALL manter integrantes e progresso. Desativar a empresa SHALL revogar as
sessões dos integrantes dela.

O `NATY` NÃO SHALL desativar nem excluir a empresa a que pertence. A tentativa SHALL ser
recusada com 409 e código `OPERACAO_NA_PROPRIA_CONTA`.

#### Scenario: Desativação derruba sessões

- **WHEN** um `NATY` desativa uma empresa cujo integrante tem sessão aberta
- **THEN** a chamada seguinte com aquele token é recusada com 401

#### Scenario: Empresa do próprio NATY

- **WHEN** um `NATY` desativa ou exclui a empresa a que pertence
- **THEN** o sistema devolve 409 com código `OPERACAO_NA_PROPRIA_CONTA`

### Requirement: Primeiro NATY por bootstrap

O papel `NATY` SHALL nascer apenas pelo bootstrap de subida ou pelo seed de
desenvolvimento, nunca por endpoint.

Na subida, quando não existe nenhum `NATY` e as variáveis do bootstrap estão preenchidas,
o sistema SHALL criar a empresa interna e a conta `NATY`. Quando já existe um `NATY`, o
sistema NÃO SHALL criar outro. Quando não existe `NATY`, faltam as variáveis e o bootstrap
é obrigatório no ambiente, o sistema SHALL recusar subir.

A senha do bootstrap NÃO SHALL aparecer em log.

#### Scenario: Primeira subida

- **WHEN** a aplicação sobe sem nenhum `NATY` e com as variáveis do bootstrap
- **THEN** existe uma empresa interna e uma conta `NATY` capaz de autenticar

#### Scenario: Subida seguinte

- **WHEN** a aplicação sobe com um `NATY` existente
- **THEN** o sistema NÃO cria conta nem empresa

#### Scenario: Produção sem bootstrap

- **WHEN** a aplicação sobe com bootstrap obrigatório, sem nenhum `NATY` e sem as
  variáveis
- **THEN** a aplicação recusa subir
