# Tarefas: progresso na trilha

## 1. Migration

- [x] 1.1 Escrever `src/main/resources/db/migration/V4__progresso_atividade.sql` criando
  `progresso_atividade` com `id` uuid padrão `gen_random_uuid()`, `usuario_id` not null
  referenciando `usuario(id)` com `on delete cascade`, `atividade_id` not null
  referenciando `atividade(id)`, `video_assistido_em` e `concluido_em` timestamptz
  anuláveis, `criado_em` e `atualizado_em` timestamptz not null padrão `now()`. Não
  editar `V1`, `V2` nem `V3`.
- [x] 1.2 Criar o índice único `progresso_atividade_usuario_atividade_idx` sobre
  `(usuario_id, atividade_id)` e o índice `progresso_atividade_usuario_idx` sobre
  `usuario_id`. Verificar com `\d progresso_atividade` em `psql` após subir o compose.

## 2. Senha na sessão

- [x] 2.1 Adicionar `@NotBlank String senha` a `usuario/dto/SessaoRequest.java`,
  preservando o construtor compacto que normaliza o e-mail. A senha não é normalizada.
- [x] 2.2 Atualizar a descrição da operação em `usuario/SessaoController.java` para
  declarar que a senha é aceita e não verificada. O serviço continua recebendo apenas o
  e-mail: a senha não é passada adiante, não é guardada e não é registrada em log.
- [x] 2.3 Conferir que `SessaoResponse` continua com `usuarioId`, `empresaId`, `nome` e
  `email`, sem nenhum campo novo.

## 3. Integrante da requisição

- [x] 3.1 Criar `usuario/IntegranteDaRequisicao.java` como record com o identificador do
  integrante e o da empresa.
- [x] 3.2 Criar `usuario/IntegranteArgumentResolver.java` implementando
  `HandlerMethodArgumentResolver`, lendo o cabeçalho `X-Integrante-Id`, resolvendo o
  integrante por `UsuarioRepository` e devolvendo `IntegranteDaRequisicao`.
- [x] 3.3 Lançar `IntegranteNaoInformadoException` quando o cabeçalho estiver ausente,
  vazio ou não for um identificador válido, e `RecursoNaoEncontradoException` quando o
  identificador não corresponder a nenhum integrante.
- [x] 3.4 Criar `shared/exception/IntegranteNaoInformadoException.java` e o
  `@ExceptionHandler` correspondente em `shared/exception/ApiExceptionHandler.java`,
  devolvendo 400 com código `INTEGRANTE_NAO_INFORMADO`.
- [x] 3.5 Criar `config/WebMvcResolverConfig.java` registrando o resolvedor por
  `WebMvcConfigurer.addArgumentResolvers`.
- [x] 3.6 Documentar o cabeçalho no OpenAPI, para o time do app ver o contrato em
  `/swagger-ui.html`.

## 4. Pacote progresso

- [x] 4.1 Criar o pacote `com.projetointegrador.natysync.progresso`.
- [x] 4.2 Criar a entidade `ProgressoAtividade` mapeada em `progresso_atividade`, com
  identificador gerado, timestamps automáticos e associações lazy para `Usuario` e
  `Atividade`.
- [x] 4.3 Criar o enum `EstadoAtividade` com `BLOQUEADO`, `DISPONIVEL` e `CONCLUIDO`.
- [x] 4.4 Criar `ProgressoAtividadeRepository` com busca por integrante e busca por
  integrante mais atividade.
- [x] 4.5 Criar os DTOs em `progresso/dto`: `TrilhaProgressoResponse`,
  `ModuloProgressoResponse`, `AtividadeProgressoResponse` e `VideoAssistidoResponse`.
- [x] 4.6 Criar `TrilhaProgressoMontador` achatando módulos e atividades na ordem do
  módulo e da atividade, calculando o estado de cada nó, o total, o número de concluídas,
  o percentual e a próxima atividade.
- [x] 4.7 Implementar a regra de desbloqueio linear: nó concluído fica concluído, o
  primeiro não concluído fica disponível, os seguintes ficam bloqueados.
- [x] 4.8 Criar `ProgressoService` lendo conteúdo por `TrilhaRepository` e
  `AtividadeRepository`, sem que `trilha` passe a conhecer `progresso`.
- [x] 4.9 Implementar o registro de vídeo assistido gravando o momento do vídeo e o da
  conclusão, de forma idempotente: a segunda chamada não cria linha nova nem move a
  conclusão.
- [x] 4.10 Recusar registro em atividade bloqueada com `AtividadeBloqueadaException`,
  sem gravar nada.
- [x] 4.11 Criar `shared/exception/AtividadeBloqueadaException.java` e o
  `@ExceptionHandler` correspondente, devolvendo 409 com código `ATIVIDADE_BLOQUEADA`.
- [x] 4.12 Criar `ProgressoController` expondo
  `GET /api/v1/trilhas/{trilhaId}/progresso` e
  `POST /api/v1/atividades/{atividadeId}/video-assistido`, ambos recebendo
  `IntegranteDaRequisicao` injetado. O controller mora em `progresso`, não em `trilha`.

## 5. Testes

- [x] 5.1 Ajustar os casos existentes de `usuario/SessaoApiTest.java` para enviar senha
  no corpo.
- [x] 5.2 Somar em `SessaoApiTest` o caso de senha ausente ou vazia devolvendo erro de
  validação, e o caso de senhas diferentes resolvendo o mesmo integrante.
- [x] 5.3 Criar `progresso/ProgressoApiTest.java` estendendo `IntegracaoTest`.
- [x] 5.4 Testar a Home inicial: primeira atividade disponível, as outras cinco
  bloqueadas, percentual zero.
- [x] 5.5 Testar a trilha inteira, das seis atividades do conteúdo semeado, provando que
  cada conclusão abre a seguinte e que a seguinte estava bloqueada antes.
- [x] 5.6 Testar que registrar vídeo em atividade bloqueada devolve 409 com código
  `ATIVIDADE_BLOQUEADA` e não grava progresso.
- [x] 5.7 Testar a idempotência: duas chamadas seguidas não duplicam linha nem alteram o
  momento da conclusão.
- [x] 5.8 Testar o cabeçalho ausente devolvendo 400 com código
  `INTEGRANTE_NAO_INFORMADO`, e o cabeçalho com identificador inexistente devolvendo 404.
- [x] 5.9 Testar o isolamento: o progresso de um integrante não aparece na leitura de
  outro integrante da mesma empresa.

## 6. Documentação de contexto

- [x] 6.1 Escrever `progresso/CLAUDE.md` com as cinco seções usadas nos outros pacotes,
  registrando a regra temporária de conclusão por vídeo e que ela muda quando o quiz
  entrar, que o controller mora aqui apesar do prefixo de trilhas, que o pacote lê
  `trilha` e `usuario` e nunca o contrário, e o motivo da cascata na chave estrangeira.
- [x] 6.2 Atualizar a seção Estado atual de `usuario/CLAUDE.md` com o resolvedor de
  integrante e a senha aceita sem verificação.
- [x] 6.3 Atualizar a seção Estado atual de `config/CLAUDE.md` com
  `WebMvcResolverConfig`.
- [x] 6.4 Atualizar a seção Estado atual de `shared/CLAUDE.md` com as duas exceções e os
  dois tratamentos novos.
- [x] 6.5 Atualizar o `CLAUDE.md` raiz movendo `progresso` de previstos para existentes,
  e somando às dívidas conhecidas a senha aceita sem verificação e o cabeçalho de
  integrante forjável.
- [x] 6.6 Marcar em `docs/tasks.md` o que esta etapa entregou e anotar em 3.5 que a senha
  já chega no corpo sem verificação.

## 7. Verificação

- [x] 7.1 Rodar `./mvnw -B spotless:apply` e depois `./mvnw -B verify` com o build verde.
- [x] 7.2 Subir `docker compose up --build -d` e conferir `/actuator/health`.
- [x] 7.3 Percorrer o fluxo das três telas por `curl`: login com e-mail e senha, leitura
  da Home, recusa ao pular a trilha, registro de vídeo assistido, repetição idempotente e
  leitura da Home de novo com a próxima atividade liberada.
- [x] 7.4 Conferir em `/swagger-ui.html` que os dois endpoints novos aparecem com o
  cabeçalho de integrante documentado.
- [x] 7.5 Conferir que nenhum arquivo novo tem comentário explicativo, `TODO`, `FIXME`,
  marcador de data ou de autoria.
