# Design

## Pacote `acompanhamento`

Pacote novo, folha do grafo de dependências: lê `usuario`, `empresa`, `trilha` e `progresso` e
nenhum pacote depende dele. Não escreve em nada. Os repositórios dele estendem `Repository`
(sem `save` nem `delete`) com consultas só de leitura sobre `ProgressoAtividade` e `Usuario`. Uma
regra ArchUnit reprova qualquer chamada de escrita de repositório ou setter de entidade de outro
pacote dentro de `acompanhamento`, e qualquer classe de fora que dependa dele.

## Segurança

`SecurityConfig` ganha `/api/v1/acompanhamento/**` com `hasRole(NATY)` antes de `anyRequest()`.
É o segundo ponto, depois do cadastro de empresas, que atravessa empresas.

## Cálculo único

Uma classe `SituacaoDoIntegrante` e um componente `CalculadoraDeAcompanhamento` produzem, para
uma empresa, a lista completa de situações. Listagem, métricas e CSV partem dela, então as
contagens das métricas coincidem com as situações da lista.

Entrada: os integrantes ativos de papel diferente de `NATY` da empresa, as atividades das
trilhas ativas em ordem global (ordem da trilha, do módulo e da atividade) e as conclusões
`(usuario, atividade, concluidoEm)` desses integrantes, em uma consulta por `usuario_id in`.
Para cada integrante, as atividades concluídas são as da lista de atividades ativas, e a atividade
atual é a primeira da lista fora desse conjunto. A atividade concluída de trilha inativa não
entra, como no resumo de progresso.

Dias sem concluir: `ChronoUnit.DAYS.between` entre a data da última conclusão e a data corrente,
ambas no fuso horário da empresa. Sem conclusão, a data de referência é a da criação da conta.
Parado é `dias >= app.acompanhamento.dias-parado`.

Ordem e paginação em memória: a lista da empresa é calculada inteira e então filtrada por
`situacao`, ordenada e fatiada. O custo cresce com o número de integrantes da empresa, que é
limitado, e evita uma consulta por integrante.

## Listagem, métricas e CSV

`AcompanhamentoService` expõe `listar(empresaId, situacao, pagina)`, `metricas(empresaId)` e
`todos(empresaId, situacao)` para o CSV. Empresa inexistente lança
`RecursoNaoEncontradoException`.

CSV: `CsvDeAcompanhamento` escreve RFC 4180 com vírgula, `\r\n`, cabeçalho e BOM UTF-8 para a
planilha abrir acentos. Célula de texto que começa com `=`, `+`, `-` ou `@` ganha apóstrofo no
início, e célula com vírgula, aspas ou quebra de linha vai entre aspas com as aspas duplicadas.
O arquivo é `integrantes-<empresaId>.csv`, nome derivado só do identificador para nunca carregar
texto do cliente. A exportação não é paginada por natureza, e o tamanho é o da empresa.

## Funil por atividade

Duas consultas agregadas: o total de integrantes considerados no escopo (todas as empresas ou a
informada) e o número de conclusões por atividade, agrupado por `atividade_id`, restrito a
integrantes ativos de papel diferente de `NATY` do escopo e a atividades de trilhas ativas. Como o
desbloqueio é linear, quem concluiu uma atividade concluiu a anterior, então chegaram da
atividade `k` de uma trilha é o total para `k = 1` e as conclusões da atividade `k - 1` nas demais.
Passaram são as conclusões da própria atividade.

## Índices

Migration com dois índices parciais:

- `progresso_atividade (atividade_id) where concluido_em is not null`, para o agrupamento do
  funil.
- `usuario (empresa_id) where ativo and papel <> 'NATY'`, para as contagens e a seleção dos
  integrantes considerados.

A consulta de conclusões por `usuario_id in` usa o índice único existente em
`progresso_atividade (usuario_id, atividade_id)`. Nenhuma consulta nova usa `tentativa_quiz`, então
nenhum índice dela é criado.

## Configuração

`app.acompanhamento.dias-parado` lê `APP_ACOMPANHAMENTO_DIAS_PARADO`, padrão 3. Não é parâmetro
de segurança, então produção não exige a variável.

## Decisões

- O papel `NATY` fica fora das contagens porque as contas da própria Naty vivem na empresa
  interna e não fazem o treinamento como cliente.
- `SEM_INICIO` existe separado de `PARADO` para conta recém-criada não aparecer como parada antes
  de ter tido tempo de começar.
- Concluído nunca é parado, porque não há o que cobrar.
- Chegaram é "atividade liberada", e não "tem registro de progresso", porque quem concluiu a
  anterior e não abriu a próxima é justamente quem trava.

## Riscos

- O cálculo em memória por empresa cresce com o número de integrantes. Se uma empresa passar de
  alguns milhares, a agregação sobe para SQL.
- A definição de parado vale igual para toda empresa. Prazo por empresa não faz parte do desenho.
