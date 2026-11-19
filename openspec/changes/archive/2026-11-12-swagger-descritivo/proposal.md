# Proposal

## Why

O Swagger é o contrato do time do app Flutter e do painel. Nenhum DTO carrega `@Schema`, então
o Swagger UI mostra campos sem descrição e corpos de exemplo com valores genéricos (`string`,
`0`). As respostas de erro (401, 403, 404, 409) não aparecem nas operações, e os códigos
estáveis de `codigo` só existem no código-fonte.

## What Changes

- `@Schema` com `description` e `example` em todo record de `Request` e `Response`, em todos os
  pacotes que expõem REST.
- `ErroResposta` documentado, com exemplo de cada formato de erro.
- Respostas de erro declaradas nas operações (`@ApiResponse` com 400, 401, 403, 404, 409), com
  os códigos estáveis de cada rota.
- Descrição dos parâmetros de rota e de consulta (`@Parameter`) onde o nome não basta.
- `description` das tags e das operações revisada para dizer papel exigido, efeito e restrições.
- Respostas comuns de 401 e 403 declaradas uma vez no `OpenApiConfig`.

Nenhuma rota, corpo, status ou regra de negócio muda. **Sem alteração de contrato HTTP.**

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

Nenhuma. A documentação OpenAPI não altera requisito de comportamento, por isso a proposta
declara `skip_specs: true`.

## Impact

- Código: records `*Request` e `*Response`, controllers e `config/OpenApiConfig.java`, todos
  por anotação.
- Dependência: nenhuma nova. `io.swagger.v3.oas.annotations` já vem do springdoc.
- Teste: nenhum comportamento novo. Um teste de integração lê `/v3/api-docs` e garante que todo
  schema e toda operação têm descrição e que as respostas de erro estão declaradas.
