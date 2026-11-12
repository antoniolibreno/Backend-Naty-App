# Spec Delta

## Purpose

Recompensa o estudo do integrante na trilha com pontos, sequência de dias e conquistas, e
devolve esse estado para o app montar o perfil no estilo Duolingo.

## ADDED Requirements

### Requirement: Ponto concedido na conclusão da atividade

O sistema SHALL conceder ao integrante os pontos de `atividade.xp` no instante em que ele
conclui a atividade pela primeira vez. O valor concedido SHALL ser o de `atividade.xp` no
momento da conclusão. O sistema NÃO SHALL conceder ponto por nenhuma outra fonte: vídeo
assistido sem conclusão, tentativa reprovada e início de vídeo não pontuam.

#### Scenario: Conclusão por tentativa aprovada

- **WHEN** o integrante é aprovado pela primeira vez no quiz de uma atividade com `xp` 10
- **THEN** o total de pontos dele aumenta em 10

#### Scenario: Conclusão por vídeo assistido em atividade sem quiz

- **WHEN** o integrante registra vídeo assistido numa atividade sem quiz com `xp` 10
- **THEN** o total de pontos dele aumenta em 10

#### Scenario: Vídeo assistido em atividade com quiz

- **WHEN** o integrante registra vídeo assistido numa atividade com quiz e não tem
  tentativa aprovada nela
- **THEN** o total de pontos dele não muda

#### Scenario: Tentativa reprovada

- **WHEN** o integrante é reprovado no quiz de uma atividade
- **THEN** o total de pontos dele não muda

#### Scenario: Valor alterado depois da concessão

- **WHEN** `atividade.xp` muda depois de o integrante ter concluído a atividade
- **THEN** os pontos já concedidos a ele por essa atividade não mudam

### Requirement: Ponto único por integrante e atividade

O sistema SHALL conceder ponto no máximo uma vez por integrante e atividade. Repetir o
registro de vídeo assistido, refazer quiz já aprovado ou repetir a mesma chamada em
concorrência NÃO SHALL somar ponto de novo.

#### Scenario: Quiz refeito depois da aprovação

- **WHEN** o integrante aprovado no quiz de uma atividade envia nova tentativa aprovada
- **THEN** o total de pontos dele é o mesmo de antes da nova tentativa

#### Scenario: Vídeo assistido repetido

- **WHEN** o integrante registra de novo vídeo assistido numa atividade sem quiz já
  concluída
- **THEN** o total de pontos dele é o mesmo de antes do novo registro

#### Scenario: Chamadas concorrentes

- **WHEN** duas tentativas aprovadas do mesmo integrante na mesma atividade chegam ao
  mesmo tempo
- **THEN** o integrante recebe os pontos da atividade uma única vez

### Requirement: Dia de estudo no fuso da empresa

O sistema SHALL marcar como dia de estudo do integrante a data local, no fuso horário da
empresa dele, em que ocorre uma tentativa de quiz, o primeiro registro de vídeo assistido
de uma atividade ou a primeira conclusão de uma atividade. O instante SHALL ser gravado em
UTC. Início de vídeo e chamada repetida que não grava fato novo NÃO SHALL marcar dia.

#### Scenario: Tentativa reprovada conta como estudo

- **WHEN** o integrante envia uma tentativa reprovada
- **THEN** a data local corrente no fuso da empresa conta como dia de estudo

#### Scenario: Virada do dia pelo fuso da empresa

- **WHEN** o integrante de uma empresa com fuso `America/Sao_Paulo` envia uma tentativa
  às 01:00 UTC
- **THEN** o dia de estudo marcado é a data local de São Paulo, que é o dia anterior à
  data UTC

#### Scenario: Início de vídeo não conta como estudo

- **WHEN** o integrante só registra o início de um vídeo num dia
- **THEN** esse dia não conta como dia de estudo

#### Scenario: Vários estudos no mesmo dia

- **WHEN** o integrante estuda mais de uma vez na mesma data local
- **THEN** essa data conta como um único dia de estudo

### Requirement: Sequência atual de dias

A sequência atual SHALL ser a quantidade de dias de estudo consecutivos que termina na
data local corrente da empresa ou na data imediatamente anterior a ela. Quando o último
dia de estudo é anterior a essas duas datas, a sequência atual SHALL ser zero.

#### Scenario: Estudou no dia corrente e nos dois anteriores

- **WHEN** o integrante tem dia de estudo na data corrente e nas duas datas anteriores
- **THEN** a sequência atual é 3

#### Scenario: Sequência viva até o fim do dia corrente

- **WHEN** o integrante estudou nas duas datas anteriores e não estudou na data corrente
- **THEN** a sequência atual é 2

#### Scenario: Sequência quebrada

- **WHEN** o último dia de estudo do integrante é anterior à data imediatamente anterior
  à corrente
- **THEN** a sequência atual é zero

#### Scenario: Lacuna no meio

- **WHEN** o integrante estudou na data corrente, não estudou na anterior e estudou nas
  duas antes dela
- **THEN** a sequência atual é 1

### Requirement: Maior sequência de dias

A maior sequência SHALL ser a quantidade máxima de dias de estudo consecutivos já
registrada pelo integrante, e NÃO SHALL diminuir quando a sequência atual quebra.

#### Scenario: Sequência antiga maior que a atual

- **WHEN** o integrante teve 5 dias seguidos de estudo no passado e tem sequência atual 2
- **THEN** a maior sequência é 5

### Requirement: Catálogo global de conquistas

As conquistas SHALL ser conteúdo global, iguais para todas as empresas e entregues pelo
schema do banco, sem rota de escrita. Cada conquista SHALL ter código estável, nome,
descrição, critério, limiar e ordem de exibição.

#### Scenario: Catálogo inicial

- **WHEN** a aplicação sobe com o schema aplicado
- **THEN** o catálogo contém, em ordem, `PRIMEIRA_ATIVIDADE`, `QUIZ_PERFEITO`,
  `MODULO_CONCLUIDO`, `TRILHA_CONCLUIDA`, `SEQUENCIA_3_DIAS`, `SEQUENCIA_7_DIAS` e
  `PONTOS_50`

#### Scenario: Mesmo catálogo em empresas diferentes

- **WHEN** integrantes de duas empresas leem o perfil de gamificação
- **THEN** os dois recebem o mesmo catálogo de conquistas

### Requirement: Concessão única de conquista

O sistema SHALL avaliar o catálogo a cada fato de estudo do integrante e conceder, uma
única vez por integrante, cada conquista cujo critério ele atinge. O instante de obtenção
SHALL ser o do fato de estudo que atingiu o critério. Conquista obtida NÃO SHALL ser
retirada.

#### Scenario: Primeira atividade concluída

- **WHEN** o integrante conclui a primeira atividade
- **THEN** ele obtém `PRIMEIRA_ATIVIDADE`

#### Scenario: Conquista não se repete

- **WHEN** o integrante que já tem `PRIMEIRA_ATIVIDADE` conclui outra atividade
- **THEN** ele continua com uma única obtenção de `PRIMEIRA_ATIVIDADE`, com o instante
  original

#### Scenario: Conquista preservada depois da quebra de sequência

- **WHEN** o integrante que obteve `SEQUENCIA_3_DIAS` fica dois dias sem estudar
- **THEN** ele continua com `SEQUENCIA_3_DIAS`

### Requirement: Critérios de conquista

Cada critério SHALL comparar um indicador do integrante com o limiar da conquista, e a
conquista SHALL ser concedida quando o indicador é maior ou igual ao limiar. Os critérios
SHALL ser: atividades concluídas, maior nota em tentativa de quiz, módulos com todas as
atividades concluídas, trilhas com todas as atividades concluídas, maior sequência de dias
e total de pontos.

#### Scenario: Quiz perfeito

- **WHEN** o integrante envia uma tentativa com nota 100
- **THEN** ele obtém `QUIZ_PERFEITO`

#### Scenario: Módulo concluído

- **WHEN** o integrante conclui a última atividade pendente de um módulo
- **THEN** ele obtém `MODULO_CONCLUIDO`

#### Scenario: Trilha concluída

- **WHEN** o integrante conclui a última atividade pendente de uma trilha
- **THEN** ele obtém `TRILHA_CONCLUIDA`

#### Scenario: Sequência de 3 dias

- **WHEN** o fato de estudo do integrante leva a maior sequência dele a 3 dias
- **THEN** ele obtém `SEQUENCIA_3_DIAS`, e não obtém `SEQUENCIA_7_DIAS`

#### Scenario: 50 pontos

- **WHEN** a conclusão de uma atividade leva o total de pontos do integrante a 50 ou mais
- **THEN** ele obtém `PONTOS_50`

#### Scenario: Limiar não atingido

- **WHEN** o integrante tem 40 pontos
- **THEN** ele não tem `PONTOS_50`

### Requirement: Pontos e dias derivados do progresso gravado

Toda conclusão gravada no progresso SHALL ter os pontos da atividade concedidos no
instante da conclusão, inclusive conclusão gravada pelo schema sem passar pela concessão.
Toda tentativa, vídeo assistido e conclusão gravados SHALL contar como dia de estudo na
data local da empresa. Conquista atingida por progresso que não passou pela avaliação SHALL
ser concedida no próximo fato de estudo do integrante.

#### Scenario: Conclusões gravadas sem concessão

- **WHEN** um integrante com duas conclusões gravadas sem concessão lê o perfil depois de
  o schema ser aplicado
- **THEN** o total de pontos dele é a soma do `xp` das duas atividades

#### Scenario: Conquista avaliada no estudo seguinte

- **WHEN** esse integrante envia a próxima tentativa de quiz
- **THEN** ele obtém `PRIMEIRA_ATIVIDADE`

### Requirement: Perfil de gamificação

`GET /api/v1/integrantes/{integranteId}/gamificacao` SHALL devolver o identificador e o
nome do integrante, o total de pontos, a posição no ranking da empresa, a sequência atual,
a maior sequência, a indicação de estudo na data corrente e o catálogo inteiro de
conquistas em ordem, cada uma com código, nome, descrição e instante de obtenção, nulo
quando não obtida.

#### Scenario: Integrante sem estudo

- **WHEN** o integrante sem nenhum estudo lê o próprio perfil
- **THEN** a resposta traz zero pontos, sequência atual zero, maior sequência zero,
  `diaCorrenteEstudado` falso e todas as conquistas com instante de obtenção nulo

#### Scenario: Integrante com conquista

- **WHEN** o integrante que obteve `PRIMEIRA_ATIVIDADE` lê o próprio perfil
- **THEN** `PRIMEIRA_ATIVIDADE` vem com o instante de obtenção e as demais vêm com nulo

#### Scenario: Posição no ranking

- **WHEN** o integrante lê o próprio perfil
- **THEN** a posição devolvida é a mesma que ele ocupa em `GET /api/v1/ranking`

### Requirement: Perfil restrito à empresa de quem chamou

O perfil de gamificação SHALL exigir token de sessão válido e SHALL ser lido apenas para
conta ativa da empresa do dono do token. Integrante de outra empresa, integrante
desativado e identificador inexistente SHALL responder 404 com o mesmo código
`RECURSO_NAO_ENCONTRADO`.

#### Scenario: Colega da mesma empresa

- **WHEN** o integrante lê o perfil de um colega ativo da mesma empresa
- **THEN** o sistema devolve o perfil do colega

#### Scenario: Integrante de outra empresa

- **WHEN** o integrante lê o perfil de um integrante de outra empresa
- **THEN** o sistema responde 404 `RECURSO_NAO_ENCONTRADO`

#### Scenario: Colega desativado

- **WHEN** o integrante lê o perfil de um colega desativado da mesma empresa
- **THEN** o sistema responde 404 `RECURSO_NAO_ENCONTRADO`

#### Scenario: Sem token

- **WHEN** o cliente lê um perfil de gamificação sem token
- **THEN** o sistema responde 401 `CREDENCIAL_INVALIDA`
