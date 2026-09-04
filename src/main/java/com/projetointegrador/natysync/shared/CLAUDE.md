# shared

## Responsabilidade

Codigo usado por mais de um pacote de funcionalidade: tratamento global de erro e
utilitarios sem dono claro. E o menor pacote do projeto de proposito.

## Contratos

- `exception/ApiExceptionHandler`: `@RestControllerAdvice` que traduz excecao em
  resposta HTTP, ponto unico de formatacao de erro da API.
- `exception/ErroResposta`: corpo de erro devolvido ao app Flutter.
- `exception/RecursoNaoEncontradoException`: 404 de dominio, jogada por qualquer
  servico.
- `util/DataUtil`: conversao de data e hora vinda da Naty API.

## Decisoes

`ErroResposta` e o unico formato de erro da API. O app Flutter faz parse de um shape
so, em vez de um por endpoint.

Excecao de integracao com a Naty mora em `natyapi/exception`, nao aqui. `shared` fica
com o que e transversal de verdade.

## Armadilhas

`shared` e ima de codigo sem dono. Antes de colocar algo aqui, verifique se o codigo
nao pertence ao pacote da funcionalidade que o usa. Classe usada por um pacote so nao
e shared.

`ApiExceptionHandler` captura por tipo de excecao. Um handler generico de `Exception`
adicionado sem cuidado engole erro de programacao e devolve 500 mascarado.

## Estado atual

Real: `ApiExceptionHandler`, `ErroResposta` e `RecursoNaoEncontradoException`. O
handler traduz recurso nao encontrado em 404 e falha de validacao em 400, sempre no
formato de `ErroResposta`, com o campo `codigo` estavel para o app Flutter ramificar.

Stub: `DataUtil`.

Cada excecao nova precisa do seu `@ExceptionHandler` explicito. Nao existe handler
generico de `Exception` de proposito: ele mascararia erro de programacao como 500
formatado e esconderia bug em producao.
