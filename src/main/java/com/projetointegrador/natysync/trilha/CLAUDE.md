# trilha

## Responsabilidade

Conteúdo do treinamento e a leitura dele. Trilha, módulo, atividade, quiz, pergunta e
alternativa. Este pacote não sabe nada sobre quem está estudando: progresso,
desbloqueio e pontuação são de outros pacotes.

## Contratos

- Entidades `Trilha`, `Modulo`, `Atividade`, `Quiz`, `Pergunta` e `Alternativa`,
  encadeadas por ordem crescente em cada nível.
- `TrilhaRepository`, `AtividadeRepository` e `QuizRepository`.
- `TrilhaService`: leitura de trilha, atividade e quiz. Lanca
  `RecursoNaoEncontradoException` quando não acha.
- `TrilhaController`: `GET /api/v1/trilhas` e `GET /api/v1/trilhas/{trilhaId}`.
- `AtividadeController`: `GET /api/v1/atividades/{atividadeId}` e
  `GET /api/v1/atividades/{atividadeId}/quiz`.
- `TrilhaMapper` e `QuizMapper`: MapStruct de entidade para DTO.

## Decisões

Conteúdo é global. Nenhuma tabela deste pacote tem `empresa_id`, porque todas as
empresas fazem o mesmo treinamento sobre o mesmo produto.

O DTO de alternativa simplesmente não tem o campo `correta`. Não existe anotação de
ocultação, não existe filtro de serialização. Um campo que não existe no record não
tem como vazar, enquanto uma anotação esquecida em campo novo vazaria em silencio.

As coleções usam `@OrderBy("ordem asc")` mais `@BatchSize`. `@BatchSize` resolve o
N mais 1 sem cair em `MultipleBagFetchException`, que é o que aconteceria com dois
níveis de `join fetch` sobre `List`.

Ordem é coluna explícita, com índice único no par pai mais ordem. Ordem implícita por
data de criação ou por identificador quebra no primeiro conteúdo reordenado.

## Armadilhas

Nunca exponha a entidade `Alternativa` diretamente em uma resposta, nem adicione
`correta` a `AlternativaResponse`. O gabarito vazado inutiliza a correção de quiz
inteira. Existe teste que lê o corpo da resposta como texto e falha se a palavra
aparecer.

`nota_minima` fica no quiz, não numa constante em Java. Ela é informada ao cliente e
será usada pela correção no servidor. Duplicar esse valor em código cria duas verdades.

Este pacote não escreve conteúdo. Não existe endpoint de criação ou edição, e isso é
deliberado: sem autenticação, um CRUD administrativo ficaria aberto na internet. CRUD
e login nascem no mesmo diff.

## Estado atual

Real: todas as entidades, repositórios, mappers, serviço e os dois controllers.

O conteúdo em banco é exemplo, semeado por `V3__seed_conteudo_exemplo.sql`, com UUID
fixo escrito na mão. Uma trilha, dois módulos, três atividades por módulo, quatro
perguntas por quiz e quatro alternativas por pergunta. Quando o conteúdo verdadeiro da
Naty chegar, ele entra em uma migration nova que apaga essas linhas pelos UUIDs fixos
e insere as reais. Não edite `V3`: o Flyway valida o checksum do que já rodou.

`Atividade.videoUrl` é sempre nulo hoje. A atividade mostra só a imagem. Hospedagem e
reprodução de vídeo são decisão de etapa futura.
