# Proposal

## Why

A Naty vende o treinamento e precisa saber, por cliente, quem está avançando e quem parou, para
cobrar quem travou e comparar empresas. O progresso só é legível pelo próprio integrante e
por colegas da mesma empresa, e nenhuma rota atravessa empresas.

## What Changes

- Pacote `acompanhamento`, somente leitura, que lê progresso e integrantes de qualquer empresa e
  nunca escreve neles, com rotas exclusivas da conta `NATY` sob `/api/v1/acompanhamento/**`.
- Listagem paginada dos integrantes ativos de uma empresa com a situação de cada um (concluiu,
  avançando, parado, sem início), onde está na trilha e há quantos dias não conclui atividade.
- Métricas agregadas da empresa: adesão, percentual médio de conclusão e contagem por situação.
- Exportação da mesma lista em CSV.
- Funil por atividade, opcionalmente de uma empresa: quantos chegaram e quantos passaram.
- Definição de parado como propriedade configurável, padrão de 3 dias.
- Índices que as consultas novas usam.

## Capabilities

### New Capabilities

- `acompanhamento-de-empresa`: listagem, métricas e exportação dos integrantes de uma empresa,
  com a definição de parado e a restrição à conta `NATY`.
- `acompanhamento-de-atividades`: funil de chegada e passagem por atividade.

### Modified Capabilities

## Impact

- Pacote novo `acompanhamento` com `CLAUDE.md`, nova migration de índices,
  `config/SecurityConfig.java`, `application.yml`, `RegrasDeDependenciaTest`.
- `CLAUDE.md` raiz, `docs/regras.md` e `docs/backlog.md`: itens 7.1 a 7.9.
- Sem dependência nova no `pom.xml`.
