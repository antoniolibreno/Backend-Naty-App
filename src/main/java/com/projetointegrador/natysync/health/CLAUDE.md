# health

## Responsabilidade

Indicadores de saude customizados expostos em `/actuator/health`. Responde se as
dependencias externas do servico estao alcancaveis.

## Contratos

- `NatyApiHealthIndicator`: vai implementar `HealthIndicator` e reportar se a Naty API
  responde.

## Decisoes

O health da Naty API fica separado do health do banco, que o Actuator ja entrega
pronto. Naty API fora do ar nao deve ser confundida com banco fora do ar.

O indicador precisa de timeout curto. Health check que demora derruba probe de
orquestrador e transforma indisponibilidade parcial em reinicio de container.

## Armadilhas

`/actuator/health` e publico neste projeto. Nao coloque no detalhe do indicador
mensagem de erro contendo token, URL interna ou trecho de resposta da Naty API.

Enquanto `NATY_API_TOKEN` puder ficar vazio, o indicador precisa distinguir "sem
credencial configurada" de "Naty API fora do ar". Reportar `DOWN` em ambiente de
desenvolvimento sem token deixa o health inutil.

## Estado atual

Stub: `NatyApiHealthIndicator`. E um `@Component` vazio, nao implementa
`HealthIndicator` e nao aparece em `/actuator/health` hoje. O componente `db` que
aparece la vem do Actuator, nao deste pacote.

A etapa da integracao com a Naty API preenche o indicador.
