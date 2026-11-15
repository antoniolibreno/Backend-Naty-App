# cadastro-integrante Specification

## Purpose
Cadastra, altera, desativa e lista os integrantes de uma empresa e define a senha deles.
O administrador do cliente alcança só as contas de integrante da própria empresa, e o
time da Naty alcança todas as empresas.

## Requirements

### Requirement: Alcance do cadastro de integrante por papel

O sistema SHALL oferecer o cadastro de integrante em dois caminhos:

- `/api/v1/painel/integrantes`, só para `ADMIN`, sempre sobre a empresa de quem chama,
  resolvida pela identidade da requisição;
- `/api/v1/painel/empresas/{empresaId}/integrantes`, só para `NATY`, sobre a empresa da
  URL.

Integrante de outra empresa que não a alcançada SHALL responder 404, para a resposta não
confirmar que o identificador existe.

O `ADMIN` SHALL cadastrar contas `INTEGRANTE` e `ADMIN` na própria empresa, e SHALL alterar,
redefinir a senha e desativar apenas contas `INTEGRANTE`. Operação do `ADMIN` sobre conta
`ADMIN`, inclusive a própria, SHALL ser recusada com 403 e código `ACESSO_NEGADO`. O `NATY`
SHALL administrar contas `INTEGRANTE` e `ADMIN` de qualquer empresa.

Conta `NATY` NÃO SHALL ser alterada, ter a senha redefinida nem ser desativada pelo painel.
A tentativa SHALL ser recusada com 409 e código `CONTA_NATY_FORA_DO_PAINEL`.

#### Scenario: ADMIN sobre a própria empresa

- **WHEN** um `ADMIN` cadastra, lê, altera ou lista integrantes
- **THEN** a operação vale só para a empresa dele

#### Scenario: ADMIN sobre integrante de outra empresa

- **WHEN** um `ADMIN` lê, altera, define senha ou desativa integrante de outra empresa
- **THEN** o sistema devolve 404 e NÃO altera nada

#### Scenario: ADMIN sobre conta ADMIN

- **WHEN** um `ADMIN` altera, redefine a senha ou desativa outra conta `ADMIN` ou a própria
- **THEN** o sistema devolve 403 com código `ACESSO_NEGADO` e NÃO altera nada

#### Scenario: NATY sobre qualquer empresa

- **WHEN** um `NATY` cadastra integrante na empresa da URL ou redefine a senha de um `ADMIN`
- **THEN** a operação vale para aquela empresa

#### Scenario: Conta NATY no painel

- **WHEN** um `NATY` altera, redefine a senha ou desativa uma conta `NATY`
- **THEN** o sistema devolve 409 com código `CONTA_NATY_FORA_DO_PAINEL`

#### Scenario: INTEGRANTE no painel

- **WHEN** um `INTEGRANTE` chama qualquer rota sob `/api/v1/painel/`
- **THEN** o sistema recusa com 403 e código `ACESSO_NEGADO`

### Requirement: Cadastro e alteração de integrante

O cadastro SHALL receber nome, e-mail, senha, papel e perfil opcional. A alteração SHALL
receber nome, e-mail, papel e perfil opcional. O papel SHALL ser `INTEGRANTE` ou `ADMIN`, e
papel `NATY` no corpo SHALL ser recusado com erro de validação. O perfil SHALL ser `admin`,
`supervisor` ou `user`; no cadastro, ausente vale `user`, e na alteração, ausente mantém o
perfil atual.

O e-mail SHALL ser comparado sem diferença de maiúsculas e minúsculas e sem espaços nas
pontas, e SHALL ser único no sistema inteiro. E-mail já cadastrado em qualquer empresa, no
cadastro ou na alteração, SHALL ser recusado com 409 e código `EMAIL_JA_CADASTRADO`.

A resposta SHALL trazer identificador, empresa, nome, e-mail, papel, perfil, ativo e os
carimbos de data, e NÃO SHALL trazer senha nem hash de senha.

#### Scenario: Cadastro válido

- **WHEN** quem administra envia nome, e-mail, senha e papel válidos
- **THEN** o sistema cria o integrante ativo e devolve 201 sem senha nem hash

#### Scenario: E-mail repetido em outra empresa

- **WHEN** o e-mail enviado, em qualquer caixa, já pertence a integrante de outra empresa
- **THEN** o sistema devolve 409 com código `EMAIL_JA_CADASTRADO`

#### Scenario: E-mail repetido na alteração

- **WHEN** a alteração troca o e-mail por um que já pertence a outro integrante
- **THEN** o sistema devolve 409 com código `EMAIL_JA_CADASTRADO`

#### Scenario: Papel NATY pedido pelo cliente

- **WHEN** o corpo do cadastro traz papel `NATY`
- **THEN** o sistema devolve erro de validação e NÃO cria o integrante

#### Scenario: Alteração sem perfil

- **WHEN** a alteração não traz perfil
- **THEN** o integrante mantém o perfil que tinha

#### Scenario: Integrante cadastrado autentica

- **WHEN** o integrante recém-cadastrado autentica com o e-mail e a senha do cadastro
- **THEN** o sistema emite a sessão

### Requirement: Senha definida por quem administra

Quem administra SHALL definir a senha no cadastro e SHALL conseguir redefini-la, dentro do
alcance do papel. A senha SHALL ter no mínimo 8 caracteres e no máximo 72 bytes em UTF-8.
Redefinir a senha SHALL revogar todas as sessões do integrante, e um login concorrente com
a senha antiga NÃO SHALL produzir sessão que sobreviva à redefinição.

A senha SHALL ser guardada apenas como hash e NÃO SHALL aparecer em resposta, log ou
mensagem de erro.

#### Scenario: Redefinição

- **WHEN** quem administra redefine a senha de um integrante com sessão aberta
- **THEN** o sistema devolve 204, o token antigo é recusado com 401 e a senha nova
  autentica

#### Scenario: Senha curta ou longa demais

- **WHEN** a senha tem menos de 8 caracteres ou mais de 72 bytes
- **THEN** o sistema devolve erro de validação, sem repetir a senha na resposta

### Requirement: Desativação sem remoção

Integrante SHALL ser desativado e reativado, nunca removido. Desativar SHALL revogar as
sessões dele e SHALL preservar o progresso dele. Integrante desativado NÃO SHALL
autenticar.

#### Scenario: Desativação

- **WHEN** quem administra desativa um integrante com sessão aberta e progresso registrado
- **THEN** o token antigo é recusado com 401, o login é recusado e o progresso continua no
  banco

#### Scenario: Reativação

- **WHEN** quem administra reativa um integrante desativado
- **THEN** o integrante autentica de novo, e o token anterior à desativação continua
  recusado

### Requirement: Listagem paginada com busca

A listagem SHALL ser paginada pelos parâmetros `pagina` e `tamanho`, com tamanho padrão de
20 e máximo de 100, e SHALL devolver os itens, a página, o tamanho, o total de itens e o
total de páginas. A ordenação SHALL ser por nome e, no empate, por identificador.

A listagem SHALL aceitar `busca`, comparada com nome e e-mail sem diferença de caixa, e
`ativo`, que filtra pelo indicador de ativo.

#### Scenario: Página pedida

- **WHEN** quem administra lista com `pagina` e `tamanho`
- **THEN** o sistema devolve só os integrantes daquela página, da empresa alcançada, na
  ordem por nome

#### Scenario: Tamanho acima do máximo

- **WHEN** quem administra pede `tamanho` acima de 100
- **THEN** o sistema devolve no máximo 100 itens

#### Scenario: Busca

- **WHEN** quem administra lista com `busca` igual a parte do nome ou do e-mail
- **THEN** o sistema devolve só os integrantes que correspondem

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
