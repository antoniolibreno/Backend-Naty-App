## Context

O repositorio tem o esqueleto da etapa 1: seis pacotes com stubs, tabela `usuario`
minima criada por `V1__baseline.sql`, `ddl-auto` em `validate` e Flyway ligado. Ver
`proposal.md` secao Why para a motivacao.

Restricao central: entregar o modelo de conteudo e a leitura dele sem tocar em
progresso do usuario e sem chamar a Naty API. O que existe de usuario nesta etapa
vem de seed, nao de sincronizacao.

## Goals / Non-Goals

**Goals:**

- Modelo de conteudo completo e migrado, com seed de exemplo reprodutivel.
- Endpoints de leitura de trilha, atividade e quiz funcionando contra o seed.
- `Usuario` como entidade real, ligada a `Empresa`.
- Teste de integracao com Testcontainers provando que o seed subiu e que o gabarito
  nao vaza.

**Non-Goals:**

- Progresso, desbloqueio, tentativa de quiz, correcao, pontuacao, sequencia de dias,
  conquistas e ranking.
- Qualquer chamada real a Naty API ou sincronizacao de usuario.
- CRUD de conteudo pela API.
- Autenticacao.
- Upload, hospedagem ou reproducao de video.

## Decisions

**Conteudo global, sem `empresa_id` nas tabelas de conteudo.** Todas as empresas
fazem o mesmo treinamento sobre o mesmo produto. Alternativa considerada: `empresa_id`
nulo significando conteudo compartilhado, com nao nulo para conteudo exclusivo. Foi
descartada porque nao existe demanda de conteudo por cliente hoje, e cada consulta
carregaria um filtro que sempre bate no mesmo valor. Quando surgir conteudo
exclusivo, ele entra como tabela de associacao entre empresa e trilha, sem quebrar o
que ja existe.

**Seed de conteudo com UUID fixo escrito na mao.** `gen_random_uuid()` no seed
produziria identificador diferente em cada ambiente, e nenhum teste conseguiria
chamar uma atividade sem descobrir o id antes. Com UUID fixo, o teste de integracao e
a chamada manual usam o mesmo caminho em qualquer maquina.

**Seed de desenvolvimento separado do seed de conteudo.** O conteudo do treinamento e
dado de producao e mora em `db/migration`. A empresa ficticia e seus integrantes
existem so para conseguir exercitar o endpoint antes da integracao com a Naty API, e
morariam em producao se ficassem no mesmo lugar. Eles vao para `db/seed-dev`, e
`spring.flyway.locations` no perfil `dev` aponta para as duas pastas enquanto o
perfil `prod` aponta so para `db/migration`. Alternativa considerada: `ddl-auto` com
`data.sql` do Spring, descartada porque conviveria mal com Flyway e nao seria
versionada.

**Nova migration em vez de editar `V1__baseline.sql`.** Flyway nao reexecuta arquivo
ja aplicado e valida o checksum do que rodou. Editar `V1` quebraria qualquer banco
onde ele ja passou, inclusive o de quem ja rodou o compose. `V2` altera `usuario` e
cria o conteudo.

**Indice unico de `usuario` passa a ser o par `(empresa_id, naty_id)`.** O
identificador da Naty e unico dentro de uma empresa, nao entre empresas, porque o
token da Naty API e por empresa e nada garante unicidade global. Manter o indice em
`naty_id` sozinho faria a sincronizacao da segunda empresa colidir com a primeira. O
mesmo vale para o e-mail.

**Quiz devolvido sem gabarito, montado por DTO proprio.** O DTO de alternativa
simplesmente nao tem o campo `correta`. Alternativa considerada: serializar a
entidade com anotacao de ocultacao, descartada porque uma anotacao esquecida em um
campo novo vaza a resposta silenciosamente, enquanto um DTO sem o campo nao tem como
vazar. A correcao acontece no servidor, na etapa seguinte.

**`POST /api/v1/sessoes` como recurso, nao como `GET /usuarios?email=`.** A operacao
representa "quem esta usando o app agora" e vai virar autenticacao de verdade na
etapa que tiver login. Nascer como criacao de sessao evita reescrever o contrato do
app Flutter depois. Ela nao emite credencial nenhuma e e marcada como provisoria no
OpenAPI.

**E-mail normalizado antes da busca.** Comparacao em minusculas e sem espaco nas
pontas, com indice funcional em `lower(email)` para a consulta nao varrer a tabela.
Sem isso, quem digita o e-mail com a primeira letra maiuscula no celular nao entra.

**MapStruct entra agora.** Existe mapeamento real de entidade para DTO, com aninhamento
de trilha, modulo e atividade. Escrever isso na mao seria repeticao pura.

## Risks / Trade-offs

`lombok-mapstruct-binding` ausente quebra o build de forma confusa → Lombok e
MapStruct disputam a ordem dos annotation processors, e sem o binding o MapStruct gera
mapper que ignora os getters que o Lombok ainda nao criou, produzindo campos nulos em
silencio ou erro de compilacao obscuro. O `pom.xml` ja declara
`annotationProcessorPaths` explicito, entao o binding entra la, na ordem correta, e o
teste de integracao pega o caso do campo nulo.

`POST /api/v1/sessoes` aceita qualquer e-mail existente → qualquer pessoa que saiba o
e-mail de um colega passa a agir como ele. Aceito no primeiro corte, ja registrado
como divida no `CLAUDE.md` da raiz. O contrato ja esta desenhado para virar
autenticacao sem mudar o caminho da URL.

Seed de exemplo pode ser confundido com conteudo real → os titulos deixam explicito
que sao exemplo, e o `CLAUDE.md` do pacote `trilha` diz em qual migration o conteudo
verdadeiro entra e que ele apaga as linhas de exemplo pelos UUIDs fixos.

`V2` altera `usuario` para `empresa_id` nao nulo → em uma tabela com linhas, isso
falharia. A tabela esta vazia em todo ambiente conhecido, porque nada escreve nela
ainda. A migration assume tabela vazia e essa suposicao fica escrita nela.

Trilha inteira aninhada em uma resposta so cresce com o conteudo → com o volume de um
treinamento institucional, dezenas de atividades, o payload continua pequeno.
Paginacao aqui seria complexidade sem problema para resolver. Se o conteudo crescer
uma ordem de grandeza, o detalhe de trilha passa a devolver so os modulos e as
atividades viram uma chamada por modulo.
