# Desenho

## Context

Motivação em `proposal.md`. Contrato em `specs/gamificacao-integrante/spec.md` e
`specs/ranking-empresa/spec.md`.

`ProgressoService` concentra os três fatos que a gamificação observa: tentativa de quiz,
vídeo assistido e conclusão. Cada escrita de progresso abre transação própria e trava a
linha do integrante em `usuario` com `PESSIMISTIC_WRITE` antes de ler qualquer coisa.
`progresso_atividade.concluido_em` é gravado uma única vez por integrante e atividade.

`Empresa.getFusoHorario()` é sempre preenchido. `usuario_empresa_idx` indexa
`usuario.empresa_id`. `spring.data.web.pageable` fixa `pagina`, `tamanho`, padrão 20 e
máximo 100. `UsuarioRepository.buscarNaEmpresa` busca conta por identificador dentro de
uma empresa.

## Goals / Non-Goals

**Goals:**

- `progresso` não conhece `gamificacao`. A seta de dependência é só
  `gamificacao -> progresso`.
- Concessão de ponto, dia de estudo e conquista atômica com o fato de progresso que a
  causou: ou grava tudo, ou nada.
- Ordem do ranking definida num único lugar, usada pela listagem e pela posição do perfil.

**Non-Goals:**

- Ponto ou conquista no corpo das rotas de progresso. A celebração vem da leitura do perfil.
- Liga semanal, reinício periódico de ranking, congelamento de sequência e notificação.
- Rota de escrita de conquista. O catálogo entra por migration.
- Retirar ponto ou conquista de integrante desativado.

## Decisions

### Fato de estudo publicado como evento síncrono

`progresso` publica o record `progresso/EstudoRegistrado(usuarioId, empresaId,
atividadeId, ocorridoEm, atividadeConcluida)` pelo `ApplicationEventPublisher`, no máximo
um por chamada:

- `registrarTentativa`: sempre publica. `atividadeConcluida` verdadeiro só quando a
  tentativa grava `concluido_em` pela primeira vez.
- `registrarVideoAssistido`: publica quando grava `video_assistido_em` ou `concluido_em`
  pela primeira vez. Chamada repetida que não grava nada não publica.
- `registrarVideoIniciado`: não publica.

`gamificacao/RecompensaDoEstudo` escuta com `@EventListener` e
`@Transactional(propagation = MANDATORY)`. O evento do Spring é síncrono sem
`@EnableAsync`, então o ouvinte roda dentro da transação do `progresso`, protegido pela
trava da linha do integrante. `MANDATORY` faz o ouvinte falhar alto se alguém publicar fora
de transação.

Alternativa descartada: `progresso` chamar um serviço de `gamificacao` direto. Cria a seta
`progresso -> gamificacao`, e a avaliação de conquista precisa ler progresso, fechando
ciclo. Alternativa descartada: `@TransactionalEventListener` depois do commit. Uma falha na
gamificação deixa conclusão sem ponto, e o ouvinte roda fora da trava.

Consequência aceita: erro na gamificação desfaz a tentativa ou o registro de vídeo da mesma
chamada.

### Ordem dentro do ouvinte

1. Marca o dia de estudo: `ocorridoEm` convertido para a data local no fuso da empresa.
2. Se `atividadeConcluida`, concede `atividade.xp` com `concedido_em = ocorridoEm`.
3. Calcula os indicadores do integrante e concede as conquistas atingidas que ele não tem,
   com `obtida_em = ocorridoEm`.

A avaliação vem por último porque sequência e pontos dependem dos passos 1 e 2.

### Tabelas de fato, estado calculado na leitura

- `ponto_concedido(id, usuario_id, atividade_id, xp, concedido_em)`, único em
  `(usuario_id, atividade_id)`. Guarda o `xp` da concessão: mudar `atividade.xp` não
  reescreve ponto dado.
- `dia_de_estudo(id, usuario_id, dia date, registrado_em)`, único em `(usuario_id, dia)`.
  O dia é gravado já no fuso da empresa no instante do estudo.
- `conquista(id, codigo unique, nome, descricao, criterio, limiar, ordem unique)`, global,
  sem `empresa_id`, com check de `criterio` nos valores de `CriterioConquista` e
  `limiar > 0`.
- `conquista_obtida(id, usuario_id, conquista_id, obtida_em)`, único em
  `(usuario_id, conquista_id)`.

Toda FK para `usuario` com `on delete cascade`. FK para `atividade` e `conquista` sem
cascade: conteúdo com histórico não é removido. Nenhuma coluna `empresa_id` nas tabelas de
fato: a empresa vem de `usuario.empresa_id`, e o integrante não troca de empresa pelo
painel. Coluna duplicada seria segunda verdade.

Total de pontos, sequência e posição são calculados a cada leitura, como o estado da
trilha. Alternativa descartada: calcular pontos direto de `progresso_atividade` com
`atividade.xp`. Mudança de `xp` reescreveria o ranking passado e não existiria instante
próprio de concessão.

Alternativa descartada para a sequência: derivar dias das datas de progresso a cada
leitura. A consulta repetiria a conversão de fuso sobre três tabelas, e mudar o fuso da
empresa reescreveria sequência passada. A data gravada no momento do estudo é fato.

### Escrita idempotente pelo banco

As três inserções usam `insert ... on conflict do nothing` em `@Modifying` nativo dos
repositórios. A trava da linha do integrante já serializa as chamadas, e o índice único é a
segunda barreira. As entidades `PontoConcedido`, `DiaDeEstudo`, `Conquista` e
`ConquistaObtida` existem para leitura e para o `ddl-auto: validate`.

### Sequência como cálculo puro

`gamificacao/SequenciaDeDias.calcular(List<LocalDate> dias, LocalDate diaCorrente)`
devolve sequência atual, maior sequência e `diaCorrenteEstudado`. A atual conta dias
consecutivos terminando em `diaCorrente` ou em `diaCorrente - 1`; fora disso é zero. A
maior é a maior corrida da lista. `diaCorrente` é `LocalDate.now(fuso da empresa)`.

O cálculo mora em `gamificacao`, único consumidor. `shared/util/DataUtil` sai do projeto,
porque classe de um pacote só não é `shared`.

### Critério de conquista como enum, limiar como dado

`CriterioConquista` tem `ATIVIDADES_CONCLUIDAS`, `MAIOR_NOTA_EM_QUIZ`,
`MODULOS_CONCLUIDOS`, `TRILHAS_CONCLUIDAS`, `MAIOR_SEQUENCIA` e `PONTOS`. Cada valor lê o
seu número de um record `IndicadoresDoIntegrante`, calculado uma vez por evento.
`AvaliadorDeConquistas` concede quando `indicador >= limiar`.

Conquista nova de critério existente entra só por migration. Critério novo exige código.

As contagens sobre progresso moram em `progresso`, dono das tabelas:
`ProgressoAtividadeRepository` ganha contagem de atividades concluídas, de módulos e de
trilhas com toda atividade concluída (módulo ou trilha sem atividade não conta), e
`TentativaQuizRepository` ganha a maior nota do integrante.

Catálogo inicial em `V11`:

| ordem | código | critério | limiar |
|---|---|---|---|
| 1 | `PRIMEIRA_ATIVIDADE` | `ATIVIDADES_CONCLUIDAS` | 1 |
| 2 | `QUIZ_PERFEITO` | `MAIOR_NOTA_EM_QUIZ` | 100 |
| 3 | `MODULO_CONCLUIDO` | `MODULOS_CONCLUIDOS` | 1 |
| 4 | `TRILHA_CONCLUIDA` | `TRILHAS_CONCLUIDAS` | 1 |
| 5 | `SEQUENCIA_3_DIAS` | `MAIOR_SEQUENCIA` | 3 |
| 6 | `SEQUENCIA_7_DIAS` | `MAIOR_SEQUENCIA` | 7 |
| 7 | `PONTOS_50` | `PONTOS` | 50 |

### Ranking numa consulta com `row_number`

`RankingRepository` guarda um único trecho SQL de classificação:

```sql
select u.id, u.nome, coalesce(sum(p.xp), 0) as pontos,
       row_number() over (
         order by coalesce(sum(p.xp), 0) desc, max(p.concedido_em) asc nulls last,
                  u.nome asc, u.id asc) as posicao
from usuario u
left join ponto_concedido p on p.usuario_id = u.id
where u.empresa_id = :empresaId and u.ativo
group by u.id, u.nome
```

A listagem lê esse trecho ordenado por `posicao` com `limit` e `offset`, e a contagem é
`count(*)` das contas ativas da empresa. O perfil lê o mesmo trecho filtrado pelo
identificador. Uma ordem só faz a posição do perfil bater com a do ranking.

`max(concedido_em)` é o instante em que o integrante chegou ao total atual, porque ponto só
cresce. Conta sem ponto fica com `null`, que vai para o fim, e cai no desempate por nome.

`RankingService` monta `PageRequest.of(pagina, tamanho)` sem ordenação, ignorando `sort`
do cliente, como a listagem do painel. A empresa vem de `IntegranteDaRequisicao`.

### Rotas e corpos

- `RankingController`: `GET /api/v1/ranking` devolve
  `PaginaResponse<PosicaoRankingResponse(posicao, integranteId, nome, pontos)>`.
- `GamificacaoController`: `GET /api/v1/integrantes/{integranteId}/gamificacao` devolve
  `GamificacaoResponse(integranteId, nome, pontos, posicaoNoRanking,
  SequenciaResponse(atual, maior, diaCorrenteEstudado),
  List<ConquistaResponse(codigo, nome, descricao, obtidaEm)>)`.

O alvo do perfil é lido por `UsuarioRepository.buscarNaEmpresa(integranteId, empresa do
token)`. Inexistente, de outra empresa ou desativado lançam `RecursoNaoEncontradoException`
com a mesma mensagem, para o 404 não confirmar que o identificador existe. As duas rotas
caem no `anyRequest().authenticated()` de `SecurityConfig`, sem papel.

### Carga do progresso gravado

`V10` cria as tabelas e carrega:

- `ponto_concedido` a partir de `progresso_atividade` com `concluido_em`, usando
  `atividade.xp` e `concedido_em = concluido_em`;
- `dia_de_estudo` a partir de `concluido_em`, `video_assistido_em` e
  `tentativa_quiz.criada_em`, convertidos por `(instante at time zone
  empresa.fuso_horario)::date`.

Conquista não é carregada em SQL: a regra dela vive em Java, e duplicá-la na migration
cria duas verdades. A avaliação do próximo estudo concede o que o histórico atingiu.

## Risks / Trade-offs

- [Falha na gamificação desfaz a ação de estudo] Atomicidade é deliberada. Testes de
  integração cobrem cada caminho de publicação.
- [Ouvinte faz seis consultas de indicador a cada estudo] Todas por `usuario_id`
  indexado, dentro de transação já aberta. Volume de uma pessoa estudando é baixo.
- [Ranking agrega a empresa inteira a cada consulta] Regra aceita em `docs/regras.md`.
  Índices `usuario_empresa_idx` e o único de `ponto_concedido` iniciando em `usuario_id`
  sustentam o `join`.
- [Data gravada congela o fuso do instante do estudo] Mudar o fuso da empresa não mexe em
  dia passado. É o comportamento desejado para fato.
- [Conquista do histórico só aparece no próximo estudo] Perfil mostra pontos e sequência
  certos na hora; a conquista chega na primeira ação seguinte.
- [Teste de fuso depende do relógio real] O teste escolhe, no instante da execução, um
  fuso cuja data local difere da data UTC (`Pacific/Kiritimati` ou `Pacific/Pago_Pago`) e
  compara com a data gravada.

## Migration Plan

1. `V10__pontuacao_sequencia_e_conquista.sql`: tabelas, índices, constraints e carga.
2. `V11__seed_conquistas.sql`: catálogo inicial.
3. Subida normal: o Flyway aplica antes do `validate`.

Reverter o código deixa as tabelas sem uso, e nenhuma entidade antiga as mapeia. Migration
aplicada não é editada: correção de catálogo entra em migration nova.
