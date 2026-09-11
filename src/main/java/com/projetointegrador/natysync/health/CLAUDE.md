# health

## Responsabilidade

Indicadores de saúde customizados expostos em `/actuator/health`. Responde se as
dependências externas do serviço estão alcançáveis.

## Contratos

- `NatyApiHealthIndicator`: vai implementar `HealthIndicator` e reportar se a Naty API
  responde.

## Decisões

O health da Naty API fica separado do health do banco, que o Actuator já entrega
pronto. Naty API fora do ar não deve ser confundida com banco fora do ar.

O indicador precisa de timeout curto. Health check que demora derruba probe de
orquestrador e transforma indisponibilidade parcial em reinicio de container.

## Armadilhas

`/actuator/health` é público neste projeto. Não coloque no detalhe do indicador
mensagem de erro contendo token, URL interna ou trecho de resposta da Naty API.

Enquanto `NATY_API_TOKEN` puder ficar vazio, o indicador precisa distinguir "sem
credencial configurada" de "Naty API fora do ar". Reportar `DOWN` em ambiente de
desenvolvimento sem token deixa o health inútil.

## Estado atual

Stub: `NatyApiHealthIndicator`. É um `@Component` vazio, não implementa
`HealthIndicator` e não aparece em `/actuator/health` hoje. O componente `db` que
aparece lá vem do Actuator, não deste pacote.

A etapa da integração com a Naty API preenche o indicador.
