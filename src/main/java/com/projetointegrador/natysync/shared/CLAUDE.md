# shared

## Responsabilidade

Código usado por mais de um pacote de funcionalidade: tratamento global de erro e
utilitários sem dono claro. É o menor pacote do projeto de propósito.

## Contratos

- `exception/ApiExceptionHandler`: `@RestControllerAdvice` que traduz exceção em resposta
  HTTP, ponto único de formatação de erro da API. Recurso não encontrado vira 404, falha
  de validação vira 400, credencial inválida vira 401 com código `CREDENCIAL_INVALIDA` e
  atividade bloqueada vira 409 com código `ATIVIDADE_BLOQUEADA` e conflito de negócio vira
  409 com o código da `ConflitoException`.
- `exception/ErroResposta`: corpo de erro devolvido ao app Flutter, com o campo `codigo`
  estável para o cliente ramificar.
- `exception/RecursoNaoEncontradoException`: 404 de domínio, jogada por qualquer serviço.
- `exception/CredencialInvalidaException` e `exception/AtividadeBloqueadaException`.
- `exception/ConflitoException`: 409 de negócio com `codigo` próprio, lido do construtor.
  Quem joga escolhe o código, e o handler só repassa.
- `pagina/PaginaResponse`: corpo único de listagem paginada, com `itens`, `pagina`,
  `tamanho`, `totalItens` e `totalPaginas`. Os parâmetros de entrada são `pagina` e
  `tamanho`, configurados em `spring.data.web.pageable`, com tamanho padrão 20 e máximo
  100.

## Decisões

`ErroResposta` é o único formato de erro da API. O app Flutter faz parse de um shape só,
em vez de um por endpoint.

`AtividadeBloqueadaException` é 409 e não 400: a requisição está bem formada, o que a
recusa é o estado da trilha para aquele integrante.

`PaginaResponse` é record próprio, e não a serialização de `Page` do Spring Data, para o
contrato da listagem não mudar com a versão da biblioteca.

Exceção de um pacote só mora no pacote dele, não aqui. `shared` fica com o que é
transversal de verdade.

## Armadilhas

`shared` é imã de código sem dono. Antes de colocar algo aqui, verifique se o código não
pertence ao pacote da funcionalidade que o usa. Classe usada por um pacote só não é
shared.

Cada exceção nova precisa do seu `@ExceptionHandler` explícito. Não existe handler
genérico de `Exception`, e isso é proposital: ele mascararia erro de programação como 500
formatado e esconderia bug em produção.

O 403 sai só por `config/NegacaoDeAcesso`, com código `ACESSO_NEGADO`.

O 401 sai por dois caminhos: `CredencialInvalidaException` quando um serviço recusa, e
`config/RecusaDeAcesso` quando a cadeia de segurança barra antes do controller. Os dois
devolvem `ErroResposta` com o mesmo código, senão o app teria dois formatos para o mesmo
caso.

## Ausências deliberadas

`util/DataUtil` é uma classe utilitária sem método e sem consumidor.
