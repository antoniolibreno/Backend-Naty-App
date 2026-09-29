# Tarefas: erro em formato único

## 1. Testes

- [x] 1.1 Criar `shared/FormatoDeErroApiTest` com rota inexistente com e sem token,
  identificador malformado, JSON malformado sem eco do corpo, 405, 415 e `GET /error`
  sem token.

## 2. Tratamento

- [x] 2.1 Fazer `ApiExceptionHandler` herdar `ResponseEntityExceptionHandler`, com
  `handleExceptionInternal` escrevendo `ErroResposta` e código por status.
- [x] 2.2 Sobrescrever `handleMethodArgumentNotValid` mantendo `FALHA_DE_VALIDACAO` e os
  campos.
- [x] 2.3 Liberar `DispatcherType.ERROR` em `SecurityConfig`.

## 3. Endurecimento

- [x] 3.1 Devolver `null` com resposta já enviada, logar os 5xx do framework com
  `ERRO_INTERNO` e tratar `HandlerMethodValidationException` como `FALHA_DE_VALIDACAO`.

## 4. Documentação

- [x] 4.1 Atualizar `shared/CLAUDE.md` e `config/CLAUDE.md`.
- [x] 4.2 Registrar os dois itens em `docs/backlog.md`.

## 5. Fechamento

- [x] 5.1 Rodar `./mvnw -B verify` com Docker ativo.
- [x] 5.2 Rodar `npx --yes @fission-ai/openspec@latest validate erro-em-formato-unico`.
- [x] 5.3 Sincronizar a spec.
