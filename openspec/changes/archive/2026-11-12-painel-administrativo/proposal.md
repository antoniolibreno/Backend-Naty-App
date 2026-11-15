# Painel administrativo

## Why

O painel é a fonte da verdade de quem existe, e o backend não oferece caminho para
cadastrar integrante, definir senha ou desativar alguém. A tabela `usuario` só tem o que o
seed de desenvolvimento insere, e produção não tem como ganhar o primeiro administrador.

O CRUD de empresa em `/api/v1/empresas` responde para qualquer token, inclusive o de um
integrante comum, e a listagem devolve todas as empresas. A entidade `Empresa` não gera
identificador nem preenche carimbo de data, não tem o fuso horário de que a sequência de
dias depende, e o conflito de exclusão sai fora do formato único de erro.

O papel `ADMIN` existe em `usuario.papel` e nenhuma rota o exige. Cadastro de empresa
atravessa empresas por natureza, e o administrador de uma empresa não pode enxergar
outra. Falta um papel que atravesse empresas e que só o time da Naty tenha.

## What Changes

Papel `NATY`, sem hierarquia sobre `ADMIN`. `NATY` administra empresas e as contas
`INTEGRANTE` e `ADMIN` de qualquer empresa, por rota própria. `ADMIN` administra as contas
`INTEGRANTE` da própria empresa, resolvida pela identidade da requisição. `NATY` nasce apenas por bootstrap em produção e pelo seed em
desenvolvimento, nunca por endpoint.

Pacote `painel`, único que escreve em `usuario` e em `empresa`, com as rotas sob
`/api/v1/painel/**`:

- `/api/v1/painel/empresas`: listagem paginada, detalhe, criação, alteração e exclusão de
  empresa com fuso horário. Exige `NATY`.
- `/api/v1/painel/integrantes`: cadastro, alteração, listagem paginada com busca,
  definição de senha e desativação dos integrantes da empresa de quem chama. Exige
  `ADMIN`.
- `/api/v1/painel/empresas/{empresaId}/integrantes`: as mesmas operações para qualquer
  empresa. Exige `NATY`.

Integrante nunca é apagado. Desativar revoga as sessões dele e preserva progresso.
Definir senha revoga as sessões dele. Empresa inativa bloqueia o login e as sessões
abertas dos integrantes dela.

Migration `V7` com `empresa.fuso_horario`, restrição de valores em `usuario.papel` e
índice em `usuario.empresa_id`. `Empresa` gera identificador e carimbos como as demais
entidades.

Negação de acesso sai em `ErroResposta` com código `ACESSO_NEGADO`. Conflito de negócio
sai em `ErroResposta` com código próprio. Listagem paginada usa os parâmetros `pagina` e
`tamanho` e devolve um corpo de página único para a API inteira.

Bootstrap do primeiro `NATY` por variável de ambiente. Produção recusa subir sem nenhum
`NATY` e sem as variáveis do bootstrap.

Fora de escopo: conteúdo de treinamento, progresso, gamificação, acompanhamento,
regras de papel do CRUD de conteúdo e renovação de sessão.

## Capabilities

### New Capabilities

- `cadastro-empresa`: cadastro de empresa pelo papel `NATY`, com fuso horário.
- `cadastro-integrante`: cadastro, senha, desativação e listagem de integrante, com
  isolamento por empresa para `ADMIN` e alcance global para `NATY`.

### Modified Capabilities

- `sessao-integrante`: a identidade carrega o papel, empresa inativa e integrante inativo
  são recusados no login e na sessão aberta, definição de senha e desativação revogam
  sessões, e a negação de acesso tem formato único.

## Impact

Nova migration `V7`. `V1` a `V6` não são tocadas. `db/seed-dev/R__seed_empresa_exemplo.sql`
ganha a empresa interna da Naty e a conta `NATY`.

`empresa` fica com a entidade, o repositório e o conversor de fuso. O controller, o
serviço, o mapper e os DTOs de empresa moram em `painel`, sob a rota nova. A rota
`/api/v1/empresas` não existe.

`usuario` ganha `NATY` em `Papel`, o papel em `IntegranteDaRequisicao`, a autoridade no
contexto de segurança, a recusa de empresa inativa e a revogação em lote. Os componentes
`usuarioId` e `empresaId` de `IntegranteDaRequisicao` são os mesmos, e `progresso` não é
tocado.

`config` ganha as regras de papel do painel e `NegacaoDeAcesso`. `shared` ganha
`ConflitoException` e `PaginaResponse`.

`IntegracaoTest.criarEmpresa` não preenche identificador. Com identificador gerado, salvar
entidade nova com identificador preenchido é tratado como `merge` e falha.

A numeração de migration a partir de `V8` e o caminho `/api/v1/integrantes/{id}/...`
pertencem ao MVP funcional.
