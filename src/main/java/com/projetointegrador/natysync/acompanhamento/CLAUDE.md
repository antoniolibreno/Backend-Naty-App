# acompanhamento

## Responsabilidade

Leitura do progresso dos integrantes de qualquer empresa para a conta `NATY`: quem está
avançando, quem parou, as métricas de cada empresa, a exportação em CSV e o funil por atividade.
O pacote só lê. Não escreve progresso, pontuação, cadastro nem conteúdo, e nenhum pacote depende
dele.

## Contratos

- `AcompanhamentoController`, sob `/api/v1/acompanhamento/**`, todo restrito a `NATY`:
  - `GET /empresas/{empresaId}/integrantes`: página dos integrantes com situação, percentual,
    atividade atual, última conclusão e dias sem concluir. Filtro `situacao`.
  - `GET /empresas/{empresaId}/metricas`: adesão, percentual médio e contagem por situação.
  - `GET /empresas/{empresaId}/integrantes.csv`: a mesma lista, sem paginação.
  - `GET /atividades`: funil de quem chegou e quem passou em cada atividade, com `empresaId`
    opcional.
- `CalculadoraDeAcompanhamento`: única origem da situação de cada integrante. Listagem, métricas
  e CSV partem dela, então as contagens das métricas coincidem com a lista.
- `AcompanhamentoService`: listagem, lista completa, métricas e funil.
- `CsvDeAcompanhamento`: escrita do CSV.
- `IntegrantesAcompanhadosRepository` e `ConclusoesRepository`: estendem `Repository`, sem
  `save` nem `delete`, com consultas de leitura sobre `Usuario` e `ProgressoAtividade`.

## Decisões

Este pacote atravessa empresas de propósito, junto com o cadastro de empresas do `painel`. Por
isso toda rota exige `NATY`, e a empresa vem do caminho e nunca da identidade da requisição.

Situação, calculada sobre as trilhas ativas e sobre integrantes ativos de papel diferente de
`NATY`: `CONCLUIU` quando concluiu tudo, `PARADO` quando passaram pelo menos
`app.acompanhamento.dias-parado` dias (padrão 3) desde a última conclusão ou, sem conclusão, desde a
criação da conta, `SEM_INICIO` para conta nova sem estudo e `AVANCANDO` nos demais casos. Quem
concluiu tudo nunca está parado. Os dias são de calendário no fuso da empresa, e a comparação é
`dias >= prazo`. O prazo é propriedade configurável e não constante.

A conta `NATY` fica fora das contagens, porque vive na empresa interna e não faz o treinamento
como cliente. Integrante desativado também fica fora.

A listagem calcula a empresa inteira em memória, filtra, ordena e fatia. Evita uma consulta por
integrante e mantém a lista, as métricas e o CSV iguais. A ordem é `PARADO`, `SEM_INICIO`,
`AVANCANDO` e `CONCLUIU`, com mais dias sem concluir primeiro e o nome como desempate.

Funil: chegaram são os integrantes para quem a atividade está liberada, ou seja, todos na
primeira atividade de uma trilha e quem concluiu a anterior nas demais, e passaram são os que a
concluíram. O desbloqueio é linear, então quem concluiu uma atividade concluiu a anterior, e duas
consultas agregadas bastam. O funil é lista de tamanho do conteúdo, não da empresa, e por isso
não é paginado.

CSV: RFC 4180 com vírgula e `\r\n`, UTF-8 com BOM para a planilha abrir acentos. Célula de texto
que começa com `=`, `+`, `-` ou `@` recebe apóstrofo no início, e célula com vírgula, aspas ou
quebra de linha vai entre aspas. O nome do arquivo é `integrantes-<empresaId>.csv`, derivado só do
identificador.

## Armadilhas

Se uma empresa passar de alguns milhares de integrantes, o cálculo em memória sobe para SQL.
Mexer na classificação exige mexer só em `CalculadoraDeAcompanhamento`, nunca no serviço.

Conclusão de atividade de trilha inativa não entra no total, nas concluídas nem na última
conclusão, como no resumo de progresso.

Os índices parciais `progresso_atividade_concluida_idx` e `usuario_empresa_considerado_idx` servem
o funil e a seleção dos integrantes. O filtro `ativo and papel <> 'NATY'` das consultas
precisa continuar igual ao do índice, senão o planejador não o usa.

`RegrasDeDependenciaTest` reprova escrita em dado de outro pacote dentro deste e qualquer
dependência de fora dele.
