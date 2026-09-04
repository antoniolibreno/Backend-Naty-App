# sincronizacao

## Responsabilidade

Puxa usuarios da Naty API e escreve no banco local. Unico pacote com permissao de
escrita na tabela `usuario`. Orquestra `natyapi` e `usuario`, sem conhecer HTTP nem
SQL diretamente.

## Contratos

- `SincronizacaoUsuarioService`: le pelo `NatyApiClient`, converte pelo
  `UsuarioMapper` e grava pelo `UsuarioRepository`. Idempotente por `naty_id`.
- `SincronizacaoScheduler`: dispara o servico no cron de `sincronizacao.cron`.
- `SincronizacaoController`: `/api/v1/sincronizacoes/usuarios`, disparo manual para
  operacao e teste.

## Decisoes

O disparo manual existe alem do agendado porque esperar o cron para validar uma
mudanca custa tempo demais em desenvolvimento e em suporte.

A sincronizacao e idempotente por `naty_id`, nao por comparacao de conteudo. Rodar
duas vezes seguidas produz o mesmo estado final.

`sincronizacao.habilitada` nasce em `false`. Scheduler ligado por padrao bate na Naty
API a partir do primeiro `docker compose up` de qualquer pessoa do time, inclusive em
maquina de desenvolvimento.

## Armadilhas

O scheduler e o endpoint manual chamam o mesmo servico. Colocar regra so no controller
faz o cron rodar com comportamento diferente do disparo manual.

`NatyRateLimitException` carrega a espera sugerida pela Naty. Ignorar esse valor e
tentar de novo em seguida derruba a integracao inteira.

Erro na sincronizacao de um usuario nao pode abortar o lote. A decisao de continuar ou
parar precisa estar explicita no servico quando ele for escrito.

## Estado atual

Stub: as tres classes. `SincronizacaoScheduler` nao tem `@Scheduled` ainda, e
`SchedulerConfig` nao habilita agendamento, entao nada dispara sozinho hoje.

A etapa da sincronizacao preenche as tres e liga o `@EnableScheduling`.
