# gamificacao

## Responsabilidade

Pontos, sequência de dias de estudo, conquistas e ranking dos integrantes. O pacote reage
aos fatos de estudo do `progresso` e expõe a leitura do perfil e do ranking. Não escreve em
`usuario`, `empresa`, `trilha` nem `progresso`.

Depende de `progresso`, `trilha`, `usuario`, `empresa` e `shared`. Nenhum pacote depende
dele: a seta é só `gamificacao -> progresso`.

## Contratos

- `RecompensaDoEstudo`: ouvinte de `progresso/EstudoRegistrado`. Marca o dia de estudo,
  concede `atividade.xp` quando o fato é a primeira conclusão e avalia as conquistas,
  nessa ordem.
- `AvaliadorDeConquistas`: mede `IndicadoresDoIntegrante` e concede cada conquista
  pendente cujo indicador alcança o limiar, com o instante do fato.
- `SequenciaDeDias.calcular`: sequência atual, maior sequência e estudo no dia corrente, a
  partir das datas de estudo e da data corrente no fuso da empresa.
- `CriterioConquista`: `ATIVIDADES_CONCLUIDAS`, `MAIOR_NOTA_EM_QUIZ`, `MODULOS_CONCLUIDOS`,
  `TRILHAS_CONCLUIDAS`, `MAIOR_SEQUENCIA` e `PONTOS`. Cada valor lê o seu indicador.
- `PontoConcedido`, `DiaDeEstudo`, `Conquista` e `ConquistaObtida`: entidades de leitura
  das tabelas `ponto_concedido`, `dia_de_estudo`, `conquista` e `conquista_obtida`.
- `RankingRepository`: classificação da empresa num único trecho SQL, usado pela listagem
  e pela posição do perfil.
- `RankingController`: `GET /api/v1/ranking`, paginado, com `PosicaoRankingResponse`.
- `GamificacaoController`: `GET /api/v1/integrantes/{integranteId}/gamificacao`, com
  `GamificacaoResponse`.

## Decisões

O ouvinte usa `@EventListener` com `@Transactional(propagation = MANDATORY)`. O evento do
Spring é síncrono, então ponto, dia e conquista gravam na mesma transação da escrita de
progresso e sob a trava da linha do integrante. `@TransactionalEventListener` rodaria
depois do commit, fora da trava, e deixaria conclusão sem ponto quando falhasse.

O ponto é concedido uma única vez por integrante e atividade. O `progresso` só marca
`atividadeConcluida` na primeira conclusão, a trava serializa as chamadas e o índice único
`(usuario_id, atividade_id)` com `on conflict do nothing` é a última barreira. O `xp`
gravado é o da concessão: mudar `atividade.xp` não reescreve ponto dado.

O dia de estudo é gravado já convertido para a data local no fuso da empresa no instante
do estudo. Mudar o fuso da empresa não mexe em dia passado. Conta como estudo tentativa
de quiz, primeiro vídeo assistido e primeira conclusão; início de vídeo não conta.

A sequência atual conta dias seguidos terminando na data corrente ou na anterior, para a
chama não apagar antes de o dia acabar. Total, sequência e posição são calculados a cada
leitura.

As conquistas são conteúdo global, sem `empresa_id`, e entram por migration com critério e
limiar por linha. Conquista nova de critério existente é só uma linha de migration;
critério novo exige valor em `CriterioConquista` e na check `conquista_criterio_check`.
Conquista obtida não é retirada.

O ranking classifica toda conta ativa da empresa, de qualquer papel e inclusive sem ponto:
pontos decrescentes, depois o instante da última concessão (quem chegou primeiro ao total),
depois nome e identificador. A posição é ordinal e não se repete. A ordem enviada pelo
cliente é ignorada.

O perfil é lido para a própria conta e para colega ativo da mesma empresa. Outra empresa,
desativado e inexistente respondem o mesmo 404, para não confirmar identificador.

## Armadilhas

As tabelas de fato não têm `empresa_id`. A empresa vem de `usuario.empresa_id`; coluna
duplicada seria segunda verdade.

As inserções são `@Modifying` nativas com `on conflict do nothing`. Salvar a entidade pela
JPA ignoraria a idempotência e quebraria no índice único.

Conquista atingida por progresso gravado sem passar pelo ouvinte, como a carga da `V10`,
só sai no próximo estudo do integrante. A regra de conquista vive em Java, e repeti-la em
SQL criaria duas verdades.

`RankingRepository` usa `NamedParameterJdbcTemplate`, porque a consulta agrega sobre
`usuario` e não devolve entidade. A ordem mora só em `CLASSIFICACAO`; listagem e perfil
precisam continuar lendo o mesmo trecho.

Teste que altera `atividade.xp` do conteúdo semeado restaura o valor no fim. O conteúdo é
global e compartilhado por todas as classes de teste.
