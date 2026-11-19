# Design

## Endpoint

`ProgressoController` ganha `GET /api/v1/integrantes/{integranteId}/progresso`, no mesmo
prefixo `/api/v1` do controller. `IntegranteDaRequisicao` traz a empresa de quem chama, e o
`integranteId` do caminho é o integrante lido.

## Acesso

`ProgressoService.resumir(integranteId, empresaId)` resolve o integrante com
`UsuarioRepository.buscarNaEmpresa(integranteId, empresaId)` filtrado por `isAtivo`, a mesma
consulta do perfil de gamificação. Outra empresa, desativado e inexistente caem no mesmo
`RecursoNaoEncontradoException`, o que não distingue os casos para o cliente. `progresso` só
lê `usuario`, então a regra de que só `painel` escreve continua valendo.

## Cálculo

O serviço carrega as trilhas com `TrilhaRepository.findAllByAtivaTrueOrderByOrdemAsc()` e o
progresso do integrante lido com `progressoAtividadeRepository.findByUsuarioId(integranteId)`,
indexado por atividade. Para cada trilha, `TrilhaProgressoMontador.sequenciar` devolve o
total, as concluídas e a próxima atividade, que é a regra única de desbloqueio linear. O
resumo soma total e concluídas, e a próxima atividade é a primeira não nula na ordem das
trilhas. Reusar o montador impede que o resumo e a leitura da trilha divirjam.

O percentual repete a divisão inteira de `SequenciaDaTrilha.percentualConcluido`, aplicada ao
total somado. Atividade concluída de trilha inativa não entra, porque a contagem parte das
sequências das trilhas ativas.

## Resposta

`ResumoProgressoResponse(UUID integranteId, int totalAtividades, int atividadesConcluidas,
int percentualConcluido, ProximaAtividadeResponse proximaAtividade)` e
`ProximaAtividadeResponse(UUID atividadeId, UUID trilhaId, String titulo)`, ambos em
`progresso/dto`. A atividade traz o `trilhaId` para o app abrir a trilha certa. Módulos e
atividades não saem.

## Decisões

Somar todas as trilhas, em vez de listar uma por trilha, serve o "continuar de onde parou"
sem o app escolher trilha. Com uma só trilha, a soma coincide com o resumo dessa trilha.

A leitura de colega espelha o perfil de gamificação, para a tela de ranking abrir o
progresso de quem aparece nela.

## Riscos

- Carregar as trilhas inteiras para contar atividades custa uma consulta por trilha com
  módulos e atividades. O conteúdo é pequeno e global, então o custo é aceito.
