# administracao-de-quiz Specification

## Purpose
Permite à conta NATY escrever quiz, pergunta e alternativa com a integridade do gabarito
(exatamente uma correta por pergunta), a leitura administrativa como único canal que devolve
`correta` e a proteção do histórico de tentativas.

## Requirements

### Requirement: Escrita de quiz restrita à conta NATY

Toda rota de escrita de quiz, pergunta e alternativa e a leitura administrativa do quiz SHALL
exigir token de sessão de conta `NATY`. Conta `ADMIN` e conta `INTEGRANTE` SHALL receber 403
`ACESSO_NEGADO`, e chamada sem token SHALL receber 401 `CREDENCIAL_INVALIDA`.

#### Scenario: Integrante lê o gabarito

- **WHEN** uma conta `INTEGRANTE` ou `ADMIN` chama `GET /api/v1/quizzes/{quizId}`
- **THEN** o sistema responde 403 `ACESSO_NEGADO`

#### Scenario: Integrante tenta escrever

- **WHEN** uma conta `INTEGRANTE` ou `ADMIN` chama uma rota de escrita de quiz, pergunta ou
  alternativa
- **THEN** o sistema responde 403 `ACESSO_NEGADO` e NÃO altera o quiz

#### Scenario: Sem token

- **WHEN** o cliente chama uma rota de escrita de quiz sem token
- **THEN** o sistema responde 401 `CREDENCIAL_INVALIDA`

### Requirement: Cadastro de quiz

`POST /api/v1/quizzes` SHALL criar o quiz de uma atividade existente com nota mínima e ao
menos uma pergunta, cada uma com suas alternativas, e responder 201 com a leitura
administrativa. A nota mínima SHALL ser inteiro de 0 a 100 e valer 70 quando omitida. Atividade
que já tem quiz SHALL responder 409 `QUIZ_JA_EXISTE`. `PUT /api/v1/quizzes/{quizId}` SHALL
alterar a nota mínima, e `DELETE /api/v1/quizzes/{quizId}` SHALL remover o quiz com suas
perguntas e alternativas e responder 204. Atividade inexistente e id inexistente SHALL
responder 404 `RECURSO_NAO_ENCONTRADO`.

#### Scenario: Quiz criado

- **WHEN** a conta `NATY` cria um quiz válido para uma atividade sem quiz
- **THEN** o sistema responde 201 e a leitura do quiz pelo integrante devolve as perguntas e
  alternativas sem o gabarito

#### Scenario: Atividade com quiz

- **WHEN** a conta `NATY` cria um quiz para uma atividade que já tem quiz
- **THEN** o sistema responde 409 `QUIZ_JA_EXISTE`

#### Scenario: Nota mínima fora da faixa

- **WHEN** a conta `NATY` cria ou altera um quiz com nota mínima 101
- **THEN** o sistema responde 400 `FALHA_DE_VALIDACAO`

#### Scenario: Quiz sem pergunta

- **WHEN** a conta `NATY` cria um quiz sem pergunta
- **THEN** o sistema responde 400 `FALHA_DE_VALIDACAO`

### Requirement: Cadastro de pergunta

`POST /api/v1/perguntas` SHALL criar uma pergunta em um quiz existente com enunciado e ao menos
duas alternativas, com ordem acrescentada depois da última. `PUT /api/v1/perguntas/{perguntaId}`
SHALL alterar o enunciado, e `DELETE /api/v1/perguntas/{perguntaId}` SHALL remover a pergunta
com suas alternativas. O sistema SHALL recusar com 409 `QUIZ_SEM_PERGUNTA` a remoção da última
pergunta de um quiz.

#### Scenario: Pergunta criada

- **WHEN** a conta `NATY` cria uma pergunta com duas alternativas e uma correta
- **THEN** o sistema responde 201 e a pergunta aparece depois das existentes

#### Scenario: Remoção da última pergunta

- **WHEN** a conta `NATY` remove a única pergunta de um quiz
- **THEN** o sistema responde 409 `QUIZ_SEM_PERGUNTA` e a pergunta continua existindo

#### Scenario: Enunciado ausente

- **WHEN** a conta `NATY` cria uma pergunta sem enunciado
- **THEN** o sistema responde 400 `FALHA_DE_VALIDACAO`

### Requirement: Exatamente uma alternativa correta por pergunta

Toda pergunta SHALL ter exatamente uma alternativa correta e ao menos duas alternativas, em
toda escrita e em todo estado intermediário. A criação de pergunta com zero ou mais de uma
correta SHALL responder 400 `FALHA_DE_VALIDACAO`. `POST /api/v1/alternativas` e
`PUT /api/v1/alternativas/{alternativaId}` com `correta` verdadeiro SHALL mover a marca,
deixando a alternativa anterior como incorreta na mesma operação. Desmarcar a única correta,
remover a correta e remover uma alternativa que deixe menos de duas SHALL responder 409
`GABARITO_INVALIDO` sem alterar nada.

#### Scenario: Pergunta sem correta

- **WHEN** a conta `NATY` cria uma pergunta sem alternativa correta
- **THEN** o sistema responde 400 `FALHA_DE_VALIDACAO` e NÃO grava a pergunta

#### Scenario: Pergunta com duas corretas

- **WHEN** a conta `NATY` cria uma pergunta com duas alternativas corretas
- **THEN** o sistema responde 400 `FALHA_DE_VALIDACAO`

#### Scenario: Mover a marca de correta

- **WHEN** a conta `NATY` altera uma alternativa incorreta para `correta` verdadeiro
- **THEN** a pergunta fica com exatamente uma correta, a alterada

#### Scenario: Desmarcar a única correta

- **WHEN** a conta `NATY` altera a alternativa correta para `correta` falso
- **THEN** o sistema responde 409 `GABARITO_INVALIDO` e a marca permanece

#### Scenario: Remover a correta

- **WHEN** a conta `NATY` remove a alternativa correta
- **THEN** o sistema responde 409 `GABARITO_INVALIDO`

#### Scenario: Remover abaixo do mínimo

- **WHEN** a conta `NATY` remove uma alternativa de uma pergunta com duas alternativas
- **THEN** o sistema responde 409 `GABARITO_INVALIDO`

### Requirement: Gabarito só na leitura administrativa

`GET /api/v1/quizzes/{quizId}` SHALL devolver o quiz com as perguntas, as alternativas e o
campo `correta` de cada alternativa, e SHALL ser o único canal que devolve `correta`. A leitura
do quiz pelo integrante em `GET /api/v1/atividades/{atividadeId}/quiz` e toda resposta de
tentativa NÃO SHALL conter `correta`. O contrato de escrita de alternativa SHALL ser distinto
do contrato de leitura do integrante.

#### Scenario: Leitura administrativa

- **WHEN** a conta `NATY` lê um quiz por `GET /api/v1/quizzes/{quizId}`
- **THEN** cada alternativa traz `correta`, e exatamente uma por pergunta é verdadeira

#### Scenario: Leitura do integrante sem gabarito

- **WHEN** o integrante lê o quiz da atividade logo depois de uma escrita da conta `NATY`
- **THEN** nenhuma alternativa da resposta contém `correta`

### Requirement: Quiz com histórico não é removido

O sistema SHALL recusar com 409 `CONTEUDO_COM_PROGRESSO` a remoção de quiz quando a atividade
dele tem progresso, ponto ou tentativa de quiz registrados, e a remoção de pergunta ou de
alternativa quando alguma resposta de tentativa referencia o item, e NÃO SHALL remover nada
nesse caso.

#### Scenario: Quiz com tentativa

- **WHEN** a conta `NATY` remove um quiz que um integrante já tentou
- **THEN** o sistema responde 409 `CONTEUDO_COM_PROGRESSO` e o quiz continua existindo

#### Scenario: Quiz de atividade com progresso

- **WHEN** a conta `NATY` remove o quiz de uma atividade que um integrante já estudou, mesmo
  sem nenhuma tentativa
- **THEN** o sistema responde 409 `CONTEUDO_COM_PROGRESSO`

#### Scenario: Alternativa já respondida

- **WHEN** a conta `NATY` remove uma alternativa escolhida em alguma tentativa
- **THEN** o sistema responde 409 `CONTEUDO_COM_PROGRESSO`

### Requirement: Registro das alterações de quiz

Toda criação, alteração e remoção de quiz, pergunta e alternativa SHALL entrar no registro de
alterações de conteúdo com as entidades `QUIZ`, `PERGUNTA` e `ALTERNATIVA`, e a mudança da marca
de correta SHALL gerar um registro `ALTERACAO` na alternativa que ganhou a marca e outro na que
a perdeu.

#### Scenario: Troca de gabarito registrada

- **WHEN** a conta `NATY` move a marca de correta de uma alternativa para outra
- **THEN** a listagem de alterações traz dois registros `ALTERACAO`, um por alternativa, com a
  conta e o momento
