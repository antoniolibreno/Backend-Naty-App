# shared

## Responsabilidade

Código usado por mais de um pacote de funcionalidade: tratamento global de erro e
utilitários sem dono claro. É o menor pacote do projeto de propósito.

## Contratos

- `exception/ApiExceptionHandler`: `@RestControllerAdvice` que traduz exceção em
  resposta HTTP, ponto único de formatação de erro da API.
- `exception/ErroResposta`: corpo de erro devolvido ao app Flutter.
- `exception/RecursoNaoEncontradoException`: 404 de domínio, jogada por qualquer
  serviço.
- `util/DataUtil`: conversão de data e hora. Classe vazia, sem consumidor.

## Decisões

`ErroResposta` é o único formato de erro da API. O app Flutter faz parse de um shape
só, em vez de um por endpoint.

Exceção de um pacote só mora no pacote dele, não aqui. `shared` fica com o que é
transversal de verdade.

## Armadilhas

`shared` é imã de código sem dono. Antes de colocar algo aqui, verifique se o código
não pertence ao pacote da funcionalidade que o usa. Classe usada por um pacote só não
é shared.

`ApiExceptionHandler` captura por tipo de exceção. Um handler genérico de `Exception`
adicionado sem cuidado engole erro de programação e devolve 500 mascarado.

## Estado atual

Real: `ApiExceptionHandler`, `ErroResposta`, `RecursoNaoEncontradoException`,
`IntegranteNaoInformadoException` e `AtividadeBloqueadaException`. O handler traduz
recurso não encontrado em 404, falha de validação em 400, integrante não informado em
400 com código `INTEGRANTE_NAO_INFORMADO` e atividade bloqueada em 409 com código
`ATIVIDADE_BLOQUEADA`, sempre no formato de `ErroResposta`, com o campo `codigo` estável
para o app Flutter ramificar.

`AtividadeBloqueadaException` é 409 e não 400 de propósito: a requisição está bem
formada, o que a recusa é o estado da trilha para aquele integrante.

Stub: `DataUtil`.

Cada exceção nova precisa do seu `@ExceptionHandler` explícito. Não existe handler
genérico de `Exception` de propósito: ele mascararia erro de programação como 500
formatado e esconderia bug em produção.
