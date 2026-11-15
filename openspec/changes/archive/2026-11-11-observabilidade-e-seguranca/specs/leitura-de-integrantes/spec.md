## ADDED Requirements

### Requirement: Listagem de integrantes da empresa de quem chama

`GET /api/v1/usuarios` SHALL exigir token de sessão válido e SHALL devolver os integrantes
ativos da empresa do dono do token, de qualquer papel, em página. O sistema NÃO SHALL
devolver integrante de outra empresa nem integrante desativado. Cada item SHALL conter
identificador, nome e papel, e NÃO SHALL conter e-mail, hash de senha nem status. A ordem
SHALL ser por nome e, em empate, por identificador. A empresa SHALL vir do token, nunca de
parâmetro.

#### Scenario: Colegas da mesma empresa

- **WHEN** um integrante autenticado chama `GET /api/v1/usuarios`
- **THEN** o sistema devolve os integrantes ativos da empresa dele, inclusive ele mesmo

#### Scenario: Outra empresa não aparece

- **WHEN** existem integrantes em duas empresas e o integrante da empresa A chama a rota
- **THEN** nenhum integrante da empresa B está na resposta

#### Scenario: Desativado não aparece

- **WHEN** um integrante da empresa foi desativado
- **THEN** ele não está na listagem

#### Scenario: Campos expostos

- **WHEN** a rota devolve um integrante
- **THEN** o item contém `id`, `nome` e `papel`, e nenhum outro campo

#### Scenario: Sem token

- **WHEN** o cliente chama a rota sem token
- **THEN** o sistema responde 401 `CREDENCIAL_INVALIDA`

### Requirement: Listagem paginada

A listagem SHALL usar `pagina` e `tamanho`, com tamanho padrão 20 e máximo 100, e SHALL
responder no corpo de página padrão. O sistema NÃO SHALL devolver a lista inteira sem
página.

#### Scenario: Tamanho acima do máximo

- **WHEN** o cliente pede `tamanho=500`
- **THEN** o sistema devolve no máximo 100 itens
