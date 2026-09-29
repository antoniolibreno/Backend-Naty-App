# cadastro-integrante Specification

## Purpose
Cadastra, altera, desativa e lista os integrantes de uma empresa e define a senha deles.
O administrador do cliente alcança só a própria empresa, e o time da Naty alcança todas.

## Requirements

### Requirement: Alcance do cadastro de integrante por papel

O sistema SHALL oferecer o cadastro de integrante em dois caminhos:

- `/api/v1/painel/integrantes`, só para `ADMIN`, sempre sobre a empresa de quem chama,
  resolvida pela identidade da requisição;
- `/api/v1/painel/empresas/{empresaId}/integrantes`, só para `NATY`, sobre a empresa da
  URL.

Integrante de outra empresa que não a alcançada SHALL responder 404, para a resposta não
confirmar que o identificador existe.

#### Scenario: ADMIN sobre a própria empresa

- **WHEN** um `ADMIN` cadastra, lê, altera ou lista integrantes
- **THEN** a operação vale só para a empresa dele

#### Scenario: ADMIN sobre integrante de outra empresa

- **WHEN** um `ADMIN` lê, altera, define senha ou desativa integrante de outra empresa
- **THEN** o sistema devolve 404 e NÃO altera nada

#### Scenario: NATY sobre qualquer empresa

- **WHEN** um `NATY` cadastra integrante na empresa da URL
- **THEN** o integrante nasce vinculado àquela empresa

#### Scenario: INTEGRANTE no painel

- **WHEN** um `INTEGRANTE` chama qualquer rota sob `/api/v1/painel/`
- **THEN** o sistema recusa com 403 e código `ACESSO_NEGADO`

### Requirement: Cadastro de integrante

O cadastro SHALL receber nome, e-mail, senha, papel e perfil opcional. O papel SHALL ser
`INTEGRANTE` ou `ADMIN`. Papel `NATY` no corpo SHALL ser recusado com erro de validação.

O e-mail SHALL ser comparado sem diferença de maiúsculas e minúsculas e sem espaços nas
pontas, e SHALL ser único no sistema inteiro. E-mail já cadastrado, em qualquer empresa,
SHALL ser recusado com 409 e código `EMAIL_JA_CADASTRADO`.

A resposta SHALL trazer identificador, empresa, nome, e-mail, papel, perfil, ativo e os
carimbos de data, e NÃO SHALL trazer senha nem hash de senha.

#### Scenario: Cadastro válido

- **WHEN** quem administra envia nome, e-mail, senha e papel válidos
- **THEN** o sistema cria o integrante ativo e devolve 201 sem senha nem hash

#### Scenario: E-mail repetido em outra empresa

- **WHEN** o e-mail enviado, em qualquer caixa, já pertence a integrante de outra empresa
- **THEN** o sistema devolve 409 com código `EMAIL_JA_CADASTRADO`

#### Scenario: Papel NATY pedido pelo cliente

- **WHEN** o corpo do cadastro traz papel `NATY`
- **THEN** o sistema devolve erro de validação e NÃO cria o integrante

#### Scenario: Integrante cadastrado autentica

- **WHEN** o integrante recém-cadastrado autentica com o e-mail e a senha do cadastro
- **THEN** o sistema emite a sessão

### Requirement: Senha definida por quem administra

Quem administra SHALL definir a senha no cadastro e SHALL conseguir redefini-la. A senha
SHALL ter no mínimo 8 caracteres e no máximo 72 bytes em UTF-8. Redefinir a senha SHALL
revogar todas as sessões do integrante.

A senha SHALL ser guardada apenas como hash e NÃO SHALL aparecer em resposta, log ou
mensagem de erro.

#### Scenario: Redefinição

- **WHEN** quem administra redefine a senha de um integrante com sessão aberta
- **THEN** o sistema devolve 204, o token antigo é recusado com 401 e a senha nova
  autentica

#### Scenario: Senha curta ou longa demais

- **WHEN** a senha tem menos de 8 caracteres ou mais de 72 bytes
- **THEN** o sistema devolve erro de validação

### Requirement: Desativação sem remoção

Integrante SHALL ser desativado e reativado, nunca removido. Desativar SHALL revogar as
sessões dele e SHALL preservar o progresso dele. Integrante desativado NÃO SHALL
autenticar.

Ninguém SHALL alterar o próprio papel nem desativar a si mesmo. A tentativa SHALL ser
recusada com 409 e código `OPERACAO_NA_PROPRIA_CONTA`.

#### Scenario: Desativação

- **WHEN** quem administra desativa um integrante com sessão aberta e progresso registrado
- **THEN** o token antigo é recusado com 401, o login é recusado e o progresso continua no
  banco

#### Scenario: Reativação

- **WHEN** quem administra reativa um integrante desativado
- **THEN** o integrante autentica de novo, e o token anterior à desativação continua
  recusado

#### Scenario: Operação sobre a própria conta

- **WHEN** quem administra altera o próprio papel ou desativa a si mesmo
- **THEN** o sistema devolve 409 com código `OPERACAO_NA_PROPRIA_CONTA`

### Requirement: Listagem paginada com busca

A listagem SHALL ser paginada pelos parâmetros `pagina` e `tamanho`, com tamanho padrão de
20 e máximo de 100, e SHALL devolver os itens, a página, o tamanho, o total de itens e o
total de páginas. A ordenação SHALL ser por nome e, no empate, por identificador.

A listagem SHALL aceitar `busca`, comparada com nome e e-mail sem diferença de caixa, e
`ativo`, que filtra pelo indicador de ativo.

#### Scenario: Página pedida

- **WHEN** quem administra lista com `pagina` e `tamanho`
- **THEN** o sistema devolve só os integrantes daquela página, da empresa alcançada

#### Scenario: Tamanho acima do máximo

- **WHEN** quem administra pede `tamanho` acima de 100
- **THEN** o sistema devolve no máximo 100 itens

#### Scenario: Busca

- **WHEN** quem administra lista com `busca` igual a parte do nome ou do e-mail
- **THEN** o sistema devolve só os integrantes que correspondem
