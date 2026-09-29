# Design: painel administrativo

## Context

A autenticação por token opaco em tabela está construída. `TokenSessaoFiltro` resolve a
sessão e põe `IntegranteDaRequisicao` no contexto de segurança, sem autoridade nenhuma.
`SecurityConfig` exige autenticação em tudo que não é público e não conhece papel.

`docs/regras.md` fixa que o painel é o único caminho que cadastra pessoa, que só o pacote
`painel` escreve em `usuario`, que integrante desativado mantém o histórico e que listagem
é paginada. O MVP funcional é construído por outra equipe em paralelo, e depende de três
entregas daqui: o fuso da empresa, o mecanismo de papel e o padrão de paginação.

## Goals / Non-Goals

**Goals:**

- Cadastro de empresa e de integrante pela API, sem nenhum estado aberto na internet.
- Administrador de uma empresa sem alcance nenhum sobre outra.
- Produção capaz de ganhar o primeiro administrador sem `POST` aberto.
- Contratos estáveis para o MVP funcional: fuso, papel, paginação e conflito.

**Non-Goals:**

- Regras de papel do CRUD de conteúdo, que usam o mecanismo daqui.
- Ranking, gamificação e acompanhamento.
- Renovação de sessão.
- Leitura de integrante pelo app em `/api/v1/usuarios`.

## Decisions

### Papel `NATY` acima de `ADMIN`

Cadastro de empresa atravessa empresas, e o administrador de uma empresa não enxerga
outra. Os dois cabem num papel só apenas se o isolamento for abandonado. `NATY` é o papel
do time da Naty: administra empresas e os integrantes de qualquer uma. `ADMIN` é o
administrador do cliente e só alcança a própria empresa.

`NATY` não nasce por endpoint. O corpo de cadastro aceita `INTEGRANTE` e `ADMIN`, e um
`NATY` pedido pelo cliente é erro de validação. Isso fecha a escalada de privilégio pela
própria API.

Todo usuário pertence a uma empresa, e `NATY` pertence à empresa interna da Naty, criada
pelo bootstrap. Tornar `usuario.empresa_id` opcional quebraria toda consulta que faz
`join fetch` na empresa.

### Rotas sob `/api/v1/painel/**`

A regra de papel é um matcher por prefixo em `SecurityConfig`, do mais específico para o
mais geral. O MVP funcional usa `/api/v1/integrantes/{integranteId}/progresso` e
`/gamificacao` para o próprio integrante, e um matcher administrativo sobre
`/api/v1/integrantes/**` bloquearia essas rotas.

### Dois controllers, um serviço

`ADMIN` tem a empresa resolvida pela identidade da requisição, nunca pela URL, como manda
`docs/regras.md`. `NATY` atravessa empresas por definição e recebe a empresa na URL, como
o acompanhamento. `IntegranteController` e `EmpresaIntegranteController` resolvem a
empresa e delegam ao mesmo `IntegranteService`, que só recebe empresa já resolvida.

Integrante de outra empresa responde 404 e não 403, para não confirmar que o identificador
existe.

### Controller de empresa em `painel`

Desativar empresa revoga as sessões dos integrantes dela, e a sessão mora em `usuario`,
que depende de `empresa`. Escrever empresa a partir de `empresa` criaria um ciclo entre os
pacotes. `painel` depende dos dois e nenhum depende dele.

### Recusa de inativo no login e na sessão aberta

A busca da sessão pelo hash já faz `join fetch` do usuário e da empresa. Conferir
`usuario.ativo` e `empresa.ativa` ali custa nenhuma consulta e faz a desativação valer na
chamada seguinte. A revogação em lote na desativação garante que reativar não ressuscita
token antigo.

### Proteção da própria conta

Ninguém altera o próprio papel nem se desativa, e `NATY` não desativa nem apaga a empresa
a que pertence. Sem isso o último administrador tranca todo mundo para fora.

### Senha

Mínimo de 8 caracteres e máximo de 72 bytes em UTF-8, que é o limite do bcrypt. O record
de entrada mascara a senha no `toString`, para ela não aparecer em log por acidente.

### Paginação

`Pageable` com `pagina` e `tamanho`, tamanho padrão 20 e máximo 100, configurado em
`spring.data.web.pageable`. A ordenação é fixa no servidor. O corpo é `PaginaResponse`,
record próprio, para o contrato não depender da serialização de `Page` do Spring Data.

### Bootstrap

`BootstrapNaty` roda na subida. Se existe algum `NATY`, não faz nada. Se não existe e as
variáveis estão preenchidas, cria a empresa interna e a conta. Se não existe, faltam as
variáveis e `app.bootstrap.obrigatorio` é verdadeiro, recusa subir. Só produção liga essa
propriedade.

## Risks / Trade-offs

- A unicidade global de e-mail faz o cadastro responder conflito para e-mail de outra
  empresa, e isso revela que o e-mail existe em algum lugar. É consequência do login sem
  escolha de empresa, e está aceita.
- A conta `NATY` do bootstrap guarda a senha da variável de ambiente. A variável pode sair
  do ambiente depois da primeira subida, porque o bootstrap não roda quando já existe
  `NATY`.
- `SecurityConfig`, `ApiExceptionHandler` e `IntegracaoTest` são tocados pelas duas
  equipes. Os contratos compartilhados entram em pull request próprio, antes do pacote
  `painel`.
