## Context

O repositório tem o esqueleto da etapa 1: seis pacotes com stubs, tabela `usuario`
mínima criada por `V1__baseline.sql`, `ddl-auto` em `validate` e Flyway ligado. Ver
`proposal.md` seção Why para a motivação.

Restrição central: entregar o modelo de conteúdo e a leitura dele sem tocar em
progresso do usuário e sem chamar a Naty API. O que existe de usuário nesta etapa
vem de seed, não de sincronização.

## Goals / Non-Goals

**Goals:**

- Modelo de conteúdo completo e migrado, com seed de exemplo reprodutível.
- Endpoints de leitura de trilha, atividade e quiz funcionando contra o seed.
- `Usuario` como entidade real, ligada a `Empresa`.
- Teste de integração com Testcontainers provando que o seed subiu e que o gabarito
  não vaza.

**Non-Goals:**

- Progresso, desbloqueio, tentativa de quiz, correção, pontuação, sequência de dias,
  conquistas e ranking.
- Qualquer chamada real à Naty API ou sincronização de usuário.
- CRUD de conteúdo pela API.
- Autenticação.
- Upload, hospedagem ou reprodução de vídeo.

## Decisions

**Conteúdo global, sem `empresa_id` nas tabelas de conteúdo.** Todas as empresas
fazem o mesmo treinamento sobre o mesmo produto. Alternativa considerada: `empresa_id`
nulo significando conteúdo compartilhado, com não nulo para conteúdo exclusivo. Foi
descartada porque não existe demanda de conteúdo por cliente hoje, e cada consulta
carregaria um filtro que sempre bate no mesmo valor. Quando surgir conteúdo
exclusivo, ele entra como tabela de associação entre empresa e trilha, sem quebrar o
que já existe.

**Seed de conteúdo com UUID fixo escrito na mão.** `gen_random_uuid()` no seed
produziria identificador diferente em cada ambiente, e nenhum teste conseguiria
chamar uma atividade sem descobrir o id antes. Com UUID fixo, o teste de integração e
a chamada manual usam o mesmo caminho em qualquer máquina.

**Seed de desenvolvimento separado do seed de conteúdo.** O conteúdo do treinamento é
dado de produção e mora em `db/migration`. A empresa fictícia e seus integrantes
existem só para conseguir exercitar o endpoint antes da integração com a Naty API, e
morariam em produção se ficassem no mesmo lugar. Eles vão para `db/seed-dev`, e
`spring.flyway.locations` no perfil `dev` aponta para as duas pastas enquanto o
perfil `prod` aponta só para `db/migration`. Alternativa considerada: `ddl-auto` com
`data.sql` do Spring, descartada porque conviveria mal com Flyway e não seria
versionada.

**Nova migration em vez de editar `V1__baseline.sql`.** Flyway não reexecuta arquivo
já aplicado e valida o checksum do que rodou. Editar `V1` quebraria qualquer banco
onde ele já passou, inclusive o de quem já rodou o compose. `V2` altera `usuario` e
cria o conteúdo.

**Índice único de `usuario` passa a ser o par `(empresa_id, naty_id)`.** O
identificador da Naty é único dentro de uma empresa, não entre empresas, porque o
token da Naty API é por empresa e nada garante unicidade global. Manter o índice em
`naty_id` sozinho faria a sincronização da segunda empresa colidir com a primeira. O
mesmo vale para o e-mail.

**Quiz devolvido sem gabarito, montado por DTO próprio.** O DTO de alternativa
simplesmente não tem o campo `correta`. Alternativa considerada: serializar a
entidade com anotação de ocultação, descartada porque uma anotação esquecida em um
campo novo vaza a resposta silenciosamente, enquanto um DTO sem o campo não tem como
vazar. A correção acontece no servidor, na etapa seguinte.

**`POST /api/v1/sessoes` como recurso, não como `GET /usuarios?email=`.** A operação
representa "quem está usando o app agora" e vai virar autenticação de verdade na
etapa que tiver login. Nascer como criação de sessão evita reescrever o contrato do
app Flutter depois. Ela não emite credencial nenhuma e é marcada como provisória no
OpenAPI.

**E-mail normalizado antes da busca.** Comparação em minúsculas e sem espaço nas
pontas, com índice funcional em `lower(email)` para a consulta não varrer a tabela.
Sem isso, quem digita o e-mail com a primeira letra maiúscula no celular não entra.

**MapStruct entra agora.** Existe mapeamento real de entidade para DTO, com aninhamento
de trilha, módulo e atividade. Escrever isso na mão seria repetição pura.

## Risks / Trade-offs

`lombok-mapstruct-binding` ausente quebra o build de forma confusa → Lombok e
MapStruct disputam a ordem dos annotation processors, e sem o binding o MapStruct gera
mapper que ignora os getters que o Lombok ainda não criou, produzindo campos nulos em
silencio ou erro de compilação obscuro. O `pom.xml` já declara
`annotationProcessorPaths` explícito, então o binding entra lá, na ordem correta, e o
teste de integração pega o caso do campo nulo.

`POST /api/v1/sessoes` aceita qualquer e-mail existente → qualquer pessoa que saiba o
e-mail de um colega passa a agir como ele. Aceito no primeiro corte, já registrado
como dívida no `CLAUDE.md` da raiz. O contrato já está desenhado para virar
autenticação sem mudar o caminho da URL.

Seed de exemplo pode ser confundido com conteúdo real → os títulos deixam explícito
que são exemplo, e o `CLAUDE.md` do pacote `trilha` diz em qual migration o conteúdo
verdadeiro entra e que ele apaga as linhas de exemplo pelos UUIDs fixos.

`V2` altera `usuario` para `empresa_id` não nulo → em uma tabela com linhas, isso
falharia. A tabela está vazia em todo ambiente conhecido, porque nada escreve nela
ainda. A migration assume tabela vazia e essa suposição fica escrita nela.

Trilha inteira aninhada em uma resposta só cresce com o conteúdo → com o volume de um
treinamento institucional, dezenas de atividades, o payload continua pequeno.
Paginação aqui seria complexidade sem problema para resolver. Se o conteúdo crescer
uma ordem de grandeza, o detalhe de trilha passa a devolver só os módulos e as
atividades viram uma chamada por módulo.
