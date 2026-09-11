# sincronizacao

## Responsabilidade

Puxa usuários da Naty API e escreve no banco local. Único pacote com permissão de
escrita na tabela `usuario`. Orquestra `natyapi` e `usuario`, sem conhecer HTTP nem
SQL diretamente.

## Contratos

- `SincronizacaoUsuarioService`: lê pelo `NatyApiClient`, converte pelo
  `UsuarioMapper` e grava pelo `UsuarioRepository`. Idempotente por `naty_id`.
- `SincronizacaoScheduler`: dispara o serviço no cron de `sincronizacao.cron`.
- `SincronizacaoController`: `/api/v1/sincronizacoes/usuarios`, disparo manual para
  operação e teste.

## Decisões

O disparo manual existe além do agendado porque esperar o cron para validar uma
mudança custa tempo demais em desenvolvimento e em suporte.

A sincronização é idempotente por `naty_id`, não por comparação de conteúdo. Rodar
duas vezes seguidas produz o mesmo estado final.

`sincronizacao.habilitada` nasce em `false`. Scheduler ligado por padrão bate na Naty
API a partir do primeiro `docker compose up` de qualquer pessoa do time, inclusive em
máquina de desenvolvimento.

## Armadilhas

O scheduler e o endpoint manual chamam o mesmo serviço. Colocar regra só no controller
faz o cron rodar com comportamento diferente do disparo manual.

`NatyRateLimitException` carrega a espera sugerida pela Naty. Ignorar esse valor e
tentar de novo em seguida derruba a integração inteira.

Erro na sincronização de um usuário não pode abortar o lote. A decisão de continuar ou
parar precisa estar explícita no serviço quando ele for escrito.

## Estado atual

Stub: as três classes. `SincronizacaoScheduler` não tem `@Scheduled` ainda, e
`SchedulerConfig` não habilita agendamento, então nada dispara sozinho hoje.

A etapa da sincronização preenche as três e liga o `@EnableScheduling`.
