## Purpose

Guarda e devolve o estado de cada integrante na trilha de treinamento: o que ele já
assistiu, o que já concluiu e o que ainda não abriu. É o que faz a trilha avançar nó a
nó, no estilo Duolingo, e a fonte de leitura que a Home do app Flutter usa para se
desenhar.

## ADDED Requirements

### Requirement: Integrante da requisição

Toda operação de progresso SHALL ser executada em nome de um integrante informado pelo
cliente no cabeçalho da requisição. O sistema SHALL resolver, a partir desse
identificador, o integrante e a empresa dele. O sistema NÃO SHALL aceitar operação de
progresso sem esse identificador.

Enquanto não existir autenticação, esse identificador NÃO constitui prova de identidade
e pode ser forjado. Essa limitação SHALL estar visível na documentação da API.

#### Scenario: Identificador presente e existente

- **WHEN** o cliente envia uma operação de progresso informando o identificador de um
  integrante existente
- **THEN** o sistema executa a operação em nome desse integrante e da empresa dele

#### Scenario: Identificador ausente

- **WHEN** o cliente envia uma operação de progresso sem informar o identificador do
  integrante
- **THEN** o sistema devolve erro de validação, e NÃO executa a operação

#### Scenario: Identificador malformado

- **WHEN** o cliente informa um identificador de integrante que não é um identificador
  válido
- **THEN** o sistema devolve erro de validação, e NÃO executa a operação

#### Scenario: Identificador de integrante inexistente

- **WHEN** o cliente informa um identificador que não corresponde a nenhum integrante
- **THEN** o sistema devolve erro de recurso não encontrado

### Requirement: Desbloqueio linear das atividades

O sistema SHALL classificar cada atividade da trilha, para um integrante, em exatamente
um de três estados: bloqueada, disponível ou concluída.

A ordem da trilha SHALL ser a sequência das atividades ordenadas pela ordem do módulo e,
dentro do módulo, pela ordem da atividade. A primeira atividade dessa sequência SHALL
nascer disponível. Cada atividade seguinte SHALL permanecer bloqueada até que a
atividade imediatamente anterior esteja concluída.

#### Scenario: Integrante que nunca estudou

- **WHEN** um integrante sem nenhum progresso consulta a trilha
- **THEN** a primeira atividade está disponível e todas as demais estão bloqueadas

#### Scenario: Conclusão libera a próxima

- **WHEN** o integrante conclui uma atividade que não é a última da sequência
- **THEN** a atividade seguinte passa de bloqueada para disponível

#### Scenario: Conclusão da última atividade

- **WHEN** o integrante conclui a última atividade da sequência
- **THEN** todas as atividades da trilha estão concluídas e nenhuma fica disponível

### Requirement: Leitura da trilha com progresso

O sistema SHALL devolver, para um integrante, a trilha com seus módulos e as atividades
de cada módulo, todos ordenados pelo campo de ordem crescente, com o estado de cada
atividade e os momentos em que o vídeo foi assistido e em que a atividade foi concluída.

O sistema SHALL informar, no mesmo resultado, o total de atividades da trilha, quantas o
integrante concluiu, o percentual concluído e qual é a próxima atividade a estudar. Essa
leitura SHALL bastar para o cliente desenhar a jornada inteira, sem exigir uma segunda
chamada.

#### Scenario: Trilha com progresso parcial

- **WHEN** o integrante que concluiu parte das atividades consulta a trilha
- **THEN** o sistema devolve cada nó com seu estado, o total de atividades, o número de
  concluídas, o percentual concluído e o identificador da próxima atividade

#### Scenario: Trilha inteiramente concluída

- **WHEN** o integrante que concluiu todas as atividades consulta a trilha
- **THEN** o percentual concluído é cem e não existe próxima atividade

#### Scenario: Trilha inexistente

- **WHEN** o integrante consulta o progresso de uma trilha que não existe
- **THEN** o sistema devolve erro de recurso não encontrado

### Requirement: Registro de vídeo assistido

O sistema SHALL registrar que o integrante assistiu o vídeo de uma atividade e SHALL
devolver o estado resultante da atividade e a próxima atividade a estudar.

Enquanto a tentativa de quiz não existir, assistir o vídeo SHALL concluir a atividade,
tenha ela quiz ou não. Quando a tentativa de quiz existir, essa regra passa a exigir
aprovação no quiz para atividade que tenha quiz.

#### Scenario: Vídeo assistido em atividade disponível

- **WHEN** o integrante registra que assistiu o vídeo de uma atividade disponível
- **THEN** o sistema guarda o momento em que o vídeo foi assistido, conclui a atividade
  e libera a atividade seguinte

#### Scenario: Registro repetido

- **WHEN** o integrante registra o mesmo vídeo assistido uma segunda vez
- **THEN** o sistema mantém o registro existente sem duplicá-lo e sem alterar o momento
  da conclusão

#### Scenario: Atividade inexistente

- **WHEN** o integrante registra vídeo assistido de uma atividade que não existe
- **THEN** o sistema devolve erro de recurso não encontrado

### Requirement: Recusa de progresso em atividade bloqueada

O sistema NÃO SHALL registrar progresso em atividade que ainda não foi liberada para o
integrante. A tentativa SHALL ser recusada com erro de conflito de regra de negócio, e
NÃO pode ser tratada como sucesso silencioso.

#### Scenario: Tentativa de pular a trilha

- **WHEN** o integrante registra vídeo assistido de uma atividade bloqueada
- **THEN** o sistema recusa a operação com erro de conflito, e a atividade permanece
  bloqueada

#### Scenario: Nada é gravado na recusa

- **WHEN** a operação em atividade bloqueada é recusada
- **THEN** nenhum registro de progresso é criado para aquela atividade

### Requirement: Progresso isolado por integrante

O progresso SHALL pertencer ao integrante que o produziu. A leitura de progresso de um
integrante NÃO pode refletir o progresso de outro, ainda que os dois sejam da mesma
empresa e percorram a mesma trilha.

#### Scenario: Dois integrantes na mesma trilha

- **WHEN** um integrante conclui atividades e outro integrante da mesma empresa consulta
  a mesma trilha
- **THEN** o segundo integrante vê o próprio progresso, sem nenhuma atividade concluída
  pelo primeiro

#### Scenario: Integrantes de empresas diferentes

- **WHEN** integrantes de duas empresas distintas percorrem a mesma trilha
- **THEN** cada um vê apenas o próprio progresso
