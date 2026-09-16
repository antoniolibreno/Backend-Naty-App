# shared

## Responsabilidade

Código usado por mais de um pacote de funcionalidade: tratamento global de erro e
utilitários sem dono claro. É o menor pacote do projeto de propósito.

## Contratos

- `exception/ApiExceptionHandler`: `@RestControllerAdvice` que traduz exceção em resposta
  HTTP, ponto único de formatação de erro da API. Recurso não encontrado vira 404, falha
  de validação vira 400, integrante não informado vira 400 com código
  `INTEGRANTE_NAO_INFORMADO` e atividade bloqueada vira 409 com código
  `ATIVIDADE_BLOQUEADA`.
- `exception/ErroResposta`: corpo de erro devolvido ao app Flutter, com o campo `codigo`
  estável para o cliente ramificar.
- `exception/RecursoNaoEncontradoException`: 404 de domínio, jogada por qualquer serviço.
- `exception/IntegranteNaoInformadoException` e `exception/AtividadeBloqueadaException`.

## Decisões

`ErroResposta` é o único formato de erro da API. O app Flutter faz parse de um shape só,
em vez de um por endpoint.

`AtividadeBloqueadaException` é 409 e não 400: a requisição está bem formada, o que a
recusa é o estado da trilha para aquele integrante.

Exceção de um pacote só mora no pacote dele, não aqui. `shared` fica com o que é
transversal de verdade.

## Armadilhas

`shared` é imã de código sem dono. Antes de colocar algo aqui, verifique se o código não
pertence ao pacote da funcionalidade que o usa. Classe usada por um pacote só não é
shared.

Cada exceção nova precisa do seu `@ExceptionHandler` explícito. Não existe handler
genérico de `Exception`, e isso é proposital: ele mascararia erro de programação como 500
formatado e esconderia bug em produção.

## Ausências deliberadas

`util/DataUtil` é uma classe utilitária sem método e sem consumidor.
