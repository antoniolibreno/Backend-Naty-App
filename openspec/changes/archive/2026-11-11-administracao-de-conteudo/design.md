# Design

## Pacote `conteudo`

Pacote novo, único a escrever em `trilha`, `modulo` e `atividade`, no mesmo desenho em que
`painel` é o único a escrever em `usuario` e `empresa`. `trilha` segue como leitura do
conteúdo e não conhece `conteudo`. O pacote nasce com `CLAUDE.md`. Contém controllers,
`ConteudoService`, `ReordenacaoService`, `AuditoriaDeConteudo`, a entidade
`AlteracaoDeConteudo`, o repositório dela, DTOs de entrada e de saída e o mapper.

Escrever nas entidades de `trilha` usa os setters existentes. `Atividade.setVideoUrl` já
valida o vídeo com `UrlVideo`, mas lança `IllegalArgumentException`, que o handler global não
trata. A validação de campo acontece antes, no DTO de entrada, para sair como
`FALHA_DE_VALIDACAO`.

## Rotas e segurança

`SecurityConfig` ganha matchers `hasRole(NATY)` antes de `anyRequest()`:
`POST /api/v1/trilhas`, `PUT|DELETE /api/v1/trilhas/*`, `PUT /api/v1/trilhas/*/ativa`,
`PUT /api/v1/trilhas/*/modulos/ordem`, `/api/v1/modulos/**` em todo método,
`POST /api/v1/atividades`, `PUT|DELETE /api/v1/atividades/*` e
`/api/v1/alteracoes-de-conteudo/**`. O padrão de um segmento (`*`) preserva
`POST /api/v1/atividades/{id}/video-assistido` e `/quiz/tentativas`, que seguem abertas ao
integrante. As rotas `GET` de trilha e atividade não casam com nenhum matcher novo.

## DTOs de entrada

Records `TrilhaRequest`, `ModuloRequest`, `AtividadeRequest`, `AtivacaoRequest` e
`OrdemRequest`. `AtividadeRequest` valida com `@AssertTrue` no estilo de `SenhaRequest`: vídeo
aceito por `UrlVideo`, e duração positiva quando há vídeo. O XP é `Integer` com `@PositiveOrZero`
e `10` quando nulo. Respostas: `TrilhaAdminResponse` (com `ativa`), `ModuloResponse` e
`AtividadeDetalheResponse` existentes, reaproveitados.

## Ordem

Criação define `ordem = max(ordem dos irmãos) + 1`, ou 1 sem irmãos. A reordenação trava o pai
com `PESSIMISTIC_WRITE`, confere que a lista é exatamente o conjunto de irmãos e renumera em
duas passagens dentro da transação: primeiro soma um deslocamento grande a todos, depois grava
1 a n. Isso evita colisão no índice único `(pai, ordem)`, que não é postergável. Divergência
lança `ConflitoException("ORDEM_INVALIDA", ...)`. A ordem de trilhas fica definida só na
criação, porque o índice de trilha não é único e a reordenação de trilhas não foi pedida.

## Ativação

`trilha.ativa` é a única coluna de ativação. Módulo e atividade não ganham coluna: o histórico
é protegido por recusa de remoção, e a desativação acontece na trilha. `GET /api/v1/trilhas/{id}`
segue devolvendo a trilha inativa, porque o progresso e as telas de histórico a leem.

## Proteção do histórico

Remover trilha, módulo ou atividade consulta, por consulta nativa em `AtividadeRepository`, se
algum id de atividade atingido aparece em `progresso_atividade`, `ponto_concedido` ou, pelo
quiz, `tentativa_quiz`. A consulta é por nome de tabela e não por classe, porque `conteudo` e
`trilha` não podem depender de `progresso`. Existindo, lança
`ConflitoException("CONTEUDO_COM_PROGRESSO", ...)` antes de remover. Sem histórico, a remoção
usa o `cascade` das entidades, que já alcança módulos, atividades, quiz, perguntas e
alternativas.

## Auditoria

Migration cria `alteracao_conteudo` com `id`, `usuario_id` com FK para `usuario` e
`on delete cascade`, `entidade`, `entidade_id`, `acao` e `ocorrida_em`, e índice por
`(entidade_id, ocorrida_em desc)`. Não há FK para o conteúdo, para o registro de uma remoção
sobreviver a ela. O cascade segue a regra do projeto para FK nova em `usuario`. Entidades:
`TRILHA`, `MODULO`, `ATIVIDADE`, `QUIZ`, `PERGUNTA`, `ALTERNATIVA`. Ações: `CRIACAO`,
`ALTERACAO`, `REMOCAO`, `ATIVACAO`, `DESATIVACAO`, `REORDENACAO`. `AuditoriaDeConteudo.registrar`
roda na transação do serviço, então alteração recusada reverte também o registro. A conta vem
de `IntegranteDaRequisicao`.

## Regras de dependência

`RegrasDeDependenciaTest` ganha duas regras: só `conteudo` escreve nas entidades e nos
repositórios de `trilha`, e `*Response` fora de `conteudo` não declara campo `correta`. A
segunda é a regra atual, estreitada para permitir a resposta administrativa de gabarito que a
change `administracao-de-quiz` cria.

## Decisões

- Criar sempre no fim e reordenar em operação própria evita colisão de ordem na criação e na
  edição.
- Auditoria em tabela própria, e não colunas `atualizado_por` nas seis tabelas, porque registra
  remoções e histórico completo e não só o último autor.
- A leitura das alterações existe para o registro servir ao propósito de descobrir quem trocou
  o gabarito quando as notas caírem.

## Riscos

- Conteúdo é global, então um erro de edição afeta todas as empresas. A restrição a `NATY` e a
  auditoria são a mitigação.
- A consulta nativa de progresso cria acoplamento por nome de tabela. O `CLAUDE.md` de
  `conteudo` registra o limite aceito.
