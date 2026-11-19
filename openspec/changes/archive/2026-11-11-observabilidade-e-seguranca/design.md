# Design

## Métricas e Actuator (10.7, 10.13)

`management.endpoints.web.exposure.include` ganha `metrics`. As métricas de requisição
HTTP e de pool de conexões do Hikari já vêm do Micrometer, sem dependência nova. Formato
Prometheus fica de fora: exige dependência e um coletor que não existe.

`SecurityConfig` mantém `/actuator/health` e `/actuator/health/**` públicos e passa a exigir
`NATY` em `/actuator/**`. Prod usa `show-details: when-authorized` com `roles: NATY`, então
a saúde pública sai só com o status. A regra de papel segue o mesmo mecanismo das rotas do
painel.

Decisão: `NATY` apenas. `ADMIN` de empresa não vê estado de banco, porque o detalhe é
operacional do sistema inteiro.

## Identificador por requisição (10.12)

`RequisicaoIdFiltro`, um `OncePerRequestFilter` com ordem `HIGHEST_PRECEDENCE`, roda antes da
cadeia de segurança, para que 401 e 403 também recebam o cabeçalho. Ele valida o
`X-Request-Id` recebido contra `^[A-Za-z0-9-]{8,64}$`, gera UUID nos demais casos, grava no
MDC com a chave `requisicaoId`, escreve o cabeçalho na resposta e limpa o MDC em `finally`.
`logging.pattern.level` inclui `%X{requisicaoId}`. Validar o valor impede injeção de quebra
de linha no log.

## Log sem credencial (10.8)

Regra: nenhuma camada registra corpo de requisição nem `Authorization`. O código existente
é auditado nos pontos que tocam credencial: `painel`, `usuario` e `shared`. Records que
carregam senha têm `toString` que não imprime o campo, porque o `toString` automático de
record vaza a senha em qualquer log que imprima o objeto. A verificação é um teste de
integração que captura a saída do log em DEBUG em login com senha errada, login válido e
chamada com token, e procura a senha, o hash e o token. O `org.hibernate` fica em INFO no
`application.yml`, porque em DEBUG o Hibernate imprime o hash da entidade.

O nível `org.hibernate.SQL` em DEBUG no perfil `dev` imprime o SQL sem valores de
parâmetro, então não vaza hash. Parâmetros ligados (`TRACE`) ficam fora do `dev` e do `prod`.

## Retenção (10.14)

Decisão: só sessão é limpa. Tentativa de quiz fica sem prazo, porque o histórico de
tentativas é o insumo do acompanhamento e a política de retentativa guarda todas.

`LimpezaDeSessao` é um componente com método `@Scheduled(cron = "${app.sessao.limpeza.cron}")`
que chama `SessaoRepository.removerEncerradasAntesDe(limite)`, um `delete` em lote por
`expira_em < limite or revogado_em < limite`. O limite é `agora - app.sessao.retencao`. O método
de serviço recebe o instante atual como parâmetro, para o teste fixá-lo. `@EnableScheduling` entra em
`config`. Em várias instâncias o `delete` repetido é idempotente.

Propriedades: `app.sessao.retencao` (padrão `30d` fora de prod, obrigatória em prod por
`APP_SESSAO_RETENCAO`) e `app.sessao.limpeza.cron` (padrão diário às 03:00). `.env.example` e
`application-prod.yml` seguem o padrão do prazo de expiração.

## Leitura de integrante (10.15)

`GET /api/v1/usuarios` em `UsuarioController`, com `IntegranteDaRequisicao` resolvendo a
empresa. `UsuarioService.listarAtivosDaEmpresa(empresaId, pageable)` usa um método de
`UsuarioRepository` que filtra por `empresa.id` e `ativo = true`, ordenado por nome e id.
`UsuarioResponse` vira record com `id`, `nome` e `papel`, e `PaginaResponse` carrega a
página. `UsuarioFiltro` e a classe vazia de resposta saem. O pacote segue só lendo `usuario`.
O teste de dependência entre pacotes continua valendo.

## Riscos

- O filtro de identificador muda a ordem dos filtros. O teste de formato de erro cobre 401 e
  403 com o cabeçalho.
- `@Scheduled` roda em todo teste de contexto. O cron fica desligado no perfil de teste por
  uma expressão que nunca dispara, e o teste chama o serviço direto.
