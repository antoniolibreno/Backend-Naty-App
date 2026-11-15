# gamificacao

Este pacote concentra XP, nivel, sequencia, conquistas e ranking. O XP nasce somente de
`atividade.xp` e e lancado em `xp_atividade`, cuja chave unica por integrante e atividade
impede pontuacao duplicada.

Conclusao de atividade e tentativa aprovada de quiz passam pelo `GamificacaoService` na
mesma transacao do progresso. A sequencia conta dias de conclusao no fuso da empresa;
eventos ficam em UTC e o dia local e persistido para evitar depender do fuso do servidor.

Conquistas sao catalogo global semeado por migration e a concessao e unica por integrante.
O ranking filtra a empresa da identidade, exclui integrantes inativos, ordena por XP
decrescente, momento em que a pontuacao atual foi atingida e nome, e sempre e paginado.
