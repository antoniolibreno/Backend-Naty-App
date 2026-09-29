# shared

## Responsabilidade

Código usado por mais de um pacote de funcionalidade: tratamento global de erro e
utilitários sem dono claro. É o menor pacote do projeto de propósito.

## Contratos

- `exception/ApiExceptionHandler`: `@RestControllerAdvice` que herda
  `ResponseEntityExceptionHandler` e é o ponto único de formatação de erro da API.
  - Domínio: recurso não encontrado vira 404 `RECURSO_NAO_ENCONTRADO`, credencial inválida
    vira 401 `CREDENCIAL_INVALIDA`, acesso negado vira 403 `ACESSO_NEGADO`, atividade
    bloqueada vira 409 `ATIVIDADE_BLOQUEADA` e conflito de negócio vira 409 com o código da
    `ConflitoException`.
  - Validação: corpo inválido e parâmetro inválido viram 400 `FALHA_DE_VALIDACAO`, com os
    campos.
  - Spring MVC: código por status, `REQUISICAO_MALFORMADA` (400), `RECURSO_NAO_ENCONTRADO`
    (404), `METODO_NAO_SUPORTADO` (405), `FORMATO_NAO_ACEITO` (406),
    `TIPO_DE_CONTEUDO_NAO_SUPORTADO` (415) e `ERRO_INTERNO` (500, com log de erro).
- `exception/ErroResposta`: corpo de erro devolvido ao app Flutter, com o campo `codigo`
  estável para o cliente ramificar.
- `exception/RecursoNaoEncontradoException`: 404 de domínio, jogada por qualquer serviço.
- `exception/CredencialInvalidaException`, `exception/AcessoNegadoException` e
  `exception/AtividadeBloqueadaException`.
- `exception/ConflitoException`: 409 de negócio com `codigo` próprio, lido do construtor.
  Quem joga escolhe o código, e o handler só repassa.
- `pagina/PaginaResponse`: corpo único de listagem paginada, com `itens`, `pagina`,
  `tamanho`, `totalItens` e `totalPaginas`. Os parâmetros de entrada são `pagina` e
  `tamanho`, configurados em `spring.data.web.pageable`, com tamanho padrão 20 e máximo
  100.

## Decisões

`ErroResposta` é o único formato de erro da API. O app Flutter faz parse de um shape só,
em vez de um por endpoint.

A base `ResponseEntityExceptionHandler` cobre todas as exceções do Spring MVC por um ponto
só, `handleExceptionInternal`. Sem ela o `DefaultHandlerExceptionResolver` responde por
`sendError`, e o despacho de erro do container vira outro formato. A base não trata
`Exception` genérica, então a regra abaixo vale.

A mensagem das exceções do Spring MVC é fixa por status. A mensagem de leitura de corpo
pode carregar trecho da entrada, e a entrada do login carrega a senha.

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

Exceção que `ResponseEntityExceptionHandler` já mapeia, como
`MethodArgumentNotValidException`, é tratada sobrescrevendo o método `handle...` da base.
Um `@ExceptionHandler` para ela gera mapeamento ambíguo e derruba a subida.

Exceção não tratada sai como 500 no corpo padrão do Spring Boot, sem mensagem nem pilha, e
com a pilha no log do container. É o que separa bug de resposta de negócio. O 5xx que o
próprio Spring MVC resolve sai em `ErroResposta` com `ERRO_INTERNO` e é logado em error.

`ResponseStatusException` não é usada. Ela sai com código genérico por status, fora do
vocabulário da API, e descarta o motivo. Erro de negócio novo ganha exceção e handler
próprios, ou usa `ConflitoException` com código.

`handleExceptionInternal` devolve `null` quando a resposta já foi enviada, como a base faz.
Escrever corpo nesse caso falha de novo e esconde o erro original.

Cada exceção nova de domínio precisa do seu `@ExceptionHandler` explícito. Não existe handler
genérico de `Exception`, e isso é proposital: ele mascararia erro de programação como 500
formatado e esconderia bug em produção.

O 403 sai por dois caminhos: `AcessoNegadoException` quando um serviço recusa pela regra
de papel, e `config/NegacaoDeAcesso` quando a cadeia de segurança barra. Os dois usam o
código `ACESSO_NEGADO`.

O 401 sai por dois caminhos: `CredencialInvalidaException` quando um serviço recusa, e
`config/RecusaDeAcesso` quando a cadeia de segurança barra antes do controller. Os dois
devolvem `ErroResposta` com o mesmo código, senão o app teria dois formatos para o mesmo
caso.

## Ausências deliberadas

`util/DataUtil` é uma classe utilitária sem método e sem consumidor.
