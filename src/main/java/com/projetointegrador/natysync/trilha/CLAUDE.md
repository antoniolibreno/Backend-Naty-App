# trilha

## Responsabilidade

Conteúdo global do treinamento, suas leituras e sua administração autenticada. Progresso e identidade pertencem a outros pacotes.

## Contratos

O pacote expõe a leitura de trilha, atividade e quiz para integrantes autenticados. `TrilhaMapper` e `QuizMapper` convertem entidade para DTOs públicos. `AlternativaResponse` não possui o campo `correta`.

As entidades são `Trilha`, `Modulo`, `Atividade`, `Quiz`, `Pergunta` e `Alternativa`, encadeadas em ordem crescente. As coleções ordenadas usam `@OrderBy` e `@BatchSize` para evitar N mais 1 sem múltiplos fetches de listas.

`TrilhaController` lê `/api/v1/trilhas`; `AtividadeController` lê `/api/v1/atividades`. `AdministracaoConteudoController` escreve nesses recursos e nos recursos `/api/v1/modulos`, `/api/v1/quizzes`, `/api/v1/perguntas` e `/api/v1/alternativas`. As rotas de escrita exigem o papel `NATY`.

## Decisões

Somente NATY escreve conteúdo. Leitura de quiz não devolve o gabarito; DTOs de escrita e leitura de alternativa são tipos distintos. Alterações administrativas registram autor e instante em `auditoria_conteudo`.

Conteúdo com progresso ou tentativa associada não pode ser removido. Trilha inativa não aparece na listagem pública. Reordenação deve preservar a unicidade da ordem por pai.

O catálogo semeado em `V3__seed_conteudo_exemplo.sql` usa UUIDs explícitos. Migration já aplicada não é editada: troca do catálogo depende de uma migration posterior.

## Armadilhas

Nunca exponha a entidade `Alternativa` em uma resposta. `nota_minima` pertence ao quiz. A regra de conclusão e o estado por integrante pertencem ao pacote `progresso`, que não deve virar dependência de `trilha`.

## Validações

Atividade com vídeo exige URL HTTPS aceita por `UrlVideo` e duração positiva. `UrlVideo` aceita `youtube.com`, `www.youtube.com`, `m.youtube.com` e `youtu.be`, sem credencial ou porta na URL. Cada pergunta possui exatamente uma alternativa correta.
