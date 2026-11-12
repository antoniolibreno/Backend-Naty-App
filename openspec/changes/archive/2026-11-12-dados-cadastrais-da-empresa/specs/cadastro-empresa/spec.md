# Spec Delta

## MODIFIED Requirements

### Requirement: Cadastro de empresa pelo papel NATY

O sistema SHALL oferecer listagem paginada, detalhe, criação, alteração e exclusão de
empresa em `/api/v1/painel/empresas`. Só o papel `NATY` SHALL alcançar essas operações.

A empresa SHALL ter nome, indicador de ativa, fuso horário, CNPJ e razão social. O fuso
horário SHALL ser um identificador de região reconhecido, como `America/Sao_Paulo`, e o
sistema SHALL recusar valor desconhecido com erro de validação. O responsável pela empresa
SHALL ser a conta `ADMIN` dela, e a empresa NÃO SHALL guardar nome, e-mail nem telefone de
responsável.

A criação e a alteração SHALL exigir CNPJ e razão social. A criação SHALL exigir também o
bloco `administrador`, descrito no requisito "Criação de empresa com o primeiro ADMIN".

O identificador e os carimbos de criação e de alteração SHALL ser gerados pelo sistema,
nunca informados pelo cliente.

#### Scenario: Criação de empresa

- **WHEN** um `NATY` envia nome, indicador de ativa, fuso horário, CNPJ, razão social e
  administrador válidos
- **THEN** o sistema cria a empresa e devolve 201 com identificador, nome, ativa, fuso
  horário, CNPJ, razão social e os carimbos de data

#### Scenario: Fuso horário desconhecido

- **WHEN** um `NATY` envia um fuso horário que não é identificador de região reconhecido
- **THEN** o sistema devolve erro de validação e NÃO cria a empresa

#### Scenario: Dado cadastral ausente

- **WHEN** um `NATY` cria ou altera uma empresa sem CNPJ ou sem razão social
- **THEN** o sistema devolve erro de validação e NÃO grava nada

#### Scenario: Empresa sem CNPJ e sem razão social no banco

- **WHEN** um `NATY` lê uma empresa gravada sem CNPJ e sem razão social
- **THEN** o sistema devolve a empresa com esses campos nulos

#### Scenario: Listagem paginada

- **WHEN** um `NATY` lista empresas informando `pagina` e `tamanho`
- **THEN** o sistema devolve a página pedida, com o total de itens e o total de páginas

#### Scenario: Papel sem alcance

- **WHEN** um `ADMIN` ou um `INTEGRANTE` chama qualquer operação de cadastro de empresa
- **THEN** o sistema recusa com 403 e código `ACESSO_NEGADO`

## ADDED Requirements

### Requirement: CNPJ válido e normalizado

O sistema SHALL aceitar o CNPJ com ou sem máscara de pontos, barra e hífen, e com letras
minúsculas. O sistema SHALL guardar e devolver o CNPJ com 14 caracteres, sem máscara e em
maiúsculas: 12 letras ou dígitos seguidos de 2 dígitos verificadores. O sistema SHALL
recusar com erro de validação o CNPJ fora desse formato, com dígito verificador incorreto
ou com os 14 caracteres iguais.

#### Scenario: CNPJ numérico com máscara

- **WHEN** um `NATY` cria uma empresa com o CNPJ `11.222.333/0001-81`
- **THEN** o sistema cria a empresa e devolve o CNPJ `11222333000181`

#### Scenario: CNPJ alfanumérico

- **WHEN** um `NATY` cria uma empresa com o CNPJ `12.abc.345/01de-35`
- **THEN** o sistema cria a empresa e devolve o CNPJ `12ABC34501DE35`

#### Scenario: Dígito verificador incorreto

- **WHEN** um `NATY` envia o CNPJ `11.222.333/0001-82`
- **THEN** o sistema devolve erro de validação e NÃO grava nada

#### Scenario: Caracteres repetidos

- **WHEN** um `NATY` envia o CNPJ `00000000000000`
- **THEN** o sistema devolve erro de validação e NÃO grava nada

### Requirement: CNPJ único entre as empresas

Duas empresas NÃO SHALL ter o mesmo CNPJ. A criação ou a alteração com CNPJ de outra
empresa SHALL ser recusada com 409 e código `CNPJ_JA_CADASTRADO`, no formato único de erro,
inclusive quando as duas gravações acontecem ao mesmo tempo. Alterar a empresa mantendo o
próprio CNPJ SHALL ser aceito.

#### Scenario: CNPJ de outra empresa

- **WHEN** um `NATY` cria uma empresa com o CNPJ de uma empresa existente, com ou sem
  máscara
- **THEN** o sistema devolve 409 com código `CNPJ_JA_CADASTRADO` e NÃO cria a empresa

#### Scenario: Alteração para CNPJ de outra empresa

- **WHEN** um `NATY` altera uma empresa informando o CNPJ de outra empresa
- **THEN** o sistema devolve 409 com código `CNPJ_JA_CADASTRADO` e NÃO altera nada

#### Scenario: Alteração mantendo o próprio CNPJ

- **WHEN** um `NATY` altera o nome de uma empresa e reenvia o CNPJ dela
- **THEN** o sistema altera a empresa e devolve 200

### Requirement: Busca na listagem de empresas

A listagem de empresas SHALL aceitar o parâmetro opcional `busca`. Com ele, o sistema SHALL
devolver só as empresas cujo nome ou razão social contém o texto, sem diferença de
maiúsculas e minúsculas, ou cujo CNPJ contém o texto sem máscara. A ordem e a paginação
SHALL ser as mesmas da listagem sem busca. Busca vazia SHALL equivaler à ausência dela.

#### Scenario: Busca por parte do nome

- **WHEN** um `NATY` lista empresas com `busca=acme` e existe a empresa de nome
  `Acme Treinamentos`
- **THEN** a página contém essa empresa e o total de itens conta só as encontradas

#### Scenario: Busca por razão social

- **WHEN** um `NATY` lista empresas com parte da razão social em maiúsculas
- **THEN** a página contém a empresa daquela razão social

#### Scenario: Busca por CNPJ com máscara

- **WHEN** um `NATY` lista empresas com `busca=11.222.333`
- **THEN** a página contém a empresa de CNPJ `11222333000181`

#### Scenario: Busca sem resultado

- **WHEN** um `NATY` lista empresas com um texto que nenhuma empresa contém
- **THEN** o sistema devolve página vazia com total de itens zero

### Requirement: Criação de empresa com o primeiro ADMIN

A criação de empresa SHALL exigir o bloco `administrador`, com nome, e-mail, senha e
telefone, e SHALL criar a empresa e a conta `ADMIN` ativa dela na mesma transação, com as
regras de e-mail e de senha do cadastro de integrante. O telefone SHALL ser guardado só com
dígitos, de 10 a 13. Falha em qualquer parte NÃO SHALL criar empresa nem conta. A alteração
de empresa NÃO SHALL criar conta.

#### Scenario: Empresa com administrador

- **WHEN** um `NATY` cria uma empresa com o bloco `administrador` válido
- **THEN** o sistema devolve 201, e a conta criada autentica com o e-mail e a senha
  enviados e tem papel `ADMIN` na nova empresa

#### Scenario: Administrador ausente

- **WHEN** um `NATY` cria uma empresa sem o bloco `administrador`
- **THEN** o sistema devolve erro de validação e NÃO cria a empresa

#### Scenario: Dado do administrador ausente

- **WHEN** um `NATY` cria uma empresa com `administrador` sem nome, sem e-mail, sem senha
  ou sem telefone
- **THEN** o sistema devolve erro de validação e NÃO cria a empresa nem a conta

#### Scenario: Telefone do administrador com máscara

- **WHEN** um `NATY` envia o telefone do administrador `(11) 98765-4321`
- **THEN** o sistema guarda o telefone `11987654321` na conta

#### Scenario: Telefone do administrador fora do tamanho

- **WHEN** um `NATY` envia telefone do administrador com menos de 10 ou mais de 13 dígitos
- **THEN** o sistema devolve erro de validação e NÃO cria a empresa nem a conta

#### Scenario: E-mail do administrador já cadastrado

- **WHEN** um `NATY` cria uma empresa com `administrador` cujo e-mail já pertence a uma conta
- **THEN** o sistema devolve 409 com código `EMAIL_JA_CADASTRADO` e NÃO cria a empresa

#### Scenario: CNPJ repetido com administrador válido

- **WHEN** um `NATY` cria uma empresa com CNPJ de outra empresa e `administrador` válido
- **THEN** o sistema devolve 409 com código `CNPJ_JA_CADASTRADO` e NÃO cria a conta

#### Scenario: Senha do administrador fora do limite

- **WHEN** um `NATY` cria uma empresa com `administrador` de senha menor que 8 caracteres
  ou maior que 72 bytes
- **THEN** o sistema devolve erro de validação e NÃO cria a empresa

#### Scenario: Alteração com bloco administrador

- **WHEN** um `NATY` altera uma empresa enviando também o bloco `administrador`
- **THEN** o sistema altera a empresa e NÃO cria conta

#### Scenario: Senha do administrador fora da resposta e do log

- **WHEN** um `NATY` cria uma empresa com o bloco `administrador`
- **THEN** a resposta e o log NÃO contêm a senha enviada, em nenhuma forma
