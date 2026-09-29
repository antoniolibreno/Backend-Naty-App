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

O sistema SHALL excluir apenas empresa sem integrante nem outro registro vinculado.
Empresa com vínculo SHALL ser recusada com 409 e código `EMPRESA_COM_VINCULOS`, no formato
único de erro, inclusive quando o vínculo nasce durante a exclusão.

#### Scenario: Empresa sem integrante

- **WHEN** um `NATY` exclui uma empresa sem integrante
- **THEN** o sistema remove a empresa e devolve 204

#### Scenario: Empresa com integrante

- **WHEN** um `NATY` exclui uma empresa que tem integrante
- **THEN** o sistema devolve 409 com código `EMPRESA_COM_VINCULOS` e NÃO remove nada

### Requirement: Desativação de empresa

Empresa inativa SHALL manter integrantes e progresso. Desativar a empresa SHALL revogar as
sessões dos integrantes dela.

Empresa com conta `NATY` NÃO SHALL ser desativada nem excluída. A tentativa SHALL ser
recusada com 409 e código `EMPRESA_COM_CONTA_NATY`. Reativar a empresa NÃO SHALL tornar
válido token revogado na desativação.

#### Scenario: Desativação derruba sessões

- **WHEN** um `NATY` desativa uma empresa cujo integrante tem sessão aberta
- **THEN** a chamada seguinte com aquele token é recusada com 401

#### Scenario: Empresa com conta NATY

- **WHEN** um `NATY` desativa ou exclui uma empresa que tem conta `NATY`, inclusive a dele
- **THEN** o sistema devolve 409 com código `EMPRESA_COM_CONTA_NATY` e as sessões dos `NATY`
  daquela empresa continuam válidas

#### Scenario: Reativação da empresa

- **WHEN** um `NATY` desativa e depois reativa uma empresa cujo integrante tinha sessão aberta
- **THEN** o token anterior à desativação continua recusado, e o integrante autentica de novo

### Requirement: Primeiro NATY por bootstrap

O papel `NATY` SHALL nascer apenas pelo bootstrap de subida ou pelo seed de
desenvolvimento, nunca por endpoint.

Na subida, quando não existe nenhum `NATY` e as variáveis do bootstrap estão preenchidas,
o sistema SHALL criar a empresa interna e a conta `NATY`. Quando já existe um `NATY`, o
sistema NÃO SHALL criar outro. Quando não existe `NATY`, faltam as variáveis e o bootstrap
é obrigatório no ambiente, o sistema SHALL recusar subir.

O sistema SHALL recusar subir quando a senha do bootstrap está fora do limite de senha ou
quando o e-mail do bootstrap pertence a uma conta que não é `NATY`. Instâncias que sobem ao
mesmo tempo SHALL criar uma única conta.

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

#### Scenario: E-mail do bootstrap em uso

- **WHEN** a aplicação sobe sem `NATY` e o e-mail do bootstrap pertence a uma conta que não
  é `NATY`
- **THEN** a aplicação recusa subir e NÃO altera a conta existente

### Requirement: Conta do seed só em desenvolvimento

As contas do seed de desenvolvimento têm senha pública. Fora do perfil de desenvolvimento,
a aplicação SHALL recusar subir quando encontra uma delas.

#### Scenario: Conta do seed fora de desenvolvimento

- **WHEN** a aplicação sobe fora do perfil de desenvolvimento e existe uma conta do seed
- **THEN** a aplicação recusa subir

#### Scenario: Desenvolvimento

- **WHEN** a aplicação sobe no perfil de desenvolvimento com as contas do seed
- **THEN** a aplicação sobe normalmente
