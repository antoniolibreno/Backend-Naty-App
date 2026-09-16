# trilha

## Responsabilidade

Conteúdo do treinamento e a leitura dele. Trilha, módulo, atividade, quiz, pergunta e
alternativa. Este pacote não sabe nada sobre quem está estudando: progresso, desbloqueio e
pontuação são de outros pacotes.

## Contratos

- Entidades `Trilha`, `Modulo`, `Atividade`, `Quiz`, `Pergunta` e `Alternativa`,
  encadeadas por ordem crescente em cada nível.
- `TrilhaRepository`, `AtividadeRepository` e `QuizRepository`.
- `TrilhaService`: leitura de trilha, atividade e quiz. Lança
  `RecursoNaoEncontradoException` quando não acha.
- `TrilhaController`: `GET /api/v1/trilhas` e `GET /api/v1/trilhas/{trilhaId}`.
- `AtividadeController`: `GET /api/v1/atividades/{atividadeId}` e
  `GET /api/v1/atividades/{atividadeId}/quiz`.
- `TrilhaMapper` e `QuizMapper`: MapStruct de entidade para DTO.

## Decisões

Conteúdo é global. Nenhuma tabela deste pacote tem `empresa_id`, porque todas as empresas
fazem o mesmo treinamento sobre o mesmo produto.

O DTO de alternativa simplesmente não tem o campo `correta`. Não existe anotação de
ocultação, não existe filtro de serialização. Um campo que não existe no record não tem
como vazar, enquanto uma anotação esquecida em campo novo vazaria em silêncio.

As coleções usam `@OrderBy("ordem asc")` mais `@BatchSize`. `@BatchSize` resolve o N mais 1
sem cair em `MultipleBagFetchException`, que é o que aconteceria com dois níveis de
`join fetch` sobre `List`.

Ordem é coluna explícita, com índice único no par pai mais ordem. Ordem implícita por data
de criação ou por identificador quebra no primeiro conteúdo reordenado.

## Armadilhas

Nunca exponha a entidade `Alternativa` diretamente em uma resposta, nem adicione `correta`
a `AlternativaResponse`. O gabarito vazado inutiliza a correção de quiz inteira. Existe
teste que lê o corpo da resposta como texto e falha se a palavra aparecer.

`nota_minima` fica no quiz, não numa constante em Java. Ela é informada ao cliente e é a
base da correção no servidor. Duplicar esse valor em código cria duas verdades.

O conteúdo em banco é exemplo, semeado por `V3__seed_conteudo_exemplo.sql`, com UUID fixo
escrito à mão: uma trilha, dois módulos, três atividades por módulo, quatro perguntas por
quiz e quatro alternativas por pergunta. A descrição semeada da trilha anuncia ao app que
ela é exemplo. A troca pelo conteúdo verdadeiro entra em migration nova que apaga essas
linhas pelos UUIDs fixos. Não edite `V3`: o Flyway valida o checksum do que já rodou.

## Ausências deliberadas

Este pacote não escreve conteúdo. Não existe endpoint de criação ou edição: o CRUD
administrativo pertence à proposta do painel, junto do papel que o protege.

A leitura de conteúdo exige token. O conteúdo continua global e idêntico para todas as
empresas: o que a sessão decide é se o servidor responde, não o que ele responde.

`Atividade.videoUrl` é nulo em todo o conteúdo semeado, e a atividade mostra só a imagem.
