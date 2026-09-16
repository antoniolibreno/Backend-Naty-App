# progresso

## Responsabilidade

Estado de cada integrante na trilha: o que ele assistiu, o que concluiu e o que não abriu.
É o pacote que faz a trilha avançar nó a nó e a fonte de leitura da Home do app.

Este pacote lê `trilha` e `usuario`. Nenhum dos dois o conhece, e essa seta não se inverte:
`trilha` não sabe quem estuda e `usuario` não sabe o que foi estudado.

## Contratos

- `ProgressoAtividade`: entidade mapeada em `progresso_atividade`, uma linha por par de
  integrante e atividade.
- `EstadoAtividade`: `BLOQUEADO`, `DISPONIVEL` e `CONCLUIDO`.
- `ProgressoAtividadeRepository`: busca por integrante e por integrante mais atividade.
- `SequenciaDaTrilha`: resultado do cálculo de estados, com a próxima atividade, o total e
  o número de concluídas.
- `TrilhaProgressoMontador`: achata a trilha em ordem, calcula os estados e monta os DTOs.
- `ProgressoService`: aplica as regras. Lança `RecursoNaoEncontradoException` e
  `AtividadeBloqueadaException`.
- `ProgressoController`: `GET /api/v1/trilhas/{trilhaId}/progresso` e
  `POST /api/v1/atividades/{atividadeId}/video-assistido`.
- Migration `V4__progresso_atividade.sql`.

## Decisões

Estado é calculado a cada leitura, não guardado. A tabela guarda fatos, quando o vídeo foi
assistido e quando a atividade foi concluída. Guardar o estado criaria uma segunda verdade
que precisa ser reescrita em todas as linhas seguintes a cada conclusão e que fica errada
em silêncio quando o conteúdo for reordenado.

Os dois endpoints moram aqui, não em `TrilhaController`, apesar do caminho começar com
`/trilhas`. Pacote é por funcionalidade, e colocá-los em `trilha` faria aquele pacote
depender deste.

Os DTOs são próprios deste pacote e repetem título, descrição e ordem que já existem em
`trilha/dto`. A duplicação é o preço da direção da dependência: adicionar campo de estado a
`AtividadeResumoResponse` inverteria a seta.

Sem MapStruct. Estado, percentual e próxima atividade são calculados a partir da sequência
inteira, não copiados campo a campo.

A leitura da Home devolve total, concluídas, percentual e próxima atividade no mesmo
resultado. O app desenha a tela inteira com uma chamada só.

Os carimbos são gravados em UTC, para a mesma gravação não sair com offset local na
resposta imediata e com `Z` depois de passar pelo banco.

O integrante da requisição chega no cabeçalho `X-Integrante-Id`, resolvido em
`usuario/IntegranteArgumentResolver`. Trocar esse mecanismo por autenticação mexe só no
resolvedor: nenhum endpoint deste pacote é reescrito.

## Armadilhas

O cabeçalho `X-Integrante-Id` é forjável. Nenhuma verificação de identidade acontece, e
qualquer um registra progresso em nome de outro.

Assistir o vídeo conclui a atividade, tenha ela quiz ou não. A regra tem gatilho único
porque não existe tentativa de quiz: exigir aprovação sem ela travaria a trilha no primeiro
nó, já que as seis atividades semeadas têm quiz.

A chave estrangeira para `usuario` apaga em cascata, e isso não é enfeite. O seed de
desenvolvimento `R__seed_empresa_exemplo.sql` executa `delete from usuario` antes de
reinserir, e os testes de sessão criam e apagam integrantes. Sem a cascata, a restrição
bloqueia esses deletes e a aplicação para de subir em desenvolvimento assim que existir um
progresso gravado.

A unicidade é o par `(usuario_id, atividade_id)`. Registrar vídeo assistido duas vezes
atualiza a mesma linha e não move o momento da conclusão. Remover esse índice transforma
repetição em linha duplicada e o percentual passa de cem.

A tabela não tem `empresa_id`. O isolamento vem do integrante, que já pertence a uma
empresa. A coluna denormalizada entra com a migration do acompanhamento, que é quem precisa
varrer progresso por empresa.

Progresso em atividade bloqueada é recusado com conflito, nunca aceito em silêncio. Sem
essa recusa o app pula a trilha inteira.

## Ausências deliberadas

Não existe pontuação aqui. `atividade.xp` é devolvido na leitura porque a tela mostra o
valor, e somar ponto, sequência de dias e ranking pertence ao pacote `gamificacao`.

Nenhuma tentativa de quiz é recebida, corrigida ou guardada.

`atividade.duracao_segundos` não é conferida no registro de vídeo assistido.
