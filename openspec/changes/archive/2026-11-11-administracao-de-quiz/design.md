# Design

## Pacote `conteudo`

As rotas de quiz entram no pacote `conteudo` criado pela change `administracao-de-conteudo`,
com `QuizController`, `PerguntaController`, `AlternativaController` e `QuizAdminService`. O
serviço usa `AuditoriaDeConteudo`, a consulta de histórico e a proteção de remoção já
existentes, e as mesmas entidades e repositórios de `trilha`.

## Contratos separados da alternativa (9.5)

Três tipos distintos, nunca um com o campo anulado:

- `trilha/dto/AlternativaResponse(id, texto, ordem)`: leitura do integrante, sem `correta`,
  intacto.
- `conteudo/dto/AlternativaRequest(texto, correta)`: escrita.
- `conteudo/dto/AlternativaAdminResponse(id, texto, correta, ordem)`: leitura administrativa.

`PerguntaAdminResponse` e `QuizAdminResponse` agregam as alternativas administrativas. Só o
`QuizController` de `conteudo` os devolve. `RegrasDeDependenciaTest` verifica que nenhuma classe
fora de `conteudo` depende de `conteudo.dto`, e que `AlternativaResponse` não declara `correta`.

## Integridade do gabarito (9.6)

Duas camadas. Na entrada, `PerguntaRequest` valida com `@Size(min = 2)` nas alternativas e com
`@AssertTrue` que exatamente uma é correta, o que gera `FALHA_DE_VALIDACAO`. Nas transições,
`QuizAdminService` mantém o invariante sobre o estado gravado, com a pergunta travada por
`PESSIMISTIC_WRITE`:

- `POST` ou `PUT` de alternativa com `correta` verdadeiro desmarca a atual e marca a nova na
  mesma transação.
- `PUT` com `correta` falso na correta atual, `DELETE` da correta e `DELETE` que deixe menos de
  duas alternativas lançam `ConflitoException("GABARITO_INVALIDO", ...)`.
- `DELETE` da última pergunta lança `ConflitoException("QUIZ_SEM_PERGUNTA", ...)`, porque quiz
  sem pergunta quebra a nota percentual.

A trava na pergunta serializa duas escritas concorrentes do mesmo gabarito.

## Criação

`QuizRequest(atividadeId, notaMinima, perguntas)` cria o quiz inteiro em uma transação. A
`ordem` de perguntas e alternativas é a posição na lista, de 1 a n. Pergunta e alternativa
criadas depois entram no fim, como na administração de módulos. A ordem de pergunta e de
alternativa não tem rota de reordenação, porque o backlog pede reordenar módulo e atividade.

## Histórico

Remoção de quiz usa a consulta de histórico da atividade. Remoção de pergunta e de alternativa
consulta `resposta_tentativa` por nome de tabela, pela mesma razão de `trilha` e `conteudo` não
dependerem de `progresso`. Criar um quiz em atividade que integrantes já estudaram muda a regra
de conclusão dela para quem não a concluiu, e esse efeito é aceito e fica registrado em
`conteudo/CLAUDE.md`.

## Auditoria

Mover a marca de correta grava dois registros `ALTERACAO`, um em cada alternativa, para a
busca por `entidadeId` mostrar a troca de gabarito a partir de qualquer uma delas.

## Riscos

- A leitura administrativa é o único canal com `correta`. O teste de dependência e o teste de
  vazamento existentes cobrem o caminho do integrante.
- Editar o gabarito depois de haver tentativas não recalcula nota gravada. A nota guardada
  reflete o gabarito do momento da tentativa, e o registro de alterações explica a diferença.
