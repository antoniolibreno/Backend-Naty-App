## Purpose

Expõe o conteúdo do treinamento que os integrantes das empresas clientes percorrem:
a trilha, seus módulos, as atividades de cada módulo e o quiz de cada atividade. É a
fonte de leitura que o app Flutter usa para desenhar a jornada de aprendizado.

## ADDED Requirements

### Requirement: Listagem de trilhas ativas

O sistema SHALL devolver a lista de trilhas ativas, ordenadas pelo campo de ordem
crescente. Trilha inativa NÃO pode aparecer na listagem.

#### Scenario: Existem trilhas ativas

- **WHEN** o cliente solicita a lista de trilhas
- **THEN** o sistema devolve as trilhas ativas em ordem crescente de ordem, cada uma
  com identificador, título e descrição

#### Scenario: Trilha inativa não é listada

- **WHEN** existe uma trilha marcada como inativa
- **THEN** essa trilha NÃO aparece na lista devolvida

#### Scenario: Nenhuma trilha ativa

- **WHEN** não existe nenhuma trilha ativa
- **THEN** o sistema devolve uma lista vazia com status de sucesso

### Requirement: Detalhe de trilha com módulos e atividades

O sistema SHALL devolver uma trilha com seus módulos e, dentro de cada módulo, suas
atividades, todos ordenados pelo campo de ordem crescente. Cada atividade SHALL
carregar título, descrição, imagem, pontuação e a duração quando houver.

#### Scenario: Trilha existente

- **WHEN** o cliente solicita o detalhe de uma trilha que existe
- **THEN** o sistema devolve a trilha com seus módulos em ordem, e cada módulo com
  suas atividades em ordem

#### Scenario: Trilha inexistente

- **WHEN** o cliente solicita o detalhe de uma trilha que não existe
- **THEN** o sistema devolve erro de recurso não encontrado

#### Scenario: Módulo sem atividade

- **WHEN** um módulo da trilha não tem nenhuma atividade
- **THEN** o módulo aparece no resultado com lista de atividades vazia

### Requirement: Detalhe de atividade

O sistema SHALL devolver os dados de uma atividade: título, descrição, imagem,
pontuação, duração quando houver, e a indicação de que ela possui quiz.

#### Scenario: Atividade existente

- **WHEN** o cliente solicita uma atividade que existe
- **THEN** o sistema devolve os dados dela

#### Scenario: Atividade inexistente

- **WHEN** o cliente solicita uma atividade que não existe
- **THEN** o sistema devolve erro de recurso não encontrado

#### Scenario: Atividade sem vídeo publicado

- **WHEN** a atividade ainda não tem vídeo associado
- **THEN** o sistema devolve a atividade com a imagem preenchida e o vídeo ausente,
  sem tratar isso como erro

### Requirement: Quiz de atividade sem gabarito

O sistema SHALL devolver o quiz de uma atividade com suas perguntas e as
alternativas de cada pergunta, todas ordenadas pelo campo de ordem crescente. A
resposta NÃO pode conter, em nenhuma forma, a indicação de qual alternativa é a
correta. O sistema SHALL informar a nota mínima de aprovação do quiz.

#### Scenario: Quiz de atividade existente

- **WHEN** o cliente solicita o quiz de uma atividade que possui quiz
- **THEN** o sistema devolve as perguntas em ordem, cada uma com suas alternativas em
  ordem, e a nota mínima de aprovação

#### Scenario: Gabarito não vaza

- **WHEN** o cliente solicita o quiz de uma atividade
- **THEN** nenhum campo da resposta revela qual alternativa é a correta

#### Scenario: Atividade sem quiz

- **WHEN** o cliente solicita o quiz de uma atividade que não possui quiz
- **THEN** o sistema devolve erro de recurso não encontrado

### Requirement: Conteúdo idêntico para todas as empresas

O conteúdo do treinamento SHALL ser global. A resposta de qualquer consulta de
conteúdo NÃO pode variar em função da empresa ou do integrante que consulta.

#### Scenario: Integrantes de empresas diferentes

- **WHEN** integrantes de duas empresas distintas consultam a mesma trilha
- **THEN** ambos recebem exatamente o mesmo conteúdo
