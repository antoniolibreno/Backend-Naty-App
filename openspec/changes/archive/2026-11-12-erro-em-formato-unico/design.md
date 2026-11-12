# Design: erro em formato único

## Context

`ErroResposta` é o formato único de erro da API. `ApiExceptionHandler` cobre as exceções
de domínio e a validação de corpo, e `config/RecusaDeAcesso` e `config/NegacaoDeAcesso`
cobrem o 401 e o 403 da cadeia de segurança. As exceções do próprio Spring MVC não têm
tratamento e caem no `DefaultHandlerExceptionResolver`, que responde por `sendError`.

## Decisions

### Herdar `ResponseEntityExceptionHandler`

A classe base conhece todas as exceções do Spring MVC e chama um ponto único,
`handleExceptionInternal`, que escreve `ErroResposta`. Listar exceção por exceção esquece
a próxima que o framework criar. A base não trata `Exception` genérica, então a regra de
não mascarar erro de programação continua de pé.

`handleMethodArgumentNotValid` é sobrescrito, e não anotado com `@ExceptionHandler`,
porque a base já mapeia essa exceção, e dois mapeamentos derrubam a subida.

A mensagem é fixa por status. A mensagem da exceção de leitura de corpo pode carregar
trecho da entrada, e a entrada do login carrega a senha.

### Liberar o despacho de erro

Com o tratamento acima, o MVC não chama mais `sendError` para as exceções dele. O que
sobra é erro de container e exceção não tratada. Liberar `DispatcherType.ERROR` faz esses
casos saírem com o status verdadeiro. O despacho de erro só acontece depois de a
requisição original passar ou ser recusada pela cadeia, então não abre rota nova: `GET
/error` chamado pelo cliente é despacho comum e continua exigindo token.

### 500 fica no corpo padrão

Formatar o 500 de exceção não tratada como `ErroResposta` pede um `ErrorController`
próprio, e a decisão de não ter tratamento genérico existe para bug não parecer resposta de
negócio. Esse 500 sai com o status certo e o corpo do Spring Boot, sem mensagem nem pilha.
O 5xx que o Spring MVC resolve por conta própria passa por `handleExceptionInternal`, sai
em `ErroResposta` com `ERRO_INTERNO` e é logado em error, porque a base não loga.

`handleExceptionInternal` devolve `null` quando a resposta já foi enviada, como a base.
