# trilha

## Responsabilidade

Conteudo do treinamento e a leitura dele. Trilha, modulo, atividade, quiz, pergunta e
alternativa. Este pacote nao sabe nada sobre quem esta estudando: progresso,
desbloqueio e pontuacao sao de outros pacotes.

## Contratos

- Entidades `Trilha`, `Modulo`, `Atividade`, `Quiz`, `Pergunta` e `Alternativa`,
  encadeadas por ordem crescente em cada nivel.
- `TrilhaRepository`, `AtividadeRepository` e `QuizRepository`.
- `TrilhaService`: leitura de trilha, atividade e quiz. Lanca
  `RecursoNaoEncontradoException` quando nao acha.
- `TrilhaController`: `GET /api/v1/trilhas` e `GET /api/v1/trilhas/{trilhaId}`.
- `AtividadeController`: `GET /api/v1/atividades/{atividadeId}` e
  `GET /api/v1/atividades/{atividadeId}/quiz`.
- `TrilhaMapper` e `QuizMapper`: MapStruct de entidade para DTO.

## Decisoes

Conteudo e global. Nenhuma tabela deste pacote tem `empresa_id`, porque todas as
empresas fazem o mesmo treinamento sobre o mesmo produto.

O DTO de alternativa simplesmente nao tem o campo `correta`. Nao existe anotacao de
ocultacao, nao existe filtro de serializacao. Um campo que nao existe no record nao
tem como vazar, enquanto uma anotacao esquecida em campo novo vazaria em silencio.

As colecoes usam `@OrderBy("ordem asc")` mais `@BatchSize`. `@BatchSize` resolve o
N mais 1 sem cair em `MultipleBagFetchException`, que e o que aconteceria com dois
niveis de `join fetch` sobre `List`.

Ordem e coluna explicita, com indice unico no par pai mais ordem. Ordem implicita por
data de criacao ou por identificador quebra no primeiro conteudo reordenado.

## Armadilhas

Nunca exponha a entidade `Alternativa` diretamente em uma resposta, nem adicione
`correta` a `AlternativaResponse`. O gabarito vazado inutiliza a correcao de quiz
inteira. Existe teste que le o corpo da resposta como texto e falha se a palavra
aparecer.

`nota_minima` fica no quiz, nao numa constante em Java. Ela e informada ao cliente e
sera usada pela correcao no servidor. Duplicar esse valor em codigo cria duas verdades.

Este pacote nao escreve conteudo. Nao existe endpoint de criacao ou edicao, e isso e
deliberado: sem autenticacao, um CRUD administrativo ficaria aberto na internet. CRUD
e login nascem no mesmo diff.

## Estado atual

Real: todas as entidades, repositorios, mappers, servico e os dois controllers.

O conteudo em banco e exemplo, semeado por `V3__seed_conteudo_exemplo.sql`, com UUID
fixo escrito na mao. Uma trilha, dois modulos, tres atividades por modulo, quatro
perguntas por quiz e quatro alternativas por pergunta. Quando o conteudo verdadeiro da
Naty chegar, ele entra em uma migration nova que apaga essas linhas pelos UUIDs fixos
e insere as reais. Nao edite `V3`: o Flyway valida o checksum do que ja rodou.

`Atividade.videoUrl` e sempre nulo hoje. A atividade mostra so a imagem. Hospedagem e
reproducao de video sao decisao de etapa futura.
