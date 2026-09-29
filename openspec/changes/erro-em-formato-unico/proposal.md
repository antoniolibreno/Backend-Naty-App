# Erro em formato único

## Why

Todo erro que o Spring MVC resolve por conta própria volta como 401 `CREDENCIAL_INVALIDA`,
com a mensagem "Sessao invalida": rota inexistente, identificador malformado na URL, JSON
malformado, método não suportado e tipo de conteúdo não suportado. Vale até para o login,
que é rota pública, e vale com token válido.

O `DefaultHandlerExceptionResolver` responde com `sendError`, o container despacha para
`/error`, e a cadeia de segurança recusa esse despacho porque ele não carrega
autenticação. O app recebe "sessão inválida" para um corpo malformado e manda o
integrante logar de novo, e o erro real some.

## What Changes

`ApiExceptionHandler` herda `ResponseEntityExceptionHandler` e escreve `ErroResposta` para
as exceções do Spring MVC, com código estável por status e mensagem fixa:

- 400 `REQUISICAO_MALFORMADA`: corpo ilegível, identificador malformado, parâmetro
  ausente ou de tipo errado;
- 404 `RECURSO_NAO_ENCONTRADO`: rota inexistente;
- 405 `METODO_NAO_SUPORTADO`;
- 406 `FORMATO_NAO_ACEITO`;
- 415 `TIPO_DE_CONTEUDO_NAO_SUPORTADO`.

`FALHA_DE_VALIDACAO` continua para a validação de corpo, com os campos.

`SecurityConfig` libera o despacho de erro do container. O status que chega por
`sendError` é o status verdadeiro, e não 401.

O 5xx que o próprio Spring MVC resolve sai em `ErroResposta` com `ERRO_INTERNO` e é logado
em error. Exceção não tratada sai como 500 no corpo padrão do Spring Boot.

## Capabilities

### New Capabilities

- `formato-de-erro`: todo erro de requisição em `ErroResposta`, com código estável e status
  verdadeiro.

## Impact

`shared/exception/ApiExceptionHandler.java` e `config/SecurityConfig.java`. Nenhuma rota
muda de caminho, e os códigos existentes continuam os mesmos. Chamada sem token para
rota inexistente continua 401, porque a cadeia recusa antes de o MVC procurar a rota.
