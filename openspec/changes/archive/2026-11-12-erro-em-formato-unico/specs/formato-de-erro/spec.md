## ADDED Requirements

### Requirement: Erro da requisição em formato único

Todo erro de requisição SHALL sair em `ErroResposta`, com momento, status, código estável,
mensagem, caminho e campos. O status do corpo SHALL ser o mesmo da resposta. A mensagem
NÃO SHALL repetir trecho do corpo enviado.

Os códigos de erro de requisição SHALL ser: `REQUISICAO_MALFORMADA` para 400 de corpo
ilegível, identificador malformado ou parâmetro de tipo errado, `FALHA_DE_VALIDACAO` para
400 de validação de corpo ou de parâmetro, `CREDENCIAL_INVALIDA` para 401, `ACESSO_NEGADO`
para 403, `RECURSO_NAO_ENCONTRADO` para 404, `METODO_NAO_SUPORTADO` para 405,
`FORMATO_NAO_ACEITO` para 406 e `TIPO_DE_CONTEUDO_NAO_SUPORTADO` para 415. Os 409 de negócio
têm código próprio, definido pela capacidade que os devolve.

Rota inexistente sob `/api/v1/painel/` responde 403 `ACESSO_NEGADO` para quem tem token,
porque a cadeia de segurança nega o prefixo inteiro antes de o MVC procurar a rota.

Exceção não tratada NÃO SHALL sair como 401: sai como 500 no corpo padrão do Spring Boot,
sem mensagem nem pilha.

#### Scenario: Rota inexistente com token válido

- **WHEN** o cliente autenticado chama uma rota que não existe
- **THEN** o sistema devolve 404 com código `RECURSO_NAO_ENCONTRADO`

#### Scenario: Rota inexistente sem token

- **WHEN** o cliente sem token chama uma rota que não existe
- **THEN** o sistema devolve 401 com código `CREDENCIAL_INVALIDA`

#### Scenario: Parâmetro de tipo errado

- **WHEN** o cliente autenticado envia um parâmetro de consulta que não converte no tipo
  esperado
- **THEN** o sistema devolve 400 com código `REQUISICAO_MALFORMADA`

#### Scenario: Identificador malformado

- **WHEN** o cliente autenticado envia na URL um identificador que não é UUID
- **THEN** o sistema devolve 400 com código `REQUISICAO_MALFORMADA`

#### Scenario: JSON malformado

- **WHEN** o cliente envia corpo JSON malformado, inclusive na emissão de sessão
- **THEN** o sistema devolve 400 com código `REQUISICAO_MALFORMADA`, e o corpo da resposta
  NÃO contém trecho do que foi enviado

#### Scenario: Método não suportado

- **WHEN** o cliente autenticado chama uma rota existente com método que ela não aceita
- **THEN** o sistema devolve 405 com código `METODO_NAO_SUPORTADO`

#### Scenario: Tipo de conteúdo não suportado

- **WHEN** o cliente envia corpo com tipo de conteúdo que a rota não aceita
- **THEN** o sistema devolve 415 com código `TIPO_DE_CONTEUDO_NAO_SUPORTADO`
