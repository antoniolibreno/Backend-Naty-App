# conteudo

## Responsabilidade

Escrita do conteúdo de treinamento: trilha, módulo, atividade e quiz com suas perguntas e
alternativas, a ordem entre irmãos, a ativação da trilha, a proteção do histórico, a integridade
do gabarito e o registro de quem alterou o quê. É o único
pacote que escreve nas entidades de `trilha`, no mesmo desenho em que `painel` é o único que
escreve em `usuario`. A leitura do conteúdo continua em `trilha`, que não conhece este pacote.

## Contratos

- `TrilhaConteudoController`: `POST /api/v1/trilhas`, `PUT` e `DELETE /api/v1/trilhas/{id}`,
  `PUT /api/v1/trilhas/{id}/ativa` e `PUT /api/v1/trilhas/{id}/modulos/ordem`.
- `ModuloConteudoController`: `/api/v1/modulos` e `PUT /api/v1/modulos/{id}/atividades/ordem`.
- `AtividadeConteudoController`: `POST /api/v1/atividades`, `PUT` e `DELETE
  /api/v1/atividades/{id}`.
- `QuizConteudoController`: `/api/v1/quizzes`, com `GET /{id}` como leitura administrativa.
  `PerguntaConteudoController`: `/api/v1/perguntas`. `AlternativaConteudoController`:
  `/api/v1/alternativas`.
- `QuizConteudoService`: escrita de quiz, pergunta e alternativa e a integridade do gabarito.
- `AlteracaoDeConteudoController`: `GET /api/v1/alteracoes-de-conteudo`, paginado, do mais
  recente para o mais antigo, com filtro opcional `entidadeId`.
- `TrilhaConteudoService`, `ModuloConteudoService`, `AtividadeConteudoService` e
  `ReordenacaoService`: regra de escrita, cada uma em uma transação.
- `AuditoriaDeConteudo`: grava `AlteracaoDeConteudo` na transação de quem chama
  (`Propagation.MANDATORY`). Entidades `TRILHA`, `MODULO`, `ATIVIDADE`, `QUIZ`, `PERGUNTA` e
  `ALTERNATIVA`, ações `CRIACAO`, `ALTERACAO`, `REMOCAO`, `ATIVACAO`, `DESATIVACAO` e
  `REORDENACAO`.
- `ProtecaoDeHistorico`: recusa a remoção quando alguma atividade atingida tem progresso,
  ponto ou tentativa de quiz.
- `dto/`: requests de entrada, `TrilhaAdminResponse` e as respostas administrativas de quiz
  (`QuizAdminResponse`, `PerguntaAdminResponse` e `AlternativaAdminResponse`, a única com o
  campo `correta`). Módulo e atividade reaproveitam `ModuloResponse` e
  `AtividadeDetalheResponse` de `trilha`.

## Decisões

Toda rota de escrita exige `NATY`, nunca `ADMIN`. O conteúdo é global, e `ADMIN` é o
administrador de um cliente. Os matchers moram em `config/SecurityConfig` e usam um segmento
(`*`) para não alcançar `POST /api/v1/atividades/{id}/video-assistido` nem
`/quiz/tentativas`, que são do integrante.

Criar sempre acrescenta ao fim, com `max(ordem) + 1` sob trava do pai. A ordem só muda pela
reordenação, que recebe a lista completa dos irmãos e renumera de 1 a n em duas passagens: soma
um deslocamento a todos e depois grava a posição final. O índice único `(pai, ordem)` não é
postergável, e uma passagem só colidiria. Lista diferente dos irmãos atuais responde 409
`ORDEM_INVALIDA`. Remover deixa lacuna na ordem e a leitura ordena por `ordem`, então a lacuna
não aparece.

`trilha.ativa` é a única ativação. Trilha inativa sai da listagem, mas `GET /api/v1/trilhas/{id}`
e o progresso continuam a lendo, porque o histórico de quem a estudou aponta para ela.

Remover trilha, módulo ou atividade com histórico responde 409 `CONTEUDO_COM_PROGRESSO`. A
consulta é nativa, por nome de tabela (`progresso_atividade`, `ponto_concedido`,
`tentativa_quiz`), porque `trilha` e `conteudo` não podem depender de `progresso`. É um
acoplamento por schema aceito de propósito. Sem histórico, o `cascade` das entidades remove os
módulos, as atividades, o quiz, as perguntas e as alternativas.

A auditoria é uma tabela própria, sem FK para o conteúdo, para o registro de uma remoção
sobreviver a ela. A FK para `usuario` tem `on delete cascade`, como toda FK nova para essa
tabela. Alteração recusada reverte a transação e o registro junto.

Reordenar módulos registra `REORDENACAO` na trilha, e reordenar atividades registra no módulo.

## Gabarito

`GET /api/v1/quizzes/{id}` é o único canal que devolve `correta`. A alternativa tem três tipos
separados, nunca um com o campo anulado: `AlternativaResponse` em `trilha` (leitura do
integrante, sem o campo), `AlternativaRequest` (escrita) e `AlternativaAdminResponse`
(leitura administrativa). Nenhuma classe fora de `conteudo` depende de `conteudo.dto`.

Toda pergunta tem exatamente uma alternativa correta e ao menos duas alternativas, na entrada
e em toda transição. Criar pergunta com zero ou mais de uma correta sai como
`FALHA_DE_VALIDACAO`. Marcar `correta` em `POST` ou `PUT` de alternativa move a marca na mesma
transação. Desmarcar a única correta, remover a correta e remover uma alternativa que deixe menos
de duas respondem 409 `GABARITO_INVALIDO`. Remover a última pergunta responde 409
`QUIZ_SEM_PERGUNTA`, porque quiz sem pergunta quebra a nota percentual.

A pergunta é travada por `PESSIMISTIC_WRITE` antes de qualquer escrita de alternativa. A trava
serializa duas escritas do mesmo gabarito, e um teste dispara marcações concorrentes e confirma
uma única correta no fim.

Mover a marca grava dois registros `ALTERACAO`, um por alternativa. Criar um quiz grava um
registro de criação para o quiz, para cada pergunta e para cada alternativa.

Remover quiz usa o histórico da atividade inteira, porque a atividade com quiz conclui por
aprovação e a sem quiz conclui por vídeo. Remover pergunta e alternativa consulta
`resposta_tentativa`. A nota gravada em tentativa antiga não é recalculada quando o gabarito
muda, e o registro de alterações explica a diferença. Criar quiz em atividade que integrantes
já estudaram muda a regra de conclusão dela para quem não a concluiu.

Ordem de pergunta e de alternativa não tem rota de reordenação: a criação acrescenta ao fim.

## Armadilhas

`Atividade.setVideoUrl` valida o vídeo e lança `IllegalArgumentException`, que o handler global
não trata e sairia como 500. A validação de campo acontece antes, nos requests, com
`@AssertTrue`, e sai como `FALHA_DE_VALIDACAO`.

Duração só existe com vídeo, e vídeo exige duração positiva. A regra está nos dois requests de
atividade, porque a progressão trata duração sem vídeo como configuração quebrada.

A reordenação usa `update` em lote com `clearAutomatically`. Entidade carregada antes dela fica
desatualizada, então o serviço não lê entidade de ordem depois de reordenar.

`RegrasDeDependenciaTest` reprova qualquer classe fora de `conteudo` que chame setter de
entidade de conteúdo ou escreva nos repositórios de `trilha`, qualquer `*Response` fora de
`conteudo` com campo `correta`, e qualquer dependência de fora de `conteudo` em `conteudo.dto`.

Remover alternativa exige tirá-la também da coleção da pergunta carregada. O `cascade` sem
`orphanRemoval` faz o flush recriar a alternativa removida que ainda está na coleção.

O conteúdo semeado por migration usa UUID fixo, e os testes de `conteudo` criam as próprias
trilhas e as apagam por SQL, nunca alteram o conteúdo semeado.
