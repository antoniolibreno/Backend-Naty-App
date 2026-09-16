# Design: progresso na trilha

## Context

O conteúdo do treinamento e a leitura dele já existem: pacote `trilha` completo, seis
atividades semeadas por `V3` com UUID fixo, quiz por atividade e `POST /api/v1/sessoes`
resolvendo o integrante pelo e-mail. O que não existe é qualquer registro de quem
estudou o quê.

O app Flutter tem três telas prontas, Login, Home estilo Duolingo e assistir aula, e
nenhuma funciona de ponta a ponta. Esta etapa entrega o que essas três telas precisam e
para aí, de propósito.

O `docs/tasks.md` coloca a autenticação antes do progresso, porque pontuação construída
sobre identidade não verificada nasce fraudável. A decisão desta etapa é inverter essa
ordem conscientemente: as telas prontas passam a funcionar agora, e a etapa de
autenticação vem em seguida. A inversão só é aceitável porque nada de pontuação, streak
ou ranking entra aqui. O que fica fraudável é o progresso do próprio integrante, não uma
classificação entre pessoas.

## Goals / Non-Goals

**Goals:**

- A Home desenhar a trilha inteira com uma única chamada, cada nó com seu estado.
- O desbloqueio linear funcionar da primeira à última atividade do conteúdo semeado.
- A tela de aula registrar vídeo assistido e ver a próxima atividade abrir.
- A tela de Login mandar o corpo definitivo, com e-mail e senha.
- Quando a autenticação real entrar, nenhuma rota e nenhuma chamada do app mudarem.

**Non-Goals:**

- Tentativa, correção e nota de quiz.
- Pontuação, sequência de dias, conquistas e ranking.
- Hospedagem e reprodução de vídeo. `atividade.video_url` continua nulo.
- Autenticação, token, expiração e papel administrativo.
- Substituir o conteúdo de exemplo pelo verdadeiro.
- Acompanhamento pela Naty.

## Decisions

**O integrante viaja em cabeçalho, não na URL.** Sem autenticação, alguém precisa dizer
de quem é o progresso. Colocar `integranteId` na rota deixaria o Swagger mais explícito,
mas amarraria o formato da URL a uma limitação temporária: quando o token chegar, o id
na rota vira decorativo ou some, e o app reescreve todas as chamadas. Com o cabeçalho
`X-Integrante-Id` resolvido em um `HandlerMethodArgumentResolver`, a troca para
autenticação real muda um arquivo e nenhum controller. O resolvedor devolve
`IntegranteDaRequisicao`, com o identificador do integrante e o da empresa.

**O nome é `IntegranteDaRequisicao`, não `IntegranteAutenticado`.** É o integrante que a
requisição alega ser. Chamar de autenticado seria mentira enquanto o cabeçalho for
forjável, e mentira em nome de classe é pior que comentário errado, porque sobrevive à
revisão.

**Assistir o vídeo conclui a atividade, nesta etapa.** As seis atividades semeadas têm
quiz. Se a conclusão exigisse aprovação no quiz, e a tentativa de quiz está fora de
escopo, a trilha travaria no primeiro nó e a Home nunca desbloquearia nada: a entrega
seria inútil. Então `video-assistido` grava o momento do vídeo e o da conclusão. Quando
a tentativa de quiz entrar, a regra passa a exigir aprovação para atividade com quiz.
Isso fica escrito no `CLAUDE.md` do pacote, que é onde pendência mora.

**Estado é calculado, não guardado.** A tabela guarda fatos, quando o vídeo foi assistido
e quando a atividade foi concluída. Bloqueada, disponível e concluída são derivados da
sequência a cada leitura. Guardar o estado criaria uma segunda verdade que precisa ser
reescrita em todas as linhas seguintes a cada conclusão, e que fica errada em silêncio
quando o conteúdo for reordenado.

**`progresso` tem DTOs próprios, não estende os de `trilha`.** `trilha/CLAUDE.md` declara
que aquele pacote não sabe nada sobre quem estuda. Adicionar campo de estado a
`AtividadeResumoResponse` inverteria essa seta. A duplicação de título, descrição e ordem
nos DTOs de progresso é o preço da direção da dependência, e é deliberada.

**Os dois endpoints moram em `progresso`, mesmo com prefixo de `trilha`.** O caminho
`/api/v1/trilhas/{trilhaId}/progresso` começa com `trilhas`, mas a funcionalidade é
progresso e a convenção do projeto é pacote por funcionalidade. Colocá-los em
`TrilhaController` faria `trilha` depender de `progresso`.

**Nada de MapStruct aqui.** MapStruct serve para campo que se copia. Em progresso, o
estado de cada nó, o percentual e a próxima atividade são calculados a partir da
sequência inteira. A montagem fica em `TrilhaProgressoMontador`, separada do serviço para
que a regra não se perca no meio da construção de lista.

**A chave de unicidade é o par integrante e atividade.** Uma linha por par. Registrar
vídeo assistido duas vezes atualiza a mesma linha em vez de criar outra, e o momento da
conclusão não se move na segunda chamada.

**A tabela não tem `empresa_id`.** O isolamento vem do integrante, que já pertence a uma
empresa. Denormalizar a empresa aqui só se justifica quando o acompanhamento precisar
varrer progresso por empresa, e isso é outra etapa.

**A chave estrangeira para `usuario` apaga em cascata.** Não é conveniência: o seed de
desenvolvimento `R__seed_empresa_exemplo.sql` executa `delete from usuario` antes de
reinserir, e os testes de sessão criam e apagam integrantes. Sem a cascata, a restrição
bloqueia esses deletes e a aplicação para de subir em desenvolvimento no primeiro
progresso gravado.

**Senha exigida e descartada.** A tela de Login já tem os dois campos. Aceitar só o
e-mail obrigaria o app a mandar um corpo que muda de novo na etapa de autenticação.
Exigir a senha agora fixa o contrato. Não guardá-la, não compará-la e não registrá-la
evita o pior dos mundos, que é uma senha trafegando e sendo persistida sem hash por um
sistema que ainda não sabe autenticar.

**Atividade bloqueada devolve conflito, não erro de validação.** A requisição está bem
formada; o que a recusa é o estado da trilha para aquele integrante. Cada exceção nova
ganha tratamento explícito, porque `shared/CLAUDE.md` declara que a ausência de
tratamento genérico é proposital.

## Risks / Trade-offs

Cabeçalho de integrante forjável: qualquer um marca atividade como concluída em nome de
outro → aceito por ora, porque nesta etapa não há pontuação nem ranking a fraudar, e a
etapa de autenticação troca só o resolvedor.

Senha aceita sem verificação pode ser lida como implementada → mitigado registrando a
limitação na descrição da operação no OpenAPI, no spec, no `CLAUDE.md` do pacote e nas
dívidas conhecidas do `CLAUDE.md` raiz.

Contrato de `POST /api/v1/sessoes` quebra para quem já mandava só o e-mail → assumido, os
testes existentes são ajustados no mesmo diff e o único cliente é o app, que já tem os
dois campos na tela.

Regra de conclusão muda quando o quiz entrar, e quem tiver concluído por vídeo continua
concluído → aceito, é conteúdo de exemplo e a mudança de regra não revoga progresso
passado.

Leitura da trilha percorre módulos e atividades a cada chamada da Home → as coleções já
usam `@BatchSize`, o conteúdo tem seis atividades e o progresso do integrante vem em uma
consulta só. Paginação e materialização não se justificam neste tamanho.

A montagem depende da ordem de módulo e atividade, que tem índice único por par pai e
ordem → se o conteúdo for reordenado com atividades já concluídas, a sequência muda e um
nó concluído pode passar a suceder um não concluído. A leitura continua correta, porque
concluído tem precedência sobre bloqueado, mas a próxima atividade pode saltar. Aceito
enquanto não existir reordenação pela API.
