# Tarefas

## 1. Schema e pacote

- [x] 1.1 Escrever `V10__pontuacao_sequencia_e_conquista.sql` com `ponto_concedido`, `dia_de_estudo`, `conquista` e `conquista_obtida`, índices únicos, checks, FK para `usuario` com `on delete cascade` e a carga de pontos e dias a partir de `progresso_atividade` e `tentativa_quiz`; verificar com `./mvnw -B verify` subindo o contexto sem erro de Flyway
- [x] 1.2 Escrever `V11__seed_conquistas.sql` com as sete conquistas do catálogo e teste de integração que lê o catálogo em ordem pelos códigos
- [x] 1.3 Criar o pacote `gamificacao` com `PontoConcedido`, `DiaDeEstudo`, `Conquista`, `ConquistaObtida`, `CriterioConquista` e os repositórios com inserção `on conflict do nothing`; verificar que o `ddl-auto: validate` aceita as entidades no `./mvnw -B verify`
- [x] 1.4 Escrever `gamificacao/CLAUDE.md` com responsabilidade, contratos, a seta `gamificacao -> progresso`, ouvinte síncrono na transação do progresso, fuso da sequência, ponto idempotente e ordem do ranking; verificar ausência de travessão e de marcador temporal com `grep`

## 2. Fato de estudo no progresso

- [x] 2.1 Criar `progresso/EstudoRegistrado` e publicá-lo em `registrarTentativa` e `registrarVideoAssistido` conforme `design.md`, sem publicar em `registrarVideoIniciado` nem em chamada repetida; verificar com teste que captura os eventos publicados em cada caminho
- [x] 2.2 Adicionar a `ProgressoAtividadeRepository` as contagens de atividades, módulos e trilhas concluídos, e a `TentativaQuizRepository` a maior nota do integrante; verificar com teste de integração que módulo e trilha sem atividade não contam
- [x] 2.3 Atualizar `progresso/CLAUDE.md` com a publicação do fato e a consequência de o ouvinte desfazer a ação; verificar com `grep` de travessão

## 3. Ponto e dia de estudo

- [x] 3.1 Implementar `RecompensaDoEstudo` com `@EventListener` e `@Transactional(propagation = MANDATORY)`, marcando o dia no fuso da empresa e concedendo `atividade.xp` na primeira conclusão; verificar com teste de integração de conclusão por quiz aprovado e por vídeo em atividade sem quiz
- [x] 3.2 Testar que vídeo em atividade com quiz, tentativa reprovada e início de vídeo não pontuam, e que quiz refeito, vídeo repetido e duas tentativas aprovadas concorrentes não somam de novo
- [x] 3.3 Testar que mudar `atividade.xp` depois da conclusão não muda o ponto concedido
- [x] 3.4 Testar o dia de estudo: tentativa reprovada marca dia, início de vídeo não marca, vários estudos no mesmo dia geram uma linha, e a data gravada é a data local de um fuso escolhido no instante do teste entre `Pacific/Kiritimati` e `Pacific/Pago_Pago`, o que difere da data UTC

## 4. Sequência e conquistas

- [x] 4.1 Implementar `SequenciaDeDias.calcular` e testes unitários para lista vazia, estudo no dia corrente, sequência viva sem estudo no dia corrente, quebra, lacuna no meio e maior sequência antiga
- [x] 4.2 Implementar `IndicadoresDoIntegrante` e `AvaliadorDeConquistas` chamados por último em `RecompensaDoEstudo`, com `obtida_em` do fato; verificar com teste de `PRIMEIRA_ATIVIDADE` concedida uma vez e mantendo o instante original
- [x] 4.3 Testar `QUIZ_PERFEITO`, `MODULO_CONCLUIDO`, `TRILHA_CONCLUIDA` e `PONTOS_50` percorrendo a trilha semeada, e `PONTOS_50` ausente com 40 pontos
- [x] 4.4 Testar `SEQUENCIA_3_DIAS` com dias anteriores inseridos por SQL e estudo no dia corrente, sem `SEQUENCIA_7_DIAS`, e a conquista preservada depois da quebra
- [x] 4.5 Testar que progresso gravado por SQL sem passar pela avaliação recebe `PRIMEIRA_ATIVIDADE` na tentativa seguinte

## 5. Ranking

- [x] 5.1 Implementar `RankingRepository` com o trecho único de classificação, `RankingService` e `RankingController` em `GET /api/v1/ranking`; verificar com teste de integração da ordem por pontos
- [x] 5.2 Testar desempate por última concessão, conta sem ponto no fim por nome, `ADMIN` sem ponto presente, desativado ausente com pontos gravados e empresa vizinha ausente
- [x] 5.3 Testar paginação: padrão 20, tamanho 500 limitado a 100, posições 3 e 4 na segunda página de tamanho 2, empate sem posição repetida, `sort` ignorado e 401 sem token

## 6. Perfil de gamificação

- [x] 6.1 Implementar `GamificacaoService` e `GamificacaoController` em `GET /api/v1/integrantes/{integranteId}/gamificacao`; verificar com teste do perfil vazio e do perfil com `PRIMEIRA_ATIVIDADE` obtida e as demais nulas
- [x] 6.2 Testar que `posicaoNoRanking` do perfil é a mesma posição de `GET /api/v1/ranking`
- [x] 6.3 Testar colega ativo devolvido, outra empresa, colega desativado e identificador inexistente com o mesmo 404 `RECURSO_NAO_ENCONTRADO`, e 401 sem token
- [x] 6.4 Conferir no Swagger em `/v3/api-docs` as duas rotas sob a tag de gamificação, com texto sem marcador temporal

## 7. Limpeza e documentação

- [x] 7.1 Remover `shared/util/DataUtil` e a linha dele em `shared/CLAUDE.md`; verificar com `./mvnw -B verify`
- [x] 7.2 Atualizar os limites e a lista de pacotes no `CLAUDE.md` raiz, o `README.md` e o `docs/backlog.md` (escopo em funcionamento, migrations até `V11`, itens 6.1 a 6.10 e 10.6 marcados); verificar com `git diff | grep -n "$(python3 -c 'print(chr(8212))')"` sem saída

## 8. Verificação integrada

- [x] 8.1 Rodar `./mvnw -B spotless:apply` e `./mvnw -B verify` com todos os testes passando
- [x] 8.2 Subir com `docker compose up --build -d`, concluir uma atividade com `user@user.com` e conferir ponto, sequência, conquista e ranking pelo Swagger
- [x] 8.3 Rodar `npx --yes @fission-ai/openspec@latest validate gamificacao --strict` sem erro

## Workflow follow-up

- Arquivar a mudança depois da revisão, sincronizando `gamificacao-integrante` e `ranking-empresa` em `openspec/specs/`.
