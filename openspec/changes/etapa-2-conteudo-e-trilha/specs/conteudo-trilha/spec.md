## Purpose

Expoe o conteudo do treinamento que os integrantes das empresas clientes percorrem:
a trilha, seus modulos, as atividades de cada modulo e o quiz de cada atividade. E a
fonte de leitura que o app Flutter usa para desenhar a jornada de aprendizado.

## ADDED Requirements

### Requirement: Listagem de trilhas ativas

O sistema SHALL devolver a lista de trilhas ativas, ordenadas pelo campo de ordem
crescente. Trilha inativa NAO pode aparecer na listagem.

#### Scenario: Existem trilhas ativas

- **WHEN** o cliente solicita a lista de trilhas
- **THEN** o sistema devolve as trilhas ativas em ordem crescente de ordem, cada uma
  com identificador, titulo e descricao

#### Scenario: Trilha inativa nao e listada

- **WHEN** existe uma trilha marcada como inativa
- **THEN** essa trilha NAO aparece na lista devolvida

#### Scenario: Nenhuma trilha ativa

- **WHEN** nao existe nenhuma trilha ativa
- **THEN** o sistema devolve uma lista vazia com status de sucesso

### Requirement: Detalhe de trilha com modulos e atividades

O sistema SHALL devolver uma trilha com seus modulos e, dentro de cada modulo, suas
atividades, todos ordenados pelo campo de ordem crescente. Cada atividade SHALL
carregar titulo, descricao, imagem, pontuacao e a duracao quando houver.

#### Scenario: Trilha existente

- **WHEN** o cliente solicita o detalhe de uma trilha que existe
- **THEN** o sistema devolve a trilha com seus modulos em ordem, e cada modulo com
  suas atividades em ordem

#### Scenario: Trilha inexistente

- **WHEN** o cliente solicita o detalhe de uma trilha que nao existe
- **THEN** o sistema devolve erro de recurso nao encontrado

#### Scenario: Modulo sem atividade

- **WHEN** um modulo da trilha nao tem nenhuma atividade
- **THEN** o modulo aparece no resultado com lista de atividades vazia

### Requirement: Detalhe de atividade

O sistema SHALL devolver os dados de uma atividade: titulo, descricao, imagem,
pontuacao, duracao quando houver, e a indicacao de que ela possui quiz.

#### Scenario: Atividade existente

- **WHEN** o cliente solicita uma atividade que existe
- **THEN** o sistema devolve os dados dela

#### Scenario: Atividade inexistente

- **WHEN** o cliente solicita uma atividade que nao existe
- **THEN** o sistema devolve erro de recurso nao encontrado

#### Scenario: Atividade sem video publicado

- **WHEN** a atividade ainda nao tem video associado
- **THEN** o sistema devolve a atividade com a imagem preenchida e o video ausente,
  sem tratar isso como erro

### Requirement: Quiz de atividade sem gabarito

O sistema SHALL devolver o quiz de uma atividade com suas perguntas e as
alternativas de cada pergunta, todas ordenadas pelo campo de ordem crescente. A
resposta NAO pode conter, em nenhuma forma, a indicacao de qual alternativa e a
correta. O sistema SHALL informar a nota minima de aprovacao do quiz.

#### Scenario: Quiz de atividade existente

- **WHEN** o cliente solicita o quiz de uma atividade que possui quiz
- **THEN** o sistema devolve as perguntas em ordem, cada uma com suas alternativas em
  ordem, e a nota minima de aprovacao

#### Scenario: Gabarito nao vaza

- **WHEN** o cliente solicita o quiz de uma atividade
- **THEN** nenhum campo da resposta revela qual alternativa e a correta

#### Scenario: Atividade sem quiz

- **WHEN** o cliente solicita o quiz de uma atividade que nao possui quiz
- **THEN** o sistema devolve erro de recurso nao encontrado

### Requirement: Conteudo identico para todas as empresas

O conteudo do treinamento SHALL ser global. A resposta de qualquer consulta de
conteudo NAO pode variar em funcao da empresa ou do integrante que consulta.

#### Scenario: Integrantes de empresas diferentes

- **WHEN** integrantes de duas empresas distintas consultam a mesma trilha
- **THEN** ambos recebem exatamente o mesmo conteudo
