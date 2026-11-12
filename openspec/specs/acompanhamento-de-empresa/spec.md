# acompanhamento-de-empresa Specification

## Purpose
Permite à conta NATY acompanhar quem avança e quem parou em cada empresa cliente: situação de
cada integrante, métricas agregadas e exportação em CSV, sem nunca escrever em progresso.

## Requirements

### Requirement: Acompanhamento restrito à conta NATY

Toda rota sob `/api/v1/acompanhamento/` SHALL exigir token de sessão de conta `NATY`. Conta
`ADMIN` e conta `INTEGRANTE` SHALL receber 403 `ACESSO_NEGADO`, e chamada sem token SHALL receber
401 `CREDENCIAL_INVALIDA`. O acompanhamento SHALL ser o único ponto de leitura de dado de
integrantes de mais de uma empresa, e NÃO SHALL escrever progresso, pontuação nem cadastro.

#### Scenario: Integrante comum

- **WHEN** uma conta `INTEGRANTE` ou `ADMIN` chama qualquer rota do acompanhamento
- **THEN** o sistema responde 403 `ACESSO_NEGADO`

#### Scenario: Sem token

- **WHEN** o cliente chama uma rota do acompanhamento sem token
- **THEN** o sistema responde 401 `CREDENCIAL_INVALIDA`

#### Scenario: Empresa inexistente

- **WHEN** a conta `NATY` consulta o acompanhamento de uma empresa que não existe
- **THEN** o sistema responde 404 `RECURSO_NAO_ENCONTRADO`

### Requirement: Situação do integrante

A situação de um integrante ativo, de papel diferente de `NATY`, SHALL ser calculada sobre as
trilhas ativas: `CONCLUIU` quando concluiu todas as atividades, `SEM_INICIO` quando não concluiu
nenhuma e a conta tem menos que o prazo de parado, `PARADO` quando passou o prazo de parado desde
a última conclusão ou, sem conclusão, desde a criação da conta, e `AVANCANDO` nos demais casos. Os
dias SHALL ser contados em dias de calendário no fuso horário da empresa, e o integrante SHALL
estar parado quando o número de dias for igual ou maior que o prazo. O prazo SHALL ser lido da
propriedade `app.acompanhamento.dias-parado`, com padrão 3, nunca de constante em código.
Integrante desativado NÃO SHALL aparecer no acompanhamento.

#### Scenario: Concluiu há pouco

- **WHEN** o integrante concluiu uma atividade há menos dias que o prazo e tem atividades
- **THEN** a situação é `AVANCANDO`

#### Scenario: Concluiu há muito

- **WHEN** a última conclusão do integrante foi há 3 dias ou mais com o prazo em 3 e ele tem
  atividades pendentes
- **THEN** a situação é `PARADO`

#### Scenario: Conta nova sem estudo

- **WHEN** o integrante não concluiu nada e a conta foi criada há menos dias que o prazo
- **THEN** a situação é `SEM_INICIO`

#### Scenario: Conta antiga sem estudo

- **WHEN** o integrante não concluiu nada e a conta foi criada há 3 dias ou mais com o prazo em 3
- **THEN** a situação é `PARADO`

#### Scenario: Tudo concluído

- **WHEN** o integrante concluiu todas as atividades das trilhas ativas
- **THEN** a situação é `CONCLUIU`, mesmo que a última conclusão seja antiga

#### Scenario: Prazo configurado

- **WHEN** a propriedade de prazo vale 7
- **THEN** o integrante que concluiu há 5 dias está `AVANCANDO`

### Requirement: Listagem de integrantes da empresa

`GET /api/v1/acompanhamento/empresas/{empresaId}/integrantes` SHALL listar em página os
integrantes ativos de papel diferente de `NATY` da empresa, com identificador, nome, e-mail,
papel, situação, atividades concluídas, total de atividades, percentual concluído, atividade atual,
instante da última conclusão e dias sem concluir. O percentual SHALL ser a divisão inteira de
concluídas por total vezes cem. A atividade atual SHALL ser a primeira atividade não concluída na
ordem das trilhas ativas, e nula quando tudo está concluído. Os dias sem concluir SHALL contar
desde a última conclusão ou, sem conclusão, desde a criação da conta. A ordem SHALL ser
`PARADO`, `SEM_INICIO`, `AVANCANDO` e `CONCLUIU`, com mais dias sem concluir primeiro e nome como
desempate, e o parâmetro `situacao` SHALL filtrar por uma situação. A paginação SHALL usar `pagina`
e `tamanho`, com tamanho padrão 20 e máximo 100.

#### Scenario: Lista da empresa

- **WHEN** a conta `NATY` lista os integrantes de uma empresa com um integrante parado e outro
  avançando
- **THEN** o parado vem primeiro, e cada item traz a situação, o percentual e a atividade atual

#### Scenario: Filtro por situação

- **WHEN** a conta `NATY` lista com `situacao=PARADO`
- **THEN** a resposta traz só os integrantes parados

#### Scenario: Outra empresa não aparece

- **WHEN** a conta `NATY` lista os integrantes da empresa A
- **THEN** nenhum integrante da empresa B está na resposta

#### Scenario: Desativado e NATY ficam fora

- **WHEN** a empresa tem um integrante desativado e uma conta `NATY`
- **THEN** nenhum dos dois está na lista

### Requirement: Métricas agregadas da empresa

`GET /api/v1/acompanhamento/empresas/{empresaId}/metricas` SHALL devolver o total de integrantes
considerados, a adesão em percentual, o percentual médio de conclusão e a contagem por situação
(`concluiram`, `avancando`, `parados`, `semInicio`). A adesão SHALL ser a divisão inteira, vezes
cem, dos integrantes que concluíram ao menos uma atividade pelo total. O percentual médio SHALL ser
a média inteira dos percentuais de conclusão de todos os integrantes considerados. Empresa sem
integrantes SHALL devolver zeros. As contagens SHALL coincidir com as situações da listagem.

#### Scenario: Métricas de empresa com quatro integrantes

- **WHEN** a empresa tem um integrante em cada situação
- **THEN** o total é 4, cada contagem é 1 e a adesão é 50

#### Scenario: Empresa vazia

- **WHEN** a empresa não tem integrante considerado
- **THEN** o total, a adesão, o percentual médio e as contagens são zero

### Requirement: Exportação em CSV

`GET /api/v1/acompanhamento/empresas/{empresaId}/integrantes.csv` SHALL devolver todos os
integrantes da listagem, sem paginação, em `text/csv` UTF-8 com cabeçalho e com a ordem e o filtro
`situacao` da listagem. Campos com vírgula, aspas ou quebra de linha SHALL ir entre aspas com as
aspas duplicadas. Campo de texto que comece com `=`, `+`, `-` ou `@` SHALL ser precedido de apóstrofo
para não ser lido como fórmula por planilha. A resposta SHALL indicar anexo com nome de arquivo
derivado do identificador da empresa.

#### Scenario: CSV da empresa

- **WHEN** a conta `NATY` baixa o CSV de uma empresa com três integrantes
- **THEN** a resposta tem uma linha de cabeçalho e três linhas, na ordem da listagem

#### Scenario: Nome que parece fórmula

- **WHEN** um integrante tem nome que começa com `=`
- **THEN** a célula do CSV começa com apóstrofo

#### Scenario: Nome com vírgula

- **WHEN** um integrante tem vírgula no nome
- **THEN** a célula vai entre aspas

#### Scenario: Cabeçalhos da resposta

- **WHEN** a conta `NATY` baixa o CSV
- **THEN** o tipo de conteúdo é `text/csv` e o cabeçalho de disposição indica anexo
