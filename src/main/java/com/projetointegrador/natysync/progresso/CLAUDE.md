# progresso

## Responsabilidade

Estado de cada integrante na trilha: vídeo assistido, aprovação do quiz e conclusão. O pacote corrige tentativas a partir do conteúdo de `trilha`, guarda seu histórico e determina a próxima atividade.

## Contratos

- `ProgressoAtividade`: fatos de vídeo assistido e conclusão, únicos por integrante e atividade.
- `TentativaQuiz` e `RespostaTentativa`: histórico completo da nota e escolhas feitas pelo integrante.
- `ProgressoService`: valida atividade desbloqueada, corrige as respostas e aplica nota mínima do quiz.
- `POST /api/v1/atividades/{atividadeId}/quiz/tentativas`: aceita uma resposta para cada pergunta; o cliente nunca envia nota.
- `POST /api/v1/atividades/{atividadeId}/video-assistido`: grava o vídeo e só conclui atividade sem quiz.
- `POST /api/v1/atividades/{atividadeId}/video-iniciado`: registra o primeiro início do vídeo, com `videoIniciadoEm` e `duracaoSegundos` na resposta.
- Uma tentativa aprovada conclui a atividade; reprovação não altera conclusão prévia nem apaga histórico.

## Decisões

A nota é a porcentagem de respostas corretas, arredondada para inteiro. Aprovação compara essa nota com `quiz.nota_minima`. Cada tentativa e suas respostas são persistidas na mesma transação.

A revisão retorna apenas nota, aprovação e identificadores de perguntas erradas. Não retorna a alternativa escolhida nem o gabarito. A leitura do quiz também usa `AlternativaResponse`, que não tem campo `correta`.

A validação exige todas as perguntas exatamente uma vez e uma alternativa pertencente à pergunta. Tentativa inválida retorna 400 e não grava histórico.

## Armadilhas

O integrante sempre vem da sessão. Não aceite identificador, nota, correção ou estado de progresso do cliente.

O desbloqueio é linear e continua obrigatório para tentativa e registro de vídeo. Repetir quiz depois da aprovação não move o instante da conclusão.

Vídeo configurado exige início registrado e tempo decorrido maior ou igual à duração.
Falta de início retorna 409 `VIDEO_NAO_INICIADO`; tempo insuficiente retorna 409
`VIDEO_TEMPO_INSUFICIENTE`; início em conteúdo sem vídeo retorna 409
`VIDEO_NAO_CONFIGURADO`. Conteúdo sem vídeo mantém o registro sem restrição temporal.
Repetir início preserva o carimbo; repetir assistência preserva assistência e conclusão.
Vídeo já assistido dispensa a conferência temporal.

As escritas de progresso travam a linha do integrante na mesma transação, inclusive
tentativas, para serializar a criação e atualização do registro único por atividade.
O servidor confere tempo decorrido, sem verificar reprodução efetiva no player.
